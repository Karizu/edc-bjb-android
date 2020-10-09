/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.tornado.billiton.common;

import android.annotation.TargetApi;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import id.co.tornado.billiton.R;

/**
 * @author indra
 */
public class CommonConfig {

    public static final String HTTP_SSL_PROTOCOL = "https";
    public static final String HTTP_NON_SSL_PROTOCOL = "http";
    public static final String HTTP_PROTOCOL = HTTP_NON_SSL_PROTOCOL;

    public static final String WS_SSL_PROTOCOL = "wss";
    private static final String WS_NON_SSL_PROTOCOL = "ws";
    public static final String WS_PROTOCOL = WS_NON_SSL_PROTOCOL;

    public static final int WS_SSL_PORT = 443;
    private static final int WS_NORMAL_PORT = 80;
    public static final int WS_NON_SSL_PORT = WS_NORMAL_PORT;

    private static final String intDeSelKey = "H4sIAAAAAAAAADM207M00TO10DO0MAQAWnddcwwAAAA=";
    public static final String intProSelKey = "H4sIAAAAAAAAADM207M00TO10DO0MLKyMLAwAACERKhvEQAAAA==";
    public static final String intProBjKey = "H4sIAAAAAAAAAEtNSdZLSszLTspK0kvO18tMAQBgXEBjEQAAAA==";

    private static final String usKey = "H4sIAAAAAAAAAEs0NDI2MQUAXbC9fwYAAAA=";
    private static final String psKey = "H4sIAAAAAAAAALO0hAAAqpcHuggAAAA=";
    private static final String httpKey = "H4sIAAAAAAAAANN3DApKLS4BAJuCLF4HAAAA";
    private static final String wSocketKey = "H4sIAAAAAAAAANMvyS0GAJSVspMEAAAA";
    private static final String htPostKey = "H4sIAAAAAAAAANN3DApKLS7RTyzIBAB/30O2CwAAAA==";
    private static final String postPathKey = "H4sIAAAAAAAAAEssyAQAD9gFrQMAAAA=";

    public static final String chKey_1 = "H4sIAAAAAAAAADM0MTcytQAAI+Bl6gYAAAA=";
    public static final String chKey_2 = "H4sIAAAAAAAAALOwsLAAAB+/PgEEAAAA";
    public static final String chKey_3 = "H4sIAAAAAAAAADMytbA0MwYAwc5YuQYAAAA=";
    public static final String chKey_4 = "H4sIAAAAAAAAADMzs7QEAPHkY4UEAAAA";

//    private static final String PROSELIP = getVal(intProBjKey);
    private static final String PROSELIP = getVal(intProSelKey);
    public static final String DESELIP = getVal(intDeSelKey);

    public static final String[] FORM_MENU_KEY = {"type", "title", "id", "print", "print_text", "ver", "comps"};
    public static final String[] FORM_MENU_COMP_KEY = {"visible", "comp_type", "comp_id", "comp_opt", "comp_act", "seq", "comp_values"};
    public static final String VER_FILE = "menu_ver";
    public static final String SETTINGS_FILE = "settings";
    public static final String DB_FILE_UPDATE_NAME = "dbupdate.sql";
    public static final String BRANCH = "303514";

    public static final String USERNAME_ADMIN = getVal(usKey);
    public static final String PASS_ADMIN = getVal(psKey);

    public static final String DEV_SOCKET_IP = "-";
    public static final String DEV_SOCKET_PORT = "-";
    public static final String DEFAULT_DISCOUNT_TYPE = "Rupiah";
    public static final String EXPD = "2111";

    public static final String DEV_TERMINAL_ID = "00000000";
    public static final String DEV_MERCHANT_ID = "000000000000";
    public static final String INIT_MERCHANT_NAME = "AGEN BJB BISA";
    public static final String INIT_MERCHANT_ADDRESS1 = "KANTOR PUSAT";
    public static final String INIT_MERCHANT_ADDRESS2 = "BANDUNG";
    public static final String INIT_REST_ACT = "S000025";

//    public static final String DEV_TERMINAL_ID = "13030022";
//    public static final String DEV_MERCHANT_ID = "00110025000001";
//    public static final String INIT_MERCHANT_NAME = "SAMSAT SUMEDANG";
//    public static final String INIT_MERCHANT_ADDRESS1 = "CABANG SUMEDANG";
//    public static final String INIT_MERCHANT_ADDRESS2 = "SUMEDANG";
//    public static final String INIT_REST_ACT = "S000SMT";

    public static final String INIT_SIM_NUMBER = "000000";
    public static final String STORE_NAME = "AGEN";

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
    public static final String IP = PROSELIP;
    public static final String HTTP_REST_URL = IP+getVal(httpKey);
    public static final String WEBSOCKET_URL = IP+getVal(wSocketKey);
    public static final String HTTP_POST = HTTP_PROTOCOL+ "://"+IP+getVal(htPostKey);
    public static final String POST_PATH = getVal(postPathKey);

    public static String getVal(String key){
        try {
            return decompress(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String setVal(String key){
        try {
            Log.d(key, compress(key));
            return compress(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

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

    public static String NSICCS_CAPK_EXP = "03";
    public static String NSICCS_CAPK_MOD = "8B1DA85F7D036EC77EF63EF47DB606D81A32FDDC0392FCF126588C9FAAC39597BEAF8A6DA7D2ED97B944A4501EB09DE52A130E17852E61E615BC16A998E44C3930906CE4C90EFD5A7357D9913C25D87A1252B3094C3E8C8E69F0A3F3D1B71AA0E5FF01A54CFAE2B6C163CBABD77D352DD18947EE2C5D35BBC25A5E0218CC7CAF";


    public static final int ICC_PROCESS_STAGE_INIT = 0;
    public static final int ICC_PROCESS_STAGE_GEN = 1;
    public static final int ICC_PROCESS_STAGE_TX = 2;
    public static final int ICC_PROCESS_STAGE_FINISHED = 3;
    public static final int ICC_PROCESS_STAGE_CANCELED = 4;

    public static final int ICC_VALIDATION_INVALIDATED = 0;
    public static final int ICC_VALIDATION_OK = 1;
    public static final int ICC_VALIDATION_FAILED = -1;

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len/2];

        for(int i = 0; i < len; i+=2){
            data[i/2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }

        return data;
    }

    public static String compress(String string) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream(string.length());
        GZIPOutputStream gos = new GZIPOutputStream(os);
        gos.write(string.getBytes());
        gos.close();
        byte[] compressed = os.toByteArray();
        os.close();
        return Base64.encodeToString(compressed, Base64.NO_WRAP);
    }

    public static String decompress(String compressed) throws IOException {
        final int BUFFER_SIZE = 32;
        ByteArrayInputStream is = new ByteArrayInputStream(Base64.decode(compressed, Base64.NO_WRAP));
        GZIPInputStream gis = new GZIPInputStream(is, BUFFER_SIZE);
        StringBuilder string = new StringBuilder();
        byte[] data = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = gis.read(data)) != -1) {
            string.append(new String(data, 0, bytesRead));
        }
        gis.close();
        is.close();
        return string.toString();
    }

}

