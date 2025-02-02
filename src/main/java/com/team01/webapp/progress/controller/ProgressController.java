package com.team01.webapp.progress.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.team01.webapp.alarm.service.IAlarmService;
import com.team01.webapp.home.service.IHomeService;
import com.team01.webapp.model.ChangeRequest;
import com.team01.webapp.model.HR;
import com.team01.webapp.model.Progress;
import com.team01.webapp.model.ProgressDetail;
import com.team01.webapp.model.ProgressFile;
import com.team01.webapp.model.ProgressFilter;
import com.team01.webapp.model.ProgressRate;
import com.team01.webapp.model.ProgressType;
import com.team01.webapp.model.SrFile;
import com.team01.webapp.model.SrProgressAjax;
import com.team01.webapp.model.SrProgressList;
import com.team01.webapp.model.SystemInfo;
import com.team01.webapp.model.Task;
import com.team01.webapp.model.ThArr;
import com.team01.webapp.model.Users;
import com.team01.webapp.progress.service.IProgressService;
import com.team01.webapp.util.AlarmInfo;
import com.team01.webapp.util.Pager;

import lombok.extern.log4j.Log4j2;

@Controller
@Log4j2
public class ProgressController {
	
	@Autowired
	private IProgressService progressService;
	
	@Autowired
	private IHomeService homeService;
	
	@Autowired
	private IAlarmService alarmService;
	
	@Autowired
	private AlarmInfo alarmInfo;
	
	/**
	 * 리스트 된 필터링 불러오기
	 * 
	 * @author					김태희
	 * @param pageNo			클라이언트가 보낸 페이지 번호 정보 저장
	 * @param progressfilter	클라이언트가 보낸 필터링 하기 위한 데이터 정보 저장
	 * @param session			HttpSession 객체 주입
	 * @param model				View로 데이터 전달을 위한 Model 객체 주입
	 * @return					progress/list 로 return
	 */
	@RequestMapping(value="/progress/list/{pageNo}", method = RequestMethod.GET)
	public String progressList(@PathVariable int pageNo, ProgressFilter progressfilter, HttpSession session, Model model) {
		
		progressfilter = progressService.filterList(progressfilter);
		
		model.addAttribute("progressFilter", progressfilter);
		
		// 알림 수 및 리스트
		alarmInfo.info(session, model);
		
		return "progress/list";
	}
	
	/**
	 * 진척 관리 상세 정보 조회
	 * 
	 * @author				김태희
	 * @param srNo			클라이언트가 보낸 SR 번호 정보 저장
	 * @param session		HttpSession 객체 주입
	 * @param model			View로 데이터 전달을 위한 Model 객체 주입
	 * @return				progress/detail 로 return
	 */
	@RequestMapping(value="/progress/detail/{srNo}", method = RequestMethod.GET)
	public String progressDetail(@PathVariable String srNo, HttpSession session, Model model) {
		
		ProgressDetail progressdetail = progressService.selectDetail(srNo);
		
		model.addAttribute("progressDetail", progressdetail);
		
		// 알림 수 및 리스트
		alarmInfo.info(session, model);
		
		return "progress/detail";
	}
	
	/**
	 * progress 리스트 불러오기
	 * 
	 * @author					김태희
	 * @param pageNo			클라이언트가 보낸 Page 번호 정보 저장
	 * @param srProgressAjax	클라이언트가 보낸 필터링 후 데이터 정보 저장
	 * @param session			HttpSession 객체 주입
	 * @param model				View로 데이터 전달을 위한 Model 객체 주입
	 * @param pager				클라이언트가 보낸 pager 데이터 정보 저장
	 * @return					progress/progressListView 로 return
	 */
	@RequestMapping(value="progress/list/progressajax/{pageNo}", produces="application/json; charset=UTF-8")
	public String progressAjax(@PathVariable String pageNo, @RequestBody SrProgressAjax srProgressAjax, HttpSession session, Model model, Pager pager) {
		List<SystemInfo> system = null;
		srProgressAjax.setAdminSysNo("");
		
		
		if(srProgressAjax.getChoice() == 2) {
			String userType = (String) session.getAttribute("userType");
			int userNo = (int) session.getAttribute("userNo");
			srProgressAjax.setUserType(userType);
			srProgressAjax.setUserNo(userNo);
			
			if(userType.equals("관리자")) {
				system = homeService.getSystemMiniViewDetail(userNo);
				srProgressAjax.setAdminSysNo(system.get(0).getSysNo());
			}
		}
		
		pager = progressService.returnPage(pageNo, pager, srProgressAjax);
		
		List<SrProgressList> list = progressService.ProgressList(pager, srProgressAjax);
		
		model.addAttribute("ProgressList", list);
		model.addAttribute("pager", pager);
		model.addAttribute("choice", srProgressAjax.getChoice());
		
		return "progress/progressListView";
	}
	
