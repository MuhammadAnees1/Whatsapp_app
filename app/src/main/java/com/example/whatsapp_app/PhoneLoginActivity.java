package com.example.whatsapp_app;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.whatsapp_app.R.id;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private Button sendVerificationCodeButton, VerifyButton;
    private EditText InputPhoneNumber,InputVerificationCode;
    private FirebaseAuth mAth;
    private PhoneAuthOptions phoneNumber;
 private String   mVerificationId;
 private PhoneAuthProvider.ForceResendingToken mResendToken;
private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAth =FirebaseAuth.getInstance();

        sendVerificationCodeButton = findViewById (R.id.send_var_code_button);
        VerifyButton = findViewById(id.verify_button);
        InputPhoneNumber =findViewById(id.phone_number_input);
        InputVerificationCode = findViewById(id.verification_code_input);
        sendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                 String phoneNumber = InputPhoneNumber.getText().toString();
                String phoneNumber = "+92" + InputPhoneNumber.getText().toString();

                if(TextUtils.isEmpty(phoneNumber)){
                    Toast.makeText(PhoneLoginActivity.this, "please enter the number...", Toast.LENGTH_SHORT).show();
                }else {
                    PhoneAuthOptions options =
                            PhoneAuthOptions.newBuilder(mAth)
                                    .setPhoneNumber(phoneNumber)
                                    .setTimeout(60L, TimeUnit.SECONDS)
                                    .setActivity(PhoneLoginActivity.this)
                                    .setCallbacks(callbacks)
                                    .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);

                }
            }
                        });

        VerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationCodeButton.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);
            String verificationCode = InputVerificationCode.getText().toString();
                if (TextUtils.isEmpty(verificationCode)) {
                    Toast.makeText(PhoneLoginActivity.this, "please Enter the verification Code First...", Toast.LENGTH_SHORT).show();
                }
                else {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

                callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                        signInWithPhoneAuthCredential(phoneAuthCredential);

                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(PhoneLoginActivity.this, "Invalid Phone number...", Toast.LENGTH_SHORT).show();

                        sendVerificationCodeButton.setVisibility(View.VISIBLE);
                        InputPhoneNumber.setVisibility(View.VISIBLE);
                        VerifyButton.setVisibility(View.INVISIBLE);
                        InputVerificationCode.setVisibility(View.INVISIBLE);

                        }


                    @Override
                    public void onCodeSent(@NonNull String verificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {

                        Log.d(TAG, "onCodeSent:" + verificationId);


                        mVerificationId = verificationId;
                        mResendToken = token;

                        sendVerificationCodeButton.setVisibility(View.INVISIBLE);
                        InputPhoneNumber.setVisibility(View.INVISIBLE);
                        VerifyButton.setVisibility(View.VISIBLE);
                        InputVerificationCode.setVisibility(View.VISIBLE);

                    }
                };

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(PhoneLoginActivity.this, "Login successful!!",
                                    Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();


                        } else {
                            String errorMessage = task.getException().getMessage();
                            Toast.makeText(PhoneLoginActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(PhoneLoginActivity.this,MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}

