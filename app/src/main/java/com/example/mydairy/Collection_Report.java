package com.example.mydairy;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class Collection_Report extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{
    private TextView txtdate;
    private Button btnprint;
    private String client;
    private String transaction;
    int x =0;
    int count = 0;
    private String curr_date = "";
    RecyclerView recyclerView;
    MyAdapter myAdapter;
    private Double milk_sum, amt_sum;
    private DatabaseReference mDatabaseReference;

    private List<String> client_list = new ArrayList<>();
    private List<String> transaction_list = new ArrayList<>();

    private List<Double> milk_list = new ArrayList<>();
    private List<Double> amt_list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection__report);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ActivityCompat.requestPermissions(Collection_Report.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

        txtdate = (TextView)findViewById(R.id.txtdatefrom);
        btnprint = (Button) findViewById(R.id.btn_print);

        txtdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePicker=new DatePickerClass();
                datePicker.show(getSupportFragmentManager(),"datePicker");
                x=1;
            }
        });
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c= Calendar.getInstance();
        c.set(Calendar.YEAR,year);
        c.set(Calendar.MONTH,month);
        c.set(Calendar.DAY_OF_MONTH,dayOfMonth);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
        String dateselected  = dateFormat.format(c.getTime());
        if(x==1)
        {
            txtdate.setText(dateselected);
            curr_date = txtdate.getText().toString();
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Processing.....");
            progressDialog.setMessage("Please Wait....");
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    progressDialog.dismiss();
                }
            }).start();
            progressDialog.show();
            showcard();
        }
    }


    private void showcard() {
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mDatabaseReference.child("user_details").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("clients").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshots) {
                client_list.clear();
                transaction_list.clear();
                milk_list.clear();
                amt_list.clear();
                for(DataSnapshot snap: dataSnapshots.getChildren()){
                    mDatabaseReference.child("user_details").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("clients").child(snap.getKey()).child("transactions")
                            .orderByChild("date").equalTo(curr_date)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()){
                                        int count = 0;
                                        for(final DataSnapshot snapshot : dataSnapshot.getChildren()){
                                            count++;
                                            final int finalCount = count;
                                            mDatabaseReference.child("user_details").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("clients")
                                                    .child(snapshot.child("Client_key").getValue().toString())
                                                    .addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            String name = dataSnapshot.child("client_name").getValue().toString();
                                                            String transaction = finalCount +")  Liter- "+snapshot.child("Liter").getValue().toString()+"     Fat- "+snapshot.child("Fat").getValue().toString()+"     SNF- "+snapshot.child("SNF").getValue().toString()+"     Amount- "+snapshot.child("Amount").getValue().toString();
                                                            String list_Values = name+":"+transaction;
                                                            client_list.add(list_Values);
                                                            transaction_list.add("       "+snapshot.child("Client_id").getValue().toString()+"            "+snapshot.child("Shift").getValue().toString()+"            "+snapshot.child("Liter").getValue().toString()+"                "+snapshot.child("Amount").getValue().toString());
                                                            Double ml = Double.parseDouble(snapshot.child("Liter").getValue().toString());
                                                            milk_list.add(ml);
                                                            Double amt = Double.parseDouble(snapshot.child("Amount").getValue().toString());
                                                            amt_list.add(amt);
                                                            calculate();
                                                            printing();
                                                        }
                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                        }
                                                    });
                                        }
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void printing() {
        myAdapter = new MyAdapter(this,getMyList());
        recyclerView.setAdapter(myAdapter);
    }

    private ArrayList<Model> getMyList(){
        ArrayList<Model> models = new ArrayList<>();
        for(String name : client_list)
        {
            Model m = new Model();
            String str[] = name.split(":");
            m.setTitle(str[0]);
            m.setDescription(str[1]);
            m.setImg(R.drawable.contact);
            models.add(m);
            System.out.println(name);
        }
        System.out.println(models);
        return models;
    }

    private void calculate(){
        milk_sum = 0.0;
        amt_sum = 0.0;
        for(int i = 0; i<milk_list.size();i++){
            milk_sum = milk_sum + milk_list.get(i);
            amt_sum = amt_sum + amt_list.get(i);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void createPdf(View view) {
        if(curr_date.isEmpty()){
            Toast.makeText(this, "Please Select Date", Toast.LENGTH_SHORT).show();
        }
        else {

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Processing.....");
            progressDialog.setMessage("Please Wait....");
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(700);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    progressDialog.dismiss();

                }
            }).start();
            progressDialog.show();

            PdfDocument myPdfDocument = new PdfDocument();
            PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(300, 600, 1).create();
            PdfDocument.Page myPage = myPdfDocument.startPage(myPageInfo);

            Paint myPaint = new Paint();

            String[] myString = transaction_list.toArray(new String[transaction_list.size()]);

            int x = 10, y = 25;
            myPage.getCanvas().drawText("                            *********** Milk Report ***********\n", x, y, myPaint);
            y += myPaint.descent() - myPaint.ascent();

            myPage.getCanvas().drawText("Your Milk Report of Date : " + txtdate.getText().toString() + "\n", x, y, myPaint);
            y += myPaint.descent() - myPaint.ascent();

            myPage.getCanvas().drawText("\n", x, y, myPaint);
            y += myPaint.descent() - myPaint.ascent();

            myPage.getCanvas().drawText("       CLIENT ID             SHIFT           LITER        AMOUNT     \n", x, y, myPaint);
            y += myPaint.descent() - myPaint.ascent();

            for (String sele : myString) {
                myPage.getCanvas().drawText(sele + "\n", x, y, myPaint);
                y += myPaint.descent() - myPaint.ascent();
            }

            myPage.getCanvas().drawText(" ", x, y, myPaint);
            y += myPaint.descent() - myPaint.ascent();

            myPage.getCanvas().drawText("Total Milk - " + milk_sum + " lit", x, y, myPaint);
            y += myPaint.descent() - myPaint.ascent();

            myPage.getCanvas().drawText("Total Amount - " + amt_sum + " rs.", x, y, myPaint);
            y += myPaint.descent() - myPaint.ascent();

            myPage.getCanvas().drawText(" ", x, y, myPaint);
            y += myPaint.descent() - myPaint.ascent();

            myPage.getCanvas().drawText("Thanks for Connecting with us.....", x, y, myPaint);
            y += myPaint.descent() - myPaint.ascent();

            myPage.getCanvas().drawText("Mydairy", x, y, myPaint);
            y += myPaint.descent() - myPaint.ascent();

            myPdfDocument.finishPage(myPage);

            String myFilePath = Environment.getExternalStorageDirectory().getPath() + "/Report.pdf";
            Toast.makeText(Collection_Report.this, "Pdf Downloded" + myFilePath, Toast.LENGTH_SHORT).show();
            File myFile = new File(myFilePath);

            try {
                myPdfDocument.writeTo(new FileOutputStream(myFile));
            } catch (Exception e) {
                Toast.makeText(Collection_Report.this, "Error" + myFilePath, Toast.LENGTH_SHORT).show();
            }
            myPdfDocument.close();
        }
    }

    public void back(View view) {
        this.finish();
    }
}
