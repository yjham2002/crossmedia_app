package bases.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import bases.Constants;
import kr.co.picklecode.crossmedia.UISyncManager;

/**
 * Created by HP on 2018-04-06.
 */

public class NetworkStateReceiver extends BroadcastReceiver {

    private boolean isAllowedToBeLaunched(){
        return
                UISyncManager.getInstance().getService() != null
                        && UISyncManager.getInstance().getService().getNowPlayingMusic() != null
                        && UISyncManager.getInstance().getService().getNowPlayingMusic().getParent() != null;
    }

    public void onReceive(Context context, Intent intent) {
        if (intent.getExtras() != null) {
            NetworkInfo ni = (NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {
                Log.i("app", "Network " + ni.getTypeName() + " connected");
                if(isAllowedToBeLaunched()) {
                    UISyncManager.getInstance().getService().resume();
                    Intent connected = new Intent(Constants.ACTIVITY_INTENT_FILTER);
                    connected.putExtra("action", "connected");
                    context.sendBroadcast(connected);
                }
            } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
                Log.d("app", "There's no network connectivity");
                if(isAllowedToBeLaunched()) {
                    UISyncManager.getInstance().getService().stopMedia();
                }
            }
        }
    }
}