	/**
	 * 상세 뷰에서 파일 다운로드
	 * 
	 * @author				김태희
	 * @param srFileNo		클라이언트가 보낸 srFile 번호 정보 저장
	 * @param userAgent		User-Agent header 정보 저장
	 * @param response		HttpServletResponse 객체 주입
	 * @throws Exception	예외 처리
	 */
	@RequestMapping(value="progress/detail/filedownload", method = RequestMethod.GET)
	public void filedownload(int srFileNo, @RequestHeader("User-Agent") String userAgent, HttpServletResponse response) throws Exception {
		SrFile srFile = progressService.getSrFile(srFileNo);
		
		String originalName = srFile.getSrFileActlNm();
		String savedName = srFile.getSrFilePhysNm();
		String contentType = srFile.getSrFileExtnNm();
		
		// originalName이 한글이 포함되어 있을 경우, 브라우저별로 한글을 인코딩
		if(userAgent.contains("Trident") || userAgent.contains("MSIE")) {
			originalName = URLEncoder.encode(originalName, "UTF-8");
		} else {
			originalName = new String(originalName.getBytes("UTF-8"), "ISO-8859-1");
		}
		
		// 응답 헤더 설정
		response.setHeader("Content-Disposition", "attachment; filename=\"" + originalName + "\"");
		response.setContentType(contentType);
		
		// 응답 바디에 파일 데이터 싣기
		String filePath = "C:/Temp/uploadfiles/" + savedName;
		
		File file = new File(filePath);
		
		if(file.exists()) {
			InputStream is = new FileInputStream(file);
			OutputStream os = response.getOutputStream();
			FileCopyUtils.copy(is, os);
			os.flush();
			os.close();
			is.close();
		}
	}
	
	/**
	 * SR인적자원관리 리스트 불러오기
	 * 
	 * @author			김태희
	 * @param hr		클라이언트가 보낸 hr 정보 저장
	 * @param model		View로 데이터 전달을 위한 Model 객체 주입
	 * @return			progress/humanResourceList 로 return
	 */
	@RequestMapping(value="progress/detail/progressajax/1", produces="application/json; charset=UTF-8")
	public String humanResourceAjax(@RequestBody HR hr, Model model) {
		
		List<Task> taskList = progressService.taskList();
		model.addAttribute("taskList", taskList);
		
		String srNo = hr.getSrNo();
		model.addAttribute("srNo", srNo);
		
		List<HR> hrList = progressService.humanResourceList(srNo);
		model.addAttribute("hrList", hrList);
		
		List<HR> developerList = progressService.developerList(hrList.get(0).getUserDpNm(), srNo);
		model.addAttribute("developerList", developerList);
		
		ProgressDetail progressDetail = progressService.getSrSttsNm(srNo);
		model.addAttribute("sttsNm", progressDetail.getSttsNm());
		
		String managerNo = progressService.managerNo(srNo);
		model.addAttribute("managerNo", managerNo);
		
		Users userData = progressService.getSysUserData(progressDetail.getSysNo());
		model.addAttribute("userData", userData);
		
		return "progress/humanResourceList";
	}
	
	/**
	 * SR인적자원 추가
	 * 
	 * @author			김태희
	 * @param srNo		클라이언트가 보낸 sr 번호 정보 저장
	 * @param thArr		클라이언트가 보낸 thArr 정보 저장
	 * @return			progress/detail 로 리다이렉트
	 */
	@RequestMapping(value="progress/detail/developerinsert/{srNo}", produces="application/json; charset=UTF-8")
	public String developerinsert(@PathVariable String srNo, @RequestBody ThArr thArr) {
		
		progressService.developerInsert(thArr);
		
		return "redirect:/progress/detail/" + srNo;
	}
	
