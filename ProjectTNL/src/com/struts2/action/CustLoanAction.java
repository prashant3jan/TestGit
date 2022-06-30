package com.struts2.action;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import com.opensymphony.xwork2.ActionSupport;
import com.struts2.model.Collateral;
import com.struts2.model.CollateralGvn;
import com.struts2.model.CustLoanBean;
import com.struts2.model.CustLoanBeanDTO;
import com.struts2.model.Document;
import com.struts2.model.LoanDocs;
import com.struts2.model.Member;

public class CustLoanAction extends ActionSupport {
	private static final long serialVersionUID = 2L;
	private CustLoanManager loanManager;
	private MemberManager memberManager;
	private CustLoanBean custLoanBean;
	public CustLoanBean getCustLoanBean() {
		return custLoanBean;
	}

	public void setCustLoanBean(CustLoanBean custLoanBean) {
		this.custLoanBean = custLoanBean;
	}

	private CustLoanBeanDTO custLoanBeanDto;
	private List<CustLoanBean> custLoanList;
	private List<Document> documentList;
	private List<Collateral>collList;
	private List<LoanDocs> documents;
	private List<CollateralGvn> collGvnList;
	private String loanAccno; 
	
	public CustLoanAction(){
		loanManager = new CustLoanManager();
		memberManager = new MemberManager();
		//collList = loanManager.listColl();
		
	}
	
	public String showLoanForm(){
		System.out.println("before colllist");
		collList = loanManager.listColl();
		System.out.println("after colllist");
		custLoanList= loanManager.loanList();
		System.out.println("after listdocs");
		documentList = memberManager.listDocs();
		return "success";
	}
	
	public String dispLoanDetIndiv(){
		return "success";
	}
	
	public String dispLoanDetByLoanAccno(){
		System.out.println("loanAccno"+custLoanBean.getLoanAccno());
		loanAccno= custLoanBean.getLoanAccno();
		custLoanBeanDto = loanManager.getLoanDetByLoanAccno(loanAccno);
		return "success";
	}
	
	public String add(){
		//member.getMemNum());
		try{
			System.out.println("inside add");
		    CustLoanBean clb = this.getCustLoanBean();
		    System.out.println("here1");
	        File fileAppImage = clb.getAppImage();
	        System.out.println("here2");
	        System.out.println("image_length"+fileAppImage.length());
	        byte[] bAppImage = new byte[(int) fileAppImage.length()];
	        
	        try {
	         FileInputStream fileInputStream = new FileInputStream(fileAppImage);
	         //convert file into array of bytes
	         fileInputStream.read(bAppImage);
	         fileInputStream.close();
	        } catch (Exception e) {
	         e.printStackTrace();
	        }
	        clb.setAppImageData(bAppImage);
	        //set the array in bean
	      
			//code repeat for adding memSign into bean
			File fileMemSign = clb.getMemSign();
	        byte[] bMemSign = new byte[(int) fileMemSign.length()];
	        
	        try {
	         FileInputStream fileInputStream = new FileInputStream(fileMemSign);
	         //convert file into array of bytes
	         fileInputStream.read(bMemSign);
	         fileInputStream.close();
	        } catch (Exception e) {
	         e.printStackTrace();
	        }
	        clb.setMemSignData(bMemSign);
	        //set the array in bean
			//end code repeat for adding memSign into bean
			
			//code repeat for adding grntrSign into bean
			File fileGrntrSign = clb.getGrntrSign();
	        byte[] bGrntrSign = new byte[(int) fileGrntrSign.length()];
	        
	        try {
	         FileInputStream fileInputStream = new FileInputStream(fileGrntrSign);
	         //convert file into array of bytes
	         fileInputStream.read(bGrntrSign);
	         fileInputStream.close();
	        } catch (Exception e) {
	         e.printStackTrace();
	        }
	        clb.setGrntrSignData(bGrntrSign);
			//end code repeat for adding grntrSign into bean
			
			//code repeat for adding empSign into bean
			File fileEmpSign = clb.getEmpSign();
	        byte[] bEmpSign = new byte[(int) fileEmpSign.length()];
	        
	        try {
	         FileInputStream fileInputStream = new FileInputStream(fileEmpSign);
	         //convert file into array of bytes
	         fileInputStream.read(bEmpSign);
	         fileInputStream.close();
	        } catch (Exception e) {
	         e.printStackTrace();
	        }
	        clb.setEmpSignData(bEmpSign);
	        //set the array in bean
	        //end code repeat for adding grntrSign into bean
	        loanManager.add(clb);
			//add member data to the database
			System.out.println("member added");
			}catch(Exception e){
				e.printStackTrace();
			}
			custLoanList=loanManager.loanList();
			collList = loanManager.listColl();
			documentList = memberManager.listDocs();
			return "success";
		}
	
	public String loanDelete(){
		try{
		loanManager.delete(loanAccno);
		}catch(Exception e){
			e.printStackTrace();
		}
		this.setCustLoanList(loanManager.loanList());
		collList = loanManager.listColl();
		documentList = memberManager.listDocs();
		return "success";
	}
	
	public List<Collateral> getCollList() {
		return collList;
	}

	public void setCollList(List<Collateral> collList) {
		this.collList = collList;
	}

	public List<Document> getDocumentList() {
		return documentList;
	}

	public void setDocumentList(List<Document> documentList) {
		this.documentList = documentList;
	}

	public List<LoanDocs> getDocuments() {
		return documents;
	}

	public void setDocuments(List<LoanDocs> documents) {
		this.documents = documents;
	}

	public List<CollateralGvn> getCollGvnList() {
		return collGvnList;
	}

	public void setCollGvnList(List<CollateralGvn> collGvnList) {
		this.collGvnList = collGvnList;
	}

	public String getLoanAccno() {
		return loanAccno;
	}

	public void setLoanAccno(String loanAccno) {
		this.loanAccno = loanAccno;
	}

	public List<CustLoanBean> getCustLoanList() {
		return custLoanList;
	}

	public void setCustLoanList(List<CustLoanBean> custLoanList) {
		this.custLoanList = custLoanList;
	}

	/**
	 * @return the custLoanBeanDto
	 */
	public CustLoanBeanDTO getCustLoanBeanDto() {
		return custLoanBeanDto;
	}

	/**
	 * @param custLoanBeanDto the custLoanBeanDto to set
	 */
	public void setCustLoanBeanDto(CustLoanBeanDTO custLoanBeanDto) {
		this.custLoanBeanDto = custLoanBeanDto;
	}


}
