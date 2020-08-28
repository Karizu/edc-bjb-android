
package com.cloudpos.jniinterface;

public class IsoFingerPrintInterface {
    static {
        String fileName = "jni_cloudpos_iso_fingerprint";
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
     * Cancel the fingerprint device.
     * @return value >= 0, success; value < 0, error code
     */
    public native static int cancel();

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
     * Enroll fingerprint.
     * @param userID: user id, set to -1, the value will genrated by the driver.(1 <= user id <= 100 )
     * @param timeout: time out, unit is ms
     * @return value > 0, success, the id of the stored feature; value <= 0, error code
     */
    public synchronized native static int enroll(int userID, int timeout);
    /**
     * Verify the active fingerprint.
     * @param timeout: time out, unit is ms
     * @return value > 0, success; value <= 0, error code
     */
    public synchronized native static int verifyAll(int timeout);
    /**
     * Delete all fingerprints.
     * @return value > 0, success; value <= 0, error code
     */
    public synchronized native static int delAllFingers();
    /**
     * Delete one fingerprint.
     * @param userID: user id
     * @return value > 0, success; value <= 0, error code
     */
    public synchronized native static int delFinger(int userID);
    /**
     * Verify the active fingerprint against the  the specified fingerprint.
     * @param userID: user id
     * @param timeout: time out, unit is ms
     * @return value > 0, success; value <= 0, error code
     */
    public synchronized native static int verifyAgainstUserId(int userID, int timeout);
    /**
     * Verify the active fingerprint against the  the specified fingerprint feature.
     * @param pFeaBuffer: The feature of the specified fingerprint
     * @param timeout: time out, unit is ms
     * @return value > 0, success; value <= 0, error code
     */
    public synchronized native static int verifyAgainstFeature(byte[] pFeaBuffer, int timeout);
    /**
     * List all the fingerprints's status.
     * @param fingerExist: returned the exist id of the fingerprint
     * @return value > 0, success; value <= 0, error code
     */
    public synchronized native static int listAllFingersStatus(int[] fingerExist);
    /**
     * Get the specified fingerprint.
     * @param userID: user id
     * @param pFeaBuffer1: The returned feature of the fingerprint
     * @param type: fingerprint type, 0: Defautl, 1:ISO2005, 2:ISO2015
     * @return value > 0, success; value <= 0, error code
     */
    public synchronized native static int getUserFeature(int userID, byte[] pFeaBuffer1, int type);
    /**
     * Store the fingerprint.
     * @param userID: user id, set to -1, the value will genrated by the driver.
     * @param pFeaBuffer: The feature of the fingerprint
     * @return value > 0, success, the id of the stored feature; value <= 0, error code
     */
    public synchronized native static int storeFeature(int userID, byte[] pFeaBuffer);
    /**
     * Get the active fingerprint feature, specified the type of the fingerprint.
     * @param arryFea: The returned feature of the fingerprint
     * @param timeout: time out, unit is ms
     * @param type: fingerprint type, 0: Defautl, 1:ISO2005, 2:ISO2015
     * @return value > 0, success; value <= 0, error code
     */
    public synchronized native static int getFeaExt(byte[] arryFea, int timeout, int type);
    /**
     * Get the active fingerprint image buffer, specified the type of the fingerprint.
     * @param pImgBuffer: The returned image buffer of the fingerprint
     * @param pImgWidth: The width of the image
     * @param pImgHeight: The height of the image
     * @param type: fingerprint type, 0: Defautl, 1:ISO2005, 2:ISO2015
     * @return value > 0, success; value <= 0, error code
     */
    public synchronized native static int getImage(byte[] pImgBuffer, int pImgWidth, int pImgHeight, int type);
    /**
     * Convert fingerprint from one type to another.
     * @param feaIn: The feature of the fingerprint before convert
     * @param typeIn: The type of the fingerprint before convert,  0: Defautl, 1:ISO2005, 2:ISO2015
     * @param feaOut: The feature of the fingerprint after convert
     * @param typeOut: The type of the fingerprint after convert,  0: Defautl, 1:ISO2005, 2:ISO2015
     * @return value > 0, success; value <= 0, error code
     */
    public synchronized native static int convertFormat(byte[] feaIn, int typeIn, byte[] feaOut, int typeOut);
    /**
     * Get the id of fingerprint.
     * @return value = 0 is crossmatch, value =2 is FP_TUZHENG_BIG, value<0 error code
     */
    public synchronized native static int getId();
    /**
     * is open fingerprinrer device.
     *
     * */
    public native static boolean isOpened();
}
