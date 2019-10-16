/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package id.co.tornado.billiton.handler;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

import id.co.tornado.billiton.common.StringLib;

/**
 *
 * @author Ahmad
 */
public class LogHandler {

    String serviceid;
    private Cursor c;
    private Context ctx;
    private DataBaseHelper helperDb;
    SQLiteDatabase clientDB = null;
    private Boolean ignoreReplyAmount;

    public LogHandler(Context ctx) {
        this.ctx = ctx;
        try {
            helperDb = new DataBaseHelper(ctx);
            clientDB = helperDb.getActiveDatabase();
        } catch (Exception ex) {
            //
        }
        ignoreReplyAmount = false;
    }

//    public String getLastAmountDeduct(String cardNumber){
//        Log.d("EDCLOG", "read amount from handler (44)");
//        String qry = "select amount from edc_log  where track2  = '"+cardNumber+"' and service_id = 'A24100' order by rqtime desc limit 1;";
//
//        Cursor c = clientDB.rawQuery(qry, null);
//        if (c.moveToFirst()) {
//            return c.getString(0);
//        } else {
//            return "";
//        }
//    }
//
//    public void writeOfflineLog(String service_id, String NomorKartu, String amount, String Proccode, String rc) {
//        int logid = 0;
//        Log.d("EDCLOG", "read seq from handler (57)");
//        String qry = "select max(log_id) from edc_log ";
//        Cursor mxid = clientDB.rawQuery(qry, null);
//        if (mxid.moveToFirst()) {
//            logid = mxid.getInt(0) + 1;
//        } else {
//            logid = 1;
//        }
//        Log.d("EDCLOG", "insert from handler (65)" + service_id + ";" + "000012");
//        String newLog = "insert or replace into edc_log("
//                + "log_id, service_id, stan, track2, amount, proccode, rc) values "
//                + "("+String.valueOf(logid)
//                +",'"+service_id+"',"
//                + "'000012"
//                +"','"+NomorKartu
//                +"',"+amount
//                +", '"+Proccode
//                +"', '"+rc
//                +"')";
//        clientDB.execSQL(newLog);
//        clientDB.close();
//    }
//
    public int writePreLog(String[] IsoBitValues, String serviceId, String messageId) {
        int logid = 0;
        Log.d("EDCLOG", "read seq from handler (82)");
        String qry = "select max(log_id) from edc_log ";
        Cursor mxid = clientDB.rawQuery(qry, null);
        if (mxid.moveToFirst()) {
            if (mxid !=null) {
                logid = mxid.getInt(0) + 1;
            } else {
                logid = 1;
            }
        } else {
            logid = 1;
        }
        if(IsoBitValues != null){
            String amount = null;
            if (IsoBitValues[4]!=null) {
                amount = IsoBitValues[4];
            }
            if (IsoBitValues[48] != null) {
                if (serviceId.equals("A54322") || serviceId.equals("A54331")) {
                    amount = IsoBitValues[48].substring(88, 97) + "00";
                }
            }
            Log.d("EDCLOG", "insert  from handler (44)");
            String newLog = "insert or replace into edc_log("
                    + "log_id, service_id, stan, track2, amount, messageid, proccode, rqtime) values "
                    + "("+String.valueOf(logid)
                    +",'"+serviceId+"',"
                    + "'"+IsoBitValues[11]
                    +"','"+IsoBitValues[35]
                    +"',"+amount
                    +",'"+messageId
                    +"','"+IsoBitValues[3]
                    + "', current_timestamp)";
            clientDB.execSQL(newLog);
        }
        this.serviceid = serviceId;
        return logid;
    }
    
