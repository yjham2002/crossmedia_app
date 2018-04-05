package kr.co.picklecode.crossmedia;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import bases.Constants;
import bases.utils.AlarmUtils;
import kr.co.picklecode.crossmedia.models.Article;
import kr.co.picklecode.crossmedia.models.ChannelScheme;
import kr.co.picklecode.crossmedia.models.MediaRaw;
import kr.co.picklecode.crossmedia.services.MediaService;
import utils.PreferenceUtil;

/**
 * Created by HP on 2018-03-15.
 */

public class UISyncManager {

    private List<Article> chList = new ArrayList<>();
    private List<MediaRaw> songList = new ArrayList<>();

    public static final int INDEX_CANNOT_FIND = -1;
    public static final int INDEX_NEXT_CHANNEL = -2;

    public int getNextSongIndex(){
        if(songList.size() == 0){
            return INDEX_CANNOT_FIND;
        }

        int current = INDEX_CANNOT_FIND;
        for(int e = 0; e < songList.size(); e++){
            if(getService().getNowPlayingMusic().getCh_id() == songList.get(e).getCh_id()){
                current = e;
                break;
            }
        }
        if(current + 1 >= songList.size()){
            return INDEX_NEXT_CHANNEL;
        }else {
            if(current == INDEX_CANNOT_FIND){
                return 0;
            }else {
                return current + 1;
            }
        }
    }

    public int getNextChannelIndex(){
        if(chList.size() == 0){
            return INDEX_CANNOT_FIND;
        }

        int current = INDEX_CANNOT_FIND;
        for(int e = 0; e < chList.size(); e++){
            if(getService().getNowPlayingMusic().getParent().getId() == chList.get(e).getId()){
                current = e;
                break;
            }
        }
        if(current + 1 >= chList.size()){
            return 0;
        }else {
            if(current == INDEX_CANNOT_FIND){
                return 0;
            }else {
                return current + 1;
            }
        }
    }

    public List<Article> getChList() {
        return chList;
    }

    public void setChList(List<Article> chList) {
        this.chList = chList;
    }

    public List<MediaRaw> getSongList() {
        return songList;
    }

    public void setSongList(List<MediaRaw> songList) {
        this.songList = songList;
    }

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
        if(mServer == null || mServer.getNowPlayingMusic() == null) return 0;

        Article article = mServer.getNowPlayingMusic().getParent();

        if(isInit) article.setCg_current(article.getCg_min() + new Random().nextInt(article.getCg_range()));
        if(article.getCg_range() <= 0) return article.getCg_current();

        int toGo = new Random().nextInt(article.getCg_range() + 1);

        final boolean isNegative = new Random().nextBoolean();

        if(!isNegative) toGo *= -1;

        if(mServer.getNowPlayingMusic().getParent().getCg_current() + toGo < article.getCg_min()){
            toGo *= -1;
        }

        if(mServer.getNowPlayingMusic().getParent().getCg_current() + toGo > article.getCg_max()){
            toGo *= -1;
        }

        return mServer.getNowPlayingMusic().getParent().getCg_current() + toGo;
    }

    private void updateThisChannelScheme(int newValue){
        channelScheme.setCg_cur(newValue);
        if(mServer.getNowPlayingMusic().getParent() != null) mServer.getNowPlayingMusic().getParent().setCg_current(newValue);
    }

    private int syncInterval = 10000;
    private Handler repeatingSyncHandler = new Handler();
    private Runnable repeatingRunnable = new Runnable() {
        @Override
        public void run() {
            syncCurrentTextInternal(context, currentText, true);
            stopSyncText();
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
        syncCurrentTextInternal(activity, id, isInitialized);
        repeatingSyncHandler.postDelayed(repeatingRunnable, syncInterval); // Continuous Calling - SyncHandler
    }

    private void syncCurrentTextInternal(Activity activity, int id, boolean initialize){ // Internal Function Method
        if(activity.findViewById(id) != null){
            if(activity.findViewById(id) instanceof TextView){
                final int newVal = getRandomCount(initialize);
                if(initialize) updateThisChannelScheme(newVal);
                if(isSchemeLoaded()){
                    ((TextView)activity.findViewById(id)).setText(newVal + "");
                }
                isInitialized = true;
            }
        }
    }

    public void syncTimerSet(Activity activity, int id){
        if(activity.findViewById(id) != null){
            if(activity.findViewById(id) instanceof ToggleButton){
                boolean isAlarmSet = PreferenceUtil.getBoolean(Constants.PREFERENCE.IS_ALARM_SET, false) && AlarmUtils.getInstance().getCurrentSetAlarm(activity) != null;
                ((ToggleButton)activity.findViewById(id)).setChecked(isAlarmSet);
            }
        }
    }

}
