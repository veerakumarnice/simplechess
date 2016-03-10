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
		case "gameSetUp" :
			setUpGame(json.pieces);

			break;
		case "clientMoveMade" :
				var attacker = document.getElementById(json.from);
				var fallen = document.getElementById(json.to);
				movePiece(attacker, fallen, json);

				break;
		case "broadCastAccess" :
			
			break;
		case "previousMoves":
		//	console.log(json.toString());
		//	console.log(json.previous);
			displayMoves(json.previous);
			break;
		case "encodedMove":

			projectMove(json.player, json.move)
			break;
	}
}

ws.onclose = function(message) {
	ws.send(JSON.stringify({ notify : 'endBroadCast', username:'broadCast'}));
	console.log("connection closed");
};


function addSquares(src) {
	var table = document.getElementById('chessTable');
	var tabBody = document.createElement("tbody");
	tabBody.setAttribute("id","chessBody");
	for(var i=1;i<=8;i++) {
		var tabrow = document.createElement("tr");
		tabrow.setAttribute("class","line");
		for(var j=1;j<=8;j++) {
			var td = document.createElement("td");
			td.setAttribute("xpos",j);
			td.setAttribute("ypos",i);
			td.setAttribute("id",""+j+i);
			var scolor;
			if(((i+j)%2)==0) {
					scolor="white";
			}
			else {			
					scolor="black";
			}
			td.setAttribute("class",scolor);
			tabrow.appendChild(td);
		}
		tabBody.appendChild(tabrow);
	}
	table.style.border = "2px solid black";
	table.appendChild(tabBody);	
}

function newElement(type, inner, cls) {
	var element = document.createElement(type);
	element.setAttribute("class", cls);
	element.innerHTML = inner ;
	return element;
}	

function movePiece(attacker, target, jsondata) {
	//console.log('movePiece called');
	//console.log(attacker);
	//console.log(target);
	//projectMove(attacker, target);
	if(jsondata.castling) {
	var end = fallen.parentNode.getAttribute('id');
	var start = attacker.parentNode.getAttribute('id');
	if(end == 81 || end == 88) {
		document.getElementById(end-10).appendChild(attacker);
		document.getElementById(end-20).appendChild(fallen);
	}
	else if( end == 11 || end == 18)  {
		document.getElementById(start-30).appendChild(attacker);
		document.getElementById(start-20).appendChild(fallen);
	}
	return;
	}


	if(target.childNodes.length > 0) {
		document.getElementById('leftboard').appendChild(target.childNodes[0]);
	}
	target.appendChild(attacker);
	
	var to = target.getAttribute('id');
	console.log(jsondata.promotion);
	if(jsondata.promotion !== undefined) {
		console.log("promtion capable");
		attacker.setAttribute("src",attacker.getAttribute("src").substring(0,9)+capitalize(jsondata.promotion)+".png");
	}
	//if(target.childNodes[0].getAttribute('piece') == 'pawn' && ( to%10 == 1  || to%10 == 8  ) ){
		//promotion(pElem);
	//}

}

function broadcast(game) {
	window.location.href= "broadCast?player1="+game.getAttribute("player1")+"&player2="+game.getAttribute("player2");
}


function removeChessElements() {
	
}


function pickThis(event, src) {
	src.style.cursor = "grabbing";
	//setInterval();
}
