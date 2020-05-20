package com.aphrodite.smartboard.model.bean;

import java.util.List;

/**
 * @Author szh
 * @Date 2019-09-25
 * @Description
 */
public class CW {
    private String VERSION;
    private List<Integer> RESOLUTION;
    private String AUDIO;
    private List<CWPage> PAGES;
    private List<CWACT> ACT;
    private String author;
    private long time;
    private long editTime;
    private String dpi;

    public String getVERSION() {
        return VERSION;
    }

    public void setVERSION(String VERSION) {
        this.VERSION = VERSION;
    }

    public List<Integer> getRESOLUTION() {
        return RESOLUTION;
    }

    public void setRESOLUTION(List<Integer> RESOLUTION) {
        this.RESOLUTION = RESOLUTION;
    }

    public String getAUDIO() {
        return AUDIO;
    }

    public void setAUDIO(String AUDIO) {
        this.AUDIO = AUDIO;
    }

    public List<CWPage> getPAGES() {
        return PAGES;
    }

    public void setPAGES(List<CWPage> PAGES) {
        this.PAGES = PAGES;
    }

    public List<CWACT> getACT() {
        return ACT;
    }

    public void setACT(List<CWACT> ACT) {
        this.ACT = ACT;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getEditTime() {
        return editTime;
    }

    public void setEditTime(long editTime) {
        this.editTime = editTime;
    }

    public String getDpi() {
        return dpi;
    }

    public void setDpi(String dpi) {
        this.dpi = dpi;
    }
}
