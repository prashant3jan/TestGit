package com.struts2.model;

import java.io.Serializable;
import java.util.Date;

public class CustLoanBeanDTO {
	private String memNum;
	private String loanAccno;
	private Date dateOfLoan; 
	private Double repayPeriod;
	private String pmtFrequency;
	private String depositMode;
	private Date chqDate;
	private String chqNum;
	private String bankName;
	private String bankBrnchDet;
	private byte[] appImageData;
	private String base64AppImage;
	private String applicantName;
	private Date applicantDob;
	private String applicantSex;
	private String applicantFather;
	private String applicantMother;
	private String applicantOccupation;
	private String applicantAddress;
	private String applicantCity;
	private String applicantState;
	private int applicantPin; 
	private String applicantPan;
	private String applicantMobile;
	private String nomineeName;
	private int nomineeAge;
	private String relnWithNominee;
	private String nomineeAddress;
	private byte [] memSignData;
	private String base64MemSign;
	private String grntrDet; 
	private byte[] grntrSignData;
	private byte[] empSignData;
	private String base64GrntrSign;
	private String base64EmpSign;
	public String getMemNum() {
		return memNum;
	}
	public void setMemNum(String memNum) {
		this.memNum = memNum;
	}
	public String getLoanAccno() {
		return loanAccno;
	}
	public void setLoanAccno(String loanAccno) {
		this.loanAccno = loanAccno;
	}
	public Date getDateOfLoan() {
		return dateOfLoan;
	}
	public void setDateOfLoan(Date dateOfLoan) {
		this.dateOfLoan = dateOfLoan;
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
	public String getDepositMode() {
		return depositMode;
	}
	public void setDepositMode(String depositMode) {
		this.depositMode = depositMode;
	}
	public Date getChqDate() {
		return chqDate;
	}
	public void setChqDate(Date chqDate) {
		this.chqDate = chqDate;
	}
	public String getChqNum() {
		return chqNum;
	}
	public void setChqNum(String chqNum) {
		this.chqNum = chqNum;
	}
	public String getBankName() {
		return bankName;
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	public String getBankBrnchDet() {
		return bankBrnchDet;
	}
	public void setBankBrnchDet(String bankBrnchDet) {
		this.bankBrnchDet = bankBrnchDet;
	}
	public byte[] getAppImageData() {
		return appImageData;
	}
	public void setAppImageData(byte[] appImageData) {
		this.appImageData = appImageData;
	}
	public String getApplicantName() {
		return applicantName;
	}
	public void setApplicantName(String applicantName) {
		this.applicantName = applicantName;
	}
	public Date getApplicantDob() {
		return applicantDob;
	}
	public void setApplicantDob(Date applicantDob) {
		this.applicantDob = applicantDob;
	}
	public String getApplicantSex() {
		return applicantSex;
	}
	public void setApplicantSex(String applicantSex) {
		this.applicantSex = applicantSex;
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
	public String getApplicantOccupation() {
		return applicantOccupation;
	}
	public void setApplicantOccupation(String applicantOccupation) {
		this.applicantOccupation = applicantOccupation;
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
	public int getApplicantPin() {
		return applicantPin;
	}
	public void setApplicantPin(int applicantPin) {
		this.applicantPin = applicantPin;
	}
	public String getApplicantPan() {
		return applicantPan;
	}
	public void setApplicantPan(String applicantPan) {
		this.applicantPan = applicantPan;
	}
	public String getApplicantMobile() {
		return applicantMobile;
	}
	public void setApplicantMobile(String applicantMobile) {
		this.applicantMobile = applicantMobile;
	}
	public String getNomineeName() {
		return nomineeName;
	}
	public void setNomineeName(String nomineeName) {
		this.nomineeName = nomineeName;
	}
	public int getNomineeAge() {
		return nomineeAge;
	}
	public void setNomineeAge(int nomineeAge) {
		this.nomineeAge = nomineeAge;
	}
	public String getRelnWithNominee() {
		return relnWithNominee;
	}
	public void setRelnWithNominee(String relnWithNominee) {
		this.relnWithNominee = relnWithNominee;
	}
	public String getNomineeAddress() {
		return nomineeAddress;
	}
	public void setNomineeAddress(String nomineeAddress) {
		this.nomineeAddress = nomineeAddress;
	}
	public byte[] getMemSignData() {
		return memSignData;
	}
	public void setMemSignData(byte[] memSignData) {
		this.memSignData = memSignData;
	}
	public String getGrntrDet() {
		return grntrDet;
	}
	public void setGrntrDet(String grntrDet) {
		this.grntrDet = grntrDet;
	}
	public byte[] getGrntrSignData() {
		return grntrSignData;
	}
	public void setGrntrSignData(byte[] grntrSignData) {
		this.grntrSignData = grntrSignData;
	}
	public byte[] getEmpSignData() {
		return empSignData;
	}
	public void setEmpSignData(byte[] empSignData) {
		this.empSignData = empSignData;
	}
	/**
	 * @return the base64AppImage
	 */
	public String getBase64AppImage() {
		return base64AppImage;
	}
	/**
	 * @param base64AppImage the base64AppImage to set
	 */
	public void setBase64AppImage(String base64AppImage) {
		this.base64AppImage = base64AppImage;
	}
	/**
	 * @return the base64MemSign
	 */
	public String getBase64MemSign() {
		return base64MemSign;
	}
	/**
	 * @param base64MemSign the base64MemSign to set
	 */
	public void setBase64MemSign(String base64MemSign) {
		this.base64MemSign = base64MemSign;
	}
	/**
	 * @return the base64GrntrSign
	 */
	public String getBase64GrntrSign() {
		return base64GrntrSign;
	}
	/**
	 * @param base64GrntrSign the base64GrntrSign to set
	 */
	public void setBase64GrntrSign(String base64GrntrSign) {
		this.base64GrntrSign = base64GrntrSign;
	}
	/**
	 * @return the base64EmpSign
	 */
	public String getBase64EmpSign() {
		return base64EmpSign;
	}
	/**
	 * @param base64EmpSign the base64EmpSign to set
	 */
	public void setBase64EmpSign(String base64EmpSign) {
		this.base64EmpSign = base64EmpSign;
	}

}
