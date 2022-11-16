package com.example.firebasesetup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    // Defining all the things
    private EditText editTextLoginEmail, editTextLoginPwd;
    private ProgressBar progressBar;
    private FirebaseAuth authProfile;
    private static final String TAG = "LoginActivity";
    private ImageView imageViewShowHidePwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Changing action bar title
        getSupportActionBar().setTitle("Login");

        editTextLoginEmail = findViewById(R.id.editText_login_email);
        editTextLoginPwd = findViewById(R.id.editText_login_pwd);
        progressBar = findViewById(R.id.progressBar);

        authProfile = FirebaseAuth.getInstance();

        // Reset Password
        TextView textViewForgotPwd = findViewById(R.id.textView_register_forgot_password);
        textViewForgotPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(LoginActivity.this, "You can reset your password now!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });

        // Register account
        TextView textViewRegisterAcc = findViewById(R.id.textView_register_link);
        textViewRegisterAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        // Show, Hide password when pressing eye icon
        imageViewShowHidePwd = findViewById(R.id.imageView_show_hide_pwd);
        imageViewShowHidePwd.setImageResource(R.drawable.ic_hide_pwd);
        imageViewShowHidePwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editTextLoginPwd.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())) {
                    // If password is visible then hide it
                    editTextLoginPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    // Change icon
                    imageViewShowHidePwd.setImageResource(R.drawable.ic_hide_pwd);
                } else {
                    editTextLoginPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    imageViewShowHidePwd.setImageResource(R.drawable.ic_show_pwd);
                }
            }
        });

        // Login button activity
        Button btnLogin = findViewById(R.id.button_login_user);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textEmail = editTextLoginEmail.getText().toString();
                String textPwd = editTextLoginPwd.getText().toString();

                if (TextUtils.isEmpty(textEmail)) {
                    Toast.makeText(LoginActivity.this, "Please enter the email", Toast.LENGTH_SHORT).show();
                    editTextLoginEmail.setError("Email id is required");
                    editTextLoginEmail.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()) {
                    Toast.makeText(LoginActivity.this, "Please enter valid email", Toast.LENGTH_SHORT).show();
                    editTextLoginEmail.setError("Email id is not valid");
                    editTextLoginEmail.requestFocus();
                } else if (TextUtils.isEmpty(textPwd)) {
                    Toast.makeText(LoginActivity.this, "Please enter the password", Toast.LENGTH_SHORT).show();
                    editTextLoginPwd.setError("Password is required");
                    editTextLoginPwd.requestFocus();
                } else {
                    progressBar.setVisibility(View.VISIBLE);

                    // calling loginUser method
                    loginUser(textEmail, textPwd);
                }
            }
        });
    }

    private void loginUser(String textEmail, String textPwd) {
        authProfile.signInWithEmailAndPassword(textEmail, textPwd).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    // Get instance of current user
                    FirebaseUser firebaseUser = authProfile.getCurrentUser();

                    // Check if email is verified or not
                    if (firebaseUser.isEmailVerified()) {
                        Toast.makeText(LoginActivity.this, "You are logged in now", Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(LoginActivity.this, UserProfileActivity.class));
                        finish();        // Close Login Activity
                    } else {
                        firebaseUser.sendEmailVerification();
                        authProfile.signOut();   // Signs out the user

                        showAlertDialog();
                    }
                } else {
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e) {
                        editTextLoginEmail.setError("User does not exist, please register first");
                        editTextLoginEmail.requestFocus();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        editTextLoginEmail.setError("Invalid credentials. Kindly, check and re-enter");
                        editTextLoginEmail.requestFocus();
                    } catch (Exception e) {
                        Log.d(TAG, e.getMessage());
                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void showAlertDialog() {
        // Setup the alert Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("Email not verified");
        builder.setMessage("Please verify your email now. You can not login without verification.");

        // Open email app if user clicks Continue button
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);     // To open email app in new window and not within our app
                startActivity(intent);
            }
        });

        // Create the alertDialog
        AlertDialog alertDialog = builder.create();

        // Show the alertBuilder
        alertDialog.show();
    }


    @Override
    protected void onStart() {
        // Check if user is already logged in or not
        super.onStart();

        if (authProfile.getCurrentUser() != null) {
            Toast.makeText(LoginActivity.this, "Already logged in", Toast.LENGTH_SHORT).show();

            // Start the userProfileActivity
            startActivity(new Intent(LoginActivity.this, UserProfileActivity.class));
            finish();        // Close Login Activity
        } else {
            Toast.makeText(LoginActivity.this, "You can login now!", Toast.LENGTH_SHORT).show();
        }
    }
}