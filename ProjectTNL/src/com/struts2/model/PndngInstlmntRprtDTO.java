package com.struts2.model;

import java.sql.Date;

public class PndngInstlmntRprtDTO {
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
	private double totPmtGvnByCust;
	private Date dateOfLoan;
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
	public double getTotPmtGvnByCust() {
		return totPmtGvnByCust;
	}
	public void setTotPmtGvnByCust(double totPmtGvnByCust) {
		this.totPmtGvnByCust = totPmtGvnByCust;
	}
	public Date getDateOfLoan() {
		return dateOfLoan;
	}
	public void setDateOfLoan(Date dateOfLoan) {
		this.dateOfLoan = dateOfLoan;
	}
}
