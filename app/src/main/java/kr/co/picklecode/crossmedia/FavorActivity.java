package kr.co.picklecode.crossmedia;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.NativeExpressAdView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import bases.BaseActivity;
import kr.co.picklecode.crossmedia.models.Article;

public class FavorActivity extends BaseActivity {

    private ImageView btn_back;

    private RecyclerView mRecyclerView;
    private ArticleAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

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
        if(AudienceSync.getInstance().isSchemeLoaded()){
            AudienceSync.getInstance().syncCurrentText(this, R.id.cg_current_id);
        }

        refreshAd(true, false);

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

        setClick(btn_back, arrowDown);

        loadList();
    }

    private void loadList(){
        mAdapter.mListData.clear();

        for(int e = 0; e < 20; e++){
            final Article article = new Article();

            article.setImgPath("http://lorempixel.com/50/50");
            article.setTitle("테스트 채널 " + e);
            article.setContent("설명이 삽입될 위치입니다.");

            mAdapter.mListData.add(article);
        }

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
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
    }

    @Override
    public void onPause() {
        super.onPause();
    }

}
