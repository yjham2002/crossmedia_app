package kr.co.picklecode.crossmedia;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;

import java.util.List;

import bases.BaseActivity;
import bases.Constants;
import bases.imageTransform.RoundedTransform;
import kr.co.picklecode.crossmedia.models.AdapterCall;
import kr.co.picklecode.crossmedia.models.Article;
import kr.co.picklecode.crossmedia.observers.ObserverCallback;
import kr.co.picklecode.crossmedia.observers.SettingsContentObserver;
import kr.co.picklecode.crossmedia.services.MediaService;

public class FavorActivity extends BaseActivity {

    private ImageView _topBtn;
    private Handler topBtnHandler;
    private Runnable topBtnRun;

    private View btn_back;

    private RecyclerView mRecyclerView;
    private ArticleAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private ToggleButton playingTimer;

    private AdView mAdView;

    private SeekBar volumeSeekbar;
    private AudioManager audioManager;
    private SettingsContentObserver mSettingsContentObserver;

    /**
     * Slider
     */
    private ControllableSlidingLayout controllableSlidingLayout;
    private View slideAnchor;
    private ImageView arrowDown;

    private Activity mContext;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getExtras().getString("action", "");
            final int state = intent.getExtras().getInt("state", -1);
            Log.e("FavorRecv", action);
            switch (action){
                case "favorRefresh":{
                    loadList();
                    break;
                }
                case "finish":{
                    Log.e("finishState", "invoked");
                    FavorActivity.this.finish();
                    break;
                }
                case "refresh":{

                    UISyncManager.getInstance().syncCurrentText(mContext, R.id.cg_current_id);
                    UISyncManager.getInstance().syncTimerSet(mContext, R.id.sleepTimer);
                    UISyncManager.getInstance().syncTimerSet(mContext, R.id.playing_timer);

                    notifyPlayerInfoChanged();
                    break;
                }
                case "state":{

                    break;
                }
            }
        }
    };

    /**
     * Player Component
     */
    private ImageView playing_thumb;
    private TextView playing_title;
    private TextView playing_sub;
    private TextView bottom_title;
    private ToggleButton playing_control;
    private ToggleButton bottom_toggle;
    private ToggleButton playing_favor;
    private ToggleButton.OnCheckedChangeListener playerFavorListener = new ToggleButton.OnCheckedChangeListener(){
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if(b){
                FavorSQLManager.getInstance(mContext).insert(UISyncManager.getInstance().getService().getNowPlaying());
            }else {
                FavorSQLManager.getInstance(mContext).delete(UISyncManager.getInstance().getService().getNowPlaying().getId());
            }
            final Intent intent = new Intent(Constants.ACTIVITY_INTENT_FILTER);
            intent.putExtra("action", "favorRefresh");
            sendBroadcast(intent);
        }
    };
    private ToggleButton.OnCheckedChangeListener playerControlListener = new ToggleButton.OnCheckedChangeListener(){
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if(b){
                try {
                    resumeMusic();
                }catch (IllegalStateException e){
                    notifyPlayerInfoChanged();
                }
            }else {
                stopMusic();
            }
        }
    };

    private void stopMusic(){
        UISyncManager.getInstance().getService().stopMedia();
        notifyPlayerInfoChanged();
    }

    private void startMusic(Article article, final boolean openSider){
        if(isNetworkEnable()) {
            try {
                UISyncManager.getInstance().getService().startVideo(article, new MediaService.VideoCallBack() {
                    @Override
                    public void onCall() {
                        if(!isNetworkEnable()){
                            UISyncManager.getInstance().getService().stopMedia();
                            showToast("네트워크에 연결할 수 없습니다.");
                        }
                        notifyPlayerInfoChanged();
                        if(openSider) controllableSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                    }
                });

            } catch (IllegalArgumentException e) {
                showToast("로드할 수 없습니다.");
            }
            notifyPlayerInfoChanged();
        }else {
            showToast("네트워크에 연결할 수 없습니다.");
        }
    }

    private void resumeMusic() throws IllegalStateException{
        final Article article = UISyncManager.getInstance().getService().getNowPlaying();
        startMusic(article, true);
        if(article == null) throw new IllegalStateException();

        notifyPlayerInfoChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setContentView(R.layout.activity_favor);

        initView();
    }

    private void notifyPlayerInfoChanged(){
        if(UISyncManager.getInstance().getService() != null && UISyncManager.getInstance().getService().getNowPlaying() != null){
            final Article article = UISyncManager.getInstance().getService().getNowPlaying();

            if(FavorSQLManager.getInstance(mContext).getPrimaryKeySet().contains(article.getId())){
                playing_favor.setOnCheckedChangeListener(null);
                playing_favor.setChecked(true);
            }else{
                playing_favor.setOnCheckedChangeListener(null);
                playing_favor.setChecked(false);
            }

            playing_favor.setOnCheckedChangeListener(playerFavorListener);

            if(article.getImgPath() != null && !article.getImgPath().trim().equals("")) {
                Picasso
                        .get()
                        .load(article.getImgPath())
                        .placeholder(R.drawable.icon_hour_glass)
                        .transform(new RoundedTransform(10, 0)).into(playing_thumb);
            }

            bottom_title.setText(article.getTitle());
            playing_title.setText(article.getTitle());
            playing_sub.setText(article.getContent());

            playing_control.setOnCheckedChangeListener(null);
            bottom_toggle.setOnCheckedChangeListener(null);

            playing_control.setChecked(UISyncManager.getInstance().getService().isPlaying());
            bottom_toggle.setChecked(UISyncManager.getInstance().getService().isPlaying());

            playing_control.setOnCheckedChangeListener(playerControlListener);
            bottom_toggle.setOnCheckedChangeListener(playerControlListener);

            mAdapter.notifyDataSetChanged();
        }
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

    private void initView(){
        mContext = this;

        UISyncManager.getInstance().syncTimerSet(this, R.id.playing_timer);

        initControls();

        playingTimer = findViewById(R.id.playing_timer);

        playing_title = findViewById(R.id.playing_title);
        bottom_title = findViewById(R.id.bottom_title);
        playing_sub = findViewById(R.id.playing_sub);
        playing_control = findViewById(R.id.playing_control);
        playing_thumb = findViewById(R.id.playing_thumb);
        bottom_toggle = findViewById(R.id.toggle);
        playing_favor = findViewById(R.id.playing_favor);

        refreshAd(true, false);

        _topBtn = findViewById(R.id.top_btn);

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("580AF1AB9D6734064E03DF3C086DB1B2").addTestDevice("A054380EE96401ECDEB88482E433AEF2")
                .build();
        mAdView.loadAd(adRequest);

        btn_back = findViewById(R.id.btn_back_action);

        mRecyclerView = findViewById(R.id.recyclerView);

        mAdapter = new ArticleAdapter(this, R.layout.layout_article, new AdapterCall<Article>(){
            @Override
            public void onCall(Article article) { // View Listener
                // TODO
                startMusic(article, true);
            }
        });

        mRecyclerView.setAdapter(mAdapter);
        layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        topBtnHandler = new Handler();
        topBtnRun = new Runnable() {
            @Override
            public void run() {
                _topBtn.setVisibility(View.INVISIBLE);
            }
        };

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState != 0){
                    _topBtn.setVisibility(View.VISIBLE);
                    topBtnHandler.removeCallbacks(topBtnRun);
                }else{
                    topBtnHandler.postDelayed(topBtnRun, 3000);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

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
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if(newState == SlidingUpPanelLayout.PanelState.EXPANDED){
                        Window window = getWindow();
                        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                        window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryPlayer));
                    }else {
                        Window window = getWindow();
                        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                        window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
                    }
                }
            }
        });

        setClick(btn_back, arrowDown, _topBtn, playingTimer);

        loadList();
    }

    private void loadList(){
        UISyncManager.getInstance().getSongList().clear();

        List<Article> articles = FavorSQLManager.getInstance(this).getResultOrderBy(null);

        for(int e = 0; e < articles.size(); e++){
            final Article article = articles.get(e);

            UISyncManager.getInstance().getSongList().add(article);
        }

        mAdapter.dataChange();
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.playing_timer: {
                final Intent intent = new Intent(FavorActivity.this, TimerActivity.class);
                UISyncManager.getInstance().syncTimerSet(this, R.id.sleepTimer);
                UISyncManager.getInstance().syncTimerSet(this, R.id.playing_timer);
                startActivity(intent);
                break;
            }
            case R.id.top_btn:{
                mRecyclerView.smoothScrollToPosition(0);
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

    @Override
    public void onResume(){
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(Constants.ACTIVITY_INTENT_FILTER));
        if(UISyncManager.getInstance().isSchemeLoaded()){
            UISyncManager.getInstance().syncCurrentText(this, R.id.cg_current_id);
        }

        UISyncManager.getInstance().syncTimerSet(this, R.id.playing_timer);

        notifyPlayerInfoChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        UISyncManager.getInstance().stopSyncText();
        unregisterReceiver(broadcastReceiver);
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
