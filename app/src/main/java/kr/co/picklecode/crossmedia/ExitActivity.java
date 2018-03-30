package kr.co.picklecode.crossmedia;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import bases.BaseActivity;
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

        refreshAd(false, true);

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
        loadInterstitialAd(null);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)){
            cancelExitingProcess();
        }
        return super.onKeyDown(keyCode, event);
    }

}
