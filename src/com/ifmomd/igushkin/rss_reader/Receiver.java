package com.ifmomd.igushkin.rss_reader;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

/**
 * Created by Sergey on 10/24/13.
 */
public class Receiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        AlarmManager m = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, FeedFetchingService.class);
        PendingIntent pi = PendingIntent.getService(context,0,i,Intent.FILL_IN_DATA);
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 6);
        m.setInexactRepeating(AlarmManager.RTC, c.getTimeInMillis(),AlarmManager.INTERVAL_DAY,pi);
    }
}
