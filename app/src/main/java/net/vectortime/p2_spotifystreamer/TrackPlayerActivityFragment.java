package net.vectortime.p2_spotifystreamer;

import android.app.Dialog;

import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.ComponentName;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.vectortime.p2_spotifystreamer.dataClasses.TrackInfo;
import net.vectortime.p2_spotifystreamer.database.MusicContract;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class TrackPlayerActivityFragment extends DialogFragment implements LoaderManager
        .LoaderCallbacks<Cursor>, MusicService.MusicCallback {
    private final String LOG_TAG = TrackPlayerActivityFragment.class.getSimpleName();

    private int mSongRank;
    private String mArtistId;
    private int mLastKnownPosition;

    private List<TrackInfo> mTracks;

    private static final int TRACK_PLAYER_LOADER = 0;

    private static final String[] PLAYER_COLUMNS = {
            MusicContract.TrackEntry.TABLE_NAME + "." + MusicContract.TrackEntry._ID,
            MusicContract.TrackEntry.COLUMN_ARTIST_ID,
            MusicContract.TrackEntry.COLUMN_ARTIST_NAME,
            MusicContract.TrackEntry.COLUMN_TRACK_ID,
            MusicContract.TrackEntry.COLUMN_TRACK_RANK,
            MusicContract.TrackEntry.COLUMN_TRACK_DURATION,
            MusicContract.TrackEntry.COLUMN_TRACK_NAME,
            MusicContract.TrackEntry.COLUMN_TRACK_PREVIEW,
            MusicContract.TrackEntry.COLUMN_ALBUM_ID,
            MusicContract.TrackEntry.COLUMN_ALBUM_NAME,
            MusicContract.TrackEntry.COLUMN_ALBUM_ART_LARGE,
            MusicContract.TrackEntry.COLUMN_ALBUM_ART_SMALL
    };
    static final int COL_TABLE_ID = 0;
    static final int COL_ARTIST_ID = 1;
    static final int COL_ARTIST_NAME = 2;
    static final int COL_TRACK_ID = 3;
    static final int COL_TRACK_RANK = 4;
    static final int COL_TRACK_DURATION = 5;
    static final int COL_TRACK_NAME = 6;
    static final int COL_TRACK_PREVIEW = 7;
    static final int COL_ALBUM_ID = 8;
    static final int COL_ALBUM_NAME = 9;
    static final int COL_ALBUM_LARGE = 10;
    static final int COL_ALBUM_SMALL = 11;

    static final String ARTIST_KEY = "artist_key";
    static final String RANK_KEY = "rank_key";
    static final String POSITION_KEY = "position_key";
    String PAUSE_STRING;
    String PLAY_STRING;

    TextView mArtistText;
    TextView mAlbumText;
    ImageView mAlbumArt;
    TextView mSongText;
    TextView mDuration;
    TextView mCurrentTime;
    SeekBar mSeekBar;
    ImageButton mPlaypause;

    MusicService mService;
    boolean mBound = false;

    public TrackPlayerActivityFragment() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(LOG_TAG, "onSaveInstanceState - saving position @ " + mLastKnownPosition + " & " +
                "rank @ " + mSongRank);
        outState.putInt(POSITION_KEY, mLastKnownPosition);
        outState.putInt(RANK_KEY, mSongRank);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(LOG_TAG, "In onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_track_player, container, false);

        mArtistText = (TextView) rootView.findViewById(R.id.track_play_artist);
        mAlbumText = (TextView) rootView.findViewById(R.id.track_play_album);
        mAlbumArt = (ImageView) rootView.findViewById(R.id.track_play_album_art);
        mSongText = (TextView) rootView.findViewById(R.id.track_play_track_title);
        mDuration = (TextView) rootView.findViewById(R.id.track_play_total_time);
        mCurrentTime = (TextView) rootView.findViewById(R.id.track_play_current_time);
        mSeekBar = (SeekBar) rootView.findViewById(R.id.track_play_seekBar);

        PAUSE_STRING = getString(R.string.track_player_pause);
        PLAY_STRING = getString(R.string.track_player_play);

        ImageButton prev = (ImageButton) rootView.findViewById(R.id.track_play_button_previous);
        ImageButton next = (ImageButton) rootView.findViewById(R.id.track_play_button_next);
        mPlaypause = (ImageButton) rootView.findViewById(R.id.track_play_button_playpause);

        prev.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (mService != null) mService.changeSong(v);
                else Log.w(LOG_TAG, "Previous clicked - Service not started yet");
            }
        });

        next.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (mService != null) mService.changeSong(v);
                else Log.w(LOG_TAG, "Next clicked - Service not started yet");
            }
        });

        mPlaypause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mService != null) {
                    mService.playPause(v);
                    if (mPlaypause.getContentDescription().equals(PLAY_STRING)) {
                        mPlaypause.setContentDescription(PAUSE_STRING);
                        mPlaypause.setImageResource(getResources().getIdentifier
                                ("@android:drawable/ic_media_pause", null, null));
                    } else {
                        mPlaypause.setContentDescription(PLAY_STRING);
                        mPlaypause.setImageResource(getResources().getIdentifier
                                ("@android:drawable/ic_media_play", null, null));
                    }
                }
            }
        });

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mService != null && fromUser) mService.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        if (savedInstanceState == null || !savedInstanceState.containsKey(POSITION_KEY)) {
            Log.d(LOG_TAG, "onCreateView - no previous last known position");
            mLastKnownPosition = 0;
        } else {
            mLastKnownPosition = savedInstanceState.getInt(POSITION_KEY);
            Log.d(LOG_TAG, "onCreateView - restoring last known position: "+ mLastKnownPosition);
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(RANK_KEY)) {
            mSongRank = savedInstanceState.getInt(RANK_KEY);
            Log.d(LOG_TAG, "onCreateView - restoring last known rank: "+ mSongRank);
        }
        return rootView;
    }

    @Override
    public void onDestroyView() {
        if (mService != null)
            getActivity().unbindService(mConnection);

        super.onDestroyView();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "In onCreateDialog");
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    private void populateFields(TrackInfo info) {
        mArtistText.setText(info.artistTitle);
        mAlbumText.setText(info.albumTitle);
        mSongText.setText(info.songTitle);
        long time = info.songDuration;
        mSeekBar.setMax((int) time);
        if (mLastKnownPosition != 0)
            mSeekBar.setProgress(mLastKnownPosition);
        mDuration.setText(formatTime(time));
        mCurrentTime.setText(formatTime(0));
        mPlaypause.setContentDescription(PAUSE_STRING);
        mPlaypause.setImageResource(getResources().getIdentifier
                ("@android:drawable/ic_media_pause", null, null));
        Picasso.with(getActivity()).load(info.getLargestImage()).into(mAlbumArt);
    }

    private String formatTime (long time){
        long minutes = (time/1000) / 60;
        long seconds = (time/1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(TRACK_PLAYER_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "In onCreateLoader");
        Intent intent = getActivity().getIntent();
        Bundle arguments = getArguments();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            mArtistId = intent.getStringExtra(Intent.EXTRA_TEXT);

            if (intent.hasExtra(Intent.EXTRA_UID)) {
                mSongRank = intent.getIntExtra(Intent.EXTRA_UID, 0);
            }

//            TextView tv = (TextView) rootView.findViewById(R.id.track_play_artist);
//            tv.setText(mArtistId);
//            TextView tv2 = (TextView) rootView.findViewById(R.id.track_play_current_time);
//            tv2.setText(Integer.toString(mSongRank));
        } else if (arguments != null && arguments.containsKey(ARTIST_KEY)) {
            mArtistId = arguments.getString(ARTIST_KEY);
            mSongRank = arguments.getInt(RANK_KEY);
        }
        Log.v(LOG_TAG, "In onCreateLoader artist: " + mArtistId + " rank: " + mSongRank);
        Uri myUri = MusicContract.TrackEntry.buildTrackByArtist(mArtistId);
        return new CursorLoader(getActivity(),myUri,PLAYER_COLUMNS,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cur) {
        Log.v(LOG_TAG, "In onLoadFinished - mSongRank = " + mSongRank);
        cur.moveToFirst();
        mTracks = new ArrayList<TrackInfo>();
        ArrayList<String> urls = new ArrayList<>();
        String startingUrl = "";
        do {
            kaaes.spotify.webapi.android.models.Image smallImage = new kaaes.spotify.webapi.android
                    .models.Image();
            smallImage.url = cur.getString(COL_ALBUM_SMALL);
            smallImage.height=1; smallImage.width=1;

            kaaes.spotify.webapi.android.models.Image largeImage = new kaaes.spotify.webapi.android
                    .models.Image();
            largeImage.url = cur.getString(COL_ALBUM_LARGE);
            largeImage.height=100; largeImage.width=100;

            ArrayList<kaaes.spotify.webapi.android.models.Image> images = new ArrayList<>();
            images.add(smallImage);
            images.add(largeImage);

            TrackInfo info = new TrackInfo(cur.getString(COL_TRACK_ID), cur.getString
                    (COL_TRACK_NAME),cur.getString(COL_TRACK_PREVIEW), cur.getString(COL_ALBUM_ID),
                    images, cur.getString(COL_ALBUM_NAME), 30000, cur.getInt(COL_TRACK_RANK), cur
                    .getString(COL_ARTIST_ID), cur.getString(COL_ARTIST_NAME));
            mTracks.add(info);
            urls.add(info.songPreview);
//            for (int i = 0; i < cur.getColumnCount(); i++){
//                Log.i(LOG_TAG, i+": "+cur.getString(i));
//            }

            if (mSongRank == info.songRank) {
                populateFields(info);
                startingUrl = info.songPreview;
            }

        } while (cur.moveToNext());

        Log.d(LOG_TAG, "Starting service");
        Intent sendIntent = new Intent(getActivity(), MusicService.class);
        sendIntent.putExtra(MusicService.TRACK_URL, startingUrl);
        sendIntent.putStringArrayListExtra(MusicService.URLS, urls);
        sendIntent.putExtra(MusicService.TRACK_POSTION, mLastKnownPosition);
        sendIntent.putExtra(MusicService.MESSENGER, new Messenger(new HandlerClass(this)));
        getActivity().startService(sendIntent);
        getActivity().bindService(sendIntent, mConnection, getActivity().BIND_AUTO_CREATE);
    }

    private static class HandlerClass extends Handler{
        private final WeakReference<TrackPlayerActivityFragment> mTarget;

        public HandlerClass(TrackPlayerActivityFragment target){
            mTarget = new WeakReference<TrackPlayerActivityFragment>(target);
        }

        @Override
        public void handleMessage(Message msg) {
//            Log.d("handler", "Received message!");
            Bundle info = msg.getData();
            TrackPlayerActivityFragment target = mTarget.get();
            if (info != null && target!= null){
                if(info.containsKey(MusicService.TRACK_POSTION))
                    target.updateSeekBar(info.getInt(MusicService.TRACK_POSTION));
                else {
                    Log.d("handler", "Received message - not a position update though...");
                }

                if(info.containsKey(MusicService.TRACK_SELECTION))
                    target.changeSongInfo(info.getString(MusicService.TRACK_SELECTION));
            }
            super.handleMessage(msg);
        }
    }

//     private static Handler handler = new Handler(){
//        private final WeakReference<TrackPlayerActivityFragment> mTarget;
//
//        public Handler(TrackPlayerActivityFragment target){
//            mTarget = new WeakReference<TrackPlayerActivityFragment>(target);
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
////            Log.d("handler", "Received message!");
//            Bundle info = msg.getData();
//            TrackPlayerActivityFragment target = mTarget.get();
//            if (info != null && target!= null){
//                if(info.containsKey(MusicService.TRACK_POSTION))
//                    target.updateSeekBar(info.getInt(MusicService.TRACK_POSTION));
//                else {
//                    Log.d("handler", "Received message - not a position update though...");
//                }
//
//                if(info.containsKey(MusicService.TRACK_SELECTION))
//                    target.changeSongInfo(info.getString(MusicService.TRACK_SELECTION));
//            }
//            super.handleMessage(msg);
//        }
//    };

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {    }

    @Override
    public void updateSeekBar(int currentTime) {
        mSeekBar.setProgress(currentTime);
        mCurrentTime.setText(formatTime(currentTime));
        mLastKnownPosition = currentTime;
    }

    @Override
    public void changeSongInfo(String url) {
        Log.d(LOG_TAG, "changeSongInfo to " + url);

        mLastKnownPosition = 0;
        mSeekBar.setProgress(0);
        if (url.equals("")) {
            mCurrentTime.setText(formatTime(0));
            mPlaypause.setContentDescription(PLAY_STRING);
            if (isAdded())
                mPlaypause.setImageResource(getResources().getIdentifier
                    ("@android:drawable/ic_media_play", null, null));
            return;
        }

        for (int i = 0; i < mTracks.size(); i++){
            if (mTracks.get(i).songPreview.equals(url)) {
                populateFields(mTracks.get(i));
                mSongRank = mTracks.get(i).songRank;
                break;
            }
        }
    }

    // http://developer.android.com/guide/components/bound-services.html
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}
