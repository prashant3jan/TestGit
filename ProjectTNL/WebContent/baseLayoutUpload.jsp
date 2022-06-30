<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
 

<html>
   <head>
      <meta http-equiv="Content-Type" content="text/html; charset = UTF-8">
      <title><tiles:insertAttribute name = "title" ignore="true" /></title>
      <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
     <link rel="stylesheet" type="text/css" media="all" href="/ProjectTNL/css/style_upload.css" />
   </head>
   <body id="home">
   <div id="wholepage">
   <div id="wrap">
   <div id="header">
	<tiles:insertAttribute name="header" />
	
	</div>
	
	<div id="content">
		<tiles:insertAttribute name="body" />
	</div>
	
	<div id="footer">
		<tiles:insertAttribute name="footer" />
	</div>
</div>
</div>
</body>
</html>