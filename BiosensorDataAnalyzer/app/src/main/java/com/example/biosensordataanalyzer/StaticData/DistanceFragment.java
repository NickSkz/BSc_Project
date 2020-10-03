package com.example.biosensordataanalyzer.StaticData;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
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
import com.example.biosensordataanalyzer.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DistanceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DistanceFragment extends Fragment {

    private static final String TAG = "DistanceFragment";

    private TextView distanceView;

    public static boolean waitingForData;
    private int distance;

    private LineGraphSeries<DataPoint> series;
    private GraphView graph;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public DistanceFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DistanceFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DistanceFragment newInstance(String param1, String param2) {
        DistanceFragment fragment = new DistanceFragment();
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
    }

    //RESET PO 24

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_distance, container, false);

        distanceView = v.findViewById(R.id.distance_view);

        graph = v.findViewById(R.id.graph_view);
        series = new LineGraphSeries<>();

        Thread thr = new Thread(new Runnable() {
            @Override
            public void run() {
                setData();
            }
        });
        thr.start();

        prepareGraph();

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
                distance = trAc.getDistance();

                distanceView.setText(String.valueOf(distance));
                Log.i(TAG, "Distance: " + distanceView);
            }
        });

        //prepareGraph();
    }


    private void prepareGraph(){

        TrainingActivity act = (TrainingActivity) getActivity();
        int idx = 0;

        for (Map.Entry<String, Integer> entry : act.processedDays.entrySet()){
            series.appendData(new DataPoint(idx, entry.getValue()), true, 100);
            ++idx;
        }

        graph.addSeries(series);
    }


}