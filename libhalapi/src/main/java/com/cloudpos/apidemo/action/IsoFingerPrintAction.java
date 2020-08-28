package com.cloudpos.apidemo.action;

import com.cloudpos.apidemo.activity.R;
import com.cloudpos.apidemo.function.ActionCallbackImpl;
import com.cloudpos.jniinterface.IsoFingerPrintInterface;

import java.util.Map;

/**
 * create by rf.w 19-1-23上午9:31
 */
public class IsoFingerPrintAction extends ConstantAction {
    static int userID = 1;
    static int timeout = 10 * 1000;
    static final int ISOFINGERPRINT_TYPE_DEFAULT = 0;
    static final int ISOFINGERPRINT_TYPE_ISO2005 = 1;
    static final int ISOFINGERPRINT_TYPE_ISO2015 = 2;

    private void setParams(Map<String, Object> param, ActionCallbackImpl callback) {
        this.mCallback = callback;
    }

    public void open(Map<String, Object> param, ActionCallbackImpl callback) {
        setParams(param, callback);
        if (isOpened) {
            callback.sendFailedMsg(mContext.getResources().getString(R.string.device_opened));
        } else {
            try {
                int result = IsoFingerPrintInterface.open();
                if (result < 0) {
                    callback.sendFailedMsg(mContext.getResources().getString(R.string.operation_with_error) + result);
                } else {
                    isOpened = true;
                    // 每次进来把指纹清空
                    IsoFingerPrintInterface.delAllFingers();
                    callback.sendSuccessMsg(mContext.getResources().getString(
                            R.string.operation_successful));
                }
            } catch (Throwable e) {
                e.printStackTrace();
                callback.sendFailedMsg(mContext.getResources().getString(R.string.operation_failed));
            }
        }
    }

    public void close(Map<String, Object> param, ActionCallbackImpl callback) {
        setParams(param, callback);
        checkOpenedAndGetData(new DataAction() {
            @Override
            public int getResult() {
                isOpened = false;
                int result = IsoFingerPrintInterface.close();
                return result;
            }
        });
    }

    public void cancel(Map<String, Object> param, ActionCallbackImpl callback) {
        setParams(param, callback);
        checkOpenedAndGetData(new DataAction() {
            @Override
            public int getResult() {
                int result = IsoFingerPrintInterface.cancel();
                return result;
            }
        });
    }


    public void enroll(Map<String, Object> param, ActionCallbackImpl callback) {
        userID++;
        setParams(param, callback);
        checkOpenedAndGetData(new DataAction() {
            @Override
            public int getResult() {
                int result = IsoFingerPrintInterface.enroll(userID, timeout);
                return result;
            }
        });
    }

    public void delFinger(Map<String, Object> param, ActionCallbackImpl callback) {
        setParams(param, callback);
        checkOpenedAndGetData(new DataAction() {
            @Override
            public int getResult() {
                int result = IsoFingerPrintInterface.delFinger(userID);
                return result;
            }
        });
    }

    public void verifyAgainstUserId(Map<String, Object> param, ActionCallbackImpl callback) {
        setParams(param, callback);
        checkOpenedAndGetData(new DataAction() {
            @Override
            public int getResult() {
                int result = IsoFingerPrintInterface.verifyAgainstUserId(userID, timeout);
                return result;
            }
        });
    }

    public void verifyAll(Map<String, Object> param, ActionCallbackImpl callback) {
        setParams(param, callback);
        checkOpenedAndGetData(new DataAction() {
            @Override
            public int getResult() {
                int result = IsoFingerPrintInterface.verifyAll(timeout);
                return result;
            }
        });
    }

    public void delAllFingers(Map<String, Object> param, ActionCallbackImpl callback) {
        setParams(param, callback);
        checkOpenedAndGetData(new DataAction() {
            @Override
            public int getResult() {
                int result = IsoFingerPrintInterface.delAllFingers();
                return result;
            }
        });
    }

    public void verifyAgainstFeature(Map<String, Object> param, ActionCallbackImpl callback) {
        setParams(param, callback);

        final byte[] pFeaBuffer = new byte[8192];
        IsoFingerPrintInterface.getUserFeature(userID, pFeaBuffer, ISOFINGERPRINT_TYPE_DEFAULT);
        checkOpenedAndGetData(new DataAction() {
            @Override
            public int getResult() {
                int result = IsoFingerPrintInterface.verifyAgainstFeature(pFeaBuffer, timeout);
                return result;
            }
        });
    }

