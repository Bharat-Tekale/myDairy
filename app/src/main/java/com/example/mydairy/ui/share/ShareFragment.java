package com.example.mydairy.ui.share;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.mydairy.R;

public class ShareFragment extends Fragment {

    private ShareViewModel shareViewModel;
    private TextView message;
    private String usermail,email="dairydaily7@gmail.com";
    private Button submit;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        shareViewModel =
                ViewModelProviders.of(this).get(ShareViewModel.class);
        View root = inflater.inflate(R.layout.fragment_help, container, false);


        message = (TextView) root.findViewById(R.id.txtfeedback);
        submit = (Button) root.findViewById(R.id.btnsubmit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(message.getText().toString().trim().isEmpty()){
                    message.setError("Please Write Something");
                }
                else {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("message/rfc822");
                    i.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
                    i.putExtra(Intent.EXTRA_SUBJECT, "Client Feedback of email");
                    i.putExtra(Intent.EXTRA_TEXT, message.getText().toString());
                    try {
                        startActivity(Intent.createChooser(i, "Send mail..."));
                    }
                    catch (android.content.ActivityNotFoundException ex) {

                    }
                }
            }
        });
        return root;
    }
}