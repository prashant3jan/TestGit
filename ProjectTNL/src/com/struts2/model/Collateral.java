package com.struts2.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "collateral")
public class Collateral {
	@Id
	@GeneratedValue
	@Column(name="coll_id")
	private int coll_id;
	public int getColl_id() {
		return coll_id;
	}
	public void setColl_id(int coll_id) {
		this.coll_id = coll_id;
	}
	public String getCollateralName() {
		return collateralName;
	}
	public void setCollateralName(String collateralName) {
		this.collateralName = collateralName;
	}
	@Column(name="coll_name")
	private String collateralName;
	

	public Collateral(){}
	public Collateral(Integer _coll_id, String _collateralName){
		this.coll_id = _coll_id;
		this.collateralName = _collateralName;
	}
	
}
