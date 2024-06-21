package com.example.mydairy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

import java.util.HashMap;
import java.util.Map;

public class ServerDetails extends AppCompatActivity {
    private EditText username,dairyname,mobno;
    private TextView email;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_details);

        username = (EditText) findViewById(R.id.name);
        dairyname = (EditText) findViewById(R.id.dairyname);
        email = (TextView) findViewById(R.id.email);
        mobno = (EditText) findViewById(R.id.mobno);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseUser = firebaseAuth.getCurrentUser();

       fetchSerever();

       Button update = (Button) findViewById(R.id.btnupdate);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dairyname.getText().toString().trim().isEmpty())
                {
                    dairyname.setError("Please Enter Dairy Name");
                }
                else if(username.getText().toString().trim().isEmpty()){
                    username.setError("Please Enter Name");
                }
                else if(mobno.getText().toString().trim().isEmpty()){
                    mobno.setError("Please Enter Mobile Number");
                }
                else {
                    databaseReference.child("user_details").child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Map<String, Object> map = new HashMap<>();

                            map.put("dairy_name", dairyname.getText().toString().trim());
                            map.put("name", username.getText().toString().trim());
                            map.put("mobile", mobno.getText().toString().trim());
                            map.put("email", email.getText().toString().trim());

                            databaseReference.child("user_details").child(firebaseUser.getUid()).updateChildren(map);
                            Toast.makeText(ServerDetails.this, "Update Successful", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    fetchSerever();
                }
            }
        });
    }

    public  void fetchSerever()
    {
        databaseReference.child("user_details").child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString().trim();
                String dairy = dataSnapshot.child("dairy_name").getValue().toString().trim();
                String mob =dataSnapshot.child("mobile").getValue().toString().trim();
                String email1 = dataSnapshot.child("email").getValue().toString().trim();

                username.setText(name);
                dairyname.setText(dairy);
                email.setText(email1);
                mobno.setText(mob);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void back(View view) {
        this.finish();
    }
}