package id.co.tornado.billiton;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rey.material.app.ThemeManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import id.co.tornado.billiton.common.CommonConfig;
import id.co.tornado.billiton.handler.JsonCompHandler;
import id.co.tornado.billiton.handler.MenuListResolver;
import id.co.tornado.billiton.layout.FormMenu;
import id.co.tornado.billiton.layout.ListMenu;

public class MainActivity extends Activity implements KeyEvent.Callback {
    //    private MagneticSwipe swipe = new MagneticSwipe();
    private LinearLayout linearLayout;
    private String id = "";
    private MenuListResolver mlr = new MenuListResolver();
    private SharedPreferences preferences;
    private String codeHolder = "";
    private boolean showSetting = false;
    private boolean showViewer = false;
    private JSONObject currentScreen;
    private TextView txFcopy;
    private boolean KEY_DEBUG = false;
    private String MENU_TARIK_TUNAI = "MA00015";
    private String MENU_SETOR_TUNAI = "MA00040";
    private String MENU_REPORT_TRANSAKSI = "RMA0010";
    private String MENU_MINI_BANKING = "S000025";
    private String MENU_INFO_SALDO = "MA00065";
    private String PURCHASE_SELADA = "MB82560";
    private String PURCHASE_BJB = "MB82510";
    private String MENU_PUCHASE = PURCHASE_SELADA;
    private String formId = "", amountFromSelada = "";
    private String serviceId = "";
    private String mid = "";
    private String mobileNumber = "";
    private String nominal = "";
    private String amount = "";
    private String margin = "";
    private String is_from_selada = "";
    private String tid = "";
    private String mids = "";
    private String mn = "";
    private String ma = "";
    private String ct = "";
    private String sid = "";
    private String storeName = "";
    private String stan;
    private String json = "";
    private boolean isKill = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ThemeManager.init(this, 1, 0, null);
        setContentView(R.layout.activity_main);

        //Disable bluetooth
//        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        if (mBluetoothAdapter.isEnabled()) {
//            mBluetoothAdapter.disable();
//        }

        showSetting = false;
        showViewer = false;
        currentScreen = new JSONObject();
        preferences = this.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);

//        Thread thread = new Thread(new VersionChecker(version));
//        thread.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        TextView txTid = (TextView) findViewById(R.id.textViewTID);
        TextView txMid = (TextView ) findViewById(R.id.textViewMID);
        TextView txMName = (TextView ) findViewById(R.id.textViewMName);
        TextView txM2Name = (TextView) findViewById(R.id.textView4);
        txFcopy = (TextView ) findViewById(R.id.textViewCopy);
        TextView txFsn = (TextView ) findViewById(R.id.textViewSN);
        TextView txFsv = (TextView ) findViewById(R.id.textViewSV);

        if (txTid == null){
            Log.d("ERROR", "LAYOUT NOT INFLATED");
        }

        try {
            txTid.setText("TID : " + preferences.getString("terminal_id", CommonConfig.DEV_TERMINAL_ID));
            txMid.setText("MID : " + preferences.getString("merchant_id", CommonConfig.DEV_MERCHANT_ID));
            txMName.setText(preferences.getString("merchant_address1", CommonConfig.INIT_MERCHANT_ADDRESS1));
            txM2Name.setText(preferences.getString("merchant_name", CommonConfig.INIT_MERCHANT_NAME));
        }
        catch (Exception e){
            e.printStackTrace();
        }

        SimpleDateFormat ydf = new SimpleDateFormat("yyyy");
        Date now = new Date();
        if (now != null){
            String year = ydf.format(now);
            PackageInfo pInfo = null;
            try {
                pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                String version = pInfo.versionName;

                txFcopy.setText("\u00a9 BANK BJB " + year + ", v" + version+ ", " + "141020");
            } catch (Exception e) {
                e.printStackTrace();
                txFcopy.setText("\u00a9 BANK BJB " + year + ", " + "141020");
            }
        }
        else{
            txFcopy.setText("\u00a9 BANK BJB, v" + "0.1"+ ", " + "141020");
        }

        String serialNum = Build.SERIAL;
        txFsn.setText(serialNum);
        txFsv.setText(preferences.getString("sim_number", CommonConfig.INIT_SIM_NUMBER));

