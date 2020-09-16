package com.example.charleshitchcock.personalbest;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;

public class WeeklyGraphActivity extends AppCompatActivity {

    final static String TAG = "WeeklyGraphActivity";
    SharedPreferences sp;
    List<BarEntry> barentries;
    CombinedChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_graph);

        sp = getSharedPreferences("personal_best", MODE_PRIVATE);

        Log.d("lol", "lol");

        Button backButton = (Button) findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        String[] befdays = new String[] {
                "Sun", "Mon", "Tues", "Wed", "Thurs", "Fri", "Sat"
        };

        int dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        List<String> tempal = Arrays.asList(Arrays.copyOfRange(befdays, dow, befdays.length));
        ArrayList<String> al = new ArrayList<>(tempal);
        for (int i = 0; i < dow; i++) {
            al.add(befdays[i]);
        }
        final String[] days = al.toArray(new String[7]);


        Log.d(TAG, "reached weekly graph activity");

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
        xAxis.setAxisMaximum(8f);
        xAxis.setSpaceMin(0.5f);
        xAxis.setSpaceMax(0.5f);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return days[(((int) value - 1) % days.length + days.length) % days.length];
            }
        });

        Log.d(TAG, "getting bar entries");

        GoogleSignInAccount gsia = GoogleSignIn.getLastSignedInAccount(this);

        barentries = new ArrayList<>();

        if (gsia != null && !MenuActivity.test) {
            // pull stuff from api
            Calendar cal = MenuActivity.fakecalendar != null ? MenuActivity.fakecalendar : Calendar.getInstance();
            long endTime = cal.getTimeInMillis();
            cal.add(Calendar.WEEK_OF_YEAR, -1);
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
            DocumentReference docRef = db.collection("users").document(gsia.getEmail()).collection("data").document("unintentional");
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            if (document.getData() != null) {
                                for (String s : document.getData().keySet()) {
                                    info.put(s + "u", (Long) document.getData().get(s));
                                }
                            }
                            db.collection("users").document(gsia.getEmail()).collection("data").document("intentional").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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
                                        for (int i = 0; i < 7; i++) {
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


            /*
            DataReadRequest readRequest =
                    new DataReadRequest.Builder()
                            // The data request can specify multiple data types to return, effectively
                            // combining multiple data queries into one call.
                            // In this example, it's very unlikely that the request is for several hundred
                            // datapoints each consisting of a few steps and a timestamp.  The more likely
                            // scenario is wanting to see how many steps were walked per day, for 7 days.
                            .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                            // Analogous to a "Group By" in SQL, defines how data should be aggregated.
                            // bucketByTime allows for a time span, whereas bucketBySession would allow
                            // bucketing by "sessions", which would need to be defined in code.
                            .bucketByTime(1, TimeUnit.DAYS)
                            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                            .build();
            Task<DataReadResponse> response = Fitness.getHistoryClient(this, gsia).readData(readRequest);
            response.addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                @Override
                public void onSuccess(DataReadResponse dataReadResponse) {
                    Log.i(TAG, "succeeded");
                    dataReadResponse.getBuckets();
                    int currentIndex = 0;
                    for (Bucket b : dataReadResponse.getBuckets()) {
                        for (DataSet d : b.getDataSets()) {
                            if (!d.getDataPoints().isEmpty()) {
                                for (Field field : d.getDataPoints().get(0).getDataType().getFields()) {
                                    long intentional = sp.getLong(("active_" + Integer.toString(currentIndex + 1)), 0);
                                    long unintent = d.getDataPoints().get(0).getValue(field).asInt() - intentional;
                                    barentries.add(new BarEntry(currentIndex + 1, new float[]{intentional, unintent}));
                                    currentIndex++;
                                }
                            }
                        }
                    }
                    for (int i = currentIndex; i < 7; i++) {
                        barentries.add(new BarEntry(i+1, new float[] {0 , 0} ));
                    }
                    drawGraph(barentries);
                }
            });
            */

        } else {
            // just use sharedprefs
            for (int i = 0; i < MenuActivity.getDayOfWeek(); i++) {
                long intentional = sp.getLong(("active_" + Integer.toString(i + 1)), 0);
                long unintent = sp.getLong((Integer.toString(i+1) +  "_total"), 0) - intentional;
                Log.d(TAG, "intentional: " + intentional);
                Log.d(TAG, "unintentional: " + unintent);
                barentries.add(new BarEntry(i+1, new float[] {intentional, unintent} ));
            }
            for (int i = MenuActivity.getDayOfWeek(); i < 7; i++) {
                barentries.add(new BarEntry(i+1, new float[] {0 , 0} ));
            }
            drawGraph(barentries);
        }




    }

    public void drawGraph(List<BarEntry> barentries) {
        BarDataSet barDataSet = new BarDataSet(barentries, "");
        barDataSet.setColors(new int[] {Color.rgb(0, 200, 0), Color.rgb(200, 0, 200)});
        barDataSet.setStackLabels(new String[] {"Intentional Steps", "Unintentional Steps"} );
        BarData barData = new BarData(barDataSet);


        List<Entry> entries = new ArrayList<Entry>();

        int goal = sp.getInt("goal", 5000);
        entries.add(new Entry(0, goal));
        entries.add(new Entry(8, goal));
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
