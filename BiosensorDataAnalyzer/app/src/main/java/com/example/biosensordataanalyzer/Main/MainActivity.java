package com.example.biosensordataanalyzer.Main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.biosensordataanalyzer.Bluetooth.BluetoothAPIUtils;
import com.example.biosensordataanalyzer.Connection.ConnectionActivity;
import com.example.biosensordataanalyzer.Connection.ConnectionService;
import com.example.biosensordataanalyzer.StaticData.TrainingActivity;
import com.example.biosensordataanalyzer.User.CurrentUser;
import com.example.biosensordataanalyzer.MeasureActivities.PressureActivity;
import com.example.biosensordataanalyzer.MeasureActivities.PulseActivity;
import com.example.biosensordataanalyzer.R;
import com.example.biosensordataanalyzer.User.EditUserInfoActivity;


// Main activity launched after start of the application
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // Declaration of current logged user
    public static CurrentUser currentUser;


    /*
     * Declare graphical components
     */
    private Toolbar toolbar;

    private Button connSettingsBtn;
    private ImageButton pulseBtn;
    private ImageButton pressureBtn;
    private Button editBtn;
    private ImageButton stepsBtn;


    private int EDIT_USER_ACTIVITY = 1;
    private TextView mainUserText, mainAgeText, mainHeightText, mainWeightText;
    public static TextView connStatText;



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

        pulseBtn = findViewById(R.id.pulse_btn);
        pulseBtn.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, PulseActivity.class)));

        pressureBtn = findViewById(R.id.pressure_btn);
        pressureBtn.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, PressureActivity.class)));

        editBtn = findViewById(R.id.edit_usr_btn);
        editBtn.setOnClickListener(view -> startActivityForResult(new Intent(MainActivity.this, EditUserInfoActivity.class), EDIT_USER_ACTIVITY));

        stepsBtn = findViewById(R.id.steps_btn);
        stepsBtn.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, TrainingActivity.class)));



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

        mainAgeText = findViewById(R.id.main_age_txt);
        mainAgeText.setText(String.valueOf(currentUser.age));

        mainHeightText = findViewById(R.id.main_height_txt);
        mainHeightText.setText(String.valueOf(currentUser.height));

        mainWeightText = findViewById(R.id.main_weight_txt);
        mainWeightText.setText(String.valueOf(currentUser.weight));

        connStatText = findViewById(R.id.conn_stat_tview);
        setConnStatus();
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
        mainAgeText.setText(String.valueOf(currentUser.age));
        mainHeightText.setText(String.valueOf(currentUser.height));
        mainWeightText.setText(String.valueOf(currentUser.weight));
    }

    public static void setConnStatus(){
        if(ConnectionService.readyForCommands){
            connStatText.setTextColor(Color.rgb(0x00, 0xFF, 0x00));
            connStatText.setText("Device Connected");
        } else {
            connStatText.setTextColor(Color.rgb(0xFF, 0x00, 0x00));
            connStatText.setText("Device Disconnected");
        }
    }


}