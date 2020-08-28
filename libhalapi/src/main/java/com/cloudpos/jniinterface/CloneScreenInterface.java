
package com.cloudpos.jniinterface;

public class CloneScreenInterface {

    static {
    	String fileName = "jni_cloudpos_clonescreen";
		JNILoad.jniLoad(fileName);
    }
    /**
     * Open the clone.
     * @return value >= 0, success; value < 0, error code
     */
    public synchronized native static int open();
    /**
     * Close the clone.
     * @return value >= 0, success; value < 0, error code
     */
    public native static int close();
    /**
     * Clone the screen.
     * @return value >= 0, success; value < 0, error code
     */
    public synchronized native static int show(int[] bitmap, int bitmapLength, int bitmapWidth, int bitmapHeight);
}
