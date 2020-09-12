package com.example.biosensordataanalyzer.User;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.biosensordataanalyzer.Main.MainActivity;
import com.example.biosensordataanalyzer.R;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ObjectiveUserInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ObjectiveUserInfoFragment extends Fragment {

    /*
     * Declare all graphical stuff
     */
    private Button saveBtn;

    private EditText enterName;
    private EditText enterAge;
    private EditText enterHeight;
    private EditText enterWeight;
    private Spinner sexSpinner;

    private TextView currentName;
    private TextView currentSex;
    private TextView currentAge;
    private TextView currentHeight;
    private TextView currentWeight;

    private HashMap<String, Integer> castSpinnerMap;

    private View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_objective_user_info, container, false);

        castSpinnerMap = new HashMap<>();

        /*
         * Initialize graphical components
         */
        enterName = view.findViewById(R.id.editTextName);
        enterAge = view.findViewById(R.id.editTextAge);
        enterHeight = view.findViewById(R.id.editTextHeight);
        enterWeight = view.findViewById(R.id.editTextWeight);

        sexSpinner = view.findViewById(R.id.spinner);
        String[] sexes = new String[]{"Female", "Male"};
        castSpinnerMap.put("Female", 1);
        castSpinnerMap.put("Male", 2);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity().getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, sexes);
        sexSpinner.setAdapter(adapter);


        /*
         * Display actual data of a user
         */
        currentName = view.findViewById(R.id.curr_name_txt);
        currentAge = view.findViewById(R.id.curr_age_txt);
        currentHeight = view.findViewById(R.id.curr_height_txt);
        currentWeight = view.findViewById(R.id.curr_weight_txt);
        currentSex = view.findViewById(R.id.curr_sex_txt);
        setCurrentTexts();

        /*
         * If someone changes info, change them to the current data, save 'em to the file, get back to the main activity
         */
        saveBtn = view.findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(view -> {
            MainActivity.currentUser.name = enterName.getText().toString();
            MainActivity.currentUser.age = Integer.parseInt(enterAge.getText().toString());
            MainActivity.currentUser.height = Integer.parseInt(enterHeight.getText().toString());
            MainActivity.currentUser.weight = Integer.parseInt(enterWeight.getText().toString());
            MainActivity.currentUser.sex = castSpinnerMap.get(sexSpinner.getSelectedItem().toString());


            try{
                MainActivity.currentUser.save(getActivity().getApplicationContext());
            }
            catch(Exception e){
                e.printStackTrace();
            }

            setCurrentTexts();
            //startActivity(new Intent(EditUserActivity.this, MainActivity.class));
        });

        // Inflate the layout for this fragment
        return view;
    }

    private void setCurrentTexts(){
        currentName.setText(MainActivity.currentUser.name);
        currentAge.setText(String.valueOf(MainActivity.currentUser.age));
        currentHeight.setText(String.valueOf(MainActivity.currentUser.height));
        currentWeight.setText(String.valueOf(MainActivity.currentUser.weight));
        currentSex.setText(String.valueOf(MainActivity.currentUser.sex));
    }


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ObjectiveUserInfoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ObjectiveUserInfoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ObjectiveUserInfoFragment newInstance(String param1, String param2) {
        ObjectiveUserInfoFragment fragment = new ObjectiveUserInfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

}