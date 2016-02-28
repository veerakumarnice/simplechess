var connected = true;

var ws = new WebSocket("ws://localhost:8080/simplechess/wsocket");
ws.onopen = function() {
	//ws.send("sample data");
	console.log("connection opened");
};

ws.onmessage = function(message) {
	console.log("message recieved");
	var json = JSON.parse(message.data);
	alert(json.message);
	
};

ws.onclose = function() {
	console.log("connection closed");
};

function closeConnect() {
	ws.close();
	//console.log("connection closed");
}

function sendMessage(source) {
		ws.send(JSON.stringify({"client":document.getElementById('textmessage').value}));
		document.getElementById('textmessage').value = "" ;
}