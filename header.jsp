<%@ page language="java" %>
<header>
		<h1>
			<%=heading %>
		</h1>
		<button id="lgout" onclick="logout(this)" class="button" value="Login"> 
			<%
				String user;
				user = (String)session.getAttribute("username");
				if(user == null) {
				user = "Login";
			}
			%>
			<%=user %>
		</button>
		<script> 
			var loggedin;
			if ((loggedin = document.getElementById('lgout')).innerHTML != 'Login') {
				loggedin.value = "Logout";
			}
		</script>
	</header>