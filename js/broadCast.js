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
				movePiece(attacker, fallen);
				break;
		case "broadCastAccess" :
			
			break;
		case "":
		
			break;
	}
}

ws.onclose = function(message) {
	ws.send(JSON.stringify({ notify : 'endBroadCast', username:'broadCast'}));
	console.log("connection closed");
};

function newElement(type, inner, cls) {
	var element = document.createElement(type);
	element.setAttribute("class", cls);
	element.innerHTML = inner ;
	return element;
}	

function movePiece(attacker, target) {
	//console.log('movePiece called');
	//console.log(attacker);
	//console.log(target);
	projectMove(attacker, target);
	if(target.childNodes.length > 0) {
		document.getElementById('leftboard').appendChild(target.childNodes[0]);
	}
	target.appendChild(attacker);
	
	var to = target.getAttribute('id');
	if(target.childNodes[0].getAttribute('piece') == 'pawn' && ( to%10 == 1  || to%10 == 8  ) ){
		//promotion(pElem);
	}

}

function broadcast(game) {
	window.location.href= "broadCast?player1="+game.getAttribute("player1")+"&player2="+game.getAttribute("player2");
}

function setUpGame(pieces) {
	console.log("setUpGame called");
	console.log(pieces);
	addSquares();
	for(var x in pieces) {
		console.log("pieces for "+x);
		addPieceType(x, pieces[x]);
	}
	console.log("setUpGame ended");
}


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




function addPieceType(type, array) {
	var target;
	for(var i in array) {
		target = document.getElementById(array[i]);
		var img = document.createElement("img");
		if(i%2==0) {
			img.setAttribute("src","img/black"+capitalize(type)+".png");
			img.setAttribute("class","piece "+type+" black");
			img.setAttribute("player","black");
			img.setAttribute("id","black"+type+(Math.floor(i/2)+1));
		}
		else {
			img.setAttribute("src","img/white"+capitalize(type)+".png");
			img.setAttribute("class","piece "+type+" white");
			img.setAttribute("player","white");
			img.setAttribute("id","white"+type+(Math.floor(i/2)+1));
		}
		img.setAttribute("onmouseover","this.style.cursor='grab';");
		img.setAttribute("piece",type);		
		target.appendChild(img);
	}
	console.log("done adding pieces for "+type);
}


function capitalize(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
}

function removeSquares() {
	var tbody = document.getElementById('chessBody');
	tbody.parentNode.removeChild(tbody);
}

function removeChessElements() {
	
}


function pickThis(event, src) {
	src.style.cursor = "grabbing";
	//setInterval();
}
