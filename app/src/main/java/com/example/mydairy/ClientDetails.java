package com.example.mydairy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ClientDetails extends AppCompatActivity {

    private Button btn_update, btn_delete;
    private EditText txt_id, txt_name1, txt_mobile, txt_email, txt_addresss;
    private TextView txt_name;

    private FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_data);

        txt_id = (EditText) findViewById(R.id.txtid);
        txt_name = (TextView) findViewById(R.id.txtname);
        btn_update = (Button) findViewById(R.id.btnupdate);
        btn_delete = (Button) findViewById(R.id.btndelete);
        txt_name1 = (EditText) findViewById(R.id.txtname1);
        txt_mobile = (EditText) findViewById(R.id.txtmobile);
        txt_email = (EditText) findViewById(R.id.txtemail);
        txt_addresss = (EditText) findViewById(R.id.txtaddress);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseUser = firebaseAuth.getCurrentUser();

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
                                    txt_name.setText("Name: "+snapshot.child("client_name").getValue().toString().trim());
                                    txt_name1.setText(snapshot.child("client_name").getValue().toString().trim());
                                    txt_mobile.setText(snapshot.child("client_mobno").getValue().toString().trim());
                                    txt_email.setText(snapshot.child("client_email").getValue().toString().trim());
                                    txt_addresss.setText(snapshot.child("client_address").getValue().toString().trim());
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
                    txt_name.setText("Name: ");
                    txt_name1.setText("");
                    txt_mobile.setText("");
                    txt_email.setText("");
                    txt_addresss.setText("");
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txt_id.getText().toString().trim().isEmpty() || txt_id.getText().toString().trim().length()>10) {
                    txt_id.setError("Please Enter Valid Client ID");
                }
                else {
                    deleteClient(txt_id.getText().toString());
                }
            }
        });

       btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (txt_id.getText().toString().trim().isEmpty()) {
                    txt_id.setError("Please Enter Client ID");
                }
                else if(txt_name1.getText().toString().trim().isEmpty()){
                    txt_name1.setError("Please Enter Name");
                }
                else if(txt_mobile.getText().toString().trim().length()<10){
                    txt_mobile.setError("Please Enter Valid Mobile Number");
                }
                else if(!isEmailValid(txt_email.getText().toString().trim())){
                    txt_email.setError("Please Enter Valid Email");
                }
                else if(txt_addresss.getText().toString().trim().isEmpty()){
                    txt_addresss.setError("Please Enter Address");
                }
                else {
                    updateClientDetail(txt_id.getText().toString());
                }
            }
        });
    }

    public void updateClientDetail(String query){
        databaseReference.child("user_details").child(firebaseUser.getUid()).child("clients").orderByChild("client_mobno").equalTo(query).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Map<String, Object> clinet = new HashMap<>();
                    clinet.put("client_name", txt_name1.getText().toString().trim());
                    clinet.put("client_email", txt_email.getText().toString().trim());
                    clinet.put("client_mobno", txt_mobile.getText().toString().trim());
                    clinet.put("client_address", txt_addresss.getText().toString().trim());

                    for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                        databaseReference.child("user_details").child(firebaseAuth.getCurrentUser().getUid()).child("clients").child(snapshot.getKey()).updateChildren(clinet);
                    }
                    sendEmail();
                    Toast.makeText(ClientDetails.this, "Client Details Updated Successfully", Toast.LENGTH_SHORT).show();
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

    public void deleteClient(final String query){
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
        databaseReference.child("user_details").child(firebaseUser.getUid()).child("clients").orderByChild("client_mobno").equalTo(query).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                        snapshot.getRef().removeValue();
                    }
                    Toast.makeText(ClientDetails.this, "Client Removed Successfully", Toast.LENGTH_SHORT).show();
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

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void sendEmail() {
        String email1 = txt_email.getText().toString().trim();
        String subject = "Your Details has been Successfully Updated";
        String message = "Dear, "+txt_name1.getText().toString().trim()+" Congratulations!!! " +
                "\nYour Details has been Successfully Updated " +
                "Your Updated Details are Following - " +
                "Name :- " +txt_name1.getText().toString().trim()+
                "Mobile :- " +txt_mobile.getText().toString().trim()+
                "Email :- " +txt_email.getText().toString().trim()+
                "Address :- " +txt_addresss.getText().toString().trim()+
                "\nThanks for Connecting with us... \n\t\tRegards - MyDairy.";
        SendMail sm = new SendMail(this,email1,subject,message);
        sm.execute();
    }

    public void back(View view) {
        this.finish();
    }
}
