package serverend;

import java.io.StringReader;
import java.io.IOException;
import javax.websocket.EncodeException;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.OnOpen;
import javax.websocket.OnMessage;
import javax.websocket.OnError;
import javax.websocket.OnClose;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.json.*;
import java.util.*;


@ServerEndpoint(value="/wsocket/{user}")
public class wsocket {

	private static Set<GameHandler> activeGames = Collections.synchronizedSet(new HashSet<GameHandler>()); 
	private static boolean turn = true;

	//private static boolean assigned = false;
	private static Set<Session> sessions = Collections.synchronizedSet(new HashSet<Session>());
	private Session opponent;
	private GameHandler gameHandler;

	@OnOpen
	public void onOpen(final Session session, @PathParam("user") final String user) throws IOException, EncodeException{
		System.out.println("client connected" + user);
		//System.out.println("New Client connected :" +json.getString("username"));
		session.getUserProperties().put("username", user);
		System.out.println("username set to " + user);
		session.getUserProperties().put("tracking",new HashSet<GameHandler>());
		
		if(!user.equals("broadCastList") || !user.equals("broadCast")) {
			sessions.add(session);
			notify("{\"notify\":\"clientConnected\",\"username\":\""+user+"\"}", session, "all");
		}
		
		
	}
	
	@OnMessage
	public void onMessage(String message, final Session session, @PathParam("user") final String user) throws IOException{
		System.out.println("Received from client :" + message);
		JsonReader jreader = Json.createReader(new StringReader(message));
		JsonObject json =  jreader.readObject();
		jreader.close();
		if(json.getString("username").equals(user)) {
				System.out.println("username satisfied");
			switch (json.getString("notify")) {

				case "clientConnected":
					if(json.getString("username") == ("broadCastList") ) {
						break;
					}
					System.out.println("New Client connected :" +json.getString("username"));
					session.getUserProperties().put("username",json.getString("username"));
					System.out.println(json);
					notify(message, session, "all");
					break;
				case "needActiveUsers":
					session.getBasicRemote().sendText(getActiveUsers(session).toString());
					System.out.println("Sent active users list to " + json.getString("username")) ;
					break;
				case "inviteRequest" :
					gameHandler = new GameHandler(session, json.getString("invite"));
					activeGames.add(gameHandler);
					startGame(json.getString("username"), json.getString("invite"));

					break;
				case "acceptInvitation" :
					if (json.getString("status").equals("yes")){
						setOppenent(json.getString("opp"));
						inviteStatus(json.getString("username"), json.getString("opp"), "yes");
						addTOGameHandler(session, json.getString("opp"));
						session.getBasicRemote().sendText("{\"notify\":\"initiateGame\",\"opponent\":\""+json.getString("opp")+"\""+ ","+"\"player\":\"black\"}");
					}
					else {
						inviteStatus(json.getString("username"), json.getString("opp"), "no");
					}
					break;
				
				case "clientMoveMade" :				
					
				//	notify(message, session, "opponent");
				//	break;
				case "resign" :
					gameHandler.notify( user, json);//notify(message, session, "opponent");
					break;
				case "broadCastListNeeded" :
					session.getBasicRemote().sendText(listActiveGames().toString());
					break;
				case "broadCast" :
					GameHandler g;
					if((g = gameExists(json.getString("player1"), json.getString("player2"))) != null) {
						System.out.println("game exists for broadcast");
						g.broadCast(session);
					}
					break;
				case "endBroadCast" :
					removeTracking(session);
					break;					
			}
		}
		else {
			System.out.println("malpractice identified");
		}
	}

	private GameHandler gameExists(String p1, String p2) {
		for(GameHandler g : activeGames) {
			if (g.getPlayer1().equals(p1) && g.getPlayer2().equals(p2)) {
				return g;
			}
		}
		return null;
	}

	private JsonObject listActiveGames() {
		JsonObjectBuilder object = Json.createObjectBuilder();
		JsonArrayBuilder array = Json.createArrayBuilder();
		for(GameHandler g : activeGames) {			
			array.add(Json.createObjectBuilder().add("player1",g.getPlayer1())
				.add("player2", g.getPlayer2()).add("startTime",g.getStartTime().toString()).add("active",g.getPlayerStatus()).build());
		}
		object.add("notify","broadCastList").add("list",array.build());
		return object.build();
	}

	private void setOppenent(String opp) {
		for (Session s : sessions) {
			if(s.getUserProperties().get("username").equals(opp)) {
				opponent = s;
				return;
			}
		}
	}

