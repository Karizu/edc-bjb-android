
package com.cloudpos.apidemo.common;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.cloudpos.apidemo.activity.ConstantActivity;
import com.cloudpos.apidemo.activity.R;
import com.cloudpos.apidemo.function.ActionCallbackImpl;

public class Common {

    public static byte[] createMasterKey(int length) {
        byte[] array = new byte[length];
        for (int i = 0; i < length; i++) {
            array[i] = (byte) 0x38;
        }
        return array;
    }
    
    public static byte[] createRandomByte(int length) {
    	byte[] dst = new byte[length];
    	for (int i = 0; i < length; i++) {
    		Integer j = (int) (Math.random() * 128);
    		dst[i] = Byte.parseByte(j.toString());
    	}
    	return dst;
    }

    public static int transferErrorCode(int errorCode) {
        int a = -errorCode;
        int b = a & 0x0000FF;
        return -b;
    }

    public static String getVersion(Context mContext) {
        // 获取packagemanager的实例
        PackageManager packageManager = mContext.getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        String version = "";
        try {
            PackageInfo packInfo = packageManager.getPackageInfo(mContext.getPackageName(), 0);
            version = packInfo.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return String.format(mContext.getResources().getString(R.string.version), version);
    }
    
    public static String getModel() {
        String model = "";
        model = SystemProperties.getSystemPropertie("ro.product.model").trim();
        model = model.replace(" ", "_");
        model = model.toUpperCase();
        Log.e("model", model);
        // model = model.substring(0, model.length() - 1).trim();//
        // find corresponding type of device.   eg:WIZARPOS,WIZARPAD,...
        // whether handheld device
        if (model.equals("WIZARHAND_Q1") || model.equals("MSM8610") || model.equals("WIZARHAND_Q0")
                || model.equals("FARS72_W_KK") || model.equals("WIZARHAND_M0")) {
            ConstantActivity.isHand = true;
            if (model.equals("WIZARHAND_Q1") || model.equals("MSM8610")
                    || model.equals("WIZARHAND_Q0")) {
                ConstantActivity.isQ1 = true;
            }
        }
        return model;
    }

    public static String getMethodName() {
        StackTraceElement[] eles = Thread.currentThread().getStackTrace();
//        for (StackTraceElement stackTraceElement : eles) {
//            Log.e("stackTraceElement", stackTraceElement.getMethodName());
//        }
        return eles[5].getMethodName();
    }

    public static boolean getActionField(Context context, String className, String fieldName) {
        boolean isOpened = false;
        try {
            Class<?> clazz = Class.forName(context.getResources().getString(
                    R.string.action_package_name)
                    + className);
            Object object = clazz.getInterfaces();
            Field field = clazz.getField(fieldName);
            isOpened = field.getBoolean(object);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            isOpened = false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            isOpened = false;
        } catch (Exception e) {
            e.printStackTrace();
            isOpened = false;
        }
        return isOpened;
    }
}
