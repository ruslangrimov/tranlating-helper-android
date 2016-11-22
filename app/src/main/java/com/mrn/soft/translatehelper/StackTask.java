package com.mrn.soft.translatehelper;

import android.os.AsyncTask;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ruslan Grimov on 16.10.2016.
 */
public class StackTask extends AsyncTask<ArrayList<ArrayList<String>>, HttpTaskResult, String> {

    protected List<OnStackTaskListener> listeners = new ArrayList<OnStackTaskListener>();

    protected int waitInterval = 0;

    public StackTask(int waitInterval) {
        super();
        this.waitInterval = waitInterval;
    }

    public void setListener(OnStackTaskListener toAdd) {
        listeners.add(toAdd);
    }

    public void removeListener(OnStackTaskListener toDel) {
        listeners.remove(toDel);
    }

    @Override
    protected String doInBackground(ArrayList<ArrayList<String>>... atasks) {
        ArrayList<ArrayList<String>> tasks = atasks[0];
        for (ArrayList<String> task : tasks) {
            String tag = task.get(0);
            String post = (task.size() > 2) ? task.get(2) : "";
            String tag2 = (task.size() > 3) ? task.get(3) : tag;

            //Wait named semaphore for tag
            TaskFragment.locks.get(tag).lock();

            HttpResult httpResult = Utils.httpQuery(task.get(1), post);
            if (httpResult.code == 0) {
                publishProgress(new HttpTaskResult(tag2, httpResult.answer));
            }
            try {
                Thread.sleep(waitInterval);
            } catch (InterruptedException e) {
                final String err = "ST: " + e.getMessage();
                MainActivity.ctx.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.ctx, err, Toast.LENGTH_SHORT).show();
                    }
                });
                //e.printStackTrace();
            }
            //Unlock named semaphore for tag
            TaskFragment.locks.get(tag).unlock();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(HttpTaskResult... values) {
        for (OnStackTaskListener hl : listeners) {
            hl.OnTask(values[0].tag, values[0].page);
        }
    }

}
