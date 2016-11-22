package com.mrn.soft.translatehelper;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class Utils {
    protected static CookieManager cookieManager = null;

    /**
     * Get the content of an asset file as UTF-8 encoded text
     *
     * @param ctx Context
     * @param filename String
     * @return String
     * @throws IOException
     */

    public static String getAssetFileContent(Context ctx, String filename) throws IOException {
        StringBuffer sb = new StringBuffer();
        InputStream rs = ctx.getAssets().open(filename);
        InputStreamReader reader = new InputStreamReader(rs, "UTF-8");
        char[] b = new char[1024];
        int readed;
        while ((readed = reader.read(b)) > -1) {
            sb.append(b, 0, readed);
        }
        reader.close();
        return sb.toString();
    }

    // The code snipped is taken from http://stackoverflow.com/a/5261472
    // @UiThread
    public static String getDefaultUserAgentString(Context context) {
        if (Build.VERSION.SDK_INT >= 17) {
            return NewApiWrapper.getDefaultUserAgent(context);
        }

        try {
            Constructor<WebSettings> constructor = WebSettings.class.getDeclaredConstructor(Context.class, WebView.class);
            constructor.setAccessible(true);
            try {
                WebSettings settings = constructor.newInstance(context, null);
                return settings.getUserAgentString();
            } finally {
                constructor.setAccessible(false);
            }
        } catch (Exception e) {
            return new WebView(context).getSettings().getUserAgentString();
        }
    }

    protected static void initCookie() {
        if (cookieManager == null) {
            cookieManager = new CookieManager();
            CookieHandler.setDefault(cookieManager);
        }
    }

    public static HttpResult httpQuery(String surl, String post) {
        String answer = "";
        int code = 0;
        String status = "";

        initCookie();

        //The purpose of this project was to create an application as light as possible so
        //I used lightweight HttpURLConnection instead of HttpClient
        try {
            URL url = new URL(surl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            if (post.length() > 0) {
                urlConnection.setDoOutput(true);
                //urlConnection.setChunkedStreamingMode(0);

                Writer out = new PrintWriter(urlConnection.getOutputStream());
                out.write(post); //out.write(post.getBytes(Charset.forName("UTF-8")));
                out.flush();
            }

            ByteArrayOutputStream result = new ByteArrayOutputStream();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            byte[] buffer = new byte[1024];
            int length;
            try {
                while ((length = in.read(buffer)) != -1) {
                    result.write(buffer, 0, length);
                }
            } catch (IOException e) {

            }
            answer = result.toString("UTF-8");
            urlConnection.disconnect();

        } catch (MalformedURLException e) {
            code = -1;
            status = e.getMessage();
            Utils.onError(e);
        } catch (IOException e) {
            code = -2;
            status = e.getMessage();
            Utils.onError(e);
        }

        return new HttpResult(code, status, answer);
    }

    @TargetApi(17)
    static class NewApiWrapper {
        static String getDefaultUserAgent(Context context) {
            return WebSettings.getDefaultUserAgent(context);
        }
    }

    public static void onError(final Throwable e) {
        StringBuilder b = new StringBuilder();
        StackTraceElement[] s = e.getStackTrace();
        b.append(s[0].getFileName()).append(":").
        append(s[0].getLineNumber()).append(":").
        append((e.getMessage() == null) ? "null" : e.getMessage());
        final String txt = b.toString();
        Log.d("MyApp", txt);

        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toast.makeText(MainActivity.ctx, txt, Toast.LENGTH_LONG).show();
        } else {
            MainActivity.ctx.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.ctx, txt, Toast.LENGTH_SHORT).show();
                }
            });
        }
        Log.d("MyApp", "afterOnError");
    }

    public static int dpToPx(int dp) {
        DisplayMetrics displayMetrics = MainActivity.ctx.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public static String serializeNGrams(Map<String, Double> ngrams) {
        StringBuilder b = new StringBuilder();
        for (Map.Entry<String, Double> pair : ngrams.entrySet()) {
            b.append(pair.getKey()).append("|").append(pair.getValue()).append("\n");
        }
        return b.toString();
    }

    public static void unserializeNGrams(Map<String, Double> ngrams, String s) {
        if (!s.isEmpty()) {
            String[] lines = s.split("\n");

            for (int i = 0; i < lines.length; i++) {
                if (!lines[i].trim().isEmpty()) {
                    String[] v = lines[i].split("\\|");
                    ngrams.put(v[0], new Double(v[1]));
                }
            }
        }
    }

}
