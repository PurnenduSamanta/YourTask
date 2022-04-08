package com.purnendu.yourtask

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

open class LoginActivity : AppCompatActivity() {

    private lateinit var lwo:MaterialButton
    private lateinit var lwe:MaterialButton
    private lateinit var googleSignInButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        lwo=findViewById(R.id.lwoButton)
        lwe=findViewById(R.id.lweButton)
        googleSignInButton=findViewById(R.id.google)

        lwo.setOnClickListener { startActivity(Intent(this,LoginWithOTPActivity::class.java)) }

        lwe.setOnClickListener { startActivity(Intent(this,MainActivity::class.java)) }

        googleSignInButton.setOnClickListener {
            val intent = Intent(this, GoogleSignInActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }
}