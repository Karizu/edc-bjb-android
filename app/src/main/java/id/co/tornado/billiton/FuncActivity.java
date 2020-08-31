package id.co.tornado.billiton;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;

import id.co.tornado.billiton.util.*;

import com.cloudpos.jniinterface.IFuntionListener;
import com.wizarpos.emvsample.constant.Constant;
import com.wizarpos.jni.MsrInterface;
import com.wizarpos.jni.PinPadInterface;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;
import static com.cloudpos.jniinterface.EMVJNIInterface.close_reader;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_aidparam_add;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_aidparam_clear;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_capkparam_add;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_capkparam_clear;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_exception_file_add;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_exception_file_clear;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_preprocess_qpboc;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_revoked_cert_add;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_revoked_cert_clear;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_set_anti_shake;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_set_kernel_type;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_set_trans_amount;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_terminal_param_set_tlv;
import static com.cloudpos.jniinterface.EMVJNIInterface.get_card_type;
import static com.cloudpos.jniinterface.EMVJNIInterface.open_reader;

public class FuncActivity extends Activity implements Constant, IFuntionListener
{
    public static WeakReferenceHandler mHandler = new WeakReferenceHandler(null);
    protected static Socket socket = null;
    protected static FuncActivity funcRef;
    public static MainApp appState = null;

    protected static Thread msrThread = null;
    protected static boolean msrThreadActived = false;
    protected static boolean readMSRCard = false;
    protected static boolean msrClosed = true;

    protected static boolean contactOpened = false;
    protected static boolean contactlessOpened = false;

    protected static Thread mOpenPinpadThread = null;

    private Timer mTimerSeconds;
    private int mIntIdleSeconds;
    private boolean mBoolInitialized=false;
    private byte mTimerMode = 0;
    private int idleTimer = DEFAULT_IDLE_TIME_SECONDS;

    public void handleMessageSafe(Message msg){}

    public static class WeakReferenceHandler extends Handler{

        private WeakReference<FuncActivity> mActivity;
        public WeakReferenceHandler(FuncActivity activity){
            mActivity = new WeakReference<FuncActivity>(activity);
        }

        public void setFunActivity(FuncActivity activity){
            mActivity = new WeakReference<FuncActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            FuncActivity activity = mActivity.get();
            if(activity != null){
                activity.handleMessageSafe(msg);
            }
        }
    }

