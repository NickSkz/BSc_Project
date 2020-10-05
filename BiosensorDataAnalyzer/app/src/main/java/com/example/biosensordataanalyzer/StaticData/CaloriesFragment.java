package com.example.biosensordataanalyzer.StaticData;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.biosensordataanalyzer.Bluetooth.BluetoothAPIUtils;
import com.example.biosensordataanalyzer.Connection.ConnectionActivity;
import com.example.biosensordataanalyzer.Constants.Consts;
import com.example.biosensordataanalyzer.Main.MainActivity;
import com.example.biosensordataanalyzer.R;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CaloriesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CaloriesFragment extends Fragment {

    private static final String TAG = "CaloriesFragment";

    private TextView caloriesView;

    public static boolean waitingForData;
    private int calories;

    private BarGraphSeries<DataPoint> series;
    private GraphView graph;
    private GridLabelRenderer gridLabelRenderer;

    ArrayList<Integer> distVals;
    DateFormat dateFormat;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CaloriesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CaloriesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CaloriesFragment newInstance(String param1, String param2) {
        CaloriesFragment fragment = new CaloriesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        dateFormat = new SimpleDateFormat("dd/MM", Locale.ENGLISH);
        distVals = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_calories, container, false);

        caloriesView = v.findViewById(R.id.calories_view);

        graph = v.findViewById(R.id.graphcal_view);
        graph.setTitle("Last 7 days");
        graph.setTitleColor(Color.rgb(0, 100, 0));


        gridLabelRenderer = graph.getGridLabelRenderer();
        gridLabelRenderer.setGridColor(Color.rgb(0, 100, 0));
        gridLabelRenderer.setHorizontalLabelsColor(Color.rgb(0, 100, 0));
        gridLabelRenderer.setVerticalLabelsColor(Color.rgb(0, 100, 0));
        gridLabelRenderer.setNumHorizontalLabels(7);
        gridLabelRenderer.setPadding(32);
        gridLabelRenderer.setHorizontalAxisTitle("Days from Today");
        gridLabelRenderer.setHorizontalAxisTitleColor(Color.rgb(100, 100, 200));
        gridLabelRenderer.setVerticalAxisTitleColor(Color.rgb(100, 100, 200));
        gridLabelRenderer.setVerticalAxisTitle("Calories");
        gridLabelRenderer.setLabelFormatter(new DefaultLabelFormatter(){
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if(isValueX){
                    return ""+(int)value;
                }

                return super.formatLabel(value, isValueX);
            }
        });


        Thread thr = new Thread(new Runnable() {
            @Override
            public void run() {
                setData();
            }
        });
        thr.start();




        // Inflate the layout for this fragment
        return v;
    }


    private void setData(){
        try{
            TrainingActivity.waitForDataLatch.await();
        }catch (Exception e){
            e.printStackTrace();
        }

        TrainingActivity trAc = (TrainingActivity) getActivity();

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                calories = trAc.getCalories();

                caloriesView.setText(String.valueOf(calories));
                Log.i(TAG, "Calories: " + caloriesView);
            }
        });

        prepareGraph();
        graph.addSeries(series);
        graph.refreshDrawableState();
    }


    public void prepareGraph(){
        TrainingActivity act = (TrainingActivity) getActivity();

        Calendar calendar = Calendar.getInstance();
        ArrayList<Date> dateVals = new ArrayList<>();

        DataPoint[] points = new DataPoint[7];


        int idx = 0;
        for (Map.Entry<String, String> entry : act.processedDays.entrySet()) {
            if(idx != 0)
                calendar.add(Calendar.DATE, -1);
            else
                calendar.add(Calendar.DATE, 0);

            String[] data = entry.getValue().split(";");
            distVals.add(Integer.parseInt(data[2]));
            dateVals.add(calendar.getTime());
            ++idx;
        }


        for(int i = 0; i < dateVals.size(); ++i){
            points[i] = new DataPoint(i, distVals.get(i));
        }

        series = new BarGraphSeries<>(points);
        series.setColor(Color.rgb(50, 100, 70));
    }

}