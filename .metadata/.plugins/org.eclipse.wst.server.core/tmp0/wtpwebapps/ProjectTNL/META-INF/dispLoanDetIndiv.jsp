<%@taglib uri="/struts-tags" prefix="s"%><%@ page language="java"
	contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<s:actionerror />
<s:form action="dispLoanDetByLoanAccno" method="post"
	enctype="multipart/form-data" theme="simple">
	<table>
		<tr>
			<td style="width: 400px;"><h3 class="leftbox">
					<a href="homePage">HOME</a>
				</h3></td>
			<td>(For office use only)</td>
		</tr>
	</table>
	<div class="container">
		<div class="leftbox">
			LoanAccount No
			<s:textfield name="custLoanBean.loanAccno" />
		</div>
		<div class="rightbox">
			<s:submit value="Get Loan Details" />
		</div>
	</div>
</s:form>

	<div class="container">
		<div class="leftbox">
			Membership No ${custLoanBeanDto.memNum}
			<s:textfield name="custLoanBean.member.memNum" />
		</div>
		<div class="leftbox">
			LoanAccount No ${custLoanBeanDto.loanAccno}
			<s:textfield name="custLoanBean.loanAccno" />
		</div>
		<div class="rightbox">
			Loan Date (MM/dd/yyyy)${custLoanBeanDto.dateOfLoan}
			<s:textfield name="custLoanBean.dateOfLoan" />
		</div>
	</div>

	<div class="colorbox-center">
		<h3 class="para-class">LOAN APPLICATION FORM</h3>
	</div>
	<div class="colorbox-left">
		<h3 class="colorbox-center">LOAN DETAILS</h3>
	</div>
	<div class="box2">
		<s:iterator value="collList" var="collateralName" status="st">
			<table>
				<tr>
					<td><s:checkbox name="custLoanBean.collGvnList.collName"
							fieldValue="%{collateralName}" /></td>
					<td><s:property value="%{#st.count}" /></td>
					<td><s:property value="collateralName" /></td>
				</tr>
			</table>
		</s:iterator>

		<table>
			<tr>
				<td><b>Payment Details</b></td>
			</tr>
			<tr>
				<td>Repayment Period in month: ${custLoanBeanDto.repayPeriod} <s:textfield
						name="custLoanBean.repayPeriod" /></td>
				<td>Payment Frequency ${custLoanBeanDto.pmtFrequency} <s:select
						name="custLoanBean.pmtFrequency"
						list="{'daily','weekly','monthly'}" multiple="false" /></td>
			</tr>
			<tr>
				<td>Mode of Deposit ${custLoanBeanDto.depositMode}<s:select
						name="custLoanBean.depositMode"
						list="{'By Cash','By DD/Cheque', 'By Phone','By Netbanking','By Debit/Credit Cards'}"
						multiple="false" /></td>
			</tr>
			<tr>
				<td>Mode of Repayment - By Cash</td>
			</tr>
			<tr>
				<td>By DD/Cheque:</td>
			</tr>
			<tr>
				<td>Cheque/DD No. ${custLoanBeanDto.chqNum}<s:textfield
						name="custLoanBean.chqNum" /></td>
				<td>Cheque Date<s:textfield name="custLoanBean.chqDate" /></td>
			</tr>
			<tr>
				<td>Name of Bank ${custLoanBeanDto.bankName}<s:textfield
						name="custLoanBean.bankName" /></td>
				<td>Branch ${custLoanBeanDto.bankBrnchDet}<s:textfield
						name="custLoanBean.bankBrnchDet" /></td>
			</tr>
			<tr>
				<td>PAN Detail</td>
			</tr>
		</table>
		<div id="box3" class="rightbox">
			Upload Self Attested Photographs <br> Signature or thumb
			impression <img id="previewImg1"
				src="data:image/jpg;base64,${custLoanBeanDto.base64AppImage}"
				alt="Placeholder" width="240" height="300" />

		</div>
	</div>


	<div class="centerbox1">
		<div class="colorbox-left">
			<h3 class="colorbox-center">ID & ADDRESS PROOF</h3>
		</div>
		<div class="colorbox-left">
			<h3 class="colorbox-center">(Please Fill In Hindi / English )</h3>
		</div>
	</div>


	<table id="table1">
		<tr>
			<td>Applicant Name: <s:textfield
					name="custLoanBean.applicantName" style="width:170px;" theme="simple" value="%{custLoanBeanDto.applicantName}"/></td>
			<td>D.O.B.: (MM/dd/yyyy) <s:textfield
					name="custLoanBean.applicantDob" theme="simple" value="%{custLoanBeanDto.applicantDob}"/></td>
			<td>Sex: <s:select
					list="{'Male','Female'}" name="custLoanBean.applicantSex"
					multiple="false" theme="simple" value="%{custLoanBeanDto.applicantSex}"/></td>
		</tr>
		<tr>
			<td>Father's/Husbands Name: ${custLoanBeanDto.applicantFather}<s:textfield
					name="custLoanBean.applicantFather" style="width:120px;" theme="simple"/></td>
		</tr>
		<tr>
			<td>Mother's Name: ${custLoanBeanDto.applicantMother}<s:textfield
					name="custLoanBean.applicantMother" style="width:120px;" theme="simple"/></td>
		</tr>
		<tr>
			<td>Occupation: ${custLoanBeanDto.applicantOccupation}<s:select
					name="custLoanBean.applicantOccupation"
					list="{'Business','Govt. Service', 'Pvt. Service','Agriculture','Others'}"
					multiple="false" theme="simple"/></td>
		</tr>
		<tr>
			<td>Applicant's Present Address:
				${custLoanBeanDto.applicantAddress}<s:textfield
					name="custLoanBean.applicantAddress" style="width:100px;" theme="simple"/>
			</td>
		</tr>
		<tr>
			<td>Applicant's City: ${custLoanBeanDto.applicantCity}<s:textfield
					name="custLoanBean.applicantCity" theme="simple"/></td>
			<td>Applicant's State: ${custLoanBeanDto.applicantState}<s:textfield
					name="custLoanBean.applicantState" theme="simple"/></td>
			<td>Applicant's Pin: ${custLoanBeanDto.applicantPin}<s:textfield
					name="custLoanBean.applicantPin" theme="simple"/></td>
		</tr>
		<tr>
			<td>Applicant's PAN No:${custLoanBeanDto.applicantPan} <s:textfield
					name="custLoanBean.applicantPan" theme="simple"/></td>
			<td>Applicant's Mobile No: ${custLoanBeanDto.applicantMobile}<s:textfield
					name="custLoanBean.applicantMobile"
					label="Applicant's Mobile Number" theme="simple"/></td>
		</tr>
		<tr>
			<td>Nominee's Name: ${custLoanBeanDto.nomineeName}<s:textfield
					name="custLoanBean.nomineeName" theme="simple"/></td>
			<td>Nominee's Age: ${memberBean.nomineeAge}<s:textfield
					name="memberBean.nomineeAge" theme="simple"/></td>
			<td>Relationship: ${custLoanBeanDto.relnWithNominee}<s:textfield
					name="custLoanBean.relnWithNominee" theme="simple"/></td>
		</tr>
		<tr>
			<td>Nominee's Address: ${custLoanBeanDto.nomineeAddress}<s:textfield
					name="custLoanBean.nomineeAddress" theme="simple"/></td>
		</tr>
	</table>

	<div class="page-break">
		<div>
			<span>page break</span>
		</div>
	</div>
	<div>
		<h3 class="para-class">
			KYC DOCUMENTATION CHECKLIST FORM (FOR LOAN)<br>
		</h3>
	</div>
