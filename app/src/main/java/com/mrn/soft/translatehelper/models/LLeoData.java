package com.mrn.soft.translatehelper.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Ruslan Grimov on 25.10.2016.
 */

public class LLeoData {
    public String phrase;
    public ArrayList<String> translations = new ArrayList<String>();

    public LLeoData(String html) {
        try {
            JSONObject json = new JSONObject(html);
            String e = json.getString("error_msg");
            if (e.length() > 0) {
                phrase = e;
            } else {
                JSONArray forms = json.getJSONArray("word_forms");
                if (forms.length() > 0) {
                    phrase = forms.getJSONObject(0).getString("word");
                }

                JSONArray t = json.getJSONArray("translate");
                for (int i = 0; i < t.length(); i ++) {
                    translations.add(t.getJSONObject(i).getString("value"));
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
