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

public class ChangePasswordActivity extends AppCompatActivity {

    private ProgressBar progressBarUserAuth, progressBarChangePwd;
    private FirebaseAuth authProfile;
    private EditText editTextOldPwd, editTextNewPwd, editTextConfirmPassword;
    private FirebaseUser firebaseUser;
    private TextView textViewAuthenticated;
    private String userOldPwd, userNewPwd, userConfirmPwd, userEmail;
    private Button btnChangePwd, btnPwdAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        getSupportActionBar().setTitle("Change Password");

        progressBarUserAuth = findViewById(R.id.progressBar_user_auth);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        editTextOldPwd = findViewById(R.id.editText_change_pwd_current);
        editTextNewPwd = findViewById(R.id.editText_change_pwd_new);
        editTextConfirmPassword = findViewById(R.id.editText_confirm_pwd_new);

        textViewAuthenticated = findViewById(R.id.textView_change_pwd_authenticated);

        btnChangePwd = findViewById(R.id.button_change_pwd);
        btnChangePwd.setEnabled(false);
        editTextNewPwd.setEnabled(false);
        editTextConfirmPassword.setEnabled(false);

        userEmail = firebaseUser.getEmail();

        if (firebaseUser.equals("")) {
            Toast.makeText(ChangePasswordActivity.this, "Something went wrong! User's details not available", Toast.LENGTH_SHORT).show();
        } else {
            reAuthenticate(firebaseUser);
        }
    }

    private void reAuthenticate(FirebaseUser firebaseUser) {
        btnPwdAuth = findViewById(R.id.button_change_pwd_authenticate);
        btnPwdAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userOldPwd = editTextOldPwd.getText().toString();

                if (TextUtils.isEmpty(userOldPwd)) {
                    Toast.makeText(ChangePasswordActivity.this, "Enter the old password", Toast.LENGTH_SHORT).show();
                    editTextOldPwd.setError("Password is required");
                    editTextOldPwd.requestFocus();
                } else {
                    progressBarUserAuth.setVisibility(View.VISIBLE);

                    AuthCredential authCredential = EmailAuthProvider.getCredential(userEmail, userOldPwd);

                    firebaseUser.reauthenticate(authCredential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressBarUserAuth.setVisibility(View.GONE);
                            if (task.isSuccessful()) {

                                Toast.makeText(ChangePasswordActivity.this, "User authentication successful", Toast.LENGTH_SHORT).show();

                                editTextNewPwd.setEnabled(true);
                                editTextConfirmPassword.setEnabled(true);
                                editTextOldPwd.setEnabled(false);
                                btnPwdAuth.setEnabled(false);
                                btnChangePwd.setEnabled(true);

                                textViewAuthenticated.setText("You are authenticated. You can change the password now");

                                btnChangePwd.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        userNewPwd = editTextNewPwd.getText().toString();
                                        userConfirmPwd = editTextConfirmPassword.getText().toString();

                                        if (TextUtils.isEmpty(userNewPwd)) {
                                            Toast.makeText(ChangePasswordActivity.this, "Re-enter the password", Toast.LENGTH_SHORT).show();
                                            editTextNewPwd.setError("Password can't be empty");
                                            editTextNewPwd.requestFocus();
                                        } else if (TextUtils.isEmpty(userConfirmPwd)) {
                                            Toast.makeText(ChangePasswordActivity.this, "Password confirmation is needed", Toast.LENGTH_SHORT).show();
                                            editTextNewPwd.setError("This field can't be empty");
                                            editTextNewPwd.requestFocus();
                                        } else if (userOldPwd.matches(userNewPwd)) {
                                            Toast.makeText(ChangePasswordActivity.this, "New password can't be same as old", Toast.LENGTH_SHORT).show();
                                            editTextNewPwd.setError("Enter new password");
                                            editTextNewPwd.requestFocus();
                                        } else if (!userNewPwd.matches(userConfirmPwd)) {
                                            Toast.makeText(ChangePasswordActivity.this, "Both password didn't match", Toast.LENGTH_SHORT).show();
                                            editTextConfirmPassword.setError("Re-enter new password");
                                            editTextConfirmPassword.requestFocus();
                                        } else if (userNewPwd.length() < 6) {
                                            Toast.makeText(ChangePasswordActivity.this, "Password should be at least 6 digits", Toast.LENGTH_SHORT).show();
                                            editTextNewPwd.setError("Re-enter new password");
                                            editTextNewPwd.requestFocus();
                                        }
                                        else {
                                            progressBarUserAuth.setVisibility(View.VISIBLE);

                                            changePassword(firebaseUser);
                                        }
                                    }
                                });
                            } else {
                                try {
                                    throw task.getException();
                                } catch (FirebaseAuthInvalidCredentialsException e) {
                                    Toast.makeText(ChangePasswordActivity.this, "Re-enter password", Toast.LENGTH_SHORT).show();
                                    editTextOldPwd.setError("Wrong password entered");
                                    editTextOldPwd.requestFocus();
                                } catch (Exception e) {
                                    Toast.makeText(ChangePasswordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    private void changePassword(FirebaseUser firebaseUser) {
        firebaseUser.updatePassword(userConfirmPwd).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isComplete()) {

                    Toast.makeText(ChangePasswordActivity.this, "Password has been updated.", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(ChangePasswordActivity.this, UserProfileActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    try {
                        throw task.getException();
                    } catch (Exception e) {
                        Toast.makeText(ChangePasswordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                progressBarUserAuth.setVisibility(View.GONE);
            }
        });
    }
}