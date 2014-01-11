package se.kth.ollecarjhellstr.MobileClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

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
					Log.i("asdasd",""+"dododobo");
					lt = new LoginTask();
					lt.execute(username.getText().toString(),password.getText().toString());
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

    	
    	
		@Override
		protected String doInBackground(String... params) {

			HttpURLConnection http;
			URL url = null;
			InputStream is = null;
			byte[] json;
			String s = null;
			try {
				url = new URL("http://ollejohanbackend.appspot.com/uh/login?username="+username.getText().toString()+"&password="+password.getText().toString());
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
				return "Strange Error";
			} catch (IOException e) {
				e.printStackTrace();
				return "Network Error";
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
