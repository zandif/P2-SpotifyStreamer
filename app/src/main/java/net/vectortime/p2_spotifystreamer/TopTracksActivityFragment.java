package net.vectortime.p2_spotifystreamer;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.app.Fragment;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import net.vectortime.p2_spotifystreamer.dataClasses.TrackInfo;
import net.vectortime.p2_spotifystreamer.database.MusicContract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

/**
 * A placeholder fragment containing a simple view.
 */
public class TopTracksActivityFragment extends Fragment {
    private TopTracksArrayAdapter mTracksAdapter;
    private String mArtistName;
    private String mArtistImageURL;
    private String mArtistId;
    private ArrayList<TrackInfo> mTracksList;

    private final String PARCEL_KEY = "tracks";
    static final String ARTIST_ID_KEY = "artistid";
    static final String ARTIST_NAME_KEY = "artistname";
    static final String ARTIST_ICON_KEY = "artisticon";
    private final String LOG_TAG = TopTracksActivityFragment.class.getSimpleName();

    public TopTracksActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        this.setRetainInstance(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        Log.d(LOG_TAG, "onSaveInstanceState");
        outState.putParcelableArrayList(PARCEL_KEY, mTracksList);
//        Log.i(TopTracksActivityFragment.class.getSimpleName(), "Saving " + mTracksList.size() +
//                " entries to parcel.");
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreateView");

        View rootView = inflater.inflate(R.layout.fragment_toptracks, container, false);

        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            mArtistName = intent.getStringExtra(Intent.EXTRA_TEXT);
//            TextView myTextView = (TextView) rootView.findViewById(R.id.text_detail);
//            myTextView.setText(mForecastStr);

            if (intent.hasExtra(Intent.EXTRA_SHORTCUT_ICON)) {
                mArtistImageURL = intent.getStringExtra(Intent.EXTRA_SHORTCUT_ICON);
            }

            if (intent.hasExtra(Intent.EXTRA_UID)) {
                mArtistId = intent.getStringExtra(Intent.EXTRA_UID);
            }

            Log.d(LOG_TAG, "Intent pulled the following values: " + mArtistId + " = " +
                    mArtistName);
        }

        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(ARTIST_ID_KEY)){
            mArtistId = arguments.getString(ARTIST_ID_KEY);
            mArtistName = arguments.getString(ARTIST_NAME_KEY);
            mArtistImageURL = arguments.getString(ARTIST_ICON_KEY);
            Log.d(LOG_TAG, "arguments pulled the following values: " + mArtistId + " = " +
                    mArtistName);
        }

        if (savedInstanceState == null || !savedInstanceState.containsKey(PARCEL_KEY)) {
            mTracksList = new ArrayList<>(10);
            Log.d(LOG_TAG, "Empty list");
        } else {
            mTracksList = savedInstanceState.getParcelableArrayList(PARCEL_KEY);
            Log.d(LOG_TAG, "Got " + mTracksList.size() + " entries from parcel.");
        }

