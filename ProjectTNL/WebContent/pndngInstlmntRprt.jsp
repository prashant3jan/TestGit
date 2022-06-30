<%@taglib uri="/struts-tags" prefix="s"%><%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags"%>

<s:actionerror/>
<table>
<tr><td style="width:400px;"><h3 class="leftbox"><a href="homePage">HOME</a></h3></td></tr>
</table>
<h2>Pending Installment Report</h2>
<div class="zui-wrapper">
    <div class="zui-scroller">
        <table class="zui-table">
            <thead>
                <tr>	
                	<th class="zui-sticky-col">LoanAccountNo</th>
					<th>Applicant Name</th>
					<th>Applicant Address</th>
					<th>Applicant City</th>
					<th>Applicant State</th>
					<th>Applicant Pin</th>
					<th>Applicant Mobile</th>
					<th>Loan Re-payment Period</th>
					<th>Payment Frequency</th>
					<th>Total Loan With Interest</th>
					<th>Loan Status</th>
					<th>Total Payment Given By Customer Till Date</th>
					<th>Date Of Loan</th>
					<th>Current Date </th>
					<th>Total Number of Installments Due Till Date</th>
					<th>Installment Amount</th>
					<th>Total Payment Due Till Date</th>
					<th>Outstanding Balance </th>
					</tr>
            </thead>
            <tbody>
            <s:iterator value="pndngInstlmntRprtList" >
				<tr>
					<td class="zui-sticky-col"><s:property value="loanAccno"/></td>
					<td><s:property value="applicantName" /></td>
					<td><s:property value="applicantAddress"/></td>
					<td><s:property value="applicantCity" /></td>
					<td><s:property value="applicantState"/></td>
					<td><s:property value="applicantPin"/></td>
					<td><s:property value="applicantMobile"/></td>
					<td><s:property value="repayPeriod"/></td>
					<td><s:property value="pmtFrequency"/></td>
					<td><s:property value="totLoanWithInterest"/></td>
					<td><s:property value="loanStatus"/></td>
					<td><s:property value="totPmtGvnByCustTillDate"/></td>
					<td><s:property value="dateOfLoan"/></td>
					<td><s:property value="currentDate"/></td>
					<td><s:property value="totNoOfInstlmntDueTillDate"/></td>
					<td><s:property value="totPmtDueTillDate"/></td>
					<td><s:property value="outstandingBalance"/></td>
				</tr>	
			</s:iterator>
		</tbody>
	</table>
</div>
</div>

