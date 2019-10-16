package id.co.tornado.billiton.common;

import java.util.HashMap;
import java.util.Map;

import id.co.tornado.billiton.handler.SingleTagParser;
import id.co.tornado.billiton.module.listener.LogOutput;

/**
 * Created by imome on 1/10/2019.
 */

public class FileControlInfo {
    String rawFci;
//    String hFCITemp; // 6F
//    String hDFName; // 84
//    String hFCIProp; // A5
//    String hAppLabel; // 50
//    String hAppPriorityIndicator; // 87
//    String hPDOL; // 9F38
//    String hLang; // 5F2D
//    String hIssuerCodeTableIndex; // 9F11
//    String hAppPrefName; // 9F12
//    String hFICIDD; // BF0C
//    String hLogEntry; // 9F4D
    String hStatus; // trail (2 byte)
    boolean valid;

    String stag;
    String htag;
    int tlen;
    String hlen;
    String hval;
    int vlen;
    int cpflag;
    String[] taglist = {"6F","84","A5","50","87","9F38","5F2D","9F11","9F12","BF0C","9F4D"};
    Map<String, String> fciValue = new HashMap<>();

    public FileControlInfo(String rawFci) {
        this.rawFci = rawFci;
        parseRaw();
    }

    private void parseRaw() {
        String rawData = this.rawFci;
        if (!rawData.startsWith("6F")) {
            //not valid
            this.rawFci = null;
            valid = false;
            return;
        }
        if (!rawData.endsWith("9000")) {
            //not valid
            this.rawFci = null;
            valid = false;
            return;
        }

        try {
            boolean hasTag = true;
            while (hasTag) {
                if (rawData.length()<5) {
                    break;
                }
                hasTag = false;
                stag = rawData.substring(0,2);
                if (!CommonConfig.KNOWN_TAG_MAP().containsKey(stag)) {
                    stag = rawData.substring(0,4);
                } else {
                    hasTag = true;
                }
                if (!hasTag) {
                    if (CommonConfig.KNOWN_TAG_MAP().containsKey(stag)) {
                        hasTag = true;
                    }
                }
                if (hasTag) {
                    cpflag = (int) CommonConfig.KNOWN_TAG_MAP().get(stag);
                    SingleTagParser stp = new SingleTagParser(stag, cpflag, rawData);
                    hval = stp.getHval();
                    rawData = stp.getRawResult();
                    fciValue.put(stag, hval);
                }
            }
//            for (int idx=0;idx<taglist.length;idx++) {
//                stag = taglist[idx];
////                logOutput.writeFromBackground("Parsing : " + stag);
//                cpflag = (int) CommonConfig.KNOWN_TAG_MAP().get(stag);
//                SingleTagParser stp = new SingleTagParser(stag, cpflag, rawData);
//                hval = stp.getHval();
//                rawData = stp.getRawResult();
//                fciValue.put(stag, hval);
////                logOutput.writeFromBackground(hval);
//            }
            hStatus = rawData;
            valid = true;

            //recheck
            if (fciValue.get("50").startsWith("4E5349434343")) { // Not NSICCS
                valid = false;
            }
//            logOutput.writeFromBackground("parse Done " + hStatus);

        } catch (Exception e) {
            this.rawFci = null;
            valid = false;
        }
    }

    public String gethStatus() {
        return hStatus;
    }

    public boolean isValid() {
        return valid;
    }

    public Map<String, String> getFciValue() {
        return fciValue;
    }
}
