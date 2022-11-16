package com.example.firebasesetup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class UploadProfilePicActivity extends AppCompatActivity {

    private ProgressBar progressBarUploadPic;
    private ImageView imageViewUploadPic;
    private FirebaseAuth authProfile;
    private StorageReference storageReference;
    private FirebaseUser firebaseUser;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri uriImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_profile_pic);

        getSupportActionBar().setTitle("Upload Profile Picture");

        Button btnChoosePic = findViewById(R.id.button_choose_pic);
        Button btnUploadPic = findViewById(R.id.button_upload_dp_pic);
        progressBarUploadPic = findViewById(R.id.progressBarUploadPic);
        imageViewUploadPic = findViewById(R.id.imageView_pic_choose);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        storageReference = FirebaseStorage.getInstance().getReference("DisplayPics");

        // To store the requested data(resource) in variable
        Uri uri = firebaseUser.getPhotoUrl();

        // Set user's Current DP in ImageView (if uploaded earlier). We will use Picasso
        Picasso.get().load(uri).into(imageViewUploadPic);

        // Choosing image to upload
        btnChoosePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });

        // Uploading image to firebase
        btnUploadPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBarUploadPic.setVisibility(View.VISIBLE);

                UploadPic();
            }
        });
    }

    private void UploadPic() {
        if(uriImage != null){
            // Save the image with uid of the currently logged user
            StorageReference fileReference = storageReference.child(authProfile.getCurrentUser().getUid() + "/displaypic." + getFileExtention(uriImage));

            // Upload image to storage
            fileReference.putFile(uriImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downloadUri = uri;
                            firebaseUser = authProfile.getCurrentUser();

                            // Finally set the display image of user after upload
                            UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                    .setPhotoUri(downloadUri).build();
                            firebaseUser.updateProfile(profileUpdate);
                        }
                    });

                    progressBarUploadPic.setVisibility(View.GONE);
                    Toast.makeText(UploadProfilePicActivity.this, "Uploaded Successfully!!", Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(UploadProfilePicActivity.this, UserProfileActivity.class));
                    finish();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UploadProfilePicActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else{
            progressBarUploadPic.setVisibility(View.GONE);
            Toast.makeText(UploadProfilePicActivity.this, "No file selected!", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileExtention(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();

        return mime.getExtensionFromMimeType(cR.getType(uri));
    }


    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uriImage = data.getData();
            imageViewUploadPic.setImageURI(uriImage);
        }

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
            Intent intent = new Intent(UploadProfilePicActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
            finish();
        }
//        else if(id == R.id.menu_update_email){
//            Intent intent = new Intent(UserProfileActivity.this, UpdateEmailActivity.class);
//            startActivity(intent);
//        }
//        else if(id == R.id.menu_settings){
//            Toast.makeText(UserProfileActivity.this, "menu_settings", Toast.LENGTH_SHORT).show();
//        }
//        else if(id == R.id.menu_change_pwd){
//            Intent intent = new Intent(UserProfileActivity.this, ChangePasswordActivity.class);
//            startActivity(intent);
//        }
//        else if(id == R.id.menu_delete_profile){
//            Intent intent = new Intent(UserProfileActivity.this, DeleteProfileActivity.class);
//            startActivity(intent);
//        }
        else if (id == R.id.menu_logout) {
            authProfile.signOut();
            Toast.makeText(UploadProfilePicActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(UploadProfilePicActivity.this, MainActivity.class);

            // Clear stack to prevent going back to user profile
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();       // CLose UserProfileActivity
        } else {
            Toast.makeText(UploadProfilePicActivity.this, "Something went wrong!", Toast.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
    }
}