package com.mrn.soft.translatehelper.models;

import android.util.Log;

import com.mrn.soft.translatehelper.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Ruslan Grimov on 11.10.2016.
 */
public class GoogleData {
    public ArrayList<PhraseExample> phrases = new ArrayList<PhraseExample>(); //

    public ArrayList<GoogleDataGroup> groups = new ArrayList<GoogleDataGroup>();

    public GoogleData(String text) {
        try {
            JSONObject json = new JSONObject(text);
            if (json.has("dict")) {
                JSONArray dict = json.getJSONArray("dict");
                for (int i = 0; i < dict.length(); i++) {
                    JSONObject group = dict.getJSONObject(i);
                    GoogleDataGroup dg = new GoogleDataGroup();
                    dg.baseForm = group.getString("base_form");
                    dg.type = group.getString("pos");

                    JSONArray entries = group.getJSONArray("entry");
                    for (int j = 0; j < entries.length(); j++) {
                        JSONObject entry = entries.getJSONObject(j);
                        GoogleDataGroupItem dgi = new GoogleDataGroupItem();
                        dgi.word = entry.getString("word");

                        JSONArray rt = entry.getJSONArray("reverse_translation");
                        for (int n = 0; n < rt.length(); n++) {
                            dgi.translations.add(rt.getString(n));
                        }

                        dg.translations.add(dgi);
                    }

                    groups.add(dg);
                }
            }

            if (json.has("sentences")) {
                JSONArray sent = json.getJSONArray("sentences");
                for (int i = 0; i < sent.length(); i ++) {
                    JSONObject s = sent.getJSONObject(i);
                    phrases.add(new PhraseExample(s.getString("orig"), s.getString("trans")));
                }
            }
        } catch (JSONException e) {
            Utils.onError(e);
        }
    }
}
