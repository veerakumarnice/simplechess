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
	//ws.send(JSON.stringify({ notify : 'clientConnected', username: username}));
	getUsers();
	console.log("connection opened");
	connected = true;
};

ws.onmessage = function(message) {
	
	var json = JSON.parse(message.data);
	console.log("message recieved");
	console.log(json);
	switch(json.notify) {
		case "gameSetUp":
			setUpGame(json.pieces);
			break;
		case "clientMoveMade" :
			oppenentMoved(json.from, json.to, json.promotion);
			console.log("The time move made was "+new Date(json.time));
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
				ws.send(JSON.stringify({notify:'acceptInvitation',username:username, opp:json.username, status: "no"}));
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
		case "encodedMove":
			projectMove(json.player, json.move)
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
			var chatChilds = sel.childNodes;
			//console.log(sel);
			var found = false;
			if(chatChilds.length != 0) {
				for(var x in chatChilds) {
					if (x < chatChilds.length && chatChilds[x].getAttribute("value") == json.username) {
						found = true;
						break;
					}
				}
			}
			if(json.username == "broadCast" || json.username == "broadCastList") {
				return;
			}	
			
			if(!found) {
				var el = document.createElement("div");
				el.setAttribute("class","chatMember");
				el.setAttribute("onclick","addChatBox(this)");
				el.setAttribute("value",json.username);
				el.value = json.username;
				el.innerHTML = json.username;
				sel.appendChild(el);			
			}
			break;

		case "resign" :
			serverMove = true;
			document.getElementById("btn").click();
			serverMove = false;
			alert("Your oppenent resigned");
			break;
		case "clientMessage":
			appendMessage(addChatBox(null, json.username), json.chatMessage);

			break;
		case "displayMessage":
			alert(json.message);
			break;
		case "previousMoves":
			displayMoves(json.previous);
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

function sendMessage(source, event) {
	//console.log(source.parentNode.parentNode.getAttribute("value"));
	event = event || window.event;
	if (event.keyCode == 13) {
		ws.send(JSON.stringify({notify:'clientMessage',username:username, target:source.parentNode.parentNode.getAttribute("value") ,chatMessage:source.value}));
		source.value = "" ;
	}
		
}

function appendMessage(box, message) {
	alert(message);
}

function inviteRequest(opponent) {
	//console.log("ivite request vlue as opp : "+opp.getAttribute("value"));
	opp = opponent.parentNode.parentNode;
	if(gameIn) {
		alert("You must resign your current Game to invite another player");
		return;
	}
	ws.send(JSON.stringify({notify:"inviteRequest",username:username,invite:opp.getAttribute("value")}));	
}

function assignRequest() {
	ws.send(JSON.stringify({notify:'assignPlayers',username:username}));
}

function playerAssignment(fromServer) {
	myPlayer = fromServer;
}

function moveMade(attacker, start, fallen, promote, castle) {
	var json = {notify:'clientMoveMade', username: username, from: attacker, start:start, to: fallen};
	if (promote != undefined || promote != null) {
		json.promotion = promote;
	}	
	if(castle != undefined) {
		json.castling = true;
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
		if(usersArray[u] != username && usersArray[u] != "broadCastList" && usersArray[u] != "broadCast") {
			var node = document.createElement("div");
			node.setAttribute("value", usersArray[u]);
			node.setAttribute("class","chatMember");
			node.setAttribute("onclick","addChatBox(this)");
			node.innerHTML = usersArray[u];
			list.appendChild(node);
		}
	}
}

function addChatBox(opponent, sender) {
//	console.log("add chat box");
	var oppoUser;
	if(opponent !== null) {
		oppoUser =  opponent.getAttribute("value"); 
	}
	else{
	oppoUser = sender	;
	} 
	var existingChatBoxes = document.getElementsByClassName("chat");
	var existingBox;
	if (existingChatBoxes.length > 0 && (existingBox = chatBoxPresent(oppoUser, existingChatBoxes))) {
		alert("alerady a chat bix is there");
			highLightBox(existingBox);
			return existingBox;
	}

	var divElem = document.createElement("div");
	divElem.setAttribute("class", "chat");
	divElem.setAttribute("value" , oppoUser);

	var innerDiv1 = document.createElement("div");
	innerDiv1.setAttribute("class", "top");
	innerDiv1.setAttribute("onclick", "minimizeChat(this)");
	
	var innerSpan1 = document.createElement("span");
	innerSpan1.innerHTML = oppoUser;
	innerDiv1.appendChild(innerSpan1);
		
	var innerSpan2 = document.createElement("span");
	innerSpan2.innerHTML = "x";
	innerSpan2.setAttribute("class", "chatClose");
	innerSpan2.setAttribute("onclick","closeChatBox(this)");
	innerDiv1.appendChild(innerSpan2);
	divElem.appendChild(innerDiv1);

	var innerDiv2 = document.createElement("div");
	innerDiv2.setAttribute("class","chatMessages");
	
	var invBut = document.createElement("button");
	invBut.innerHTML = "invite";
	invBut.setAttribute("class", "inviteButton");
	invBut.setAttribute("onclick","inviteRequest(this)");
	innerDiv2.appendChild(invBut);
		
	var textInput = document.createElement("input");
	textInput.setAttribute("class", "chatinput");
	textInput.setAttribute("type", "text");
	textInput.setAttribute("onkeypress","sendMessage(this, event)");
	textInput.setAttribute("placeholder", "Enter message");
	innerDiv2.appendChild(textInput);
	divElem.appendChild(innerDiv2);
	document.getElementById("chatboxcontainer").appendChild(divElem);
	return divElem;

}

function chatBoxPresent(uname, list) {

	for(var x in list) {	
		if(x < list.length && list[x].getAttribute("value")== uname) {
			return list[x];
		}
	}
}

function minimizeChat(source) {
	var target = source.parentNode.getElementsByClassName('chatMessages')[0];
	if(target.style.display == "none") {
		target.style.display = "block";
	}
	else {
		target.style.display = "none";
	}
}

function closeChatBox(source) {
	var target = source.parentNode.parentNode;
	target.parentNode.removeChild(target);
}

function highLightBox(box) {

}