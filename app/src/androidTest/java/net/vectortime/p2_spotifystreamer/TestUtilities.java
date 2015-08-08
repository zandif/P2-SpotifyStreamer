package net.vectortime.p2_spotifystreamer;

import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import net.vectortime.p2_spotifystreamer.database.MusicContract;
import net.vectortime.p2_spotifystreamer.utils.PollingCheck;

import java.util.Map;
import java.util.Set;

/**
 * Created by Kevin on 8/1/2015.
 */
public class TestUtilities extends AndroidTestCase{
    static ContentValues createTestTrackData () {
        // https://api.spotify.com/v1/search?q=barenaked+ladies&type=artist
        // https://api.spotify.com/v1/artists/0dEvJpkqhrcn64d3oI8v79/top-tracks?country=US
        ContentValues testValues = new ContentValues();
        testValues.put(MusicContract.TrackEntry.COLUMN_ALBUM_NAME, "Stunt");
        testValues.put(MusicContract.TrackEntry.COLUMN_ALBUM_ID, "4FsibLgkGMV9AfbLtEqvxT");
        testValues.put(MusicContract.TrackEntry.COLUMN_ALBUM_ART_LARGE, "https://i.scdn.co/image/6b69a2f5a5265a1cb35525598e44fbe3d4e7fe0f");
        testValues.put(MusicContract.TrackEntry.COLUMN_ALBUM_ART_SMALL, "https://i.scdn.co/image/6b6882f369d1894b5824daac3a5fcc6903b7fd1c");
        testValues.put(MusicContract.TrackEntry.COLUMN_ARTIST_ID, "0dEvJpkqhrcn64d3oI8v79");
        testValues.put(MusicContract.TrackEntry.COLUMN_ARTIST_NAME, "Barenaked Ladies");
        testValues.put(MusicContract.TrackEntry.COLUMN_TRACK_DURATION, 169760);
        testValues.put(MusicContract.TrackEntry.COLUMN_TRACK_ID, "1C0pmryC2MdXfa7MZ9uIrU");
        testValues.put(MusicContract.TrackEntry.COLUMN_TRACK_NAME, "One Week");
        testValues.put(MusicContract.TrackEntry.COLUMN_TRACK_PREVIEW, "https://p.scdn.co/mp3-preview/889f0af9e390ff0c1c17eafa6eaa7f41409a0016");
        testValues.put(MusicContract.TrackEntry.COLUMN_TRACK_RANK, 1);
        return  testValues;
    }

    // Used directly from TestUtilities from Lesson 4
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String,Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + "err", idx == -1);

            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() + "' did not match the expected " +
                    "value '" + expectedValue + "'. " + "err", expectedValue, valueCursor.getString
                    (idx));
        }
    }

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }
}
