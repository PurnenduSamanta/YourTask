package com.purnendu.yourtask

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.rilixtech.widget.countrycodepicker.CountryCodePicker
import java.util.concurrent.TimeUnit

class LoginWithOTPActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var ccp : CountryCodePicker
    private lateinit var phNo: EditText
    private lateinit var getOtpButton:Button
    private lateinit var progressBar: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_with_otpactivity)

        auth = Firebase.auth

        phNo=findViewById(R.id.phoneNo)
        ccp=findViewById(R.id.ccp)
        getOtpButton=findViewById(R.id.getOtpButton)
        progressBar=findViewById(R.id.otpSendingProgressBar)

        ccp.registerPhoneNumberTextView(phNo)

        getOtpButton.setOnClickListener {

            if (phNo.text.toString().isEmpty())
                return@setOnClickListener

            if(!ccp.isValid)
            {
                phNo.error = "Not a valid No"
                return@setOnClickListener
            }


            hideKeyBoard()
            statusChangeOfProgressbarAndButton(visibility=View.VISIBLE,buttonStatus=false)
            startPhoneNumberVerification( ccp.selectedCountryCodeWithPlus+ phNo.text.trim().toString())

        }

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {

                auth.signInWithCredential(credential)
                    .addOnCompleteListener(this@LoginWithOTPActivity) { task ->
                        if (task.isSuccessful) {
                            statusChangeOfProgressbarAndButton(visibility=View.INVISIBLE,buttonStatus=true)
                            val intent= Intent(this@LoginWithOTPActivity,HomeActivity::class.java)
                            startActivity(intent)
                        } else {
                            statusChangeOfProgressbarAndButton(visibility=View.INVISIBLE,buttonStatus=true)
                            if (task.exception is FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Toast.makeText(this@LoginWithOTPActivity,"Invalid OTP", Toast.LENGTH_SHORT).show()
                            }
                            Toast.makeText(this@LoginWithOTPActivity,"Error occurred", Toast.LENGTH_SHORT).show()
                            // Update UI
                        }
                    }
            }

            override fun onVerificationFailed(e: FirebaseException) {

                statusChangeOfProgressbarAndButton(visibility=View.INVISIBLE,buttonStatus=true)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(this@LoginWithOTPActivity, e.message, Toast.LENGTH_SHORT).show()
                    return
                } else if (e is FirebaseTooManyRequestsException) {
                    Toast.makeText(this@LoginWithOTPActivity, "Too many request", Toast.LENGTH_SHORT)
                        .show()
                    return
                }
                Toast.makeText(this@LoginWithOTPActivity, e.message, Toast.LENGTH_SHORT).show()

            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                statusChangeOfProgressbarAndButton(visibility=View.INVISIBLE,buttonStatus=true)
                val intent = Intent(this@LoginWithOTPActivity, OtpVerificationActivity::class.java)
                intent.putExtra("USER_PHONE_NO", ccp.selectedCountryCodeWithPlus+ phNo.text.trim().toString())
                intent.putExtra("VERIFICATION_ID", verificationId)
                val gson = Gson()
                intent.putExtra("RESEND_OTP_TOKEN", gson.toJson(token))
                startActivity(intent)
            }
        }


    }

    private fun startPhoneNumberVerification(phNo: String) {

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phNo)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

    }

    private fun hideKeyBoard() {
        this.currentFocus?.let { view ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun statusChangeOfProgressbarAndButton(visibility: Int, buttonStatus: Boolean) {
        progressBar.visibility= visibility
        getOtpButton.isEnabled=buttonStatus
    }
}