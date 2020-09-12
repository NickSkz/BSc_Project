package com.example.biosensordataanalyzer.User;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.biosensordataanalyzer.Main.MainActivity;
import com.example.biosensordataanalyzer.R;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ExaminatedUserInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExaminatedUserInfoFragment extends Fragment {

    private Spinner choSpinner;
    private Spinner gluSpinner;

    private TextView currentCho;
    private TextView currentGlu;

    private Button saveBtn;
    private HashMap<String, Integer> castSpinnerMap;

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_examinated_user_info, container, false);

        castSpinnerMap = new HashMap<>();

        String[] answers = new String[]{"Normal", "Above Normal", "High above normal"};
        castSpinnerMap.put("Normal", 1);
        castSpinnerMap.put("Above Normal", 2);
        castSpinnerMap.put("High above normal", 3);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity().getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, answers);

        choSpinner = view.findViewById(R.id.cho_spinner);
        choSpinner.setAdapter(adapter);

        gluSpinner = view.findViewById(R.id.glu_spinner);
        gluSpinner.setAdapter(adapter);


        currentCho = view.findViewById(R.id.curr_cho_txt);
        currentGlu = view.findViewById(R.id.curr_glu_txt);
        setCurrentTexts();


        saveBtn = view.findViewById(R.id.save_exa_btn);
        saveBtn.setOnClickListener(view -> {
            MainActivity.currentUser.cholesterol = castSpinnerMap.get(choSpinner.getSelectedItem().toString());
            MainActivity.currentUser.glucose = castSpinnerMap.get(gluSpinner.getSelectedItem().toString());


            try{
                MainActivity.currentUser.save(getActivity().getApplicationContext());
            }
            catch(Exception e){
                e.printStackTrace();
            }

            setCurrentTexts();
            //startActivity(new Intent(EditUserActivity.this, MainActivity.class));
        });


        return view;
    }


    private void setCurrentTexts(){
        currentCho.setText(String.valueOf(MainActivity.currentUser.cholesterol));
        currentGlu.setText(String.valueOf(MainActivity.currentUser.glucose));
    }


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ExaminatedUserInfoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ExaminatedUserInfoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ExaminatedUserInfoFragment newInstance(String param1, String param2) {
        ExaminatedUserInfoFragment fragment = new ExaminatedUserInfoFragment();
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
    }
    
}