package com.example.charleshitchcock.personalbest.message;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.example.charleshitchcock.personalbest.MenuActivity;
import com.example.charleshitchcock.personalbest.R;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

public class FriendDataActivity extends AppCompatActivity {

    public static final String TAG = "FriendDataActivity";
    CombinedChart chart;
    List<BarEntry> barentries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_data);

        String receiver = getIntent().getStringExtra("other");

        chart = (CombinedChart) findViewById(R.id.chart);
        chart.getDescription().setText("");
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);
        chart.setDrawOrder(new CombinedChart.DrawOrder[]{
                CombinedChart.DrawOrder.BAR,  CombinedChart.DrawOrder.LINE
        });

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisMinimum(0);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(29f);
        xAxis.setSpaceMin(0.5f);
        xAxis.setSpaceMax(0.5f);
        xAxis.setGranularity(1f);
        /*
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return days[(((int) value - 1) % days.length + days.length) % days.length];
            }
        }); */

        Log.d(TAG, "getting bar entries");

        GoogleSignInAccount gsia = GoogleSignIn.getLastSignedInAccount(this);

        barentries = new ArrayList<>();

        if (gsia != null && !MenuActivity.test) {
            // pull stuff from api
            Calendar cal = MenuActivity.fakecalendar != null ? MenuActivity.fakecalendar : Calendar.getInstance();
            long endTime = cal.getTimeInMillis();
            cal.add(Calendar.WEEK_OF_YEAR, -4);
            cal.add(Calendar.DAY_OF_WEEK, 1);
//            cal.set(Calendar.HOUR_OF_DAY, 0);
//            cal.clear(Calendar.MINUTE);
//            cal.clear(Calendar.HOUR);
//            cal.clear(Calendar.MILLISECOND);
//            cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
            long startTime = cal.getTimeInMillis();

            DateFormat dateFormat = DateFormat.getDateInstance();
            Log.i(TAG, "Range Start: " + dateFormat.format(startTime));
            Log.i(TAG, "Range End: " + dateFormat.format(endTime));
            Log.i(TAG, "ID: " + gsia.getId());

            Map<String, Long> info = new HashMap<>();

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("users").document(receiver).collection("data").document("unintentional");
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            for (String s : document.getData().keySet()) {
                                info.put(s + "u", (Long) document.getData().get(s));
                            }
                            db.collection("users").document(receiver).collection("data").document("intentional").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@android.support.annotation.NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if (document.getData() != null) {
                                            for (String s : document.getData().keySet()) {
                                                Log.i(TAG, s + "i: " + (Long) document.getData().get(s));
                                                info.put(s + "i", (Long) document.getData().get(s));
                                            }
                                        }
                                        for (int i = 0; i < 28; i++) {
                                            String key = cal.get(Calendar.MONTH) + "" + cal.get(Calendar.DATE) + "" + cal.get(Calendar.YEAR);
                                            cal.add(Calendar.DAY_OF_YEAR, 1);
                                            long unintent = info.containsKey(key + "u") ? info.get(key + "u") : 0;
                                            long intent = info.containsKey(key + "i") ? info.get(key + "i") : 0;
                                            Log.i(TAG, key);
                                            barentries.add(new BarEntry(i + 1, new float[]{intent, unintent}));
                                        }
                                        drawGraph(barentries);
                                    }
                                }
                            });
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });

        }
    }

    public void drawGraph(List<BarEntry> barentries) {
        BarDataSet barDataSet = new BarDataSet(barentries, "");
        barDataSet.setColors(new int[] {Color.rgb(0, 200, 0), Color.rgb(200, 0, 200)});
        barDataSet.setStackLabels(new String[] {"Intentional Steps", "Unintentional Steps"} );
        BarData barData = new BarData(barDataSet);

        SharedPreferences sp = getSharedPreferences("personal_best", MODE_PRIVATE);

        List<Entry> entries = new ArrayList<Entry>();

        int goal = sp.getInt("goal", 5000);
        entries.add(new Entry(0, goal));
        entries.add(new Entry(29, goal));
        LineDataSet lds = new LineDataSet(entries, "Goal");
        lds.setAxisDependency(YAxis.AxisDependency.LEFT);
        lds.setDrawValues(true);

        LineData lineData = new LineData(lds);

        CombinedData cd = new CombinedData();
        cd.setData(barData);
        cd.setData(lineData);

        chart.setData(cd);
        chart.invalidate();

    }
}
