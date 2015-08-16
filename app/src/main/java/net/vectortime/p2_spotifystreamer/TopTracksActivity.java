package net.vectortime.p2_spotifystreamer;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class TopTracksActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toptracks);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.toptracks_fragment, new TopTracksActivityFragment())
                    .commit();
        }

        // Set up the sub-title
        Intent intent = getIntent();
        android.support.v7.app.ActionBar ab = getSupportActionBar();
        if (ab != null && intent != null && intent.hasExtra(Intent.EXTRA_TEXT))
            ab.setSubtitle(intent.getStringExtra(Intent.EXTRA_TEXT));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_top_tracks, menu);
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
}
