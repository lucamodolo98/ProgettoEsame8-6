package com.example.luca.progettoesame;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SpalshActivity extends AppCompatActivity {
    public static final int SPLAH_TIME_OUT = 4320;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spalsh);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(SpalshActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        }, SPLAH_TIME_OUT);
    }
}
