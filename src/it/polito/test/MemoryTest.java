package it.polito.test;

import it.polito.Cell;
import it.polito.Constants;
import it.polito.POI;
import it.polito.Position;
import it.polito.User;
import it.polito.friendship.Friendship;
import it.polito.friendship.impl.MockFriendship;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MemoryTest {
	public static final int MAXUSERS = 10000;
	public static final int MAX_ACCURACY = 100;
	public static final int POI_FACTOR = 5000;
	public static final String FILE_COMUNI = "resources/Comuni.txt";
	public static final String FILE_POI = "resorces/Poi.txt";
	
	private static final ConcurrentHashMap<Integer, User> users = new ConcurrentHashMap<Integer, User>();
	
	public static void main(String[] args) {
		//loadPoi();
		testComuni();
	}

	public static void testComuni(){

		Random random = new Random();
  		final AtomicInteger ai=new AtomicInteger();
		final AtomicInteger friendCounter = new AtomicInteger();
		friendCounter.set(0);
		int indiceComune, popolazione, raggio, lat, lon, accuracy;
		final int deviceId=0;
		double angolo;
		Comune comune;
		ArrayList<Comune> listaComuni = new ArrayList<Comune>();
		if(TestComuni.readComuni(listaComuni) == -1)System.out.println("lettura file comuni fallita");
	
		long intialMemory = Runtime.getRuntime().freeMemory();
		ExecutorService es= Executors.newFixedThreadPool(32);
		long startTime=System.currentTimeMillis();
		final Friendship f = new MockFriendship();
		for(Integer i=0;i<MAXUSERS;i++){
			indiceComune = random.nextInt(listaComuni.size());
			comune = listaComuni.get(indiceComune);
			popolazione = comune.getPopolazione();
			if(popolazione-1 == 0)
				listaComuni.remove(indiceComune);
			
			comune.setPopolazione(popolazione-1);
			angolo = random.nextDouble()*2*Math.PI;
			raggio = random.nextInt(comune.getRaggio());
			accuracy = random.nextInt(MAX_ACCURACY);
			lat = comune.getLat() + (int)( raggio * Math.sin(angolo) );
			lon = comune.getLon() + (int)( raggio * Math.cos(angolo) );
			final User u = new User(i.toString());
			u.addPosition(lat, lon, accuracy, 0, deviceId);
			final Position p = new Position(lat, lon, accuracy, 0);
			final int j=i;
			es.execute(new Runnable() {
				public void run() {
					users.put(j, u);
					HashMap<String,Position> friends = new HashMap<String,Position>();
					HashSet<POI> pois = new HashSet<POI>();
					System.out.println("addUser:"+p.getLat()+" "+p.getLon()+" "+p.getAccuracy());
					Cell.getRoot().addUser(u, friends, pois, p, f);
					friendCounter.addAndGet(friends.size());
					int i=ai.incrementAndGet();
					if(i%100000==0)System.out.println(">> "+i);
				}
			});
			if(i%100000==0)System.out.println("<< "+i);
		}
		es.shutdown();
		try {
			es.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		long endTime=System.currentTimeMillis();
		System.gc();
		Cell.getRoot().explore(1,false);
		int totalFriends = friendCounter.intValue();
		System.out.println("Population:"+Cell.getRoot().explore(1,false));
		System.out.println("Total Nodes:"+Cell.getRoot().nodeCount());
		System.out.println("Pois:"+Cell.getRoot().explorePois(1,false));
		System.out.println("Average friends:"+(totalFriends/MAXUSERS));
		System.out.println("Max level:"+Cell.getRoot().maxLevel(1, 1));
		System.out.println("Time: "+(endTime-startTime)/1000f);
		long finalMemory = Runtime.getRuntime().freeMemory();
		System.out.println("Mem:"+(intialMemory-finalMemory));
		
	}
	
	private static int loadPoi() {
		
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(FILE_POI));
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
			return -1;
		}
		
		String line;
		try {
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split("[ ]");
				if(tokens.length != 3) throw new IOException();

				int id = Integer.valueOf(tokens[0]);
			    int lat = Integer.valueOf(tokens[1]);
			    int lon = Integer.valueOf(tokens[2]);
			    POI poi = new POI(lat, lon, id, 0);
			    Cell.getRoot().preloadPOI(poi);
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return -1;
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}finally{
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
				return -1;
			}
		}
		return 0;
	}

	
	public static int readComuni(ArrayList<Comune> listaComuni){
		File file = new File(FILE_COMUNI);
		Scanner input;
		try {
			input = new Scanner(file).useDelimiter(",");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return -1;
		}

		while(input.hasNextLine()) {
			input.nextInt();input.nextInt();input.nextInt();
		    double latGradi = input.nextDouble();
		    double latMinuti = input.nextDouble();
		    double latSecondi = input.nextDouble();

		    double lat = ( latGradi + (latMinuti/60) + (latSecondi/3600) ) * Constants.COORDINATE_PRECISION;

		    double lonGradi = input.nextDouble();
		    double lonMinuti = input.nextDouble();
		    double lonSecondi = input.nextDouble();
		    double lon = (lonGradi + (lonMinuti/60) + (lonSecondi/3600) )* Constants.COORDINATE_PRECISION;;
		    
		    input.nextInt();input.nextInt();input.nextInt();
		    
		    int superficie = input.nextInt();
		    int popolazione = input.nextInt();
		    int raggio = (int)Math.sqrt((superficie/Math.PI))*100; // superficie in hm2
		    if(popolazione != 0 && superficie!=0 ){
		    	Comune comune = new Comune((int)lat, (int)lon, raggio, popolazione);
		    	listaComuni.add(comune);
		    }
		    input.nextLine();
		}
		input.close();
		return 0;
		
	}
	
	public static int makePOI(){
		File fileComuni = new File(FILE_COMUNI);
		FileWriter filePoi;
		Scanner input;
		int poiCounter = 1;
		Random random = new Random();
		
		try {
			filePoi = new FileWriter(FILE_POI);
			input = new Scanner(fileComuni).useDelimiter(",");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return -1;
		} catch (IOException e1) {
			e1.printStackTrace();
			return -1;
		}
		PrintWriter out = new PrintWriter(filePoi);

		while(input.hasNextLine()) {
			input.nextInt();input.nextInt();input.nextInt();
		    double latGradi = input.nextDouble();
		    double latMinuti = input.nextDouble();
		    double latSecondi = input.nextDouble();

		    double lat = ( latGradi + (latMinuti/60) + (latSecondi/3600) ) * Constants.COORDINATE_PRECISION;

		    double lonGradi = input.nextDouble();
		    double lonMinuti = input.nextDouble();
		    double lonSecondi = input.nextDouble();
		    double lon = (lonGradi + (lonMinuti/60) + (lonSecondi/3600) )* Constants.COORDINATE_PRECISION;;
		    
		    input.nextInt();input.nextInt();input.nextInt();
		    
		    int superficie = input.nextInt();
		    int popolazione = input.nextInt();
		    
		    int maxRaggio = (int)Math.sqrt((superficie/Math.PI))*1000;
		    int numPoi = popolazione/POI_FACTOR;
		    if(numPoi > 0 && maxRaggio > 0){
		    	for(int i = 0; i < numPoi; i++){
		    		double angolo = random.nextDouble()*2*Math.PI;
		    		int raggio = random.nextInt(maxRaggio);
		    		int poiLat = (int)lat + (int)( raggio * Math.sin(angolo) );
		    		int poiLon = (int)lon + (int)( raggio * Math.cos(angolo) );
		    		out.println(poiCounter+" "+ poiLat+" "+poiLon);
		    		poiCounter++;
		    	}
		    }

		    input.nextLine();
		}
		input.close();
		out.close();
		return 0;

	}
}
