package com.example.biosensordataanalyzer.MeasurmentsActivities;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.biosensordataanalyzer.Bluetooth.BluetoothAPIUtils;
import com.example.biosensordataanalyzer.Connection.ConnectionActivity;
import com.example.biosensordataanalyzer.Constants.Consts;
import com.example.biosensordataanalyzer.Main.MainActivity;
import com.example.biosensordataanalyzer.R;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.lang.reflect.Array;
import java.security.interfaces.DSAKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


// Activity that menages pulse measure
public class PulseActivity extends AppCompatActivity {

    private static final String TAG = "PulseActivity";

    // Declare TextViews
    private TextView pulseText, oxygenText;
    TextView readyMeasureText;

    // Current Pulse and Oxygen Values
    private int pulse, oxygen;

    // Pulse and Oxygen final measurement (average after 20sec interval) + counter used to calculate average
    private int pulseSum, oxygenSum, counter;

    // Declare buttons
    private Button startMeasureButton, stopMeasureButton;

    // Flag that tells whether pulse measure is performed
    private boolean pulseMeasurement;

    private HashMap<String, ArrayList<String>> normsTableMale, normsTableFemale;

    private GraphView graph;
    private GridLabelRenderer gridLabelRenderer;
    private LineGraphSeries<DataPoint> series;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pulse);

        /*
         * Initialize graphical components
         */
        pulseText = (TextView) findViewById(R.id.bpm_view);
        oxygenText = (TextView) findViewById(R.id.oxygen_view);
        readyMeasureText = findViewById(R.id.ready_pulse_view);


        normsTableMale = new HashMap<>();
        normsTableMale.put("18;25", new ArrayList<>(Arrays.asList("49;55", "56;61", "62;65", "66;69", "70;73", "74;81", "82;999")));
        normsTableMale.put("26;35", new ArrayList<>(Arrays.asList("49;54", "56;61", "62;65", "66;70", "71;74", "75;81", "82;999")));
        normsTableMale.put("36;45", new ArrayList<>(Arrays.asList("50;56", "57;62", "63;66", "67;70", "71;75", "76;82", "83;999")));
        normsTableMale.put("46;55", new ArrayList<>(Arrays.asList("50;57", "58;63", "64;67", "68;71", "72;76", "77;83", "84;999")));
        normsTableMale.put("56;65", new ArrayList<>(Arrays.asList("51;56", "57;61", "62;67", "68;71", "72;75", "76;81", "82;999")));
        normsTableMale.put("66;999", new ArrayList<>(Arrays.asList("50;55", "56;61", "62;65", "66;69", "70;73", "74;19", "80;999")));


        normsTableFemale = new HashMap<>();
        normsTableFemale.put("18;25", new ArrayList<>(Arrays.asList("54;60", "61;65", "66;69", "70;73", "74;78", "79;84", "85;999")));
        normsTableFemale.put("26;35", new ArrayList<>(Arrays.asList("54;59", "60;64", "65;68", "69;72", "73;76", "77;82", "83;999")));
        normsTableFemale.put("36;45", new ArrayList<>(Arrays.asList("54;59", "60;64", "65;69", "70;73", "74;78", "79;84", "85;999")));
        normsTableFemale.put("46;55", new ArrayList<>(Arrays.asList("54;60", "61;65", "66;69", "70;73", "74;77", "77;83", "84;999")));
        normsTableFemale.put("56;65", new ArrayList<>(Arrays.asList("54;59", "60;64", "65;68", "69;73", "74;77", "78;83", "84;999")));
        normsTableFemale.put("66;999", new ArrayList<>(Arrays.asList("50;59", "60;64", "65;68", "69;72", "73;76", "77;84", "84;999")));

        /*
         * Assign methods to start/stop button
         */
        startMeasureButton = (Button) findViewById(R.id.start_measurement_btn);
        startMeasureButton.setOnClickListener(view -> {
            startMeasurement();
        });

        stopMeasureButton = (Button) findViewById(R.id.stop_measurement_btn);
        stopMeasureButton.setOnClickListener(view -> {
            stopMeasurement();
        });


        series = new LineGraphSeries<>();
        graph = findViewById(R.id.pulse_graph);
        graph.addSeries(series);

        graph.setTitle("Live pulse measure");
        graph.setTitleColor(Color.rgb(0, 100, 0));


        gridLabelRenderer = graph.getGridLabelRenderer();
        gridLabelRenderer.setGridColor(Color.rgb(0, 100, 0));
        gridLabelRenderer.setHorizontalLabelsColor(Color.rgb(0, 100, 0));
        gridLabelRenderer.setVerticalLabelsColor(Color.rgb(0, 100, 0));
        gridLabelRenderer.setPadding(32);
        gridLabelRenderer.setHorizontalAxisTitle("Probes");
        gridLabelRenderer.setHorizontalAxisTitleColor(Color.rgb(100, 100, 200));
        gridLabelRenderer.setVerticalAxisTitleColor(Color.rgb(100, 100, 200));
        gridLabelRenderer.setVerticalAxisTitle("BPM");
        gridLabelRenderer.setLabelFormatter(new DefaultLabelFormatter(){
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if(isValueX){
                    return ""+(int)value;
                }

                return super.formatLabel(value, isValueX);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(pulseReceiver, new IntentFilter("GetPulseData"));
    }

    @Override
    protected void onPause() {
        unregisterReceiver(pulseReceiver);
        super.onPause();
    }


    /*
     * Start measure method
     * If the measure is off - begin
     * Zero all variables, set flag pulseMeasurement to true
     * Stop the measure with 20sec delay (ExecutorService)
     * Set flag that indicates measure
     * Write proper characteristic to WRITE CHANNEL, to get stuff from the bracelet
     */
    private void startMeasurement(){
        if(BluetoothAPIUtils.bluetoothGatt != null && !ConnectionActivity.isMeasuring){

            pulseSum = 0;
            oxygenSum = 0;
            counter = 0;

            series = new LineGraphSeries<>();
            graph.removeAllSeries();
            graph.addSeries(series);

            pulseMeasurement = true;
            readyMeasureText.setText("Measure in progress...");

            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.schedule(this::stopMeasurement, 20, TimeUnit.SECONDS);

            ConnectionActivity.isMeasuring = true;
            BluetoothGattCharacteristic writeChar = BluetoothAPIUtils.bluetoothGatt.getService(Consts.THE_SERVICE).getCharacteristic(Consts.THE_WRITE_CHAR);
            writeChar.setValue(Consts.openLiveDataStream);
            BluetoothAPIUtils.bluetoothGatt.writeCharacteristic(writeChar);
        }
    }


    /*
     * Stop measurement method
     * If measure is on, prepare characteristic and write it with 1 sec delay, so every flag in the system has time to properly set
     * Make a toast, set flags to false
     */
    private void stopMeasurement() {
        if(BluetoothAPIUtils.bluetoothGatt != null && ConnectionActivity.isMeasuring){
            BluetoothGattCharacteristic writeChar = BluetoothAPIUtils.bluetoothGatt.getService(Consts.THE_SERVICE).getCharacteristic(Consts.THE_WRITE_CHAR);
            writeChar.setValue(Consts.closeLiveDataStream);

            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.schedule(() -> { BluetoothAPIUtils.bluetoothGatt.writeCharacteristic(writeChar); }, 1, TimeUnit.SECONDS);

            this.runOnUiThread(() -> {
                Toast.makeText(getApplicationContext(), "Measure finished!", Toast.LENGTH_LONG).show();
                showPopUp();
            });
        }
    }



    // Listen to incoming Pulse and Oxygen signals
    private BroadcastReceiver pulseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            /*
             * Get values as IntExtra
             */
            pulse = intent.getIntExtra(Consts.PULSE,-1);
            oxygen = intent.getIntExtra(Consts.OXYGEN,-1);

            // If we get measurements != 0 and measurement is on, calculate current average and print current text
            if(ConnectionActivity.isMeasuring && pulse != 0 && oxygen != 0) {
                pulseSum += pulse;
                oxygenSum += oxygen;
                counter += 1;

                series.appendData(new DataPoint(counter, pulse), true, 100);
                graph.addSeries(series);

                pulseText.setText(String.valueOf(pulse) + " BPM");
                oxygenText.setText(String.valueOf(oxygen) + "%");

                Log.i(TAG, "Pulse Finale: " + String.valueOf(pulseSum / counter));
                Log.i(TAG, "Oxygen Finale: " + String.valueOf(oxygenSum / counter));

                // If the measurement is still going, send ack signal for next data (write characteristic)
                if (pulseMeasurement) {
                    BluetoothGattCharacteristic writeChar = BluetoothAPIUtils.bluetoothGatt.getService(Consts.THE_SERVICE).getCharacteristic(Consts.THE_WRITE_CHAR);
                    writeChar.setValue(Consts.ackLiveDataStream);
                    BluetoothAPIUtils.bluetoothGatt.writeCharacteristic(writeChar);
                }
            }

            Log.i(TAG, "Pulse: " + String.valueOf(pulse));
            Log.i(TAG, "Oxygen: " + String.valueOf(oxygen));


        }
    };


    private TextView popUpPul, popUpOxy, popUpPSText, popUpPulseNorm;

    private void showPopUp(){

        ConnectionActivity.isMeasuring = false;
        pulseMeasurement = false;

        pulseText.setText(String.valueOf(pulseSum / counter) + " BPM");
        oxygenText.setText(String.valueOf(oxygenSum / counter) + "%");
        readyMeasureText.setText("Ready for measure!");

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        try {
            View popView = inflater.inflate(R.layout.pulse_popup, null);
            PopupWindow popWindow = new PopupWindow(popView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

            popWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            popWindow.setElevation(20);
            popWindow.showAtLocation(readyMeasureText, Gravity.CENTER, 0, 0);


            popUpPul = popView.findViewById(R.id.pop_pul_view);
            popUpPul.setText(String.valueOf(pulseSum / counter) + " BPM");

            popUpOxy = popView.findViewById(R.id.pop_oxy_view);
            popUpOxy.setText(String.valueOf(oxygenSum / counter) + " %");

            popUpPSText = popView.findViewById(R.id.pop_ps_text);
            popUpPSText.setText(String.format(Locale.ENGLISH, "There is %.2f", MainActivity.currentUser.outputPressureHDArr[0][0] * 100) + "% chance of you having a heart disease");

            popUpPulseNorm = popView.findViewById(R.id.pulse_norm_check);
            popUpPulseNorm.setText("Your pulse is: " + checkPulseNorms());


            popView.setOnTouchListener((v, event) -> {
                popWindow.dismiss();
                return true;
            });

        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }


    private String checkPulseNorms(){

        int pulse = pulseSum / counter;

        ArrayList<String> communicateTable = new ArrayList<>(Arrays.asList("athlete", "excellent", "good", "above average", "average", "below average", "poor"));

        HashMap<String, ArrayList<String>> normsTable;

        switch (MainActivity.currentUser.sex){
            case 1:
                normsTable = normsTableFemale;
                break;
            case 2:
                normsTable = normsTableMale;
                break;
            default:
                return "enter user data first";
        }


        for(Map.Entry<String, ArrayList<String>> it : normsTable.entrySet()){
            String[] thresholds = it.getKey().split(";");
            if(Integer.parseInt(thresholds[0]) <= MainActivity.currentUser.age && Integer.parseInt(thresholds[1]) >= MainActivity.currentUser.age){
                int iter = 0;
                for(String item : it.getValue()){
                    String[] miniThresholds = item.split(";");
                    if(Integer.parseInt(miniThresholds[0]) <= pulse && Integer.parseInt(miniThresholds[1]) >= pulse){
                        return communicateTable.get(iter);
                    }
                    ++iter;
                }
            }
        }

        return "you're too young";
    }
}