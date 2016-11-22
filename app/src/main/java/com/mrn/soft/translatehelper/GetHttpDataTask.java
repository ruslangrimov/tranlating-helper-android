package com.mrn.soft.translatehelper;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;


public class GetHttpDataTask extends AsyncTask<String, Integer, HttpTaskResult> {

    private List<OnHttpDataListener> listeners = new ArrayList<OnHttpDataListener>();

    public void setListener(OnHttpDataListener toAdd) {
        listeners.add(toAdd);
    }

    public void removeListener(OnHttpDataListener toDel) {
        listeners.remove(toDel);
    }

    @Override
    protected HttpTaskResult doInBackground(String... strings) {
        String tag = strings[0];
        String surl = strings[1];
        String post = "";
        String page = "";

        if (strings.length > 2) {
            post = strings[2];
        }

        HttpResult httpResult = Utils.httpQuery(surl, post);

        return new HttpTaskResult(strings[0], httpResult.answer);
    }

    @Override
    protected void onPostExecute(HttpTaskResult httpTaskResult) {
        for (OnHttpDataListener hl : listeners) {
            hl.OnHttpData(httpTaskResult.tag, httpTaskResult.page);
        }
    }
}
