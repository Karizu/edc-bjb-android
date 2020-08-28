
package com.cloudpos.jniinterface;

import android.util.Log;

public class RFCardInterface {
    // load *.so library for use
    static {
        String fileName = "jni_cloudpos_rfcard";
        JNILoad.jniLoad(fileName);
    }

    public static int CONTACTLESS_CARD_MODE_AUTO = 0;
    public static int CONTACTLESS_CARD_MODE_TYPE_A = 1;
    public static int CONTACTLESS_CARD_MODE_TYPE_B = 2;
    public static int CONTACTLESS_CARD_MODE_TYPE_C = 3;

    public static int RC500_COMMON_CMD_GET_READER_VERSION = 0x40;
    public static int RC500_COMMON_CMD_RF_CONTROL = 0x38; // command data :
    // 0x01(turn on),
    // 0x00(turn off)

    public static final int CONTACTLESS_CARD_EVENT_FOUND_CARD = 0;
    public static final int CONTACTLESS_CARD_EVENT_TIME_OUT = 1;
    public static final int CONTACTLESS_CARD_EVENT_COMM_ERROR = 2;
    public static final int CONTACTLESS_CARD_EVENT_USER_CANCEL = 3;
    public static final int EVENT_NONE = -1;

    /**
     * Open the contactless card reader.
     *
     * @return value == 0, error ; value != 0 , correct handle
     */
    public synchronized native static int open();

    /**
     * Contactless card reader is Opened return Boolean
     *
     * @return value == true, reader has opened ; value != 0 , reader not open.
     */
    public synchronized native static boolean isOpened();

    /**
     * Close the contactless card reader.
     *
     * @return value >= 0, success ; value < 0, error code
     */
    public native static int close();

    /**
     * 语义代表通知非接设备开始寻卡， Start searching the contactless1 card If you set the
     * nCardMode is auto, reader will try to activate card in type A, type B and
     * type successively; If you set the nCardMode is type A, type B, or type C,
     * reader only try to activate card in the specified way.
     *
     * @param nCardMode      : handle of this card reader
     *                       possible value :
     *                       CONTACTLESS_CARD_MODE_AUTO = 0 CONTACTLESS_CARD_MODE_TYPE_A = 1;
     *                       CONTACTLESS_CARD_MODE_TYPE_B = 2; CONTACTLESS_CARD_MODE_TYPE_C = 3;
     * @param nFlagSearchAll : 0 : signal user if we find one card in the field,
     *                       1 : signal user only we find all card in the field。
     * @param nTimeout_MS    : time out in milliseconds. if nTimeout_MS is less
     *                       then zero, the searching process is infinite.
     * @return value >= 0, success in starting the process; value < 0, error
     * code
     */
    public synchronized native static int searchBegin(int nCardMode, int nFlagSearchAll, int nTimeout_MS);

    /**
     * Stop the process of searching card.
     *
     * @return :value >= 0, success in starting the process; value < 0, error
     * code
     */

    public native static int searchEnd();

    /**
     * Attach the target before transmitting apdu command In
     * this process, the target(card) is activated and return ATR.
     *
     * @param byteArrayATR: ATR buffer, if you set it null, you can not get the
     *                      data.
     * @return value >= 0, success in starting the process and return length of
     * ATR; value < 0, error code
     */
    public synchronized native static int attach(byte byteArrayATR[]);

    /**
     * Detach the target. If you want to send APDU again, you
     * should attach it.
     *
     * @return value >= 0, success in starting the process; value < 0, error
     * code
     */
    public native static int detach();

    /**
     * Transmit APDU command and get the response
     *
     * @param byteArrayAPDU     : command of APDU。
     * @param nAPDULength       : length of command of APDU。
     * @param byteArrayResponse : response of command of APDU。
     * @return value >= 0, success in starting the process; value < 0, error
     * code
     */
    public synchronized native static int transmit(byte byteArrayAPDU[], int nAPDULength,
                                                   byte byteArrayResponse[]);

    /**
     * Transmit command of level 3 and get the response
     *
     * @param byteArrayAPDU     : command of APDU。
     * @param nAPDULength       : length of command of APDU.
     * @param byteArrayResponse : response of command of APDU。
     * @param nOptionValue : reserved for future,  must be set to zero。
     * @return value >= 0, success in starting the process; value < 0, error
     * code
     */
    public synchronized native static int transmit_level3(byte byteArrayAPDU[], int nAPDULength,
                                                          byte byteArrayResponse[], int nOptionValue);

