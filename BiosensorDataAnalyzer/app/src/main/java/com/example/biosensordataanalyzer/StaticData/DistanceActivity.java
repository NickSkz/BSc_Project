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
import com.example.biosensordataanalyzer.Constants.Consts;
import com.example.biosensordataanalyzer.R;

public class DistanceActivity extends AppCompatActivity {

    private static final String TAG = "DistanceActivity";

    private Button refreshBtn;
    private TextView distanceView;

    public static boolean waitingForData;
    private int distance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distance);

        distanceView = findViewById(R.id.distance_view);

        refreshBtn = findViewById(R.id.refresh3_btn);
        refreshBtn.setOnClickListener(view -> requestDistance());

        requestDistance();
    }

    private void requestDistance(){
        waitingForData = true;

        BluetoothGattCharacteristic writeChar = BluetoothAPIUtils.bluetoothGatt.getService(Consts.THE_SERVICE).getCharacteristic(Consts.THE_WRITE_CHAR);
        writeChar.setValue(Consts.getStaticData);
        BluetoothAPIUtils.bluetoothGatt.writeCharacteristic(writeChar);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(distanceReceiver, new IntentFilter("GetDistanceData"));
    }

    @Override
    protected void onPause() {
        unregisterReceiver(distanceReceiver);
        super.onPause();
    }

    private BroadcastReceiver distanceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            /*
             * Get values as IntExtra
             */
            distance = intent.getIntExtra(Consts.DISTANCE,-1);

            distanceView.setText(String.valueOf(distance));
            Log.i(TAG, "Distance: " +  distance);

            waitingForData = false;
        }
    };
}