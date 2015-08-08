package net.vectortime.p2_spotifystreamer;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
public class TrackPlayerActivityFragment extends Fragment implements LoaderManager
        .LoaderCallbacks<Cursor>{
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

    TextView mArtistText;
    TextView mAlbumText;
    ImageView mAlbumArt;
    TextView mSongText;
    TextView mDuration;
    TextView mCurrentTime;

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

        return rootView;
    }

    private void populateFields(TrackInfo info) {
        mArtistText.setText(info.artistTitle);
        mAlbumText.setText(info.albumTitle);
        mSongText.setText(info.songTitle);
        long time = info.songDuration;
        long minutes = (time/1000) / 60;
        long seconds = (time/1000) % 60;
        String length = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(time),
                TimeUnit.MILLISECONDS.toSeconds(time) -
                        TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time)));
        length = String.format("%02d:%02d", minutes, seconds);
        mDuration.setText(length);
        Picasso.with(getActivity()).load(info.getLargestImage()).into(mAlbumArt);
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
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            mArtistId = intent.getStringExtra(Intent.EXTRA_TEXT);

            if (intent.hasExtra(Intent.EXTRA_UID)) {
                mSongRank = intent.getIntExtra(Intent.EXTRA_UID, 0);
            }

//            TextView tv = (TextView) rootView.findViewById(R.id.track_play_artist);
//            tv.setText(mArtistId);
//            TextView tv2 = (TextView) rootView.findViewById(R.id.track_play_current_time);
//            tv2.setText(Integer.toString(mSongRank));
        }
        Uri myUri = MusicContract.TrackEntry.buildTrackByArtist(mArtistId);
        return new CursorLoader(getActivity(),myUri,PLAYER_COLUMNS,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cur) {
        Log.v(LOG_TAG, "In onLoadFinished");
        cur.moveToFirst();
        mTracks = new ArrayList<TrackInfo>();
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
                    images, cur.getString(COL_ALBUM_NAME), 30, cur.getInt(COL_TRACK_RANK), cur
                    .getString(COL_ARTIST_ID), cur.getString(COL_ARTIST_NAME));
            mTracks.add(info);
            for (int i = 0; i < cur.getColumnCount(); i++){
                Log.i(LOG_TAG, i+": "+cur.getString(i));
            }

            if (mSongRank == info.songRank)
                populateFields(info);

        } while (cur.moveToNext());

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {    }
}
