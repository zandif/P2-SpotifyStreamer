package net.vectortime.p2_spotifystreamer;

import android.app.Fragment;
import android.app.Service;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.vectortime.p2_spotifystreamer.dataClasses.TrackInfo;
import net.vectortime.p2_spotifystreamer.database.MusicContract;
import net.vectortime.p2_spotifystreamer.database.MusicProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * A placeholder fragment containing a simple view.
 */
public class TrackPlayerActivityFragment extends Fragment {
    private final String LOG_TAG = TrackPlayerActivityFragment.class.getSimpleName();

    private int mSongRank;
    private String mArtistId;
    private List<TrackInfo> mTracks;

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

    public TrackPlayerActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_track_player, container, false);

        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            mArtistId = intent.getStringExtra(Intent.EXTRA_TEXT);

            if (intent.hasExtra(Intent.EXTRA_UID)) {
                mSongRank = intent.getIntExtra(Intent.EXTRA_UID, 0);
            }

            TextView tv = (TextView) rootView.findViewById(R.id.track_play_artist);
            tv.setText(mArtistId);
            TextView tv2 = (TextView) rootView.findViewById(R.id.track_play_current_time);
            tv2.setText(Integer.toString(mSongRank));

            getTracks(rootView);
        }

        return rootView;
    }

    private void getTracks(View rootView) {
//        Uri myUri = MusicContract.TrackEntry.buildTrackByArtistWithRank(mArtistId, mSongRank);
        Uri myUri = MusicContract.TrackEntry.buildTrackByArtist(mArtistId);
//            Cursor cur = new CursorLoader(getActivity(), myUri, PLAYER_COLUMNS, null, null, null);
        Log.i(LOG_TAG, "URI: "+myUri);
        Cursor cur = getActivity().getContentResolver().query(myUri, PLAYER_COLUMNS, null,
                null, null);
        cur.moveToFirst();
        mTracks = new ArrayList<TrackInfo>();
        do {
            // long inSongDuration, int inSongRank, String
            //inArtistId, String inArtistTitle
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
        } while (cur.moveToNext());
//        populateFields(rootView, cur);
        if (mTracks.size() > mSongRank)
            populateFields(rootView, mTracks.get(mSongRank));
        cur.close();
    }

    private void populateFields(View rootView, Cursor cursor) {
        TextView artist = (TextView) rootView.findViewById(R.id.track_play_artist);
        TextView album = (TextView) rootView.findViewById(R.id.track_play_album);
        ImageView albumArt = (ImageView) rootView.findViewById(R.id.track_play_album_art);
        TextView song = (TextView) rootView.findViewById(R.id.track_play_track_title);
        TextView duration = (TextView) rootView.findViewById(R.id.track_play_total_time);
        TextView currentTime = (TextView) rootView.findViewById(R.id.track_play_current_time);

        artist.setText(cursor.getString(COL_ARTIST_NAME));
        album.setText(cursor.getString(COL_ALBUM_NAME));
        song.setText(cursor.getString(COL_TRACK_NAME));
        long time = cursor.getLong(COL_TRACK_DURATION);
        long minutes = (time/1000) / 60;
        long seconds = (time/1000) % 60;
        String length = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(time),
                TimeUnit.MILLISECONDS.toSeconds(time) -
                        TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time)));
        length = String.format("%02d:%02d", minutes, seconds);
        duration.setText(length);
        Picasso.with(getActivity()).load(cursor.getString(COL_ALBUM_LARGE)).into(albumArt);
    }

    private void populateFields(View rootView, TrackInfo info) {
        TextView artist = (TextView) rootView.findViewById(R.id.track_play_artist);
        TextView album = (TextView) rootView.findViewById(R.id.track_play_album);
        ImageView albumArt = (ImageView) rootView.findViewById(R.id.track_play_album_art);
        TextView song = (TextView) rootView.findViewById(R.id.track_play_track_title);
        TextView duration = (TextView) rootView.findViewById(R.id.track_play_total_time);
        TextView currentTime = (TextView) rootView.findViewById(R.id.track_play_current_time);

        artist.setText(info.artistTitle);
        album.setText(info.albumTitle);
        song.setText(info.songTitle);
        long time = info.songDuration;
        long minutes = (time/1000) / 60;
        long seconds = (time/1000) % 60;
        String length = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(time),
                TimeUnit.MILLISECONDS.toSeconds(time) -
                        TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time)));
        length = String.format("%02d:%02d", minutes, seconds);
        duration.setText(length);
        Picasso.with(getActivity()).load(info.getLargestImage()).into(albumArt);
    }
}
