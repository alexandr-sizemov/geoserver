package it.polito;

import it.polito.util.Utils;

import org.codehaus.jackson.annotate.JsonIgnore;

public class Position {
	
	private int lat;
	private int lon;
	private int accuracy;
	private long timestamp;

	/**
	 * Defines a position
	 */
	public Position(int lat, int lon, int accuracy, long timestamp){
		this.lat = lat;
		this.lon = lon;
		this.accuracy = accuracy;
		this.timestamp = timestamp;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public int getLat() {
		return lat;
	}

	public int getLon() {
		return lon;
	}

	@JsonIgnore
	public int getAccuracy() {
		return accuracy;
	}

	public String toJSON() {
		return "\"lat\":\""+(double)lat/Constants.COORDINATE_PRECISION+"\"," +
				"\"lon\": \""+(double)lon/Constants.COORDINATE_PRECISION+"\"," +
				"\"time\": \""+timestamp+"\"";
		
//		return Utils.objectToJSONString(this);
	}

}
