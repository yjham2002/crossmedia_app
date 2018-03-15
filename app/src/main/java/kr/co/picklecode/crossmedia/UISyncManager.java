package kr.co.picklecode.crossmedia;

import android.app.Activity;
import android.widget.TextView;
import android.widget.ToggleButton;

import bases.Constants;
import kr.co.picklecode.crossmedia.models.ChannelScheme;
import utils.PreferenceUtil;

/**
 * Created by HP on 2018-03-15.
 */

public class UISyncManager {

    private static UISyncManager instance;
    private ChannelScheme channelScheme;

    public static UISyncManager getInstance(){
        if(instance == null) instance = new UISyncManager();
        return instance;
    }

    public void setChannelScheme(ChannelScheme channelScheme) {
        this.channelScheme = channelScheme;
    }

    public boolean isSchemeLoaded() {
        return channelScheme != null;
    }

    private int getRandomCount(){
        //TODO
        return channelScheme.getCg_max();
    }

    private void updateThisChannelScheme(int newValue){
        channelScheme.setCg_cur(newValue);
        //TODO
    }

    public void syncCurrentText(Activity activity, int id){
        if(activity.findViewById(id) != null){
            if(activity.findViewById(id) instanceof TextView){
                final int newVal = getRandomCount();
                ((TextView)activity.findViewById(id)).setText(newVal + "");
                updateThisChannelScheme(newVal);
            }
        }
    }

    public void syncTimerSet(Activity activity, int id){
        if(activity.findViewById(id) != null){
            if(activity.findViewById(id) instanceof ToggleButton){
                ((ToggleButton)activity.findViewById(id)).setChecked(PreferenceUtil.getBoolean(Constants.PREFERENCE.IS_ALARM_SET, false));
            }
        }
    }

}
