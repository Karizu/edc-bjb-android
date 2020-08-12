/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.tornado.billiton.common;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.co.tornado.billiton.R;

/**
 * @author indra
 */
public class CommonConfig {

    public static final String KONFIRM_UPDATE_URL = "192.168.43.243:8080";


    public static final String HTTP_SSL_PROTOCOL = "https";
    public static final String HTTP_NON_SSL_PROTOCOL = "http";
    public static final String HTTP_PROTOCOL = HTTP_NON_SSL_PROTOCOL;

    public static final String WS_SSL_PROTOCOL = "wss";
    public static final String WS_NON_SSL_PROTOCOL = "ws";
    public static final String WS_PROTOCOL = WS_NON_SSL_PROTOCOL;

    public static final int WS_SSL_PORT = 443;
    public static final int WS_NORMAL_PORT = 80;
    public static final int WS_DEV_PORT = 8000;
    public static final int WS_NON_SSL_PORT = WS_NORMAL_PORT;
    public static final int WS_PORT = WS_SSL_PORT;

    public static final String DEV_IP_LOKAL = "192.168.43.28:8080";
    public static final String DEV_IP = "192.168.43.28:8000";
    public static final String DEV_IP_SELADA = "36.94.58.181:8080";//192.168.43.28:8080 192.168.0.7:8080
    public static final String PROD_IP_SELADA = "36.94.58.179:8080";
    public static final String PROD_IP = "edc.bankbjb.co.id";

    public static final String IP = DEV_IP_LOKAL;

    public static final String HTTP_REST_URL = IP+"/ARRest";
    public static final String WEBSOCKET_URL = IP+"/tms";
    public static final String HTTP_POST = HTTP_PROTOCOL+ "://"+IP+"/ARRest/api";

    public static final String POST_PATH = "api";

