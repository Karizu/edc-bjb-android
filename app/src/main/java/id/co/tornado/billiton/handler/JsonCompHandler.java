/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.tornado.billiton.handler;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

import id.co.tornado.billiton.common.CommonConfig;

/**
 * @author indra
 */
public class JsonCompHandler {

    private static MenuListResolver mlr = new MenuListResolver();

    public JsonCompHandler() {

    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static JSONObject checkUpdate(Context ctx) throws IOException, JSONException {
        SharedPreferences preferences = ctx.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
        String hostname = preferences.getString("hostname", CommonConfig.HTTP_REST_URL);
        String initRest = preferences.getString("init_screen", CommonConfig.INIT_REST_ACT);
        // Create an unbound socket
        URL url = new URL(CommonConfig.HTTP_PROTOCOL+"://" + hostname + "/screen?id=" + initRest);

        InputStream is = url.openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }

    public static int loadConf(Context ctx) throws IOException {
        int ret = 0;
        try {
            SharedPreferences preferencesConfig = ctx.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
            String hostname = preferencesConfig.getString("sockethost", CommonConfig.WEBSOCKET_URL);
            String serialNum = Build.SERIAL;
            String tid = preferencesConfig.getString("terminal_id", serialNum);
            // Create an unbound socket
            URL url = new URL(CommonConfig.HTTP_PROTOCOL+"://" + hostname + "/device/" + tid + "/loadConf");
//            URL url = new URL("http://" + hostname + "/device/" + tid + "/loadConf");
            InputStream is = url.openStream();
            is.close();
//        JSONObject json = new JSONObject();
//        try {
//            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
//            String jsonText = readAll(rd);
//            json = new JSONObject(jsonText);
//            Log.i("conf", json.toString(2));
//            SharedPreferences preferencesSetting = ctx.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
//            preferencesSetting.edit().putString("merchant_name", json.getString("merchantname")).apply();
//            preferencesSetting.edit().putString("merchant_address1", json.getString("address1")).apply();
//            preferencesSetting.edit().putString("merchant_address2", json.getString("address2")).apply();
//            preferencesSetting.edit().putString("merchant_id", json.getString("merchantid")).apply();
//            preferencesSetting.edit().putString("init_screen", json.getString("initscreen")).apply();
            ret = 1;
        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            is.close();
        }
        return ret;
    }

    public static int notifyConfigSuccess(Context ctx) {
        int ret = 0;
        try {
            SharedPreferences preferencesConfig = ctx.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
            String hostname = preferencesConfig.getString("sockethost", CommonConfig.WEBSOCKET_URL);
            String serialNum = Build.SERIAL;
            String tid = preferencesConfig.getString("terminal_id", serialNum);
            URL url = new URL(CommonConfig.HTTP_PROTOCOL+"://" + hostname + "/device/" + serialNum + "/loadMenuSuccess");
//            URL url = new URL("http://" + hostname + "/device/" + serialNum + "/loadMenuSuccess");
            InputStream is = url.openStream();
            is.close();
            ret = 1;
        } catch (Exception e) {

        }
        return ret;
    }

    public static JSONObject readJsonFromUrl(String id, Context ctx) throws IOException, JSONException {
        SharedPreferences preferences = ctx.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
        String hostname = CommonConfig.HTTP_PROTOCOL+"://" + preferences.getString("hostname", CommonConfig.HTTP_REST_URL);
//        String hostname = "http://" + preferences.getString("hostname", CommonConfig.HTTP_REST_URL);
        String serialNum = Build.SERIAL;
        // Create an unbound socket
        if (id.contains("Rp")) {
            return new JSONObject();
        }
//        Log.d("LOAD URL",hostname + "/device/" + serialNum + "/loadMenu/" + id);
        String simNumber = preferences.getString("sim_number", CommonConfig.INIT_SIM_NUMBER);
        String tid = preferences.getString("terminal_id", CommonConfig.DEV_TERMINAL_ID);
        Log.d("LOAD URL", hostname + "/screen?id=" + id + "&simNumber=" + simNumber + "&tid=" + tid);
//        URL url = new URL(hostname + "/device/" + serialNum + "/loadMenu/" + id);
        URL url = new URL(hostname + "/screen?id=" + id);
//        URL url = new URL(hostname + "/screen?id=" + id + "&simNumber=" + simNumber + "&tid=" + tid);
        InputStream is = url.openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return (JSONObject) json.get("screen");
        } finally {
            is.close();
        }
    }

