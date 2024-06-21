package com.example.mydairy;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.text.PageSize;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Billing extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    final int UPI_PAYMENT = 0;
    private TextView datefrom,dateto;
    int x=0;
    private ProgressDialog LoadingBar;

    private FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private TextView tv_liter, tv_name, tv_amt;
    private String dateselected;
    private EditText txt_id;

    private Double milk_sum, amt_sum;
    public String datefrom1 = "", dateto1 = "";
    private String name_pay = "", phone_pay = "", amt_pay = "";

    List<String> billing = new ArrayList<>();
    ArrayList<Double> total = new ArrayList<>();
    ArrayList<Double> total_amt = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_milk);

        LoadingBar = new ProgressDialog(this);
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseUser = firebaseAuth.getCurrentUser();

        tv_liter = (TextView) findViewById(R.id.tvliter);
        tv_amt = (TextView) findViewById(R.id.tvamt);
        tv_name = (TextView)findViewById(R.id.txtname);
        txt_id = (EditText)findViewById(R.id.txtid);

        ActivityCompat.requestPermissions(Billing.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

        txt_id.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(txt_id.getText().toString().trim().length()==10){
                    databaseReference.child("user_details").child(firebaseUser.getUid()).child("clients").orderByChild("client_mobno").equalTo(txt_id.getText().toString().trim()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                                    tv_name.setText("Name: "+snapshot.child("client_name").getValue().toString().trim());
                                }
                            }
                            else{
                                txt_id.setError("Client Not Available");
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                else {
                    tv_name.setText("Name : ");
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        datefrom = (TextView)findViewById(R.id.txtdatefrom);
        datefrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePicker=new DatePickerClass();
                datePicker.show(getSupportFragmentManager(),"datePicker");
                x=1;
            }
        });

        dateto = (TextView)findViewById(R.id.txtdateto);
        dateto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePicker = new DatePickerClass();
                datePicker.show(getSupportFragmentManager(), "datePicker");
                x = 2;
            }
        });

    }

    public void getBillingClients(){
        databaseReference.child("user_details").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("clients").orderByChild("client_mobno").equalTo(txt_id.getText().toString().trim()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                total.clear();
                total_amt.clear();
                if(dataSnapshot.exists()){
                    for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                        name_pay = snapshot.child("client_name").getValue().toString();
                        phone_pay = snapshot.child("client_mobno").getValue().toString();
                        snapshot.getRef().child("transactions").orderByChild("date").startAt(datefrom1).endAt(dateto1).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                                    String client_details = "    "+snapshot.child("date").getValue().toString() + "           " +snapshot.child("Shift").getValue().toString()+"           "+snapshot.child("Liter").getValue().toString() + "           " +snapshot.child("Fat").getValue().toString() + "           "+snapshot.child("SNF").getValue().toString();
                                    billing.add(client_details);
                                    Double total_value = Double.parseDouble(snapshot.child("Liter").getValue().toString().trim());
                                    total.add(total_value);
                                    Double amt_val = Double.parseDouble(snapshot.child("Amount").getValue().toString().trim());
                                    total_amt.add(amt_val);
                                }
                                calculate();
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
                else{
                    txt_id.setError("Client Not Available");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void calculate(){
        milk_sum = 0.0;
        amt_sum = 0.0;
        for(int i = 0; i<total.size();i++){
            milk_sum = milk_sum + total.get(i);
            amt_sum = amt_sum + total_amt.get(i);
        }
        tv_liter.setText(""+milk_sum);
        tv_amt.setText(""+amt_sum);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c= Calendar.getInstance();
        c.set(Calendar.YEAR,year);
        c.set(Calendar.MONTH,month);
        c.set(Calendar.DAY_OF_MONTH,dayOfMonth);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
        dateselected= dateFormat.format(c.getTime());
        if(x==1)
        {
            datefrom.setText(dateselected);
            datefrom1 = datefrom.getText().toString();
        }
        if(x==2)
        {
            dateto.setText(dateselected);
            dateto1 = dateto.getText().toString();
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
            getBillingClients();
            progressDialog.show();
           }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void makeBill(View view) {
        if(txt_id.getText().toString().isEmpty() || txt_id.getText().toString().trim().length()<10){
            txt_id.setError("Please Enter Valid Client ID");
        }
        else if(dateto1.isEmpty())
        {
            Toast.makeText(this, "Please Select Date", Toast.LENGTH_SHORT).show();
        }
        else if(datefrom1.isEmpty()){
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
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    progressDialog.dismiss();

                }
            }).start();
            generatePdf();
            progressDialog.show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void generatePdf() {
        PdfDocument myPdfDocument = new PdfDocument();
        PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(300, 600, 1).create();
        PdfDocument.Page myPage = myPdfDocument.startPage(myPageInfo);

        Paint myPaint = new Paint();

        String[] myString = billing.toArray(new String[billing.size()]);

        int x = 10, y = 25;
        myPage.getCanvas().drawText("Client ID - "+txt_id.getText().toString().trim()+"\n", x, y, myPaint);
        y += myPaint.descent() - myPaint.ascent();

        myPage.getCanvas().drawText("Client "+tv_name.getText().toString().trim()+"\n", x, y, myPaint);
        y += myPaint.descent() - myPaint.ascent();

        myPage.getCanvas().drawText("Your Milk Report From Date : "+datefrom1+" to "+dateto1+"\n\n", x, y, myPaint);
        y += myPaint.descent() - myPaint.ascent();

        myPage.getCanvas().drawText("\n\n", x, y, myPaint);
        y += myPaint.descent() - myPaint.ascent();

        myPage.getCanvas().drawText("      DATE               SHIFT        LITER      FAT      SNF     \n", x, y, myPaint);
        y += myPaint.descent() - myPaint.ascent();

        for (String sele : myString) {
            myPage.getCanvas().drawText(sele + "\n", x, y, myPaint);
            y += myPaint.descent() - myPaint.ascent();
        }

        myPage.getCanvas().drawText(" ", x, y, myPaint);
        y += myPaint.descent() - myPaint.ascent();

        myPage.getCanvas().drawText("Total Milk - "+tv_liter.getText().toString().trim()+" lit", x, y, myPaint);
        y += myPaint.descent() - myPaint.ascent();

        myPage.getCanvas().drawText("Total Amount - "+tv_amt.getText().toString().trim(), x, y, myPaint);
        y += myPaint.descent() - myPaint.ascent();

        myPage.getCanvas().drawText(" ", x, y, myPaint);
        y += myPaint.descent() - myPaint.ascent();

        myPage.getCanvas().drawText("Thanks for Connecting with us.....", x, y, myPaint);
        y += myPaint.descent() - myPaint.ascent();

        myPage.getCanvas().drawText("Mydairy", x, y, myPaint);
        y += myPaint.descent() - myPaint.ascent();

        myPdfDocument.finishPage(myPage);

        String str = "/Bill-"+txt_id.getText().toString()+".pdf";
        String myFilePath = Environment.getExternalStorageDirectory().getPath() + str;
        Toast.makeText(Billing.this, "Downloded File Path" + myFilePath, Toast.LENGTH_LONG).show();
        File myFile = new File(myFilePath);

        try {
            myPdfDocument.writeTo(new FileOutputStream(myFile));
        }
        catch (Exception e) {
            Toast.makeText(Billing.this, "Error" + str, Toast.LENGTH_SHORT).show();
        }
        myPdfDocument.close();
    }

    public void back(View view) {
        this.finish();
    }

    public void makepayment(View view) {
        if(txt_id.getText().toString().isEmpty() || txt_id.getText().toString().trim().length()<10){
            txt_id.setError("Please Enter Valid Client ID");
        }
        else if(dateto1.isEmpty())
        {
            Toast.makeText(this, "Please Select Date", Toast.LENGTH_SHORT).show();
        }
        else if(datefrom1.isEmpty()){
            Toast.makeText(this, "Please Select Date", Toast.LENGTH_SHORT).show();
        }
        else {
            String note = "Your Milk Payment";
            payUsingUpi(tv_amt.getText().toString().trim(), phone_pay + "@ybl", name_pay, note);
        }
    }

    void payment_pay(){
        System.out.println(name_pay);
    }


    void payUsingUpi(String amount, String upiId, String name, String note) {

        Uri uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", upiId)
                .appendQueryParameter("pn", name)
                .appendQueryParameter("tn", note)
                .appendQueryParameter("am", amount)
                .appendQueryParameter("cu", "INR")
                .build();

        Intent upiPayIntent = new Intent(Intent.ACTION_VIEW);
        upiPayIntent.setData(uri);

        // will always show a dialog to user to choose an app
        Intent chooser = Intent.createChooser(upiPayIntent, "Pay with");

        // check if intent resolves
        if(null != chooser.resolveActivity(getPackageManager())) {
            startActivityForResult(chooser, UPI_PAYMENT);
        }
        else {
            Toast.makeText(Billing.this,"No UPI app found, please install one to continue",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case UPI_PAYMENT:
                if ((RESULT_OK == resultCode) || (resultCode == 11)) {
                    if (data != null) {
                        String trxt = data.getStringExtra("response");
                        Log.d("UPI", "onActivityResult: " + trxt);
                        ArrayList<String> dataList = new ArrayList<>();
                        dataList.add(trxt);
                        upiPaymentDataOperation(dataList);
                    }
                    else {
                        Log.d("UPI", "onActivityResult: " + "Return data is null");
                        ArrayList<String> dataList = new ArrayList<>();
                        dataList.add("nothing");
                        upiPaymentDataOperation(dataList);
                    }
                }
                else {
                    Log.d("UPI", "onActivityResult: " + "Return data is null"); //when user simply back without payment
                    ArrayList<String> dataList = new ArrayList<>();
                    dataList.add("nothing");
                    upiPaymentDataOperation(dataList);
                }
                break;
        }
    }

    private void upiPaymentDataOperation(ArrayList<String> data) {
        if (isConnectionAvailable(Billing.this)) {
            String str = data.get(0);
            Log.d("UPIPAY", "upiPaymentDataOperation: "+str);
            String paymentCancel = "";
            if(str == null) str = "discard";
            String status = "";
            String approvalRefNo = "";
            String response[] = str.split("&");
            for (int i = 0; i < response.length; i++) {
                String equalStr[] = response[i].split("=");
                if(equalStr.length >= 2) {
                    if (equalStr[0].toLowerCase().equals("Status".toLowerCase())) {
                        status = equalStr[1].toLowerCase();
                    }
                    else if (equalStr[0].toLowerCase().equals("ApprovalRefNo".toLowerCase()) || equalStr[0].toLowerCase().equals("txnRef".toLowerCase())) {
                        approvalRefNo = equalStr[1];
                    }
                }
                else {
                    paymentCancel = "Payment cancelled by user.";
                }
            }

            if (status.equals("success")) {
                //Code to handle successful transaction here.
                Toast.makeText(Billing.this, "Transaction successful.", Toast.LENGTH_SHORT).show();
                Log.d("UPI", "responseStr: "+approvalRefNo);
            }
            else if("Payment cancelled by user.".equals(paymentCancel)) {
                Toast.makeText(Billing.this, "Payment cancelled by user.", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(Billing.this, "Transaction failed.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(Billing.this, "Internet connection is not available. Please check and try again", Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isConnectionAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()
                    && netInfo.isConnectedOrConnecting()
                    && netInfo.isAvailable()) {
                return true;
            }
        }
        return false;
    }

}