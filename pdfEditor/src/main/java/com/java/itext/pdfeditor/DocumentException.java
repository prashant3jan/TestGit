package com.java.itext.pdfeditor;

public class DocumentException extends Exception  {
	private String msg;
	public DocumentException(String msg) {
		this.msg = msg;
		}
		public String getMessage() {
		return msg;
		}
}
