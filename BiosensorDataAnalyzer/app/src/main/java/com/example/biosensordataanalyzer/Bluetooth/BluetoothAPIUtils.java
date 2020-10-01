package com.example.biosensordataanalyzer.Bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;

// Class that contains Bluetooth API stuff
public class BluetoothAPIUtils {

    private static final String TAG = "BluetoothAPIUtils";

    // Declare BluetoothAdapter, that represents device Bluetooth radio (one for entire system)
    static public BluetoothAdapter bluetoothAdapter;

    // Our main BLE Device - Our chosen bracelet
    static public BluetoothDevice bluetoothDevice;

    // Declare Bluetooth Gatt instance
    static public BluetoothGatt bluetoothGatt;

    static public int rssi;

    private BluetoothAPIUtils(){

    };
}
