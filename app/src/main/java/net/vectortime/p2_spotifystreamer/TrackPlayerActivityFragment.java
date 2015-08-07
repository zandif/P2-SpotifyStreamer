package net.vectortime.p2_spotifystreamer;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.vectortime.p2_spotifystreamer.database.MusicContract;
import net.vectortime.p2_spotifystreamer.database.MusicProvider;

import java.util.concurrent.TimeUnit;


/**
 * A placeholder fragment containing a simple view.
 */
public class TrackPlayerActivityFragment extends Fragment {
    private final String LOG_TAG = TrackPlayerActivityFragment.class.getSimpleName();

    private int mSongRank;
    private String mArtistId;

    private static final String[] PLAYER_COLUMNS = {
            MusicContract.TrackEntry.TABLE_NAME + "." + MusicContract.TrackEntry._ID,
            MusicContract.TrackEntry.COLUMN_ARTIST_ID,
            MusicContract.TrackEntry.COLUMN_ARTIST_NAME,
            MusicContract.TrackEntry.COLUMN_TRACK_ID,
            MusicContract.TrackEntry.COLUMN_TRACK_RANK,
            MusicContract.TrackEntry.COLUMN_TRACK_DURATION,
            MusicContract.TrackEntry.COLUMN_TRACK_NAME,
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
    static final int COL_ALBUM_ID = 7;
    static final int COL_ALBUM_NAME = 8;
    static final int COL_ALBUM_LARGE = 9;
    static final int COL_ALBUM_SMALL = 10;

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

            Uri myUri = MusicContract.TrackEntry.buildTrackByArtistWithRank(mArtistId, mSongRank);
//            Cursor cur = new CursorLoader(getActivity(), myUri, PLAYER_COLUMNS, null, null, null);
            Log.i(LOG_TAG, "URI: "+myUri);
            Cursor cur = getActivity().getContentResolver().query(myUri, PLAYER_COLUMNS, null,
                    null, null);
            cur.moveToFirst();
            populateFields(rootView, cur);
            for (int i = 0; i < cur.getColumnCount(); i++){
                Log.i(LOG_TAG, i+": "+cur.getString(i));
            }
            cur.close();

        }

        return rootView;
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
}