    public static final String INIT_REST_ACT = "S000025";
//    public static final String INIT_REST_ACT = "S000SMT";
    public static final String[] LIST_MENU_KEY = {"type", "title", "id", "ver", "comps"};
    public static final String[] LIST_MENU_COMP_KEY = {"visible", "comp_type", "comp_id", "comp_lbl", "comp_act", "seq"};
    public static final String[] FORM_MENU_KEY = {"type", "title", "id", "print", "print_text", "ver", "comps"};
    public static final String[] FORM_MENU_COMP_KEY = {"visible", "comp_type", "comp_id", "comp_opt", "comp_act", "seq", "comp_values"};
    public static final String[] FORM_MENU_COMP_VALUES_KEY = {"comp_value", "value", "print"};
    public static final String VER_FILE = "menu_ver";
    public static final String SETTINGS_FILE = "settings";
    public static final int TIME_OUT = 60;//SECOND
    public static final String DB_FILE_UPDATE_NAME = "dbupdate.sql";
    public static final String BRANCH = "303514";
    public static final String USERNAME_ADMIN = "a12345";
    public static final String PASS_ADMIN = "99999999";
    public static final String DEV_SOCKET_IP = "172.18.37.14";//"10.35.65.209";
    public static final String DEV_SOCKET_PORT = "5707";//"1402";
    public static final String DEFAULT_DISCOUNT_TYPE = "Rupiah";
    public static final String DEFAULT_DISCOUNT_RATE = "0";
    public static final String EXPD = "2111";
    public static final String DEV_TERMINAL_ID = "13050201"; //13050033
    public static final String DEV_MERCHANT_ID = "000063708571";
    public static final String INIT_MERCHANT_NAME = "AGEN BJB BISA"; //BJB NARIPAN
    public static final String INIT_MERCHANT_ADDRESS1 = "KANTOR PUSAT"; //KANTOR PUSAT
    public static final String INIT_MERCHANT_ADDRESS2 = "BANDUNG";
    public static final String INIT_SIM_NUMBER = "000000";
    public static final String STORE_NAME = "AGEN";

//    public static final String DEV_TERMINAL_ID = "13030001";public static final String DEV_MERCHANT_ID = "00010025000001";public static final String INIT_MERCHANT_NAME = "SAMSAT BANDUNG 3";public static final String INIT_MERCHANT_ADDRESS1 = "SOEKARNO HATTA";public static final String INIT_MERCHANT_ADDRESS2 = "KCU BANDUNG";
//    public static final String DEV_TERMINAL_ID = "13030002";public static final String DEV_MERCHANT_ID = "00010025000001";public static final String INIT_MERCHANT_NAME = "SAMSAT BANDUNG 3";public static final String INIT_MERCHANT_ADDRESS1 = "SOEKARNO HATTA";public static final String INIT_MERCHANT_ADDRESS2 = "KCU BANDUNG";
//    public static final String DEV_TERMINAL_ID = "13030003";public static final String DEV_MERCHANT_ID = "00010025000001";public static final String INIT_MERCHANT_NAME = "SAMSAT BANDUNG 3";public static final String INIT_MERCHANT_ADDRESS1 = "SOEKARNO HATTA";public static final String INIT_MERCHANT_ADDRESS2 = "KCU BANDUNG";
//    public static final String DEV_TERMINAL_ID = "13030004";public static final String DEV_MERCHANT_ID = "00010025000001";public static final String INIT_MERCHANT_NAME = "SAMSAT BANDUNG 2";public static final String INIT_MERCHANT_ADDRESS1 = "KAWALUYAAN";public static final String INIT_MERCHANT_ADDRESS2 = "KCU BANDUNG";
//    public static final String DEV_TERMINAL_ID = "13030005";public static final String DEV_MERCHANT_ID = "00010025000001";public static final String INIT_MERCHANT_NAME = "SAMSAT BANDUNG 2";public static final String INIT_MERCHANT_ADDRESS1 = "KAWALUYAAN";public static final String INIT_MERCHANT_ADDRESS2 = "KCU BANDUNG";
//    public static final String DEV_TERMINAL_ID = "13030006";public static final String DEV_MERCHANT_ID = "00010025000001";public static final String INIT_MERCHANT_NAME = "SAMSAT BANDUNG 2";public static final String INIT_MERCHANT_ADDRESS1 = "KAWALUYAAN";public static final String INIT_MERCHANT_ADDRESS2 = "KCU BANDUNG";
//    public static final String DEV_TERMINAL_ID = "13030007";public static final String DEV_MERCHANT_ID = "00010025000001";public static final String INIT_MERCHANT_NAME = "SAMSAT BANDUNG 2";public static final String INIT_MERCHANT_ADDRESS1 = "KAWALUYAAN";public static final String INIT_MERCHANT_ADDRESS2 = "KCU BANDUNG";
//    public static final String DEV_TERMINAL_ID = "13030008";public static final String DEV_MERCHANT_ID = "00010025000001";public static final String INIT_MERCHANT_NAME = "SAMSAT BANDUNG 1";public static final String INIT_MERCHANT_ADDRESS1 = "PADJAJARAN";public static final String INIT_MERCHANT_ADDRESS2 = "KCU BANDUNG";
//    public static final String DEV_TERMINAL_ID = "13030009";public static final String DEV_MERCHANT_ID = "00010025000001";public static final String INIT_MERCHANT_NAME = "SAMSAT BANDUNG 1";public static final String INIT_MERCHANT_ADDRESS1 = "PADJAJARAN";public static final String INIT_MERCHANT_ADDRESS2 = "KCU BANDUNG";
//    public static final String DEV_TERMINAL_ID = "13030010";public static final String DEV_MERCHANT_ID = "00010025000001";public static final String INIT_MERCHANT_NAME = "SAMSAT BANDUNG 1";public static final String INIT_MERCHANT_ADDRESS1 = "PADJAJARAN";public static final String INIT_MERCHANT_ADDRESS2 = "KCU BANDUNG";
//    public static final String DEV_TERMINAL_ID = "13030011";public static final String DEV_MERCHANT_ID = "00010025000001";public static final String INIT_MERCHANT_NAME = "SAMSAT BANDUNG 1";public static final String INIT_MERCHANT_ADDRESS1 = "PADJAJARAN";public static final String INIT_MERCHANT_ADDRESS2 = "KCU BANDUNG";
//    public static final String DEV_TERMINAL_ID = "13030012";public static final String DEV_MERCHANT_ID = "00230025000001";public static final String INIT_MERCHANT_NAME = "SAMSAT KK";public static final String INIT_MERCHANT_ADDRESS1 = "CIMAHI";public static final String INIT_MERCHANT_ADDRESS2 = "CAB CIMAHI";
//    public static final String DEV_TERMINAL_ID = "13030013";public static final String DEV_MERCHANT_ID = "00230025000001";public static final String INIT_MERCHANT_NAME = "SAMSAT KK";public static final String INIT_MERCHANT_ADDRESS1 = "CIMAHI";public static final String INIT_MERCHANT_ADDRESS2 = "CAB CIMAHI";
//    public static final String DEV_TERMINAL_ID = "13030014";public static final String DEV_MERCHANT_ID = "00230025000001";public static final String INIT_MERCHANT_NAME = "SAMSAT";public static final String INIT_MERCHANT_ADDRESS1 = "CABANG CIMAHI";public static final String INIT_MERCHANT_ADDRESS2 = "CAB CIMAHI";
//    public static final String DEV_TERMINAL_ID = "13030015";public static final String DEV_MERCHANT_ID = "00220025000001";public static final String INIT_MERCHANT_NAME = "SAMSAT";public static final String INIT_MERCHANT_ADDRESS1 = "CABANG SOREANG";public static final String INIT_MERCHANT_ADDRESS2 = "CAB SOREANG";
//    public static final String DEV_TERMINAL_ID = "13030016";public static final String DEV_MERCHANT_ID = "00220025000001";public static final String INIT_MERCHANT_NAME = "SAMSAT";public static final String INIT_MERCHANT_ADDRESS1 = "CABANG SOREANG";public static final String INIT_MERCHANT_ADDRESS2 = "CAB SOREANG";
//    public static final String DEV_TERMINAL_ID = "13030017";public static final String DEV_MERCHANT_ID = "00220025000001";public static final String INIT_MERCHANT_NAME = "SAMSAT";public static final String INIT_MERCHANT_ADDRESS1 = "CABANG SOREANG";public static final String INIT_MERCHANT_ADDRESS2 = "CAB SOREANG";
//    public static final String DEV_TERMINAL_ID = "13030018";public static final String DEV_MERCHANT_ID = "00750025000001";public static final String INIT_MERCHANT_NAME = "SAMSAT";public static final String INIT_MERCHANT_ADDRESS1 = "KAB BANDUNG BARAT";public static final String INIT_MERCHANT_ADDRESS2 = "CAB PADALARANG";
//    public static final String DEV_TERMINAL_ID = "13030019";public static final String DEV_MERCHANT_ID = "00750025000001";public static final String INIT_MERCHANT_NAME = "SAMSAT";public static final String INIT_MERCHANT_ADDRESS1 = "KAB BANDUNG BARAT";public static final String INIT_MERCHANT_ADDRESS2 = "CAB PADALARANG";
//    public static final String DEV_TERMINAL_ID = "13030020";public static final String DEV_MERCHANT_ID = "00750025000001";public static final String INIT_MERCHANT_NAME = "SAMSAT";public static final String INIT_MERCHANT_ADDRESS1 = "KAB BANDUNG BARAT";public static final String INIT_MERCHANT_ADDRESS2 = "CAB PADALARANG";
//    public static final String DEV_TERMINAL_ID = "13030021";public static final String DEV_MERCHANT_ID = "00110025000001";public static final String INIT_MERCHANT_NAME = "SAMSAT";public static final String INIT_MERCHANT_ADDRESS1 = "CABANG SUMEDANG";public static final String INIT_MERCHANT_ADDRESS2 = "CAB SUMEDANG";
//    public static final String DEV_TERMINAL_ID = "13030022";public static final String DEV_MERCHANT_ID = "00110025000001";public static final String INIT_MERCHANT_NAME = "SAMSAT";public static final String INIT_MERCHANT_ADDRESS1 = "CABANG SUMEDANG";public static final String INIT_MERCHANT_ADDRESS2 = "CAB SUMEDANG";
//    public static final String DEV_TERMINAL_ID = "13030023";public static final String DEV_MERCHANT_ID = "00780025000001";public static final String INIT_MERCHANT_NAME = "SAMSAT";public static final String INIT_MERCHANT_ADDRESS1 = "CABANG MAJALAYA";public static final String INIT_MERCHANT_ADDRESS2 = "CAB MAJALAYA";
//    public static final String DEV_TERMINAL_ID = "13030024";public static final String DEV_MERCHANT_ID = "00780025000001";public static final String INIT_MERCHANT_NAME = "SAMSAT";public static final String INIT_MERCHANT_ADDRESS1 = "CABANG MAJALAYA";public static final String INIT_MERCHANT_ADDRESS2 = "CAB MAJALAYA";
    public static final String CVA = "12600000";
    public static final String DEFAULT_SETTLEMENT_PASS = "1234";
    public static final String DEFAULT_MIN_BALANCE_BRIZZI = "2500";
    public static final String DEFAULT_MAX_MONTHLY_DEDUCT = "20000000";
    public static final Boolean DEBUG_MODE = false;
    public static final String SQUEN = "8977";
    public static final int CAPTURE_PINBLOCK = 52;
    public static final int CALLBACK_KEYPRESSED = 53;
    public static final int CALLBACK_RESULT = 54;
    public static final int CAPTURE_CANCEL = 55;
    public static final int FLAG_INUSE = 56;
    public static final int FLAG_READY = 57;
    public static final String VTB = "22300000";
    public static final int UPDATE_FLAG_RECEIVER = 58;
    public static final int MODE_CALCULATE = 59;
    public static final int CALLBACK_CANCEL = 60;
    public static final int CALLBACK_CANCEL_DONE = 61;
    public static final String ONE_BIN = "522184";


