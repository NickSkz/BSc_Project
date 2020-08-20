package com.example.biosensordataanalyzer;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PressureActivity extends AppCompatActivity {

    private static final String TAG = "PressureActivity";

    //TextView to display stuff
    TextView systolicText, diastolicText;

    //pulse, oxygen
    int systolic, diastolic;

    int systolicSum, diastolicSum, counter;

    Button startMeasureButton, stopMeasureButton;

    boolean pressureMeasurement;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pressure);

        systolicText = (TextView) findViewById(R.id.systolic_view);
        diastolicText = (TextView) findViewById(R.id.diastolic_view);

        //On start button write characteristic to WRITE CHANNEL to get stuff from tha bracelet
        startMeasureButton = (Button) findViewById(R.id.start_pressure_btn);
        startMeasureButton.setOnClickListener(view -> {
            startMeasurement();
        });

        //On close write characteristic that stops live measure
        stopMeasureButton = (Button) findViewById(R.id.stop_pressure_btn);
        stopMeasureButton.setOnClickListener(view -> {
            stopMeasurement();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(pressureReceiver, new IntentFilter("GetBloodPressureData"));
    }

    @Override
    protected void onPause() {
        unregisterReceiver(pressureReceiver);
        super.onPause();
    }


    private void startMeasurement(){
        if(BluetoothAPIUtils.bluetoothGatt != null && !ConnectionActivity.isMeasuring){

            systolicSum = 0;
            diastolicSum = 0;
            counter = 0;

            pressureMeasurement = true;

            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.schedule(this::stopMeasurement, 20, TimeUnit.SECONDS);

            ConnectionActivity.isMeasuring = true;
            BluetoothGattCharacteristic writeChar = BluetoothAPIUtils.bluetoothGatt.getService(Consts.THE_SERVICE).getCharacteristic(Consts.THE_WRITE_CHAR);
            writeChar.setValue(Consts.openLiveDataStream);
            BluetoothAPIUtils.bluetoothGatt.writeCharacteristic(writeChar);
        }
    }


    private void stopMeasurement() {
        if(BluetoothAPIUtils.bluetoothGatt != null && ConnectionActivity.isMeasuring){
            BluetoothGattCharacteristic writeChar = BluetoothAPIUtils.bluetoothGatt.getService(Consts.THE_SERVICE).getCharacteristic(Consts.THE_WRITE_CHAR);
            writeChar.setValue(Consts.closeLiveDataStream);

            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.schedule(() -> { BluetoothAPIUtils.bluetoothGatt.writeCharacteristic(writeChar); }, 1, TimeUnit.SECONDS);

            ConnectionActivity.isMeasuring = false;
            pressureMeasurement = false;
        }
    }



    //Listen to incoming Pulse and Oxygen signals
    private BroadcastReceiver pressureReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            systolic = intent.getIntExtra(Consts.SYSTOLIC,-1);
            diastolic = intent.getIntExtra(Consts.DIASTOLIC,-1);

            if(ConnectionActivity.isMeasuring && systolic != 0 && diastolic != 0){
                systolicSum += systolic;
                diastolicSum += diastolic;
                counter += 1;

                systolicText.setText(String.valueOf(systolic) + " mmHg");
                diastolicText.setText(String.valueOf(diastolic) + " mmHg");

                Log.i(TAG, "Systolic Finale: " + String.valueOf(systolicSum / counter));
                Log.i(TAG, "Diastolic Finale: " + String.valueOf(diastolicSum / counter));

                if(pressureMeasurement) {
                    BluetoothGattCharacteristic writeChar = BluetoothAPIUtils.bluetoothGatt.getService(Consts.THE_SERVICE).getCharacteristic(Consts.THE_WRITE_CHAR);
                    writeChar.setValue(Consts.ackLiveDataStream);
                    BluetoothAPIUtils.bluetoothGatt.writeCharacteristic(writeChar);
                }
            }
            if(!ConnectionActivity.isMeasuring && counter != 0){
                systolicText.setText(String.valueOf(systolicSum / counter) + " mmHg");
                diastolicText.setText(String.valueOf(diastolicSum / counter) + " mmHg");
            }

            Log.i(TAG, "Systolic: " + String.valueOf(systolic));
            Log.i(TAG, "Diastolic: " + String.valueOf(diastolic));

        }
    };
}