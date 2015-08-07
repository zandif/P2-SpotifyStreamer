package net.vectortime.p2_spotifystreamer.dataClasses;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import net.vectortime.p2_spotifystreamer.database.MusicContract;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Image;

public class TrackInfo implements Parcelable {

    public String songId;
    public String songTitle;
    public long songDuration;
    public int songRank;
    public String ablumId;
    public List<ImageParcel> images;
    public String albumTitle;
    public String artistId;
    public String artistTitle;

    public TrackInfo(String inId, String inName, String inAlbumId, List<Image> inAlbumImages,
                     String inAlbumName, long inSongDuration, int inSongRank, String inArtistId,
                     String inArtistTitle) {
        this.songId = inId;
        this.songTitle = inName;
        this.ablumId = inAlbumId;
//            this.images = inAlbumImages;
        images = new ArrayList<>();
        for (int i = 0; i < inAlbumImages.size(); i++)
            images.add(new ImageParcel(inAlbumImages.get(i)));
        this.albumTitle = inAlbumName;
        this.songDuration = inSongDuration;
        this.songRank = inSongRank;
        this.artistId = inArtistId;
        this.artistTitle = inArtistTitle;
    }

    private TrackInfo(Parcel in){
        songId = in.readString();
        songTitle = in.readString();
        albumTitle = in.readString();
        in.readList(images, null);
        albumTitle = in.readString();
        songDuration = in.readLong();
        songRank = in.readInt();
        artistId = in.readString();
        artistTitle = in.readString();
    }

    public String getSmallestImage() {
        String returnString = null;
        int smallest_size = 0;
        if (images != null && images.size() > 0)

            for (int i = 0; i < images.size(); i++) {
                if (smallest_size == 0) {
                    smallest_size = images.get(i).width;
                    returnString = images.get(i).url;
                }

                if (images.get(i).width < smallest_size) {
                    returnString = images.get(i).url;
                    smallest_size = images.get(i).width;
                }
            }
        return returnString;
    }

    public String getLargestImage() {
        String returnString = null;
        int largestSize = 0;
        if (images != null && images.size() > 0)
            for (int i = 0; i < images.size(); i++) {
                if (images.get(i).width > largestSize) {
                    returnString = images.get(i).url;
                    largestSize = images.get(i).width;
                }
            }
        return returnString;
    }

    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MusicContract.TrackEntry.COLUMN_ALBUM_NAME, albumTitle);
        contentValues.put(MusicContract.TrackEntry.COLUMN_ALBUM_ID, ablumId);
        contentValues.put(MusicContract.TrackEntry.COLUMN_ALBUM_ART_LARGE, getLargestImage());
        contentValues.put(MusicContract.TrackEntry.COLUMN_ALBUM_ART_SMALL, getSmallestImage());
        contentValues.put(MusicContract.TrackEntry.COLUMN_ARTIST_ID, artistId);
        contentValues.put(MusicContract.TrackEntry.COLUMN_ARTIST_NAME, artistTitle);
        contentValues.put(MusicContract.TrackEntry.COLUMN_TRACK_DURATION, songDuration);
        contentValues.put(MusicContract.TrackEntry.COLUMN_TRACK_ID, songId);
        contentValues.put(MusicContract.TrackEntry.COLUMN_TRACK_NAME, songTitle);
        contentValues.put(MusicContract.TrackEntry.COLUMN_TRACK_RANK, songRank);
        return  contentValues;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(songId);
        dest.writeString(songTitle);
        dest.writeString(albumTitle);
        dest.writeList(images);
        dest.writeString(albumTitle);
        dest.writeLong(songDuration);
        dest.writeInt(songRank);
        dest.writeString(artistId);
        dest.writeString(artistTitle);
    }

    public final Parcelable.Creator<TrackInfo> CREATOR = new Parcelable
            .ClassLoaderCreator<TrackInfo>() {

        @Override
        public TrackInfo createFromParcel(Parcel source) {
            return new TrackInfo(source);
        }

        @Override
        public TrackInfo[] newArray(int size) {
            return new TrackInfo[0];
        }

        @Override
        public TrackInfo createFromParcel(Parcel source, ClassLoader loader) {
            return null;
        }
    };

    private class ImageParcel extends Image implements Parcelable {
        private ImageParcel (Parcel in){
            url = in.readString();
            width = in.readInt();
            height = in.readInt();
        }

        public ImageParcel (Image in) {
            url = in.url;
            width = in.width;
            height = in.height;
        }

        public final Creator<ImageParcel> CREATOR = new Creator<ImageParcel>() {
            @Override
            public ImageParcel createFromParcel(Parcel in) {
                return new ImageParcel(in);
            }

            @Override
            public ImageParcel[] newArray(int size) {
                return new ImageParcel[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(url);
            dest.writeInt(width.intValue());
            dest.writeInt(height.intValue());
        }
    }

}