package id.co.tornado.billiton;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import id.co.tornado.billiton.common.CommonConfig;
import id.co.tornado.billiton.common.StringLib;
import id.co.tornado.billiton.customview.HelveticaTextView;
import id.co.tornado.billiton.handler.DownloadSoftware;
import id.co.tornado.billiton.handler.JsonCompHandler;

public class UpdateAppActivity extends AppCompatActivity {
    private HelveticaTextView txtDot, txtConnecting;
    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            if (message != null) {
                if (message.getData() != null) {
                    String result = message.getData().getString("result");

                    if (result.equals("SUKSES")) {
                        Toast.makeText(UpdateAppActivity.this, "File downloaded " + result, Toast.LENGTH_SHORT).show();
                        File file = new File(DownloadSoftware.FILE_NAME);
                        if (file.exists()) {
                            install();
                        }

                    } else {
                        Toast.makeText(UpdateAppActivity.this, "Download error: " + result, Toast.LENGTH_LONG).show();
                    }
                }
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final int method = getIntent().getIntExtra("method",0);
        setContentView(R.layout.splash_screen);
        DialogInterface.OnClickListener click = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        switch (method){
            case 123://UPDATE MENU
                click = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, final int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                txtConnecting = (HelveticaTextView) findViewById(R.id.txtConnecting);
                                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                StrictMode.setThreadPolicy(policy);
                                try {

//                                    final JSONObject json = JsonCompHandler.checkUpdate(UpdateAppActivity.this);

                                    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                                    String version = pInfo.versionName;
                                    Log.i("ANDROID_VER", version);
//                                    Log.i("JSON_CHECK", json.toString());
                                    if (/**!json.getString("software").equals(version)**/true) {
                                        final ProgressDialog mProgressDialog;
                                        mProgressDialog = new ProgressDialog(UpdateAppActivity.this);
                                        mProgressDialog.setMessage("Synchronizing data.\nDon't turn off your device");
                                        mProgressDialog.setIndeterminate(true);
                                        mProgressDialog.show();
                                        final int ucs[] = new int[1];
                                        ucs[0] = 0;
                                        Thread t1 = new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    ucs[0] = JsonCompHandler.checkVer(UpdateAppActivity.this.getApplication());
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                } finally {
                                                    if (ucs[0]>1) {
                                                    }
                                                    String ns = Context.NOTIFICATION_SERVICE;
                                                    NotificationManager nMgr = (NotificationManager) UpdateAppActivity.this.getSystemService(ns);
                                                    nMgr.cancel(method);
                                                    mProgressDialog.dismiss();
                                                    Intent intent = new Intent(UpdateAppActivity.this, MainActivity.class);
                                                    startActivity(intent);
                                                }
                                            }
                                        });
                                        t1.start();
                                    }
                                } catch (PackageManager.NameNotFoundException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                Intent intent = new Intent(UpdateAppActivity.this, MainActivity.class);
                                startActivity(intent);
                                break;
                        }
                    }
                };

                builder.setTitle("Konfirmasi update fitur");

                break;
            case 1234://Update Software
                click = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                txtConnecting = (HelveticaTextView) findViewById(R.id.txtConnecting);
                                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                StrictMode.setThreadPolicy(policy);
                                try {

                                    final JSONObject json = JsonCompHandler.checkUpdate(UpdateAppActivity.this);

                                    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                                    String version = pInfo.versionName;
                                    Log.i("ANDROID_VER", version);
                                    Log.i("JSON_CHECK", json.toString());
                                    if (/**!json.getString("software").equals(version)**/true) {
                                        if (new File(DownloadSoftware.FILE_NAME).exists()) {
                                            String hash = StringLib.fileToMD5(DownloadSoftware.FILE_NAME);
                                            Log.i("FILE_HASH", hash);
                                            if (false) {//(hash.equals(json.getString("hash"))) {
                                                install();
                                            } else {
                                                downloadNewUpdate();
                                            }
                                        } else {
                                            downloadNewUpdate();
                                        }
                                    }
                                } catch (PackageManager.NameNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                Intent intent = new Intent(UpdateAppActivity.this, MainActivity.class);
                                startActivity(intent);
                                break;
                        }
                    }
                };
                builder.setTitle("Konfirmasi update software");
                break;
        }
        builder.setMessage("Lakukan update sekarang?");
        builder.setPositiveButton("Sekarang", click).setNegativeButton("Tidak Sekarang", click).show();
    }

    private void downloadNewUpdate() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        ProgressDialog mProgressDialog;
        mProgressDialog = new ProgressDialog(UpdateAppActivity.this);
        mProgressDialog.setMessage("Downloading new software.\nDon't turn off your device");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        final DownloadSoftware downloadTask = new DownloadSoftware(UpdateAppActivity.this, mProgressDialog, handler);
        downloadTask.execute();
    }

    public class Update extends AsyncTask<Integer, Void, Boolean> {
        private ProgressDialog pg;

        public Update(ProgressDialog progressDialog) {
            this.pg = progressDialog;
        }

        @Override
        protected void onPreExecute() {
            pg.show();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            if (params[0] == 2) {
                try {
                    Log.i("EXEC", "[RUNNING] " + "pm install -r " + DownloadSoftware.FILE_NAME);
                    Runtime.getRuntime().exec("chmod 777 " + DownloadSoftware.FILE_NAME);
                    Process proc = Runtime.getRuntime().exec("pm install -r " + DownloadSoftware.FILE_NAME);
                    BufferedReader stdError = new BufferedReader(new
                            InputStreamReader(proc.getInputStream()));
                    BufferedReader stdInput = new BufferedReader(new
                            InputStreamReader(proc.getInputStream()));
                    String line;
                    while ((line = stdError.readLine()) != null) {
                        Log.i("EXEC", line);
                    }
                    while ((line = stdInput.readLine()) != null) {
                        Log.e("EXEC", line);
                    }
                    proc.waitFor();
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(new File(DownloadSoftware.FILE_NAME)), "application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            pg.dismiss();
        }
    }

    private void install() {
        ProgressDialog mProgressDialog;
        mProgressDialog = new ProgressDialog(UpdateAppActivity.this);
        mProgressDialog.setMessage("Installing update.\nDon't turn off your device");
        mProgressDialog.setIndeterminate(true);
        Update syncMenu = new Update(mProgressDialog);
        syncMenu.execute(2);
    }

}
