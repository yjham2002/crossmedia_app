package bases;

import android.app.Application;
import android.content.Context;

import com.google.android.gms.ads.MobileAds;

import comm.Comm;

public class BaseApp extends Application {

    private static Context context;

    static{
        Comm.call();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.context = this.getApplicationContext();
        MobileAds.initialize(this, "ca-app-pub-1846833106939117~4067137440");
    }

    public static Context getContext() {
        return context;
    }
}
