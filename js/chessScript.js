var chance = 'white' ;
var gameIn = false;
var pElem;
var onmove = false;
var fallenPieces = [];
var restart = false;
var castling = false;
var history ;//= {whiterook1:false,whiterook2:false,whiteking1:false,blackrook1:false, blackrook2:false, blackking1:false};
history.whiterook1 = false;
history.whiterook2 = false;
history.whiteking1 = false;
history.blackrook1 = false;
history.blackrook2 = false;
history.blackking1 = false;
//alert('out alert ' + history.whiterook2);
function startGame(src) {
	if(!gameIn) {
		src.innerHTML = "Click here to Stop!" ;
		gameIn = true;
		chance = 'white';
		//alert(history['whiterook1']);
		addSquares(src);
		addChessElements(src);
	}
	else {
		src.innerHTML = "Click here to Start!";
		gameIn = false;
		onmove = false;
		pElem  = null;
		removeSquares();
		removeChessElements();
		removeLeftPieces();		
	}
}

function removeLeftPieces() {
	var target = document.getElementById("leftboard");
	while(target.firstChild) {
		target.removeChild(target.firstChild);
	}
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
			td.setAttribute("onclick","moveToThis(event,this)");
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

function addChessElements(src) {
	addPawns(src);
	addRooks();
	addBhisops();
	addKnights();
	addQueen();
	addKing();
}

function addPawns(src) {
	
	for(var col =1;col<=8;col++) {	
		var pawn = document.getElementById(""+col+2);
		var img = document.createElement("img");
		img.setAttribute("src","img/blackPawn.png");
		img.setAttribute("class","piece pawn black");
		img.setAttribute("onmouseover","this.style.cursor='grab';");
		img.setAttribute("onclick","moveElement(event,this)");
		img.setAttribute("id","pawn"+col);
		img.setAttribute("player","black");
		img.setAttribute("piece","pawn");
		pawn.appendChild(img);
	}

	for(var col =1;col<=8;col++) {	
		var pawn = document.getElementById(""+col+7);
		var img = document.createElement("img");
		img.setAttribute("src","img/whitePawn.png");
		img.setAttribute("class","piece pawn white");
		img.setAttribute("onmouseover","this.style.cursor='grab';");
		img.setAttribute("onclick","moveElement(event,this)");
		img.setAttribute("id","pawn"+col);
		img.setAttribute("player","white");
		img.setAttribute("piece","pawn");
		pawn.appendChild(img);
	}


}


function addRooks() {
	var array = [11,18,81,88];
	var target;
	for(var i in array) {
		target = document.getElementById(array[i]);
		var img = document.createElement("img");
		if(i%2==0) {
			img.setAttribute("src","img/blackRook.png");
			img.setAttribute("class","piece rook black");
			img.setAttribute("player","black");
		}
		else {
			img.setAttribute("src","img/whiteRook.png");
			img.setAttribute("class","piece rook white");
			img.setAttribute("player","white");
		}
		img.setAttribute("onmouseover","this.style.cursor='grab';");
		img.setAttribute("onclick","moveElement(event,this)");
		img.setAttribute("piece","rook");
		img.setAttribute("id","rook"+(Math.floor(i/2)+1));
		target.appendChild(img);
	}
}

function addBhisops() {
	var array = [31,38,61,68];
	var target;
	for(var i in array) {
		target = document.getElementById(array[i]);
		var img = document.createElement("img");
		if(i%2==0) {
			img.setAttribute("src","img/blackBishop.png");
			img.setAttribute("class","piece bishop black");
			img.setAttribute("player","black");
		}
		else {
			img.setAttribute("src","img/whiteBishop.png");
			img.setAttribute("class","piece bishop white");
			img.setAttribute("player","white");
		}
		img.setAttribute("onmouseover","this.style.cursor='grab';");
		img.setAttribute("onclick","moveElement(event,this)");
		img.setAttribute("piece","bishop");
		img.setAttribute("id","bishop"+(Math.floor(i/2)+1));
		target.appendChild(img);
	}

}

function addKnights() {
	var array = [21,28,71,78];
	var target;
	for(var i in array) {
		target = document.getElementById(array[i]);
		var img = document.createElement("img");
		if(i%2==0) {
			img.setAttribute("src","img/blackKnight.png");
			img.setAttribute("class","piece knight black");
			img.setAttribute("player","black");
		}
		else {
			img.setAttribute("src","img/whiteKnight.png");
			img.setAttribute("class","piece knight white");
			img.setAttribute("player","white");
		}
		img.setAttribute("onmouseover","this.style.cursor='grab';");
		img.setAttribute("onclick","moveElement(event,this)");
		img.setAttribute("piece","knight");
		img.setAttribute("id","knight"+(Math.floor(i/2)+1));
		target.appendChild(img);
	}
}

function addQueen() {
		var array = [41,48];
		var target;
		for(var i in array) {
		target = document.getElementById(array[i]);
		var img = document.createElement("img");
		if(i%2==0) {
			img.setAttribute("src","img/blackQueen.png");
			img.setAttribute("class","piece queen black");
			img.setAttribute("player","black");
		}
		else {
			img.setAttribute("src","img/whiteQueen.png");
			img.setAttribute("class","piece queen white");
			img.setAttribute("player","white");
		}
		img.setAttribute("onmouseover","this.style.cursor='grab';");
		img.setAttribute("onclick","moveElement(event,this)");
		img.setAttribute("piece","queen");
		img.setAttribute("id","queen"+(Math.floor(i/2)+1));
		target.appendChild(img);
	}

}

function addKing() {
		var array = [51,58];
		var target;
		for(var i in array) {
		target = document.getElementById(array[i]);
		var img = document.createElement("img");
		if(i%2==0) {
			img.setAttribute("src","img/blackKing.png");
			img.setAttribute("class","piece king black");
			img.setAttribute("player","black");
		}
		else {
			img.setAttribute("src","img/whiteKing.png");
			img.setAttribute("class","piece king white");
			img.setAttribute("player","white");
		}
		img.setAttribute("onmouseover","this.style.cursor='grab';");
		img.setAttribute("onclick","moveElement(event,this)");
		img.setAttribute("piece","king");
		img.setAttribute("id","king"+(Math.floor(i/2)+1));
		target.appendChild(img);
	}
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

function moveElement(event, src) {
	if(!gameIn && restart == true) {
		var con = confirm("Do you want to restart");
		if(con) {
			gameIn = true;
			restart = false;
			startGame(document.getElementById('btn'));
		
		}
		return;
	}

	if(src.getAttribute("player") != chance && !onmove) {
		alert("Its your oppnenet's move");
		stopEvent(event);
		return;
	}

	if(onmove && src==pElem) {
		//alert("source = destination");		
		onmove = false;
		clearSelection(pElem);
		pElem = null;
		
		src.style.cursor = "grab";
		stopEvent(event);
		return;
	}
	if(!onmove) {
		stopEvent(event);
		pElem = src;
		src.style.cursor = "grabbing";
		onmove = true;
	}

}

function stopEvent(event) {
	event = event || window.event;
	if(event.stopPropagation) 
		event.stopPropagation();
	else
		event.cancelBubble = true; 	
}

function moveToThis(event, src) {
	console.log("new move funcion called");

	if(src.childNodes.length ==0) {
		if(validMove(src)) {
			if(onmove) {
				var pieceType = pElem.getAttribute("piece");
				if(pieceType == 'rook' || pieceType == 'king' ) {
					moved(pElem);	
				}
				src.appendChild(pElem);
				var to= Number(src.getAttribute('id'));
				if(pieceType == 'pawn' && ( to%10 == 1  || to%10 == 8  ) ){
					promotion(pElem);
				}
				var from = Number(pElem.parentNode.getAttribute('id'));
				//else if (pieceType == 'pawn' && ((to%10 == 4 || to%10 == 5) && (from % 10 == 2 || from % 10 == 7) ) {
					//if(isEnpassant(to, pElem.getAttribute('player'))) {
						//enpassant = {piece:pElem,pos:} ;
				//	}
				//}
				onmove = false;
				clearSelection(pElem);
				pElem = null;
				
				changeChance();				
			} 
		}
		else
			alert("Not a valid move");
	}
	else {
		//alert("The destination contains another coin");
		//alert("The first piece is "+ pElem.getAttribute("player"));
		if(src.childNodes[0].getAttribute("player") == pElem.getAttribute("player")) {
			
			if(pElem.getAttribute('piece') == 'king') {
				console.log('king is moving');
				if(validMove(src)) {
					console.log("castle found to be true");
					if(!hasIntermediate(Number(pElem.parentNode.getAttribute('id')),Number(src.getAttribute('id')),'castle', null)) {
						castle(pElem, src.childNodes[0]);
						clearSelection(pElem);
						pElem = null;
						
						onmove = false;
						changeChance();
					}
				}
			}
			else
				alert("That's your piece try some other position");

		}			
		else
		{
			if(pElem.getAttribute('piece') == 'king' && !isKing(pElem.parentNode, src)) {
				return;
			}

			if(validMove(src)) {
				//alert("Trying to cut oppnenet coin");
				var pieceType = pElem.getAttribute("piece");
				if(pieceType == 'rook' || pieceType == 'king' ) {
					moved(pElem);	
				}
				
				cutPiece(pElem, src.childNodes[0]);
				clearSelection(pElem);
				pElem = null;
				
				onmove = false;
				changeChance();				
			}
			
		}
	}

}

function isEnpassant(to, player) {
	var result = false;
	var opp;
	if(to+10 > 10 && to+10 <90) {
		if(opp = document.getElementById(to+10).childNodes[0]) {
			if(opp.getAttribute('player') != player ) {
				result = true;
			}
		}
	}
	if(to-10 >0 && to-10 <80) {
		if(opp = document.getElementById(to-10).childNodes[0]) {
			if(opp.getAttribute('player') != player) {
				result = true;
			}
		}
	}

	return result;
}


function titleCase(string) {
	return string.charAt(0).toUpperCase() +string.slice(1);
}

function promotion(src) {
	var choiceMade = false;
	var choice ;
	var player = src.getAttribute('player');
	var array = ['queen', 'rook', 'bishop', 'knight'];
	while(!choiceMade) {
		choice = prompt("Enter the choice for promotion:\nQueen\nKnight\nBishop\nRook");
		choice = choice.toLowerCase();
		if(array.indexOf(choice) > -1) {
			choiceMade = true;
		}
	}
	src.setAttribute("src","img/"+player+titleCase(choice)+".png");
	src.setAttribute("class", "piece "+choice+" "+player);
	src.setAttribute("piece",choice);
}



function changeChance() {
	if(chance == 'white') {
		chance = 'black';
 	}
 	else {
 		chance = "white";
 	}
}


function castle(attacker, fallen) {
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
}

function validMove(src) {

	var pType = pElem.getAttribute("piece");
	var plyr = pElem.getAttribute("player");
	var sParent = pElem.parentNode;
	var sPos  = Number(pElem.parentNode.getAttribute("id"));
	var dPos  = Number(src.getAttribute("id"));
	//alert("src = "+sPos+" des = "+dPos);
	var result = false;
	var retObj;
	switch(pType) {
		case "pawn" :
			if(plyr=="black") {
				if(dPos > sPos) {
					if(sPos%10==2) {
						
						if(dPos == sPos +1 || dPos == sPos+2){
							result = true;
						}
					}
					else
					{
						if(dPos == sPos+1) {
							result = true;
						}
					}
					
				}
			}
			else
			{
				if(sPos > dPos) {
					if(sPos%10==7) {
						if(sPos == dPos+1 || sPos == dPos+2) {
							result = true;
						}
					}
					else
					{
						if(sPos == dPos+1) {
							result = true;
						}
					}
				}
			}
			if(checkSpeicial(pType, plyr, sPos, dPos, src)){
				result = true;
			}
			break;
		case "rook" :
			
			if((retObj = isPlus(pElem.parentNode,src)).bool) {
					
					if(!hasIntermediate(sPos,dPos, 'plus', retObj.direction)) {
						result = true;
					}				
			}
			break;
		case "knight" :
			result  = isHorse(pElem.parentNode,src);						
			break;
		case "bishop" :
			if((retObj = isCross(pElem.parentNode,src)).bool) {
				    //console.log("iscross success");
					if(!hasIntermediate(sPos, dPos, 'cross', retObj.direction)) {
						result = true;
					}
			}
			break;
		case "queen" :
			if((retObj = isQueenCross(pElem.parentNode, src)).bool) {
				if(!hasIntermediate(sPos, dPos, retObj.sign , retObj.direction)) {
					result =  true;
				}
			}
			break;
		case "king" :
			result = isKing(pElem.parentNode, src);
			console.log("isking = "+result);
			var tar = src.getAttribute("id");
			var cast = ['11', '18', '81', '88'];
			if(!result && (cast.indexOf(tar) > -1)) {
				console.log('entered first test');
				if(src.childNodes[0]) {
					console.log('entered second test');
					if(isNotMoved(pElem, src.childNodes[0])) {
						console.log('entered third test');
						result = true;
						console.log('castling found success');
					}						
				}
			}
			break;
	}
 	return result;
}


function isNotMoved(attacker, fallen) {
	var att = attacker.getAttribute('player') + attacker.getAttribute('id');
	var fal = fallen.getAttribute('player') + fallen.getAttribute('id');
	console.log('attacker id = ' + att + ' moved ' + history["whiteking1"]+' '+ ' and fallen id = '+ fal+ ' '+history['whiterook1']);
	if(history[att] == false && history[fal] == false) {
		return true;
	}
	else {
		return false;
	}

}

function hasIntermediate(attacker, fallen, sign, direction) {
	var result = false;
	var start;
	var end;
	if((attacker - fallen ) <0) {
			start = attacker;
			end = fallen;
	}				
	else {
			start = fallen;
			end = attacker;
	}
	var increment;
	switch(sign) {
		case 'plus' :		
			if(direction == 'xpos') {
				increment = 1;
			}
			else if(direction == 'ypos') {
				increment = 10;
			}
			for(start=start+increment;start<end;start+=increment) {
				if(document.getElementById(start).childNodes[0]) {
						result = true;
						alert("There is a coin inbteween " + start);
						break;
					}
				}
			break; 
		case 'cross' :
			//alert("intermediat cross");
			if(direction == 'right') {
				increment = 11;
			}
			else if(direction == 'left') {
				increment = 9;
			}
			//console.log('increment = '+increment);
			for(start+=increment;start!=end;start+=increment) {
				//console.log('checking start='+start +' end = '+end);
				if(document.getElementById(start).childNodes[0]) {
					result = true;
					//console.log("hitting another coin at " + start);
				 	break;
				}
			}
			break;
		case 'castle' :
			increment = 10;
			for(start+=increment;start<end;start+=increment) {
				console.log('start ' + start+ ' end = '+ end);
				if(document.getElementById(start).childNodes[0]) {
					result = true;
					break;
				}
			}
			break;
	}
	return result;
}

function isPlus(attacker, fallen) {
	var result = {bool:false, direction:"none"};
	if(attacker.getAttribute("xpos") == fallen.getAttribute("xpos")) {
			result.bool = true;
			result.direction = "xpos";
	}
	else if(attacker.getAttribute("ypos") == fallen.getAttribute("ypos")) {
			result.bool = true;
			result.direction = "ypos";
	}

	return result;
}


function isCross(attacker, fallen) {
	var result = {bool:false,direction:'none'};
	var start = Number(attacker.getAttribute('id'));
	var end = Number(fallen.getAttribute('id'));
	console.log('iscross started start = '+start+' end = '+ end);
	var temp;
	if((start-end)<0) {
		console.log("entered negative");
	}
	else
	{
		temp = start;
		start = end;
		end = temp;
	}
	var temp = start;
	console.log('first loop with start = '+start+' end = '+end);
	for(;start <= end; start+=11) {
		console.log('start =' + start+ ' end = '+ end);		
		if(start == end) {
			//alert("start = end");
			result.bool = true;
			result.direction = 'right';
			break;
		}
		if(start%10 == 8) {
			break;
		}
	}
	start = temp;
	if(!result.bool) {
		console.log('started second loop with start  =' +start+' end = '+end);
		for( ;end >= start;end-=9) {
			console.log('start ' + start+ ' end = '+ end);
			if(end == start) {
				//alert("start = end");
				result.bool = true;
				result.direction = 'left';
				break;
			}
			if(end%10 == 8) {
				break;
			}
		}
	}

	return result;
}

function isQueenCross(attacker, fallen) {
	var result = {bool:false,direction:'none'};
	var sign;
	if( (result= isPlus(attacker, fallen)).bool ) {
		result.sign = 'plus';
	}
	else {
		result = isCross(attacker, fallen);
		result.sign = 'cross';
	}
	return result;
}


function isHorse(attacker, fallen) {
	//console.log("ishorse exeuctes");
	var result = false;
	var horse = [-8,8,-12,12,-19,19,-21,21];
	var start = attacker.getAttribute('id');
	var end = fallen.getAttribute('id');
	if(horse.indexOf(start-end) > -1) {
		result = true;
	}
	return result;
}

function isKing(attacker, fallen) {
	var result = false;
	var king = [-1, 1, -9, 9, -10, 10, -11, 11];
	var start = attacker.getAttribute('id');
	var end = fallen.getAttribute('id');
	if(king.indexOf(start-end) > -1) {
		result = true;
	}
	return result;
}

function checkSpeicial(type, player, sPos, dPos, src) {
	var result = false;
	if(type == 'pawn') {
		if(player == "black") {
			if(dPos == sPos - 9 || dPos == sPos + 11) {
				if(src.childNodes[0].getAttribute("player") != player)
					result = true;
			}
		}
		else
		{
			if(dPos == sPos + 9  || dPos == sPos -11) {
				if(src.childNodes[0].getAttribute("player") != player)
					result = true;
			}
		}
	}
	return result;
}

function moved(source) {
	var attacker = source.getAttribute("player")+source.getAttribute("id");
	console.log((history[attacker] = true) +""+ attacker);
}



function hasOppenent(source , dest) {
	var result = false;
	if(source.getAttribute("player") != dest.childNodes[0].getAttribute("player")) {
		result = true;
	}
	return result;
}

var pelem = document.getElementById("pel");
var mov = document.getElementById("onm");
var tu = document.getElementById("turn");
setInterval("update()",100);

function update() {
	/*if(pElem)
		pelem.innerHTML = pElem.getAttribute("player")+" "+pElem.getAttribute("id");
	else
		pelem.innerHTML = null;*/
	//onm.innerHTML = onmove;
	turn.innerHTML = 'Turn : ' + chance ;
	if(pElem) {
		setSelection(pElem);
	}
}

function clearSelection(target) {
	target.style.boxShadow = "0px 0px 0px";
}

function setSelection(target) {
	target.style.boxShadow = "5px 5px 5px blue" ;
}

function cutPiece(attacker, fallen) {
	fallenPieces.push({piece:fallen, id:fallen.parentNode.getAttribute("id")});
	console.log('cutPiece called');
	var target = fallen.parentNode;
	if(fallen.getAttribute('piece') == 'king') {
		if(attacker.getAttribute('player') == 'white') {
			alert('white wins');
		}
		else
		{
			alert('black wins') ;
		}
		gameIn = false;
		restart = true;
	}
	document.getElementById('leftboard').appendChild(target.childNodes[0]);
	target.appendChild(attacker);
	to = target.getAttribute('id');
	if(target.childNodes[0].getAttribute('piece') == 'pawn' && ( to%10 == 1  || to%10 == 8  ) ){
		promotion(pElem);
	}


}
