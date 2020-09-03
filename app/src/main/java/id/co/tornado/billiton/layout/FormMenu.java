package id.co.tornado.billiton.layout;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.rey.material.app.ThemeManager;
import com.wizarpos.apidemo.printer.ESCPOSApi;
import com.wizarpos.apidemo.printer.FontSize;
import com.wizarpos.apidemo.printer.PrintSize;
import com.wizarpos.jni.PinPadInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import id.co.tornado.billiton.ActivityList;
import id.co.tornado.billiton.MainActivity;
import id.co.tornado.billiton.R;
import id.co.tornado.billiton.common.CommonConfig;
import id.co.tornado.billiton.common.NsiccsData;
import id.co.tornado.billiton.common.StringLib;
import id.co.tornado.billiton.handler.DataBaseHelper;
import id.co.tornado.billiton.handler.JsonCompHandler;
import id.co.tornado.billiton.handler.Track2BINChecker;
import id.co.tornado.billiton.module.Button;
import id.co.tornado.billiton.module.CheckBox;
import id.co.tornado.billiton.module.ChipInsert;
import id.co.tornado.billiton.module.ComboBox;
import id.co.tornado.billiton.module.EditText;
import id.co.tornado.billiton.module.InsertICC;
import id.co.tornado.billiton.module.MagneticSwipe;
import id.co.tornado.billiton.module.RadioButton;
import id.co.tornado.billiton.module.TextView;
import id.co.tornado.billiton.module.listener.GPSLocation;
import id.co.tornado.billiton.module.listener.InputListener;
import id.co.tornado.billiton.module.listener.SwipeListener;


/**
 * Created by indra on 25/11/15.
 */
public class FormMenu extends ScrollView implements View.OnClickListener, SwipeListener, InputListener, View.OnKeyListener {
    public static final int APPEND_LOG = 0;
    public static final int LOG = 1;
    public static final int SUCCESS_LOG = 2;
    public static final int FAILED_LOG = 3;
    public static final int PIN_KEY_CALLBACK = 4;
    private static final String SAMSAT_PROFILES = "1";
    private static final String MPN_PROFILES = "2";
    private static final String PURCHASE_PROFILES = "3";
    private static final String PBB_PROFILES = "4";
    private static final String MINI_BANKING_PROFILES = "5";
    public static boolean SWIPELESS = false;
    public static boolean SWIPEANY = false;
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    protected Handler mHandler = null;
    private Activity context;
    private boolean hasTapModule = false;
    private boolean hasMagModule = false;
    private int pinModuleCounter = 0;
    private int pinDialogCounter = 0;
    private int pinDialogCloseCounter = 0;
    private boolean pinDialogCanceled = false;
    private String dummyTrack;
    private LinearLayout baseLayout, baseLayoutButton;
    private JSONObject comp;
    private JSONObject result;
    private LayoutInflater li;
    private LinearLayout printBtn;
    private int ret = -1;
    private ProgressDialog dialog;
    private AlertDialog alert;
    private List pinpadTextList = new ArrayList();
    private List pinpadDialogList = new ArrayList();
    private int countPrintButton = 0;
    private MagneticSwipe magneticSwipe;
    private String emode = "021";
    private InsertICC insertICC;
    private boolean iccPreProcessed = false;
    private boolean iccIsTriggerd = false;
    private android.widget.TextView iccUIDisplay;
    private final int STATE_CHANGE = 0;
    private final int MESSAGE_CHANGE = 1;
    private final int ERROR_CHANGE = 2;
    private String screenLoader;
    private String pinblockHolder;
    private String panHolder = "";
    private String tcaid = "";
    private boolean isOpened = false;
    private String TAG = "PINPad";
    //    Map<Integer,View> form = new HashMap<>();
    //    private ProgressDialog dialog;
    private boolean externalCard = false;
    private EditText pinpadText = null;
    private String formId;
    private boolean isReprint = false;
    private String reprintTrace = "";
    private android.widget.TextView confirmationText;
    private String[] printConfirm = {
            "Print Customer Copy ?",
            "Print Bank Copy ?",
            "Print Merchant Copy ?", "",
            "Print Duplicate Copy ?", "", "", ""
    };
    private String[] printConfirmTbank = {
            "Print Agent Copy ?",
            "Print Bank Copy ?",
            "Print Customer Copy ?", "",
            "Print Duplicate Copy ?", "", "", ""
    };
    private String serverRef = null;
    private String serverDate = null;
    private String serverTime = null;
    private String serverAppr = null;
    private boolean printInUse = false;
    private android.widget.TextView pinpadWarningText;
    private boolean focusHasSets = false;
    private String nomorKartu;
    private String cardType;
    private Handler focusHandler = new Handler();
    final private int PINPAD_IDLE = 0;
    final private int PINPAD_WORKING = 1;
    final private int PINPAD_CLOSING = 2;
    private int pinpadState = PINPAD_IDLE;
    private Messenger syncMessenger = null;
    private ActivityList parent;
    private Button formTrigger;
    private DataBaseHelper helperDb;
    private String lastan;
    private boolean fallback = false;
    private long mLastClickTime = SystemClock.elapsedRealtime() - 10000;
    private boolean txVerification = false;
    private String[] miniBanking = {"MA0015", "MA0025", "MA0035", "MA0045", "MA0055", "MA0065", "MA0075", "MA0085", "MA0095",
            "MA0011", "MA0022", "MA0032", "MA0042", "MA0051", "MA0062", "MA0072", "MA0082", "MA0092",
            "MA0012", "MA0023", "MA0033", "MA0043", "MA0052", "MA0063", "MA0073", "MA0083", "MA0093",
            "MAG031", "MAG021", "MAG081", "MAG091", "MAG050"};

    private String cdata, nop = null;

    private static final String PEMKOT_BANDUNG_ID = "0027";
    private static String PEMKOT_BANDUNG_NOP = "3273";
    private static final String PEMKAB_BANDUNG_ID = "0022";
    private static String PEMKAB_BANDUNG_NOP = "3206";
    private static final String PEMKOT_CIREBON_ID = "0002";
    private String sal_amount;
    private String stan;
    private String date = "null";
    private String stanForReversal = "null";
    private String pin, pin_confirm;
    private int counter = 0;
    private String messageId;
    private String TAG_CARD = null;
    private String PURCHASE_BJB = "MB82510";
    private String PURCHASE_SELADA = "MB82560";
    private String SERVICE_PURCHASE_SELADA = "E82560";
    private String MENU_PUCHASE = PURCHASE_SELADA;
    private String MENU_PUCHASE_BJB = PURCHASE_BJB;
    private String serviceId = "";
    private String mid = "";
    private String mobileNumber = "";
    private String nominal = "";
    private String amount = "";
    private String margin = "";
    private String json = "";
    private String SCREEN_PURCHASE_SELADA = "MB82561";
    private String SCREEN_PURCHASE_FALLBACK = "MB82562";
    private String SCREEN_PURCHASE_BJB = "MB82511";
    private String SCREEN_PURCHASE_BJB_FALLBACK = "MB82512";
//    PEMKAB KARAWANG
//    PEMKAB CIAMIS
//    PEMKOT TASIKMALAYA
//    PEMKOT SUKABUMI
//    PEMKOT SERANG
//    PEMKAB SERANG
//    PEMKAB SUBANG
//    PEMKAB INDRAMAYU
//    PEMKOT BEKASI
//    PEMKAB SUMEDANG
//    PEMKOT TANGERANG
//    PEMKOT BOGOR
//    PEMKAB KUNINGAN


    public FormMenu(Activity context, String id, String serviceIds, String mids, String mobileNumbers, String nominals, String amounts, String margins, String jsons) {
        super(context);
        ThemeManager.init(context, 1, 0, null);
        this.context = context;
        parent = (ActivityList) context;
        dummyTrack = composeNumber();
        this.syncMessenger = parent.getSyncMessenger();

        serviceId = serviceIds;
        mid = mids;
        mobileNumber = mobileNumbers;
        nominal = nominals;
        amount = amounts;
        margin = margins;
        json = jsons;

        li = LayoutInflater.from(context);
        ScrollView ll = (ScrollView) li.inflate(R.layout.form_menu, this);
        baseLayout = (LinearLayout) ll.findViewById(R.id.base_layout);
        baseLayoutButton = (LinearLayout) ll.findViewById(R.id.base_layout_button);
        LayoutParams params = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );
        baseLayout.setPadding(8, 8, 8, 8);
        this.dettachPrint();
        dummyTrack += "=" + composeCV();

        if (id.equals("REVERSALEFROMSELADA")) {
            sendReversalAdviceSale(serviceId);
            return;
        }

        try {
            if (id.equals("STL0001")) {
                comp = parent.prepareSettlement(); //parent is null
                comp = parent.prepareSettlement(); //parent is null
                init();
            }
//            else if (id.equals(MENU_PUCHASE)){
//                comp = new JSONObject("{\n" +
//                        "    \"action_url\": \"E82560\",\n" +
//                        "    \"ver\": \"1\",\n" +
//                        "    \"print\": null,\n" +
//                        "    \"comps\": {\n" +
//                        "      \"comp\": [\n" +
//                        "        {\n" +
//                        "          \"visible\": false,\n" +
//                        "          \"comp_lbl\": \"ICC Insert Tx\",\n" +
//                        "          \"comp_type\": \"9\",\n" +
//                        "          \"comp_id\": \"I0209\",\n" +
//                        "          \"seq\": 0\n" +
//                        "        },\n" +
//                        "        {\n" +
//                        "          \"visible\": true,\n" +
//                        "          \"comp_lbl\": \"PIN\",\n" +
//                        "          \"comp_type\": \"3\",\n" +
//                        "          \"comp_id\": \"I0001\",\n" +
//                        "          \"comp_opt\": \"102006006\",\n" +
//                        "          \"seq\": 1\n" +
//                        "        },\n" +
//                        "        {\n" +
//                        "          \"visible\": true,\n" +
//                        "          \"comp_lbl\": \"Proses\",\n" +
//                        "          \"comp_type\": \"7\",\n" +
//                        "          \"comp_id\": \"G0001\",\n" +
//                        "          \"seq\": 2\n" +
//                        "        }\n" +
//                        "      ]\n" +
//                        "    },\n" +
//                        "    \"static_menu\": [\n" +
//                        "      \"Purchase\"\n" +
//                        "    ],\n" +
//                        "    \"print_text\": \"IPOP\",\n" +
//                        "    \"id\": \"MB82560\",\n" +
//                        "    \"type\": \"1\",\n" +
//                        "    \"title\": \"Purchase\"\n" +
//                        "  }");
//                init();
//            }
            else {
//                comp = JsonCompHandler.readJson(context, id);
                comp = JsonCompHandler
                        .readJsonFromCacheIfAvailable(context, id)
//                        .readJsonFromUrl(id, context)
                ;
                init();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        focusHandler.postDelayed(delayFocus, 400);
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);

    }

    public String composeNumber() {
        String t = CommonConfig.ONE_BIN;
        t += CommonConfig.BRANCH;
        t += CommonConfig.SQUEN;
        return t;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)

                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public String composeCV() {
        String vt = CommonConfig.EXPD;
        vt += CommonConfig.CVA;
        vt += CommonConfig.VTB;
        return vt;
    }

    public LinearLayout getBaseLayout() {
        return baseLayout;
    }

    @Override
    public void onSwipeComplete(View v, String string) {
//        Log.d("INFO_SWIPE", string);
        if (SWIPEANY) {
            string = "5221842001365318=18111260000058300000";
            magneticSwipe.setText("5221842001365318=18111260000058300000");
        }
        Track2BINChecker tbc = new Track2BINChecker(this.context, string);
        this.externalCard = tbc.isExternalCard();
        if (string == null || string.equals("")) {
//          Log.d("SWIPE", "BACK PRESSED");
        }
        try {
            magneticSwipe.openDriver();
            magneticSwipe.closeDriver();
            magneticSwipe.setIsQuit(true);
        } catch (Exception e) {
            //failed to close, maybe already closed or not open yet
        }
        alert.dismiss();
        focusHandler.postDelayed(delayFocus, 400);
        panHolder = panFromTrack2(string);
        if (fallback) {
            String track2AndSc = serviceCodeFromBin(string);
            SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
            String invoRef = sdf.format(new Date()) + String.format("%04d", getSeqNum());
            magneticSwipe.setText(track2AndSc + "|" + invoRef);
        } else {
            magneticSwipe.setText(string);
        }
        // showPinDialog();
        try {
            JSONArray array = comp.getJSONObject("comps").getJSONArray("comp");
            if (SWIPELESS) {
                if (formId.startsWith("52")) {
                    for (int ch = 0; ch < baseLayout.getChildCount(); ch++) {
                        if (array.getJSONObject(ch)
                                .getInt("comp_type") == CommonConfig.ComponentType.Button) {
                            Button proses = (Button) baseLayout.getChildAt(ch);
                            proses.setVisibility(VISIBLE);
                            Log.i("SWL", "SWIPELESS Button visible");
                        }
                    }
                }
            } else {
                if (formId.startsWith("52")) {
                    for (int ch = 0; ch < baseLayout.getChildCount(); ch++) {
                        if (array.getJSONObject(ch)
                                .getInt("comp_type") == CommonConfig.ComponentType.Button) {
                            Button proses = (Button) baseLayout.getChildAt(ch);
                            proses.setVisibility(GONE);
                            if (proses.performClick()) {
                                //ok
                            } else {
                                //at least we tried
                            }
                            break;
                        }
                    }
                } else {
                    int viscount = 0;
                    int visindex = 0;
                    for (int ch = 0; ch < baseLayout.getChildCount(); ch++) {
                        if (baseLayout.getChildAt(ch).getVisibility() == VISIBLE) {
                            viscount++;
                            visindex = ch;
                        }
                    }
                    if (viscount == 1 && array.getJSONObject(visindex).getInt("comp_type") == CommonConfig.ComponentType.Button) {
                        Button proses = (Button) baseLayout.getChildAt(visindex);
                        proses.setVisibility(GONE);
                        if (proses.performClick()) {
                            //ok
                        } else {
                            //at least we tried
                        }
                    }
//                Log.d(TAG, "P-Check : " + String.valueOf(viscount) + String.valueOf(visindex));
                }
            }
            if (fallback) {
                insideClick(formTrigger);
            }
        } catch (Exception e) {
            //pass
        }
    }

