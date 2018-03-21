package kr.co.picklecode.crossmedia.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
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

import java.io.IOException;

import bases.Constants;
import kr.co.picklecode.crossmedia.R;
import kr.co.picklecode.crossmedia.models.Article;

/**
 * Created by HP on 2018-03-16.
 */
public class MediaService extends Service implements View.OnClickListener{

    private boolean isPlaying = false;

    private Article nowPlaying;
    private View mView;
    private WebView webView;
    private WindowManager mManager;
    private IBinder mBinder = new LocalBinder();

    public interface VideoCallBack{
        void onCall();
    }

    @Override
    public IBinder onBind(Intent intent) {
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
        if(article == null) throw new IllegalArgumentException();

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

        mManager.addView(mView, mParams);
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
        isPlaying = false;
    }

}
