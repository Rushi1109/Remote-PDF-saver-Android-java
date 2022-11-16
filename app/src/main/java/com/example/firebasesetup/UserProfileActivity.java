package com.example.firebasesetup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UserProfileActivity extends AppCompatActivity {

    // Getting textViews to display user data
    private TextView textViewWelcome, textViewFullName, textViewEmail, textViewDoB, textViewGender, textViewMobile, textViewRegisteredSince;
    private ProgressBar progressBar;
    private String fullName, email, DoB, gender, mobile, userCreationDate;
    private ImageView imageViewProfileDP;
    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;
    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        getSupportActionBar().setTitle("Home");

        swipeToRefresh();

        textViewWelcome = findViewById(R.id.textView_show_welcome);
        textViewFullName = findViewById(R.id.textView_show_full_name);
        textViewEmail = findViewById(R.id.textView_show_email);
        textViewDoB = findViewById(R.id.textView_show_dob);
        textViewGender = findViewById(R.id.textView_show_gender);
        textViewMobile = findViewById(R.id.textView_show_mobile);
        textViewRegisteredSince = findViewById(R.id.textView_show_register_date);

        imageViewProfileDP = findViewById(R.id.imageView_profile_dp);
        imageViewProfileDP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserProfileActivity.this, UploadProfilePicActivity.class);
                startActivity(intent);
            }
        });


        progressBar = findViewById(R.id.progressBarUserProfile);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        Date creationDate = new Date(firebaseUser.getMetadata().getCreationTimestamp());
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        userCreationDate = df.format(creationDate);

        if (firebaseUser == null) {
            Toast.makeText(UserProfileActivity.this, "Something went wrong! User's details are not available now", Toast.LENGTH_SHORT).show();
        } else {
            checkEmailVerified(firebaseUser);
            progressBar.setVisibility(View.VISIBLE);

            // Calling method to show user profile
            showUserProfile(firebaseUser);
        }
    }

    private void swipeToRefresh() {
        // Look for swipe container
        swipeContainer = findViewById(R.id.swipeContainer);

        // Setup Refresh Listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Code to refresh goes here. Make sure to call swipeContainer.setRefreshing(false) once the refresh is complete
                startActivity(getIntent());
                finish();
                overridePendingTransition(0, 0);

                swipeContainer.setRefreshing(false);
            }
        });

        // Configure refresh colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
    }

    // For users coming to UserProfileActivity after successful registration
    private void checkEmailVerified(FirebaseUser firebaseUser) {
        if (!firebaseUser.isEmailVerified()) {
            showAlertDialog();
        }
    }

    private void showAlertDialog() {
        // Setup the alert Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
        builder.setTitle("Email not verified");
        builder.setMessage("Please verify your email now. You can not login next time without verification.");

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

    private void showUserProfile(FirebaseUser firebaseUser) {
        String userID = firebaseUser.getUid();

        // Extracting user reference from Database for "Registered Users"
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Registered Users");
        databaseReference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // snapshot requires an empty constructor to get the data in Object form
                ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if (readUserDetails != null) {
                    fullName = firebaseUser.getDisplayName();
                    email = firebaseUser.getEmail();
                    DoB = readUserDetails.getDoB();
                    gender = readUserDetails.getGender();
                    mobile = readUserDetails.getMobile();

                    textViewWelcome.setText("Welcome, " + fullName + "!");
                    textViewFullName.setText(fullName);
                    textViewEmail.setText(email);
                    textViewDoB.setText(DoB);
                    textViewGender.setText(gender);
                    textViewMobile.setText(mobile);
                    textViewRegisteredSince.setText(userCreationDate);
                    Log.d("Date1", userCreationDate);

                    // Set user's DP(after user has uploaded)
                    Uri uri = firebaseUser.getPhotoUrl();

                    //ImageView setImageUri() can't be used, So we'll use picasso
                    Picasso.get().load(uri).into(imageViewProfileDP);

                }
                else{
                    Toast.makeText(UserProfileActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserProfileActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    // Creating ActionBar Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu items
        getMenuInflater().inflate(R.menu.common_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // When any item is selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_refresh) {
            // Refresh the Activity
            startActivity(getIntent());
            finish();
            overridePendingTransition(0, 0);
        }
        else if(id == R.id.menu_update_profile){
            Intent intent = new Intent(UserProfileActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
        }
        else if(id == R.id.menu_update_email){
            Intent intent = new Intent(UserProfileActivity.this, UpdateEmailActivity.class);
            startActivity(intent);
        }
        else if(id == R.id.menu_upload_pdf){
//            Toast.makeText(UserProfileActivity.this, "menu_settings", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(UserProfileActivity.this, UploadPdfActivity.class);
            startActivity(intent);
        }else if(id == R.id.menu_view_pdf){
//            Toast.makeText(UserProfileActivity.this, "menu_settings", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(UserProfileActivity.this, ViewPdfActivity.class);
            startActivity(intent);
        }
        else if(id == R.id.menu_change_pwd){
            Intent intent = new Intent(UserProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        }
        else if(id == R.id.menu_delete_profile){
            Intent intent = new Intent(UserProfileActivity.this, DeleteProfileActivity.class);
            startActivity(intent);
            finish();
        }
        else if(id == R.id.menu_logout){
            authProfile.signOut();
            Toast.makeText(UserProfileActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(UserProfileActivity.this, MainActivity.class);

            // Clear stack to prevent going back to user profile
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();       // CLose UserProfileActivity
        }
        else{
            Toast.makeText(UserProfileActivity.this, "Something went wrong!", Toast.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
    }
}