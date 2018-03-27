package bases;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.google.android.gms.ads.MobileAds;

import comm.Comm;
import kr.co.picklecode.crossmedia.UISyncManager;
import kr.co.picklecode.crossmedia.services.MediaService;

public class BaseApp extends Application {

    private boolean mBounded;

    public static final String ADMOB_AD_ID = "ca-app-pub-1846833106939117~4067137440";
    private static Context context;

    static{
        Comm.call();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.context = this.getApplicationContext();
        MobileAds.initialize(this, ADMOB_AD_ID);
        final Intent backgroundIntentCall = new Intent(getBaseContext(), MediaService.class);
        bindService(backgroundIntentCall, mConnection, BIND_AUTO_CREATE);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBounded = false;
            UISyncManager.getInstance().setService(null);
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBounded = true;
            MediaService.LocalBinder mLocalBinder = (MediaService.LocalBinder)service;
            UISyncManager.getInstance().setService(mLocalBinder.getServiceInstance());
            final Intent activityIntent1 = new Intent(Constants.ACTIVITY_INTENT_FILTER);
            activityIntent1.putExtra("action", "refresh");
            sendBroadcast(activityIntent1);
        }
    };

    public static Context getContext() {
        return context;
    }

    @Override
    public void onTerminate(){
        unbindService(mConnection);
        super.onTerminate();
    }

}
