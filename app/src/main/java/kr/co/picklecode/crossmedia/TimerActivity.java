package kr.co.picklecode.crossmedia;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import bases.BaseActivity;
import kr.co.picklecode.crossmedia.models.TimerItem;

public class TimerActivity extends BaseActivity {

    private ImageView btn_back;

    private RecyclerView mRecyclerView;
    private TimerAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    /**
     * Slider
     */
    private ControllableSlidingLayout controllableSlidingLayout;
    private View slideAnchor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        initView();
    }

    private void initView(){
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

        setClick(btn_back);

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
            case R.id.btn_back_action:
                finish();
                break;
            default: break;
        }
    }

}
