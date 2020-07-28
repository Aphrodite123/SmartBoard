package com.aphrodite.smartboard.model.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class WorkInfoBean implements Parcelable {
    private String name;
    private String time;
    private String author;
    private String path;
    private String picture;
    private String dataPath;
    private String audioPath;
    private String date;
    private int type;

    public WorkInfoBean() {
    }

    protected WorkInfoBean(Parcel in) {
        name = in.readString();
        time = in.readString();
        author = in.readString();
        path = in.readString();
        picture = in.readString();
        dataPath = in.readString();
        audioPath = in.readString();
        date = in.readString();
        type = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(time);
        dest.writeString(author);
        dest.writeString(path);
        dest.writeString(picture);
        dest.writeString(dataPath);
        dest.writeString(audioPath);
        dest.writeString(date);
        dest.writeInt(type);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<WorkInfoBean> CREATOR = new Creator<WorkInfoBean>() {
        @Override
        public WorkInfoBean createFromParcel(Parcel in) {
            return new WorkInfoBean(in);
        }

        @Override
        public WorkInfoBean[] newArray(int size) {
            return new WorkInfoBean[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
