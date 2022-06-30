package com.struts2.model;

import java.util.Date;

public class PndngInstlmntRprtBean {
	private String loanAccno;
	private String applicantName;
	private String applicantAddress;
	private String applicantCity;
	private String applicantState;
	private Long applicantPin;
	private String applicantMobile;
	private Double repayPeriod;
	private String pmtFrequency;
	private Double totLoanWithInterest;
	private double totPmtGvnByCustTillDate;
	private Date dateOfLoan;
	private Date currentDate;
	private Long totNoOfInstlmntDueTillDate;
	private Double instlmntAmt;
	private Double totPmtDueTillDate;
	private Double outstandingBalance;
	private String loanStatus;
	public String getLoanAccno() {
		return loanAccno;
	}
	public void setLoanAccno(String loanAccno) {
		this.loanAccno = loanAccno;
	}
	public String getApplicantName() {
		return applicantName;
	}
	public void setApplicantName(String applicantName) {
		this.applicantName = applicantName;
	}
	public String getApplicantAddress() {
		return applicantAddress;
	}
	public void setApplicantAddress(String applicantAddress) {
		this.applicantAddress = applicantAddress;
	}
	public String getApplicantCity() {
		return applicantCity;
	}
	public void setApplicantCity(String applicantCity) {
		this.applicantCity = applicantCity;
	}
	public String getApplicantState() {
		return applicantState;
	}
	public void setApplicantState(String applicantState) {
		this.applicantState = applicantState;
	}
	public Long getApplicantPin() {
		return applicantPin;
	}
	public void setApplicantPin(Long applicantPin) {
		this.applicantPin = applicantPin;
	}
	public String getApplicantMobile() {
		return applicantMobile;
	}
	public void setApplicantMobile(String applicantMobile) {
		this.applicantMobile = applicantMobile;
	}
	public Double getRepayPeriod() {
		return repayPeriod;
	}
	public void setRepayPeriod(Double repayPeriod) {
		this.repayPeriod = repayPeriod;
	}
	public String getPmtFrequency() {
		return pmtFrequency;
	}
	public void setPmtFrequency(String pmtFrequency) {
		this.pmtFrequency = pmtFrequency;
	}
	public Double getTotLoanWithInterest() {
		return totLoanWithInterest;
	}
	public void setTotLoanWithInterest(Double totLoanWithInterest) {
		this.totLoanWithInterest = totLoanWithInterest;
	}
	public double getTotPmtGvnByCustTillDate() {
		return totPmtGvnByCustTillDate;
	}
	public void setTotPmtGvnByCustTillDate(double totPmtGvnByCustTillDate) {
		this.totPmtGvnByCustTillDate = totPmtGvnByCustTillDate;
	}
	public Date getDateOfLoan() {
		return dateOfLoan;
	}
	public void setDateOfLoan(Date dateOfLoan) {
		this.dateOfLoan = dateOfLoan;
	}
	public Date getCurrentDate() {
		return currentDate;
	}
	public void setCurrentDate(Date currentDate) {
		this.currentDate = currentDate;
	}
	public Long getTotNoOfInstlmntDueTillDate() {
		return totNoOfInstlmntDueTillDate;
	}
	public void setTotNoOfInstlmntDueTillDate(Long totNoOfInstlmntDueTillDate) {
		this.totNoOfInstlmntDueTillDate = totNoOfInstlmntDueTillDate;
	}
	public Double getInstlmntAmt() {
		return instlmntAmt;
	}
	public void setInstlmntAmt(Double instlmntAmt) {
		this.instlmntAmt = instlmntAmt;
	}
	public Double getTotPmtDueTillDate() {
		return totPmtDueTillDate;
	}
	public void setTotPmtDueTillDate(Double totPmtDueTillDate) {
		this.totPmtDueTillDate = totPmtDueTillDate;
	}
	public Double getOutstandingBalance() {
		return outstandingBalance;
	}
	public void setOutstandingBalance(Double outstandingBalance) {
		this.outstandingBalance = outstandingBalance;
	}
	public String getLoanStatus() {
		return loanStatus;
	}
	public void setLoanStatus(String loanStatus) {
		this.loanStatus = loanStatus;
	}

}
