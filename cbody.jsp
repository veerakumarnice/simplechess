<%@ page language="java"%>
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
			<div id="whitemove">
					</div>
					<div id="blackmove"></div>
		</div>
	</div>
	<div class="board"> 
		<table id="chessTable" class="cboard">
	
		</table>

	</div>
	<div class = "right">
	<h3>Event Board</h3>
	<div class="chatContainer">
		<div id="chatlist" onchange="console.log('values changed')">
			
		</div>
		<!--button onclick="inviteRequest(document.getElementById('chatlist').value)">Invite</button-->
		<!--div class="chatMember" value="veerakumarnice@gmail.com">veerakumarnice@gmail.com
		</div-->
	</div>
	<button onclick="getUsers()">Refresh List</button>
	</div>
</div>
<br><br>

<div class="start" >
	<button id="btn" class="button" onclick="startGame(this)" >Click here to Start! </button>
</div>
<div id="chatboxcontainer" class="chatBoxContainer">
		
	
</div>
<script src="js/chessScript.js" type="text/javascript"></script>
<script type="text/javascript">
	window.onbeforeunload = function(event)
    {
        return "All your progress will be lost?"
    };

</script>