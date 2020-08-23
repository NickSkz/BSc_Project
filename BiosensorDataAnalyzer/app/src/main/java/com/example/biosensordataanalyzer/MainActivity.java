package com.example.biosensordataanalyzer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static CurrentUser currentUser;

    private Toolbar toolbar;

    private Button connSettingsBtn;
    private Button pulseBtn;
    private Button pressureBtn;
    private Button editBtn;

    private TextView mainUserText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Initiate graphical components
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        toolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(toolbar);

        connSettingsBtn = findViewById(R.id.connection_settings_btn);
        connSettingsBtn.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, ConnectionActivity.class)));

        pulseBtn= (Button) findViewById(R.id.pulse_btn);
        pulseBtn.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, PulseActivity.class)));

        pressureBtn = (Button) findViewById(R.id.pressure_btn);
        pressureBtn.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, PressureActivity.class)));

        editBtn = findViewById(R.id.edit_usr_btn);
        editBtn.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, EditUserActivity.class)));

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Enable and turn the bluetooth on
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        int PERMISSION_REQUEST_COARSE_LOCATION = 1;
        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAPIUtils.bluetoothAdapter = bluetoothManager.getAdapter();

        //BluetoothAdapter, that represents device's Bluetooth radio (one for entire system)
        int REQUEST_ENABLE_BT = 1;

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


        currentUser = new CurrentUser();
        try{
            currentUser.load(getApplicationContext());
        }
        catch(Exception e){
            e.printStackTrace();
        }

        mainUserText = findViewById(R.id.main_user_txt);
        mainUserText.setText(currentUser.name);

    }



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
    }
}