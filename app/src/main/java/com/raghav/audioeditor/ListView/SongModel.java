package com.raghav.audioeditor.ListView;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class SongModel implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "album")
    private String album;
    @ColumnInfo(name = "artist")
    private String artist;
    @ColumnInfo(name = "title")
    private String title;
    @ColumnInfo(name = "duration")
    private String duration;
    @ColumnInfo(name = "size")
    private String size;
    @ColumnInfo(name = "uri")
    private String uri;
    @ColumnInfo(name = "date")
    private String date;
    @ColumnInfo(name = "durationLong")
    private long durationLong;


    public SongModel(String album, String artist,
                     String title, String duration,long durationLong,
                     String date, String size, String uri) {
        this.album = album;
        this.artist = artist;
        this.title = title;
        this.duration = duration;
        this.size = size;
        this.uri = uri;
        this.durationLong = durationLong;
        this.date=date;
    }

    protected SongModel(Parcel in) {
        album = in.readString();
        artist = in.readString();
        title = in.readString();
        duration = in.readString();
        size = in.readString();
        uri = in.readString();
        date = in.readString();
        durationLong = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(album);
        dest.writeString(artist);
        dest.writeString(title);
        dest.writeString(duration);
        dest.writeString(size);
        dest.writeString(uri);
        dest.writeString(date);
        dest.writeLong(durationLong);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SongModel> CREATOR = new Creator<SongModel>() {
        @Override
        public SongModel createFromParcel(Parcel in) {
            return new SongModel(in);
        }

        @Override
        public SongModel[] newArray(int size) {
            return new SongModel[size];
        }
    };

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public long getDurationLong() {
        return durationLong;
    }

    public void setDurationLong(long durationLong) {
        this.durationLong = durationLong;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

