package com.example.mydairy.ui.send;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.mydairy.Login_Form;
import com.example.mydairy.R;
import com.example.mydairy.SharedPrefrenceConfig;

public class SendFragment extends Fragment {

    private SendViewModel sendViewModel;
    private SharedPrefrenceConfig sharedPrefrenceConfig;
    private Button buttonLogout;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sendViewModel = ViewModelProviders.of(this).get(SendViewModel.class);
        View root = inflater.inflate(R.layout.fragment_logout, container, false);
        sharedPrefrenceConfig = new SharedPrefrenceConfig(getContext());
        sharedPrefrenceConfig.writeLoginStatus(false);
        startActivity(new Intent(getActivity(), Login_Form.class));
        getActivity().finish();
        return root;
    }
}