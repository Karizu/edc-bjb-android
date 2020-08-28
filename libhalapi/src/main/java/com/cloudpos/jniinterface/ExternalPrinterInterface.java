package com.cloudpos.jniinterface;

public class ExternalPrinterInterface {
    static {
        String fileName = "jni_cloudpos_externalprinter";
        JNILoad.jniLoad(fileName);
    }
    /**
     * open the device
     * @param entity
     * @return value < 0 : error code, value >= 0: success, return value is a
     *         handle.This handle will be employed bye other API as an input
     *         parameter
     * */
    public synchronized native static int open(String entity);
    /**
     * close the device
     * @param handle : returned from method of open.
     * @return value  >= 0, success in starting the process; value < 0, error code
     * */
    
    public native static int close(int handle);
    /**
     * prepare to print
     * @param handle : returned from method of open.
     * @return value  >= 0, success in starting the process; value < 0, error code
     * */
    
    public synchronized native static int begin(int handle);
    /** end to print
     * @param handle : returned from method of open.
     *  @return value  >= 0, success in starting the process; value < 0, error code
     * */
    
    public synchronized native static int end(int handle);
    /**
     * write the data to the device
     * @param handle : returned from method of open.
     * @param arryData : data or control command
     * @param nDataLength : length of data or control command
     * @return value  >= 0, success in starting the process; value < 0, error code
     * */
    
    public synchronized native static int write(int handle, byte arryData[], int nDataLength);
    
    /**
     * read the data from the device
     * @param handle : returned from method of open.
     * @param arryData : byte to save result
     * @param nDataLength : expect length of data
     * @param nTimeout : time out
     * @return value : 0 : success   < 0 : error code
     * */
    public synchronized native static int read(int handle, byte arryData[], int nDataLength, int nTimeout);
    
    /**
     * query the status of printer
     * 
     * @param handle : returned from method of open.
     * return value : < 0 : error code
     *                = 0 : no paper
     *                = 1 : has paper
     *                other value : RFU
     */
    public synchronized native static int queryStatus(int handle);
    /**
     * query the capacity and voltage
     * @param handle : returned from method of open.
     * return value : < 0 : error code
     *                >= 0 : success
     */
    public synchronized native static int queryVoltage(int handle, int[] pCapacity, int[] pVoltage);
}
