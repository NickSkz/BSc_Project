package com.example.biosensordataanalyzer.MeasurmentsActivities;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.biosensordataanalyzer.Bluetooth.BluetoothAPIUtils;
import com.example.biosensordataanalyzer.Connection.ConnectionActivity;
import com.example.biosensordataanalyzer.Constants.Consts;
import com.example.biosensordataanalyzer.Main.MainActivity;
import com.example.biosensordataanalyzer.R;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PressureActivity extends AppCompatActivity {

    private static final String TAG = "PressureActivity";

    // Declare TextViews
    TextView systolicText, diastolicText;
    TextView readyMeasureText;


    // Current Systolic and Diastolic Values
    int systolic, diastolic;

    // Systolic and Diastolic final measurement (average after 20sec interval) + counter used to calculate average
    int systolicSum, diastolicSum, counter;

    // Declare buttons
    Button startMeasureButton, stopMeasureButton;

    // Flag that tells whether blood pressure measure is performed
    boolean pressureMeasurement;

    // Interpreter/ByteBuffer for running tha network
    Interpreter interpreter;
    ByteBuffer tfLiteBuffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pressure);

        /*
         * Initialize graphical components
         */
        systolicText = (TextView) findViewById(R.id.systolic_view);
        diastolicText = (TextView) findViewById(R.id.diastolic_view);
        readyMeasureText = findViewById(R.id.ready_view);

        /*
         * Assign methods to start/stop button
         */
        startMeasureButton = (Button) findViewById(R.id.start_pressure_btn);
        startMeasureButton.setOnClickListener(view -> {
            startMeasurement();
        });

        stopMeasureButton = (Button) findViewById(R.id.stop_pressure_btn);
        stopMeasureButton.setOnClickListener(view -> {
            stopMeasurement();
        });

        //Initialize TfLite Interpreter
        initializeTfLite();
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


    /*
     * Start measure method
     * If the measure is off - begin
     * Zero all variables, set flag pressureMeasurement to true
     * Stop the measure with 20sec delay (ExecutorService)
     * Set flag that indicates measure
     * Write proper characteristic to WRITE CHANNEL, to get stuff from the bracelet
     */
    private void startMeasurement(){
        if(BluetoothAPIUtils.bluetoothGatt != null && !ConnectionActivity.isMeasuring){

            systolicSum = 0;
            diastolicSum = 0;
            counter = 0;

            pressureMeasurement = true;
            readyMeasureText.setText("Measure in progress...");

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
            executorService.schedule(() -> {
                BluetoothAPIUtils.bluetoothGatt.writeCharacteristic(writeChar);
                }, 1, TimeUnit.SECONDS);

            this.runOnUiThread(() -> {
                Toast.makeText(getApplicationContext(), "Measure finished!", Toast.LENGTH_LONG).show();
                interpreter.run(MainActivity.currentUser.inputPressureHDArr, MainActivity.currentUser.outputPressureHDArr);
                Log.i(TAG, String.valueOf(MainActivity.currentUser.outputPressureHDArr[0][0]));
                showPopUp();
            });

            ConnectionActivity.isMeasuring = false;
            pressureMeasurement = false;
        }
    }



    // Listen to incoming Systolic and Diastolic signals
    private BroadcastReceiver pressureReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            /*
             * Get values as IntExtra
             */
            systolic = intent.getIntExtra(Consts.SYSTOLIC,-1);
            diastolic = intent.getIntExtra(Consts.DIASTOLIC,-1);

            // If we get measurements != 0 and measurement is on, calculate current average and print current text
            if(ConnectionActivity.isMeasuring && systolic != 0 && diastolic != 0){
                systolicSum += systolic;
                diastolicSum += diastolic;
                counter += 1;

                systolicText.setText(String.valueOf(systolic) + " mmHg");
                diastolicText.setText(String.valueOf(diastolic) + " mmHg");

                Log.i(TAG, "Systolic Finale: " + String.valueOf(systolicSum / counter));
                Log.i(TAG, "Diastolic Finale: " + String.valueOf(diastolicSum / counter));

                MainActivity.currentUser.inputPressureHDArr[0][4] = systolicSum / counter;
                MainActivity.currentUser.inputPressureHDArr[0][5] = systolicSum / counter;

                // If the measurement is still going, send ack signal for next data (write characteristic)
                if(pressureMeasurement) {
                    BluetoothGattCharacteristic writeChar = BluetoothAPIUtils.bluetoothGatt.getService(Consts.THE_SERVICE).getCharacteristic(Consts.THE_WRITE_CHAR);
                    writeChar.setValue(Consts.ackLiveDataStream);
                    BluetoothAPIUtils.bluetoothGatt.writeCharacteristic(writeChar);
                }
            }
            // If the measure is over print average measurement on the screen
            if(!ConnectionActivity.isMeasuring && counter != 0){
                systolicText.setText(String.valueOf(systolicSum / counter) + " mmHg");
                diastolicText.setText(String.valueOf(diastolicSum / counter) + " mmHg");
                readyMeasureText.setText("Ready for measure!");
            }

            Log.i(TAG, "Systolic: " + String.valueOf(systolic));
            Log.i(TAG, "Diastolic: " + String.valueOf(diastolic));

        }
    };


    private void initializeTfLite(){
        try{
            AssetFileDescriptor assetFileDescriptor = getAssets().openFd("heart_disease_model.tflite");
            long startOff = assetFileDescriptor.getStartOffset();
            long length = assetFileDescriptor.getDeclaredLength();

            FileInputStream fileInputStream = new FileInputStream(assetFileDescriptor.getFileDescriptor());
            FileChannel fileChannel = fileInputStream.getChannel();
            tfLiteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOff, length);

        } catch (IOException e) {
            e.printStackTrace();
        }

        interpreter = new Interpreter(tfLiteBuffer);
    }

    private TextView popUpSys, popUpDia, popUpHDText;

    private void showPopUp(){
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        try {
            View popView = inflater.inflate(R.layout.pressure_popup, null);
            PopupWindow popWindow = new PopupWindow(popView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

            popWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            popWindow.setElevation(20);
            popWindow.showAtLocation(readyMeasureText, Gravity.CENTER, 0, 0);


            popUpSys = popView.findViewById(R.id.pop_sys_view);
            popUpSys.setText(String.valueOf(systolicSum / counter) + " mmHg");

            popUpDia = popView.findViewById(R.id.pop_dia_view);
            popUpDia.setText(String.valueOf(diastolicSum / counter) + " mmHg");

            popUpHDText = popView.findViewById(R.id.pop_hd_text);
            popUpHDText.setText(String.format(Locale.ENGLISH, "There is %.2f", MainActivity.currentUser.outputPressureHDArr[0][0] * 100) + "% chance of you having a heart disease");


            popView.setOnTouchListener((v, event) -> {
                popWindow.dismiss();
                return true;
            });

        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }
}