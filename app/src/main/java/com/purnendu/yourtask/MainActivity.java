package com.purnendu.yourtask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private EditText  mail_login,password_login;
    private ProgressDialog progressDialog;
    private FirebaseAuth mauth;
    private FirebaseUser mUser;
    boolean isPasswordVisible=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button login = findViewById(R.id.login);
        mail_login=findViewById(R.id.mail_login);
        password_login=findViewById(R.id.password_login);
        TextView registration_login = findViewById(R.id.registration_login);
        TextView forget_password = findViewById(R.id.forget_password);
        ImageView show_pass_btn = findViewById(R.id.show_pass_btn);
        progressDialog=new ProgressDialog(this);
        progressDialog.setCancelable(false);
        mauth=FirebaseAuth.getInstance();
        forget_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,ForgetPasswordActivity.class);
                startActivity(intent);
            }
        });
        registration_login.setOnClickListener(v -> {
            Intent intent=new Intent(MainActivity.this,RegistrationActivity.class);
            startActivity(intent);
        });
        show_pass_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPasswordVisible){
                    ((ImageView)(v)).setImageResource(R.drawable.password_toggle);

                    //Show Password
                    password_login.setTransformationMethod(new PasswordTransformationMethod());
                    isPasswordVisible=false;
                }
                else{
                    ((ImageView)(v)).setImageResource(R.drawable.password_toggle_show);

                    //Hide Password
                    password_login.setTransformationMethod(null);
                    isPasswordVisible=true;
                }
            }
        });

        login.setOnClickListener(v -> {
            String mpass,memail;
            mpass=password_login.getText().toString().trim();
            memail=mail_login.getText().toString().trim();
            if(TextUtils.isEmpty(mpass))
            {
                password_login.setError("Required Field");
                return;
            }
            if(TextUtils.isEmpty(memail))
            {
                mail_login.setError("Required Field");
                return;

            }
            progressDialog.setMessage("Hold on...");
            progressDialog.show();
            mauth.signInWithEmailAndPassword(memail,mpass).addOnCompleteListener(task -> {
              if(task.isSuccessful()) {
                  progressDialog.dismiss();
                  mUser=mauth.getCurrentUser();
                  if(mUser!=null) {
                      if (mUser.isEmailVerified()) {
                          Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                          intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                          startActivity(intent);
                      } else {
                          Toast.makeText(MainActivity.this, "You need to verify your Email", Toast.LENGTH_SHORT).show();
                          Intent intent = new Intent(MainActivity.this, EmailVerificationActivity.class);
                          startActivity(intent);
                      }
                  }
              }
              else
              {
                  Toast.makeText(MainActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                  progressDialog.dismiss();
              }
          }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this,e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });


        });


    }
}