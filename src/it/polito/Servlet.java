package it.polito;

import it.polito.AroundResponse.GeoUser;
import it.polito.friendship.Friendship;
import it.polito.friendship.impl.MockFriendship;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static it.polito.util.Utils.*;

public class Servlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private final ConcurrentHashMap<Integer, User> users = new ConcurrentHashMap<Integer, User>();

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		double lat, lon;
		Integer userid, accuracy, deviceid, intLat, intLon;
		
		try
		{
			String param = request.getParameter("accuracy");
			if(param==null) throw new NumberFormatException();
			accuracy = Integer.valueOf(param);
			
			param = request.getParameter("lat");
			if(param==null) throw new NumberFormatException();
			lat = Double.valueOf(param);
			
			param = request.getParameter("lon");
			if(param==null) throw new NumberFormatException();
			lon = Double.valueOf(param);
			
			param = request.getParameter("userid");
			if(param==null) throw new NumberFormatException();
			userid = Integer.valueOf(param);
			
			param = request.getParameter("deviceid");
			if(param==null) throw new NumberFormatException();
			deviceid = Integer.valueOf(param);
			
			if(lat > Constants.MAX_LAT || lat < Constants.MIN_LAT ||
				lon > Constants.MAX_LON || lon < Constants.MIN_LON ||
				userid < 0 || deviceid < 0 || accuracy < 0 || accuracy > Constants.MAX_ACCURACY)
					throw new NumberFormatException();
		}
		catch(NumberFormatException e)
		{
			response.setContentType("application/json");
			response.setStatus(400);
			PrintWriter pw = response.getWriter();
			String output = "{\"error\":\"bad parameters format.\"}";
			Set<Entry<String, String[]>> EntrySet = request.getParameterMap().entrySet();
			for(Entry<String,String[]> entry : EntrySet ){
				output+= entry.getKey() + " = "+ entry.getValue()[0]+"/n";
			}
	        pw.print(output);
	        pw.close();
			return;
		}
		
		Friendship friendship = new MockFriendship();
		
		HashMap<String,Position> friends = new HashMap<String,Position>();
		Set<POI> pois = new HashSet<POI>();
		User u = users.get(userid);
		intLat = (int)Math.floor(lat * Constants.COORDINATE_PRECISION);
		intLon = (int)Math.floor(lon * Constants.COORDINATE_PRECISION);
		Position position = new Position(intLat, intLon, accuracy, System.currentTimeMillis());
		if( u == null ){
			u = new User(userid.toString());
			u.addPosition(intLat, intLon, accuracy, System.currentTimeMillis(),deviceid);
			users.put(userid, u);
		}else{
			u.addPosition(intLat, intLon, accuracy, System.currentTimeMillis(),deviceid);
		}
		Cell.getRoot().addUser(u, friends, pois, position, friendship);

		response.setStatus(200);
		response.setContentType("application/json");
        
//        String output = new String();
//        Iterator <String> it = friends.keySet().iterator();
//        
//        output = "{\"users\":[";
//        while(it.hasNext()) {
//        	String key = it.next();
//        	output += "{"+"\"id\":"+key+","+friends.get(key).toJSON()+"}";
//        	if(it.hasNext()) output+=",";
//        }
//        output+="]}";

        AroundResponse ar = new AroundResponse();
        for (Map.Entry<String, Position> entry : friends.entrySet()) {
            String id = entry.getKey();
            Position pos = entry.getValue();
            ar.users.add(new GeoUser(id,pos));
        }
        ar.pois = new ArrayList<POI>(pois);
        String output = objectToJSONString(ar);
		
        PrintWriter pw = response.getWriter(); 
        pw.print(output);
        pw.close();
	}

}
