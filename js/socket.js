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
	opp.click();
	pos.click();
}

function resignRequest() {
	ws.send(JSON.stringify({notify:'resign',username: username}));
}

function showUsers(usersArray) {
	var list = document.getElementById('chatlist');
	for(var u in usersArray ) {
		if(usersArray[u] != username) {
			var node = document.createElement("option");
			node.setAttribute("value", usersArray[u]);
			node.innerHTML = usersArray[u];
			list.appendChild(node);
		}
	}
}