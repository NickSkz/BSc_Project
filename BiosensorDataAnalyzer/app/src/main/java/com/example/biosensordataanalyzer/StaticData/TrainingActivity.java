package com.example.biosensordataanalyzer.StaticData;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.example.biosensordataanalyzer.Bluetooth.BluetoothAPIUtils;
import com.example.biosensordataanalyzer.Connection.ConnectionActivity;
import com.example.biosensordataanalyzer.Constants.Consts;
import com.example.biosensordataanalyzer.R;
import com.example.biosensordataanalyzer.User.UserDataPageAdapter;
import com.google.android.material.tabs.TabLayout;

import java.util.concurrent.CountDownLatch;

public class TrainingActivity extends AppCompatActivity {

    private static final String TAG = "TrainingActivity";

    public static boolean waitingForData;
    public static CountDownLatch waitForDataLatch;

    int steps, distance, calories;

    Button refreshBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);

        refreshBtn = findViewById(R.id.refresh_trainbutton);
        refreshBtn.setOnClickListener(view -> requestSteps());

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

    private BroadcastReceiver stepsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            /*
             * Get values as IntExtra
             */
            steps = intent.getIntExtra(Consts.STEPS,-1);
            distance = intent.getIntExtra(Consts.DISTANCE,-1);
            calories = intent.getIntExtra(Consts.CALORIES,-1);

            waitingForData = false;
            waitForDataLatch.countDown();
        }
    };

}