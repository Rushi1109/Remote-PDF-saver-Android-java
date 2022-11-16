package com.example.firebasesetup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DeleteProfileActivity extends AppCompatActivity {

    private ProgressBar progressBarUserAuth;
    private FirebaseAuth authProfile;
    private EditText editTextAuthPwd;
    private FirebaseUser firebaseUser;
    private TextView textViewAuthenticated;
    private String userPwd, userEmail;
    private Button btnDeleteProfile, btnPwdAuth;
    private static final String TAG = "DeleteProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_profile);

        getSupportActionBar().setTitle("Delete Profile");

        progressBarUserAuth = findViewById(R.id.progressBar_user_auth);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        editTextAuthPwd = findViewById(R.id.editText_delete_user_pwd);

        textViewAuthenticated = findViewById(R.id.textView_delete_user_authenticated);

        btnDeleteProfile = findViewById(R.id.button_delete_user);
        btnDeleteProfile.setEnabled(false);

        userEmail = firebaseUser.getEmail();

        if (firebaseUser.equals("")) {
            Toast.makeText(DeleteProfileActivity.this, "Something went wrong! User's details not available", Toast.LENGTH_SHORT).show();
        } else {
            reAuthenticate(firebaseUser);
        }
    }

    private void reAuthenticate(FirebaseUser firebaseUser) {
        btnPwdAuth = findViewById(R.id.button_delete_user_authenticate);
        btnPwdAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Obtain password for authentication
                userPwd = editTextAuthPwd.getText().toString();

                if (TextUtils.isEmpty(userPwd)) {
                    Toast.makeText(DeleteProfileActivity.this, "Enter the password", Toast.LENGTH_SHORT).show();
                    editTextAuthPwd.setError("Password is required");
                    editTextAuthPwd.requestFocus();
                } else {
                    progressBarUserAuth.setVisibility(View.VISIBLE);

                    AuthCredential authCredential = EmailAuthProvider.getCredential(userEmail, userPwd);

                    firebaseUser.reauthenticate(authCredential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressBarUserAuth.setVisibility(View.GONE);
                            if (task.isSuccessful()) {

                                Toast.makeText(DeleteProfileActivity.this, "User authentication successful", Toast.LENGTH_SHORT).show();

                                btnPwdAuth.setEnabled(false);
                                editTextAuthPwd.setEnabled(false);
                                btnDeleteProfile.setEnabled(true);
                                btnDeleteProfile.setBackgroundTintList(ContextCompat.getColorStateList(DeleteProfileActivity.this, R.color.red));

                                textViewAuthenticated.setText("You are authenticated. You can delete your profile now. Be careful, all your data will be lost!!");

                                btnDeleteProfile.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        showAlertDialog();
                                    }
                                });

                            } else {
                                try {
                                    throw task.getException();
                                } catch (FirebaseAuthInvalidCredentialsException e) {
                                    Toast.makeText(DeleteProfileActivity.this, "Re-enter password", Toast.LENGTH_SHORT).show();
                                    editTextAuthPwd.setError("Password didn't match");
                                    editTextAuthPwd.requestFocus();
                                } catch (Exception e) {
                                    Toast.makeText(DeleteProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    private void showAlertDialog() {
        // Setup the alert Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(DeleteProfileActivity.this);
        builder.setTitle("Delete Profile & Related data !!!");
        builder.setMessage("Are you sure you want to delete your profile?? This action is irreversible");

        // Delete User if user clicks Continue button
        builder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteUserData(firebaseUser);
            }
        });

        // Cancel the action if clicked on cancel & return to user profile activity
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startActivity(new Intent(DeleteProfileActivity.this, UserProfileActivity.class));
                finish();
            }
        });

        // Create the alertDialog
        AlertDialog alertDialog = builder.create();

        // Change the button color of continue
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.red));
            }
        });

        // Show the alertBuilder
        alertDialog.show();
    }

    private void deleteProfile() {
        firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    authProfile.signOut();
                    Toast.makeText(DeleteProfileActivity.this, "The profile has been deleted!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(DeleteProfileActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    try {
                        throw task.getException();
                    } catch (Exception e) {
                        Toast.makeText(DeleteProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    // For deleting Profile Pic from storage
    private void deleteUserData(FirebaseUser firebaseUser) {

        // Delete display pic. Also check if the user has uploaded any pic before deleting
        if (firebaseUser.getPhotoUrl() != null) {
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            StorageReference storageReference = firebaseStorage.getReferenceFromUrl(firebaseUser.getPhotoUrl().toString());
            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Log.d(TAG, "OnSuccess : Photo Deleted");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, e.getMessage());
                    Toast.makeText(DeleteProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Delete data from realtime database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Registered Users");
        databaseReference.child(firebaseUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "OnSuccess : User data deleted");

                // Finally delete user profile
                deleteProfile();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.getMessage());
                Toast.makeText(DeleteProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}