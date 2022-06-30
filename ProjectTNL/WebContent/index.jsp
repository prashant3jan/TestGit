<%@ taglib prefix="s" uri="/struts-tags"%>

<s:actionerror />
<s:form action="authlogin" method="post">
		<s:textfield name="username" label="Username" size="20" />
		<s:password name="password" label="Password" size="20" />
		<s:submit method="execute" value="Login" align="center" />
</s:form>

