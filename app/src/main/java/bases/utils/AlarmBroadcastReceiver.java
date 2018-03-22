package bases.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import bases.Constants;
import kr.co.picklecode.crossmedia.FavorActivity;
import kr.co.picklecode.crossmedia.MainActivity;
import kr.co.picklecode.crossmedia.TimerActivity;
import kr.co.picklecode.crossmedia.UISyncManager;
import utils.PreferenceUtil;

public class AlarmBroadcastReceiver extends BroadcastReceiver {
    public static boolean isLaunched = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        isLaunched = true;
        UISyncManager.getInstance().getService().stopMedia();

        final Intent activityIntent1 = new Intent(Constants.ACTIVITY_INTENT_FILTER);
        activityIntent1.putExtra("action", "refresh");
        context.sendBroadcast(activityIntent1);

        PreferenceUtil.setBoolean(Constants.PREFERENCE.IS_ALARM_SET, false);
        Log.e("alarmCall", "Stopping media");
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