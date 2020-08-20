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

public class PulseActivity extends AppCompatActivity {

    private static final String TAG = "PulseActivity";

    //TextView to display stuff
    TextView pulseText, oxygenText;

    //pulse, oxygen
    int pulse, oxygen;

    int pulseSum, oxygenSum, counter;

    Button startMeasureButton, stopMeasureButton;

    boolean pulseMeasurement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pulse);

        pulseText = (TextView) findViewById(R.id.bpm_view);
        oxygenText = (TextView) findViewById(R.id.oxygen_view);

        //On start button write characteristic to WRITE CHANNEL to get stuff from tha bracelet
        startMeasureButton = (Button) findViewById(R.id.start_measurement_btn);
        startMeasureButton.setOnClickListener(view -> {
            startMeasurement();
        });

        //On close write characteristic that stops live measure
        stopMeasureButton = (Button) findViewById(R.id.stop_measurement_btn);
        stopMeasureButton.setOnClickListener(view -> {
            stopMeasurement();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(pulseReceiver, new IntentFilter("GetPulseData"));
    }

    @Override
    protected void onPause() {
        unregisterReceiver(pulseReceiver);
        super.onPause();
    }


    private void startMeasurement(){
        if(BluetoothAPIUtils.bluetoothGatt != null && !ConnectionActivity.isMeasuring){

            pulseSum = 0;
            oxygenSum = 0;
            counter = 0;

            pulseMeasurement = true;

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
            pulseMeasurement = false;
        }
    }



    //Listen to incoming Pulse and Oxygen signals
    private BroadcastReceiver pulseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            pulse = intent.getIntExtra(Consts.PULSE,-1);
            oxygen = intent.getIntExtra(Consts.OXYGEN,-1);

            if(ConnectionActivity.isMeasuring && pulse != 0 && oxygen != 0){
                pulseSum += pulse;
                oxygenSum += oxygen;
                counter += 1;

                pulseText.setText(String.valueOf(pulse) + " BPM");
                oxygenText.setText(String.valueOf(oxygen) + "%");

                Log.i(TAG, "Pulse Finale: " + String.valueOf(pulseSum / counter));
                Log.i(TAG, "Oxygen Finale: " + String.valueOf(oxygenSum / counter));

                if(pulseMeasurement) {
                    BluetoothGattCharacteristic writeChar = BluetoothAPIUtils.bluetoothGatt.getService(Consts.THE_SERVICE).getCharacteristic(Consts.THE_WRITE_CHAR);
                    writeChar.setValue(Consts.ackLiveDataStream);
                    BluetoothAPIUtils.bluetoothGatt.writeCharacteristic(writeChar);
                }
            }
            if(!ConnectionActivity.isMeasuring && counter != 0){
                pulseText.setText(String.valueOf(pulseSum / counter) + " BPM");
                oxygenText.setText(String.valueOf(oxygenSum / counter) + "%");
            }

            Log.i(TAG, "Pulse: " + String.valueOf(pulse));
            Log.i(TAG, "Oxygen: " + String.valueOf(oxygen));


        }
    };
}