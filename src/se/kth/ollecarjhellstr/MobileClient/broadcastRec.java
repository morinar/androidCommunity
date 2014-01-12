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

@SuppressLint("NewApi")
public class broadcastRec extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent arg1) {
		ChatActivity chat = ChatActivity.getInstance();
		
		if(chat != null){
			chat.updateMessages();
		} else{
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
		            new Intent(context, MainActivity.class), 0);
	
		    Notification.Builder mBuilder =
		            new Notification.Builder(context)
		            .setSmallIcon(R.drawable.ic_launcher)
		            .setContentTitle("New Message!")
		            .setContentText(arg1.getExtras().getString("username")+" has sent you a message!");
		    mBuilder.setContentIntent(contentIntent);
		    mBuilder.setAutoCancel(true);
		    NotificationManager mNotificationManager =
		        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		    mNotificationManager.notify(1, mBuilder.build());
		}
		
	    Vibrator vib = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
	    vib.vibrate(1500);
	    
		Log.i("asdsda",""+arg1.getExtras().getString("username"));
	
		
	}

}
