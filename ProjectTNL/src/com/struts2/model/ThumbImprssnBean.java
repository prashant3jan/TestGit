package com.struts2.model;

import java.io.File;
import java.io.Serializable;
import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="thmbImprssn_bean")
public class ThumbImprssnBean implements Serializable {
	@Id
	@GeneratedValue
	@Column(name="thumbImprssn_id")
	private int thumbImprssnId;
	@Column(name = "thumb_imprssn")
	private File thumbImprssn;
	@Column(name = "app_name")
	private String applicantName;
	@OneToOne
	@JoinColumn(name="loanAccno")
	private CustLoanBean custloanbean;
	@Column(name = "mem_sign")
	private File memSign;
	@Column(name = "thumbImprssn_date")
	private Date thumbImprssnDate;
	@Column(name = "instlmntAmt_deficit")
	private Double instlmntAmtDeficit;
	@Column(name = "checkEqualsInstlAmt")
	private String checkEqualsInstlAmt;
	@Column(name = "firstCheckAmt")
	private Double firstCheckAmt;
	@Column(name = "remainingCheckAmt")
	private Double remainingCheckAmt;
	
	public ThumbImprssnBean(){}
	
	public ThumbImprssnBean(int thumbImprssnId,
			File thumbImprssn,
			String applicantName,
			File memSign,
			Date thumbImprssnDate,
			Double instlmntAmtDeficit,
			String checkEqualsInstlAmt,
			Double firstCheckAmt,
			Double remainingCheckAmt){
		this.thumbImprssnId=thumbImprssnId;
		this.thumbImprssn=thumbImprssn;
		this.applicantName=applicantName;
		this.memSign=memSign;
		this.thumbImprssnDate=thumbImprssnDate;
		this.instlmntAmtDeficit=instlmntAmtDeficit;
		this.checkEqualsInstlAmt=checkEqualsInstlAmt;
		this.firstCheckAmt=firstCheckAmt;
		this.remainingCheckAmt=remainingCheckAmt;
	}
	public int getThumbImprssnId() {
		return thumbImprssnId;
	}

	public void setThumbImprssnId(int thumbImprssnId) {
		this.thumbImprssnId = thumbImprssnId;
	}

	public File getThumbImprssn() {
		return thumbImprssn;
	}

	public void setThumbImprssn(File thumbImprssn) {
		this.thumbImprssn = thumbImprssn;
	}

	public String getApplicantName() {
		return applicantName;
	}

	public void setApplicantName(String applicantName) {
		this.applicantName = applicantName;
	}

	public CustLoanBean getCustloanbean() {
		return custloanbean;
	}

	public void setCustloanbean(CustLoanBean custloanbean) {
		this.custloanbean = custloanbean;
	}

	public File getMemSign() {
		return memSign;
	}

	public void setMemSign(File memSign) {
		this.memSign = memSign;
	}

	public Date getThumbImprssnDate() {
		return thumbImprssnDate;
	}

	public void setThumbImprssnDate(Date thumbImprssnDate) {
		this.thumbImprssnDate = thumbImprssnDate;
	}

	public Double getInstlmntAmtDeficit() {
		return instlmntAmtDeficit;
	}

	public void setInstlmntAmtDeficit(Double instlmntAmtDeficit) {
		this.instlmntAmtDeficit = instlmntAmtDeficit;
	}

	public String getCheckEqualsInstlAmt() {
		return checkEqualsInstlAmt;
	}

	public void setCheckEqualsInstlAmt(String checkEqualsInstlAmt) {
		this.checkEqualsInstlAmt = checkEqualsInstlAmt;
	}

	public Double getFirstCheckAmt() {
		return firstCheckAmt;
	}

	public void setFirstCheckAmt(Double firstCheckAmt) {
		this.firstCheckAmt = firstCheckAmt;
	}

	public Double getRemainingCheckAmt() {
		return remainingCheckAmt;
	}

	public void setRemainingCheckAmt(Double remainingCheckAmt) {
		this.remainingCheckAmt = remainingCheckAmt;
	}


}
