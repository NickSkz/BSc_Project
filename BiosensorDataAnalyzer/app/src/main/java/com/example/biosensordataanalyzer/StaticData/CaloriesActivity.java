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

public class CaloriesActivity extends AppCompatActivity {

    private static final String TAG = "CaloriesActivity";

    private Button refreshBtn;
    private TextView caloriesView;

    public static boolean waitingForData;
    private int calories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calories);

        caloriesView = findViewById(R.id.calories_view);

        refreshBtn = findViewById(R.id.refresh2_btn);
        refreshBtn.setOnClickListener(view -> requestCalories());

        requestCalories();
    }

    private void requestCalories(){
        waitingForData = true;

        BluetoothGattCharacteristic writeChar = BluetoothAPIUtils.bluetoothGatt.getService(Consts.THE_SERVICE).getCharacteristic(Consts.THE_WRITE_CHAR);
        writeChar.setValue(Consts.getStaticData);
        BluetoothAPIUtils.bluetoothGatt.writeCharacteristic(writeChar);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(caloriesReceiver, new IntentFilter("GetCaloriesData"));
    }

    @Override
    protected void onPause() {
        unregisterReceiver(caloriesReceiver);
        super.onPause();
    }

    private BroadcastReceiver caloriesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            /*
             * Get values as IntExtra
             */
            calories = intent.getIntExtra(Consts.CALORIES,-1);

            caloriesView.setText(String.valueOf(calories));
            Log.i(TAG, "Calories: " + calories);

            waitingForData = false;
        }
    };

}