package com.example.mydairy.ui.tools;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.mydairy.Billing;
import com.example.mydairy.DatePickerClass;
import com.example.mydairy.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class ToolsFragment extends Fragment { //implements DatePickerDialog.OnDateSetListener

    private ToolsViewModel toolsViewModel;
    private TextView liter1,amount,monthyear,datefrom,dateto;
    private String datefrom1,dateto1;
    private Button print;

    String outputPattern = "dd-MMM-yyyy";

    Double milk_sum,amt_sum;

    List<String> billing = new ArrayList<>();
    List<Double> totalLiter = new ArrayList<>();
    List<Double> totalAmount = new ArrayList<>();


    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase firebaseDatabase;

    private String dateselected1,empty = null;

    int x=0;

    final Calendar myCalendar = Calendar.getInstance();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        toolsViewModel = ViewModelProviders.of(this).get(ToolsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_monthlyreport, container, false);

        liter1 = (TextView) root.findViewById(R.id.txtliter);
        amount = (TextView) root.findViewById(R.id.txtamount);

        datefrom = (TextView)root.findViewById(R.id.txtdatefrom);

        datefrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getActivity(), date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                x=1;
            }
        });

        dateto = (TextView) root.findViewById(R.id.txtdateto);
        dateto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getActivity(), date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                x=2;
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseUser = firebaseAuth.getCurrentUser();

        print = (Button) root.findViewById(R.id.btnprint);

        print.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                if(datefrom.getText().toString().isEmpty() || datefrom.getText().toString().equals("Date")){
                    Toast.makeText(getActivity(), "Please Select Date", Toast.LENGTH_SHORT).show();
                }
                else if(dateto.getText().toString().isEmpty() || dateto.getText().toString().equals("Date")){
                    Toast.makeText(getActivity(), "PLease Select Date", Toast.LENGTH_SHORT).show();
                }
                else {
                    generatePdf();
                }
            }
        });
        return root;
    }


    public void monthly()
    {
        billing.clear();
        totalLiter.clear();
        databaseReference.child("user_details").child(firebaseAuth.getCurrentUser().getUid()).child("clients").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot clients :dataSnapshot.getChildren()){
                    databaseReference.child("user_details").child(firebaseAuth.getCurrentUser().getUid()).child("clients").child(clients.getKey())
                            .child("transactions").orderByChild("date").startAt(datefrom1).endAt(dateto1)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for(DataSnapshot snap:dataSnapshot.getChildren()){
                                        String dataString = "    "+snap.child("date").getValue().toString().trim()+"            "+snap.child("Shift").getValue().toString().trim()+"         "+snap.child("Liter").getValue().toString().trim()+"            "+snap.child("Amount").getValue().toString().trim();
                                        billing.add(dataString);
                                        Double total_value = Double.parseDouble(snap.child("Liter").getValue().toString().trim());
                                        totalLiter.add(total_value);
                                        Double amt_value = Double.parseDouble(snap.child("Amount").getValue().toString().trim());
                                        totalAmount.add(amt_value);
                                    }
                                    sum();
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sum()
    {
        milk_sum = 0.0;
        amt_sum = 0.0;
        for (int i=0;i<totalLiter.size();i++)
        {
            milk_sum = milk_sum + totalLiter.get(i);
            amt_sum = amt_sum + totalAmount.get(i);
        }
        amt_sum = Double.parseDouble(new DecimalFormat("####.##").format(amt_sum));
        amount.setText(""+amt_sum);
        milk_sum = Double.parseDouble(new DecimalFormat("####.##").format(milk_sum));
        liter1.setText(""+milk_sum);
    }


    final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yy");
            dateselected1 = dateFormatter.format(myCalendar.getTime());

            if(x==1)
            {
                datefrom1 = dateselected1;
                datefrom.setText(dateselected1);
            }
            else if(x==2)
            {
                dateto1 = dateselected1;
                dateto.setText(dateselected1);
                monthly();
            }
        }

    };

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void generatePdf() {
        PdfDocument myPdfDocument = new PdfDocument();
        PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(300, 600, 1).create();
        PdfDocument.Page myPage = myPdfDocument.startPage(myPageInfo);

        Paint myPaint = new Paint();

        String[] myString = billing.toArray(new String[billing.size()]);

        int x = 10, y = 25;
        myPage.getCanvas().drawText("                    *********** Your Milk Report *********** \n", x, y, myPaint);
        y += myPaint.descent() - myPaint.ascent();

        myPage.getCanvas().drawText("Your Milk Report From Date : "+datefrom1+" to "+dateto1+"\n\n", x, y, myPaint);
        y += myPaint.descent() - myPaint.ascent();

        myPage.getCanvas().drawText("\n\n", x, y, myPaint);
        y += myPaint.descent() - myPaint.ascent();

        myPage.getCanvas().drawText("      DATE               SHIFT        LITER      AMOUNT     \n", x, y, myPaint);
        y += myPaint.descent() - myPaint.ascent();

        for (String sele : myString) {
            myPage.getCanvas().drawText(sele + "\n", x, y, myPaint);
            y += myPaint.descent() - myPaint.ascent();
        }

        myPage.getCanvas().drawText(" ", x, y, myPaint);
        y += myPaint.descent() - myPaint.ascent();

        myPage.getCanvas().drawText("Total Milk - "+liter1.getText().toString().trim()+" lit", x, y, myPaint);
        y += myPaint.descent() - myPaint.ascent();

        myPage.getCanvas().drawText("Total Amount - "+amount.getText().toString().trim()+"rs.", x, y, myPaint);
        y += myPaint.descent() - myPaint.ascent();

        myPage.getCanvas().drawText(" ", x, y, myPaint);
        y += myPaint.descent() - myPaint.ascent();

        myPage.getCanvas().drawText("Thanks for Connecting with us.....", x, y, myPaint);
        y += myPaint.descent() - myPaint.ascent();

        myPage.getCanvas().drawText("Mydairy", x, y, myPaint);
        y += myPaint.descent() - myPaint.ascent();

        myPdfDocument.finishPage(myPage);

        String myFilePath = Environment.getExternalStorageDirectory().getPath() + "/Monthly_Report.pdf";
        Toast.makeText(getActivity(),"Downloded File Path" + myFilePath,Toast.LENGTH_SHORT).show();
        File myFile = new File(myFilePath);
        try {
            myPdfDocument.writeTo(new FileOutputStream(myFile));
        }
        catch (Exception e) {
            Toast.makeText(getActivity(),"Error" + "/Monthly_Report.pdf",Toast.LENGTH_SHORT).show();
        }
        myPdfDocument.close();
    }

}