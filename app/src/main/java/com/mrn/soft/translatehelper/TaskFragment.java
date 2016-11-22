package com.mrn.soft.translatehelper;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This code is based on code offered here http://www.androiddesignpatterns.com/2013/04/retaining-objects-across-config-changes.html
 */
public class TaskFragment extends Fragment implements OnHttpDataListener, OnStackTaskListener {

    private OnHttpDataListener listener;

    private HttpTaskResult lastHttpTaskResult = null;

    public TaskFragment() {
        // Required empty public constructor
    }

    public static HashMap<String, Lock> locks = new HashMap<String, Lock>();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (OnHttpDataListener) activity;
        //If Activity wasn't in good state when the OnHttpData was called we have to give it the last result
        if (lastHttpTaskResult != null) {
            listener.OnHttpData(lastHttpTaskResult.tag, lastHttpTaskResult.page);
            lastHttpTaskResult = null;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public void getPage(String tag, String url, String post) {
        lastHttpTaskResult = null;
        GetHttpDataTask task = new GetHttpDataTask();
        task.setListener(this);
        if (post.equals("")) {
            task.execute(tag, url);
        } else {
            task.execute(tag, url, post);
        }
    }

    public void getPages(ArrayList<ArrayList<String>> tasks, int waitInterval) {
        StackTask task = new StackTask(waitInterval);
        for (ArrayList<String> t : tasks) {
            String tag = t.get(0);
            if (!locks.containsKey(tag)) {
                locks.put(tag, new ReentrantLock());
            }
        }
        task.setListener(this);
        task.execute(tasks);
    }

    @Override
    public void OnHttpData(String tag, String page) {
        //if Activity is in process of rebuilding the fragment has to remember result and deliver it when it is possible
        if (listener == null) {
            lastHttpTaskResult = new HttpTaskResult(tag, page);
        } else { //if Activity in good state then this fragment just redirect result to it
            listener.OnHttpData(tag, page);
        }
    }

    @Override
    public void OnTask(String tag, String page) {
        if (listener == null) {
            lastHttpTaskResult = new HttpTaskResult(tag, page);
        } else { //if Activity in good state then this fragment just redirect result to it
            listener.OnHttpData(tag, page);
        }
    }
}
