package bases;

import android.app.Application;
import android.content.Context;

import com.google.android.gms.ads.MobileAds;

import comm.Comm;

public class BaseApp extends Application {

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
    }

    public static Context getContext() {
        return context;
    }
}
