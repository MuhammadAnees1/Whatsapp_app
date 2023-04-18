package com.example.whatsapp_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
public class LoginActivity extends AppCompatActivity {

    private Button login_button , PhoneLoginButton;
    private EditText login_password ,login_email;
    private TextView NeedNewAccountLink, ForgetPasswordLink;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        InitializeFields();

        mAuth = FirebaseAuth.getInstance();


        NeedNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerActivity = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity (registerActivity);
            }
        });
      login_button.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              AllowUserToLogin();
          }
      });

      PhoneLoginButton.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              Intent intent = new Intent(LoginActivity.this,PhoneLoginActivity.class);
              startActivity(intent);
          }
      });

    }
    private void AllowUserToLogin() {
        String Email = login_email.getText().toString();
        String password = login_password.getText().toString();

        if (TextUtils.isEmpty(Email)) {
            Toast.makeText(this, "please Enter the email", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "please Enter the password", Toast.LENGTH_SHORT).show();
        } else {
            mAuth.signInWithEmailAndPassword(Email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Login successful!!",
                                    Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();

                        } else {
                            String errorMessage = task.getException().getMessage();
                            Toast.makeText(LoginActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();

                        }
                    });
        }
    }
    private void InitializeFields()
    {
        login_button = findViewById(R.id.login_button);
        PhoneLoginButton = findViewById(R.id.phone_login_button);
        login_email =findViewById(R.id.login_email);
        login_password = findViewById(R.id.login_password);
        NeedNewAccountLink = findViewById(R.id.need_new_account_link);
        ForgetPasswordLink = findViewById(R.id.forget_password_link);
    }
    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}