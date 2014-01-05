package se.kth.ollecarjhellstr.MobileClient;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;



public class MainActivity extends Activity {

	
	private EditText username;
	private EditText password;
	private Button loginButton;

    //protected SpiceManager sM = new SpiceManager(JacksonSpringAndroidSpiceService.class);

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		username = (EditText)findViewById(R.id.username);
		password = (EditText)findViewById(R.id.password);
		
		loginButton = (Button)findViewById(R.id.loginButton);
		loginButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!("".equals(username.getText().toString())) && !("".equals(password.getText().toString()))){
					NetTask nt = new NetTask();
					nt.execute(username.getText().toString(),password.getText().toString());
				}
			}
		});
		
		
	}
	
	@Override
	protected void onStart() {
	  super.onStart();
	  //sM.start(this);
	}

	@Override
	protected void onStop() {
	  //sM.shouldStop();
	  super.onStop();
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
    
    private class NetTask extends AsyncTask<String, Void, String>{

    	
    	
		@Override
		protected String doInBackground(String... params) {
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet request = new HttpGet("http://ollejohanbackend.appspot.com/uh/login?username="+username.getText().toString()+"&password="+password.getText().toString());
			String result = "";
			ResponseHandler<String> handler = new BasicResponseHandler();
			
			try {
				result = httpclient.execute(request,handler);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Log.i("asdasd",""+result);
			httpclient.getConnectionManager().shutdown();
			return result;
		}
		
		@Override
		protected void onPostExecute(String result) {
			if("true".equals(result)){
				doToast("Logged IN!");
			}
		}
    	
    }
    
    
}