    public void writePostLog(String[] IsoBitValues, int logid) {
        if (!clientDB.isOpen()) {
            try {
                helperDb = new DataBaseHelper(ctx);
                clientDB = helperDb.getActiveDatabase();
            } catch (Exception ex) {
                //
            }
        }
        if (IsoBitValues[63]!=null) {
            Log.d("LGH", IsoBitValues[63]);
        }
        String amount = IsoBitValues[4];
        if (amount==null) {
            amount = "0";
        }
        if (IsoBitValues[3]!=null&&IsoBitValues[63]!=null) {
            if (IsoBitValues[3].equals("111000") && IsoBitValues[63].startsWith("203TELKOMSEL")) {
                //set amount HALO
                amount = IsoBitValues[48].substring(24,36);
                Log.d("AMT", amount);
            }
            if (serviceid.equals("A54212")) {
                //set amount HALO
                amount = IsoBitValues[48].substring(24,36);
                Log.d("AMT", amount);
            }
            if (IsoBitValues[3].equals("111000") && IsoBitValues[63].startsWith("202INDOSAT")) {
                //set amount MATRIX
                amount = IsoBitValues[48].substring(11,23);
                Log.d("AMT", amount);
            }
            if (serviceid.equals("A54222")) {
                //set amount MATRIX
                amount = IsoBitValues[48].substring(11,23);
                Log.d("AMT", amount);
            }
            if (IsoBitValues[3].equals("111000") && IsoBitValues[63].startsWith("204BRICC")) {
                //set amount BRICC
                amount = "x100";
                Log.d("AMT", amount);
            }
            if (serviceid.equals("A54411")) {
                //set amount BRICC
                amount = "x100";
                Log.d("AMT", amount);
            }
        }
        if (amount.equals("x100")) {
            amount = "amount * 100";
        } else if (!amount.replaceAll("[0123456789]","").equals("")) {
            amount = "0";
        }
        String updAmount = "";
        if (!ignoreReplyAmount) {
            updAmount = "amount = " + amount + ", ";
            Log.i("LOG", "Apply reply amount");
        }
        if (serviceid.equals("A54322")||serviceid.equals("A54331")||serviceid.equals("A54312")) {
//            updAmount = "amount = (amount-2500) * 100, ";
            updAmount = " ";
        }
        String updRqTime = "";
        if (IsoBitValues[13]!=null) {
            Date d = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
            String repDate = sdf.format(d) + "-";
            String tmStamp = null;
            repDate = repDate + IsoBitValues[13].substring(0, 2) + "-" +
                    IsoBitValues[13].substring(2, 4);
            if (IsoBitValues[12] != null) {
                tmStamp = StringLib.toSQLiteTimestamp(repDate, IsoBitValues[12]);
            } else {
                SimpleDateFormat stf = new SimpleDateFormat("HHmmss");
                tmStamp = StringLib.toSQLiteTimestamp(repDate, stf.format(d));
            }
            updRqTime = ", rqtime = '" + tmStamp + "' ";
        }
        String updLog = "update edc_log set " + updAmount
                + " rran = '"+ IsoBitValues[37]
                + "', rc = '"+ IsoBitValues[39]
                + "', rptime = current_timestamp "
                + updRqTime
                + " where log_id = "+ logid;
        Log.d("UQL", updLog);
        clientDB.beginTransaction();
        try {
            clientDB.execSQL(updLog);
            clientDB.setTransactionSuccessful();
            clientDB.endTransaction();
            Log.d("UQL", "Update OK");
        } catch (Exception e) {
            clientDB.endTransaction();
            Log.d("UQL", "Update not OK");
        } finally {
            clientDB.close();
        }
    }

    public void writeRevResponse(String rc, String oriMsg, int logid) {
        if (!clientDB.isOpen()) {
            try {
                helperDb = new DataBaseHelper(ctx);
                clientDB = helperDb.getActiveDatabase();
            } catch (Exception ex) {
                //
            }
        }
        String revStatus = rc.equals("00") ? "T" : "P";
        String updLog = "update edc_log set "
                + "reversed = '"+ revStatus
                + "' where log_id = "+ logid;
        clientDB.execSQL(updLog);
        String saveStack = "insert or replace into reversal_stack("
                + "elogid, orimessage, revstatus) values "
                + "(" + String.valueOf(logid)
                + ",'" + oriMsg + "',"
                + "'" + revStatus + "')";
        clientDB.execSQL(saveStack);
        clientDB.close();
    }

    public void closeDB() {
        if (clientDB!=null) {
            clientDB.close();
        }
    }

    public Context getCtx() {
        return ctx;
    }

    public String[] getLastRevStatus() {
        String gq = "select elogid, orimessage from reversal_stack where revstatus='P'";
        Cursor gd = clientDB.rawQuery(gq, null);
        String[] rets = new String[3];
        if (gd.moveToFirst()) {
            if (gd!=null) {
                rets[0] = "1";
                rets[1] = gd.getString(gd.getColumnIndex("elogid"));
                rets[2] = gd.getString(gd.getColumnIndex("orimessage"));
            } else {
                rets[0] = "0";
            }
        } else {
            rets[0] = "0";
        }
        return rets;
    }

    public void setIgnoreReplyAmount(Boolean ignoreReplyAmount) {
        this.ignoreReplyAmount = ignoreReplyAmount;
    }
}
