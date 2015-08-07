package net.vectortime.p2_spotifystreamer.dataClasses;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import kaaes.spotify.webapi.android.models.Image;

public class ArtistInfo implements Parcelable {
    public String artistName;
    public List<Image> images;
    public String artistId;

    public ArtistInfo (String inName, List<Image> inImages, String inId){
        this.artistName = inName;
        this.images = inImages;
        this.artistId = inId;
    }

    public ArtistInfo (String inName, List<Image> inImages){
        this.artistName = inName;
        this.images = inImages;
        this.artistId = null;
    }

    public ArtistInfo (String inName){
        this.artistName = inName;
        this.images = null;
        this.artistId = null;
    }

    protected ArtistInfo(Parcel in) {
        artistName = in.readString();
        artistId = in.readString();
    }

    public static final Creator<ArtistInfo> CREATOR = new Creator<ArtistInfo>() {
        @Override
        public ArtistInfo createFromParcel(Parcel in) {
            return new ArtistInfo(in);
        }

        @Override
        public ArtistInfo[] newArray(int size) {
            return new ArtistInfo[size];
        }
    };

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
        int lagest_size = 0;
        if (images != null && images.size() > 0)

            for (int i = 0; i < images.size(); i++) {
                if (images.get(i).width > lagest_size) {
                    returnString = images.get(i).url;
                    lagest_size = images.get(i).width;
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
        dest.writeString(artistName);
        dest.writeList(images);
        dest.writeString(artistId);
    }
}