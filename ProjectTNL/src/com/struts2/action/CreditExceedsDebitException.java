package com.struts2.action;

public class CreditExceedsDebitException extends Exception {
	private String msg;
	public CreditExceedsDebitException(String msg) {
		this.msg = msg;
		}
		public String getMessage() {
		return msg;
		}
}


