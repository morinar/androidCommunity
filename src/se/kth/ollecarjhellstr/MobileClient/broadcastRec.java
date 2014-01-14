package se.kth.ollecarjhellstr.MobileClient;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.util.Log;

/**
 * 
 * BroadcastRec, a broadcastreceiver that receives messages from google.
 * 
 * @author Olle Carlquist 
 * @author Johan Hellström
 *
 */

@SuppressLint("NewApi")
public class BroadcastRec extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		ChatActivity chat = ChatActivity.getInstance(true);
		
		if(chat != null){
			chat.updateMessages();
		} else{			
			chat = ChatActivity.getInstance(false);

			PendingIntent contentIntent = null;
			
			if(chat != null){
				if(chat.reciever.equals(intent.getExtras().getString("username"))){
					contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, ChatActivity.class).putExtra("username", chat.username).putExtra("reciever", chat.reciever), 0);
				}
				else{
					contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);
				}
			} else{
				contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);
			}
			
		    Notification.Builder mBuilder = new Notification.Builder(context).setSmallIcon(R.drawable.ic_launcher).setContentTitle("New Message!").setContentText(intent.getExtras().getString("username")+" has sent you a message!");
		    
		    mBuilder.setContentIntent(contentIntent);
		    
		    mBuilder.setAutoCancel(true);
		    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		    mNotificationManager.notify(1, mBuilder.build());
		}
		
	    Vibrator vib = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
	    vib.vibrate(1500);
	    
		Log.i("asdsda",""+intent.getExtras().getString("username"));
	
		
	}

}
