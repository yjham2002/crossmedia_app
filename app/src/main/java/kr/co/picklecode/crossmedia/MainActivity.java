package kr.co.picklecode.crossmedia;

import android.content.Intent;
import android.content.res.Configuration;
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
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import bases.BaseActivity;

import kr.co.picklecode.crossmedia.models.Article;
import kr.co.picklecode.crossmedia.models.ChannelScheme;

import static com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState.EXPANDED;

public class MainActivity extends BaseActivity {

    private ToggleButton sleepTimer;
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
     * Slider
     */
    private ControllableSlidingLayout controllableSlidingLayout;
    private View slideAnchor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("");

        init();
    }

    private void init(){
        btn_menu_top = findViewById(R.id.btn_menu_action);
        sleepTimer = findViewById(R.id.sleepTimer);

        /**
         * Slider
         */
        controllableSlidingLayout = findViewById(R.id.sliding_layout);
        slideAnchor = findViewById(R.id.slideAnchor);
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
        mAdapter = new ArticleAdapter(this, R.layout.layout_article);
        mRecyclerView.setAdapter(mAdapter);
        layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        /**
         * Drawer Menu
         */
        mRecyclerViewMenu = findViewById(R.id.recyclerViewMenu);
        mAdapterMenu = new SchemeAdapter(this, R.layout.layout_scheme);
        mRecyclerViewMenu.setAdapter(mAdapterMenu);
        layoutManagerMenu = new LinearLayoutManager(this);
        mRecyclerViewMenu.setLayoutManager(layoutManagerMenu);
        mRecyclerViewMenu.setItemAnimator(new DefaultItemAnimator());

        btn_favor = findViewById(R.id.top_fav);

        setClick(btn_favor, btn_menu_top, sleepTimer);

        syncTimerState();

        openPopAd();

        loadList();
        loadMenuList();
    }

    private void loadList(){
        mAdapter.mListData.clear();

        for(int e = 0; e < 20; e++){
            final Article article = new Article();

            article.setImgPath("http://lorempixel.com/50/50");
            article.setTitle("테스트 채널 " + e);
            article.setContent("설명이 삽입될 위치입니다.");
            if(e == 10) {
                article.setTitle("아주 긴 채널명입니다. 아주 길고 긴 채널명입니다. 길고 깁니다.");
                article.setContent("설명이 삽입될 위치입니다. 설명이 삽입될 위치입니다. 설명이 삽입될 위치입니다.");
            }

            mAdapter.mListData.add(article);
        }

        mAdapter.notifyDataSetChanged();
    }

    private void loadMenuList(){
        mAdapterMenu.mListData.clear();

        for(int e = 0; e < 7; e++){
            final ChannelScheme article = new ChannelScheme();

            article.setCrawlUrl("");
            article.setTitle("테스트 메뉴 " + e);
            article.setId(0);
            article.setOrder(0);

            mAdapterMenu.mListData.add(article);
        }

        mAdapterMenu.notifyDataSetChanged();
    }

    private void openPopAd(){
        final Intent intent = new Intent(MainActivity.this, PopAdActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View v){
        Log.e(this.getClass().getSimpleName(), "onClick Called.");
        switch (v.getId()){
            case R.id.btn_menu_action:
                openDrawer();
                break;
            case R.id.top_fav: {
                closeDrawer();
                final Intent favorIntent = new Intent(MainActivity.this, FavorActivity.class);
                startActivity(favorIntent);
            }
                break;
            case R.id.sleepTimer: {
                closeDrawer();
                syncTimerState();
                final Intent timerIntent = new Intent(MainActivity.this, TimerActivity.class);
                startActivity(timerIntent);
            }
                break;
            default:
                break;
        }
    }

    private void syncTimerState(){
        sleepTimer.setChecked(false);
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
        mDrawerToggle.syncState();
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
        if ((keyCode == KeyEvent.KEYCODE_BACK)){
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
