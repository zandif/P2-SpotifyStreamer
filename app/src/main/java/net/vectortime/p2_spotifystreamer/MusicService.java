package net.vectortime.p2_spotifystreamer;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.PlaybackState;
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

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/**
 * Created by Kevin on 8/15/2015.
 */
public class MusicService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private static final String LOG_TAG = MusicService.class.getSimpleName();
    private static final String ACTION_PLAY = "net.vectortime.p2_spotifystreamer.PLAY";
    public static final String CMD_NAME = "CMD_NAME";
    public static final String CMD_PAUSE = "CMD_PAUSE";
    public static final String TRACK_URL = "TRACK_URL";
    public static final String URLS = "URLS";;
    public static final String MESSENGER = "MESSENGER";
    public static final String TRACK_POSTION = "TRACK_POS";
    public static final String TRACK_SELECTION = "TRACK_SEL";

    private final IBinder mBinder = new LocalBinder();

    MediaPlayer mediaPlayer;
    MusicCallback mCallback;
    Messenger mMessenger;
//    String url = "https://p.scdn.co/mp3-preview/889f0af9e390ff0c1c17eafa6eaa7f41409a0016";
    String url;
    ArrayList<String> urls;

    private final Runnable mUpdateProgressTask = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };

    private void updateProgress() {
        if (mediaPlayer != null) {
//            mCallback.updateSeekBar(mediaPlayer.getCurrentPosition());
//            Log.d(LOG_TAG, "updateProgress - " + mediaPlayer.getCurrentPosition());

            try {
                Message msg = Message.obtain();
                Bundle bundle  = new Bundle();
                bundle.putInt(TRACK_POSTION, mediaPlayer.getCurrentPosition());
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

//    public MusicService() {
//        super("Music");
//    }

    public MusicService() {}

//    /**
//     * Creates an IntentService.  Invoked by your subclass's constructor.
//     *
//     * @param name Used to name the worker thread, important only for debugging.
//     */
//    public MusicService(String name) {
//        super(name);
//    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(LOG_TAG, "onCompletion");
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.d(LOG_TAG, "onError");
        mp.reset();
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand " + flags + " " + startId);

        if (intent != null) {
            String action = intent.getAction();
            String command = intent.getStringExtra(CMD_NAME);
            if (ACTION_PLAY.equals(action)) {
                if (CMD_PAUSE.equals(command)) {
//                    if (mPlayback != null && mPlayback.isPlaying()) {
//                        handlePauseRequest();
//                    }
//                } else if (CMD_STOP_CASTING.equals(command)) {
//                    mCastManager.disconnect();
                }
            }

            Log.d(LOG_TAG, "onStartCommand - intent not null");
            url = intent.getStringExtra(TRACK_URL);
            urls = intent.getStringArrayListExtra(URLS);
            mMessenger = (Messenger) intent.getExtras().get(MESSENGER);
            Log.d(LOG_TAG, "onHandleIntent - URLS found: "+ urls.size());
            init();
            mediaPlayer.prepareAsync(); // prepare async to not block main thread
        }

        if (intent != null &&
                intent.getAction() != null &&
                intent.getAction().equals(ACTION_PLAY)) {
            //init();
            //mediaPlayer.prepareAsync();

        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(LOG_TAG, "onPrepared");
        mp.start();
        mUpdateProgressTask.run();
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        if (mediaPlayer != null) mediaPlayer.release();
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate");
        super.onCreate();
    }

    private void init() {
        Log.d(LOG_TAG, "init");
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnCompletionListener(this);

            if (url != null) {
                try {
                    Log.d(LOG_TAG, "init - set url to " + url);
                    mediaPlayer.setDataSource(url);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setUrl(String inUrl) {
        Log.d(LOG_TAG, "setUrl: "+ inUrl);
        url = inUrl;
    }

    public void changeSong(View view) {
        ImageButton button = (ImageButton) view;
        String buttonText = (String) button.getContentDescription();

        Log.i(LOG_TAG, "Button: " + buttonText);

        String previous = urls.get(urls.size()-1);
        String next = "";
        for (int i = 0; i < urls.size(); i++) {
            if (urls.get(i).equals(url)) {
                if (i != urls.size()-1) next = urls.get(i+1);
                else next = urls.get(0);
                break;
            }
            previous = urls.get(urls.size()-1);
        }

        String changeToTrack = next;

        if (buttonText.equals(R.string.track_player_previous)) {
            changeToTrack = previous;
        }

        loadTrack(url);
    }

    private void loadTrack(String url) {
        if (mediaPlayer == null) init();
        mediaPlayer.reset();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        return mBinder;
    }

    public void setCallback(MusicCallback callback) {
        mCallback = callback;
    }

    public interface MusicCallback {
        void updateSeekBar(int currentTime);
        void changeSongInfo(String url);
    }
}
