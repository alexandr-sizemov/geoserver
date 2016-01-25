package it.polito;

public class POI {

	private final int lat;
	private final int lon;
	private final int id;
	private final int accuracy;
	
	/**
	 * Creates a POI (Point Of Interest) in the given latitude and longitude
	 */
	public POI(int lat, int lon,int id, int accuracy){
		this.lat = lat;
		this.lon = lon;
		this.id = id;
		this.accuracy = accuracy;
	}
	
	public int getAccuracy(){
		return accuracy;
	}
	
	public int getLat() {
		return lat;
	}

	public int getLon() {
		return lon;
	}
	
	public int getId() {
		return id;
	}
	
	
}
