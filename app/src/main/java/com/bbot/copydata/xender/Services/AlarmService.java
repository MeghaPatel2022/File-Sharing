package com.bbot.copydata.xender.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;

import com.bbot.copydata.xender.Activity.MainActivity;
import com.bbot.copydata.xender.R;

import org.jetbrains.annotations.NotNull;



public class AlarmService extends JobIntentService {

    public static final String ANDROID_CHANNEL_ID = "com.bbot.copydata.xender";
    /**
     * Unique job ID for this service.
     */
    static final int JOB_ID = 1000;
    int Id;
    /**
     * Convenience method for enqueuing work in to this service.
     */
    NotificationCompat.Builder notificationBuilder;

    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, AlarmService.class, JOB_ID, work);
    }


    @Override
    protected void onHandleWork(@NonNull @NotNull Intent intent) {
        sendNotification();
    }

    private void sendNotification() {
        Intent editIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent mClick = PendingIntent.getActivity(getApplicationContext(), Id, editIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), ANDROID_CHANNEL_ID)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.icon))
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(getApplicationContext().getResources().getString(R.string.app_name))
                .setContentText("Sharing file from one to another device fast")
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(mClick)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true);
        notificationBuilder.setPriority(Notification.PRIORITY_HIGH);

        NotificationManager nManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(ANDROID_CHANNEL_ID, getApplicationContext().getResources().getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);

            // Configure the notification channel.
            notificationChannel.setDescription("Sharing file from one to another device fast");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                notificationChannel.setAllowBubbles(true);
            }
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationBuilder.setGroup(ANDROID_CHANNEL_ID);
            nManager.createNotificationChannel(notificationChannel);
        }
        Notification note = notificationBuilder.build();
        note.defaults |= Notification.DEFAULT_SOUND;
        note.defaults |= Notification.DEFAULT_VIBRATE;
        nManager.notify(Id, note);

    }

}
