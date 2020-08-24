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

public class CurrentUser implements Serializable {
    public String name;
    public int age;
    public int weight;
    public int height;

    public void save(Context context) throws IOException {
        FileOutputStream fos = context.openFileOutput(Consts.userFileName, Context.MODE_PRIVATE);
        ObjectOutputStream os = new ObjectOutputStream(fos);
        os.writeObject(this);
        os.close();
        fos.close();
    }

    public void load(Context context) throws IOException, ClassNotFoundException {
        FileInputStream fis = context.openFileInput(Consts.userFileName);
        ObjectInputStream is = new ObjectInputStream(fis);
        MainActivity.currentUser = (CurrentUser) is.readObject();
        is.close();
        fis.close();
    }
}
