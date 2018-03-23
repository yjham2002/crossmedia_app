package kr.co.picklecode.crossmedia;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import bases.BaseActivity;
import bases.Constants;
import bases.utils.AlarmUtils;
import kr.co.picklecode.crossmedia.models.AdapterCall;
import kr.co.picklecode.crossmedia.models.TimerItem;
import kr.co.picklecode.crossmedia.observers.ObserverCallback;
import kr.co.picklecode.crossmedia.observers.SettingsContentObserver;
import utils.PreferenceUtil;

public class TimerActivity extends BaseActivity {

    private Activity mContext;

    private ImageView btn_back;

    private RecyclerView mRecyclerView;
    private TimerAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private TextView caption;

    private AdView mAdView;

    private ToggleButton playingTimer;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getExtras().getString("action", "");
            Log.e("LocalBroadCast", action);
            switch (action){
                case "refresh":{
                    UISyncManager.getInstance().syncCurrentText(mContext, R.id.cg_current_id);
                    UISyncManager.getInstance().syncTimerSet(mContext, R.id.playing_timer);
                    loadList();
                    timeIndHandler.post(timeIndicator);
                    refreshTimerText();
//                    notifyPlayerInfoChanged();
                    break;
                }
            }
        }
    };

    /**
     * Slider
     */
    private ControllableSlidingLayout controllableSlidingLayout;
    private View slideAnchor;
    private ImageView arrowDown;

    /**
     * Audio Controls
     */
    private SeekBar volumeSeekbar;
    private AudioManager audioManager;
    private SettingsContentObserver mSettingsContentObserver;

    @Override
    protected void onResume() {
        super.onResume();
        if(UISyncManager.getInstance().isSchemeLoaded()){
            UISyncManager.getInstance().syncCurrentText(this, R.id.cg_current_id);
        }
        registerReceiver(broadcastReceiver, new IntentFilter(Constants.ACTIVITY_INTENT_FILTER));
        UISyncManager.getInstance().syncTimerSet(this, R.id.playing_timer);
    }

    @Override
    protected void onPause() {
        super.onPause();
        UISyncManager.getInstance().stopSyncText();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setContentView(R.layout.activity_timer);

        initView();
    }

    private void initControls() {
        mSettingsContentObserver = new SettingsContentObserver(this, new Handler(), new ObserverCallback() {
            @Override
            public void onNegativeCall(int volume) {
                volumeSeekbar.setProgress(volume);
            }
            @Override
            public void onPositiveCall(int volume) {
                volumeSeekbar.setProgress(volume);
            }
        });

        getApplicationContext().getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, mSettingsContentObserver );

        try {
            volumeSeekbar = findViewById(R.id.volumebar);
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            volumeSeekbar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            volumeSeekbar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
            volumeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onStopTrackingTouch(SeekBar arg0) {
                }
                @Override
                public void onStartTrackingTouch(SeekBar arg0) {
                }
                @Override
                public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        getApplicationContext().getContentResolver().unregisterContentObserver(mSettingsContentObserver);
    }

    private Runnable timeIndicator = new Runnable() {
        @Override
        public void run() {
            final long remainings = (PreferenceUtil.getLong(Constants.PREFERENCE.ALARM_TIME, 0) - System.currentTimeMillis()) / 1000;
            final long hour = remainings / 60 / 60;
            final long min = (remainings - (hour * 60 * 60)) / 60;
            final long sec = (remainings - (hour * 60 * 60)) - (min * 60);
            if(remainings <= 0){
                caption.setText("Sleep Timer");
            }else{
                caption.setText(String.format("%02d:%02d:%02d", hour, min, sec));
                refreshTimerText();
            }
        }
    };

    private Handler timeIndHandler = new Handler();

    private void refreshTimerText(){
        if(AlarmUtils.getInstance().getCurrentSetAlarm(mContext) == null){
            timeIndHandler.removeCallbacks(timeIndicator);
            caption.setText("Sleep Timer");
        }else{
            timeIndHandler.postDelayed(timeIndicator, 1000);
        }
    }

    private void initView(){
        this.mContext = this;

        initControls();

        playingTimer = findViewById(R.id.playing_timer);

        refreshAd(true, false);

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("580AF1AB9D6734064E03DF3C086DB1B2").addTestDevice("A054380EE96401ECDEB88482E433AEF2")
                .build();
        mAdView.loadAd(adRequest);

        caption = findViewById(R.id.sleepTimerCaption);
        btn_back = findViewById(R.id.btn_back_action);

        mRecyclerView = findViewById(R.id.recyclerView);
        mAdapter = new TimerAdapter(this, R.layout.layout_timer, new AdapterCall<TimerItem>() {
            @Override
            public void onCall(TimerItem article) {
                UISyncManager.getInstance().syncTimerSet(mContext, R.id.playing_timer);
                if(!article.isCancel()) {
                    final int inHour = article.getTimeInMins() / 60;
                    final int leftMin = article.getTimeInMins() - (inHour * 60);
                    final String timeString = String.format("%02d:%02d:00", inHour, leftMin);
                    showToast(timeString + " 후에 음악방송 재생이 중지됩니다.");
                }else{
                    showToast("슬립타이머 설정이 취소되었습니다.");
                }
            }
        });

        mRecyclerView.setAdapter(mAdapter);
        layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        /**
         * Slider
         */
        controllableSlidingLayout = findViewById(R.id.sliding_layout);
        slideAnchor = findViewById(R.id.slideAnchor);
        arrowDown = findViewById(R.id.btn_arrow_down);
        controllableSlidingLayout.setClickToCollapseEnabled(false);
        controllableSlidingLayout.setActionWhenExpanded(true, new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                Log.d("Slider", "slideOffset:"+slideOffset);

                final float toAlpha = 1.0f - slideOffset;
                slideAnchor.setAlpha(toAlpha);
                if(toAlpha == 0.0f) slideAnchor.setVisibility(View.INVISIBLE);
                else slideAnchor.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                Log.d("Slider", "previousState:"+previousState +"\nnewState:"+newState);
            }
        });

        setClick(btn_back, arrowDown, playingTimer);

        loadList();

        timeIndHandler.post(timeIndicator);
        refreshTimerText();
    }

    private void loadList(){
        mAdapter.mListData.clear();

        mAdapter.mListData.add(new TimerItem(15, "15 Mins", false));
        mAdapter.mListData.add(new TimerItem(30, "30 Mins", false));
        mAdapter.mListData.add(new TimerItem(45, "45 Mins", false));
        mAdapter.mListData.add(new TimerItem(60 * 1, "1 Hour", false));
        mAdapter.mListData.add(new TimerItem(60 * 2, "2 Hours", false));
        mAdapter.mListData.add(new TimerItem(60 * 3, "3 Hours", false));
        mAdapter.mListData.add(new TimerItem(60 * 4, "4 Hours", false));
        mAdapter.mListData.add(new TimerItem(60 * 5, "5 Hours", false));
        if(PreferenceUtil.getBoolean(Constants.PREFERENCE.IS_ALARM_SET, false)) {
            mAdapter.mListData.add(new TimerItem(0, "Cancel", true));
        }

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.playing_timer: {
                UISyncManager.getInstance().syncTimerSet(this, R.id.sleepTimer);
                UISyncManager.getInstance().syncTimerSet(this, R.id.playing_timer);
                break;
            }
            case R.id.btn_back_action: {
                finish();
                break;
            }
            case R.id.btn_arrow_down: {
                controllableSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                break;
            }
            default: break;
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (controllableSlidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                controllableSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                return false;
            } else {
            }
        }
        return super.onKeyDown(keyCode, event);
    }

}
