<%@ page language="java" %>

<% String title = "Simple Chess";	%>
<%@ include file="head.jsp" %>

<body>
	<%@ include file="checkSessionIndex.jsp"%>
	<% String heading = "Welcome to Simple Chess INDEX";%>
	<%@ include file="header.jsp" %>
	<script type="text/javascript">
		document.addEventListener("DOMContentLoaded", function (event) {
			var loginbt = document.getElementById('lgout');
		//console.log("out "+ loginbt.innerHTML);
		if(loginbt.innerHTML.trim() == 'Login') {
			loginbt.style.visibility = "hidden";
		}
		

		});		
	</script>
	<form class="signupform" method="post" action="bean">
		<input type="text" name="email" placeholder="Email"> 
		<input type="password" name="pass" placeholder="password">
		<input type="text" name="firstname" placeholder="First Name">
		<input type="text" name="lastname" placeholder="Last Name">
		<input type="hidden" name="source"  value="signup"> 
		<input type="submit" value="submit" > 
	</form>

	<form class="loginform" method="post" action="bean">
		<input type="text" name="email" placeholder="Email"> 
		<input type="password" name="pass" placeholder="password">
		<input type="hidden" name="source"  value="login"> 
		<input type="submit" value="submit" > 
	</form>
	<br><br><div id="message">
		<% 

		%>
	</div>
	
</body>
</html>