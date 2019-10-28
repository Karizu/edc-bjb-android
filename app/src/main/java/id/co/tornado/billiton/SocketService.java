package id.co.tornado.billiton;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import id.co.tornado.billiton.common.CommonConfig;
import id.co.tornado.billiton.handler.GPSService;
import id.co.tornado.billiton.handler.WebSocketClient;

public class SocketService extends Service implements WebSocketClient.Listener {
    private static final String TAG = "WEBSOCKET";
    private IBinder mBinder;
    private boolean isConnect = false;
    private int retryConnect = 0;
    private boolean isLogin = false;
    private WebSocketClient client;
    private boolean ifForm = false;
    NotificationManager mNotificationManager;
    List<BasicNameValuePair> extraHeaders = new ArrayList<>();
    private Handler recoHandler = new Handler();
    private boolean recoJob = false;
    private int recoInterval = 180000;
    private int recoTries = 1;
    private String serialNum = Build.SERIAL;
    private boolean DEBUG_MODE = CommonConfig.DEBUG_MODE;
    private boolean warnotActive = false;
    public enum MessageMethod {
        LOGIN, LOGOUT, UPDATE_SOFTWARE, UPDATE_MENU,UPDATE_SETTINGS, MESSAGE, HEARTBEAT, PENDING_MESSAGE;
    }

    public enum MessageType {
        TOAST, NOTIFICATION, NOTIFICATION_STICKY, NOTIFICATION_TOAST, NOTIFICATION_STICKY_TOAST;
    }

    public enum MessageStatus {
        DELIVERED, FAILED, DEVICE_NOT_ACTIVE, DEVICE_NOT_REGISTERED, SEND, LOGIN_SUCCESS, LOGIN_FAILED;
    }

    private Runnable recoRun = new Runnable() {
        @Override
        public void run() {
            if (!isConnect) {
                SharedPreferences preferences = getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
                client = new WebSocketClient(URI.create("wss://" + preferences.getString("sockethost", CommonConfig.WEBSOCKET_URL) + "/devlog"), SocketService.this, extraHeaders);
                client.connect();
                recoHandler.postDelayed(this, recoInterval);
                recoJob = true;
            } else {
                recoHandler.removeCallbacks(this);
                recoJob = false;
            }
        }
    };

    private void reConnect() {
        if (!recoJob) {
            recoHandler.postDelayed(recoRun,0);
        } else {
            recoHandler.removeCallbacks(recoRun);
            recoHandler.postDelayed(recoRun, 3000 * recoTries * recoTries);
            recoTries++;
        }

    }

