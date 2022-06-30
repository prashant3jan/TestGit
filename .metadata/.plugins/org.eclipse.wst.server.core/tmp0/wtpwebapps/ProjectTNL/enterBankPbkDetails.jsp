<%@taglib uri="/struts-tags" prefix="s"%><%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<s:actionerror/>
<table>
<tr><td style="width:400px;"><h3 class="leftbox"><a href="homePage">HOME</a></h3></td></tr>
<tr><td style="width:400px;"><h2>TNL BANK PASSBOOK</h2></td></tr>
</table>
<div id="float-container">
			<div id="content">
			<s:form action="commit" method="post" theme="simple" >
						<h2>Enter Bank Deposits</h2>
						<div>Transaction Date<s:textfield name="transacBean.tranDate" /></div>
						<div>Loan Account No<s:textfield name="transacBean.loan.loanAccno" /></div>
	 					<div>Name of Depositor(debtor)<s:textfield name="transacBean.debtorName" /></div>
	 					<div>Mode Of Deposit<s:select list="{'UPI','IMPS','NEFT','CHEQUE','CASH'}" name="transacBean.pmtMode" multiple="false" /></div>
	 					<div>Cheque Number / NEFT Transaction Number<s:textfield name="transacBean.chequeNo" /></div>
	 					<div>Amount Deposited in Fig <s:textfield name="transacBean.debitAmountFig" /></div>
	 					<div>Amount Deposited in Words <s:textfield name="transacBean.debitAmountWords" /></div>
	 					<div>Name Of Recipient<s:textfield name="transacBean.recipient" /></div>
	 					<s:submit value="submit" method="addTran"/>
	 		</s:form>
			</div>
			<div id="sidebar">
			<s:form action="commit" method="post" theme="simple" >
			<h2>Enter Credit Given by Company</h2>
						<div>Transaction Date<s:textfield name="transacBean.tranDate" /></div>
						<div>Loan Account No<s:textfield name="transacBean.loan.loanAccno" /></div>
	 					<div>Creditor Name/ Organization <s:textfield name="transacBean.creditorName" /></div>
	 					<div>Mode Of Deposit <s:select list="{'UPI','IMPS','NEFT','CHEQUE','CASH'}" name="transacBean.pmtMode" multiple="false" /></div>
	 					<div>Cheque Number / NEFT Transaction Number<s:textfield name="transacBean.chequeNo" /></div>
	 					<div>Amount Given in Fig (Credited)<s:textfield name="transacBean.creditAmountFig" /></div>
	 					<div>Amount Given in Words (Credited)<s:textfield name="transacBean.creditAmountWords" /></div>
	 					<div>Name Of Recipient<s:textfield name="transacBean.recipient" /></div>
	 					<s:submit value="submit" method="addTran" />
	 					</s:form>
			</div>
		

</div>

<h2>Transactions Details</h2>
<div class="zui-wrapper">
    <div class="zui-scroller">
        <table class="zui-table">
            <thead>
                <tr>	
                	<th class="zui-sticky-col">Transaction Date</th>
                	<th>Loan Account No.</th>
					<th>Depositor Name</th>
					<th>Creditor Name/ Organization</th>
					<th>Mode Of Deposit/Payment</th>
					<th>Cheque / NEFT Number</th>
					<th>Amount Deposited</th>
					<th>Amount Credited as Loan</th>
					<th>Recipient Name</th>
					<th>Total Debit</th>
					<th>Total Credit</th>
					<th>Balance</th>
					<th>Delete</th>
					</tr>
            </thead>
            <tbody>
            <s:iterator value="transacList" var="transac">
				<tr>
					<td class="zui-sticky-col"><s:property value="tranDate"/></td>
					<td><s:property value="loan.loanAccno" /></td>
					<td><s:property value="debtorName" /></td>
					<td><s:property value="creditorName" /></td>  
					<td><s:property value="pmtMode"/></td>
					<td><s:property value="chequeNo"/></td>
					<td><s:property value="debitAmountFig"/></td>
					<td><s:property value="creditAmountFig"/></td>
					<td><s:property value="recipient"/></td>
					<td><s:property value="totalDebit"/></td>
					<td><s:property value="totalCredit"/></td>
					<td><s:property value="balance"/></td>
					<td><a href="tranDelete?tranId=<s:property value="tranId"/>">Delete</a></td>
				</tr>	
			</s:iterator>
		</tbody>
	</table>
</div>
</div>
