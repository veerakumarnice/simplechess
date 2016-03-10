<%@ page language="java" %>
<% String title = "Chess game"; %>

<%@ include file="head.jsp" %>

<body>
	<%
		String player1 = request.getParameter("player1");
		String player2 = request.getParameter("player2");
	%>
	<script type="text/javascript">
		var player1 = "<%=player1 %>";
		var player2 = "<%=player2 %>";
	</script>

	<% String heading = "Simple Chess";%>
	<%@ include file="header.jsp" %>
	<table>
		<tbody>
			<tr>
				<td id="pel"></td><td id="onm"></td><td id="turn"></td><td id="myP"></td> 
			</tr>
		</tbody>
	</table>
		<div class="container">
			<div class ="left" >
				<div  id="leftboard"></div>
				<div  id="moveboard">
				<table>
				<tbody>
					<td id="whitemove">
					</td>
					<td id="blackmove"></td>
				</tbody>
				</table>
				</div>
			</div>
			<div class="board"> 
				<table id="chessTable" class="cboard">
				</table>
			</div>
		</div>

		<span id="test"></span>
	
	<script type="text/javascript" src="js/broadCast.js"></script>
	<script type="text/javascript" src="js/gameSetUp.js"></script>
	<script type="text/javascript" src="js/chessMoves.js"></script>
</body>
</html>