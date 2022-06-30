package com.struts2.model;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;

@Entity
@Table(name="custloan_bean")
public class CustLoanBean implements Serializable {
	@Id
    @Column(name = "loan_accno", nullable = false)	
	private String loanAccno;
	@Temporal(TemporalType.DATE)
	@Column(name = "loan_dt")
	private Date dateOfLoan;
	public Integer getApplicantPin() {
		return applicantPin;
	}

	public void setApplicantPin(Integer applicantPin) {
		this.applicantPin = applicantPin;
	}

	public Integer getNomineeAge() {
		return nomineeAge;
	}

	public void setNomineeAge(Integer nomineeAge) {
		this.nomineeAge = nomineeAge;
	}

	@OneToMany(cascade=CascadeType.ALL)
	@JoinColumn(name="loan_accno")
	private List<CustPmtBean> paymentList = new ArrayList<CustPmtBean>();
	@Column(name = "repay_period")
	private Double repayPeriod;
	@JoinColumn(name="pmt_freq")
	private String pmtFrequency;
	@Column(name = "dpst_mode")
	private String depositMode;
	@Temporal(TemporalType.DATE)
	@Column(name="chq_date")
	private Date chqDate;
	@Column(name = "chq_num")
	private String chqNum;
	@Column(name = "bank_name")
	private String bankName;
	@Column(name = "bank_br_det")
	private String bankBrnchDet;
	@Column(name = "app_image")
	private File appImage;
	@Lob
	@Column(name = "appImageData", unique = false, nullable = false, length = 100000)
	private byte[] appImageData;
	private String base64AppImage;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="mem_no")
	private Member member;
	@OneToMany(cascade=CascadeType.ALL)
	@JoinColumn(name="loanAccno")
	private List<CollateralGvn> collGvnList = new ArrayList<CollateralGvn>();
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
	@OneToMany(cascade=CascadeType.ALL)
	@JoinColumn(name="loanAccno")
	private List<LoanDocs> documents = new ArrayList<LoanDocs>();
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
	@OneToMany(cascade=CascadeType.ALL)
	@JoinColumn(name="loan_accno")
	private List<CompanyBankAccount> onlinePaymentList = new ArrayList<CompanyBankAccount>();
	@OneToMany(cascade=CascadeType.ALL)
	@JoinColumn(name="loan_accno")
	private List<Installment> installmentList = new ArrayList<Installment>();
	@OneToOne(mappedBy="custLoanBean")
	private PromisaryBean promisaryBean;
	@OneToOne(mappedBy="custloanbean")
	private ThumbImprssnBean thumbImprssnBean;
	
	public CustLoanBean(){}
	
	public CustLoanBean(Date _dateOfLoan, String _weeklyDue, Integer _loanAmountgiven, Integer _totAmountReceived,
			Member _member, List<CollateralGvn> _collGvnList, Double _repayPeriod, String _depositMode, Date _chqDate,
			String _chqNum, String _bankName, String _bankBrnchDet, File _appImage, byte[] _appImageData, String _base64AppImage, String _applicantName,
			Date _applicantDob, String _applicantSex, String _applicantFather, String _applicantMother,
			String _applicantOccupation, String _applicantAddress, String _applicantCity, String _applicantState,
			Integer _applicantPin, String _applicantPan, String _applicantMobile, String _nomineeName, Integer _nomineeAge,
			String _relnWithNominee, String _nomineeAddress, List<LoanDocs> _documents, File _memSign, byte[] _memSignData,
			String _grntrDet, String _base64MemSign, File _grntrSign, byte[] _grntrSignData, String _base64GrntrSign,
			File _empSign, byte[] _empSignData, String _base64EmpSign, String _pmtFrequency) {
		 
		 this.dateOfLoan = _dateOfLoan;
		 this.applicantName = _applicantName;
		 this.member = _member;
		 this.collGvnList = _collGvnList;
		 this.repayPeriod = _repayPeriod;
		 this.depositMode = _depositMode;
		 this.chqDate = _chqDate;
		 this.chqNum = _chqNum;
		 this.bankName = _bankName;
		 this.bankBrnchDet = _bankBrnchDet;
		 this.appImage = _appImage;
		 this.appImageData=_appImageData;
		 this.base64AppImage=_base64AppImage;
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
		 this.nomineeAddress =  _nomineeAddress;
		 this.documents = _documents;
		 this.memSign = _memSign;
		 this.memSignData=_memSignData;
		 this.base64MemSign=_base64MemSign;
		 this.grntrDet = _grntrDet;
		 this.grntrSign = _grntrSign;
		 this.grntrSignData=_grntrSignData;
		 this.base64GrntrSign=_base64GrntrSign;
		 this.empSign = _empSign;
		 this.empSignData=_empSignData;
		 this.pmtFrequency = _pmtFrequency;
		 this.base64EmpSign=_base64EmpSign;
	 }

	public File getAppImage() {
		return appImage;
	}

	public void setAppImage(File appImage) {
		this.appImage = appImage;
	}

	public byte[] getAppImageData() {
		return appImageData;
	}

	public void setAppImageData(byte[] appImageData) {
		this.appImageData = appImageData;
	}

	public String getBase64AppImage() {
		return base64AppImage;
	}

	public void setBase64AppImage(String base64AppImage) {
		this.base64AppImage = base64AppImage;
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

	public List<Installment> getInstallmentList() {
		return installmentList;
	}

	public void setInstallmentList(List<Installment> installmentList) {
		this.installmentList = installmentList;
	}

	
	public String getLoanAccno() {
		return loanAccno;
	}

	public void setLoanAccno(String loanAccno) {
		this.loanAccno = loanAccno;
	}


	public List<CompanyBankAccount> getOnlinePaymentList() {
		return onlinePaymentList;
	}

	public void setOnlinePaymentList(List<CompanyBankAccount> onlinePaymentList) {
		this.onlinePaymentList = onlinePaymentList;
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

	public List<CollateralGvn> getCollGvnList() {
		return collGvnList;
	}

	public void setCollGvnList(List<CollateralGvn> collGvnList) {
		this.collGvnList = collGvnList;
	}

	public Double getRepayPeriod() {
		return repayPeriod;
	}

	public void setRepayPeriod(Double repayPeriod) {
		this.repayPeriod = repayPeriod;
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

	public Member getMember() {
		return member;
	}

	public void setMember(Member member) {
		this.member = member;
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

	public List<LoanDocs> getDocuments() {
		return documents;
	}

	public void setDocuments(List<LoanDocs> documents) {
		this.documents = documents;
	}

	public File getMemSign() {
		return memSign;
	}

	public void setMemSign(File memSign) {
		this.memSign = memSign;
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

	public File getEmpSign() {
		return empSign;
	}

	public void setEmpSign(File empSign) {
		this.empSign = empSign;
	}
}

