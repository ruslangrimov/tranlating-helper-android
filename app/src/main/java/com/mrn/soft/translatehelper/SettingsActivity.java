package com.mrn.soft.translatehelper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import android.support.v7.app.AppCompatActivity;

import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SubPreferenceFragment()).commit();
        setupActionBar();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            startActivity(new Intent(this, MainActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SubPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.main);
        }

        public void onResume() {
            super.onResume();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
            Map<String, ?> allEntries = prefs.getAll();
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                updateSummary(prefs, entry.getKey(), false);
            }
            prefs.registerOnSharedPreferenceChangeListener(this);
        }

        public void onPause() {
            super.onPause();
            PreferenceManager.getDefaultSharedPreferences(this.getActivity()).unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences prefs, String s) {
            updateSummary(prefs, s, true);
        }

        protected void updateSummary(SharedPreferences prefs, String s, boolean fromEvent) {
            if (s.equals("fontsize")) {
                Preference p = findPreference(s);

                String v = prefs.getString(s, getResources().getString(R.string.fontsize_default_value));
                Integer i = null;

                try {
                    i = new Integer(v);
                } catch (NumberFormatException nfe) {

                }
                p.setSummary("Font size is " + (i == null ? "default" : i.toString() + " sp"));

                if (fromEvent) {
                    //Toast.makeText(MainActivity.ctx, "Restart application in order to apply the font change", Toast.LENGTH_LONG).show();
                }
            } else if (s.equals("topmain")) {
                Preference p = findPreference(s);
                p.setSummary(prefs.getBoolean(s, true) ? "Top To textbox is visible" : "Top To textbox is hidden");
            } else if (s.equals("topmainfrom")) {
                Preference p = findPreference(s);
                p.setSummary(prefs.getBoolean(s, true) ? "Top From textbox is visible" : "Top From textbox is hidden");
            }
        }
    }
}
