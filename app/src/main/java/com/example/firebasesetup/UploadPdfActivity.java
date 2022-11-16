package com.example.firebasesetup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class UploadPdfActivity extends AppCompatActivity {

    private Button btnUploadPDF;
    private EditText editTextPdfName;
    private static final int PICK_PDF_CODE = 2342;
    private FirebaseAuth authProfile;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_pdf);

        getSupportActionBar().setTitle("Upload PDF");

        editTextPdfName = findViewById(R.id.editText_upload_pdf_name);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        storageReference = FirebaseStorage.getInstance().getReference("Files");
        databaseReference = FirebaseDatabase.getInstance().getReference("Files/" + firebaseUser.getUid());

        btnUploadPDF = findViewById(R.id.button_choose_pdf);

        btnUploadPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                selectPdfFile();

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_PDF_CODE && resultCode == RESULT_OK && data != null && data.getData() != null){
            uploadPdfFile(data.getData());
        }
    }

    private void selectPdfFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select pdf file"), PICK_PDF_CODE);
    }

    private void uploadPdfFile(Uri data) {

        final ProgressDialog progressDialog = new ProgressDialog(UploadPdfActivity.this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        StorageReference reference = storageReference.child(authProfile.getCurrentUser().getUid() + "/" +System.currentTimeMillis() + ".pdf");
        reference.putFile(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                        while(!uri.isComplete());     // Important condition because this will wait till the access token of url is generated.
                        Uri url = uri.getResult();    // Gets the url of file from storageReference

                        PDFdetails pdFdetails = new PDFdetails(editTextPdfName.getText().toString(), url.toString());

                        databaseReference.child(databaseReference.push().getKey()).setValue(pdFdetails);
                        Toast.makeText(UploadPdfActivity.this, "File Uploaded", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot snapshot) {

                        double progress = (100.0*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();

                        progressDialog.setMessage("Uploaded: " + (int)progress + "%");

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UploadPdfActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}