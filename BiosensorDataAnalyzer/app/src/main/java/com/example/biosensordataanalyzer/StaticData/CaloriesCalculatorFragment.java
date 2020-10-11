package com.example.biosensordataanalyzer.StaticData;

import android.content.res.AssetFileDescriptor;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.biosensordataanalyzer.Main.MainActivity;
import com.example.biosensordataanalyzer.R;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CaloriesCalculatorFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CaloriesCalculatorFragment extends Fragment {

    private static final String TAG = "CalculatorFragment";

    private Spinner activitySpinner;
    private Map<String, Double> castSpinnerMap;

    private int bmr;
    private int caloriesLoss;
    private int caloriesMaintenance;

    private TextView bmrView, lossView, maintenanceView;

    private TextView walkingView, joggingView, fastRunView, cyclingView;

    // Interpreter/ByteBuffer for running tha network
    Interpreter interpreter;
    ByteBuffer tfLiteBuffer;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CaloriesCalculatorFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CaloriesCalculatorFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CaloriesCalculatorFragment newInstance(String param1, String param2) {
        CaloriesCalculatorFragment fragment = new CaloriesCalculatorFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        castSpinnerMap = new HashMap<>();

        // Initialize TfLite Interpreter
        initializeTfLite();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_calories_calculator, container, false);


        bmrView = v.findViewById(R.id.bmr_text_view);
        lossView = v.findViewById(R.id.wghloss_text_view);
        maintenanceView = v.findViewById(R.id.wghmain_text_view );


        walkingView = v.findViewById(R.id.walking_tview);
        joggingView = v.findViewById(R.id.jogging_tview);
        fastRunView = v.findViewById(R.id.frunn_tview);
        cyclingView = v.findViewById(R.id.cycling_tview);


        //WALK 3mph
        float[][] inpWalkArr = {{3, (float)(MainActivity.currentUser.weight / 0.45359237)}};
        float[][] walkArr = {{0.0f}};

        //RUN 6mph
        float[][] inpJogArr = {{1, (float)(MainActivity.currentUser.weight / 0.45359237)}};
        float[][] jogArr = {{0.0f}};

        //RUN 8mph
        float[][] inpRunArr = {{2, (float)(MainActivity.currentUser.weight / 0.45359237)}};
        float[][] runArr = {{0.0f}};

        //CYCLING 12-13mph
        float[][] inpCyclArr = {{0, (float)(MainActivity.currentUser.weight / 0.45359237)}};
        float[][] cyclArr = {{0.0f}};

        interpreter.run(inpWalkArr, walkArr);
        interpreter.run(inpJogArr, jogArr);
        interpreter.run(inpRunArr, runArr);
        interpreter.run(inpCyclArr, cyclArr);

        walkingView.setText(String.format("%.1f", walkArr[0][0]) + " kcal");
        joggingView.setText(String.format("%.1f", jogArr[0][0]) + " kcal");
        fastRunView.setText(String.format("%.1f", runArr[0][0]) + " kcal");
        cyclingView.setText(String.format("%.1f", cyclArr[0][0]) + " kcal");



        String[] answers = new String[]{
                "No physical activity",
                "Lightly active (sport 1-3 days/week)",
                "Moderately active (sport 3-5 days/week)",
                "Very active (sport 6-7 days/week)",
                "Extremely active (physical job or training twice a day)"
        };
        castSpinnerMap.put("No physical activity", 1.2d);
        castSpinnerMap.put("Lightly active (sport 1-3 days/week)", 1.375d);
        castSpinnerMap.put("Moderately active (sport 3-5 days/week)", 1.55d);
        castSpinnerMap.put("Very active (sport 6-7 days/week)", 1.725d);
        castSpinnerMap.put("Extremely active (physical job or training twice a day)", 1.9d);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity().getApplicationContext(), R.layout.spinner_row, answers);

        activitySpinner = v.findViewById(R.id.activity_spinner);
        activitySpinner.setAdapter(adapter);
        activitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calculateBMR();
                setViews();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        // Inflate the layout for this fragment
        return v;
    }


    private void calculateBMR(){
        bmr = (int)(10 * MainActivity.currentUser.weight + 6.25 * MainActivity.currentUser.height - 5 * MainActivity.currentUser.age - 5);
        caloriesLoss = (int)(castSpinnerMap.get(activitySpinner.getSelectedItem().toString()) * bmr - 500);
        caloriesMaintenance = (int)(bmr * castSpinnerMap.get(activitySpinner.getSelectedItem().toString()));
    }

    private void setViews(){
        bmrView.setText(String.valueOf(bmr));
        lossView.setText(String.valueOf(caloriesLoss) + " kcal");
        maintenanceView.setText(String.valueOf(caloriesMaintenance) + " kcal");
    }


    private void initializeTfLite(){
        try{
            AssetFileDescriptor assetFileDescriptor = getActivity().getAssets().openFd("calories_model.tflite");
            long startOff = assetFileDescriptor.getStartOffset();
            long length = assetFileDescriptor.getDeclaredLength();

            FileInputStream fileInputStream = new FileInputStream(assetFileDescriptor.getFileDescriptor());
            FileChannel fileChannel = fileInputStream.getChannel();
            tfLiteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOff, length);

        } catch (IOException e) {
            e.printStackTrace();
        }

        interpreter = new Interpreter(tfLiteBuffer);
    }


}