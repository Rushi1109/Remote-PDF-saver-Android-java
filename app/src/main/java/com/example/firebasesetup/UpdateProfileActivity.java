package com.example.firebasesetup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateProfileActivity extends AppCompatActivity {

    private EditText editTextUpdateName, editTextUpdateDoB, editTextUpdateMobile;
    private RadioGroup radioGroupUpdateGender;
    private RadioButton radioButtonUpdateGenderSelected;
    private String textFullName, textDoB, textGender, textMobile;
    private ImageView imageViewDatePicker;
    private FirebaseAuth authProfile;
    private ProgressBar progressBarUpdateProfile;
    private static final String TAG = "UpdateProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        getSupportActionBar().setTitle("Update Profile Details");

        progressBarUpdateProfile = findViewById(R.id.progressBarUpdateProfile);
        editTextUpdateName = findViewById(R.id.editText_update_profile_name);
        editTextUpdateDoB = findViewById(R.id.editText_update_profile_dob);
        editTextUpdateMobile = findViewById(R.id.editText_update_profile_mobile);

        radioGroupUpdateGender = findViewById(R.id.radio_group_update_profile_gender);

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        imageViewDatePicker = findViewById(R.id.imageView_date_picker);

        // Show Profile data
        showProfile(firebaseUser);

        // Upload Profile pic text
        TextView textViewUploadProfilePic = findViewById(R.id.textView_profile_upload_pic);
        textViewUploadProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UpdateProfileActivity.this, UploadProfilePicActivity.class));
                finish();
            }
        });

        // Update Email
        TextView textViewUpdateEmail = findViewById(R.id.textView_profile_update_email);
        textViewUpdateEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UpdateProfileActivity.this, UpdateEmailActivity.class));
                finish();
            }
        });

        imageViewDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Extracting saved dd,mm,yyyy into different variable by creating an array delimiter bt "/"
                String textSADoB[] = textDoB.split("/");

                final Calendar calendar = Calendar.getInstance();
                int day = Integer.parseInt(textSADoB[0]);
                int month = Integer.parseInt(textSADoB[1]) - 1;
                int year = Integer.parseInt(textSADoB[2]);

                DatePickerDialog picker;

                // Defining date picker
                picker = new DatePickerDialog(UpdateProfileActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        editTextUpdateDoB.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, day);
                picker.show();
            }
        });

        // Update Profile button
        Button btnUpdateProfile = findViewById(R.id.button_update_profile);
        btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile(firebaseUser);
            }
        });
    }

    private void updateProfile(FirebaseUser firebaseUser) {
        int selectedGenderID = radioGroupUpdateGender.getCheckedRadioButtonId();
        radioButtonUpdateGenderSelected = findViewById(selectedGenderID);

        // Validate Mobile number using Matcher and Pattern
        String mobileRegex = "[6-9][0-9]{9}";  // First digit 6-9 and rest nine number 0-9
        Matcher mobileMatcher;
        Pattern patternMobile = Pattern.compile(mobileRegex);
        mobileMatcher = patternMobile.matcher(textMobile);

        if (TextUtils.isEmpty(textFullName)) {
            Toast.makeText(UpdateProfileActivity.this, "Please enter your full name", Toast.LENGTH_SHORT).show();
            editTextUpdateName.setError("Full Name is required");
            editTextUpdateName.requestFocus();
        } else if (TextUtils.isEmpty(textDoB)) {
            Toast.makeText(UpdateProfileActivity.this, "Please enter your date of birth", Toast.LENGTH_SHORT).show();
            editTextUpdateDoB.setError("Date of birth is required");
            editTextUpdateDoB.requestFocus();
        } else if (TextUtils.isEmpty(radioButtonUpdateGenderSelected.getText())) {
            Toast.makeText(UpdateProfileActivity.this, "Please select the gender", Toast.LENGTH_SHORT).show();
            radioButtonUpdateGenderSelected.setError("Gender is required");
            radioButtonUpdateGenderSelected.requestFocus();
        } else if (TextUtils.isEmpty(textMobile)) {
            Toast.makeText(UpdateProfileActivity.this, "Please enter your mobile no.", Toast.LENGTH_SHORT).show();
            editTextUpdateMobile.setError("Mobile no. is required");
            editTextUpdateMobile.requestFocus();
        } else if (textMobile.length() != 10) {
            Toast.makeText(UpdateProfileActivity.this, "Please re-enter your mobile no.", Toast.LENGTH_SHORT).show();
            editTextUpdateMobile.setError("Mobile no. should be 10 digits");
            editTextUpdateMobile.requestFocus();
        } else if(!mobileMatcher.find()) {
            Toast.makeText(UpdateProfileActivity.this, "Please enter valid mobile no.", Toast.LENGTH_SHORT).show();
            editTextUpdateMobile.setError("Mobile no. is not valid");
            editTextUpdateName.requestFocus();
        }
        else {
            // Obtain data entered by user
            textGender = radioButtonUpdateGenderSelected.getText().toString();
            textFullName = editTextUpdateName.getText().toString();
            textDoB = editTextUpdateDoB.getText().toString();
            textMobile = editTextUpdateMobile.getText().toString();

            // Make progressBar visible
            progressBarUpdateProfile.setVisibility(View.VISIBLE);

            // Update user data in firebase realtime database. Set up dependency
            ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(textDoB,textGender, textMobile);

            // Extract User Reference from database for "Registered Users"
            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

            String userID = firebaseUser.getUid();

            referenceProfile.child(userID).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {

                        // Setting new display name
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(textFullName).build();
                        firebaseUser.updateProfile(profileUpdates);

                        Toast.makeText(UpdateProfileActivity.this, "Updated information successfully!!", Toast.LENGTH_SHORT).show();

                        // Stop user from returning again to update profile activity
                        Intent intent = new Intent(UpdateProfileActivity.this, UserProfileActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                    else{
                        try {
                            throw task.getException();
                        } catch (Exception e){
                            Toast.makeText(UpdateProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    progressBarUpdateProfile.setVisibility(View.GONE);
                }
            });
        }
    }

    // Fetch data from database and put it in editTexts
    private void showProfile(FirebaseUser firebaseUser) {
        String userID = firebaseUser.getUid();

        // Extracting user reference from database for "Registered Users"
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

        progressBarUpdateProfile.setVisibility(View.VISIBLE);

        referenceProfile.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);

                if (readUserDetails != null) {
                    textFullName = firebaseUser.getDisplayName();
                    textDoB = readUserDetails.getDoB();
                    textGender = readUserDetails.getGender();
                    textMobile = readUserDetails.getMobile();

                    editTextUpdateName.setText(textFullName);
                    editTextUpdateDoB.setText(textDoB);
                    editTextUpdateMobile.setText(textMobile);

                    // Show Gender through radio button
                    if (textGender.equals("Male")) {
                        radioButtonUpdateGenderSelected = findViewById(R.id.radio_male);
                    } else {
                        radioButtonUpdateGenderSelected = findViewById(R.id.radio_female);
                    }
                    radioButtonUpdateGenderSelected.setChecked(true);
                } else {
                    Toast.makeText(UpdateProfileActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
                progressBarUpdateProfile.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UpdateProfileActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                progressBarUpdateProfile.setVisibility(View.GONE);
            }
        });
    }
}