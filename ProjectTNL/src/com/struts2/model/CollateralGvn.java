package com.struts2.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "coll_gvn")
public class CollateralGvn {
	@Id
	@GeneratedValue
	@Column(name="coll_id")
	private int coll_id;
	@Column(name="coll_name")
	private String collName;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="loanAccno")
	private CustLoanBean loan;
	public int getColl_id() {
		return coll_id;
	}
	public void setColl_id(int coll_id) {
		this.coll_id = coll_id;
	}
	public String getCollName() {
		return collName;
	}
	public void setCollName(String collName) {
		this.collName = collName;
	}
	public CollateralGvn(){ }
	public CollateralGvn(int _coll_id, String _collName,CustLoanBean _loan ){
		this.coll_id = _coll_id;
		this.collName = _collName;
		this.loan = _loan;
	}
}
