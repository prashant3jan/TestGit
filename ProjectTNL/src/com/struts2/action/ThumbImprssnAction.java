package com.struts2.action;

import java.util.List;

import com.opensymphony.xwork2.ActionSupport;
import com.struts2.model.ThumbImprssnBean;

public class ThumbImprssnAction extends ActionSupport  {
	private ThumbImprssnManager thumbImprssnManager;
	private ThumbImprssnBean thumbImprssnBean;
	private List<ThumbImprssnBean> thumbInprssnList;
	private int thumbImprssnId;
	public ThumbImprssnAction(){
		thumbImprssnManager = new ThumbImprssnManager();
		setThumbInprssnList(thumbImprssnManager.getThumbImprssnDet());	
	}
	public String execute() {
		setThumbInprssnList(thumbImprssnManager.getThumbImprssnDet());
		System.out.println("execute called");
		return "success";
	}
	public String addThumbImprssnDet() {
		thumbImprssnManager.add(getThumbImprssnBean());
		System.out.println("member added");
		setThumbInprssnList(thumbImprssnManager.getThumbImprssnDet());
		    return "success";
	}
	public String deleteThumbImprssnDet(){
		try{
			thumbImprssnManager.delete(thumbImprssnId);
		}catch(Exception e){
			e.printStackTrace();
		}
		thumbInprssnList = thumbImprssnManager.getThumbImprssnDet();
		return "success";
	}
	public ThumbImprssnBean getThumbImprssnBean() {
		return thumbImprssnBean;
	}
	public void setThumbImprssnBean(ThumbImprssnBean thumbImprssnBean) {
		this.thumbImprssnBean = thumbImprssnBean;
	}
	public List<ThumbImprssnBean> getThumbInprssnList() {
		return thumbInprssnList;
	}
	public void setThumbInprssnList(List<ThumbImprssnBean> thumbInprssnList) {
		this.thumbInprssnList = thumbInprssnList;
	}
	public int getThumbImprssnId() {
		return thumbImprssnId;
	}
	public void setThumbImprssnId(int thumbImprssnId) {
		this.thumbImprssnId = thumbImprssnId;
	}
	public ThumbImprssnManager getThumbImprssnManager() {
		return thumbImprssnManager;
	}
	public void setThumbImprssnManager(ThumbImprssnManager thumbImprssnManager) {
		this.thumbImprssnManager = thumbImprssnManager;
	}
}
