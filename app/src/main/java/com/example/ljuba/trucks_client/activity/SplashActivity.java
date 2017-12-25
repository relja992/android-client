package com.example.ljuba.trucks_client.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.ljuba.trucks_client.R;

public class SplashActivity extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent i = new Intent(SplashActivity.this, Login2Activity.class);
                startActivity(i);
                finish();

            }
        }, SPLASH_TIME_OUT);
    }
}
