package it.polito.test;


import it.polito.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;


@SuppressWarnings("serial")
public class Client {

	HttpClient httpclient;
	public static final double LAT_STEP = 0.000015;
	public static final double LON_STEP = 0.000022;

	public static final double TOP_LIMIT = 45.065;
	public static final double LEFT_LIMIT = 7.660;
	public static final double BOTTOM_LIMIT = 45.069;
	public static final double RIGHT_LIMIT = 7.665;

	public static final int NUM_THREADS = 12;
	public static final int STEPS = 5;
	public static final int SLEEP_TIME = 15000;

	public static final double MIN_LAT =  36.79;
	public static final double MAX_LAT =  46.08;
	public static final double MIN_LON =  7.55;
	public static final double MAX_LON =  17.50;
	
	private static final int MAX_ACCURACY = 1;
	private static final int MAXUSERS = 10;
	private static final String SERVLET_URL = "http://localhost:8080/CelleRicorsive2/update";

	public static void main(String[] args) throws IOException, InterruptedException{
		httpTest();
	}

	public static void httpTest(){

		Random random = new Random();
  		final AtomicInteger ai=new AtomicInteger();
		int indiceComune, raggio;
		final Integer deviceId=0;

		final HttpParams params = new BasicHttpParams();
		params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		HttpConnectionParams.setTcpNoDelay(params, true);
		
		Comune comune;
		ArrayList<Comune> listaComuni = new ArrayList<Comune>();
		if(TestComuni.readComuni(listaComuni) == -1){System.out.println("lettura file comuni fallita");return;}
		long startTime=System.currentTimeMillis();
		final int size = listaComuni.size();
		ExecutorService es= Executors.newFixedThreadPool(24);
		for(int i=0;i<MAXUSERS;i++){
			final Double angolo, lat, lon;
			final Integer userId, accuracy;
			indiceComune = random.nextInt(size);
			comune = listaComuni.get(indiceComune);
			
			userId=i;
			angolo = random.nextDouble()*2*Math.PI;
			raggio = random.nextInt(comune.getRaggio());
			accuracy = random.nextInt(MAX_ACCURACY);
			lat = (comune.getLat() + raggio * Math.sin(angolo)) /Constants.COORDINATE_PRECISION;
			lon = (comune.getLon() + raggio * Math.cos(angolo)) /Constants.COORDINATE_PRECISION;
			es.execute(new Runnable() {
				public void run() {
					String agent = "Mozilla/4.0";
			        String type = "application/x-www-form-urlencoded";
			        HttpURLConnection connection = null;
					
			        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(5){{
						add(new BasicNameValuePair("lat", lat.toString()));
						add(new BasicNameValuePair("lon", lon.toString()));
						add(new BasicNameValuePair("deviceid", deviceId.toString()));
						add(new BasicNameValuePair("userid",userId.toString()));
						add(new BasicNameValuePair("accuracy", accuracy.toString()));
					}};
					
					//connect
			        try
			        {
						UrlEncodedFormEntity entity=new UrlEncodedFormEntity(nameValuePairs);
			            URL searchUrl = new URL(SERVLET_URL);
			            connection = (HttpURLConnection)searchUrl.openConnection();
			            connection.setDoOutput(true);
			            connection.setRequestMethod("POST");
			            connection.setRequestProperty( "User-Agent", agent );
			            connection.setRequestProperty( "Content-Type", type );
			            connection.setRequestProperty( "Content-Length", Long.toString(entity.getContentLength()) );
			            connection.setRequestProperty( "Connection", "Close" );
			            OutputStream os = connection.getOutputStream();
			            entity.writeTo(os);
			            os.flush();
			            os.close();
			            //check if theres an http error
			            System.out.println("lat: "+lat+", lon: "+lon+ " - response: "+getResponse(connection));
			            connection.disconnect();
//			            Thread.sleep(10);
			        } catch (Exception e) {
			        	System.err.println(e.toString());
			        	e.printStackTrace();
			        }

					int i=ai.incrementAndGet();
//					if(i%10==0)System.out.println(">> "+i);
				}
			});
//			if(i%10==0)System.out.println("<< "+i);
		}
		es.shutdown();
		try {
			es.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		long endTime=System.currentTimeMillis();
		System.out.println("Time: "+(endTime-startTime));
	}
	
	private static String getResponse(HttpURLConnection huc){
		String responseString = "";
		try {
			BufferedReader response = new BufferedReader(new InputStreamReader(huc.getInputStream(), "UTF-8"));
			String inputLine;

			while ((inputLine = response.readLine()) != null)
				responseString = responseString + inputLine;

			response.close();

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return responseString;
	}
}