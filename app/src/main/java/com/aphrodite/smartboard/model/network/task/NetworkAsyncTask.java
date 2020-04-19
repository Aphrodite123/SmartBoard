package com.aphrodite.smartboard.model.network.task;

import android.content.Context;
import android.os.AsyncTask;

import com.aphrodite.smartboard.model.network.WebServiceUtils;
import com.aphrodite.smartboard.model.network.inter.IResponseListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aphrodite on 20-4-17
 */
public class NetworkAsyncTask extends AsyncTask {
    private String method;//方法
    private String[] key;//参数key
    private List<String> values;//参数值
    private List<String> list = new ArrayList<>();//返回结果
    private Object mObj;
    private Context context;
    private IResponseListener mResponseListener;

    public NetworkAsyncTask() {
    }

    public NetworkAsyncTask(String method, String[] key, List<String> values, Object mObj, Context context, IResponseListener listener) {
        this.method = method;
        this.key = key;
        this.values = values;
        this.mObj = mObj;
        this.context = context;
        this.mResponseListener = listener;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        return WebServiceUtils.getWebServiceResult(method, key, values, list, context);
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        if (null != mResponseListener) {
            mResponseListener.result(method, list, o);
        }
    }

}
