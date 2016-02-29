var connected ;
var username;
(function(){
	var user = document.getElementById('lgout');
	if(user.innerHTML != null || user.innerHTML.trim() != '') {
		username = user.innerHTML.trim();	
	}
})();


var ws = new WebSocket("ws://localhost:8080/simplechess/wsocket/"+username);
ws.onopen = function() {	
	ws.send(JSON.stringify({ notify : 'clientConnected', username: username}));	
	console.log("connection opened");
	connected = true;
};

ws.onmessage = function(message) {
	
	var json = JSON.parse(message.data);
	console.log("message recieved");
	console.log(json);
	switch(json.notify) {
		case "clientMoveMade" :
			oppenentMoved(json.from, json.to);
			break;
		case "activeUsers" :
			showUsers(json.users);
			break;
		case "inviteRequest" :
			var choicemade = confirm("Do you wish to play with "+json.username);
			if(choicemade) {
				ws.send(JSON.stringify({notify:'acceptInvitation',username: username, opp:json.username,status:"yes"}));
			}
			else {
				ws.send(JSON.stringify({notify:'acceptInvitation',username:json.username, status: "no"}));
			}
			break;
		case "inviteStatus" :
			if(json.status == "yes") {
				console.log("accpeted invitation");
				myPlayer = json.player;
				document.getElementById('myP').innerHTML= "  You :" +myPlayer;
				document.getElementById('btn').click();
			}
			break;
		case "initiateGame" :
			myPlayer = json.player;
			document.getElementById('myP').innerHTML= "  You :" +myPlayer;
			document.getElementById('btn').click();
			break;
	}
};

ws.onclose = function() {
	console.log("connection closed");
};

function closeConnect() {
	ws.close();
	connected = false;
	//console.log("connection closed");
}

function getUsers() {
	ws.send(JSON.stringify({notify:'needActiveUsers', username:username}));
}

function sendMessage(source) {
		ws.send(JSON.stringify({notify:'clientMessage',username:username, chatMessage:document.getElementById('textmessage').value}));
		document.getElementById('textmessage').value = "" ;
}

function inviteRequest(opp) {
	ws.send(JSON.stringify({notify:"inviteRequest",username:username,invite:opp}));
}

function assignRequest() {
	ws.send(JSON.stringify({notify:'assignPlayers',username:username}));
}

function playerAssignment(fromServer) {
	myPlayer = fromServer;
}

function moveMade(attacker, fallen) {
	ws.send(JSON.stringify({notify:'clientMoveMade', username: username, from: attacker, to: fallen}));
}

function oppenentMoved(attacker, fallen) {
	var opp = document.getElementById(attacker);
	var pos = document.getElementById(fallen);
	console.log("oppenentMoved called");
	serverMove = true;
	opp.click();
	pos.click();
	serverMove = false;
}

function resignRequest() {
	ws.send(JSON.stringify({notify:'resign',username: username}));
}

function showUsers(usersArray) {
	var list = document.getElementById('chatlist');
	list.innerHTML = "";
	var sel = document.createElement("option");
	sel.innerHTML = " ------Select----- ";
	list.appendChild(sel);
	for(var u in usersArray ) {
		if(usersArray[u] != username) {
			var node = document.createElement("option");
			node.setAttribute("value", usersArray[u]);
			node.innerHTML = usersArray[u];
			list.appendChild(node);
		}
	}
}