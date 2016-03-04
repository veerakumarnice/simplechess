var username;
(function(){
	var user = document.getElementById('lgout');
	if(user.innerHTML != null || user.innerHTML.trim() != '') {
		username = user.innerHTML.trim();	
	}
})();

var ws = new WebSocket("ws://veera-pt988:8080/simplechess/wsocket/broadCast");
ws.onopen = function() {
	ws.send(JSON.stringify({ notify : 'broadCast', username:'broadCast', player1:player1, player2:player2}));
	console.log("broadCast opened");
};

ws.onmessage = function(message) {
	var json = JSON.parse(message.data);
	console.log("message recieved");
	console.log(json);
	switch (json.notify) {
		case "clientMoveMade" :
				var attacker = document.getElementById(json.from);
				var fallen = document.getElementById(json.to);
				movePiece(attacker, fallen);
				break;
		case "broadCastAccess" :
			
			break;
	}
}

ws.onclose = function(message) {
	console.log("connection closed");
};

function createList(list) {
	console.log("creating list");
	var target = document.getElementById("broadCastContainer");
	if(list.length != 0) {
		console.log("list contains game");
		for(var x in list) {
			var node = document.createElement("div");
			node.setAttribute("class", "broadCastItem");
			node.setAttribute("player1",list[x].player1);
			node.setAttribute("player2", list[x].player2);
			node.setAttribute("onclick", "broadcast(this)");
			node.appendChild(newElement("div","Player 1 : "+list[x].player1 , ""));
			node.appendChild(newElement("div","Player 2 : "+list[x].player2 , ""));
			node.appendChild(newElement("div","Start Time : "+list[x].startTime , ""));
			node.appendChild(newElement("div","Active players : "+list[x].active , ""));
			target.appendChild(node);
		}
	}
}

function newElement(type, inner, cls) {
	var element = document.createElement(type);
	element.setAttribute("class", cls);
	element.innerHTML = inner ;
	return element;
}	

function movePiece(attacker, fallen) {
	console.log('movePiece called');
	if(fallen.childNodes.length > 0) {
		document.getElementById('leftboard').appendChild(fallen.childNodes[0]);
	}
	fallen.appendChild(attacker);
	
	to = target.getAttribute('id');
	if(fallen.childNodes[0].getAttribute('piece') == 'pawn' && ( to%10 == 1  || to%10 == 8  ) ){
		promotion(pElem);
	}
}

function broadcast(game) {
	window.location.href= "broadCast?player1="+game.getAttribute("player1")+"&player2="+game.getAttribute("player2");
}