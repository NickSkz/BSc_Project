package com.example.biosensordataanalyzer.Connection;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.biosensordataanalyzer.Bluetooth.BluetoothAPIUtils;
import com.example.biosensordataanalyzer.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConnectionActivity extends AppCompatActivity {

    private static final String TAG = "СonnectionActivity";

    // Flag tells whether a measurement is performed
    public static boolean isMeasuring;
    // Flag that signalizes if ConnectionService is running
    public static boolean serviceRunning;
    // Flag for signalizing whether scanning process takes place
    private boolean isScanning;

    /*
     * Declare graphical components
     */
    private Button scanBtn;
    private ListView deviceLst;


    // List of available devices' name
    private ArrayList<String> lstDevices;
    // Array adapter for List View
    private ArrayAdapter<String> lstAdapter;
    // Set of BLE Devices - not to multiplicate same devices again and again
    private Set<BluetoothDevice> BLEDevices;


    // Scanner used for detecting devices
    private final BluetoothLeScanner BLEScanner = BluetoothAPIUtils.bluetoothAdapter.getBluetoothLeScanner();
    // Intent for ConnectionService
    private Intent connServiceIntent;
    // Thread for listening service
    private Thread serviceThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        /*
         * Initialize scan button and assign appropriate method
         */
        scanBtn = findViewById(R.id.scan_button);
        scanBtn.setOnClickListener((view) -> {
            findPairedDevices();
        });

         /*
          * Initialize Containers
          */
        BLEDevices = new HashSet<>();
        lstDevices = new ArrayList<>();
        lstAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lstDevices);

        /*
         * Initialize device list
         * If anyone presses bracelet, start a background service and make a connection
         */
        deviceLst = findViewById(R.id.scan_list_view);
        deviceLst.setAdapter(lstAdapter);
        deviceLst.setOnItemClickListener((parent, view, position, id) -> {
            // Stop service if already running in bg (if someone swaps device on the list)
            if(connServiceIntent != null)
                stopService(connServiceIntent);

            String name = (String) parent.getItemAtPosition(position);

            // If someone chooses bracelet from devices -  HBracelet...
            if(name.startsWith("HBracelet") && !serviceRunning) {
                String[] deviceName = name.split("RSSI");
                Log.d(TAG, deviceName[0]);

                // ...Start a background service
                serviceThread = new Thread(() ->{
                    serviceRunning = true;
                    startService(new Intent(getApplicationContext(), ConnectionService.class));
                    Log.i(TAG, "Service started");
                 });
                serviceThread.start();
            }
            else if (name.startsWith("HBracelet") && serviceRunning){
                Toast.makeText(this, "Device already connected!", Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(this, "Unknown Device", Toast.LENGTH_LONG).show();
                Log.i(TAG, name);
            }

        });

    }

    // Handler to manage message queue + main looper (Handler() - deprecated)
    private Handler handler = new Handler(Looper.getMainLooper());

    // Desired scan period
    private static final long SCAN_PERIOD = 10000;


    // Method that starts scanning - after SCAN_PERIOD, break scanning
    private void scanLeDevice(final boolean enable) {

        if(connServiceIntent != null)
            stopService(connServiceIntent);

        if (enable) {
            // Stops scanning after a pre-defined scan period, add this to message queue (its still UI Thread!).
            handler.postDelayed(() -> {
                BLEScanner.stopScan(leScanCallback);
                Toast.makeText(this, "SCAN FINISHED!", Toast.LENGTH_LONG).show();
                isScanning = false;
            }, SCAN_PERIOD);

            BLEScanner.startScan(leScanCallback);
            isScanning = true;
        } else {
            BLEScanner.stopScan(leScanCallback);
        }
    }


    // Report scan results
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            // Add device to the message queue - notify bout it ListView
            handler.post(() ->
            {
                // Accept only singnals that are stronger than -90 RSSI (we want a bracelet to be near our device)
                if(result.getRssi() > -90) {
                    // Add to the list view stuff that hasn't appeared earlier (add to LV if its possible to add smth to the HashSet)
                    if (BLEDevices.add(result.getDevice())) {
                        if (result.getDevice().getName() != null) {
                            if (result.getDevice().getName().startsWith("HBracelet"))
                                BluetoothAPIUtils.bluetoothDevice = result.getDevice();

                            // Add device name + signal strength
                            lstDevices.add(result.getDevice().getName() + " RSSI: " + result.getRssi());
                            lstAdapter.notifyDataSetChanged();
                        }
                    }
                    // Print to logs all scan results
                    Log.i(TAG, "FOUND: " + result.getDevice().getName());
                }
            });

        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.i(TAG, "SCAN FAILED");
        }
    };


    // Before scanning for devices, clear all the previous stuff
    public void findPairedDevices(){
        if(!isScanning) {
            BLEDevices.clear();
            lstDevices.clear();
            lstAdapter.notifyDataSetChanged();
            scanLeDevice(true);
        }
    }

}
