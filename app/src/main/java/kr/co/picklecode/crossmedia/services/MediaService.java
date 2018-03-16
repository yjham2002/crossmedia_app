package kr.co.picklecode.crossmedia.services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import java.io.IOException;

/**
 * Created by HP on 2018-03-16.
 */
public class MediaService extends Service implements MediaPlayer.OnCompletionListener {
    MediaPlayer mediaPlayer;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource("https://www.youtube.com/watch?v=kulaHaxO2rk");
            mediaPlayer.prepare();
        }catch (IOException e){
            e.printStackTrace();
        }

        mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
        return START_STICKY;
    }

    public void onDestroy() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer.release();
    }

    public void onCompletion(MediaPlayer _mediaPlayer) {
        stopSelf();
    }

}