	/**
	 * SR인적자원 수정폼 불러오기
	 * 
	 * @author			김태희
	 * @param hr		클라이언트가 보낸 hr 정보 저장
	 * @param model		View로 데이터 전달을 위한 Model 객체 주입
	 * @return			progress/humanResourceUpdateView 로 return
	 */
	@RequestMapping(value="progress/detail/developerUpdateView", produces="application/json; charset=UTF-8")
	public String developerUpdateView(@RequestBody HR hr, Model model) {
		Date sysdate = new Date();
		
		HR developer = progressService.developer(hr.getSrNo(), hr.getUserNo());
		
		boolean startresult = developer.getHrStartDate().after(sysdate);
		boolean endresult = developer.getHrEndDate().after(sysdate);
		
		model.addAttribute("developer", developer);
		model.addAttribute("startresult", startresult);
		model.addAttribute("endresult", endresult);
		
		return "progress/humanResourceUpdateView";
	}
	
	/**
	 * SR인적자원 수정
	 * 
	 * @author		김태희
	 * @param hr	클라이언트가 보낸 hr 정보 저장
	 * @return		progress/detail 로 리다이렉트
	 */
	@RequestMapping(value="progress/detail/developerUpdate", produces="application/json; charset=UTF-8")
	public String developerUpdate(@RequestBody HR hr) {
		
		progressService.developerUpdate(hr);
		
		return "redirect:/progress/detail/" + hr.getSrNo();
	}
	
	/**
	 * SR인적자원 삭제
	 * 
	 * @author		김태희
	 * @param hr	클라이언트가 보낸 hr 정보 저장
	 * @return		progress/detail 로 리다이렉트
	 */
	@RequestMapping(value="progress/detail/developerDelete", produces="application/json; charset=UTF-8")
	public String developerDelete(@RequestBody HR hr) {

		progressService.developerDelete(hr.getSrNo(), hr.getUserNo());
		
		return "redirect:/progress/detail/" + hr.getSrNo();
	}
	
	/**
	 * SR 진척율 리스트 불러오기
	 * 
	 * @author			김태희
	 * @param hr		클라이언트가 보낸 hr 정보 저장
	 * @param model		View로 데이터 전달을 위한 Model 객체 주입
	 * @param session	HttpSession 객체 주입
	 * @return			progress/progressRateList 로 return
	 */
	@RequestMapping(value="progress/detail/progressajax/2", produces="application/json; charset=UTF-8")
	public String Progressrate(@RequestBody HR hr, Model model, HttpSession session) {
		int userNo = (int) session.getAttribute("userNo");
		
		List<Progress> progressRateList = progressService.progressRateList(hr.getSrNo());
		
		model.addAttribute("progressRateList", progressRateList);
		
		model.addAttribute("srNo", hr.getSrNo());
		
		ProgressDetail progressDetail = progressService.getSrSttsNm(hr.getSrNo());
		model.addAttribute("sttsNm", progressDetail.getSttsNm());
		
		String managerNo = progressService.managerNo(hr.getSrNo());
		model.addAttribute("managerNo", managerNo);
		
		List<Integer> humanList = progressService.humanList(hr.getSrNo());
		humanList.add(Integer.parseInt(managerNo));
		
		boolean check = humanList.contains(userNo);
		model.addAttribute("check", check);
		
		return "progress/progressRateList";
	}
	
	/**
	 * 진척율 추가 페이지로 이동
	 * 
	 * @author			김태희
	 * @param progNo	클라이언트가 보낸 progNo 정보 저장
	 * @param model		View로 데이터 전달을 위한 Model 객체 주입
	 * @return			progress/progressRateAdd 로 return
	 */
	@RequestMapping(value="progress/detail/progressRateAdd/{progNo}", method=RequestMethod.POST)
	public String ProgressRateAdd(@PathVariable String progNo, Model model) {
		
		Progress progress = progressService.progressRate(progNo);
		
		model.addAttribute("progress", progress);
		
		return "progress/progressRateAdd";
	}
	
