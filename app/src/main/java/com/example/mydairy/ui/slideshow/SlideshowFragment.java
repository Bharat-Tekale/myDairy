package com.example.mydairy.ui.slideshow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.mydairy.AddMilk;
import com.example.mydairy.R;
import com.example.mydairy.Signup_Form;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class SlideshowFragment extends Fragment {

    private SlideshowViewModel slideshowViewModel;

    private TextView tfat,tsnf,trate;
    private EditText efat,esnf,erate;
    private Button bupdate,bsave;
    private  CardView card;

    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        slideshowViewModel = ViewModelProviders.of(this).get(SlideshowViewModel.class);
        View root = inflater.inflate(R.layout.fragment_rate, container, false);

        card = (CardView) root.findViewById(R.id.card);

        tfat = (TextView) root.findViewById(R.id.txtfatvalue);
        tsnf = (TextView) root.findViewById(R.id.txtsnfvalue);
        trate = (TextView) root.findViewById(R.id.txtpricevalue);

        efat = (EditText) root.findViewById(R.id.txtnewfat);
        esnf = (EditText) root.findViewById(R.id.txtnewsnf);
        erate = (EditText) root.findViewById(R.id.txtnewprice);

        bupdate = (Button) root.findViewById(R.id.btnupdate);
        bsave = (Button) root.findViewById(R.id.btnsave);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("user_details").child(firebaseAuth.getCurrentUser().getUid()).child("rate").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
              if(!dataSnapshot.exists())
              {
                  Toast.makeText(getActivity(),"update the Rate",Toast.LENGTH_SHORT).show();
              }
              else
              {
                  tfat.setText(dataSnapshot.child("fat").getValue().toString().trim());
                  tsnf.setText(dataSnapshot.child("snf").getValue().toString().trim());
                  trate.setText(dataSnapshot.child("rate").getValue().toString().trim());
              }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        bupdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bsave.setVisibility(View.VISIBLE);
                card.setVisibility(View.VISIBLE);
            }
        });

        bsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    databaseReference.child("user_details").child(firebaseAuth.getCurrentUser().getUid()).child("rate").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                           Map<String,Object> rate = new HashMap<>();
                           if(efat.getText().toString().trim().isEmpty())
                           {
                               efat.setError("Please Enter Fat");
                           }
                           else if(esnf.getText().toString().trim().isEmpty()){
                               esnf.setError("Please Enter SNF");
                           }
                           else if(erate.getText().toString().trim().isEmpty())
                           {
                               erate.setError("Please Enter Milk Rate");
                           }
                           else {
                               rate.put("fat", efat.getText().toString().trim());
                               rate.put("snf", esnf.getText().toString().trim());
                               rate.put("rate", erate.getText().toString().trim());

                               databaseReference.child("user_details").child(firebaseAuth.getCurrentUser().getUid()).child("rate").setValue(rate);
                               bsave.setVisibility(View.INVISIBLE);
                               card.setVisibility(View.INVISIBLE);
                           }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
            }
        });
        return root;
    }
}