    public void forceReConnect() {
        if (!recoJob) {
            recoHandler.postDelayed(recoRun,0);
        } else {
            recoHandler.removeCallbacks(recoRun);
            recoHandler.postDelayed(recoRun, 3000);
            recoTries = 1;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mBinder = new LocalBinder();
        SharedPreferences preferences = getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
        DEBUG_MODE = preferences.getBoolean("debug_mode",DEBUG_MODE);
        if(!DEBUG_MODE){
            client = new WebSocketClient(URI.create("wss://" + preferences.getString("sockethost",CommonConfig.WEBSOCKET_URL) + "/devlog"), this, extraHeaders);
            if (Looper.myLooper() == null) {
                Looper.prepare();
            }
            if (!isConnect) {
                client.connect();
            }
//        Log.d(TAG, "WebSocket instance created");
        }
        final Thread gpsService = new Thread(new GPSService(this));
        gpsService.start();
//        Log.d(TAG, "GPS Service started");
    }

    @Override
    public IBinder onBind(Intent intent) {
        SharedPreferences preferences = getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
        DEBUG_MODE = preferences.getBoolean("debug_mode",DEBUG_MODE);
        if(!DEBUG_MODE) {
            if (!isConnect) {
                client.connect();
            }
        }
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public SocketService getServerInstance() {
            return SocketService.this;
        }
    }


    @Override
    public void onConnect() {
//        Log.d(TAG, "WEBSOCKET CONNECTED");
        isConnect = true;
        doLogin();
    }

    private void doLogin() {
        SharedPreferences preferences = getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
        String tid = preferences.getString("terminal_id", CommonConfig.DEV_TERMINAL_ID);
        if (tid.equals("00000000")) {
            isLogin = false;
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("type", MessageType.NOTIFICATION.name());
                jsonObject.put("message", "Silahkan lakukan setting EDC");
                showNotification(5432, jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.i("RESP","EDC Belum Disetting");
            return;
        }

        if (!isLogin) {
            JSONObject object = new JSONObject();
            final TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
            String tim = telephonyManager.getDeviceId();
            PackageInfo pInfo = null;
            try {
                pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            String version = pInfo.versionName;

            try {
                object.put("method", MessageMethod.LOGIN.name());
                object.put("tid", tid);
                object.put("tim", tim);
                object.put("snDevice", serialNum);
                object.put("timestamp", new Date().getTime());
                object.put("status", MessageStatus.SEND);
                JSONObject addInfo = new JSONObject();
                addInfo.put("version", version);
                object.put("additionalInfo", addInfo);
                Log.d("SEND_LOGIN", object.toString());
                client.send(object.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onMessage(String message) {
//        Log.d(TAG, message);
        try {
            JSONObject response = new JSONObject(message);
            MessageMethod method = MessageMethod.valueOf(response.getString("method"));
            MessageStatus status = MessageStatus.valueOf(response.getString("status"));
            switch (method) {
                case LOGIN:
                    if (status == MessageStatus.LOGIN_SUCCESS) {
                        isLogin = true;
                        warnotActive = false;
                        cancelNotif(5432);
                        cancelNotif(1234);
                        cancelNotif(123);
                        SharedPreferences preferencesSetting = getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
                        preferencesSetting.edit().putBoolean("login_state", true).apply();
                        Log.i("RESP","LOGIN SUKSES");
                    } else if (status == MessageStatus.LOGIN_FAILED) {
                        isLogin = false;
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("type", MessageType.NOTIFICATION.name());
                            jsonObject.put("message", "EDC Tidak terkoneksi dengan server");
                            showNotification(5432, jsonObject);
                            SharedPreferences preferencesSetting = getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
                            preferencesSetting.edit().putBoolean("login_state", false).apply();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.i("RESP","LOGIN GAGAL");
                    }
                    break;
                case MESSAGE:
                    showNotification(response);
                    break;
                case UPDATE_SOFTWARE:
                case UPDATE_MENU:
                    String m = response.getString("message");
                    updateNotif(m, method);
                    break;
                case HEARTBEAT:
                    if (isConnect) {
//                        client.send();
                    } else {
                        client.connect();
                        doLogin();
                    }
                    break;
                case LOGOUT:
                    isLogin = false;
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("type", MessageType.NOTIFICATION.name());
                        jsonObject.put("message", "EDC Tidak terkoneksi dengan server");
                        showNotification(5432, jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case UPDATE_SETTINGS:
                    JSONObject json = response.getJSONObject("additionalInfo");
                    updateSettings(json);
                    break;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void updateSettings(JSONObject json) throws JSONException {
        try {
            SharedPreferences preferencesSetting = getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
//            preferencesSetting.edit().putString("hostname", json.getString("hostname")).apply();
//            preferencesSetting.edit().putString("postpath", json.getString("postpath")).apply();
//            preferencesSetting.edit().putString("sockethost", json.getString("sockethost")).apply();
//            preferencesSetting.edit().putString("terminal_id", json.getString("terminalid")).apply();
            preferencesSetting.edit().putString("merchant_id", json.getString("merchantid")).apply();
            preferencesSetting.edit().putString("merchant_name", json.getString("merchantname")).apply();
            preferencesSetting.edit().putString("merchant_address1", json.getString("address1")).apply();
            preferencesSetting.edit().putString("merchant_address2", json.getString("address2")).apply();
            preferencesSetting.edit().putString("init_screen", json.getString("initscreen")).apply();
            Log.i("UPDATE", "UPDATE SUCCESSFULLY");
        } catch (Exception e) {
            Log.i("UPDATE", "UPDATE FAILED : " + e.getMessage());
        }
    }

    @Override
    public void onMessage(byte[] data) {

    }

    @Override
    public void onDisconnect(int code, String reason) {
//        Log.d(TAG, reason);
//        Toast.makeText(this, "onD" + reason, Toast.LENGTH_SHORT).show();
        isConnect = false;
        isLogin = false;
        client.disconnect();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", MessageType.NOTIFICATION.name());
            jsonObject.put("message", "EDC Tidak terkoneksi dengan server");
            showNotification(5432, jsonObject);
            SharedPreferences preferencesSetting = getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
            preferencesSetting.edit().putBoolean("login_state", false).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        reConnect();
    }

    @Override
    public void onError(Exception error) {
        String reason = error.getMessage();
        Log.e(TAG + " ERROR", reason);
//        Toast.makeText(this, "onE" + reason, Toast.LENGTH_SHORT).show();
        if (reason.contains("Connection timed out") ||
                reason.contains("Connection reset by peer") ||
                reason.contains("Network is unreachable")) {
            isConnect = false;
            isLogin = false;
            client.disconnect();
            reConnect();
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("type", MessageType.NOTIFICATION.name());
                jsonObject.put("message", "EDC Tidak terkoneksi dengan server");
                showNotification(5432, jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(SocketService.this.getApplicationContext(), msg.getData().getString("message"), Toast.LENGTH_LONG).show();
        }
    };

    private void showNotification(JSONObject message) {
        showNotification(0, message);
    }

    private void showNotification(int nid, JSONObject message) {
        try {
            MessageType type = MessageType.valueOf(message.getString("type"));
            if (mNotificationManager == null) {
                mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            }
            Notification.Builder builder = new Notification.Builder(this)
                    .setContentTitle("Informasi")
                    .setContentText(message.getString("message"))
                    .setSmallIcon(R.mipmap.logo_bjb_small)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.if_email));
            Notification n;

            if (nid==5432) {
                Intent intent = new Intent(this, SplashScreen.class);
                intent.putExtra("restart", true);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP |
                        Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
                builder.setContentIntent(pendingIntent)
                        .setContentTitle("Peringatan")
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_admin));
                isLogin = false;
                isConnect = false;
                client.disconnect();
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                n = builder.build();
            } else {
                n = builder.getNotification();
            }

            Message data = new Message();
            Bundle bundle = new Bundle();
            if (nid==0) {
                nid=12345;
            }
            switch (type) {
                case NOTIFICATION_STICKY:
                    n.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
                    mNotificationManager.notify(nid, n);
                    break;
                case NOTIFICATION_TOAST:
                    bundle.putString("message", message.getString("message"));
                    data.setData(bundle);
                    mHandler.sendMessage(data);
                    mNotificationManager.notify(nid, n);
                    break;
                case TOAST:
                    bundle.putString("message", message.getString("message"));
                    data.setData(bundle);
                    mHandler.sendMessage(data);
                    break;
                case NOTIFICATION_STICKY_TOAST:
                    n.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
                    mNotificationManager.notify(nid, n);
                    bundle.putString("message", message.getString("message"));
                    data.setData(bundle);
                    mHandler.sendMessage(data);
                    break;
                case NOTIFICATION:
                    mNotificationManager.notify(nid, n);
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateNotif(String message, MessageMethod method) {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
        Intent intent = new Intent(this, UpdateAppActivity.class);
        int what = 0;
        String title = "Update Software";
        if (method == MessageMethod.UPDATE_MENU) {
            what = 123;
            title = "Update Fitur";
        } else if (method == MessageMethod.UPDATE_SOFTWARE) {
            what = 1234;
            title = "Update Software";
        }
        intent.putExtra("method", what);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(this)
                .setContentText(message)
                .setContentTitle(title)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_autorenew_white_24dp)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_autorenew_white_24dp));
        Notification n;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            n = new Notification.BigTextStyle(builder).bigText(message).build();
        } else {
            n = builder.getNotification();
        }
        n.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        mNotificationManager.notify(what, n);
    }

    public void cancelNotif(int id) {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
        Log.i("Notif", "cancel");
        mNotificationManager.cancel(id);
        mNotificationManager.cancelAll();
    }

    public void setIfForm() {
        this.ifForm = true;
    }

    public void setIfNotForm() {
        this.ifForm = false;
    }

    public boolean getIfForm() {
        return ifForm;
    }

    public boolean isConnect() {
        if (!isConnect) {
            recoHandler.removeCallbacks(recoRun);
            recoHandler.postDelayed(recoRun, 0);
        }
        return isConnect;
    }
}
