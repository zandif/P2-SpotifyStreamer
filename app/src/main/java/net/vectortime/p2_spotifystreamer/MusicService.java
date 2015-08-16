package net.vectortime.p2_spotifystreamer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Kevin on 8/15/2015.
 */
public class MusicService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private static final String LOG_TAG = MusicService.class.getSimpleName();

    public static final String TRACK_URL = "TRACK_URL";
    public static final String URLS = "URLS";;
    public static final String MESSENGER = "MESSENGER";
    public static final String TRACK_POSITION = "TRACK_POS";
    public static final String TRACK_SELECTION = "TRACK_SEL";

    private final IBinder mBinder = new LocalBinder();

    private MediaPlayer mediaPlayer;
    private Messenger mMessenger;
    private int startingPosition;

    private String url;
    private ArrayList<String> urls;

    private final Runnable mUpdateProgressTask = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };

    private void updateProgress() {
        if (mediaPlayer != null) {
//            Log.d(LOG_TAG, "updateProgress - " + mediaPlayer.getCurrentPosition());

            try {
                Message msg = Message.obtain();
                Bundle bundle  = new Bundle();
                bundle.putInt(TRACK_POSITION, mediaPlayer.getCurrentPosition());
                msg.setData(bundle);
                mMessenger.send(msg);
            }
            catch (android.os.RemoteException e1) {
                Log.w(LOG_TAG, "Exception sending message", e1);
            }

            new Handler().postDelayed(mUpdateProgressTask, 100);
        }
    }

    public class LocalBinder extends Binder {
        MusicService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MusicService.this;
        }
    }

    public MusicService() {}

    @Override
    public void onCompletion(MediaPlayer mp) {
//        Log.d(LOG_TAG, "onCompletion");
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        unloadTrack();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
//        Log.d(LOG_TAG, "onError");
        mp.reset();
        unloadTrack();
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.d(LOG_TAG, "onStartCommand " + flags + " " + startId);

        if (intent != null) {
//            Log.d(LOG_TAG, "onStartCommand - intent not null");
            url = intent.getStringExtra(TRACK_URL);
            urls = intent.getStringArrayListExtra(URLS);
            startingPosition = intent.getIntExtra(TRACK_POSITION,0);
            mMessenger = (Messenger) intent.getExtras().get(MESSENGER);
//            Log.d(LOG_TAG, "onHandleIntent - URLS found: "+ urls.size() + "(" + url + ") and " +
//                    "starting at "+ startingPosition);
            init();
            mediaPlayer.prepareAsync(); // prepare async to not block main thread
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
//        Log.d(LOG_TAG, "onPrepared");
        if (startingPosition != 0) {
//            Log.d(LOG_TAG, "onPrepared - starting at " + startingPosition);
            seekTo(startingPosition);
            startingPosition = 0;
        }
        mp.start();
        mUpdateProgressTask.run();
    }

    @Override
    public void onDestroy() {
//        Log.d(LOG_TAG, "onDestroy");
        if (mediaPlayer != null) mediaPlayer.release();
        super.onDestroy();
    }

    @Override
    public void onCreate() {
//        Log.d(LOG_TAG, "onCreate");
        super.onCreate();
    }

    private void init() {
//        Log.d(LOG_TAG, "init");
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnCompletionListener(this);

            if (url != null) {
                try {
//                    Log.d(LOG_TAG, "init - set url to " + url);
                    mediaPlayer.setDataSource(url);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            mediaPlayer.reset();

            if (url != null) {
                try {
//                    Log.d(LOG_TAG, "init - set url to " + url);
                    mediaPlayer.setDataSource(url);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void seekTo(int progress) {
        if (mediaPlayer != null)
            mediaPlayer.seekTo(progress);
    }

    public void playPause(View view) {
        ImageButton button = (ImageButton) view;
        String buttonText = (String) button.getContentDescription();
//        Log.i(LOG_TAG, "Button: " + buttonText);

        if (buttonText.equals(getString(R.string.track_player_play)) && mediaPlayer != null) {
            // Track is paused
            mediaPlayer.start();
        } else if (buttonText.equals(getString(R.string.track_player_play))) {
            // Track finished and mediaplayer was released
            init();
            mediaPlayer.prepareAsync();
        } else {
            // Track is playing
            mediaPlayer.pause();
        }
    }

    public void changeSong(View view) {
        ImageButton button = (ImageButton) view;
        String buttonText = (String) button.getContentDescription();

//        Log.i(LOG_TAG, "Button: " + buttonText);

        String previous = urls.get(urls.size()-1);
        String next = "";
        for (int i = 0; i < urls.size(); i++) {
            if (urls.get(i).equals(url)) {
                if (i != urls.size()-1) next = urls.get(i+1);
                else next = urls.get(0);
                break;
            }
            previous = urls.get(i);
        }

        String changeToTrack = next;
        if (buttonText.equals(getString(R.string.track_player_previous))) {
            changeToTrack = previous;
        }

        loadTrack(changeToTrack);
    }

    private void loadTrack(String inUrl) {
        url = inUrl;
//        Log.d(LOG_TAG, "loadTrack - New track: " + url);

        init();

        try {
            Message msg = Message.obtain();
            Bundle newSong = new Bundle();
            newSong.putString(TRACK_SELECTION, url);
            msg.setData(newSong);
            mMessenger.send(msg);;
        }
        catch (android.os.RemoteException e1) {
            Log.w(LOG_TAG, "Exception sending message", e1);
        }

        mediaPlayer.prepareAsync();
    }

    private void unloadTrack() {
        try {
            Message msg = Message.obtain();
            Bundle newSong = new Bundle();
            newSong.putString(TRACK_SELECTION, "");
            msg.setData(newSong);
            mMessenger.send(msg);;
        }
        catch (android.os.RemoteException e1) {
            Log.w(LOG_TAG, "Exception sending message", e1);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
//        Log.d(LOG_TAG, "onBind");
        return mBinder;
    }
}