    public static Object[] getOpt(String compOpt) {
        Object[] result = new Object[5];
        Boolean bool = compOpt.substring(1, 2).equals("1");
//        Log.d("COMP", bool.toString());
        result[CompOption.MANDATORY] = compOpt.substring(0, 1).equals("1");
        result[CompOption.DISABLED] = compOpt.substring(1, 2).equals("1");
        result[CompOption.TYPE] = Integer.parseInt(compOpt.substring(2, 3));
        result[CompOption.MIN_LENGTH] = Integer.parseInt(compOpt.substring(3, 6));
        result[CompOption.MAX_LENGTH] = Integer.parseInt(compOpt.substring(6, 9));
        return result;
    }

    public static Map<String, Integer> ICONS() {
        Map<String, Integer> icons = new HashMap<>();
        icons.put("Mini ATM", R.drawable.info_atm);
        icons.put("Informasi", R.drawable.cek_saldo);
        icons.put("Transfer", R.drawable.transfer_sesama);
        icons.put("Pemindahbukuan", R.drawable.transfer_sesama);
        icons.put("Transfer bank lain", R.drawable.transfer_antarbank);
        icons.put("Info Kode Bank", R.drawable.info_petunjuk);
        icons.put("Pembayaran", R.drawable.mb_isi_ulang_brizzi);
        icons.put("Pembayaran eSamsat Banten", R.drawable.icon_esamsat);
        icons.put("SAMSAT", R.drawable.mb_info_dplk);
        icons.put("Pembayaran eSamsat via Transfer", R.drawable.mb_info_dplk);
        icons.put("Logon", R.drawable.logon);
        icons.put("Settings", R.drawable.mb_info_mutasi);
        icons.put("Info Saldo", R.drawable.cek_saldo);
        icons.put("View Log",R.drawable.mb_info_poin);
        icons.put("Cetak Ulang Transaksi Terakhir",R.drawable.reprint);
        icons.put("Pembayaran eSamsat Debit", R.drawable.mb_isi_ulang_brizzi);
        icons.put("Reprint", R.drawable.print);
        icons.put("Mutasi Rekening", R.drawable.cek_mutasi);
        icons.put("Mutasi", R.drawable.cek_mutasi);
        icons.put("Tarik tunai", R.drawable.tarik_tunai);
        icons.put("Void", R.drawable.void_tarik);
        icons.put("Sale", R.drawable.sale);
        icons.put("Settlement", R.drawable.settelment);
        return icons;
    }

