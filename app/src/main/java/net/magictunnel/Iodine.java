package net.magictunnel;


public class Iodine {

    public static native int iodineTest();

    static {
        System.loadLibrary("iodine");
    }
}