//        txFcopy.setText("\u00a9 BANK BJB " + year);
//        String serialNum = Build.SERIAL;
//        txFsn.setText(serialNum);
//        txFsv.setText("v"+version);

        linearLayout = (LinearLayout) findViewById(R.id.base_layout);

        //GET INTENT FROM SELADA APP
        Intent intent = getIntent();
        if (getIntent().getStringExtra("menu") != null){
            formId = intent.getStringExtra("menu");
            try {
                serviceId = intent.getStringExtra("serviceId");
                mid = intent.getStringExtra("mid");
                mobileNumber = intent.getStringExtra("mobileNumber");
                nominal = intent.getStringExtra("nominal");
                amount = intent.getStringExtra("amount");
                margin = intent.getStringExtra("margin");
            } catch (Exception e){
                e.printStackTrace();
            }
            try {
                is_from_selada = intent.getStringExtra("is_from_selada");
            } catch (Exception e){
                e.printStackTrace();
            }

            try {
                tid = intent.getStringExtra("tid");
                mids = intent.getStringExtra("mids");
                mn = intent.getStringExtra("mn");
                ma = intent.getStringExtra("ma");
                ct = intent.getStringExtra("ct");
                sid = intent.getStringExtra("sid");
            } catch (Exception e){e.printStackTrace();}

            try {
                stan = intent.getStringExtra("stan");
            } catch (Exception e){}

            try {
                json = intent.getStringExtra("json");
            } catch (Exception e){}

            try {
                storeName = intent.getStringExtra("storeName");
            } catch (Exception e){}

            // try {
            //     amountFromSelada = intent.getStringExtra("nominal");
            // }catch (Exception e){}

            if (formId.equals("setting")) {
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.boardinglabs.mireta.selada");
                if (tid != null && !tid.equals("")) preferences.edit().putString("terminal_id",tid).apply();
                if (mids != null && !mids.equals("")) preferences.edit().putString("merchant_id",mids).apply();
                if (mn != null && !mn.equals("")) preferences.edit().putString("merchant_name",mn).apply();
                if (ma != null && !ma.equals("")) preferences.edit().putString("merchant_address1",ma).apply();
                if (ct != null && !ct.equals("")) preferences.edit().putString("merchant_address2",ct).apply();
                if (sid != null && sid.equals("")) {
                    preferences.edit().putString("init_screen",sid).apply();
                    String[] screenFiles = getApplicationContext().fileList();
                    for (int i = 0; i < screenFiles.length; i++) {
                        getApplicationContext().deleteFile(screenFiles[i]);
                    }
                }

                //set IP for BJB Selada
                preferences.edit().putString("hostname", CommonConfig.HTTP_REST_URL).apply();
                preferences.edit().putString("sockethost", CommonConfig.WEBSOCKET_URL).apply();

                Bundle bundle = new Bundle();
                bundle.putString("TID", preferences.getString("terminal_id",CommonConfig.DEV_TERMINAL_ID));
                bundle.putString("MID", preferences.getString("merchant_id",CommonConfig.DEV_MERCHANT_ID));
                bundle.putString("MerchantName", preferences.getString("merchant_name",CommonConfig.INIT_MERCHANT_NAME));
                bundle.putString("MerchantAddress", preferences.getString("merchant_address1",CommonConfig.INIT_MERCHANT_ADDRESS1));
                if (launchIntent != null) {
                    launchIntent.putExtras(bundle);
                    startActivity(launchIntent);//null pointer check in case package name was not found
                    finish();
                }
            }

            if (formId.equals("profil")) {
                if (storeName != null && !storeName.equals("")) preferences.edit().putString("store_name",storeName).apply();
                isKill = true;
                Log.d("INTENT PROFIL", "MASUK");
                finishAffinity();
                return;
            }
        }

        //KILL APP FROM POPUP GAGAL
        Intent intents = getIntent();
        if (intents.getStringExtra("kill") != null){
            isKill = true;
            Log.d("INTENT KILL", "MASUK");
            finishAffinity();
            return;
        }

        try {

            //GET SCREEN FROM INTENT SELADA
            if (formId != null){
                if (formId.equals(MENU_TARIK_TUNAI) || formId.equals(MENU_SETOR_TUNAI) || formId.equals(MENU_REPORT_TRANSAKSI)
                        || formId.equals(MENU_MINI_BANKING) || formId.equals(MENU_INFO_SALDO)) {
                    Log.i("Set Menu", formId);
                    if (!isKill){
                        currentScreen = JsonCompHandler
                                .readJsonFromIntent(formId, this);
                        Log.d("JSON", currentScreen.toString());
                        setMenu(currentScreen);
                    }
                }
                else if (formId.equals(PURCHASE_SELADA)){
                    JSONObject jsonObject = new JSONObject("{\n" +
                            "    \"action_url\": \"E82560\",\n" +
                            "    \"ver\": \"1\",\n" +
                            "    \"print\": null,\n" +
                            "    \"comps\": {\n" +
                            "      \"comp\": [\n" +
                            "        {\n" +
                            "          \"visible\": false,\n" +
                            "          \"comp_lbl\": \"ICC Insert Tx\",\n" +
                            "          \"comp_type\": \"9\",\n" +
                            "          \"comp_id\": \"I0209\",\n" +
                            "          \"seq\": 0\n" +
                            "        },\n" +
                            "        {\n" +
                            "          \"visible\": true,\n" +
                            "          \"comp_lbl\": \"PIN\",\n" +
                            "          \"comp_type\": \"3\",\n" +
                            "          \"comp_id\": \"I0001\",\n" +
                            "          \"comp_opt\": \"102006006\",\n" +
                            "          \"seq\": 1\n" +
                            "        },\n" +
                            "        {\n" +
                            "          \"visible\": true,\n" +
                            "          \"comp_lbl\": \"Proses\",\n" +
                            "          \"comp_type\": \"7\",\n" +
                            "          \"comp_id\": \"G0001\",\n" +
                            "          \"seq\": 2\n" +
                            "        }\n" +
                            "      ]\n" +
                            "    },\n" +
                            "    \"static_menu\": [\n" +
                            "      \"Purchase\"\n" +
                            "    ],\n" +
                            "    \"print_text\": \"IPOP\",\n" +
                            "    \"id\": \"MB82560\",\n" +
                            "    \"type\": \"1\",\n" +
                            "    \"title\": \"Purchase\"\n" +
                            "  }");
                    setMenu(jsonObject);
                }
                else if (formId.equals(PURCHASE_BJB)){
                    JSONObject jsonObject = new JSONObject("{\n" +
                            "    \"action_url\": \"E82510\",\n" +
                            "    \"ver\": \"1\",\n" +
                            "    \"print\": null,\n" +
                            "    \"comps\": {\n" +
                            "      \"comp\": [\n" +
                            "        {\n" +
                            "          \"visible\": false,\n" +
                            "          \"comp_lbl\": \"ICC Insert Tx\",\n" +
                            "          \"comp_type\": \"9\",\n" +
                            "          \"comp_id\": \"I0209\",\n" +
                            "          \"seq\": 0\n" +
                            "        },\n" +
                            "        {\n" +
                            "          \"visible\": true,\n" +
                            "          \"comp_lbl\": \"PIN\",\n" +
                            "          \"comp_type\": \"3\",\n" +
                            "          \"comp_id\": \"I0001\",\n" +
                            "          \"comp_opt\": \"102006006\",\n" +
                            "          \"seq\": 1\n" +
                            "        },\n" +
                            "        {\n" +
                            "          \"visible\": true,\n" +
                            "          \"comp_lbl\": \"Proses\",\n" +
                            "          \"comp_type\": \"7\",\n" +
                            "          \"comp_id\": \"G0001\",\n" +
                            "          \"seq\": 2\n" +
                            "        }\n" +
                            "      ]\n" +
                            "    },\n" +
                            "    \"static_menu\": [\n" +
                            "      \"Purchase\"\n" +
                            "    ],\n" +
                            "    \"print_text\": \"IPOP\",\n" +
                            "    \"id\": \"MB82510\",\n" +
                            "    \"type\": \"1\",\n" +
                            "    \"title\": \"Purchase\"\n" +
                            "  }");
                    setMenu(jsonObject);
                }
                else if (formId.equals("ONCLICK")) {
                    // SKIP
                }
                else {
                    Log.i("Set Menu", preferences.getString("init_screen", CommonConfig.INIT_REST_ACT));
//              currentScreen = JsonCompHandler.readJson(this, preferences.getString("init_screen", CommonConfig.INIT_REST_ACT));
                    currentScreen = JsonCompHandler
                            .readJsonFromCacheIfAvailable(this, preferences.getString("init_screen", CommonConfig.INIT_REST_ACT))
//                      .readJsonFromUrl(preferences.getString("init_screen", CommonConfig.INIT_REST_ACT), this)
                    ;
                    if (!currentScreen.keys().hasNext()){
                        currentScreen = JsonCompHandler
                      .readJsonFromUrl(preferences.getString("init_screen", CommonConfig.INIT_REST_ACT), this);
                    }
                    setMenu(currentScreen);
                }
            }
            else {
                Log.i("Set Menu", preferences.getString("init_screen", CommonConfig.INIT_REST_ACT));
//              currentScreen = JsonCompHandler.readJson(this, preferences.getString("init_screen", CommonConfig.INIT_REST_ACT));
                currentScreen = JsonCompHandler
                        .readJsonFromCacheIfAvailable(this, preferences.getString("init_screen", CommonConfig.INIT_REST_ACT))
//                      .readJsonFromUrl(preferences.getString("init_screen", CommonConfig.INIT_REST_ACT), this)
                ;
                setMenu(currentScreen);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showSetting = false;
        showViewer = false;
        preferences = this.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);


        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setMenu(JSONObject obj) {
        View child = null;
        Integer type = -1;
        String message = "";
//        Log.d("JSON_MENU", obj.toString());

        try {
            type = obj.getInt("type");
            id = obj.get("id").toString();

            //FORCE INTENT TO FORM MENU FROM SELADA
            if (formId.equals(MENU_TARIK_TUNAI) || formId.equals(MENU_SETOR_TUNAI)
                    || formId.equals(MENU_INFO_SALDO) || formId.equals(PURCHASE_SELADA)
                    || formId.equals(PURCHASE_BJB)|| formId.equals("REVERSALEFROMSELADA")) {
                Intent intent = new Intent(MainActivity.this, ActivityList.class);
                Bundle bundle = new Bundle();
                bundle.putString("comp_act", id);
                bundle.putString("serviceId", serviceId);
                bundle.putString("mid", mid);
                bundle.putString("mobileNumber", mobileNumber);
                bundle.putString("nominal", nominal);
                bundle.putString("amount", amount);
                bundle.putString("margin", margin);
                if (stan!=null){
                    bundle.putString("stan", stan);
                }
                if (json!=null){
                    bundle.putString("json", json);
                }
                if (is_from_selada!=null && !is_from_selada.equals("")){
                    bundle.putString("is_from_selada", is_from_selada);
                }

                intent.putExtras(bundle);
                startActivity(intent);
                return;
            }

        } catch (Exception e) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Informasi");
            alertDialog.setMessage("EDC tidak terkoneksi dengan server\nGagal mengambil data\nSilahkan coba beberapa saat lagi\n");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
//            if (id.equals("")) {
//                id = "XXXXXXX";
//            }
//            if (showSetting) {
//                id += "opt:ms=on";
//            }
//            if (showViewer) {
//                if (id.contains("opt")) {
//                    id += ",ls=on";
//                } else {
//                    id += "opt:ls=on";
//                }
//            }
            ListMenu lm = new ListMenu(this, id);
//                    LinearLayout vp = lm.getPager();
//                    child = vp;
            child = lm;
            linearLayout.removeAllViews();
            linearLayout.addView(child);
            e.printStackTrace();
            return;
        }
        if (type != -1 && !id.equals("")) {
            switch (type) {
                case CommonConfig.MenuType.Form:
                    child = new FormMenu(this, id, "", "", "", "","", "", json);

                    break;
                case CommonConfig.MenuType.ListMenu:
                    if (showSetting) {
                        id += "opt:ms=on";
                    }
                    if (showViewer) {
                        if (id.contains("opt")) {
                            id += ",ls=on";
                        } else {
                            id += "opt:ls=on";
                        }
                    }
                    ListMenu lm = new ListMenu(this, id);
//                    LinearLayout vp = lm.getPager();
//                    child = vp;
                    child = lm;
                    break;
                case CommonConfig.MenuType.PopupBerhasil:
                    break;
                case CommonConfig.MenuType.PopupGagal:
                    message = "SIM Card atau Terminal tidak terdaftar";
                    if (obj.has("comps")) {
                        JSONObject comps = null;
                        try {
                            comps = obj.getJSONObject("comps");

                            if (comps.has("comp")) {
                                JSONArray comp_array = comps.getJSONArray("comp");
                                if (comp_array.length() == 1) {
                                    JSONObject comp = comp_array.getJSONObject(0);
                                    if (comp != null && comp.has("comp_values")) {
                                        JSONArray comp_values_array = comp.getJSONArray("comp_values");
                                        if (comp_values_array.length() == 1) {
                                            JSONObject comp_value = comp_values_array.getJSONObject(0);
                                            if (comp_value != null && comp_value.has("print")) {

                                                message = comp_value.getString("print");

                                            }
                                        }
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                    alertDialog.setTitle("Informasi");
                    alertDialog.setMessage(message);
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                    return;
                case CommonConfig.MenuType.PopupLogout:
                    break;
                case CommonConfig.MenuType.SecuredForm:
                    child = new FormMenu(this, id, "", "", "", "","","",json);
                    break;
            }
            linearLayout.removeAllViews();
            linearLayout.addView(child);
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        Log.i("KEY", "Code : " + keyCode + ", Event : " + event);
        if (KEY_DEBUG) {
            txFcopy.setText("Code : " + keyCode + ", Event : " + event);
        }
        if (keyCode==232) {
            return false;
        }
        if (codeHolder.length()>=6) {
            codeHolder = "";
        }
        if (keyCode==56) {
            codeHolder = "";
        }
        if (keyCode==67) {
            if (codeHolder.length()>0) {
                codeHolder = codeHolder.substring(0, codeHolder.length() - 1);
            }
        }
        if (keyCode>6 && keyCode<17) {
            codeHolder += String.valueOf(keyCode-7);
        }
        if (codeHolder.equals(CommonConfig.getVal(CommonConfig.chKey_1))) {
            if (!showSetting) {
                Log.d("onKeyDown", "Code : " + keyCode + ", Event : " + event);
                showSetting = true;
                setMenu(currentScreen);
            }
        }
        if (codeHolder.equals(CommonConfig.getVal(CommonConfig.chKey_2))) {
            if (showSetting) {
                showSetting = false;
                setMenu(currentScreen);
            }
        }
        if (codeHolder.equals(CommonConfig.getVal(CommonConfig.chKey_3))) {
            if (!showViewer) {
                showViewer = true;
                setMenu(currentScreen);
            }
        }
        if (codeHolder.equals(CommonConfig.getVal(CommonConfig.chKey_4))) {
            if (showViewer) {
                showViewer = false;
                setMenu(currentScreen);
            }
        }
        if (keyCode==7) {
            Log.d("CD", codeHolder);
        }
        return super.onKeyDown(keyCode, event);
    }

    class VersionChecker implements Runnable {
        private String version;

        public VersionChecker(String version) {
            this.version = version;
        }
        @Override
        public void run() {
            try {
                if(!preferences.getBoolean("debug_mode",CommonConfig.DEBUG_MODE)){
                    if(JsonCompHandler.checkUpdate(MainActivity.this).getString("software").equals(version)){

                        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.cancel(1234);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
