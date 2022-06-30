package com.struts2.model;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="installment")
public class Installment {
	@Id
	@GeneratedValue
	@Column(name="install_id")
	private int installId;
	@Column(name = "install_amt")
	private double installmentAmt;
	@Column(name = "install_dt")
	private Date installmentDt;
	@ManyToOne(fetch = FetchType.LAZY) 
	@JoinColumn(name="payment_id")
	private CustPmtBean custPmtDet;
	public CustPmtBean getCustPmtDet() {
		return custPmtDet;
	}

	public void setCustPmtDet(CustPmtBean custPmtDet) {
		this.custPmtDet = custPmtDet;
	}
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="loan_accno")
	private CustLoanBean custLoanDet;
	public Installment(){}
	
	public Installment(double _installmentAmt, Date _installmentDt){
		this.installmentAmt = _installmentAmt;
		this.installmentDt = _installmentDt;
	}

	public CustLoanBean getCustLoanDet() {
		return custLoanDet;
	}

	public void setCustLoanDet(CustLoanBean custLoanDet) {
		this.custLoanDet = custLoanDet;
	}

	public int getInstallId() {
		return installId;
	}

	public void setInstallId(int installId) {
		this.installId = installId;
	}
	public double getInstallmentAmt() {
		return installmentAmt;
	}
	public void setInstallmentAmt(double installmentAmt) {
		this.installmentAmt = installmentAmt;
	}
	public Date getInstallmentDt() {
		return installmentDt;
	}
	public void setInstallmentDt(Date installmentDt) {
		this.installmentDt = installmentDt;
	}
}
