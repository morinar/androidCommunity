package se.kth.ollecarjhellstr.MobileClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class ChatActivity extends Activity {

	private String username;
	private String reciever;
	
	private SendMessageTask smt;
	private GetMessagesTask getMyMessages;
	private GetMessagesTask getRecieverMessages;
	
	private Button sendButton;
	
	private static ChatActivity instance;
	
	private EditText messageText;
	
	private ListView myMessages;
	private ListView recieverMessages;
	
	private ArrayList<String> myList;
	private ArrayList<String> recieverList;
	
	private ArrayAdapter<String> myAdapter;
	private ArrayAdapter<String> recieverAdapter;
	
	private LocationManager lm;
	private LocListener GPSListener;
	private LocListener NETListener;
	
	private CheckBox useGPS;

	public static ChatActivity getInstance() {
		if(instance != null){
			if(instance.hasWindowFocus()){
				return instance;
			}
		}
		return null;
     }
	
	public void updateMessages(){
		getMyMessages = new GetMessagesTask();
		getMyMessages.execute(username, reciever);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_chat);
		
		lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		GPSListener = new LocListener();
		NETListener = new LocListener();
		
		useGPS = (CheckBox)findViewById(R.id.checkBox1);
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, GPSListener);
		lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, NETListener);
		
		Bundle b = getIntent().getExtras();
		username = b.getString("username");
		reciever = b.getString("reciever");
		
		myList = new ArrayList<String>();
		recieverList = new ArrayList<String>();
		
		messageText = (EditText)findViewById(R.id.textWriteId);
		
		sendButton = (Button)findViewById(R.id.sendButton);
		sendButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {		
				int playenabled = GooglePlayServicesUtil.isGooglePlayServicesAvailable(instance);
				if(playenabled == ConnectionResult.SUCCESS){
					//Play services are enabled on this device, we may try to use location services now.
					//Log.d("PLAY SERVICES", "We in.");
					
					if(lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
						//NETWORK locations are enabled, use them
						Log.d("GPS SERVICES", "We in NETWORK.");
						Location latestLoc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						if(latestLoc != null){
							String loc = "Lat: " + latestLoc.getLatitude() + " Lon: " + latestLoc.getLongitude();
							doToast("NETWORK: " + loc);
						}
						else{
							doToast("NETWORK: " + "No location data availible yet.");
						}
					}//End of Network provider
					else if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
					    //GPS is enabled, use it
						Log.d("GPS SERVICES", "We in GPS.");
						Location latestLoc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
						if(latestLoc != null){
							String loc = "Lat: " + latestLoc.getLatitude() + " Lon: " + latestLoc.getLongitude();
							doToast("GPS: " + loc);
						}
						else{
							doToast("GPS: " + "No location data availible yet.");
						}
					}//End of GPS provider
					else{
				        //GPS or NETWORK is not enabled, ask user if they want to enable it here
				    	Log.d("GPS SERVICES", "We out.");
				    	Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);  
				    	startActivity(gpsOptionsIntent);
				    }
				}//End of playenabled
				else{
					Log.d("PLAY SERVICES", "Error: " + playenabled);
				}
				
				
				/*if(!("".equals(messageText.getText().toString()))){
					smt = new SendMessageTask();
					smt.execute(username,reciever,messageText.getText().toString());
					getMyMessages = new GetMessagesTask();
					getMyMessages.execute(username, reciever);
				}*/
				
			}
		});
		
		myMessages = (ListView)findViewById(R.id.recieverList);
		recieverMessages = (ListView)findViewById(R.id.sendList);
		
		myAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, myList);
		recieverAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, recieverList);

		myMessages.setAdapter(myAdapter);
		recieverMessages.setAdapter(recieverAdapter);
		
		getMyMessages = new GetMessagesTask();
		getMyMessages.execute(username, reciever);
		getRecieverMessages = new GetMessagesTask();
		getRecieverMessages.execute(reciever, username);
		
		this.instance = this;
	}

	@Override
	protected void onStop(){
		super.onStop();
		if(getMyMessages != null){
			getMyMessages.cancel(true);
		}
		if(getRecieverMessages != null){
			getRecieverMessages.cancel(true);
		}
		if(smt != null){
			smt.cancel(true);
		}
		if(lm != null){
			lm.removeUpdates(GPSListener);
			lm.removeUpdates(NETListener);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.chat, menu);
		return true;
	}
	
    //Gör en "Toast"
    private void doToast(String tmp){
    	Toast toast = Toast.makeText(this, tmp, Toast.LENGTH_SHORT);
    	toast.show();
    }
    
    private class SendMessageTask extends AsyncTask<String, Void, String>{

    	@Override
		protected String doInBackground(String... params) {
			
			HttpURLConnection http;
			URL url = null;
			InputStream is = null;
			byte[] json;
			
			String myUsername = params[0];
			String recieverUsername = params[1];
			String message = params[2];
			
			String s = null;
			try {
				url = new URL("http://ollejohanbackend.appspot.com/mh/sendPrivateMessage?fromUser="+ myUsername +"&toUser="+ recieverUsername +"&message="+ URLEncoder.encode(message,"UTF-8") +"");
				
				http = (HttpURLConnection)url.openConnection();
				is = http.getInputStream();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();				
				json = new byte[8];
				int read = 0;
				if(this.isCancelled()){return null;}
				while ((read = is.read(json, 0, json.length)) != -1) {
					baos.write(json, 0, read);
					if(this.isCancelled()){return null;}
				}		
				baos.flush();	
				s = new String(baos.toByteArray());
				Log.i("asd",""+s);
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return "Strange error";
			} catch (IOException e) {
				e.printStackTrace();
				return "Network Error";
			} finally{
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
					return "Strange Error";
				}
			}
			if(this.isCancelled()){return null;}
			
			return s;

		}
		
		@Override
		protected void onPostExecute(String result) {
			if("true".equals(result)){
				doToast("Message Sent");
			} else if("false".equals(result)){
				doToast("Message not sent");
			} else{
				doToast(result);
			}
		}
    	
    }
    
    private class GetMessagesTask extends AsyncTask<String, Void, String>{

    	private ArrayList<String> messageList;
    	private String messageReciever;
    	private String messageSender;
    	
    	@Override
		protected String doInBackground(String... params) {
			
    		messageList = new ArrayList<String>();
    		
			HttpURLConnection http;
			URL url = null;
			InputStream is = null;
			byte[] json;
			
			messageReciever = params[0];
			messageSender = params[1];
			
			String s = null;
			new ArrayList<String>();
			try {
				url = new URL("http://ollejohanbackend.appspot.com/mh/getPMsForUserFromUser?username="+messageReciever+"&fromUser="+messageSender);
				http = (HttpURLConnection)url.openConnection();
				is = http.getInputStream();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();				
				json = new byte[256];
				int read = 0;
				if(this.isCancelled()){return null;}
				while ((read = is.read(json, 0, json.length)) != -1) {
					baos.write(json, 0, read);
					if(this.isCancelled()){return null;}
				}		
				baos.flush();	
				s = new String(baos.toByteArray());
				Log.i("asd",""+s);
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return "Strange error";
			} catch (IOException e) {
				e.printStackTrace();
				return "Network Error";
			} finally{
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
					return "Strange Error";
				}
			}
			if(this.isCancelled()){return null;}
			
			JSONObject jsob = null;
			JSONArray jsarray = null;
			try {
				jsob = new JSONObject(s);
				jsarray = jsob.getJSONArray("messages");
				for(int i = 0; i < jsarray.length(); i++){
					if(this.isCancelled()){return null;}
					
					messageList.add(jsarray.optJSONObject(i).getString("message"));
					Log.i("messages",""+jsarray.optJSONObject(i).getString("message"));
				}
			} catch (JSONException e) {
				e.printStackTrace();
				return "Back-end Error";
			}
			return "Downloaded Messages";

		}
		
		@Override
		protected void onPostExecute(String result) {
			if(username.equals(messageReciever)){
				myList.clear();
				myList.addAll(messageList);
				myAdapter.notifyDataSetChanged();
				//doToast("myUser");
			} else {
				recieverList.clear();
				recieverList.addAll(messageList);
				recieverAdapter.notifyDataSetChanged();
			}

			doToast(result);
		}
    	
    }
    
	private class LocListener implements LocationListener{
		@Override
		public void onLocationChanged(Location loc){
			loc.getLatitude();
			loc.getLongitude();
			
			//String Text = "My current location is: " + 
			//"Latitud = " + loc.getLatitude() +
			//"Longitud = " + loc.getLongitude();
			//Toast.makeText(getApplicationContext(), Text, Toast.LENGTH_SHORT).show();
			//Log.d("GPS SERVICE", "Lat: " + loc.getLatitude() + " Lon: " + loc.getLongitude());
		}

		@Override
		public void onProviderDisabled(String provider){
			Toast.makeText( getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT ).show();
		}

		@Override
		public void onProviderEnabled(String provider){
			Toast.makeText(getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras){

		}
	}

}
