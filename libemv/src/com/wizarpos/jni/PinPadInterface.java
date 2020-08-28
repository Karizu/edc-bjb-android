package com.wizarpos.jni;

public class PinPadInterface {
    /* native interface */
    static {
    	System.loadLibrary("wizarpos_pinpad");
    }


    /** key类型dukpt */
    public final static int KEY_TYPE_DUKPT = 0;
    /** key类型tdukpt */
    public final static int KEY_TYPE_TDUKPT = 1;
    /** key类型master */
    public final static int KEY_TYPE_MASTER = 2;
    /** key类型public */
    public final static int KEY_TYPE_PUBLIC = 3;
    /** key类型fix */
    public final static int KEY_TYPE_FIX = 4;

    /** master keyID 只有再key类型为master才有效 */
    public static final int[] MASTER_KEY_ID = new int[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09 };
    /** user keyID 只有再key类型为master才有效 */
    public static final int[] USER_KEY_ID = new int[] { 0x00, 0x01 };

    /** 加密算法 3DES */
    public static final int ALGORITH_3DES = 1;
    /** 加密算法 DES */
    public static final int ALGORITH_DES = 0;

    public final static int MAC_METHOD_X99 = 0;
    public final static int MAC_METHOD_ECB = 1;

    public native static int open();

    public native static int close();

    public native static int setText(int nLineIndex, byte arryText[], int nTextLength, int nFlagSound);

    public native static int setKey(int nKeyType, int nMasterKeyID, int nUserKeyID, int nAlgorith);

    public native static int setPinLength(int nLength, int nFlag);
    
    public native static int encrypt(byte arryPlainText[], int nTextLength, byte arryCipherTextBuffer[]);

    public native static int inputPIN(byte arryASCIICardNumber[], int nCardNumberLength, byte arryPinBlockBuffer[], int nTimeout_MS, int nFlagSound);

    /**
     * encrypt string using user key
     * @param arrayPlainText 加密数据
     * @param arrayCipherTextBuffer  储存密文数组
     * @param nMode  PINPAD_ENCRYPT_STRING_MODE_EBC  0
     *                    PINPAD_ENCRYPT_STRING_MODE_CBC    1
     *                    PINPAD_ENCRYPT_STRING_MODE_CFB    2
     *                    PINPAD_ENCRYPT_STRING_MODE_OFB    3
     * @param arrayIV
     * @param nIVLen
     * @return
     */
    public native static int encryptWithMode(byte[] arrayPlainText, byte[] arrayCipherTextBuffer, int nMode, byte[] arrayIV, int nIVLen);


    public native static int calculateMac(byte arryData[], int nDataLength, int nMACFlag, byte arryMACOutBuffer[]);

    public native static int updateUserKey(int nMasterKeyID, int nUserKeyID, byte arryCipherNewUserKey[], int nCipherNewUserKeyLength);
    
    public native static int updateMasterKey(int nMasterKeyID, byte arrayOldKey[], int nOldKeyLength, byte arrayNewKey[], int nNewKeyLength);

    public native static int getHwserialno(byte[] arrayResult);

    public native static int getMacForSnk(byte[] arrayUniqueSN, byte[] arrayRandom, byte[] arrayResult);

    /**
     * get Authorization Info from PINPAD
     * User can verify the signature with Pinpad CERT to ensure data returned is valid.
     *
     * @param authInfo fixed 4420 bytes
     *      -----------------------------------------------------------------------------------------------------------------------------------------------
     *      | 4     bytes, valid length of Pinpad CERT, can be converted to int (big endian)
     *      | 4096  bytes, Pinpad CERT buffer, format PEM
     *      | 32    bytes, random
     *      | 1     byte,  valid length of SN
     *      | 31    bytes, SN buffer
     *      | 256   bytes, signature, format SHA256withRSA (signature text: valid bytes of 'sn' and 'random', can be verified by Pinpad CERT)
     *      -----------------------------------------------------------------------------------------------------------------------------------------------
     * @return success(0) or error code(<0)
     */
    public native static int getAuthInfo(byte[] authInfo);

    /**
     * import key info to Driver
     * Before use this Method, User should get the certificate issued by vendor,
     * then getAuthInfo first and verify the signature and package the parameter tmkInfo as following format.
     *
     * @param tmkInfo fixed 4644 bytes
     *      -----------------------------------------------------------------------------------------------------------------------------------------------
     *      | 4     bytes, valid length of User's CERT, can be converted to int (big endian)
     *      | 4096  bytes, User's CERT buffer, format PEM
     *      | 32    bytes, random, should be equal with the random from 'getAuthInfo'
     *      | 256   bytes, cipher text of 'MasterKeyInfo' or 'DukptKeyInfo', encrypted by Pinpad public key
     *      |               MasterKeyInfo:
     *      |                       ---------------------------------------------------------------------------------------------------
     *      |                       | keyType(1 byte) | masterKeyID(1 byte) | keyLen(1 byte) | reserved(1 byte) | masterKey(24 bytes) |
     *      |                       ---------------------------------------------------------------------------------------------------
     *      |                       keyType:        2-masterKey 3-transmissionKey
     *      |                       masterKeyID:    index of PINPAD
     *      |                       keyLen:         valid length of master key
     *      |                       reserved:       unused
     *      |                       masterKey:      master key plaintext
     *      |
     *      |                DukptKeyInfo:
     *      |                       ---------------------------------------------------------------------------------------------------------------------
     *      |                       | keyType(1 byte) | dukptKeyID(1 byte) | reserved(2 bytes) | keySN(8 bytes) | counter(4 bytes) | dukptKey(16 bytes) |
     *      |                       ---------------------------------------------------------------------------------------------------------------------
     *      |                       keyType:        1-dukptKey
     *      |                       dukptKeyID:     index of PINPAD (0-2)
     *      |                       reverved:       byte1-usage of dukptKey(0-PIN, 1-MAC, 3-DATA), byte2 unused
     *      |                       keySN:          KSN of dukptKey
     *      |                       counter:        unused
     *      |                       dukptKey:       dukpt key plaintext
     *      | 256   bytes, signature, format SHA256withRSA (signature text: 'random' connect 'tmkEnc'), signed by user's private key
     *      -----------------------------------------------------------------------------------------------------------------------------------------------
     * @return success(0) or error code(<0)
    */
    public native static int importTMK(byte[] tmkInfo);


    /**
     * enable or disable bypass for pinpad
     * @param flag 0-disable bypass 1-allow bypass
     * @return >=0:success others:failed
    */
    public native static int setFlagAllowBypass(int flag);

    // 开启回调. (被回调的方法为  void pinpadCallback(byte[] v) - v[0]: 星号个数; v[1]: 额外参数
    // >=0 : success;
    // -1: has not find lib
    // -2: has not find pinpad_set_pinblock_callback in lib
    // -3: has not find PinpadCallback in Java code
    public native static int setPinblockCallback();

    private static PinPadCallbackHandler callbackHandler;
    public static int setupCallbackHandler(PinPadCallbackHandler handler) {
        callbackHandler = handler;
        if (handler != null) {
            return setPinblockCallback();
        }
        return 0;
    }
    public static void pinpadCallback(byte[] data) {
        if (callbackHandler != null) {
            callbackHandler.processCallback(data);
        }
    }

}
