package bases;

import android.app.Application;
import android.content.Context;

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
    }

    public static Context getContext() {
        return context;
    }
}
