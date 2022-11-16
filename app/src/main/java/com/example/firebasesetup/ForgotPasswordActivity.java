package com.example.firebasesetup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText editTextPwdResetEmail;
    private Button btnPwdReset;
    private ProgressBar progressBar;
    private FirebaseAuth authProfile;
    private final static String TAG = "ForgotPasswordActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        getSupportActionBar().setTitle("Forgot Password");

        editTextPwdResetEmail = findViewById(R.id.editText_password_reset_email);
        progressBar = findViewById(R.id.progressBar_password_reset);

        btnPwdReset = findViewById(R.id.button_password_reset);
        btnPwdReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailId = editTextPwdResetEmail.getText().toString();

                if(TextUtils.isEmpty(emailId)){
                    Toast.makeText(ForgotPasswordActivity.this, "Please enter your registered email", Toast.LENGTH_SHORT).show();
                    editTextPwdResetEmail.setError("Email id is required");
                    editTextPwdResetEmail.requestFocus();
                }
                else if(!Patterns.EMAIL_ADDRESS.matcher(emailId).matches()){
                    Toast.makeText(ForgotPasswordActivity.this, "Please enter valid email id", Toast.LENGTH_SHORT).show();
                    editTextPwdResetEmail.setError("Valid email id is required");
                    editTextPwdResetEmail.requestFocus();
                }
                else{
                    progressBar.setVisibility(View.VISIBLE);

                    // Calling reset password method
                    resetPassword(emailId);
                }
            }
        });
    }

    private void resetPassword(String emailId) {
        authProfile = FirebaseAuth.getInstance();
        authProfile.sendPasswordResetEmail(emailId).addOnCompleteListener(ForgotPasswordActivity.this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(ForgotPasswordActivity.this, "Check your inbox for password reset link", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(ForgotPasswordActivity.this, MainActivity.class);

                    // Clear stack to prevent going back to forgot password activity
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();       // CLose UserProfileActivity
                }
                else{
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e) {
                        editTextPwdResetEmail.setError("User doesn't exist");
                        editTextPwdResetEmail.requestFocus();
                    } catch (Exception e){
                        Log.d(TAG, e.getMessage());
                        Toast.makeText(ForgotPasswordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(ForgotPasswordActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }

                progressBar.setVisibility(View.GONE);
            }
        });
    }
}