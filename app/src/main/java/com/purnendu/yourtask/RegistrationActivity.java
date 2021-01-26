package com.purnendu.yourtask;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class RegistrationActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText mail_reg,password_reg;
    private Button  registration;
    private ImageView show_pass_btn;
    private TextView login_reg;
    private  FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    boolean isPasswordVisible=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        mail_reg=findViewById(R.id.mail_reg);
        password_reg=findViewById(R.id.password_reg);
        registration=findViewById(R.id.registration);
        login_reg=findViewById(R.id.login_reg);
        show_pass_btn=findViewById(R.id.show_pass_btn);
        show_pass_btn.setOnClickListener(this);
        mAuth=FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(RegistrationActivity.this);
        login_reg.setOnClickListener(v -> {
            Intent intent=new Intent(RegistrationActivity.this,MainActivity.class);
            startActivity(intent);
        });
        registration.setOnClickListener(v -> {
            String mpass,memail;
            mpass=password_reg.getText().toString().trim();
            memail=mail_reg.getText().toString().trim();
            if(TextUtils.isEmpty(mpass))
            {
                password_reg.setError("Required Field");
                return;
            }
            if(TextUtils.isEmpty(memail))
            {
                mail_reg.setError("Required Field");
                return;

            }
            progressDialog.setMessage("Processing...");
            progressDialog.show();
            mAuth.createUserWithEmailAndPassword(memail,mpass).addOnCompleteListener(RegistrationActivity.this, task -> {
                if(task.isSuccessful()) {
                    progressDialog.dismiss();
                    FirebaseUser mUser=mAuth.getCurrentUser();
                    if(mUser!=null) {
                        mUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {
                                    Toast.makeText(RegistrationActivity.this, "Please verify your email sent to " + mUser.getEmail() + ",then log into account", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(RegistrationActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                    }
                }
                else
                {
                    Toast.makeText(RegistrationActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });

        });
    }
    @Override
    public void onBackPressed() {
        finishAffinity();
        System.exit(0);
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.show_pass_btn){

            if(isPasswordVisible){
                ((ImageView)(v)).setImageResource(R.drawable.password_toggle);

                //Show Password
                password_reg.setTransformationMethod(new PasswordTransformationMethod());
                isPasswordVisible=false;
            }
            else{
                ((ImageView)(v)).setImageResource(R.drawable.password_toggle_show);

                //Hide Password
                password_reg.setTransformationMethod(null);
                isPasswordVisible=true;
            }

        }

    }
}