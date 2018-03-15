package kr.co.picklecode.crossmedia;

import android.app.Activity;
import android.widget.TextView;

import java.util.Random;

import kr.co.picklecode.crossmedia.models.ChannelScheme;

/**
 * Created by HP on 2018-03-15.
 */

public class AudienceSync {

    private static AudienceSync instance;
    private ChannelScheme channelScheme;

    public static AudienceSync getInstance(){
        if(instance == null) instance = new AudienceSync();
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

}
