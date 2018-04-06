package kr.co.picklecode.crossmedia.services;

import android.app.AlarmManager;
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
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
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
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import bases.Constants;
import bases.SimpleCallback;
import bases.imageTransform.RoundedTransform;
import bases.utils.AlarmUtils;
import comm.SimpleCall;
import kr.co.picklecode.crossmedia.MainActivity;
import kr.co.picklecode.crossmedia.R;
import kr.co.picklecode.crossmedia.UISyncManager;
import kr.co.picklecode.crossmedia.models.Article;
import kr.co.picklecode.crossmedia.models.MediaRaw;
import utils.PreferenceUtil;

/**
 * Created by HP on 2018-03-16.
 */
public class MediaService extends Service implements View.OnClickListener{

    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;

    private NotificationManager mNotificationManager;
    private static final int notiId = 20180312;
    private static boolean isInitialRunning = true;
    private MediaRaw nowPlayingMusic;
    private View mView;
    private WebView webView;
    private WindowManager mManager;
    private IBinder mBinder = new LocalBinder();

    private void nextMusic(){
        UISyncManager.getInstance().getService().setRepeatFlag(false);
        final int nextIdx = UISyncManager.getInstance().getNextSongIndex();
        if(nextIdx == -1) {
            startPlay(UISyncManager.getInstance().getService().getNowPlayingMusic(), new VideoCallBack() {
                @Override
                public void onCall() {
                    sendRefreshingBroadcast();
                }
            }, true);
        }else if(nextIdx == -2){
            final int nextChIdx = UISyncManager.getInstance().getNextChannelIndex();
            if(nextChIdx == -1){
                startChannel(UISyncManager.getInstance().getService().getNowPlayingMusic().getParent(), new VideoCallBack() {
                    @Override
                    public void onCall() {
                        sendRefreshingBroadcast();
                    }
                });
            }else{
                startChannel(UISyncManager.getInstance().getChList().get(nextChIdx), new VideoCallBack() {
                    @Override
                    public void onCall() {
                        sendRefreshingBroadcast();
                    }
                });
            }
        }else {
            startPlay(UISyncManager.getInstance().getSongList().get(nextIdx), new VideoCallBack() {
                @Override
                public void onCall() {
                    sendRefreshingBroadcast();
                }
            }, true);
        }
    }

