package com.macbitsgoa.bitsgridwatch.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.macbitsgoa.bitsgridwatch.MainActivity;
import com.macbitsgoa.bitsgridwatch.NotificationIntentService;
import com.macbitsgoa.bitsgridwatch.R;

import java.util.HashMap;
import java.util.Map;

public class CutoffFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "CutoffFbMsgService";

    private String keyID="";
    private String dateTime="";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Check if message contains a data payload. && remoteMessage.getNotification().getBody().equalsIgnoreCase("cutoff")
        if (remoteMessage.getData().size() > 0) {

            Map<String, String> infoMap = remoteMessage.getData();
            keyID=infoMap.get("key");
            dateTime=infoMap.get("date");
            Log.e(TAG, "Message data title: " + infoMap.get("title"));
            Log.e(TAG, "Message data key: " + infoMap.get("key"));
            Log.e(TAG, "Message data payload: " + remoteMessage.getData());
            sendNotification();
            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use WorkManager.
                scheduleJob();
            } else {
                // Handle message within 10 seconds
                handleNow();
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.e(TAG, "Message notification Body: " + remoteMessage.getNotification().toString());
            //sendNotification(remoteMessage.getNotification());

        }


        /*if (remoteMessage.getNotification() != null) {
            Log.e(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }*/
    }

    private void handleNow() {
    }

    private void scheduleJob() {
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     *
     */
    private void sendNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String notifTitle = "Power Outage detected";
        String notifMessage = "Please confirm our sensor data by choosing one of the options";
        // Get the layouts to use in the custom notification
        RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.notification_small);
        RemoteViews notificationLayoutExpanded = new RemoteViews(getPackageName(), R.layout.notification_large);

        if(dateTime.length()>20){
            dateTime=dateTime.substring(3,15);
        } else{
            dateTime="";
        }
        notificationLayout.setTextViewText(R.id.notification_title,"Power Outage Detected");
        notificationLayoutExpanded.setTextViewText(R.id.notification_title,
                "Did you just experience a power outage at "+dateTime+" ?");

        Intent yesIntent = new Intent(this, NotificationIntentService.class);
        yesIntent.setAction("yes");
        yesIntent.putExtra("key",keyID);
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.notif_button_yes,PendingIntent
                .getService(this,0,yesIntent,PendingIntent.FLAG_UPDATE_CURRENT));

        Intent noIntent = new Intent(this, NotificationIntentService.class);
        noIntent.setAction("no");
        noIntent.putExtra("key",keyID);
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.notif_button_no,PendingIntent
                .getService(this,1,noIntent,PendingIntent.FLAG_UPDATE_CURRENT));

        Intent maybeIntent = new Intent(this, NotificationIntentService.class);
        maybeIntent.setAction("maybe");
        maybeIntent.putExtra("key",keyID);
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.notif_button_maybe,PendingIntent
                .getService(this,2,maybeIntent,PendingIntent.FLAG_UPDATE_CURRENT));

        //String channelId = message.getChannelId() != null ? message.getChannelId() :

        String channelId = getString(R.string.default_notification_channel_id);
        //Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ujala_logo)
                        .setContentTitle(notifTitle)
                        .setContentText(notifMessage)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setCustomContentView(notificationLayout)
                        .setCustomBigContentView(notificationLayoutExpanded)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle());

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }


    /*private void sendNotification(String messageBody) {

        // Get the layouts to use in the custom notification
        RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.notification_small);
        RemoteViews notificationLayoutExpanded = new RemoteViews(getPackageName(), R.layout.notification_large);

        // Apply the layouts to the notification
        Notification customNotification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ujala_logo)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setCustomBigContentView(notificationLayoutExpanded)
                .build();


    }*/
}
