
package com.cloudpos.jniinterface;

public class PINPadInterface {
    /* native interface */
    static {
        String fileName = "jni_cloudpos_pinpad";
        JNILoad.jniLoad(fileName);
    }

    /**
     * ALGO_CHECK_VALUE type
     */
    public static final int ALGO_CHECK_VALUE_DEFAULT = 0;
    public static final int ALGO_CHECK_VALUE_SE919 = 1;

    /**
     * tdukpt key type
     */
    public final static int KEY_TYPE_TDUKPT = 1;
    /**
     * master-session key type
     */
    public final static int KEY_TYPE_MASTER = 2;
    /**
     * public key type
     */
    public final static int KEY_TYPE_PUBLIC = 3;
    /**
     * fix key type
     */
    public final static int KEY_TYPE_FIX = 4;
    /**
     * tdukpt 2009 key type
     */
    public final static int KEY_TYPE_TDUKPT_2009 = 5;

    /**
     * master keyID
     */
    public static final int[] MASTER_KEY_ID = new int[]{
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09
    };
    /**
     * user keyID
     */
    public static final int[] USER_KEY_ID = new int[]{
            0x00, 0x01
    };

    /**
     * 3DES
     */
    public static final int ALGORITH_3DES = 1;
    /**
     * DES
     */
    public static final int ALGORITH_DES = 0;
    /**
     * SM4
     */
    public static final int ALGORITH_SM4 = 2;

    public final static int MAC_METHOD_X99 = 0;
    public final static int MAC_METHOD_ECB_FIRST = 1;
    public final static int MAC_METHOD_SE919 = 2;
    public final static int MAC_METHOD_ECB = 3;

    /**
     * MODE EBC
     */
    public static final int PINPAD_ENCRYPT_STRING_MODE_EBC = 0;
    /**
     * MODE CBC
     */
    public static final int PINPAD_ENCRYPT_STRING_MODE_CBC = 1;
    /**
     * MODE CFB
     */
    public static final int PINPAD_ENCRYPT_STRING_MODE_CFB = 2;
    /**
     * MODE OFB
     */
    public static final int PINPAD_ENCRYPT_STRING_MODE_OFB = 3;

    /**
     * Open the PINPad device.
     *
     * @return value >= 0, success; value < 0, error code
     */
    public synchronized native static int open();

    /**
     * Close the PINPad device.
     *
     * @return value >= 0, success; value < 0, error code
     */
    public native static int close();

    /**
     * Show text in the specified line.
     *
     * @param nLineIndex:  Line No. to display, 0 or 1.
     * @param arryText:    Text to show, String.getBytes().
     * @param nTextLength: Text length.
     * @param nFlagSound:  Not used.
     * @return value >= 0, success; value < 0, error code
     */
    public native static int showText(int nLineIndex, byte arryText[], int nTextLength,
                                      int nFlagSound);

    /**
     * Select master key and user key before encryption operations.
     *
     * @param nKeyType:     1 : TDUKPT, 2 : MASTER-SESSION PAIR, 5 : TDUKPT_2009.
     * @param nMasterKeyID: Master key id, 0-9 when nKeyType is master-session; Master key id, 0,1,2when nKeyType is TDUKPT or TDUKPT_2009
     * @param nUserKeyID:   User key id, used when nKeyType is master-session.
     * @param nAlgorith:    0,Not used
     * @return value >= 0, success; value < 0, error code
     */
    public native static int selectKey(int nKeyType, int nMasterKeyID, int nUserKeyID, int nAlgorith);

    /**
     * Set the max or min length of PIN. When you call the calculate_pin_block api, the number you can input is no more than the max length.
     *
     * @param nLength: PIN length
     * @param nFlag:   Flag, 0--min length     1--max length
     * @return value >= 0, success; value < 0, error code
     */
    public native static int setPinLength(int nLength, int nFlag);

