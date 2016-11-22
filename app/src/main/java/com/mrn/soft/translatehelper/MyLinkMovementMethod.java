package com.mrn.soft.translatehelper;

import android.text.Layout;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ruslan Grimov on 07.10.2016.
 */
public class MyLinkMovementMethod extends LinkMovementMethod {
    private static MyLinkMovementMethod sInstance;

    private List<OnUrlClickListener> listeners = new ArrayList<OnUrlClickListener>();

    public void setListener(OnUrlClickListener toAdd) {
        listeners.add(toAdd);
    }

    public void removeListener(OnUrlClickListener toDel) {
        listeners.remove(toDel);
    }

    public static MyLinkMovementMethod getInstance() {
        if (sInstance == null)
            sInstance = new MyLinkMovementMethod();
        return sInstance;
    }

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event ) {
        boolean mLinkClicked = false;

        int action = event.getAction();

        if (action == MotionEvent.ACTION_UP) {

            String mClickedLink = null;
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);

            if (link.length != 0) {
                URLSpan span = (URLSpan) link[0];
                for (OnUrlClickListener hl : listeners) {
                    hl.onUrlClick(span.getURL());
                }
            }
        }

        return mLinkClicked;
    }
}
