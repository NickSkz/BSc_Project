package com.example.biosensordataanalyzer.Connection;

import android.app.Service;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.biosensordataanalyzer.Bluetooth.BluetoothAPIUtils;
import com.example.biosensordataanalyzer.Constants.Consts;
import com.example.biosensordataanalyzer.Main.MainActivity;
import com.example.biosensordataanalyzer.MeasurmentsActivities.PulseActivity;
import com.example.biosensordataanalyzer.StaticData.CaloriesActivity;
import com.example.biosensordataanalyzer.StaticData.DistanceActivity;
import com.example.biosensordataanalyzer.StaticData.StepsActivity;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static android.bluetooth.BluetoothAdapter.STATE_CONNECTED;
import static android.bluetooth.BluetoothAdapter.STATE_DISCONNECTED;

public class ConnectionService extends Service {

    public ConnectionService() {
    }

    static final String TAG =  "ConnectionService";

    //Current connection state
    private int connectionState = STATE_DISCONNECTED;

    /*
     * Declare availible connection states
     */
    public final static String ACTION_GATT_CONNECTED =
            "ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "EXTRA_DATA";

    public static boolean readyForCommands;

    // Handler to put messages on UI Thread
    Handler handler = new Handler(Looper.getMainLooper());

    // Matrix of Gatt characteristics
    List<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics;