//        mTracksList.add(new TrackInfo("0", "A Sky Full of Stars", "0", null, "Ghost " +
//                "Stories"));
//        mTracksList.add(new TrackInfo("1", "Fix You", "1", null, "X&Y"));
//        mTracksList.add(new TrackInfo("2", "The Scientist", "2", null, "A Rush of Blood to " +
//                "the..."));

        mTracksAdapter = new TopTracksArrayAdapter(getActivity(),R.layout.list_item_toptracks,
                mTracksList);

        ListView myListView = (ListView) rootView.findViewById(R.id.listview_toptracks);
        myListView.setAdapter(mTracksAdapter);

        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TrackInfo info = mTracksAdapter.getItem(i);
                FragmentManager fragmentManager = getActivity().getFragmentManager();

                boolean mIsLargeLayout = getResources().getBoolean(R.bool.large_layout);

                if (mIsLargeLayout) {
                    TrackPlayerActivityFragment fragment = new TrackPlayerActivityFragment();
                    Bundle arguments = new Bundle();
                    arguments.putString(TrackPlayerActivityFragment.ARTIST_KEY, info.artistId);
                    arguments.putInt(TrackPlayerActivityFragment.RANK_KEY, info.songRank);
                    fragment.setArguments(arguments);
                    fragment.show(fragmentManager, "PLAYER");
                } else {
                    // Explicit intent to launch the detail activity
                    Intent trackPlayerIntent = new Intent(getActivity(), TrackPlayerActivity.class);
                    trackPlayerIntent.putExtra(Intent.EXTRA_TEXT, info.artistId);
                    trackPlayerIntent.putExtra(Intent.EXTRA_UID, info.songRank);
                    startActivity(trackPlayerIntent);
                }
            }
        });

        if (mArtistId != null && mTracksList.size() < 1) {
            SearchSpotifyTopTracksTask searchTask = new SearchSpotifyTopTracksTask();
            searchTask.execute(mArtistId);
        }

        return rootView;
    }

    private class SearchSpotifyTopTracksTask extends AsyncTask<String, Void, TrackInfo[]> {
        private final String LOG_TAG = SearchSpotifyTopTracksTask.class.getSimpleName();

        @Override
        protected TrackInfo[] doInBackground(String... params) {
            if (params.length == 0){
                return null;
            }
            String artistQueryId = params[0];

//            Log.i(LOG_TAG, "artistID: " + artistQueryId);
            SpotifyApi api = new SpotifyApi();
            Map<String, Object> map = new HashMap<>();
            map.put("country", Locale.getDefault().getCountry());
            Tracks tracks = api.getService().getArtistTopTrack(artistQueryId, map);

            List<TrackInfo> info = new ArrayList<>();
            for (int i = 0; i < tracks.tracks.size(); i++){
                Track track = tracks.tracks.get(i);
//                Log.i(LOG_TAG, i + " " + track.name);
                TrackInfo tempInfo = new TrackInfo(track.id, track.name, track.preview_url, track
                        .album.id, track.album.images, track.album.name,track.duration_ms,i,track
                        .artists.get(0).id,track.artists.get(0).name);
                info.add(tempInfo);
            }
            return info.toArray(new TrackInfo[info.size()]);
        }

        @Override
        protected void onPostExecute(TrackInfo[] inTrackInfo) {
            super.onPostExecute(inTrackInfo);
            if (inTrackInfo != null && inTrackInfo.length > 0) {
                mTracksAdapter.clear();
                int max = 10;
                if (inTrackInfo.length < 10)
                    max = inTrackInfo.length;
                for (int i = 0; i < max; i++){
                    mTracksAdapter.addAll(inTrackInfo[i]);
                    Uri insertedItem = getActivity().getContentResolver().insert(MusicContract.TrackEntry
                                    .TRACK_CONTENT_URI,
                            inTrackInfo[i].getContentValues());
                    Log.d(LOG_TAG, "Inserted track: " + insertedItem.toString());

                }
            } else {
                // Display a toast
                Context context = getActivity();
                CharSequence text = "No track results found for the mArtistText " + mArtistName;
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        }
    }

    private class TopTracksArrayAdapter extends ArrayAdapter<TrackInfo> {
        private final String LOG_TAG = TopTracksArrayAdapter.class.getSimpleName();

        public TopTracksArrayAdapter(Context context, int resource, List<TrackInfo> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TrackInfo trackInfo = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout
                        .list_item_toptracks, parent, false);
            }
            if (trackInfo == null)
                return convertView;

            ImageView iconView = (ImageView) convertView.findViewById(R.id
                    .list_item_toptracks_imageview);
            String thumbnailURL = trackInfo.getSmallestImage();
            if (thumbnailURL != null)
//                Picasso.with(getContext()).load("http://i.imgur.com/DvpvklR.png").into(iconView);
                Picasso.with(getContext()).load(trackInfo.getSmallestImage()).into(iconView);
            else
                iconView.setImageResource(R.drawable.streamer_logo);

            TextView songTitle = (TextView) convertView.findViewById(R.id
                    .list_item_toptracks_songtitle_textview);
            songTitle.setText(trackInfo.songTitle);

            TextView ablumName = (TextView) convertView.findViewById(R.id
                    .list_item_toptracks_albumtitle_textview);
            ablumName.setText(trackInfo.albumTitle);

            return convertView;
        }
    }
}
