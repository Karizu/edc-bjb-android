
package com.cloudpos.jniinterface;

public class FingerPrintInterface {
    static {
    	String fileName = "jni_cloudpos_fingerprint";
		JNILoad.jniLoad(fileName);
    }
    /**
     * Open the fingerprint device.
     * @return value >= 0, success; value < 0, error code
     */
    public synchronized native static int open();
    /**
     * Close the fingerprint device.
     * @return value >= 0, success; value < 0, error code
     */
    public native static int close();
    /**
     * Get the feature of fingerprint.
     * @param arryFea:        The buffer to store the feature, not null
     * @param nFeaLength:     The length of the buffer
     * @param pRealFeaLength: Reture the real length of the buffer
     * @param n_TimeOut_S:    Timeout, unit of time:s
     * @return value >= 0, success; value < 0, error code
     */
    public synchronized native static int getFea(byte[] arryFea, int nFeaLength, int[] pRealFeaLength,
            int n_TimeOut_S);
    /**
     * Get the image of fingerprint.
     * @param pImgBuffer: The image buffer
     * @param nImgLength: The length of image buffer
     * @param pRealImaLength: The real length of the image buffer
     * @param pImgWidth: The width of the image
     * @param pImgHeight: The height of the image
     * @return value >= 0, success; value < 0, error code
     */
    public synchronized native static int getLastImage(byte[] pImgBuffer, int nImgLength, int[] pRealImaLength,
            int[] pImgWidth, int[] pImgHeight);
    /**
     * Match the fingerprint.
     * @param pFeaBuffer1: The feature of the old fingerprint
     * @param nFea1Length: The length of the feature
     * @param pFealBuffer2: The feature of the new fingerprint
     * @param nFea2Length: The length of the feature
     * @return value > 0, success; value <= 0, error code
     */
    public synchronized native static int match(byte[] pFeaBuffer1, int nFea1Length, byte[] pFealBuffer2,
            int nFea2Length);
    /**
     * Cancel the fingerprint device.
     * @return value >= 0, success; value < 0, error code
     */
    public native static int cancel();

}