Membership No..................................(Required document to be collected and tick-marked asper KYC Normsof Govt of India)
<div id="box6">
		<div class="centerbox1">
			<div class="colorbox-left">
				<h3 class="colorbox-center">ID & ADDRESS PROOF.</h3>
			</div>
			<div class="colorbox-left">
				<h3 class="colorbox-center">Please Tick</h3>
			</div>
		</div>

		<s:iterator value="documentList" var="docName" status="st">

			<s:if test="#st.Even">
				<div class="floatLeft">
					<table>
						<tr>
							<td><s:checkbox name="custLoanBean.documents.checkbox"
									fieldValue="%{docName}" /></td>
							<td><s:property value="docName" /></td>
							<td><s:textfield name="custLoanBean.documents.textbox" /></td>
						</tr>
					</table>
				</div>
			</s:if>
			<s:else>
				<div class="floatRight">
					<table>
						<tr>
							<td><s:checkbox name="custLoanBean.documents.checkbox"
									fieldValue="%{docName}" /></td>
							<td><s:property value="docName" /></td>
							<td><s:textfield name="custLoanBean.documents.textbox" /></td>
						</tr>
					</table>
				</div>
			</s:else>
		</s:iterator>


	</div>
Documents shall not be more than two months old.<br>
(i)Telephone Bill (ii)Bank A/c Statement (iii)Electricity Bill<br>


	<h3 class="leftalign-class">
		General Terms And Conditions<br>
	</h3>
