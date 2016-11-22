package com.mrn.soft.translatehelper.models;

import com.mrn.soft.translatehelper.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BooksGoogleData {
    public NGram root = new NGram("", 0.0);

    public BooksGoogleData(String html) {
        Pattern pattern = Pattern.compile("data[\\s]*=[\\s]*(\\[\\{.*?\\}\\])", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            String data = matcher.group(1);
            try {
                JSONArray json = new JSONArray(data);
                for (int k = 0; k < json.length(); k ++) {
                    JSONObject item = json.getJSONObject(k);
                    String parent = item.getString("parent");
                    String text = item.getString("ngram").replace(" &#39;", "'");
                    Double v = item.getJSONArray("timeseries").getDouble(0);
                    NGram ngram = new NGram(text, v);
                    root.add(parent, ngram);
                    if (text.indexOf(" not") > -1) {
                        ngram = new NGram(text.replace(" not", "n't"), v);
                        root.add(parent, ngram);
                        ngram = new NGram(text.replace(" not", "'t"), v);
                        root.add(parent, ngram);
                    }

                }
            } catch (JSONException e) {
                Utils.onError(e);
            }
        }
    }

}