	/**
	 * 진척율 추가(업데이트)
	 * 
	 * @author					김태희
	 * @param progress			클라이언트가 보낸 progress 정보 저장
	 * @param session			HttpSession 객체 주입
	 * @return					progress/detail/{srNo} 로 리다이렉트
	 * @throws IOException
	 */
	@RequestMapping(value="progress/detail/progressRate/update", method=RequestMethod.POST)
	public String ProgressRateUpdate(Progress progress, HttpSession session) throws IOException {
		// 첨부 파일이 있는지 확인
		List<MultipartFile> mfList = progress.getProgressattach();
		
		if(mfList != null && !mfList.isEmpty()) {
			for(int i=0; i<mfList.size(); i++) {
				// 파일의 원래 이름
				progress.setProgFileActlNm(mfList.get(i).getOriginalFilename());
				
				// 파일의 저장 이름
				String progFilePhysNm = new Date().getTime() + "-" + mfList.get(i).getOriginalFilename();
				progress.setProgFilePhysNm(progFilePhysNm);
				
				// 파일의 타입 설정
				progress.setProgFileExtnNm(mfList.get(i).getContentType());
				
				// 서버 파일 시스템에 파일로 저장
				String filePath = "C:/OTI/uploadfiles/" + progress.getSrNo() + "/" + progFilePhysNm;
				File dir = new File(filePath);
				
				// 폴더가 없다면 생성한다
				if(!dir.exists()) {
					try {
						Files.createDirectories(Paths.get(filePath));
						mfList.get(i).transferTo(dir);
					} catch (Exception e) {
						log.info("생성 실패 : " + filePath);
					}
				} else {
					mfList.get(i).transferTo(dir);
				}
				
				progressService.writeProgressRateFile(progress);
			}
		}
		session.setAttribute("message", 2);
		progressService.updateProgressRate(progress);
		
		return "redirect:/progress/detail/" + progress.getSrNo();
	}
	
	/**
	 * 진척율 일괄 추가
	 * 
	 * @author					김태희
	 * @param progressRate		클라이언트가 보낸 progressRate 정보 저장
	 * @param session			HttpSession 객체 주입			
	 * @return					progress/detail/{srNo} 로 리다이렉트
	 */
	@RequestMapping(value="progress/detail/progressRateAllAdd", produces="application/json; charset=UTF-8")
	public String ProgressRateAllAdd(@RequestBody ProgressRate progressRate, HttpSession session) {
		
		progressService.progressRateAllAdd(progressRate);
		
		session.setAttribute("message", 2);
		
		return "redirect:/progress/detail/" + progressRate.getSrNo();
	}
	
	/**
	 * 진척율 파일 다운로드
	 * 
	 * @author				김태희
	 * @param progFileNo	클라이언트가 보낸 progFileNo 정보 저장
	 * @param srNo			클라이언트가 보낸 srNo 정보 저장
	 * @param userAgent		@RequestHeader("user-Agent") 정보 저장
	 * @param response		HttpServletResponse 객체 주입
	 * @throws Exception
	 */
	@RequestMapping(value="progress/detail/progressFiledownload/{srNo}", method = RequestMethod.GET)
	public void progressFiledownload(String progFileNo, @PathVariable String srNo, @RequestHeader("User-Agent") String userAgent, HttpServletResponse response) throws Exception {
		ProgressFile progressFile = progressService.getProgressFile(progFileNo);
		
		String originalName = progressFile.getProgFileActlNm();
		String savedName = progressFile.getProgFilePhysNm();
		String contentType = progressFile.getProgFileExtnNm();
		
		// originalName이 한글이 포함되어 있을 경우, 브라우저별로 한글을 인코딩
		if(userAgent.contains("Trident") || userAgent.contains("MSIE")) {
			originalName = URLEncoder.encode(originalName, "UTF-8");
		} else {
			originalName = new String(originalName.getBytes("UTF-8"), "ISO-8859-1");
		}
		
		// 응답 헤더 설정
		response.setHeader("Content-Disposition", "attachment; filename=\"" + originalName + "\"");
		response.setContentType(contentType);
		
		// 응답 바디에 파일 데이터 싣기
		String filePath = "C:/OTI/uploadfiles/" + srNo + "/" + savedName;
		
		File file = new File(filePath);
		
		if(file.exists()) {
			InputStream is = new FileInputStream(file);
			OutputStream os = response.getOutputStream();
			FileCopyUtils.copy(is, os);
			os.flush();
			os.close();
			is.close();
		}
	}
	
