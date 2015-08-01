package net.vectortime.p2_spotifystreamer;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import net.vectortime.p2_spotifystreamer.database.DatabaseHelper;
import net.vectortime.p2_spotifystreamer.database.MusicContract;

import java.util.Map;
import java.util.Set;

import static net.vectortime.p2_spotifystreamer.TestUtilities.createTestTrackData;

/**
 * Created by Kevin on 7/31/2015.
 */
public class DatabaseTest extends AndroidTestCase {
    public void testCreateDB() throws Throwable {
        mContext.deleteDatabase(DatabaseHelper.DATABASE_NAME);

        SQLiteDatabase db = new DatabaseHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        assertTrue("Error: This means that the database has not been created correctly.", c
                .moveToFirst());

        boolean tableExists = false;
        do {
            if (c.getString(0).equals(MusicContract.TrackEntry.TABLE_NAME))
                tableExists = true;
        } while ( c.moveToNext());
        assertTrue("Error - table not created",tableExists);


    }

    public void testTrackTable () {
        SQLiteDatabase db = new DatabaseHelper(this.mContext).getWritableDatabase();

        insertTrack(db);

        Cursor cursor = db.query(MusicContract.TrackEntry.TABLE_NAME, null, null, null, null,
                null, null);

        cursor.moveToFirst();

        Set<Map.Entry<String, Object>> valueSet = createTestTrackData().valueSet();
        for (Map.Entry<String,Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = cursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + "err", idx == -1);

            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() + "' did not match the expected " +
                    "value '" + expectedValue + "'. " + "err", expectedValue, cursor.getString
                    (idx));
        }
    }

    private long insertTrack (SQLiteDatabase inDB) {
        ContentValues testValues = createTestTrackData();

        long rowID = inDB.insert(MusicContract.TrackEntry.TABLE_NAME, null, testValues);
        assertTrue("Error: Could not insert", rowID != -1);

        return rowID;
    }


}
