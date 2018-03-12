package kr.co.picklecode.crossmedia;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;

import bases.BaseActivity;

public class ExitActivity extends BaseActivity {

    private NativeExpressAdView mNativeExpressAdView;

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

        mNativeExpressAdView = findViewById(R.id.adView);

        AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
        adRequestBuilder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);

        mNativeExpressAdView.loadAd(adRequestBuilder.build());

        setClick(btn_cancel, btn_exit);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.exit_action:{
                System.exit(0);
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
        mNativeExpressAdView.resume();
    }

    @Override
    public void onPause() {
        mNativeExpressAdView.pause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mNativeExpressAdView.destroy();
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