	/**
	 * 개발 완료 신청 & 승인
	 * 
	 * @author					김태희
	 * @param progress			클라이언트가 보낸 progress 정보 저장
	 * @param session			HttpSession 객체 주입
	 * @return					progress/detail/{srNo} 로 리다이렉트
	 */
	@RequestMapping(value="progress/detail/progressRateFinishRequest", method=RequestMethod.POST)
	public String progressRateFinishRequest(@RequestBody Progress progress, HttpSession session) {
		
		String srNo = progress.getSrNo();
		String choice = progress.getChoice();
		
		progressService.progressRateFinishRequest(srNo, progress.getProgNoList(), choice);
		
		session.setAttribute("message", 2);

		//알람 DB에 저장
		session.setAttribute("choice", choice);
		alarmService.insertAlarm(srNo,session);
		
		return "redirect:/progress/detail/" + progress.getSrNo();
	}
	
	/**
	 * 산출물 파일 리스트 출력
	 * 
	 * @author			김태희
	 * @param hr		클라이언트가 보낸 hr 정보 저장
	 * @param model		View로 데이터 전달을 위한 Model 객체 주입
	 * @param session	HttpSession 객체 주입
	 * @return			progress/progressFileAdd 로 리턴
	 */
	@RequestMapping(value="progress/detail/progressajax/3", produces="application/json; charset=UTF-8")
	public String progresssFileList(@RequestBody HR hr, Model model, HttpSession session) {
		int userNo = (int) session.getAttribute("userNo");
		
		List<ProgressFile> progressFileList = progressService.progressfileList(hr.getSrNo());
		
		ProgressDetail progressDetail = progressService.getSrSttsNm(hr.getSrNo());
		
		model.addAttribute("sttsNm", progressDetail.getSttsNm());
		
		model.addAttribute("progressFileList", progressFileList);
		model.addAttribute("srNo", hr.getSrNo());
		
		String managerNo = progressService.managerNo(hr.getSrNo());
		model.addAttribute("managerNo", managerNo);
		
		List<Integer> humanList = progressService.humanList(hr.getSrNo());
		humanList.add(Integer.parseInt(managerNo));
		
		boolean check = humanList.contains(userNo);
		model.addAttribute("check", check);
		
		return "progress/progressFileList";
	}
	
	/**
	 *	산출물 추가 페이지 이동
	 * 
	 * @author			김태희
	 * @param srNo		클라이언트가 보낸 srNo 정보 저장
	 * @param model		View로 데이터 전달을 위한 Model 객체 주입
	 * @return			progress/progressFileAdd 로 리턴
	 */
	@RequestMapping(value="progress/detail/progressFileAdd/{srNo}", method=RequestMethod.GET)
	public String progressFileAdd(@PathVariable String srNo, Model model) {
		List<ProgressType> progressTypeList = progressService.getProgressTypeList();
		
		model.addAttribute("srNo", srNo);
		model.addAttribute("progressTypeList", progressTypeList);
		
		return "progress/progressFileAdd";
	}
	
