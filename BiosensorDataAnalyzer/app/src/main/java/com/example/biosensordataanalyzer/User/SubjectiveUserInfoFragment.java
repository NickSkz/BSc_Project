package com.example.biosensordataanalyzer.User;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.biosensordataanalyzer.Main.MainActivity;
import com.example.biosensordataanalyzer.R;

import org.w3c.dom.Text;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SubjectiveUserInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SubjectiveUserInfoFragment extends Fragment implements UserFragmentable {

    private View view;

    //Spinners responsible for certain information
    private Spinner cigSpinner;
    private Spinner alcSpinner;
    private Spinner spoSpinner;

    private TextView currentCig;
    private TextView currentAlc;
    private TextView currentSpo;

    private Button saveBtn;
    //HashMap used for casting data from spinner
    private HashMap<String, Integer> castSpinnerMap;

    String[] answers;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_subjective_user_info, container, false);

        castSpinnerMap = new HashMap<>();

        answers = new String[]{"No", "Yes"};
        castSpinnerMap.put("No", 0);
        castSpinnerMap.put("Yes", 1);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity().getApplicationContext(), R.layout.spinner_row, answers);

        cigSpinner = view.findViewById(R.id.cig_spinner);
        cigSpinner.setAdapter(adapter);

        alcSpinner = view.findViewById(R.id.alc_spinner);
        alcSpinner.setAdapter(adapter);

        spoSpinner = view.findViewById(R.id.spo_spinner);
        spoSpinner.setAdapter(adapter);



        currentCig = view.findViewById(R.id.curr_cig_txt);
        currentAlc = view.findViewById(R.id.curr_alc_txt);
        currentSpo = view.findViewById(R.id.curr_spo_txt);
        setCurrentTexts();


        saveBtn = view.findViewById(R.id.save_sub_btn);
        saveBtn.setOnClickListener(view -> {
            if (!cigSpinner.getSelectedItem().toString().equals("")){
                MainActivity.currentUser.cigaretten = castSpinnerMap.get(cigSpinner.getSelectedItem().toString());
            }
            if (!alcSpinner.getSelectedItem().toString().equals("")) {
                MainActivity.currentUser.alcohol = castSpinnerMap.get(alcSpinner.getSelectedItem().toString());
            }
            if (!spoSpinner.getSelectedItem().toString().equals("")) {
                MainActivity.currentUser.sport = castSpinnerMap.get(spoSpinner.getSelectedItem().toString());
            }

            try{
                MainActivity.currentUser.save(getActivity().getApplicationContext());
            }
            catch(Exception e){
                e.printStackTrace();
            }

            setCurrentTexts();
        });


        return view;
    }


    @Override
    public void setCurrentTexts(){
        currentCig.setText(String.valueOf(answers[MainActivity.currentUser.cigaretten]));
        currentAlc.setText(String.valueOf(answers[MainActivity.currentUser.alcohol]));
        currentSpo.setText(String.valueOf(answers[MainActivity.currentUser.sport]));
    }



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SubjectiveUserInfoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SubjectiveUserInfoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SubjectiveUserInfoFragment newInstance(String param1, String param2) {
        SubjectiveUserInfoFragment fragment = new SubjectiveUserInfoFragment();
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