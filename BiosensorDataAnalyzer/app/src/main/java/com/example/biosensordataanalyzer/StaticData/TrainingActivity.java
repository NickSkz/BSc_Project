package com.example.biosensordataanalyzer.StaticData;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.example.biosensordataanalyzer.Bluetooth.BluetoothAPIUtils;
import com.example.biosensordataanalyzer.Connection.ConnectionActivity;
import com.example.biosensordataanalyzer.Constants.Consts;
import com.example.biosensordataanalyzer.Main.MainActivity;
import com.example.biosensordataanalyzer.R;
import com.example.biosensordataanalyzer.User.CurrentUser;
import com.example.biosensordataanalyzer.User.UserDataPageAdapter;
import com.google.android.material.tabs.TabLayout;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class TrainingActivity extends AppCompatActivity {

    private static final String TAG = "TrainingActivity";

    public static boolean waitingForData;
    public static CountDownLatch waitForDataLatch;

    Map<String, Integer> processedDays;
    Map<String, Integer> realDays;

    int steps, distance, calories;

    Button refreshBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);

        waitForDataLatch = new CountDownLatch(1);

        refreshBtn = findViewById(R.id.refresh_trainbutton);
        refreshBtn.setOnClickListener(view -> requestSteps());

        realDays = new LinkedHashMap<>();
        processedDays = new LinkedHashMap<>();

        TabLayout tabLayout = findViewById(R.id.tab_trainlayout);

        final ViewPager viewPager = findViewById(R.id.view_trainpager);
        TrainingPageAdapter pagerAdapter = new TrainingPageAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        requestSteps();

        try {
            transferData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public int getSteps(){
        return steps;
    }
    public int getDistance(){
        return distance;
    }
    public int getCalories(){
        return calories;
    }

    private void requestSteps(){
        if(!ConnectionActivity.serviceRunning)
            return;

        waitForDataLatch = new CountDownLatch(1);
        waitingForData = true;

        BluetoothGattCharacteristic writeChar = BluetoothAPIUtils.bluetoothGatt.getService(Consts.THE_SERVICE).getCharacteristic(Consts.THE_WRITE_CHAR);
        writeChar.setValue(Consts.getStaticData);
        BluetoothAPIUtils.bluetoothGatt.writeCharacteristic(writeChar);
    }

    private void prepareDays(int steps){
        processedDays.clear();

        Calendar calendar = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);


        calendar.add(Calendar.DATE, 0);
        processedDays.put(dateFormat.format(calendar.getTime()), steps);
        calendar.add(Calendar.DATE, -1);
        processedDays.put(dateFormat.format(calendar.getTime()), 0);
        calendar.add(Calendar.DATE, -1);
        processedDays.put(dateFormat.format(calendar.getTime()), 0);
        calendar.add(Calendar.DATE, -1);
        processedDays.put(dateFormat.format(calendar.getTime()), 0);
        calendar.add(Calendar.DATE, -1);
        processedDays.put(dateFormat.format(calendar.getTime()), 0);
        calendar.add(Calendar.DATE, -1);
        processedDays.put(dateFormat.format(calendar.getTime()), 0);
        calendar.add(Calendar.DATE, -1);
        processedDays.put(dateFormat.format(calendar.getTime()), 0);

    }

    //Ad saving on quit !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

    public void save(Context context) throws IOException {
        FileOutputStream fileOutputStream = context.openFileOutput(Consts.lastDaysFileName, Context.MODE_PRIVATE);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(processedDays);
        objectOutputStream.close();
        fileOutputStream.close();
    }


    public void load(Context context) throws IOException, ClassNotFoundException {
        FileInputStream fileInputStream = context.openFileInput(Consts.lastDaysFileName);
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
        realDays = (LinkedHashMap) objectInputStream.readObject();
        objectInputStream.close();
        fileInputStream.close();
    }

    private BarGraphSeries<DataPoint> series;
    private GraphView graph;
    private GridLabelRenderer gridLabelRenderer;

    ArrayList<Integer> stepVals;


    private void transferData() throws IOException {

        prepareDays(0);

        realDays.clear();

        try {
            load(getApplicationContext());
        } catch (Exception e){
            e.printStackTrace();
        }

        for (Map.Entry<String, Integer> entry : realDays.entrySet()){
            Log.i(TAG, entry.getKey() + " : " + String.valueOf(entry.getValue()));
        }


        for (Map.Entry<String, Integer> entry : processedDays.entrySet()){
            if(realDays.containsKey(entry.getKey())){
                processedDays.put(entry.getKey(), entry.getValue() + realDays.get(entry.getKey()));
            }
        }

        for (Map.Entry<String, Integer> entry : processedDays.entrySet()){
            Log.i(TAG, entry.getKey() + " : " + String.valueOf(entry.getValue()));
        }

        save(getApplicationContext());
    }


    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(stepsReceiver, new IntentFilter("GetTrainingData"));
    }

    @Override
    public void onPause() {
        unregisterReceiver(stepsReceiver);
        super.onPause();
    }

    @Override
    public void onStop() {
        try {
            save(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.onStop();
    }

    private BroadcastReceiver stepsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            /*
             * Get values as IntExtra
             */
            steps = intent.getIntExtra(Consts.STEPS,-1);
            distance = intent.getIntExtra(Consts.DISTANCE,-1);
            calories = intent.getIntExtra(Consts.CALORIES,-1);

            prepareDays(distance);

            waitingForData = false;
            waitForDataLatch.countDown();
        }
    };

}