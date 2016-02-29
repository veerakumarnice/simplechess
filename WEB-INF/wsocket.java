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
					notifyOpponent(message, session);
					break;
				case "needActiveUsers":
					session.getBasicRemote().sendText(getActiveUsers(session).toString());
					System.out.println("Sent active users list to " + json.getString("username")) ;
					break;
				case "assignPlayers" :
					assignPlayers();
					break;
				case "clientMoveMade" :
					notifyOpponent(message, session);
					break;					
		}


		//System.out.println(json.toString());
		//JsonObject json = Json.createObjectBuilder().add("message", message + "sent from server").build();
		

		//return json.toString();//"echo " + message + session.getOpenSessions() +" "+ object.toString()+" " + user ;
	}

	private void notifyOpponent(String message, Session session) throws IOException{
		System.out.println("notifyOpponent called");
			for(Session s : sessions) {
		//	System.out.println("session open :" + s.getId());
		//	if(s.isOpen()) {
				if(!s.equals(session)) {
					s.getBasicRemote().sendText(message);
				}
			//}
			}				
	}

	private JsonArray getActiveUsers(Session session) throws IOException {
		//JsonArray array = Json.createArrayBuilder().add("veera").build();
		String st = "[";
		int count = 1;
		int size = sessions.size();
		for (Session s : sessions) {
			if (!s.equals(session)) {
		//		array.add("username", (String) s.getUserProperties().get("username"));
				st +=(String) s.getUserProperties().get("username");
			}
			if(count >= size) {
				st += ",";

			}
			count++;		
		}
		st += "]";
		//array.build();
		JsonReader jreader = Json.createReader(new StringReader(st));
		JsonArray array =  jreader.readArray();
		jreader.close();
		return array;
	}


	@OnClose
	public void onClose(Session session) {
		System.out.println("Connection close");
		sessions.remove(session);
	}

	@OnError
	public void onError(Throwable e) {
		System.out.println("Error occured at wsocket\n");
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
		/*if(assigned) {
			return;
		}*/

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

