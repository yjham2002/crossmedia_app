package kr.co.picklecode.crossmedia;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Random;

import bases.Constants;
import kr.co.picklecode.crossmedia.models.ChannelScheme;
import kr.co.picklecode.crossmedia.services.MediaService;
import utils.PreferenceUtil;

/**
 * Created by HP on 2018-03-15.
 */

public class UISyncManager {

    private static UISyncManager instance;

    private ChannelScheme channelScheme;

    private MediaService mServer;
    private Activity context;
    private int currentText;

    public static UISyncManager getInstance(){
        if(instance == null) instance = new UISyncManager();
        return instance;
    }

    public void setService(MediaService mServer){
        this.mServer = mServer;
    }

    public MediaService getService(){
        return mServer;
    }

    public void setChannelScheme(ChannelScheme channelScheme) {
        this.channelScheme = channelScheme;
    }

    public ChannelScheme getChannelScheme() {
        return channelScheme;
    }

    public boolean isSchemeLoaded() {
        return channelScheme != null;
    }

    private int getRandomCount(boolean isInit){
        //TODO
        int diff = channelScheme.getCg_max() - channelScheme.getCg_min();
        int toRet;
        if(diff <= 0){
            toRet = channelScheme.getCg_min();
        }else{
            toRet = channelScheme.getCg_min() + new Random().nextInt(diff);
        }

        if(channelScheme.getCg_range() <= 0) return toRet;
        final int times = diff / channelScheme.getCg_range();
        if(times <= 0) return toRet;
        int toGo = new Random().nextInt(times + 1) * channelScheme.getCg_range();
        if(isInit) return toGo + channelScheme.getCg_min();
        final boolean isNegative = new Random().nextBoolean();

        if(!isNegative) toGo *= -1;

        if(channelScheme.getCg_cur() + toGo < channelScheme.getCg_min()){
            toGo *= -1;
        }

        if(channelScheme.getCg_cur() + toGo > channelScheme.getCg_max()){
            toGo *= -1;
        }

        return channelScheme.getCg_cur() + toGo;
    }

    private void updateThisChannelScheme(int newValue){
        channelScheme.setCg_cur(newValue);
        //TODO
    }

    private int syncInterval = 10000;
    private Handler repeatingSyncHandler = new Handler();
    private Runnable repeatingRunnable = new Runnable() {
        @Override
        public void run() {
            syncCurrentTextInternal(context, currentText, true);
            repeatingSyncHandler.postDelayed(repeatingRunnable, syncInterval);
        }
    };

    private boolean isInitialized = false;

    public void stopSyncText(){
        repeatingSyncHandler.removeCallbacks(repeatingRunnable);
    }

    /**
     * @author EuiJin.Ham
     * @description Detaching the handler to let the syncManager stop repeating and initializes the flag bit.
     * @caution must have to be used in an activity which has the sync target
     */
    public void resetInitialized(){
        this.isInitialized = false;
        stopSyncText();
    }

    /**
     * @author EuiJin.Ham
     * As this class has been designed to singleton,
     * must have to set the params below whenever calling entirely.
     * @param activity Context to use with the sync handler
     * @param id Resource View ID
     */
    public void syncCurrentText(Activity activity, int id){
        // TODO : Make this method compatible with any context-extended classes
        // Not on this Project
        this.context = activity;
        this.currentText = id;
        syncCurrentTextInternal(activity, id, !isInitialized);
        repeatingSyncHandler.postDelayed(repeatingRunnable, syncInterval); // Continuous Calling - SyncHandler
    }

    private void syncCurrentTextInternal(Activity activity, int id, boolean initialize){ // Internal Function Method
        if(activity.findViewById(id) != null){
            if(activity.findViewById(id) instanceof TextView){
                final int newVal = getRandomCount(initialize);
                if(initialize) updateThisChannelScheme(newVal);
                if(isSchemeLoaded()){
                    ((TextView)activity.findViewById(id)).setText(channelScheme.getCg_cur() + "");
                }
                isInitialized = true;
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