	private void addTOGameHandler(Session mySession, String otherPlayer) {

		for(GameHandler g: activeGames) {
			String p1 = g.getPlayer1();
			String p2 = g.getPlayer2();
			String thisPlayer = (String)mySession.getUserProperties().get("username");
			if(otherPlayer.equals(p1) && p2.equals(thisPlayer)) {
				g.addSecondPlayer(mySession);
				gameHandler = g;
			}
			else if(otherPlayer.equals(p2) && p1.equals(thisPlayer)) {
				g.addFirstPlayer(mySession);
				gameHandler = g;
			}
		}
	}

	private void inviteStatus(String user, String opp, String status) throws IOException {
		JsonObjectBuilder json = Json.createObjectBuilder().add("notify","inviteStatus").add("username",user).add("status", status);
		if(status == "yes") {
			json.add("player","white");
		}
		JsonObject obj = json.build();
		for(Session s : sessions) {
					if(s.getUserProperties().get("username").equals(opp)) {
						s.getBasicRemote().sendText(obj.toString());
						return;
					}
				}	
	}

	private boolean startGame(String myPlayer, String opp) throws IOException {
		System.out.println("startGame");
		setOppenent(opp);		
		System.out.println("setOppenent done");
		opponent.getBasicRemote().sendText(inviteToGame(myPlayer));
		System.out.println("opponent requested");
		return true;
	}


	private String inviteToGame(String myPlayer) {
		JsonObject json = Json.createObjectBuilder().add("notify","inviteRequest").add("username",myPlayer).build();
		return json.toString();
	}

	private void notify(String message, Session session, String reciever) throws IOException{
		System.out.println("notify called");
		
		switch (reciever) {

			case "all":
				for(Session s : sessions) {
					if(s.isOpen() && !s.equals(session)) {
						s.getBasicRemote().sendText(message);
					}
				}	
				break;

			case "opponent":
				System.out.println("notifying oppoenent");
				opponent.getBasicRemote().sendText(message);
				break;			
		}			
		System.out.println("successfully completed notify");
	}

	private JsonObject getActiveUsers(Session session) throws IOException {
		System.out.println("function getUsers called ");
		Set<Session> list = getPlayerList();
		JsonArrayBuilder object = Json.createArrayBuilder();
		Set<String> added = new HashSet<String>();
		for(Session s : list) {
			String presentUsername = (String)s.getUserProperties().get("username");
			
			if (!added.contains(presentUsername) && !presentUsername.equals("broadCastList")) {
				System.out.println("trying to add " + presentUsername);
				object.add(presentUsername);
				added.add(presentUsername);
			}
		}
		JsonArray array = object.build();
		JsonObject json = Json.createObjectBuilder().add("notify","activeUsers").add("users",array).build();
		return json;
	}


	@OnClose
	public void onClose(Session session,  @PathParam("user") final String user) throws IOException {
		System.out.println("Connection closing for "+user);
		if(session.getUserProperties().get("username").equals("broadCastList")) {
			System.out.println("Session broadcastList closing");
			return;
		}
		for(Session s: sessions) {
			if(s.isOpen() && s != session) {
				s.getBasicRemote().sendText("{\"notify\":\"clientDisconnected\",\"username\":\""+ session.getUserProperties().get("username")+"\"}");
			}
		}
		sessions.remove(session);
		removeTracking(session);
		System.out.println("Onclose finished successfully");
	}

	@OnError
	public void onError(Throwable e) {
		System.out.println("Error occured at wsocket " + e);
		//e.printStackTrace();
	}

	public void removeTracking(Session s) {
		System.out.println("endSession called for" + s.getUserProperties().get("username"));
		@SuppressWarnings("unchecked")
		Set<GameHandler> gHand= (HashSet<GameHandler>)s.getUserProperties().get("tracking");
		for(GameHandler g : gHand ) {
			System.out.println("onclose trying to signal game " + g.getPlayer1()+" "+g.getPlayer2());
			g.sessionEnded(s);
		}
		System.out.println("successfully removed trackings");
	}

	private static Set<Session> getPlayerList() {
		return sessions;
	}
}

class GameHandler {

	private String p1uname;
	private String p2uname;
	private Set<Session> player1;
	private Set<Session> player2;
	private Set<Session> broadcastList;
	private boolean gameStarted;
	private Date startTime;
	Game game;
	public GameHandler(Session starter, String opponent) {
		System.out.println("gameHandler initiated");
		player1 = new HashSet<Session>();
		player2 = new HashSet<Session>();
		broadcastList = new HashSet<Session>();
		player1.add(starter);
		@SuppressWarnings("unchecked")
		Set<GameHandler> gHand= (HashSet<GameHandler>)starter.getUserProperties().get("tracking");
		gHand.add(this);
		p1uname = (String)starter.getUserProperties().get("username");
		p2uname = opponent;
		startTime = new Date();
		System.out.println("gameHandler for player one done");
		game = new Game();
	}

