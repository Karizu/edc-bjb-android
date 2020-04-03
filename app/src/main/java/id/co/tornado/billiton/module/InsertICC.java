package id.co.tornado.billiton.module;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;

import com.wizarpos.jni.ContactICCardReaderInterface;
import com.wizarpos.jni.ContactICCardSlotInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import id.co.tornado.billiton.TestActivity;
import id.co.tornado.billiton.common.CommonConfig;
import id.co.tornado.billiton.common.FileControlInfo;
import id.co.tornado.billiton.common.NsiccsData;
import id.co.tornado.billiton.common.StringLib;
import id.co.tornado.billiton.handler.SingleTagParser;
import id.co.tornado.billiton.module.listener.InputListener;
import id.co.tornado.billiton.module.listener.LogOutput;


/**
 * Created by indra on 24/11/15.
 */
public class InsertICC extends com.rey.material.widget.EditText {
    Context context;
    public String tag = "ICC Module";

    private final int ICC_NO_EVENT = -1;
    private final int ICC_INSERT = 0;
    private final int ICC_REMOVE = 1;

    private boolean isQuit = true;
    private boolean isOpen = false;
    private int ret = -1;
    private LogOutput logOutput = null;

    private boolean isSCIOpen = false;
    private boolean iccReady = false;
    private boolean cardPresent = false;
    private static int CHIP_SLOT_INDEX = 0;
    private byte byteArrayATR[];
    private int nCardHandle = 0;
    private byte byteArrayAPDU[];
    private int nAPDULength;
    private byte byteArrayResponse[];
    private int procStage;
    public boolean isByPass = false;

    private FileControlInfo fci;
    private NsiccsData nsiccsData;
    private String aip;
    private String afl;
    private Map<String, String> recordValues = new HashMap<>();

