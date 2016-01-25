package it.polito;

import java.util.ArrayList;
import java.util.List;

public class AroundResponse {
	List<GeoUser> users  = new ArrayList<GeoUser>();
	List<POI> 	  pois	 = new ArrayList<POI>();
	
	public List<GeoUser> getUsers() {
		return users;
	}

	public void setUsers(List<GeoUser> users) {
		this.users = users;
	}

	public List<POI> getPois() {
		return pois;
	}

	public void setPois(List<POI> pois) {
		this.pois = pois;
	}
	
	public static class GeoUser{
		private String id;
		private int lat;
		private int lon;
		private long timestamp;
		
		public GeoUser() {
		}
		
		public GeoUser(String id, Position pos) {
			this.id = id;
			this.lat = pos.getLat();
			this.lon = pos.getLon();
			this.timestamp = pos.getTimestamp();
		}
		
		public int getLat() {
			return lat;
		}
		public void setLat(int lat) {
			this.lat = lat;
		}
		public int getLon() {
			return lon;
		}
		public void setLon(int lon) {
			this.lon = lon;
		}
		public long getTimestamp() {
			return timestamp;
		}
		public void setTimestamp(long timestamp) {
			this.timestamp = timestamp;
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
	}

}