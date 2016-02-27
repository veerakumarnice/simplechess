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
	</header>