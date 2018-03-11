package kr.co.picklecode.crossmedia;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import bases.BaseActivity;

public class IntroActivity extends BaseActivity {

    private static final int INTRO_DEFAULT_DELAY = 2000;

    private Handler intentHandler;
    private Runnable exitRunnable = new Runnable() {
        public void run() {
            System.exit(0);
        }
    };

    private Runnable introRunnable = new Runnable() {
        public void run() {
            Intent i = new Intent(IntroActivity.this, MainActivity.class);;
            startActivity(i);
            finish();
            overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        init();
    }

    private void init(){
        this.intentHandler = new Handler();
        this.intentHandler.postDelayed(introRunnable, INTRO_DEFAULT_DELAY);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.intentHandler.removeCallbacks(introRunnable);
    }

}