	public void addSecondPlayer(Session second) {
		if (p2uname.equals(second.getUserProperties().get("username"))) {
			@SuppressWarnings("unchecked")
			Set<GameHandler> gHand= (HashSet<GameHandler>)second.getUserProperties().get("tracking");
			if(player2.size() == 0){
				if (gameStarted) {
					player2.add(second);					
					gHand.add(this);
					System.out.println("gameHandler resumes player two");
				}
				else {
					player2.add(second);
					gHand.add(this);
					p2uname = (String)second.getUserProperties().get("username");
					gameStarted = true;
					System.out.println("gameHandler adds player two");
				}
			}
			else {
				player2.add(second);
				gHand.add(this);
				System.out.println("gameHandler adds and extra session for the second player");
			}
		}
	}

	public void addFirstPlayer(Session first) {
		if (p1uname.equals(first.getUserProperties().get("username"))) {
			@SuppressWarnings("unchecked")
			HashSet<GameHandler> gHand= (HashSet<GameHandler>)first.getUserProperties().get("tracking");
			if(player1.size() == 0){
				if (gameStarted && p1uname.equals(first.getUserProperties().get("username"))) {
					player1.add(first);
					gHand.add(this);
					System.out.println("gameHandler resumes player  one");
				}
				else {
					player1.add(first);
					gHand.add(this);
					gameStarted = true;
					System.out.println("gameHandler adds player  one\nAnd this is unusal you have to check this issue loop hole");
				}
			}
			else {
				player1.add(first);
				gHand.add(this);
				System.out.println("gameHandler adds and extra session for the first player");
			}
		}
	}

	public String getPlayer1() {
		return p1uname;
	}

	public String getPlayer2() {
		return p2uname;
	}

	public Date getStartTime() {
		return startTime;
	}

	public String getPlayerStatus() {
		if(player1.size() >0 && player2.size() > 0) {
			return "both";
		}
		else if (player1.size() >0 && player2.size() == 0) {
			return "player1";
		}
		else if (player1.size() == 0 && player2.size() > 0) {
			return "player2";
		}
		else if (player1.size() == 0 && player2.size() == 0) {
			return "none";
		}
		return null;
	}

	public void notify(String username, JsonObject message) throws IOException {
		Set<Session> target;
		if(username.equals(p1uname)) {
			target = player2;
		}
		else if(username.equals(p2uname)) {
			target = player1;
		}
		else {
			return;
		}

		for(Session s : target) {
			if(s.isOpen()) {
				s.getBasicRemote().sendText(message.toString());	
			}
			
		}

		for(Session sub : broadcastList) {
			if(sub.isOpen()) {
				System.out.println("broadcasting a move");
				sub.getBasicRemote().sendText(message.toString());	
			}
			
		}
	}

	public void broadCast(Session s) throws IOException{
		broadcastList.add(s);
		s.getBasicRemote().sendText(getPiecePositions().toString());
		System.out.println("broadCast subscribed");
	}

	public void sessionEnded(Session s) {
		System.out.println("game handler gets notified that a session for " + s.getUserProperties().get("username")+" ended");
		if(player1.contains(s)) {
			player1.remove(s);
			System.out.println("player1 session removed");
		}
		else if(player2.contains(s)) {
			player2.remove(s);
			System.out.println("player2 session removed");
		}
		else if (broadcastList.contains(s)) {
			broadcastList.remove(s);
			System.out.println("subscription session removed");
		}
	}

	private JsonObject getPiecePositions() throws IOException {
		JsonObjectBuilder obj = Json.createObjectBuilder();
		obj.add("notify","gameSetUp");
		//JsonArrayBuilder array = Json.createArrayBuilder();
		System.out.println("getPiecePositions going to execute for getting string.");
		return obj.add("pieces", game.getPositions()).build();
	}
}

class Game {
	private JsonObject json;

	public Game() {
		//JsonObjectBuilder obj  = ;
		initPos();
		System.out.println("game created with json"+json.toString());
	}

	public JsonObject getPositions() {
		System.out.println("getpos in game execuetes");
		return json;		
	}
	private void initPos() {

		System.out.println("initial postions adding");
		JsonObjectBuilder job = Json.createObjectBuilder().add("pawn",Json.createArrayBuilder().add(12).add(17).add(22).add(27).add(32).add(37).add(42).add(47)
			.add(52).add(57).add(62).add(67).add(72).add(77).add(82).add(87).build());
		job.add("knight",Json.createArrayBuilder().add(21).add(28).add(71).add(78).build());
		job.add("bishop",Json.createArrayBuilder().add(31).add(38).add(61).add(68).build());
		job.add("rook",Json.createArrayBuilder().add(11).add(18).add(81).add(88).build());
		job.add("queen",Json.createArrayBuilder().add(41).add(48).build());
		job.add("king",Json.createArrayBuilder().add(51).add(58).build());
		json = job.build();
		System.out.println("done adding initial positons ");//+job.build().toString());
	}

}