    /**
     * Send control command.
     *
     * @param nCmdID           : id of command.
     * @param byteArrayCmdData : data associated with command, if no data, you
     *                         can set it NULL.
     * @param nDataLength      : data length of command.
     * @return value >= 0, success in starting the process; value < 0, error
     * code
     */

    public synchronized native static int sendControlCommand(int nCmdID, byte byteArrayCmdData[], int nDataLength);

    /**
     * Verify pin for MiFare one card, and do authentication for ultralight C card.
     *
     * @param nSectorIndex : sector index.
     * @param nPinType     : 0 : Key A 1 : Key B 2 : TDEA type (for ultralight C).
     * @param strPin       : password of this pin.
     * @param nPinLength   : length of password.
     * @return value >= 0 : success; value < 0, error code.
     */
    public synchronized native static int verify(int nSectorIndex, int nPinType, byte[] strPin, int nPinLength);

    /**
     * Read data only for MiFare one card.
     *
     * @param nSectorIndex      : sector index。
     * @param nBlockIndex       : block index。
     * @param pDataBuffer       : data buffer。
     * @param nDataBufferLength : buffer length return value.
     * @return value >= 0 : success; value < 0, error code.'
     */
    public synchronized native static int read(int nSectorIndex, int nBlockIndex, byte[] pDataBuffer,
                                               int nDataBufferLength);

    /**
     * Write data only for MiFare one card.
     *
     * @param nSectorIndex : sector index.
     * @param nBlockIndex  : block index.
     * @param pData        : data.
     * @param nDataLength  : data length.
     * @return value >= 0 : success; value < 0, error code.
     */
    public synchronized native static int write(int nSectorIndex, int nBlockIndex, byte[] pData, int nDataLength);

    /**
     * Query the number and type of cards on the contactless card reader.
     *
     * @param nHandle       : handle of this card reader
     * @param pHasMoreCards : 0 : only one PICC in the
     *                      field 0x0A : more cards in the field(type A) 0x0B : more
     *                      cards in the field(type B) 0xAB : more cards in the
     *                      field(type A and type B)
     * @param pCardType     : CONTACTLESS_CARD_TYPE_A_CPU
     *                      0x0000 CONTACTLESS_CARD_TYPE_B_CPU 0x0100
     *                      CONTACTLESS_CARD_TYPE_A_CLASSIC_MINI 0x0001
     *                      CONTACTLESS_CARD_TYPE_A_CLASSIC_1K 0x0002
     *                      CONTACTLESS_CARD_TYPE_A_CLASSIC_4K 0x0003
     *                      CONTACTLESS_CARD_TYPE_A_UL_64 0x0004
     *                      CONTACTLESS_CARD_TYPE_A_UL_192 0x0005
     *                      CONTACTLESS_CARD_TYPE_A_MP_2K_SL1 0x0006
     *                      CONTACTLESS_CARD_TYPE_A_MP_4K_SL1 0x0007
     *                      CONTACTLESS_CARD_TYPE_A_MP_2K_SL2 0x0008
     *                      CONTACTLESS_CARD_TYPE_A_MP_4K_SL2 0x0009
     *                      CONTACTLESS_CARD_UNKNOWN 0x00FF.
     * @return value >= 0 : success; value < 0, error code.
     */
    public native static int queryInfo(int[] pHasMoreCards, int[] pCardType);

    /**
     * Read value from a block
     *
     * @return value >= 0 : success; value < 0, error code.
     * @param[in] : int nHandle : handle of this card reader
     * @param[in] : unsigned int nSectorIndex : sector index
     * @param[in] : unsigned int nBlockIndex : block index
     * @param[out] : unsigned char* pValue : buffer for saving value. LSB,
     * 4bytes
     * @param[in] : unsigned int nValueBufLength : must be greater than 4
     * @param[out] : unsigned char* pAddrData : one byte, for saving a user data
     */
    public synchronized native static int readValue(int nSectorIndex, int nBlockIndex, byte[] pValue,
                                                    int nValueBufLength, byte[] pAddrData);

