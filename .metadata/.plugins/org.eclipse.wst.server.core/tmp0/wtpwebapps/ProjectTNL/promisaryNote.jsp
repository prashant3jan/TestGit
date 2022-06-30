<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<table>
<tr><td style="width:400px;"><h3 class="leftbox"><a href="homePage">HOME</a></h3></td></tr>
</table>
<s:actionerror/>
<s:form action="addPrmsryDet" method="post" enctype="multipart/form-data" theme="simple" >
<div id="wrapper" >
<div class="box">
<div><h3 class="alignleft">Promissary Note</h3></div><div class="container"></div>
</div>
<br>

<p>We <s:textfield name="promisaryBean.applicantName" /></p>
<p>jointly & Separately Promise to Pay M/S TRIVENI NIDHI LIMITED- 11B/1A/2 Lala </p> 
<p>Ram Narayan Lal Road, Bank Road, Katra, Allahabad or order to whom pay sum of </p>
<p>Rs <s:textfield name="promisaryBean.loanAmount" /> with applicable interest rate as </p>
<p>recommended as per RBI circular.</p>
<br>
<table>
		<tr><td><div id="box8" class="alignleft"><s:textfield name="promisaryBean.grntrDet" /><br>Guarantor </div></td>
		<td><div id="box9" class="alignright"><s:file name="promisaryBean.appSign" id="fileinput1" onChange="previewFile1(this)" /><img id="previewSign1" src="#" alt="Placeholder" /><br>Applicant Thumb Imp. And Sign</div></td>
		<td><div id="box10" class="aligncenter"><s:file name="promisaryBean.grntrSign" id="fileinput2" onChange="previewFile2(this)" /><img id="previewSign2" src="#" alt="Placeholder" /><br>Guarantor Sign or Thumb Impression</div></td></tr>
</table>


<p>-------------------------------------------------------------------------------------------------------------------------------------------------</p>
<table>
<tr><td><p class="centerbox">Receipt</p></td></tr>
<tr><td><p>Received with thanks from Triveni Nidhi Limited Rs: <s:textfield name="promisaryBean.amtRcvdByRcpnt" /></p></td></tr>
<tr><td><p>by cash/check/D.D./through RTGS.</p></td></tr>
<tr><td><div class="container_noborder"><div class="alignleft">Loan A/c No. <s:textfield name="promisaryBean.custLoanBean.loanAccno" /></div><div class="alignright"> Date <s:textfield name="promisaryBean.dateOfLoan"></s:textfield></div></div></td></tr>
<tr><td><div class="container_noborder"><div class="alignleft">Guarantor</div></td><td><div class="alignright">Applicant Thumb Imp. And Sign</div></div></td></tr>
</table>
<br>
<br>
<s:submit value="Add Promisary Details" align="center"  />

</div>
</s:form>
<div class="page-break"><div><span>page break</span></div></div> 

<h2>Promisary Note</h2>
<div class="zui-wrapper">
    <div class="zui-scroller">
        <table class="zui-table">
            <thead>
                <tr>	
                	<th class="zui-sticky-col">Promissary ID</th>
					<th>Applicant Name</th>
					<th>Loan Amount</th>
					<th>Guarantor Details</th>
					<th>Guarantor Sign or Thumb Impression</th>
					<th>Applicant Thumb Imprssn. % Sign</th>
					<th>Amount received by recipient</th>
					<th>Loan A/c No</th>
					<th>Date of Loan </th>
					<th>Delete</th>
					</tr>
            </thead>
            <tbody>
            <s:iterator value="promissaryList" var="promisary">
				<tr>
					<td class="zui-sticky-col"><s:property value="prmssryId"/></td>
					<td><s:property value="applicantName" /></td>
					<td><s:property value="loanAmount"/></td>
					<td><s:property value="grntrDet" /></td>
					<td><s:property value="grntrSign"/></td>
					<td><s:property value="appSign"/></td>
					<td><s:property value="amtRcvdByRcpnt"/></td>
					<td><s:property value="custLoanBean.loanAccno"/></td>
					<td><s:property value="dateOfLoan"/></td>
					<td><a href="deletePrmsryDet?prmssryId=<s:property value="prmssryId"/>">Delete</a></td>
				</tr>	
			</s:iterator>
		</tbody>
	</table>
</div>
</div>
