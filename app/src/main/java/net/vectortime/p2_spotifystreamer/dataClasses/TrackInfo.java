package net.vectortime.p2_spotifystreamer.dataClasses;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Image;

public class TrackInfo implements Parcelable {

    public String songId;
    public String songTitle;
    public String ablumId;
    public List<ImageParcel> images;
    public String albumTitle;

    public TrackInfo(String inId, String inName, String inAlbumId, List<Image> inAlbumImages,
                     String inAlbumName) {
        this.songId = inId;
        this.songTitle = inName;
        this.ablumId = inAlbumId;
//            this.images = inAlbumImages;
        images = new ArrayList<>();
        for (int i = 0; i < inAlbumImages.size(); i++)
            images.add(new ImageParcel(inAlbumImages.get(i)));
        this.albumTitle = inAlbumName;
    }

    private TrackInfo(Parcel in){
        songId = in.readString();
        songTitle = in.readString();
        albumTitle = in.readString();
        in.readList(images, null);
        albumTitle = in.readString();
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