    /**
     * Encrypt string. The user key should be a data key whose id is 2 in general.
     *
     * @param arryPlainText:        Plain text data buffer.
     * @param nTextLength:          Length of plain text data buffer.
     * @param arryCipherTextBuffer: buffer for saving cipher text。
     * @return value >= 0, success; value < 0, error code
     */
    public native static int encrypt(byte arryPlainText[], int nTextLength,
                                     byte arryCipherTextBuffer[]);

    /**
     * Calculate the PIN block of the inputted PIN. You should call select_key at first. The user key should be a PIN key whose id is 0 in general.
     *
     * @param arryASCIICardNumber: Card number in ASCII format.
     * @param nCardNumberLength:   Length of card number.
     * @param arryPinBlockBuffer:  buffer for saving PIN block。
     * @param nTimeout_MS:         Timeout waiting for user input in milliseconds. If it is less than 0, then wait forever.
     * @param nFlagSound:          Not used。
     * @return value >= 0, success; value < 0, error code
     */
    public native static int calculatePINBlock(byte arryASCIICardNumber[], int nCardNumberLength,
                                               byte arryPinBlockBuffer[], int nTimeout_MS, int nFlagSound);

    /**
     * Calculate the MAC. The user key should be a MAC key whose id is 1 in general.
     *
     * @param arryData:         data buffer.
     * @param nDataLength:      Length of data buffer.
     * @param arryMACOutBuffer: buffer for saving mac result。
     * @return value >= 0, success; value < 0, error code
     */
    public native static int calculateMac(byte arryData[], int nDataLength, int nMACFlag,
                                          byte arryMACOutBuffer[]);

    /**
     * Update the user key. You should check the cipher user key by yourself through the specified master key and check value obtained from the server or other.
     *
     * @param nMasterKeyID:            Master key id.
     * @param nUserKeyID:              User key id.
     * @param arryCipherNewUserKey:    New user key in cipher text。
     * @param nCipherNewUserKeyLength: Length of new user key。
     * @return value >= 0, success; value < 0, error code
     */
    public native static int updateUserKey(int nMasterKeyID, int nUserKeyID,
                                           byte arryCipherNewUserKey[], int nCipherNewUserKeyLength);

    /**
     * Update master key, the master key must be ciphered by transport key.
     *
     * @param nMasterKeyID:              Master key id.
     * @param arryCipherNewMasterKey:    Ciphered master key.
     * @param nCipherNewMasterKeyLength: Length of ciphered master key。
     * @param pCheckValue:               Check value。
     * @param nCheckValueLen:            Length of check value。
     * @return value >= 0, success; value < 0, error code
     */
    public native static int updateCipherMasterKey(int nMasterKeyID, byte[] arryCipherNewMasterKey,
                                                   int nCipherNewMasterKeyLength, byte[] pCheckValue, int nCheckValueLen);

    /**
     * Update the user key with check value. You don’t need to check the cipher user key by yourself.
     *
     * @param nMasterKeyID:            Master key id.
     * @param nUserKeyID:              User key id.
     * @param arryCipherNewUserKey:    New user key in cipher text。
     * @param nCipherNewUserKeyLength: Length of new user key。
     * @param nUserKeyType:            Key type. 0--PIN key;1--MAC key;2—Data key。
     * @param checkValue:              Check value of user key。
     * @param checkValueLength:        Length of check value, 4 bytes in general。
     * @return value >= 0, success; value < 0, error code
     */
    public native static int updateUserKeyWithCheck(int nMasterKeyID, int nUserKeyID,
                                                    byte[] arryCipherNewUserKey, int nCipherNewUserKeyLength, int nUserKeyType,
                                                    byte[] checkValue,
                                                    int checkValueLength);

