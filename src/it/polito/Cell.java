package it.polito;

import it.polito.friendship.Friendship;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Cell {

	/**
	 * @author  Sizemov Alexandr (alexandr.sizemov@gmail.com)
	 * @version 1.0
	 * @since 	24-10-2013
	 */
	
	private final ReadWriteLock rwl=new ReentrantReadWriteLock(false);
	private final int lat;
	private final int lon;
	private final int size;
	private final Map<Integer, POI> POIs;
	private final Map<String, User> users;
	private final Cell[] subcells;
	private boolean splitted;
	private long nextCleanTime;
	
	private static Cell root;
	
	static {
		root=new Cell(Constants.ITALY_LAT_CENTER,Constants.ITALY_LON_CENTER,Constants.ITALY_LENGHT);
	}
	
	/**
	 * @return The root cell
	 */
	public static Cell getRoot() {
		return root;
	}

	/**
	 * Creates a cell centered in the given latitude, longitude of the given size
	 *
	 * @param  lat latitude of the center of the cell
	 * @param  lon longitude of the center of the cell
	 * @param  size width of the cell
	 */
	public Cell(int lat, int lon, int size) {
		this.lat=lat;
		this.lon=lon;
		this.size=size;
		
		POIs=new HashMap<Integer,POI>();
		users=new HashMap<String,User>();
		subcells=new Cell[4];
		splitted=false;
		nextCleanTime= System.currentTimeMillis() + Constants.CELL_CLEAN_INTERVAL;
	}
	
	/**
	 * Splits the cell in 4 sub cells of the same size and
	 * distributes its Users and POIs according to their position.
	 * <p>
	 * The split process is triggered in {@link #addUser(User, HashMap, Set, int, FriendPolicy)}
	 * when the user number exceeds the {@value Constants#SPLIT_LIMIT}
	 * 
	 */
	private void split() {
		rwl.writeLock().lock();
		try {
			if (splitted) return;
			int s2=size/2;
			int s4=s2/2;
			subcells[0]=new Cell(lat-s4,lon-s4,s2);
			subcells[1]=new Cell(lat-s4,lon+s4,s2);
			subcells[2]=new Cell(lat+s4,lon-s4,s2);
			subcells[3]=new Cell(lat+s4,lon+s4,s2);
			for (String k:users.keySet()) {
				User u1=users.get(k);
				Position p=u1.getInRangePosition(lat,lon,size);
				if(p!=null){
					if (subcells[0].overlap(p.getLat(), p.getLon(), p.getAccuracy())) 
						subcells[0].users.put(u1.getId(),u1);
					if (subcells[1].overlap(p.getLat(), p.getLon(), p.getAccuracy())) 
						subcells[1].users.put(u1.getId(),u1);
					if (subcells[2].overlap(p.getLat(), p.getLon(), p.getAccuracy()))
						subcells[2].users.put(u1.getId(),u1);
					if (subcells[3].overlap(p.getLat(), p.getLon(), p.getAccuracy()))
						subcells[3].users.put(u1.getId(),u1);
				}
			}
			for (Integer k:POIs.keySet()) {
				POI p=POIs.get(k);
				if (subcells[0].overlap(p.getLat(), p.getLon(), p.getAccuracy())) subcells[0].POIs.put(p.getId(),p);
				if (subcells[1].overlap(p.getLat(), p.getLon(), p.getAccuracy())) subcells[1].POIs.put(p.getId(),p);
				if (subcells[2].overlap(p.getLat(), p.getLon(), p.getAccuracy())) subcells[2].POIs.put(p.getId(),p);
				if (subcells[3].overlap(p.getLat(), p.getLon(), p.getAccuracy())) subcells[3].POIs.put(p.getId(),p);
			}
			users.clear();
			POIs.clear();
			splitted=true;
		} finally {
		rwl.writeLock().unlock();
		}
	}
	
	
	/**
	 * Adds a User into the cell.
	 * <p>
	 * Recursively penetrates the cell tree until it finds a leaf cell that overlaps with
	 * the Users position, then the given User is added to the cell. In the meanwhile all of the POIs
	 * and friends are added respectively to pois and friends parameters.
	 * <p>
	 * It may trigger the split or the clean process
	 * @see #split()
	 * @see #clean()
	 * 
	 * @param  u user
	 * @param  friends Users friends nearby the user position
	 * @param  pois POIs nearby the user position
	 * @param  deviceId the id of the device that sent the request
	 * @param friendPolicy 
	 */
	public void addUser(User u, HashMap<String,Position> friends, Set<POI> pois, Position p, Friendship friendship ) {
		if(!overlap(p.getLat(),p.getLon(),p.getAccuracy())) return;
		boolean addToChildren=false;
		boolean populateFriends=false;
		rwl.readLock().lock();
		try {
			if (splitted) {
				addToChildren=true;
			} else {
				rwl.readLock().unlock();
				rwl.writeLock().lock();
				try {
					if (splitted) addToChildren=true;
					else {
						if( nextCleanTime < System.currentTimeMillis() )
							clean();
						if (users.size()<Constants.SPLIT_LIMIT || size < Constants.MIN_CELL_SIZE) { //Add to local map
							users.put(u.getId(),u);
							populateFriends=true;
						} else { //SPLIT
							split();
							addToChildren=true;
						}
					}
				} finally {
					rwl.readLock().lock();
					rwl.writeLock().unlock();
				}
			}
			if (populateFriends) {
				if (friends!=null) {
					Set<String> userFriends = friendship.getFriends(u.getId());
					for (User u1:users.values()) {
						Position p1 = u1.getInRangePosition(lat, lon, size);
						if (p1 !=null &&
							userFriends.contains(u1.getId()) &&
							distance(p.getLat(), p.getLon(), p1.getLat(), p1.getLon()) < Constants.FRIENDSHIP_DISTANCE){
								friends.put(u1.getId(), p1);
						}
					}
				}
				if (pois!=null) {
					for(POI poi:POIs.values()){
						if(distance(p.getLat(),p.getLon(),poi.getLat(),poi.getLon()) < Constants.POI_DISTANCE)
							pois.add(poi);
					}
				}
			}
		} finally {
			rwl.readLock().unlock();
		}
		if (addToChildren) {
			for (int i=0; i<subcells.length; i++) {
				subcells[i].addUser(u,friends,pois,p,friendship);
			}
		}
	}

	
	/**
	 * Searches POIs and friends in the given position.
	 * <p>
	 * For every cell that overlaps with the given position and range all of the POIs and User friends of the cell are added respectively to pois and friends.
	 * @param  u user
	 * @param  friends Users friends nearby the user position
	 * @param  pois POIs nearby the user position
	 * @param  deviceId the id of the device that sent the request
	 */
	
	public void searchInRange(User u, HashMap<String,Position> friends, Set<POI> pois, Integer range, Position p, Friendship friendship ) {
		if(!overlap(p.getLat(),p.getLon(),range)) return;
		boolean addToChildren=false;
		rwl.readLock().lock();
		try {
			if (splitted) {
				addToChildren=true;
			} else {
				if (friends!=null) {
					Set<String> userFriends = friendship.getFriends(u.getId());
					for (User u1:users.values()) {
						Position p1 = u1.getInRangePosition(lat, lon, size);
						if (p1 !=null &&
							userFriends.contains(u1.getId()) &&
							distance(p.getLat(), p.getLon(), p1.getLat(), p1.getLon()) < Constants.FRIENDSHIP_DISTANCE){
								friends.put(u1.getId(), p1);
						}
					}
				}
				if (pois!=null) {
					for(POI poi:POIs.values()){
						if(distance(p.getLat(),p.getLon(),poi.getLat(),poi.getLon()) < Constants.POI_DISTANCE)
							pois.add(poi);
					}
				}
			}
		} finally {
			rwl.readLock().unlock();
		}
		if (addToChildren){
			for (int i=0; i<subcells.length; i++) {
				subcells[i].searchInRange(u,friends,pois,range,p,friendship);
			}
		}
	}
	
	/**
	 * Adds a POI in every cell that overlaps its position
	 * @param  p POI 
	 */
	
	public void addPOI(POI p) {
		if (!overlap(p.getLat(), p.getLon(), p.getAccuracy())) return;
		boolean addToChildren= false;
		rwl.readLock().lock();
		try {
			if (splitted) {
				addToChildren=true;
			} else {
				rwl.readLock().unlock();
				rwl.writeLock().lock();
				try {
					if (splitted) addToChildren=true;
					else {
						POIs.put(p.getId(), p);
					}
				} finally {
					rwl.readLock().lock();
					rwl.writeLock().unlock();
				}
			}
		} finally {
			rwl.readLock().unlock();
		}
		if (addToChildren) {
			for (int i=0; i<subcells.length; i++) {
				subcells[i].addPOI(p);
			}
		}
	}
	
	/**
	 * Adds a POI into the cell that overlaps its position.
	 * This method is intended to be used at startup, before adding any User, because it may
	 * trigger the split process if the POI amount exceeds the {@link Constants#POI_LIMIT} threshold.
	 * @see #split()
	 * 
	 * @param  p POI 
	 */
	
	public void preloadPOI(POI p) {
		if (!overlap(p.getLat(), p.getLon(), p.getAccuracy())) return;
		boolean addToChildren= false;
		rwl.readLock().lock();
		try {
			if (splitted) {
				addToChildren=true;
			} else {
				rwl.readLock().unlock();
				rwl.writeLock().lock();
				try {
					if (splitted) addToChildren=true;
					else {
						if (POIs.size()<Constants.POI_LIMIT || size < Constants.MIN_CELL_SIZE) { //Add to local map
							POIs.put(p.getId(), p);
						} else { //SPLIT
							split();
							addToChildren=true;
						}
					}
				} finally {
					rwl.readLock().lock();
					rwl.writeLock().unlock();
				}
			}
		} finally {
			rwl.readLock().unlock();
		}
		if (addToChildren) {
			for (int i=0; i<subcells.length; i++) {
				subcells[i].addPOI(p);
			}
		}
	}

	/**
	 * This method is triggered no more then once every {@value Constants#CELL_CLEAN_INTERVAL}
	 * It removes from this cell any user out of date.
	 *  
	 */
	private void clean() {
		rwl.writeLock().lock();
		try {
			long invalidTime = System.currentTimeMillis() - Constants.CELL_CLEAN_INTERVAL; 
			Iterator<String> it = users.keySet().iterator();
			while (it.hasNext()){
				String key = it.next();
				User user=users.get(key);
				Position p = user.getInRangePosition(lat, lon, size);
				if( p!=null && p.getTimestamp() < invalidTime || p==null)
					it.remove();
			}
			nextCleanTime = System.currentTimeMillis() + Constants.CELL_CLEAN_INTERVAL;
		}finally{
			rwl.writeLock().unlock();
		}
	}
	
	/**
	 * Verifys if the given latitude, longitude and accuracy intersect this cell
	 */
	private boolean overlap(int alat, int alon, int accuracy) {
		int s2=size/2;
		if (alat-accuracy>= lat+s2 ) return false;
		if (alat+accuracy< lat-s2 ) return false;
		if (alon-accuracy>= lon+s2 ) return false;
		if (alon+accuracy< lon-s2 ) return false;
		return true;
	}
	
	/**
	 * Returns the distance between two points
	 */
	private long distance(int lat, int lon, int lat2, int lon2){
		long latDelta = lat-lat2;
		long lonDelta = lon-lon2;
		return (long)Math.sqrt((latDelta*latDelta)+(lonDelta*lonDelta));
	}
	
	/* 
	 * STATISTICS METHODS
	 */

	/**
	 * For every cell in the tree adds into the Set a String in JSON format with all the cell informations.
	 * es. {"lat":"12.22","lon":"7.82","size":"8.20","pop":"345"}
	 * @param cells
	 */
	public void exploreJSON(HashSet<String> cells){
		
		if(!splitted){
			cells.add( "{\"lat\":\""+(double)lat/Constants.COORDINATE_PRECISION+"\"," +
					"\"lon\": \""+(double)lon/Constants.COORDINATE_PRECISION+"\"," +
					"\"size\": \""+(double)(size/2)/Constants.COORDINATE_PRECISION+"\"," +
					"\"pop\": \""+((double)users.size()/(double)Constants.SPLIT_LIMIT)+"\"}");
			return;
		}
		for (int i=0; i<subcells.length; i++)
			subcells[i].exploreJSON(cells);
	}
	
	/**
	 * Explores the tree cell and find out the total POIs number.
	 * @param level Must be set to 1 when invoked.
	 * @param show if true shows the in the stdout
	 * @return Total POIs
	 */
	public int explorePois(int level, boolean show){
		int res=0;
		rwl.readLock().lock();
		try {
			String s = String.format("%" + level + "s", " ") + this.size + " " +lat + " " + lon;
			if(!splitted) {
				s=s.concat(" pois:"+String.valueOf(POIs.size()));
				res=POIs.size();
			}
			if (show)
				System.out.println(s);
			if(splitted){
				for (int i=0; i<subcells.length; i++)
					res+=subcells[i].explorePois(level+1,show);
			}
		}
		finally {
			rwl.readLock().unlock();
		}
		return res;
	}
	
	/**
	 * Explore the tree cell and find out the total population number.
	 * @param level Must be set to 1 when invoked.
	 * @param show if true shows the in the console
	 * @return Total population
	 */
	
	public int explore(int level, boolean show){
		int res=0;
		rwl.readLock().lock();
		try {
			String s = String.format("%" + level + "s", " ") + this.size + " " +lat + " " + lon;
			if(!splitted) {
				s=s.concat(" users:"+String.valueOf(users.size()));
				res=users.size();
			}
			if (show)
				System.out.println(s);
			if(splitted){
				for (int i=0; i<subcells.length; i++)
					res+=subcells[i].explore(level+1,show);
			}
		}
		finally {
			rwl.readLock().unlock();
		}
		return res;
	}
	/**
	 * Explores the cell tree and return the higher tree level
	 * @param level Current level of the cell tree. Must be set to 1 when used manually.
	 * @param maxLevel Maximum tree level found so far. Must be set to 1 when used manuallly. 
	 * @return The highest tree level
	 */
	public int maxLevel(int level, int maxLevel){
		if(maxLevel < level) maxLevel = level;
		rwl.readLock().lock();
		try {
			if(splitted){
				for (int i=0; i<subcells.length; i++)
					maxLevel = subcells[i].maxLevel(level+1,maxLevel);
			}
		}
		finally {
			rwl.readLock().unlock();
		}
		return maxLevel;
	}
	/**
	 * Return the number of nodes in the tree cell
	 * @return total cell number
	 */
	public int nodeCount(){
		int count = 0;
		rwl.readLock().lock();
		try {
			if(splitted){
				for (int i=0; i<subcells.length; i++){
					count += subcells[i].nodeCount();
				}
				count++; // se stesso
			}else
				return 1;
		}
		finally {
			rwl.readLock().unlock();
		}
		return count;
	}

}
