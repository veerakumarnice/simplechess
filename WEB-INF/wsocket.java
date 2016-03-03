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
		notify("{\"notify\":\"clientConnected\",\"username\":\""+user+"\"}", session, "all");
		
		sessions.add(session);
		
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
					if(json.getString("username").equals("broadCastList")) {
						return;
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
				case "assignPlayers" :
					assignPlayers();
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
			}
		}
		else {
			System.out.println("malpractice identified");
		}
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
		setOppenent(opp);		
		opponent.getBasicRemote().sendText(inviteToGame(myPlayer));
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
					if(!s.equals(session)) {
						s.getBasicRemote().sendText(message);
					}
				}	
				break;

			case "opponent":
				System.out.println("notifying oppoenent");
				opponent.getBasicRemote().sendText(message);
				break;

			
		}			
	}

	private JsonObject getActiveUsers(Session session) throws IOException {
		System.out.println("function getUsers called ");
		Set<Session> list = getPlayerList();
		JsonArrayBuilder object = Json.createArrayBuilder();
		Set<String> added = new HashSet<String>();
		for(Session s : list) {
			String presentUsername = (String)s.getUserProperties().get("username");
			
			if (!added.contains(presentUsername)) {
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
	public void onClose(Session session) throws IOException {
		System.out.println("Connection close");
		for(Session s: sessions) {
			if(s != session) {
				s.getBasicRemote().sendText("{\"notify\":\"clientDisconnected\",\"username\":\""+ session.getUserProperties().get("username")+"\"}");
			}
		}
		sessions.remove(session);
	}

	@OnError
	public void onError(Throwable e) {
		System.out.println("Error occured at wsocket " + e);
		//e.printStackTrace();
	}


	private static String getTurn() {
		if(turn) {
			turn = false;
			return "white";
		}
		else {
			turn = true;
			return "black";
		}		
	}

	private static void assignPlayers() throws IOException{
		Set<Session> gameSessions = getPlayerList();
		for(Session s: gameSessions) {
			String playerTurn = getTurn();
			s.getUserProperties().put("player",playerTurn);
			JsonObject json = Json.createObjectBuilder().add("notify","playerAssigned").add("player",playerTurn).build();
			s.getBasicRemote().sendText(json.toString());
		}
		//assigned = true;
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
	public GameHandler(Session starter, String opponent) {
		System.out.println("gameHandler initiated");
		player1 = new HashSet<Session>();
		player2 = new HashSet<Session>();
		broadcastList = new HashSet<Session>();
		player1.add(starter);
		p1uname = (String)starter.getUserProperties().get("username");
		p2uname = opponent;
		startTime = new Date();
		System.out.println("gameHandler for player one done");
	}

	public void addSecondPlayer(Session second) {
		if (p2uname.equals(second.getUserProperties().get("username"))) {
			if(player2.size() == 0){
				if (gameStarted) {
					player2.add(second);
					System.out.println("gameHandler resumes player two");
				}
				else {
					player2.add(second);
					p2uname = (String)second.getUserProperties().get("username");
					gameStarted = true;
					System.out.println("gameHandler adds player two");
				}
			}
			else {
				player2.add(second);
				System.out.println("gameHandler adds and extra session for the second player");
			}
		}
	}

	public void addFirstPlayer(Session first) {
		if (p1uname.equals(first.getUserProperties().get("username"))) {
			if(player1.size() == 0){
				if (gameStarted && p1uname.equals(first.getUserProperties().get("username"))) {
					player1.add(first);
					System.out.println("gameHandler resumes player  one");
				}
				else {
					player1.add(first);
					gameStarted = true;
					System.out.println("gameHandler adds player  one\nAnd this is unusal you have to check this issue loop hole");
				}
			}
			else {
				player1.add(first);
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
			s.getBasicRemote().sendText(message.toString());
		}

		for(Session sub : broadcastList) {
			sub.getBasicRemote().sendText(message.toString());
		}
	}
}

class Game {
	HashMap<String, Integer> presentState;
}

class Piece {
	String type;
	int position;
	public Piece(String t, int p) {
		type = t;
		position = p;
	}

	public boolean cuts(Piece fallen) {
		return false;
	}

	public boolean movesTo(int fallen) {
		return false;
	}
}

class Move {

}