    private BroadcastReceiver notificationListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getExtras().getString("action", "");
            Log.e("notiListener", action);
            switch (action) {
                case Constants.INTENT_NOTIFICATION.ACTION_PLAY:{
                    if(nowPlayingMusic != null) startChannel(nowPlayingMusic.getParent(), new VideoCallBack() {
                        @Override
                        public void onCall() {
                            sendRefreshingBroadcast();
                        }});
                    break;
                }
                case Constants.INTENT_NOTIFICATION.ACTION_STOP:{
                    stopMedia();
                    break;
                }
                case Constants.INTENT_NOTIFICATION.ACTION_CLOSE:{
                    stopAll();
                    mNotificationManager.cancel(notiId);
                    AlarmUtils.getInstance().cancelAll(MediaService.this);
                    PreferenceUtil.setBoolean(Constants.PREFERENCE.IS_ALARM_SET, false);
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(0);
                    break;
                }
            }
        }
    };

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getExtras().getString("action", "");
            final int state = intent.getExtras().getInt("state", -1);
            Log.e("MediaReceiver", action + " : FROM " + context);
            switch (action){
                case "refresh":{
                    showPlayerNotification();
                    break;
                }
                case "state":{
                    if(state == 0){
                        nextMusic();
                    }
                    break;
                }
            }
        }
    };

    public interface VideoCallBack{
        void onCall();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        stopSelf();
        return super.onUnbind(intent);
    }

    public boolean isInitialRunning() {
        if(nowPlayingMusic != null && nowPlayingMusic.getParent() != null) isInitialRunning = false;
        return isInitialRunning;
    }

    public void setInitialRunning(boolean initialRunning) {
        isInitialRunning = initialRunning;
    }

    @Override
    public IBinder onBind(Intent intent) {
        registerReceiver(broadcastReceiver, new IntentFilter(Constants.ACTIVITY_INTENT_FILTER));
        registerReceiver(notificationListener, new IntentFilter(Constants.INTENT_NOTIFICATION.REP_FILTER));
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public MediaService getServiceInstance() {
            return MediaService.this;
        }
    }

    public MediaRaw getNowPlayingMusic(){
        return nowPlayingMusic;
    }

    public void loadChannel(final Article article, final VideoCallBack videoCallBack) throws IllegalArgumentException{
        HashMap<String, Object> params = new HashMap<>();
        params.put("ap_id", 374);
        params.put("cg_id", article.getId());
        params.put("page", -1);
        SimpleCall.getHttpJson("http://zacchaeus151.cafe24.com/api/video_list.php", params, new SimpleCall.CallBack() {
            @Override
            public void handle(JSONObject jsonObject) {
                final List<MediaRaw> mediaRaws = new ArrayList<>();
                try {
                    final JSONObject result = jsonObject.getJSONObject("result");
                    final JSONArray json_arr = result.getJSONArray("list");
                    for(int j = 0; j < json_arr.length(); j++){
                        final JSONObject ch = json_arr.getJSONObject(j);
                        if(ch.getString("vd_id") == null || ch.getString("vd_id").equals("null")) continue;
                        final MediaRaw mediaRaw = new MediaRaw();
                        mediaRaw.setParent(article);
                        mediaRaw.setCg_id(article.getId());
                        mediaRaw.setCh_id(ch.getInt("vd_id"));
                        mediaRaw.setTitle(ch.getString("vd_name"));
                        mediaRaw.setImgPath(ch.getString("vd_thum_url"));
                        mediaRaw.setType(ch.getInt("vd_internet_use"));
                        mediaRaw.setRepPath(ch.getString("vd_url"));
                        mediaRaws.add(mediaRaw);
                    }
                    article.setMediaRaws(mediaRaws);
                    Collections.shuffle(article.getMediaRaws());
                    UISyncManager.getInstance().setSongList(article.getMediaRaws());
                    if(videoCallBack != null) videoCallBack.onCall();

                    MediaService.this.nowPlayingMusic = article.getMediaRaws().get(0);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    public void resume(){
        if(this.nowPlayingMusic != null) {
            startChannel(this.nowPlayingMusic.getParent(), new VideoCallBack() {
                @Override
                public void onCall() {
                    MediaService.this.sendRefreshingBroadcast();
                }
            });
        }
    }

    public void startChannel(final Article article, final VideoCallBack videoCallBack) throws IllegalArgumentException{
        HashMap<String, Object> params = new HashMap<>();
        params.put("ap_id", 374);
        params.put("cg_id", article.getId());
        params.put("page", -1);
        SimpleCall.getHttpJson("http://zacchaeus151.cafe24.com/api/video_list.php", params, new SimpleCall.CallBack() {
            @Override
            public void handle(JSONObject jsonObject) {
                final List<MediaRaw> mediaRaws = new ArrayList<>();
                try {
                    final JSONObject result = jsonObject.getJSONObject("result");
                    final JSONArray json_arr = result.getJSONArray("list");
                    for(int j = 0; j < json_arr.length(); j++){
                        final JSONObject ch = json_arr.getJSONObject(j);
                        if(ch.getString("vd_id") == null || ch.getString("vd_id").equals("null")) continue;
                        final MediaRaw mediaRaw = new MediaRaw();
                        mediaRaw.setParent(article);
                        mediaRaw.setCg_id(article.getId());
                        mediaRaw.setCh_id(ch.getInt("vd_id"));
                        mediaRaw.setTitle(ch.getString("vd_name"));
                        mediaRaw.setImgPath(ch.getString("vd_thum_url"));
                        mediaRaw.setType(ch.getInt("vd_internet_use"));
                        mediaRaw.setRepPath(ch.getString("vd_url"));
                        mediaRaws.add(mediaRaw);
                    }
                    article.setMediaRaws(mediaRaws);
                    Collections.shuffle(article.getMediaRaws());
                    UISyncManager.getInstance().setSongList(article.getMediaRaws());
                    startPlay(article.getMediaRaws().get(0), videoCallBack, false);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    private void startPlay(final MediaRaw mediaRaw, final VideoCallBack videoCallBack, boolean nonStop) throws IllegalArgumentException{
        Log.e("startPlay", mediaRaw.toString());
//        if(!nonStop)
            stopMedia();
        repeatFlag = false;
        Log.e("MediaService", "startVideo Invoked.");
        if(mediaRaw == null) {
            stopMedia();
            throw new IllegalArgumentException("The channel was null.");
        }

        this.nowPlayingMusic = mediaRaw;
        final String url = mediaRaw.getRepPath();

        if(mediaRaw.getType() == 0 || mediaRaw.getType() == 1){ // YouTube
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
                    repeatFlag = true;
                    if(videoCallBack != null){
                        videoCallBack.onCall();
                    }
                    sendRefreshingBroadcast();
                }
            }, 1200);

            checkState();
        }else if(mediaRaw.getType() == 2){ // Saycast
            intervalStateCheckHandler.removeCallbacks(stateCheck); // cancel state check

            SimpleCall.getHttp("http://zacchaeus151.cafe24.com/saycast.php?vd_internet_radio_url=" + url, new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    try {
                        Log.e("saycast", msg.getData().toString());
                        mediaPlayer.setDataSource(msg.getData().getString("jsonString"));
                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                nextMusic();
                            }
                        });
                        mediaPlayer.prepareAsync();
                        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mediaPlayer) {
                                mediaPlayer.start();
                            }
                        });

                        /**
                         * Preparing Process has been changed to asynchronous mode at Mar, 24th, 2018 14:03
                         */

                        isPlaying = true;

                        if(videoCallBack != null){
                            videoCallBack.onCall();
                        }
                        sendRefreshingBroadcast();

                    }catch (IOException e){
                        e.printStackTrace();
                        stopMedia();
//                        throw new IllegalArgumentException("Cannot get server address of saycast");
                    }catch (IllegalStateException ie){
                        ie.printStackTrace();
                        stopMedia();
                        startPlay(mediaRaw, videoCallBack, false);
                    }catch (Exception ee){
                        ee.printStackTrace();
                        stopMedia();
                        startPlay(mediaRaw, videoCallBack, false);
                    }
                }
            });
        }else{ // else
            throw new IllegalArgumentException("Unexpected Type number.");
        }
    }

    public void setRepeatFlag(boolean repeatFlag) {
        this.repeatFlag = repeatFlag;
    }

    private boolean repeatFlag = false;
    private Handler intervalStateCheckHandler = new Handler();
    private Runnable stateCheck = new Runnable() {
        @Override
        public void run() {
            checkState();
        }
    };

    private void checkState(){
        if(repeatFlag) webView.loadUrl("javascript:currentStatus();");
        intervalStateCheckHandler.postDelayed(stateCheck, 1000);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = mInflater.inflate(R.layout.always_on_display_layout, null);

        int versionDependedType = WindowManager.LayoutParams.TYPE_PHONE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            versionDependedType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }

        mediaPlayer = new MediaPlayer();

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
                    if(url.startsWith("pickle://currentStatus?state=")){
                        final int parsedState = Integer.parseInt(url.replaceAll("pickle://currentStatus\\?state=", ""));
                        final Intent activityIntent1 = new Intent(Constants.ACTIVITY_INTENT_FILTER);
                        if(repeatFlag){
                            activityIntent1.putExtra("action", "state");
                            activityIntent1.putExtra("state", parsedState);
                            MediaService.this.sendBroadcast(activityIntent1);
                        }
                    }
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

        mManager.addView(mView, mParams);
    }

    protected void showPlayerNotification(){

        Notification.Builder mBuilder = createNotification();

//        mNotificationManager.notify(20180312, mBuilder.build());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(Constants.NOTIFICATION_CHANNEL_ID, Constants.NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
//            notificationChannel.enableLights(true);
//            notificationChannel.setLightColor(Color.RED);
//            notificationChannel.setShowBadge(true);
//            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationChannel.setSound(null, null);
            mNotificationManager.createNotificationChannel(notificationChannel);
            mBuilder.setChannelId(Constants.NOTIFICATION_CHANNEL_ID);
        }

        final RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.player_notification_layout);

        /**
         * Setting Listeners start
         */
        final Intent intent_play = new Intent(Constants.INTENT_NOTIFICATION.REP_FILTER);
        intent_play.putExtra("action", Constants.INTENT_NOTIFICATION.ACTION_PLAY);
