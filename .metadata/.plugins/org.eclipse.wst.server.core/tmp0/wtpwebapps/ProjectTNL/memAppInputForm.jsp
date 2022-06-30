<%@taglib uri="/struts-tags" prefix="s"%><%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<table>
<tr><td style="width:400px;"><h3 class="leftbox"><a href="homePage">HOME</a></h3></td></tr>
</table>
<s:actionerror/>
<s:form action="addMemInputDet" method="post" enctype="multipart/form-data" theme="simple" >
<div id="colorbox"><h3 class="para-class" >DETAILS OF MEMBERS</h3></div>
	
<div><h3 class="para-class" >PLEASE FILL-UP THE FORM IN ENGLISH CAPITAL LETTERS</h3></div>
		
<div id="wrapper" >
<div class="container_noborder" >
  	<div  class="leftbox">Applicant Name</div>
  	<div  class="rightbox">Applicant Age: <s:textfield name="memberBankBean.applicantAge" /></div>
</div>
<div class="container_noborder" >
	<div class="leftbox">Father's Name: </div>
	<div class="rightbox">Father's Age: <s:textfield name="memberBankBean.fatherAge" /></div>
</div>
<div class="container_noborder" >
<div class="leftbox">Mother's Name: </div>
<div class="rightbox">Mother's Age: <s:textfield name="memberBankBean.motherAge" /></div>
</div>
<div class="container_noborder">
<div class="leftbox">Spouse(Wife/Husband) Name: <s:textfield name="memberBankBean.spouseName" /></div>
<div class="rightbox">Spouse Age: <s:textfield name="memberBankBean.spouseAge" /></div>
</div>
<div class="container_noborder">
<div class="leftbox">WIFE'S PATERNAL ADDRESS<s:textfield name="memberBankBean.applicantWifePaternalAddress" /></div>
</div>
<div class="container_noborder">
<div class="leftbox">SHOP NAME AND ADDRESS WITH THE LANDMARK <s:textfield name="memberBankBean.applicantShopAddressWithLandmark" /></div>
</div>
<div class="container_noborder">
<div class="leftbox">RESIDENTIAL ADDRESS WITH THE LANDMARK </div>
</div>
<div class="container_noborder">
<div class="leftbox">LOCAL ADDRESS WITH THE LANDMARK <s:textfield name="memberBankBean.applicantLocalAddressWithLandmark" /></div>
</div>

<div  class="container_noborder">
	<div class="leftbox">BANK NAME <s:textfield name="memberBankBean.appBankName" /></div>
	<div class="rightbox">BRANCH NAME <s:textfield name="memberBankBean.appBankBranch" /></div>
</div>
		
<div class="container_noborder">
	<div class="leftbox">ACCOUNT NUMBER<s:textfield name="memberBankBean.appBankAccno" /></div>
	<div class="rightbox">IFSC CODE<s:textfield name="memberBankBean.appBankIFSC" /></div>
</div>
<div  class="container_noborder">
 <div class="leftbox"> MEMBERSHIP NO. <s:textfield name="memberBankBean.memNum" /></div>
 <div class="rightbox"> PAYMENT MODE <s:select name="loanBean.depositMode" list="{'By Cash','By DD/Cheque', 'By Phone','By Netbanking','By Debit/Credit Cards'}" multiple="false" /></div>
 </div>
 <div  class="container_noborder">
<div class="leftbox">APPLICANT MO</div>
<div class="rightbox">CO-APPLICANT MO<s:textfield name="memberBankBean.coAppMobile" /></div>
</div>
<div  class="container_noborder">
<div class="leftbox">CO-APPLICANT RESIDENTIAL ADDRESS WITH LANDMARK: <s:textfield name="memberBankBean.coAppResAddWithLandmark" /></div>
</div>
<div  class="container_noborder">
<div class="leftbox">APPLICANT SIGNATURE</div>
<div class="rightbox"><s:submit value="Add Member" align="center"  /></div>
</div>
</div>
</s:form >
<div class="page-break"><div><span>page break</span></div></div> 

<h2>Members</h2>
<div class="zui-wrapper">
    <div class="zui-scroller">
        <table class="zui-table">
            <thead>
                <tr>	
                	<th class="zui-sticky-col">Membership No</th>
					<th>Applicant Age</th>
					<th>Father's Age</th>
					<th>Mother's Age</th>
					<th>Spouse(Wife/Husband) Name</th>
					<th>Spouse Age</th>
					<th>SHOP NAME AND ADDRESS WITH THE LANDMARK</th>
					<th>LOCAL ADDRESS WITH THE LANDMARK </th>
					<th>BANK NAME</th>
					<th>BRANCH NAME</th>
					<th>ACCOUNT NUMBER</th>
					<th>IFSC CODE</th>
					<th>PAYMENT MODE</th>
					<th>CO-APPLICANT MO</th>
					<th>CO-APPLICANT RESIDENTIAL ADDRESS WITH LANDMARK</th>
					<th>Delete</th>
					</tr>
            </thead>
            <tbody>
            <s:iterator value="memberBankInfoList" var="member">
				<tr>
					<td class="zui-sticky-col"><s:property value="memNum"/></td>
					<td><s:property value="applicantAge" /></td>
					<td><s:property value="fatherAge"/></td>
					<td><s:property value="motherAge" /></td>
					<td><s:property value="spouseName"/></td>
					<td><s:property value="spouseAge"/></td>
					<td><s:property value="applicantShopAddressWithLandmark"/></td>
					<td> <s:property value="applicantLocalAddressWithLandmark"/> </td>
					<td><s:property value="appBankName"/></td>
					<td><s:property value="appBankBranch"/></td>
					<td><s:property value="appBankAccno"/></td>
					<td><s:number name="appBankIFSC" /></td>
					<td><s:property value="depositMode"/></td>
					<td><s:property value="coAppMobile"/></td>
					<td><s:property value="coAppResAddWithLandmark"/></td>
					<td><a href="memBnkInfoDelete?membership_no=<s:property value="memNum"/>">Delete</a></td>
				</tr>	
			</s:iterator>
		</tbody>
	</table>
</div>
</div>


