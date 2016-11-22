package com.mrn.soft.translatehelper.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Ruslan on 29.10.2016.
 */

public class MicrosoftData {
    public String translation = "";

    public MicrosoftData(String html) {
        Pattern pattern = Pattern.compile("_mst[c|e]2\\((.*?)\\);", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            String data = matcher.group(1);
            try {
                JSONArray json = new JSONArray(data);
                translation = json.getJSONObject(0).getString("TranslatedText");
            } catch (JSONException e) {
                try {
                    JSONArray str = new JSONArray("["+data+"]");
                    translation = str.getString(0);
                } catch (JSONException e1) {
                    translation = data;
                    //e1.printStackTrace();
                }
                //e.printStackTrace();
            }
        }
    }
}