//        intent_play.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent_play = PendingIntent.getBroadcast(this, Constants.INTENT_NOTIFICATION.REQ_CODE_ACTION_PLAY, intent_play, 0);
        remoteViews.setOnClickPendingIntent(R.id.noti_play, pendingIntent_play);

        final Intent intent_stop = new Intent(Constants.INTENT_NOTIFICATION.REP_FILTER);
        intent_stop.putExtra("action", Constants.INTENT_NOTIFICATION.ACTION_STOP);
//        intent_stop.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent_stop = PendingIntent.getBroadcast(this, Constants.INTENT_NOTIFICATION.REQ_CODE_ACTION_STOP, intent_stop, 0);
        remoteViews.setOnClickPendingIntent(R.id.noti_pause, pendingIntent_stop);

        final Intent intent_close = new Intent(Constants.INTENT_NOTIFICATION.REP_FILTER);
        intent_close.putExtra("action", Constants.INTENT_NOTIFICATION.ACTION_CLOSE);
//        intent_close.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent_close = PendingIntent.getBroadcast(this, Constants.INTENT_NOTIFICATION.REQ_CODE_ACTION_CLOSE, intent_close, 0);
        remoteViews.setOnClickPendingIntent(R.id.noti_close, pendingIntent_close);

        PendingIntent noti_intent = createPendingIntent();
        remoteViews.setOnClickPendingIntent(R.id.mainLayout, createPendingIntent());
        /**
         * Setting Listeners end
         */

        mBuilder.setContent(remoteViews);
