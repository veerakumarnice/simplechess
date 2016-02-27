<%@ page language="java" import="javax.servlet.*"%>

<% 
	RequestDispatcher rd ;
	if(session.getAttribute("username") != null) {
		rd = request.getRequestDispatcher("home.jsp");
		rd.forward(request, response);
	}
%>
