package com.example.biosensordataanalyzer.Constants;

import java.util.UUID;

public class Consts {
    static final public String PULSE = "pulse";
    static final public String OXYGEN = "oxygen";

    static final public String SYSTOLIC = "systolic";
    static final public String DIASTOLIC = "diastolic";

    static final public UUID THE_SERVICE = UUID.fromString("000001ff-3c17-d293-8e48-14fe2e4da212");
    static final public UUID THE_NOTIFY_CHAR = UUID.fromString("0000ff03-0000-1000-8000-00805f9b34fb");
    static final public UUID THE_WRITE_CHAR = UUID.fromString("0000ff02-0000-1000-8000-00805f9b34fb");

    static final public byte[] openLiveDataStream = {(byte)-85, (byte)0, (byte)0, (byte)9, (byte)-63, (byte)123, (byte)0, (byte)64, (byte)5, (byte)0, (byte)6, (byte)0, (byte)4, (byte)0, (byte)1, (byte)5, (byte)2};
    static final public byte[] closeLiveDataStream = {(byte)-85, (byte)0, (byte)0, (byte)9, (byte)1, (byte)42, (byte)0, (byte)65, (byte)5, (byte)0, (byte)6, (byte)0, (byte)4, (byte)0, (byte)0, (byte)5, (byte)2};
    static final public byte[] ackLiveDataStream = {(byte)-85, (byte)16, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)-1};

    static final public String userFileName = "currentUser.txt";
}
