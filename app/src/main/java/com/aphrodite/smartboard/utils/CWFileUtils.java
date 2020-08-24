package com.aphrodite.smartboard.utils;

import android.graphics.Point;
import android.text.TextUtils;

import com.apeman.sdk.bean.DevicePoint;
import com.aphrodite.framework.utils.ObjectUtils;
import com.aphrodite.framework.utils.SPUtils;
import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.application.MainApplication;
import com.aphrodite.smartboard.config.AppConfig;
import com.aphrodite.smartboard.model.bean.CW;
import com.aphrodite.smartboard.model.bean.CWACT;
import com.aphrodite.smartboard.model.bean.CWBackground;
import com.aphrodite.smartboard.model.bean.CWLine;
import com.aphrodite.smartboard.model.bean.CWPage;
import com.aphrodite.smartboard.model.bean.CWSwitch;
import com.aphrodite.smartboard.model.bean.ScreenRecordEntity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author szh
 * @Date 2019-09-23
 * @Description
 */
public class CWFileUtils {
    public static StringBuilder actStringBuilder = new StringBuilder();
    public static int mRecordSeconds;

    public static void write(List<ScreenRecordEntity> data, String filePath, int width, int height, long timestamp) {
        File path = new File(filePath);
        if (!path.exists()) {
            path.mkdirs();
        }

        File file = new File(filePath + AppConfig.DATA_FILE_NAME);

        try {
            Writer wr = new FileWriter(file, true);
            wr.write("#VERSION:1.0.0");
            wr.write("\n");
            wr.write("#RESOLUTION:[" + width + "," + height + "]");
            wr.write("\n");
            wr.write("#AUDIO:audio.mp3");
            wr.write("\n");
            List<CWPage> PAGES = new ArrayList<>();
            for (ScreenRecordEntity screenRecordEntity : data) {
                CWPage cwPage = new CWPage();
                CWBackground background;
                if ("data/0".equals(screenRecordEntity.getType())) {
                    background = new CWBackground();
                    background.setRgba("255,255,255,1");
                } else {
                    background = new CWBackground();
                    File file1 = new File(screenRecordEntity.getPath());
                    background.setUrl(file1.getName());
                }
                cwPage.setBackground(background);
                PAGES.add(cwPage);
            }
            Gson gson = new Gson();
            wr.write("#PAGES:" + gson.toJson(PAGES));
            wr.write("\n");
            wr.write("#AUTHOR:" + SPUtils.get(AppConfig.SharePreferenceKey.PHONE_NUMBER, MainApplication.getApplication().getString(R.string.app_name)));
            wr.write("\n");
            wr.write("#TIME:" + timestamp);
            wr.write("\n\n");
            wr.write(actStringBuilder.toString());
            wr.write("\n");
            wr.close();

            actStringBuilder = null;
            actStringBuilder = new StringBuilder();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setSeconds(int seconds) {
        mRecordSeconds = seconds;
    }

    public static void writeACTLine(List<Point> points, int width, String color) {
        int len = points.size();
        if (len == 0) {
            return;
        }
        int minute = 0, second = 0;
        if (mRecordSeconds >= 60) {
            minute = mRecordSeconds / 60;
            second = mRecordSeconds % 60;
        } else {
            second = mRecordSeconds;
        }
        String timeTip = minute + ":" + (second < 10 ? "data/0" + second : second + "") + ".000";
        actStringBuilder.append("#ACT:" + timeTip + ",line");
        actStringBuilder.append("\n");
        actStringBuilder.append("{\"width\":" + width + ",\"color\":\"" + color + "\",\"points\":[");
        for (int i = 0; i < len; i++) {
            actStringBuilder.append((i == 0 ? "[" : ",[") + points.get(i).x + "," + points.get(i).y + "]");
        }
        actStringBuilder.append("]}");
        actStringBuilder.append("\n");
    }

    public static void writeLine(List<DevicePoint> points, int width, String color) {
        int len = points.size();
        if (len == 0) {
            return;
        }
        int minute = 0, second = 0;
        if (mRecordSeconds >= 60) {
            minute = mRecordSeconds / 60;
            second = mRecordSeconds % 60;
        } else {
            second = mRecordSeconds;
        }
        String timeTip = minute + ":" + (second < 10 ? "data/0" + second : second + "") + ".000";
        actStringBuilder.append("#ACT:" + timeTip + ",line");
        actStringBuilder.append("\n");
        actStringBuilder.append("{\"width\":" + width + ",\"color\":\"" + color + "\",\"points\":[");
        int x;
        int y;
        int pressure;
        for (int i = 0; i < len; i++) {
            x = points.get(i).getX();
            y = points.get(i).getY();
            pressure = points.get(i).getPressure();
            actStringBuilder.append((i == 0 ? "[" : ",[") + x + "," + y + "," + pressure + "]");
        }
        actStringBuilder.append("]}");
        actStringBuilder.append("\n");
    }

    public static void writeACTSwitch(int index) {
        int minute = 0, second = 0;
        if (mRecordSeconds >= 60) {
            minute = mRecordSeconds / 60;
            second = mRecordSeconds % 60;
        } else {
            second = mRecordSeconds;
        }
        String timeTip = minute + ":" + (second < 10 ? "data/0" + second : second + "") + ".000";
        actStringBuilder.append("#ACT:" + timeTip + ",switch");
        actStringBuilder.append("\n");
        actStringBuilder.append("{\"index\":" + index + "}");
        actStringBuilder.append("\n");
    }

    public static void writeACTClear() {
        int minute = 0, second = 0;
        if (mRecordSeconds >= 60) {
            minute = mRecordSeconds / 60;
            second = mRecordSeconds % 60;
        } else {
            second = mRecordSeconds;
        }
        String timeTip = minute + ":" + (second < 10 ? "data/0" + second : second + "") + ".000";
        actStringBuilder.append("#ACT:" + timeTip + ",clear");
        actStringBuilder.append("\n");
    }

    public static CW read(String path) {
        File file = new File(path);
        CW cw = new CW();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String result;
            String[] splitResult;
            Gson gson = new Gson();
            List<CWACT> cwacts = new ArrayList<>();
            while ((result = bufferedReader.readLine()) != null) {
                if (result.contains("VERSION")) {
                    splitResult = result.split("\\:");
                    cw.setVERSION(splitResult[1]);
                } else if (result.contains("RESOLUTION")) {
                    splitResult = result.split("\\:");
                    String[] screen = splitResult[1].split("\\,");
                    List<Integer> integers = new ArrayList<>();
                    integers.add(Integer.valueOf(screen[0].substring(1)));
                    integers.add(Integer.valueOf(screen[1].substring(0, screen[1].length() - 1)));
                    cw.setRESOLUTION(integers);
                } else if (result.contains("AUDIO")) {
                    splitResult = result.split("\\:");
                    cw.setAUDIO(splitResult[1]);
                } else if (result.contains("PAGES")) {
                    List<CWPage> cwPages;
                    CWPage[] cwPage = gson.fromJson(result.substring(7), CWPage[].class);
                    cwPages = Arrays.asList(cwPage);
                    cw.setPAGES(cwPages);
                } else if (result.contains("AUTHOR")) {
                    splitResult = result.split("\\:");
                    cw.setAuthor(splitResult[1]);
                } else if (result.contains("TIME")) {
                    splitResult = result.split("\\:");
                    cw.setTime(Long.parseLong(splitResult[1]));
                } else if (result.contains("ACT")) {
                    splitResult = result.split("\\,");
                    CWACT cwact = new CWACT();
                    cwact.setTime(TimeUtils.convertTime(splitResult[0].substring(5)));
                    cwact.setAction(splitResult[1]);
                    if ("line".equals(splitResult[1])) {
                        result = bufferedReader.readLine();
                        cwact.setLine(gson.fromJson(result, CWLine.class));
                    } else if ("switch".equals(splitResult[1])) {
                        result = bufferedReader.readLine();
                        cwact.setCwSwitch(gson.fromJson(result, CWSwitch.class));
                    }
                    cwacts.add(cwact);
                }
            }
            cw.setACT(cwacts);
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cw;
    }

    public static List<List<Object>> readOnLineFile(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }

        File file = new File(path);
        if (!file.exists()) {
            return null;
        }

        List<List<Object>> points = new ArrayList<>();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String result;
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Integer>>() {
            }.getType();
            while ((result = bufferedReader.readLine()) != null) {
                result = bufferedReader.readLine();
                if (ObjectUtils.isEmpty(result)) {
                    continue;
                }

                ArrayList<Integer> arrayList = gson.fromJson(result, type);
                Object[] coordinates = DataTransferUtils.splitArray(arrayList, 3);
                points.add(Arrays.asList(coordinates));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return points;
    }
}
