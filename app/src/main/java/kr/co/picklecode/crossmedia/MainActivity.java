package kr.co.picklecode.crossmedia;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import bases.BaseActivity;

import bases.BaseApp;
import comm.SimpleCall;
import kr.co.picklecode.crossmedia.models.AdapterCall;
import kr.co.picklecode.crossmedia.models.Article;
import kr.co.picklecode.crossmedia.models.ChannelScheme;
import kr.co.picklecode.crossmedia.observers.ObserverCallback;
import kr.co.picklecode.crossmedia.observers.SettingsContentObserver;
import kr.co.picklecode.crossmedia.services.MediaService;

public class MainActivity extends BaseActivity {

    private boolean mBounded;
    private MediaService mServer;

    private Activity mContext;

    private TextView titleDisplay;

    private ImageView _topBtn;
    private Handler topBtnHandler;
    private Runnable topBtnRun;

    private ProgressBar progress;
    private ProgressBar progressMain;

    private ToggleButton sleepTimer;
    private ToggleButton playingTimer;
    private ImageView btn_favor;

    private RecyclerView mRecyclerView;
    private ArticleAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private RecyclerView mRecyclerViewMenu;
    private SchemeAdapter mAdapterMenu;
    private RecyclerView.LayoutManager layoutManagerMenu;

    private ImageView btn_menu_top; // Menu Buttons
    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;

