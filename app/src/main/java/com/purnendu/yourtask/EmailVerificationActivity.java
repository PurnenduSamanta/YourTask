package com.purnendu.yourtask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
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
                                try {
                                    Intent intent = new Intent("android.intent.action.MAIN");
                                    intent.addCategory("android.intent.category.APP_EMAIL");
                                    startActivity(Intent.createChooser(intent, "Verify Your Email"));
                                }catch (ActivityNotFoundException e)
                                {
                                    Toast.makeText(EmailVerificationActivity.this, "Email Client not found", Toast.LENGTH_LONG).show();
                                }

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
        Intent intent=new Intent(EmailVerificationActivity.this,MainActivity.class);
        startActivity(intent);
        super.onRestart();
    }
}