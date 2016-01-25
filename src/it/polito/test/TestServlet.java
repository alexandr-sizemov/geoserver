package it.polito.test;

import it.polito.Cell;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public TestServlet() {}

    @Override
    public void init(ServletConfig config) throws ServletException {
    }
    
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Integer userid,lat,lon,accuracy,deviceid;
    	try
		{
			 accuracy = Integer.valueOf(request.getParameter("accuracy"));
			 lat = Integer.valueOf(request.getParameter("lat"));
			 lon = Integer.valueOf(request.getParameter("lon"));
			 userid = Integer.valueOf(request.getParameter("userid"));
			 deviceid = Integer.valueOf(request.getParameter("deviceid"));
			
			if(lat > 90 || lat < -90 || lon > 180 || lon < -180 || userid < 0 || deviceid < 0 || accuracy < 0)
				throw new NumberFormatException();
		}
		catch(NumberFormatException e)
		{
			response.setContentType("application/json");
			response.setStatus(400);
			PrintWriter pw = response.getWriter();
			String output = "{\"error\":\"bad parameters format.\"} ";
	        pw.print(output);
	        pw.close();
			return;
		}
    	
		if(request.getParameter("explore") != null){
			HashSet<String> set = new HashSet<String>();
			Cell.getRoot().exploreJSON(set);
			
			PrintWriter pw = response.getWriter();
			pw.print("[");
			Iterator<String> it = set.iterator();
			while(it.hasNext()){
				String s = it.next();
				pw.print(s);
				if(it.hasNext()) pw.print(",");
			}
			
			pw.print("]");
	        pw.close();
	        
			return;
		}
    	
        
	}

}
