package com.example.mydairy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Signup_Form extends AppCompatActivity {
    private EditText txt_email, txt_password, txt_confirm_password, txt_name, txt_dairy_name,txt_mobile;
    private Button btn_register;
    private FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    FirebaseDatabase database;
    user_details user;
    FirebaseUser firebaseUser;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup__form);

        progressDialog = new ProgressDialog(this);

        txt_email = (EditText) findViewById(R.id.txtemail);
        txt_name = (EditText) findViewById(R.id.txtname);
        txt_dairy_name = (EditText) findViewById(R.id.txtdairy);
        txt_mobile = (EditText) findViewById(R.id.txtmobile);
        txt_password = (EditText) findViewById(R.id.txtpassword);
        btn_register = (Button) findViewById(R.id.btnregister);
        txt_confirm_password = (EditText) findViewById(R.id.txtconfirm_password);

        database = FirebaseDatabase.getInstance();

        databaseReference = database.getReference("user_details");
        firebaseAuth = FirebaseAuth.getInstance();
        user = new user_details();

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user_email = txt_email.getText().toString().trim();
                String user_password = txt_password.getText().toString().trim();
                if(txt_name.getText().toString().trim().isEmpty()){
                    txt_name.setError("Please Enter Name");
                }
                else if(txt_dairy_name.getText().toString().trim().isEmpty()){
                    txt_dairy_name.setError("Please Enter Dairy Name");
                }
                else if(txt_mobile.getText().toString().trim().length()<10){
                    txt_mobile.setError("Please Enter valid Mobile Number");
                }
                else if(!isEmailValid(user_email)){
                    txt_email.setError("Please Enter Valid Email");
                }
                else if(txt_password.getText().toString().trim().isEmpty()){
                    txt_password.setError("Please Enter Password");
                }
                else if(txt_confirm_password.getText().toString().trim().isEmpty()){
                    txt_confirm_password.setError("Please Enter Confirm Password");
                }
                else if(!txt_password.getText().toString().trim().equals(txt_confirm_password.getText().toString().trim())){
                    txt_confirm_password.setError("Password are not Matching");
                }
                else {
                    firebaseAuth.createUserWithEmailAndPassword(user_email, user_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                progressDialog.setTitle("Sending Verification Email");
                                progressDialog.setMessage("Please wait we are sending verification email to verify your account....");
                                progressDialog.setCanceledOnTouchOutside(false);
                                progressDialog.show();

                                firebaseUser = firebaseAuth.getCurrentUser();
                                user.setName(txt_name.getText().toString().trim());
                                user.setDairy_name(txt_dairy_name.getText().toString().trim());
                                user.setMobile(txt_mobile.getText().toString().trim());
                                user.setEmail(txt_email.getText().toString().trim());
                                databaseReference.child(firebaseUser.getUid()).setValue(user);

                                sendEmailVerification();
                            } else {
                                Toast.makeText(Signup_Form.this, "Email Already Registered Otherwise Check Your Internet Connection", Toast.LENGTH_SHORT).show();
                            }
                        }

                    });

                }
            }
        });
    }

    private void sendEmailVerification() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser!=null){
            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(Signup_Form.this, "Successfully Registered, Verification mail sent!", Toast.LENGTH_SHORT).show();
                        firebaseAuth.signOut();
                        finish();
                        startActivity(new Intent(Signup_Form.this, Login_Form.class));
                    }
                    else{
                        Toast.makeText(Signup_Form.this, "Verification mail has'nt been sent!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

}
