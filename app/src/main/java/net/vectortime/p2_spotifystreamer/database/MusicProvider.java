package net.vectortime.p2_spotifystreamer.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

/**
 * Created by Kevin on 7/31/2015.
 */
public class MusicProvider extends ContentProvider {
    private final String LOG_TAG = MusicProvider.class.getSimpleName();
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private DatabaseHelper mHelper;

    static final int TRACK = 100;
    static final int TRACK_BY_ID = 101;
    static final int TRACKS_FROM_ARTIST = 102;
    static final int TRACK_FROM_ARTIST_AND_RANK = 103;

    private static final SQLiteQueryBuilder sQueryBuilder;

    static {
        sQueryBuilder = new SQLiteQueryBuilder();

        sQueryBuilder.setTables(MusicContract.TrackEntry.TABLE_NAME);
    }

    // Track info for track id = ?
    private static final String sTrackByTrackID = MusicContract.TrackEntry.TABLE_NAME+"."+
            MusicContract.TrackEntry.COLUMN_TRACK_ID + " = ? ";

    // Track info for artist id = ?
    private static final String sTrackByArtist = MusicContract.TrackEntry.TABLE_NAME+"."+
            MusicContract.TrackEntry.COLUMN_ARTIST_ID + " = ? ";

    // Track info for artist id = ? AND rank = ?
    private static final String sTrackByArtistAndRank = MusicContract.TrackEntry.TABLE_NAME+"."+
            MusicContract.TrackEntry.COLUMN_ARTIST_ID + " = ? AND " +
            MusicContract.TrackEntry.COLUMN_TRACK_RANK + " = ? ";

    private Cursor getTrackById(Uri uri, String[] projection) {
        String[] selectionArgs;
        String selection;

        selection = sTrackByTrackID;
        selectionArgs = new String[]{MusicContract.TrackEntry.getIdFromUri(uri)};

        return sQueryBuilder.query(mHelper.getReadableDatabase(), projection, selection,
                selectionArgs, null, null, MusicContract.TrackEntry.COLUMN_TRACK_RANK);
    }

    private Cursor getTracksByArtist(Uri uri, String[] projection) {
        String[] selectionArgs;
        String selection;

        selection = sTrackByTrackID;
        selectionArgs = new String[]{MusicContract.TrackEntry.getIdFromUri(uri)};

        return sQueryBuilder.query(mHelper.getReadableDatabase(), projection, selection,
                selectionArgs, null, null, MusicContract.TrackEntry.COLUMN_TRACK_RANK);
    }

    private Cursor getTrackByArtistAndRank(Uri uri, String[] projection) {
        String[] selectionArgs;
        String selection;

        selection = sTrackByArtistAndRank;
        selectionArgs = MusicContract.TrackEntry.getSelectionArgsFromUri(uri);

        return sQueryBuilder.query(mHelper.getReadableDatabase(), projection, selection,
                selectionArgs, null, null, MusicContract.TrackEntry.COLUMN_TRACK_RANK);
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher um = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MusicContract.CONTENT_AUTHORITY;

        um.addURI(authority, MusicContract.PATH_ARTIST, TRACKS_FROM_ARTIST);
        um.addURI(authority, MusicContract.PATH_ARTIST + "/*/#", TRACK_FROM_ARTIST_AND_RANK);

        um.addURI(authority, MusicContract.PATH_TRACK, TRACK);
        um.addURI(authority, MusicContract.PATH_TRACK + "/*", TRACK_BY_ID);

        return um;
    }

    @Override
    public boolean onCreate() {
        mHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)){
            case TRACK:
            case TRACK_BY_ID: {
                retCursor = mHelper.getReadableDatabase().query(MusicContract.TrackEntry
                        .TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case TRACKS_FROM_ARTIST:
            case TRACK_FROM_ARTIST_AND_RANK: {
                retCursor = mHelper.getReadableDatabase().query(MusicContract.TrackEntry
                        .TABLE_NAME, projection, sTrackByArtistAndRank,
                        MusicContract.TrackEntry.getSelectionArgsFromUri(uri),
                        null,
                        null,
                        sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri 1: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(),uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match){
            case TRACK:
                return MusicContract.TrackEntry.TRACK_CONTENT_TYPE;
            case TRACK_BY_ID:
                return MusicContract.TrackEntry.TRACK_CONTENT_TYPE;
            case TRACKS_FROM_ARTIST:
            case TRACK_FROM_ARTIST_AND_RANK:
                return MusicContract.TrackEntry.ARTIST_CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri 2: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match){
            case TRACK: {
                long _id = db.insert(MusicContract.TrackEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    //TODO: Determine correct return URI
                    Log.d(LOG_TAG, "ID of inserted = "+ _id);
                    returnUri = MusicContract.TrackEntry.buildTrackByTrackID(values.getAsString
                            (MusicContract.TrackEntry.COLUMN_TRACK_ID));
//                    returnUri = MusicContract.TrackEntry.buildTrackByTrackID(Long.toString(_id));
                } else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri 3: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        int rowsDeleted = 0;
        if (selection == null)
            selection = "1";

        switch (match){
            case TRACK: {
                rowsDeleted = db.delete(MusicContract.TrackEntry.TABLE_NAME, selection,
                        selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri 4: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public void shutdown() {
        mHelper.close();
        super.shutdown();
    }
}
