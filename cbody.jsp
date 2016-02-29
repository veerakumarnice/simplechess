<%@ page language="java"%>
<table>
<tbody>
	<tr>
		<td id="pel"></td><td id="onm"></td><td id="turn"></td><td id="myP"></td> 
	</tr>
</tbody>
</table>
<div class="container">
	<div class ="left" id="leftboard">
		
	</div>
	<div class="board"> 
		<table id="chessTable" class="cboard">
	
		</table>

	</div>
	<div class = "right">
	<h3>Event Board</h3>
	<div class="chatContainer">
		<select id="chatlist" onchange="console.log(this.value)">
			<option value="none"> ------Select----- </option>
		</select>
		<button onclick="inviteRequest(document.getElementById('chatlist').value)">Invite</button>
	</div>
	<button onclick="getUsers()">Refresh List</button>
	</div>
</div>
<br><br>

<div class="start" >
	<button id="btn" class="button" onclick="startGame(this)" >Click here to Start! </button>
</div>
<script src="js/chessScript.js" type="text/javascript"></script>
<script type="text/javascript">
	window.onbeforeunload = function(event)
    {
        return confirm("Confirm refresh");
    };

</script>