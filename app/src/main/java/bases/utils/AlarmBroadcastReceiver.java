package bases.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmBroadcastReceiver extends BroadcastReceiver {
    public static boolean isLaunched = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        isLaunched = true;
        broadcastTime(context, intent);
    }

    private void broadcastTime(Context context, Intent raw) {
        long currentTime = System.currentTimeMillis();
        Intent intent = new Intent(IntentConstants.INTENT_FILTER);
        Log.e("pIntents", raw.getExtras().toString());
        intent.putExtra(IntentConstants.CURRENT_TIME_KEY, currentTime);
        context.sendBroadcast(intent);
    }

    public interface IntentConstants {
        String INTENT_FILTER = "picklecode_intent_filter_alarm_crossmedia";
        String CURRENT_TIME_KEY = "picklecode_current_time_key_crossmedia";
    }
}