package kr.co.picklecode.crossmedia;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.NativeExpressAdView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.Date;

import bases.BaseActivity;
import bases.utils.AlarmBroadcastReceiver;
import kr.co.picklecode.crossmedia.models.TimerItem;

public class TimerActivity extends BaseActivity {

    private ImageView btn_back;

    private RecyclerView mRecyclerView;
    private TimerAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private TextView caption;

    private AdView mAdView;

    /**
     * Slider
     */
    private ControllableSlidingLayout controllableSlidingLayout;
    private View slideAnchor;
    private ImageView arrowDown;

    /**
     * Broadcast Receiver Test
     */
    private BroadcastReceiver mTimeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(this.getClass().getSimpleName(), "Alarm Received at [" + new Date() + "]");
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mTimeReceiver,new IntentFilter(AlarmBroadcastReceiver.IntentConstants.INTENT_FILTER));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mTimeReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

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

        caption = findViewById(R.id.sleepTimerCaption);
        btn_back = findViewById(R.id.btn_back_action);

        mRecyclerView = findViewById(R.id.recyclerView);
        mAdapter = new TimerAdapter(this, R.layout.layout_timer);
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

        mAdapter.mListData.add(new TimerItem(15, "15 Mins", false));
        mAdapter.mListData.add(new TimerItem(30, "30 Mins", false));
        mAdapter.mListData.add(new TimerItem(45, "45 Mins", false));
        mAdapter.mListData.add(new TimerItem(60 * 1, "1 Hour", false));
        mAdapter.mListData.add(new TimerItem(60 * 2, "2 Hours", false));
        mAdapter.mListData.add(new TimerItem(60 * 3, "3 Hours", false));
        mAdapter.mListData.add(new TimerItem(60 * 4, "4 Hours", false));
        mAdapter.mListData.add(new TimerItem(60 * 5, "5 Hours", false));
        mAdapter.mListData.add(new TimerItem(0, "Cancel", true));

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

}
