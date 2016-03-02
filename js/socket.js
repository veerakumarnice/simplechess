var connected ;
var username;
(function(){
	var user = document.getElementById('lgout');
	if(user.innerHTML != null || user.innerHTML.trim() != '') {
		username = user.innerHTML.trim();	
	}
})();


var ws = new WebSocket("ws://veera-pt988:8080/simplechess/wsocket/"+username);
ws.onopen = function() {
	ws.send(JSON.stringify({ notify : 'clientConnected', username: username}));
	getUsers();
	console.log("connection opened");
	connected = true;
};

ws.onmessage = function(message) {
	
	var json = JSON.parse(message.data);
	console.log("message recieved");
	console.log(json);
	switch(json.notify) {
		case "clientMoveMade" :
			oppenentMoved(json.from, json.to, json.promotion);
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
				ws.send(JSON.stringify({notify:'acceptInvitation',username:json.username, opp:json.username, status: "no"}));
			}
			break;
		case "inviteStatus" :
			if(json.status == "yes") {
				console.log("accpeted invitation");
				myPlayer = json.player;
				document.getElementById('myP').innerHTML= "  You :" +myPlayer;
				serverMove = true;
				document.getElementById('btn').click();
				serverMove = false;
			}
			else {
				console.log("rejected invitation");
			}
			break;
		case "initiateGame" :
			myPlayer = json.player;
			document.getElementById('myP').innerHTML= "  You :" +myPlayer;
			serverMove =true;
			document.getElementById('btn').click();
			serverMove = false;
			break;
		case "clientDisconnected" :
			var sel = document.getElementById('chatlist').childNodes;
			console.log("disconnection recognized");
			for (var x in sel) {
				if(sel[x].value == json.username ) {
					console.log("child found");
					sel[x].parentNode.removeChild(sel[x]);
					return;
				}
			}
			break;
		case "clientConnected" :

			var sel = document.getElementById("chatlist");
			var el = document.createElement("div");
			el.setAttribute("class","chatMember");
			el.setAttribute("onclick","inviteRequest(this.value)");
			el.setAttribute("value",json.username);
			el.value = json.username;
			el.innerHTML = json.username;
			sel.appendChild(el);
			break;

		case "resign" :
			serverMove = true;
			document.getElementById("btn").click();
			serverMove = false;
			alert("Your oppenent resigned");
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
	if(gameIn) {
		alert("You must resign your current Game to invite another player");
		return;
	}
	ws.send(JSON.stringify({notify:"inviteRequest",username:username,invite:opp}));	
}

function assignRequest() {
	ws.send(JSON.stringify({notify:'assignPlayers',username:username}));
}

function playerAssignment(fromServer) {
	myPlayer = fromServer;
}

function moveMade(attacker, fallen, promote) {
	var json = {notify:'clientMoveMade', username: username, from: attacker, to: fallen};
	if (promote != undefined || promote != null) {
		json.promotion = promote;
	}	
	ws.send(JSON.stringify(json));
}

function oppenentMoved(attacker, fallen, promote) {
	var opp = document.getElementById(attacker);
	var pos = document.getElementById(fallen);
	console.log("oppenentMoved called");
	serverMove = true;
	promotedTo = promote;
	opp.click();
	pos.click();	
	serverMove = false;
}

function resignRequest() {
	ws.send(JSON.stringify({notify:'resign',username: username}));
}

function showUsers(usersArray) {
	var list = document.getElementById('chatlist');
	while (list.firstChild) {
		list.removeChild(list.firstChild);
	}
	for(var u in usersArray ) {
		if(usersArray[u] != username) {
			var node = document.createElement("div");
			node.setAttribute("value", usersArray[u]);
			node.setAttribute("class","chatMember");
			node.setAttribute("onclick","inviteRequest(this.value)");
			node.innerHTML = usersArray[u];
			list.appendChild(node);
		}
	}
}