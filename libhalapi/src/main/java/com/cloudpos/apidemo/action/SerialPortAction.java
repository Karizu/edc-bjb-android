
package com.cloudpos.apidemo.action;

import java.util.Map;

import com.cloudpos.apidemo.activity.R;
import com.cloudpos.apidemo.common.SystemProperties;
import com.cloudpos.apidemo.function.ActionCallbackImpl;
import com.cloudpos.jniinterface.SerialPortInterface;

public class SerialPortAction extends ConstantAction {

    private int baudrate = 38400;
    private String testString = "wizarpos";

    private void setParams(Map<String, Object> param, ActionCallbackImpl callback) {
        this.mCallback = callback;
    }

    public void open(Map<String, Object> param, ActionCallbackImpl callback) {
        setParams(param, callback);
        if (isOpened) {
            mCallback.sendFailedMsg(mContext.getResources().getString(R.string.device_opened));
        } else {
            try {
                int result = SerialPortInterface.open(getModelName(Mode.SLAVE));
                if (result < 0) {
                    mCallback.sendFailedMsg("open "
                            + mContext.getResources().getString(R.string.operation_with_error)
                            + result);
                } else {
                    isOpened = true;
                    mCallback.sendSuccessMsg("open "
                            + mContext.getResources().getString(R.string.operation_successful));
                }

//                38400
//                result = SerialPortInterface.setBaudrate(38400);
//                if (result < 0) {
//                    mCallback.sendFailedMsg("setBaudrate "
//                            + mContext.getResources().getString(R.string.operation_with_error)
//                            + result);
//                } else {
//                    isOpened = true;
//                    mCallback.sendSuccessMsg("setBaudrate "
//                            + mContext.getResources().getString(R.string.operation_successful));
//                }
            } catch (Throwable e) {
                e.printStackTrace();
                mCallback.sendFailedMsg("open "
                        + mContext.getResources().getString(R.string.operation_failed));
            }
        }
    }

    public void close(Map<String, Object> param, ActionCallbackImpl callback) {
        setParams(param, callback);
        checkOpenedAndGetData(new DataAction() {

            @Override
            public int getResult() {
                isOpened = false;
                int result = SerialPortInterface.close();
                return result;
            }
        });
    }

    public void flushIO(Map<String, Object> param, ActionCallbackImpl callback) {
        setParams(param, callback);
        checkOpenedAndGetData(new DataAction() {

            @Override
            public int getResult() {
                int result = SerialPortInterface.flushIO();
                return result;
            }
        });
    }

    public void read(Map<String, Object> param, ActionCallbackImpl callback) {
        final byte[] arryData = new byte[testString.length()];
        final int length = testString.length();
        setParams(param, callback);
        checkOpenedAndGetData(new DataAction() {

            @Override
            public int getResult() {
                int result = SerialPortInterface.read(arryData, length, 3000);
                return result;
            }
        });
    }

    public void write(Map<String, Object> param, ActionCallbackImpl callback) {
        final byte[] arryData = new String(testString).getBytes();
        final int length = 2;
        final int offset = 2;
        setParams(param, callback);
        checkOpenedAndGetData(new DataAction() {

            @Override
            public int getResult() {
                int result = SerialPortInterface.write(arryData, offset, length);
                return result;
            }
        });
    }

    public void setBaudrate(Map<String, Object> param, ActionCallbackImpl callback) {
        setParams(param, callback);
        checkOpenedAndGetData(new DataAction() {

            @Override
            public int getResult() {
                int result = SerialPortInterface.setBaudrate(baudrate);
                return result;
            }
        });
    }

    private enum Mode {SLAVE, HOST}

    ;

    private String getModelName(Mode mode) {
//    	"USB_SLAVE_SERIAL" : slave mode,(USB)  
//    	"USB_HOST_SERIAL" : host mode(OTG)
        String deviceName;
        String model = SystemProperties.getSystemPropertie("ro.wp.product.model").trim().replace(" ", "_");
        if (mode.equals(Mode.SLAVE)) {
            deviceName = "USB_SLAVE_SERIAL";
            if (model.equalsIgnoreCase("W1") || model.equalsIgnoreCase("W1V2")) {
                deviceName = "DB9";
            } else if (model.equalsIgnoreCase("Q1")) {
                deviceName = "WIZARHANDQ1";
            }
        } else {
            deviceName = "USB_SERIAL";
            if (model.equalsIgnoreCase("W1") || model.equalsIgnoreCase("W1V2")) {
                deviceName = "GS0_Q1";
            }
        }
        return deviceName;
    }

}
