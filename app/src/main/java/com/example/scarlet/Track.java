package com.example.scarlet;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class Track implements Parcelable {

    private String songName;
    private String songArtist;
    private String id;
    private Bitmap albumImg;

    public Track(String songName, String songArtist, String id, Bitmap albumImg) {
        this.songName = songName;
        this.songArtist = songArtist;
        this.id = id;
        this.albumImg = albumImg;
    }

    protected Track(Parcel in) {
        songName = in.readString();
        songArtist = in.readString();
        id = in.readString();
        albumImg = in.readParcelable(Bitmap.class.getClassLoader());
    }

    public static final Creator<Track> CREATOR = new Creator<Track>() {
        @Override
        public Track createFromParcel(Parcel in) {
            return new Track(in);
        }

        @Override
        public Track[] newArray(int size) {
            return new Track[size];
        }
    };

    public String getSongName() { return songName; }

    public String getSongArtist() { return songArtist; }

    public String getID() {
        return id;
    }

    public Bitmap getAlbumImg() {
        return albumImg;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(songName);
        dest.writeString(songArtist);
        dest.writeString(id);
        dest.writeValue(albumImg);
    }
}
