function projectMove(ptype, chessmove) {
	var board = document.getElementById(ptype+"move");
	var sp = document.createElement("div");
	sp.innerHTML = chessmove;
	board.appendChild(sp);
}

function displayMoves(moves) {
	for(var x in moves) {
		projectMove(moves[x].player, moves[x].move);
	}
}