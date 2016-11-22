package com.mrn.soft.translatehelper.models;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Ruslan Grimov on 16.10.2016.
 */

public class NGram {
    public String ngram;
    public Double val;
    public TreeMap<Double, NGram> children = new TreeMap<Double, NGram>(Collections.reverseOrder());

    public NGram(String ngram, Double val) {
        this.ngram = ngram;
        this.val = val;
    }

    public boolean add(String parent, NGram ngram) {
        boolean res = false;
        if (this.ngram.equals(parent)) {
            Double key = ngram.ngram.split("\\s").length + ngram.val;
            while (children.containsKey(key)) { //Hack. If two different ngram have the same number of words and frequency
                key *= 1.001;
            }
            children.put(key, ngram);
            res = true;
        } else {
            for (Map.Entry<Double, NGram> child : children.entrySet()) {
                res = child.getValue().add(parent, ngram);
                if (res) {
                    break;
                }
            }
        }

        return res;
    }

    protected void getAll(HashMap<String, Double> m) {
        for (Map.Entry<Double, NGram> child : children.entrySet()) {
            m.put(child.getValue().ngram, child.getValue().val);
            child.getValue().getAll(m);
        }
    }

    public HashMap<String, Double> getAll() {
        HashMap<String, Double> m = new HashMap<String, Double>();
        getAll(m);
        return m;
    }
}