1.  Application form must be completed in full BLOCK letters in ENGLISH/HINDI. Application which is incomplete is liable to be rejected.<br>
2.  Application for membership fee should be submitted with Rs100.00/-<br>
3.  This application form is for own use only and only applicable to members.<br>
4.  The company may at any time alter, vary, add to or delete from these terms and conditions on account of <br>
    government policy as applicable from time to time or otherwise by notifying of company's notice board or by <br>
    publication on the newspapers.<br>
5.  The company reserves the right to reject any application without showing any reason.<br>
6.  Disputes if any arising in connection to the deposit scheme will be subjected to the jurisdiction of Allahabad Court.<br>
7.  In case of non-payment of the depositor part thereof as per the terms and conditions of such deposit, the <br>
    depositor may approach the Registrar of companies Uttar Pradesh and Uttarakhand.<br>
8.  In case of any deficiency of Nidhi is servicing its depositors, the depositor may approach the National Consumers<br>
    Disputes Redressal Forum, the State Consumers Disputes Redressal Forum or District Consumers Disputes <br>
    Redressal Forum, as the case may be, for redressal of this relief.<br>
9.  The Financial poistion of Nidhi as disclosed and the representations made in the application form are true and <br>
    correct and that Nidhi has complied with all the applicable rules.<br>
10. A statement to the effect that the Central Governement does not undertake any responsibility for the financial<br>
    soundness of Nidhi or for the correctness of any of the statement or the representatiuons made or opinions <br>
    expressed by Nidhi.
11. The deposits accepted by Nidhi are not insured and the repayment of deposit is not guaranteed by either the <br>
    Central Government or the Reserve Bank of India.
    
<h3 class="leftalign-class">
		DECLARATION / VERIFICATION<br>
	</h3>     
1.  I hereby declare that based on the facts represented here in by myself to Triveni Nidhi Limited for my<br>
    appointment as member is true as to my based knowledge and<br> hereby abide by the following rules and<br>
    regulations of the company.<br>
2.  I hereby declare that the declarations made by me are correct and have been explained everything related to the<br>
    above account in the language known to me also I agree to abide by the rules and regulations of the company and I <br>
    shall never request anything against the terms, tenure and conditions of the scheme in letter and spirit. I also certify<br>
    that all the information particulars given by me are true to the best of my knowledge and belief.<br>
3.  I have read and understood the financial and other particulars furnished and representations made by Nidhi in this<br>
    application form and after careful consideration I am making the deposit with Nidhi at my own risk and violation.<br>
	<br>
	<br>
	<div id="wrapper">
		<div id="box7" class="rightbox">
			<img id="previewSign3"
				src="data:image/jpg;base64,${custLoanBeanDto.base64MemSign}"
				alt="Placeholder" width="240" height="300" /><br>Acceptance by
			Member
		</div>
		<div id="box8" class="leftbox">
			<s:textfield name="custLoanBean.grntrDet" />
			<br>Guarantor Detail
		</div>
		<div id="box9" class="rightbox">
			<img id="previewSign3"
				src="data:image/jpg;base64,${custLoanBeanDto.base64GrntrSign}"
				alt="Placeholder" width="240" height="300" /><br>Authorized
			Employee/Officer
		</div>
		<div id="box10" class="centerbox">
			<img id="previewSign4"
				src="data:image/jpg;base64,${custLoanBeanDto.base64EmpSign}"
				alt="Placeholder" width="240" height="300" /><br>Guarantor
			Signature
		</div>
	</div>






