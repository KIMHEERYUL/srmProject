package com.team01.webapp.qnaboard.dao;

import java.util.List;

import com.team01.webapp.model.QSTN;
import com.team01.webapp.model.QSTNComment;
import com.team01.webapp.util.Pager;

public interface IQnaboardRepository {
	public List<QSTN> selectQstnList(QSTN qstn);
	public int totalRow(QSTN qstn);
	public Pager returnPager(int pageNo, Pager pager, QSTN qstn);
	public QSTN selectDetail(int qstnNo);
	
	public int insertQSTN(QSTN qstn);
	public void	insertComment(QSTNComment qComment);
	
	public int countComment(int qstnNo);
	
	public List<QSTNComment> selectCommentList(int qstnNo);
	public QSTNComment selectComment();
	
	public void updateComment(QSTNComment qComment);
	public void deleteComment(int qstnCmntNo);
	public void insertQSTN(QSTN qstn);
	

	
}
