package com.cloudpos.jniinterface;

public class SecondaryDisplayInterface {

	static {
		String fileName = "jni_cloudpos_secondarydisplay";
		JNILoad.jniLoad(fileName);
	}

	/**
     * Open device.
     * @return value >= 0 : success 
     *                 < 0 : error code
     */
    public synchronized native static int open();

    /**
     * write picture point data (one point 4 bytes ARGB)
     * 
     * @param nXcoordinate: X cordinate.
     * @param nYcoordinate: Y cordinate.
     * @param nWidth: Width.
     * @param nHeight: Height.
     * @param pData : all point data.
     * @param nDataLength : point data length. 
     * @return value >=0 : success 
     *                 < 0 : error code
     */
    public synchronized native static int writePicture(int nXcoordinate, int nYcoordinate,int nWidth, int nHeight, byte[] pData, int nDataLength);

    /**
     * set background color.
     * @param nColor : 
     * <ul>
     * <li>RED :0x001F</li>
     * <li>BLACK:0X0000</li>
     * <li>YELLOW:0X07FF</li>
     * <li>BLUE:0XF800</li>
     * <li>GRAY0:0XCE9A</li>
     * </ul>              
     * @return value >= 0 : success 
     *                 < 0 : error code
     * */
    public native static int setBackground(int nColor);
    /**
     * Buzzer beep.
     * @return value >= 0 : success 
     *                 < 0 : error code
     * */
    public native static int buzzerBeep();
    /**
     * Power led.
     * @param nValue : 1:open led. 0:close led.
     * @return value >= 0 : success 
     *                 < 0 : error code
     * */
    public native static int ledPower(int nValue);
    /**
     * Default screen.
     * @return value >= 0 : success 
     *                 < 0 : error code
     * */
    public native static int displayDefaultScreen();

    /**
     * Close the device.
     * 
     * @return value >= 0 : success 
     *                 < 0 : error code
     */
    public native static int close();

}
