package id.co.tornado.billiton;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.wizarpos.jni.PINPadInterface;

import id.co.tornado.billiton.common.CommonConfig;
import id.co.tornado.billiton.common.StringLib;
import id.co.tornado.billiton.handler.DataBaseHelper;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends Activity implements View.OnClickListener {
    private EditText txtHostname, txtSocketHost, txtPostPath, txtInitScreen,txtTerminalId,txtMerchantId,
            txtMerchantName,txtMerchantAddrees1,txtMerchantAddrees2;
    private CheckBox debugMode;
    private SharedPreferences preferences;
    private DataBaseHelper helperDb;
    private SQLiteDatabase clientDB = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        debugMode = (CheckBox) findViewById(R.id.debug_mode);
        txtHostname = (EditText) findViewById(R.id.txtHostname);
        txtSocketHost = (EditText) findViewById(R.id.txtSocketHost);
        txtPostPath = (EditText) findViewById(R.id.txtPostPath);
        txtInitScreen = (EditText) findViewById(R.id.txtInitScreen);
        txtTerminalId = (EditText) findViewById(R.id.txtTerminalId);
        txtMerchantId = (EditText) findViewById(R.id.txtMerchantId);
        txtMerchantName = (EditText) findViewById(R.id.txtMerchantName);
        txtMerchantAddrees1 = (EditText) findViewById(R.id.txtMerchantAddress1);
        txtMerchantAddrees2 = (EditText) findViewById(R.id.txtMerchantAddress2);
        Button btnSimpan = (Button) findViewById(R.id.btnSimpan);
        btnSimpan.setOnClickListener(this);
        btnSimpan.setTag(0);
        Button btnMKey = (Button) findViewById(R.id.btnMKey);
        btnMKey.setOnClickListener(this);
        btnMKey.setTag(1);
        Button btnWKey = (Button) findViewById(R.id.btnWKey);
        btnWKey.setOnClickListener(this);
        btnWKey.setTag(2);
        Button btnCsc = (Button) findViewById(R.id.btnClrScrCache);
        btnCsc.setOnClickListener(this);
        btnCsc.setTag(4);
        helperDb = new DataBaseHelper(this.getApplicationContext());
        try {
            helperDb.openDataBase();
            clientDB = helperDb.getActiveDatabase();
        } catch (Exception ex) {
            Toast.makeText(this,"Could not open key holder",Toast.LENGTH_LONG).show();
            btnMKey.setClickable(false);
        }
        preferences  = this.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
        String hostname = preferences.getString("hostname", CommonConfig.HTTP_REST_URL);
        String postpath = preferences.getString("postpath", CommonConfig.POST_PATH);
        String sockethost = preferences.getString("sockethost", CommonConfig.WEBSOCKET_URL);
        String initScreen = preferences.getString("init_screen",CommonConfig.INIT_REST_ACT);
        String terminalId = preferences.getString("terminal_id",CommonConfig.DEV_TERMINAL_ID);
        String merchantId = preferences.getString("merchant_id",CommonConfig.DEV_MERCHANT_ID);
        String merchantName = preferences.getString("merchant_name",CommonConfig.INIT_MERCHANT_NAME);
        String merchantAddr1 = preferences.getString("merchant_address1",CommonConfig.INIT_MERCHANT_ADDRESS1);
        String merchantAddr2 = preferences.getString("merchant_address2",CommonConfig.INIT_MERCHANT_ADDRESS2);
        boolean isDebug = preferences.getBoolean("debug_mode",CommonConfig.DEBUG_MODE);
        txtHostname.setText(hostname);
        txtSocketHost.setText(sockethost);
        txtPostPath.setText(postpath);
        txtInitScreen.setText(initScreen);
        txtTerminalId.setText(terminalId);
        txtMerchantId.setText(merchantId);
        txtMerchantName.setText(merchantName);
        txtMerchantAddrees1.setText(merchantAddr1);
        txtMerchantAddrees2.setText(merchantAddr2);
        debugMode.setChecked(isDebug);
    }

    @Override
    public void onClick(View v) {
        Button callerButton = (Button) v;
        int bTag = (int) v.getTag();
        switch (bTag) {
            case 0 :
                String hostname = txtHostname.getText().toString();
                String sockethost = txtSocketHost.getText().toString();
                String postpath = txtPostPath.getText().toString();
                String initScreen = txtInitScreen.getText().toString();
                String terminalId = txtTerminalId.getText().toString();
                String merchantId = txtMerchantId.getText().toString();
                String merchantName = txtMerchantName.getText().toString();
                String merchantAddress1 = txtMerchantAddrees1.getText().toString();
                String merchantAddress2 = txtMerchantAddrees2.getText().toString();
                boolean isDebug = debugMode.isChecked();
                preferences.edit().putString("hostname", hostname).apply();
                preferences.edit().putString("sockethost", sockethost).apply();
                preferences.edit().putString("postpath", postpath).apply();
                preferences.edit().putString("init_screen",initScreen).apply();
                preferences.edit().putString("terminal_id",terminalId).apply();
                preferences.edit().putString("merchant_id",merchantId).apply();
                preferences.edit().putString("merchant_name",merchantName).apply();
                preferences.edit().putString("merchant_address1",merchantAddress1).apply();
                preferences.edit().putString("merchant_address2",merchantAddress2).apply();
                preferences.edit().putBoolean("debug_mode", isDebug).apply();
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        AdminActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AdminActivity.this,"Data berhasil disimpan",Toast.LENGTH_LONG).show();
                            }
                        });
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        restart(AdminActivity.this,100);
                    }
                });
                ((Button)findViewById(R.id.btnSimpan)).setText("Processing");
                ((Button)findViewById(R.id.btnSimpan)).setEnabled(false);
                t.start();
                break;
            case 1 :
                final Dialog updateKey = new Dialog(this);

                updateKey.setContentView(R.layout.dialog_mkey);
                updateKey.setTitle("Update Master Key");

                // get the Refferences of views
                final EditText editTextOldKey = (EditText) updateKey.findViewById(R.id.editTextOldKey);
                final EditText editTextNewKey = (EditText) updateKey.findViewById(R.id.editTextNewKey);
                Button btnUpdate = (android.widget.Button) updateKey.findViewById(R.id.buttonUpdateKey);

                // Set On ClickListener
                btnUpdate.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View v) {
                        // TODO Auto-generated method stub

                        // get the old and new key
                        String oldKey = editTextOldKey.getText().toString();
                        String newKey = editTextNewKey.getText().toString();
//                        Log.d("MK", "O : " + oldKey);
//                        Log.d("MK", "N : " + newKey);
                        byte[] boKey = StringLib.hexStringToByteArray(oldKey);
                        byte[] bnKey = StringLib.hexStringToByteArray(newKey);
                        byte[] tmKey = null;
                        if (boKey.length==8) {
                            tmKey = boKey;
                            boKey = ByteBuffer.allocate(16).put(tmKey).put(tmKey).array();
                        }
                        if (bnKey.length==8) {
                            tmKey = bnKey;
                            bnKey = ByteBuffer.allocate(16).put(tmKey).put(tmKey).array();
                        }
                        try {
                            PINPadInterface.open();

                            int ret = PINPadInterface.updateMasterKey(0, boKey, boKey.length, bnKey, bnKey.length);
//                            Log.d("PINPAD", "UPD MK STAT : " + String.valueOf(ret));
                            if (ret > -1) {
                                Toast.makeText(AdminActivity.this, "Master Key successfully updated", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(AdminActivity.this, "Update Master Key failed", Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(AdminActivity.this, "Update Master Key failed", Toast.LENGTH_LONG).show();
                        } finally {
                            PINPadInterface.close();
                        }

                        // fetch the old key form database
//                        String q = "select mk from holder where kid = 0";
//                        Cursor ck = clientDB.rawQuery(q, null);
//                        if (ck.moveToFirst()) {
//                            String existingKey = ck.getString(ck.getColumnIndex("mk"));
//                            if (existingKey.equals(oldKey)) {
//                                String w = "update holder set mk = '" + newKey + "' "
//                                        + "where kid = 0";
//                                clientDB.execSQL(w);
//                                Toast.makeText(AdminActivity.this, "Key successfully updated", Toast.LENGTH_LONG).show();
//                            } else {
//                                Toast.makeText(AdminActivity.this, "Old key do not matched", Toast.LENGTH_LONG).show();
//                            }
//                            clientDB.close();
//                        } else {
//                            Toast.makeText(AdminActivity.this, "Secure module controller not responding", Toast.LENGTH_LONG).show();
//                        }
                        updateKey.dismiss();
                    }
                });
                updateKey.show();
                break;
            case 2:
                final Dialog overrideWk = new Dialog(this);

                overrideWk.setContentView(R.layout.dialog_mkey);
                overrideWk.setTitle("Override Working Key");

                // get the Refferences of views
                final EditText editOK = (EditText) overrideWk.findViewById(R.id.editTextOldKey);
                final EditText editWK = (EditText) overrideWk.findViewById(R.id.editTextNewKey);
                Button btnWkUpdate = (android.widget.Button) overrideWk.findViewById(R.id.buttonUpdateKey);
                editOK.setVisibility(View.GONE);
                editWK.setHint("Input Working Key");
                btnWkUpdate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String wks = "failed";
                        try {
                            PINPadInterface.open();
                            String wk = String.valueOf(editWK.getText());
//                    wk += "FFFFFFFFFFFFFFFF";
//                    String wk = "56CC09E7CFDC4CEF086F9A1D74C94D4E";
                            //override wk
//                        wk = "376EB729FB11373BC0F097ECE49F6A25";
                            if (wk.length()==16) {
                                wk = wk+wk;
                            }
                            byte[] newKey = StringLib.hexStringToByteArray(wk);
                            int ret = PINPadInterface.updateUserKey(0,0, newKey, newKey.length);
                            if (ret >= 0) {
                                wks = "success (" + ret + ")";
                            } else {
                                wks += " (" + ret + ")";
                            }
                        } catch (Exception e) {
                            //teu bisa update
                            Log.e("LOGON", e.getMessage());
                        } finally {
                            PINPadInterface.close();
                            Toast.makeText(AdminActivity.this, "Override working key " + wks, Toast.LENGTH_SHORT).show();
                        }
                        overrideWk.dismiss();
                    }
                });
                overrideWk.show();
                break;
            case 4:
                try {
                    String[] screenFiles = getApplicationContext().fileList();
                    for (int i = 0; i < screenFiles.length; i++) {
                        getApplicationContext().deleteFile(screenFiles[i]);
                    }
                    Toast.makeText(AdminActivity.this, "Clear screen cache completed", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(AdminActivity.this, "Clear screen cache error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                break;
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Toast.makeText(this,"Please wait until saved",Toast.LENGTH_SHORT);
    }
}
