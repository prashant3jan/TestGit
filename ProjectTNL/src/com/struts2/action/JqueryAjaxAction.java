package com.struts2.action;

import com.opensymphony.xwork2.ActionSupport;

public class JqueryAjaxAction extends ActionSupport {
	private static final long serialVersionUID = 1L;
	private int principalAmount;
	private int interestRate;
	private int numberOfMonths;
	private int calLoanAmt;
	public String calculateLoanAmount() throws Exception{
		System.out.println("here1");
       return "success";
		
	}
	
	
	public int getPrincipalAmount() {
		return principalAmount;
	}


	public void setPrincipalAmount(int principalAmount) {
		this.principalAmount = principalAmount;
	}


	public int getInterestRate() {
		return interestRate;
	}


	public void setInterestRate(int interestRate) {
		this.interestRate = interestRate;
	}


	public int getNumberOfMonths() {
		return numberOfMonths;
	}


	public void setNumberOfMonths(int numberOfMonths) {
		this.numberOfMonths = numberOfMonths;
	}


	public int getCalLoanAmt() {
		return calLoanAmt;
	}


	public void setCalLoanAmt(int calLoanAmt) {
		this.calLoanAmt = calLoanAmt;
	}
}
