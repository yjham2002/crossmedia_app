package bases.utils;

import android.content.Context;
import android.content.Intent;

import java.util.Date;

import kr.co.picklecode.crossmedia.UISyncManager;

/**
 * Created by HP on 2018-04-06.
 */

public class PhoneCallDetector extends PhonecallReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    private boolean isAllowedToBeLaunched(){
        return
                UISyncManager.getInstance().getService() != null
                && UISyncManager.getInstance().getService().getNowPlayingMusic() != null
                && UISyncManager.getInstance().getService().getNowPlayingMusic().getParent() != null;
    }

    @Override
    protected void onIncomingCallStarted(String number, Date start) {
        if(isAllowedToBeLaunched()) UISyncManager.getInstance().getService().stopMedia();
    }

    @Override
    protected void onOutgoingCallStarted(String number, Date start) {
        if(isAllowedToBeLaunched()) UISyncManager.getInstance().getService().stopMedia();
    }

    @Override
    protected void onIncomingCallEnded(String number, Date start, Date end) {
        if(isAllowedToBeLaunched()) UISyncManager.getInstance().getService().resume();
    }

    @Override
    protected void onOutgoingCallEnded(String number, Date start, Date end) {
        if(isAllowedToBeLaunched()) UISyncManager.getInstance().getService().resume();
    }

    @Override
    protected void onMissedCall(String number, Date start) {
    }

}
