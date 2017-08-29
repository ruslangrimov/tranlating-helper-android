package com.mrn.soft.translatehelper;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.app.FragmentManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mrn.soft.translatehelper.models.*;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,
    OnHttpDataListener {

    public static String PACKAGE_NAME;

    public static Activity ctx;

    private class Txt {
        Txt (EditText edit) {
            txt = edit.getText().toString();
            isEn = (txt.length() == 0) || (Character.codePointAt(txt, 0) < 127);
        };
        boolean isEn;
        String txt;
    }

    private class WordState {
        WordState (int i, String word) {
            this.i = i;
            this.word = word;
        };
        int i;
        String word;
    }

    protected ReversoData rd;

    protected Spinner spinTrRes;

    protected EditText editTxtMainFrom;
    protected MainEditText editTxtMain;
    protected EditText editTxtFrom;
    protected EditText editTxtTo;

    protected FrameLayout lBottom;

    protected ImageButton btnTrRefresh;
    protected ImageView imgMTRefresh;

    protected LinearLayout lLoading;
    protected ImageView imgLoader;

    protected LinearLayout lReverso;
    protected TextView txtReversoPhrase;
    protected TextView txtReversoTrs;
    protected LinearLayout listReversoExamples;

    protected LinearLayout lGoogle;

    protected LinearLayout lTranslate;

    protected LinearLayout lHints;
    protected LinearLayout hintsTop;
    protected LinearLayout listHints;
    protected Spinner spinGBPref;
    protected Spinner spinGBTags;

    protected LinearLayout lLLeo;

    protected LinearLayout lMicrosoft;

    protected ArrayList<View> lList = new ArrayList<View>();
    protected ArrayList<Method> lActions = new ArrayList<Method>();

    protected MyLinkMovementMethod linkFrom;
    protected MyLinkMovementMethod linkTo;

    private static final String TAG_TASK_FRAGMENT = "task_fragment";
    protected TaskFragment taskFragment;

    protected int fontSize;

    protected Stack<WordState> wordsStack = new Stack<WordState>();

    protected HashMap<String, Double> ngrams = new HashMap<String, Double>();

    protected HashMap<Integer, ArrayList<String>> ngBatches = new
        HashMap<Integer, ArrayList<String>>();

    protected boolean fromIntent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PreferenceManager.setDefaultValues(this, R.xml.main, false);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String font = prefs.getString("fontsize", getResources().getString(R.string.fontsize_default_value));
        fontSize = new Integer(font);
        int themeId = getResources().getIdentifier("Font" + (new Integer(fontSize)).toString(), "style", "com.mrn.soft.translatehelper");
        setTheme(themeId);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PACKAGE_NAME = getApplicationContext().getPackageName();
        ctx = this;

        System.setProperty("http.agent", Utils.getDefaultUserAgentString(this));

        spinTrRes = (Spinner) findViewById(R.id.spinTrRes);

        editTxtMainFrom = (EditText) findViewById(R.id.editTxtMainFrom);
        editTxtMain = (MainEditText) findViewById(R.id.editTxtMain);
        editTxtFrom = (EditText) findViewById(R.id.editTxtFrom);
        editTxtTo = (EditText) findViewById(R.id.editTxtTo);

        lBottom = (FrameLayout) findViewById(R.id.lBottom);

        btnTrRefresh = (ImageButton) findViewById(R.id.btnTrRefresh);
        imgMTRefresh = (ImageView) findViewById(R.id.imgMTRefresh);

        lLoading = (LinearLayout) findViewById(R.id.lLoading);
        imgLoader = (ImageView) findViewById(R.id.imgLoader);

        //listReversoExamples = (ListView) findViewById(R.id.listReversoExamples);
        lReverso = (LinearLayout) findViewById(R.id.lReverso);
        txtReversoPhrase = (TextView) findViewById(R.id.txtReversoPhrase);
        txtReversoTrs = (TextView) findViewById(R.id.txtReversoTrs);
        listReversoExamples = (LinearLayout) findViewById(R.id.listReversoExamples);

        lGoogle = (LinearLayout) findViewById(R.id.lGoogle);

        lTranslate = (LinearLayout) findViewById(R.id.lTranslate);

        lHints = (LinearLayout) findViewById(R.id.lHints);
        hintsTop = (LinearLayout) findViewById(R.id.hintsTop);
        listHints = (LinearLayout) findViewById(R.id.listHints);
        spinGBPref = (Spinner) findViewById(R.id.spinGBPref);
        spinGBTags = (Spinner) findViewById(R.id.spinGBTags);

        lLLeo = (LinearLayout) findViewById(R.id.lLLeo);

        lMicrosoft = (LinearLayout) findViewById(R.id.lMicrosoft);

        spinTrRes.setOnItemSelectedListener(this);

        lList.add(lReverso);
        lList.add(lGoogle);
        lList.add(lTranslate);
        lList.add(lHints);
        lList.add(lLLeo);
        lList.add(lMicrosoft);

        Class cl = getClass();
        try {
            lActions.add(cl.getDeclaredMethod("getReverso"));
            lActions.add(cl.getDeclaredMethod("getGoogle"));
            lActions.add(cl.getDeclaredMethod("getTranslate"));
            lActions.add(cl.getDeclaredMethod("getHints"));
            lActions.add(cl.getDeclaredMethod("getLLeo"));
            lActions.add(cl.getDeclaredMethod("getMicrosoft1"));
        } catch (NoSuchMethodException e) {
            Utils.onError(e);
        }

        linkFrom = new MyLinkMovementMethod();
        linkFrom.setListener(new OnUrlClickListener() {
            @Override
            public void onUrlClick(String URL) {
                editTxtFrom.setText(URL);
                editTxtFrom.setSelection(editTxtFrom.getText().length());
            }
        });

        linkTo = new MyLinkMovementMethod();
        linkTo.setListener(new OnUrlClickListener() {
            @Override
            public void onUrlClick(String URL) {
                editTxtTo.setText(URL);
                editTxtTo.setSelection(editTxtTo.getText().length());
            }
        });

        FragmentManager fm = getFragmentManager();
        taskFragment = (TaskFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (taskFragment == null) {
            taskFragment = new TaskFragment();
            fm.beginTransaction().add(taskFragment, TAG_TASK_FRAGMENT).commit();
        }

        showTopView();

        loadState();

        //Add ngrams from db
        editTxtMain.addNGrmas(ngrams);

        processIntent();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_prefs:
                Intent intent = new Intent().setClass(this, SettingsActivity.class);
                startActivityForResult(intent, 0);
                break;
            case R.id.menu_clear:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.t_q_clear_caption).setMessage(R.string.t_q_clear)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                clearCache();
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
                break;
            case R.id.menu_kbd_hide:
                hideSoftKeyboard(lBottom);
                break;
        }

        return true;
    }

    protected void saveState() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("mainfrom", editTxtMainFrom.getText().toString());
        editor.putString("main", editTxtMain.getText().toString());
        editor.putString("from", editTxtFrom.getText().toString());
        editor.putInt("src", spinTrRes.getSelectedItemPosition());
        editor.putString("ngrams", Utils.serializeNGrams(ngrams));
        editor.commit();
    }

    protected void loadState() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        setState(sharedPref.getString("mainfrom", ""), sharedPref.getString("main", ""), sharedPref.getString("from", ""),
            sharedPref.getInt("src", 0) );
        Utils.unserializeNGrams(ngrams, sharedPref.getString("ngrams", ""));
    }

    protected void setState(String mainfrom, String main, String from, int i) {
        if (mainfrom != null) {
            editTxtMainFrom.setText(mainfrom);
            editTxtMainFrom.setSelection(editTxtMainFrom.getText().length());
        }
        if (main != null) {
            editTxtMain.setText(main);
            editTxtMain.setSelection(editTxtMain.getText().length());
        }
        if (from != null) {
            editTxtFrom.setText(from);
            editTxtFrom.setSelection(editTxtFrom.getText().length());
        }
        spinTrRes.setSelection(i);
    }

    protected void processIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            String action = intent.getAction();
            String type = intent.getType();

            if (action != null) {
                if (action.equals(Intent.ACTION_SEND) && type != null) {
                    if ((type != null) && type.equals("text/plain")) {
                        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                        if (sharedText != null) {
                            fromIntent = true;
                            editTxtFrom.setText(sharedText);
                            editTxtFrom.setSelection(editTxtFrom.getText().length());
                            findViewById(R.id.lTopView).setVisibility(View.GONE);
                            findViewById(R.id.lTopFromView).setVisibility(View.GONE);
                        }
                    }
                } else if (action.equals("com.mrn.soft.translatehelper.intent.action.TranslateBigText")) {
                    String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                    if (sharedText != null) {
                        fromIntent = true;
                        editTxtMainFrom.setText(sharedText);
                        editTxtMainFrom.setSelection(editTxtMainFrom.getText().length());
                        findViewById(R.id.lTopFromView).setVisibility(View.VISIBLE);
                    }
                } else if (action.equals(Intent.ACTION_VIEW)) {
                    String[] segments = intent.getData().getPath().split("/");
                    String sharedText = segments[segments.length - 1];
                    if (sharedText != null) {
                        fromIntent = true;
                        editTxtFrom.setText(sharedText);
                        editTxtFrom.setSelection(editTxtFrom.getText().length());
                        findViewById(R.id.lTopView).setVisibility(View.GONE);
                        findViewById(R.id.lTopFromView).setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    protected void showTopView() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        findViewById(R.id.lTopView).setVisibility(prefs.getBoolean("topmain", true) ? View.VISIBLE : View.GONE);
        findViewById(R.id.lTopFromView).setVisibility(prefs.getBoolean("topmainfrom", true) ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        showTopView();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        //firstRowH = spinTrRes.getMeasuredHeight();
    }

    @Override
    public void onPause() {
        super.onPause();
        imgLoader.clearAnimation();
        imgMTRefresh.clearAnimation();
        //Save main elements
        saveState();
    }

    @Override
    public void onBackPressed() {
        if (fromIntent) {
            super.onBackPressed();
        } else {
            if (wordsStack.size() > 0) { //Remove current state
                wordsStack.pop();
            }
            //Get previous state
            WordState top = wordsStack.size() > 0 ? wordsStack.pop() : null;
            if (top == null) {
                super.onBackPressed();
            } else {
                setState(null, null, top.word, top.i);
                refreshTr(top.i);
            }
        }
    }

    protected void showLoader() {
        lLoading.setVisibility(View.VISIBLE);
        imgLoader.startAnimation(
            AnimationUtils.loadAnimation(this, R.anim.rotation) );
    }

    protected void hideLoader() {
        lLoading.setVisibility(View.GONE);
        imgLoader.clearAnimation();
    }

    protected void getPage(String tag, String url, String post) {
        taskFragment.getPage(tag, url, post);
    }

    protected void getReverso() {
        Txt txt = new Txt(editTxtFrom);
        String url = "";
        try {
            url = "http://context.reverso.net/translation/" +
                (txt.isEn ? "english-russian/" : "russian-english/") + URLEncoder.encode(txt.txt, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Utils.onError(e);
        }
        getPage("reverso", url, "");
    }

    protected void getGoogle() {
        Txt txt = new Txt(editTxtFrom);
        String l1 = txt.isEn ? "en" : "ru";
        String l2 = txt.isEn ? "ru" : "en";
        String url = "";
        try {
            url = "http://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=" +
                l2 + "&hl=" + l1 + "&dt=t&dt=bd&dj=1&source=input&tk=0&q=" + URLEncoder.encode(txt.txt, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Utils.onError(e);
        }
        getPage("google", url, "");
    }

    protected void getTranslate() {
        Txt txt = new Txt(editTxtFrom);
        String url = "http://www.translate.ru/services/TranslationService.asmx/GetTranslateNew";
        String post = "";
        try {
            post = "dirCode=" + (txt.isEn ? "en-ru" : "ru-en") + "&template=auto&text=" + URLEncoder.encode(txt.txt, "UTF-8") +
                "&lang=ru&limit=3000&useAutoDetect=true&key=&ts=MainSite&tid=&IsMobile=false";
        } catch (UnsupportedEncodingException e) {
            Utils.onError(e);
        }
        getPage("translate", url, post);
    }

    protected void getHints() {
        Txt txt = new Txt(editTxtFrom);
        String url = "https://books.google.com/ngrams/interactive_chart?year_start=1999&year_end=2000&corpus=15&smoothing=5&content=";
        try {
            url += URLEncoder.encode(txt.txt, "UTF-8");;
        } catch (UnsupportedEncodingException e) {
            Utils.onError(e);
        }
        getPage("booksgoogle", url, "");
    }

    protected void getLLeo() {
        Txt txt = new Txt(editTxtFrom);
        String url = "http://api.lingualeo.com/gettranslates?port=1001";
        String post = "";
        try {
            post = "word=" + URLEncoder.encode(txt.txt, "UTF-8") +
                "&include_media=1&add_word_forms=1&port=1001";
        } catch (UnsupportedEncodingException e) {
            Utils.onError(e);
        }
        getPage("lleo", url, post);
    }

    protected void getMicrosoft1() {
        String url = "https://ssl.microsofttranslator.com/ajax/v3/widgetv3.ashx";
        getPage("microsoft1", url, "");
    }

    protected void getMicrosoft2(String html) {
        Txt txt = new Txt(editTxtFrom);
        String appId = "";
        Pattern pattern = Pattern.compile("appId:'([^']*?)'", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            appId = matcher.group(1).replaceAll("\\\\x", "\\%");
        }
        //String appId = "TdzyqaKXt_j82xelf2n_f5BatKnhu29_PkvNJbgH0rOs*";

        String from = txt.isEn ? "en" : "ru";
        String to = txt.isEn ? "ru" : "en";
        String url = "";
        try {
            url = "https://api.microsofttranslator.com/v2/ajax.svc/TranslateArray?appId=%22"+appId+"%22&texts=[%22"+
                    URLEncoder.encode(txt.txt, "UTF-8")+"%22]&from=%22"+from+"%22&to=%22"+to+
                    "%22&oncomplete=_mstc2&onerror=_mste2&loc=ru&ctr=&ref=WidgetV3&rgp=aa0fbc1";
        } catch (UnsupportedEncodingException e) {
            Utils.onError(e);
        }
        getPage("microsoft2", url, "");
    }

    @Override
    public void OnHttpData(String tag, String page) {
        switch (tag) {
            case "reverso":
                onReversoAnswerReceived(page);
                break;
            case "google":
                onGoogleAnswerReceived(page);
                break;
            case "translate":
                onTranslateAnswerReceived(page);
                break;
            case "booksgoogle":
                onHintsAnswerReceived(page);
                break;
            case "lleo":
                onLLeoAnswerReceived(page);
                break;
            case "microsoft1":
                getMicrosoft2(page);
                break;
            case "microsoft2":
                onMicrosoftAnswerReceived(page);
                break;
            default:
                if (tag.startsWith("booksgoogle")) {
                    onBooksGoogleAnswerReceived(tag, page);
                }
                break;
        }
    }

    protected void onReversoAnswerReceived(String html) {
        //String html = Utils.getAssetFileContent(this, "reverso_answer.html");
        rd = new ReversoData(html);

        txtReversoPhrase.setText(rd.phrase);
        String translations = "";
        for (String item : rd.translations) {
            translations += "<a href=\"" + item + "\">" + item + "</a>&nbsp;&nbsp;&nbsp;&nbsp;";
        }
        txtReversoTrs.setText(Html.fromHtml(translations));
        txtReversoTrs.setMovementMethod(linkTo);

        //It could have been done through a ListView and an Adapter, but it seems more convenient
        // when there is a ScrollView for all contents including top block
        listReversoExamples.removeAllViews();

        lReverso.setVisibility(View.VISIBLE);
        hideLoader();

        for (PhraseExample item : rd.examples) {
            View view = LayoutInflater.from(this).inflate(R.layout.reverso_examples_row, null, false);

            TextView txtPhraseFrom = (TextView) view.findViewById(R.id.txtPhraseFrom);
            TextView txtPhraseTo = (TextView) view.findViewById(R.id.txtPhraseTo);

            txtPhraseFrom.setText(Html.fromHtml(item.phraseFrom));
            txtPhraseFrom.setMovementMethod(linkFrom);

            txtPhraseTo.setText(Html.fromHtml(item.phraseTo));
            txtPhraseTo.setMovementMethod(linkTo);

            listReversoExamples.addView(view);
        }
    }

    protected void onGoogleAnswerReceived(String html) {
        GoogleData gd = new GoogleData(html);

        lGoogle.removeAllViews();

        lGoogle.setVisibility(View.VISIBLE);
        hideLoader();

        for (GoogleDataGroup group : gd.groups) {
            LinearLayout gView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.group, null, false);
            TextView txtGWord = (TextView) gView.findViewById(R.id.txtGWord);

            txtGWord.setText(Html.fromHtml("<font color='@android:color/black'><b><a href=\"" + group.baseForm + "\">" + group.baseForm + "</a></b></font> <i>(" + group.type + ")</i>"));
            txtGWord.setMovementMethod(linkFrom);

            for (GoogleDataGroupItem item : group.translations) {
                LinearLayout l = new LinearLayout(this);
                l.setOrientation(LinearLayout.HORIZONTAL);
                l.setPadding(8, 0, 0, 0);

                TextView t1 = new TextView(this);
                t1.setText(Html.fromHtml("<a href=\"" + item.word + "\">" +item.word+ "</a>"));
                t1.setMovementMethod(linkTo);

                StringBuilder b = new StringBuilder();
                for (String translation : item.translations) {
                    if (b.length() != 0) {
                        b.append(", ");
                    }
                    b.append("<font color='@android:color/black'><a href=\"").append(translation).append("\">").append(translation).append("</a></font>");
                }

                TextView t2 = new TextView(this);
                t2.setPadding(8, 0, 0, 0);
                t2.setText(Html.fromHtml("(" + b.toString() + ")"));
                t2.setMovementMethod(linkFrom);

                l.addView(t1);
                l.addView(t2);

                gView.addView(l);
            }

            if (group.translations.size() > 5) {
                int h = 0;
                for (int i = 0; i < 6; i ++) {
                    View v = gView.getChildAt(i);
                    v.measure(0, 0);
                    h += v.getMeasuredHeight();
                }
                gView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, h));
                View btn = gView.findViewById(R.id.btnGExpand);
                btn.setVisibility(View.VISIBLE);
                btn.setTag(new Integer(0));
                gView.setTag(new Integer(h));
            }
            lGoogle.addView(gView);
        }

        for (PhraseExample phrase : gd.phrases) {
            LinearLayout l = new LinearLayout(this);
            l.setOrientation(LinearLayout.HORIZONTAL);
            TextView t1 = new TextView(this);
            t1.setText(Html.fromHtml("<font color='@android:color/black'><a href=\"" + phrase.phraseFrom + "\">" + phrase.phraseFrom + "</a></font>"));
            t1.setMovementMethod(linkFrom);
            TextView t2 = new TextView(this);
            t2.setPadding(8, 0, 0, 0);
            t2.setText(Html.fromHtml("<a href=\"" + phrase.phraseTo + "\">" + phrase.phraseTo + "</a>"));
            t2.setMovementMethod(linkTo);
            l.addView(t1);
            l.addView(t2);
            lGoogle.addView(l);
        }
    }

    protected void onTranslateAnswerReceived(String html) {
        TranslateData td = new TranslateData(html);

        lTranslate.removeAllViews();

        lTranslate.setVisibility(View.VISIBLE);
        hideLoader();

        if (td.translation.equals("")) {
            for (TranslateDataGroup group : td.groups) {
                LinearLayout gView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.group, null, false);
                TextView txtGWord = (TextView) gView.findViewById(R.id.txtGWord);

                StringBuilder b = new StringBuilder();
                b.append("<font color='@android:color/black'><b><a href=\"").append(group.baseForm).
                    append("\">").append(group.baseForm ).append("</a></b></font> <i>(" ).append(group.type).
                    append(")</i> ");
                boolean bF = true;
                for (String form : group.forms) {
                    if (!bF) {
                        b.append(" / ");
                    } else {
                        bF = false;
                    }
                    b.append("<font color='@android:color/black'><a href=\"").append(form).append("\">").append(form).append("</a></font>");
                }

                txtGWord.setText(Html.fromHtml(b.toString()));
                txtGWord.setMovementMethod(linkFrom);

                for (String word : group.translations) {
                    TextView t1 = new TextView(this);
                    t1.setText(Html.fromHtml("<a href=\"" + word + "\">" + word + "</a>"));
                    t1.setMovementMethod(linkTo);
                    t1.setPadding(8, 0, 0, 0);

                    gView.addView(t1);
                }

                if (group.translations.size() > 6) {
                    int h = 0;
                    for (int i = 0; i < 6; i ++) {
                        View v = gView.getChildAt(i);
                        v.measure(0, 0);
                        h += v.getMeasuredHeight();
                    }
                    gView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, h));
                    View btn = gView.findViewById(R.id.btnGExpand);
                    btn.setVisibility(View.VISIBLE);
                    btn.setTag(new Integer(0));
                    gView.setTag(new Integer(h));
                }

                lTranslate.addView(gView);
            }
        } else {
            TextView t1 = new TextView(this);
            t1.setPadding(8, 0, 0, 0);
            t1.setText(Html.fromHtml("<a href=\"" + td.translation + "\">" + td.translation + "</a>"));
            t1.setMovementMethod(linkTo);

            editTxtTo.setText(td.translation);

            lTranslate.addView(t1);
        }
    }

    protected void showHintsChildren(NGram ngram, LinearLayout parent) {
        LinearLayout gView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.group, null, false);
        TextView txtGWord = (TextView) gView.findViewById(R.id.txtGWord);

        if (ngram.children.size() > 0) {
            txtGWord.setText(Html.fromHtml("<font color='@android:color/black'><b>" + ngram.ngram + "</b></font> " +
                " <i>(" + String.format("%.2E", ngram.val) + ")</i>"));

            for (Map.Entry<Double, NGram> child : ngram.children.entrySet()) {
                showHintsChildren(child.getValue(), gView);
            }

            if (ngram.children.size() > 6) {
                int h = 0;
                for (int i = 0; i < 6; i ++) {
                    View v = gView.getChildAt(i);
                    v.measure(0, 0);
                    h += v.getMeasuredHeight();
                }
                gView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, h));
                View btn = gView.findViewById(R.id.btnGExpand);
                btn.setVisibility(View.VISIBLE);
                btn.setTag(new Integer(0));
                gView.setTag(new Integer(h));
            }
        } else {
            gView.setPadding(16, 0, 0, 0);
            String txt = ngram.ngram.replaceAll("^(.*?)([\\w|\\'|-]*)[^\\w|\\'|-]*$", "<font color='@android:color/black'>$1</font> <a href=\"$2\">$2</a>");
            txtGWord.setText(Html.fromHtml(txt + " <i>(" + String.format("%.2E", ngram.val) + ")</i>"));
            txtGWord.setMovementMethod(linkTo);
        }

        parent.addView(gView);
    }

    protected void onHintsAnswerReceived(String html) {
        BooksGoogleData bd = new BooksGoogleData(html);

        listHints.removeAllViews();

        lHints.setVisibility(View.VISIBLE);
        hideLoader();
        //keySet()
        for (Map.Entry<Double, NGram> child : bd.root.children.entrySet()) {
            showHintsChildren(child.getValue(), listHints);
        }
    }

    protected void onLLeoAnswerReceived(String html) {
        LLeoData ld = new LLeoData(html);

        lLLeo.removeAllViews();

        lLLeo.setVisibility(View.VISIBLE);

        LinearLayout gView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.group, null, false);
        TextView txtGWord = (TextView) gView.findViewById(R.id.txtGWord);

        txtGWord.setText(ld.phrase);

        for (String translation: ld.translations) {
            TextView t1 = new TextView(this);
            t1.setText(Html.fromHtml("<a href=\"" + translation + "\">" + translation + "</a>"));
            t1.setMovementMethod(linkTo);
            t1.setPadding(8, 0, 0, 0);

            gView.addView(t1);
        }

        lLLeo.addView(gView);

        hideLoader();
    }

    protected void onMicrosoftAnswerReceived(String html) {
        MicrosoftData md = new MicrosoftData(html);

        lMicrosoft.removeAllViews();

        lMicrosoft.setVisibility(View.VISIBLE);

        TextView t1 = new TextView(this);
        t1.setPadding(8, 0, 0, 0);
        t1.setText(Html.fromHtml("<a href=\"" + md.translation + "\">" + md.translation + "</a>"));
        t1.setMovementMethod(linkTo);

        editTxtTo.setText(md.translation);

        lMicrosoft.addView(t1);

        hideLoader();
    }

    public void hideSoftKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    protected void clearCache() {
        ngrams.clear();
        //SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        //SharedPreferences.Editor editor = sharedPref.edit();
        //editor.putString("ngrams", "");
        //editor.commit();
        Toast.makeText(this, R.string.t_cache_was_cleared, Toast.LENGTH_SHORT).show();
    }

    protected void onBooksGoogleAnswerReceived(String tag, String html) {
        Integer i = new Integer(tag.substring("booksgoogle".length()));

        BooksGoogleData bd = new BooksGoogleData(html);
        ngrams.putAll(bd.root.getAll());

        //We should set values of phrases for which books.google hasn't returned stats to 0
        //as it didn't return those phrases within answer at all
        ArrayList<String> lwords = ngBatches.get(i);
        for (String word: lwords) {
            if (!ngrams.containsKey(word)) {
                ngrams.put(word, new Double(0.0));
            }
        }
        ngBatches.remove(i);

        if (ngBatches.size() == 0) {
            imgMTRefresh.clearAnimation();
        }

        editTxtMain.addNGrmas(bd.root.getAll());
    }

    protected void getBooksGoogle(ArrayList<String> nwords) {
        int sliceSize = 5;
        ArrayList<ArrayList<String>> tasks = new ArrayList<ArrayList<String>>();
        String urlT = "https://books.google.com/ngrams/interactive_chart?year_start=1999&year_end=2000&corpus=15&smoothing=5&content=";
        for (int r = 0; r < nwords.size(); r += sliceSize) {
            ArrayList<String> lwords = new ArrayList<String>(nwords.subList(r, Math.min(nwords.size(), r + sliceSize)));
            Integer i = new Integer((int)(Math.random() * 1000000));
            ngBatches.put(i, lwords);

            String x = TextUtils.join(",", lwords);
            String url = "";
            try {
                url = urlT + URLEncoder.encode(x, "UTF-8");;
            } catch (UnsupportedEncodingException e) {
                Utils.onError(e);
            }
            ArrayList<String> task = new ArrayList<String>();
            task.add("booksgoogle");
            task.add(url);
            task.add("");
            task.add("booksgoogle" + i.toString());
            tasks.add(task);
        }
        imgMTRefresh.startAnimation(
            AnimationUtils.loadAnimation(this, R.anim.rotation) );
        taskFragment.getPages(tasks, 50); //Set interval between requests to books.google.com to 50 ms
    }

    protected boolean isNGramExist(String ngram) {
        return ngrams.containsKey(ngram);
    }

    protected void refreshMT() {
        String txt = editTxtMain.getText().toString();
        String[] words = txt.split("[^\\w|\\'|-]+");
        //String[] words = txt.split("\\W+"); //([\w|\'|-]+) //"\\W+"
        ArrayList<String> nwords = new ArrayList<String>();

        //Composite ngramms of diffrent size
        for (int n = 3; n < 6; n ++) {
            for (int i = 0; i < words.length - n + 1; i ++) {
                StringBuffer str = new StringBuffer();
                for (int a = 0; a < n; a ++) {
                    if (a > 0) {
                        str.append(" ");
                    }
                    str.append(words[i + a]);
                }
                String s = str.toString();
                if (!isNGramExist(s)) {
                    //ngrams.put(s, -1.0);
                    nwords.add(s);
                }
            }
        }

        if (nwords.size() > 0) {
            getBooksGoogle(nwords);
        }

    }

    protected void showHint() {
        int start = editTxtMain.getSelectionStart();
        int end = editTxtMain.getSelectionEnd();
        String txt = editTxtMain.getText().toString().substring(start, end);
        Toast.makeText(MainActivity.ctx, txt, Toast.LENGTH_SHORT).show();
    }

    protected void refreshTr(int i) {
        String word = editTxtFrom.getText().toString();
        if (word.length() > 0) {
            showLoader();

            for (int n = 0; n < lList.size(); n ++) {
                lList.get(n).setVisibility(View.GONE);
            }

            try {
                lActions.get(i).invoke(this);
            } catch (IllegalAccessException e) {
                Utils.onError(e);
            } catch (InvocationTargetException e) {
                Utils.onError(e);
            }

            hideSoftKeyboard(btnTrRefresh);

            saveState();

            WordState top = wordsStack.size() > 0 ? wordsStack.peek() : null;
            if ((top == null) || (!top.word.equals(word))) {
                wordsStack.push(new WordState(i, editTxtFrom.getText().toString()));
            }
        } else {
            Toast.makeText(this, R.string.t_empty_input, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        refreshTr(i);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        //
    }

    protected void expandGGroup(ImageButton btn) {
        LinearLayout gView = (LinearLayout)btn.getParent().getParent();
        Integer state = (Integer) btn.getTag();
        Integer h = (Integer) gView.getTag();

        if (state == 0) { //Not expanded
            gView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            btn.setTag(new Integer(1));
            btn.setImageResource(R.drawable.ic_shrink);
        } else {
            gView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, h));
            btn.setTag(new Integer(0));
            btn.setImageResource(R.drawable.ic_expand);
        }
    }

    protected void showHintsForMT() {
        int i = 3;
        int start = editTxtMain.getSelectionStart();
        int end = editTxtMain.getSelectionEnd();
        String txt = editTxtMain.getText().toString().replaceAll("(\\s+)", " ");
        String from = "";
        if (start == end) { //no text is selected
            int l = (end == 0) ? txt.length() : end;
            txt = txt.substring(0, l).trim();
            txt = txt.replaceAll("\\'", " \\'");
            Pattern pattern = Pattern.compile("^.*?([\\w|\\'|-]*)[^\\w|\\'|-]*([\\w|\\'|-]*)[^\\w|\\'|-]*([\\w|\\'|-]*)[^\\w|\\'|-]*([\\w|\\'|-]*)[^\\w|\\'|-]*$", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(txt);
            StringBuilder b = new StringBuilder();
            while (matcher.find()) {
                for (int n = 1; n < matcher.groupCount() + 1; n ++) {
                    if (matcher.group(n).length() > 0) {
                        if (b.length() > 0) {
                            b.append(",");
                        }
                        for (int k = n; k < matcher.groupCount() + 1; k++) {
                            if (matcher.group(k).length() > 0) {
                                b.append(matcher.group(k));
                                b.append(" ");
                            } else {
                                break;
                            }
                        }
                        b.append("*");
                    } else {
                        break;
                    }
                }
            }
            from = b.toString();
        } else {
            txt = txt.substring(start, end);
            from = txt.replaceAll("[^\\w|\\'|-]+", " ").replaceAll("\\'", " \\'").trim() + " *";
        }
        setState(null, null, from, i);
        refreshTr(i);
    }

    public void doClick(View view) {
        int start, end;
        String txt, itxt;

        switch (view.getId()) {
            case R.id.btnTrRefresh:
                refreshTr(spinTrRes.getSelectedItemPosition());
                break;
            case R.id.btnGExpand:
                expandGGroup((ImageButton)view);
                break;
            case R.id.btnCopy:
            case R.id.btnMTCopy:
                txt = (view.getId() == R.id.btnCopy) ? editTxtTo.getText().toString() :
                    editTxtMain.getText().toString();
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(txt, txt);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Text \"" + txt + "\" was copied into the clipboard", Toast.LENGTH_LONG).show();
                break;
            case R.id.btnMTRefresh:
            case R.id.imgMTRefresh:
                refreshMT();
                break;
            case R.id.btnHint:
                showHintsForMT();
                break;
            case R.id.btnInsertInMT:
                start = editTxtMain.getSelectionStart();
                txt = editTxtMain.getText().toString();
                itxt = (((start > 0) && (txt.charAt(start - 1) != ' ')) ? " " : "") +
                    editTxtTo.getText().toString();
                editTxtMain.getText().replace(start, start, itxt, 0, itxt.length());
                break;
            case R.id.btnInsertInFrom:
                start = editTxtFrom.getSelectionStart();
                txt = editTxtFrom.getText().toString();
                //spinGBTags.setSelection();
                String tag = getResources().getStringArray(R.array.gb_tags_values)[spinGBTags.getSelectedItemPosition()];
                itxt = spinGBPref.getSelectedItem().toString().equals("*") ? ("*" + tag) : (tag + "_");
                editTxtFrom.getText().replace(start, start, itxt, 0, itxt.length());
                break;
            case R.id.btnMFTCopy:
                start = editTxtMainFrom.getSelectionStart();
                end = editTxtMainFrom.getSelectionEnd();
                editTxtFrom.setText(editTxtMainFrom.getText().toString().substring(start, end));
                break;
            case R.id.lBottom:
                hideSoftKeyboard(lBottom);
                break;
        }
    }

}
