package com.example.firebasesetup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ViewPdfActivity extends AppCompatActivity {

    private ListView listViewPdfViewer;
    private DatabaseReference databaseReference;
    private List<PDFdetails> viewPdfs;
    private TextView textViewNoPdf;
    private RelativeLayout relativeLayoutNoPdf;
    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pdf);

        getSupportActionBar().setTitle("My PDFs");

        listViewPdfViewer = findViewById(R.id.listView_view_pdf);
        listViewPdfViewer.setVisibility(View.VISIBLE);
        textViewNoPdf = findViewById(R.id.textView_view_pdf_menu);
        relativeLayoutNoPdf = findViewById(R.id.RL_view_pdf);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        viewPdfs = new ArrayList<>();

        viewAllFiles();

        listViewPdfViewer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                PDFdetails pdfDetails = viewPdfs.get(position);

                Uri webpage = Uri.parse(pdfDetails.getUrl());
                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                startActivity(intent);
            }
        });
    }

    private void viewAllFiles() {

        databaseReference = FirebaseDatabase.getInstance().getReference("Files/" + firebaseUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot postSnapshot : snapshot.getChildren()){

                    PDFdetails pdfDetails = postSnapshot.getValue(PDFdetails.class);
                    viewPdfs.add(pdfDetails);

                }

                if(viewPdfs.size() == 0){
                    textViewNoPdf.setText("No Pdfs are uploaded !");
                    relativeLayoutNoPdf.setVisibility(View.GONE);
                }
                else{
                    relativeLayoutNoPdf.setVisibility(View.VISIBLE);
                    textViewNoPdf.setVisibility(View.GONE);
                }

                String[] pdfs = new String[viewPdfs.size()];

                for(int i=0; i<pdfs.length; i++){
                    pdfs[i] = viewPdfs.get(i).getName();
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_expandable_list_item_1, pdfs){
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {

                        View view = super.getView(position, convertView, parent);

                        TextView mytext = (TextView) view.findViewById(android.R.id.text1);
                        mytext.setTextColor(Color.BLACK);

                        return view;
                    }
                };
                listViewPdfViewer.setAdapter(adapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {



            }
        });

    }
}