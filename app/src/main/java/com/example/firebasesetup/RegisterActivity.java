package com.example.firebasesetup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    // Getting elements from activity_register.xml
    private EditText editTextRegisterFullName, editTextRegisterEmail, editTextRegisterDoB, editTextRegisterMobile, editTextRegisterPwd;
    private ProgressBar progressBar;
    private RadioGroup radioGroupRegisterGender;
    private RadioButton radioButtonRegisterGenderSelected;
    private static final String TAG = "RegisterActivity";
    private DatePickerDialog picker;
    private CheckBox checkBoxTerms;
    private ImageView imageViewDatePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Set Action bar title
        getSupportActionBar().setTitle("Register");

        // Created toast for user convenience
        Toast.makeText(RegisterActivity.this, "You can register now", Toast.LENGTH_LONG).show();

        // finding all editText with there ids
        editTextRegisterFullName = findViewById(R.id.editText_register_full_name);
        editTextRegisterEmail = findViewById(R.id.editText_register_email);
        editTextRegisterDoB = findViewById(R.id.editText_register_dob);
        editTextRegisterMobile = findViewById(R.id.editText_register_mobile);
        editTextRegisterPwd = findViewById(R.id.editText_register_password);

        checkBoxTerms = findViewById(R.id.checkBox_terms_conditions);

        //RadioButton for gender
        radioGroupRegisterGender = findViewById(R.id.radio_group_register_gender);
        radioGroupRegisterGender.clearCheck();

        // Setting date picker on editTextRegisterDoB
        imageViewDatePicker = findViewById(R.id.imageView_date_picker);
        imageViewDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                // Defining date picker
                picker = new DatePickerDialog(RegisterActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        editTextRegisterDoB.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, day);
                picker.show();
            }
        });

        progressBar = findViewById(R.id.progressBar);

        // Getting button and putting onClickEventListener
        Button buttonRegisterUser = findViewById(R.id.button_register_user);
        buttonRegisterUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Getting gender from radioGroup

                int selectedGenderId = radioGroupRegisterGender.getCheckedRadioButtonId();
                radioButtonRegisterGenderSelected = findViewById(selectedGenderId);

                // Obtain the entered details
                String textFullName = editTextRegisterFullName.getText().toString();
                String textEmail = editTextRegisterEmail.getText().toString();
                String textDoB = editTextRegisterDoB.getText().toString();
                String textMobile = editTextRegisterMobile.getText().toString();
                String textPwd = editTextRegisterPwd.getText().toString();
                String textGender;    // Can't Obtain value before checking if button is selected or not

                // Validate Mobile number using Matcher and Pattern
                String mobileRegex = "[6-9][0-9]{9}";  // First digit 6-9 and rest nine number 0-9
                Matcher mobileMatcher;
                Pattern patternMobile = Pattern.compile(mobileRegex);
                mobileMatcher = patternMobile.matcher(textMobile);

                if (TextUtils.isEmpty(textFullName)) {
                    Toast.makeText(RegisterActivity.this, "Please enter your full name", Toast.LENGTH_SHORT).show();
                    editTextRegisterFullName.setError("Full Name is required");
                    editTextRegisterFullName.requestFocus();
                } else if (TextUtils.isEmpty(textEmail)) {
                    Toast.makeText(RegisterActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                    editTextRegisterEmail.setError("Email is required");
                    editTextRegisterEmail.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()) {
                    Toast.makeText(RegisterActivity.this, "Please re-enter your email", Toast.LENGTH_LONG).show();
                    editTextRegisterEmail.setError("Valid email is required");
                    editTextRegisterEmail.requestFocus();
                } else if (TextUtils.isEmpty(textDoB)) {
                    Toast.makeText(RegisterActivity.this, "Please enter your date of birth", Toast.LENGTH_SHORT).show();
                    editTextRegisterDoB.setError("Date of birth is required");
                    editTextRegisterDoB.requestFocus();
                } else if (radioGroupRegisterGender.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(RegisterActivity.this, "Please select the gender", Toast.LENGTH_SHORT).show();
                    radioButtonRegisterGenderSelected.setError("Gender is required");
                    radioButtonRegisterGenderSelected.requestFocus();
                } else if (TextUtils.isEmpty(textMobile)) {
                    Toast.makeText(RegisterActivity.this, "Please enter your mobile no.", Toast.LENGTH_SHORT).show();
                    editTextRegisterMobile.setError("Mobile no. is required");
                    editTextRegisterMobile.requestFocus();
                } else if (textMobile.length() != 10) {
                    Toast.makeText(RegisterActivity.this, "Please re-enter your mobile no.", Toast.LENGTH_SHORT).show();
                    editTextRegisterMobile.setError("Mobile no. should be 10 digits");
                    editTextRegisterMobile.requestFocus();
                } else if(!mobileMatcher.find()) {
                    Toast.makeText(RegisterActivity.this, "Please enter valid mobile no.", Toast.LENGTH_SHORT).show();
                    editTextRegisterMobile.setError("Mobile no. is not valid");
                    editTextRegisterMobile.requestFocus();
                }
                else if (TextUtils.isEmpty(textPwd)) {
                    Toast.makeText(RegisterActivity.this, "Please enter your password", Toast.LENGTH_SHORT).show();
                    editTextRegisterPwd.setError("Password is required");
                    editTextRegisterPwd.requestFocus();
                } else if (textPwd.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "Please re-enter your password", Toast.LENGTH_SHORT).show();
                    editTextRegisterPwd.setError("Password should be at least 6 digits");
                    editTextRegisterPwd.requestFocus();
                } else if(!checkBoxTerms.isChecked()){
                    Toast.makeText(RegisterActivity.this, "You need to accept the terms and conditions", Toast.LENGTH_SHORT).show();
                    checkBoxTerms.setError("Read Terms and Conditions and Tick the checkbox");
                    checkBoxTerms.requestFocus();
                }
                else {
                    textGender = radioButtonRegisterGenderSelected.getText().toString();

                    // Make progressBar visible
                    progressBar.setVisibility(View.VISIBLE);

                    // Calling register user method
                    registerUser(textFullName, textEmail, textDoB, textGender, textMobile, textPwd);
                }
            }
        });
    }

    // Register user using the credential given
    private void registerUser(String textFullName, String textEmail, String textDoB, String textGender, String textMobile, String textPwd) {
        // Created Instance of firebaseAuth
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Setting authentication for user
        auth.createUserWithEmailAndPassword(textEmail, textPwd).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sending user an email about registration
                    FirebaseUser firebaseUser = auth.getCurrentUser();

                    //Update display name of User
                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(textFullName).build();
                    firebaseUser.updateProfile(profileChangeRequest);

                    // Enter User Data into the firebase Realtime Database.
                    ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(textDoB, textGender, textMobile);

                    // Extracting User reference from Database for "Registered Users"
                    DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

                    referenceProfile.child(firebaseUser.getUid()).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // send verification Email
                                firebaseUser.sendEmailVerification();
                                Toast.makeText(RegisterActivity.this, "User registered successfully, Please verify your email", Toast.LENGTH_LONG).show();

                                // Open user profile after successful registration
                                Intent intent = new Intent(RegisterActivity.this, UserProfileActivity.class);

                                // To prevent user form returning back to Register Activity on pressing back button after registration
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();    // To close Register Activity
                            } else {
                                Toast.makeText(RegisterActivity.this, "User registration failed, Please try again", Toast.LENGTH_SHORT).show();
                            }

                            progressBar.setVisibility(View.GONE);
                        }
                    });
                } else {
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthWeakPasswordException e) {
                        editTextRegisterPwd.setError("Your password is too weak. Kindly use alphabets, numerals and special characters");
                        editTextRegisterPwd.requestFocus();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        editTextRegisterEmail.setError("Your email is invalid or already in use. Kindly re-enter.");
                        editTextRegisterEmail.requestFocus();
                    } catch (FirebaseAuthUserCollisionException e) {
                        editTextRegisterEmail.setError("A user is already registered with this email. Use another mail");
                        editTextRegisterEmail.requestFocus();
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }
}