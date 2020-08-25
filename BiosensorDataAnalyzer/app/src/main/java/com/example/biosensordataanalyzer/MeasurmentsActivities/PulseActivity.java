package com.example.biosensordataanalyzer.MeasurmentsActivities;

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
import android.widget.Toast;

import com.example.biosensordataanalyzer.Bluetooth.BluetoothAPIUtils;
import com.example.biosensordataanalyzer.Connection.ConnectionActivity;
import com.example.biosensordataanalyzer.Constants.Consts;
import com.example.biosensordataanalyzer.R;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


// Activity that menages pulse measure
public class PulseActivity extends AppCompatActivity {

    private static final String TAG = "PulseActivity";

    // Declare TextViews
    private TextView pulseText, oxygenText;

    // Current Pulse and Oxygen Values
    private int pulse, oxygen;

    // Pulse and Oxygen final measurement (average after 20sec interval) + counter used to calculate average
    private int pulseSum, oxygenSum, counter;

    // Declare buttons
    private Button startMeasureButton, stopMeasureButton;

    // Flag that tells whether pulse measure is performed
    private boolean pulseMeasurement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pulse);

        /*
         * Initialize graphical components
         */
        pulseText = (TextView) findViewById(R.id.bpm_view);
        oxygenText = (TextView) findViewById(R.id.oxygen_view);

        /*
         * Assign methods to start/stop button
         */
        startMeasureButton = (Button) findViewById(R.id.start_measurement_btn);
        startMeasureButton.setOnClickListener(view -> {
            startMeasurement();
        });

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


    /*
     * Start measure method
     * If the measure is off - begin
     * Zero all variables, set flag pulseMeasurement to true
     * Stop the measure with 20sec delay (ExecutorService)
     * Set flag that indicates measure
     * Write proper characteristic to WRITE CHANNEL, to get stuff from the bracelet
     */
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


    /*
     * Stop measurement method
     * If measure is on, prepare characteristic and write it with 1 sec delay, so every flag in the system has time to properly set
     * Make a toast, set flags to false
     */
    private void stopMeasurement() {
        if(BluetoothAPIUtils.bluetoothGatt != null && ConnectionActivity.isMeasuring){
            BluetoothGattCharacteristic writeChar = BluetoothAPIUtils.bluetoothGatt.getService(Consts.THE_SERVICE).getCharacteristic(Consts.THE_WRITE_CHAR);
            writeChar.setValue(Consts.closeLiveDataStream);

            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.schedule(() -> { BluetoothAPIUtils.bluetoothGatt.writeCharacteristic(writeChar); }, 1, TimeUnit.SECONDS);

            this.runOnUiThread(() -> {
                Toast.makeText(getApplicationContext(), "Measure finished!", Toast.LENGTH_LONG).show();
            });

            ConnectionActivity.isMeasuring = false;
            pulseMeasurement = false;
        }
    }



    // Listen to incoming Pulse and Oxygen signals
    private BroadcastReceiver pulseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            /*
             * Get values as IntExtra
             */
            pulse = intent.getIntExtra(Consts.PULSE,-1);
            oxygen = intent.getIntExtra(Consts.OXYGEN,-1);

            // If we get measurements != 0 and measurement is on, calculate current average and print current text
            if(ConnectionActivity.isMeasuring && pulse != 0 && oxygen != 0){
                pulseSum += pulse;
                oxygenSum += oxygen;
                counter += 1;

                pulseText.setText(String.valueOf(pulse) + " BPM");
                oxygenText.setText(String.valueOf(oxygen) + "%");

                Log.i(TAG, "Pulse Finale: " + String.valueOf(pulseSum / counter));
                Log.i(TAG, "Oxygen Finale: " + String.valueOf(oxygenSum / counter));

                // If the measurement is still going, send ack signal for next data (write characteristic)
                if(pulseMeasurement) {
                    BluetoothGattCharacteristic writeChar = BluetoothAPIUtils.bluetoothGatt.getService(Consts.THE_SERVICE).getCharacteristic(Consts.THE_WRITE_CHAR);
                    writeChar.setValue(Consts.ackLiveDataStream);
                    BluetoothAPIUtils.bluetoothGatt.writeCharacteristic(writeChar);
                }
            }
            // If the measure is over print average measurement on the screen
            if(!ConnectionActivity.isMeasuring && counter != 0){
                pulseText.setText(String.valueOf(pulseSum / counter) + " BPM");
                oxygenText.setText(String.valueOf(oxygenSum / counter) + "%");
            }

            Log.i(TAG, "Pulse: " + String.valueOf(pulse));
            Log.i(TAG, "Oxygen: " + String.valueOf(oxygen));


        }
    };
}