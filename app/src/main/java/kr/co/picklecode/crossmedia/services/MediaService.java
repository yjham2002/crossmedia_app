package kr.co.picklecode.crossmedia.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RemoteViews;

import java.io.IOException;
import java.util.Date;

import bases.Constants;
import bases.utils.AlarmBroadcastReceiver;
import kr.co.picklecode.crossmedia.MainActivity;
import kr.co.picklecode.crossmedia.R;
import kr.co.picklecode.crossmedia.models.Article;

import static android.app.NotificationManager.IMPORTANCE_HIGH;

/**
 * Created by HP on 2018-03-16.
 */
public class MediaService extends Service implements View.OnClickListener{

    private boolean isPlaying = false;

    private boolean isInitialRunning = true;
    private Article nowPlaying;
    private View mView;
    private WebView webView;
    private WindowManager mManager;
    private IBinder mBinder = new LocalBinder();

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };

    public interface VideoCallBack{
        void onCall();
    }

    public boolean isInitialRunning() {
        return isInitialRunning;
    }

    public void setInitialRunning(boolean initialRunning) {
        isInitialRunning = initialRunning;
    }

    @Override
    public IBinder onBind(Intent intent) {
        registerReceiver(broadcastReceiver, new IntentFilter(AlarmBroadcastReceiver.IntentConstants.INTENT_FILTER));
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public MediaService getServiceInstance() {
            return MediaService.this;
        }
    }

    public Article getNowPlaying(){
        return nowPlaying;
    }

    public void startVideo(Article article, final VideoCallBack videoCallBack) throws IllegalArgumentException{
        Log.e("MediaService", "startVideo Invoked.");
        if(article == null) {
            stopMedia();
            throw new IllegalArgumentException();
        }

        if(article.getImgPath().indexOf("sayclub") != -1){
            webView.loadUrl("http://zacchaeus151.cafe24.com/saycast.php?vd_internet_radio_url=http://saycast.sayclub.com/station/home/index/sc101");
            return;
        }

        this.nowPlaying = article;
        final String url = article.getRepPath();
        String filtered = null;
        try {
            final int paramPos = url.indexOf("?v=") + 3;
            int longParamRevPos = url.indexOf("&", paramPos);
            if(longParamRevPos == -1) longParamRevPos = url.length();
            filtered = url.substring(paramPos, longParamRevPos);
        }catch (Exception e){
            isPlaying = false;
            e.printStackTrace();
            stopMedia();
            throw new IllegalArgumentException();
        }

        webView.loadUrl(Constants.BASE_YOUTUBE_URL + filtered);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isPlaying = true;
                if(videoCallBack != null){
                    videoCallBack.onCall();
                }
            }
        }, 1200);

//        webView.loadData(Constants.getYoutubeSrc(filtered), "text/html", "UTF-8");
//
    }

    @Override
    public void onCreate() {
        super.onCreate();

        LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = mInflater.inflate(R.layout.always_on_display_layout, null);

        int versionDependedType = WindowManager.LayoutParams.TYPE_PHONE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            versionDependedType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }

        WindowManager.LayoutParams mParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                versionDependedType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
//                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        mManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        webView = mView.findViewById(R.id.mediaWebView);

        webView.setOnClickListener(this);

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("pickle://")) {
                    Log.e("webView", url);
                    return true;
                }else {
                    if(url.startsWith("javascript")){
                        Log.e("webView", url);
                        return false;
                    }
                }
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if(url.indexOf(Constants.BASE_YOUTUBE_URL) != -1){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            webView.loadUrl("javascript:player.playVideo();");
                        }
                    }, 1000);
                }
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                isPlaying = false;
                stopMedia();
                super.onReceivedError(view, request, error);
            }
        });

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setVerticalScrollBarEnabled(false);
        webView.setLayerType(WebView.LAYER_TYPE_HARDWARE, null);
        webView.setBackgroundColor(0);
        webView.loadUrl("http://zacchaeus151.cafe24.com/youtube.php?vid=GjCEH-KZq2c");

        Log.e("MediaService", "onCreate : webView initialized.");

        showPlayerNotification();

        mManager.addView(mView, mParams);
    }

    protected void showPlayerNotification(){
        Notification.Builder mBuilder = createNotification();

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.player_notification_layout);
        remoteViews.setImageViewResource(R.id.noti_img, R.drawable.icon_hour_glass);
        remoteViews.setTextViewText(R.id.noti_title, "Title");
        remoteViews.setTextViewText(R.id.noti_sub, "message");

        mBuilder.setContent(remoteViews);
        mBuilder.setContentIntent(createPendingIntent());
        mBuilder.setOngoing(true);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        mNotificationManager.notify(20180312, mBuilder.build());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(Constants.NOTIFICATION_CHANNEL_ID, Constants.NOTIFICATION_CHANNEL_NAME, IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationChannel.setSound(null, null);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }

        startForeground(20180312, mBuilder.build());
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

    private Notification.Builder createNotification(){
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        Notification.Builder builder;
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            builder = new Notification.Builder(this, Constants.NOTIFICATION_CHANNEL_ID);
        }else{
            builder = new Notification.Builder(this);
        }

        builder
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(icon)
                .setContentTitle("StatusBar Title")
                .setContentText("StatusBar subTitle")
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_ALL);
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
//            builder.setCategory(Notification.CATEGORY_MESSAGE)
//                    .setPriority(Notification.PRIORITY_HIGH)
//                    .setVisibility(Notification.VISIBILITY_PUBLIC);
//        }
        return builder;
    }

    public boolean isPlaying(){
        return isPlaying;
    }

    public void stopMedia(){
        webView.loadUrl("javascript:player.pauseVideo();");
        Log.e("MediaService", "stopMedia Invoked.");
        isPlaying = false;
    }

    public void stopAll(){
        if(mView != null){
            mManager.removeView(mView);
        }
        stopMedia();
        this.stopSelf();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        if (!mediaPlayer.isPlaying()) {
//            mediaPlayer.start();
//        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        isPlaying = false;
    }

}