    public static int getIcon(String name) {
        return ICONS().containsKey(name) ? ICONS().get(name) : -1;
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    /**
     * Get Imei Device
     *
     * @param ctx
     * @return
     */
    public static String getImei(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

    /**
     * Get current location of device
     *
     * @param ctx Android Context
     * @return Array double of long and lat. Index 0 is longtitude and index 1 is latitude
     */
    public static double[] getLocation(Context ctx) {
        LocationManager lm = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double[] d = new double[2];
        d[0] = location.getLongitude();
        d[1] = location.getLatitude();
        return d;
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public static class ComponentType {
        //Component Type
        public static final int ListMenuItem = 0;
        public static final int TextView = 1;
        public static final int EditText = 2;
        public static final int PasswordField = 3;
        public static final int ComboBox = 4;
        public static final int CheckBox = 5;
        public static final int RadioButton = 6;
        public static final int Button = 7;
        public static final int MagneticSwipe = 8;
        public static final int ChipInsert = 9;
        public static final int TapCard = 10;
        public static final int SwipeInsert = 11;
        public static final int SwipeTap = 12;
        public static final int InsertTap = 13;
        public static final int SwipeInsertTap = 14;
    }

    public static class MenuType {
        //Menu Type
        public static final int ListMenu = 0;
        public static final int Form = 1;
        public static final int PopupBerhasil = 2;
        public static final int PopupGagal = 3;
        public static final int PopupLogout = 4;
        public static final int SecuredForm = 5;
    }

    public static class CompOption {
        public static final int MANDATORY = 0;
        public static final int DISABLED = 1;
        public static final int TYPE = 2;
        public static final int MIN_LENGTH = 3;
        public static final int MAX_LENGTH = 4;
    }

    public static class TextType {
        public static final int ALPHA_NUMERIC = 0;
        public static final int ALPHA = 1;
        public static final int NUMERIC = 2;
        public static final int NO_CONSTRAINT = 3;
    }

    public static Map<String, Integer> KNOWN_TAG_MAP() {
        Map<String, Integer> tagmap = new HashMap<>();
        //from FCI
        tagmap.put("6F", 0);
        tagmap.put("84", 1);
        tagmap.put("A5", 0);
        tagmap.put("50", 1);
        tagmap.put("87", 1);
        tagmap.put("9F38", 1);
        tagmap.put("5F2D", 1);
        tagmap.put("9F11", 1);
        tagmap.put("9F12", 1);
        tagmap.put("BF0C", 0);
        tagmap.put("9F4D", 1);
        //from FIS 010C
        tagmap.put("70", 0);
        tagmap.put("57", 1);
        tagmap.put("5F20", 1);
        tagmap.put("9F1F", 1);
        //from FIS 0114
//        tagmap.put("70", 0);
        tagmap.put("8F", 1);
        tagmap.put("92", 1);
        tagmap.put("9F32", 1);
        tagmap.put("9F4A", 1);
        //from FIS 0214
//        tagmap.put("70", 0);
        tagmap.put("90", 1);
        //from FIS 0314
//        tagmap.put("70", 0);
        tagmap.put("9F46", 1);
        tagmap.put("9F47", 1);
        tagmap.put("9F49", 1);
        //from FIS 011C
//        tagmap.put("70", 0);
        tagmap.put("5A", 1);
        tagmap.put("5F24", 1);
        tagmap.put("9F0D", 1);
        tagmap.put("9F0E", 1);
        tagmap.put("9F0F", 1);
        tagmap.put("5F25", 1);
        tagmap.put("5F34", 1);
        tagmap.put("9F07", 1);
        //from FIS 021C
//        tagmap.put("70", 0);
        tagmap.put("93", 1);
        //from FIS 0124
//        tagmap.put("70", 0);
        tagmap.put("9F08", 1);
        tagmap.put("8D", 1);
        tagmap.put("8E", 1);
        tagmap.put("5F28", 1);
        tagmap.put("9F6C", 1);
        //from FIS 0224
//        tagmap.put("70", 0);
        tagmap.put("8C", 1);

        // CDOL1
        tagmap.put("9F02", 1);
        tagmap.put("9F03", 1);
        tagmap.put("9F1A", 1);
        tagmap.put("95", 1);
        tagmap.put("5F2A", 1);
        tagmap.put("9A", 1);
        tagmap.put("9C", 1);
        tagmap.put("9F37", 1);
        tagmap.put("9F4C", 1);
        tagmap.put("9F45", 1);

        // AC Resp
        tagmap.put("77", 0);
        tagmap.put("80", 0);
        tagmap.put("9F27", 1);
        tagmap.put("9F36", 1);
        tagmap.put("9F26", 1);
        tagmap.put("9F10", 1);

        // CDOL2
        tagmap.put("8A", 1);
//        tagmap.put("95", 1);
//        tagmap.put("9F37", 1);

        // Issuer Response
        tagmap.put("91", 1);
        tagmap.put("8A", 1);
        tagmap.put("71", 0);
        tagmap.put("72", 0);
        tagmap.put("9F18", 1);
        tagmap.put("86", 1);
        return tagmap;
    }

    public static String NSICCS_AID = "D3600000010000";
    public static String NSICCS_VER = "030";
    public static String NSICCS_RID = "D360000001";
    public static String NSICCS_CAPK_IDX = "01";
    public static String NSICCS_CAPK_EXP = "03";
    public static String NSICCS_CAPK_CHK = "C7D0386A1ECDAE9FFA0CFD6BC9132F11D577B401";
    public static String NSICCS_CAPK_MOD = "8B1DA85F7D036EC77EF63EF47DB606D81A32FDDC0392FCF126588C9FAAC39597BEAF8A6DA7D2ED97B944A4501EB09DE52A130E17852E61E615BC16A998E44C3930906CE4C90EFD5A7357D9913C25D87A1252B3094C3E8C8E69F0A3F3D1B71AA0E5FF01A54CFAE2B6C163CBABD77D352DD18947EE2C5D35BBC25A5E0218CC7CAF";

    public static final List<String> ICC_LIST_SUPPORTED_APP() {
        List<String> appList = new ArrayList<>();
        appList.add("A0000006021010"); //NSICCS
//                appList.add("A0000000043060"); // Maestro Debit
//                appList.add("A0000000046000");  // Cirrus
//                appList.add("A0000000031010"); // VISA Debit
        return appList;
    }

    public static final int ICC_PROCESS_STAGE_INIT = 0;
    public static final int ICC_PROCESS_STAGE_GEN = 1;
    public static final int ICC_PROCESS_STAGE_TX = 2;
    public static final int ICC_PROCESS_STAGE_FINISHED = 3;
    public static final int ICC_PROCESS_STAGE_CANCELED = 4;

    public static final int ICC_VALIDATION_INVALIDATED = 0;
    public static final int ICC_VALIDATION_OK = 1;
    public static final int ICC_VALIDATION_FAILED = -1;

}

