package com.struts2.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class AbstractCustomerLoanAccount {
	@Id
    @Column(name = "loan_accno", nullable = false)	
	private String loanAccno;

	public String getLoanAccno() {
		return loanAccno;
	}

	public void setLoanAccno(String loanAccno) {
		this.loanAccno = loanAccno;
	}
}
