package net.vectortime.p2_spotifystreamer.database;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Kevin on 7/31/2015.
 */
public class MusicContract {
    public static final String CONTENT_AUTHORITY = "net.vectortime.android.p2_spotifystreamer.app";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_TRACK = "track";
    public static final String PATH_ARTIST = "artist";

    public static final class TrackEntry implements BaseColumns {
        public static final String TABLE_NAME = "tracks";

        public static final String COLUMN_ARTIST_ID = "artist_id";
        public static final String COLUMN_ARTIST_NAME = "artist_name";
        public static final String COLUMN_ALBUM_ID = "album_id";
        public static final String COLUMN_ALBUM_NAME = "album_name";
        public static final String COLUMN_ALBUM_ART_LARGE = "album_art_large";
        public static final String COLUMN_ALBUM_ART_SMALL = "album_art_small";
        public static final String COLUMN_TRACK_RANK = "track_rank";
        public static final String COLUMN_TRACK_ID = "track_id";
        public static final String COLUMN_TRACK_NAME = "track_name";
        public static final String COLUMN_TRACK_DURATION = "track_duration";
        public static final String COLUMN_TRACK_PREVIEW = "track_preview";

        // TrackID searches
        public static final Uri TRACK_CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath
                (PATH_TRACK).build();
        public static final String TRACK_CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRACK;


        public static Uri buildTrackByTrackID(String trackID){
            return TRACK_CONTENT_URI.buildUpon().appendPath(trackID).build();
        }

        // AristID searches
        public static final Uri ARTIST_CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath
                (PATH_ARTIST).build();
        public static final String ARTIST_CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ARTIST;

        public static Uri buildTrackByArtist(String artistID){
            return ARTIST_CONTENT_URI.buildUpon().appendPath(artistID).build();
        }

        public static Uri buildTrackByArtistWithRank(String artistId, int rank){
            return ARTIST_CONTENT_URI.buildUpon().appendPath(artistId).appendPath(Integer.toString(rank)).build();
        }

        public static String getIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String[] getSelectionArgsFromUri(Uri uri){
            String[] returnArray = new String[2];
            returnArray[0] = uri.getPathSegments().get(1);
            returnArray[1] = uri.getPathSegments().get(2);
            return returnArray;
        }

    }


}
