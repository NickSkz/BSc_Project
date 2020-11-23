package com.example.biosensordataanalyzer.User;


import android.content.Context;

import com.example.biosensordataanalyzer.Constants.Consts;
import com.example.biosensordataanalyzer.Main.MainActivity;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

// Class that stores information about current user
public class CurrentUser implements Serializable {
    /*
     * Declare features
     */
    public String name;
    public int sex; //1 - Women, 2 - Men
    public int age;
    public int weight;
    public int height;

    public int cigaretten; //0 - No 1 - Yes
    public int alcohol; //0 - No 1 - Yes
    public int sport; //0 - No 1 - Yes

    public int cholesterol; //1 - Normal, 2 - Above Normal, 3 - Well Above Normal
    public int glucose; //1 - Normal, 2 - Above Normal, 3 - Well Above Normal

    public float[][] inputPressureHDArr = {{20228, 1, 156, 85.0f, 0, 0, 3, 1, 0, 0, 1}};
    public float[][] outputPressureHDArr = {{0.0f}};

    // Save data to a file (Serialize object)
    public void save(Context context) throws IOException {
        FileOutputStream fileOutputStream = context.openFileOutput(Consts.userFileName, Context.MODE_PRIVATE);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(this);
        objectOutputStream.close();
        fileOutputStream.close();
    }

    // Load data from file (Deserialize object)
    public void load(Context context) throws IOException, ClassNotFoundException {
        FileInputStream fileInputStream = context.openFileInput(Consts.userFileName);
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
        MainActivity.currentUser = (CurrentUser) objectInputStream.readObject();
        objectInputStream.close();
        fileInputStream.close();
    }

    public void prepareNeuralPressureArray(){
        inputPressureHDArr[0][0] = age * 365;
        inputPressureHDArr[0][1] = sex;
        inputPressureHDArr[0][2] = height;
        inputPressureHDArr[0][3] = weight;
        inputPressureHDArr[0][4] = 0;
        inputPressureHDArr[0][5] = 0;
        inputPressureHDArr[0][6] = cholesterol;
        inputPressureHDArr[0][7] = glucose;
        inputPressureHDArr[0][8] = cigaretten;
        inputPressureHDArr[0][9] = alcohol;
        inputPressureHDArr[0][10] = sport;
    }
}
