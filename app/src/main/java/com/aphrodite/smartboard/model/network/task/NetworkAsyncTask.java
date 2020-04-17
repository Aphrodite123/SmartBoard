package com.aphrodite.smartboard.model.network.task;

import android.content.Context;
import android.os.AsyncTask;

import com.aphrodite.smartboard.model.network.WebServiceUtils;

import java.util.List;

/**
 * Created by Aphrodite on 20-4-17
 */
public class NetworkAsyncTask extends AsyncTask {
    String method;//方法
    String[] key;//参数key
    List<String> values;//参数值
    List<String> list;//返回结果
    Object mObj;
    Context context;
//    public interface

    public NetworkAsyncTask(String method, String[] key, List<String> values, List<String> list, Object mObj, Context context) {
        this.method = method;
        this.key = key;
        this.values = values;
        this.list = list;
        this.mObj = mObj;
        this.context = context;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        return WebServiceUtils.getWebServiceResult(method, key, values, list, context);
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
    }

}
