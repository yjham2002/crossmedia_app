package kr.co.picklecode.crossmedia;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import bases.BaseActivity;

import bases.Constants;
import bases.SimpleCallback;
import bases.imageTransform.RoundedTransform;
import comm.SimpleCall;
import kr.co.picklecode.crossmedia.models.AdapterCall;
import kr.co.picklecode.crossmedia.models.Article;
import kr.co.picklecode.crossmedia.models.ChannelScheme;
import kr.co.picklecode.crossmedia.models.MediaRaw;
import kr.co.picklecode.crossmedia.observers.ObserverCallback;
import kr.co.picklecode.crossmedia.observers.SettingsContentObserver;
import kr.co.picklecode.crossmedia.services.MediaService;

public class MainActivity extends BaseActivity {

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

    private Activity mContext;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getExtras().getString("action", "");
            final int state = intent.getExtras().getInt("state", -1);
            Log.e("MainReceiver", action);
            switch (action){
                case "open":{
                    controllableSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                    break;
                }
                case "finish":{
                    Log.e("finishState", "invoked");
                    MainActivity.this.finish();
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
                FavorSQLManager.getInstance(mContext).insert(UISyncManager.getInstance().getService().getNowPlayingMusic().getParent());
            }else {
                FavorSQLManager.getInstance(mContext).delete(UISyncManager.getInstance().getService().getNowPlayingMusic().getParent().getId());
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

    private void startMusic(Article article, final boolean openSider){
        UISyncManager.getInstance().setSongList(article.getMediaRaws());
        if(isNetworkEnable()) {
            try {
                UISyncManager.getInstance().getService().startChannel(article, new MediaService.VideoCallBack() {
                    @Override
                    public void onCall() {
                        if(!isNetworkEnable()){
                            UISyncManager.getInstance().getService().stopMedia();
                            showToast("네트워크에 연결할 수 없습니다.");
                        }
                        notifyPlayerInfoChanged();
//                        if(openSider) controllableSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
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

    private void loadMusic(Article article){
        UISyncManager.getInstance().setSongList(article.getMediaRaws());
        if(isNetworkEnable()) {
            try {
                UISyncManager.getInstance().getService().loadChannel(article, new MediaService.VideoCallBack() {
                    @Override
                    public void onCall() {
                        UISyncManager.getInstance().getService().stopMedia();
                        notifyPlayerInfoChanged();
//                        if(openSider) controllableSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
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
        final Article article = UISyncManager.getInstance().getService().getNowPlayingMusic().getParent();
        startMusic(article, true);
        if(article == null) throw new IllegalStateException();

        notifyPlayerInfoChanged();
    }

    private void notifyPlayerInfoChanged(){
        if(UISyncManager.getInstance().getService() != null && UISyncManager.getInstance().getService().getNowPlayingMusic() != null && UISyncManager.getInstance().getService().getNowPlayingMusic().getParent() != null){
            final Article article = UISyncManager.getInstance().getService().getNowPlayingMusic().getParent();

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
                        .centerCrop()
                        .resize(100, 100)
                        .placeholder(R.drawable.icon_hour_glass)
                        .transform(new RoundedTransform(5, 0)).into(playing_thumb);
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

    private void stopMusic(){
        UISyncManager.getInstance().getService().stopMedia();
        notifyPlayerInfoChanged();
    }

    private void init(){
        mContext = this;

        initControls();

        refreshAd(false, true);

        playingTimer = findViewById(R.id.playing_timer);

        playing_title = findViewById(R.id.playing_title);
        bottom_title = findViewById(R.id.bottom_title);
        playing_sub = findViewById(R.id.playing_sub);
        playing_control = findViewById(R.id.playing_control);
        playing_thumb = findViewById(R.id.playing_thumb);
        bottom_toggle = findViewById(R.id.toggle);
        playing_favor = findViewById(R.id.playing_favor);

        playing_title.setSelected(true);
        playing_sub.setSelected(true);
        bottom_title.setSelected(true);

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
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if(newState == SlidingUpPanelLayout.PanelState.EXPANDED){
                        Window window = getWindow();
                        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                        window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryPlayer));
                        slideAnchor.setVisibility(View.INVISIBLE);
                    }else {
                        Window window = getWindow();
                        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                        window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
                        slideAnchor.setVisibility(View.VISIBLE);
                    }
                }

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

                if (!mRecyclerView.canScrollVertically(1)) {
//                    if(currentPage < totalPage && !isLoading) loadList(UISyncManager.getInstance().getChannelScheme(), ++currentPage);
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
                closeDrawer();
                loadList(article, 1);
            }
        });

        mRecyclerViewMenu.setAdapter(mAdapterMenu);
        layoutManagerMenu = new LinearLayoutManager(this);
        mRecyclerViewMenu.setLayoutManager(layoutManagerMenu);
        mRecyclerViewMenu.setItemAnimator(new DefaultItemAnimator());

        btn_favor = findViewById(R.id.top_fav);

        setClick(btn_favor, btn_menu_top, sleepTimer, arrowDown, _topBtn, playingTimer, playing_control, playing_sub, playing_title);

        if(!isNetworkEnable()){
            showToast("네트워크에 연결할 수 없어 앱을 종료합니다.");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    System.exit(0);
                }
            }, 4000);
        }

        notifyPlayerInfoChanged();
    }

    private boolean isLoading = false;

    private void loadList(ChannelScheme channelScheme, int page){

        if(!UISyncManager.getInstance().isSchemeLoaded()){
            UISyncManager.getInstance().setChannelScheme(channelScheme);
        }

        UISyncManager.getInstance().resetInitialized();
        UISyncManager.getInstance().setChannelScheme(channelScheme);
        UISyncManager.getInstance().syncCurrentText(this, R.id.cg_current_id);

        titleDisplay.setText(channelScheme.getTitle());

        isLoading = true;
        progress.setVisibility(View.VISIBLE);
        progressMain.setVisibility(View.VISIBLE);

        UISyncManager.getInstance().getChList().clear();

        Map<String, Object> params = new HashMap<>();
        params.put("app_id", 374);
        params.put("cg_id", channelScheme.getId());
        SimpleCall.getHttpJson("http://zacchaeus151.cafe24.com/api/radio_list.php", params, new SimpleCall.CallBack() {
            @Override
            public void handle(JSONObject jsonObject) {
                try {
                    final JSONArray json_arr  = jsonObject.getJSONArray("result");

                    for(int i = 0; i < json_arr.length(); i++){
                        final JSONObject object = json_arr.getJSONObject(i);
                        final JSONArray chs = object.getJSONArray("channels");
                        final Article article = new Article();
                        article.setId(object.getInt("cg_id"));
                        article.setType(-1);
                        article.setImgPath(object.getString("cg_thumb"));
                        article.setTitle(object.getString("cg_name"));
                        article.setContent(object.getString("cg_sub_name"));
                        article.setCg_max(object.getInt("cg_max"));
                        article.setCg_min(object.getInt("cg_min"));
                        article.setCg_range(object.getInt("cg_range"));
                        article.setCg_current(0);

                        for(int j = 0; j < chs.length(); j++){
                            final JSONObject ch = chs.getJSONObject(j);
                            if(ch.getString("cg_id") == null || ch.getString("cg_id").equals("null")) continue;
                            final MediaRaw mediaRaw = new MediaRaw();
                            mediaRaw.setParent(article);
                            mediaRaw.setCg_id(ch.getInt("cg_id"));
                            mediaRaw.setCh_id(ch.getInt("ch_id"));
                            mediaRaw.setTitle(ch.getString("ch_name"));
                            mediaRaw.setImgPath(ch.getString("ch_thumb"));
                            mediaRaw.setType(ch.getInt("vd_internet_use"));
                            mediaRaw.setRegDate(ch.getString("vd_url"));
                            article.getMediaRaws().add(mediaRaw);
                        }

                        Collections.shuffle(article.getMediaRaws());
                        UISyncManager.getInstance().getChList().add(article);
                    }

//                    Log.e("result", json_arr.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }finally {
                    if(UISyncManager.getInstance().getChList().size() > 0) {
                        /**
                         * Must have to be located above the notifyDataSetChanged().
                         * cause, the marking process need to be ran after the service-now-playing setting process.
                         */
                        if(UISyncManager.getInstance().getService().isInitialRunning()) {
                            Log.e("initialF", "play");
                            mAdapter.setClickedPos(0);
                            loadMusic(UISyncManager.getInstance().getChList().get(0));
                            UISyncManager.getInstance().getService().setInitialRunning(false);
                        }
                    }

                    Log.e("mASize", UISyncManager.getInstance().getChList().size() + "");

                    mAdapter.dataChange();
                    progress.setVisibility(View.INVISIBLE);
                    progressMain.setVisibility(View.INVISIBLE);
                    isLoading = false;
                    mRecyclerView.smoothScrollToPosition(0);
                }
            }
        });
    }

    private void loadMenuList(final int page){
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

                        mAdapterMenu.mListData.add(article);
                        if(i == page){
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
                final Intent intent = new Intent(MainActivity.this, TimerActivity.class);
                UISyncManager.getInstance().syncTimerSet(this, R.id.sleepTimer);
                UISyncManager.getInstance().syncTimerSet(this, R.id.playing_timer);
                startActivity(intent);
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

    @Override
    protected void onStop() {
        super.onStop();
//        if(mBounded) {
//            unbindService(mConnection);
//            mBounded = false;
//        }
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

        int toLoad = mAdapterMenu.getClickedPos();
        loadMenuList(toLoad);
        mAdapterMenu.setClickedPos(toLoad);

        registerReceiver(broadcastReceiver, new IntentFilter(Constants.ACTIVITY_INTENT_FILTER));

        mDrawerToggle.syncState();
        if(UISyncManager.getInstance().isSchemeLoaded()){
            UISyncManager.getInstance().syncCurrentText(this, R.id.cg_current_id);
        }

        notifyPlayerInfoChanged();

        UISyncManager.getInstance().syncTimerSet(this, R.id.sleepTimer);
        UISyncManager.getInstance().syncTimerSet(this, R.id.playing_timer);
        mAdapter.dataChange();

        if(getIntent().getStringExtra("action") != null && getIntent().getStringExtra("action").equals("open")) {
            controllableSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            getIntent().removeExtra("action");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
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
