package com.mrn.soft.translatehelper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Editable;
import android.text.Spannable;
import android.text.style.LineBackgroundSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Ruslan Grimov on 18.10.2016.
 */
public class MainEditText extends EditText {
    protected class NValue {
        int words;
        double val;

        NValue(int words, double val) {
            this.words = words;
            this.val = val;
        }
    }

    protected class BgSpan implements LineBackgroundSpan {
        private int wx;
        private int start;
        private int end;

        public BgSpan(int wx, int start, int end) {
            super();
            this.wx = wx;
            this.start = start;
            this.end = end;
        }

        @Override
        public void drawBackground(Canvas c, Paint p, int left, int right, int top, int baseline, int bottom, CharSequence text, int start, int end, int lnum) {
            final int oldColor = p.getColor();
            final int m = (bottom - top) / 6;

            int rLeft = this.start > start ? (int)p.measureText(text, start, this.start) : 0;
            int rRight = this.end > end ? right : (int)p.measureText(text, start, this.end);

            if (wx == 3) {
                p.setColor(0x18FFFF00);
                c.drawRect(rLeft, top + m, rRight, bottom - m, p);
            } else if (wx == 4) {
                p.setColor(0x1800FF00);
                c.drawRect(rLeft, top, rRight, top + m, p);
            } else if (wx == 5) {
                p.setColor(0x180000FF);
                c.drawRect(rLeft, bottom - m, rRight, bottom, p);
            }
            //c.drawRect(new Rect(rLeft, top, rRight, bottom), p);
            p.setColor(oldColor);
        }
    }

    protected HashMap<String, NValue> ngrams = new HashMap<String, NValue>();

    public MainEditText(Context context) {
        super(context);
    }

    public MainEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MainEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void addNGrmas(HashMap<String, Double> ngrams) {
        boolean added = false;
        for (Map.Entry<String, Double> pair : ngrams.entrySet()) {
            if (pair.getValue() > 0) {
                added = true;
                this.ngrams.put(pair.getKey().replaceAll("([\\s]{1})", "[\\\\W]+"),
                    new NValue(pair.getKey().split(" ").length, pair.getValue()));
            }
        }
        if (added) {
            updateSpans();
        }
    }

    /*public MainEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }*/

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        super.onTextChanged(s, start, before, count);
        updateSpans();
    }

    protected void updateSpans() {
        Editable drawText = getText();
        String txt = drawText.toString();

        BgSpan[] mySpans = drawText.getSpans(0, drawText.length(), BgSpan.class);
        for (int i = 0; i < mySpans.length; i ++) {
            drawText.removeSpan(mySpans[i]);
        }

        if (txt.length() > 0) {
            if (ngrams.size() > 0) {
                for (Map.Entry<String, NValue> pair : ngrams.entrySet()) {
                    Pattern pattern = Pattern.compile(pair.getKey());
                    Matcher matcher = pattern.matcher(txt);
                    while (matcher.find()) {
                        int wx = pair.getValue().words;
                        int start = matcher.start();
                        int end = matcher.end();
                        drawText.setSpan(new BgSpan(wx, start, end), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }
        }
    }

}