//        mBuilder.setContentIntent(createPendingIntent());
        mBuilder.setOngoing(true);

        final Notification notification = mBuilder.build();



        if(nowPlayingMusic == null || nowPlayingMusic.getParent() == null) {
            remoteViews.setImageViewResource(R.id.noti_img, R.drawable.icon_hour_glass);
            remoteViews.setTextViewText(R.id.noti_title, "재생중인 채널이 없습니다.");
            remoteViews.setTextViewText(R.id.noti_sub, "");
        }else{
            remoteViews.setImageViewResource(R.id.noti_img, R.drawable.icon_hour_glass);
            if(nowPlayingMusic.getParent().getImgPath() != null && !nowPlayingMusic.getParent().getImgPath().trim().equals("")) {
                try {
                    Picasso
                            .get()
                            .load(nowPlayingMusic.getParent().getImgPath())
                            .centerCrop()
                            .resize(50, 50)
                            .transform(new RoundedTransform(5, 0)).into(remoteViews, R.id.noti_img, notiId, notification);
                }catch (Exception e){
                    e.printStackTrace();
                    remoteViews.setImageViewResource(R.id.noti_img, R.drawable.icon_hour_glass);
                }
            }
            remoteViews.setTextViewText(R.id.noti_title, nowPlayingMusic.getParent().getTitle());
            remoteViews.setTextViewText(R.id.noti_sub, nowPlayingMusic.getParent().getContent());
        }
        setNotificationPlaying(remoteViews, R.id.noti_play, R.id.noti_pause, isPlaying);

        Log.e("MediaService", "Notification showed");

        if(nowPlayingMusic != null && nowPlayingMusic.getParent() != null) startForeground(notiId, notification);
    }

    private void setNotificationPlaying(RemoteViews remoteViews, int playId, int stopId, boolean isPlaying){
        if(isPlaying){
            remoteViews.setViewVisibility(playId, View.INVISIBLE);
            remoteViews.setViewVisibility(stopId, View.VISIBLE);
        }else{
            remoteViews.setViewVisibility(playId, View.VISIBLE);
            remoteViews.setViewVisibility(stopId, View.INVISIBLE);
        }
    }

    private PendingIntent createPendingIntent(){
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.putExtra("action", "open");
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        return stackBuilder.getPendingIntent(
                95,
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
                .setSmallIcon(R.drawable.temp_ico_small)
                .setLargeIcon(icon)
                .setContentTitle("StatusBar Title")
                .setContentText("StatusBar subTitle")
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis());
//                .setDefaults(Notification.);
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
        mediaPlayer.reset();
        isPlaying = false;
        sendRefreshingBroadcast();
    }

    public void sendFinishingBroadcast(){
        final Intent activityIntent1 = new Intent(Constants.ACTIVITY_INTENT_FILTER);
        activityIntent1.putExtra("action", "finish");
        this.sendBroadcast(activityIntent1);
    }

    public void sendRefreshingBroadcast(){
        final Intent activityIntent1 = new Intent(Constants.ACTIVITY_INTENT_FILTER);
        activityIntent1.putExtra("action", "refresh");
        this.sendBroadcast(activityIntent1);
    }

    public void stopAll(){
        if(mView != null){
            mManager.removeView(mView);
        }
        webView.loadUrl("javascript:player.pauseVideo();");
        Log.e("MediaService", "stopMedia Invoked.");
        mediaPlayer.reset();
        isPlaying = false;

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
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        unregisterReceiver(notificationListener);
        mediaPlayer.stop();
        isPlaying = false;
    }

}
