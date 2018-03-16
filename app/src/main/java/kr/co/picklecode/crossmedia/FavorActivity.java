package kr.co.picklecode.crossmedia;

import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.List;

import bases.BaseActivity;
import kr.co.picklecode.crossmedia.models.Article;

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

    /**
     * Slider
     */
    private ControllableSlidingLayout controllableSlidingLayout;
    private View slideAnchor;
    private ImageView arrowDown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favor);

        initView();
    }

    private void initView(){
        UISyncManager.getInstance().syncTimerSet(this, R.id.playing_timer);

        playingTimer = findViewById(R.id.playing_timer);

        refreshAd(true, false);

        _topBtn = findViewById(R.id.top_btn);

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("580AF1AB9D6734064E03DF3C086DB1B2")
                .build();
        mAdView.loadAd(adRequest);

        btn_back = findViewById(R.id.btn_back_action);

        mRecyclerView = findViewById(R.id.recyclerView);
        mAdapter = new ArticleAdapter(this, R.layout.layout_article);
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
            }
        });

        setClick(btn_back, arrowDown, _topBtn, playingTimer);

        loadList();
    }

    private void loadList(){
        mAdapter.mListData.clear();

        List<Article> articles = FavorSQLManager.getInstance(this).getResultOrderBy(null);

        for(int e = 0; e < articles.size(); e++){
            final Article article = articles.get(e);

            mAdapter.mListData.add(article);
        }

        mAdapter.dataChange();
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.playing_timer: {
                UISyncManager.getInstance().syncTimerSet(this, R.id.sleepTimer);
                UISyncManager.getInstance().syncTimerSet(this, R.id.playing_timer);
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

        if(UISyncManager.getInstance().isSchemeLoaded()){
            UISyncManager.getInstance().syncCurrentText(this, R.id.cg_current_id);
        }

        UISyncManager.getInstance().syncTimerSet(this, R.id.playing_timer);
    }

    @Override
    public void onPause() {
        super.onPause();
        UISyncManager.getInstance().stopSyncText();
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
