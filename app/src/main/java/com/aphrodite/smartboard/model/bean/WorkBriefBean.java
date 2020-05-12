package com.aphrodite.smartboard.model.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class WorkBriefBean implements Parcelable {
    private String name;
    private String value;

    public WorkBriefBean() {
    }

    public WorkBriefBean(String name, String value) {
        this.name = name;
        this.value = value;
    }

    protected WorkBriefBean(Parcel in) {
        name = in.readString();
        value = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(value);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<WorkBriefBean> CREATOR = new Creator<WorkBriefBean>() {
        @Override
        public WorkBriefBean createFromParcel(Parcel in) {
            return new WorkBriefBean(in);
        }

        @Override
        public WorkBriefBean[] newArray(int size) {
            return new WorkBriefBean[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
