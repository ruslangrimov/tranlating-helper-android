package com.mrn.soft.translatehelper;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class BigTextActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent != null) {
            String action = intent.getAction();
            String type = intent.getType();
            if (action != null && type != null) {
                if (action.equals(Intent.ACTION_SEND) && type != null) {
                    if (type.equals("text/plain")) {
                        intent.setAction("com.mrn.soft.translatehelper.intent.action.TranslateBigText");
                        intent.setClass(this, MainActivity.class);
                        startActivity(intent);
                    }
                }
            }
        }

        finish();
    }
}
