package net.vectortime.p2_spotifystreamer;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import net.vectortime.p2_spotifystreamer.database.MusicContract.TrackEntry;


/**
 * Created by Kevin on 8/1/2015.
 */
public class ProviderTest extends AndroidTestCase{
    private final String LOG_TAG = ProviderTest.class.getSimpleName();

    public void deleteAllRecordsFromProvider() {
        int deleted = mContext.getContentResolver().delete(TrackEntry.TRACK_CONTENT_URI, null,
                null);
        Log.i(LOG_TAG, "Rows deleted: " + deleted);

        Cursor cursor = mContext.getContentResolver().query(TrackEntry.TRACK_CONTENT_URI, null,
                null, null, null);

        assertEquals("Error: Records not deleted from track table during delete", 0, cursor
                .getCount());
        cursor.close();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecordsFromProvider();
    }

    public void testGetType() {
        ContentResolver cr = mContext.getContentResolver();
        String type = cr.getType(TrackEntry.TRACK_CONTENT_URI);
        Log.i(LOG_TAG, "Uri: " + TrackEntry.TRACK_CONTENT_URI.toString());
        Log.i(LOG_TAG, "Type: " + type);
        assertEquals("Error - types don't match", TrackEntry.TRACK_CONTENT_TYPE, type);

        String fakeID = "123abc";
        Uri builtType = TrackEntry.buildTrackByTrackID(fakeID);
        type = mContext.getContentResolver().getType(builtType);
        Log.i(LOG_TAG, "Uri2: " + builtType.toString());
        Log.i(LOG_TAG, "Type2: " + type);
        assertEquals("Error - types don't match again", TrackEntry.TRACK_CONTENT_TYPE, type);
    }

    public void testInsertTrack() {
        ContentValues testValues = TestUtilities.createTestTrackData();

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(TrackEntry.TRACK_CONTENT_URI, true,
                tco);
        Uri trackUri = mContext.getContentResolver().insert(TrackEntry.TRACK_CONTENT_URI,
                testValues);

        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

//        long rowId = ContentUris.parseId(trackUri);
//        Log.i(LOG_TAG, "Insert - id: " + rowId);
//
//        assertTrue(rowId != -1);

        Cursor cursor = mContext.getContentResolver().query(TrackEntry.TRACK_CONTENT_URI, null,
                null, null, null);

        TestUtilities.validateCursor("Insert - error validating insert.", cursor, testValues);


    }
}