    private List<String> getValueFromScreen() {
        List<String> values = new ArrayList<>();
        try {
            for (int i = 0; i < baseLayout.getChildCount(); i++) {
                View v = baseLayout.getChildAt(i);
                if (v instanceof EditText) {
                    values.add(String.valueOf(((EditText) v).getText()));
                } else if (v instanceof ComboBox) {
                    ComboBox comboBox = (ComboBox) v;
                    if (comboBox.compValues != null && comboBox.compValues.length() > 0) {
                        String cdata = comboBox.compValuesHashMap.get(comboBox.getSelectedItemPosition());
                        values.add(cdata);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return values;
    }

    private String[] getAmountFromScreen() {
        String[] amts = {"000000000000", "000000000000", ""}; // amount, addamount, de55
        String amt = null;
        String aamt = null;
        String iccDe = null;
        try {
            for (int i = 0; i < baseLayout.getChildCount(); i++) {
                View v = baseLayout.getChildAt(i);
                if (v instanceof EditText) {
                    String hint = (String) ((EditText) v).getHint();
                    if (hint.contains("Nominal")) {
                        amt = String.valueOf(((EditText) v).getText());
                        amt = "000000000000" + amt + "00";
                        amt = amt.substring(amt.length() - 12);
                        amts[0] = amt;
                    }
                }
            }

            if (comp.has("comps")) {
                JSONObject comps = comp.getJSONObject("comps");
                if (comps.has("comp")) {
                    JSONArray comp_array = comps.getJSONArray("comp");
                    for (int i = 0; i < comp_array.length(); i++) {
                        JSONObject comp_obj = comp_array.getJSONObject(i);

                        if (formId.equals("POC0031")) {
                            if (comp_obj != null && comp_obj.getString("comp_type").equals("1") && comp_obj.has("comp_act") && comp_obj.getString("comp_act").equals("sal_total")) {
                                if (comp_obj.has("comp_values")) {
                                    JSONObject comp_values = comp_obj.getJSONObject("comp_values");
                                    if (comp_values.has("comp_value")) {
                                        JSONArray comp_value_array = comp_values.getJSONArray("comp_value");
                                        if (comp_value_array.length() == 1) {
                                            JSONObject comp_value_obj = comp_value_array.getJSONObject(0);
                                            String value = comp_value_obj.getString("value");
                                            value = value.replace("Rp", "");
                                            value = value.replace(".", "");
                                            if (value.contains(",00")) {
                                                value = value.replace(",00", "");
                                                sal_amount = value;
                                            }
                                            value = value.trim();

                                            amt = value;//comp_obj.
                                            amt = "000000000000" + amt + "00";

                                            amt = amt.substring(amt.length() - 12);
                                            amts[0] = amt;
                                        }
                                    }
                                }

                            }
                        } else {
                            if (comp_obj != null && comp_obj.getString("comp_type").equals("1") && comp_obj.has("comp_act") && comp_obj.getString("comp_act").equals("sal_amount")) {
                                if (comp_obj.has("comp_values")) {
                                    JSONObject comp_values = comp_obj.getJSONObject("comp_values");
                                    if (comp_values.has("comp_value")) {
                                        JSONArray comp_value_array = comp_values.getJSONArray("comp_value");
                                        if (comp_value_array.length() == 1) {
                                            JSONObject comp_value_obj = comp_value_array.getJSONObject(0);
                                            String value = comp_value_obj.getString("value");
                                            value = value.replace("Rp", "");
                                            value = value.replace(".", "");
                                            if (value.contains(",00")) {
                                                value = value.replace(",00", "");
                                                sal_amount = value;
                                            }
                                            value = value.trim();

                                            amt = value;//comp_obj.
                                            //add 00 on tag 9F02, all screen MPN, pbb
                                            if (formId.equals("SR30017") || formId.equals("SR50017")
                                                    || formId.equals("SR10017") || formId.equals("POC0032")
                                                    || formId.equals("POC0031") || formId.equals("POC0034")
                                                    || formId.equals("POC0035") || formId.equals("MA00040")
                                                    || formId.equals("MA00041") || formId.equals("MA00030")
                                                    || formId.equals("MA00031") || formId.equals("MA00020")
                                                    || formId.equals("MA00021") || formId.equals("MA00091")
                                                    || formId.equals("MA00010") || formId.equals("MA00016")
                                                    || formId.equals("MA0080F") || formId.equals("EP00301")
                                                    || formId.equals("EP00311") || formId.equals("EP00321")
                                                    || formId.equals("EP00411") || formId.equals("EP00511")
                                                    || formId.equals("EP00611") || formId.equals("EP00711")
                                                    || formId.equals("EP00811") || formId.equals("EP00821")
                                                    || formId.equals("EP00911")
                                            ) {
                                                amt = "000000000000" + amt + "00";
                                            } else {
                                                amt = "00000000000000" + amt;
                                            }
                                            amt = amt.substring(amt.length() - 12);
                                            amts[0] = amt;
                                        }
                                    }
                                }

                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return amts;
    }

    private void howToSendToInsertICCEditor() {
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("p1", "your text here");
        bundle.putInt("p2", 79);
        message.setData(bundle);
        stateChangeHandler.sendMessage(message);
    }

    private void showIccDialog(final View v) {
        try {
            WindowManager.LayoutParams icclp = refreshICCProcessDialog("Silahkan masukkan kartu anda", false);
            alert.show();
            alert.getWindow().setAttributes(icclp);
            if (parent.modulStage == CommonConfig.ICC_PROCESS_STAGE_INIT) {
                parent.cardData = new NsiccsData();
                insertICC.init(parent.modulStage, parent.cardData);
            }
            if (parent.modulStage == CommonConfig.ICC_PROCESS_STAGE_GEN) {
                // get amount here
                String[] amts = getIccDataFromComp(comp);
                SimpleDateFormat simpledf = new SimpleDateFormat("yyMMdd");

                parent.cardData.setTxdate(simpledf.format(new Date()));
                parent.cardData.setAmount(amts[0]);
                parent.cardData.setAddamount(amts[1]);
                insertICC.init(parent.modulStage, parent.cardData);
            }
            if (parent.modulStage == CommonConfig.ICC_PROCESS_STAGE_TX) {
                parent.cardData = new NsiccsData();
                // get amount here
                String[] amts = getAmountFromScreen();
                SimpleDateFormat simpledf = new SimpleDateFormat("yyMMdd");

                parent.cardData.setTxdate(simpledf.format(new Date()));
                parent.cardData.setAmount(amts[0]);
                parent.cardData.setAddamount(amts[1]);
                insertICC.init(parent.modulStage, parent.cardData);
            }
            if (parent.modulStage == CommonConfig.ICC_PROCESS_STAGE_FINISHED) {
                // get arpc here
                String[] amts = getIccDataFromComp(comp);
                Log.i("AMT1", amts[1]);
                Log.i("AMT2", amts[2]);
                parent.cardData.setArpc(amts[2]);
                parent.cardData.setArc(amts[2]);
                insertICC.init(parent.modulStage, parent.cardData);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error create icc dialog: " + e.getMessage());
            context.onBackPressed();
        }
    }

    public void closeAllDrivers(){
        try {
            insertICC.closeDriver();
            insertICC = null;
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    private void showChangePinDialog(final View v) {
        try {
            final boolean isChangePIN = formId.equals("5900000") || formId.equals("MA00072") || formId.equals("MA00070");
            if (syncMessenger == null) {
                Log.i("PINPAD", "Refresh sync messenger");
                syncMessenger = parent.getSyncMessenger();
            }
            pinDialogCanceled = false;
            LayoutInflater li = LayoutInflater.from(context);
            View promptsView = li.inflate(R.layout.pinpad_dialog, null);
            final android.widget.TextView alertText = (android.widget.TextView) promptsView.findViewById(R.id.pinAlert);
            alertText.setVisibility(View.GONE);
            pinpadWarningText = alertText;
            if (isChangePIN) {
                pinDialogCounter = pinModuleCounter - pinDialogCloseCounter;
                pinpadText = (EditText) pinpadTextList.get(pinDialogCloseCounter);
            } else {
                pinDialogCounter++;
                if (pinModuleCounter > 1) {
                    pinpadText = (EditText) pinpadTextList.get(pinModuleCounter - pinDialogCounter);
                } else {
                    pinpadText = (EditText) pinpadTextList.get(pinDialogCloseCounter);
                }
            }
            android.widget.TextView dialogLabel = (android.widget.TextView) promptsView.findViewById(R.id.pinPass);
            if (pinpadText.getHint() != null) {
                if (!pinpadText.getHint().equals("PIN")) {
                    dialogLabel.setText(pinpadText.getHint());
                }
            }
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            // set prompts.xml to alertdialog builder
            alertDialogBuilder.setView(promptsView);

            final android.widget.EditText userInput = (android.widget.EditText) promptsView
                    .findViewById(R.id.editTextDialogUserInput);

            final android.widget.Button btnOk = (android.widget.Button) promptsView
                    .findViewById(R.id.btnOk);
            Boolean doIneedHSM = pinModuleCounter == pinDialogCounter;
//            Log.d("PINPAD", String.valueOf(pinModuleCounter) + String.valueOf(pinDialogCounter));
            if (isChangePIN) {
                doIneedHSM = true;
            }
            if (!CommonConfig.getDeviceName().startsWith("WizarPOS")) {
                doIneedHSM = false;
            }
            final Boolean needHsm = doIneedHSM;
            if (needHsm) {
                if (formId.equals("7100000") || formId.equals("7300000")) {
                    btnOk.setVisibility(GONE);
                    android.widget.TextView dialogFoot = (android.widget.TextView) promptsView
                            .findViewById(R.id.dialogFoot);
                    dialogFoot.setVisibility(VISIBLE);
                } else {
                    android.widget.TextView cardLabel = (android.widget.TextView) promptsView
                            .findViewById(R.id.cardLabel);
                    android.widget.TextView cardNumber = (android.widget.TextView) promptsView
                            .findViewById(R.id.cardNumber);
                    cardLabel.setVisibility(VISIBLE);
                    cardNumber.setText(panHolder);
                    cardNumber.setVisibility(VISIBLE);
                    btnOk.setVisibility(GONE);
                    android.widget.TextView dialogFoot = (android.widget.TextView) promptsView
                            .findViewById(R.id.dialogFoot);
                    dialogFoot.setVisibility(VISIBLE);
                }
            }

//        userInput.setKeyListener(null);
            // set dialog message
            if (CommonConfig.getDeviceName().startsWith("WizarPOS")) {
                userInput.setInputType(InputType.TYPE_NULL);
            } else {
                userInput.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
            userInput.setTransformationMethod(new EditText.MyPasswordTransformationMethod());
            alertDialogBuilder
                    .setCancelable(false);
            // create alert dialog
            final AlertDialog alertDialog = alertDialogBuilder.create();
            final WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(alertDialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            final Button realProcess = (Button) v;
            final Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case CommonConfig.CALLBACK_KEYPRESSED:
                            Log.i(TAG, "receive callback");
                            byte[] data = msg.getData().getByteArray("data");
                            int datanol = 0;
                            if (data != null) {
                                datanol = Integer.valueOf(String.valueOf(data[0]));
                            } else {
                                if (btnOk.getVisibility() == View.GONE) {
                                    btnOk.callOnClick();
                                }
                            }
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < datanol; i++) {
                                sb.append("*");
                            }
                            userInput.setText(sb.toString());
                            break;
                        case CommonConfig.CALLBACK_RESULT:
                            Log.i(TAG, "receive result");
//                            Toast.makeText(context, "receive result " + pinModuleCounter, Toast.LENGTH_SHORT).show();
                            if (pinpadTextList.size() >= 0) {
                                try {
                                    String encPin = msg.getData().getString("data");
                                    pinblockHolder = encPin;
                                    pinpadText = (EditText) pinpadTextList.get(pinDialogCloseCounter);
                                    if (!needHsm) {
                                        encPin = userInput.getText().toString();
                                    } else {
                                        pinpadText.setMaxLength(16);
                                    }
                                    pinpadText.setText(encPin.replaceAll(" ", ""));
                                    pinDialogCloseCounter++;
                                } catch (IndexOutOfBoundsException e) {
                                    Log.e("PINPAD", "Dialog closed already");
                                }
                                alertDialog.dismiss();
                                if (pinModuleCounter == 1) {
                                    try {
                                        if (!pinDialogCanceled) {
                                            actionUrl(realProcess, realProcess.getTag().toString());
                                        } else {
                                            if (insertICC != null) {
                                                insertICC.closeDriver();
                                            }
                                        }
                                    } catch (JSONException e) {
                                        Log.e("PINPAD", "Post act failed");
                                    }
                                } else {
                                    pinModuleCounter--;
                                    if (isChangePIN) {
                                        showChangePinDialog(v);
                                    }
                                }
                            }
                            break;
                        case CommonConfig.CALLBACK_CANCEL:
//                            Toast.makeText(context, "receive cancel", Toast.LENGTH_SHORT).show();
//                            dialog = ProgressDialog.show(context, "Clean Up", "Clearing Input Cache", true);
                            context.onBackPressed();
                            break;
                        case CommonConfig.CALLBACK_CANCEL_DONE:
//                            Toast.makeText(context, "receive cancel done", Toast.LENGTH_SHORT).show();
                            try {
                                if (insertICC != null) {
                                    insertICC.closeDriver();
                                }
                                dialog.dismiss();
                            } catch (Exception e) {
                                //no dialog
                            }
                            if (!formId.equals("MA00072")) {
                                alertDialog.dismiss();
                                JSONObject rps = new JSONObject();

                                try {
                                    rps = new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":" +
                                            "[{\"visible\":true,\"comp_values\":{\"comp_value\":[" +
                                            "{\"print\":\"PIN minimal 6 digit\",\n" +
                                            "\"value\":\"PIN minimal 6 digit\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                                            "\"type\":\"3\",\"title\":\"Gagal\"}}");
                                    processResponse(rps, "PINPAD");
                                } catch (Exception e) {

                                }
                            }

//                            context.onBackPressed();
                            break;
                        default:
                            super.handleMessage(msg);
                    }
                }
            };
            final Messenger pinblockReceiver = new Messenger(handler);
            btnOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("PINPAD", "Dialog # " + pinModuleCounter + " ok click");
//                    Toast.makeText(context, "Dialog # " + pinModuleCounter + " ok click", Toast.LENGTH_SHORT).show();
                    if (pinpadTextList.size() >= 0) {
                        try {
                            String encPin = pinblockHolder;
                            pinpadText = (EditText) pinpadTextList.get(pinDialogCloseCounter);
                            if (!needHsm) {
                                encPin = userInput.getText().toString();
                            } else {
                                pinpadText.setMaxLength(16);
                            }
                            pinpadText.setText(encPin.replaceAll(" ", ""));
                            pinDialogCloseCounter++;
                        } catch (IndexOutOfBoundsException e) {
                            Log.e("PINPAD", "Dialog closed already");
                        }
                        alertDialog.dismiss();
                        if (pinModuleCounter == 1) {
                            try {
                                if (!pinDialogCanceled) {
                                    actionUrl(realProcess, realProcess.getTag().toString());
                                } else {
                                    if (insertICC != null) {
                                        insertICC.closeDriver();
                                    }
                                }
                            } catch (JSONException e) {
                                Log.e("PINPAD", "Post act failed");
                            }
                        } else {
                            pinModuleCounter--;
                            if (isChangePIN) {
                                showChangePinDialog(v);
                            }
                        }
                    } else {
                        return;
                    }

                }
            });
            // show it
            if (needHsm) {
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        try {
                            Log.i("PINPAD", "send message");
                            Message message = Message.obtain(null, CommonConfig.CAPTURE_PINBLOCK);
                            Bundle bundle = new Bundle();
                            bundle.putString("pan", panHolder);
                            bundle.putString("formid", formId);
                            message.setData(bundle);
                            message.replyTo = pinblockReceiver;
                            syncMessenger.send(message);
                        } catch (Exception e) {
                            //cannot start pinpad
                            if (insertICC != null) {
                                insertICC.closeDriver();
                            }
                        }
                    }
                });

                alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                            Log.i("PINPAD", "Back pressed");
                            try {
                                pinDialogCanceled = true;
//                                t1.interrupt();
                                Message message = Message.obtain(null, CommonConfig.CAPTURE_CANCEL);
                                message.replyTo = pinblockReceiver;
                                syncMessenger.send(message);
                                btnOk.setVisibility(GONE);
                                if (insertICC != null) {
                                    insertICC.closeDriver();
                                }
                            } catch (Exception e) {
                                //failed to close, maybe already closed or not open yet
                            }
                        } else {

                        }
                        return true;
                    }
                });
            }
            if (formId.equals("SR10004")) {
                counter += 1;
                if (counter == 1) {
                    alertDialog.show();
                    alertDialog.getWindow().setAttributes(lp);
                }
            } else if (formId.equals("MA00072")) {
                counter += 1;
                if (counter == 2) {
                    alertDialog.hide();
                } else {
                    alertDialog.show();
                    alertDialog.getWindow().setAttributes(lp);
                }
            } else {
                alertDialog.show();
                alertDialog.getWindow().setAttributes(lp);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error create pinpad dialog: " + e.getMessage());
            if (insertICC != null) {
                insertICC.closeDriver();
            }
            context.onBackPressed();
        }
    }


    public void actionUrl(Button button, final String actionUrl) throws JSONException {
        Log.d("DO_ACTION", actionUrl);
//        if (insertICC != null && insertICC.needToCloseICC){
//            insertICC.closeDriver();
//        }
        String newActionUrl = "";
//        Toast.makeText(context, "pinblock " + pinblockHolder, Toast.LENGTH_SHORT).show();
        final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        SharedPreferences preferences = context.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
        boolean isLogin = preferences.getBoolean("login_state", false);
//        if (!isLogin) {
//            AlertDialog alertDialog = new AlertDialog.Builder(context).create();
//            alertDialog.setTitle("Koneksi Gagal");
//            alertDialog.setMessage("EDC tidak terkoneksi ke server atau EDC tidak terdaftar, silahkan periksa notifikasi terminal");
//            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
//                    new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss();
//                            context.onBackPressed();
//                            context.finish();
//                        }
//                    });
//            alertDialog.show();
//            return;
//        }

        final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        final JSONObject msg = new JSONObject();
        final List<String> data = new ArrayList<>();
//        StringBuilder data = new StringBuilder();
        int size = baseLayout.getChildCount();
        boolean isError = true;
        String message = "";
        screenLoader = comp.getString("id");


        String newAct = null;
        for (int i = 0; i < baseLayout.getChildCount(); i++) {
            for (int j = 0; j < baseLayout.getChildCount(); j++) {
                View v = baseLayout.getChildAt(i);
                try {
                    int childIndex = Integer.parseInt(v.getTag(R.string.seq_holder).toString());
                    if (childIndex == j) {

                        if (v instanceof EditText) {
                            EditText editText = (EditText) v;

                            //HANDEL TRANSFER IF REFERENSI IS EMPTY
                            if (actionUrl.equals("MA0020") || actionUrl.equals("MA0030") || actionUrl.equals("MA0022") || actionUrl.equals("MA0032")) {
                                if (editText.comp.getString("comp_id").equals("MA033") || editText.comp.getString("comp_id").equals("MA024")) {
                                    Log.d("REFERENSI BEFORE", "MASUK");
                                    String value = editText.getText().toString();
                                    editText.setText(value + " ");
                                    Log.d("REFERENSI BEFORE", "MASUK " + editText.getText().toString());
                                }
                            }

                            if (editText.getText().toString().equals("") && editText.isMandatory()) {
                                continue;
                            }
                            if (actionUrl.equals("A57000")) {
                                if (((int) editText.getTag()) == 2) {
                                    String idwp = editText.getText().toString();
                                    if (idwp.startsWith("4") || idwp.startsWith("5") || idwp.startsWith("6")) {
                                        newAct = "A57200";
                                    }
                                    if (idwp.startsWith("7") || idwp.startsWith("8") || idwp.startsWith("9")) {
                                        newAct = "A57400";
                                    }

                                }
                            }
                            // HANDLING MAPPING MPN G2; S000C15=CASH;S000017=DEBIT
                            if (formId.equals("S000015") && editText.comp.getString("comp_id").equals("M1001")) {
                                // THERE'S ONLY ONE EDIT TEXT IN FORM, IF THERE'S ANOTHER NEW, PLEASE LOOKUP FOR TAG, FIND MASUKKAN KODE BILLING AND USE IT's SEQ AS A TAG
                                String kodeBilling = editText.getText().toString();
                                if (kodeBilling.startsWith("0") || kodeBilling.startsWith("1") || kodeBilling.startsWith("2") || kodeBilling.startsWith("3")) {
                                    // DJP
                                    newActionUrl = "M0006A";
                                } else if (kodeBilling.startsWith("4") || kodeBilling.startsWith("5") || kodeBilling.startsWith("6")) {
                                    // DJBC
                                    newActionUrl = "M0006C";
                                } else if (kodeBilling.startsWith("7") || kodeBilling.startsWith("8") || kodeBilling.startsWith("9")) {
                                    // DJA
                                    newActionUrl = "M0006E";
                                }
                            } else if (formId.equals("S000017") && editText.comp.getString("comp_id").equals("M1001")) {
                                // THERE'S ONLY ONE EDIT TEXT IN FORM, IF THERE'S ANOTHER NEW, PLEASE LOOKUP FOR TAG, FIND MASUKKAN KODE BILLING AND USE IT's SEQ AS A TAG
                                String kodeBilling = editText.getText().toString();
                                if (kodeBilling.startsWith("0") || kodeBilling.startsWith("1") || kodeBilling.startsWith("2") || kodeBilling.startsWith("3")) {
                                    // DJP
                                    newActionUrl = "M0007A";
                                } else if (kodeBilling.startsWith("4") || kodeBilling.startsWith("5") || kodeBilling.startsWith("6")) {
                                    // DJBC
                                    newActionUrl = "M0007C";
                                } else if (kodeBilling.startsWith("7") || kodeBilling.startsWith("8") || kodeBilling.startsWith("9")) {
                                    // DJA
                                    newActionUrl = "M0007E";
                                }
                            }
                            // MAPPING SERVICE FOR CETAK ULANG MPN
                            else if (formId.equals("S000019") && editText.comp.getString("comp_id").equals("M1001")) {
                                // THERE'S ONLY ONE EDIT TEXT IN FORM, IF THERE'S ANOTHER NEW, PLEASE LOOKUP FOR TAG, FIND MASUKKAN KODE BILLING AND USE IT's SEQ AS A TAG
                                String kodeBilling = editText.getText().toString();
                                if (kodeBilling.startsWith("0") || kodeBilling.startsWith("1") || kodeBilling.startsWith("2") || kodeBilling.startsWith("3")) {
                                    // Cetak Ulang DJP
                                    newActionUrl = "M0007I";
                                } else if (kodeBilling.startsWith("4") || kodeBilling.startsWith("5") || kodeBilling.startsWith("6")) {
                                    // Cetak Ulang DJBC
                                    newActionUrl = "M0007G";
                                } else if (kodeBilling.startsWith("7") || kodeBilling.startsWith("8") || kodeBilling.startsWith("9")) {
                                    // Cetak Ulang DJA
                                    newActionUrl = "M0007H";
                                }
                            }
                            //Add Filter No Pajak & PEMDA ID PBB
                            if ((formId.equals("POC0030") || formId.equals("POC0033")) && editText.comp.getString("comp_id").equals("P0031")) {
                                nop = editText.getText().toString();
                            }

                            if (actionUrl.equals("R0000A") || actionUrl.equals("RMA020")) {
                                stan = editText.getText().toString();
                            }


                            //INPUT CONFIG FITUR REPORT
                            if (actionUrl.equals("R001A1") || actionUrl.equals("RMA011")
                                    || actionUrl.equals("RMA014") || actionUrl.equals("R002A1")) {
                                date = editText.getText().toString();
                                SimpleDateFormat fromUser = new SimpleDateFormat("ddMMyyyy");
                                SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");

                                try {
                                    date = myFormat.format(fromUser.parse(date));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }

                            //INPUT CONFIG FITUR SALE REVERSAL
                            if (actionUrl.equals("REV561")) {
                                stanForReversal = editText.getText().toString();
                            }

                            //HANDLE FITUR GANTI PIN
                            try {
                                if (actionUrl.equals("MA0070")) {
                                    if (editText.comp.getString("comp_id").equals("MA071")) {
                                        pin = editText.getText().toString();
                                        Log.d("PIN BARU", pin);
                                    }
                                    if (editText.comp.getString("comp_id").equals("MA072")) {
                                        pin_confirm = editText.getText().toString();
                                        Log.d("CONFIRM PIN BARU", pin_confirm);
                                        if (!pin_confirm.equals(pin)) {
                                            JSONObject rps = new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"PIN Baru Tidak Sesuai\",\n" +
                                                    "\"value\":\"PIN Baru Tidak Sesuai\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                                                    "\"type\":\"3\",\"title\":\"Gagal\"}}");

                                            processResponse(rps, "000000");
                                            return;
                                        }
                                    }
                                }

                                if (actionUrl.equals("MA0072")) {
                                    if (editText.comp.getString("comp_id").equals("MA071")) {
                                        pin = editText.getText().toString();
                                        Log.d("PIN BARU", pin);
                                    }
                                    if (editText.comp.getString("comp_id").equals("MA072")) {
                                        pin_confirm = editText.getText().toString();
                                        Log.d("CONFIRM PIN BARU", pin_confirm);
                                        if (!pin_confirm.equals(pin)) {
                                            JSONObject rps = new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"PIN Baru Tidak Sesuai\",\n" +
                                                    "\"value\":\"PIN Baru Tidak Sesuai\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                                                    "\"type\":\"3\",\"title\":\"Gagal\"}}");

                                            processResponse(rps, "000000");
                                            return;
                                        }
                                    }
                                }
                            } catch (Exception es) {
                            }

                            //INPUT CONFIG FITUR BUKA REKENING
                            if (actionUrl.equals("MA0080") || actionUrl.equals("MA0082")) {
                                String addValue = editText.getText().toString();
                                if (editText.comp.getString("comp_id").equals("MA097")) {
                                    SimpleDateFormat fromUser = new SimpleDateFormat("ddMMyyyy");
                                    SimpleDateFormat myFormat = new SimpleDateFormat("yyyyMMdd");

                                    try {
                                        addValue = myFormat.format(fromUser.parse(addValue));
                                        editText.setText(addValue);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }

                                if (editText.comp.getString("comp_id").equals("MA094") || editText.comp.getString("comp_id").equals("MA096")) {
                                    String sCap = editText.getText().toString().toUpperCase();
                                    editText.setText(sCap);
                                }
                            }

                            //INPUT CONFIG FITUR BATAL REKENING
                            if (actionUrl.equals("MA0090") || actionUrl.equals("MA0092")) {
                                String addValue = editText.getText().toString();
                                if (editText.comp.getString("comp_id").equals("MA107")) {
                                    if (addValue.length() < 12) {
                                        addValue = String.format("%-" + 12 + "s", addValue);
                                        editText.setText(addValue);
                                    }
                                }
                            }

                            Log.d("EDIT READ", editText.getText().toString());
                            data.add(editText.getText().toString());
                        }
                        if (v instanceof ComboBox) {
                            ComboBox comboBox = (ComboBox) v;
                            if (comboBox.compValues != null && comboBox.compValues.length() > 0) {
                                if (comboBox.compValuesHashMap != null && comboBox.compValuesHashMap.size() > 0) {
                                    cdata = comboBox.compValuesHashMap.get(comboBox.getSelectedItemPosition());
                                } else {

                                    //CONFIG COMBOBOX FOR GET ACCOUNT LIST
                                    cdata = comboBox.list.get(comboBox.getSelectedItemPosition());

                                    if (comboBox.compId.equals("MA015")) {
                                        if (!formId.equals("MA00050") && !formId.equals("MA00051") && !formId.equals("MA00080") && !formId.equals("MA00082") && !formId.equals("MA00090") && !formId.equals("MA00092")
                                                && !formId.equals("EP00300") && !formId.equals("EP00310") && !formId.equals("EP00410") && !formId.equals("EP00610") && !formId.equals("EP00612")
                                                && !formId.equals("EP00710") && !formId.equals("EP00712") && !formId.equals("EP00810") && !formId.equals("EP00812")
                                                && !formId.equals("EP00820") && !formId.equals("EP00822") && !formId.equals("EP00910") && !formId.equals("EP00912")) {
                                            cdata = cdata.substring(0, 2) + "|" + cdata.substring(2);
                                        } else {
                                            cdata = cdata.substring(2);
                                        }
                                    }

                                    //CONFIG COMBOBOX
                                    if (comboBox.compId.equals("MA021") || comboBox.compId.equals("EP010")
                                            || comboBox.compId.equals("EP025") || comboBox.compId.equals("EP060")
                                    ) {
                                        cdata = cdata.substring(0, 3);
                                    }

                                    //CONFIG COMBOBOX PEMDA BPHTB
                                    if (comboBox.compId.equals("EP075")) {
                                        cdata = cdata.substring(0, 4) + "|" + cdata.substring(5, 8);
                                    }

                                    //CONFIG COMBOBOX PEMDA PJDL & WEB-REG
                                    if (comboBox.compId.equals("EP130")) {
                                        cdata = cdata.substring(0, 4) + "|" + cdata.substring(0, 4);
                                    }

                                    if (comboBox.compId.equals("EP025")) {
                                        if (formId.equals("EP00310") && cdata.equals("PKD")) {
                                            //HANDLING ESAMSAT SAMOLNAS CHIP TO OTHER SERVICE
                                            newActionUrl = "EP0320";
                                        } else if (formId.equals("EP00312") && cdata.equals("PKD")) {
                                            //HANDLING ESAMSAT SAMOLNAS SWIPE TO OTHER SERVICE
                                            newActionUrl = "EP0322";
                                        }
                                    }
                                }

                                //Add Filter No Pajak & PEMDA ID PBB
                                try {
                                    String nops = cdata.substring(5);
                                    if (!nop.startsWith(nops)) {
                                        if (!nop.startsWith(PEMKAB_BANDUNG_NOP)) {
                                            JSONObject rps = new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"NOP tidak sesuai dengan Pemda yang dipilih\",\n" +
                                                    "\"value\":\"NOP tidak sesuai dengan Pemda yang dipilih\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                                                    "\"type\":\"3\",\"title\":\"Gagal\"}}");

                                            processResponse(rps, "000000");
                                            return;
                                        }
                                    }
                                } catch (Exception e) {
                                }

                                Log.d("EDIT READ", cdata);
                                if (actionUrl.equals("A54321")) {
                                    cdata = cdata.replace("Rp ", "").replace(".", "").replace(",00", "");
                                }

                                data.add(cdata);
                            } else {
                                cdata = comboBox.getSelectedItem().toString();
                                Log.d("EDIT READ", cdata);
                                if (actionUrl.equals("A54321")) {
                                    cdata = cdata.replace("Rp ", "").replace(".", "").replace(",00", "");
                                }
                                data.add(cdata);
                            }
                        }
                        if (v instanceof RadioButton) {
                            RadioButton radioButton = (RadioButton) v;
                            data.add(radioButton.isChecked() + "");
                        }
                        if (v instanceof CheckBox) {
                            CheckBox checkBox = (CheckBox) v;
                            data.add(checkBox.isChecked() + "");
                        }
                        if (v instanceof MagneticSwipe) {
                            MagneticSwipe magneticSwipe = (MagneticSwipe) v;
                            String pan = magneticSwipe.getText().toString();

                            if (actionUrl.equals("M0001A") || actionUrl.equals("M0006E")
                                    || actionUrl.equals("M0006C") || actionUrl.equals("M0006A")
                                    || actionUrl.equals("M0005A") || actionUrl.equals("P00032")
                                    || actionUrl.equals("P00034")) {
                                if (!pan.startsWith("110226")) {
                                    if (TAG_CARD != null) {
                                        if (TAG_CARD.equals("PETUGAS")) {
                                            JSONObject rps = new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"Kartu yang anda gunakan bukan kartu petugas\",\n" +
                                                    "\"value\":\"Kartu yang anda gunakan bukan kartu petugas\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                                                    "\"type\":\"3\",\"title\":\"Gagal\"}}");

                                            processResponse(rps, "000000");
                                            return;
                                        } else {
                                            data.add(pan);
                                        }
                                    } else {
                                        JSONObject rps = new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"Kartu yang anda gunakan bukan kartu petugas\",\n" +
                                                "\"value\":\"Kartu yang anda gunakan bukan kartu petugas\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                                                "\"type\":\"3\",\"title\":\"Gagal\"}}");

                                        processResponse(rps, "000000");
                                        return;
                                    }
                                } else {
                                    data.add(pan);
                                }
                            } else {
                                Boolean isMiniBanking = false;
                                for (int m = 0; m < miniBanking.length; m++) {
                                    if (actionUrl.equals(miniBanking[m])) {
                                        isMiniBanking = true;
                                    }
                                }

                                if (actionUrl.equals("E31000")) {
                                    pan = pan.substring(0, 16);
                                    data.add(pan);
                                }
//                                        else if (actionUrl.equals("MA0042")) {
//                                            if (!pan.startsWith("110226")) {
//                                                JSONObject rps = new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"Kartu yang anda gunakan tidak sesuai\",\n" +
//                                                        "\"value\":\"Kartu yang anda gunakan tidak sesuai\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
//                                                        "\"type\":\"3\",\"title\":\"Gagal\"}}");
//
//                                                processResponse(rps, "000000");
//                                                return;
//                                            } else {
//                                                data.add(pan);
//                                            }
//                                        }
//                                        else if (!isMiniBanking) {
//                                            if (pan.startsWith("110226")) {
//                                                JSONObject rps = new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"Kartu yang anda gunakan tidak sesuai\",\n" +
//                                                        "\"value\":\"Kartu yang anda gunakan tidak sesuai\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
//                                                        "\"type\":\"3\",\"title\":\"Gagal\"}}");
//
//                                                processResponse(rps, "000000");
//                                                return;
//                                            }
//                                        }
                                else {
                                    data.add(pan);
                                }
                            }
                            Log.d("EDIT READ", pan);
                        }
                        if (v instanceof ChipInsert) {
                            ChipInsert chipInsert = (ChipInsert) v;
                            data.add(chipInsert.getText().toString());
                        }
                        if (v instanceof InsertICC) {
                            InsertICC insertICC = (InsertICC) v;
//                            insertICC.funcActivity = (FuncActivity) context;
//                            insertICC.appState = ((FuncActivity) context).appState;
                            String track2 = insertICC.getText().toString();

                            if (actionUrl.equals("M0008B") || actionUrl.equals("M0010C")
                                    || actionUrl.equals("P00035")) {
                                if (!track2.startsWith("110226")) {
                                    JSONObject rps = new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"Kartu yang anda gunakan bukan kartu petugas\",\n" +
                                            "\"value\":\"Kartu yang anda gunakan bukan kartu petugas\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                                            "\"type\":\"3\",\"title\":\"Gagal\"}}");

                                    processResponse(rps, "000000");
                                    return;
                                } else {
                                    data.add(track2);
                                }
                            } else {
                                if (track2.startsWith("110226")) {
                                    JSONObject rps = new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"Kartu yang anda gunakan tidak sesuai\",\n" +
                                            "\"value\":\"Kartu yang anda gunakan tidak sesuai\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                                            "\"type\":\"3\",\"title\":\"Gagal\"}}");

                                    processResponse(rps, "000000");
                                    return;
                                } else if (actionUrl.equals("M0008A") || actionUrl.equals("M0010A")) {
                                    if (track2.startsWith("622011") || track2.startsWith("462040")) {
                                        data.add(track2);
                                    } else {
                                        JSONObject rps = new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"Kartu yang anda gunakan tidak sesuai\",\n" +
                                                "\"value\":\"Kartu yang anda gunakan tidak sesuai\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                                                "\"type\":\"3\",\"title\":\"Gagal\"}}");

                                        processResponse(rps, "000000");
                                        return;
                                    }
                                } else if (actionUrl.equals("M0011A") || actionUrl.equals("M0510A")) {
                                    if (track2.startsWith("622011") || track2.startsWith("462040")) {
                                        JSONObject rps = new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"Kartu yang anda gunakan tidak sesuai\",\n" +
                                                "\"value\":\"Kartu yang anda gunakan tidak sesuai\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                                                "\"type\":\"3\",\"title\":\"Gagal\"}}");

                                        processResponse(rps, "000000");
                                        return;
                                    } else {
                                        data.add(track2);
                                    }
                                } else {
                                    data.add(track2);
                                }
                            }
                            Log.d("EDIT READ", track2);
                        }
                    }

                } catch (Exception ex) {
//                            ex.printStackTrace();
                    StringWriter sw = new StringWriter();
                    PrintWriter ps = new PrintWriter(sw);
                    ex.printStackTrace(ps);
                }
            }
        }

//                if (actionUrl.equals(SERVICE_PURCHASE_SELADA)){
//                    data.add(nominal);
//                    data.add(mobileNumber);
//                    Log.d("DATA", data.toString());
//                }

        String dataOutput = TextUtils.join("|", data);
