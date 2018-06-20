/*
 * [MyADMMessageHandler.java]
 *
 * (c) 2012, Amazon.com, Inc. or its affiliates. All Rights Reserved.
 */

package com.kanoapps.cordova.amazonpush;

import android.content.Intent;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.content.Context;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;

import java.util.Date;

import com.amazon.device.messaging.ADMMessageHandlerBase;
import com.amazon.device.messaging.ADMMessageReceiver;

public class MyADMMessageHandler extends ADMMessageHandlerBase
{
    private final static String TAG = "MyADMMessageHandler";

    public static class Receiver extends ADMMessageReceiver
    {
        public Receiver()
        {
            super(MyADMMessageHandler.class);
        }

    // Nothing else is required here; your broadcast receiver automatically
    // forwards intents to your service for processing.
    }

    /**
     * Class constructor.
     */
    public MyADMMessageHandler()
    {
        super(MyADMMessageHandler.class.getName());
    }

    /**
     * Class constructor, including the className argument.
     *
     * @param className The name of the class.
     */
    public MyADMMessageHandler(final String className)
    {
        super(className);
    }

    @Override
    protected void onRegistered(final String newRegistrationId)
    {
        // You start the registration process by calling startRegister() in your Main
        // Activity. When the registration ID is ready, ADM calls onRegistered() on
        // your app. Transmit the passed-in registration ID to your server, so your
        // server can send messages to this app instance. onRegistered() is also
        // called if your registration ID is rotated or changed for any reason; your
        // app should pass the new registration ID to your server if this occurs.
        // Your server needs to be able to handle a registration ID up to 1536 characters
        // in length.

        // The following is an example of sending the registration ID to your
        // server via a header key/value pair over HTTP.

        /*
        URL url = new URL(YOUR_WEBSERVER_URL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setDoInput(true);
        con.setUseCaches(false);
        con.setRequestMethod("POST");
        con.setRequestProperty("RegistrationId", newRegistrationId);
        con.getResponse();
        */

        Log.v(TAG, "onRegistered " + newRegistrationId);

        AmazonPushPlugin.setRegistrationID(newRegistrationId);
    }

    @Override
    protected void onUnregistered(final String registrationId)
    {
        Log.v(TAG, "onUnregistered " + registrationId);

        // If your app is unregistered on this device, inform your server that
        // this app instance is no longer a valid target for messages.
    }

    @Override
    protected void onRegistrationError(final String errorId)
    {
        Log.v(TAG, "onRegistrationError " + errorId);

        // You should consider a registration error fatal. In response, your app may
        // degrade gracefully, or you may wish to notify the user that this part of
        // your app's functionality is not available.
    }

    @Override
    protected void onMessage(final Intent intent)
    {
        // Extract the message content from the set of extras attached to
        // the com.amazon.device.messaging.intent.RECEIVE intent.

        // Create strings to access the message and timeStamp fields from the JSON data.
        final String msgKey = "message";
        final String timeKey = "timeStamp";

        // Obtain the intent action that will be triggered in onMessage() callback.
        final String intentAction = "com.kanoapps.cordova.amazonpush.ON_MESSAGE";

        // Obtain the extras that were included in the intent.
        final Bundle extras = intent.getExtras();

        // Extract the message and time from the extras in the intent.
        // ADM makes no guarantees about delivery or the order of messages.
        // Due to varying network conditions, messages may be delivered more than once.
        // Your app must be able to handle instances of duplicate messages.
        final String msg = extras.getString(msgKey);
        final String time = extras.getString(timeKey);

        Log.v(TAG, "onMessage " + msg);

        AmazonPushPlugin.setReceivedMessage(msg);

        postNotification(msgKey, timeKey, intentAction, msg, time);
    }

    /**
     * This method posts a notification to notification manager.
     *
     * @param msgKey String to access message field from data JSON.
     * @param timeKey String to access timeStamp field from data JSON.
     * @param intentAction Intent action that will be triggered in onMessage() callback.
     * @param msg Message that is included in the ADM message.
     * @param time Timestamp of the ADM message.
     */
    private void postNotification(final String msgKey, final String timeKey,
                                  final String intentAction, final String msg, final String time)
    {
        final Context context = getApplicationContext();
        final String packageName = context.getPackageName();
        final Resources resources = context.getResources();

        final NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        final Builder notificationBuilder = new Notification.Builder(context);

        PackageManager pm = getPackageManager();
        final Intent notificationIntent = pm.getLaunchIntentForPackage(packageName);

        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.putExtra(msgKey, msg);
        notificationIntent.putExtra(timeKey, time);

        notificationIntent.setAction(intentAction + time);

        final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL);

        final int iconId = resources.getIdentifier("icon", "drawable", packageName);

        CharSequence appName = context.getPackageManager().getApplicationLabel(context.getApplicationInfo());
        final String appNameString = (String)appName;

        final Notification notification = notificationBuilder.setSmallIcon( iconId )
                .setContentTitle(appNameString)
                .setContentText(msg)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .getNotification();

        final int notificationId = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
        mNotificationManager.notify(notificationId, notification);
    }
}
