package com.purnendu.yourtask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetPasswordActivity extends AppCompatActivity {
    private Button reset;
    private EditText femail;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        reset=findViewById(R.id.reset);
        femail=findViewById(R.id.femail);
        mAuth=FirebaseAuth.getInstance();
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               String remail=femail.getText().toString().trim();
                if(TextUtils.isEmpty(remail))
                {
                    femail.setError("Required Field");
                    return;
                }
                mAuth.sendPasswordResetEmail(remail).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(ForgetPasswordActivity.this, "Reset email has been sent to "+remail+"emaid address" ,Toast.LENGTH_SHORT).show();
                            Intent intent=new Intent(ForgetPasswordActivity.this,MainActivity.class);
                            startActivity(intent);

                        }
                        else
                        {
                            Toast.makeText(ForgetPasswordActivity.this, "This is not a registered mail address", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            }
        });


    }
}