//                Toast.makeText(context, "Sending " + dataOutput, Toast.LENGTH_SHORT).show();
        if (actionUrl.equals("L00001")) {
            Location xLocation;
            GPSLocation gpsLocation = new GPSLocation();
            String gloc = "";
            try {
                InputStream inputStream = context.openFileInput("loc.txt");
                if (inputStream != null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String receiveString = "";
                    StringBuilder stringBuilder = new StringBuilder();
                    while ((receiveString = bufferedReader.readLine()) != null) {
                        stringBuilder.append(receiveString);
                    }
                    inputStream.close();
                    gloc = stringBuilder.toString();
                }
            } catch (FileNotFoundException e) {
                gloc = "0,0";
                try {
                    OutputStreamWriter osw = new OutputStreamWriter(context.openFileOutput("loc.txt", Context.MODE_PRIVATE));
                    osw.write("0,0");
                    osw.close();
                } catch (FileNotFoundException ee) {
                    ee.printStackTrace();
                } catch (IOException ee) {
                    ee.printStackTrace();
                }
                e.printStackTrace();
            } catch (IOException e) {
                gloc = "0,0";
                e.printStackTrace();
            }
            if (gpsLocation != null) {
//                        gloc += String.valueOf(xLocation.getLongitude());
//                        gloc += ",";
//                        gloc += String.valueOf(xLocation.getLatitude());
//                        Log.d("GPS", gloc);
            }
            String serialNum = Build.SERIAL;
            PackageInfo pInfo = null;
            try {
                pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            String version = pInfo.versionName;
            String ldata = String.format("%1$-10s", version);
            ldata += String.format("%1$-20s", serialNum);
            ldata += gloc;
//                    ldata = ldata + "                                                            ";
//                    ldata = ldata.substring(0,60);
            dataOutput = String.format("%1$-60s", ldata);
        }
        // settlement
        if (actionUrl.equals("S00001")) {
            JSONArray rowList = handleSettlement();
            for (int i = 0; i < rowList.length(); i++) {
                JSONObject row = rowList.getJSONObject(i);
                data.add(row.getString("stan"));
            }
            // test
//                    data.add("963018");
//                    data.add("963015");
            // end test
            dataOutput = TextUtils.join("|", data);
        }

        // reprint samsat
        if (actionUrl.equals("R0000A")) {
            String tid = preferences.getString("terminal_id", CommonConfig.DEV_TERMINAL_ID);
            String pid = SAMSAT_PROFILES;

            JSONObject rps = null;
            String simNumber = preferences.getString("sim_number", CommonConfig.INIT_SIM_NUMBER);
//            requestReprintReport(date, tid, pid, simNumber, "reprint");
            try {
                rps = JsonCompHandler.reprintFromArrest(pid, tid, stan, getContext());
            } catch (IOException e) {
                e.printStackTrace();
            }
            processResponse(rps, "reprint");
            return;
        }

        //Reprint MA
        if (actionUrl.equals("RMA020")) {
            String tid = preferences.getString("terminal_id", CommonConfig.DEV_TERMINAL_ID);
            String pid = MINI_BANKING_PROFILES;

            JSONObject rps = null;
            String simNumber = preferences.getString("sim_number", CommonConfig.INIT_SIM_NUMBER);
//            requestReprintReport(date, tid, pid, simNumber, "reprint");
            try {
                rps = JsonCompHandler.reprintFromArrest(pid, tid, stan, getContext());
            } catch (IOException e) {
                e.printStackTrace();
            }
            processResponse(rps, "reprint");
            return;
        }

        //Report Summary Samsat
        if (actionUrl.equals("R001A1") || actionUrl.equals("R001A2")) {
            String tid = preferences.getString("terminal_id", CommonConfig.DEV_TERMINAL_ID);
            String pid = SAMSAT_PROFILES;

            JSONObject rps = null;
            String simNumber = preferences.getString("sim_number", CommonConfig.INIT_SIM_NUMBER);
//            requestReprintReport(date, tid, pid, simNumber, "summary");
            try {
                rps = JsonCompHandler.reportFromArrest(pid, tid, date, getContext());
            } catch (IOException e) {
                e.printStackTrace();
            }
            processResponse(rps, "");
            return;
        }

        //Report Detail Samsat
        if (actionUrl.equals("R002A1") || actionUrl.equals("R002A2")) {
            String tid = preferences.getString("terminal_id", CommonConfig.DEV_TERMINAL_ID);
            String pid = SAMSAT_PROFILES;

            JSONObject rps = null;
            String simNumber = preferences.getString("sim_number", CommonConfig.INIT_SIM_NUMBER);
//            requestReprintReport(date, tid, pid, simNumber, "detail");
            try {
                rps = JsonCompHandler.reportDetailFromArrest(pid, tid, date, getContext());
            } catch (IOException e) {
                e.printStackTrace();
            }
            processResponse(rps, "");
            return;
        }

        //Report Detail Mini Banking
        if (actionUrl.equals("RMA014") || actionUrl.equals("RMA015")) {
            String tid = preferences.getString("terminal_id", CommonConfig.DEV_TERMINAL_ID);
            String pid = MINI_BANKING_PROFILES;

            JSONObject rps = null;
            String simNumber = preferences.getString("sim_number", CommonConfig.INIT_SIM_NUMBER);
//            requestReprintReport(date, tid, pid, simNumber, "detail");
            try {
                rps = JsonCompHandler.reportDetailFromArrest(pid, tid, date, getContext());
            } catch (IOException e) {
                e.printStackTrace();
            }
            processResponse(rps, "");
            return;
        }

        //Report Summary Mini Banking
        if (actionUrl.equals("RMA011") || actionUrl.equals("RMA012")) {
            String tid = preferences.getString("terminal_id", CommonConfig.DEV_TERMINAL_ID);
            String pid = MINI_BANKING_PROFILES;
            JSONObject rps = null;
            String simNumber = preferences.getString("sim_number", CommonConfig.INIT_SIM_NUMBER);
//            requestReprintReport(date, tid, pid, simNumber, "summary");
            try {
                rps = JsonCompHandler.reportFromArrest(pid, tid, date, getContext());
            } catch (IOException e) {
                e.printStackTrace();
            }
            processResponse(rps, "");
            return;
        }

        //Reversal Sale Selada
        if (actionUrl.equals("REV561")) {
            sendReversalAdviceSale(stanForReversal);
            return;
        }

        // reprint
        if (actionUrl.startsWith("P") && !actionUrl.equals("P00030") && !actionUrl.equals("P00031") && !actionUrl.equals("P00032") && !actionUrl.equals("P00033") && !actionUrl.equals("P00034") && !actionUrl.equals("P00010")) {
            JSONObject jsonResp = handleReprint(actionUrl);
            processResponse(jsonResp, "reprint");
            return;
        }
//                if (actionUrl.startsWith("R")) {
//                    JSONObject jsonResp = handleReport(actionUrl);
//                    processResponse(jsonResp, "report");
//                    return;
//                }
        if (actionUrl.startsWith(""))
            // void
            if (actionUrl.startsWith("V")) {
                JSONObject jsonResp = handleVoid(dataOutput);
                processResponse(jsonResp, "void");
                return;
            }
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            final String msgId = telephonyManager.getDeviceId() + sdf.format(new Date());
            msg.put("msg_id", msgId);
            msg.put("msg_ui", telephonyManager.getDeviceId());
            // HANDLING NEW ASSIGNED NEW ACTION URL IN THIS METHOD; EG FOR MPN G2 BJB
            if (!newActionUrl.equals("")) {
                msg.put("msg_si", newActionUrl);
            } else {
                msg.put("msg_si", actionUrl);
            }
            //ovride
            if (newAct != null) {
                msg.put("msg_si", newAct);
            }
            msg.put("msg_dt", dataOutput);

            //ADD JSON OBJECT msg_sn, msg_tid FOR ALL TRANSACTION (PENTEST)
            msg.put("msg_sn", preferences.getString("sim_number", CommonConfig.INIT_SIM_NUMBER));
            msg.put("msg_tid", preferences.getString("terminal_id", CommonConfig.DEV_TERMINAL_ID));
//                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                    builder.setMessage(dataOutput);
//                    builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            dialogInterface.dismiss();
//                        }
//                    });
//                    AlertDialog diaLog = builder.create();
//                    diaLog.show();

                    //enc
//                    try {
//                        msg.put("encrypted", "t");
//                        msg.put("msg_dt", compress(dataOutput));
//                    } catch (Exception e) {
//                        msg.put("msg_dt", dataOutput);
//                    }

                    final JSONObject msgRoot = new JSONObject();
                    msgRoot.put("msg", msg);
//                    new PostData().execute(msgRoot.toString());

            JsonCompHandler.saveJsonMessage(context, msgId, "rq", msgRoot);
            // HANDLING NEW ASSIGNED NEW ACTION URL IN THIS METHOD; EG FOR MPN G2 BJB
            if (!newActionUrl.equals("")) {

                saveEdcLog(newActionUrl, msgId);
            } else {

                saveEdcLog(actionUrl, msgId);
            }
            if (panHolder != null) {
                updEdcLogPan(msgId, panHolder, tcaid);
            }

            String hostname = preferences.getString("hostname", CommonConfig.HTTP_REST_URL);
            String postpath = preferences.getString("postpath", CommonConfig.POST_PATH);
            String httpPost = CommonConfig.HTTP_PROTOCOL + "://" + hostname + "/" + postpath;

            StringRequest jor = new StringRequest(Request.Method.POST,
                    httpPost,
                    response -> {
                        try {
                            Log.d("TERIMA", response);
                            JSONObject jsonResp = new JSONObject(response);
                            if (jsonResp.has("encrypted")) {
                                String isEnc = jsonResp.getString("encrypted");
                                if (isEnc.equals("t")) {
                                    jsonResp = decResponse(jsonResp);
                                }
                            }
                            dialog.dismiss();
                            processResponse(jsonResp, msgId);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                        Toast.makeText(context, "Request Timeout",
                                Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        context.onBackPressed();
                        context.finish();
                    }
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "text/plain; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {

                        return msgRoot == null ? null : msgRoot.toString().getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        Log.e("VOLLEY", "Unsupported Encoding while trying to get the bytes of " + msgRoot.toString() + "utf-8");
                        return null;
                    }
                }

            };
            RequestQueue rq = Volley.newRequestQueue(context);

            jor.setRetryPolicy(new DefaultRetryPolicy(100000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//                    dialog = ProgressDialog.show(context, "Silahkan Tunggu....", "Data sedang dikirim", true);
            dialog = ProgressDialog.show(context, "Silahkan Tunggu....", "Transaksi sedang diproses", true);
            rq.add(jor);
            // display data to dialog
//                    dialog = ProgressDialog.show(context, "Debug send data", msgRoot.toString(), true);
//                    Handler dhan = new Handler();
//                    Runnable dr = new Runnable() {
//                        @Override
//                        public void run() {
//                            dialog.dismiss();
//                        }
//                    };
//                    dhan.postDelayed(dr, 8000);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        Log.d("PINPAD", "KEYCODE = " + keyCode);
//        Log.d("PINPAD", "EVENT = " + event.toString());
        return super.onKeyUp(keyCode, event);
    }

    private void init() throws JSONException {
        String id = comp.getString("id");
        if (comp.has("fallback")) {
            fallback = true;
        }
        if (comp.has("title")) {
//            AutofitTextView tv = (AutofitTextView) context.findViewById(R.id.title_list);
//            tv.setText(comp.getString("title"));
//            Log.d("TITH", tv.getHeight()+"");
//            Log.d("TITCH", comp.getString("title").length()+"");
        }
        if (comp.has("stan")) {
            lastan = comp.getString("stan");
        }
        if (comp.has("emode")) {
            emode = comp.getString("emode");
        }
        formId = id;
        Log.i("FORM", "Init : " + id);
        if (nomorKartu == null) {
            nomorKartu = "";
        }
        if (cardType == null) {
            cardType = "";
        }
        JSONArray array = comp.getJSONObject("comps").getJSONArray("comp");
        hasTapModule = false;
        hasMagModule = false;
        pinModuleCounter = 0;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        );
        params.setMargins(0, 20, 0, 0);
        baseLayout.removeAllViews();

        for (int j = 0; j < array.length(); j++) {
            final JSONObject data = array.getJSONObject(j);
            int seq = data.getInt("seq");
//                Log.d("ITERASI2", ""+j);
            int type = data.getInt("comp_type");
            String lbl = data.getString("comp_lbl");
            Log.d("INIT", lbl);
            if (lbl.equals("voidsukses")) {
                JSONObject cvals = data.getJSONObject("comp_values");
                JSONArray cval = cvals.getJSONArray("comp_value");
                JSONObject fval = cval.getJSONObject(0);
                String stanVoidSukses = fval.getString("value");
                updVoidSukses(stanVoidSukses, lastan);
            }
            if (lbl.equals("track2")) {
                JSONObject cvals = data.getJSONObject("comp_values");
                JSONArray cval = cvals.getJSONArray("comp_value");
                JSONObject fval = cval.getJSONObject(0);
                String track2 = fval.getString("value");
                if (track2 != null) {
                    if (track2.length() > 15) {
                        panHolder = panFromTrack2(track2);
                        boolean echip = emode.substring(1, 2).equals("5");
                        if (echip) {
                            if (parent.cardData != null) {
                                parent.cardData.setPan(panFromTrack2(track2));
                                parent.cardData.setTxst(0);
                            }
                        }
                    }
                }
            }

//            switch (type) {
//                case CommonConfig.ComponentType.TextView:
//                    TextView textView = (TextView) li.inflate(R.layout.text_view, null);
//                    textView.init(data);
//                    textView.setTag(R.string.seq_holder, seq);
//                    textView.setTag(R.string.lbl_holder, lbl);
//                    textView.setLayoutParams(params);
//
//                    baseLayout.addView(textView);
//                    break;
//                case CommonConfig.ComponentType.EditText:
//                    final EditText editText = (EditText) li.inflate(R.layout.edit_text, null);
//                    editText.init(data);
//                    String txt = editText.getText().toString();
//                    editText.setTag(R.string.seq_holder, seq);
//                    editText.setTag(R.string.lbl_holder, lbl);
//                    editText.setLayoutParams(params);
//
//                    if (formId.equals(MENU_PUCHASE)){
//                        if (editText.getHint().toString().contains("Masukkan Nominal Sale")){
//                            editText.setText(amount);
//                            editText.setEnabled(false);
//                        }
//                        if (editText.getHint().toString().contains("Masukkan Kode Billing")) {
//                            editText.setText(mobileNumber);
//                            editText.setEnabled(false);
//                        }
//                        if (editText.getHint().toString().contains("Biaya Margin")) {
//                            editText.setText(margin);
//                            editText.setEnabled(false);
//                        }
//                    }
//
//                    if (formId.equals(MENU_PUCHASE_BJB)){
//                        if (editText.getHint().toString().contains("Masukkan Nominal Sale")){
//                            editText.setText(amount);
////                                editText.setFocusable(false);
//                            editText.setEnabled(false);
//
//                            editText.setEnabled(false);
//                            editText.setFocusable(false);
//                            editText.setClickable(false);
//                            editText.setInputType(InputType.TYPE_NULL);
//                            editText.setOnKeyListener(new View.OnKeyListener() {
//                                @Override
//                                public boolean onKey(View v,int keyCode,KeyEvent event) {
//                                    return true;  // Blocks input from hardware keyboards.
//                                }
//                            });
//                        }
//                    }
//                }
                switch (type) {
                    case CommonConfig.ComponentType.TextView:
                        TextView textView = (TextView) li.inflate(R.layout.text_view, null);
                        textView.init(data);
                        textView.setTag(R.string.seq_holder, seq);
                        textView.setTag(R.string.lbl_holder, lbl);

                        textView.setLayoutParams(params);
                        baseLayout.addView(textView);
                        break;
                    case CommonConfig.ComponentType.EditText:
                        final EditText editText = (EditText) li.inflate(R.layout.edit_text, null);
                        editText.init(data);
                        String txt = editText.getText().toString();
                        editText.setTag(R.string.seq_holder, seq);
                        editText.setTag(R.string.lbl_holder, lbl);
                        editText.setLayoutParams(params);

                        if (formId.equals(MENU_PUCHASE)){
                            if (editText.getHint().toString().contains("Masukkan Nominal Sale")){
                                editText.setText(amount);
                                editText.setEnabled(false);
                            }
                            if (editText.getHint().toString().contains("Masukkan Kode Billing")) {
                                editText.setText(mobileNumber);
                                editText.setEnabled(false);
                            }
                            if (editText.getHint().toString().contains("Biaya Margin")) {
                                editText.setText(margin);
                                editText.setEnabled(false);
                            }
                        }

                        if (formId.equals(MENU_PUCHASE_BJB)){
                            if (editText.getHint().toString().contains("Masukkan Nominal Sale")){
                                editText.setText(amount);
                            }
                        }
                    // for purchase bjb-selada only

//                        if (editText.getHint().toString().equals("Jumlah Transfer")
//                                || editText.getHint().toString().equals("Nominal Setor")
//                                || editText.getHint().toString().equals("Jumlah Penarikan")
//                                || editText.getHint().toString().contains("Nominal")
//                                )
//                        {
//                            editText.addTextChangedListener(new TextWatcher() {
//                                @Override
//                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//                                }
//
//                                @Override
//                                public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//                                }
//
//                                @Override
//                                public void afterTextChanged(Editable editable) {
//                                    editText.removeTextChangedListener(this);
//                                    try {
//                                        StringBuilder originalString = new StringBuilder(editable.toString().replaceAll(",", ""));
//                                        int indx = 0;
//                                        for (int i = originalString.length(); i > 0; i--) {
//                                            if (indx % 3 == 0 && indx > 0)
//                                                originalString = originalString.insert(i, ",");
//                                            indx++;
//                                        }
//                                        editText.setText(originalString);
//                                        editText.setSelection(originalString.length());
//                                    } catch (NumberFormatException nfe) {
//                                        nfe.printStackTrace();
//                                    }
//                                    editText.addTextChangedListener(this);
//                                }
//                            });
//                        }

                    baseLayout.addView(editText);
                    break;
                case CommonConfig.ComponentType.PasswordField:
                    EditText pinpadText = null;
                    pinpadText = new EditText(context);
                    pinpadText.init(data);
                    pinpadText.setTag(R.string.seq_holder, seq);
                    pinpadText.setTag(R.string.lbl_holder, lbl);
                    pinpadText.setLayoutParams(params);
                    if (pinpadText.isNumber()) {
                        pinpadText.setVisibility(GONE);
                    }

                    baseLayout.addView(pinpadText);
                    pinpadTextList.add(pinpadText);
                    pinModuleCounter++;
                    break;
                case CommonConfig.ComponentType.Button:
                    final String actionURl = comp.has("action_url") ? comp.getString("action_url") : "";
                    Button button = (Button) li.inflate(R.layout.button, null);
                    button.init(data);
                    button.setTag(R.string.seq_holder, seq);
                    button.setTag(R.string.lbl_holder, lbl);
                    button.setLayoutParams(params);
                    button.setTag(actionURl);
                    button.setOnClickListener(FormMenu.this);
                    button.setOnKeyListener(FormMenu.this);
                    baseLayout.addView(button);
                    formTrigger = button;
                    break;
                case CommonConfig.ComponentType.CheckBox:
                    CheckBox checkBox = (CheckBox) li.inflate(R.layout.check_box, null);
                    checkBox.init(data);
                    checkBox.setTag(R.string.seq_holder, seq);
                    checkBox.setTag(R.string.lbl_holder, lbl);
                    checkBox.setLayoutParams(params);
                    baseLayout.addView(checkBox);
                    break;
                case CommonConfig.ComponentType.RadioButton:
                    RadioButton radioButton = (RadioButton) li.inflate(R.layout.radio_button, null);
                    radioButton.setTag(R.string.seq_holder, seq);
                    radioButton.setTag(R.string.lbl_holder, lbl);
                    radioButton.init(data);
                    radioButton.setLayoutParams(params);
                    baseLayout.addView(radioButton, seq);
                    break;
                case CommonConfig.ComponentType.ComboBox:
                    ComboBox comboBox = (ComboBox) li.inflate(R.layout.spinner, null);
                    comboBox.setTag(R.string.seq_holder, seq);
                    comboBox.setTag(R.string.lbl_holder, lbl);
                    comboBox.init(data);
                    comboBox.setLayoutParams(params);
                    baseLayout.addView(comboBox, seq);
                    break;
                case CommonConfig.ComponentType.ChipInsert:
                    if (!isReprint) {
                        String comid = data.getString("comp_id");
                        int iccStage = Integer.parseInt(comid.substring(2, 3));
                        parent.modulStage = iccStage;
                        Log.i("ICC", "" + iccStage);
                        iccPreProcessed = true;
                        iccIsTriggerd = true;
                        if (parent.modulStage != CommonConfig.ICC_PROCESS_STAGE_FINISHED) {
                            if (insertICC != null) {
                                try {
                                    insertICC.closeDriver();
                                    insertICC = null;
                                } catch (Exception e) {
                                }
                            }
                            insertICC = (InsertICC) li.inflate(R.layout.icc_insert, null);
//                            insertICC.funcActivity = (FuncActivity) context;
//                            insertICC.appState = ((FuncActivity) context).appState;
                            insertICC.addInputListener(this);
                        }
                        insertICC.setTag(R.string.seq_holder, seq);
                        insertICC.setTag(R.string.lbl_holder, lbl);
                        baseLayout.addView(insertICC);
                        insertICC.setVisibility(GONE);
                    }
                    break;
                case CommonConfig.ComponentType.InsertTap:
                    break;
                case CommonConfig.ComponentType.MagneticSwipe:
                    magneticSwipe = (MagneticSwipe) li.inflate(R.layout.magnetic_swipe, null);
                    magneticSwipe.init();
                    magneticSwipe.setTag(R.string.seq_holder, seq);
                    magneticSwipe.setTag(R.string.lbl_holder, lbl);
                    magneticSwipe.addSwipeListener(this);
                    LayoutInflater li = LayoutInflater.from(context);
                    View promptsView = li.inflate(R.layout.swipe_dialog, null);
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                    // set prompts.xml to alertdialog builder
                    alertDialogBuilder.setView(promptsView);
                    //alertDialogBuilder.setCancelable(false);
                    // create alert dialog
                    alert = alertDialogBuilder.create();
                    hasMagModule = true;
                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                    lp.copyFrom(alert.getWindow().getAttributes());
                    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                    lp.height = WindowManager.LayoutParams.MATCH_PARENT;
                    // show it
                    alert.setOnKeyListener(new Dialog.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                            if (keyCode == KeyEvent.KEYCODE_BACK) {
//                                    Log.d("BACK", "FROM DIALOG");
                                try {
                                    magneticSwipe.openDriver();
                                    magneticSwipe.closeDriver();
                                    magneticSwipe.setIsQuit(true);
                                    alert.dismiss();
                                    context.onBackPressed();
                                } catch (Exception e) {
                                    //failed to close, maybe already closed or not open yet
                                }
                            }
                            return true;
                        }
                    });
                    if (CommonConfig.getDeviceName().startsWith("WizarPOS")) {
                        alert.show();
                        if (SWIPELESS) {
                            magneticSwipe.setText(dummyTrack);

//                                magneticSwipe.setText("5221842001365318=18111260000058300000");
//                                onSwipeComplete(magneticSwipe, "5221842001365318=18111260000058300000");
                        }
                        if (SWIPEANY) {
                            magneticSwipe.setText(dummyTrack);
                        }
                    } else {
//                            magneticSwipe.setText("6013010612793951=17121200000075600000");
                        magneticSwipe.setText(dummyTrack);
                        SWIPELESS = true;
                        SWIPEANY = true;
                    }

                    alert.getWindow().setAttributes(lp);

                    baseLayout.addView(magneticSwipe);
                    magneticSwipe.setVisibility(GONE);
                    break;
                case CommonConfig.ComponentType.SwipeInsert:
                    break;
                case CommonConfig.ComponentType.SwipeInsertTap:
                    break;
                case CommonConfig.ComponentType.SwipeTap:
                    break;
                case CommonConfig.ComponentType.TapCard:
                    hasTapModule = true;
                    break;
            }
        }


        if (!isReprint) {
            if (insertICC != null) {
                if (parent.modulStage == CommonConfig.ICC_PROCESS_STAGE_FINISHED) {
                    iccIsTriggerd = false;
                    txVerification = true;
                    WindowManager.LayoutParams icclp = refreshICCProcessDialog("Melakukan verifikasi transaksi", true);
                    alert.show();
                    alert.getWindow().setAttributes(icclp);
                    Log.i("ICCC", "Continue");
                    String[] suppliedData = getIccDataFromComp(comp);
//                    parent.cardData.setArc(suppliedData[2]);
                    parent.cardData.setArpc(suppliedData[2]);
                    insertICC.cont(parent.modulStage, parent.cardData);
                }
            }
        } else {
            if (comp.has("tcaid")) {
                String tcaid = comp.getString("tcaid");
                String tc = tcaid.substring(0, 16);
                String aid = tcaid.substring(16);
                if (comp.has("pan")) {
                    panHolder = comp.getString("pan");
                }
                emode = "051";
                if (parent.cardData == null) {
                    parent.cardData = new NsiccsData();
                }
                parent.cardData.setPan(panHolder);
                parent.cardData.setTc(tc);
                parent.cardData.setAid(aid);
                parent.cardData.setTxst(1);
            }
        }

        boolean hasChip = false;
        if (comp != null) {
            JSONObject comps = comp.has("comps") ? comp.getJSONObject("comps") : null;
            if (comps != null) {
                JSONArray comp = comps.has("comp") ? comps.getJSONArray("comp") : null;
                if (comp != null && comp.length() > 0) {
                    for (int i = 0; i < comp.length(); i++) {
                        JSONObject cmp = comp.getJSONObject(i);
                        if (cmp != null & cmp.has("comp_id")) {
                            String compId = cmp.getString("comp_id");
                            if (compId.equalsIgnoreCase("I0209")) {
                                hasChip = true;
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (hasChip) {
            if (formId.equals("MA00050") || formId.equals("POC0020")
                    || formId.equals("MA00070") || formId.equals("MA00065")
                    || formId.equals("MA00055") || formId.equals("MA00035")
                    || formId.equals("MA00075") || formId.equals("MA00095")
                    || formId.equals("MA00085") || formId.equals("MA00025")
                    || formId.equals("MA00015") || formId.equals("EPG0300")
                    || formId.equals("EPG0310") || formId.equals("EPG0410")
                    || formId.equals("EPG0510") || formId.equals("EPG0610")
                    || formId.equals("EPG0710") || formId.equals("EPG0810")
                    || formId.equals("EPG0820") || formId.equals("EPG0910")
//                    || formId.equals(MENU_PUCHASE)
            ) {
                //Skip icc
                insertICC.removeCardFirst = true;
            }
//            else {
//                insertICC.isByPass = true;
////                showIccDialog(null);
//            }
        }

        if (!txVerification) {
            preparePrint();
        }


    }

    private void preparePrint() {
        try {
            JSONArray array = comp.getJSONObject("comps").getJSONArray("comp");
            if (comp.has("print")) {
                if (!comp.get("print").equals(null)) {
                    int print = comp.getInt("print");
//                Log.d(TAG, "SET PRINT " + print);
                    if (print > 0) {
                        switch (print) {
                            case 1:
                                printBtn = printView(li);
                                attachPrint();
                                if (countPrintButton > 1) {
                                    dettachPrint();
                                }
                                break;
                            case 2:
                                printBtn = printView(li);
                                attachPrint();
                                break;
                            case 3://isi ulang 1x lsg cetak, + 2x print CMB
                                printBtn = printView(li);
                                attachPrint();
                                print();
                                if (countPrintButton > 2) {
                                    dettachPrint();
                                }
                                break;
                            case 4://info saldo print 2x optional
                                printBtn = printView(li);
                                attachPrint();
                                if (countPrintButton > 2) {
                                    dettachPrint();
                                }
                                break;
                            case 5://trf print 2x wajib
                                for (int i = 0; i < 2; i++) {
                                    print();
                                }
                                break;
                            case 6://print 1x auto
                                print();
                                break;
                        }
                        if (comp.has("server_ref")) {
                            serverRef = comp.getString("server_ref");
                        }
                        if (comp.has("server_appr")) {
                            serverAppr = comp.getString("server_appr");
                        }
                        if (comp.has("server_date")) {
                            serverDate = comp.getString("server_date");
                        }
                        if (comp.has("server_time")) {
                            serverTime = comp.getString("server_time");
                        }
                    }
                }
            }
            if (SWIPELESS) {
                if (hasMagModule) {
                    onSwipeComplete(magneticSwipe, dummyTrack);
                } else {
                    int viscount = 0;
                    int visindex = 0;
                    for (int ch = 0; ch < baseLayout.getChildCount(); ch++) {
                        if (baseLayout.getChildAt(ch).getVisibility() == VISIBLE) {
                            viscount++;
                            visindex = ch;
                        }
                    }
                    if (viscount == 1 && array.getJSONObject(visindex).getInt("comp_type") == CommonConfig.ComponentType.Button) {
                        Button proses = (Button) baseLayout.getChildAt(visindex);
                        proses.setVisibility(GONE);
                        if (proses.performClick()) {
                            //ok
                        } else {
                            //at least we tried
                        }
                    }
                }
//            Log.d(TAG, "P-Check : " + String.valueOf(viscount) + String.valueOf(visindex));
            } else {
                if (!hasMagModule) {
                    int viscount = 0;
                    int visindex = 0;
                    for (int ch = 0; ch < baseLayout.getChildCount(); ch++) {
                        if (baseLayout.getChildAt(ch).getVisibility() == VISIBLE) {
                            viscount++;
                            visindex = ch;
                        }
                    }
                    Log.i("viscount", "" + viscount);
                    if (viscount == 1 && array.getJSONObject(visindex).getInt("comp_type") == CommonConfig.ComponentType.Button) {
                        Button proses = (Button) baseLayout.getChildAt(visindex);
                        proses.setVisibility(GONE);
                        if (proses.performClick()) {
                            //ok
                        } else {
                            //at least we tried
                        }
                    }
                }
            }
            if (formId.equals("2A2000F")) {
                print();
                context.onBackPressed();
            }

            if (formId.equals(SCREEN_PURCHASE_SELADA) || formId.equals(SCREEN_PURCHASE_FALLBACK)) {
//                context.finish();
//                print();
//                context.finish();


                    Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage("com.boardinglabs.mireta.selada");
                    Bundle bundle = new Bundle();


                    Log.d("EDC: SVCID", serviceId);
                    Log.d("EDC: mid", mid);
                    Log.d("EDC: mobileNumber", mobileNumber);
                    Log.d("EDC: nominal", nominal);
                    Log.d("EDC: amount", amount);
                    Log.d("EDC: stan", lastan);


                    bundle.putString("serviceId", serviceId);
                    bundle.putString("mid", mid);
                    bundle.putString("mobileNumber", mobileNumber);
                    bundle.putString("nominal", nominal);
                    bundle.putString("amount", amount);
                    bundle.putString("stan", lastan);
                    Log.d("stan", lastan);


                    if (launchIntent != null) {
                        Log.d("EDC AFTER: SVCID", bundle.getString("serviceId"));
                        Log.d("EDC AFTER: mid", bundle.getString("mid"));
                        Log.d("EDC AFTER: mobileNumber", bundle.getString("mobileNumber"));
                        Log.d("EDC AFTER: nominal", bundle.getString("nominal"));
                        Log.d("EDC AFTER: amount", bundle.getString("amount"));
                        Log.d("EDC AFTER: stan", bundle.getString("stan"));
                        launchIntent.putExtras(bundle);
                        context.startActivity(launchIntent);//null pointer check in case package name was not found
                        context.finish();
                }

            }

            if (formId.equals(SCREEN_PURCHASE_BJB) || formId.equals(SCREEN_PURCHASE_BJB_FALLBACK)) {
//                context.finish();
//                print();
//                context.finish();
                if (json != null) {
                    Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage("com.boardinglabs.mireta.selada");
                    Bundle bundle = new Bundle();

                    bundle.putString("json", json);
                    bundle.putString("stan", lastan);

                    if (launchIntent != null) {
                        Log.d("EDC AFTER: JSON", bundle.getString("json"));
                        launchIntent.putExtras(bundle);
                        context.startActivity(launchIntent);//null pointer check in case package name was not found
                        context.finish();
                    }
                }
            }

            focusHandler.postDelayed(delayFocus, 400);
            if (formId.equals("292000F")) {
                dettachPrint();
            }
            String dbgtxt = "AFTER INIT :";
            dbgtxt += "Count=" + baseLayout.getChildCount();
            for (int i = 0; i < baseLayout.getChildCount(); i++) {
                dbgtxt += "\n" + baseLayout.getChildAt(i).getTag(R.string.lbl_holder).toString();
            }
//        Toast.makeText(context, dbgtxt, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void attachPrint() {
//        Log.d("FORM", "set footer");
        if (context instanceof ActivityList) {
            ((ActivityList) context).attachFooter(printBtn);
        } else {
            baseLayout.addView(printBtn);
        }
    }

    private void dettachPrint() {
//        Log.d("FORM", "unset footer");
        if (context instanceof ActivityList) {
            ((ActivityList) context).detachFooter();
        } else {
            printBtn.setVisibility(GONE);
        }

    }

    private LinearLayout printView(LayoutInflater li) {
        LinearLayout printConfirmationView = new LinearLayout(context);
        confirmationText = new android.widget.TextView(context);
        LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );
        printConfirmationView.setOrientation(LinearLayout.VERTICAL);
        printConfirmationView.setLayoutParams(nlp);
        if (isReprint) {
            countPrintButton = 4;
        }
        confirmationText.setText(printConfirm[countPrintButton]);
        if (formId.equals("281000F")) {
            confirmationText.setText("Print Settlement ?");
        }
        if (formId.equals("STL0001")) {
            confirmationText.setText("Print Settlement ?");
        }
        if (formId.startsWith("R")) {
            confirmationText.setText("Print Report ?");
        }
        if (formId.equals("71000FF") || formId.equals("721000F") || formId.equals("731000F")) {
            confirmationText.setText(printConfirmTbank[countPrintButton]);
        }
        Button printBtn = (Button) li.inflate(R.layout.button, null);
        printBtn.setText("Ya");
        printBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    print();
//                    countPrint++;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        Button noBtn = (Button) li.inflate(R.layout.button, null);
        noBtn.setText("Tidak");
        noBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                doNotPrint();
            }
        });
        printConfirmationView.addView(confirmationText);
        printConfirmationView.addView(printBtn);
        printConfirmationView.addView(noBtn);
        return printConfirmationView;
    }

    private void print() throws JSONException {
        SharedPreferences preferences;
        DataBaseHelper helperDb = new DataBaseHelper(context);
        SQLiteDatabase clientDB = null;
        boolean iccPrint = false;
        List<PrintSize> data = new ArrayList<>();
        List<String> mdata = new ArrayList<>();
        try {
            helperDb.openDataBase();
            clientDB = helperDb.getActiveDatabase();
            String getStanSeq = "select stan from edc_log order by log_id desc";
            Cursor stanSeq = clientDB.rawQuery(getStanSeq, null);
            int msgStan = 0;
            try {
                if (stanSeq != null) {
                    if (stanSeq.moveToFirst()) {
                        msgStan = Integer.parseInt(stanSeq.getString(0));
                    } else {
                        msgStan = 1;
                    }
                }
            } catch (Exception e){
            }

//        Log.d("DEBUG", "STAN : " + msgStan);
            stanSeq.close();
            if (comp.has("stan")) {
                msgStan = Integer.parseInt(comp.getString("stan"));
            }
            if (comp.has("rrn")) {
                serverRef = comp.getString("rrn");
            }
            if (comp.has("svrappr")) {
                serverAppr = comp.getString("svrappr");
            }
            String batchNo = "";
            if (formId.startsWith("2")) {
                String getBatchNo = "select batch from holder";
                Cursor batchSeq = clientDB.rawQuery(getBatchNo, null);
                int b = 0;
                if (batchSeq != null) {
                    if (batchSeq.moveToFirst()) {
                        b = batchSeq.getInt(0);
                        batchNo = StringLib.fillZero(String.valueOf(b), 6);
                    }
                }
                batchSeq.close();
                batchSeq = null;
            }
            preferences = context.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
            mdata.add(preferences.getString("merchant_name", CommonConfig.INIT_MERCHANT_NAME));
            mdata.add(preferences.getString("merchant_address1", CommonConfig.INIT_MERCHANT_ADDRESS1));
            mdata.add(preferences.getString("merchant_address2", CommonConfig.INIT_MERCHANT_ADDRESS2));
            String tid = preferences.getString("terminal_id", CommonConfig.DEV_TERMINAL_ID);
            String mid = preferences.getString("merchant_id", CommonConfig.DEV_MERCHANT_ID);
            String stan = StringLib.fillZero(String.valueOf(msgStan), 6);
            data.add(new PrintSize(FontSize.EMPTY, "\n"));

            if (magneticSwipe != null && (!formId.equals("640000F"))) {
                String track2Data = magneticSwipe.getText().toString();
                if (!track2Data.equals("")) {
                    track2Data = track2Data.split("=")[0];
                    track2Data = track2Data.substring(0, 6) + "******" + track2Data.substring(12);
//                data.add(new PrintSize(FontSize.BOLD, "No Kartu : "));
//                data.add(new PrintSize(FontSize.EMPTY, "\n"));
//                data.add(new PrintSize(FontSize.BOLD, track2Data + "\n"));
//                data.add(new PrintSize(FontSize.EMPTY, "\n"));
//                nomorKartu = "************" + track2Data.substring(12);
                    nomorKartu = track2Data;
                }
            }
            if (formId.equals("640000F")) {
                nomorKartu = "";
            }
            String ptx = "";
            if (comp.has("print_text")) {
                ptx = comp.getString("print_text");
            }
            boolean echip = emode.substring(1, 2).equals("5");
            if (parent.cardData != null && echip) {
                iccPrint = true;
                if (parent.cardData.getPan() != null) {
                    data.add(new PrintSize(FontSize.NORMAL, "CardType : DEBIT\n"));
                    String karno = "************" + parent.cardData.getPan().substring(12);
                    data.add(new PrintSize(FontSize.BOLD, karno + "-(C)\n"));
                    data.add(new PrintSize(FontSize.EMPTY, "\n"));
                    nomorKartu = "";
                    if (parent.cardData.getTxst() == 0) {
                        iccPrint = false;
                    }
                } else {
                    iccPrint = false;
                }
            } else {
                if (panHolder != null && !panHolder.equals("") && !ptx.startsWith("STL")) {
                    nomorKartu = panFromTrack2(panHolder);
                    nomorKartu = nomorKartu.substring(0, 6) + "******" + nomorKartu.substring(12);
                }
            }
            data.add(new PrintSize(FontSize.TITLE, comp.getString("title") + "\n"));
            data.add(new PrintSize(FontSize.EMPTY, "\n"));
            JSONArray array = comp.getJSONObject("comps").getJSONArray("comp");
            for (int i = 0; i < array.length(); i++) {
                for (int j = 0; j < array.length(); j++) {
                    final JSONObject dataArr = array.getJSONObject(j);
                    int seq = dataArr.getInt("seq");
                    boolean comp_visible = dataArr.getBoolean("visible");
                    if (seq == i && comp_visible) {
                        JSONObject val = dataArr.getJSONObject("comp_values").getJSONArray("comp_value").getJSONObject(0);
                        FontSize size = FontSize.NORMAL;
                        String lbl = dataArr.getString("comp_lbl");
                        String value = val.getString("print");
                        value = value.replaceFirst("\\s++$", "");
                        // tambahin ntpn: value diganti strip, valuenya "null", compid nya M1012
                        if (dataArr.getString("comp_id").equals("M1012") && lbl.contains("NTPN") && (value == null || value.equals("null"))) {
                            value = "-";
                        }
                        if (!lbl.startsWith("--S")) {
                            if (lbl.startsWith("--B")) {
                                lbl = lbl.substring(3);
                                size = FontSize.BOLD_3;
                                data.add(new PrintSize(size, lbl));
                                data.add(new PrintSize(size, " "));
                                data.add(new PrintSize(size, value + "\n"));
                            } else {
                                if (lbl.startsWith("[")) {
                                    String tag = lbl.substring(1, lbl.indexOf("]"));
                                    if (tag.matches(".*\\d+.*")) {

                                        if (tag.startsWith("B")) {
                                            tag = tag.substring(1);
                                            tag = "BOLD_" + tag;

                                        } else {
                                            tag = "NORMAL_" + tag;
                                        }
                                    } else {

                                        if (tag.startsWith("B")) {
                                            tag = "BOLD_2";

                                        } else {
                                            tag = "NORMAL";
                                        }
                                    }
                                    Log.i("zzzz", tag);
                                    size = FontSize.valueOf(tag);
                                    lbl = lbl.substring(lbl.indexOf("]") + 1);
                                }
                                data.add(new PrintSize(size, lbl));
                                data.add(new PrintSize(size, " "));
                                if (value.startsWith("[")) {
                                    String tag = value.substring(1, value.indexOf("]"));
                                    if (tag.matches(".*\\d+.*")) {

                                        if (tag.startsWith("B")) {
                                            tag = tag.substring(1);
                                            tag = "BOLD_" + tag;

                                        } else {
                                            tag = "NORMAL_" + tag;
                                        }
                                    } else {

                                        if (tag.startsWith("B")) {
                                            tag = "BOLD_2";

                                        } else {
                                            tag = "NORMAL";
                                        }
                                    }
                                    Log.i("zzzz", tag);
                                    size = FontSize.valueOf(tag);
                                    value = value.substring(value.indexOf("]") + 1);
                                }
                                data.add(new PrintSize(size, value + "\n"));
//                    data.add(new PrintSize(FontSize.EMPTY, "\n"));
                            }
                        }
                    }
                }
            }
//        Thread thread = new Thread(new PrintData(data));
            if (iccPrint) {
                data.add(new PrintSize(FontSize.EMPTY, "\n"));
                data.add(new PrintSize(FontSize.NORMAL, "TC - " + parent.cardData.getTc() + "\n"));
                data.add(new PrintSize(FontSize.NORMAL, "AID - " + parent.cardData.getAid() + "\n"));
                data.add(new PrintSize(FontSize.EMPTY, "\n"));
                ptx = "ICC";
            }
            if (countPrintButton < 5) {
                Thread thread = new Thread(new PrintData(data, mdata, tid, mid, stan, ptx,
                        countPrintButton, serverRef, serverDate, serverTime, nomorKartu, cardType, batchNo, serverAppr));
                thread.start();
            }
            countPrintButton++;
            if (!(formId.equals("71000FF") || formId.equals("721000F") ||
                    formId.equals("731000F") || formId.equals("521000F") ||
                    ptx.startsWith("STL") || ptx.startsWith("RP"))) {
                confirmationText.setText(printConfirm[countPrintButton]);
            }
            if (formId.equals("71000FF") || formId.equals("721000F") || formId.equals("731000F")) {
                confirmationText.setText(printConfirmTbank[countPrintButton]);
            }
            if (formId.equals("290000F")) {
                confirmationText.setText(printConfirm[countPrintButton]);
            }
            if (formId.equals("521000F") ||
                    formId.equals("220000F") ||
                    formId.equals("2B0000F") ||
                    formId.equals("231000F")) {
//            printBtn.setVisibility(GONE);
                if (clientDB.isOpen()) {
                    clientDB.close();
                }
                context.onBackPressed();
            }
            if (ptx.startsWith("STL")) {
//            printBtn.setVisibility(GONE);
                if (clientDB.isOpen()) {
                    clientDB.close();
                }
                context.onBackPressed();
            }
            if (ptx.startsWith("RP")) {
                if (clientDB.isOpen()) {
                    clientDB.close();
                }
                context.onBackPressed();
            }
//        Log.d(TAG, "PTEXT    : " + ptx);
//        Log.d(TAG, "Count PB : " + String.valueOf(countPrintButton));
//        Log.d(TAG, "PB Label : " + printConfirm[countPrintButton]);
            if (countPrintButton > 2) {
//            printBtn.setVisibility(GONE);
                if (clientDB != null) {
                    if (clientDB.isOpen()) {
                        clientDB.close();
                    }
                }
                if (context instanceof ActivityList) {
                    context.onBackPressed();
                }
            }
        } catch (Exception ex) {
            Log.e("TX", "DB error");
            ex.printStackTrace();
        }
    }

    public void doNotPrint() {
        String ptx = "";
        if (comp.has("print_text")) {
            try {
                ptx = comp.getString("print_text");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        countPrintButton++;
        if (!(formId.equals("71000FF") || formId.equals("721000F") || formId.equals("731000F") ||
                formId.equals("521000F") || ptx.startsWith("STL") || ptx.startsWith("RP"))) {
            confirmationText.setText(printConfirm[countPrintButton]);
        }
        if (formId.equals("71000FF") || formId.equals("721000F") || formId.equals("731000F")) {
            confirmationText.setText(printConfirmTbank[countPrintButton]);
        }
        if (formId.equals("521000F")) {
            context.onBackPressed();
//            printBtn.setVisibility(GONE);
        }
        if (ptx.startsWith("STL")) {
//            printBtn.setVisibility(GONE);
            context.onBackPressed();
        }
        if (ptx.startsWith("RP")) {
            context.onBackPressed();
        }
//        Log.d(TAG, "Count PB : " + String.valueOf(countPrintButton));
        if (countPrintButton > 2) {
            context.onBackPressed();
//            printBtn.setVisibility(GONE);
        }
    }

    @Override
    public void onClick(View v) {
        // mis-clicking prevention, using threshold of 10000 ms
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        insideClick(v);
    }

    private void insideClick(View v) {
        int amtVal = hardValidation();
        if (amtVal == -1) {
            try {
                JSONObject rps = new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"Nominal kurang dari minimum\",\n" +
                        "\"value\":\"Nominal kurang dari minimum\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                        "\"type\":\"3\",\"title\":\"Gagal\"}}");

                processResponse(rps, "000000");
                return;
            } catch (Exception e) {

            }
        }
        if (amtVal == -2) {
            try {
                JSONObject rps = new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"Nominal melebihi limit maksimum\",\n" +
                        "\"value\":\"Nominal melebihi limit maksimum\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                        "\"type\":\"3\",\"title\":\"Gagal\"}}");

                processResponse(rps, "000000");
                return;
            } catch (Exception e) {

            }
        }
        if (amtVal == -2) {
            try {
                JSONObject rps = new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"Nominal harus berisi angka\",\n" +
                        "\"value\":\"Nominal harus berisi angka\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                        "\"type\":\"3\",\"title\":\"Gagal\"}}");

                processResponse(rps, "000000");
                return;
            } catch (Exception e) {

            }
        }
        if (iccPreProcessed) {
            showIccDialog(v);
        } else {
            String serviceId = v.getTag().toString();
            if (serviceId.equals("A56000") && externalCard) {
                v.setTag("A56100");
            }
            if (pinModuleCounter < 1) {
                try {
//          x      Toast.makeText(context, "Send", Toast.LENGTH_SHORT).show();
                    actionUrl((Button) v, v.getTag().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (formId.equals("5900000") || formId.equals("MA00072") || formId.equals("MA00070")) {
                showChangePinDialog(v);
            } else {
                for (int w = 0; w < pinModuleCounter; w++) {
//                showPinDialog(v);
                    showChangePinDialog(v);
                    Log.e("PINPAD", "Create Dialog" + String.valueOf(w));
                }
            }
        }
    }

    private void open() {
        if (isOpened) {
            Log.e(TAG, "PINPad is opened");
        } else {
            try {
                int result = PinPadInterface.open();
                if (result < 0) {
                    Log.e(TAG, "open() error! Error code = " + result);
                } else {
                    isOpened = true;
                    Log.e(TAG, "open() success!");
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private void close() {
        if (isOpened) {
            try {
                int result = PinPadInterface.close();
                if (result < 0) {
                    Log.e(TAG, "close() error! Error code = " + result);
                } else {
                    isOpened = false;
                    Log.e(TAG, "close() success!");
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "PINPad is not opened");
        }
    }

    @Override
    public void onInputCompleted(View v, String result, String additional, NsiccsData cardData) {
        boolean usingPopup = false;

//        if (insertICC != null && insertICC.removeCardFirst) {
//            focusHasSets = true;
//
//            insertICC.removeCardFirst = false;
//            cancelFocus();
//            alert.dismiss();
//            AlertDialog alertDialog = new AlertDialog.Builder(context).create();
//            alertDialog.setTitle("EDC Bank BJB");
//
//            alertDialog.setMessage("Silahkan untuk melepas kartu terlebih dahulu");
//
//            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
//                    new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss();
//                            context.onBackPressed();
//                            context.finish();
//                        }
//                    });
//            alertDialog.show();
//        }

        if (insertICC != null && insertICC.isByPass) {

            insertICC.isByPass = false;
            alert.dismiss();
        } else {
            if (additional == null) {
                try {
                    if (insertICC != null) {
                        if (insertICC.isOpen()) {
                            insertICC.closeDriver();
                        }
                    }
                    if (comp.has("print")) {
                        if (!comp.get("print").equals(null)) {
                            preparePrint();
                        }
                    }
                } catch (Exception e) {
                    //failed to close, maybe already closed or not open yet
                }
            } else {
                if (additional.startsWith("reversal")) {
                    usingPopup = true;
                    try {
                        if (insertICC != null) {
                            if (insertICC.isOpen()) {
                                insertICC.closeDriver();
                            }
                        }
                    } catch (Exception e) {
                        //failed to close, maybe already closed or not open yet
                    }
                    // try sending reversal
                    if (alert.isShowing()) {
                        alert.dismiss();
                    }
                    updReversedSukses(lastan, lastan);
                    sendReversalAdvice();
                    return;

                } else if (additional.startsWith("fallback")) {
//                    try {
//                        if (insertICC != null) {
//                            if (insertICC.isOpen()) {
//                                insertICC.closeDriver();
//                            }
//                        }
//                    } catch (Exception e) {
//                        //failed to close, maybe already closed or not open yet
//                    }
                    refreshICCProcessDialog("Chip tidak terdeteksi, menyiapkan fallback", true);
                    alert.show();
                    iccPreProcessed = false;
//                    insertICC = null;
                    if (formId.equals("MB82510")) {
                        prepareSaleFallback();
                    } else if (formId.equals("MB82560")) {
                        prepareSaleSeladaFallback();
                    } else if (formId.equals("S000008") || formId.equals("S000011")) {
                        prepareSamsatFallback();
                    } else if (formId.equals("S0000C4")) {
                        prepareSamsatBulkCashFallback();
                    } else if (formId.equals("POC0030")) {
                        preparePbbFallback();
                        TAG_CARD = "NON-PETUGAS";
                    } else if (formId.equals("POC0C30")) {
                        preparePbbFallback();
                        TAG_CARD = "PETUGAS";
                    } else if (formId.equals("S000017")) {
                        prepareMPNFallback();
                        TAG_CARD = "NON-PETUGAS";
                    } else if (formId.equals("S000020")) {
                        prepareMPNFallback();
                        TAG_CARD = "PETUGAS";
                    } else if (formId.equals("S000010") || formId.equals("S000510")) {
                        prepareSamsatBulkFallback();
                    } else if (formId.equals("S0000C8")) {
                        prepareSamsatCashFallback();
                    } else if (formId.equals("MA00065")) {
                        prepareInfoSaldoFallback();
                    } else if (formId.equals("MA00015")) {
                        prepareTarikTunaiFallback();
                    } else if (formId.equals("MA00040")) {
                        prepareSetorTunaiFallback();
                    } else if (formId.equals("MA00035")){
                        prepareOverbookingFallback();
                    } else if (formId.equals("MA00025")) {
                        prepareTransferAntarBankFallback();
                    } else if (formId.equals("MA00085")) {
                        prepareBukaRekeningFallback();
                    } else if (formId.equals("MA00095")) {
                        prepareBatalRekeningFallback();
                    } else if (formId.equals("MA00055")) {
                        prepareMiniStatementFallback();
                    } else if (formId.equals("MA00070")) {
                        prepareGantiPinFallback();
                    } else if (formId.equals("EPG0300")) {
                        prepareEdupayFallback();
                    } else if (formId.equals("EPG0310")) {
                        prepareEsamsatFallback();
                    } else if (formId.equals("EPG0410")) {
                        preparePdamMitraFallback();
                    } else if (formId.equals("EPG0510")) {
                        prepareBphtbFallback();
                    } else if (formId.equals("EPG0610")) {
                        preparePjdlFallback();
                    } else if (formId.equals("EPG0710")) {
                        prepareWebRegFallback();
                    } else if (formId.equals("EPG0810")) {
                        prepareFallback("EPG812");
                    } else if (formId.equals("EPG0820")) {
                        prepareFallback("EPG822");
                    } else if (formId.equals("EPG0910")) {
                        prepareFallback("EPG912");
                    }
                    return;
                } else if (additional.startsWith("blocked")) {
                    try {
                        if (insertICC != null) {
                            if (insertICC.isOpen()) {
                                insertICC.closeDriver();
                            }
                        }
                    } catch (Exception e) {
                        //failed to close, maybe already closed or not open yet
                    }
                    refreshICCProcessDialog("Aplikasi chip tidak terdeteksi atau kartu telah diblok", true);
                    alert.show();
                    iccPreProcessed = false;
//                    insertICC = null;
                    preparePopupGagal("Aplikasi chip tidak terdeteksi atau kartu telah diblok");
                    return;
                }
            }


            if (result == null || result.equals("")) {
//          Log.d("SWIPE", "BACK PRESSED");
                if (alert.isShowing()) {
                    alert.dismiss();
                }
                if (!usingPopup) {
                    context.onBackPressed();
                }
            } else {
                //tandaaan
                if (parent.modulStage == CommonConfig.ICC_PROCESS_STAGE_INIT
                        || parent.modulStage == CommonConfig.ICC_PROCESS_STAGE_TX) {
                    panHolder = panFromTrack2(result);
                    Track2BINChecker tbc = new Track2BINChecker(this.context, panHolder);
                    this.externalCard = tbc.isExternalCard();
                    //generate Invoice
                    SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
                    String invoRef = sdf.format(new Date()) + String.format("%04d", getSeqNum());

                    //add service_code
                    int pos = result.indexOf('|');
                    List<String> ajBinList = new ArrayList<>();
                    ajBinList.add("532595");
                    ajBinList.add("603605");
                    ajBinList.add("502287");
                    ajBinList.add("459870");
                    ajBinList.add("470585");
                    if (panHolder.startsWith("628173")) { //internal bin
                        if (pos > 0) {
                            result = result.substring(0, pos) + "|01001" + result.substring(pos);
                            result += "|" + invoRef; //inject invoice
                        } else {
                            result += "|01001";
                        }
                    } else if (ajBinList.contains(panHolder.substring(0, 6))) {
                        // Artajasa
                        if (pos > 0) {
                            result = result.substring(0, pos) + "|01002" + result.substring(pos);
                            result += "|" + invoRef; //inject invoice
                        } else {
                            result += "|01002";
                        }
                    } else {
                        // Rintis
                        if (pos > 0) {
                            result = result.substring(0, pos) + "|01003" + result.substring(pos);
                            result += "|" + invoRef; //inject invoice
                        } else {
                            result += "|01003";
                        }
                    }
                    if (additional.startsWith("TCAID")) {
                        tcaid = additional.substring(5);
                    }
                }
//            if (result.length()>255) {
//                result = result.substring(0,250) + "|" + result.substring(250);
//            }
                insertICC.setText(result);
                parent.cardData = cardData;
                if (alert.isShowing()) {
                    alert.dismiss();
                }
                iccPreProcessed = false;
                if (iccIsTriggerd) {
                    insideClick(formTrigger);
                }
//            focusHandler.postDelayed(delayFocus, 400);
            }
        }

    }

    public void sendReversalAdvice() {
        dettachPrint();
        dialog = ProgressDialog.show(context, "Transaksi Ditolak oleh Kartu", "Mengirim Reversal", true);
        final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        SharedPreferences preferences = context.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        final JSONObject msg = new JSONObject();
        try {
            final String msgId = telephonyManager.getDeviceId() + sdf.format(new Date());
            msg.put("msg_id", msgId);
            msg.put("msg_ui", telephonyManager.getDeviceId());

            if (formId.equals("MA00040") || formId.equals("MA00041") || formId.equals("MA00041F")) {
                // Reversal Setor Tunai
                msg.put("msg_si", "RA0040");
            } else if (formId.equals("MA00010") || formId.equals("MA0010F")) {
                // Reversal Tarik Tunai
                msg.put("msg_si", "RA0010");
            } else if (formId.equals("MA00030") || formId.equals("MA00031") || formId.equals("MA0031F")) {
                // Reversal Overbooking
                msg.put("msg_si", "RA0030");
            } else if (formId.equals("MA00020") || formId.equals("MA00021") || formId.equals("MA0002F")) {
                // Reversal Transfer Antar Bank
                msg.put("msg_si", "RA0020");
            } else if (formId.equals("MB82560") || formId.equals("MB82561") || formId.equals("MB82562")) {
                // Reversal Sale Selada
                msg.put("msg_si", "R82561");
            } else {
                // Reversal Sale
                msg.put("msg_si", "R82510");
            }

            msg.put("msg_dt", lastan);

            final JSONObject msgRoot = new JSONObject();
            msgRoot.put("msg", msg);
            String hostname = preferences.getString("hostname", CommonConfig.HTTP_REST_URL);
            String postpath = preferences.getString("postpath", CommonConfig.POST_PATH);
            String httpPost = CommonConfig.HTTP_PROTOCOL + "://" + hostname + "/" + postpath;

            StringRequest jor = new StringRequest(Request.Method.POST,
                    httpPost,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                Log.d("TERIMA", response);
                                JSONObject rps = new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"Transaksi ditolak oleh kartu\",\n" +
                                        "\"value\":\"Transaksi ditolak oleh kartu\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                                        "\"type\":\"3\",\"title\":\"Gagal\"}}");

                                processResponse(rps, msgId);
                                dialog.dismiss();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                        try {
                            Toast.makeText(context, "Request Timeout",
                                    Toast.LENGTH_LONG).show();
                            JSONObject rps = new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[" +
                                    "{\"visible\":true,\"comp_values\":{\"comp_value\":[" +
                                    "{\"print\":\"Transaksi ditolak oleh kartu\nTidak dapat mengirim Reversal\",\n" +
                                    "\"value\":\"Transaksi ditolak oleh kartu\nTidak dapat mengirim Reversal\"}]" +
                                    "},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                                    "\"type\":\"3\",\"title\":\"Gagal\"}}");

                            processResponse(rps, msgId);
                            dialog.dismiss();
                        } catch (Exception e) {

                        }
                    }
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "text/plain; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {

                        return msgRoot == null ? null : msgRoot.toString().getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        Log.e("VOLLEY", "Unsupported Encoding while trying to get the bytes of " + msgRoot.toString() + "utf-8");
                        return null;
                    }
                }


            };
            jor.setRetryPolicy(new DefaultRetryPolicy(10000,
                    0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            RequestQueue revrq = Volley.newRequestQueue(context);
            revrq.add(jor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //ADD REVERSAL FOR SALE SELADA
    public void sendReversalAdviceSale(String stan) {
        dettachPrint();
        dialog = ProgressDialog.show(context, "Reversal", "Sedang Mengirim Reversal", true);
        final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        SharedPreferences preferences = context.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        final JSONObject msg = new JSONObject();
        try {
            final String msgId = telephonyManager.getDeviceId() + sdf.format(new Date());
            msg.put("msg_id", msgId);
            msg.put("msg_ui", telephonyManager.getDeviceId());
            // Reversal Sale
            msg.put("msg_si", "R82561");
            msg.put("msg_dt", stan);

            final JSONObject msgRoot = new JSONObject();
            msgRoot.put("msg", msg);
            String hostname = preferences.getString("hostname", CommonConfig.HTTP_REST_URL);
            String postpath = preferences.getString("postpath", CommonConfig.POST_PATH);
            String httpPost = CommonConfig.HTTP_PROTOCOL + "://" + hostname + "/" + postpath;

            StringRequest jor = new StringRequest(Request.Method.POST,
                    httpPost,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                Log.d("TERIMA", response);
                                JSONObject rps = new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"Reversal Berhasil Dikirim\",\n" +
                                        "\"value\":\"Reversal Berhasil Dikirim\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                                        "\"type\":\"3\",\"title\":\"Reversal\"}}");

                                processResponse(rps, msgId);
                                dialog.dismiss();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                        try {
                            Toast.makeText(context, "Request Timeout",
                                    Toast.LENGTH_LONG).show();
                            JSONObject rps = new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[" +
                                    "{\"visible\":true,\"comp_values\":{\"comp_value\":[" +
                                    "{\"print\":\"Transaksi ditolak oleh kartu\nTidak dapat mengirim Reversal\",\n" +
                                    "\"value\":\"Transaksi ditolak oleh kartu\nTidak dapat mengirim Reversal\"}]" +
                                    "},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                                    "\"type\":\"3\",\"title\":\"Gagal\"}}");

                            processResponse(rps, msgId);
                            dialog.dismiss();
                        } catch (Exception e) {

                        }
                    }
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "text/plain; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {

                        return msgRoot == null ? null : msgRoot.toString().getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        Log.e("VOLLEY", "Unsupported Encoding while trying to get the bytes of " + msgRoot.toString() + "utf-8");
                        return null;
                    }
                }


            };
            jor.setRetryPolicy(new DefaultRetryPolicy(10000,
                    0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            RequestQueue revrq = Volley.newRequestQueue(context);
            revrq.add(jor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void requestReprintReport(String date, final String tid, String pid, String iccid, String type) {
        dettachPrint();
        dialog = ProgressDialog.show(context, "Loading", "Sedang Mengirim Permintaan", true);
        final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        SharedPreferences preferences = context.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        final JSONObject msg = new JSONObject();
        try {
            msg.put("msg_id", stan);
            msg.put("msg_pd", pid);
            msg.put("msg_ui", telephonyManager.getDeviceId());
            try {
                msg.put("msg_td", compress(tid));
                Log.d("td", compress(tid));
            } catch (Exception e) {
                msg.put("msg_td", tid);
                e.printStackTrace();
            }
            msg.put("msg_sn", iccid);
            msg.put("msg_dte", date);

            final JSONObject msgRoot = new JSONObject();
            msgRoot.put("msg", msg);
            String hostname = preferences.getString("hostname", CommonConfig.HTTP_REST_URL);
            String postpath = "";

            switch (type) {
                case "summary":
                    postpath = "report";
                    break;
                case "detail":
                    postpath = "reportDetail";
                    break;
                case "reprint":
                    postpath = "print";
                    break;
            }

            String httpPost = CommonConfig.HTTP_PROTOCOL + "://" + hostname + "/" + postpath;

            StringRequest jor = new StringRequest(Request.Method.POST,
                    httpPost,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                Log.d("TERIMA", response);
//                                String resp = decompress(response);
//                                JSONObject jsonResp = new JSONObject(resp);
//                                JSONObject newRoot = new JSONObject();
//                                String screen = (String) jsonResp.get("screen");
//                                newRoot.put("screen", screen);
                                JSONObject jsonResp = new JSONObject(response);
                                if (jsonResp.has("encrypted")) {
                                    String isEnc = jsonResp.getString("encrypted");
                                    if (isEnc.equals("t")) {
                                        jsonResp = decResponse(jsonResp);
                                    }
                                }
                                dialog.dismiss();
                                processResponse(jsonResp, tid);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                        try {
                            Toast.makeText(context, "Request Timeout",
                                    Toast.LENGTH_LONG).show();
                            JSONObject rps = new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[" +
                                    "{\"visible\":true,\"comp_values\":{\"comp_value\":[" +
                                    "{\"print\":\"Tidak Dapat Mengirim Permintaan\",\n" +
                                    "\"value\":\"Tidak Dapat Mengirim Permintaan\"}]" +
                                    "},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                                    "\"type\":\"3\",\"title\":\"Gagal\"}}");

                            Log.d("ERROR", error.getMessage());
                            processResponse(rps, tid);
                            dialog.dismiss();
                        } catch (Exception e) {

                        }
                    }
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "text/plain; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {

                        return msgRoot == null ? null : msgRoot.toString().getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        Log.e("VOLLEY", "Unsupported Encoding while trying to get the bytes of " + msgRoot.toString() + "utf-8");
                        return null;
                    }
                }


            };
            jor.setRetryPolicy(new DefaultRetryPolicy(10000,
                    0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            RequestQueue revrq = Volley.newRequestQueue(context);
            revrq.add(jor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void prepareSaleFallback() {
        if (alert.isShowing()) {
            alert.dismiss();
        }
        String[] amts = getAmountFromScreen();
        String lastAmount = String.format("%.0f", Double.parseDouble(amts[0]) / 100);
        try {
            JSONObject fallbackScreen = new JSONObject("{\"screen\":" +

                    "{\"fallback\":1,\"action_url\":\"E82511\",\"ver\":\"1\",\"print\":null," +
                    "\"comps\":{\"comp\":[" +

                    "{\"visible\":false,\"comp_lbl\":\"Magnetic Swipe\",\"comp_type\":\"8\",\"comp_id\":\"I0003\",\"seq\":0}," +
                    "{\"visible\":true,\"comp_lbl\":\"PIN\",\"comp_type\":\"3\",\"comp_id\":\"I0001\",\"comp_opt\":\"102006006\",\"seq\":2}," +

                    "{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"" + lastAmount +
                    "\",\"value\":\"" + lastAmount +
                    "\"}]},\"comp_lbl\":\"Masukkan Nominal Sale\",\"comp_type\":\"2\"," +
                    "\"comp_id\":\"E8251\",\"comp_opt\":\"102012012\",\"seq\":1}," +

                    "{\"visible\":true,\"comp_lbl\":\"Proses\",\"comp_type\":\"7\",\"comp_id\":\"G0001\",\"seq\":3}]}," +
                    "\"static_menu\":[\"\"],\"print_text\":\"IPOP\",\"id\":\"SR10004\",\"type\":\"1\",\"title\":\"Setor Tunai Fallback\"}}");
//            processResponse(fallbackScreen, "001");
            comp = fallbackScreen.getJSONObject("screen");
            pinpadTextList = new ArrayList();
            pinModuleCounter = 0;
            init();
        } catch (Exception e) {

        }
    }

    public void prepareSaleSeladaFallback() {
        if (alert.isShowing()) {
            alert.dismiss();
        }
        String[] amts = getAmountFromScreen();
        String lastAmount = String.format("%.0f", Double.parseDouble(amts[0]) / 100);
        try {
            JSONObject fallbackScreen = new JSONObject("{\"screen\":" +

                    "{\"fallback\":1,\"action_url\":\"E82561\",\"ver\":\"1\",\"print\":null," +
                    "\"comps\":{\"comp\":[" +

                    "{\"visible\":false,\"comp_lbl\":\"Magnetic Swipe\",\"comp_type\":\"8\",\"comp_id\":\"I0003\",\"seq\":0}," +
                    "{\"visible\":true,\"comp_lbl\":\"PIN\",\"comp_type\":\"3\",\"comp_id\":\"I0001\",\"comp_opt\":\"102006006\",\"seq\":4}," +

                    "{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"" + amount +
                    "\",\"value\":\"" + amount +
                    "\"}]},\"comp_lbl\":\"Masukkan Nominal Sale\",\"comp_type\":\"2\"," +
                    "\"comp_id\":\"E8251\",\"comp_opt\":\"102012012\",\"seq\":1}," +

                    "{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"" + mobileNumber +
                    "\",\"value\":\"" + mobileNumber +
                    "\"}]},\"comp_lbl\":\"Masukkan Kode Billing\",\"comp_type\":\"2\"," +
                    "\"comp_id\":\"M1001\",\"comp_opt\":\"102012012\",\"seq\":2}," +

                    "{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"" + margin +
                    "\",\"value\":\"" + margin +
                    "\"}]},\"comp_lbl\":\"Biaya Margin\",\"comp_type\":\"2\"," +
                    "\"comp_id\":\"E8252\",\"comp_opt\":\"102012012\",\"seq\":3}," +

                    "{\"visible\":true,\"comp_lbl\":\"Proses\",\"comp_type\":\"7\",\"comp_id\":\"G0001\",\"seq\":5}]}," +
                    "\"static_menu\":[\"\"],\"print_text\":\"IPOP\",\"id\":\"SR10004\",\"type\":\"1\",\"title\":\"Sale Selada Fallback\"}}");
//            processResponse(fallbackScreen, "001");
            comp = fallbackScreen.getJSONObject("screen");
            pinpadTextList = new ArrayList();
            pinModuleCounter = 0;
            init();
        } catch (Exception e) {

        }
    }

    public void prepareSamsatFallback() {
        if (alert.isShowing()) {
            alert.dismiss();
        }
        List<String> values = getValueFromScreen();
        Log.d("VALUES", values.get(1) + " - " + values.get(2) + " - " + values.get(3));
        try {
            JSONObject fallbackScreen = new JSONObject("{\"screen\":" +
                    "{\"fallback\":1,\"action_url\":\"M0001C\",\"ver\":\"1\",\"print\":null," +
                    "\"comps\":{\"comp\":[" +

                    "{\"visible\":false,\"comp_lbl\":\"Magnetic Swipe\",\"comp_type\":\"8\",\"comp_id\":\"I0003\",\"seq\":0}," +
                    "{\"visible\":true,\"comp_lbl\":\"PIN\",\"comp_type\":\"3\",\"comp_id\":\"I0001\",\"comp_opt\":\"102006006\",\"seq\":4}," +

                    "{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"" + values.get(1) +
                    "\",\"value\":\"" + values.get(1) +
                    "\"}]},\"comp_lbl\":\"Nomor Kendaraan\",\"comp_type\":\"2\"," +
                    "\"comp_id\":\"M2001\",\"comp_opt\":\"102012012\",\"seq\":1}," +

                    "{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"" + values.get(2) +
                    "\",\"value\":\"" + values.get(2) +
                    "\"}]},\"comp_lbl\":\"Total No. Polisi\",\"comp_type\":\"2\"," +
                    "\"comp_id\":\"M2002\",\"comp_opt\":\"102012012\",\"seq\":2}," +

                    "{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"" + values.get(3) +
                    "\",\"value\":\"" + values.get(3) +
                    "\"}]},\"comp_lbl\":\"Nominal Bayar\",\"comp_type\":\"2\"," +
                    "\"comp_id\":\"M2003\",\"comp_opt\":\"102012012\",\"seq\":3}," +

                    "{\"visible\":true,\"comp_lbl\":\"Proses\",\"comp_type\":\"7\",\"comp_id\":\"G0001\",\"seq\":5}]}," +
                    "\"static_menu\":[\"\"],\"print_text\":\"IPOP\",\"id\":\"SR10004\",\"type\":\"1\",\"title\":\"Payment Samsat Cash\"}}");
//            processResponse(fallbackScreen, "001");
            comp = fallbackScreen.getJSONObject("screen");
            pinpadTextList = new ArrayList();
            pinModuleCounter = 0;
            init();
        } catch (Exception e) {

        }
    }

    public void prepareSamsatCashFallback() {
        if (alert.isShowing()) {
            alert.dismiss();
        }
        List<String> values = getValueFromScreen();
        Log.d("VALUES", values.get(1) + " - " + values.get(2) + " - " + values.get(3));
        try {
            JSONObject fallbackScreen = new JSONObject("{\"screen\":" +
                    "{\"fallback\":1,\"action_url\":\"M0001A\",\"ver\":\"1\",\"print\":null," +
                    "\"comps\":{\"comp\":[" +

                    "{\"visible\":false,\"comp_lbl\":\"Magnetic Swipe\",\"comp_type\":\"8\",\"comp_id\":\"I0003\",\"seq\":0}," +
                    "{\"visible\":true,\"comp_lbl\":\"PIN\",\"comp_type\":\"3\",\"comp_id\":\"I0001\",\"comp_opt\":\"102006006\",\"seq\":4}," +

                    "{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"" + values.get(1) +
                    "\",\"value\":\"" + values.get(1) +
                    "\"}]},\"comp_lbl\":\"Nomor Kendaraan\",\"comp_type\":\"2\"," +
                    "\"comp_id\":\"M2001\",\"comp_opt\":\"102012012\",\"seq\":1}," +

                    "{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"" + values.get(2) +
                    "\",\"value\":\"" + values.get(2) +
                    "\"}]},\"comp_lbl\":\"Total No. Polisi\",\"comp_type\":\"2\"," +
                    "\"comp_id\":\"M2002\",\"comp_opt\":\"102012012\",\"seq\":2}," +

                    "{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"" + values.get(3) +
                    "\",\"value\":\"" + values.get(3) +
                    "\"}]},\"comp_lbl\":\"Nominal Bayar\",\"comp_type\":\"2\"," +
                    "\"comp_id\":\"M2003\",\"comp_opt\":\"102012012\",\"seq\":3}," +

                    "{\"visible\":true,\"comp_lbl\":\"Proses\",\"comp_type\":\"7\",\"comp_id\":\"G0001\",\"seq\":5}]}," +
                    "\"static_menu\":[\"\"],\"print_text\":\"IPOP\",\"id\":\"SR10004\",\"type\":\"1\",\"title\":\"Payment Samsat Cash\"}}");
//            processResponse(fallbackScreen, "001");
            comp = fallbackScreen.getJSONObject("screen");
            pinpadTextList = new ArrayList();
            pinModuleCounter = 0;
            init();
        } catch (Exception e) {

        }
    }

    public void prepareSamsatBulkFallback() {
        if (alert.isShowing()) {
            alert.dismiss();
        }
        List<String> values = getValueFromScreen();
        Log.d("VALUES", values.get(1) + " - " + values.get(2) + " - " + values.get(3));
        try {
            JSONObject fallbackScreen = new JSONObject("{\"screen\":" +
                    "{\"fallback\":1,\"action_url\":\"M0005B\",\"ver\":\"1\",\"print\":null," +
                    "\"comps\":{\"comp\":[" +

                    "{\"visible\":false,\"comp_lbl\":\"Magnetic Swipe\",\"comp_type\":\"8\",\"comp_id\":\"I0003\",\"seq\":0}," +
                    "{\"visible\":true,\"comp_lbl\":\"PIN\",\"comp_type\":\"3\",\"comp_id\":\"I0001\",\"comp_opt\":\"102006006\",\"seq\":4}," +

                    "{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"" + values.get(1) +
                    "\",\"value\":\"" + values.get(1) +
                    "\"}]},\"comp_lbl\":\"Nomor Kendaraan\",\"comp_type\":\"2\"," +
                    "\"comp_id\":\"M2001\",\"comp_opt\":\"102012012\",\"seq\":1}," +

                    "{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"" + values.get(2) +
                    "\",\"value\":\"" + values.get(2) +
                    "\"}]},\"comp_lbl\":\"Total No. Polisi\",\"comp_type\":\"2\"," +
                    "\"comp_id\":\"M2002\",\"comp_opt\":\"102012012\",\"seq\":2}," +

                    "{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"" + values.get(3) +
                    "\",\"value\":\"" + values.get(3) +
                    "\"}]},\"comp_lbl\":\"Nominal Bayar\",\"comp_type\":\"2\"," +
                    "\"comp_id\":\"M2003\",\"comp_opt\":\"102012012\",\"seq\":3}," +

                    "{\"visible\":true,\"comp_lbl\":\"Proses\",\"comp_type\":\"7\",\"comp_id\":\"G0001\",\"seq\":5}]}," +
                    "\"static_menu\":[\"\"],\"print_text\":\"IPOP\",\"id\":\"SR10004\",\"type\":\"1\",\"title\":\"Payment Samsat Bulk Cash\"}}");
//            processResponse(fallbackScreen, "001");
            comp = fallbackScreen.getJSONObject("screen");
            pinpadTextList = new ArrayList();
            pinModuleCounter = 0;
            init();
        } catch (Exception e) {

        }
    }

    public void prepareSamsatBulkCashFallback() {
        if (alert.isShowing()) {
            alert.dismiss();
        }
        List<String> values = getValueFromScreen();
        Log.d("VALUES", values.get(1) + " - " + values.get(2) + " - " + values.get(3));
        try {
            JSONObject fallbackScreen = new JSONObject("{\"screen\":" +
                    "{\"fallback\":1,\"action_url\":\"M0005A\",\"ver\":\"1\",\"print\":null," +
                    "\"comps\":{\"comp\":[" +

                    "{\"visible\":false,\"comp_lbl\":\"Magnetic Swipe\",\"comp_type\":\"8\",\"comp_id\":\"I0003\",\"seq\":0}," +
                    "{\"visible\":true,\"comp_lbl\":\"PIN\",\"comp_type\":\"3\",\"comp_id\":\"I0001\",\"comp_opt\":\"102006006\",\"seq\":4}," +

                    "{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"" + values.get(1) +
                    "\",\"value\":\"" + values.get(1) +
                    "\"}]},\"comp_lbl\":\"Nomor Kendaraan\",\"comp_type\":\"2\"," +
                    "\"comp_id\":\"M2001\",\"comp_opt\":\"102012012\",\"seq\":1}," +

                    "{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"" + values.get(2) +
                    "\",\"value\":\"" + values.get(2) +
                    "\"}]},\"comp_lbl\":\"Total No. Polisi\",\"comp_type\":\"2\"," +
                    "\"comp_id\":\"M2002\",\"comp_opt\":\"102012012\",\"seq\":2}," +

                    "{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"" + values.get(3) +
                    "\",\"value\":\"" + values.get(3) +
                    "\"}]},\"comp_lbl\":\"Nominal Bayar\",\"comp_type\":\"2\"," +
                    "\"comp_id\":\"M2003\",\"comp_opt\":\"102012012\",\"seq\":3}," +

                    "{\"visible\":true,\"comp_lbl\":\"Proses\",\"comp_type\":\"7\",\"comp_id\":\"G0001\",\"seq\":5}]}," +
                    "\"static_menu\":[\"\"],\"print_text\":\"IPOP\",\"id\":\"SR10004\",\"type\":\"1\",\"title\":\"Payment Samsat Bulk Cash\"}}");
//            processResponse(fallbackScreen, "001");
            comp = fallbackScreen.getJSONObject("screen");
            pinpadTextList = new ArrayList();
            pinModuleCounter = 0;
            init();
        } catch (Exception e) {

        }
    }

    public void preparePbbFallback() {
        if (alert.isShowing()) {
            alert.dismiss();
        }
        List<String> values = getValueFromScreen();
        Log.d("VALUES", values.get(1) + " - " + values.get(2) + " - " + values.get(3));
        try {
            JSONObject fallbackScreen = new JSONObject("{\"screen\":" +
                    "{\"fallback\":1,\"action_url\":\"P00034\",\"ver\":\"1\",\"print\":null," +
                    "\"comps\":{\"comp\":[" +

                    "{\"visible\":false,\"comp_lbl\":\"Magnetic Swipe\",\"comp_type\":\"8\",\"comp_id\":\"I0003\",\"seq\":0}," +
                    "{\"visible\":true,\"comp_lbl\":\"PIN\",\"comp_type\":\"3\",\"comp_id\":\"I0001\",\"comp_opt\":\"102006006\",\"seq\":4}," +

                    "{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"" + values.get(1) +
                    "\",\"value\":\"" + values.get(1) +
                    "\"}]},\"comp_lbl\":\"Nomor Objek Pajak\",\"comp_type\":\"2\"," +
                    "\"comp_id\":\"P0031\",\"comp_opt\":\"102012012\",\"seq\":1}," +

                    "{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"" + values.get(2) +
                    "\",\"value\":\"" + values.get(2) +
                    "\"}]},\"comp_lbl\":\"Tahun Pajak\",\"comp_type\":\"2\"," +
                    "\"comp_id\":\"P0032\",\"comp_opt\":\"102012012\",\"seq\":2}," +

                    "{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"" + values.get(3) +
                    "\",\"value\":\"" + values.get(3) +
                    "\"}]},\"comp_lbl\":\"Kab/Kota\",\"comp_type\":\"2\"," +
                    "\"comp_id\":\"P0047\",\"comp_opt\":\"102012012\",\"seq\":3}," +

                    "{\"visible\":true,\"comp_lbl\":\"Proses\",\"comp_type\":\"7\",\"comp_id\":\"G0001\",\"seq\":5}]}," +
                    "\"static_menu\":[\"\"],\"print_text\":\"IPOP\",\"id\":\"POC0034\",\"type\":\"1\",\"title\":\"Inquiry PBB\"}}");
//            processResponse(fallbackScreen, "001");
            comp = fallbackScreen.getJSONObject("screen");
            pinpadTextList = new ArrayList();
            pinModuleCounter = 0;
            init();
        } catch (Exception e) {

        }
    }

    public void prepareMPNFallback() {
        if (alert.isShowing()) {
            alert.dismiss();
        }
        List<String> values = getValueFromScreen();
        Log.d("VALUES", values.get(1));

        //MAPPING HANDLING MPN FALLBACK
        if (values.get(1).startsWith("0") || values.get(1).startsWith("1") || values.get(1).startsWith("2") || values.get(1).startsWith("3")) {
            //DJP
            try {
                JSONObject fallbackScreen = new JSONObject("{\"screen\":" +
                        "{\"fallback\":1,\"action_url\":\"M0006A\",\"ver\":\"1\",\"print\":null," +
                        "\"comps\":{\"comp\":[" +

                        "{\"visible\":false,\"comp_lbl\":\"Magnetic Swipe\",\"comp_type\":\"8\",\"comp_id\":\"I0003\",\"seq\":0}," +
                        "{\"visible\":true,\"comp_lbl\":\"PIN\",\"comp_type\":\"3\",\"comp_id\":\"I0001\",\"comp_opt\":\"102006006\",\"seq\":2}," +

                        "{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"" + values.get(1) +
                        "\",\"value\":\"" + values.get(1) +
                        "\"}]},\"comp_lbl\":\"Masukkan Kode Billing\",\"comp_type\":\"2\"," +
                        "\"comp_id\":\"M1001\",\"comp_opt\":\"102012012\",\"seq\":1}," +

                        "{\"visible\":true,\"comp_lbl\":\"Proses\",\"comp_type\":\"7\",\"comp_id\":\"G0001\",\"seq\":3}]}," +
                        "\"static_menu\":[\"\"],\"print_text\":\"IPOP\",\"id\":\"SR10015\",\"type\":\"1\",\"title\":\"Inquiry DJP\"}}");
//            processResponse(fallbackScreen, "001");
                comp = fallbackScreen.getJSONObject("screen");
                pinpadTextList = new ArrayList();
                pinModuleCounter = 0;
                init();
            } catch (Exception e) {

            }
        } else if (values.get(1).startsWith("4") || values.get(1).startsWith("5") || values.get(1).startsWith("6")) {
            //DJBC
            try {
                JSONObject fallbackScreen = new JSONObject("{\"screen\":" +
                        "{\"fallback\":1,\"action_url\":\"M0006C\",\"ver\":\"1\",\"print\":null," +
                        "\"comps\":{\"comp\":[" +

                        "{\"visible\":false,\"comp_lbl\":\"Magnetic Swipe\",\"comp_type\":\"8\",\"comp_id\":\"I0003\",\"seq\":0}," +
                        "{\"visible\":true,\"comp_lbl\":\"PIN\",\"comp_type\":\"3\",\"comp_id\":\"I0001\",\"comp_opt\":\"102006006\",\"seq\":2}," +

                        "{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"" + values.get(1) +
                        "\",\"value\":\"" + values.get(1) +
                        "\"}]},\"comp_lbl\":\"Masukkan Kode Billing\",\"comp_type\":\"2\"," +
                        "\"comp_id\":\"M1001\",\"comp_opt\":\"102012012\",\"seq\":1}," +

                        "{\"visible\":true,\"comp_lbl\":\"Proses\",\"comp_type\":\"7\",\"comp_id\":\"G0001\",\"seq\":3}]}," +
                        "\"static_menu\":[\"\"],\"print_text\":\"IPOP\",\"id\":\"POC0034\",\"type\":\"1\",\"title\":\"Inquiry DJBC\"}}");
//            processResponse(fallbackScreen, "001");
                comp = fallbackScreen.getJSONObject("screen");
                pinpadTextList = new ArrayList();
                pinModuleCounter = 0;
                init();
            } catch (Exception e) {

            }
        } else if (values.get(1).startsWith("7") || values.get(1).startsWith("8") || values.get(1).startsWith("9")) {
            //DJA
            try {
                JSONObject fallbackScreen = new JSONObject("{\"screen\":" +
                        "{\"fallback\":1,\"action_url\":\"M0006E\",\"ver\":\"1\",\"print\":null," +
                        "\"comps\":{\"comp\":[" +

                        "{\"visible\":false,\"comp_lbl\":\"Magnetic Swipe\",\"comp_type\":\"8\",\"comp_id\":\"I0003\",\"seq\":0}," +
                        "{\"visible\":true,\"comp_lbl\":\"PIN\",\"comp_type\":\"3\",\"comp_id\":\"I0001\",\"comp_opt\":\"102006006\",\"seq\":2}," +

                        "{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"" + values.get(1) +
                        "\",\"value\":\"" + values.get(1) +
                        "\"}]},\"comp_lbl\":\"Masukkan Kode Billing\",\"comp_type\":\"2\"," +
                        "\"comp_id\":\"M1001\",\"comp_opt\":\"102012012\",\"seq\":1}," +

                        "{\"visible\":true,\"comp_lbl\":\"Proses\",\"comp_type\":\"7\",\"comp_id\":\"G0001\",\"seq\":3}]}," +
                        "\"static_menu\":[\"\"],\"print_text\":\"IPOP\",\"id\":\"POC0034\",\"type\":\"1\",\"title\":\"Inquiry DJA\"}}");
//            processResponse(fallbackScreen, "001");
                comp = fallbackScreen.getJSONObject("screen");
                pinpadTextList = new ArrayList();
                pinModuleCounter = 0;
                init();
            } catch (Exception e) {

            }
        }
    }

    public void prepareInfoSaldoFallback() {
        if (alert.isShowing()) {
            alert.dismiss();
        }
        List<String> values = getValueFromScreen();
        try {
            JSONObject fallbackScreen = new JSONObject("{\"screen\":" +
                    "{\"fallback\":1,\"action_url\":\"MA0062\",\"ver\":\"1\",\"print\":null," +
                    "\"comps\":{\"comp\":[" +

                    "{\"visible\":false,\"comp_lbl\":\"Magnetic Swipe\",\"comp_type\":\"8\",\"comp_id\":\"I0003\",\"seq\":0}," +
                    "{\"visible\":true,\"comp_lbl\":\"PIN\",\"comp_type\":\"3\",\"comp_id\":\"I0001\",\"comp_opt\":\"102006006\",\"seq\":1}," +

                    "{\"visible\":true,\"comp_lbl\":\"Proses\",\"comp_type\":\"7\",\"comp_id\":\"G0001\",\"seq\":2}]}," +
                    "\"static_menu\":[\"\"],\"print_text\":\"IPOP\",\"id\":\"SR10004\",\"type\":\"1\",\"title\":\"Info Saldo Fallback\"}}");
//            processResponse(fallbackScreen, "001");
            comp = fallbackScreen.getJSONObject("screen");
            pinpadTextList = new ArrayList();
            pinModuleCounter = 0;
            init();
        } catch (Exception e) {

        }
    }

    public void prepareTarikTunaiFallback(){
        if (alert.isShowing()) {
            alert.dismiss();
        }
        List<String> values = getValueFromScreen();
        try {
            JSONObject fallbackScreen = new JSONObject("{\"screen\":" +
                    "{\"fallback\":1,\"action_url\":\"MA0011\",\"ver\":\"1\",\"print\":null," +
                    "\"comps\":{\"comp\":[" +

                    "{\"visible\":false,\"comp_lbl\":\"Magnetic Swipe\",\"comp_type\":\"8\",\"comp_id\":\"I0003\",\"seq\":0}," +
                    "{\"visible\":true,\"comp_lbl\":\"PIN\",\"comp_type\":\"3\",\"comp_id\":\"I0001\",\"comp_opt\":\"102006006\",\"seq\":1}," +

                    "{\"visible\":true,\"comp_lbl\":\"Proses\",\"comp_type\":\"7\",\"comp_id\":\"G0001\",\"seq\":2}]}," +
                    "\"static_menu\":[\"\"],\"print_text\":\"IPOP\",\"id\":\"SR10004\",\"type\":\"1\",\"title\":\"Tarik Tunai Fallback\"}}");
//            processResponse(fallbackScreen, "001");
            comp = fallbackScreen.getJSONObject("screen");
            pinpadTextList = new ArrayList();
            pinModuleCounter = 0;
            init();
        } catch (Exception e) {

        }
    }

    public void prepareSetorTunaiFallback(){
        if (alert.isShowing()) {
            alert.dismiss();
        }
        List<String> values = getValueFromScreen();
        Log.d("VALUES", values.get(1) + " - " + values.get(2));
        try {
            JSONObject fallbackScreen = new JSONObject("{\"screen\":" +
                    "{\"fallback\":1,\"action_url\":\"MA0042\",\"ver\":\"1\",\"print\":null," +
                    "\"comps\":{\"comp\":[" +

                    "{\"visible\":false,\"comp_lbl\":\"Magnetic Swipe\",\"comp_type\":\"8\",\"comp_id\":\"I0003\",\"seq\":0}," +
                    "{\"visible\":true,\"comp_lbl\":\"PIN\",\"comp_type\":\"3\",\"comp_id\":\"I0001\",\"comp_opt\":\"102006006\",\"seq\":3}," +

                    "{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"" + values.get(1) +
                    "\",\"value\":\"" + values.get(1) +
                    "\"}]},\"comp_lbl\":\"Nomor Rekening\",\"comp_type\":\"2\"," +
                    "\"comp_id\":\"MA042\",\"comp_opt\":\"102012012\",\"seq\":1}," +

                    "{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"" + values.get(2) +
                    "\",\"value\":\"" + values.get(2) +
                    "\"}]},\"comp_lbl\":\"Nominal Setor\",\"comp_type\":\"2\"," +
                    "\"comp_id\":\"MA043\",\"comp_opt\":\"102012012\",\"seq\":2}," +

                    "{\"visible\":true,\"comp_lbl\":\"Proses\",\"comp_type\":\"7\",\"comp_id\":\"G0001\",\"seq\":4}]}," +
                    "\"static_menu\":[\"\"],\"print_text\":\"IPOP\",\"id\":\"SR10004\",\"type\":\"1\",\"title\":\"Setor Tunai Fallback\"}}");
//            processResponse(fallbackScreen, "001");
            comp = fallbackScreen.getJSONObject("screen");
            pinpadTextList = new ArrayList();
            pinModuleCounter = 0;
            init();
        } catch (Exception e) {

        }
    }

    public void prepareOverbookingFallback(){
        if (alert.isShowing()) {
            alert.dismiss();
        }
        List<String> values = getValueFromScreen();
        try {
            JSONObject fallbackScreen = new JSONObject("{\"screen\":" +
                    "{\"fallback\":1,\"action_url\":\"MAG031\",\"ver\":\"1\",\"print\":null," +
                    "\"comps\":{\"comp\":[" +

                    "{\"visible\":false,\"comp_lbl\":\"Magnetic Swipe\",\"comp_type\":\"8\",\"comp_id\":\"I0003\",\"seq\":0}," +
                    "{\"visible\":true,\"comp_lbl\":\"PIN\",\"comp_type\":\"3\",\"comp_id\":\"I0001\",\"comp_opt\":\"102006006\",\"seq\":1}," +

                    "{\"visible\":true,\"comp_lbl\":\"Proses\",\"comp_type\":\"7\",\"comp_id\":\"G0001\",\"seq\":2}]}," +
                    "\"static_menu\":[\"\"],\"print_text\":\"IPOP\",\"id\":\"SR10004\",\"type\":\"1\",\"title\":\"Pemindahbukuan Fallback\"}}");
//            processResponse(fallbackScreen, "001");
            comp = fallbackScreen.getJSONObject("screen");
            pinpadTextList = new ArrayList();
            pinModuleCounter = 0;
            init();
        } catch (Exception e) {

        }
    }

    public void prepareTransferAntarBankFallback(){
        if (alert.isShowing()) {
            alert.dismiss();
        }
        List<String> values = getValueFromScreen();
        try {
            JSONObject fallbackScreen = new JSONObject("{\"screen\":" +
                    "{\"fallback\":1,\"action_url\":\"MAG021\",\"ver\":\"1\",\"print\":null," +
                    "\"comps\":{\"comp\":[" +

                    "{\"visible\":false,\"comp_lbl\":\"Magnetic Swipe\",\"comp_type\":\"8\",\"comp_id\":\"I0003\",\"seq\":0}," +
                    "{\"visible\":true,\"comp_lbl\":\"PIN\",\"comp_type\":\"3\",\"comp_id\":\"I0001\",\"comp_opt\":\"102006006\",\"seq\":1}," +

                    "{\"visible\":true,\"comp_lbl\":\"Proses\",\"comp_type\":\"7\",\"comp_id\":\"G0001\",\"seq\":2}]}," +
                    "\"static_menu\":[\"\"],\"print_text\":\"IPOP\",\"id\":\"SR10004\",\"type\":\"1\",\"title\":\"Transfer Antar Bank Fallback\"}}");
//            processResponse(fallbackScreen, "001");
            comp = fallbackScreen.getJSONObject("screen");
            pinpadTextList = new ArrayList();
            pinModuleCounter = 0;
            init();
        } catch (Exception e) {

        }
    }

    public void prepareBukaRekeningFallback(){
        if (alert.isShowing()) {
            alert.dismiss();
        }
        List<String> values = getValueFromScreen();
        try {
            JSONObject fallbackScreen = new JSONObject("{\"screen\":" +
                    "{\"fallback\":1,\"action_url\":\"MAG081\",\"ver\":\"1\",\"print\":null," +
                    "\"comps\":{\"comp\":[" +

                    "{\"visible\":false,\"comp_lbl\":\"Magnetic Swipe\",\"comp_type\":\"8\",\"comp_id\":\"I0003\",\"seq\":0}," +
                    "{\"visible\":true,\"comp_lbl\":\"PIN\",\"comp_type\":\"3\",\"comp_id\":\"I0001\",\"comp_opt\":\"102006006\",\"seq\":1}," +

                    "{\"visible\":true,\"comp_lbl\":\"Proses\",\"comp_type\":\"7\",\"comp_id\":\"G0001\",\"seq\":2}]}," +
                    "\"static_menu\":[\"\"],\"print_text\":\"IPOP\",\"id\":\"SR10004\",\"type\":\"1\",\"title\":\"Buka Rekening Fallback\"}}");
//            processResponse(fallbackScreen, "001");
            comp = fallbackScreen.getJSONObject("screen");
            pinpadTextList = new ArrayList();
            pinModuleCounter = 0;
            init();
        } catch (Exception e) {

        }
    }

    public void prepareBatalRekeningFallback() {
        if (alert.isShowing()) {
            alert.dismiss();
        }
        List<String> values = getValueFromScreen();
        try {
            JSONObject fallbackScreen = new JSONObject("{\"screen\":" +
                    "{\"fallback\":1,\"action_url\":\"MAG091\",\"ver\":\"1\",\"print\":null," +
                    "\"comps\":{\"comp\":[" +

                    "{\"visible\":false,\"comp_lbl\":\"Magnetic Swipe\",\"comp_type\":\"8\",\"comp_id\":\"I0003\",\"seq\":0}," +
                    "{\"visible\":true,\"comp_lbl\":\"PIN\",\"comp_type\":\"3\",\"comp_id\":\"I0001\",\"comp_opt\":\"102006006\",\"seq\":1}," +

                    "{\"visible\":true,\"comp_lbl\":\"Proses\",\"comp_type\":\"7\",\"comp_id\":\"G0001\",\"seq\":2}]}," +
                    "\"static_menu\":[\"\"],\"print_text\":\"IPOP\",\"id\":\"SR10004\",\"type\":\"1\",\"title\":\"Batal Rekening Fallback\"}}");
//            processResponse(fallbackScreen, "001");
            comp = fallbackScreen.getJSONObject("screen");
            pinpadTextList = new ArrayList();
            pinModuleCounter = 0;
            init();
        } catch (Exception e) {

        }
    }

    public void prepareMiniStatementFallback() {
        if (alert.isShowing()) {
            alert.dismiss();
        }
        List<String> values = getValueFromScreen();
        try {
            JSONObject fallbackScreen = new JSONObject("{\"screen\":" +
                    "{\"fallback\":1,\"action_url\":\"MAG050\",\"ver\":\"1\",\"print\":null," +
                    "\"comps\":{\"comp\":[" +

                    "{\"visible\":false,\"comp_lbl\":\"Magnetic Swipe\",\"comp_type\":\"8\",\"comp_id\":\"I0003\",\"seq\":0}," +
                    "{\"visible\":true,\"comp_lbl\":\"PIN\",\"comp_type\":\"3\",\"comp_id\":\"I0001\",\"comp_opt\":\"102006006\",\"seq\":1}," +

                    "{\"visible\":true,\"comp_lbl\":\"Proses\",\"comp_type\":\"7\",\"comp_id\":\"G0001\",\"seq\":2}]}," +
                    "\"static_menu\":[\"\"],\"print_text\":\"IPOP\",\"id\":\"SR10004\",\"type\":\"1\",\"title\":\"Mini Statement Fallback\"}}");
//            processResponse(fallbackScreen, "001");
            comp = fallbackScreen.getJSONObject("screen");
            pinpadTextList = new ArrayList();
            pinModuleCounter = 0;
            init();
        } catch (Exception e) {

        }
    }

    public void prepareGantiPinFallback() {
        if (alert.isShowing()) {
            alert.dismiss();
        }
        List<String> values = getValueFromScreen();
        try {
            JSONObject fallbackScreen = new JSONObject("{\"screen\":" +
                    "{\"fallback\":1,\"action_url\":\"MA0072\",\"ver\":\"1\",\"print\":null," +
                    "\"comps\":{\"comp\":[" +

                    "{\"visible\":false,\"comp_lbl\":\"Magnetic Swipe\",\"comp_type\":\"8\",\"comp_id\":\"I0003\",\"seq\":0}," +
                    "{\"visible\":true,\"comp_lbl\":\"PIN\",\"comp_type\":\"3\",\"comp_id\":\"I0001\",\"comp_opt\":\"102006006\",\"seq\":1}," +

                    "{\"visible\":true,\"comp_lbl\":\"Masukkan PIN Baru\",\"comp_type\":\"3\",\"comp_id\":\"MA071\",\"comp_opt\":\"102006006\",\"seq\":2}," +
                    "{\"visible\":true,\"comp_lbl\":\"Konfirmasi PIN Baru\",\"comp_type\":\"3\",\"comp_id\":\"MA072\",\"comp_opt\":\"102006006\",\"seq\":3}," +


                    "{\"visible\":true,\"comp_lbl\":\"Proses\",\"comp_type\":\"7\",\"comp_id\":\"G0001\",\"seq\":4}]}," +
                    "\"static_menu\":[\"\"],\"print_text\":\"IPOP\",\"id\":\"MA00072\",\"type\":\"1\",\"title\":\"Ganti PIN Fallback\"}}");
//            processResponse(fallbackScreen, "001");
            comp = fallbackScreen.getJSONObject("screen");
            pinpadTextList = new ArrayList();
            pinModuleCounter = 0;
            init();
        } catch (Exception e) {

        }
    }

    public void prepareEdupayFallback(){
        if (alert.isShowing()) {
            alert.dismiss();
        }
        List<String> values = getValueFromScreen();
        try {
            JSONObject fallbackScreen = new JSONObject("{\"screen\":" +
                    "{\"fallback\":1,\"action_url\":\"EPG302\",\"ver\":\"1\",\"print\":null," +
                    "\"comps\":{\"comp\":[" +

                    "{\"visible\":false,\"comp_lbl\":\"Magnetic Swipe\",\"comp_type\":\"8\",\"comp_id\":\"I0003\",\"seq\":0}," +
                    "{\"visible\":true,\"comp_lbl\":\"PIN\",\"comp_type\":\"3\",\"comp_id\":\"I0001\",\"comp_opt\":\"102006006\",\"seq\":1}," +

                    "{\"visible\":true,\"comp_lbl\":\"Proses\",\"comp_type\":\"7\",\"comp_id\":\"G0001\",\"seq\":2}]}," +
                    "\"static_menu\":[\"\"],\"print_text\":\"IPOP\",\"id\":\"SR10004\",\"type\":\"1\",\"title\":\"Edupay Fallback\"}}");
//            processResponse(fallbackScreen, "001");
            comp = fallbackScreen.getJSONObject("screen");
            pinpadTextList = new ArrayList();
            pinModuleCounter = 0;
            init();
        } catch (Exception e) {

        }
    }

    public void prepareEsamsatFallback(){
        if (alert.isShowing()) {
            alert.dismiss();
        }
        List<String> values = getValueFromScreen();
        try {
            JSONObject fallbackScreen = new JSONObject("{\"screen\":" +
                    "{\"fallback\":1,\"action_url\":\"EPG312\",\"ver\":\"1\",\"print\":null," +
                    "\"comps\":{\"comp\":[" +

                    "{\"visible\":false,\"comp_lbl\":\"Magnetic Swipe\",\"comp_type\":\"8\",\"comp_id\":\"I0003\",\"seq\":0}," +
                    "{\"visible\":true,\"comp_lbl\":\"PIN\",\"comp_type\":\"3\",\"comp_id\":\"I0001\",\"comp_opt\":\"102006006\",\"seq\":1}," +

                    "{\"visible\":true,\"comp_lbl\":\"Proses\",\"comp_type\":\"7\",\"comp_id\":\"G0001\",\"seq\":2}]}," +
                    "\"static_menu\":[\"\"],\"print_text\":\"IPOP\",\"id\":\"SR10004\",\"type\":\"1\",\"title\":\"Edupay Fallback\"}}");
//            processResponse(fallbackScreen, "001");
            comp = fallbackScreen.getJSONObject("screen");
            pinpadTextList = new ArrayList();
            pinModuleCounter = 0;
            init();
        } catch (Exception e) {

        }
    }

    public void preparePdamMitraFallback(){
        if (alert.isShowing()) {
            alert.dismiss();
        }
        List<String> values = getValueFromScreen();
        try {
            JSONObject fallbackScreen = new JSONObject("{\"screen\":" +
                    "{\"fallback\":1,\"action_url\":\"EPG412\",\"ver\":\"1\",\"print\":null," +
                    "\"comps\":{\"comp\":[" +

                    "{\"visible\":false,\"comp_lbl\":\"Magnetic Swipe\",\"comp_type\":\"8\",\"comp_id\":\"I0003\",\"seq\":0}," +
                    "{\"visible\":true,\"comp_lbl\":\"PIN\",\"comp_type\":\"3\",\"comp_id\":\"I0001\",\"comp_opt\":\"102006006\",\"seq\":1}," +

                    "{\"visible\":true,\"comp_lbl\":\"Proses\",\"comp_type\":\"7\",\"comp_id\":\"G0001\",\"seq\":2}]}," +
                    "\"static_menu\":[\"\"],\"print_text\":\"IPOP\",\"id\":\"SR10004\",\"type\":\"1\",\"title\":\"Edupay Fallback\"}}");
//            processResponse(fallbackScreen, "001");
            comp = fallbackScreen.getJSONObject("screen");
            pinpadTextList = new ArrayList();
            pinModuleCounter = 0;
            init();
        } catch (Exception e) {

        }
    }

    public void prepareBphtbFallback() {
        if (alert.isShowing()) {
            alert.dismiss();
        }
        List<String> values = getValueFromScreen();
        try {
            JSONObject fallbackScreen = new JSONObject("{\"screen\":" +
                    "{\"fallback\":1,\"action_url\":\"EPG512\",\"ver\":\"1\",\"print\":null," +
                    "\"comps\":{\"comp\":[" +

                    "{\"visible\":false,\"comp_lbl\":\"Magnetic Swipe\",\"comp_type\":\"8\",\"comp_id\":\"I0003\",\"seq\":0}," +
                    "{\"visible\":true,\"comp_lbl\":\"PIN\",\"comp_type\":\"3\",\"comp_id\":\"I0001\",\"comp_opt\":\"102006006\",\"seq\":1}," +

                    "{\"visible\":true,\"comp_lbl\":\"Proses\",\"comp_type\":\"7\",\"comp_id\":\"G0001\",\"seq\":2}]}," +
                    "\"static_menu\":[\"\"],\"print_text\":\"IPOP\",\"id\":\"SR10004\",\"type\":\"1\",\"title\":\"Edupay Fallback\"}}");
//            processResponse(fallbackScreen, "001");
            comp = fallbackScreen.getJSONObject("screen");
            pinpadTextList = new ArrayList();
            pinModuleCounter = 0;
            init();
        } catch (Exception e) {

        }
    }

    public void preparePjdlFallback() {
        if (alert.isShowing()) {
            alert.dismiss();
        }
        List<String> values = getValueFromScreen();
        try {
            JSONObject fallbackScreen = new JSONObject("{\"screen\":" +
                    "{\"fallback\":1,\"action_url\":\"EPG612\",\"ver\":\"1\",\"print\":null," +
                    "\"comps\":{\"comp\":[" +

                    "{\"visible\":false,\"comp_lbl\":\"Magnetic Swipe\",\"comp_type\":\"8\",\"comp_id\":\"I0003\",\"seq\":0}," +
                    "{\"visible\":true,\"comp_lbl\":\"PIN\",\"comp_type\":\"3\",\"comp_id\":\"I0001\",\"comp_opt\":\"102006006\",\"seq\":1}," +

                    "{\"visible\":true,\"comp_lbl\":\"Proses\",\"comp_type\":\"7\",\"comp_id\":\"G0001\",\"seq\":2}]}," +
                    "\"static_menu\":[\"\"],\"print_text\":\"IPOP\",\"id\":\"SR10004\",\"type\":\"1\",\"title\":\"Edupay Fallback\"}}");
//            processResponse(fallbackScreen, "001");
            comp = fallbackScreen.getJSONObject("screen");
            pinpadTextList = new ArrayList();
            pinModuleCounter = 0;
            init();
        } catch (Exception e) {

        }
    }

    public void prepareWebRegFallback() {
        if (alert.isShowing()) {
            alert.dismiss();
        }
        List<String> values = getValueFromScreen();
        try {
            JSONObject fallbackScreen = new JSONObject("{\"screen\":" +
                    "{\"fallback\":1,\"action_url\":\"EPG712\",\"ver\":\"1\",\"print\":null," +
                    "\"comps\":{\"comp\":[" +

                    "{\"visible\":false,\"comp_lbl\":\"Magnetic Swipe\",\"comp_type\":\"8\",\"comp_id\":\"I0003\",\"seq\":0}," +
                    "{\"visible\":true,\"comp_lbl\":\"PIN\",\"comp_type\":\"3\",\"comp_id\":\"I0001\",\"comp_opt\":\"102006006\",\"seq\":1}," +

                    "{\"visible\":true,\"comp_lbl\":\"Proses\",\"comp_type\":\"7\",\"comp_id\":\"G0001\",\"seq\":2}]}," +
                    "\"static_menu\":[\"\"],\"print_text\":\"IPOP\",\"id\":\"SR10004\",\"type\":\"1\",\"title\":\"Edupay Fallback\"}}");
//            processResponse(fallbackScreen, "001");
            comp = fallbackScreen.getJSONObject("screen");
            pinpadTextList = new ArrayList();
            pinModuleCounter = 0;
            init();
        } catch (Exception e) {

        }
    }

    public void prepareFallback(String svcId) {
        if (alert.isShowing()) {
            alert.dismiss();
        }
        List<String> values = getValueFromScreen();
        try {
            JSONObject fallbackScreen = new JSONObject("{\"screen\":" +
                    "{\"fallback\":1,\"action_url\":\"" +
                    svcId +
                    "\",\"ver\":\"1\",\"print\":null," +
                    "\"comps\":{\"comp\":[" +

                    "{\"visible\":false,\"comp_lbl\":\"Magnetic Swipe\",\"comp_type\":\"8\",\"comp_id\":\"I0003\",\"seq\":0}," +
                    "{\"visible\":true,\"comp_lbl\":\"PIN\",\"comp_type\":\"3\",\"comp_id\":\"I0001\",\"comp_opt\":\"102006006\",\"seq\":1}," +

                    "{\"visible\":true,\"comp_lbl\":\"Proses\",\"comp_type\":\"7\",\"comp_id\":\"G0001\",\"seq\":2}]}," +
                    "\"static_menu\":[\"\"],\"print_text\":\"IPOP\",\"id\":\"SR10004\",\"type\":\"1\",\"title\":\"Edupay Fallback\"}}");
//            processResponse(fallbackScreen, "001");
            comp = fallbackScreen.getJSONObject("screen");
            pinpadTextList = new ArrayList();
            pinModuleCounter = 0;
            init();
        } catch (Exception e) {

        }
    }

    public void preparePopupGagal(String message) {
        if (alert.isShowing()) {
            alert.dismiss();
        }
        try {
            JSONObject fallbackScreen = new JSONObject("{\"screen\":" +
                    "{\"action_url\":\"\",\"ver\":\"1\",\"print\":null," +
                    "\"comps\":{\"comp\":[" +
                    "{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"" + message +
                    "\",\"value\":\"" + message +
                    "\"}]}," +
                    "\"comp_lbl\":\"Message\",\"comp_type\":\"1\"," +
                    "\"comp_id\":\"E8251\",\"seq\":0}]}," +
                    "\"static_menu\":[\"\"],\"print_text\":\"\",\"id\":\"MB82512\",\"type\":\"3\",\"title\":\"Gagal\"}}");
//            processResponse(fallbackScreen, "001");
            processResponse(fallbackScreen, "000000");
        } catch (Exception e) {

        }
    }

    @Override
    public void onStateChanged(String p1, int p2) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("p1", p1);
        bundle.putInt("p2", p2);
        message.setData(bundle);
        stateChangeHandler.sendMessage(message);
    }

    private Handler stateChangeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle b = msg.getData();
            String p1 = b.getString("p1");
            int p2 = b.getInt("p2");
            try {
                switch (p2) {
                    case STATE_CHANGE:
                        break;
                    case MESSAGE_CHANGE:
                        refreshICCProcessDialog(p1, true);
                    case ERROR_CHANGE:
                        break;
                    case 79:
                        insertICC.setText(p1);
                        break;
                    case 999:
//                        insertICC.setText(p1);

                        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                        alertDialog.setTitle("Pemberitahuan");
                        alertDialog.setMessage(p1);
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
//                                        dialog.dismiss();
//                                        if (context instanceof ActivityList) {
//                                            Intent intents = new Intent(((ActivityList) context), MainActivity.class);
//                                            intents.putExtra("kill", "kill");
//                                            ((ActivityList) context).startActivity(intents);
//                                        } else {
                                            dialog.dismiss();
                                            context.onBackPressed();
                                            context.finish();
//                                        }
                                    }
                                });
                        Log.v("ALERT", "Remove card first, closing driver");
                        insertICC.closeDriver();
                        alert.dismiss();
                        alertDialog.show();
//                        context.onBackPressed();
                        break;
                    case 99:
                        refreshICCProcessDialog(p1, true);
                        break;
                    default:
                        Log.i("STATE", "Unhandled P2 Flag");
                }
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                PrintWriter ps = new PrintWriter(sw);
                e.printStackTrace(ps);
                insertICC.setText(sw.toString());
                Log.i("STCH", e.getMessage());
            }
        }
    };

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        if (i == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_UP) {
            view.callOnClick();
        }
        return false;
    }

    private class PrintData implements Runnable {
        private List<PrintSize> data;
        private List<String> mdata;
        private String tid;
        private String mid;
        private String stan;
        private boolean isStl;
        private boolean isReport;
        private boolean isDetail;
        private int countPrint;
        private String svrAppr;
        private String svrRef;
        private String svrDate;
        private String svrTime;
        private String reportDate;
        private String nomorKartu;
        private String jenisKartu;
        private String batchNumber;
        private String wf;

        public PrintData(List<PrintSize> data, List<String> mdata, String tid, String mid,
                         String stan, String wf, int countPrint, String svrRef, String svrDate,
                         String svrTime, String nomorKartu, String cardType, String batch, String appr) {
            this.isStl = false;
            this.isReport = false;
            this.isDetail = false;
            this.reportDate = "";
            this.wf = wf;
            if (wf.equals("WF")) {
                data = addStandardFooter(data);
            } else if (wf.equals("CU")) {
                data = addCetakUlangFooter(data);
            } else if (wf.equals("PIN")) {
                data = addChangePINFooter(data);
            }else if (wf.equals("PF")) {
                data = addPulsaFooter(data);
            } else if (wf.equals("SAM")) {
                data = addSamsatFooter(data);
            } else if (wf.equals("SUSPECT")) {
                data = addSuspectFooter(data);
            } else if (wf.equals("TO")){
                data = addTimeoutFooter(data);
            } else if (wf.equals("MPN")) {
                data = addFooterMPN(data);
            } else if (wf.equals("STL")) {
                this.isStl = true;
                data.add(new PrintSize(FontSize.NORMAL, "START_FOOTER"));
                data.add(new PrintSize(FontSize.EMPTY, "\n"));
                data.add(new PrintSize(FontSize.NORMAL, "Settlement BERHASIL\n"));
                data.add(new PrintSize(FontSize.EMPTY, "\n"));
                data = addReportFooter(data);
            } else if (wf.startsWith("RPT")) {
                this.isReport = true;
                if (wf.length() > 3) {
                    this.reportDate = wf.substring(3);
                }
                data = addReportFooter(data);
            } else if (wf.startsWith("RPD")) {
                this.isDetail = true;
                if (wf.length() > 3) {
                    this.reportDate = wf.substring(3);
                }
                data = addReportFooter(data);
            } else if (wf.equals("ICC")) {
                data = addICCStatement(data);
            }
            this.data = data;
            this.mdata = mdata;
            this.tid = tid;
            this.mid = mid;
            this.stan = stan;
            this.countPrint = countPrint;
            this.nomorKartu = nomorKartu;
//            Log.d("PRINT INIT", "card number : " + nomorKartu);
            this.jenisKartu = cardType;
            if (svrRef != null) {
                this.svrRef = svrRef;
            } else {
                this.svrRef = "000000000000";
            }
            if (svrDate != null) {
                this.svrDate = svrDate;
            } else {
                this.svrDate = "0";
            }
            if (svrTime != null) {
                this.svrTime = svrTime;
            } else {
                this.svrTime = "0";
            }
            if (appr != null) {
                this.svrAppr = appr;
            } else {
                this.svrAppr = "00000000";
            }
            this.batchNumber = StringLib.fillZero(String.valueOf(batch), 6);
        }

        public List<PrintSize> addStandardFooter(List<PrintSize> data) {
            data.add(new PrintSize(FontSize.NORMAL, "START_FOOTER"));
            data.add(new PrintSize(FontSize.EMPTY, "\n"));
            boolean foundNullNtpn = false;
            boolean foundNTPN = false;
            for (int i = 0; i < data.size(); i++) {
                PrintSize dataPrint = data.get(i);
                //Add Struk Format MPN NTPN
                Log.d("dataPrint = ", dataPrint.getMessage());

                if (dataPrint.getMessage().contains("NTPN")) {
                    foundNTPN = true;
                }

                if (foundNTPN) {
                    if (dataPrint.getMessage().contains("-")) {
                        if (dataPrint.getMessage().contains("TC") || dataPrint.getMessage().contains("AID")) {
                            foundNullNtpn = false;
                        } else {
                            foundNullNtpn = true;
                            break;
                        }
                    }
                }

            }
            if (foundNullNtpn) {
                data.add(new PrintSize(FontSize.NORMAL, "TRANSAKSI SEDANG DALAM PROSES\n"));
            } else {
                data.add(new PrintSize(FontSize.NORMAL, "TRANSAKSI BERHASIL\n"));
            }
            data.add(new PrintSize(FontSize.EMPTY, "\n"));
            data.add(new PrintSize(FontSize.NORMAL, "Informasi lebih lanjut, silahkan hubungi\n"));
            data.add(new PrintSize(FontSize.NORMAL, "Bank BJB\n"));
            data.add(new PrintSize(FontSize.NORMAL, "***Terima Kasih***\n"));
            return data;
        }

        public List<PrintSize> addFooterMPN(List<PrintSize> data) {
            data.add(new PrintSize(FontSize.NORMAL, "START_FOOTER"));
            data.add(new PrintSize(FontSize.EMPTY, "\n"));
            boolean foundNullNtpn = false;
            boolean foundNTPN = false;
            for (int i = 0; i < data.size(); i++) {
                PrintSize dataPrint = data.get(i);
                //Add Struk Format MPN NTPN
                Log.d("dataPrint = ", dataPrint.getMessage());
                if (dataPrint.getMessage().contains("NTPN")) {
                    foundNTPN = true;
                }

                if (foundNTPN) {
                    if (dataPrint.getMessage().contains("-")) {
                        if (dataPrint.getMessage().contains("TC") || dataPrint.getMessage().contains("AID")) {
                            foundNullNtpn = false;
                        } else {
                            foundNullNtpn = true;
                            break;
                        }
                    }
                }
            }

            if (foundNullNtpn) {
                data.add(new PrintSize(FontSize.NORMAL, "TRANSAKSI SEDANG DALAM PROSES\n"));
            } else {
                data.add(new PrintSize(FontSize.NORMAL, "TRANSAKSI BERHASIL\n"));
            }
            data.add(new PrintSize(FontSize.EMPTY, "\n"));
            data.add(new PrintSize(FontSize.NORMAL, "MOHON DISIMPAN RESI INI ADALAH\n"));
            data.add(new PrintSize(FontSize.NORMAL, "BUKTI PEMBAYARAN YANG SAH\n\n"));
            data.add(new PrintSize(FontSize.NORMAL, "***Terima Kasih***\n"));
            return data;
        }

        public List<PrintSize> addSuspectFooter(List<PrintSize> data) {
            data.add(new PrintSize(FontSize.NORMAL, "START_FOOTER"));
            data.add(new PrintSize(FontSize.EMPTY, "\n"));

            data.add(new PrintSize(FontSize.NORMAL, "SILAHKAN CEK SALDO ANDA\n"));
            data.add(new PrintSize(FontSize.NORMAL, "ATAU HUBUNGI BANK ANDA\n"));
            data.add(new PrintSize(FontSize.NORMAL, "BJB CALL 14049\n\n"));
            data.add(new PrintSize(FontSize.NORMAL, "***Terima Kasih***\n"));
            return data;
        }

        public List<PrintSize> addTimeoutFooter(List<PrintSize> data) {
            data.add(new PrintSize(FontSize.NORMAL, "START_FOOTER"));
            data.add(new PrintSize(FontSize.EMPTY, "\n"));
            data.add(new PrintSize(FontSize.NORMAL, "TRANSAKSI SEDANG DALAM PROSES\n"));
            data.add(new PrintSize(FontSize.NORMAL, "SILAHKAN CEK SALDO ANDA\n"));
            data.add(new PrintSize(FontSize.NORMAL, "ATAU HUBUNGI BANK ANDA\n"));
            data.add(new PrintSize(FontSize.NORMAL, "BJB CALL 14049\n\n"));
            data.add(new PrintSize(FontSize.NORMAL, "***Terima Kasih***\n"));
            return data;
        }

        public List<PrintSize> addICCStatement(List<PrintSize> data) {
            data.add(new PrintSize(FontSize.NORMAL, "START_FOOTER"));
            data.add(new PrintSize(FontSize.EMPTY, "\n"));

            boolean foundNullNtpn = false;
            boolean foundNTPN = false;
            for (int i = 0; i < data.size(); i++) {
                PrintSize dataPrint = data.get(i);
                //Add Struk Format MPN NTPN
                Log.d("dataPrint = ", dataPrint.getMessage());

                if (dataPrint.getMessage().contains("NTPN")) {
                    foundNTPN = true;
                }

                if (foundNTPN) {
                    if (dataPrint.getMessage().contains("-")) {
                        if (dataPrint.getMessage().contains("TC") || dataPrint.getMessage().contains("AID")) {
                            foundNullNtpn = false;
                        } else {
                            foundNullNtpn = true;
                            break;
                        }
                    }
                }
            }

            if (foundNullNtpn) {
                data.add(new PrintSize(FontSize.NORMAL, "TRANSAKSI SEDANG DALAM PROSES\n\n"));
            } else {
                data.add(new PrintSize(FontSize.NORMAL, "TRANSAKSI BERHASIL\n\n"));
            }

            data.add(new PrintSize(FontSize.NORMAL, "No Signature Required/PIN Verified\n"));
            data.add(new PrintSize(FontSize.NORMAL, "I Agree to Pay Above Total Amount\n"));
            data.add(new PrintSize(FontSize.NORMAL, "According To The Card Issuer Agreement\n"));
            return data;
        }

        public List<PrintSize> addSamsatFooter(List<PrintSize> data) {
            data.add(new PrintSize(FontSize.NORMAL, "START_FOOTER"));
            data.add(new PrintSize(FontSize.EMPTY, "\n"));
            data.add(new PrintSize(FontSize.NORMAL, "STRUK INI SEBAGAI BUKTI PEMBAYARAN SAH\n"));
            if (this.wf.equals("3")) {
                data.add(new PrintSize(FontSize.NORMAL, "YANG BERLAKU 3 HARI SETELAH TANGGAL\n"));
            } else {
                data.add(new PrintSize(FontSize.NORMAL, "YANG BERLAKU 14 HARI SETELAH TANGGAL\n"));
            }
            data.add(new PrintSize(FontSize.NORMAL, "BAYAR, DAN WAJIB DITUKARKAN DENGAN\n"));
            data.add(new PrintSize(FontSize.NORMAL, "SKPD DI SAMSAT/GERAI TERDEKAT SERTA\n"));
            data.add(new PrintSize(FontSize.NORMAL, "MEMBAWA STNK&KTP ASLI PEMILIK KENDARAAN\n"));
            return data;
        }

        public List<PrintSize> addTrfSamsatFooter(List<PrintSize> data) {
            data.add(new PrintSize(FontSize.NORMAL, "START_FOOTER"));
            data.add(new PrintSize(FontSize.EMPTY, "\n"));
            data.add(new PrintSize(FontSize.NORMAL, "TRANSAKSI BERHASIL\n"));
            data.add(new PrintSize(FontSize.NORMAL, "INFORMASI LEBIH LANJUT, SILAHKAN\n"));
            data.add(new PrintSize(FontSize.NORMAL, "HUBUNGI BANK BJB\n"));
            data.add(new PrintSize(FontSize.EMPTY, "\n"));
            data.add(new PrintSize(FontSize.NORMAL, "***TERIMA KASIH***\n"));
            return data;
        }

        public List<PrintSize> addCetakUlangFooter(List<PrintSize> data) {
            data.add(new PrintSize(FontSize.NORMAL, "START_FOOTER"));
            data.add(new PrintSize(FontSize.EMPTY, "\n"));
            data.add(new PrintSize(FontSize.NORMAL, "CETAK ULANG\n"));
            data.add(new PrintSize(FontSize.NORMAL, "MOHON DISIMPAN RESI INI ADALAH\n"));
            data.add(new PrintSize(FontSize.NORMAL, "BUKTI PEMBAYARAN YANG SAH\n"));
            data.add(new PrintSize(FontSize.EMPTY, "\n"));
            data.add(new PrintSize(FontSize.NORMAL, "***TERIMA KASIH***\n"));
            return data;
        }

        public List<PrintSize> addChangePINFooter(List<PrintSize> data) {
            data.add(new PrintSize(FontSize.NORMAL, "START_FOOTER"));
            data.add(new PrintSize(FontSize.EMPTY, "\n"));
            data.add(new PrintSize(FontSize.NORMAL, "TRANSAKSI BERHASIL\n"));
            data.add(new PrintSize(FontSize.NORMAL, "RESI INI MERUPAKAN BUKTI YANG SAH\n"));
            data.add(new PrintSize(FontSize.NORMAL, "RAHASIAKAN PIN ANDA\n"));
            data.add(new PrintSize(FontSize.NORMAL, "BJB CALL 14049\n"));
            data.add(new PrintSize(FontSize.EMPTY, "\n"));
            data.add(new PrintSize(FontSize.NORMAL, "***TERIMA KASIH***\n"));
            return data;
        }

        public List<PrintSize> addPulsaFooter(List<PrintSize> data) {
            data.add(new PrintSize(FontSize.NORMAL, "START_FOOTER"));
            data.add(new PrintSize(FontSize.EMPTY, "\n"));
            data.add(new PrintSize(FontSize.NORMAL, "TRANSAKSI BERHASIL\n"));
            data.add(new PrintSize(FontSize.NORMAL, "INFORMASI LEBIH LANJUT, SILAHKAN\n"));
            data.add(new PrintSize(FontSize.NORMAL, "HUBUNGI BANK BJB\n"));
            data.add(new PrintSize(FontSize.EMPTY, "\n"));
            data.add(new PrintSize(FontSize.NORMAL, "***TERIMA KASIH***\n"));
            return data;
        }

        public List<PrintSize> addReportFooter(List<PrintSize> data) {
            data.add(new PrintSize(FontSize.NORMAL, "START_FOOTER"));
            data.add(new PrintSize(FontSize.EMPTY, "\n"));
            data.add(new PrintSize(FontSize.NORMAL, "Informasi lebih lanjut, silahkan hubungi\n"));
            data.add(new PrintSize(FontSize.NORMAL, "Bank BJB\n"));
            data.add(new PrintSize(FontSize.EMPTY, "\n"));
            data.add(new PrintSize(FontSize.NORMAL, "***Terima Kasih***\n"));
            return data;
        }

        @Override
        public void run() {
            while (printInUse) {
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    Log.e("PRINT", "CANCELED");
                    return;
                }
            }
            printInUse = true;

            //ADD STORE NAME HEADER
            SharedPreferences preferences = context.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
            String storeName = CommonConfig.STORE_NAME;
            try {
                storeName = preferences.getString("store_name", CommonConfig.STORE_NAME);
            } catch (Exception e){}

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            AssetManager assetManager = context.getAssets();
            String bmp_path = "logo_bjb.jpg";
            InputStream inputStream = null;
            try {
                inputStream = assetManager.open(bmp_path);
            } catch (IOException e) {
                Log.e("PRINT", "CANNOT OPEN BITMAP");
            }
//            Bitmap bitmap = BitmapFactory.decodeFile("/sdcard/Pictures/bri-small.jpg");
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (isReprint) {
                countPrint = 4;
                stan = reprintTrace;
            }
            if (isStl) {
                ESCPOSApi.printSettlement(bitmap, data, mdata, tid, mid, stan, svrDate, svrTime);
            } else if (isReport) {
                ESCPOSApi.printReport(bitmap, data, mdata, tid, mid, reportDate, storeName);
            } else if (isDetail) {
                ESCPOSApi.printDetailReport(bitmap, data, mdata, tid, mid, reportDate, storeName);
            } else {
                if (tid != null) {
//                    Log.d(TAG, "Count Print : " + String.valueOf(countPrint));
//                    Log.d(TAG, "Start Print @"+svrTime + " " + svrDate);
                    String cardType = "DEBIT (SWIPE)";
                    if (!jenisKartu.equals("")) {
                        cardType = jenisKartu;
                    }
                    if (formId.equals("MB91002") || formId.equals("MB82102")) {
                        data = addSamsatFooter(data);
                    }
                    if (formId.equals("MB82302")) {
                        data = addTrfSamsatFooter(data);
                    }
                    ESCPOSApi.printStruk(bitmap, data, mdata, tid, mid, stan, countPrint,
                            svrRef, svrDate, svrTime, cardType, nomorKartu, formId, batchNumber, svrAppr, storeName);
                } else {
                    ESCPOSApi.printStruk(bitmap, data);
                }
            }
            countPrint++;
//            Log.d("PRINT", "FINISHED");
            printInUse = false;
        }
    }

    public void reFocus() {
        focusHasSets = false;
        for (int ch = 0; ch < baseLayout.getChildCount(); ch++) {
            if (baseLayout.getChildAt(ch).getVisibility() == VISIBLE &&
                    baseLayout.getChildAt(ch) instanceof EditText) {
                if (!focusHasSets) {
                    focusHasSets = true;
                    baseLayout.getChildAt(ch).requestFocus();
                    Log.i("FOCUS", baseLayout.getChildAt(ch).toString() + " had focus is #" + ch);
                }
            }
        }
    }

    public void cancelFocus() {
        for (int ch = 0; ch < baseLayout.getChildCount(); ch++) {
            if (baseLayout.getChildAt(ch).getVisibility() == VISIBLE &&
                    baseLayout.getChildAt(ch) instanceof EditText) {
                if (focusHasSets) {
                    focusHasSets = false;
                    baseLayout.getChildAt(ch).clearFocus();
                }
            }
        }
    }

    Runnable delayFocus = new Runnable() {
        @Override
        public void run() {
            try {
//                while (((ActivityList) context).isPinpadInUse()) {
//                    Thread.sleep(1000);
//                }
                reFocus();
            } catch (Exception e) {
                //pass
            }
        }
    };

    public static String encData(String plainKey) throws InvalidKeyException,
            NoSuchAlgorithmException,
            InvalidKeySpecException,
            NoSuchPaddingException,
            IllegalBlockSizeException,
            BadPaddingException {
        String dummyKey = "1C1C1C1C1F1F1F1F";
        DESKeySpec dks = new DESKeySpec(hexStringToByteArray(dummyKey));
        SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
        SecretKey desKey = skf.generateSecret(dks);
        Cipher cipher = Cipher.getInstance("DES"); // DES/ECB/PKCS5Padding for SunJCE
        cipher.init(Cipher.ENCRYPT_MODE, desKey);
//        if (plainKey.length() % 2 > 0) {
//            plainKey = plainKey + "F";
//        }
        plainKey = plainKey + "FFFFFFFFFFFFFFFF";
        plainKey = plainKey.substring(0, 16);
        byte[] pinBlock = cipher.doFinal(hexStringToByteArray(plainKey));
        return bytesToHex(pinBlock);
    }

    public void processResponse(JSONObject jsonResp, String msgId) throws JSONException {
//        Toast.makeText(context, jsonResp.toString(), Toast.LENGTH_LONG).show();
        Log.i("JSR", jsonResp.toString());
        if (jsonResp.has("reprint")) {
            isReprint = true;
            if (jsonResp.has("pan")) {
                panHolder = jsonResp.getString("pan");
            }
            reprintTrace = jsonResp.getString("rstan");
        } else if (jsonResp.has("void")) {
            isReprint = false;
            String vstan = jsonResp.getString("rstan");
            if (vstan.equals("")) {
                vstan = jsonResp.getJSONObject("screen").getString("stan");
            }
            try {
                JSONArray newComp = new JSONArray();
                JSONArray oldComp = jsonResp.getJSONObject("screen").getJSONObject("comps").getJSONArray("comp");
                int cseq = 1;
                for (int i = 0; i < oldComp.length(); i++) {
                    JSONObject cmp = oldComp.getJSONObject(i);
                    cseq++;
                    if (cmp.getString("comp_lbl").contains("AMOUNT")) {
                        newComp.put(cmp);
                        String cmpVal = cmp.getJSONObject("comp_values").getJSONArray("comp_value").getJSONObject(0).getString("value");
                        if (cmpVal.startsWith("Rp")) {
                            cmpVal = cmpVal.substring(3);
                        }
                        if (cmpVal.contains(",")) {
                            cmpVal = cmpVal.substring(0, cmpVal.indexOf(',')).trim();
                        }
                        if (cmpVal.contains(".")) {
                            cmpVal = cmpVal.replaceAll(".", "");
                        }
                        if (cmpVal.length() < 12) {
                            cmpVal = "000000000000" + cmpVal;
                            cmpVal = cmpVal.substring(cmpVal.length() - 12);
                        }
                        JSONObject ncv = new JSONObject();
                        ncv.put("print", cmpVal);
                        ncv.put("value", cmpVal);
                        JSONArray acv = new JSONArray();
                        acv.put(ncv);
                        JSONObject cvs = new JSONObject();
                        cvs.put("comp_value", acv);
                        JSONObject nCmp = new JSONObject();
                        nCmp.put("visible", false);
                        nCmp.put("comp_values", cvs);
                        nCmp.put("comp_lbl", "Raw Nominal");
                        nCmp.put("comp_type", "2");
                        nCmp.put("comp_id", "V0002");
                        nCmp.put("comp_opt", "102012012");
                        nCmp.put("seq", cseq);
//                        newComp.put(nCmp);
                    }
                    if (cmp.getString("comp_lbl").contains("Invoice")) {
                        newComp.put(cmp);
                        String cmpVal = cmp.getJSONObject("comp_values").getJSONArray("comp_value").getJSONObject(0).getString("value");
                        JSONObject ncv = new JSONObject();
                        ncv.put("print", cmpVal);
                        ncv.put("value", cmpVal);
                        JSONArray acv = new JSONArray();
                        acv.put(ncv);
                        JSONObject cvs = new JSONObject();
                        cvs.put("comp_value", acv);
                        JSONObject nCmp = new JSONObject();
                        nCmp.put("visible", false);
                        nCmp.put("comp_values", cvs);
                        nCmp.put("comp_lbl", "No Invoice");
                        nCmp.put("comp_type", "2");
                        nCmp.put("comp_id", "V0001");
                        nCmp.put("comp_opt", "102010010");
                        nCmp.put("seq", cseq);
//                        newComp.put(nCmp);
                    }
                }
                //inject data
                JSONObject ncv = new JSONObject();
                ncv.put("print", vstan+"|"+messageId);
                ncv.put("value", vstan+"|"+messageId);
                JSONArray acv = new JSONArray();
                acv.put(ncv);
                JSONObject cvs = new JSONObject();
                cvs.put("comp_value", acv);
                JSONObject nCmp = new JSONObject();
                nCmp.put("visible", false);
                nCmp.put("comp_values", cvs);
                nCmp.put("comp_lbl", "void");
                nCmp.put("comp_type", "2");
                nCmp.put("comp_id", "V0003");
                nCmp.put("comp_opt", "102001006");
                nCmp.put("seq", 0);
                newComp.put(nCmp);

                //inject button
                ncv = new JSONObject();
                ncv.put("print", null);
                ncv.put("value", null);
                acv = new JSONArray();
                acv.put(ncv);
                cvs = new JSONObject();
                cvs.put("comp_value", acv);
                nCmp = new JSONObject();
                nCmp.put("visible", true);
                nCmp.put("comp_values", cvs);
                nCmp.put("comp_lbl", "Proses");
                nCmp.put("comp_type", "7");
                nCmp.put("comp_id", "G0001");
                nCmp.put("seq", cseq + 1);
                newComp.put(nCmp);
                jsonResp.getJSONObject("screen").getJSONObject("comps").remove("comp");
                jsonResp.getJSONObject("screen").getJSONObject("comps").put("comp", newComp);
                jsonResp.getJSONObject("screen").put("print", null);
                jsonResp.getJSONObject("screen").put("title", "Konfirmasi Void Sale");
                jsonResp.getJSONObject("screen").put("id", "MB82521");
                jsonResp.getJSONObject("screen").put("action_url", "E82520");
                jsonResp.getJSONObject("screen").put("stan", "void");
            } catch (Exception e) {

            }
        } else {
            isReprint = false;
            JsonCompHandler.saveJsonMessage(context, msgId, "rp", jsonResp);
            String stan = "000000";
            String amount = "";
            JSONObject responObj = jsonResp.getJSONObject("screen");
            if (msgId.equals("reprint")){
                isReprint = true;
                responObj.remove("print");
                responObj.put("print", "3");
                try {
                    reprintTrace = responObj.getString("stan");
                } catch (Exception e){}
            }

            try {
                if (responObj.has("comps")) {
                    JSONObject comps = responObj.getJSONObject("comps");
                    if (comps.has("comp")) {
                        JSONArray comp_array = comps.getJSONArray("comp");
                        for (int i = 0; i < comp_array.length(); i++) {
                            JSONObject comp_obj = comp_array.getJSONObject(i);
                            if (comp_obj != null && comp_obj.getString("comp_type").equals("1") && comp_obj.has("comp_act") && comp_obj.getString("comp_act").equals("sal_amount")) {
                                if (comp_obj.has("comp_values")) {
                                    JSONObject comp_values = comp_obj.getJSONObject("comp_values");
                                    if (comp_values.has("comp_value")) {
                                        JSONArray comp_value_array = comp_values.getJSONArray("comp_value");
                                        if (comp_value_array.length() == 1) {
                                            JSONObject comp_value_obj = comp_value_array.getJSONObject(0);
                                            String value = comp_value_obj.getString("value");
                                            value = value.replace("Rp", "");
                                            value = value.replace(".", "");
                                            if (value.contains(",00")) {
                                                value = value.replace(",00", "");
                                                amount = value.trim();
                                            }
                                        }
                                    }
                                }

                            }
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (responObj.has("stan")) {
                stan = responObj.getString("stan");
            }
            String rc = "";
            String emode = "021";
            if (responObj.has("print")) {
                if (responObj.getString("print") != null) {
                    rc = responObj.getString("print");
                }
            }
            if (responObj.has("print_text")) {
                if (responObj.getString("print_text") != null) {
                    String screen_flag = responObj.getString("print_text");
                    if (screen_flag.contains("STL")) {
                        rc = "";
                    }
                }
            }

            if (!amount.equals("")) {
                rc = "00";
            }

            //mun bisa updLogEdc didieu
            updEdcLog(msgId, stan, rc, amount);
            if (responObj.has("print_text")) {
                if (responObj.getString("print_text") != null) {
                    String screen_flag = responObj.getString("print_text");
                    if (screen_flag.contains("STL")) {
                        updSettlementSukses();
                    }
                }
            }

        }
        if (jsonResp.has("server_ref")) {
            serverRef = jsonResp.getString("server_ref");
        }
        if (jsonResp.has("server_date")) {
            serverDate = jsonResp.getString("server_date");
        }
        if (jsonResp.has("server_time")) {
            serverTime = jsonResp.getString("server_time");
        }
        if (jsonResp.has("card_type")) {
            cardType = jsonResp.getString("card_type");
        }
        if (jsonResp.has("nomor_kartu")) {
            nomorKartu = jsonResp.getString("nomor_kartu");
            nomorKartu = nomorKartu.split("=")[0];
            nomorKartu = nomorKartu.substring(0, 6) + "******" + nomorKartu.substring(12);
        }
        JSONObject responObj = jsonResp.getJSONObject("screen");
//                                        JsonCompHandler.saveJson(context, responObj);
        int type = responObj.getInt("type");
        if (isReprint) {
            if (jsonResp.has("tcaid")) {
                String tcaid = jsonResp.getString("tcaid");
                responObj.put("tcaid", tcaid);
                if (jsonResp.has("txpan")) {
                    responObj.put("pan", jsonResp.getString("txpan"));
                }
            }
        }
        //convert type to old
        if (type == 6) {
            type = 1;
        } else if (type == 7) {
            type = 2;
        } else if (type == 5) {
            type = 2;
        }
        if (type == CommonConfig.MenuType.Form) {
            Log.d("VNEXT", "Form");
            comp = responObj;
            init();
        } else if (type == CommonConfig.MenuType.PopupGagal) {
            Log.d("VNEXT", "Popup Gagal");
            // add close icc interface if exists
            try {
                if (insertICC != null) {
                    if (insertICC.isOpen()) {
                        insertICC.closeDriver();
                    }
                }
            } catch (Exception e) {
                //failed to close, maybe already closed or not open yet
            }
            // end add close icc
            JSONObject val = responObj
                    .getJSONObject("comps")
                    .getJSONArray("comp").getJSONObject(0)
                    .getJSONObject("comp_values")
                    .getJSONArray("comp_value").getJSONObject(0);
            AlertDialog alertDialog = new AlertDialog.Builder(context).create();
            alertDialog.setTitle(responObj.getString("title"));
            if (val.has("value")) {
                alertDialog.setMessage(Html.fromHtml(val.getString("value")));
            } else {
                alertDialog.setMessage("Error belum terdefinisi");
            }

            String rc = "none";
            if (responObj.has("print")) {
                if (responObj.getString("print") != null) {
                    rc = responObj.getString("print");
                }
            }
            Log.i("RC", "RC EUY: " + Html.fromHtml(val.getString("value")).toString());

//            if (Html.fromHtml(val.getString("value")).toString().equalsIgnoreCase("Expired Card")){
//                // Tagihan sudah terbayar
//                alertDialog.setMessage("Tagihan sudah terbayar");
//            }
//            if (Html.fromHtml(val.getString("value")).toString().equalsIgnoreCase("Invalid PIN")){
//                // Tagihan tidak ditemukan
//                alertDialog.setMessage("Tagihan tidak ditemukan");
//            }

            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            Intent intents = new Intent(context, MainActivity.class);
                            intents.putExtra("kill", "kill");
                            context.startActivity(intents);

                        }
                    });
            alertDialog.show();
        } else if (type == CommonConfig.MenuType.PopupBerhasil) {
            Log.d("VNEXT", "Popup Sukses");
            JSONObject val = responObj
                    .getJSONObject("comps")
                    .getJSONArray("comp").getJSONObject(0)
                    .getJSONObject("comp_values")
                    .getJSONArray("comp_value").getJSONObject(0);
            AlertDialog alertDialog = new AlertDialog.Builder(context).create();
            alertDialog.setTitle(responObj.getString("title"));
            if (val.has("value")) {
                alertDialog.setMessage(Html.fromHtml(val.getString("value")));
            } else {
                alertDialog.setMessage("Error belum terdefinisi");
            }
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (screenLoader.equals("2100000")) {
                                context.onBackPressed();
                            } else {
                                Intent myIntent = new Intent(context, MainActivity.class);
                                context.startActivityForResult(myIntent, 0);
                            }
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
    }

    private JSONObject handleReprint(String serviceId) {
        if (helperDb == null) {
            helperDb = new DataBaseHelper(context);
        }
        SQLiteDatabase clientDB = null;
        JSONObject rps = new JSONObject();
        String logText = "reprint [" + serviceId + "]";
        try {
            Log.d("CEK ARYO", "5");
            rps = new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"Data transaksi tidak ditemukan\",\n" +
                    "\"value\":\"Data transaksi tidak ditemukan\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                    "\"type\":\"3\",\"title\":\"Gagal\"}}");
            helperDb.openDataBase();
            clientDB = helperDb.getActiveDatabase();
            String qLog = "";
            if (serviceId.startsWith("E")) {
                qLog = "select * from edc_log  "
                        + "where service_id like 'E" + serviceId.substring(1) + "%' "
                        + "and rc <> '' and settled = 0 "
                        + "order by log_id desc";
            } else {
                qLog = "select * from edc_log  "
                        + "where service_id like 'E825%' "
                        + "and rc <> '' and settled = 0 "
                        + "order by log_id desc";
            }

            Cursor cLog = clientDB.rawQuery(qLog, null);
            if (cLog.moveToFirst()) {
                String msgId = cLog.getString(cLog.getColumnIndex("messageid"));
                String tcaid = cLog.getString(cLog.getColumnIndex("previd"));
                String txpan = cLog.getString(cLog.getColumnIndex("track2"));
                if (cLog != null) {
                    cLog.close();
                }
                logText += ", db found";
                rps = JsonCompHandler.loadJsonMessage(context, msgId, "rp", "");
                rps.put("reprint", 1);
                String screen_trace = "000000";
                if (rps.has("stan")) {
                    screen_trace = rps.getString("stan");
                }
                JSONObject screenRps = rps.getJSONObject("screen");
                if (screenRps.has("stan")) {
                    screen_trace = screenRps.getString("stan");
                }
                rps.put("rstan", screen_trace);
                if (clientDB.isOpen()) {
                    clientDB.close();
                }
                if (tcaid != null) {
                    if (tcaid.length() > 4) {
                        rps.put("tcaid", tcaid);
                        rps.put("txpan", txpan);
                        logText += ", injected tcaid and txpan";
                    }
                }
            } else {
                logText += ", db not found";
                if (cLog != null) {
                    cLog.close();
                }
                if (clientDB.isOpen()) {
                    clientDB.close();
                }
                Log.d("CEK ARYO", "6");
                rps = new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"Data transaksi tidak ditemukan\",\n" +
                        "\"value\":\"Data transaksi tidak ditemukan\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                        "\"type\":\"3\",\"title\":\"Gagal\"}}");
            }
        } catch (Exception ex) {
            Log.i("TX", "DB error");
            ex.printStackTrace();
            logText += ", excptn";
        }
//        Toast.makeText(context, logText, Toast.LENGTH_LONG).show();
        return rps;
    }

    private JSONObject handleReport(String serviceId) {
        if (helperDb == null) {
            helperDb = new DataBaseHelper(context);
        }

        SQLiteDatabase clientDB = null;
        JSONObject rps = new JSONObject();

        Log.d("CEK ARYO", "5");
        try {

            helperDb.openDataBase();
            clientDB = helperDb.getActiveDatabase();

            String dateFilter = "";
//            String dateFilter = "and date(a.rqtime)='" + StringLib.getYYYYMMDD() + "'\n";
            String tgl = StringLib.getYYYYMMDD();
            String modBase = "edc_log";
//            String siLimit = serviceId.substring(0, 2);
            String siLimit = "M00";
            String nGrand = "0";
            String jGrand = "0";
            Log.d("EDCLOG", "read grand (744)");
            String qGrand = ""
                    + "select sum(a.amount) tot, count(*) jml, service_name from " + modBase + " a left outer join service b\n" +
                    "on (a.service_id = b.service_id)\n" +
                    "where a.rc= '00'\n" +
                    dateFilter +
//                    "and a.amount is not null\n" +
//                    "and a.amount <> 0\n" +
                    "and (lower(a.reversed) <> 't' or a.reversed is null)\n" +
                    "and a.service_id like '" + siLimit + "%';";

            Log.d("RPT", "Query Grand \n" + qGrand);
            Cursor cGrand = clientDB.rawQuery(qGrand, null);
            if (cGrand.moveToFirst()) {
                nGrand = String.valueOf(cGrand.getInt(cGrand.getColumnIndex("tot")));
                jGrand = String.valueOf(cGrand.getInt(cGrand.getColumnIndex("jml")));
                String cTx = cGrand.getString(cGrand.getColumnIndex("service_name"));
                if (nGrand.matches("-?\\d+(\\.\\d+)?")) {
                    double d = Double.parseDouble(nGrand);
                    DecimalFormatSymbols idrFormat = new DecimalFormatSymbols(Locale.getDefault());
                    idrFormat.setDecimalSeparator(',');
                    DecimalFormat formatter = new DecimalFormat("###,###,##0", idrFormat);
//                    if (cTx!=null){
//                        if (!cTx.startsWith("Pembayaran PLN") && !cTx.startsWith("Pembayaran Non-")) {
//                            d = d/100;
//                        }
//                    }
                    d = d / 10;
                    nGrand = formatter.format(d);
                }
            }
            if (cGrand != null) {
                cGrand.close();
            }

            Log.d("EDCLOG", "read (772)");
            String qLog = ""
                    + "select (case when b.service_name is null then a.service_id else b.service_name end)" +
                    " as service_name, count(*) jml, sum(a.amount) tot from edc_log a left outer join service b\n" +
                    "on (a.service_id = b.service_id)\n" +
                    "where a.rc= '00'\n" +
                    dateFilter +
//                    "and a.amount is not null\n" +
//                    "and a.amount > 0\n" +
                    "and (lower(a.reversed) <> 't' or a.reversed is null)\n" +
                    "and a.service_id like '" + siLimit + "%'\n" +
                    "group by b.service_name;";

            Log.d("RPT", "Query Data \n" + qLog);
            Cursor cLog = clientDB.rawQuery(qLog, null);
            if (cLog.moveToFirst()) {
                String cmp = "";
                int sq = 0;
                String ssq = String.valueOf(sq);
                cmp += "{\"visible\":true,\"comp_values\":"
                        + "{\"comp_value\":["
                        + "{\"print\":\"----------------------------------------\",\n"
                        + "\"value\":\"----------------------------------------\"}"
                        + "]},\"comp_lbl\":\"\",\"comp_type\":\"1\","
                        + "\"comp_id\":\"R00001\",\"seq\":" + ssq + "}";
                sq++;
                ssq = String.valueOf(sq);
                cmp += ",";
                cmp += "{\"visible\":true,\"comp_values\":"
                        + "{\"comp_value\":["
                        + "{\"print\":\"TRANSAKSI           COUNT         AMOUNT\",\n"
                        + "\"value\":\"TRANSAKSI           COUNT         AMOUNT\"}"
                        + "]},\"comp_lbl\":\"\",\"comp_type\":\"1\","
                        + "\"comp_id\":\"R00001\",\"seq\":" + ssq + "}";
                sq++;
                ssq = String.valueOf(sq);
                cmp += ",";
                cmp += "{\"visible\":true,\"comp_values\":"
                        + "{\"comp_value\":["
                        + "{\"print\":\"----------------------------------------\",\n"
                        + "\"value\":\"----------------------------------------\"}"
                        + "]},\"comp_lbl\":\"\",\"comp_type\":\"1\","
                        + "\"comp_id\":\"R00001\",\"seq\":" + ssq + "}";
                sq++;
                ssq = String.valueOf(sq);

                do {
                    if (!cmp.equals("")) {
                        cmp += ",";
                    }
                    String cval = String.valueOf(cLog.getInt(cLog.getColumnIndex("tot")));
                    String clab = cLog.getString(cLog.getColumnIndex("service_name"));
                    if (cval.matches("-?\\d+(\\.\\d+)?")) {
                        double d = Double.parseDouble(cval);
                        DecimalFormatSymbols idrFormat = new DecimalFormatSymbols(Locale.getDefault());
                        idrFormat.setDecimalSeparator(',');
                        DecimalFormat formatter = new DecimalFormat("###,###,##0", idrFormat);
//                        if (!clab.startsWith("Pembayaran PLN") && !clab.startsWith("Pembayaran Non-Taglis")) {
//                            d = d/100;
//                        }
                        d = d / 10;
                        cval = formatter.format(d);
                        cval = StringLib.strToCurr(String.valueOf(d), "Rp");
                    }

                    if (clab.startsWith("Transaksi ")) {
                        clab = clab.substring(10);
                    }
                    int ccount = cLog.getInt(cLog.getColumnIndex("jml"));
                    ssq = String.valueOf(sq);
                    sq++;
                    cmp += "{\"visible\":true,\"comp_values\":"
                            + "{\"comp_value\":["
                            + "{\"print\":\"" + cval + "\",\n"
                            + "\"value\":\"" + cval + "\"}"
                            + "]},\"comp_lbl\":\"" + clab + " :" + String.valueOf(ccount) + "\",\"comp_type\":\"1\","
                            + "\"comp_id\":\"R00001\",\"seq\":" + ssq + "}";
                } while (cLog.moveToNext());
                ssq = String.valueOf(sq);
                cmp += ",";
                cmp += "{\"visible\":true,\"comp_values\":"
                        + "{\"comp_value\":["
                        + "{\"print\":\"----------------------------------------\",\n"
                        + "\"value\":\"----------------------------------------\"}"
                        + "]},\"comp_lbl\":\"\",\"comp_type\":\"1\","
                        + "\"comp_id\":\"R00001\",\"seq\":" + ssq + "}";
                sq++;
                ssq = String.valueOf(sq);
                nGrand = StringLib.strToCurr(nGrand.replaceAll("[,.]", ""), "Rp");
                cmp += ",";
                cmp += "{\"visible\":true,\"comp_values\":"
                        + "{\"comp_value\":["
                        + "{\"print\":\"[B]" + nGrand + "\",\n"
                        + "\"value\":\"[B]" + nGrand + "\"}"
                        + "]},\"comp_lbl\":\"[B]GRAND TOTAL :" + jGrand + "\",\"comp_type\":\"1\","
                        + "\"comp_id\":\"R00001\",\"seq\":" + ssq + "}";
                sq++;
                ssq = String.valueOf(sq);
                cmp += ",";
                cmp += "{\"visible\":true,\"comp_values\":"
                        + "{\"comp_value\":["
                        + "{\"print\":\"----------------------------------------\",\n"
                        + "\"value\":\"----------------------------------------\"}"
                        + "]},\"comp_lbl\":\"\",\"comp_type\":\"1\","
                        + "\"comp_id\":\"R00001\",\"seq\":" + ssq + "}";
                if (cLog != null) {
                    cLog.close();
                }

                rps = new JSONObject("{\"screen\":{\"ver\":\"1.5\",\"print\":\"1\",\"print_text\":\"RPT" + tgl
                        + "\",\"comps\":{\"comp\":["
                        + cmp
                        + "]},\"id\":\"RS00001\",\n" +
                        "\"type\":\"1\",\"title\":\"Summary Report\"}}");
            } else {
                if (cLog != null) {
                    cLog.close();
                }
                try {
                    rps = new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"Data transaksi tidak ditemukan\",\n" +
                            "\"value\":\"Data transaksi tidak ditemukan\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                            "\"type\":\"3\",\"title\":\"Gagal\"}}");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rps;
    }

    private JSONObject handleVoid(String stanvoid) {
        if (helperDb == null) {
            helperDb = new DataBaseHelper(context);
        }
        SQLiteDatabase clientDB = null;
        JSONObject rps = new JSONObject();
        String logText = "void [" + stanvoid + "]";
        try {
            Log.d("CEK ARYO", "7");
            rps = new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"Data transaksi tidak ditemukan\",\n" +
                    "\"value\":\"Data transaksi tidak ditemukan\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                    "\"type\":\"3\",\"title\":\"Gagal\"}}");
            helperDb.openDataBase();
            clientDB = helperDb.getActiveDatabase();
            String qLog = "select * from edc_log  "
                    + "where stan = '" + stanvoid + "' "
                    + "and reversed = 0 and settled = 0 "
                    + "order by log_id desc";
            Cursor cLog = clientDB.rawQuery(qLog, null);
            if (cLog.moveToFirst()) {
                String msgId = cLog.getString(cLog.getColumnIndex("messageid"));
                messageId = cLog.getString(cLog.getColumnIndex("messageid"));
                String dttx = "";
                String dtm = cLog.getString(cLog.getColumnIndex("rqtime"));
                if (dtm != null) {
                    if (dtm.length() > 10) {
                        dtm = dtm.substring(0, 10);
                        dtm = dtm.replaceAll("-", "");
                        dttx = dtm;
                    }
                }
                if (cLog != null) {
                    cLog.close();
                }
                logText += ", db found";
                rps = JsonCompHandler.loadJsonMessage(context, msgId, "rp", dttx);
                rps.put("void", 1);
                rps.put("rstan", stanvoid);
                if (clientDB.isOpen()) {
                    clientDB.close();
                }
            } else {
                logText += ", db not found";
                if (cLog != null) {
                    cLog.close();
                }
                if (clientDB.isOpen()) {
                    clientDB.close();
                }
                Log.d("CEK ARYO", "8");
                rps = new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"Data transaksi tidak ditemukan\",\n" +
                        "\"value\":\"Data transaksi tidak ditemukan\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                        "\"type\":\"3\",\"title\":\"Gagal\"}}");
            }
        } catch (Exception ex) {
            Log.i("TX", "DB error");
            ex.printStackTrace();
            logText += ", excptn";
        }
        if (logText != null) {
            Log.i("VLG", logText);
            Log.i("VDS", rps.toString());
        }
//        Toast.makeText(context, logText, Toast.LENGTH_LONG).show();
        return rps;
    }

    private JSONArray handleSettlement() {
        if (helperDb == null) {
            helperDb = new DataBaseHelper(context);
        }
        SQLiteDatabase clientDB = null;
        JSONArray rps = new JSONArray();
        String logText = "Settlement";
        try {
            helperDb.openDataBase();
            clientDB = helperDb.getActiveDatabase();
            String qLog = "select * from edc_log "
                    + "where reversed is not null and settled <> 1 and stan <> '000000' "
                    + "order by log_id desc";
            Cursor cLog = clientDB.rawQuery(qLog, null);
            if (cLog.moveToFirst()) {
                JSONObject row = new JSONObject();
                String msgId = cLog.getString(cLog.getColumnIndex("messageid"));
                String dttx = "";
                String dtm = cLog.getString(cLog.getColumnIndex("rqtime"));
                String stan = cLog.getString(cLog.getColumnIndex("stan"));
                row.put("msgId", msgId);
                row.put("dtm", dtm);
                row.put("stan", stan);
                rps.put(row);
                while (cLog.moveToNext()) {
                    row = new JSONObject();
                    msgId = cLog.getString(cLog.getColumnIndex("messageid"));
                    dttx = "";
                    dtm = cLog.getString(cLog.getColumnIndex("rqtime"));
                    stan = cLog.getString(cLog.getColumnIndex("stan"));
                    row.put("msgId", msgId);
                    row.put("dtm", dtm);
                    row.put("stan", stan);
                    rps.put(row);
                }
            }
        } catch (Exception ex) {
            Log.i("TX", "DB error");
            ex.printStackTrace();
            logText += ", excptn";
        }
        if (logText != null) {
            Log.i("VLG", logText);
            Log.i("VDS", rps.toString());
        }
//        Toast.makeText(context, logText, Toast.LENGTH_LONG).show();
        return rps;

    }

    private void saveEdcLog(String serviceId, String msgId) {
        //save per request
        //serviceId, msgId
        if (helperDb == null) {
            helperDb = new DataBaseHelper(context);
        }
        SQLiteDatabase clientDB = null;
        String logText = "Save log";
        try {
            helperDb.openDataBase();
            clientDB = helperDb.getActiveDatabase();
            int logid = 0;
            logText += ", try get log seq";
            String qry = "select max(log_id) from edc_log ";
            Cursor mxid = clientDB.rawQuery(qry, null);
            if (mxid.moveToFirst()) {
                if (mxid != null) {
                    logid = mxid.getInt(0) + 1;
                } else {
                    logid = 1;
                }
            } else {
                logid = 1;
            }
            logText += ", found = " + logid;
            String newLog = "insert or replace into edc_log("
                    + "log_id, service_id, messageid, rqtime, settled) values "
                    + "(" + String.valueOf(logid)
                    + ",'" + serviceId + "'"
                    + ",'" + msgId
                    + "', datetime('now','local'), 0)";
            logText += ", exec " + newLog;
            clientDB.execSQL(newLog);
            logText += ", data saved";
            if (clientDB != null) {
                clientDB.close();
            }
        } catch (Exception ex) {
            if (clientDB != null) {
                clientDB.close();
            }
            Log.e("TX", "DB error");
            ex.printStackTrace();
            logText += ", data not saved";
        }
        if (logText != null) {
            Log.i("VLG", logText);
        }
//        Toast.makeText(context, logText, Toast.LENGTH_LONG).show();
    }

    private void updEdcLog(String msgId, String stan, String rc, String amount) {
        //upd on resp or error
        //msgId, stan, rc
        if (helperDb == null) {
            helperDb = new DataBaseHelper(context);
        }
        SQLiteDatabase clientDB = null;
        String logText = "Update log";
        String reversed = "null";
        if (rc != null) {
            if (!rc.equals("")) {
                reversed = "0";
            }
        }
        try {
            helperDb.openDataBase();
            clientDB = helperDb.getActiveDatabase();
            String newLog = "update edc_log set "
                    + "stan='" + stan + "', "
                    + "rqtime='" + StringLib.getYYYYMMDD() + "', "
                    + "amount='" + amount + "', "
                    + "rc='" + rc + "', "
                    + "reversed = " + reversed + " "
                    + "where messageid='" + msgId + "' ";
            Log.d("newLog", newLog);
            logText += ", exec " + newLog;
            clientDB.execSQL(newLog);
            logText += ", updated";
            if (clientDB != null) {
                clientDB.close();
            }
        } catch (Exception ex) {
            if (clientDB != null) {
                clientDB.close();
            }
            Log.e("TX", "DB error");
            ex.printStackTrace();
            logText += ", failed";
        }
        if (logText != null) {
            Log.i("VLG", logText);
        }
//        Toast.makeText(context, logText, Toast.LENGTH_LONG).show();
    }

    private void updEdcLogPan(String msgId, String pan, String tcaid) {
        //upd on resp or error
        //msgId, stan, rc
        if (helperDb == null) {
            helperDb = new DataBaseHelper(context);
        }
        SQLiteDatabase clientDB = null;
        String logText = "Update log";
        try {
            helperDb.openDataBase();
            clientDB = helperDb.getActiveDatabase();
            String newLog = "update edc_log set "
                    + "track2='" + pan + "', "
                    + "previd = '" + tcaid + "' "
                    + "where messageid='" + msgId + "' ";
            logText += ", exec " + newLog;
            clientDB.execSQL(newLog);
            logText += ", updated";
            if (clientDB != null) {
                clientDB.close();
            }
        } catch (Exception ex) {
            if (clientDB != null) {
                clientDB.close();
            }
            Log.e("TX", "DB error");
            ex.printStackTrace();
            logText += ", failed";
        }
        if (logText != null) {
            Log.i("VLG", logText);
        }
//        Toast.makeText(context, logText, Toast.LENGTH_LONG).show();
    }

    private void updVoidSukses(String stan, String vstan) {
        //upd on resp or error
        //msgId, stan, rc
        if (helperDb == null) {
            helperDb = new DataBaseHelper(context);
        }
        SQLiteDatabase clientDB = null;
        String logText = "Update log";
        try {
            helperDb.openDataBase();
            clientDB = helperDb.getActiveDatabase();
            String newLog = "update edc_log set "
                    + "reversed = 1 "
                    + "where stan in ('" + stan + "','" + vstan + "') ";
            logText += ", exec " + newLog;
            clientDB.execSQL(newLog);
            logText += ", updated";
            if (clientDB != null) {
                clientDB.close();
            }
        } catch (Exception ex) {
            if (clientDB != null) {
                clientDB.close();
            }
            Log.e("TX", "DB error");
            ex.printStackTrace();
            logText += ", failed";
        }
        if (logText != null) {
            Log.i("VLG", logText);
        }
//        Toast.makeText(context, logText, Toast.LENGTH_LONG).show();
    }

    private void updReversedSukses(String stan, String vstan) {
        //upd on resp or error
        //msgId, stan, rc
        if (helperDb == null) {
            helperDb = new DataBaseHelper(context);
        }
        SQLiteDatabase clientDB = null;
        String logText = "Update log";
        try {
            helperDb.openDataBase();
            clientDB = helperDb.getActiveDatabase();
            String newLog = "update edc_log set "
                    + "reversed = 1, rc = '', settled = 1 "
                    + "where stan in ('" + stan + "','" + vstan + "') ";
            logText += ", exec " + newLog;
            clientDB.execSQL(newLog);
            logText += ", updated";
            if (clientDB != null) {
                clientDB.close();
            }
        } catch (Exception ex) {
            if (clientDB != null) {
                clientDB.close();
            }
            Log.e("TX", "DB error");
            ex.printStackTrace();
            logText += ", failed";
        }
        if (logText != null) {
            Log.i("VLG", logText);
        }
//        Toast.makeText(context, logText, Toast.LENGTH_LONG).show();
    }

    private void updSettlementSukses() {
        //upd on resp or error
        //msgId, stan, rc
        if (helperDb == null) {
            helperDb = new DataBaseHelper(context);
        }
        SQLiteDatabase clientDB = null;
        String logText = "Update log";
        try {
            helperDb.openDataBase();
            clientDB = helperDb.getActiveDatabase();
            String newLog = "update edc_log set "
                    + "settled = 1 ";
            logText += ", exec " + newLog;
            clientDB.execSQL(newLog);
            String addBatch = "update holder set batch = case when batch == 999999 then 0 else batch+1 end ";
            clientDB.execSQL(addBatch);
            logText += ", updated";
            if (clientDB != null) {
                clientDB.close();
            }
        } catch (Exception ex) {
            if (clientDB != null) {
                clientDB.close();
            }
            Log.e("TX", "DB error");
            ex.printStackTrace();
            logText += ", failed";
        }
        if (logText != null) {
            Log.i("VLG", logText);
        }
//        Toast.makeText(context, logText, Toast.LENGTH_LONG).show();
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

    public static JSONObject decResponse(JSONObject resp) {
        try {
            String screen = (String) resp.get("screen");
            JSONObject screenObj = new JSONObject(decompress(screen));
            resp.put("screen", screenObj);
        } catch (Exception e) {
            Log.i("DEC", "Decompress failed");
        }
        return resp;
    }

    private String[] getIccDataFromComp(JSONObject screen) {
        String[] amts = {"000000000000", "000000000000", ""}; // amount, addamount, de55
        String amt = null;
        String aamt = null;
        String iccDe = null;
        Log.i("GET", "Data from comp ");
        try {
            if (parent.modulStage == CommonConfig.ICC_PROCESS_STAGE_GEN) {
                // STAGE 2 get amount
                Log.i("GET", "Data amount");
                try {
                    JSONObject comps = screen.getJSONObject("comps");
                    JSONArray comp = comps.getJSONArray("comp");
                    for (int i = 0; i < comp.length(); i++) {
                        JSONObject cmp = comp.getJSONObject(i);
                        String cid = cmp.getString("comp_id");
                        if (cid.equals("I1109")) {
                            JSONObject cvals = cmp.getJSONObject("comp_values");
                            JSONArray cval = cvals.getJSONArray("comp_value");
                            JSONObject fval = cval.getJSONObject(0);
                            amt = "000000000000" + fval.getString("value") + "00";
                            if (amt.length() > 12) {
                                amt = amt.substring(amt.length() - 12);
                            }
                        }
                        if (cid.equals("I2109")) {
                            JSONObject cvals = cmp.getJSONObject("comp_values");
                            JSONArray cval = cvals.getJSONArray("comp_value");
                            JSONObject fval = cval.getJSONObject(0);
                            aamt = "000000000000" + fval.getString("value") + "00";
                            if (aamt.length() > 12) {
                                aamt = aamt.substring(aamt.length() - 12);
                            }
                        }
                    }
                    if (amt != null) {
                        amts[0] = amt;
                    }
                    if (aamt != null) {
                        amts[1] = aamt;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (parent.modulStage == CommonConfig.ICC_PROCESS_STAGE_FINISHED) {
                // STAGE 3 get iccDe
                Log.i("GET", "Data iccde");
                try {
                    JSONObject comps = screen.getJSONObject("comps");
                    JSONArray comp = comps.getJSONArray("comp");
                    for (int i = 0; i < comp.length(); i++) {
                        JSONObject cmp = comp.getJSONObject(i);
                        String cid = cmp.getString("comp_id");
                        if (cid.equals("I1209")) {
                            JSONObject cvals = cmp.getJSONObject("comp_values");
                            JSONArray cval = cvals.getJSONArray("comp_value");
                            JSONObject fval = cval.getJSONObject(0);
                            iccDe = fval.getString("value");
                            Log.i("GET", "Result : " + iccDe);
                        }
                    }
                    if (iccDe != null) {
                        amts[2] = iccDe;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return amts;
    }

    private WindowManager.LayoutParams refreshICCProcessDialog(String messsage, boolean showAfterCreated) {
        if (alert != null) {
            if (alert.isShowing()) {
                alert.dismiss();
            }
        }
        LayoutInflater iccli = LayoutInflater.from(context);
        View iccPromptsView = iccli.inflate(R.layout.swipe_dialog, null);
        iccUIDisplay = (android.widget.TextView) iccPromptsView.findViewById(R.id.pinPass);
        iccUIDisplay.setText(messsage);
        AlertDialog.Builder iccalertDialogBuilder = new AlertDialog.Builder(context);
        // set prompts.xml to alertdialog builder
        iccalertDialogBuilder.setView(iccPromptsView);
        //alertDialogBuilder.setCancelable(false);
        // create alert dialog
        alert = iccalertDialogBuilder.create();
        WindowManager.LayoutParams icclp = new WindowManager.LayoutParams();
        icclp.copyFrom(alert.getWindow().getAttributes());
        icclp.width = WindowManager.LayoutParams.MATCH_PARENT;
        icclp.height = WindowManager.LayoutParams.MATCH_PARENT;
        // show it
        alert.setOnKeyListener(new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {

                    try {
                        if (insertICC.isOpen()) {
                            insertICC.closeDriver();
                        }
                        alert.dismiss();
                        context.onBackPressed();
                    } catch (Exception e) {
                        //failed to close, maybe already closed or not open yet
                    }
                }
                return true;
            }
        });
        if (showAfterCreated) {
            alert.show();
            alert.getWindow().setAttributes(icclp);
        }
        return icclp;
    }

    private int getSeqNum() {
        int val = 0;
        if (helperDb == null) {
            helperDb = new DataBaseHelper(context);
        }
        SQLiteDatabase clientDB = null;
        try {
            helperDb.openDataBase();
            clientDB = helperDb.getActiveDatabase();
            String qLog = "select * from holder where kid=0 ";
            Cursor cLog = clientDB.rawQuery(qLog, null);
            if (cLog.moveToFirst()) {
                val = cLog.getInt(cLog.getColumnIndex("seq"));
            } else {
                val = 1;
            }
            int newVal = val + 1;
            if (newVal > 9999) {
                newVal = 1;
            }
            String newLog = "update holder set seq=" + newVal + " where kid=0 ";
            clientDB.execSQL(newLog);
            if (clientDB != null) {
                clientDB.close();
            }

        } catch (Exception e) {
            if (clientDB != null) {
                clientDB.close();
            }
        }
        return val;
    }

    private String serviceCodeFromBin(String bin) {
        String result = bin;
        int pos = bin.indexOf('|');
        List<String> ajBinList = new ArrayList<>();
        ajBinList.add("532595");
        ajBinList.add("603605");
        ajBinList.add("502287");
        ajBinList.add("459870");
        ajBinList.add("470585");
        if (bin.startsWith("628173")) { //internal bin
            if (pos > 0) {
                result = result.substring(0, pos) + "|01001" + result.substring(pos);
            } else {
                result += "|01001";
            }
        } else if (ajBinList.contains(bin.substring(0, 6))) {
            // Artajasa
            if (pos > 0) {
                result = result.substring(0, pos) + "|01002" + result.substring(pos);
            } else {
                result += "|01002";
            }
        } else {
            // Rintis
            if (pos > 0) {
                result = result.substring(0, pos) + "|01003" + result.substring(pos);
            } else {
                result += "|01003";
            }
        }
        return result;
    }

    private int hardValidation() {
        int val = 0;
        double minTxAmt = 1.0;
        double maxTxAmt = 25000000.0;
        String amt = "";
        try {
            for (int i = 0; i < baseLayout.getChildCount(); i++) {
                View v = baseLayout.getChildAt(i);
                if (v instanceof EditText) {
                    String hint = (String) ((EditText) v).getHint();
                    if (hint.contains("Nominal")) {
                        amt = String.valueOf(((EditText) v).getText());
                        amt = "000000000000" + amt + "00";
                        amt = amt.substring(amt.length() - 12);
                        if (amt.matches("\\d+")) {
                            double d = Double.parseDouble(amt);
                            if (d < minTxAmt) {
                                if (!formId.equals("MA00080") && !formId.equals("MA00082")){
                                    val = -1;
                                } else {
                                    val = 1;
                                }
//                        } else if (d>maxTxAmt) {
//                            val = -2;
                            } else {
                                val = 1;
                            }
                        } else {
                            val = -3;
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return val;
    }

    public String panFromTrack2(String track2) {
        String tempStr = track2.replaceAll("[^\\d.]", "=");
        if (tempStr.contains("=")) {
            return tempStr.substring(0, tempStr.indexOf("="));
        } else {
            return tempStr;
        }

    }

}

