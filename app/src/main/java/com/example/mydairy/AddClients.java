package com.example.mydairy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;

public class AddClients extends AppCompatActivity {
    private EditText name,email,mobno,address;
    private Button save;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_clients);

        name = (EditText) findViewById(R.id.txtname);
        email = (EditText) findViewById(R.id.txtmail);
        mobno = (EditText) findViewById(R.id.txtmob);
        address = (EditText) findViewById(R.id.txtaddress);
        save = (Button) findViewById(R.id.btnsave);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseUser = firebaseAuth.getCurrentUser();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(name.getText().toString().trim().isEmpty()){
                    name.setError("Please Enter Name");
                }
                else if(mobno.getText().toString().trim().length()<10){
                    mobno.setError("Enter Valid Mobile Number");
                }
                else if(!isEmailValid(email.getText().toString().trim())){
                    email.setError("Please Enter Valid Email");
                }
                else if(address.getText().toString().trim().isEmpty()){
                    address.setError("Please Enter Address");
                }
                else {
                    databaseReference.child("user_details").child(firebaseUser.getUid()).child("clients").orderByChild("client_mobno").equalTo(mobno.getText().toString().trim()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.exists()) {
                                String id = databaseReference.child("user_details").child(firebaseAuth.getCurrentUser().getUid()).child("clients").push().getKey();
                                Map<String, Object> clinet = new HashMap<>();
                                clinet.put("client_name", name.getText().toString().trim());
                                clinet.put("client_email", email.getText().toString().trim());
                                clinet.put("client_mobno", mobno.getText().toString().trim());
                                clinet.put("client_address", address.getText().toString().trim());
                                clinet.put("client_id", id);
                                databaseReference.child("user_details").child(firebaseAuth.getCurrentUser().getUid()).child("clients").child(id).setValue(clinet);
                                Toast.makeText(AddClients.this, "New Client Added Successfully", Toast.LENGTH_SHORT).show();
                                sendEmail();
                                startActivity(new Intent(AddClients.this, MainActivity.class));
                            }
                            else {
                                Toast.makeText(AddClients.this, "Client Exists", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
            }
        });
    }

    private void sendEmail() {
        String email1 = email.getText().toString().trim();
        String subject = "Your Account is Successfully Created by MyDairy";
        String message = "Dear, "+name.getText().toString().trim()+" Congratulations!!! " +
                "\n\tYou are one of the new Member of MyDairy. " +
                "Your Account has been Created Successfully. With the following details - " +
                "\nName :- " +name.getText().toString().trim()+
                "\nMobile :- " +mobno.getText().toString().trim()+
                "\nEmail :- " +email.getText().toString().trim()+
                "\nAddress :- " +address.getText().toString().trim()+
                "\nThanks for being new member of MyDairy..." +
                "\n\t\tRegards - MyDairy.";
        SendMail sm = new SendMail(this,email1,subject,message);
        sm.execute();
    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void back(View view) {
        this.finish();
    }
}
