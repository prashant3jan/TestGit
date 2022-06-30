<%@ taglib prefix="s" uri="/struts-tags" %>
<table>
<tr><td style="width:400px;"><h3 class="leftbox"><a href="homePage">HOME</a></h3></td></tr>
</table>
<s:actionerror />
<s:form action="addThumbImprssnDet" namespace="/" method="post" enctype="multipart/form-data">
<table id="thmbImprssn">
<tr><td><h3>Upload Member Thumb Impression</h3></td></tr>
<tr><td>Select Member Thumb Impression File</td><td><s:file name="thumbImprssnBean.thumbImprssn" /><br></td></tr>
<tr><td>Applicant Name: </td><td><s:textfield name="thumbImprssnBean.applicantName" /></td></tr>
<tr><td>LoanAccount No </td><td><s:textfield name="thumbImprssnBean.custloanbean.loanAccno"  /></td></tr>
<tr><td>Note: If check is not received on time or if the check bounces the customer will have to pay<br>
       1% or 2% everyday of the installment amount. The customer will also need to pay Rs 500.00 or Rs 1000.00 as check bounce charge</td></tr>
<tr><td>I have read and agree to all the rules and terms written on the loan form</td></tr>
<tr><td>Member Signature Upload </td><td><s:file name="thumbImprssnBean.memSign" /></td></tr>
<tr><td>Thumb Impression Date</td><td><s:textfield name="thumbImprssnBean.thumbImprssnDate" /></td></tr>
<tr><td>Check is less than Installment Amount By Rs<s:textfield name="thumbImprssnBean.instlmntAmtDeficit" /></td></tr>
<tr><td>Check is equal to the Installment Amount<s:select name="thumbImprssnBean.checkEqualsInstlAmt" list="{'Yes','No'}" multiple="false" /></td></tr>
<tr><td>Amount received by first check <s:textfield name="thumbImprssnBean.firstCheckAmt" /></td></tr>
<tr><td>Amount received by remaining checks <s:textfield name="thumbImprssnBean.remainingCheckAmt" />
</table>
<s:submit value="submit" name="Upload"/>
</s:form>

<div class="page-break"><div><span>page break</span></div></div> 

<h2>Thumb Impression Upload Details</h2>
<div class="zui-wrapper">
    <div class="zui-scroller">
        <table class="zui-table">
            <thead>
                <tr>	
                	<th class="zui-sticky-col">ThumbImprssn Id</th>
                	<th>ThumbInprssnFile</th>
                	<th>Applicant_Name</th>
                	<th>Loan Account Number</th>
                	<th>Member Sign</th>
                	<th>Thumb Imprssn Date</th>
                	<th>Instllmnt Amt Deficit</th>
                	<th>Check Equals Instllmnt Amt</th>
                	<th>First Check Amt</th>
                	<th>Remaining Check Amt</th>
                	<th>Delete</th>
				</tr>
            </thead>
            <tbody>
            <s:iterator value="thumbInprssnList" var="thumbImprssn">
				<tr>
					<td class="zui-sticky-col"><s:property value="thumbImprssnId"/></td>
						<td><s:property value="thumbImprssn"/></td>
						<td><s:property value="applicantName"/></td>
						<td><s:property value="custloanbean.loanAccno"/></td>
						<td><s:property value="memSign"/></td>
						<td><s:property value="thumbImprssnDate"/></td>
						<td><s:property value="instlmntAmtDeficit"/></td>
						<td><s:property value="checkEqualsInstlAmt"/></td>
						<td><s:property value="firstCheckAmt"/></td>
						<td><s:property value="remainingCheckAmt"/></td>
					    <td><a href="deleteThumbImprssnDet?thumbImprssnId=<s:property value="thumbImprssnId"/>">Delete</a></td>
				</tr>	
			</s:iterator>
		</tbody>
	</table>
</div>
</div>
