package com.struts2.model;

import java.io.File;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


public class DebtorPassbook {
	private String loanAccno;
	private String chequeNo;
	private String applicantName;
	private String applicantFather;
	private String applicantMother;
	private String applicantAddress;
	private String applicantCity;
	private String applicantState;
	private Long applicantPin;
	private Double debitloanGvnByComp;
	private Double repayPeriod;
	private String pmtFrequency;
	private Double installment;
	private Date dateOfLoan;
	private File memSign;
	private File empSign;
		public String getLoanAccno() {
		return loanAccno;
	}
	public void setLoanAccno(String loanAccno) {
		this.loanAccno = loanAccno;
	}
	public String getChequeNo() {
		return chequeNo;
	}
	public void setChequeNo(String chequeNo) {
		this.chequeNo = chequeNo;
	}
	public String getApplicantName() {
		return applicantName;
	}
	public void setApplicantName(String applicantName) {
		this.applicantName = applicantName;
	}
	public String getApplicantFather() {
		return applicantFather;
	}
	public void setApplicantFather(String applicantFather) {
		this.applicantFather = applicantFather;
	}
	public String getApplicantMother() {
		return applicantMother;
	}
	public void setApplicantMother(String applicantMother) {
		this.applicantMother = applicantMother;
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

	public Date getDateOfLoan() {
		return dateOfLoan;
	}
	public void setDateOfLoan(Date dateOfLoan) {
		this.dateOfLoan = dateOfLoan;
	}
	public File getMemSign() {
		return memSign;
	}
	public void setMemSign(File memSign) {
		this.memSign = memSign;
	}
	public File getEmpSign() {
		return empSign;
	}
	public void setEmpSign(File empSign) {
		this.empSign = empSign;
	}
		
		public Double getDebitloanGvnByComp() {
		return debitloanGvnByComp;
	}
	public void setDebitloanGvnByComp(Double debitloanGvnByComp) {
		this.debitloanGvnByComp = debitloanGvnByComp;
	}

		public Double getInstallment() {
		return installment;
	}
	public void setInstallment(Double installment) {
		this.installment = installment;
	}


}
