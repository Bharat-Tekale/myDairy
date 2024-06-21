package com.example.mydairy;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import static java.lang.Thread.sleep;

public class SaveSplash extends AppCompatActivity {
    public static int Splash_time=500;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_splash);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    sleep(500);
                    Intent intent=new Intent(SaveSplash.this, Login_Form.class);
                    startActivity(intent);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },Splash_time);
    }

}
