package com.example.biosensordataanalyzer.Main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.ActivityNotFoundException;
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
import com.example.biosensordataanalyzer.Connection.ConnectionService;
import com.example.biosensordataanalyzer.Constants.Consts;
import com.example.biosensordataanalyzer.StaticData.CaloriesActivity;
import com.example.biosensordataanalyzer.StaticData.DistanceActivity;
import com.example.biosensordataanalyzer.StaticData.StepsActivity;
import com.example.biosensordataanalyzer.User.CurrentUser;
import com.example.biosensordataanalyzer.MeasurmentsActivities.PressureActivity;
import com.example.biosensordataanalyzer.MeasurmentsActivities.PulseActivity;
import com.example.biosensordataanalyzer.R;
import com.example.biosensordataanalyzer.User.EditUserInfoActivity;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


// Main activity launched after start of the application
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // Declaration of current logged user
    public static CurrentUser currentUser;

    public static boolean waitingForBattery;
    private int batteryLevel;


    /*
     * Declare graphical components
     */
    private Toolbar toolbar;

    private Button connSettingsBtn;
    private Button pulseBtn;
    private Button pressureBtn;
    private Button editBtn;
    private Button stepsBtn;
    private Button caloriesBtn;
    private Button distanceBtn;
    private Button batteryCheckBtn;
    private Button calibrateBtn;

    private int EDIT_USER_ACTIVITY = 1;
    private TextView mainUserText;

    private TextView batteryView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
         * Initialize graphical components
         */
        toolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(toolbar);

        connSettingsBtn = findViewById(R.id.connection_settings_btn);
        connSettingsBtn.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, ConnectionActivity.class)));

        pulseBtn= (Button) findViewById(R.id.pulse_btn);
        pulseBtn.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, PulseActivity.class)));

        pressureBtn = (Button) findViewById(R.id.pressure_btn);
        pressureBtn.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, PressureActivity.class)));

        editBtn = findViewById(R.id.edit_usr_btn);
        editBtn.setOnClickListener(view -> startActivityForResult(new Intent(MainActivity.this, EditUserInfoActivity.class), EDIT_USER_ACTIVITY));

        stepsBtn = findViewById(R.id.steps_btn);
        stepsBtn.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, StepsActivity.class)));

        caloriesBtn = findViewById(R.id.calories_btn);
        caloriesBtn.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, CaloriesActivity.class)));

        distanceBtn = findViewById(R.id.distance_btn);
        distanceBtn.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, DistanceActivity.class)));


        batteryView = findViewById(R.id.baterry_view);

        batteryCheckBtn = findViewById(R.id.check_bat_but);
        batteryCheckBtn.setOnClickListener(view -> batteryLevelCheck());

        calibrateBtn = findViewById(R.id.calibrate_btn);
        calibrateBtn.setOnClickListener(view -> {
            try {
                calibrateBand();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        /*
         * Enable and turn the bluetooth on
         */
        int PERMISSION_REQUEST_COARSE_LOCATION = 1;
        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);

        // BluetoothAdapter, that represents device's Bluetooth radio (one for entire system)
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAPIUtils.bluetoothAdapter = bluetoothManager.getAdapter();

        // Define variable needed for Bluetooth launch
        int REQUEST_ENABLE_BT = 2;


        /*
         * If device supports Bluetooth and is not enabled - enable it
         */
        if(BluetoothAPIUtils.bluetoothAdapter != null) {
            //If not enabled, enable it
            if (!BluetoothAPIUtils.bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            //If a device does not support Bt, make a toast
        }else {
            Toast.makeText(this, "The device does not support Bluetooth", Toast.LENGTH_LONG).show();
        }


        /*
         * Initialize and read from the file data about last logged user
         */
        currentUser = new CurrentUser();
        try{
            currentUser.load(getApplicationContext());
        }
        catch(Exception e){
            e.printStackTrace();
        }

        // Parse user name on screen
        mainUserText = findViewById(R.id.main_user_txt);
        mainUserText.setText(currentUser.name);

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
        if(ConnectionService.readyForCommands) {


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
            prepareAccordingToDecompiler(tempArr, false, false, 0, 8);

            Log.i(TAG, String.valueOf(passedValue));
            Log.i(TAG, Arrays.toString(Consts.calibrateBand));


            /*
             * Really, really terrible version (send date from the past to zero out data, then send current datetime :) )
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


    /*
     * METHOD BASED ON DECOMPILED .apk, variable names slightly changed according to JADX decompiler (to see raw decompiled
     * functions go to decompilation folder in project's main directory)
     */
    private static void prepareAccordingToDecompiler(byte[] bArr, boolean z, boolean z2, int i, int i2) {
        int i3;
        int i4;
        if (bArr != null) {
            i4 = bArr.length;
            i3 = 0;
            for (int idx = 0; idx < i4; ++idx) {
                i3 = (i3 >> 8) ^ Consts.crc16_table[(bArr[idx] ^ i3) & 255];
            }
        } else {
            i4 = 0;
            i3 = 0;
        }

        Consts.calibrateBand[0] = (byte)-85;
        Consts.calibrateBand[1] = (byte) ((z2 ? z ? 48 : 16 : 0) | (i & 15));
        Consts.calibrateBand[2] = (byte) ((i4 >> 8) & 255);
        Consts.calibrateBand[3] = (byte) (i4 & 255);
        Consts.calibrateBand[4] = (byte) ((i3 >> 8) & 255);
        Consts.calibrateBand[5] = (byte) (i3 & 255);
        Consts.calibrateBand[6] = (byte) ((i2 >> 8) & 255);
        Consts.calibrateBand[7] = (byte) (i2 & 255);
    }



    // Perform appropriate toast according to Bluetooth action
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Handle REQUEST_ENABLE_BT const, passed as requestCode
        switch(requestCode){
            case RESULT_OK:{
                Toast.makeText(this, "Bluetooth enabled", Toast.LENGTH_LONG).show();
                break;
            }
            case RESULT_CANCELED:{
                Toast.makeText(this, "Error, Bluetooth is not enabled", Toast.LENGTH_LONG).show();
                break;
            }
            default:
                break;
        }

        mainUserText.setText(currentUser.name);
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
            Log.i(TAG, "Battery: " + batteryLevel);

            waitingForBattery = false;
        }
    };

}