    private List<InputListener> inputListeners = new ArrayList<>();
    private Handler commHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String respmsg = null;
            String additional = null;
            try {
                Bundle b = msg.getData();
                if (b != null) {
                    HashMap data = (HashMap) b.getSerializable("DATA");
                    writeLog("MSG RC : " + data.get("RC").toString());
                    writeLog("MSG  : " + data.get("msg").toString());
                    writeLog("DATA  : " + data.toString());
                    if (data.get("RC").toString().equals("00")) {
                        setText(data.get("msg").toString());
                        isQuit = true;
                        respmsg = data.get("msg").toString();
                        if (procStage == CommonConfig.ICC_PROCESS_STAGE_GEN || procStage == CommonConfig.ICC_PROCESS_STAGE_TX) {
                            additional = "keep open";
                            additional = "TCAID" + recordValues.get("9F26") + nsiccsData.getAid();
                        }
                    } else if (data.get("RC").toString().equals("02")) {
                        additional = "reversal advice";
                        closeDriver();
                    } else if (data.get("RC").toString().equals("07")) {
                        additional = "fallback";
                        closeDriver();
                    } else if (data.get("RC").toString().equals("10")) {
                        additional = "blocked";
                        closeDriver();
                    } else {
                        closeDriver();
                    }
                }
            } catch (Exception e) {
                writeLog("commHandler Error : " + e.getMessage());
                if (isOpen) {
                    closeDriver();
                }
                if (logOutput!=null) {
                    ((TestActivity) logOutput).hideFab();
                }
            }
//            for (InputListener listener : inputListeners) {
//                listener.onInputCompleted(InsertICC.this, respmsg, additional, nsiccsData);
//            }
            if (inputListeners.size()>0) {
                InputListener inputListener = inputListeners.get(0);
                inputListener.onInputCompleted(InsertICC.this, respmsg, additional, nsiccsData);
            }
        }
    };

    private Handler eventICCHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ICC_INSERT:
                    try {
                        if(isByPass){
                            closeDriver();
                            InputListener inputListener = inputListeners.get(0);
                            inputListener.onInputCompleted(InsertICC.this, "", "", nsiccsData);
                        }
                        else if (isOpen) {
                            isQuit = false;
                            Thread t1 = new Thread(new GetData());
                            t1.start();
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                case ICC_REMOVE:
                    break;
                default:
                    break;
            }
        }
    };

    public InsertICC(Context context) {
        super(context);
        this.context = context;
    }

    public InsertICC(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InsertICC(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public InsertICC(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public boolean closeDriver() {
        int val=0;
        isQuit = true;
        isOpen = false;
        iccReady = false;
        try {
            val=ContactICCardReaderInterface.close(nCardHandle);
//            writeLog("Close SCI : " + val);
            val=ContactICCardReaderInterface.terminate();
//            writeLog("Terminate SCI : " + val);
//            writeLog("Closing Driver");
        } catch (Exception e) {
            val=-1;
            e.printStackTrace();
        }
        return val >= 0;
    }

    public boolean openDriver() {
        int val=0;
//        writeLog("Opening ICC Slot");
        try {
            val=openEmvSlot();
        } catch (Exception e) {
            val=-1;
        }
        return val >= 0;
    }


    public void addInputListener(InputListener inputListener){
        inputListeners.clear();
        inputListeners.add(inputListener);
    }
    public void init(int stage, NsiccsData dataHolder) {
        procStage = stage;
        nsiccsData = dataHolder;
        setEnabled(false);
        setKeyListener(null);
        if (!openDriver()) {
            writeLog("Open Driver failed!");

        } else {
//            writeLog("ICC DRIVER", "Open Driver succeed!");
            Thread t1 = new Thread(new CardStateListener());
            t1.start();
        }
    }

    public void cont(int stage, NsiccsData dataHolder) {
        procStage = stage;
        nsiccsData = dataHolder;
        Thread t1 = new Thread(new GetData());
        t1.start();
    }

    public void setIsQuit(boolean isQuit) {
        this.isQuit = isQuit;
    }

    public void setIsOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    private class CardStateListener implements Runnable {

        @Override
        public void run() {
            boolean listening = true;
            int counter = 0;
            try {
                while (listening) {
                    if (!listening) {
                        break;
                    }
                    com.wizarpos.jni.SmartCardEvent event = new com.wizarpos.jni.SmartCardEvent();
                    if (isByPass){
                        event.nEventID = -1;
                        event.nSlotIndex = -1;
                    }
                    ContactICCardReaderInterface.pollEvent(2000, event);
//                writeFromBackground("Pool result : " + event.nEventID + " loop " + counter);
//                    for (InputListener il : inputListeners) {
//                        il.onStateChanged("Event " + event.nEventID == null ? "null" : String.valueOf(event.nEventID)
//                                + " Loop " + counter == null ? "null" : String.valueOf(counter), 99);
//                    }
                    Message msg = new Message();
                    msg.what = event.nEventID;

                    if (cardPresent && iccReady == false && msg.what == 1){

                        event.nEventID = 0;
                        msg.what = event.nEventID;
                    }

                    switch (event.nEventID) {
                        case ICC_INSERT:
                            listening = false;
                            cardPresent = true;
                            eventICCHandler.sendMessage(msg);
                            break;
                        case ICC_REMOVE:
                            listening = false;
                            cardPresent = false;
                            eventICCHandler.sendMessage(msg);
                            break;
                        default:
                            if (cardPresent) {
                                // ready
                            } else {
                                // waiting for card
                            }
                    }
                    counter++;
                }
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                PrintWriter ps = new PrintWriter(sw);
                e.printStackTrace(ps);
                writeFromBackground(sw.toString());
            }
        }
    };

    private class GetData implements Runnable {
        @Override
        public void run() {
            if (cardPresent) {
                try {
                    switch (procStage) {
                        case CommonConfig.ICC_PROCESS_STAGE_INIT:
                            readStage1();
                            break;
                        case CommonConfig.ICC_PROCESS_STAGE_GEN:
                            readStage2();
                            break;
                        case  CommonConfig.ICC_PROCESS_STAGE_TX:
                            readStage2();
                            break;
                        case CommonConfig.ICC_PROCESS_STAGE_FINISHED:
                            readStage3();
                            break;
                        case CommonConfig.ICC_PROCESS_STAGE_CANCELED:
                            writeFromBackground("Should be re-init");
                            break;
                        default:
                            writeFromBackground("Why am i here???");
                            break;
                    }
                } catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    PrintWriter ps = new PrintWriter(sw);
                    e.printStackTrace(ps);
                    writeFromBackground(sw.toString());
                }
            }
        }

    }

    public void setLogOutput(LogOutput logOutput) {
        this.logOutput = logOutput;
    }

    private void writeLog(String string) {
        if (string!=null) {
            if (logOutput != null) {
                logOutput.writeLog(string);
            } else {
                Log.i(tag, string);
            }
        } else {
            if (logOutput != null) {
                logOutput.writeLog("tried to write null string");
            } else {
                Log.i(tag, "null");
            }
        }
    }

    private void writeLog(String tag, String string) {
        if (string != null) {
            if (logOutput != null) {
                logOutput.writeLog(string);
            } else {
                Log.i(tag, string);
            }
        } else {
            if (logOutput != null) {
                logOutput.writeLog("tried to write null string");
            } else {
                Log.i(tag, "null");
            }
        }
    }

    private int openEmvSlot() {
        int val = 0;
        try {
            val = ContactICCardReaderInterface.init();
//            writeLog("SCI Init : " + val);
            val = ContactICCardReaderInterface.open(CHIP_SLOT_INDEX);
//            writeLog("SCI Open : " + val);
            nCardHandle = val;
            isOpen = true;

            if (val >= 0) {
                Thread.sleep(100);
                int powerOn = iccPowerOn();
                if (powerOn > 0) {
                    cardPresent = true;
                    writeLog("Card already present");
                    Message msg = new Message();
                    msg.what = ICC_INSERT;
                    eventICCHandler.sendMessage(msg);
                } else {
                    writeLog("Silahkan masukkan kartu debit");
                }
            }

        } catch (Exception e) {
            writeLog(e.getMessage());
        }

        return val;
    }

    public void readStage1() {
        int val = 0;
        int proc = 0;
        String cResp = "";
//        writeLog("Reading Card");
        HashMap<String, String> data = new HashMap<>();
        Message msg = new Message();
        Bundle b = new Bundle();
        try {
            val = iccPowerOn();
            if (val >= 0) {
                // terminal init
                initNsiccsTerminal(nsiccsData);
                // changed to single support, for multiple app support use CommonConfig.ICC_LIST_SUPPORTED_APP();
                String supportedAppId = "A0000006021010";
                val = selectAppById(supportedAppId);
            } else {
                data.put("RC", "07");
                data.put("msg", "Fallback");
            }
            if (val>0 && fci.isValid()) {
                writeFromBackground("Select AID : OK");
                val = getProcessingOptions();
            }
            if (val>0 && aip!=null) {
                writeFromBackground("Get PO : OK");
                nsiccsData.setAip(aip);
                val = readRecords("010C");
                proc++;
            }
            if (val>0 && recordValues.size()>0) {
                writeFromBackground("Read Records : OK");
                nsiccsData.setTrack2(recordValues.get("57"));
                proc++;
            }
            if (val>0) {
                cResp = nsiccsData.getTrack2();
                data.put("RC", "00");
                data.put("msg", cResp);
                proc++;
            } else {
                data.put("RC", "05");
                data.put("msg", "Read failed @proc " + proc);
            }
        } catch (Exception e) {
            data.put("RC", "05");
            data.put("msg", e.getMessage() + " proc " + proc);
        }
        iccReady = false;
        b.putSerializable("DATA", data);
        msg.setData(b);
        commHandler.sendMessage(msg);
    }

    public void readStage2() {
        int val = 0;
        int proc = 0;
        String cResp = "";
//        writeLog("Reading Card");
        HashMap<String, String> data = new HashMap<>();
        Message msg = new Message();
        Bundle b = new Bundle();
        try {
            val = iccPowerOn();
            if (val >= 0) {
                // terminal init
                initNsiccsTerminal(nsiccsData);
                // changed to single support, for multiple app support use CommonConfig.ICC_LIST_SUPPORTED_APP();
                String supportedAppId = "A0000006021010";
                nsiccsData.setAid(supportedAppId);
                val = selectAppById(supportedAppId);
                recordValues.put("4F", supportedAppId);
            } else {
                data.put("RC", "07");
                data.put("msg", "Fallback");
            }
            if (val>0 && fci.isValid()) {
                writeFromBackground("Select AID : OK");
                val = getProcessingOptions();
            } else if (!data.containsKey("RC")) {
                data.put("RC", "10");
                data.put("msg", "Aplikasi CHIP error atau Kartu telah diblok");
                val = -1;
            }
            if (val>0 && aip!=null) {
                writeFromBackground("Get PO : OK");
                nsiccsData.setAip(aip);
                val = readRecords();
                proc++;
            }
            if (val>0 && recordValues.size()>0) {
                writeFromBackground("Read Records : OK");
                syncNsiccsData();
            }
            if (val>0) {
//                Map<String, String> fciValue = fci.getFciValue();
//                writeFromBackground("50: " + fciValue.get("50"));
//                writeFromBackground("9F38: " + fciValue.get("9F38"));
//                writeFromBackground("9F12: " + fciValue.get("9F12"));
//                writeFromBackground("ST: " + fci.gethStatus());
                recordValues.put("9A", nsiccsData.getTxdate());
                recordValues.put("9F02", nsiccsData.getAmount());
                recordValues.put("9F03", nsiccsData.getAddamount());

//                String termRandom = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                Random rnd = new Random();
                int n = 10000000 + rnd.nextInt(90000000);
                String termRandom = String.valueOf(n);
                nsiccsData.setTermRandomNum(termRandom);
                recordValues.put("9F37", termRandom);

                // SDA only tidak support generate random

//                try {
//
//                    String cardRandom = getCardRandom();
//                    if (!cardRandom.equals("")) {
//                        nsiccsData.setCardRandomNum(cardRandom);
//                        recordValues.put("9F45", cardRandom);
//                        val = 1;
//                    } else {
//                        val = -1;
//                    }
//                    writeFromBackground("Generate Random : OK");
//                } catch (Exception e) {
//                    writeFromBackground("Rand Err : " + e.getMessage());
//                }
            }
            if (val > 0) {
                String p1genARQC = "80"; // generate ARQC using CDOL1
                val = doGenerateAC(p1genARQC, nsiccsData.getCdol());
                writeFromBackground("Generate AC : OK");
            }
            if (val>0) {
                String de55 = composeDe55();
                switch (procStage) {
                    case CommonConfig.ICC_PROCESS_STAGE_GEN:
                        cResp = de55;
                        break;
                    case CommonConfig.ICC_PROCESS_STAGE_TX:
                        cResp = nsiccsData.getTrack2() + "|" + de55;
                        break;
                }
                data.put("RC", "00");
                data.put("msg", cResp);
            }
        } catch (Exception e) {
            data.put("RC", "05");
            data.put("msg", e.getMessage() + " proc " + proc);
        }
        iccReady = false;
        b.putSerializable("DATA", data);
        msg.setData(b);
        commHandler.sendMessage(msg);
    }

    public void readStage3() {
        int val = 0;
        int proc = 0;
        String cResp = "";
//        writeLog("Reading Card");
        HashMap<String, String> data = new HashMap<>();
        Message msg = new Message();
        Bundle b = new Bundle();
        try {
            val = externalAuth();
            writeFromBackground("Verify ARPC : OK");

            if (nsiccsData.getTxst()>0) {
                cResp = nsiccsData.getTc();
                if (cResp!=null) {
                    writeFromBackground("ARPC Verified");
                    data.put("RC", "00");
                    data.put("msg", cResp);
                } else {
                    data.put("RC", "00");
                    data.put("msg", "Cannot retrive certificate");
                }
            } else {
                data.put("RC", "02");
                data.put("msg", "ARPC verification failed");
            }
        } catch (Exception e) {
            data.put("RC", "02");
            data.put("msg", e.getMessage() + " proc " + proc);
        }
        iccReady = false;
        b.putSerializable("DATA", data);
        msg.setData(b);
        commHandler.sendMessage(msg);
    }

    public int iccPowerOn() {
        int val = 0;
        if (!iccReady) {
//            writeFromBackground("ICC Power On");
            byteArrayATR = new byte[64];
            ContactICCardSlotInfo mSlotInfo = new ContactICCardSlotInfo();
            val = ContactICCardReaderInterface.powerOn(nCardHandle, byteArrayATR, mSlotInfo);
//            writeFromBackground("Power On : " + val);
            if (val >= 0 ) {
                iccReady = true;
//                for (InputListener listener : inputListeners) {
//                    listener.onStateChanged("Melakukan transaksi CHIP\nHarap jangan lepaskan kartu sampai transaksi selesai",1);
//                }
                if (inputListeners.size()>0) {
                    InputListener inputListener = inputListeners.get(0);
                    inputListener.onStateChanged("Melakukan transaksi CHIP\nHarap jangan lepaskan kartu sampai transaksi selesai",1);
                }
            }
        }
        return val;
    }

    public void initNsiccsTerminal(NsiccsData dataHolder) {
        if (dataHolder==null) {
            nsiccsData = new NsiccsData();
        }
        nsiccsData.setTvr("8000040000");
        nsiccsData.setCurrency("0360");
        nsiccsData.setTxtype("00");
        nsiccsData.setCountry("0360");
        nsiccsData.setTxst(CommonConfig.ICC_VALIDATION_INVALIDATED);
        nsiccsData.setTdol("9F02065F2A029A039C0195059F3704");

    }

    public void syncNsiccsData() {
        nsiccsData.setTrack2(recordValues.get("57"));
        nsiccsData.setPanseq(recordValues.get("5F34"));
        nsiccsData.setPan(recordValues.get("5A"));
        nsiccsData.setCdol(recordValues.get("8C"));
        nsiccsData.setCdol2(recordValues.get("8D"));
        writeFromBackground("PAN : " + recordValues.get("5A"));
        recordValues.put("95", nsiccsData.getTvr());
        recordValues.put("9F1A", nsiccsData.getCountry());
        recordValues.put("9C", nsiccsData.getTxtype());
        recordValues.put("5F2A", nsiccsData.getCurrency());
        recordValues.put("82", aip);
        Iterator fcIter = fci.getFciValue().keySet().iterator();
        while (fcIter.hasNext()) {
            String key = (String) fcIter.next();
            recordValues.put(key, fci.getFciValue().get(key));
        }
    }

    public void readSCI() {
        int val = 0;
//        writeLog("Reading Card");
        HashMap<String, String> data = new HashMap<>();
        Message msg = new Message();
        Bundle b = new Bundle();
        try {

            val = iccPowerOn();

            if (val >= 0) {

                iccReady = true;

                // terminal init
                initNsiccsTerminal(null);

                // changed to single support
//                CommonConfig.ICC_LIST_SUPPORTED_APP();

                String supportedAppId = "A0000006021010";
                val = selectAppById(supportedAppId);
            }
            if (val>0 && fci.isValid()) {
                writeFromBackground("Select AID : OK");
                val = getProcessingOptions();
            }
            if (val>0 && aip!=null) {
                writeFromBackground("Get PO : OK");
                nsiccsData.setAip(aip);
                val = readRecords();
            }
            if (val>0 && recordValues.size()>0) {
                writeFromBackground("Read Records : OK");
                syncNsiccsData();
            }
            if (val>0) {
                writeFromBackground("Generate AC with dummy");

                String dummyAmt = "000000010000";
                String dummyAddAmt = "000000000000";
                SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");

                nsiccsData.setTxdate(sdf.format(new Date()));
                recordValues.put("9A", nsiccsData.getTxdate());

                nsiccsData.setAmount(dummyAmt);
                recordValues.put("9F02", dummyAmt);
                nsiccsData.setAddamount(dummyAddAmt);
                recordValues.put("9F03", dummyAddAmt);

//                String termRandom = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                Random rnd = new Random();
                int n = 10000000 + rnd.nextInt(90000000);
                String termRandom = String.valueOf(n);
                nsiccsData.setTermRandomNum(termRandom);
                recordValues.put("9F37", termRandom);

                try {

                    String cardRandom = getCardRandom();
                    if (!cardRandom.equals("")) {
                        nsiccsData.setCardRandomNum(cardRandom);
                        recordValues.put("9F45", cardRandom);
                        val = 1;
                    } else {
                        val = -1;
                    }
                    writeFromBackground("Generate Random : OK");
                } catch (Exception e) {
                    writeFromBackground("Rand Err : " + e.getMessage());
                }
            }
//            if (val>0) {
//
//            }
            if (val > 0) {
                val = doGenerateAC("80", nsiccsData.getCdol());
                writeFromBackground("Generate AC : OK");
            }
            String cResp = "";
            String de55 = "";
            if (val>0) {

                cResp = nsiccsData.getTrack2();
                de55 = composeDe55();
            }
            if (!de55.equals("")) {
                cResp += "[]" + de55;
                data.put("RC", "00");
                data.put("msg", "Card Response : " + cResp);
            } else {
                data.put("RC", "05");
                data.put("msg", "Read failed");
            }
        } catch (Exception e) {
            data.put("RC", "05");
            data.put("msg", e.getMessage());
        }
        b.putSerializable("DATA", data);
        msg.setData(b);
        commHandler.sendMessage(msg);
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void writeFromBackground(String string) {
        HashMap<String, String> data = new HashMap<>();
        Message msg = new Message();
        Bundle b = new Bundle();
        data.put("msg", string);
        b.putSerializable("DATA", data);
        msg.setData(b);
        bgMessageHandler.sendMessage(msg);
    }

    private Handler bgMessageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle b = msg.getData();
            HashMap<String, String> data = (HashMap<String, String>) b.getSerializable("DATA");
            String textMessage = data.get("msg");
            writeLog(textMessage);
        }
    };

    public void sendCmd(String apdu) {
        if (iccReady) {
            int val = 0;
            writeFromBackground("Send : " + apdu);
            byteArrayAPDU = StringLib.hexStringToByteArray(apdu);
            nAPDULength = byteArrayAPDU.length;
            byteArrayResponse = new byte[256];
            val = ContactICCardReaderInterface.transmit(nCardHandle, byteArrayAPDU, nAPDULength, byteArrayResponse);
            writeFromBackground("Transmit : " + val);
            writeFromBackground("Card Response : " + StringLib.toHexString(byteArrayResponse, 0, byteArrayResponse.length, false));
        } else {
            writeFromBackground("ICC not ready");
            if (!openDriver()) {
                writeLog("Open Driver failed!");

            } else {
//            writeLog("ICC DRIVER", "Open Driver succeed!");
                Thread t1 = new Thread(new CardStateListener());
                t1.start();
            }
        }
    }

    private int selectAppById(String supportedAppId) {
        int val = 0;
        String resp = "";
        String rc = "";
        String rl = "";
        String rm = "";
        int nrLen = 0;
        // select APP by AID
        String cmd = "00A4040007" + supportedAppId + "00";
        writeFromBackground("Send : " + cmd);
        byteArrayAPDU = StringLib.hexStringToByteArray(cmd);
        nAPDULength = byteArrayAPDU.length;
        byteArrayResponse = new byte[256];
        val = ContactICCardReaderInterface.transmit(nCardHandle, byteArrayAPDU, nAPDULength, byteArrayResponse);
        writeFromBackground("Transmit : " + val);
        resp = StringLib.toHexString(byteArrayResponse, 0, val, false);
        rc = resp.substring(0, 2);
        if (resp.length() > 3) {
            rl = resp.substring(2, 4);
        }
        if (resp.length() > 4) {
            rm = resp.substring(4);
        }
        writeFromBackground("Card Response : " + resp);

        if (rc.equals("61") && !rl.equals("")) {
            cmd = "00C00000" + rl;
            writeFromBackground("Send : " + cmd);
            byteArrayAPDU = StringLib.hexStringToByteArray(cmd);
            nAPDULength = byteArrayAPDU.length;
            nrLen = Integer.parseInt(rl, 16);
            byteArrayResponse = new byte[256];
            val = ContactICCardReaderInterface.transmit(nCardHandle, byteArrayAPDU, nAPDULength, byteArrayResponse);
            writeFromBackground("Transmit : " + val);
            resp = StringLib.toHexString(byteArrayResponse, 0, val, false);
            writeFromBackground("Card Response : " + resp);
            rc = resp.substring(0, 2);
            if (resp.length() > 3) {
                rl = resp.substring(2, 4);
                nrLen = Integer.parseInt(rl, 16);
            }
            if (resp.length() > 4) {
                rm = resp.substring(4, (nrLen * 2) + 4);
            }

            fci = new FileControlInfo(resp);
        }
        return val;
    }

    private int getProcessingOptions() {
        int val = 0;
        String resp = "";
        String rc = "";
        String rl = "";
        String rm = "";
        int nrLen = 0;
        String cmd = "";
        aip = null;
        afl = null;
        if (fci.isValid()) {
//            Map<String, String> fciValue = fci.getFciValue();
//            writeFromBackground("50: " + fciValue.get("50"));
//            writeFromBackground("9F38: " + fciValue.get("9F38"));
//            writeFromBackground("9F12: " + fciValue.get("9F12"));
//            writeFromBackground("ST: " + fci.gethStatus());

            // GPO
            // String termCurrCode = "0360";
            //bytelenTCC = 02
            //tag = 83
            //bytelenLc = 04
            //GPO ClaIns = 80A8
            //P1 = 00;P2 = 00
            //Le = 00
            cmd = "80A80000048302036000";
//            writeFromBackground("Send : " + cmd);
            byteArrayAPDU = StringLib.hexStringToByteArray(cmd);
            nAPDULength = byteArrayAPDU.length;
            byteArrayResponse = new byte[256];
            val = ContactICCardReaderInterface.transmit(nCardHandle, byteArrayAPDU, nAPDULength, byteArrayResponse);
//            writeFromBackground("Transmit : " + val);
            resp = StringLib.toHexString(byteArrayResponse, 0, val, false);
            rc = resp.substring(0, 2);
            if (resp.length() > 3) {
                rl = resp.substring(2, 4);
            }
            if (resp.length() > 4) {
                rm = resp.substring(4);
            }
//            writeFromBackground("Card Response : " + resp);
            if (rc.equals("61") && !rl.equals("")) {
                cmd = "00C00000" + rl;
//                writeFromBackground("Send : " + cmd);
                byteArrayAPDU = StringLib.hexStringToByteArray(cmd);
                nAPDULength = byteArrayAPDU.length;
                nrLen = Integer.parseInt(rl, 16);
                byteArrayResponse = new byte[256];
                val = ContactICCardReaderInterface.transmit(nCardHandle, byteArrayAPDU, nAPDULength, byteArrayResponse);
//                writeFromBackground("Transmit : " + val);
                resp = StringLib.toHexString(byteArrayResponse, 0, val, false);
//                writeFromBackground("Card Response : " + resp);
                rc = resp.substring(0, 2);
                if (resp.length() > 3) {
                    rl = resp.substring(2, 4);
                    nrLen = Integer.parseInt(rl, 16);
                }
                if (resp.length() > 4) {
                    rm = resp.substring(4, (nrLen * 2) + 4);
                }

                String rawData = resp;
                SingleTagParser stp = new SingleTagParser("77", 0, rawData);
                String rmtp = stp.getHval();
                rawData = stp.getRawResult();
                stp = new SingleTagParser("82", 1, rawData);
                aip = stp.getHval();
                rawData = stp.getRawResult();
                stp = new SingleTagParser("94", 1, rawData);
                afl = stp.getHval();
                rawData = stp.getRawResult();
//                writeFromBackground("AIP : " + aip);
//                writeFromBackground("AFL : " + afl);
//                writeFromBackground("Status : " + rawData);

            }
        }
        return  val;

    }

    private int readRecords() {
        return readRecords(null);
    }

    private int readRecords(String singleSFI) {
        int val = 0;
        String resp = "";
        String rc = "";
        String rl = "";
        String rm = "";
        int nrLen = 0;
        String cmd = "";

        // Read Record based on AFL
        // SFI 08010100 -> 0x08 (+4 or 0b100) = 0x0C has 1 rec from 01 to 01 [010C]
        // SFI 10010300 -> 0x10 (+4 or 0b100) = 0x14 has 3 rec from 01 to 03 [0114,0214,0314]
        // SFI 18010201 -> 0x18 (+4 or 0b100) = 0x1C has 2 rec from 01 to 02 [011C,021C]
        // SFI 20010200 -> 0x20 (+4 or 0b100) = 0x24 has 2 rec from 01 to 02 [0124,0224]
//        writeFromBackground(getSfiFromAfl(afl).toString());
//        String[] sfis = {"010C", "0114", "0214", "0314", "011C", "021C", "0124", "0224"};
        String[] sfis = getSfiFromAfl(afl);
        if (singleSFI != null) {
            sfis = new String[1];
            sfis[0] = singleSFI;
        }

        for (int sfiidx = 0; sfiidx < sfis.length; sfiidx++) {

            String sficode = sfis[sfiidx];
            try {
                Thread.sleep(100);
            } catch (Exception e) {

            }
            cmd = "00B2" + sficode + "00";
            writeFromBackground("Send : " + cmd);
            byteArrayAPDU = StringLib.hexStringToByteArray(cmd);
            nAPDULength = byteArrayAPDU.length;
            byteArrayResponse = new byte[256];
            val = ContactICCardReaderInterface.transmit(nCardHandle, byteArrayAPDU, nAPDULength, byteArrayResponse);
            writeFromBackground("Transmit : " + val);
            resp = StringLib.toHexString(byteArrayResponse, 0, val, false);
            rc = resp.substring(0, 2);
            if (resp.length() > 3) {
                rl = resp.substring(2, 4);
            }
            if (resp.length() > 4) {
                rm = resp.substring(4);
            }
            writeFromBackground("Card Response : " + resp);
            if (rc.equals("6C") && !rl.equals("")) {
                cmd = "00B2" + sficode + rl;
                writeFromBackground("Send : " + cmd);
                byteArrayAPDU = StringLib.hexStringToByteArray(cmd);
                nAPDULength = byteArrayAPDU.length;
                nrLen = Integer.parseInt(rl, 16);
                byteArrayResponse = new byte[256];
                val = ContactICCardReaderInterface.transmit(nCardHandle, byteArrayAPDU, nAPDULength, byteArrayResponse);
                writeFromBackground("Transmit : " + val);
                resp = StringLib.toHexString(byteArrayResponse, 0, val, false);
                writeFromBackground("Card Response : " + resp);
                rc = resp.substring(0, 2);
                if (resp.length() > 3) {
                    rl = resp.substring(2, 4);
                    nrLen = Integer.parseInt(rl, 16);
                }
                if (resp.length() > 4) {
                    rm = resp.substring(4, (nrLen * 2) + 4);
                }

                writeFromBackground(sficode + " : " + resp.substring(0,10) + "... ");
                writeFromBackground(sficode + " : " + resp);
                String rawData = resp;
                boolean hasTag = true;
                while (hasTag) {
                    if (rawData.length()<5) {
                        break;
                    }
                    hasTag = false;
                    String xTag = rawData.substring(0,2);
                    if (!CommonConfig.KNOWN_TAG_MAP().containsKey(xTag)) {
                        xTag = rawData.substring(0,4);
                    } else {
                        hasTag = true;
                    }
                    if (!hasTag) {
                        if (CommonConfig.KNOWN_TAG_MAP().containsKey(xTag)) {
                            hasTag = true;
                        }
                    }
                    if (hasTag) {
                        try {
                            writeFromBackground("Parsing " + xTag);
                            SingleTagParser stp = new SingleTagParser(xTag, CommonConfig.KNOWN_TAG_MAP().get(xTag), rawData);
                            recordValues.put(xTag, stp.getHval());
                            rawData = stp.getRawResult();
                            writeFromBackground("Value " + stp.getHval());
                        } catch (Exception e) {
                            writeFromBackground("STP Err : " + e.getMessage());
                        }
                    }
                }
            }
        }
        return val;
    }

    private String[] getSfiFromAfl(String afl) {
        List<String> arrayResult = new ArrayList<String>();
        String sfi = "";
        while (afl.length()>=8) {
            sfi = afl.substring(0,8);
//            writeFromBackground(sfi);
            afl = afl.substring(8);
            int idx = Integer.parseInt(sfi.substring(0,2), 16) + 4;
            int start = Integer.parseInt(sfi.substring(2,4), 16);
            int end = Integer.parseInt(sfi.substring(4,6), 16);
            for (int x=start;x<=end;x++) {
                String ressfi = String.format("%02d", x) + String.format("%02x", idx).toUpperCase();
//                writeFromBackground(ressfi);
                arrayResult.add(ressfi);
            }
        }
        String[] result = new String[arrayResult.size()];
        result = arrayResult.toArray(result);
        return result;
    }

    private String getCardRandom() {
        int val = 0;
        String resp = "";
        String rc = "";
        String rl = "";
        String rm = "";
        int nrLen = 0;
        // Get Random
        String cmd = "0084000000";
//        writeFromBackground("Send : " + cmd);
        byteArrayAPDU = StringLib.hexStringToByteArray(cmd);
        nAPDULength = byteArrayAPDU.length;
        byteArrayResponse = new byte[256];
        val = ContactICCardReaderInterface.transmit(nCardHandle, byteArrayAPDU, nAPDULength, byteArrayResponse);
//        writeFromBackground("Transmit : " + val);
        resp = StringLib.toHexString(byteArrayResponse, 0, val, false);
        rc = resp.substring(0, 2);
        if (resp.length() > 3) {
            rl = resp.substring(2, 4);
        }
        if (resp.length() > 4) {
            rm = resp.substring(4);
        }
//        writeFromBackground("Card Response : " + resp);

        if (rc.equals("6C") && !rl.equals("")) {
            cmd = "00840000" + rl;
//            writeFromBackground("Send : " + cmd);
            byteArrayAPDU = StringLib.hexStringToByteArray(cmd);
            nAPDULength = byteArrayAPDU.length;
            nrLen = Integer.parseInt(rl, 16);
            byteArrayResponse = new byte[256];
            val = ContactICCardReaderInterface.transmit(nCardHandle, byteArrayAPDU, nAPDULength, byteArrayResponse);
//            writeFromBackground("Transmit : " + val);
            resp = StringLib.toHexString(byteArrayResponse, 0, val, false);
//            writeFromBackground("Card Response : " + resp);

        }
        String retVal = "";
        if (resp.length()>=16) {
            retVal = resp.substring(0,16);
        }
        return retVal;
    }

    private int doGenerateAC(String p1, String cdol) {
        int val = 0;
        String resp = "";
        String rc = "";
        String rl = "";
        String rm = "";
        int nrLen = 0;
        String cmd = "80AE" + p1 + "00";
        String data = "";
        writeFromBackground(cdol);
        String[] cdolTemplate = getCdolTemplate(cdol);
        for (int ic=0;ic<cdolTemplate.length;ic++) {

            if (recordValues.containsKey(cdolTemplate[ic])) {
                data += recordValues.get(cdolTemplate[ic]);
            } else {
                writeFromBackground("No rec : " + cdolTemplate[ic]);
                return -1;
            }
        }
        int ilen = data.length();
        String lc = Integer.toHexString(ilen/2).toUpperCase();
        cmd += lc;
        cmd += data;
        cmd += "00";
        writeFromBackground("Send : " + cmd);
        byteArrayAPDU = StringLib.hexStringToByteArray(cmd);
        nAPDULength = byteArrayAPDU.length;
        byteArrayResponse = new byte[256];
        val = ContactICCardReaderInterface.transmit(nCardHandle, byteArrayAPDU, nAPDULength, byteArrayResponse);
        writeFromBackground("Transmit : " + val);
        resp = StringLib.toHexString(byteArrayResponse, 0, val, false);
        rc = resp.substring(0, 2);
        if (resp.length() > 3) {
            rl = resp.substring(2, 4);
        }
        if (resp.length() > 4) {
            rm = resp.substring(4);
        }
        writeFromBackground("Card Response : " + resp);

        if (rc.equals("6C") && !rl.equals("")) {
            cmd = cmd.substring(0, cmd.length()-2) + rl;
            writeFromBackground("Send : " + cmd);
            byteArrayAPDU = StringLib.hexStringToByteArray(cmd);
            nAPDULength = byteArrayAPDU.length;
            nrLen = Integer.parseInt(rl, 16);
            byteArrayResponse = new byte[256];
            val = ContactICCardReaderInterface.transmit(nCardHandle, byteArrayAPDU, nAPDULength, byteArrayResponse);
            writeFromBackground("Transmit : " + val);
            resp = StringLib.toHexString(byteArrayResponse, 0, val, false);
            writeFromBackground("Card Response : " + resp);
            rc = resp.substring(0, 2);
            if (resp.length() > 3) {
                rl = resp.substring(2, 4);
                nrLen = Integer.parseInt(rl, 16);
            }
            if (resp.length() > 4) {
                rm = resp.substring(4, (nrLen * 2) + 4);
            }
        } else if (rc.equals("61") && !rl.equals("")) {
            cmd = "00C00000" + rl;
            byteArrayAPDU = StringLib.hexStringToByteArray(cmd);
            nAPDULength = byteArrayAPDU.length;
            nrLen = Integer.parseInt(rl, 16);
            byteArrayResponse = new byte[256];
            val = ContactICCardReaderInterface.transmit(nCardHandle, byteArrayAPDU, nAPDULength, byteArrayResponse);
            writeFromBackground("Transmit : " + val);
            resp = StringLib.toHexString(byteArrayResponse, 0, val, false);
            writeFromBackground("Card Response : " + resp);
            rc = resp.substring(0, 2);
            if (resp.length() > 3) {
                rl = resp.substring(2, 4);
                nrLen = Integer.parseInt(rl, 16);
            }
            if (resp.length() > 4) {
                rm = resp.substring(4, (nrLen * 2) + 4);
            }
        }
        String rawData = resp;
        if (p1.equals("80")) {
            if (rawData.startsWith("80") || rawData.startsWith("77")) {
                boolean hasTag = true;
                while (hasTag) {
                    if (rawData.length() < 5) {
                        break;
                    }
                    hasTag = false;
                    String xTag = rawData.substring(0, 2);
                    if (!CommonConfig.KNOWN_TAG_MAP().containsKey(xTag)) {
                        xTag = rawData.substring(0, 4);
                    } else {
                        hasTag = true;
                    }
                    if (!hasTag) {
                        if (CommonConfig.KNOWN_TAG_MAP().containsKey(xTag)) {
                            hasTag = true;
                        }
                    }
                    if (hasTag) {
                        try {
                            writeFromBackground("Parsing " + xTag);
                            SingleTagParser stp = new SingleTagParser(xTag, CommonConfig.KNOWN_TAG_MAP().get(xTag), rawData);
                            recordValues.put(xTag, stp.getHval());
                            writeFromBackground("Value " + stp.getHval());
                            if (xTag.equals("9F10")) {
                                nsiccsData.setIad(stp.getHval());
                            }
                            rawData = stp.getRawResult();
                        } catch (Exception e) {
                            writeFromBackground("STP Err : " + e.getMessage());
                        }
                    }
                }
            } else {
                // return no TLV only primitive values len 35
                // CID[1],ATC[2],ARQC[8],IAD[rest] all zero
                if (rawData.length() > 22) {
                    String respCid = rawData.substring(0, 2);
                    String respAtc = rawData.substring(2, 6);
                    String respArqc = rawData.substring(6, 22);
                    String respIad = rawData.substring(22);
                    recordValues.put("9F27", respCid);

                    recordValues.put("9F36", respAtc);
                    nsiccsData.setAtc(respAtc);
                    recordValues.put("9F26", respArqc);
                    nsiccsData.setArqc(respArqc);
                    recordValues.put("9F10", respIad);
                    nsiccsData.setIad(respIad);
                }
            }
        } else { // "40"
            nsiccsData.setTc(resp);
        }
        return val;
    }

    private String[] getCdolTemplate(String cdol) {
        String raw = cdol;
        List<String> arrayResult = new ArrayList<String>();
        try {
            boolean hasTag = true;
            while (hasTag) {
                if (raw.length() < 5) {
                    break;
                }
                hasTag = false;
                String xTag = raw.substring(0, 2);
                if (!CommonConfig.KNOWN_TAG_MAP().containsKey(xTag)) {
                    xTag = raw.substring(0, 4);
                } else {
                    hasTag = true;
                }
                if (!hasTag) {
                    if (CommonConfig.KNOWN_TAG_MAP().containsKey(xTag)) {
                        hasTag = true;
                    }
                }
                if (hasTag) {
                    arrayResult.add(xTag);
                    raw = raw.substring(xTag.length() + 2);
//                    writeFromBackground(xTag);
                }

            }
        } catch (Exception e) {
            writeFromBackground("parse CDOL Err : " + e.getMessage());
        }
        String[] result = new String[arrayResult.size()];
        result = arrayResult.toArray(result);

        return result;
    }

    private String composeDe55() {
        String result = "";
        String tag = "";
        writeFromBackground(aip);
        writeFromBackground("Composing DE55 ... ");
//        for (String k : recordValues.keySet()) {
//            writeFromBackground(k + " : " + recordValues.get(k));
//        }
        try {
//            String[] cHelper = {"82", "95", "5F2A", "5F34", "9A", "9C", "9F02",
//                    "9F03", "9F10", "9F1A", "9F26", "9F36", "9F37"};
            String[] cHelper = {"82", "9F36", "9F26", "9F27",
//                    "9F34",
                    "9F10",
//                    "9F33", "9F35",
                    "95", "9F37", "9F02", "9F03",
//                    "5A",
                    "5F34", "9F1A", "5F2A", "9A", "9C",
//                    "9F09", "9F1E", "4F",
                    "84"
//                    "9F41", "9F69", "9F6A"
            };
            for (int i = 0; i < cHelper.length; i++) {
                tag = cHelper[i];
//                writeFromBackground(tag + "[" + recordValues.get(tag).length() + "]");
//                writeFromBackground(recordValues.get(tag));
                result += tag;
                String val = recordValues.get(tag);
                String vlen = String.format("%02X",val.length()/2);
                result += vlen;
                result += val;
            }
//            if (recordValues.containsKey("9F27")) {
//                tag = "9F27";
//                result += tag;
//                String val = recordValues.get(tag);
//                String vlen = String.format("%02X",val.length()/2);
//                result += vlen;
//                result += val;
//            }
//            if (recordValues.containsKey("84")) {
//                tag = "84";
//                result += tag;
//                String val = recordValues.get(tag);
//                String vlen = String.format("%02X",val.length()/2);
//                result += vlen;
//                result += val;
//            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter ps = new PrintWriter(sw);
            e.printStackTrace(ps);
            writeFromBackground(tag + " Not found ?");
            writeFromBackground(sw.toString());
            return "";
        }
        return result;
    }

    private int externalAuth() {
        int val = 0;
        String resp = "";
        String rc = "";
        String rl = "";
        String rm = "";
        int nrLen = 0;
        JSONArray preProcessingCmd = new JSONArray();
        List<String> preProcessingResult = new ArrayList<>();
        JSONArray postProcessingCmd = new JSONArray();
        List<String> postProcessingResult = new ArrayList<>();

        String rawData = nsiccsData.getArpc();
        try {
            JSONObject jsonData = parseHostResponse(rawData);
            if (jsonData.has("arpc")) {
                String arpc = jsonData.getString("arpc");
                recordValues.put("91", arpc);
                nsiccsData.setArpc(arpc);
            } else {
                nsiccsData.setArpc(null);
            }
            if (jsonData.has("arc")) {
                String arc = jsonData.getString("arc");
                recordValues.put("8A", arc);
                nsiccsData.setArc(arc);
            }
            if (jsonData.has("pre")) {
                preProcessingCmd = jsonData.getJSONArray("pre");
            }
            if (jsonData.has("post")) {
                postProcessingCmd = jsonData.getJSONArray("post");
            }
        } catch (Exception e) {

        }

        if (nsiccsData.getArpc() == null || nsiccsData.getArc() == null) {
            writeFromBackground("Cannot parse response from host, ARPC/ARC not found");
            nsiccsData.setTxst(CommonConfig.ICC_VALIDATION_FAILED);
            nsiccsData.setTc(null);
//            nsiccsData.setTxst(CommonConfig.ICC_VALIDATION_OK);
//            nsiccsData.setTc(recordValues.get("9F26"));
            return  val;
        }

        boolean prePassed = true;
        String cmd = "";
        int counter = 0;

        if (preProcessingCmd.length()>0) {
            // do pre cmd (71)
            try {
                for (int i=0;i<preProcessingCmd.length();i++) {
                    JSONObject preCmd = preProcessingCmd.getJSONObject(i);
                    if (preCmd.has("86")) {
                        cmd = preCmd.getString("86");
                        writeFromBackground("Send : " + cmd);
                        byteArrayAPDU = StringLib.hexStringToByteArray(cmd);
                        nAPDULength = byteArrayAPDU.length;
                        byteArrayResponse = new byte[256];
                        val = ContactICCardReaderInterface.transmit(nCardHandle, byteArrayAPDU, nAPDULength, byteArrayResponse);
                        writeFromBackground("Transmit : " + val);
                        resp = StringLib.toHexString(byteArrayResponse, 0, val, false);
                        writeFromBackground("Card Response : " + resp);
                        if (resp.startsWith("90") || resp.startsWith("62") || resp.startsWith("63")) {
                            writeFromBackground(resp);

                        } else {
                            prePassed = false;
                            break;
                        }
                    }
                    counter = i;
                }

            } catch (Exception e) {

            }
        }

        if (!prePassed) {
            writeFromBackground("Issuer Script Failed at Pre Process Command #" + counter);
            nsiccsData.setTxst(CommonConfig.ICC_VALIDATION_FAILED);
            nsiccsData.setTc(null);
            return  val;
        }

        cmd = "008200000A" + nsiccsData.getArpc() + nsiccsData.getArc();
        writeFromBackground("Send : " + cmd);
        byteArrayAPDU = StringLib.hexStringToByteArray(cmd);
        nAPDULength = byteArrayAPDU.length;
        byteArrayResponse = new byte[256];
        val = ContactICCardReaderInterface.transmit(nCardHandle, byteArrayAPDU, nAPDULength, byteArrayResponse);
        writeFromBackground("Transmit : " + val);
        resp = StringLib.toHexString(byteArrayResponse, 0, val, false);
        writeFromBackground("Card Response : " + resp);

        if (resp.equals("9000")) { //(true) { //
            writeFromBackground("Authentication approved");
            nsiccsData.setTxst(CommonConfig.ICC_VALIDATION_OK);
            nsiccsData.setTc("Cannot get transaction certificate");

            String p1genTC = "40"; // generate TC using Default TDOL
//            val = doGenerateAC(p1genTC, nsiccsData.getTdol());
            String txCert = "";
            if (recordValues.containsKey("9F26")) {
                txCert = recordValues.get("9F26");
            }
//            if (nsiccsData.getArpc()!=null) {
//                txCert += nsiccsData.getArpc();
//            }
            nsiccsData.setTc(txCert);
        } else {
            nsiccsData.setTxst(CommonConfig.ICC_VALIDATION_FAILED);
            nsiccsData.setTc(null);
        }

        boolean postPassed = true;

        if (postProcessingCmd.length()>0) {
            // do pre cmd (71)
            try {
                for (int i=0;i<postProcessingCmd.length();i++) {
                    JSONObject postCmd = postProcessingCmd.getJSONObject(i);
                    if (postCmd.has("86")) {
                        cmd = postCmd.getString("86");
                        writeFromBackground("Send : " + cmd);
                        byteArrayAPDU = StringLib.hexStringToByteArray(cmd);
                        nAPDULength = byteArrayAPDU.length;
                        byteArrayResponse = new byte[256];
                        val = ContactICCardReaderInterface.transmit(nCardHandle, byteArrayAPDU, nAPDULength, byteArrayResponse);
                        writeFromBackground("Transmit : " + val);
                        resp = StringLib.toHexString(byteArrayResponse, 0, val, false);
                        writeFromBackground("Card Response : " + resp);
                        if (resp.startsWith("90") || resp.startsWith("62") || resp.startsWith("63")) {
                            writeFromBackground(resp);

                        } else {
                            postPassed = false;
                            break;
                        }
                    }
                    counter = i;
                }

            } catch (Exception e) {

            }
        }

        if (!postPassed) {
            writeFromBackground("Issuer Script Failed at Post Process Command #" + counter);
            nsiccsData.setTxst(CommonConfig.ICC_VALIDATION_FAILED);
            nsiccsData.setTc(null);
            return  val;
        }

        return val;
    }

    private int verifyDAC() {
        int val = 0;
        String modulus = CommonConfig.NSICCS_CAPK_MOD;
        String exponent = CommonConfig.NSICCS_CAPK_EXP;
        try {
            RSAPublicKeySpec spec = new RSAPublicKeySpec(BigInteger.valueOf(Long.valueOf(modulus)), BigInteger.valueOf(Long.valueOf(exponent)));
            KeyFactory factory = KeyFactory.getInstance("RSA");
            PublicKey pub = factory.generatePublic(spec);

            String IssuerCert = recordValues.get("90");
        } catch (Exception e) {
            writeFromBackground("SDA Err : " + e.getMessage());
        }
        return val;
    }

    private JSONObject parseHostResponse(String resp) {
        JSONObject listProc = new JSONObject();
        try {
            JSONArray listPreProc = new JSONArray();
            JSONArray listPostProc = new JSONArray();
            String rawData = resp;
            boolean hasTag = true;
            boolean isPre = true;
            JSONObject holder = new JSONObject();
            String arpc = null;
            String arc = null;
            while (hasTag) {
                if (rawData.length() < 5) {
                    break;
                }
                hasTag = false;
                String xTag = rawData.substring(0, 2);
                if (!CommonConfig.KNOWN_TAG_MAP().containsKey(xTag)) {
                    xTag = rawData.substring(0, 4);
                } else {
                    hasTag = true;
                }
                if (!hasTag) {
                    if (CommonConfig.KNOWN_TAG_MAP().containsKey(xTag)) {
                        hasTag = true;
                    }
                }
                if (hasTag) {
                    try {
//                            writeFromBackground("Parsing " + xTag);
                        SingleTagParser stp = new SingleTagParser(xTag, CommonConfig.KNOWN_TAG_MAP().get(xTag), rawData);
                        if (xTag.equals("91")) {
                            arpc = stp.getHval();
                            if (arpc.length()==20) {
                                arc = arpc.substring(16);
                                arpc = arpc.substring(0,16);
                            }
                        }
                        if (xTag.equals("8A")) {
                            recordValues.put(xTag, stp.getHval());
                            nsiccsData.setArc(stp.getHval());
                            arc = stp.getHval();
                        }
                        if (xTag.equals("71")) {
                            isPre = true;
                            holder = new JSONObject();
                        }
                        if (xTag.equals("72")) {
                            isPre = false;
                            holder = new JSONObject();
                        }
                        if (xTag.equals("9F18")) {
                            holder.put("9F18", stp.getHval());
                        }
                        if (xTag.equals("86")) {
                            holder.put("86", stp.getHval());
                        }
                        if (holder.has("9F18") && holder.has("86")) {
                            if (isPre) {
                                listPreProc.put(holder);
                            } else {
                                listPostProc.put(holder);
                            }
                        }
                        rawData = stp.getRawResult();
                    } catch (Exception e) {
                        writeFromBackground("STP Err : " + e.getMessage());
                    }
                }
            }
            if (arpc.length()>16) {
                arpc = arpc.substring(0,16);
            }
            listProc.put("arpc", arpc);
            listProc.put("arc", arc);
            listProc.put("pre", listPreProc);
            listProc.put("post", listPostProc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listProc;
    }
}
