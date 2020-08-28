package com.cloudpos.jniinterface;

public class BatteryInterface {
    static {
    	String fileName = "jni_cloudpos_battery";
        JNILoad.jniLoad(fileName);
    }
    /**
     * Open the battery device.
     * @return value >= 0, success; value < 0, error code
     */
    public synchronized native static int open();
    /**
     * Close the battery device.
     * @return value >= 0, success; value < 0, error code
     */
    public native static int close();
    /**
     * Get the capacity and voltage of the battery device.
     * @param capacity: battery capacity data buffer
     * @param voltage: battery voltage data buffer
     * @return value >= 0, success; value < 0, error code
     */
    public native static int queryInfo(int[] capacity, int[] voltage);
}
