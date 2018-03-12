package bases;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import kr.co.picklecode.crossmedia.MainActivity;
import kr.co.picklecode.crossmedia.R;

/**
 * Base class for activities
 * @author EuiJin.Ham
 * @version 1.0.0
 */
public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener{

    private InterstitialAd mInterstitialAd = null;

    public void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View view){
        Log.e(this.getClass().getSimpleName(), "Override onClick method in BaseActivity to use View.OnClickListener");
    }

    protected void showPlayerNotification(){
        NotificationCompat.Builder mBuilder = createNotification();

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.player_notification_layout);
        remoteViews.setImageViewResource(R.id.noti_img, R.mipmap.ic_launcher);
        remoteViews.setTextViewText(R.id.noti_title, "Title");
        remoteViews.setTextViewText(R.id.noti_sub, "message");

        mBuilder.setContent(remoteViews);
        mBuilder.setContentIntent(createPendingIntent());
        mBuilder.setOngoing(true);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(20180312, mBuilder.build());

    }

    private PendingIntent createPendingIntent(){
        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        return stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    private NotificationCompat.Builder createNotification(){
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(icon)
                .setContentTitle("StatusBar Title")
                .setContentText("StatusBar subTitle")
                .setSmallIcon(R.mipmap.ic_launcher/*스와이프 전 아이콘*/)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_ALL);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            builder.setCategory(Notification.CATEGORY_MESSAGE)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        return builder;
    }

    protected void loadInterstitialAd() {
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-1846833106939117/2370912396");
        mInterstitialAd.setAdListener(new AdListener() {

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                if(mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
            }
        });

        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest);
    }

    /**
     * Bind itself as a OnClickListener on parameters
     * @param views view objects to bind a listener
     */
    protected void setClick(View... views){
        for(View view : views) view.setOnClickListener(this);
    }

}