    /**
     * Update plain master key.
     *
     * @param nMasterKeyID:  Master key id.
     * @param arrayOldKey:   Old plain master key.
     * @param nOldKeyLength: Length of old key。
     * @param arrayNewKey:   New plain master key。
     * @param nNewKeyLength: Length of new key。
     * @return value >= 0, success; value < 0, error code
     */
    public native static int updateMasterKey(int nMasterKeyID, byte arrayOldKey[],
                                             int nOldKeyLength, byte arrayNewKey[], int nNewKeyLength);

    /**
     * Update the user key with check value. You can set the the algorithm of check.
     *
     * @param nMasterKeyID:            Master key id.
     * @param nUserKeyID:              User key id.
     * @param arryCipherNewUserKey:    New user key in cipher text。
     * @param nCipherNewUserKeyLength: Length of new user key。
     * @param nUserKeyType:            Key type. 0--PIN key;1--MAC key;2—Data key。
     * @param checkValue:              Check value of user key。
     * @param checkValueLength:        Length of check value, 4 bytes in general。
     * @param algoCheckValue:          0-- ALGO_CHECK_VALUE_DEFAULT;1-- ALGO_CHECK_VALUE_SE919(only for aisino).
     * @return value >= 0, success; value < 0, error code
     */
    public native static int updateUserKeyWithCheckE(int nMasterKeyID, int nUserKeyID,
                                                     byte[] arryCipherNewUserKey, int nCipherNewUserKeyLength, int nUserKeyType,
                                                     byte[] checkValue, int checkValueLength, int algoCheckValue);

    /**
     * Update master key, the master key must be ciphered by transport key, and use the selected algorithm of check .
     *
     * @param nMasterKeyID:              Master key id.
     * @param arryCipherNewMasterKey:    Ciphered master key.
     * @param nCipherNewMasterKeyLength: Length of ciphered master key。
     * @param pCheckValue:               Check value。
     * @param nCheckValueLen:            Length of check value。
     * @param algoCheckValue:            0-- ALGO_CHECK_VALUE_DEFAULT;1-- ALGO_CHECK_VALUE_SE919(only for aisino).
     * @return value >= 0, success; value < 0, error code
     */
    public native static int updateCipherMasterKeyE(int nMasterKeyID,
                                                    byte[] arryCipherNewMasterKey,
                                                    int nCipherNewMasterKeyLength, byte[] pCheckValue, int nCheckValueLen,
                                                    int algoCheckValue);

    /**
     * get serial number
     *
     * @param arrySerialNo serial number buffer return value。
     * @return < 0: error code >= 0: success, length of serial number
     */
    public native static int getSerialNo(byte arrySerialNo[]);

    /**
     * set the callback, the callback method is pinpadCallback
     *
     * @return >= 0: success
     * -1: has not find lib
     * -2: has not find pinpad_set_pinblock_callback in lib
     * -3: has not find PinpadCallback in Java code
     */
    public native static int setPinblockCallback();

    /**
     * Set bypass pin.permit click Enter key in PINPAD, bypass the pin.
     *
     * @param flag: 1--bypass;0--can not bypass.
     * @return >= 0 : success; <0: error code.
     */
    public native static int setAllowBypassPinFlag(int flag);


    /**
     * encrypt string using user key
     *
     * @param arrayPlainText:        Plain text
     * @param arrayCipherTextBuffer: buffer for saving cipher text, for dukpt encrypt, the buffer data structure is: cipher data + KSN + counter.
     * @param nMode:                 PINPAD_ENCRYPT_STRING_MODE_EBC  0
     *                               PINPAD_ENCRYPT_STRING_MODE_CBC    1
     *                               PINPAD_ENCRYPT_STRING_MODE_CFB    2
     *                               PINPAD_ENCRYPT_STRING_MODE_OFB    3
     * @param arrayIV:               Initial vector, only for CBC, CFB, OFB mode
     * @param nIVLen:                Length of IV, must be equal to block length according to the algorithm
     * @return >= 0: success; the value is the length of the cipher data length; <0: error code.
     */
    public native static int encryptWithMode(byte[] arrayPlainText, byte[] arrayCipherTextBuffer, int nMode, byte[] arrayIV, int nIVLen);

