<%@ page pageEncoding="ISO-8859-1"%>

<html>
<title>EJBCA Certificate Enroll Error</title>
<body>

<%@ page isErrorPage="true" %>

<h2>EJBCA Certificate Enroll Error</h2>
<%
String isException = (String)request.getAttribute("Exception");
String errMsg = (String)request.getAttribute("ErrorMessage");
if ( (isException != null) && (isException.equals("true")) ) {
%>
An Exception occured:
<hr>
<%
}
if (errMsg != null) {
%>
<%=errMsg%>
<%
} else {
%>
Unknown error, or you came to this page directly without beeing redirected.
<%
}
%>
<p>
<a href="javascript:history.back()">Go back</a>
</body>
</html>
