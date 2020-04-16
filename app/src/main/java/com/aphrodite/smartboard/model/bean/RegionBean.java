package com.aphrodite.smartboard.model.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Aphrodite on 20-4-15
 */
public class RegionBean implements Parcelable {
    private int code;
    private String tw;
    private String en;
    private String locale;
    private String zh;

    public RegionBean() {
    }

    protected RegionBean(Parcel in) {
        code = in.readInt();
        tw = in.readString();
        en = in.readString();
        locale = in.readString();
        zh = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(code);
        dest.writeString(tw);
        dest.writeString(en);
        dest.writeString(locale);
        dest.writeString(zh);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RegionBean> CREATOR = new Creator<RegionBean>() {
        @Override
        public RegionBean createFromParcel(Parcel in) {
            return new RegionBean(in);
        }

        @Override
        public RegionBean[] newArray(int size) {
            return new RegionBean[size];
        }
    };

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getTw() {
        return tw;
    }

    public void setTw(String tw) {
        this.tw = tw;
    }

    public String getEn() {
        return en;
    }

    public void setEn(String en) {
        this.en = en;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getZh() {
        return zh;
    }

    public void setZh(String zh) {
        this.zh = zh;
    }
}
