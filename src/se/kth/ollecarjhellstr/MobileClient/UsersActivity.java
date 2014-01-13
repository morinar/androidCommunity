package se.kth.ollecarjhellstr.MobileClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class UsersActivity extends Activity {

	private Button updateButton;
	private GetUsersTask gut;
	private String username;
	private ListView userList;
	private ArrayAdapter<String> aas;
	private ArrayList<String> users;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_users);
		Bundle b = getIntent().getExtras();
		username = b.getString("username");
		doToast(username);
		updateButton = (Button)findViewById(R.id.updateButton);
		updateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				gut = new GetUsersTask();
				gut.execute(username);
			}
		});
		

		userList = (ListView)findViewById(R.id.userList);
		
		users = new ArrayList<String>();
		
		aas = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, users);

		userList.setAdapter(aas);
		
		userList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				sendToChatActivity((String)arg0.getItemAtPosition(arg2));
			}
		});
	}
	
	@Override
	protected void onStop(){
		super.onStop();
		if(gut != null){
			gut.cancel(true);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.users, menu);
		return true;
	}
	
    private void sendToChatActivity(String reciever){
    	Intent intent = new Intent(this, ChatActivity.class);
    	intent.putExtra("username", username);
    	intent.putExtra("reciever", reciever);
    	startActivity(intent);
    }
	
    //Gör en "Toast"
    private void doToast(String tmp){
    	Toast toast = Toast.makeText(this, tmp, Toast.LENGTH_SHORT);
    	toast.show();
    }
	
    private class GetUsersTask extends AsyncTask<String, Void, String>{

    	private ArrayList<String> usernameList;
    	
		@Override
		protected String doInBackground(String... params) {
			
			HttpURLConnection http;
			URL url = null;
			InputStream is = null;
			byte[] json;
			String myUsername = params[0];
			String s = null;
			usernameList = new ArrayList<String>();
			try {
				url = new URL("http://ollejohanbackend.appspot.com/uh/getAllUsers");
				http = (HttpURLConnection)url.openConnection();
				is = http.getInputStream();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();				
				json = new byte[1024];
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
				jsarray = jsob.getJSONArray("users");
				for(int i = 0; i < jsarray.length(); i++){
					if(this.isCancelled()){return null;}
					if(!(myUsername.equals(jsarray.optJSONObject(i).getString("username")))){
						usernameList.add(jsarray.optJSONObject(i).getString("username"));
						Log.i("users",""+jsarray.getString(i));
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
				return "Back-end Error";
			}
			
			if(this.isCancelled()){return null;}
			
			return "Users Loaded";

		}
		
		@Override
		protected void onPostExecute(String result) {
			if("Users Loaded".equals(result)){
				users.clear();
				users.addAll(usernameList);
				aas.notifyDataSetChanged();
			}
			doToast(result);
		}
    	
    }

}
