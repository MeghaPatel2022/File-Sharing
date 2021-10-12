package com.bbot.copydata.xender.Services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.util.Log;

import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {

    AlarmManager mAlarmManager;
    PendingIntent mPendingIntent;
    Calendar mCalendar;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("LLLL_Onrecicve: ", "OnReceive: ");
        AlarmService.enqueueWork(context, intent);
    }

    public void setRepeatAlarm(Context context, int ID, Calendar calendar) {
        Log.e("LLL_ID: ", String.valueOf(ID));

        mCalendar = calendar;

        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Put Reminder ID in Intent Extra
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("id", ID);
        mPendingIntent = PendingIntent.getBroadcast(context, ID, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Calculate notification timein
        Calendar c = Calendar.getInstance();
        long currentTime = c.getTimeInMillis();
        long diffTime = mCalendar.getTimeInMillis() - currentTime;

        mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + diffTime,
                3600000, mPendingIntent);

        // Restart alarm if device is rebooted
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }
}
