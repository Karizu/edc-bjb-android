package com.cloudpos.jniinterface;

public class CashDrawerInterface {
	static {
		String fileName = "jni_cloudpos_cashdrawer";
		JNILoad.jniLoad(fileName);
	}
	/**
	 * Open the money box device
	 * @return value : < 0 : error code
	 * 				   >= 0 : success;	
	 */
	public synchronized native static int open();
	/**
	 * Close the money box device
	 * @return value : < 0 : error code
	 * 				   >= 0 : success;
	 */
	
	public synchronized native static int close();
	/**
	 * Kick out money box
	 * @return value : < 0 : error code;
	 *                 >= 0 : success
	 */
	public synchronized native static int kickOut();
}
