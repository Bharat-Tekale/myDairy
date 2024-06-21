package com.example.mydairy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddMilk extends AppCompatActivity {
    private TextView txtdate,amount,txtamount,name, txt_email;
    private Button btnsave;
    private  TextView price;
    private RadioGroup groupshift;
    private RadioButton btnshift;
    private FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private EditText mobileInp;
    private Button saveBtn;
    private EditText liter,fat,snf;
    private TextView txt_name;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Calendar cdate = Calendar.getInstance();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
        String Curr_date= dateFormat.format(cdate.getTime());

        txtdate = findViewById(R.id.txtdate);
        amount=(TextView)findViewById(R.id.txtprice);
        txt_name = (TextView)findViewById(R.id.txtname);
        txt_email =(TextView) findViewById(R.id.txtemail);
        txtdate.setText(Curr_date);

        liter = (EditText) findViewById(R.id.txtliter);
        fat = (EditText) findViewById(R.id.txtfat);
        snf = (EditText) findViewById(R.id.txtsnf);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseUser = firebaseAuth.getCurrentUser();

        price = (TextView) findViewById(R.id.txtprice);
        saveBtn = findViewById(R.id.btnsave);

        mobileInp = findViewById(R.id.clientIdInp);

        mobileInp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(mobileInp.getText().toString().trim().length()==10){
                    databaseReference.child("user_details").child(firebaseUser.getUid()).child("clients").orderByChild("client_mobno").equalTo(mobileInp.getText().toString().trim()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                                    txt_name.setText("Name: "+snapshot.child("client_name").getValue().toString().trim());
                                    email = snapshot.child("client_email").getValue().toString().trim();
                                }
                            }
                            else{
                                mobileInp.setError("Client  Not Available");
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                else {
                    txt_name.setText("Name: ");
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        snf.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(liter.getText().toString().trim().isEmpty()){
                }
                else if(fat.getText().toString().trim().isEmpty()){
                    Toast.makeText(AddMilk.this, "Please Enter Milk Fat", Toast.LENGTH_SHORT).show();
                }
                else if(!snf.getText().toString().trim().isEmpty()){
                    calculate();
                }
                else{
                    price.setText("00.00");
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mobileInp.getText().toString().trim().isEmpty() || mobileInp.getText().toString().trim().length()<10)
                {
                    mobileInp.setError("Please Enter ID");
                }
                else if(liter.getText().toString().trim().isEmpty()){
                    liter.setError("Please Enter Milk Quantity");
                }
                else if(fat.getText().toString().trim().isEmpty()){
                    fat.setError("Please Enter Milk Fat");
                }
                else if(snf.getText().toString().trim().isEmpty()){
                    snf.setError("Please Enter Milk SNF");
                }
                else{
                searchClient(mobileInp.getText().toString());
                }
            }
        });
    }

    public void searchClient(final String query){
        groupshift = (RadioGroup) findViewById(R.id.rbshift);
        int selectedId = groupshift.getCheckedRadioButtonId();
        btnshift = (RadioButton) findViewById(selectedId);
        databaseReference.child("user_details").child(firebaseUser.getUid()).child("clients").orderByChild("client_mobno").equalTo(query).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot snap:dataSnapshot.getChildren()){
                        String id = databaseReference.child("user_details").child(firebaseAuth.getCurrentUser().getUid()).child("clients")
                                .child(snap.getKey()).child("transactions").push().getKey();
                        String str=btnshift.toString();
                        Map<String,Object> milk = new HashMap<>();
                        milk.put("date",txtdate.getText().toString());
                        milk.put("Liter",liter.getText().toString());
                        milk.put("Fat",fat.getText().toString());
                        milk.put("SNF",snf.getText().toString());
                        milk.put("Shift",btnshift.getText().toString());
                        milk.put("Amount",price.getText().toString());
                        milk.put("Client_id",query);
                        milk.put("Client_key",snap.getKey());
                        databaseReference.child("user_details").child(firebaseAuth.getCurrentUser().getUid()).child("clients")
                                .child(snap.getKey()).child("transactions").child(id).setValue(milk);
                    }
                    sendEmail();
                    mobileInp.setText("");
                    liter.setText("");
                    fat.setText("");
                    snf.setText("");
                    price.setText("");
                }
                else{
                    mobileInp.setError("Client Not Available");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendEmail() {
        String subject = "Your Today's Milk Report. Date: "+txtdate.getText().toString().trim();
        String message = "Dear, Customer \nYour Today's Milk Reports are Following \nMilk Quantity =  "+liter.getText().toString()+
                "\nMilk Fat = "+fat.getText().toString()+"\nMilk SNF = "+snf.getText().toString()+
                "\nThanks for connecting with us...\n\t\t\tRegards MyDairy";
        SendMail sm = new SendMail(this,email,subject,message);
        sm.execute();
    }

    public void back(View view) {
        this.finish();
    }

    private void calculate()
    {
        databaseReference.child("user_details").child(firebaseAuth.getCurrentUser().getUid()).child("rate").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String fat1,snf1,rate1;
                fat1 = dataSnapshot.child("fat").getValue().toString().trim();
                snf1 = dataSnapshot.child("snf").getValue().toString().trim();
                rate1 = dataSnapshot.child("rate").getValue().toString().trim();

                double dfat,dsnf,drate, calculate_amt;
                dfat = Double.parseDouble(fat1);
                dsnf = Double.parseDouble(snf1);
                drate = Double.parseDouble(rate1);
                double milk_qty = Double.parseDouble(liter.getText().toString().trim());

                if(Double.parseDouble(fat.getText().toString().trim())<=dfat && Double.parseDouble(snf.getText().toString().trim())<=dsnf){

                    double n = Math.abs(Double.parseDouble(fat.getText().toString().trim()) - dfat);
                    double m = Math.abs(Double.parseDouble(snf.getText().toString().trim()) - dsnf);
                    double n1 = n * 0.50 * Double.parseDouble(liter.getText().toString().trim());;
                    double m1 = m * 0.50 * Double.parseDouble(liter.getText().toString().trim());;

                    calculate_amt = (milk_qty * drate)-(n1+m1);
                    calculate_amt = Double.parseDouble(new DecimalFormat("####.##").format(calculate_amt));
                    price.setText(""+calculate_amt);
                }
                else {
                    double n = Math.abs(Double.parseDouble(fat.getText().toString().trim()) - dfat);
                    double m = Math.abs(Double.parseDouble(snf.getText().toString().trim()) - dsnf);
                    System.out.println(n);
                    System.out.println(m);
                    double n1 = n * 0.50;
                    double m1 = m * 0.50;
                    System.out.println(n1);
                    System.out.println(m1);

                    calculate_amt = (milk_qty * drate)+(n1+m1);
                    calculate_amt = Double.parseDouble(new DecimalFormat("####.##").format(calculate_amt));
                    price.setText(""+calculate_amt);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}