package com.example.mydairy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login_Form extends AppCompatActivity {
    private EditText txt_email, txt_password;
    private TextView tv_forget,btn_signup;
    private Button  btn_login;
    private FirebaseAuth firebaseAuth;
    private String email, password;
    private ProgressDialog LoadingBar;
    private SharedPrefrenceConfig sharedPrefrenceConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login__form);

        LoadingBar = new ProgressDialog(this);

        txt_email = (EditText) findViewById(R.id.txtemail);
        txt_password = (EditText) findViewById(R.id.txtpassword);
        btn_signup = (TextView) findViewById(R.id.btnsignup);
        btn_login = (Button) findViewById(R.id.btnlogin);
        tv_forget = (TextView) findViewById(R.id.tvforget);

        firebaseAuth = FirebaseAuth.getInstance();

        email = txt_email.getText().toString().trim();
        password = txt_password.getText().toString().trim();

        sharedPrefrenceConfig = new SharedPrefrenceConfig(getApplicationContext());

        if(sharedPrefrenceConfig.readLoginStatus())
        {
            startActivity(new Intent(Login_Form.this, MainActivity.class));
            finish();
        }

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startlogin();
            }
        });

        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login_Form.this,Signup_Form.class));
            }
        });

        tv_forget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showForgetPassword();
            }
        });
    }

    private void startlogin() {
        if(!isEmailValid(txt_email.getText().toString().trim())){
            txt_email.setError("Please Enter Valid Email");
        }
        else if(txt_password.getText().toString().trim().isEmpty())
        {
            txt_password.setError("Please Enter Password");
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
            progressDialog.show();
            firebaseAuth.signInWithEmailAndPassword(txt_email.getText().toString().trim(),txt_password.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        if (firebaseAuth.getCurrentUser().isEmailVerified()) {
                            startActivity(new Intent(Login_Form.this, MainActivity.class));
                            sharedPrefrenceConfig.writeLoginStatus(true);
                            finish();
                        }
                        else {
                            Toast.makeText(Login_Form.this, "Please Verify Email", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Toast.makeText(Login_Form.this, "Please Check Login Credentioal or Internet Connection", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void showForgetPassword() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Forget Password");

        LinearLayout linearLayout = new LinearLayout(this);
        final EditText etemail = new EditText(this);
        etemail.setHint("Enter Registered Email                      ");
        etemail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        linearLayout.addView(etemail);
        linearLayout.setPadding(10,10,10,10);
        builder.setView(linearLayout);

        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {

            @Override

            public void onClick(DialogInterface dialogInterface, int i) {

                String email = etemail.getText().toString().trim();
                if(email.isEmpty())
                {
                    etemail.setError("Please Enter Email");
                    showForgetPassword();
                }
                else {
                    LoadingBar.setTitle("Please Wait......");
                    LoadingBar.setMessage("We are sending forget password link to your registred Email Address");
                    LoadingBar.setCanceledOnTouchOutside(false);
                    LoadingBar.show();
                    beginForget(email);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    private void beginForget(String email) {
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(Login_Form.this, "Forget Password Link Sent on Registred Email", Toast.LENGTH_SHORT).show();
                    LoadingBar.dismiss();
                }
                else {
                    Toast.makeText(Login_Form.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Login_Form.this, "Enter Valid Email", Toast.LENGTH_SHORT).show();
                showForgetPassword();
            }
        });
    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
