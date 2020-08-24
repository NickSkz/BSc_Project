package com.example.biosensordataanalyzer.User;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.biosensordataanalyzer.Main.MainActivity;
import com.example.biosensordataanalyzer.R;

public class EditUserActivity extends AppCompatActivity {

    private Button saveBtn;

    private EditText enterName;
    private EditText enterAge;
    private EditText enterHeight;
    private EditText enterWeight;

    private TextView currentName;
    private TextView currentAge;
    private TextView currentHeight;
    private TextView currentWeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);

        enterName = findViewById(R.id.editTextName);
        enterAge = findViewById(R.id.editTextAge);
        enterHeight = findViewById(R.id.editTextHeight);
        enterWeight = findViewById(R.id.editTextWeight);


        currentName = findViewById(R.id.curr_name_txt);
        currentName.setText(MainActivity.currentUser.name);

        currentAge = findViewById(R.id.curr_age_txt);
        currentAge.setText(String.valueOf(MainActivity.currentUser.age));

        currentHeight = findViewById(R.id.curr_height_txt);
        currentHeight.setText(String.valueOf(MainActivity.currentUser.height));

        currentWeight = findViewById(R.id.curr_weight_txt);
        currentWeight.setText(String.valueOf(MainActivity.currentUser.weight));


        saveBtn = findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(view -> {
            MainActivity.currentUser.name = enterName.getText().toString();
            MainActivity.currentUser.age = Integer.parseInt(enterAge.getText().toString());
            MainActivity.currentUser.height = Integer.parseInt(enterHeight.getText().toString());
            MainActivity.currentUser.weight = Integer.parseInt(enterWeight.getText().toString());

            try{
                MainActivity.currentUser.save(getApplicationContext());
            }
            catch(Exception e){
                e.printStackTrace();
            }

            startActivity(new Intent(EditUserActivity.this, MainActivity.class));
        });
    }


}