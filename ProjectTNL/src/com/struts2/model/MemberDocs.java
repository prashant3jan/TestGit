package com.struts2.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="mem_docs")
public class MemberDocs {
	@Id
	@GeneratedValue
	@Column(name="id")
	private int id;
	@Column(name = "checkbox")
	private String checkbox;
	@Column(name = "textbox")
	private String textbox;	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mem_no")
    private Member member;
	public MemberDocs() { }
	 
    public MemberDocs(int _id, String _checkbox, String _textbox) {
    	this.id = _id;
        this.checkbox = _checkbox;
        this.textbox = _textbox;
       
    }
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCheckbox() {
		return checkbox;
	}
	public void setCheckbox(String checkbox) {
		this.checkbox = checkbox;
	}
	public String getTextbox() {
		return textbox;
	}
	public void setTextbox(String textbox) {
		this.textbox = textbox;
	}
	

}