	/**
	 * 산출물 추가
	 * 
	 * @author					김태희
	 * @param progress			클라이언트가 보낸 progress 정보 저장
	 * @param session			HttpSession 객체 주입
	 * @return					progress/detail/{srNo} 로 리다이렉트
	 * @throws IOException
	 */
	@RequestMapping(value="progress/detail/progressFile/add", method=RequestMethod.POST)
	public String progressFileAdd(Progress progress, HttpSession session) throws IOException {
		// 첨부 파일이 있는지 확인
		List<MultipartFile> mfList = progress.getProgressattach();
		
		Progress progNo = progressService.getProgNo(progress.getProgTypeNo(), progress.getSrNo());
		
		progress.setProgNo(progNo.getProgNo());
		
		if(mfList != null && !mfList.isEmpty()) {
			for(int i=0; i<mfList.size(); i++) {
				// 파일의 원래 이름
				progress.setProgFileActlNm(mfList.get(i).getOriginalFilename());
				
				// 파일의 저장 이름
				String progFilePhysNm = new Date().getTime() + "-" + mfList.get(i).getOriginalFilename();
				progress.setProgFilePhysNm(progFilePhysNm);
				
				// 파일의 타입 설정
				progress.setProgFileExtnNm(mfList.get(i).getContentType());
				
				// 서버 파일 시스템에 파일로 저장
				String filePath = "C:/OTI/uploadfiles/" + progress.getSrNo() + "/" + progFilePhysNm;
				File dir = new File(filePath);
				
				// 폴더가 없다면 생성한다
				if(!dir.exists()) {
					try {
						Files.createDirectories(Paths.get(filePath));
						mfList.get(i).transferTo(dir);
					} catch (Exception e) {
						log.info("생성 실패 : " + filePath);
					}
				} else {
					mfList.get(i).transferTo(dir);
				}
				
				progressService.writeProgressRateFile(progress);
			}
		}
		session.setAttribute("message", 3);
		
		return "redirect:/progress/detail/" + progress.getSrNo();
	}
	
	/**
	 * 산출물 삭제
	 * 
	 * @author				김태희
	 * @param progress		클라이언트가 보낸 progress 정보 저장
	 * @param session		HttpSession 객체 주입
	 * @return				progress/detail/{srNo} 로 리다이렉트
	 */
	@RequestMapping(value="progress/detail/progressFileRemove", produces="application/json; charset=UTF-8")
	public String progressFileRemove(@RequestBody Progress progress, HttpSession session) {
		
		for(int i=0; i<progress.getProgressFile().size(); i++) {
			String filePath = "C:/OTI/uploadfiles/" + progress.getSrNo() + "/" + progress.getProgressFile().get(i).getProgFilePhysNm();
			
			File file = new File(filePath);
			
			if(file.exists()) {
				if(file.delete()) {
					progressService.removeProgressFiles(progress.getProgressFile().get(i).getProgFileNo());
				} else {
					log.info("파일 삭제 실패");
				}
			}
		}
		
		session.setAttribute("message", 3);
		
		return "redirect:/progress/detail/" + progress.getSrNo();
	}
	
	/**
	 * 엑셀 다운로드
	 * 
	 * @author				김태희
	 * @param progressArr	클라이언트가 보낸 progressArr 정보 저장
	 * @param response		HttpServletResponse 객체 주입
	 * @throws IOException
	 */
	@RequestMapping(value="progress/list/excelDownload", method=RequestMethod.POST)
	public void excelDownload(@RequestParam List<String> progressArr, HttpServletResponse response) throws IOException {
		XSSFWorkbook wb=null;
		Sheet sheet=null;
		Row row=null;
		Cell cell=null; 
		wb = new XSSFWorkbook();
		sheet = wb.createSheet("freeBoard");
        
        String[] HeaderList = {"SR 번호", "시스템 구분", "업무 구분", "SR 명", "요청자", "완료 예정일", "진행 상태", "중요도"};
        
        //첫행   열 이름 표기 
        int cellCount=0;
        row = sheet.createRow(0);
        for(int i=0; i<HeaderList.length; i++) {
    		cell=row.createCell(cellCount++);
    		cell.setCellValue(HeaderList[i]);
        }
        
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy년 MM월 dd일");
		
		List<ProgressDetail> list = progressService.getProgressList(progressArr);
		
		for(int i=0; i<list.size(); i++) {
			row=sheet.createRow(i+1);
			cellCount = 0;
			cell=row.createCell(cellCount++);
			cell.setCellValue(list.get(i).getSrNo());
			cell=row.createCell(cellCount++);
			cell.setCellValue(list.get(i).getSysNm());
			cell=row.createCell(cellCount++);
			cell.setCellValue(list.get(i).getSrTypeNm());
			cell=row.createCell(cellCount++);
			cell.setCellValue(list.get(i).getSrTtl());
			cell=row.createCell(cellCount++);
			cell.setCellValue(list.get(i).getRequesterNm());
			cell=row.createCell(cellCount++);
			String SrDdlnDate = simpleDateFormat.format(list.get(i).getSrDdlnDate()); 
			cell.setCellValue(SrDdlnDate);
			cell=row.createCell(cellCount++);
			cell.setCellValue(list.get(i).getSttsNm());
			cell=row.createCell(cellCount++);
			cell.setCellValue(list.get(i).getSrPry());
		}
		
		// 컨텐츠 타입과 파일명 지정
		response.setContentType("ms-vnd/excel");
		response.setHeader("Content-Disposition", "attachment;filename=progressList.xlsx");  //파일이름지정.
		//response OutputStream에 엑셀 작성
		wb.write(response.getOutputStream());
		wb.close();
	}
	
