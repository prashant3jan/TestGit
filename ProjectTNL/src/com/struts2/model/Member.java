package com.struts2.model;

import java.io.File;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

import java.io.Serializable;

@Entity
public class Member implements Serializable {
	@Id
	@Column(name = "mem_no", nullable = false)
	private String memNum;
	@Temporal(TemporalType.DATE)
	@Column(name = "mem_dt")
	private Date memDate;
	@Column(name = "mem_image")
	private File memImage;
	@Lob
	@Column(name = "memImageData", unique = false, nullable = false, length = 100000)
	private byte[] memImageData;
	private String base64MemImage;
	@Column(name = "app_name")
	private String applicantName;
	@Temporal(TemporalType.DATE)
	@Column(name = "app_dob")
	private Date applicantDob;
	@Column(name = "app_sex")
	private String applicantSex;
	@Column(name = "app_father")
	private String applicantFather;
	@Column(name = "app_mother")
	private String applicantMother;
	@Column(name = "app_occptn")
	private String applicantOccupation;
	@Column(name = "app_addrss")
	private String applicantAddress;
	@Column(name = "app_city")
	private String applicantCity;
	@Column(name = "app_state")
	private String applicantState;
	@Column(name = "app_pin")
	private Integer applicantPin;
	@Column(name = "app_pan")
	private String applicantPan;
	@Column(name = "app_mob")
	private String applicantMobile;
	@Column(name = "nom_name")
	private String nomineeName;
	@Column(name = "nom_age")
	private Integer nomineeAge;
	@Column(name = "nom_rel")
	private String relnWithNominee;
	@Column(name = "nom_add")
	private String nomineeAddress;
	@Column(name = "memfee_fig")
	private String memFeeFig;
	@Column(name = "memfee_words")
	private String memFeeWords;
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "mem_no")
	private List<MemberDocs> documents = new ArrayList<MemberDocs>();
	@Column(name = "auth_signtry")
	private File authSigntry;
	@Lob
	@Column(name = "authSigntryData", unique = false, nullable = false, length = 100000)
	private byte[] authSigntryData;
	private String base64authSigntry;
	@Column(name = "mem_sign")
	private File memSign;
	@Lob
	@Column(name = "memSignData", unique = false, nullable = false, length = 100000)
	private byte[] memSignData;
	private String base64MemSign;
	@Column(name = "grntr_det")
	private String grntrDet;
	@Column(name = "grntr_sign")
	private File grntrSign;
	@Lob
	@Column(name = "grntrSignData", unique = false, nullable = false, length = 100000)
	private byte[] grntrSignData;
	private String base64GrntrSign;
	@Column(name = "emp_sign")
	private File empSign;
	@Lob
	@Column(name = "empSignData", unique = false, nullable = false, length = 100000)
	private byte[] empSignData;
	private String base64EmpSign;
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "mem_no")
	private List<CustLoanBean> loanList = new ArrayList<CustLoanBean>();
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "mem_no")
	private List<CustPmtBean> paymentList = new ArrayList<CustPmtBean>();

	public Member() {
	}

	public Member(Date _memDate, File _memImage, String _applicantName, byte[] _memImageData, String _base64MemImage,
			Date _applicantDob, String _applicantSex, String _applicantFather, String _applicantMother,
			String _applicantOccupation, String _applicantAddress, String _applicantCity, String _applicantState,
			Integer _applicantPin, String _applicantPan, String _applicantMobile, String _nomineeName,
			Integer _nomineeAge, String _relnWithNominee, String _nomineeAddress, String _memFeeFig,
			String _memFeeWords, List<MemberDocs> _documents, File _authSigntry, byte[] _authSigntryData,
			String _base64authSigntry, File _memSign, byte[] _memSignData, String _base64MemSign, String _grntrDet,
			File _grntrSign, byte[] _grntrSignData, String _base64GrntrSign, File _empSign, byte[] _empSignData,
			String _base64EmpSign) {
		this.memDate = _memDate;
		this.memImage = _memImage;
		this.memImageData = _memImageData;
		this.base64MemImage = _base64MemImage;
		this.applicantName = _applicantName;
		this.applicantDob = _applicantDob;
		this.applicantSex = _applicantSex;
		this.applicantFather = _applicantFather;
		this.applicantMother = _applicantMother;
		this.applicantOccupation = _applicantOccupation;
		this.applicantAddress = _applicantAddress;
		this.applicantCity = _applicantCity;
		this.applicantState = _applicantState;
		this.applicantPin = _applicantPin;
		this.applicantPan = _applicantPan;
		this.applicantMobile = _applicantMobile;
		this.nomineeName = _nomineeName;
		this.nomineeAge = _nomineeAge;
		this.relnWithNominee = _relnWithNominee;
		this.nomineeAddress = _nomineeAddress;
		this.memFeeFig = _memFeeFig;
		this.memFeeWords = _memFeeWords;
		this.documents = _documents;
		this.authSigntry = _authSigntry;
		this.authSigntryData = _authSigntryData;
		this.base64authSigntry = _base64authSigntry;
		this.memSign = _memSign;
		this.memSignData = _memSignData;
		this.base64MemSign = _base64MemSign;
		this.grntrDet = _grntrDet;
		this.grntrSign = _grntrSign;
		this.grntrSignData = _grntrSignData;
		this.base64GrntrSign = _base64GrntrSign;
		this.empSign = _empSign;
		this.empSignData = _empSignData;
		this.base64EmpSign = _base64EmpSign;
	}
	public String getMemNum() {
		return memNum;
	}

	public void setMemNum(String memNum) {
		this.memNum = memNum;
	}

	public File getMemImage() {
		return memImage;
	}
	public void setMemImage(File memImage) {
		this.memImage = memImage;
	}

	public byte[] getMemImageData() {
		return memImageData;
	}

	public void setMemImageData(byte[] memImageData) {
		this.memImageData = memImageData;
	}

	public File getAuthSigntry() {
		return authSigntry;
	}

	public void setAuthSigntry(File authSigntry) {
		this.authSigntry = authSigntry;
	}

	public byte[] getAuthSigntryData() {
		return authSigntryData;
	}

	public void setAuthSigntryData(byte[] authSigntryData) {
		this.authSigntryData = authSigntryData;
	}

	public String getBase64authSigntry() {
		return base64authSigntry;
	}

	public void setBase64authSigntry(String base64authSigntry) {
		this.base64authSigntry = base64authSigntry;
	}

	public byte[] getMemSignData() {
		return memSignData;
	}

	public void setMemSignData(byte[] memSignData) {
		this.memSignData = memSignData;
	}

	public String getBase64MemSign() {
		return base64MemSign;
	}

	public void setBase64MemSign(String base64MemSign) {
		this.base64MemSign = base64MemSign;
	}

	public byte[] getGrntrSignData() {
		return grntrSignData;
	}

	public void setGrntrSignData(byte[] grntrSignData) {
		this.grntrSignData = grntrSignData;
	}

	public String getBase64GrntrSign() {
		return base64GrntrSign;
	}

	public void setBase64GrntrSign(String base64GrntrSign) {
		this.base64GrntrSign = base64GrntrSign;
	}

	public byte[] getEmpSignData() {
		return empSignData;
	}

	public void setEmpSignData(byte[] empSignData) {
		this.empSignData = empSignData;
	}

	public String getBase64EmpSign() {
		return base64EmpSign;
	}

	public void setBase64EmpSign(String base64EmpSign) {
		this.base64EmpSign = base64EmpSign;
	}

	public File getMemSign() {
		return memSign;
	}

	public void setMemSign(File memSign) {
		this.memSign = memSign;
	}

	public File getGrntrSign() {
		return grntrSign;
	}

	public void setGrntrSign(File grntrSign) {
		this.grntrSign = grntrSign;
	}

	public File getEmpSign() {
		return empSign;
	}

	public void setEmpSign(File empSign) {
		this.empSign = empSign;
	}

	public Date getMemDate() {
		return memDate;
	}

	public void setMemDate(Date memDate) {
		this.memDate = memDate;
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

	public Integer getApplicantPin() {
		return applicantPin;
	}

	public void setApplicantPin(Integer applicantPin) {
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

	public Integer getNomineeAge() {
		return nomineeAge;
	}

	public void setNomineeAge(Integer nomineeAge) {
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

	public String getMemFeeFig() {
		return memFeeFig;
	}

	public void setMemFeeFig(String memFeeFig) {
		this.memFeeFig = memFeeFig;
	}

	public String getMemFeeWords() {
		return memFeeWords;
	}

	public void setMemFeeWords(String memFeeWords) {
		this.memFeeWords = memFeeWords;
	}

	public List<MemberDocs> getDocuments() {
		return documents;
	}

	public void setDocuments(List<MemberDocs> documents) {
		this.documents = documents;
	}

	public String getGrntrDet() {
		return grntrDet;
	}

	public void setGrntrDet(String grntrDet) {
		this.grntrDet = grntrDet;
	}

	public List<CustLoanBean> getLoanList() {
		return loanList;
	}

	public void setLoanList(List<CustLoanBean> loanList) {
		this.loanList = loanList;
	}

	public List<CustPmtBean> getPaymentList() {
		return paymentList;
	}

	public void setPaymentList(List<CustPmtBean> paymentList) {
		this.paymentList = paymentList;
	}

	public String getApplicantAddress() {
		return applicantAddress;
	}

	public void setApplicantAddress(String applicantAddress) {
		this.applicantAddress = applicantAddress;
	}

	/**
	 * @return the base64MemImage
	 */
	public String getBase64MemImage() {
		return base64MemImage;
	}

	/**
	 * @param base64MemImage
	 *            the base64MemImage to set
	 */
	public void setBase64MemImage(String base64MemImage) {
		this.base64MemImage = base64MemImage;
	}

}
