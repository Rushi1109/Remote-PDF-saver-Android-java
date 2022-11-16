package com.example.firebasesetup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;

public class UpdateEmailActivity extends AppCompatActivity {

    private ProgressBar progressBarUserAuth;
    private FirebaseAuth authProfile;
    private EditText editTextUserPwd, editTextNewEmail;
    private FirebaseUser firebaseUser;
    private TextView textViewAuthenticated, textViewOldEmailId;
    private String userOldEmail, userNewEmail, userPwd;
    private Button btnUpdateEmail, btnPwdAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_email);

        getSupportActionBar().setTitle("Update Email");

        progressBarUserAuth = findViewById(R.id.progressBar_user_auth);

        authProfile = FirebaseAuth.getInstance();

        editTextUserPwd = findViewById(R.id.editText_update_email_verify_password);
        editTextNewEmail = findViewById(R.id.editText_update_email_new);

        firebaseUser = authProfile.getCurrentUser();

        textViewAuthenticated = findViewById(R.id.textView_update_email_authenticated);
        textViewOldEmailId = findViewById(R.id.textView_update_email_old);

        btnUpdateEmail = findViewById(R.id.button_update_email);
        btnUpdateEmail.setEnabled(false);
        editTextNewEmail.setEnabled(false);

        // Set old email ID in textView
        userOldEmail = firebaseUser.getEmail();
        textViewOldEmailId.setText(userOldEmail);

        if (firebaseUser.equals("")) {
            Toast.makeText(UpdateEmailActivity.this, "Something went wrong! User's details not available", Toast.LENGTH_SHORT).show();
        } else {
            reAuthenticate(firebaseUser);
        }
    }

    // ReAuthenticate/verify user before updating email
    private void reAuthenticate(FirebaseUser firebaseUser) {
        btnPwdAuth = findViewById(R.id.button_authenticate_user);
        btnPwdAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Obtain password for authentication
                userPwd = editTextUserPwd.getText().toString();

                if (TextUtils.isEmpty(userPwd)) {
                    Toast.makeText(UpdateEmailActivity.this, "Enter the password", Toast.LENGTH_SHORT).show();
                    editTextUserPwd.setError("Password is required");
                    editTextUserPwd.requestFocus();
                } else {
                    progressBarUserAuth.setVisibility(View.VISIBLE);

                    AuthCredential authCredential = EmailAuthProvider.getCredential(userOldEmail, userPwd);

                    firebaseUser.reauthenticate(authCredential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressBarUserAuth.setVisibility(View.GONE);
                            if (task.isSuccessful()) {

                                Toast.makeText(UpdateEmailActivity.this, "User authentication successful", Toast.LENGTH_SHORT).show();

                                editTextNewEmail.setEnabled(true);
                                editTextUserPwd.setEnabled(false);
                                btnPwdAuth.setEnabled(false);
                                btnUpdateEmail.setEnabled(true);

                                textViewAuthenticated.setText("You are authenticated. You can update your email now");

                                btnUpdateEmail.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        userNewEmail = editTextNewEmail.getText().toString();

                                        if(TextUtils.isEmpty(userNewEmail)){
                                            Toast.makeText(UpdateEmailActivity.this, "Re-enter the email", Toast.LENGTH_SHORT).show();
                                            editTextNewEmail.setError("Email can't be empty");
                                            editTextNewEmail.requestFocus();
                                        }
                                        else if(!Patterns.EMAIL_ADDRESS.matcher(userNewEmail).matches()){
                                            Toast.makeText(UpdateEmailActivity.this, "Enter valid email", Toast.LENGTH_SHORT).show();
                                            editTextNewEmail.setError("Valid email required");
                                            editTextNewEmail.requestFocus();
                                        }
                                        else if(userOldEmail.matches(userNewEmail)){
                                            Toast.makeText(UpdateEmailActivity.this, "New email can't be same as old", Toast.LENGTH_SHORT).show();
                                            editTextNewEmail.setError("Enter new email");
                                            editTextNewEmail.requestFocus();
                                        }
                                        else{
                                            progressBarUserAuth.setVisibility(View.VISIBLE);

                                            updateEmail(firebaseUser);
                                        }
                                    }
                                });
                            }
                            else{
                                try {
                                    throw task.getException();
                                } catch (FirebaseAuthInvalidCredentialsException e){
                                    Toast.makeText(UpdateEmailActivity.this, "Re-enter password", Toast.LENGTH_SHORT).show();
                                    editTextUserPwd.setError("Password didn't match");
                                    editTextUserPwd.requestFocus();
                                } catch (Exception e){
                                    Toast.makeText(UpdateEmailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    private void updateEmail(FirebaseUser firebaseUser) {
        firebaseUser.updateEmail(userNewEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isComplete()){

                    // Verify new email
                    firebaseUser.sendEmailVerification();

                    Toast.makeText(UpdateEmailActivity.this, "Email has been updated. Please verify your new email", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(UpdateEmailActivity.this, UserProfileActivity.class);
                    startActivity(intent);
                    finish();
                }
                else{
                    try {
                        throw task.getException();
                    } catch (Exception e){
                        Toast.makeText(UpdateEmailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                progressBarUserAuth.setVisibility(View.GONE);
            }
        });
    }
}