package com.purnendu.yourtask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class EmailVerificationActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);
        Button verification_button = findViewById(R.id.verification_button);

        mAuth=FirebaseAuth.getInstance();
        verification_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser mUser=mAuth.getCurrentUser();
                if(mUser!=null)
                {
                    mUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful())
                            {
                                Toast.makeText(EmailVerificationActivity.this, "Please verify your email sent to "+mUser.getEmail()+",then log into account", Toast.LENGTH_LONG).show();

                                Intent intent = new Intent(EmailVerificationActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);

                            }
                            else
                            {
                                Toast.makeText(EmailVerificationActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }

            }
        });



    }

    @Override
    protected void onRestart() {
        mAuth.signOut();
        Intent intent=new Intent(EmailVerificationActivity.this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
         mAuth.signOut();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        mAuth.signOut();
        super.onStop();
    }
}