package id.co.tornado.billiton;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import id.co.tornado.billiton.common.CommonConfig;
import id.co.tornado.billiton.customview.HelveticaTextView;
import id.co.tornado.billiton.handler.DataBaseHelper;
import id.co.tornado.billiton.handler.JsonCompHandler;

/**
 * Created by indra on 24/11/15.
 */
public class SplashScreen extends Activity {

    private HelveticaTextView txtConnecting;
    private boolean DEBUG_MODE = CommonConfig.DEBUG_MODE;
    private SocketService socketService;
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
    private String json;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent.hasExtra("restart")) {
            boolean doRestart = intent.getBooleanExtra("restart", false);
            if (doRestart) {
                restart(this, 100);
            }
        }

        //GET INTENT FROM SELADA APPSSSSSSSSSSSSS
        if (intent.getStringExtra("menu") != null){
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
            } catch (Exception e){
                e.printStackTrace();
            }

            try {
                storeName = intent.getStringExtra("storeName");
            } catch (Exception e){e.printStackTrace();}

            try {
                json = intent.getStringExtra("json");
            } catch (Exception e){e.printStackTrace();}
        }

        SharedPreferences preferences = SplashScreen.this.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
//        Log.d("DEVICE", CommonConfig.getDeviceName());
        DEBUG_MODE = preferences.getBoolean("debug_mode",DEBUG_MODE);
        Log.i("OP", "Billiton EDC");
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        TelephonyManager telemamanger = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        @SuppressLint("HardwareIds") String getSimNumber = telemamanger.getSimSerialNumber();
        if (getSimNumber != null && !getSimNumber.isEmpty()){
            getSimNumber = getSimNumber.substring(2, 18);
        }
        preferences.edit().putString("sim_number", getSimNumber).apply();

        String version = pInfo.versionName;


        Log.i("OP", "Version : " + version);
        Log.i("OP", "");
        Log.i("OP", "©TORNADO 2016");
        Log.i("OP", "---------------------------");
        Log.i("OP", "S/N : " + Build.SERIAL);
        Log.i("OP", "SIM Number : " + preferences.getString("sim_number", CommonConfig.INIT_SIM_NUMBER));
        Log.i("OP", "Application start");
        setContentView(R.layout.splash_screen);
        if (!PreferenceManager.getDefaultSharedPreferences(
                getApplicationContext())
                .getBoolean("installed", false)) {
            PreferenceManager.getDefaultSharedPreferences(
                    getApplicationContext())
                    .edit().putBoolean("installed", true).commit();
            if (!DEBUG_MODE) {
                copyAssetFolder(getAssets(), "files",
                        "/data/data/"+getPackageName()+"/files");
            }
        }
        txtConnecting = (HelveticaTextView) findViewById(R.id.txtConnecting);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        DataBaseHelper helperDb = new DataBaseHelper(this.getApplicationContext());
        try {
            helperDb.createDataBase();
        } catch (IOException e) {
            Log.e("DB", "Cannot access database : " + e.getMessage());
//            e.printStackTrace();
        } finally {
            helperDb.close();
            helperDb = null;
        }
        startService(new Intent(this,SocketService.class));
        recheck();
    }

    private static boolean copyAssetFolder(AssetManager assetManager,
                                           String fromAssetPath, String toPath) {
        try {
            String[] files = assetManager.list(fromAssetPath);
            new File(toPath).mkdirs();
            boolean res = true;
            for (String file : files)
                if (file.contains("."))
                    res &= copyAsset(assetManager,
                            fromAssetPath + "/" + file,
                            toPath + "/" + file);
                else
                    res &= copyAssetFolder(assetManager,
                            fromAssetPath + "/" + file,
                            toPath + "/" + file);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean copyAsset(AssetManager assetManager,
                                     String fromAssetPath, String toPath) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(fromAssetPath);
            new File(toPath).createNewFile();
            out = new FileOutputStream(toPath);
            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    private void recheck() {
        if (DEBUG_MODE) {
            txtConnecting.setText("Synchronization..");
            final Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    SharedPreferences preferences = SplashScreen.this.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
                    //inject init preferences values
                    String hostname = preferences.getString("hostname", CommonConfig.HTTP_REST_URL);
                    String postpath = preferences.getString("postpath", CommonConfig.POST_PATH);
                    String sockethost = preferences.getString("sockethost", CommonConfig.WEBSOCKET_URL);
                    String initScreen = preferences.getString("init_screen",CommonConfig.INIT_REST_ACT);
                    String diskonId = preferences.getString("diskon_id",CommonConfig.DEFAULT_DISCOUNT_TYPE);
                    String terminalId = preferences.getString("terminal_id",CommonConfig.DEV_TERMINAL_ID);
                    String merchantId = preferences.getString("merchant_id",CommonConfig.DEV_MERCHANT_ID);
                    String merchantName = preferences.getString("merchant_name",CommonConfig.INIT_MERCHANT_NAME);
                    String merchantAddr1 = preferences.getString("merchant_address1",CommonConfig.INIT_MERCHANT_ADDRESS1);
                    String merchantAddr2 = preferences.getString("merchant_address2",CommonConfig.INIT_MERCHANT_ADDRESS2);
                    preferences.edit().putString("hostname", hostname).apply();
                    preferences.edit().putString("postpath", postpath).apply();
                    preferences.edit().putString("sockethost", sockethost).apply();
                    preferences.edit().putString("init_screen",initScreen).apply();
                    preferences.edit().putString("diskon_id",diskonId).apply();
                    preferences.edit().putString("terminal_id",terminalId).apply();
                    preferences.edit().putString("merchant_id",merchantId).apply();
                    preferences.edit().putString("merchant_name",merchantName).apply();
                    preferences.edit().putString("merchant_address1",merchantAddr1).apply();
                    preferences.edit().putString("merchant_address2",merchantAddr2).apply();
                    //end of inject
                    try {
                        JSONObject exMenu = JsonCompHandler.readJsonFromCacheIfAvailable(
                                SplashScreen.this,
                                preferences.getString("init_screen", CommonConfig.INIT_REST_ACT)
                                );
                        JSONObject rMenu = JsonCompHandler.readJsonFromUrl(
                                preferences.getString("init_screen", CommonConfig.INIT_REST_ACT),
                                SplashScreen.this);
                        if (!exMenu.getString("ver").equals(rMenu.getString("ver"))) {
                            String path = "/data/data/"+getApplicationContext().getPackageName()+"/files";
//                            Log.d("Files", "Path: " + path);
                            File directory = new File(path);
                            File[] files = directory.listFiles();
//                            Log.d("Files", "Size: "+ files.length);
                            for (int i = 0; i < files.length; i++)
                            {
//                                Log.d("Files", "FileName:" + files[i].getName());
                                if (files[i].delete()) {
                                    Log.d("DEL", files[i].getName());
                                } else {
                                    Log.d("PER", files[i].getName());
                                }
                            }                        }
//                        JsonCompHandler.readJson(SplashScreen.this, preferences.getString("init_screen", "0000000"));
//                        Log.d("Data check", "Ready to go");
                    } catch (IOException e) {
//                        Log.d("Data check", "Not Initialized");
                        txtConnecting.setText("Sychronization..");
                        try {
//                            JsonCompHandler.checkVerNoTms(SplashScreen.this.getApplication());
                            JSONObject exMenu = JsonCompHandler.readJsonFromCacheIfAvailable(
                                    SplashScreen.this,
                                    preferences.getString("init_screen", CommonConfig.INIT_REST_ACT)
                            );
                            JSONObject rMenu = JsonCompHandler.readJsonFromUrl(
                                    preferences.getString("init_screen", CommonConfig.INIT_REST_ACT),
                                    SplashScreen.this);
                            if (!exMenu.getString("ver").equals(rMenu.getString("ver"))) {
                                String path = "/data/data/"+getApplicationContext().getPackageName()+"/files";
//                            Log.d("Files", "Path: " + path);
                                File directory = new File(path);
                                File[] files = directory.listFiles();
//                            Log.d("Files", "Size: "+ files.length);
                                for (int i = 0; i < files.length; i++)
                                {
//                                Log.d("Files", "FileName:" + files[i].getName());
                                    if (files[i].delete()) {
                                        Log.d("DEL", files[i].getName());
                                    } else {
                                        Log.d("PER", files[i].getName());
                                    }
                                }                        }
                        } catch (Exception ex) {
                            Log.e("INIT", "FAILED");
                        }
                    } catch (Exception e) {
//                        Log.d("Data check", "Not Initialized");
                        txtConnecting.setText("Sychronization..");
                        try {
//                            JsonCompHandler.checkVerNoTms(SplashScreen.this.getApplication());
                            JSONObject exMenu = JsonCompHandler.readJsonFromCacheIfAvailable(
                                    SplashScreen.this,
                                    preferences.getString("init_screen", CommonConfig.INIT_REST_ACT)
                            );
                            JSONObject rMenu = JsonCompHandler.readJsonFromUrl(
                                    preferences.getString("init_screen", CommonConfig.INIT_REST_ACT),
                                    SplashScreen.this);
                            if (!exMenu.getString("ver").equals(rMenu.getString("ver"))) {
                                String path = "/data/data/"+getApplicationContext().getPackageName()+"/files";
//                            Log.d("Files", "Path: " + path);
                                File directory = new File(path);
                                File[] files = directory.listFiles();
//                            Log.d("Files", "Size: "+ files.length);
                                for (int i = 0; i < files.length; i++)
                                {
//                                Log.d("Files", "FileName:" + files[i].getName());
                                    if (files[i].delete()) {
                                        Log.d("DEL", files[i].getName());
                                    } else {
                                        Log.d("PER", files[i].getName());
                                    }
                                }                        }
                        } catch (Exception ex) {
                            Log.e("INIT", "FAILED");
                        }
                    } finally {
//                        UpdateDatabase updateDatabase = new UpdateDatabase(SplashScreen.this);
//                        boolean jsonRebuild = false;
//                        jsonRebuild = updateDatabase.doUpdate();
//                        if (jsonRebuild) {
////                            txtConnecting.setText("DEBUG MODE : Rebuild from local DB");
//                            try {
//                                Log.d("INIT", "REBUILD");
//                                JsonCompHandler.jsonRebuild(SplashScreen.this.getApplication());
//                                Log.d("INIT", "REBUILD DONE");
//                            } catch (Exception ex) {
//                                Log.e("INIT", "FAILED");
//                            }
//                        }
                        Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                        intent.putExtra("menu", formId);
                        intent.putExtra("serviceId", serviceId);
                        intent.putExtra("mid", mid);
                        intent.putExtra("mobileNumber", mobileNumber);
                        intent.putExtra("nominal", nominal);
                        intent.putExtra("amount", amount);
                        intent.putExtra("margin", margin);
                        intent.putExtra("is_from_selada", is_from_selada);

                        intent.putExtra("tid", tid);
                        intent.putExtra("mids", mids);
                        intent.putExtra("mn", mn);
                        intent.putExtra("ma", ma);
                        intent.putExtra("ct", ct);
                        intent.putExtra("sid", sid);

                        intent.putExtra("storeName", storeName);

                        if (json != null) {
                            intent.putExtra("json", json);
                        }

                        if (stan != null){
                            intent.putExtra("stan", stan);
                        }
                        startActivity(intent);
                    }
                }
            });
            thread.start();
        } else {
            Intent intent = new Intent(SplashScreen.this, MainActivity.class);
            intent.putExtra("menu", formId);
            intent.putExtra("serviceId", serviceId);
            intent.putExtra("mid", mid);
            intent.putExtra("mobileNumber", mobileNumber);
            intent.putExtra("nominal", nominal);
            intent.putExtra("amount", amount);
            intent.putExtra("margin", margin);
            intent.putExtra("is_from_selada", is_from_selada);


            intent.putExtra("tid", tid);
            intent.putExtra("mids", mids);
            intent.putExtra("mn", mn);
            intent.putExtra("ma", ma);
            intent.putExtra("ct", ct);
            intent.putExtra("sid", sid);

            intent.putExtra("storeName", storeName);

            if (json != null) {
                intent.putExtra("json", json);
            }

            if (stan != null){
                intent.putExtra("stan", stan);
            }
            startActivity(intent);
        }
    }

    public class debloat extends AsyncTask<Integer, Void, Boolean> {

        public debloat() {};

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                //disable bloatware that has notification
                //not working in production???
                Runtime.getRuntime().exec("pm disable com.wizarpos.wizarviewagentassistant");
                Runtime.getRuntime().exec("pm disable com.wizarpos.wizarviewagent");
                Runtime.getRuntime().exec("pm disable com.wizarpos.log.server");
            } catch (Exception e) {

            }
            return null;
        }


    }

    public void restart(final Context context, int delay) {
        if (delay == 0) {
            delay = 1;
        }

        Log.e("", "restarting app");
        Intent restartIntent = context.getPackageManager()
                .getLaunchIntentForPackage(context.getPackageName() );
        restartIntent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent intent = PendingIntent.getActivity(
                context, 0,
                restartIntent,0);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.set(AlarmManager.RTC, System.currentTimeMillis() + delay, intent);
        NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancelAll();

        Process.killProcess(Process.myPid());
    }


}