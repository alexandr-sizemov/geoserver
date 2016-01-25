package it.polito;

public interface Constants {
	
	/*
	 * UNITA DI MISURA
	 * Le unita' di misura sono intese in gradi.
	 * Si puo' approssimare che un grado di latitudine/longitudine sia uguale a 111,111 KM
	 * 
	 * Le misure sono convertite e trattate come interi per:
	 * 1. ottimizzare lo spazio
	 * 2. permettere calcoli precisi non possibili con i double
	 * 
	 */
	
	public static final int ITALY_LENGHT = 	1024*1024*2;
	public static final int ITALY_LAT_CENTER = 	4143805; // 41,43805 degrees
	public static final int ITALY_LON_CENTER = 	1253402; // 12,53402 degrees
	public static final int COORDINATE_PRECISION = 100000; // es. 1° / 100000 ~= 1,11m
	
	public static final int POI_LIMIT = 30;			// max pois x cell
	public static final int SPLIT_LIMIT = 500; 		// max users x cell
	public static final int MIN_CELL_SIZE = 500;
	
	// coordinates validation
	public static final int MIN_LAT = (ITALY_LAT_CENTER-(ITALY_LENGHT/2))/COORDINATE_PRECISION;
	public static final int MAX_LAT = (ITALY_LAT_CENTER+(ITALY_LENGHT/2))/COORDINATE_PRECISION;
	public static final int MIN_LON = (ITALY_LON_CENTER-(ITALY_LENGHT/2))/COORDINATE_PRECISION;
	public static final int MAX_LON = (ITALY_LON_CENTER+(ITALY_LENGHT/2))/COORDINATE_PRECISION;

	// user
	public static final int DEVICE_X_USER = 2;
	public static final int TRAIL_LENGHT = 3;
	public static final int FRIENDSHIP_DISTANCE = 500;
	public static final int POI_DISTANCE = 500;
	public static final int MAX_ACCURACY = 1000;
	
	// clean timers
	public static final long CELL_CLEAN_INTERVAL = 100000000L;
	public static final long USER_EXPIRATION_TIME = 150000000L;
	
}
