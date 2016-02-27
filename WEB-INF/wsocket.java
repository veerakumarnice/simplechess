package serverend;

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


@ServerEndpoint(value="/wsocket")
public class wsocket {

	@OnOpen
	public void onOpen(final Session session) throws IOException, EncodeException{
		System.out.println("client connected");
		JsonObject json = Json.createObjectBuilder().add("message", "sent from server").build();
		session.getBasicRemote().sendText(json.toString());
	}
	
	@OnMessage
	public String onMessage(String message, final Session session) {
		System.out.println("Received from client :" + message);
		JsonObject json = Json.createObjectBuilder().add("message", message).build();
		System.out.println(json);
		try {
			for(Session s : session.getOpenSessions()) {
				if(s.isOpen()) {
					s.getBasicRemote().sendText(json.toString());
				}
			}
		}
		catch(IOException e) {
			System.out.println("IOException occured at wsocket");
		}

		return json.toString();//"echo " + message + session.getOpenSessions() +" "+ object.toString()+" " + user ;
	}

	@OnClose
	public void onClose() {
		System.out.println("Connection close");
	}

	@OnError
	public void onError(Throwable e) {
		System.out.println("Error occured at wsocket" + e);
	}


}

