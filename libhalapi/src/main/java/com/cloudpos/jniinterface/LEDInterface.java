package com.cloudpos.jniinterface;

public class LEDInterface {
	static{
		String fileName = "jni_cloudpos_led";
		JNILoad.jniLoad(fileName);
	}
	/**
     * Open the LED device.
     * @return value >= 0, success; value < 0, error code
     */
    public synchronized native static int open();
    /**
     * Close the LED device.
     * @return value >= 0, success; value < 0, error code
     */
    public native static int close();
    /**
     * Turn on the LED device.
     * @param index: index of LED, >=0 && <=3
     * @return value >= 0, success; value < 0, error code
     */
    public synchronized native static int turnOn(int index);
    /**
     * Turn off the LED device.
     * @param index: index of LED, >=0 && <=3
     * @return value >= 0, success; value < 0, error code
     */
    public synchronized native static int turnOff(int index);
    /**
     * get the status of led
     * @param index: index of LED, >=0 && <=3
     * @return value : == 0 : turn off
     *                 > 0 : turn on
     *                 < 0 : error code
     */
    public native static int getStatus(int index);

}
