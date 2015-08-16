package net.vectortime.p2_spotifystreamer;

import android.app.Dialog;

import android.app.DialogFragment;
import android.app.Fragment;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.vectortime.p2_spotifystreamer.dataClasses.TrackInfo;
import net.vectortime.p2_spotifystreamer.database.MusicContract;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * A placeholder fragment containing a simple view.
 */
public class TrackPlayerActivityFragment extends DialogFragment implements LoaderManager
        .LoaderCallbacks<Cursor>, MusicService.MusicCallback {
    private final String LOG_TAG = TrackPlayerActivityFragment.class.getSimpleName();

    private int mSongRank;
    private String mArtistId;

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

    TextView mArtistText;
    TextView mAlbumText;
    ImageView mAlbumArt;
    TextView mSongText;
    TextView mDuration;
    TextView mCurrentTime;
    SeekBar mSeekBar;

    MusicService mService;
    boolean mBound = false;

    public TrackPlayerActivityFragment() {
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

        ImageButton prev = (ImageButton) rootView.findViewById(R.id.track_play_button_previous);
        ImageButton next = (ImageButton) rootView.findViewById(R.id.track_play_button_next);

        next.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (mService != null) {
                    mService.changeSong(v);
                    Log.d(LOG_TAG, "Previous clicked - Previous onClickListener is set");
                }
                else Log.w(LOG_TAG, "Previous clicked - Service not started yet");
            }
        });

        prev.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (mService != null) mService.changeSong(v);
                else Log.w(LOG_TAG, "Next clicked - Service not started yet");
            }
        });

        return rootView;
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
        mSeekBar.setMax((int)time);
        mDuration.setText(formatTime(time));
        mCurrentTime.setText(formatTime(0));
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
        Uri myUri = MusicContract.TrackEntry.buildTrackByArtist(mArtistId);
        return new CursorLoader(getActivity(),myUri,PLAYER_COLUMNS,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cur) {
        Log.v(LOG_TAG, "In onLoadFinished");
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
        sendIntent.putExtra(MusicService.MESSENGER, new Messenger(handler));
        getActivity().startService(sendIntent);
        getActivity().bindService(sendIntent, mConnection, getActivity().BIND_AUTO_CREATE);
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
//            Log.d("handler", "Received message!");
            Bundle info = msg.getData();
            if (info != null){
                if(info.containsKey(MusicService.TRACK_POSTION))
                    updateSeekBar(info.getInt(MusicService.TRACK_POSTION));

                if(info.containsKey(MusicService.TRACK_SELECTION))
                    changeSongInfo(info.getString(MusicService.TRACK_SELECTION));
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {    }

    @Override
    public void updateSeekBar(int currentTime) {
        mSeekBar.setProgress(currentTime);
        mCurrentTime.setText(formatTime(currentTime));
    }

    @Override
    public void changeSongInfo(String url) {
        for (int i = 0; i < mTracks.size(); i++){
            if (mTracks.get(i).songPreview == url) {
                populateFields(mTracks.get(i));
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
