package chess.api;

import	java.io.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServlet;
import javax.json.*;
import java.util.*;

public class RESTapi extends HttpServlet {
		public  void  doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
				response.setContentType("application/json");
				Date date = new Date();
				//SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				JsonObject json = Json.createObjectBuilder().add("Date",date.toString()).add("Time",date.toString()).add("json","yes").build();
				PrintWriter writer = response.getWriter();
				writer.println(json.toString());
		}
}