package com.purnendu.yourtask;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.Objects;


public class RegistrationActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText mail_reg,password_reg,password_reg_retype;
    private  FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    boolean isPasswordVisible=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        mail_reg=findViewById(R.id.mail_reg);
        password_reg=findViewById(R.id.password_reg);
        Button registration = findViewById(R.id.registration);
        TextView login_reg = findViewById(R.id.login_reg);
        ImageView show_pass_btn = findViewById(R.id.show_pass_btn);
        ImageView  show_pass_btn1=findViewById(R.id.show_pass_btn1);
        password_reg_retype=findViewById(R.id.password_reg_retype);
        show_pass_btn.setOnClickListener(this);
        show_pass_btn1.setOnClickListener(this);
        mAuth=FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(RegistrationActivity.this);
        progressDialog.setCancelable(false);
        login_reg.setOnClickListener(v -> {
            Intent intent=new Intent(RegistrationActivity.this,MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
        registration.setOnClickListener(v -> {
            String mpass,memail,mpassRetype;
            mpass=password_reg.getText().toString().trim();
            memail=mail_reg.getText().toString().trim();
            mpassRetype=password_reg_retype.getText().toString().trim();
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
            if(TextUtils.isEmpty(mpassRetype))
            {
                password_reg_retype.setError("Required Field");
                return;

            }
            if(!mpass.equals(mpassRetype))
            {
                password_reg_retype.setError("Password not matching");
                return;
            }
            progressDialog.setMessage("Hold on..");
            progressDialog.show();
            mAuth.createUserWithEmailAndPassword(memail,mpass).addOnCompleteListener(RegistrationActivity.this, task -> {
                if(task.isSuccessful()) {
                    FirebaseUser mUser=mAuth.getCurrentUser();
                    if(mUser!=null) {
                        mUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @RequiresApi(api = Build.VERSION_CODES.M)
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {
                                    mAuth.signOut();
                                    progressDialog.dismiss();
                                    hideKeyboard(v);
                                    Toast.makeText(RegistrationActivity.this, "Please verify your email sent to " + mUser.getEmail() + ",then log into account", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                } else {
                                    progressDialog.dismiss();
                                    Toast.makeText(RegistrationActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                    }
                }
                else
                {
                    Toast.makeText(RegistrationActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });

        });
    }
    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.show_pass_btn || v.getId()==R.id.show_pass_btn1){

            if(isPasswordVisible){
                ((ImageView)(v)).setImageResource(R.drawable.password_toggle);

                //Show Password
                if(v.getId()==R.id.show_pass_btn)
                password_reg.setTransformationMethod(new PasswordTransformationMethod());
                else if(v.getId()==R.id.show_pass_btn1)
                    password_reg_retype.setTransformationMethod(new PasswordTransformationMethod());
                isPasswordVisible=false;
            }
            else{
                ((ImageView)(v)).setImageResource(R.drawable.password_toggle_show);

                //Hide Password
                if(v.getId()==R.id.show_pass_btn)
                    password_reg.setTransformationMethod(null);
                else if(v.getId()==R.id.show_pass_btn1)
                    password_reg_retype.setTransformationMethod(null);
                isPasswordVisible=true;
            }

        }

    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void hideKeyboard(View view) {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch(Exception ignored) {
        }
    }
}