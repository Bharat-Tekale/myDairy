package com.example.mydairy.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.mydairy.AddClients;
import com.example.mydairy.AddMilk;
import com.example.mydairy.Billing;
import com.example.mydairy.Collection_Report;
import com.example.mydairy.R;
import com.example.mydairy.ClientDetails;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    Button addmilk,addclient,collection,clientdetails,billing;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        addmilk=root.findViewById(R.id.dashaddmilk);
        addmilk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent intent=new Intent(HomeFragment.this.getContext(), AddMilk.class);
               startActivity(intent);
            }
        });
        collection=root.findViewById(R.id.dashcollection);
        collection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(HomeFragment.this.getContext(), Collection_Report.class);
                startActivity(intent);
            }
        });
        billing=root.findViewById(R.id.dashbilling);
        billing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(HomeFragment.this.getContext(), Billing.class);
                startActivity(intent);
            }
        });

        clientdetails=root.findViewById(R.id.dashclientdetails);
        clientdetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(HomeFragment.this.getContext(), ClientDetails.class);
                startActivity(intent);
            }
        });

        addclient=root.findViewById(R.id.dashaddclient);
        addclient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(HomeFragment.this.getContext(), AddClients.class);
                startActivity(intent);
            }
        });

        return root;
    }

}