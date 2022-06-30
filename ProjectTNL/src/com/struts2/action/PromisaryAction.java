package com.struts2.action;

import java.util.List;

import com.opensymphony.xwork2.ActionSupport;
import com.struts2.model.PromisaryBean;

public class PromisaryAction extends ActionSupport {
	private PromisaryManager promisaryManager;
	private PromisaryBean promisaryBean;
	private List<PromisaryBean> promissaryList;
	private int prmssryId;
	public PromisaryAction(){
		promisaryManager = new PromisaryManager();
		promissaryList = promisaryManager.getPromisaryDet();

		
	}
	public String execute() {
		promissaryList = promisaryManager.getPromisaryDet();
		System.out.println("execute called");
		return "success";
	}

	public String addPrmsryDet() {
			promisaryManager.add(getPromisaryBean());
			System.out.println("member added");
			promissaryList = promisaryManager.getPromisaryDet();
		    return "success";
	}
	public String deletePrmsryDet(){
		try{
			promisaryManager.delete(prmssryId);
		}catch(Exception e){
			e.printStackTrace();
		}
		promissaryList = promisaryManager.getPromisaryDet();
		return "success";
	}
	public int getPrmssryId() {
		return prmssryId;
	}

	public void setPrmssryId(int prmssryId) {
		this.prmssryId = prmssryId;
	}

	public PromisaryManager getPromissaryManager() {
		return promisaryManager;
	}

	public void setPromissaryManager(PromisaryManager promissaryManager) {
		this.promisaryManager = promissaryManager;
	}

	public PromisaryBean getPromisaryBean() {
		return promisaryBean;
	}

	public void setPromisaryBean(PromisaryBean promisaryBean) {
		this.promisaryBean = promisaryBean;
	}

	public List<PromisaryBean> getPromissaryList() {
		return promissaryList;
	}

	public void setPromissaryList(List<PromisaryBean> promissaryList) {
		this.promissaryList = promissaryList;
	}

}
