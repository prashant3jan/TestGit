package com.struts2.model;

import java.io.File;
import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
@Entity
@Table(name="prmssry_bean")
public class PromisaryBean implements Serializable {
	@Id
	@GeneratedValue
	@Column(name="prmssry_id")
	private int prmssryId;
	@Column(name = "app_name")
	private String applicantName;
	@Column(name = "loan_amt")
	private Double loanAmount;
	@Column(name = "grntr_det")
	private String grntrDet;
	@Column(name = "grntr_sign")
	private File grntrSign;
	@Column(name = "app_sign")
	private File appSign;
	@Column(name = "amt_rcvd_rcpnt")
	private Double amtRcvdByRcpnt;
	@Column(name = "loan_dt")
	private Date dateOfLoan;
	@OneToOne
	@JoinColumn(name="loanAccno")
	private CustLoanBean custLoanBean;
	
	public PromisaryBean(){}
	
	public PromisaryBean(String applicantName,
		Double loanAmount,
		String grntrDet,
		File grntrSign,
		File appSign,
		Double amtRcvdByRcpnt, Date dateOfLoan ){
		this.applicantName=applicantName;
		this.loanAmount=loanAmount;
		this.grntrDet=grntrDet;
		this.grntrSign=grntrSign;
		this.appSign=appSign;
		this.amtRcvdByRcpnt=amtRcvdByRcpnt;
		this.dateOfLoan=dateOfLoan;	
	}
	public Date getDateOfLoan() {
		return dateOfLoan;
	}

	public void setDateOfLoan(Date dateOfLoan) {
		this.dateOfLoan = dateOfLoan;
	}

	public int getPrmssryId() {
		return prmssryId;
	}
	public void setPrmssryId(int prmssryId) {
		this.prmssryId = prmssryId;
	}
	public String getApplicantName() {
		return applicantName;
	}
	public void setApplicantName(String applicantName) {
		this.applicantName = applicantName;
	}
	public Double getLoanAmount() {
		return loanAmount;
	}
	public void setLoanAmount(Double loanAmount) {
		this.loanAmount = loanAmount;
	}
	public String getGrntrDet() {
		return grntrDet;
	}
	public void setGrntrDet(String grntrDet) {
		this.grntrDet = grntrDet;
	}
	public File getGrntrSign() {
		return grntrSign;
	}
	public void setGrntrSign(File grntrSign) {
		this.grntrSign = grntrSign;
	}
	public File getAppSign() {
		return appSign;
	}
	public void setAppSign(File appSign) {
		this.appSign = appSign;
	}
	public Double getAmtRcvdByRcpnt() {
		return amtRcvdByRcpnt;
	}
	public void setAmtRcvdByRcpnt(Double amtRcvdByRcpnt) {
		this.amtRcvdByRcpnt = amtRcvdByRcpnt;
	}
	public CustLoanBean getCustLoanBean() {
		return custLoanBean;
	}
	public void setCustLoanBean(CustLoanBean custLoanBean) {
		this.custLoanBean = custLoanBean;
	}

}
