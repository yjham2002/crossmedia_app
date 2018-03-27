package kr.co.picklecode.crossmedia;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.NativeAppInstallAd;
import com.google.android.gms.ads.formats.NativeAppInstallAdView;
import com.google.android.gms.ads.formats.NativeContentAd;
import com.google.android.gms.ads.formats.NativeContentAdView;

import java.util.List;
import java.util.Locale;

import bases.BaseActivity;
import bases.BaseApp;
import bases.Constants;

public class ExitActivity extends BaseActivity {

    private Button btn_exit;
    private Button btn_cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exit);

        initView();
    }

    private void initView(){
        btn_exit = findViewById(R.id.exit_action);
        btn_cancel = findViewById(R.id.btn_cancel);

        refreshAd(true, false);

        setClick(btn_cancel, btn_exit);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.exit_action:{
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        final Intent i = new Intent(Constants.ACTIVITY_INTENT_FILTER);
                        i.putExtra("action", "finish");
                        sendBroadcast(i);
                    }
                }, 1000);
                finish();
            }
                break;
            case R.id.btn_cancel:{
                cancelExitingProcess();
            }
            break;
            default: break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void cancelExitingProcess(){
        finish();
        overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
        loadInterstitialAd();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)){
            cancelExitingProcess();
        }
        return super.onKeyDown(keyCode, event);
    }

}
