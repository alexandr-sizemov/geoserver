package it.polito.test;

public class Comune {

	private final int lat;
	private final int lon;
	private final int raggio;
	private int popolazione;
	
	public Comune(int lat, int lon, int raggio, int popolazione){
		this.lat = lat;
		this.lon = lon;
		this.raggio = raggio;
		this.popolazione = popolazione;
	}

	public int getLat() {
		return lat;
	}

	public int getLon() {
		return lon;
	}

	public int getRaggio() {
		return raggio;
	}

	public int getPopolazione() {
		return popolazione;
	}
	
	public void setPopolazione(int popolazione){
		this.popolazione = popolazione;
	}
	
}
