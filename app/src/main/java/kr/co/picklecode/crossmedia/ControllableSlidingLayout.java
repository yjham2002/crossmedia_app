package kr.co.picklecode.crossmedia;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

/**
 * Created by HP on 2018-03-11.
 */

public class ControllableSlidingLayout extends SlidingUpPanelLayout {

    private View anchor = null;
    private boolean mClickToCollapse = true;

    public ControllableSlidingLayout(Context context) {
        super(context);
    }

    public ControllableSlidingLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ControllableSlidingLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setClickToCollapseEnabled(boolean enabled){
        mClickToCollapse = enabled;
    }

    public void setActionWhenExpanded(boolean enabled, SlidingUpPanelLayout.PanelSlideListener callback){
        this.addPanelSlideListener(callback);
    }

    @Override
    public void setDragView(View dragView) {
        super.setDragView(dragView);
        if (dragView != null) {
            dragView.setClickable(true);
            dragView.setFocusable(false);
            dragView.setFocusableInTouchMode(false);
            dragView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isEnabled() || !isTouchEnabled()) return;
                    if (getPanelState() != PanelState.EXPANDED && getPanelState() != PanelState.ANCHORED) {
                        if (getAnchorPoint() < 1.0f) {
                            setPanelState(PanelState.ANCHORED);
                        } else {
                            setPanelState(PanelState.EXPANDED);
                        }
                    } else if (mClickToCollapse){
                        setPanelState(PanelState.COLLAPSED);
                    }
                }
            });
        }
    }
}