    public static JSONObject readJsonFromIntent(String id, Context ctx) throws IOException, JSONException {
        SharedPreferences preferences = ctx.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
        String hostname = CommonConfig.HTTP_PROTOCOL+"://" + preferences.getString("hostname", CommonConfig.HTTP_REST_URL);
//        String hostname = "http://" + preferences.getString("hostname", CommonConfig.HTTP_REST_URL);
        String serialNum = Build.SERIAL;
        // Create an unbound socket
        if (id.contains("Rp")) {
            return new JSONObject();
        }
//        Log.d("LOAD URL",hostname + "/device/" + serialNum + "/loadMenu/" + id);
        Log.d("LOAD URL", hostname + "/screen?id=" + id);
//        URL url = new URL(hostname + "/device/" + serialNum + "/loadMenu/" + id);
        URL url = new URL(hostname + "/screen?id=" + id);
        InputStream is = null;
        try {
            is = url.openStream();
        } catch (Exception e){
            e.printStackTrace();
        }
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return (JSONObject) json.get("screen");
        } finally {
            is.close();
        }
    }

    public static JSONObject reprintFromArrest(String pid, String tid, String stan, Context ctx) throws IOException, JSONException {
        SharedPreferences preferences = ctx.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
        String hostname = CommonConfig.HTTP_PROTOCOL+"://" + preferences.getString("hostname", CommonConfig.HTTP_REST_URL);
//        String hostname = "http://" + preferences.getString("hostname", CommonConfig.HTTP_REST_URL);
        String serialNum = Build.SERIAL;
//        Log.d("LOAD URL",hostname + "/device/" + serialNum + "/loadMenu/" + id);
        String simNumber = preferences.getString("sim_number", CommonConfig.INIT_SIM_NUMBER);
        Log.d("LOAD URL", hostname + "/print?pid=" + pid + "&tid=" + tid + "&stan=" + stan + "&simNumber=" + simNumber);
//        URL url = new URL(hostname + "/device/" + serialNum + "/loadMenu/" + id);
        URL url = new URL(hostname + "/print?pid=" + pid + "&tid=" + tid + "&stan=" + stan + "&simNumber=" + simNumber);
//        URL url = new URL(hostname + "/print?pid=" + pid + "&tid=" + tid + "&stan=" + stan );
        InputStream is = url.openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }

    public static JSONObject reportFromArrest(String pid, String tid, String date, Context ctx) throws IOException, JSONException {
        SharedPreferences preferences = ctx.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
        String hostname = CommonConfig.HTTP_PROTOCOL+"://" + preferences.getString("hostname", CommonConfig.HTTP_REST_URL);
//        String hostname = "http://" + preferences.getString("hostname", CommonConfig.HTTP_REST_URL);
        String serialNum = Build.SERIAL;
//        Log.d("LOAD URL",hostname + "/device/" + serialNum + "/loadMenu/" + id);
        String simNumber = preferences.getString("sim_number", CommonConfig.INIT_SIM_NUMBER);
        Log.d("LOAD URL", hostname + "/report?pid=" + pid + "&tid=" + tid + "&date=" + date + "&simNumber=" + simNumber);
//        URL url = new URL(hostname + "/device/" + serialNum + "/loadMenu/" + id);
        URL url = new URL(hostname + "/report?pid=" + pid + "&tid=" + tid + "&date=" + date + "&simNumber=" + simNumber);
//        URL url = new URL(hostname + "/report?pid=" + pid + "&tid=" + tid + "&date=" + date);
        InputStream is = url.openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }

    public static JSONObject reportDetailFromArrest(String pid, String tid, String date, Context ctx) throws IOException, JSONException {
        SharedPreferences preferences = ctx.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
        String hostname = CommonConfig.HTTP_PROTOCOL+"://" + preferences.getString("hostname", CommonConfig.HTTP_REST_URL);
//        String hostname = "http://" + preferences.getString("hostname", CommonConfig.HTTP_REST_URL);
        String serialNum = Build.SERIAL;
//        Log.d("LOAD URL",hostname + "/device/" + serialNum + "/loadMenu/" + id);
        String simNumber = preferences.getString("sim_number", CommonConfig.INIT_SIM_NUMBER);
        Log.d("LOAD URL", hostname + "/reportDetail?pid=" + pid + "&tid=" + tid + "&date=" + date + "&simNumber=" + simNumber);
//        URL url = new URL(hostname + "/device/" + serialNum + "/loadMenu/" + id);
        URL url = new URL(hostname + "/reportDetail?pid=" + pid + "&tid=" + tid + "&date=" + date + "&simNumber=" + simNumber);
//        URL url = new URL(hostname + "/reportDetail?pid=" + pid + "&tid=" + tid + "&date=" + date);
        InputStream is = url.openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }

    public static JSONObject loginSetting(String username, String password, Context ctx) throws IOException, JSONException {
        SharedPreferences preferences = ctx.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
        String hostname = CommonConfig.HTTP_PROTOCOL+"://" + preferences.getString("hostname", CommonConfig.HTTP_REST_URL);
//        String hostname = "http://" + preferences.getString("hostname", CommonConfig.HTTP_REST_URL);
        String serialNum = Build.SERIAL;
//        Log.d("LOAD URL",hostname + "/device/" + serialNum + "/loadMenu/" + id);
        Log.d("LOAD URL", hostname + "/loginSetting?username=" + username + "&password=" + password);
//        URL url = new URL(hostname + "/device/" + serialNum + "/loadMenu/" + id);
        URL url = new URL(hostname + "/loginSetting?username=" + username + "&password=" + password);
        InputStream is = url.openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }

    public static boolean saveJson(Context context, String id) throws IOException, Exception {
        try {
            FileOutputStream fos = context.openFileOutput(id + ".json", Context.MODE_PRIVATE);
            Writer out = new OutputStreamWriter(fos);
            out.write(readJsonFromUrl(id, context).toString());
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static JSONObject saveJsonWhenNoCache(Context context, String id) throws IOException {
        JSONObject respJsonObject = null;
        try {
            respJsonObject = readJsonFromUrl(id, context);
            if (respJsonObject!=null) {
                FileOutputStream fos = context.openFileOutput(id + ".json", Context.MODE_PRIVATE);
                Writer out = new OutputStreamWriter(fos);
                out.write(respJsonObject.toString());
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return respJsonObject;
    }

    public static boolean saveJsonNoTms(Context context, String id) throws IOException, Exception {
        try {
            boolean isdel = context.deleteFile(id + ".json");
        } catch (Exception e) {
            e.printStackTrace();
            //pass
        }
        try {
            FileOutputStream fos = context.openFileOutput(id + ".json", Context.MODE_PRIVATE);
            Writer out = new OutputStreamWriter(fos);
            out.write(mlr.loadMenu(context, id).get("screen").toString());
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean saveJson(Context context, JSONObject obj) throws IOException {
        try {
            String id = obj.getString("id");
            FileOutputStream fos = context.openFileOutput(id + ".json", Context.MODE_PRIVATE);
            Writer out = new OutputStreamWriter(fos);
            out.write(obj.toString());
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static JSONObject readJson(Context context, String id) throws IOException, JSONException {
//        BufferedReader rd = new BufferedReader(new InputStreamReader(context.openFileInput(id + ".json")));
        BufferedReader rd = new BufferedReader(new InputStreamReader(new FileInputStream("/data/data/" + context.getPackageName() + "/files/" + id + ".json")));
        return new JSONObject(readAll(rd));
    }

    public static JSONObject readJsonFromCacheIfAvailable(Context context, String id) throws IOException, JSONException {
//        BufferedReader rd = new BufferedReader(new InputStreamReader(context.openFileInput(id + ".json")));
        JSONObject respJsonObject = new JSONObject();
        try {
            FileInputStream fis = new FileInputStream("/data/data/" + context.getPackageName() + "/files/" + id + ".json");
            BufferedReader rd = new BufferedReader(new InputStreamReader(fis));
            respJsonObject = new JSONObject(readAll(rd));
        } catch (FileNotFoundException e) {
            respJsonObject = saveJsonWhenNoCache(context, id);
        }
        return respJsonObject;
    }

    public static void checkVer(Context ctx,
                                String id, String[] arrays1,
                                String[] arrays2) throws Exception {
        JSONObject json = JsonCompHandler.readJsonFromUrl(id, ctx);
        if (json.get("comps") != null) {
            JSONObject obj = (JSONObject) json.get("comps");
            JSONArray ar = obj.getJSONArray("comp");
            for (int i = 0; i < ar.length(); i++) {
                JSONObject obj2 = ar.getJSONObject(i);
                for (String str : arrays2) {
                    if (str.equals("comp_act")) {
                        try {
                            checkVer(ctx, obj2.get(str).toString(), CommonConfig.FORM_MENU_KEY, CommonConfig.FORM_MENU_COMP_KEY);
                        } catch (JSONException ex) {

                        }
                    }
                }
            }
        }
//        if (json.get("ver") != null) {
//            String ver = json.getString("ver");
//            SharedPreferences preferences = ctx.getSharedPreferences(CommonConfig.VER_FILE, Context.MODE_PRIVATE);
//            Log.d("VER", "New Version");
//            Log.d("VER", "ID :" + id);
//            Log.d("VER", "VER :" + ver);
//            preferences.edit().putString(id, ver).apply();
            saveJson(ctx, id);
//        }
    }

    public static void checkVerNoTms(Context ctx,
                                     String id, String[] arrays1,
                                     String[] arrays2) throws IOException, JSONException, Exception {
        JSONObject json = null;
        try {
            json = mlr.loadMenu(ctx, id).getJSONObject("screen");
        } catch (Exception e) {
            Log.e("M2J", "Error cannot load menu : " + e.getMessage());
//            e.printStackTrace();
        }
        if (json.get("comps") != null) {
            JSONObject obj = (JSONObject) json.get("comps");
            JSONArray ar = obj.getJSONArray("comp");
            for (int i = 0; i < ar.length(); i++) {
                JSONObject obj2 = ar.getJSONObject(i);
                for (String str : arrays2) {
                    if (str.equals("comp_act")) {
                        try {
                            checkVerNoTms(ctx, obj2.get(str).toString(), CommonConfig.FORM_MENU_KEY, CommonConfig.FORM_MENU_COMP_KEY);
                        } catch (JSONException ex) {
//                            Log.e("JPR", "Cannot parse JSON : " + ex.getMessage());
                        }
                    }
                }
            }
        }
        if (json.get("ver") != null) {
            String ver = json.getString("ver");
            SharedPreferences preferences = ctx.getSharedPreferences(CommonConfig.VER_FILE, Context.MODE_PRIVATE);
            if (preferences.getString(id, "0").equals("0")) {
//                Log.d("VER","Create new menu from Rest");
//                Log.d("VER","ID :"+id);
//                Log.d("VER","VER :"+ver);
                preferences.edit().putString(id, ver).apply();
                saveJsonNoTms(ctx, id);
            } else if (!preferences.getString(id, "0").equals(ver)) {
//                Log.d("VER","New Version");
//                Log.d("VER","ID :"+id);
//                Log.d("VER","VER :"+ver);
                preferences.edit().putString(id, ver).apply();
                saveJsonNoTms(ctx, id);
            }
        }
    }

    public static void jsonRebuild(Context ctx,
                                   String id, String[] arrays1,
                                   String[] arrays2) throws IOException, JSONException, Exception {
        JSONObject json = null;
        try {
            json = mlr.loadMenu(ctx, id).getJSONObject("screen");
        } catch (Exception e) {
            Log.e("M2J", "Error cannot load menu : " + e.getMessage());
//            e.printStackTrace();
        }
        if (json.get("comps") != null) {
            JSONObject obj = (JSONObject) json.get("comps");
            JSONArray ar = obj.getJSONArray("comp");
            for (int i = 0; i < ar.length(); i++) {
                JSONObject obj2 = ar.getJSONObject(i);
                for (String str : arrays2) {
                    if (str.equals("comp_act")) {
                        try {
                            jsonRebuild(ctx, obj2.get(str).toString(), CommonConfig.FORM_MENU_KEY, CommonConfig.FORM_MENU_COMP_KEY);
                        } catch (JSONException ex) {
//                            Log.e("JPR", "Cannot parse JSON : " + ex.getMessage());
                        }
                    }
                }
            }
        }
        if (json.get("ver") != null) {
            String ver = json.getString("ver");
            SharedPreferences preferences = ctx.getSharedPreferences(CommonConfig.VER_FILE, Context.MODE_PRIVATE);
            preferences.edit().putString(id, ver).apply();
            saveJsonNoTms(ctx, id);
        }
    }

    public static int checkVer(Context ctx) {
        int r = 0;
        try {
            SharedPreferences preferences = ctx.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
            String[] screenFiles = ctx.fileList();
            for (int i = 0; i < screenFiles.length; i++) {
                ctx.deleteFile(screenFiles[i]);
            }
//            checkVer(ctx, preferences.getString("init_screen", CommonConfig.INIT_REST_ACT), CommonConfig.LIST_MENU_KEY, CommonConfig.LIST_MENU_COMP_KEY);
            r = loadConf(ctx);
            if (r>0) {
                r = notifyConfigSuccess(ctx);
            }
        } catch (Exception e) {
            Toast.makeText(ctx, "Error : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return r;
    }

    public static File makeAndGetLogDirectory(Context context, String logDate) {
        // determine the log directory
        File logDirectory = new File(context.getFilesDir(), logDate);

        // creates the directory if not present yet
        logDirectory.mkdir();

        return logDirectory;
    }
    public static void saveJsonMessage(Context context, String mid, String mtype, JSONObject jmsg) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String logDir = sdf.format(new Date());
            String filename = mtype + mid;
            File saveDir = makeAndGetLogDirectory(context, logDir);
            File msgFile = new File(saveDir, filename + ".json");
            FileOutputStream fos = new FileOutputStream(msgFile);
            Writer out = new OutputStreamWriter(fos);
            out.write(jmsg.toString());
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static JSONObject loadJsonMessage(Context context, String mid, String mtype, String mdate) {
        JSONObject resp = new JSONObject();
        try {
            String logDir = mdate;
            String filename = mtype + mid;
            if (mdate.equals("")) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                logDir = sdf.format(new Date());
            }
            File loadDir = makeAndGetLogDirectory(context, logDir);
            File msgFile = new File(loadDir, filename + ".json");
            FileInputStream fis = new FileInputStream(msgFile);
            BufferedReader rd = new BufferedReader(new InputStreamReader(fis));
            resp = new JSONObject(readAll(rd));
        } catch (Exception e) {

        }
        return resp;
    }

    public static void main(String[] args) throws IOException, JSONException, Exception {
    }
}
