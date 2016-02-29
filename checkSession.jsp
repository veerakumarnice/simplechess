<%@ page language="java" import="javax.servlet.*"%>

<% 
	//String yes = "username of "+title+" :"+(String)session.getAttribute("username");//System.out.println();
	RequestDispatcher rd ;
	if(session.getAttribute("username") == null) {
		System.out.println("username set to null");
		rd = request.getRequestDispatcher("index.jsp");
		rd.forward(request, response);
	}
%>

