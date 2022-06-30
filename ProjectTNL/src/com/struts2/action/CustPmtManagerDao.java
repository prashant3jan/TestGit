package com.struts2.action;

import java.util.List;
import com.struts2.model.CustPmtBean;

public class CustPmtManagerDao {

public interface UploadManagerDAO{
	Integer saveUploadFileDetails(CustPmtBean payment);
	List<CustPmtBean> getPaymentList();
}
}