	/**
	 * 계획조정 리스트 조회
	 * 
	 * @author					김태희
	 * @param hr				클라이언트가 보낸 hr 정보 저장
	 * @param model				View로 데이터 전달을 위한 Model 객체 주입
	 * @param session			HttpSession 객체 주입
	 * @return					progress/progressChangeRequest 로 리턴
	 */
	@RequestMapping(value="progress/detail/progressajax/4", produces="application/json; charset=UTF-8")
	public String progressChangeRequestList(@RequestBody HR hr, Model model, HttpSession session) {
		model.addAttribute("srNo", hr.getSrNo());
		
		List<ChangeRequest> changeRequestList = progressService.getChangeRequestList(hr.getSrNo());
		
		model.addAttribute("changeRequestList", changeRequestList);
		
		int userNo = (int) session.getAttribute("userNo");
		
		ProgressDetail progressDetail = progressService.getSrSttsNm(hr.getSrNo());
		model.addAttribute("sttsNm", progressDetail.getSttsNm());
		
		List<Integer> humanList = progressService.humanList(hr.getSrNo());
		
		boolean check = humanList.contains(userNo);
		model.addAttribute("check", check);
		
		return "progress/progressChangeRequestList";
	}
	
	/**
	 * 기간 추가 신청 페이지 이동
	 * 
	 * @author			김태희
	 * @param srNo		클라이언트가 보낸 srNo 정보 저장
	 * @return			progress/progressChangeRequest 로 리턴
	 */
	@RequestMapping(value="progress/detail/changeRequestList/{srNo}", method=RequestMethod.POST)
	public String changeRequest(@PathVariable String srNo) {
		
		return "progress/progressChangeRequest";
	}
	
