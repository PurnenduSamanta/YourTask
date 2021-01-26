package com.purnendu.yourtask;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class SplashActivity extends AppCompatActivity {
    private FirebaseAuth mauth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mauth=FirebaseAuth.getInstance();
        FirebaseUser mUser=mauth.getCurrentUser();
        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mauth.getCurrentUser()!=null &&(Objects.requireNonNull(mUser).isEmailVerified()))
                {
                    Intent intent=new Intent(SplashActivity.this,HomeActivity.class);
                    startActivity(intent);
                }
                else {
                    Intent i = new Intent(SplashActivity.this,MainActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        },3000);
    }
}