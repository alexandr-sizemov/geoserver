package it.polito;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class User {

	private final String id;
	private final HashMap<Integer, LinkedList<Position>> userDevices;
	
	/**
	 * A User contains:
	 * {@link User#userDevices} every user may own multiple devices, each device has a list of positions
	 * @param  id userId
	 */
	public User(String id) {
		this.id = id;
		userDevices = new HashMap<Integer,LinkedList<Position>>();
	}
	
	/**
	 * Adds a new position to the given device, overwriting the oldest if there is no space in the list
	 * If the device is new, a new list of positions is created 
	 * 
	 * @param lat latitude of the position
	 * @param lon longitude of the position
	 * @param accuracy accuracy in meters
	 * @param timestamp timestamp
	 * @param deviceId device id
	 */
	public synchronized void addPosition(int lat, int lon, int accuracy, long timestamp, int deviceId) {
		Position position=new Position(lat,lon,accuracy,timestamp);
		LinkedList<Position> deviceSamples;
		
		if(userDevices.isEmpty() || !userDevices.containsKey(deviceId)){
			deviceSamples = new  LinkedList<Position>();
			deviceSamples.add(position);
			userDevices.put(deviceId, deviceSamples);
		}else{
			deviceSamples = userDevices.get(deviceId);
			if(deviceSamples.size() >= Constants.TRAIL_LENGHT){
				deviceSamples.remove();
				deviceSamples.add(position);
			}else{
				deviceSamples.add(position);
			}
		}
		clean();
	}


	/**
	 * Searches for this user a position that overlaps the given cell coordinates.
	 * 
	 * @param cellLat cells latitude
	 * @param cellLon cells longitude
	 * @param cellDim cells size
	 * @return a position object or null if there is no position that overlaps the given cell coordinates
	 */
	public synchronized Position getInRangePosition(int cellLat, int cellLon, int cellDim){
		Position p = null;
		Iterator<Integer> i = userDevices.keySet().iterator();
		while( i.hasNext() ){
			Iterator<Position> it = userDevices.get(i.next()).iterator();
			while( it.hasNext() ){
				p = it.next();
				if( overlap(cellLat, cellLon, cellDim, p.getLat(), p.getLon(), p.getAccuracy()) )
					return p;
			}
		}
		return null;
	}
	
	/**
	 * Test if the 2 sets of coordinates overlaps
	 * @param cellLat latitude of the cell
	 * @param cellLon longitude of the cell
	 * @param cellDim cells size
	 * @param userLat users latitude
	 * @param userLon users longitude
	 * @param accuracy users accuracy
	 * @return
	 */
	private boolean overlap(int cellLat, int cellLon, int cellDim, int userLat, int userLon, int accuracy ) {
		int s2 = cellDim/2;
		if (userLat-accuracy>= cellLat+s2 ) return false;
		if (userLat+accuracy<  cellLat-s2 ) return false;
		if (userLon-accuracy>= cellLon+s2 ) return false;
		if (userLon+accuracy<  cellLon-s2 ) return false;
		return true;
	}
	
	/**
	 * Cleans for every device all the positions out of date.
	 * A position is considered out of date if is older then {@link Constants#USER_EXPIRATION_TIME}
	 */
	private synchronized void clean(){

		boolean purgeComplete;
		Position currentPosition;
		long cleanTime = System.currentTimeMillis() - Constants.USER_EXPIRATION_TIME;
		
		// for each device
		Iterator<Integer> i = userDevices.keySet().iterator();
		while( i.hasNext() )
		{
			Integer key = i.next();
			purgeComplete = false;
			LinkedList<Position> samplesList = userDevices.get(key);
			// for each sample
			while( !purgeComplete && samplesList.size() > 1 ){
				currentPosition = samplesList.remove();
				// remove if current position is too old OR if the next position (recent) is in the same cell
				if( currentPosition.getTimestamp() > cleanTime || 
					(samplesList.getFirst().getLat() == currentPosition.getLat() &&
					 samplesList.getFirst().getLon() == currentPosition.getLon()) ){
						purgeComplete = true;
						samplesList.addFirst(currentPosition);
				}
			}
			// if there are no more sample for this device, the device is removed
			if(samplesList.isEmpty()){
				i.remove();
			}
		}
	}
	
	public String getId(){
		return id;
	}
	
	
	@Override 
	public boolean equals(Object o) {
		if (o==null) return false;
		if (o.getClass()!=getClass()) return false;
		User u=(User) o;
		return u.id==this.id;
	}
	@Override
	public int hashCode() {
		return id.hashCode();
	}
}
