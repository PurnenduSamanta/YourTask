package com.purnendu.yourtask

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import java.util.concurrent.TimeUnit


class OtpVerificationActivity : AppCompatActivity() {

    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var auth: FirebaseAuth
    private lateinit var otp1:EditText
    private lateinit var otp2:EditText
    private lateinit var otp3:EditText
    private lateinit var otp4:EditText
    private lateinit var otp5:EditText
    private lateinit var otp6:EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var verify:Button
    private lateinit var resend:TextView
    private lateinit var resendToken: ForceResendingToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_verification)

        auth = Firebase.auth

        val verificationId= intent.getStringExtra("VERIFICATION_ID") ?: return
        val userNo= intent.getStringExtra("USER_PHONE_NO") ?: return
        val gson = Gson()
        resendToken = gson.fromJson(intent.getStringExtra("RESEND_OTP_TOKEN"), ForceResendingToken::class.java)


         otp1=findViewById(R.id.otp1)
         otp2=findViewById(R.id.otp2)
         otp3=findViewById(R.id.otp3)
         otp4=findViewById(R.id.otp4)
         otp5=findViewById(R.id.otp5)
         otp6=findViewById(R.id.otp6)
         verify=findViewById(R.id.verifyOtpButton)
         progressBar=findViewById(R.id.otpVerificationProgressBar)
         resend=findViewById(R.id.resendOtp)
         val userPhNo=findViewById<TextView>(R.id.userPhNoTextView)

        editTextFocusControl()

        userPhNo.text = userNo

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {

                statusChangeOfProgressbarAndButton(visibility=View.INVISIBLE,buttonStatus=true, resendStatus = true)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(this@OtpVerificationActivity, e.message, Toast.LENGTH_SHORT).show()
                    return
                } else if (e is FirebaseTooManyRequestsException) {
                    Toast.makeText(this@OtpVerificationActivity, "Too many request", Toast.LENGTH_SHORT)
                        .show()
                    return
                }
                Toast.makeText(this@OtpVerificationActivity, e.message, Toast.LENGTH_SHORT).show()

            }

            override fun onCodeSent(
                verificationId: String,
                token: ForceResendingToken
            )  {
                resendToken=token
                statusChangeOfProgressbarAndButton(visibility=View.INVISIBLE,buttonStatus=true, resendStatus = true)
               }
        }

        verify.setOnClickListener {
            
            if(!validateEditText(otp1))
                return@setOnClickListener

            if(!validateEditText(otp2))
                return@setOnClickListener

            if(!validateEditText(otp3))
                return@setOnClickListener

            if(!validateEditText(otp4))
                return@setOnClickListener

            if(!validateEditText(otp5))
                return@setOnClickListener

            if(!validateEditText(otp6))
                return@setOnClickListener

            statusChangeOfProgressbarAndButton(visibility= View.VISIBLE,buttonStatus=false, resendStatus =false)

            val otpCode=otp1.text.toString()+otp2.text.toString()+otp3.text.toString()+otp4.text.toString()+otp5.text.toString()+otp6.text.toString()
          
            verifyPhoneNumberWithOtpCode(verificationId,otpCode)
        }

        resend.setOnClickListener {
            statusChangeOfProgressbarAndButton(visibility= View.VISIBLE,buttonStatus=false, resendStatus =false)
            resendVerificationCode(userNo,resendToken) }


    }

    private fun verifyPhoneNumberWithOtpCode(verificationId: String, otp: String) {

        val credential = PhoneAuthProvider.getCredential(verificationId, otp)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                        statusChangeOfProgressbarAndButton(visibility=View.INVISIBLE,buttonStatus=true, resendStatus = true)
                        val intent= Intent(this@OtpVerificationActivity,HomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                } else {

                    statusChangeOfProgressbarAndButton(visibility=View.INVISIBLE,buttonStatus=true, resendStatus = true)

                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this@OtpVerificationActivity,"Invalid OTP", Toast.LENGTH_SHORT).show()
                        return@addOnCompleteListener
                    }
                    Toast.makeText(this@OtpVerificationActivity,"Error occurred", Toast.LENGTH_SHORT).show()

                }
            }

    }

    private fun resendVerificationCode(
        phoneNumber: String,
        token: ForceResendingToken
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .setForceResendingToken(token) // ForceResendingToken from callbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun validateEditText(editText: EditText):Boolean= editText.text.isNotEmpty()

    private fun editTextFocusControl() {

      otp1.addTextChangedListener(object : TextWatcher {
          override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

          override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
              if (otp1.text.length == 1)
                  otp2.requestFocus()

          }
          override fun afterTextChanged(p0: Editable?)  {}

      })

        otp2.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int)  {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (otp2.text.length == 1)
                    otp3.requestFocus()

            }
            override fun afterTextChanged(p0: Editable?)  {}

        })

        otp3.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int)  {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (otp3.text.length == 1)
                    otp4.requestFocus()

            }
            override fun afterTextChanged(p0: Editable?)  {}

        })

        otp4.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (otp4.text.length == 1)
                    otp5.requestFocus()

            }
            override fun afterTextChanged(p0: Editable?)  {}

        })

        otp5.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (otp5.text.length == 1)
                    otp6.requestFocus()

            }
            override fun afterTextChanged(p0: Editable?)  {}

        })
    }

    private fun statusChangeOfProgressbarAndButton(visibility: Int, buttonStatus: Boolean,resendStatus:Boolean) {
        progressBar.visibility= visibility
        verify.isEnabled=buttonStatus
        resend.isEnabled=resendStatus
    }
    
}