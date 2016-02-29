<%@ page language="java" import="javax.servlet.*"%>

<% 
	boolean loggedin = false;
	RequestDispatcher rd ;
	if(session.getAttribute("username") != null) {
		rd = request.getRequestDispatcher("home.jsp");
		rd.forward(request, response);
		loggedin = true;
	}
%>
