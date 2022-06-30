<%@taglib uri="/struts-tags" prefix="s"%><%@ page language="java"
	contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<s:actionerror />
<s:form action="dispMemberDetByMemNo" method="post"
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
			Membership No
			<s:textfield name="memberBean.memNum" />
		</div>
		<div class="rightbox">
			<s:submit value="Get Member Details" />
		</div>
	</div>
</s:form>