    /**
     * Write value to a block.
     *
     * @param nSectorIndex    : sector index.
     * @param nBlockIndex     : block index.
     * @param pValue          : data for saving value. LSB, 4bytes.
     * @param nValueBufLength : must be 4.
     * @param pAddrData       : one byte, for saving a user data.
     * @return value >= 0 : success; value < 0, error code.
     */
    public synchronized native static int writeValue(int nSectorIndex, int nBlockIndex, int pValue,
                                                     int nValueLength, byte pAddrData);

    /**
     * Increment value to a block, using it with the API :
     * hal_contactless_card_mc_restore and hal_contactless_card_mc_transfer
     *
     * @param nSectorIndex : sector index
     * @param nBlockIndex  : block index
     * @param pValue       : buffer for saving value. LSB, 4bytes
     * @return value >= 0 : success; value < 0, error code.
     */
    public synchronized native static int increment(int nSectorIndex, int nBlockIndex, int value);

    /**
     * Decrement value to a block, using it with the API :
     * hal_contactless_card_mc_restore and hal_contactless_card_mc_transfer
     *
     * @param nSectorIndex : sector index
     * @param nBlockIndex  : block index
     * @param value        : buffer for saving value. LSB, 4bytes
     * @return value >= 0 : success; value < 0, error code.
     */
    public synchronized native static int decrement(int nSectorIndex, int nBlockIndex, int value);

    /**
     * Save the value to the block from temporary buffer
     *
     * @param nSectorIndex : sector index
     * @param nBlockIndex  : block index return value : >= 0.
     * @return value >= 0 : success; value < 0, error code.
     */
    public synchronized native static int transfer(int nSectorIndex, int nBlockIndex);

    /**
     * Read the value to the temporary from the block
     *
     * @param nSectorIndex : sector index。
     * @param nBlockIndex  : block index return value : >= 0。
     * @return value >= 0 : success; value < 0, error code.
     */
    public synchronized native static int restore(int nSectorIndex, int nBlockIndex);

    /**
     * @deprecated Check whether this is a card on the rfcard reader
     * return value : == 0 : no card
     * >0 : find a card
     * <0 : error code
     */
    public native static int touch();

    /**
     * 初始化的参数
     */
//    public static final int NONE = -1;
//
//    public static Object object = new Object();
//    public static int nEventID = NONE;
//    public static byte[] arryEventData = null;
//
//    public static void callBack(int nEvent, byte[] nData) {
//        nEventID = nEvent;
//        if (nData != null) {
//            arryEventData = new byte[nData.length];
//            for (int i = 0; i < nData.length; i++) {
//                arryEventData[i] = nData[i];
//            }
//        }
//
//        synchronized (object) {
//            // Log.i("RFCard", "notify");
//            object.notifyAll();
//        }
//    }

    public static Object object = new Object();
    public static NotifyEvent notifyEvent = null;
    public static boolean isCallBackCalled = false;

    /**
     * Call back method, called by driver.
     *
     * @param nEvent: event id is {@link #CONTACTLESS_CARD_EVENT_FOUND_CARD};{@link #CONTACTLESS_CARD_EVENT_TIME_OUT};{@link #CONTACTLESS_CARD_EVENT_COMM_ERROR};{@link #CONTACTLESS_CARD_EVENT_USER_CANCEL};
     * @param nData
     */
    public static void callBack(int nEvent, byte[] nData) {

        synchronized (object) {
            notifyEvent = new NotifyEvent(nEvent, nData);
            Log.d("DEBUG", "callBack event = " + nEvent);
            isCallBackCalled = true;
            object.notifyAll();
        }
    }

    public static void waitForCardPresent(int nCardMode, int nFlagSearchAll, int nTimeout_MS) throws InterruptedException {
        synchronized (object) {
            notifyEvent = null;
            searchBegin(nCardMode, nFlagSearchAll, nTimeout_MS);
            object.wait();
        }
    }

    public static void notifyCancel() {
        synchronized (object) {
            notifyEvent = new NotifyEvent(CONTACTLESS_CARD_EVENT_USER_CANCEL, null);
            object.notify();
        }
    }

    /**
     * clear the notify event from callback.
     */
    public static void clear() {
        isCallBackCalled = false;
        Log.d("DEBUG", "clear status");
        notifyEvent = null;
    }

    public static class NotifyEvent {
        public int eventID;
        public byte[] eventData;

        public NotifyEvent(int eventID, byte[] eventData) {
            this.eventID = eventID;
            this.eventData = eventData;
        }
    }

}
