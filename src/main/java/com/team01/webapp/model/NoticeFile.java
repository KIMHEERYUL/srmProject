package com.team01.webapp.model;

import lombok.Data;

@Data
public class NoticeFile {

	private int ntcFileNo;
	private int ntcNo;
	private String ntcFilePhysNm; //바꾼 파일 명
	private String ntcFileActlNm; //원본 파일 이름
	private String ntcFileExtnNm; //파일 확장명
	
	
}
