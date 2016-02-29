var connected ;
var username;
(function(){
	var user = document.getElementById('lgout');
	if(user.innerHTML != null || user.innerHTML.trim() != '') {
		username = user.innerHTML.trim();	
	}
})();


var ws = new WebSocket("ws://localhost:8080/simplechess/wsocket/"+username);
ws.onopen = function() {	
	ws.send(JSON.stringify({ notify : 'clientConnected', username: username}));	
	console.log("connection opened");
	connected = true;
};

ws.onmessage = function(message) {
	
	var json = JSON.parse(message.data);
	console.log("message recieved");
	console.log(json);
	
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
	ws.send(JSON.stringify({notify:'needActiveUsers', client:username}));
}

function sendMessage(source) {
		ws.send(JSON.stringify({notify:'clientMessage',client:username, chatMessage:document.getElementById('textmessage').value}));
		document.getElementById('textmessage').value = "" ;
}