package se.kth.ollecarjhellstr.MobileClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

/**
 * 
 * MainActivity, only a login-screen
 * 
 * @author Olle Carlquist 
 * @author Johan Hellström
 *
 */

public class MainActivity extends Activity {

	
	private EditText username;
	private EditText password;
	private Button loginButton;
	private LoginTask lt;
	private ListView userList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		username = (EditText)findViewById(R.id.username);
		password = (EditText)findViewById(R.id.password);
		
		userList = (ListView)findViewById(R.id.userList);
				
		loginButton = (Button)findViewById(R.id.loginButton);
		loginButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!("".equals(username.getText().toString())) && !("".equals(password.getText().toString()))){
					ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
					if(cm.getActiveNetworkInfo() != null){
						Log.i("asdasd",""+"dododobo");
						lt = new LoginTask();
						lt.execute(username.getText().toString(),password.getText().toString());
					} else{
						doToast("No Internet Connection");
					}
				}
			}
		});

		
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if(lt != null){
			lt.cancel(true);
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
    //Gör en "Toast"
    private void doToast(String tmp){
    	Toast toast = Toast.makeText(this, tmp, Toast.LENGTH_SHORT);
    	toast.show();
    }
    
    private void sendToUsersActivity(){
    	Intent intent = new Intent(this, UsersActivity.class);
    	intent.putExtra("username", username.getText().toString());
    	startActivity(intent);
    }
    
    private class LoginTask extends AsyncTask<String, Void, String>{

    	/**
    	 * LoginTask, needs username and password to execute properly.
    	 */
    	
		@Override
		protected String doInBackground(String... params) {
			
			HttpURLConnection http;
			URL url = null;
			InputStream is = null;
			byte[] json;
			String loginName = params[0];
			String pass = params[1];
			String s = null;
			ByteArrayOutputStream baos = null;
			try {
				url = new URL("http://ollejohanbackend.appspot.com/uh/login?username="+loginName+"&password="+pass);
				http = (HttpURLConnection)url.openConnection();
				is = http.getInputStream();
				baos = new ByteArrayOutputStream();				
				json = new byte[8];
				int read = 0;
				if(this.isCancelled()){return null;}
				while ((read = is.read(json, 0, json.length)) != -1) {
					baos.write(json, 0, read);
					if(this.isCancelled()){return null;}
				}		
				baos.flush();	
				s = new String(baos.toByteArray());
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return "Strange Error";
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
			
			if("true".equals(s)){
				GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
				String regId = null;
				try {
					regId = gcm.register("27156684778");
					url = new URL("http://ollejohanbackend.appspot.com/uh/registerTelephone?username="+username.getText().toString() + "&id="+regId);
					http = (HttpURLConnection)url.openConnection();
					http.getResponseMessage();
				} catch (IOException e) {
					e.printStackTrace();
					return "Could not register cloudz";
				}
				Log.i("REG-ID",""+regId);
			}
			return s;

		}
		
		@Override
		protected void onPostExecute(String result) {
			if("true".equals(result)){
				doToast("Logged IN!");
				sendToUsersActivity();
			} else if("false".equals(result)){
				doToast("Wrong username or password");
			} else {
				doToast(result);
			}
		}
    	
    }
    
    
}
