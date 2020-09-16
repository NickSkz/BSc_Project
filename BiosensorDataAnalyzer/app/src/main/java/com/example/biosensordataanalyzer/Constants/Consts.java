package com.example.biosensordataanalyzer.Constants;

import java.util.UUID;

// Class of final variables
public class Consts {
    /*
     * Variables used for Broadcast Recievers in MeasurementsActivities
     */
    static final public String PULSE = "pulse";
    static final public String OXYGEN = "oxygen";
    static final public String STEPS = "steps";
    static final public String CALORIES = "calories";
    static final public String DISTANCE = "distance";

    static final public String SYSTOLIC = "systolic";
    static final public String DIASTOLIC = "diastolic";

    /*
     * Key characteristics from Bluetooth device
     */
    static final public UUID THE_SERVICE = UUID.fromString("000001ff-3c17-d293-8e48-14fe2e4da212");
    static final public UUID THE_NOTIFY_CHAR = UUID.fromString("0000ff03-0000-1000-8000-00805f9b34fb");
    static final public UUID THE_WRITE_CHAR = UUID.fromString("0000ff02-0000-1000-8000-00805f9b34fb");

    /*
     * Bytes that are sent to start/end measure + ack signal to request for another array of data from smartband
     */
    static final public byte[] openLiveDataStream = {(byte)-85, (byte)0, (byte)0, (byte)9, (byte)-63, (byte)123, (byte)0, (byte)64, (byte)5, (byte)0, (byte)6, (byte)0, (byte)4, (byte)0, (byte)1, (byte)5, (byte)2};
    static final public byte[] closeLiveDataStream = {(byte)-85, (byte)0, (byte)0, (byte)9, (byte)1, (byte)42, (byte)0, (byte)65, (byte)5, (byte)0, (byte)6, (byte)0, (byte)4, (byte)0, (byte)0, (byte)5, (byte)2};
    static final public byte[] ackLiveDataStream = {(byte)-85, (byte)16, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)-1};

    static final public byte[] getStaticData = {(byte)-85, (byte)0, (byte)0, (byte)5, (byte)10, (byte)156, (byte)0, (byte)24, (byte)5, (byte)0, (byte)33, (byte)0, (byte)0};

    // Name of text file where we store all user's data
    static final public String userFileName = "currentUser.txt";
}
