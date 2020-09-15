package com.example.biosensordataanalyzer.StaticData;

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

import com.example.biosensordataanalyzer.Bluetooth.BluetoothAPIUtils;
import com.example.biosensordataanalyzer.Connection.ConnectionActivity;
import com.example.biosensordataanalyzer.Constants.Consts;
import com.example.biosensordataanalyzer.R;

public class StepsActivity extends AppCompatActivity {

    private static final String TAG = "StepsActivity";


    private Button refreshBtn;
    private TextView stepsView;

    public static boolean waitingForData;
    private int steps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steps);

        stepsView = findViewById(R.id.steps_view);

        refreshBtn = findViewById(R.id.refresh_btn);
        refreshBtn.setOnClickListener(view -> requestSteps());

        requestSteps();
    }


    private void requestSteps(){
        waitingForData = true;

        BluetoothGattCharacteristic writeChar = BluetoothAPIUtils.bluetoothGatt.getService(Consts.THE_SERVICE).getCharacteristic(Consts.THE_WRITE_CHAR);
        writeChar.setValue(Consts.getStepsData);
        BluetoothAPIUtils.bluetoothGatt.writeCharacteristic(writeChar);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(stepsReceiver, new IntentFilter("GetStepsData"));
    }

    @Override
    protected void onPause() {
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

            stepsView.setText(String.valueOf(steps));
            Log.i(TAG, "Steps: " + steps);

            waitingForData = false;
        }
    };

}