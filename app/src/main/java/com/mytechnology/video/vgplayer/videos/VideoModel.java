package com.mytechnology.video.vgplayer.videos;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class VideoModel implements Parcelable {
    private final String dateAdded;
    private final int duration;
    private final String name;
    private final String path;
    private final int size;

    public VideoModel(final String path, final String name, final String dateAdded, final int duration, final int size) {
        this.path = path;
        this.name = name;
        this.dateAdded = dateAdded;
        this.duration = duration;
        this.size = size;
    }

    protected VideoModel(Parcel in) {
        dateAdded = in.readString();
        duration = in.readInt();
        name = in.readString();
        path = in.readString();
        size = in.readInt();
    }

    public static final Creator<VideoModel> CREATOR = new Creator<VideoModel>() {
        @Override
        public VideoModel createFromParcel(Parcel in) {
            return new VideoModel(in);
        }

        @Override
        public VideoModel[] newArray(int size) {
            return new VideoModel[size];
        }
    };

    public String getDateAdded() {
        return dateAdded;
    }

    public int getDuration() {
        return duration;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public int getSize() {
        return size;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(dateAdded);
        dest.writeInt(duration);
        dest.writeString(name);
        dest.writeString(path);
        dest.writeInt(size);
    }
}
