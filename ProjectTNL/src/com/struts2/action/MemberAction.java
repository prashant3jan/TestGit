package com.struts2.action;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionSupport;
import com.struts2.model.Document;
import com.struts2.model.Member;
import com.struts2.model.MemberBankInfo;
import com.struts2.model.MemberDocs;

public class MemberAction extends ActionSupport {
	private static final long serialVersionUID = 2L;
	private Member memberBean;
	private MemberBankInfo memberBankBean;
	private String memNum;
	public String getMemNum() {
		return memNum;
	}

	public void setMemNum(String memNum) {
		this.memNum = memNum;
	}

	private List<Member> memberList;
	private List<MemberBankInfo> memberBankInfoList;
	private MemberManager memberManager;
	private List<Document> documentList;
	private List<MemberDocs> documents;
	

	public MemberAction(){
		memberManager = new MemberManager();
		documentList = memberManager.listDocs();
	}
	
	public String execute(){
		this.memberList = memberManager.memberList();
		System.out.println("execute called");
		return "success";
	}
	
	public String dispMemInputForm(){
		this.memberBankInfoList = memberManager.memberBankInfoList();
		return "success";
	}
	
	public String dispMemDetIndiv(){
		System.out.println("dispMemDetIndiv called");
		return "success";
	}
	
	public String dispMemberDetByMemNo(){
		System.out.println("memNum"+memberBean.getMemNum());
		memNum= memberBean.getMemNum();
		memberBean = memberManager.getMemDetByMemNo(memNum);
		return "success";
	}
	
	public String addMemDet(){
		try{
			System.out.println("inside addMemDet");
	        //Member member = this.getMemberBean();
	        System.out.println("mem_no"+getMemberBean().getMemNum());
	        try{
	        File fileMemImage = getMemberBean().getMemImage();
	        byte[] bMemImage = new byte[(int) fileMemImage.length()];
	        FileInputStream fileInputStream = new FileInputStream(fileMemImage);
	         //convert file into array of bytes
	         fileInputStream.read(bMemImage);
	         fileInputStream.close();
	         getMemberBean().setMemImageData(bMemImage);
	        }catch(NullPointerException e){
	        	e.printStackTrace();
	            System.out.print("NullPointerException Caught for member image"); 
	        } 
	        //set the array in bean
	        //code repeat for adding memSign into bean
	        try{
			File fileAuthSigntry = getMemberBean().getAuthSigntry();
	        byte[] bAuthSigntry = new byte[(int) fileAuthSigntry.length()];
	         FileInputStream fileInputStream = new FileInputStream(fileAuthSigntry);
	         //convert file into array of bytes
	         fileInputStream.read(bAuthSigntry);
	         fileInputStream.close();
	         getMemberBean().setAuthSigntryData(bAuthSigntry);
	        }catch(NullPointerException e){
	        	e.printStackTrace();
	            System.out.print("NullPointerException Caught for AuthSigntry"); 
	        } 
	        //set the array in bean
			//end code repeat for adding memSign into bean
			//code repeat for adding memSign into bean
	        try{
			File fileMemSign = getMemberBean().getMemSign();
	        byte[] bMemSign = new byte[(int) fileMemSign.length()];
	         FileInputStream fileInputStream = new FileInputStream(fileMemSign);
	         //convert file into array of bytes
	         fileInputStream.read(bMemSign);
	         fileInputStream.close();
	         getMemberBean().setMemSignData(bMemSign);
	        }catch(NullPointerException e){
	        	e.printStackTrace();
	            System.out.print("NullPointerException Caught for member sign"); 
	        } 
	        //set the array in bean
			//end code repeat for adding memSign into bean
			
			//code repeat for adding grntrSign into bean
	        try{
			File fileGrntrSign = getMemberBean().getGrntrSign();
	        byte[] bGrntrSign = new byte[(int) fileGrntrSign.length()];
	         FileInputStream fileInputStream = new FileInputStream(fileGrntrSign);
	         //convert file into array of bytes
	         fileInputStream.read(bGrntrSign);
	         fileInputStream.close();
	         getMemberBean().setGrntrSignData(bGrntrSign);
	        }catch(NullPointerException e){
	        	e.printStackTrace();
	            System.out.print("NullPointerException Caught for grntr sign"); 
	        } 
			//end code repeat for adding grntrSign into bean
			
			//code repeat for adding empSign into bean
	        try{
			File fileEmpSign = getMemberBean().getEmpSign();
	        byte[] bEmpSign = new byte[(int) fileEmpSign.length()];
	         FileInputStream fileInputStream = new FileInputStream(fileEmpSign);
	         //convert file into array of bytes
	         fileInputStream.read(bEmpSign);
	         fileInputStream.close();
	         getMemberBean().setEmpSignData(bEmpSign);
	        }catch(NullPointerException e){
	        	e.printStackTrace();
	            System.out.print("NullPointerException Caught for emp sign"); 
	        } 
	        //set the array in bean
	        //end code repeat for adding grntrSign into bean
			memberManager.add(getMemberBean());
			//add member data to the database
			System.out.println("member added");
			}catch(Exception e){
				e.printStackTrace();
			}
			this.memberList = memberManager.memberList();
			return "success";
		}
	
	public String addMemBankDet(){
		try{
		memberManager.addMemBankDet(this.getMemberBankBean());
		System.out.println("memebr details added");
		}catch(Exception e){
		e.printStackTrace();
		}
		this.memberBankInfoList = memberManager.memberBankInfoList();
		return "success";
	}
	
	public String delete(){
		try{
		memberManager.delete(memNum);
		}catch(Exception e){
			e.printStackTrace();
		}
		this.memberList = memberManager.memberList();
		return "success";
	}
	
	public String deleteMemBnkInfo(){
		try{
		memberManager.deleteMemBnkInfo(memNum);
		}catch(Exception e){
			e.printStackTrace();
		}
		this.setMemberBankInfoList(memberManager.memberBankInfoList());
		return "success";
		
	}
	
	public List<MemberDocs> getDocuments() {
		return documents;
	}

	public void setDocuments(List<MemberDocs> documents) {
		this.documents = documents;
	}
	
	public List<Document> getDocumentList() {
		return documentList;
	}

	public void setDocumentList(List<Document> documentList) {
		this.documentList = documentList;
	}

	public Member getMemberBean() {
		return memberBean;
	}

	public void setMemberBean(Member memberBean) {
		this.memberBean = memberBean;
	}
	
	public List<Member> getMemberList() {
		return memberList;
	}

	public void setMemberList(List<Member> memberList) {
		this.memberList = memberList;
	}

	public MemberBankInfo getMemberBankBean() {
		return memberBankBean;
	}

	public void setMemberBankBean(MemberBankInfo memberBankBean) {
		this.memberBankBean = memberBankBean;
	}

	public List<MemberBankInfo> getMemberBankInfoList() {
		return memberBankInfoList;
	}

	public void setMemberBankInfoList(List<MemberBankInfo> memberBankInfoList) {
		this.memberBankInfoList = memberBankInfoList;
	}	
}
