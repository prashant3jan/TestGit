package com.struts2.action;

import java.util.List;

import com.opensymphony.xwork2.ActionSupport;
import com.struts2.model.PndngInstlmntRprtBean;
import com.struts2.model.PromisaryBean;

public class PndngInstlmntRprtAction extends ActionSupport {
	private PndngInstlmntRprtManager pndngInstlmntRprtManager;
	private List<PndngInstlmntRprtBean> pndngInstlmntRprtList;
	public PndngInstlmntRprtAction(){
		pndngInstlmntRprtManager = new PndngInstlmntRprtManager();
		
	}
	public String execute() {
		setPndngInstlmntRprtList(pndngInstlmntRprtManager.getPendingInstlmntRprt());
		System.out.println("execute called");
		return "success";
	}
	public List<PndngInstlmntRprtBean> getPndngInstlmntRprtList() {
		return pndngInstlmntRprtList;
	}
	public void setPndngInstlmntRprtList(List<PndngInstlmntRprtBean> pndngInstlmntRprtList) {
		this.pndngInstlmntRprtList = pndngInstlmntRprtList;
	}

}
