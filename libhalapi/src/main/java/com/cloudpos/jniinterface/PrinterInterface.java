package com.cloudpos.jniinterface;

public class PrinterInterface 
{
	static {
		String fileName = "jni_cloudpos_printer";
		JNILoad.jniLoad(fileName);
	}
	/**
	 * open the device.
	 * @return value  >= 0, success in starting the process; value < 0, error code
	 * */
	public synchronized native static int open();
	/**
	 * close the device.
	 * @return value  >= 0, success in starting the process; value < 0, error code
	 * */
	
	public native static int close();
	/**
	 * prepare to print.
	 * @return value  >= 0, success in starting the process; value < 0, error code
	 * */
	
	public synchronized native static int begin();
	/** end to print.
	 *  @return value  >= 0, success in starting the process; value < 0, error code
	 * */
	
	public synchronized native static int end();
	/**
	 * write the data to the device.
	 * @param arryData : data or control command
	 * @param nDataLength : length of data or control command
	 * @return value  >= 0, success in starting the process; value < 0, error code
	 * */
	
	public synchronized native static int write(byte arryData[], int nDataLength);
	
	/**
	 * read the data from the device.
	 * @param arryData : byte to save result
	 * @param nDataLength : expect length of data
	 * @param nTimeout : time out
	 * @return value : 0 : success   < 0 : error code
	 * */
	public synchronized native static int read(byte arryData[], int nDataLength, int nTimeout);
	
	/**
	 * write the data to the device.
	 * @param arryData : data or control command
	 * @param offset : offset for data.
	 * @param nDataLength : length of data or control command
	 * @return value  >= 0, success in starting the process; value < 0, error code
	 * */
	public synchronized native static int write(byte arryData[], int offset, int nDataLength);

	/**
	 * query the status of printer.
	 * return value : < 0 : error code
	 *                = 0 : no paper
	 *                = 1 : has paper
	 *                other value : RFU
	 */
	public synchronized native static int queryStatus();
	/**
	 * query the capacity and voltage.
	 * return value : < 0 : error code
	 *                >= 0 : success
	 */
	public synchronized native static int queryVoltage(int[] pCapacity, int[] pVoltage);
	
	public synchronized native static boolean isOpened();
	
}
