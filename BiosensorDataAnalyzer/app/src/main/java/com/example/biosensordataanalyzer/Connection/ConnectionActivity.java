package com.example.biosensordataanalyzer.Connection;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.biosensordataanalyzer.Bluetooth.BluetoothAPIUtils;
import com.example.biosensordataanalyzer.Constants.Consts;
import com.example.biosensordataanalyzer.Main.MainActivity;
import com.example.biosensordataanalyzer.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
    private Button infoBtn;
    private ListView deviceLst;
    private TextView scanText;


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
        scanBtn.setOnClickListener(view -> findPairedDevices());

        infoBtn = findViewById(R.id.info_button);
        infoBtn.setOnClickListener(view -> showPopUp());

         /*
          * Initialize Containers
          */
        BLEDevices = new HashSet<>();
        lstDevices = new ArrayList<>();
        lstAdapter = new ArrayAdapter<>(this, R.layout.listview_row, lstDevices);
        scanText = findViewById(R.id.scan_text);

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
                Toast.makeText(this, "SCAN FINISHED!", Toast.LENGTH_SHORT).show();
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
                // Accept only singnals that are stronger than -90 RSSI (we want the bracelet to be near our device)
                if(result.getRssi() > -90) {
                    // Add to the list view stuff that hasn't appeared earlier (add to LV if its possible to add smth to the HashSet)
                    if (BLEDevices.add(result.getDevice())) {
                        if (result.getDevice().getName() != null) {
                            if (result.getDevice().getName().startsWith("HBracelet"))
                                BluetoothAPIUtils.bluetoothDevice = result.getDevice();
                                BluetoothAPIUtils.rssi = result.getRssi();
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

/////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////

    public static boolean waitingForBattery;
    private int batteryLevel;
    private Button calibrateBtn;

    private TextView batteryView;
    private TextView rssiView;


    private void showPopUp(){

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        try {
            View popView = inflater.inflate(R.layout.bandinfo_popup, null);
            PopupWindow popWindow = new PopupWindow(popView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

            popWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            popWindow.setElevation(20);
            popWindow.showAtLocation(scanText, Gravity.CENTER, 0, 0);


            batteryView = popView.findViewById(R.id.battery_view);

            rssiView = popView.findViewById(R.id.rssi_view);

            calibrateBtn = popView.findViewById(R.id.calibrate_button);
            calibrateBtn.setOnClickListener(view -> {
                try {
                    calibrateBand();
                }catch (Exception e){
                    e.printStackTrace();
                }
            });



            popView.setOnTouchListener((v, event) -> {
                popWindow.dismiss();
                return true;
            });

        }catch (NullPointerException e){
            e.printStackTrace();
        }

        batteryLevelCheck();
    }


    private void batteryLevelCheck(){
        if(ConnectionService.readyForCommands) {
            BluetoothGattCharacteristic writeChar = BluetoothAPIUtils.bluetoothGatt.getService(Consts.THE_SERVICE).getCharacteristic(Consts.THE_WRITE_CHAR);
            writeChar.setValue(Consts.getBatteryLevel);
            BluetoothAPIUtils.bluetoothGatt.writeCharacteristic(writeChar);

            waitingForBattery = true;
        }
    }


    public static CountDownLatch zeroingDataLatch = new CountDownLatch(1);
    public static boolean waitingForZeroing;

    private void calibrateBand() throws InterruptedException {
        if(ConnectionService.readyForCommands && !waitingForBattery) {

            StringBuffer dateTimeBits = new StringBuffer();

            Calendar calendar = Calendar.getInstance();
            int seconds = calendar.get(Calendar.SECOND);
            String secondBits = String.format("%6s", Integer.toBinaryString(seconds)).replace(' ', '0');
            int minutes = calendar.get(Calendar.MINUTE);
            String minutesBits = String.format("%6s", Integer.toBinaryString(minutes)).replace(' ', '0');
            int hours = calendar.get(Calendar.HOUR_OF_DAY);
            String hourBits = String.format("%5s", Integer.toBinaryString(hours)).replace(' ', '0');
            int days = calendar.get(Calendar.DAY_OF_MONTH);
            String dayBits = String.format("%5s", Integer.toBinaryString(days)).replace(' ', '0');
            int months = calendar.get(Calendar.MONTH) + 1;
            String monthBits = String.format("%4s", Integer.toBinaryString(months)).replace(' ', '0');
            int years = calendar.get(Calendar.YEAR);
            String yearBits = Integer.toBinaryString(Integer.parseInt(String.valueOf(years).substring(2)));

            dateTimeBits.append(yearBits);
            dateTimeBits.append(monthBits);
            dateTimeBits.append(dayBits);
            dateTimeBits.append(hourBits);
            dateTimeBits.append(minutesBits);
            dateTimeBits.append(secondBits);

            Log.i(TAG, dateTimeBits.toString());


            int passedValue = Integer.parseInt(dateTimeBits.toString(), 2);

            Consts.calibrateBand[13] = (byte)(passedValue >>> 24);
            Consts.calibrateBand[14] = (byte)(passedValue >>> 16);
            Consts.calibrateBand[15] = (byte)(passedValue >>> 8);
            Consts.calibrateBand[16] = (byte)passedValue;

            byte[] tempArr = new byte[9];
            System.arraycopy(Consts.calibrateBand, 8, tempArr, 0, 9);
            prepareMessage(tempArr);

            Log.i(TAG, String.valueOf(passedValue));
            Log.i(TAG, Arrays.toString(Consts.calibrateBand));


            /*
             * Send date from the past to zero out data, then send current datetime :)
             */
            waitingForZeroing = true;
            BluetoothGattCharacteristic writeChar = BluetoothAPIUtils.bluetoothGatt.getService(Consts.THE_SERVICE).getCharacteristic(Consts.THE_WRITE_CHAR);
            writeChar.setValue(Consts.zeroOutStaticData);
            BluetoothAPIUtils.bluetoothGatt.writeCharacteristic(writeChar);

            // Latch it, until something releases it, or certain amount of time passes - to avoid deadlock
            zeroingDataLatch = new CountDownLatch(1);
            zeroingDataLatch.await(5, TimeUnit.SECONDS);

            BluetoothGattCharacteristic writeDateChar = BluetoothAPIUtils.bluetoothGatt.getService(Consts.THE_SERVICE).getCharacteristic(Consts.THE_WRITE_CHAR);
            writeDateChar.setValue(Consts.calibrateBand);
            BluetoothAPIUtils.bluetoothGatt.writeCharacteristic(writeDateChar);
        }
    }


    private static void prepareMessage(byte[] arr) {
        int buffOne = 0;
        int messageLength = arr.length;

        for (int idx = 0; idx < messageLength; ++idx) {
            buffOne = (buffOne >> 8) ^ Consts.crc16_table[(arr[idx] ^ buffOne) & 255];
        }

        Consts.calibrateBand[0] = (byte) -85;
        Consts.calibrateBand[1] = (byte) 0;
        Consts.calibrateBand[2] = (byte) ((messageLength >> 8) & 255);
        Consts.calibrateBand[3] = (byte) (messageLength & 255);
        Consts.calibrateBand[4] = (byte) ((buffOne >> 8) & 255);
        Consts.calibrateBand[5] = (byte) (buffOne & 255);
        Consts.calibrateBand[6] = (byte) 0;
        Consts.calibrateBand[7] = (byte) 8;
    }



    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(batteryReceiver, new IntentFilter("GetBatteryData"));
    }

    @Override
    protected void onPause() {
        unregisterReceiver(batteryReceiver);
        super.onPause();
    }

    private BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            /*
             * Get values as IntExtra
             */
            batteryLevel = intent.getIntExtra(Consts.BATTERY,-1);

            batteryView.setText(String.valueOf(batteryLevel) + "%");
            rssiView.setText(String.valueOf(BluetoothAPIUtils.rssi));
            Log.i(TAG, "Battery: " + batteryLevel);

            waitingForBattery = false;
        }
    };



}
