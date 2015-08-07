package net.vectortime.p2_spotifystreamer.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import net.vectortime.p2_spotifystreamer.database.MusicContract.TrackEntry;

/**
 * Created by Kevin on 7/31/2015.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "music.db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_TRACKS_TABLE = "CREATE TABLE " + TrackEntry.TABLE_NAME +
                " (" + TrackEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TrackEntry.COLUMN_ARTIST_ID + " TEXT NOT NULL, " +
                TrackEntry.COLUMN_ARTIST_NAME + " TEXT NOT NULL, " +
                TrackEntry.COLUMN_ALBUM_ID + " TEXT NOT NULL, " +
                TrackEntry.COLUMN_ALBUM_NAME + " TEXT NOT NULL, " +
                TrackEntry.COLUMN_ALBUM_ART_LARGE + " TEXT NOT NULL, " +
                TrackEntry.COLUMN_ALBUM_ART_SMALL + " TEXT NOT NULL, " +
                TrackEntry.COLUMN_TRACK_RANK + " INTEGER NOT NULL, " +
                TrackEntry.COLUMN_TRACK_ID + " TEXT NOT NULL, " +
                TrackEntry.COLUMN_TRACK_NAME + " TEXT NOT NULL, " +
                TrackEntry.COLUMN_TRACK_DURATION + " INTEGER NOT NULL, " +
                "UNIQUE ("+TrackEntry.COLUMN_ARTIST_ID + ", " +
                TrackEntry.COLUMN_TRACK_RANK + ") ON CONFLICT REPLACE);";

                // Unique constraint page: http://stackoverflow.com/questions/2701877/sqlite-table-constraint-unique-on-multiple-columns

        Log.i(DatabaseHelper.class.getSimpleName(), "Database create string: " +
                ""+SQL_CREATE_TRACKS_TABLE);
        db.execSQL(SQL_CREATE_TRACKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TrackEntry.TABLE_NAME);
        onCreate(db);
    }
}
