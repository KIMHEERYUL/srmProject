<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="bg-primary px-3 py-2" style="border-top-left-radius:10px; border-top-right-radius:10px; width:121px;">
 	<h6 class="mb-0 text-white">SR 요청관리</h6>
</div>
<div class="card mb-4 ">
	<!-- 검색 -->
	<div class="mb-1 mt-5 px-5">
		<form class="navbar-search">
	     	<div class="row">
	     		<div class="col-4">
	     			<div class="form-group row" id="simple-date4" >
		        		<div class="input-daterange input-group input-group-sm">
			        		<label for="startDate" class="col-sm-4 col-form-label-sm">조회기간</label>
			            	<input type="text" class="input-sm form-control form-control-sm col-sm-8" name="start" id="startDate"/>
			            	<div class="input-group-prepend">
			            		<span class="input-group-text" style="height:31px;">~</span>
			            	</div>
			            	<input type="text" class="input-sm form-control form-control-sm" name="end" />
			            </div>
		    		</div>
	     		</div>
	     		<div class="col-3">
	     			<div class="form-group row">
	     				<label class="col-sm-4 col-form-label-sm" for="srStts">진행상태</label>
			           	<select class="form-control form-control-sm col-sm-8" id="srStts">
			               	<option selected>전체</option>
			               	<option>요청</option>
			               	<option>검토중</option>
			               	<option>접수</option>
			               	<option>개발중</option>
			               	<option>개발완료</option>
			               	<option>재검토</option>
			           	</select>
			       	</div>
	     		</div>
	     		<div class="col-4">
	     			<div class="form-group row">
	     				<label for="srSystem" class="col-sm-4 col-form-label-sm">관련시스템</label>
			           	<select class="form-control form-control-sm col-sm-8" id="srSystem" >
			               	<option selected>전체</option>
			               	<option>KHR시스템</option>
			               	<option>KTH시스템</option>
			               	<option>JHJ시스템 </option>
			               	<option>HGH시스템 </option>
			           	</select>
			       	</div>
	     		</div>
	     		<div class="col-1">
	     		</div>
	     	</div>
	     	<div class="row">
	     		<div class="col-4">
	     			<div class="form-group row">
	     				<label for="srOgdp" class="col-sm-4 col-form-label-sm">등록자 소속</label>
			           	<select class="form-control form-control-sm col-sm-8" id="srOgdp">
			               	<option selected>전체</option>
			               	<option>KHR시스템</option>
			               	<option>KTH시스템</option>
			               	<option>JHJ시스템</option>
			               	<option>HGH시스템</option>
			           	</select>
	       			</div>
	     		</div>
	     		<div class="col-3">
	     			<div class="form-group row">
	     			<label for="srDept" class="col-sm-4 col-form-label-sm">개발부서</label>
			           	<select class="form-control form-control-sm col-sm-8" id="srDept">
			               	<option selected>전체</option>
			               	<option>한국소프트웨어 개발1팀</option>
			               	<option>한국소프트웨어 개발2팀</option>
			               	<option>서강소프트웨어 개발1팀</option>
			               	<option>미래소프트웨어 개발1팀</option>
			           	</select>
	       			</div>
     			</div>
	     		<div class="col-4">
			       	<div class="form-group row">
			       		<label for="srKeyWord" class="col-sm-3 col-form-label-sm pr-3">키워드</label>
			           	<input type="text" id="srKeyWord" class="form-control form-control-sm col-sm-9 bg-light" 
			          		aria-label="Search" placeholder="검색어를 입력하세요" style="border-color: #3f51b5;">
			        </div>
	        	</div>
        		<div class="col-1">
        			<div class="input-group-append float-right">
						<button class="btn btn-primary btn-sm" type="button" onclick="progressList()">
							조회<i class="fas fa-search fa-sm"></i>
						</button>
					</div>
         		</div>
	         	</div>
	         </form>
           </div>
           <hr/>
	<!-- SR 검토 목록 -->
	<div class="card-header py-3 d-flex flex-row align-items-center justify-content-between">
		<h6 class="m-0 font-weight-bold text-primary">SR 요청 목록</h6>
		<div class="d-sm-flex justify-content-end">
			<a class="btn btn-sm btn-secondary mr-1"
				href="<c:url value='/request/write/'/>"> 요청등록 </a>
			<button class="btn btn-sm btn-secondary ">엑셀 다운로드</button>
		</div>
	</div>
     <div class="table-responsive p-3">
       <table class="table align-items-center table-flush table-hover border" id="dataTableHover">
         <thead class="thead-light">
           <tr>
             <th class="pr-0">
              <div class="custom-control custom-checkbox">
         			<input type="checkbox" class="custom-control-input" id="customCheck1">
         			<label class="custom-control-label" for="customCheck1"></label>
       		</div>
             </th>
             <th>요청번호 </th>
             <th>제목 </th>
             <th>관련시스템 </th>
             <th>상태 </th>
             <th>등록일 </th>
             <th>완료예정일 </th>
             <th>중요도 </th>
           </tr>
         </thead>
         
         <tbody>
         <tr>
         	<td class="pr-0">
              <div class="custom-control custom-checkbox">
         			<input type="checkbox" class="custom-control-input" id="customCheck5">
         			<label class="custom-control-label" for="customCheck5"></label>
       		</div>
             </td>
             <td>JHJ-SR-0001</td>
             <td><button  onclick="getDetail(1)"  class="requsetTtl">댓글 기능 추가ddddddddddddddddddd </button></td>
             <td>JHJ쇼핑몰</td>
             <td><span  class="badge badge-danger" style="font-size:100%">미검토</span></td>
             <td>2023/02/09 </td>
             <td>2023/04/08 </td>
             <td><span  class="badge badge-danger" style="font-size:100%">상</span></td>
           </tr>
           <tr>
           	<td class="pr-0">
              <div class="custom-control custom-checkbox">
         			<input type="checkbox" class="custom-control-input" id="customCheck2">
         			<label class="custom-control-label" for="customCheck2"></label>
       		</div>
             </td>
             <td>JHJ-SR-0002</td>
             <td>게시 기능 추가 부탁드립니다.</td>
             <td>JHJ쇼핑몰</td>
             <td><span  class="badge badge-danger" style="font-size:100%">미검토</span></td>
             <td>2023/02/09 </td>
             <td>2023/04/08 </td>
             <td><span  class="badge badge-secondary" style="font-size:100%">하</span></td>
           </tr>
           <tr>
           	<td class="pr-0">
              <div class="custom-control custom-checkbox">
         			<input type="checkbox" class="custom-control-input" id="customCheck3">
         			<label class="custom-control-label" for="customCheck3"></label>
       		</div>
             </td>
             <td>JHJ-SR-0003</td>
             <td >알림 기능 추가 부탁드립니다.</td>
             <td>JHJ쇼핑몰</td>
             <td><span  class="badge badge-danger" style="font-size:100%">미검토</span></td>
             <td>2023/02/01 </td>
             <td>2023/04/11 </td>
             <td><span  class="badge badge-secondary" style="font-size:100%">하</span></td>
           </tr>
           <tr>
           	<td class="pr-0">
              <div class="custom-control custom-checkbox">
         			<input type="checkbox" class="custom-control-input" id="customCheck4">
         			<label class="custom-control-label" for="customCheck4"></label>
       		</div>
             </td>
             <td>JHJ-SR-0004</td>
             <td>댓글/검색 기능 보완 부탁드립니다.</td>
             <td>JHJ쇼핑몰</td>
             <td><span class="badge badge-primary" style="font-size:100%" >개발중</span></td>
             <td>2023/02/09 </td>
             <td>2023/04/08 </td>
             <td><span  class="badge badge-primary" style="font-size:100%">중</span></td>
           </tr>
         </tbody>
       </table>
		<div class="pager d-flex justify-content-center my-4">
			<div class="pagingButtonSet d-flex justify-content-center">
				<c:if test="${pager.pageNo > 1}">
					<a onclick="requestList(1)" type="button" class="btn btn-outline-primary btn-sm m-1">처음</a>
				</c:if>
				<c:if test="${pager.groupNo > 1}">
					<a onclick="requestList(${pager.startPageNo-1})" type="button" class="btn btn-outline-info btn-sm m-1">이전</a>
				</c:if>
		
				<c:forEach var="i" begin="${pager.startPageNo}" end="${pager.endPageNo}">
					<c:if test="${pager.pageNo != i}">
						<a onclick="requestList(${i})" type="button" class="btn btn-outline-success btn-sm m-1">${i}</a>
					</c:if>
					<c:if test="${pager.pageNo == i}">
						<a onclick="requestList(${i})" type="button" class="btn btn-primary btn-sm m-1 text-white">${i}</a>
					</c:if>
				</c:forEach>
		
				<c:if test="${pager.groupNo < pager.totalGroupNo }">
					<a onclick="requestList(${pager.endPageNo+1})" type="button" class="btn btn-outline-info btn-sm m-1">다음</a>
		
				</c:if>
				<c:if test="${pager.pageNo < pager.totalPageNo }">
					<a onclick="requestList(${pager.totalPageNo})" type="button" class="btn btn-outline-primary btn-sm m-1">맨끝</a>
				</c:if>
			</div>
		</div>
  	</div>
</div>
<script>
function getDetail(i){
	$.ajax({
		url : "/myapp/request/detail/sr/"+i,
		type : "GET",
		datatype : "html",
		success : function(data) {
			$('#detailView').html(data);
			console.log(data);
		}
	});
};
</script>