package com.example.mydairy.ui.gallery;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Gallery;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mydairy.R;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.example.mydairy.user_details;
import java.util.ArrayList;
import java.util.List;

import static android.widget.Toast.LENGTH_SHORT;

public class GalleryFragment extends Fragment {

    private GalleryViewModel galleryViewModel;

    private DatabaseReference databaseReference;
    private FirebaseAuth mauth;
    private int customercount=0;
    private  String currentUserId;
    private FirebaseUser firebaseUser;
    private ListView listView;
    private ArrayList<String> arrayList;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mauth = FirebaseAuth.getInstance();
        currentUserId = mauth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.keepSynced(true);

        arrayList = new ArrayList<>();

        galleryViewModel = ViewModelProviders.of(this).get(GalleryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_customers, container, false);
        listView = root.findViewById(R.id.listview);
        final TextView textView = root.findViewById(R.id.txtcustomerno);

        galleryViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        final TextView textView1 = root.findViewById(R.id.txttotal_no);


        databaseReference.child("user_details").child(currentUserId).child("clients").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    customercount = (int) dataSnapshot.getChildrenCount();
                    textView1.setText(Integer.toString(customercount));
                }
                else
                {
                    textView1.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        super.onStart();

        listDisplay();

        return root;

    }

    public void listDisplay()
    {
        arrayList.clear();
        databaseReference.child("user_details").child(currentUserId).child("clients").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               for(DataSnapshot snapshot: dataSnapshot.getChildren())
               {
                   String name = snapshot.child("client_name").getValue().toString().trim();
                   String id = snapshot.child("client_mobno").getValue().toString().trim();
                   arrayList.add(name+"\n"+id);
                  ArrayAdapter arrayAdapter = new ArrayAdapter(getActivity(),android.R.layout.simple_list_item_1,arrayList);
                  listView.setAdapter(arrayAdapter);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
