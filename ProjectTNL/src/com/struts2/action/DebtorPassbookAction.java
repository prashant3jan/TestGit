package com.struts2.action;

import java.util.ArrayList;
import java.util.List;

import com.opensymphony.xwork2.ActionSupport;
import com.struts2.model.DebtorPassbook;
import com.struts2.model.Installment;

public class DebtorPassbookAction extends ActionSupport {
	private DebtorPassbookManager debtorPassbookManager;
	private CustPmtManager uploadManager;
	private String loanAccno;
	private DebtorPassbook debtorPbkBean;
	private List<Installment> installmentList  = new ArrayList<Installment>();
	
	public DebtorPassbookAction(){
		debtorPassbookManager = new DebtorPassbookManager();
		uploadManager = new CustPmtManager();
	}
	private static final long serialVersionUID = 1L;
	
	public String execute(){
		System.out.println("execute called");
		return "success";
	}
	
	public String addPassBookDet(){
		System.out.println("loanAccno"+debtorPbkBean.getLoanAccno());
		String loanAccno= debtorPbkBean.getLoanAccno();
		debtorPbkBean = debtorPassbookManager.getDebtorPassbookDet(loanAccno);
		setInstallmentList(uploadManager.getInstallmentList(loanAccno));
		return "success";
	}

	public String getLoanAccno() {
		return loanAccno;
	}

	public void setLoanAccno(String loanAccno) {
		this.loanAccno = loanAccno;
	}

	public DebtorPassbook getDebtorPbkBean() {
		return debtorPbkBean;
	}

	public void setDebtorPbkBean(DebtorPassbook debtorPbkBean) {
		this.debtorPbkBean = debtorPbkBean;
	}

	public List<Installment> getInstallmentList() {
		return installmentList;
	}

	public void setInstallmentList(List<Installment> installmentList) {
		this.installmentList = installmentList;
	}

}