    /**
     * Get pinpad serial number.
     *
     * @param buffer: data buffer for saving the serial number.
     * @return >= 0: success; <0: error code.
     */
    public native static int getHwserialno(byte[] buffer);

    private static PinPadCallbackHandler callbackHandler;

    public static int setupCallbackHandler(PinPadCallbackHandler handler) {
        callbackHandler = handler;
        if (handler != null) {
            return setPinblockCallback();
        }
        return 0;
    }

    /**
     * Call back method, called by driver.
     *
     * @param data: callback data from the driver, data[0] is the count of *, data[1] is other parameters
     */
    public static void pinpadCallback(byte[] data) {
        if (callbackHandler != null) {
            callbackHandler.processCallback(data);
        }
    }

    /**
     * permission：android.permission.CLOUDPOS_PIN_LOAD_KEY.
     *
     * @param dukptKeyId : 0, 1, 2
     * @param keyUsage   0 : SESSION_KEY_USAGE_pin,
     *                   1 : SESSION_KEY_USAGE_mac,
     *                   2 : SESSION_KEY_USAGE_data,
     * @param snkey      :8 byte
     * @param counter    :4 byte
     * @param key        :16byte
     */
    public native static int importDukptKey(int dukptKeyId, int keyUsage, byte[] snKey, byte[] counter, byte[] key);

    public native static int getMacForSnk(byte[] p1, byte[] p2, byte[] p3);

    /**
     * calcualte check value using specified master key, only allowed when the TK is existence.
     *
     * @param nMKID             : master key ID;
     * @param nAlgo             : now only support the default format, must be specified with 0
     * @param pCheckValueBuf    : buffer for the final result
     * @param nCheckValueBufLen : length of buffer, must be greater than or equal to 4
     *                          return value : < 0 : error code
     *                          > 0 : length of check value
     *                          never return 0;
     */
    public native static int getMKCheckValue(int nMKID, int nAlgo, byte[] pCheckValueBuf, int nCheckValueBufLen);

    /**
     * calculate check value using specified session key. make sure that you have updated the session key in this slot
     *
     * @param nMKID             : master key ID;
     * @param nSKID             : session key ID;
     * @param nAlgo             : now only support the default format, must be specified with 0
     * @param pCheckValueBuf    : buffer for the final result
     * @param nCheckValueBufLen : length of buffer, must be greater than or equal to 4
     * @param nCheckValueBufLen : length of buffer, must be greater than or equal to 4
     *                          return value : < 0 : error code > 0 : length of check value never return 0;
     */
    public native static int getSKCheckValue(int nMKID, int nSKID, int nAlgo, byte[] pCheckValueBuf, int nCheckValueBufLen);

    /**
     * Verify the MAC
     *
     * @param pData       : data
     * @param nDataLen    : data length
     * @param nMacFlag    : 0: X99, 1 : ECB, 2 : SE919, 3 : Union pay ECB
     * @param pMacData    : mac data
     * @param nMacDataLen : mac data length, >=4 && <=8
     * @param nDirection  : must be 0, reserved for future
     */
    public native static int verifyResponseMac(byte[] pData, int nDataLen, int nMacFlag, byte[] pMacData, int nMacDataLen, int nDirection);

    /**
     * Set PINPAD GUI configuration. When setting flag 1 and 2, the setting will keep forever.
     *
     * @param nFlag 1 to set asterisk, 2 to set title bar
     * @param pConfigData  when flag is 1, 0x00: Left, 0x01: Center, 0x02: Right; when flag is 2, the title content.
     * @return value : < 0 : error code
     *                >= 0 : success
     */
    public native static int setGUIConfiguration(int nFlag, byte[] pConfigData, int nDataLen);
}
