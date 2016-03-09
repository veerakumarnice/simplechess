function projectMove(attacker, target) {
	var board = document.getElementById(attacker.getAttribute("player")+"move");
	var sp = document.createElement("div");
	sp.innerHTML = getLetter(attacker, target) +""+encodePos(target.getAttribute("id"));
	board.appendChild(sp);

}

function getLetter(attacker, target) {
	var let = attacker.getAttribute("piece");
	var letter = "";
	if(let != 'p') {
		letter = let;
	}
	return let.toUpperCase();
}

function encodePos(input) {
	var array = ['a','b','c','d','e','f','g','h'];
	var letter = array[((input-(input%10))/10)-1];
	return letter+""+(input%10);
}