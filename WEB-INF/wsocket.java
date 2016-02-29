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
	private static Set<Session> sessions = Collections.synchronizedSet(new HashSet<Session>());
	@OnOpen
	public void onOpen(final Session session) throws IOException, EncodeException{
		System.out.println("client connected");
		sessions.add(session);
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
					break;
				case "needActiveUsers":
					session.getBasicRemote().sendText(getActiveUsers(session));
					return;
					
		}


		//System.out.println(json.toString());
		//JsonObject json = Json.createObjectBuilder().add("message", message + "sent from server").build();
		System.out.println(json);
		try {
			for(Session s : sessions) {
			//	System.out.println("session open :" + s.getId());
			//	if(s.isOpen()) {
				if(!s.equals(session)) {
					System.out.println("sent to client " + s.getId() +" list size is "+sessions.size());
					s.getBasicRemote().sendText(message);
				}
				//}
			}
		}
		catch(IOException e) {
			System.out.println("IOException occured at wsocket");
		}

		//return json.toString();//"echo " + message + session.getOpenSessions() +" "+ object.toString()+" " + user ;
	}

	private String getActiveUsers(Session session) {
		String array = "[";
		int size = sessions.size();
		int count = 0;
		for (Session s : sessions) {
			array += "'"+s.getUserProperties().get("username") +"'";
			count++;
			if (count != size) {
				array += ",";
			}
		}
		array += "]";
	/*	JsonReader jreader = Json.createReader(new StringReader(array));
		JsonArray jArray = jreader.readArray();*/
		return Json.createObjectBuilder().add("notify","invoked").build().toString();//jArray.toString();
	}


	@OnClose
	public void onClose(Session session) {
		System.out.println("Connection close");
		sessions.remove(session);
	}

	@OnError
	public void onError(Throwable e) {
		System.out.println("Error occured at wsocket" + e);
	}


	private String getTurn() {
		if(turn) {
			turn = false;
			return "white";
		}
		else {
			turn = true;
			return "black";
		}
		
	}


}