	/**
	 * 기간 추가 신청
	 * 
	 * @author					김태희
	 * @param changeRequest		클라이언트가 보낸 changeRequest 정보 저장
	 * @param session			HttpSession 객체 주입
	 * @return					progress/detail/{srNo} 로 리다이렉트
	 */
	@RequestMapping(value="progress/detail/changeRequest", method=RequestMethod.POST)
	public String changeRequestWrite(ChangeRequest changeRequest, HttpSession session) {
		int userNo = (Integer) session.getAttribute("userNo");
		
		changeRequest.setUserNo(userNo);
		
		try {
			changeRequest.setCrTtl(Jsoup.clean(changeRequest.getCrTtl(), Whitelist.basic()));
			String content = changeRequest.getCrCn();
			content = content.replace("\r\n", "<br>");
			content = content.replace("\r", "<br>");
			content = content.replace("\n", "<br>");
			changeRequest.setCrCn(Jsoup.clean(content, Whitelist.basic()));
			
			MultipartFile mf = changeRequest.getChangeRequestFile();
			
			if(mf!=null &&!mf.isEmpty()) {
				// 파일 원래 이름 저장
				changeRequest.setCrFileActlNm(mf.getOriginalFilename());
				// 파일의 저장 이름 설정
				String crFilePhysNm = new Date().getTime()+"-"+mf.getOriginalFilename();
				changeRequest.setCrFilePhysNm(crFilePhysNm);
				// 파일 타입 설정
				String str = mf.getContentType();
				int beginIndex = str.indexOf("/");
				int endIndex = str.length();
				String type = str.substring(beginIndex,endIndex);
				changeRequest.setCrFileExtnNm(type);
				
				// 서버 파일 시스템에 파일로 저장
				String filePath = "C:/OTI/uploadfiles/ChangeRequest/" + changeRequest.getSrNo() + "/" + crFilePhysNm;
				File file = new File(filePath);
				// 폴더가 없다면 생성한다.
				if(!file.exists()) {
					try {
						Files.createDirectories(Paths.get(filePath));
						mf.transferTo(file);
					} catch (Exception e) {
						log.info("생성 실패 : " + filePath);
					}
				} else {
					mf.transferTo(file);
				}
			}
			progressService.changeRequest(changeRequest);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		alarmService.insertAlarm(changeRequest.getSrNo(),session);
		session.setAttribute("message", 4);
		
		return "redirect:/progress/detail/" + changeRequest.getSrNo();
	}
	
	/**
	 * 계획 조정 파일 다운로드
	 * 
	 * @author				김태희
	 * @param crNo			클라이언트가 보낸 crNo 정보 저장
	 * @param userAgent		User-Agent header 정보 저장
	 * @param response		HttpServletResponse 객체 주입
	 * @throws Exception
	 */
	@RequestMapping(value="progress/detail/ChangeRequestFileDownload/{crNo}")
	public void ChangeRequestFileDownload(@PathVariable int crNo, @RequestHeader("User-Agent") String userAgent, HttpServletResponse response) throws Exception {
		ChangeRequest changeRequest = progressService.getChangeRequestFile(crNo);
		
		String originalName = changeRequest.getCrFileActlNm();
		String savedName = changeRequest.getCrFilePhysNm();
		String contentType = changeRequest.getCrFileExtnNm();
		
		// originalName이 한글이 포함되어 있을 경우, 브라우저별로 한글을 인코딩
		if(userAgent.contains("Trident") || userAgent.contains("MSIE")) {
			originalName = URLEncoder.encode(originalName, "UTF-8");
		} else {
			originalName = new String(originalName.getBytes("UTF-8"), "ISO-8859-1");
		}
		
		// 응답 헤더 설정
		response.setHeader("Content-Disposition", "attachment; filename=\"" + originalName + "\"");
		response.setContentType(contentType);
		
		
		// 응답 바디에 파일 데이터 싣기
		String filePath = "C:/OTI/uploadfiles/ChangeRequest/" + changeRequest.getSrNo() + "/" + savedName;
		
		File file = new File(filePath);
		
		if(file.exists()) {
			InputStream is = new FileInputStream(file);
			OutputStream os = response.getOutputStream();
			FileCopyUtils.copy(is, os);
			os.flush();
			os.close();
			is.close();
		}
	}
	
	/**
	 * 계획 조정 상세 뷰
	 * 
	 * @author			김태희
	 * @param crNo		클라이언트가 보낸 crNo 정보 저장
	 * @param model		View로 데이터 전달을 위한 Model 객체 주입
	 * @return			progress/progressChangeRequestDetail 로 리턴
	 */
	@RequestMapping(value="progress/detail/changeRequestDetail/{crNo}", method=RequestMethod.POST)
	public String changeRequestDetail(@PathVariable int crNo, Model model) {
		ChangeRequest changeRequest = progressService.getChangeRequestFile(crNo);
		
		String[] list = changeRequest.getCrDdlnDate().split(" ");
		changeRequest.setCrDdlnDate(list[0]);
		
		model.addAttribute("changeRequest", changeRequest);
		
		String managerNo = progressService.managerNo(changeRequest.getSrNo());
		model.addAttribute("managerNo", managerNo);
		
		return "progress/progressChangeRequestDetail";
	}
	
	/**
	 * 계획 조정 확정
	 * 
	 * @author					김태희
	 * @param changeRequest		클라이언트가 보낸 changeRequest 정보 저장
	 * @param session			HttpSession 객체 주입
	 * @return					progress/detail/{srNo} 로 리다이렉트
	 */
	@RequestMapping(value="progress/detail/srChangeRequest", produces="application/json; charset=UTF-8")
	public String progressRateFinishRequest(@RequestBody ChangeRequest changeRequest, HttpSession session) {
		
		progressService.changeRequestUpdate(changeRequest);
		
		session.setAttribute("message", 4);
		
		return "redirect:/progress/detail/" + changeRequest.getSrNo();
	}
	
}
