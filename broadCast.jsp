<%@ page language="java" %>
<% String title = "Chess game"; %>

<%@ include file="head.jsp" %>

<body>
	
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
				<div  id="moveboard"></div>
			</div>
			<div class="board"> 
				<table id="chessTable" class="cboard">
				</table>
			</div>
		</div>
		
	<script type="text/javascript" src="js/broadCast.js"></script>
</body>
</html>