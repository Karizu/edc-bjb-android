
package com.cloudpos.jniinterface;

public class SerialPortInterface
{
    static
    {
    	String fileName = "jni_cloudpos_serial";
		JNILoad.jniLoad(fileName);
    }

    /**
     * Open the serial port by the specified device name.
     * @param deviceName in WizarPOS1, deviceName is DB9, in WizarHANDQ1, deviceName is WIZARHANDQ1.
     * @return value >= 0, success in starting the process; value < 0, error code
     */
    public synchronized native static int open(String deviceName);
    
    /**
     * Close the serial port opened before.
     * 
     * @return value >= 0, success in starting the process; value < 0, error code
     */
    public native static int close();

    /**
     * Get information from the serial port.
     * @param pDataBuffer Data buffer
     * @param nExpectedDataLength Data length to read
     * @param nTimeout_MS Time in milliseconds. <0: read until got data.
     * 
     * @return value >0, data length; value < 0, error code
     */
    public synchronized native static int read(byte pDataBuffer[], int nExpectedDataLength, int nTimeout_MS);

    /**
     * Send information from the serial port.
     * @param pDataBuffer Data buffer
     * @param offset offset of the data buffer
     * @param nDataLength data length of write
     * 
     * @return value >=0, write data length; value < 0, error code
     */
    public synchronized native static int write(byte pDataBuffer[], int offset, int nDataLength);

    /**
     * Set the baud rate of the serial port so that this device can read and write in the same baud rate.
     * @param nBaudrate baud rate     
     * 
     * @return value >=0, write data length; value < 0, error code
     */
    public synchronized native static int setBaudrate(int nBaudrate);

    /**
     * Flush the IO buffer of the serial port.
     * 
     * @return value >=0, success; value < 0, error code
     */
    public synchronized native static int flushIO();
    
    /**
     * Recognize if the serial port is open.
     * 
     * @return true is opened
     */    
    public synchronized native static boolean isOpened(); 
}
