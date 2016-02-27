<%@ page language="java" import="javax.servlet.*" %>
<% 
String title = "Home";
%>
<%@ include  file="head.jsp"%> 
<body>
	
 	<% String heading = "Welcome to simple chess HOME";%>
	<%@ include file="header.jsp"%>

	<script type="text/javascript">
		function logout(source) {
			if(source.value == "Login") {
				alert("logging in");
				source.value = 'Logout';
			}
			else if (source.value == "Logout"){
				//alert("logging out");
				var xhttp;
				if(window.XMLHttpRequest) {
					xhttp = new XMLHttpRequest();
					console.log("XMLHttpRequest object created");
				}
				else  {
					xhttp = new ActiveXObject("Microsoft.XMLHTTP");
				}

				xhttp.onreadystatechange = function() {
					console.log("readystate: " +xhttp.readystate );
					console.log(xhttp);
					if(xhttp.readyState == 4 && xhttp.status==200) {
						source.value = 'Login';
						var json = JSON.parse(xhttp.responseText);
						window.location.href = json.url;
					}
				}

				xhttp.open("GET","bean?logout=true",true);
				xhttp.send();				
			}
		}

	</script>

</body>
</html>