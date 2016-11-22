package com.mrn.soft.translatehelper.models;

import android.util.Log;
import android.util.Xml;

import com.mrn.soft.translatehelper.Utils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Ruslan Grimov on 11.10.2016.
 */
public class TranslateData {
    public String translation  = "";
    public ArrayList<TranslateDataGroup> groups = new ArrayList<TranslateDataGroup>();

    protected String getTextFromXml(String xml) {
        String result = "";

        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(new StringReader(xml));
            parser.nextTag();

            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                if (parser.getName().equals("result")) {
                    parser.next();
                    result = parser.getText();
                    break;
                }
            }

        } catch (XmlPullParserException e) {
            Utils.onError(e);
        } catch (IOException e) {
            Utils.onError(e);
        }

        return result;
    }

    protected TranslateDataGroup parseGroup(XmlPullParser parser) {
        TranslateDataGroup dg = new TranslateDataGroup();
        int d = parser.getDepth();
        boolean isTr = false;
        try {
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() == XmlPullParser.START_TAG) {
                    String cl = parser.getAttributeValue("", "class");
                    if (cl != null) {
                        if (cl.indexOf("source_only") > -1) {
                            parser.next();
                            dg.baseForm = parser.getText().trim();
                        } else if (cl.indexOf("ref_psp") > -1) {
                            parser.next();
                            dg.type = parser.getText();
                        } else if (cl.indexOf("otherImportantForms") > -1) {
                            parser.next();
                            String forms[] = parser.getText().replace("\n", "").split("/");
                            for (int i = 0; i < forms.length; i++) {
                                dg.forms.add(forms[i].trim());
                            }
                        } else if (cl.indexOf("ref_result") > -1) {
                            isTr = true;
                            while (parser.next() != XmlPullParser.TEXT) {

                            }
                            dg.translations.add(parser.getText().trim());
                        }
                    }
                } else if (parser.getEventType() == XmlPullParser.END_TAG) {
                    if (isTr && parser.getName().equals("div")) {
                        break;
                    }
                }


            }
        } catch (XmlPullParserException e) {
            Utils.onError(e);
        } catch (IOException e) {
            Utils.onError(e);
        }
        return dg;
    }

    public TranslateData(String html) {
        String text = getTextFromXml(html);
        if (text != null) {
            if (text.indexOf("<style") > -1) { //this is a html with info of a word
                XmlPullParser parser = Xml.newPullParser();
                try {
                    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    //It is not a good idea to parse a HTML with the XML parser, but in this case it works
                    //provided that next option is enabled
                    parser.setFeature("http://xmlpull.org/v1/doc/features.html#relaxed", true);
                    parser.setInput(new StringReader(text));
                    parser.nextTag();

                    while (parser.next() != XmlPullParser.END_DOCUMENT) {
                        if (parser.getEventType() != XmlPullParser.START_TAG) {
                            continue;
                        }
                        if (parser.getName().equals("div")) {
                            String cl = parser.getAttributeValue("", "class");
                            if ((cl != null) && (cl.indexOf("cforms_result") > -1)) {
                                groups.add(parseGroup(parser));
                            }
                        }
                    }

                } catch (XmlPullParserException e) {
                    Utils.onError(e);
                } catch (IOException e) {
                    Utils.onError(e);
                }
            } else { //this is a translation of a sentence
                translation = text;
            }
        }

    }
}
