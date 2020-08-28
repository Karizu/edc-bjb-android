package id.co.tornado.billiton.module;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;

import com.wizarpos.apidemo.jniinterface.HALMsrInterface;
import com.wizarpos.jni.ContactICCardReaderInterface;
import com.wizarpos.jni.ContactICCardSlotInfo;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
public class SwipeOrInsert extends com.rey.material.widget.EditText {
    Context context;
    public String tag = "SoI";
    private byte track1[] = new byte[250];
    private byte track2[] = new byte[250];
    private byte track3[] = new byte[250];
    private byte msrKey[] = new byte[20];

    private boolean isQuit = false;
    private boolean isOpen = false;
    private int ret = -1;
    private LogOutput logOutput;
    private int checkCount = 0;
    private int successCount = 0;
    private int failCount = 0;

    private boolean isSCIOpen = false;
    private boolean iccReady = false;
    private static int CHIP_SLOT_INDEX = 0;
    private byte byteArrayATR[];
    private int nCardHandle = 0;
    private byte byteArrayAPDU[];
    private int nAPDULength;
    private byte byteArrayResponse[];

    private FileControlInfo fci;
    private NsiccsData cdol;
    private String aip;
    private String afl;
    private Map<String, String> recordValues = new HashMap<>();

    private List<InputListener> inputListeners = new ArrayList<>();
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                Bundle b = msg.getData();
                if (b != null) {
                    HashMap data = (HashMap) b.getSerializable("DATA");
                    writeLog("MSG RC : " + data.get("RC").toString());
                    if (data.get("RC").toString().equals("00")) {
//                        setText(data.get("track2").toString());
                        isQuit = true;
                        if (closeDriver()) {
                            writeLog("Swipe data completed");
                            for (InputListener listener : inputListeners) {

                                listener.onInputCompleted(SwipeOrInsert.this, data.get("track2").toString(), null, cdol);
                            }
//                            swipe(getText().toString());
//                        LinearLayout menu = (LinearLayout) getParent();
//                        int childCount = menu.getChildCount();
//                        for (int i = 0; i < childCount; i++) {
//                            if (menu.getChildAt(i) instanceof SwipeOrInsert) {
//                                int nextFocus = i + 1;
//                                if (menu.getChildAt(nextFocus) != null) {
//                                    View v = menu.getChildAt(nextFocus);
//                                    v.requestFocus();
//                                }
//                            }
//                        }

                        }
                    } else if (data.get("RC").toString().startsWith("S")) {
                        String respmsg = data.get("msg").toString();
                        for (InputListener listener : inputListeners) {
                            listener.onInputCompleted(SwipeOrInsert.this, respmsg, null, cdol);
                        }
                    } else {
                        closeDriver();
                    }
                }
            } catch (Exception e) {
                writeLog("handler Error : " + e.getMessage());
                if (isOpen) {
                    closeDriver();
                }
            }
        }
    };

    private Handler ICCHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle b = msg.getData();
            if (b != null) {
                HashMap data = (HashMap) b.getSerializable("DATA");
                writeLog(data.get("msg").toString());
            }
        }
    };

    public SwipeOrInsert(Context context) {
        super(context);
        this.context = context;
    }
    private void swipe(String string){
        writeLog("INFO_SWIPE",string);

    }
    public SwipeOrInsert(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SwipeOrInsert(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SwipeOrInsert(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public boolean closeDriver() {
        int val=0;
        try {
            val=HALMsrInterface.msr_close();
            writeLog("Close MSR : " + val);
            val=ContactICCardReaderInterface.close(nCardHandle);
            writeLog("Close SCI : " + val);
            val=ContactICCardReaderInterface.terminate();
            writeLog("Terminate SCI : " + val);
            writeLog("Closing Driver");
            isSCIOpen = false;
            isQuit = true;
            isOpen = false;
            iccReady = false;
        } catch (Exception e) {
            val=-1;
        }
        return val >= 0;
    }

    public boolean openDriver() {
        int val=0;
        writeLog("Opening MSR and SCSlot");
        try {
            val=HALMsrInterface.msr_open();
            writeLog("MSR : " + val);
            val=openEmvSlot();
            writeLog("SCI : " + val);
            isSCIOpen = true;
            isOpen = true;
        } catch (Exception e) {
            val=-1;
        }
        return val >= 0;
    }


    public void addInputListener(InputListener inputListener){
        inputListeners.add(inputListener);
    }
    public void init() {
        setEnabled(false);
        setKeyListener(null);
        if (!openDriver()) {
//            Log.d("MSR DRIVER", "Open Driver failed");
            writeLog("Open Driver failed!");

        } else {
//            setHint("Swipe Card Please!");
            writeLog("MSR DRIVER", "Open Driver succeed!");
            Thread t1 = new Thread(new GetData());
            t1.start();
            isOpen = true;
        }
    }

    private void readTrackData() {
        int ret;
        byte[] byteArry = new byte[255];
        int length = 255;

        ret = HALMsrInterface.msr_get_track_data(1, byteArry, length);
        HashMap<String, String> data = new HashMap<>();
        if (ret > 0) {
            String str = new String(byteArry, 0, ret);
            Message msg = new Message();
            Bundle b = new Bundle();

            data.put("RC", "00");
            data.put("track2", str);
            b.putSerializable("DATA", data);

            msg.setData(b);
            handler.sendMessage(msg);

//            writeLog("MSR_DRIVER", new String(byteArry, 0, ret));
        }else{
//            writeLog("MSR_DRIVER", "No Input");
            data.put("RC","05");
        }
    }

    public void setIsQuit(boolean isQuit) {
        this.isQuit = isQuit;
    }

    public void setIsOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    private class GetData implements Runnable {

        @Override
        public void run() {
            while (true) {
                int nReturn = -1;

                if (isQuit) {
                    break;
                } else {
                    nReturn = HALMsrInterface.msr_poll(2000);
                    if (nReturn >= 0) {
//                    Log.d("MSR_DRIVER", "New data found");
                        try {
                            readTrackData();
                            isQuit = true;
                        } catch (Exception e) {
                            //read failed
                        }
                    } else {
                        if (!isQuit) {
                            try {
                                com.wizarpos.jni.SmartCardEvent event = new com.wizarpos.jni.SmartCardEvent();
                                ContactICCardReaderInterface.pollEvent(2000, event);
                                if (event.nEventID == 0) {
                                    readSCI();
                                    isQuit = true;
                                } else {
                                    writeFromBackground("ICC no card");
                                }
                            } catch (Exception e) {

                            }
                        }
                    }
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
            writeLog("SCI Init : " + val);
            val = ContactICCardReaderInterface.open(CHIP_SLOT_INDEX);
            writeLog("SCI Open : " + val);
            nCardHandle = val;

        } catch (Exception e) {
            writeLog(e.getMessage());
        }

        return val;
    }

    public boolean isSCIPresent(boolean present) {
        boolean res = false;
        HashMap<String, String> data = new HashMap<>();
        Message msg = new Message();
        Bundle b = new Bundle();
        int val = 0;
        if (isSCIOpen) {
            try {

                data.put("msg", "PoolEvent : " + String.valueOf(present));

                msg.setData(b);
                if (val > 0) {
                    res = present;
                }
            } catch (Exception e) {
                data.put("msg", e.getMessage());
            }
        } else {
            data.put("msg", "SCI not Open");
        }
        b.putSerializable("DATA", data);
        ICCHandler.sendMessage(msg);
        return res;
    }

    public void readSCI() {
        int val = 0;
//        writeLog("Reading Card");
        HashMap<String, String> data = new HashMap<>();
        Message msg = new Message();
        Bundle b = new Bundle();
        String resp = "";
        String rc = "";
        String rl = "";
        String rm = "";
        int nrLen = 0;
        try {

            byteArrayATR = new byte[64];
            ContactICCardSlotInfo mSlotInfo = new ContactICCardSlotInfo();
            val = ContactICCardReaderInterface.powerOn(nCardHandle, byteArrayATR, mSlotInfo);
            writeFromBackground("Power On : " + val);

            if (val >= 0) {

                iccReady = true;

                // terminal init
                cdol = new NsiccsData();
                cdol.setTvr("8080040000");
                cdol.setCurrency("0360");
                cdol.setTxtype("00");
                cdol.setCountry("0360");


                // changed to single support
                List<String> appList = new ArrayList<>();
                appList.add("A0000006021010"); //NSICCS
//                appList.add("A0000000043060"); // Maestro Debit
//                appList.add("A0000000046000");  // Cirrus
//                appList.add("A0000000031010"); // VISA Debit

                String supportedAppId = "A0000006021010";
                val = selectAppById(supportedAppId);
            }
            if (val>0 && fci.isValid()) {
                writeFromBackground("Select AID : OK");
                val = getProcessingOptions();
            }
            if (val>0 && aip!=null) {
                writeFromBackground("Get PO : OK");
                cdol.setAip(aip);
                val = readRecords();
            }
            if (val>0 && recordValues.size()>0) {
                writeFromBackground("Read Records : OK");
                cdol.setTrack2(recordValues.get("57"));
                cdol.setPanseq(recordValues.get("5F34"));
                cdol.setPan(recordValues.get("5A"));
                cdol.setCdol(recordValues.get("8C"));
                writeFromBackground("PAN : " + recordValues.get("5A"));
                recordValues.put("95", cdol.getTvr());
                recordValues.put("9F1A", cdol.getCountry());
                recordValues.put("9C", cdol.getTxtype());
                recordValues.put("5F2A", cdol.getCurrency());
                recordValues.put("82", aip);
            }
            if (val>0) {
                writeFromBackground("Generate AC with dummy");

                String dummyAmt = "000000010000";
                String dummyAddAmt = "000000000000";
                SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");

                cdol.setTxdate(sdf.format(new Date()));
                recordValues.put("9A", cdol.getTxdate());

                cdol.setAmount(dummyAmt);
                recordValues.put("9F02", dummyAmt);
                cdol.setAddamount(dummyAddAmt);
                recordValues.put("9F03", dummyAddAmt);

//                String termRandom = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                Random rnd = new Random();
                int n = 10000000 + rnd.nextInt(90000000);
                String termRandom = String.valueOf(n);
                cdol.setTermRandomNum(termRandom);
                recordValues.put("9F37", termRandom);

                try {

                    String cardRandom = getCardRandom();
                    if (!cardRandom.equals("")) {
                        cdol.setCardRandomNum(cardRandom);
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
                val = doGenerateAC();
                writeFromBackground("Generate AC : OK");
            }
            String cResp = "";
            String de55 = "";
            if (val>0) {

                cResp = cdol.getTrack2();
                de55 = composeDe55();
            }
            if (!de55.equals("")) {
                cResp += "[]" + de55;
                data.put("RC", "S0");
                data.put("msg", "Card Response : " + cResp);
            } else {
                data.put("RC", "S5");
                data.put("msg", "Card not ready");
            }
        } catch (Exception e) {
            data.put("RC", "S5");
            data.put("msg", e.getMessage());
        }
        b.putSerializable("DATA", data);
        msg.setData(b);
        handler.sendMessage(msg);
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
        ICCHandler.sendMessage(msg);
    }

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

        if (rc.equals("61") && !rl.equals("")) {
            cmd = "00C00000" + rl;
//            writeFromBackground("Send : " + cmd);
            byteArrayAPDU = StringLib.hexStringToByteArray(cmd);
            nAPDULength = byteArrayAPDU.length;
            nrLen = Integer.parseInt(rl, 16);
            byteArrayResponse = new byte[256];
            val = ContactICCardReaderInterface.transmit(nCardHandle, byteArrayAPDU, nAPDULength, byteArrayResponse);
//            writeFromBackground("Transmit : " + val);
            resp = StringLib.toHexString(byteArrayResponse, 0, val, false);
//            writeFromBackground("Card Response : " + resp);
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
//            writeFromBackground(fciValue.get("50"));
//            writeFromBackground(fciValue.get("9F38"));
//            writeFromBackground(fciValue.get("9F12"));
//            writeFromBackground(fci.gethStatus());

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

        for (int sfiidx = 0; sfiidx < sfis.length; sfiidx++) {

            String sficode = sfis[sfiidx];
            try {
                Thread.sleep(300);
            } catch (Exception e) {

            }
            cmd = "00B2" + sficode + "00";
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
            if (rc.equals("6C") && !rl.equals("")) {
                cmd = "00B2" + sficode + rl;
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

//                writeFromBackground(sficode + " : " + resp.substring(0,10) + "... ");
//                writeFromBackground(sficode + " : " + resp);
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
//                            writeFromBackground("Parsing " + xTag);
                            SingleTagParser stp = new SingleTagParser(xTag, CommonConfig.KNOWN_TAG_MAP().get(xTag), rawData);
                            recordValues.put(xTag, stp.getHval());
                            rawData = stp.getRawResult();
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

    private int doGenerateAC() {
        int val = 0;
        String resp = "";
        String rc = "";
        String rl = "";
        String rm = "";
        int nrLen = 0;
        String p1 = "80"; // req ARQC
        String cmd = "80AE" + p1 + "00";
        String data = "";
        String[] cdolTemplate = getCdolTemplate(cdol.getCdol());
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
//            writeFromBackground("Send : " + cmd);
            byteArrayAPDU = StringLib.hexStringToByteArray(cmd);
            nAPDULength = byteArrayAPDU.length;
            nrLen = Integer.parseInt(rl, 16);
            byteArrayResponse = new byte[256];
            val = ContactICCardReaderInterface.transmit(nCardHandle, byteArrayAPDU, nAPDULength, byteArrayResponse);
//            writeFromBackground("Transmit : " + val);
            resp = StringLib.toHexString(byteArrayResponse, 0, val, false);
//            writeFromBackground("Card Response : " + resp);
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
//                            writeFromBackground("Parsing " + xTag);
                        SingleTagParser stp = new SingleTagParser(xTag, CommonConfig.KNOWN_TAG_MAP().get(xTag), rawData);
                        recordValues.put(xTag, stp.getHval());
                        if (xTag.equals("9F10")) {
                            cdol.setIad(stp.getHval());
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
            String respCid = rawData.substring(0,2);
            String respAtc = rawData.substring(2,6);
            String respArqc = rawData.substring(6,22);
            String respIad = rawData.substring(22);
            recordValues.put("9F27", respCid);

            recordValues.put("9F36", respAtc);
            cdol.setAtc(respAtc);
            recordValues.put("9F26", respArqc);
            cdol.setArqc(respArqc);
            recordValues.put("9F10", respIad);
            cdol.setIad(respIad);
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
        writeFromBackground("Composing DE55 ... ");
        try {
            String[] cHelper = {"82", "95", "5F2A", "5F34", "9A", "9C", "9F02", "9F03", "9F10", "9F1A", "9F26", "9F36", "9F37"};
            for (int i = 0; i < cHelper.length; i++) {
                tag = cHelper[i];
//                writeFromBackground(tag + "[" + recordValues.get(tag).length() + "]");
//                writeFromBackground(recordValues.get(tag));
                result += tag;
                String val = recordValues.get("tag");
                String vlen = Integer.toHexString(val.length()/2).toUpperCase();
                result += vlen;
                result += val;
            }
        } catch (Exception e) {
            writeFromBackground(tag + " Not found ?");
            return "";
        }
        return result;
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
}
