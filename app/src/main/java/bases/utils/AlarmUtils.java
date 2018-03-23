package bases.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.Date;

import bases.Constants;
import utils.PreferenceUtil;

public class AlarmUtils {

    private static AlarmUtils instance;
    private static final int ALARM_UNIQUE_ID = 1029389505;

    public static AlarmUtils getInstance() {
        if (instance == null) instance = new AlarmUtils();
        return instance;
    }

    public void startAlarm(Context context, int delay) {
        cancelAll(context);

        Intent alarmIntent = new Intent(context, AlarmBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ALARM_UNIQUE_ID, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        final long setTime = System.currentTimeMillis() + delay;
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, setTime, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            manager.setExact(AlarmManager.RTC_WAKEUP, setTime, pendingIntent);
        } else {
            manager.set(AlarmManager.RTC_WAKEUP, setTime, pendingIntent);
        }

        Log.e(this.getClass().getSimpleName(), "Alarm Registered at [" + new Date() + "]");
        PreferenceUtil.setLong(Constants.PREFERENCE.ALARM_TIME, setTime);
    }

    public PendingIntent getCurrentSetAlarm(Context context){
        Intent alarmIntent = new Intent(context, AlarmBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ALARM_UNIQUE_ID, alarmIntent, PendingIntent.FLAG_NO_CREATE);
        if(pendingIntent == null){
            return null;
        }else{
            return pendingIntent;
        }
    }

    public void cancelAll(Context context){
        Intent alarmIntent = new Intent(context, AlarmBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ALARM_UNIQUE_ID, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if(pendingIntent != null){
            manager.cancel(pendingIntent);
            pendingIntent.cancel();
            PreferenceUtil.setBoolean(Constants.PREFERENCE.IS_ALARM_SET, false);
        }
    }

}