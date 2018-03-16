package kr.co.picklecode.crossmedia.observers;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import android.util.Log;

public class SettingsContentObserver extends ContentObserver {

    private int previousVolume;
    private Context context;
    private ObserverCallback observerCallback;

    public SettingsContentObserver(Context c, Handler handler, ObserverCallback callback) {
        super(handler);
        context = c;
        this.observerCallback = callback;

        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        previousVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    @Override
    public boolean deliverSelfNotifications() {
        return super.deliverSelfNotifications();
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);

        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);

        int delta=previousVolume-currentVolume;

        if(delta>0) {
            Log.d("Volume Changed", "Decreased");
            previousVolume=currentVolume;
            observerCallback.onNegativeCall(currentVolume);
        }
        else if(delta<0) {
            Log.d("Volume Changed","Increased");
            previousVolume=currentVolume;
            observerCallback.onPositiveCall(currentVolume);
        }
    }
}