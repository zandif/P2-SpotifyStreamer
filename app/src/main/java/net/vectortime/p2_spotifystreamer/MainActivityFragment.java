package net.vectortime.p2_spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import net.vectortime.p2_spotifystreamer.dataClasses.ArtistInfo;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    private ArtistArrayAdapter mArtistsAdapter;
    private int mPosition = ListView.INVALID_POSITION;
    private final String POSITION_KEY = "position";

    public MainActivityFragment() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != ListView.INVALID_POSITION)
            outState.putInt(POSITION_KEY, mPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        EditText mEdit = (EditText)rootView.findViewById(R.id.search_textfield);
        mEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() < 1)
                    return;

                SearchSpotifyArtistsTask searchTask = new SearchSpotifyArtistsTask();
                searchTask.execute(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        ArrayList<ArtistInfo> artistsTestData = new ArrayList<>(10);

        List<ArtistInfo> artistsList = artistsTestData;

        mArtistsAdapter = new ArtistArrayAdapter(getActivity(),R.layout.list_item_artist,
                artistsList);

        ListView myListView = (ListView) rootView.findViewById(R.id.listview_artists);
        myListView.setAdapter(mArtistsAdapter);

        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ArtistInfo info = mArtistsAdapter.getItem(i);
                ((Callback) getActivity()).onItemSelected(info);

//                // Explicit intent to launch the detail activity
//                Intent topTracksIntent = new Intent(getActivity(), TopTracksActivity.class);
//                topTracksIntent.putExtra(Intent.EXTRA_TEXT, info.artistName);
//                topTracksIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, info.getLargestImage());
//                topTracksIntent.putExtra(Intent.EXTRA_UID, info.artistId);
//                startActivity(topTracksIntent);
                mPosition = i;
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(POSITION_KEY)) {
            mPosition = savedInstanceState.getInt(POSITION_KEY);
        }

        return rootView;
    }

    private class ArtistArrayAdapter extends ArrayAdapter<ArtistInfo> {
        private final String LOG_TAG = ArtistArrayAdapter.class.getSimpleName();


        public ArtistArrayAdapter(Context context, int resource, List<ArtistInfo> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ArtistInfo artistInfo = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout
                        .list_item_artist, parent, false);
            }
            if (artistInfo == null)
                return convertView;

            ImageView iconView = (ImageView) convertView.findViewById(R.id
                    .list_item_artist_imageview);
            String thumbnailURL = artistInfo.getSmallestImage();
            if (thumbnailURL != null)
//                Picasso.with(getContext()).load("http://i.imgur.com/DvpvklR.png").into(iconView);
                Picasso.with(getContext()).load(artistInfo.getSmallestImage()).into(iconView);
            else
                iconView.setImageResource(R.drawable.streamer_logo);

            TextView artistName = (TextView) convertView.findViewById(R.id
                    .list_item_artist_textview);
            artistName.setText(artistInfo.artistName);

            return convertView;
        }
    }

    private class SearchSpotifyArtistsTask extends AsyncTask<String, Void, ArtistInfo[]> {
        private final String LOG_TAG = SearchSpotifyArtistsTask.class.getSimpleName();
        private String artistQueryName;

        @Override
        protected ArtistInfo[] doInBackground(String... params) {
            if (params.length == 0){
                return null;
            }
            artistQueryName = params[0];

            SpotifyApi api = new SpotifyApi();

            ArtistsPager results = api.getService().searchArtists(artistQueryName);
            if (results.artists.items.size() < 1)
                results = api.getService().searchArtists(artistQueryName + "*");
            List<Artist> artists = results.artists.items;
            List<ArtistInfo> info = new ArrayList<>();
            for (int i = 0; i < artists.size(); i++){
                Artist artist = artists.get(i);
//                Log.i(LOG_TAG, i + " " + mArtistText.name);
                info.add(new ArtistInfo(artist.name, artist.images, artist.id));
//                for (int j = 0; j < mArtistText.images.size(); j++) {
//                    Log.i(LOG_TAG, j + " " + mArtistText.images.get(j).url + " " + mArtistText.images.get
//                            (j).width.toString() + "x" + mArtistText.images.get(j).height.toString());
//                }
            }
            return info.toArray(new ArtistInfo[info.size()]);
        }

        @Override
        protected void onPostExecute(ArtistInfo[] inArtistInfo) {
            super.onPostExecute(inArtistInfo);
            if (inArtistInfo != null && inArtistInfo.length > 0) {
                mArtistsAdapter.clear();
                int max = 10;
                if (inArtistInfo.length < 10)
                    max = inArtistInfo.length;
                for (int i = 0; i < max; i++){
                    mArtistsAdapter.addAll(inArtistInfo[i]);
                }
            } else {
                // Display a toast
                Context context = getActivity();
                CharSequence text = "No results found for that mArtistText: " + artistQueryName;
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        }
    }

    public interface Callback {
        public void onItemSelected(ArtistInfo selectedArtist);
    }
}
