function setUpGame(pieces) {
	console.log("setUpGame called");
	console.log(pieces);
	cleanBoard();
	addSquares();
	var cutpieces = [];
	for(var x in pieces) {
		if(x != "promoted") {
		//console.log("pieces for "+x);
			addPieceType(x, pieces[x], pieces["promoted"], cutpieces);
		}

	}
	var leftboard= document.getElementById("leftboard");
	for(var y in cutpieces) {
		//console.log(y);
		//if(y < leftboard.length) {
		//	console.log(cutpieces[y]);
			leftboard.appendChild(cutpieces[y]);	
		//}
		
	}
	console.log("setUpGame ended");
}

function addPieceType(type, array, promotedPieces, cutpieces) {
	console.log('addPieceType for '+ type);
	for(var i in array) {		
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
		if(type == "pawn" && (promotedPieces[i] != "none")) {
			img.setAttribute("piece",promotedPieces[i]);
			if(img.getAttribute("player") == "white") {
				img.setAttribute("src","img/whiteQueen.png");
			}
			else {
				img.setAttribute("src","img/blackQueen.png");
			}
		}
		else {
			img.setAttribute("piece",type);
		}
		if(array[i] > 10 && array[i] < 89) {
			document.getElementById(array[i]).appendChild(img);
			//target.appendChild(img);
		}
		else if(array[i] >=100) {
			cutpieces[array[i]%100]= img;
		//	console.log("setting it up at "+ array[i]%100 + " for "+array[i]);
		//	console.log("cuuted piecec is ");
		//	console.log(img);
		//	console.log(cutpieces);
		}
		
	}
	console.log("done adding pieces for "+type);
}


function capitalize(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
}

function cleanBoard() {
	var elementToDelete = ["chessTable","whitemove","blackmove","leftboard"];
	for(var x in elementToDelete){
		document.getElementById(elementToDelete[x]).innerHTML = "";
	}
}
