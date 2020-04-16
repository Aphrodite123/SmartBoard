package com.aphrodite.smartboard.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Aphrodite on 20-4-15
 */
public class ParseUtil {
    public interface CodeFormat {
        String UTF8 = "UTF-8";
    }

    public static String getAssetsJson(Context context, String fileName) {
        if (null == context || TextUtils.isEmpty(fileName)) {
            return null;
        }

        AssetManager assetManager = context.getResources().getAssets();
        if (null == assetManager) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        try {
            InputStreamReader streamReader = new InputStreamReader(assetManager.open(fileName), CodeFormat.UTF8);
            BufferedReader reader = new BufferedReader(streamReader);
            String line;
            while (null != (line = reader.readLine())) {
                builder.append(line);
            }
            reader.close();
        } catch (IOException e) {
            LogUtil.e("Enter to getAssetsJson. " + e);
        }
        return builder.toString();
    }

}
