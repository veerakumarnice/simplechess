package chess.db;

import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.*;
import javax.json.*;
import javax.servlet.*;
import java.util.ArrayList;


public class Bean extends HttpServlet{
	ArrayList<User> users = new ArrayList<User>();
	ArrayList<User> activeUsers = new ArrayList<User>();

	public void init() {
		users.add(new User("veerakumarnice@gmail.com","nicepass","veera","kumar", 1));
		users.add(new User("arjungowthaman@gmail.com","arjun","arjun","gowthaman", 2));
		users.add(new User("ramprasanth@gmail.com","ranragav","ram","prasanth", 3));
		System.out.println("number of users " + users.size());
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		System.out.println("Do Get method of bean executes");
		if(request.getParameter("logout") != null) {
			request.getSession().invalidate();
			response.setContentType("application/json");
			JsonObject json = Json.createObjectBuilder().add("url","index.jsp").build();
			PrintWriter writer = response.getWriter();
			writer.println(json);
		}
		else
		 	goToError(request, response);
	}


	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		//goToError(request, response);

		System.out.println("do post called");
		String action = request.getParameter("source");
		String user;
		String pass;
		if(action.equals("login")) {
			if(!loginExist(request.getSession())) {
				user = request.getParameter("email"); 
				pass = request.getParameter("pass");
				login(user, pass, request, response);
				//System.out.println("login after that");
				return;
			}
		}
		else if(action.equals("signup")) {
			user = request.getParameter("email"); 
			pass = request.getParameter("pass");
			String firstname = request.getParameter("firstname");
			String lastname = request.getParameter("lastname");
			if(!userExist(user)) {
				System.out.println("User does not exist");
				signup(user, pass, firstname, lastname);
				System.out.println("number of users " + users.size());				
			}
			else {
				goToError(request, response);
			}
		}
		else {
			goToError(request, response);
		}
	}



	private boolean loginExist(HttpSession session) {
		if(session.getAttribute("username") != null) {
			return true;
		}	
		return false;
	}

	private boolean signup(String email, String pass, String firstname, String lastname) {
		users.add(new User(email, pass, firstname, lastname, users.size()+1));
		return true;
	}	
	private void goToError(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		RequestDispatcher rd = request.getRequestDispatcher("errorpage.jsp");
		rd.forward(request, response);
	}

	private void goToHome(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		RequestDispatcher rd = request.getRequestDispatcher("home.jsp");
		rd.forward(request, response);
	} 

	private boolean userExist(String user)  {
		for(User u:users) {
			if(u.getUserName().equals(user)) {
				return true;
			}
		}	
		return false;
	}

	private boolean login(String email, String pass, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		for(User u:users) {
			if(u.getUserName().equals(email)) {
				System.out.println("logging in for "+ email + " user exists");
				if(u.getPass().equals(pass)) {

					HttpSession session = request.getSession();
					session.setAttribute("username", email);
					session.setAttribute("firstname", u.getFirstName());
					session.setAttribute("lastname", u.getLastName());
					session.setAttribute("userid", u.getUserId());
					//goToHome(request, response);
					//System.out.println("Dispatching to chess jsp file");
					response.sendRedirect("/simplechess/chess");
					return true;
					//RequestDispatcher rd = request.getRequestDispatcher("chess.jsp");
					//rd.forward(request, response);
					
				}
				else {
					System.out.println("logging in for "+ email + " user exists but wrong pass");
					goToError(request, response);
					
				}
			}
		}

		return true;
	}
}


class User {
	int userid;
	String pass;
	String username;
	String firstname;
	String lastname;
	public User(String email, String pass, String firstname, String lastname,int id) {
		this.username = email;
		this.pass = pass;
		this.firstname = firstname;
		this.lastname = lastname;
		this.userid = id;
	}

	public String getFirstName() {
		return	firstname;
	}

	public String getLastName() {
		return lastname;
	}

	public String getFullName() {
		return getFirstName() + " " + getLastName();
	}

	public String getUserName() {
		return username;
	}

	public String getPass() {
		return pass;
	}

	public int getUserId() {
		return userid;
	}

	public JsonObject getUserInfo() {
		JsonObject json = Json.createObjectBuilder().add("userId",userid).add("userName",username).add("firstName",firstname)
		.add("lastName",lastname).build();
		return json;
	}
}