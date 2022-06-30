package com.java.itext.pdfeditor;

public class BadElementException extends Exception {
	private String msg;
	public BadElementException(String msg) {
		this.msg = msg;
		}
		public String getMessage() {
		return msg;
		}

}