    public void capkChecksumErrorDialog(Context context)
    {
        AlertDialog.Builder builder = new Builder(context);
        builder.setTitle("提示");
        builder.setMessage("CAPK:" + appState.failedCAPKInfo + "\nChecksum Error");

        builder.setPositiveButton("确认", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
    public void emvProcessCallback(byte[] data)
    {
        if(debug)Log.d(APP_TAG, "emvProcessNextCompleted");
        appState.trans.setEMVStatus(data[0]);
        appState.trans.setEMVRetCode(data[1]);

        Message msg = new Message();
        msg.what = EMV_PROCESS_NEXT_COMPLETED_NOTIFIER;
        mHandler.sendMessage(msg);
    }

    public void cardEventOccured(int eventType)
    {
        if(debug)Log.d(APP_TAG, "get cardEventOccured");
        Message msg = new Message();
        if(eventType == SMART_CARD_EVENT_INSERT_CARD)
        {
            appState.cardType = get_card_type();
            if(debug)Log.d(APP_TAG, "cardType = " + appState.cardType);

            if(appState.cardType == CARD_CONTACT)
            {
                msg.what = CARD_INSERT_NOTIFIER;
                mHandler.sendMessage(msg);
            }
            else if(appState.cardType == CARD_CONTACTLESS)
            {
                msg.what = CARD_TAPED_NOTIFIER;
                mHandler.sendMessage(msg);
            }
            else{
                appState.cardType = -1;
            }
        }
        else if(eventType == SMART_CARD_EVENT_POWERON_ERROR)
        {
            appState.cardType = -1;
            msg.what = CARD_ERROR_NOTIFIER;
            mHandler.sendMessage(msg);
        }
        else if(eventType == SMART_CARD_EVENT_REMOVE_CARD)
        {
            appState.cardType = -1;
        }
        else if(eventType == SMART_CARD_EVENT_CONTALESS_HAVE_MORE_CARD)
        {
            appState.cardType = -1;
            msg.what = CONTACTLESS_HAVE_MORE_CARD_NOTIFIER;
            mHandler.sendMessage(msg);
        }
        else if(eventType == SMART_CARD_EVENT_CONTALESS_ANTI_SHAKE){
            msg.what = CARD_CONTACTLESS_ANTISHAKE;
            mHandler.sendMessage(msg);
        }

    }

    // This pinpad callback is from emv kernel
    public void pinpadCallback(int nCount, int nExtra) {

        byte[] tempByte = new byte[2];
        tempByte[0] = (byte)(nCount & 0xFF);
        tempByte[1] = (byte)(nExtra & 0xFF);
        if (debug) Log.d(TAG, ByteUtil.arrayToHexStr("EMV OFFLINE pinpadCallback: ", tempByte, 0, tempByte.length));
        Message msg = new Message();
        msg.what = OFFLINE_PIN_NOTIFIER;
        msg.arg1 = nCount;
        msg.arg2 = nExtra;
        mHandler.sendMessage(msg);
    }

    public void setEMVTermInfo()
    {
        byte[] termInfo = new byte[256];
        int offset = 0;
        // 5F2A: Transaction Currency Code
        termInfo[offset] = (byte)0x5F;
        termInfo[offset+1] = 0x2A;
        termInfo[offset+2] = 2;
        offset += 3;
        System.arraycopy(StringUtil.hexString2bytes(appState.terminalConfig.getCurrencyCode()),
                0, termInfo, offset, 2);
        offset += 2;
        // 5F36: Transaction Currency Exponent
        termInfo[offset] = (byte)0x5F;
        termInfo[offset+1] = 0x36;
        termInfo[offset+2] = 1;
        termInfo[offset+3] = appState.terminalConfig.getCurrencyExponent();
        offset += 4;
        // 9F16: Merchant Identification
        if (appState.terminalConfig.getMID().length() == 15) {
            termInfo[offset] = (byte)0x9F;
            termInfo[offset+1] = 0x16;
            termInfo[offset+2] = 15;
            offset += 3;
            System.arraycopy(appState.terminalConfig.getMID().getBytes(), 0, termInfo, offset, 15);
            offset += 15;
        }
        // 9F1A: Terminal Country Code
        termInfo[offset] = (byte)0x9F;
        termInfo[offset+1] = 0x1A;
        termInfo[offset+2] = 2;
        offset += 3;
        System.arraycopy(StringUtil.hexString2bytes(appState.terminalConfig.getCountryCode()),
                0, termInfo, offset, 2);
        offset += 2;
        // 9F1C: Terminal Identification
        if (appState.terminalConfig.getTID().length() == 8) {
            termInfo[offset] = (byte)0x9F;
            termInfo[offset+1] = 0x1C;
            termInfo[offset+2] = 8;
            offset += 3;
            System.arraycopy(appState.terminalConfig.getTID().getBytes(), 0, termInfo, offset, 8);
            offset += 8;
        }
        // 9F1E: IFD Serial Number
        String ifd = android.os.Build.SERIAL;
        if(ifd.length() > 0)
        {
            termInfo[offset] = (byte) 0x9F;
            termInfo[offset + 1] = 0x1E;
            termInfo[offset + 2] = (byte) ifd.length();
            offset += 3;
            System.arraycopy(ifd.getBytes(), 0, termInfo, offset, ifd.length());
            offset += ifd.length();
        }
        // 9F33: Terminal Capabilities
        termInfo[offset] = (byte)0x9F;
        termInfo[offset+1] = 0x33;
        termInfo[offset+2] = 3;
        offset += 3;
        System.arraycopy(StringUtil.hexString2bytes(appState.terminalConfig.getTerminalCapabilities()),
                0, termInfo, offset, 3);
        offset += 3;
        // 9F35: Terminal Type
        termInfo[offset] = (byte)0x9F;
        termInfo[offset+1] = 0x35;
        termInfo[offset+2] = 1;
        termInfo[offset+3] = StringUtil.hexString2bytes(appState.terminalConfig.getTerminalType())[0];
        offset += 4;
        // 9F40: Additional Terminal Capabilities
        termInfo[offset] = (byte)0x9F;
        termInfo[offset+1] = 0x40;
        termInfo[offset+2] = 5;
        offset += 3;
        System.arraycopy(StringUtil.hexString2bytes(appState.terminalConfig.getAdditionalTerminalCapabilities()),
                0, termInfo, offset, 5);
        offset += 5;
        // 9F4E: Merchant Name and Location
        int merNameLength = appState.terminalConfig.getMerchantName1().length();
        if (merNameLength > 0) {
            termInfo[offset] = (byte)0x9F;
            termInfo[offset+1] = 0x4E;
            termInfo[offset+2] = (byte)merNameLength;
            offset += 3;
            System.arraycopy(appState.terminalConfig.getMerchantName1().getBytes(), 0, termInfo, offset, merNameLength);
            offset += merNameLength;
        }
        // 9F66: TTQ first byte
        termInfo[offset] = (byte)0x9F;
        termInfo[offset+1] = 0x66;
        termInfo[offset+2] = 1;
        termInfo[offset+3] = appState.terminalConfig.getTTQ();
        offset += 4;
        // DF19: Contactless floor limit
        if(appState.terminalConfig.getContactlessFloorLimit() >= 0)
        {
            termInfo[offset] = (byte)0xDF;
            termInfo[offset+1] = 0x19;
            termInfo[offset+2] = 6;
            offset += 3;
            System.arraycopy(NumberUtil.intToBcd(appState.terminalConfig.getContactlessFloorLimit(), 6),
                    0, termInfo, offset, 6);
            offset += 6;
        }
        // DF20: Contactless transaction limit
        if(appState.terminalConfig.getContactlessLimit() >= 0)
        {
            termInfo[offset] = (byte)0xDF;
            termInfo[offset+1] = 0x20;
            termInfo[offset+2] = 6;
            offset += 3;
            System.arraycopy(NumberUtil.intToBcd(appState.terminalConfig.getContactlessLimit(), 6),
                    0, termInfo, offset, 6);
            offset += 6;
        }
        // DF21: CVM limit
        if(appState.terminalConfig.getCvmLimit() >= 0)
        {
            termInfo[offset] = (byte)0xDF;
            termInfo[offset+1] = 0x21;
            termInfo[offset+2] = 6;
            offset += 3;
            System.arraycopy(NumberUtil.intToBcd(appState.terminalConfig.getCvmLimit(), 6),
                    0, termInfo, offset, 6);
            offset += 6;
        }
        // EF01: Status check support
        termInfo[offset] = (byte)0xEF;
        termInfo[offset+1] = 0x01;
        termInfo[offset+2] = 1;
        termInfo[offset+3] = appState.terminalConfig.getStatusCheckSupport();
        offset += 4;

        emv_terminal_param_set_tlv(termInfo, offset);
    }

    void setEMVTransAmount(String strAmt)
    {
        byte[] amt = new byte[strAmt.length() + 1];
        System.arraycopy(strAmt.getBytes(), 0, amt, 0, strAmt.length());
        emv_set_trans_amount(amt);
    }


    public static boolean loadAID()
    {
        appState.aids = appState.aidService.query();
        emv_aidparam_clear();
        byte[] aidInfo = null;
        for(int i=0; i< appState.aids.length; i++)
        {
            if(appState.aids[i] != null)
            {
                aidInfo = appState.aids[i].getDataBuffer();
                if(emv_aidparam_add(aidInfo, aidInfo.length) < 0)
                    return false;
            }
            else
            {
                break;
            }
        }
        return true;
    }

    public static int loadCAPK()
    {
        appState.capks = appState.capkService.query();
        emv_capkparam_clear();
        byte[] capkInfo = null;
        for(int i=0; i< appState.capks.length; i++)
        {
            if(appState.capks[i] != null)
            {
                capkInfo = appState.capks[i].getDataBuffer();
                int ret = emv_capkparam_add(capkInfo, capkInfo.length);
                if( ret < 0)
                {
                    appState.failedCAPKInfo = appState.capks[i].getRID() + "_" + appState.capks[i].getCapki();
                    return ret;
                }
            }
            else
            {
                break;
            }
        }
        return 0;
    }

    public static boolean loadExceptionFile()
    {
        appState.exceptionFiles = appState.exceptionFileService.query();
        emv_exception_file_clear();
        byte[] exceptionFileInfo = null;
        for(int i=0; i< appState.exceptionFiles.length; i++)
        {
            if(appState.exceptionFiles[i] != null)
            {
                exceptionFileInfo = appState.exceptionFiles[i].getDataBuffer();
                if(emv_exception_file_add(exceptionFileInfo) < 0)
                    return false;
            }
            else
            {
                break;
            }
        }
        return true;
    }

    public static boolean loadRevokedCAPK()
    {
        appState.revokedCapks = appState.revokedCAPKService.query();
        emv_revoked_cert_clear();
        byte[] revokedCAPKInfo = null;
        for(int i=0; i< appState.revokedCapks.length; i++)
        {
            if(appState.revokedCapks[i] != null)
            {
                revokedCAPKInfo = appState.revokedCapks[i].getDataBuffer();
                if(revokedCAPKInfo != null)
                {
                    if(emv_revoked_cert_add(revokedCAPKInfo) < 0)
                        return false;
                }
            }
            else
            {
                break;
            }
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        appState = ((MainApp)getApplicationContext());
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    public void onTouch()
    {
        //if(debug)Log.d(APP_TAG, "mIntIdleSeconds = 0");
        mIntIdleSeconds=0;
    }

    public void cancelIdleTimer()
    {
        mIntIdleSeconds=0;
        if (mTimerSeconds != null)
        {
            if(debug)Log.d(APP_TAG, "timer cancelled");
            mTimerSeconds.cancel();
        }
    }

    public void startIdleTimer(byte timerMode, int timerSecond)
    {
        idleTimer = timerSecond;
        mTimerMode = timerMode;
        //initialize idle counter
        mIntIdleSeconds=0;
        if (mBoolInitialized == false)
        {
            if(debug)Log.d(APP_TAG, "timer start");
            mBoolInitialized = true;
            //create timer to tick every second
            mTimerSeconds = new Timer();
            mTimerSeconds.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    mIntIdleSeconds++;
                    if (mIntIdleSeconds == idleTimer)
                    {
                        if(mTimerMode == TIMER_IDLE)
                        {

                        }
                        else
                        {
                            if(appState.needClearPinpad == true)
                            {
                                // clear pinpad
                                appState.needClearPinpad = false;
                                PinPadInterface.setText(0, null, 0, 0);
                                PinPadInterface.setText(1, null, 0, 0);
                            }

                            setResult(Activity.RESULT_CANCELED, getIntent());
                            exit();
                        }
                    }
                }
            }, 0, 1000);
        }
    }

    protected boolean connectSocket(String ip, int port, int timeout)
    {
        try {
            socket = new Socket();
            socket.setSoTimeout(timeout); // 设置读超时
            SocketAddress remoteAddr = new InetSocketAddress(ip, port);
            if(debug)
            {
                Log.d(APP_TAG, "Connect IP[" + ip + "]port[" + port + "]");
            }
            socket.connect(remoteAddr, timeout);
        } catch (UnknownHostException e) {

        } catch (IOException e) {

        }
        if(socket!= null && socket.isConnected())
        {
            return true;
        }
        return false;
    }

    protected void disconnectSocket()
    {
        if(debug)Log.d(APP_TAG, "disconnectSocket");
        try {
            if(socket != null)
            {
                socket.close();
            }
        } catch (IOException e) {

        }
    }


    protected synchronized void readAllCard()
    {
        if(appState.acceptMSR)
        {
            startMSRThread();
        }

        if(appState.acceptContactCard)
        {
            contactOpened = true;
            open_reader(1);
        }
        if(appState.acceptContactlessCard)
        {
            contactlessOpened = true;
            emv_set_anti_shake(1);
            appState.msrPollResult = -1;
            open_reader(2);
        }
    }

    public void waitContactCard()
    {
        contactOpened = true;
        open_reader(1);
    }

    protected void cancelAllCard()
    {
        cancelMSRThread();
        cancelContactCard();
        cancelContactlessCard();
    }

    protected void cancelContactCard()
    {
        if(debug)Log.d(APP_TAG, "Close contact card");
        if(contactOpened)
        {
            contactOpened = false;
            close_reader(1);
        }
    }


    protected void cancelContactlessCard()
    {
        if(contactlessOpened)
        {
            contactlessOpened = false;
            close_reader(2);
        }
    }

    private void notifyContactlessCardOpenError()
    {
        Message msg = new Message();
        msg.what = CARD_OPEN_ERROR_NOTIFIER;
        mHandler.sendMessage(msg);
    }

    protected void startMSRThread()
    {
        if(   readMSRCard == false
                && appState.msrError == false
        )
        {
            while(msrThreadActived){

            }
            msrThread=new MSRThread();
            msrThread.start();
        }
    }

    protected void cancelMSRThread()
    {
        if(readMSRCard == true)
        {
            readMSRCard = false;
        }
    }

    protected void notifyMSR()
    {
        Message msg = new Message();
        msg.what = MSR_READ_DATA_NOTIFIER;
        mHandler.sendMessage(msg);
    }

    protected void notifyMsrOpenError()
    {
        Message msg = new Message();
        msg.what = MSR_OPEN_ERROR_NOTIFIER;
        mHandler.sendMessage(msg);
    }

    protected void notifyMsrReadError()
    {
        Message msg = new Message();
        msg.what = MSR_READ_ERROR_NOTIFIER;
        mHandler.sendMessage(msg);
    }

    protected boolean read_track2_data()
    {
        if(debug)Log.d(APP_TAG, "read_track2_data");
        int trackDatalength;
        byte[] byteArry = new byte[255];
        trackDatalength = MsrInterface.getTrackData(1, byteArry, byteArry.length);  // nTrackIndex: 0-Track1; 1-track2; 2-track3
        if(debug)
        {
            String strDebug = "";
            for(int i=0; i<trackDatalength; i++)
                strDebug += String.format("%02X ", byteArry[i]);
            Log.d(APP_TAG, "track2 Data: " + strDebug);
        }
        if(trackDatalength > 0)
        {
            if(   trackDatalength > 37
                    || trackDatalength < 21
            )
            {
                return false;
            }

            int panStart = -1;
            int panEnd = -1;
            for (int i = 0; i < trackDatalength; i++)
            {
                if (byteArry[i] >= (byte) '0' && byteArry[i] <= (byte) '9')
                {
                    if( panStart == -1)
                    {
                        panStart = i;
                    }
                }
                else if (byteArry[i] == (byte) '=')
                {
                    /* Field separator */
                    panEnd = i;
                    break;
                }
                else
                {
                    panStart = -1;
                    panEnd = -1;
                    break;
                }
            }
            if (panEnd == -1 || panStart == -1)
            {
                return false;
            }
            appState.trans.setPAN(new String(byteArry, panStart, panEnd - panStart));
            appState.trans.setExpiry(new String(byteArry, panEnd + 1, 4));
            appState.trans.setServiceCode(new String(byteArry, panEnd + 5, 3));
            appState.trans.setTrack2Data(byteArry, 0, trackDatalength);
            appState.trans.setCardEntryMode(SWIPE_ENTRY);
            return true;
        }
        return false;
    }

    protected void read_track3_data()
    {
        if(debug)Log.d(APP_TAG, "read_track3_data");
        int trackDatalength;
        byte[] byteArry = new byte[255];
        trackDatalength = MsrInterface.getTrackData(2, byteArry, byteArry.length);  // nTrackIndex: 0-Track1; 1-track2; 2-track3
        if(debug)
        {
            String strDebug = "";
            for(int i=0; i<trackDatalength; i++)
                strDebug += String.format("%02X ", byteArry[i]);
            Log.d(APP_TAG, "track3 Data: " + strDebug);
        }
        if(trackDatalength > 0)
        {
            appState.trans.setTrack3Data(byteArry, 0, trackDatalength);
        }
    }

    class MSRThread extends Thread
    {
        public void run()
        {
            super.run();
            msrThreadActived = true;
            readMSRCard = false;
            if(msrClosed == true)
            {
                if(MsrInterface.open() >= 0)
                {
                    msrClosed = false;
                }
            }
            if(msrClosed == false)
            {
                readMSRCard = true;
                do{
                    int nReturn = -1;
                    nReturn = MsrInterface.poll(500);
                    appState.msrPollResult = nReturn;
                    if(debug)Log.d(APP_TAG, "MsrInterface.poll, " + nReturn);
                    if(readMSRCard == false)
                    {
                        MsrInterface.close();
                        msrClosed = true;
                        if(debug)Log.d(APP_TAG, "MsrInterface.close");
                    }
                    else if(nReturn >= 0)
                    {
                        if(read_track2_data())
                        {
                            read_track3_data();
                            MsrInterface.close();
                            readMSRCard = false;
                            msrClosed = true;
                            notifyMSR();
                        }
                        else
                        {
                            MsrInterface.close();
                            msrClosed = true;
                            readMSRCard = false;
                            notifyMsrReadError();
                        }
                    }
                }while(readMSRCard == true);
            }
            else
            {
                notifyMsrOpenError();
            }
            if(debug)Log.d(APP_TAG, "MSRThread.exit");
            msrThreadActived = false;
        }
    }


    protected void offlineSuccess()
    {
        transSuccess();
    }

    public void saveTrans()
    {
        if(debug)Log.d(APP_TAG, "save trans");
        appState.transDetailService.save(appState.trans);
    }

    public void saveAdvice()
    {
        if(debug)Log.d(APP_TAG, "save advice");
        appState.adviceService.save(appState.trans);
    }

    public void clearTrans()
    {
        appState.transDetailService.clearTable();
    }

    public void clearAdvice()
    {
        appState.adviceService.clearTable();
    }

    public void transSuccess()
    {
        if(appState.getTranType() != TRAN_SETTLE)
        {
            saveTrans();
            appState.batchInfo.incSale(appState.trans.getTransAmount());
        }
    }

    public void exit()
    {
        cancelIdleTimer();
        finish();
    }

    //=============== Q1 keyboard =============
    protected void onEnter()
    {
    }

    protected void onCancel()
    {
        super.onBackPressed();
    }

    protected void onBack()
    {
        super.onBackPressed();
    }

    protected void onDel()
    {
    }

    protected void onKeyCode(char key)
    {}

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(debug) Log.i("FuncActivity", "onKeyDown:"+keyCode);
        onTouch();
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_BACK:
                onBack();
                break;
            case KeyEvent.KEYCODE_ESCAPE:
                onCancel();
                break;
            case KeyEvent.KEYCODE_DEL:
                onDel();
                break;
            case KeyEvent.KEYCODE_ENTER:
                onEnter();
                break;
            case 232://'.'
                onKeyCode('.');
                break;
            default:
                onKeyCode((char) ('0'+(keyCode-KeyEvent.KEYCODE_0)));
                break;
        }
        return true;
    }
    //=============== Q1 keyboard =============

    protected boolean preProcessQpboc()
    {
        //pre-process
        int res = emv_preprocess_qpboc();
        if(res < 0)
        {
            if(res == -2)
            {
                appState.setErrorCode(R.string.error_amount_zero);
            }
            else
            {
                appState.setErrorCode(R.string.error_amount_over_limit);
            }
            emv_set_kernel_type(CONTACT_EMV_KERNAL);
            return false;
        }
        return true;
    }
}
