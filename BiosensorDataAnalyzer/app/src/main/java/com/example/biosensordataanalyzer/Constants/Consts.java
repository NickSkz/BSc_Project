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
    static final public String BATTERY = "battery";

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

    static final public byte[] getBatteryLevel = {(byte)-85, (byte)0, (byte)0, (byte)5, (byte)196, (byte)57, (byte)0, (byte)9, (byte)2, (byte)0, (byte)20, (byte)0, (byte)0};

    static final public byte[] calibrateBand = {(byte)-85, (byte)0, (byte)0, (byte)9, (byte)151, (byte)104, (byte)0, (byte)8, (byte)2, (byte)0, (byte)1, (byte)0, (byte)4, (byte)82, (byte)100, (byte)0, (byte)193}; //LAST 4 = DATUM
    static final public int[] crc16_table = {0, 49345, 49537, 320, 49921, 960, 640, 49729, 50689, 1728, 1920, 51009, 1280, 50625, 50305, 1088, 52225, 3264, 3456, 52545, 3840, 53185, 52865, 3648, 2560, 51905, 52097, 2880, 51457, 2496, 2176, 51265, 55297, 6336, 6528, 55617, 6912, 56257, 55937, 6720, 7680, 57025, 57217, 8000, 56577, 7616, 7296, 56385, 5120, 54465, 54657, 5440, 55041, 6080, 5760, 54849, 53761, 4800, 4992, 54081, 4352, 53697, 53377, 4160, 61441, 12480, 12672, 61761, 13056, 62401, 62081, 12864, 13824, 63169, 63361, 14144, 62721, 13760, 13440, 62529, 15360, 64705, 64897, 15680, 65281, 16320, 16000, 65089, 64001, 15040, 15232, 64321, 14592, 63937, 63617, 14400, 10240, 59585, 59777, 10560, 60161, 11200, 10880, 59969, 60929, 11968, 12160, 61249, 11520, 60865, 60545, 11328, 58369, 9408, 9600, 58689, 9984, 59329, 59009, 9792, 8704, 58049, 58241, 9024, 57601, 8640, 8320, 57409, 40961, 24768, 24960, 41281, 25344, 41921, 41601, 25152, 26112, 42689, 42881, 26432, 42241, 26048, 25728, 42049, 27648, 44225, 44417, 27968, 44801, 28608, 28288, 44609, 43521, 27328, 27520, 43841, 26880, 43457, 43137, 26688, 30720, 47297, 47489, 31040, 47873, 31680, 31360, 47681, 48641, 32448, 32640, 48961, 32000, 48577, 48257, 31808, 46081, 29888, 30080, 46401, 30464, 47041, 46721, 30272, 29184, 45761, 45953, 29504, 45313, 29120, 28800, 45121, 20480, 37057, 37249, 20800, 37633, 21440, 21120, 37441, 38401, 22208, 22400, 38721, 21760, 38337, 38017, 21568, 39937, 23744, 23936, 40257, 24320, 40897, 40577, 24128, 23040, 39617, 39809, 23360, 39169, 22976, 22656, 38977, 34817, 18624, 18816, 35137, 19200, 35777, 35457, 19008, 19968, 36545, 36737, 20288, 36097, 19904, 19584, 35905, 17408, 33985, 34177, 17728, 34561, 18368, 18048, 34369, 33281, 17088, 17280, 33601, 16640, 33217, 32897, 16448};
    static final public byte[] zeroOutStaticData = {(byte)-85, (byte)0, (byte)0, (byte)9, (byte)151, (byte)104, (byte)0, (byte)8, (byte)2, (byte)0, (byte)1, (byte)0, (byte)4, (byte)82, (byte)100, (byte)0, (byte)193};

    // Name of text file where we store all user's data
    static final public String userFileName = "currentUser.txt";
    static final public String lastDaysFileName = "lastBDays.txt";
}
