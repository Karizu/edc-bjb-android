package com.cloudpos.jniinterface;

public class IDCardInterface {
	static{
		String fileName = "jni_cloudpos_idcard";
		JNILoad.jniLoad(fileName);
	}
	/**
     * Open the ID device.
     * @return value >= 0, success; value < 0, error code
     */
	public synchronized native static int open();
	/**
     * Close the ID device.
     * @return value >= 0, success; value < 0, error code
     */
	public native static int close();
	/**
     * Get the ID card information.
     * @param data: ID card property data
     * @return value >= 0, success; value < 0, error code
     */
	public native static int getInformation(IDCardProperty data);
	/**
     * Begin to search IDCard. This api will return until find a IDCard.
     * @return value >= 0, success; value < 0, error code
     */
	public synchronized native static int searchTarget();

}
