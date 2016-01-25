package it.polito.test;

import it.polito.Cell;
import it.polito.Constants;
import it.polito.POI;
import it.polito.Position;
import it.polito.User;
import it.polito.friendship.Friendship;
import it.polito.friendship.impl.MockFriendship;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TestComuni {
	public static final int MAXUSERS = 20;
	public static final int MAX_ACCURACY =1;
	public static final String FILE_COMUNI = "resources/Comuni.txt";
	
	public static void run(){
		
		Random random = new Random();
  		final AtomicInteger ai=new AtomicInteger();
		final AtomicInteger friendCounter = new AtomicInteger();
		friendCounter.set(0);
		int indiceComune, raggio, lat, lon, accuracy;
		final int deviceid=0;
		double angolo;
		Comune comune;
		ArrayList<Comune> listaComuni = new ArrayList<Comune>();
		if(readComuni(listaComuni) == -1){System.out.println("lettura file comuni fallita"); return;}
		final int comuniSize = listaComuni.size();
		long intialMemory = Runtime.getRuntime().freeMemory();
		ExecutorService es= Executors.newFixedThreadPool(32);
		long startTime=System.currentTimeMillis();
		final Friendship f = new MockFriendship();
		for(Integer i=0;i<MAXUSERS;i++){
			indiceComune = random.nextInt(comuniSize);
			comune = listaComuni.get(indiceComune);

			angolo = random.nextDouble()*2*Math.PI;
			raggio = random.nextInt(comune.getRaggio());
			accuracy = random.nextInt(MAX_ACCURACY);
			lat = comune.getLat() + (int)( raggio * Math.sin(angolo) );
			lon = comune.getLon() + (int)( raggio * Math.cos(angolo) );
			final User u = new User(i.toString());
			u.addPosition(lat, lon, accuracy, 0, deviceid);
			final Position p = new Position(lat, lon, accuracy, 0);
			es.execute(new Runnable() {
				public void run() {
					HashMap<String,Position> friends = new HashMap<String,Position>();
					HashSet<POI> pois = new HashSet<POI>();
					Cell.getRoot().addUser(u, friends,pois,p,f);
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
	public static int readComuni(ArrayList<Comune> listaComuni){
		File file = new File(FILE_COMUNI);
		Scanner input;
		try {
			input = new Scanner(file).useDelimiter(",");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return -1;
		}
		int i=0;
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
		    int raggio = (int)Math.sqrt((superficie/Math.PI))*100;
		    if(popolazione != 0 && superficie!=0 ){
		    	Comune comune = new Comune((int)lat, (int)lon, raggio, popolazione);
		    	for(int j=0;j<popolazione  ;i++,j++){
		    		listaComuni.add(i,comune);
		    	}
		    }
		    input.nextLine();
		}
		input.close();
		return 0;
		
	}
	
}
