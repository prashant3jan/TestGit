package com.struts2.model;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "document")
public class Document {
	@Id
	@GeneratedValue
	@Column(name="doc_id")
	private int doc_id;
	@Column(name="doc_name")
	private String docName;
	
	public int getDoc_id() {
		return doc_id;
	}
	public void setDoc_id(int doc_id) {
		this.doc_id = doc_id;
	}
	
	public String getDocName() {
		return docName;
	}
	public void setDocName(String docName) {
		this.docName = docName;
	}
	public Document(){}
	public Document(int _doc_id, String _docName){
		this.doc_id = _doc_id;
		this.docName = _docName;
	}
	
}
