package com.mrn.soft.translatehelper.models;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReversoData {
    public String phrase; //
    public ArrayList<String> translations = new ArrayList<String>();
    public ArrayList<PhraseExample> examples = new ArrayList<PhraseExample>(); //

    public ReversoData(String html) {
        //Extraction of data by parsing the html page

        //Extract phrase
        Pattern pattern = Pattern.compile("id=\"entry\" value=\"([^\"]*?)\"", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            phrase = matcher.group(1);
        }

        //Extract translations
        pattern = Pattern.compile("<em class='translation'>(.*?)<\\/em>", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(html);
        while (matcher.find()) {
            translations.add(matcher.group(1));
        }

        //Extract examples and their translations
        //pattern = Pattern.compile("<span class=\"text\"[^>]*?>\\s*(.*?)\\s*<\\/div>", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        pattern = Pattern.compile("<div class=\"[^\"]*ltr\">\\s*.*\\s*<span class=\"text\"[^>]*>\\s*(.*?)\\s*<\\/div>", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

        matcher = pattern.matcher(html);
        while (matcher.find()) {
            String pFrom = remTags(matcher.group(1));
            if (matcher.find()) {
                String pTo = remTags(matcher.group(1));
                examples.add(new PhraseExample(pFrom, pTo));
            }
        }

    }

    protected String remTags(String s) {
        Map<String, String> t = new LinkedHashMap<>();
        t.put("<\\/div>", "");
        t.put("<\\/span>", "");
        t.put("<div class=\"text\">\\s*", "");
        t.put("<span class=\"text\">\\s*", "");
        t.put("<\\/a>", "");
        t.put("<a[^>]*?>", "");
        t.put("<em>(.*?)<\\/em>", "<a href=\"$1\">$1</a>");

        for (Map.Entry<String, String> pair : t.entrySet()) {
            s = Pattern.compile(pair.getKey().toString(), Pattern.MULTILINE | Pattern.CASE_INSENSITIVE).
                    matcher(s).replaceAll(pair.getValue().toString());
        }

        return s;
    }
}

