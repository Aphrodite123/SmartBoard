package com.aphrodite.smartboard.model.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class WorksBean implements Parcelable {
    private String date;
    private List<WorkInfoBean> data;

    public WorksBean() {
    }

    protected WorksBean(Parcel in) {
        date = in.readString();
        data = in.createTypedArrayList(WorkInfoBean.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(date);
        dest.writeTypedList(data);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<WorksBean> CREATOR = new Creator<WorksBean>() {
        @Override
        public WorksBean createFromParcel(Parcel in) {
            return new WorksBean(in);
        }

        @Override
        public WorksBean[] newArray(int size) {
            return new WorksBean[size];
        }
    };

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<WorkInfoBean> getData() {
        return data;
    }

    public void setData(List<WorkInfoBean> data) {
        this.data = data;
    }
}
