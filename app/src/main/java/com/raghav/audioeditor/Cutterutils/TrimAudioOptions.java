package com.raghav.audioeditor.Cutterutils;

import android.os.Parcel;
import android.os.Parcelable;

public class TrimAudioOptions implements Parcelable {

    public String destination;

    public String fileName;

    public boolean hideSeekBar;

//    public long fixedDuration;

//    public int trimType=1;

    public TrimAudioOptions() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.destination);
        dest.writeString(this.fileName);
//        dest.writeLong(this.fixedDuration);
//        dest.writeInt(this.trimType);
        dest.writeByte(this.hideSeekBar ? (byte) 1 : (byte) 0);
    }

    protected TrimAudioOptions(Parcel in) {
        this.destination = in.readString();
        this.fileName = in.readString();
//        this.fixedDuration = in.readLong();
//        this.trimType=in.readInt();
        this.hideSeekBar = in.readByte() != 0;
    }

    public static final Creator<TrimAudioOptions> CREATOR = new Creator<TrimAudioOptions>() {
        @Override
        public TrimAudioOptions createFromParcel(Parcel source) {
            return new TrimAudioOptions(source);
        }

        @Override
        public TrimAudioOptions[] newArray(int size) {
            return new TrimAudioOptions[size];
        }
    };
}
