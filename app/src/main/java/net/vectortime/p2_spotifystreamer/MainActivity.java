package net.vectortime.p2_spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import net.vectortime.p2_spotifystreamer.dataClasses.ArtistInfo;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.Callback {

    private boolean mTwoPane;
    private static final String TOPTRACKS_TAG = "TTTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.toptracks_fragment) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                getFragmentManager().beginTransaction().replace(R.id.toptracks_fragment,
                        new TopTracksActivityFragment()).commit();
            } else {
                mTwoPane = false;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(ArtistInfo info) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            // Put stuff in bundle
            args.putString(TopTracksActivityFragment.ARTIST_ID_KEY,info.artistId);
            args.putString(TopTracksActivityFragment.ARTIST_NAME_KEY,info.artistName);
            args.putString(TopTracksActivityFragment.ARTIST_ICON_KEY,info.getLargestImage());
            getSupportActionBar().setSubtitle(info.artistName);

            //Make new fragment
            TopTracksActivityFragment fragment = new TopTracksActivityFragment();
            //Pass in bundle
            fragment.setArguments(args);

            getFragmentManager().beginTransaction().replace(R.id.toptracks_fragment,
                    fragment, TOPTRACKS_TAG).commit();

        } else {
            // Explicit intent to launch the detail activity
            Intent topTracksIntent = new Intent(this, TopTracksActivity.class);
            topTracksIntent.putExtra(Intent.EXTRA_TEXT, info.artistName);
            topTracksIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, info.getLargestImage());
            topTracksIntent.putExtra(Intent.EXTRA_UID, info.artistId);
            startActivity(topTracksIntent);
        }
    }
}
