<%@ page language="java" %>
<% String title = "Chess game"; %>

<%@ include file="head.jsp" %>

<body>
	
	<% String heading = "Simple Chess";%>
	<%@ include file="header.jsp" %>
	<%@ include file="cbody.jsp" %>
	
	</script>
	<input id="textmessage" type="text">
	<button onclick="sendMessage(this)" >Send</button>
	<button onclick="closeConnect()" >Close</button>
	<!--button onclick="getUsers()">Get Users</button-->
	<button onclick="assignRequest()">assign</button>
	<script type="text/javascript" src="js/socket.js"></script>


</body>
</html>