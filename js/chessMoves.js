function projectMove(ptype, chessmove) {
	var board = document.getElementById(ptype+"move");
	var sp = document.createElement("div");
	sp.innerHTML = chessmove;
	board.appendChild(sp);
}