    public void listAllFingersStatus(Map<String, Object> param, ActionCallbackImpl callback) {
        setParams(param, callback);
        final int[] fingerExist = new int[100];
        checkOpenedAndGetData(new DataAction() {
            @Override
            public int getResult() {
                int result = IsoFingerPrintInterface.listAllFingersStatus(fingerExist);
                return result;
            }
        });
    }

    public void getUserFeature(Map<String, Object> param, ActionCallbackImpl callback) {
        setParams(param, callback);
        final byte[] pFeaBuffer = new byte[8192];

        checkOpenedAndGetData(new DataAction() {
            @Override
            public int getResult() {
                int result = IsoFingerPrintInterface.getUserFeature(userID, pFeaBuffer, ISOFINGERPRINT_TYPE_DEFAULT);
                return result;
            }
        });
    }

    public void storeFeature(Map<String, Object> param, ActionCallbackImpl callback) {
        setParams(param, callback);
        final byte[] pFeaBuffer = new byte[8192];
        IsoFingerPrintInterface.getUserFeature(userID, pFeaBuffer, ISOFINGERPRINT_TYPE_DEFAULT);
        checkOpenedAndGetData(new DataAction() {
            @Override
            public int getResult() {
                int result = IsoFingerPrintInterface.storeFeature(userID, pFeaBuffer);
                return result;
            }
        });
    }

    final byte[] arryFea = new byte[8192];

    public void getFeaExt(Map<String, Object> param, ActionCallbackImpl callback) {
        setParams(param, callback);
        checkOpenedAndGetData(new DataAction() {
            @Override
            public int getResult() {
                int result = IsoFingerPrintInterface.getFeaExt(arryFea, timeout, ISOFINGERPRINT_TYPE_DEFAULT);
                return result;
            }
        });
    }

    public void match(Map<String, Object> param, ActionCallbackImpl callback) {
        setParams(param, callback);

        final byte[] arryFea2 = new byte[8192];
        IsoFingerPrintInterface.getFeaExt(arryFea2, timeout, ISOFINGERPRINT_TYPE_DEFAULT);

        checkOpenedAndGetData(new DataAction() {
            @Override
            public int getResult() {
                int result = IsoFingerPrintInterface.match(arryFea, arryFea.length, arryFea2,
                        arryFea2.length);
                return result;
            }
        });
    }
    public void getImage(Map<String, Object> param, ActionCallbackImpl callback) {
        setParams(param, callback);
        final byte[] pImgBuffer = new byte[102400];
        IsoFingerPrintInterface.getUserFeature(userID, pImgBuffer, ISOFINGERPRINT_TYPE_DEFAULT);
        final int pImgWidth = 128;
        final int pImgHeight = 128;
        checkOpenedAndGetData(new DataAction() {
            @Override
            public int getResult() {
                int result = IsoFingerPrintInterface.getImage(pImgBuffer, pImgWidth, pImgHeight, ISOFINGERPRINT_TYPE_DEFAULT);
                return result;
            }
        });
    }

    public void convertFormat(Map<String, Object> param, ActionCallbackImpl callback) {
        setParams(param, callback);
        final byte[] feaIn = new byte[8192];
        IsoFingerPrintInterface.getUserFeature(userID, feaIn, ISOFINGERPRINT_TYPE_DEFAULT);
        final byte[] feaOut = new byte[8192];
        IsoFingerPrintInterface.getUserFeature(userID, feaOut, ISOFINGERPRINT_TYPE_DEFAULT);
        checkOpenedAndGetData(new DataAction() {
            @Override
            public int getResult() {
                int result = IsoFingerPrintInterface.convertFormat(feaIn, ISOFINGERPRINT_TYPE_DEFAULT, feaOut, ISOFINGERPRINT_TYPE_ISO2005);
                return result;
            }
        });
    }

    public void getId(Map<String, Object> param, ActionCallbackImpl callback) {
        setParams(param, callback);
        checkOpenedAndGetData(new DataAction() {
            @Override
            public int getResult() {
                int result = IsoFingerPrintInterface.getId();
                return result;
            }
        });
    }
}
