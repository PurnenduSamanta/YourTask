package com.purnendu.yourtask;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.Objects;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAuth mauth = FirebaseAuth.getInstance();
        FirebaseUser mUser= mauth.getCurrentUser();
        Handler handler=new Handler();
        if(mauth.getCurrentUser()!=null && (Objects.requireNonNull(mUser).isEmailVerified()))
        {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            Intent intent=new Intent(SplashActivity.this,HomeActivity.class);
            startActivity(intent);
        }
        else
        {
            setContentView(R.layout.activity_splash);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                        Intent i = new Intent(SplashActivity.this,MainActivity.class);
                        startActivity(i);
                        finish();
                }
            },1000);
        }

    }
}