<%@taglib uri="/struts-tags" prefix="s"%><%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags"%>

<s:actionerror/>
<table>
<tr><td style="width:400px;"><h3 class="leftbox"><a href="homePage">HOME</a></h3></td></tr>
</table>
<s:form action="addPassBookDet" method="post" enctype="multipart/form-data" theme="simple" >
<div class="container">
	<div class="leftbox"> Loan Account No <s:textfield name="debtorPbkBean.loanAccno" /></div>
	<div class="rightbox"><s:submit value="Get Details" /></div>
</div>
</s:form>
<div class="box4" >
<s:bean name="com.struts2.model.DebtorPassbook" var="debtorPbkBean" />
<div>Check Number: ${debtorPbkBean.chequeNo}</div>
<div>Account Name: ${debtorPbkBean.applicantName}</div>
<div>Father's Name / Husbands Name: ${debtorPbkBean.applicantFather}</div>
<div>Mother's Name: ${debtorPbkBean.applicantMother}</div>
<div>Address: ${debtorPbkBean.applicantAddress}, ${debtorPbkBean.applicantCity}, ${debtorPbkBean.applicantState}, ${debtorPbkBean.applicantPin}</div>
<div>Amount (in figure): ${debtorPbkBean.debitloanGvnByComp}</div>
<div>Installment Amount : ${debtorPbkBean.installment}</div>
<div>Opening day/date: ${debtorPbkBean.dateOfLoan}</div>
<div>Payment Frequency: ${debtorPbkBean.pmtFrequency}</div>
<div>Member Signature: ${debtorPbkBean.memSign}</div>
<div>Authorized Signature: ${debtorPbkBean.empSign}</div>
</div>
<div class="box4">
<table cellpadding="3" cellspacing="3" border="1">
<thead>
 <tr>
 <th>Loan Account No </th>
 <th>Installment Date</th>
 <th>Installment Amt</th>
 </tr>
 </thead>
 <tbody>
  <s:iterator value="installmentList" >
  <tr>
  <td><s:property value="loan.loanAccno"/></td>
  <td><s:property value="installmentDt"/></td>
  <td><s:property value="installmentAmt"/></td>
  </tr>
  </s:iterator>
</tbody>
</table>
</div>

