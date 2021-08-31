package com.example.bookapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp.databinding.ActivityPdfAddBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PdfAddActivity extends AppCompatActivity {

    private ActivityPdfAddBinding binding;
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;
    //arraylist to hold pdf categories
    private ArrayList<ModelCategory> categoryArrayList;
    //Uri of picked pdf
    private  Uri pdfUri=null;

    private static  final int PDF_PICK_CODE=1000;
    //TAG for debugging
    private static final String TAG = "ADD_PDF_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        loadPdfCategories();

        //setup progress dialog
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);


        //handle click,go to previous activity
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //handle click,attach pdf
        binding.attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pdfPickIntent();
            }
        });

        //handle click, pick category
        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryPickDialog();
            }
        });
        //handle click, upload pdf
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //validateData
                validateData();

            }
        });
    }
    private  String title="",description="",category="";

    private void validateData() {
        //step 1: validation data
        Log.d(TAG,"validateData: validation data...");

        //get data
        title=binding.titleEt.getText().toString().trim();
        description=binding.descriptionEt.getText().toString().trim();
        category=binding.categoryTv.getText().toString().trim();
        //validate dat
        if(TextUtils.isEmpty(title)){
            Toast.makeText(this, "Enter Title...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(description)){
            Toast.makeText(this, "Enter Description...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(category)){
            Toast.makeText(this, "Pick Category", Toast.LENGTH_SHORT).show();
        }
        else if (pdfUri==null){
            Toast.makeText(this, "Pick Pdf", Toast.LENGTH_SHORT).show();
        }
        else{
            //all data is valid, can upload now
            uploadPdfToStorage();
        }


    }

    private void uploadPdfToStorage() {
        //step 2: upload pdf to firebase storage
        Log.d(TAG,"uploadPdfToStorage: uploading to storage...");
        //show progress
        progressDialog.setMessage("Uploading Pdf...");
        progressDialog.show();
        //timestamp
        long timestamp=System.currentTimeMillis();
        //path of pdf in firebase storage
        String filePathAndName="Books/"+timestamp;
        //storage refrence
    }

    private void loadPdfCategories() {
        Log.d(TAG,"loadPdfCategories: Loading pdf categories...");
        categoryArrayList=new ArrayList<>();
        //db refrence to load categories
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryArrayList.clear();//clear before adding ata
                for (DataSnapshot ds: snapshot.getChildren()){
                    //get data
                    ModelCategory model = ds. getValue(ModelCategory.class);
                    //add to arraylist
                    categoryArrayList.add(model);
                    Log.d(TAG,"onDtaChange:"+model.getCategory());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void categoryPickDialog() {
        Log.d(TAG,"categoreyPickDialog: showing category pick dialog");

        //get string array of categories from arraylist
        String[] categoriesArray=new String[categoryArrayList.size()];
        for(int i=0; i<categoryArrayList.size();i++){
            categoriesArray[i]=categoryArrayList.get(i).getCategory();
        }
        //alert dialog
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setTitle("Pick Category")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        //handle item click
                        //get clicked item from list
                        String category= categoriesArray[which];
                        //set to category textview
                        binding.categoryTv.setText(category);
                        Log.d(TAG, "onClick:Selected Category:"+category);

                    }
                })
                .show();

    }

    private void pdfPickIntent() {
        Log.d(TAG,"pdfPickIntent: starting pdf pick intent");
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Pdf"),PDF_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== RESULT_OK){
            Log.d(TAG, "onActivityResult:PDF Picked");
            pdfUri=data.getData();
            Log.d(TAG, "OnActivityResult:URI"+pdfUri);
        }
        else{
            Log.d(TAG,"onActivityResults:cancelled picking pdf");
            Toast.makeText(this, "cancelled picking pdf", Toast.LENGTH_SHORT).show();
        }
    }
}