    /**
     * Player Component
     */
    private TextView playing_title;
    private TextView playing_sub;
    private TextView bottom_title;
    private ToggleButton playing_control;
    private ToggleButton bottom_toggle;
    private ToggleButton.OnCheckedChangeListener playerControlListener = new ToggleButton.OnCheckedChangeListener(){
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if(b){
                resumeMusic();
            }else {
                stopMusic();
            }
        }
    };

    private SeekBar volumeSeekbar;
    private AudioManager audioManager;

    private AdView mAdView;

    private SettingsContentObserver mSettingsContentObserver;

    /**
     * Slider
     */
    private ControllableSlidingLayout controllableSlidingLayout;
    private View slideAnchor;
    private ImageView arrowDown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setContentView(R.layout.activity_main);
        setTitle("");

        init();
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

    private void startMusic(Article article){
        if(isNetworkEnable()) {
            try {
                mServer.startVideo(article, new MediaService.VideoCallBack() {
                    @Override
                    public void onCall() {
                        if(!isNetworkEnable()){
                            mServer.stopMedia();
                            showToast("네트워크에 연결할 수 없습니다.");
                        }
                        notifyPlayerInfoChanged();
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
        final Article article = mServer.getNowPlaying();
        startMusic(article);
        if(article == null) throw new IllegalStateException();

        notifyPlayerInfoChanged();
    }

    private void notifyPlayerInfoChanged(){
        if(mServer != null && mServer.getNowPlaying() != null){
            final Article article = mServer.getNowPlaying();

            bottom_title.setText(article.getTitle());
            playing_title.setText(article.getTitle());
            playing_sub.setText(article.getContent());

            playing_control.setOnCheckedChangeListener(null);
            bottom_toggle.setOnCheckedChangeListener(null);

            playing_control.setChecked(mServer.isPlaying());
            bottom_toggle.setChecked(mServer.isPlaying());

            playing_control.setOnCheckedChangeListener(playerControlListener);
            bottom_toggle.setOnCheckedChangeListener(playerControlListener);
        }
    }

    private void stopMusic(){
        mServer.stopMedia();
        notifyPlayerInfoChanged();
    }

    private void init(){
        mContext = this;

        initControls();

        refreshAd(true, false);

        playingTimer = findViewById(R.id.playing_timer);

        playing_title = findViewById(R.id.playing_title);
        bottom_title = findViewById(R.id.bottom_title);
        playing_sub = findViewById(R.id.playing_sub);
        playing_control = findViewById(R.id.playing_control);
        bottom_toggle = findViewById(R.id.toggle);

        titleDisplay = findViewById(R.id.titleDisplay);

        _topBtn = findViewById(R.id.top_btn);

        progress = findViewById(R.id.progress);
        progressMain = findViewById(R.id.progressMain);

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("580AF1AB9D6734064E03DF3C086DB1B2").addTestDevice("A054380EE96401ECDEB88482E433AEF2")
                .build();
        mAdView.loadAd(adRequest);

        btn_menu_top = findViewById(R.id.btn_menu_action);
        sleepTimer = findViewById(R.id.sleepTimer);

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
                if(toAlpha == 0.0f) {
                    slideAnchor.setVisibility(View.INVISIBLE);
                } else {
                    slideAnchor.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                Log.d("Slider", "previousState:"+previousState +"\nnewState:"+newState);
            }
        });

        /**
         * Channel Menu
         */
        mRecyclerView = findViewById(R.id.recyclerView);
        mAdapter = new ArticleAdapter(this, R.layout.layout_article, new AdapterCall<Article>(){
            @Override
            public void onCall(Article article) { // View Listener
                // TODO
                startMusic(article);
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

                if (!mRecyclerView.canScrollVertically(1)) {
                    if(currentPage < totalPage && !isLoading) loadList(UISyncManager.getInstance().getChannelScheme(), ++currentPage);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        /**
         * Drawer Menu
         */
        mRecyclerViewMenu = findViewById(R.id.recyclerViewMenu);
        mAdapterMenu = new SchemeAdapter(this, R.layout.layout_scheme, new AdapterCall<ChannelScheme>(){
            @Override
            public void onCall(ChannelScheme article) {
                loadList(article, 1);
            }
        });

        mRecyclerViewMenu.setAdapter(mAdapterMenu);
        layoutManagerMenu = new LinearLayoutManager(this);
        mRecyclerViewMenu.setLayoutManager(layoutManagerMenu);
        mRecyclerViewMenu.setItemAnimator(new DefaultItemAnimator());

        btn_favor = findViewById(R.id.top_fav);

        setClick(btn_favor, btn_menu_top, sleepTimer, arrowDown, _topBtn, playingTimer, playing_control, playing_sub, playing_title);

        loadInterstitialAd();

        if(!isNetworkEnable()){
            showToast("네트워크에 연결할 수 없어 앱을 종료합니다.");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    System.exit(0);
                }
            }, 4000);
        }

        loadMenuList();

        showPlayerNotification();

        notifyPlayerInfoChanged();
    }

    private int currentPage = 1;
    private int totalPage = 0;
    private int totalCount = 0;
    private boolean isLoading = false;

    private void loadList(ChannelScheme channelScheme, int page){

        boolean init = false;
        if(page == 0 || page == 1) init = true;

        if(!UISyncManager.getInstance().isSchemeLoaded()){
            UISyncManager.getInstance().setChannelScheme(channelScheme);
        }

        if(init){
            currentPage = 1;
            totalPage = 0;
            totalCount = 0;
            UISyncManager.getInstance().resetInitialized();
            UISyncManager.getInstance().setChannelScheme(channelScheme);
            UISyncManager.getInstance().syncCurrentText(this, R.id.cg_current_id);
        }

        final boolean finalInitVar = init;

        titleDisplay.setText(channelScheme.getTitle());

        isLoading = true;
        progress.setVisibility(View.VISIBLE);
        progressMain.setVisibility(View.VISIBLE);
        if(init) mAdapter.mListData.clear();

        Map<String, Object> params = new HashMap<>();
        params.put("ap_id", 374);
        params.put("cg_id", channelScheme.getId());
        params.put("page", page);
        SimpleCall.getHttpJson("http://zacchaeus151.cafe24.com/api/video_list.php", params, new SimpleCall.CallBack() {
            @Override
            public void handle(JSONObject jsonObject) {
                try {
                    final JSONObject result = jsonObject.getJSONObject("result");
                    final JSONArray json_arr  = result.getJSONArray("list");
                    totalCount = result.getInt("total_count");
                    totalPage = result.getInt("total_page");
                    currentPage = result.getInt("page");

                    for(int i = 0; i < json_arr.length(); i++){
                        JSONObject object = json_arr.getJSONObject(i);
                        final Article article = new Article();
                        article.setId(object.getInt("vd_id"));
                        article.setRepPath(object.getString("vd_url"));
                        article.setImgPath(object.getString("vd_thum_url"));
                        article.setTitle(object.getString("vd_title"));
                        article.setContent(object.getString("vd_name"));
                        mAdapter.mListData.add(article);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }finally {
                    mAdapter.dataChange();
                    progress.setVisibility(View.INVISIBLE);
                    progressMain.setVisibility(View.INVISIBLE);
                    isLoading = false;
                    if(finalInitVar){
                        mRecyclerView.smoothScrollToPosition(0);
                    }
                    // TODO Add Progressbar on the main list
                }
            }
        });
    }

    private void loadMenuList(){
        progress.setVisibility(View.VISIBLE);
        mAdapterMenu.mListData.clear();

        Map<String, Object> params = new HashMap<>();
        params.put("ap_id", 374);
        SimpleCall.getHttpJson("http://zacchaeus151.cafe24.com/api/category.php", params, new SimpleCall.CallBack() {
            @Override
            public void handle(JSONObject jsonObject) {
                try {
                    JSONArray json_arr  = jsonObject.getJSONArray("result");
                    for(int i = 0; i < json_arr.length(); i++){
                        JSONObject object = json_arr.getJSONObject(i);
                        final ChannelScheme article = new ChannelScheme();
                        article.setCrawlUrl("");
                        article.setTitle(object.getString("cg_name"));
                        article.setId(object.getInt("cg_id"));
                        article.setOrder(object.getInt("cg_order"));
                        article.setCg_max(object.getInt("cg_max"));
                        article.setCg_min(object.getInt("cg_min"));
                        article.setCg_range(object.getInt("cg_range"));
                        article.setCg_cur(object.getInt("cg_current"));
                        mAdapterMenu.mListData.add(article);
                        if(i == 0){
                            loadList(article, 1);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }finally {
                    mAdapterMenu.notifyDataSetChanged();
                    progress.setVisibility(View.INVISIBLE);
                }
            }
        });

    }

    @Override
    public void onClick(View v){
        Log.e(this.getClass().getSimpleName(), "onClick Called.");
        switch (v.getId()){
            case R.id.top_btn:{
                mRecyclerView.smoothScrollToPosition(0);
                break;
            }
            case R.id.btn_menu_action: {
                openDrawer();
                break;
            }
            case R.id.playing_timer: {
                UISyncManager.getInstance().syncTimerSet(this, R.id.sleepTimer);
                UISyncManager.getInstance().syncTimerSet(this, R.id.playing_timer);
                break;
            }
            case R.id.top_fav: {
                closeDrawer();
                final Intent favorIntent = new Intent(MainActivity.this, FavorActivity.class);
                startActivity(favorIntent);
                break;
            }
            case R.id.sleepTimer: {
                closeDrawer();
                UISyncManager.getInstance().syncTimerSet(this, R.id.sleepTimer);
                UISyncManager.getInstance().syncTimerSet(this, R.id.playing_timer);
                final Intent timerIntent = new Intent(MainActivity.this, TimerActivity.class);
                startActivity(timerIntent);
                break;
            }
            case R.id.btn_arrow_down: {
                controllableSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                break;
            }
            default:
                break;
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            showToast("disconnected");
            mBounded = false;
            mServer = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            showToast("Connected");
            mBounded = true;
            MediaService.LocalBinder mLocalBinder = (MediaService.LocalBinder)service;
            mServer = mLocalBinder.getServiceInstance();
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        if(mBounded) {
            unbindService(mConnection);
            mBounded = false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        btn_menu_top = findViewById(R.id.btn_menu_action);
        mDrawer = findViewById(R.id.drawer);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawer, 0, 0);
        mDrawer.setDrawerListener(mDrawerToggle);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setHomeButtonEnabled(true);
        }

        final Intent backgroundIntentCall = new Intent(getBaseContext(), MediaService.class);
        bindService(backgroundIntentCall, mConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
        Log.e("Drawer", "onPostCreate Called");
    }

    @Override
    public void onResume(){
        super.onResume();
        mDrawerToggle.syncState();
        if(UISyncManager.getInstance().isSchemeLoaded()){
            UISyncManager.getInstance().syncCurrentText(this, R.id.cg_current_id);
        }

        UISyncManager.getInstance().syncTimerSet(this, R.id.sleepTimer);
        UISyncManager.getInstance().syncTimerSet(this, R.id.playing_timer);
        mAdapter.dataChange();
    }

    @Override
    public void onPause() {
        super.onPause();
        UISyncManager.getInstance().stopSyncText();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) ||
                super.onOptionsItemSelected(item);
    }

    private boolean mFlag;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            if(msg.what == 0){
                mFlag=false;
            }
        }
    };

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (controllableSlidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                controllableSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                return false;
            } else {
                final Intent exitIntent = new Intent(MainActivity.this, ExitActivity.class);
                startActivity(exitIntent);
                overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
//            if(!mFlag) {
//                Toast.makeText(getApplicationContext(), "뒤로 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
//                mFlag = true;
//                mHandler.sendEmptyMessageDelayed(0, 2000);
//                return false;
//            } else {
//                finish();
//                System.exit(0);
//            }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private static final String UPDATE_INTENT = "UPDATE_INTENT_FROM_ACTIVITY";

    public void closeDrawer() {
        mDrawer.closeDrawer(Gravity.LEFT);
    }

    public void openDrawer(){
        mDrawer.openDrawer(Gravity.LEFT);
    }

}
