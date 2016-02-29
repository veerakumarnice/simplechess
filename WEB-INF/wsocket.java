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
	
	private static boolean turn = true;
	private static boolean assigned = false;
	private static Set<Session> sessions = Collections.synchronizedSet(new HashSet<Session>());
	private Session opponent;

	@OnOpen
	public void onOpen(final Session session) throws IOException, EncodeException{
		System.out.println("client connected");
		if(sessions.size() == 2) {
			JsonObject json = Json.createObjectBuilder().add("notify", "sessionOverflow").add("count",sessions.size()).build();
			session.getBasicRemote().sendText(json.toString());
			session.close();
		}
		else {
			sessions.add(session);
		}
		//JsonObject json = Json.createObjectBuilder().add("notify", "clientConnected").add().build();
		//session.getBasicRemote().sendText(json.toString());
	}
	
	@OnMessage
	public void onMessage(String message, final Session session, @PathParam("user") final String user) throws IOException{
		System.out.println("Received from client :" + message);
		JsonReader jreader = Json.createReader(new StringReader(message));
		JsonObject json =  jreader.readObject();
		jreader.close();

		switch (json.getString("notify")) {
				case "clientConnected":
					System.out.println("New Client connected :" +json.getString("username"));
					session.getUserProperties().put("username",json.getString("username"));
					System.out.println(json);
					notify(message, session, "all");
					break;
				case "needActiveUsers":
					session.getBasicRemote().sendText(getActiveUsers(session).toString());
					System.out.println("Sent active users list to " + json.getString("username")) ;
					break;
				case "assignPlayers" :
					assignPlayers();
					break;
				case "clientMoveMade" :					
					notify(message, session, "opponent");
					break;
		}
	}

	private boolean startGame(String opp) throws IOException {
		for (Session s : sessions) {
			if(s.getUserProperties().get("username").equals(opp)) {
				opponent = s;
			}
		}
		return true;
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

				break;
		}			
	}

	private JsonObject getActiveUsers(Session session) throws IOException {
	
		Set<Session> list = getPlayerList();
		JsonArrayBuilder object = Json.createArrayBuilder();
		for(Session s : list) {
			object.add((String)s.getUserProperties().get("username"));
		}
		JsonArray array = object.build();
		JsonObject json = Json.createObjectBuilder().add("notify","activeUsers").add("users",array).build();
		return json;

	}


	@OnClose
	public void onClose(Session session) {
		System.out.println("Connection close");
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
		assigned = true;
	}

	private static Set<Session> getPlayerList() {
		return sessions;
	}


}