    /*
     * Override onBind - mandatory
     */
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        ConnectionService getService() {
            return ConnectionService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    // Latch to control asynchronous gatt methods
    private final CountDownLatch latch = new CountDownLatch(1);

    // Launched after service is started
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Connect to GATT server
        BluetoothAPIUtils.bluetoothGatt = BluetoothAPIUtils.bluetoothDevice.connectGatt(getApplicationContext(), false, gattCallback);

        // If connection is established (latch is disabled), get services
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Get all services to the list
        getServChar( BluetoothAPIUtils.bluetoothGatt.getServices());


        // Set notification for main channel
        BluetoothGattCharacteristic chara = BluetoothAPIUtils.bluetoothGatt.getService(Consts.THE_SERVICE).getCharacteristic(Consts.THE_NOTIFY_CHAR);
        BluetoothAPIUtils.bluetoothGatt.setCharacteristicNotification(chara, true);

        // We need to set notification when particular value changes
        BluetoothGattDescriptor descriptor = chara.getDescriptor(
                UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
        if(descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            BluetoothAPIUtils.bluetoothGatt.writeDescriptor(descriptor);
            Log.i(TAG, "Notification set!");
        }

        readyForCommands = true;

        return super.onStartCommand(intent, flags, startId);
    }


    // Methods from BLE API, based on Android Developer BLE overview
    private final BluetoothGattCallback gattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                    int newState) {

                    // If connected - notify user
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        // Set actual connection + perform action connected with it
                        connectionState = STATE_CONNECTED;
                        broadcastUpdate(ACTION_GATT_CONNECTED);

                        // Inform user, by toast launched on the UI Thread thanks to handler
                        handler.post(() ->
                                Toast.makeText(getApplicationContext(), "Connected to GATT server!", Toast.LENGTH_LONG).show()
                        );

                        // If connected - discover devices
                        Log.i(TAG, "Attempting to start service discovery:" +
                                BluetoothAPIUtils.bluetoothGatt.discoverServices());

                        // If disconnected
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        // Set actual connection + perform action connected with it
                        connectionState = STATE_DISCONNECTED;
                        Log.i(TAG, "Disconnected from GATT server.");
                        broadcastUpdate(ACTION_GATT_DISCONNECTED);
                    }
                }

                @Override
                /*
                 * New services discovered
                 * Perform action on discovery
                 */
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    // If discovery succeed
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        // Perform action connected with it
                        broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);

                        // Print out UUIDs of detected services
                        for(BluetoothGattService item : BluetoothAPIUtils.bluetoothGatt.getServices()){
                            Log.i(TAG, item.getUuid().toString());
                        }

                        // Release latch (set it to 0)
                        latch.countDown();

                    } else {
                        Log.w(TAG, "onServicesDiscovered received: " + status);
                    }
                }

                /*
                 * Result of a characteristic read operation
                 * Perform action after reading char
                 */

                @Override
                public void onCharacteristicRead(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic,
                                                 int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        // Perform action connected with it + send read characteristic
                        Log.i(TAG, "******************************CHARACTERISTIC FOUND!******************************");
                        BluetoothAPIUtils.bluetoothGatt.setCharacteristicNotification(characteristic, true);

                        // We need to set notification when particular value changes
                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                                UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                        if(descriptor != null) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            BluetoothAPIUtils.bluetoothGatt.writeDescriptor(descriptor);
                        }
                        broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                        Log.i(TAG, "***************************************************************************");
                    }else{
                        Log.i(TAG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!CHARACTERISTIC FOUND!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                        Log.i(TAG, "CHAR READ FAILED, STATUS: " + Integer.toBinaryString(status));
                        Log.i(TAG, "Service UUID: " + characteristic.getService().getUuid());
                        Log.i(TAG, "Characteristic UUID: " + characteristic.getUuid());
                        Log.i(TAG, "Properties: " + Integer.toBinaryString(characteristic.getProperties()));
                        Log.i(TAG, "Bytes to string: " + Arrays.toString(characteristic.getValue()));
                        Log.i(TAG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    }
                }

                @Override
                // Characteristic notification - when it changes notify user
                public void onCharacteristicChanged(BluetoothGatt gatt,
                                                    BluetoothGattCharacteristic characteristic) {

                    // Get characteristic of incomin' value
                    Log.i(TAG, "VALUE: " +  Arrays.toString(characteristic.getValue()));

                    // Broadcast pulse and oxygen message to corresponding receivers
                    if(ConnectionActivity.isMeasuring) {
                        if (characteristic.getValue().length == 16) {
                            Intent intent = new Intent("GetPulseData");
                            intent.putExtra(Consts.PULSE, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 11));
                            intent.putExtra(Consts.OXYGEN, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 12));
                            sendBroadcast(intent);

                            Intent pIntent = new Intent("GetBloodPressureData");
                            pIntent.putExtra(Consts.SYSTOLIC, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 14));
                            pIntent.putExtra(Consts.DIASTOLIC, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 13));
                            sendBroadcast(pIntent);
                        } else if (characteristic.getValue().length == 8) {
                            Consts.ackLiveDataStream[7] = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 7).byteValue();
                        }
                    } else if (StepsActivity.waitingForData){
                        if (characteristic.getValue().length == 20){
                            Intent intent = new Intent("GetStepsData");

                            byte[] stepArr = new byte[4];
                            stepArr[0] = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 5).byteValue();
                            stepArr[1] = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 6).byteValue();
                            stepArr[2] = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 7).byteValue();
                            stepArr[3] = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 8).byteValue();
                            ByteBuffer byteBuffer = ByteBuffer.wrap(stepArr);
                            int stepShortVal = byteBuffer.getInt();

                            intent.putExtra(Consts.STEPS, stepShortVal);
                            sendBroadcast(intent);
                        }
                    } else if (CaloriesActivity.waitingForData){
                        if (characteristic.getValue().length == 20) {
                            Intent intent = new Intent("GetCaloriesData");

                            byte[] caloriesArr = new byte[4];
                            caloriesArr[0] = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 13).byteValue();
                            caloriesArr[1] = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 14).byteValue();
                            caloriesArr[2] = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 15).byteValue();
                            caloriesArr[3] = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 16).byteValue();
                            ByteBuffer byteBuffer = ByteBuffer.wrap(caloriesArr);
                            int caloriesShortVal = byteBuffer.getInt();

                            intent.putExtra(Consts.CALORIES, caloriesShortVal);
                            sendBroadcast(intent);
                        }
                    } else if (DistanceActivity.waitingForData){
                        if (characteristic.getValue().length == 20) {
                            Intent intent = new Intent("GetDistanceData");

                            byte[] distanceArr = new byte[4];
                            distanceArr[0] = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 9).byteValue();
                            distanceArr[1] = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 10).byteValue();
                            distanceArr[2] = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 11).byteValue();
                            distanceArr[3] = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 12).byteValue();
                            ByteBuffer byteBuffer = ByteBuffer.wrap(distanceArr);
                            int distanceShortVal = byteBuffer.getInt();

                            intent.putExtra(Consts.DISTANCE, distanceShortVal);
                            sendBroadcast(intent);
                        }
                    } else if (MainActivity.waitingForBattery){
                        if(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) != 171) {
                            Intent intent = new Intent("GetBatteryData");
                            intent.putExtra(Consts.BATTERY, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 5));
                            sendBroadcast(intent);
                        }
                    } else if (MainActivity.waitingForZeroing){
                        if(characteristic.getValue().length == 8){
                            MainActivity.waitingForZeroing = false;
                            MainActivity.zeroingDataLatch.countDown(); //To 0
                        }
                    }

                }


            };

    // Destroy service
    @Override
    public void onDestroy() {
        super.onDestroy();
        readyForCommands = false;
        Log.i(TAG, "Service destroyed!");
    }

    // Method to collect info from BluetoothGattCallback
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);

        sendBroadcast(intent);
    }

    // Method to collect info from BluetoothGattCallback
    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {

        // Show these bytes
        byte[] data = characteristic.getValue();
        final StringBuilder stringBuilder = new StringBuilder(data.length);
        for(byte i : data)
            stringBuilder.append(String.format("%02X ", i));

        Log.i(TAG, "Service it belongs to: " + characteristic.getService().getUuid());

        Log.i(TAG, "Raw bytes!: " + stringBuilder.toString());

        Log.i(TAG, "String value: " + characteristic.getStringValue(0));

        Log.i(TAG, "Properties: " + Integer.toBinaryString(characteristic.getProperties()));
        Log.i(TAG, "Permissions: " + Integer.toBinaryString(characteristic.getPermissions()));

        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }


    // Add all characteristic to ArrayList
    private void getServChar(List<BluetoothGattService> gattServices) {

        // If null return
        if (gattServices == null)
            return;

        // Initialize container for all services and characteristics within them
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
        // Loops through available GATT Services.

        // Go through all services
        for (BluetoothGattService gattService : gattServices) {

            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();

            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Go through all characteristics in the service
            for (BluetoothGattCharacteristic gattCharacteristic :
                    gattCharacteristics) {
                if(gattCharacteristic == null)
                    continue;

                charas.add(gattCharacteristic);
                Log.i(TAG, gattCharacteristic.getUuid().toString());
            }
            // Add ArrayList of characteristics to the container (cell in the mGattCharacteristics corresponds to one service - in which there are characteristics)
            mGattCharacteristics.add(charas);
        }

    }

}

