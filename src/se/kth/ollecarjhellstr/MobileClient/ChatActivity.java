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

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

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

	public static ChatActivity getInstance() {
        return instance;
     }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_chat);
		
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
				
				if(!("".equals(messageText.getText().toString()))){
					smt = new SendMessageTask();
					smt.execute(username,reciever,messageText.getText().toString());
					getMyMessages = new GetMessagesTask();
					getMyMessages.execute(username);
				}
			}
		});
		
		myMessages = (ListView)findViewById(R.id.sendList);
		recieverMessages = (ListView)findViewById(R.id.recieverList);
		
		myAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, myList);
		recieverAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, recieverList);

		myMessages.setAdapter(myAdapter);
		recieverMessages.setAdapter(recieverAdapter);
		
		getMyMessages = new GetMessagesTask();
		getMyMessages.execute(username);
		getRecieverMessages = new GetMessagesTask();
		getRecieverMessages.execute(reciever);
		
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
				url = new URL("http://ollejohanbackend.appspot.com/mh/sendPrivateMessage?fromUser="+ myUsername +"&toUser="+ recieverUsername +"&message="+ message +"");
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
    	
    	@Override
		protected String doInBackground(String... params) {
			
    		messageList = new ArrayList<String>();
    		
			HttpURLConnection http;
			URL url = null;
			InputStream is = null;
			byte[] json;
			
			messageReciever = params[0];
			
			String s = null;
			new ArrayList<String>();
			try {
				url = new URL("http://ollejohanbackend.appspot.com/mh/getPMsForUser?username="+messageReciever);
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

}
