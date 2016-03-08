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
import org.json.*;


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
				case "clientMessage" :
					sendMessage(message, json.getString("target"));
					break;					
			}
		}
		else {
			System.out.println("malpractice identified");
		}
	}

	private void sendMessage(String message, String target) throws IOException{
		for(Session s: sessions) {
			if(s.isOpen() && s.getUserProperties().get("username").equals(target)) {
				s.getBasicRemote().sendText(message);
			}
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
	public GameHandler(Session starter, String opponent)  {
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
		if(game.clientMoveMade(message))
		{

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
	private JSONObject duplicate;
	private int[][] board;
	JSONObject tracking;
	int count = 0;
	JSONObject promotedPawns;

	public Game() {
		//JsonObjectBuilder obj  = ;
		initPos();
		System.out.println("game created with json "+json.toString());
		System.out.println("game created with json duplicate "+duplicate.toString());
		board = new int[9][9];
		try {
		setPiecesOnBoard();	
		}
		catch(JSONException e) {
			System.out.println("JSONException on setting up pieces on board " +e);
		}
	}

	public JsonObject getPositions() {
		System.out.println("getpos in game execuetes");
		return json;		
	}

	private void initPos() {
		try {
			System.out.println("initial postions adding");
		JsonObjectBuilder job = Json.createObjectBuilder().add("pawn",Json.createArrayBuilder().add(12).add(17).add(22).add(27).add(32).add(37).add(42).add(47)
			.add(52).add(57).add(62).add(67).add(72).add(77).add(82).add(87).build());
		job.add("knight",Json.createArrayBuilder().add(21).add(28).add(71).add(78).build());
		job.add("bishop",Json.createArrayBuilder().add(31).add(38).add(61).add(68).build());
		job.add("rook",Json.createArrayBuilder().add(11).add(18).add(81).add(88).build());
		job.add("queen",Json.createArrayBuilder().add(41).add(48).build());
		job.add("king",Json.createArrayBuilder().add(51).add(58).build());
		json = job.build();
		duplicate = new JSONObject(json.toString());
		System.out.println("done adding initial positons ");
		}
		catch (JSONException e) {
			System.out.println("Exception at creatng json on Game initpos"+e);
		}		
	}

	public boolean clientMoveMade(JsonObject j) {
		String attacker = j.getString("from");
		String player = attacker.substring(0,5);
		int pieceNum = Integer.parseInt(attacker.substring(attacker.length()-1));
		String attackerType = attacker.substring(5, attacker.length()-1);

		System.out.println(player + " moved piece "+attackerType+" of num " + pieceNum);
		System.out.println("piece is in  array pos " + getArrayPos(player, pieceNum));
		int start = Integer.parseInt(j.getString("start"));
		int end = Integer.parseInt(j.getString("to"));
		try {
			if(piecePurity(start, attackerType, getArrayPos(player, pieceNum)) && isValidMove(attackerType, player, pieceNum, duplicate.getJSONArray(attackerType).getInt(getArrayPos(player, pieceNum)), 
				Integer.parseInt(j.getString("to")))) {
				System.out.println("all satisfied ");
				changePos(j, start, end, getArrayPos(player, pieceNum), attackerType);
				printBoard("changed position");
				printJSON("changed JSON", duplicate);
				return true;
			}	
		}	
		catch(JSONException e) {
			System.out.println("JSONException occured while processing isvalid move " + e);
		}
		return false;
	}

	private void changePos(JsonObject j, int start, int end, int arraypos, String type) {
		System.out.println("changePos called with strart "+start+" end = "+end+" arraypos =  "+arraypos+"type = "+type);
		int[] track = {11,18,51,58,81,88};
		try{
			if(board[end/10][end%10] != 0 ) {
				System.out.println("Trget has some piece");
				if(j.getBoolean("castling", false)) {

					if(start%10 == 1) {
						if(end == 81) {
							duplicate.getJSONArray("king").put(0,71);
							duplicate.getJSONArray("rook").put(2,61);
							board[7][1] = -6;
							board[6][1]= -4;
						}
						else{
							duplicate.getJSONArray("king").put(0,21);
							duplicate.getJSONArray("rook").put(0,31);
							board[2][1] = -6;
							board[3][1]= -4;
						}
					}
					else if(start%10 == 8) {
						if(end == 88) {
							duplicate.getJSONArray("king").put(1,78);
							duplicate.getJSONArray("rook").put(3,68);
							board[7][8] = 6;
							board[6][8]= 4;
						}
						else{
							duplicate.getJSONArray("king").put(1,28);
							duplicate.getJSONArray("rook").put(1,38);
							board[2][8] = 6;
							board[3][8]= 4;
						}
					}
					board[start/10][start%10] = 0;
					board[end/10][end%10] = 0;
				}
				else {
					System.out.println("target has piece othere than castling");
					if(board[start/10][start%10] * board[end/10][end%10] < 0) {
						System.out.println("target piece is found to have oppoenent piece");
						System.out.println("cutting moves");
						try{
							cutted(end);
							System.out.println("cutted finished");
						}
						catch(JSONException e) {
							System.out.println("JSONException occured at maing  cut piece " + e);
						}
						duplicate.getJSONArray(type).put(arraypos, end);
						board[end/10][end%10] = board[start/10][start%10];
						board[start/10][start%10] = 0;
						if(j.getString("promotion", "") != "") {
							System.out.println("promotion is eligibles");
							promotion(j, start, end, arraypos, type);
						}

					}
				}
			}
			else {
				board[end/10][end%10] = board[start/10][start%10];
				board[start/10][start%10] = 0;

				duplicate.getJSONArray(type).put(arraypos, end);				
			}
			if(Arrays.binarySearch(track, start) > -1) {
				tracking.put(String.valueOf(start),true);
			}
		}catch(JSONException e) {
			System.out.println("JSONException at changePos " + e) ;
		}
	}

	private void cutted(int pos) throws JSONException{
		System.out.println("cuuted called for "+ pos);
		JSONArray array = duplicate.names();
		int len = duplicate.length();
		for(int i =0; i <len;i++){
			String type = array.getString(i);
			JSONArray ja = duplicate.getJSONArray(type);
			int len2 = ja.length();
			for(int j = 0 ; j < len2 ; j++) {
				if(ja.getInt(j) == pos) {
					ja.put(j, 100+count);
					count++;
					return;
				}				
			}			
		}
	}

	private void promotion(JsonObject j, int start, int end, int arraypos, String type) {

	}

	private boolean isValidMove(String attackerType, String player, int pieceNum, int src, int dest) throws JSONException {
		if(!notMyPiece(src, dest) && !attackerType.equals("king")) {
			System.out.println("Cuting same playerr piece and thats not a king");
			return false;
		}
		System.out.println("In valid Move cheking");
		boolean result =false;
		String dir;
		switch (attackerType) {
			case "pawn" :
			System.out.println("Checking is valid pawn");
				if(isPawn(src, dest)) {
					System.out.println("pawn move verified");
					return true;
				}
				break;
			case "knight" :
				if(isHorse(src, dest)) {
					System.out.println("Found to be valid knight move");
					return true;
				}
				break;
			case "bishop" :
				if(isCross(src, dest) && !hasIntermediate(src, dest, "cross")) {
					System.out.println("bishop move verified");
					return true;
				}
				break;
			case "rook" :
				if(isPlus(src, dest) && !hasIntermediate(src, dest, "plus")) {
					return true;
				}
				break;
			case "queen" :
				
				if(((dir = isQueenCross(src, dest) ) != null )&& !hasIntermediate(src, dest, dir)) {
					return true;
				}
				break;
			case "king" :
				if(isKing(src, dest)) {
					return true;
				}
				else if(isCastling(attackerType, player, pieceNum, src, dest)) {
					return true;
				}
				break;
		}

		return result;
	}

	private boolean notMyPiece(int src, int dest) {
		System.out.println("Checking not my piece");
		if((board[src/10][src%10] * board[dest/10][dest%10]) > 0) {

			return false;
		}
		return true;
	}

	private boolean piecePurity(int st, String type, int  pnum ) {
		System.out.println("Checking purity start = "+st+" type = "+type+" pieceNumber = "+pnum);
		try {
			printJSON("at chking piecePurity", duplicate);
			if(duplicate.getJSONArray(type).getInt(pnum) == st) {
				System.out.println("purity verified");
				return true;
			}
		}
		catch(JSONException e) {
			System.out.println("JSONException at piecePurity  "+e);
		}
		return false;
	}

	private void printJSON(String info, JSONObject j){
		System.out.println(info);
		System.out.println(j.toString());
	}
	private int getArrayPos(String player, int count) {

		if(player.equals("white")) {
			return count*2 -1;
		}
		return (count - 1 )*2;
	}

	private void setPiecesOnBoard() throws JSONException {
		int len = duplicate.length();
		JSONArray jnames = duplicate.names();
			for(int i = 0; i < len;i++) {
				JSONArray ja = duplicate.getJSONArray(jnames.getString(i));
				int len2 = ja.length();
				int value;
				switch(jnames.getString(i)) {
					case "pawn":
						value = 1;
						break;
					case "knight":
						value = 2;
						break;
					case "bishop":
						value = 3;
						break;
					case "rook":
						value = 4;
						break;
					case "queen":
						value = 5;
						break;
					case "king" :
						value = 6;
						break;
					default:
						value = 0;
						break;
				}
				for(int j = 0; j < len2; j++) {
					int pos = ja.getInt(j);
					int sym = j%2 == 0? -1: 1;
					board[pos/10][pos%10] = sym * value;
				}
			}
			tracking = new JSONObject("{\"11\":false, \"18\":false,\"51\":false,\"58\":false,\"81\":false,\"88\":false}");
			promotedPawns = new JSONObject();

			printBoard("Initial Setup of the Game");		
	}

	private void printBoard(String info) {

		System.out.println(info);
		for(int k=1;k<=8;k++) {
				for(int l=1;l<=8;l++) {
					System.out.print(board[k][l]+" ");
				}
				System.out.println();
			}
	}

	private boolean isCross(int start, int end) {
		int temp;
		if(start > end) {
			temp = end;
			end = start;
			start = temp;
		}				
		for(temp = start;start <= end;start += 11) {
			if(start == end) {
				return true;
			}
		}
		
		for(start = temp;start <= end;start += 9) {
			if(start == end) {
				return true;
			}
		}
		return false;
	}

	private boolean isPlus(int start, int end) {
		if( (start/10) == (end/10)|| (start%10) == (end % 10)) {
			return true;
		}
		return false;
	}

	private String isQueenCross(int start, int end) {
		if(isCross(start, end)){
			return "cross";
		}
		else if(isPlus(start, end)) {
			return "plus";
		}
		return null;
	}

	private boolean isPawn(int start, int end) {
		if(board[start/10][start%10] < 0 && (end - start == 1)) {
			return true;
		}
		else if(board[start/10][start%10] > 0 && (end - start == -1)) {
			return true;
		}
		else if(start%10 == 2 && end-start == 2) {
			return true;
		}
		else if(start % 10 == 7 && end-start == -2) {
			return true;
		}
		else if(board[start/10][start%10] > 0) {
			if(end == start+9 && board[end/10][end%10] < 0 ) {
				return true;
			}
			else if(end == start - 11 && board[end/10][end%10] < 0) {
				return true;
			}
		}
		else if(board[start/10][start%10] < 0) {
			if(end == start+11 && board[end/10][end%10] > 0) {
				return true;
			}
			else if(end == start -9 && board[end/10][end%10] > 0) {
				return true;
			}
		}	

		System.out.println("is pawn result false");
		return false;
	}

	private boolean isHorse(int start, int end) {
		System.out.println("Checking isHorse");
		int[] horse = {-21,-19,-12,-8,8,12,19,21};
		if( Arrays.binarySearch(horse, start-end) > -1 ) {
			return true;
		}
		return false;
	}

	private boolean hasIntermediate(int start, int end, String type) {

		System.out.println("Checking has intermediate for "+type);
		System.out.println("called with start = "+ start+ " end = " + end);
		int increment = 0;
		if(start > end) {
			int temp = end;
			end = start;
			start = temp;
		}
		switch(type) {
			case "plus":
				if((start%10 == end%10)) {
					increment = 10;
				}
				else if(start/10 == end/10){
					increment = 1;
				}

				break;
			case "cross":
				if((end-start)%11 ==0) {
					increment = 11;
				}
				else if((end - start)%9 == 0 ) {
					increment = 9;
				}
				break;
			case "castle" :
				increment = 10;
				break;
		}

		System.out.println("intermediate increment is "+increment);
		printBoard("The position before calling checking the intermediate");
		for(start+=increment; start < end;start+=increment) {
			System.out.println("start = "+start+" end = " + end);
			if(board[start/10][start%10] != 0) {
				return true;
			}
		}
		System.out.println("no intermediate found");
		return false;
	}
	private boolean isKing(int start, int end) {
		int[] king = {-11,-10,-9,-1,1,9,10,11};
		if(Arrays.binarySearch(king, start - end) > -1) {
			return true;
		}
		return false;
	}

	private boolean isCastling(String attackerType, String player, int pieceNum, int src, int dest) throws JSONException{
		int[] validDest = {11,18,81,88};
		if((src == 51 || src == 58) && (Arrays.binarySearch(validDest, dest) > -1) 
			&& !tracking.getBoolean(String.valueOf(dest)) && !tracking.getBoolean(String.valueOf(dest))) {
			return true;
		}
		return false;	
	}
	
}
