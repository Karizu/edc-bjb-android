#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <dlfcn.h>
#include <semaphore.h>
#include <unistd.h>
#include <errno.h>
#include <sys/cdefs.h>
#include <stddef.h>
#include <malloc.h>

#include <jni.h>

#include "hal_sys_log.h"
#include "pinpad_jni_interface.h"
#include "pinpad_interface.h"
#include "pthread.h"

const char *g_pJNIREG_CLASS = "com/cloudpos/jniinterface/PINPadInterface";
//	+add by pengli
static const char *g_pJNIREG_CLASS_INTERNAL = "com/wizarpos/internal/jniinterface/PINPadInterface";
//	-add by pengli
static jmethodID mmid;
static JavaVM *g_jvm = NULL;
static jclass mcls;

typedef struct pinpad_hal_interface {
    pinpad_open open;
    pinpad_close close;
    pinpad_show_text set_text;
    pinpad_select_key set_key;
    pinpad_set_pin_length set_pin_length;
    pinpad_encrypt_string encrypt;
    pinpad_calculate_pin_block input_pin;
    pinpad_calculate_mac calculate_mac;
    pinpad_update_user_key update_user_key;
    pinpad_update_user_key_with_check update_user_key_with_check;
    pinpad_update_master_key update_master_key;
    pinpad_set_pinblock_callback set_pinblock_callback;
    pinpad_get_serial_number get_serial_number;
    pinpad_update_cipher_master_key update_cipher_master_key;
    pinpad_update_user_key_with_check_E update_user_key_with_check_E;
    pinpad_update_cipher_master_key_E update_cipher_master_key_E;
    pinpad_set_flag_allow_bypass_pin set_flag_allow_bypass_pin;
    pinpad_encrypt_string_with_mode encrypt_string_with_mode;
    pinpad_get_hwserialno get_hwserialno;
    pinpad_get_mac_for_snk get_mac_for_snk;
    pinpad_get_mk_checkvalue get_mk_checkvalue;
    pinpad_get_sk_checkvalue get_sk_checkvalue;
    pinpad_verify_response_mac verify_response_mac;
    pinpad_set_gui_configuration set_gui_configuration;
    void *pSoHandle;
} PINPAD_HAL_INSTANCE;

static PINPAD_HAL_INSTANCE *g_pPinpadInstance = NULL;
static int ERR_NOT_OPENED = -255;
static int ERR_HAS_OPENED = -254;
static int ERR_NO_IMPLEMENT = -253;
static int ERR_INVALID_ARGUMENT = -252;
static int ERR_NORMAL = -251;

pthread_mutex_t pthread_mute;

int native_pinpad_open(JNIEnv *env, jclass obj) {
    hal_sys_info("+ native_pinpad_open()");
    int nResult = ERR_HAS_OPENED;
    env->GetJavaVM(&g_jvm);

    if (g_pPinpadInstance == NULL) {
        void *pHandle = dlopen("/system/lib/libwizarposHAL.so", RTLD_LAZY);
        if (!pHandle) {
            hal_sys_error("%s", dlerror());
            return ERR_NORMAL;
        }
        g_pPinpadInstance = new PINPAD_HAL_INSTANCE();
        g_pPinpadInstance->pSoHandle = pHandle;

        char const *methodName;
        if (NULL ==
            (g_pPinpadInstance->open = (pinpad_open)
                    dlsym(pHandle, methodName = "pinpad_open"))
            || NULL == (g_pPinpadInstance->close = (pinpad_close)
                dlsym(pHandle,
                      methodName = "pinpad_close"))
            || NULL == (g_pPinpadInstance->set_text = (pinpad_show_text)
                dlsym(pHandle,
                      methodName = "pinpad_show_text"))
            || NULL == (g_pPinpadInstance->set_key = (pinpad_select_key)
                dlsym(pHandle,
                      methodName = "pinpad_select_key"))
            || NULL == (g_pPinpadInstance->set_pin_length = (pinpad_set_pin_length)
                dlsym(pHandle,
                      methodName = "pinpad_set_pin_length"))
            || NULL == (g_pPinpadInstance->encrypt = (pinpad_encrypt_string)
                dlsym(pHandle,
                      methodName = "pinpad_encrypt_string"))
            || NULL == (g_pPinpadInstance->input_pin = (pinpad_calculate_pin_block)
                dlsym(pHandle,
                      methodName = "pinpad_calculate_pin_block"))
            || NULL == (g_pPinpadInstance->calculate_mac = (pinpad_calculate_mac)
                dlsym(pHandle,
                      methodName = "pinpad_calculate_mac"))
            || NULL ==
               (g_pPinpadInstance->update_master_key = (pinpad_update_master_key)
                       dlsym(pHandle,
                             methodName = "pinpad_update_master_key"))
                ) {
            hal_sys_error("can't find %s", methodName);
            nResult = ERR_NO_IMPLEMENT;
            goto pinpad_open_clean;
        }

        if (NULL ==
            (g_pPinpadInstance->get_serial_number = (pinpad_get_serial_number)
                    dlsym(pHandle,
                          methodName = "pinpad_get_serial_number"))
            | NULL == (g_pPinpadInstance->update_user_key = (pinpad_update_user_key)
                dlsym(pHandle,
                      methodName = "pinpad_update_user_key"))
            | NULL ==
              (g_pPinpadInstance->update_user_key_with_check = (pinpad_update_user_key_with_check)
                      dlsym(
                              pHandle, methodName = "pinpad_update_user_key_with_check"))
            | NULL ==
              (g_pPinpadInstance->update_cipher_master_key = (pinpad_update_cipher_master_key)
                      dlsym(
                              pHandle, methodName = "pinpad_update_cipher_master_key"))
            | NULL ==
              (g_pPinpadInstance->set_pinblock_callback = (pinpad_set_pinblock_callback)
                      dlsym(
                              pHandle, methodName = "pinpad_set_pinblock_callback"))
            | NULL ==
              (g_pPinpadInstance->update_cipher_master_key_E = (pinpad_update_cipher_master_key_E)
                      dlsym(
                              pHandle, methodName = "pinpad_update_cipher_master_key_E"))
            | NULL ==
              (g_pPinpadInstance->update_user_key_with_check_E = (pinpad_update_user_key_with_check_E)
                      dlsym(
                              pHandle, methodName = "pinpad_update_user_key_with_check_E"))
            | NULL ==
              (g_pPinpadInstance->set_flag_allow_bypass_pin = (pinpad_set_flag_allow_bypass_pin)
                      dlsym(
                              pHandle, methodName = "pinpad_set_flag_allow_bypass_pin"))
            | NULL ==
              (g_pPinpadInstance->encrypt_string_with_mode = (pinpad_encrypt_string_with_mode)
                      dlsym(
                              pHandle, methodName = "pinpad_encrypt_string_with_mode"))
            | NULL ==
              (g_pPinpadInstance->get_mk_checkvalue = (pinpad_get_mk_checkvalue)
                      dlsym(pHandle,
                            methodName = "pinpad_get_mk_checkvalue"))
            | NULL ==
              (g_pPinpadInstance->get_sk_checkvalue = (pinpad_get_sk_checkvalue)
                      dlsym(pHandle,
                            methodName = "pinpad_get_sk_checkvalue"))
            | NULL ==
              (g_pPinpadInstance->verify_response_mac = (pinpad_verify_response_mac)
                      dlsym(
                              pHandle, methodName = "pinpad_verify_response_mac"))
            | NULL ==
              (g_pPinpadInstance->set_gui_configuration = (pinpad_set_gui_configuration)
                      dlsym(
                              pHandle, methodName = "pinpad_set_gui_configuration"))
                ) {
            hal_sys_error("can't find %s", methodName);
        }

        hal_sys_info("find pinpad_get_hwserialno : %p ",
                     (g_pPinpadInstance->get_hwserialno = (pinpad_get_hwserialno) dlsym(pHandle,
                                                                                        methodName = "pinpad_get_hwserialno")));
        hal_sys_info("find pinpad_get_mac_for_snk : %p ",
                     (g_pPinpadInstance->get_mac_for_snk = (pinpad_get_mac_for_snk) dlsym(pHandle,
                                                                                          methodName = "pinpad_get_mac_for_snk")));

        nResult = g_pPinpadInstance->open();
        if (nResult < 0) {
            goto pinpad_open_clean;
        }

    }
    hal_sys_info("-native_pinpad_open(),result=%d", nResult);
    return nResult;

    pinpad_open_clean:
    hal_sys_info("pinpad_open_clean");
    dlclose(g_pPinpadInstance->pSoHandle);
    delete g_pPinpadInstance;
    g_pPinpadInstance = NULL;
    hal_sys_info("-native_pinpad_open(),result=%d", nResult);
    return nResult;
}

int native_pinpad_close(JNIEnv *env, jclass obj) {
    hal_sys_info("+ native_pinpad_close()");
    pthread_mutex_lock(&pthread_mute);
    int nResult = ERR_NORMAL;
    if (g_pPinpadInstance == NULL) {
        pthread_mutex_unlock(&pthread_mute);
        return ERR_NOT_OPENED;
    }
    if (g_pPinpadInstance->close == NULL) {
        pthread_mutex_unlock(&pthread_mute);
        return ERR_NO_IMPLEMENT;
    }

    nResult = g_pPinpadInstance->close();

    dlclose(g_pPinpadInstance->pSoHandle);
    delete g_pPinpadInstance;
    g_pPinpadInstance = NULL;
    pthread_mutex_unlock(&pthread_mute);
    hal_sys_info("- native_pinpad_close(),result=%d", nResult);
    return nResult;
}

int native_pinpad_show_text(JNIEnv *env, jclass obj, jint nLineIndex,
                            jbyteArray arryText, jint nLength, jint nFlagSound) {
    hal_sys_info("+ native_pinpad_show_text()");
    int nResult = ERR_NORMAL;
    if (g_pPinpadInstance == NULL || g_pPinpadInstance->set_text == NULL)
        return ERR_NOT_OPENED;
    if (g_pPinpadInstance->set_text == NULL)
        return ERR_NO_IMPLEMENT;

    if (nLength == 0)
        nResult = g_pPinpadInstance->set_text(nLineIndex, NULL, 0, nFlagSound);
    else {
        jbyte *pText = env->GetByteArrayElements(arryText, 0);
        nResult = g_pPinpadInstance->set_text(nLineIndex, (char *) pText, nLength, nFlagSound);
        env->ReleaseByteArrayElements(arryText, pText, 0);
    }
    hal_sys_info("- native_pinpad_show_text(),result=%d", nResult);
    return nResult;
}

int native_pinpad_select_key(JNIEnv *env, jclass obj, jint nKeyType,
                             jint nMasterKeyID, jint nUserKeyID, jint nAlgorith) {
    hal_sys_info("+native_pinpad_select_key()");
    int nResult = ERR_NORMAL;
    if (g_pPinpadInstance == NULL)
        return ERR_NOT_OPENED;
    if (g_pPinpadInstance->set_key == NULL)
        return ERR_NO_IMPLEMENT;

    nResult = g_pPinpadInstance->set_key(nKeyType, nMasterKeyID, nUserKeyID, nAlgorith);
    hal_sys_info("-native_pinpad_select_key(),result=%d", nResult);
    return nResult;
}

int native_pinpad_encrypt_string(JNIEnv *env, jclass obj,
                                 jbyteArray arryPlainText, jint nTextLength,
                                 jbyteArray arryCipherTextBuffer) {
    hal_sys_info("+ native_pinpad_encrypt_string()");
    int nResult = ERR_NORMAL;
    if (g_pPinpadInstance == NULL)
        return ERR_NOT_OPENED;
    if (g_pPinpadInstance->encrypt == NULL)
        return ERR_NO_IMPLEMENT;

    if (arryPlainText == NULL || arryCipherTextBuffer == NULL)
        return ERR_INVALID_ARGUMENT;

    jbyte *pPlainText = env->GetByteArrayElements(arryPlainText, 0);
    jbyte *pCipherTextBuffer = env->GetByteArrayElements(arryCipherTextBuffer, 0);
    jint nCipherTextBufferLength = env->GetArrayLength(arryCipherTextBuffer);
    nResult = g_pPinpadInstance->encrypt((unsigned char *) pPlainText,
                                         nTextLength, (unsigned char *) pCipherTextBuffer,
                                         nCipherTextBufferLength);
    env->ReleaseByteArrayElements(arryPlainText, pPlainText, 0);
    env->ReleaseByteArrayElements(arryCipherTextBuffer, pCipherTextBuffer, 0);
    hal_sys_info("-native_pinpad_encrypt_string(),result=%d", nResult);
    return nResult;
}

int native_pinpad_calculate_pin_block(JNIEnv *env, jclass obj,
                                      jbyteArray arryASCIICardNumber, jint nCardNumberLength,
                                      jbyteArray arryPinBlockBuffer, jint nTimeout_MS,
                                      jint nFlagSound) {
    hal_sys_info("+native_pinpad_calculate_pin_block()");
    int nResult = ERR_NORMAL;
    if (g_pPinpadInstance == NULL)
        return ERR_NOT_OPENED;
    if (g_pPinpadInstance->input_pin == NULL)
        return ERR_NO_IMPLEMENT;

    if (nCardNumberLength == 0 || arryPinBlockBuffer == NULL)
        return ERR_INVALID_ARGUMENT;

    jbyte *pASCIICardNumber = env->GetByteArrayElements(arryASCIICardNumber, 0);
    jbyte *pPinBlockBuffer = env->GetByteArrayElements(arryPinBlockBuffer, 0);
    jint nPinBlockBufferLength = env->GetArrayLength(arryPinBlockBuffer);

    nResult = g_pPinpadInstance->input_pin((unsigned char *) pASCIICardNumber,
                                           nCardNumberLength, (unsigned char *) pPinBlockBuffer,
                                           nPinBlockBufferLength, nTimeout_MS, nFlagSound);

    env->ReleaseByteArrayElements(arryASCIICardNumber, pASCIICardNumber, 0);
    env->ReleaseByteArrayElements(arryPinBlockBuffer, pPinBlockBuffer, 0);
    hal_sys_info("-native_pinpad_calculate_pin_block(),result=%d", nResult);
    return nResult;
}

int native_pinpad_calculate_mac(JNIEnv *env, jclass obj, jbyteArray arryData,
                                jint nDataLength, jint nMACFlag, jbyteArray arryMACOutBuffer) {
    hal_sys_info("+ native_pinpad_calculate_mac,nMACFlag = %d",
                 nMACFlag);
    int nResult = ERR_NORMAL;
    if (g_pPinpadInstance == NULL)
        return ERR_NOT_OPENED;
    if (g_pPinpadInstance->calculate_mac == NULL)
        return ERR_NO_IMPLEMENT;

    if (arryData == NULL || arryMACOutBuffer == NULL)
        return ERR_INVALID_ARGUMENT;

    //typedef int (*pinpad_calculate_mac)(unsigned char* pData, int nDataLength, int nMACFlag, unsigned char* pMACOutBuffer, int nMACOutBufferLength);

    jbyte *pData = env->GetByteArrayElements(arryData, 0);
    jbyte *pMACOutBuffer = env->GetByteArrayElements(arryMACOutBuffer, 0);
    int nMACOutBufferLength = env->GetArrayLength(arryMACOutBuffer);

    nResult = g_pPinpadInstance->calculate_mac((unsigned char *) pData,
                                               nDataLength, nMACFlag,
                                               (unsigned char *) pMACOutBuffer,
                                               nMACOutBufferLength);

    env->ReleaseByteArrayElements(arryData, pData, 0);
    env->ReleaseByteArrayElements(arryMACOutBuffer, pMACOutBuffer, 0);
    hal_sys_info("- native_pinpad_calculate_mac,result=%d", nResult);
    return nResult;
}

int native_pinpad_update_user_key(JNIEnv *env, jclass obj, jint nMasterKeyID,
                                  jint nUserKeyID, jbyteArray arryCipherNewUserKey,
                                  jint nCipherNewUserKeyLength) {
    hal_sys_info("+ native_pinpad_update_user_key");
    int nResult = ERR_NORMAL;
    if (g_pPinpadInstance == NULL)
        return ERR_NOT_OPENED;
    if (g_pPinpadInstance->calculate_mac == NULL)
        return ERR_NO_IMPLEMENT;

    if (arryCipherNewUserKey == NULL)
        return ERR_INVALID_ARGUMENT;
    jbyte *pCipherNewUserKey = env->GetByteArrayElements(arryCipherNewUserKey, 0);
    nResult = g_pPinpadInstance->update_user_key(nMasterKeyID, nUserKeyID,
                                                 (unsigned char *) pCipherNewUserKey,
                                                 nCipherNewUserKeyLength);
    env->ReleaseByteArrayElements(arryCipherNewUserKey, pCipherNewUserKey, 0);
    hal_sys_info("- native_pinpad_update_user_key,result=%d", nResult);
    return nResult;
}

int native_pinpad_update_user_key_with_check(JNIEnv *env, jclass obj,
                                             jint nMasterKeyID, jint nUserKeyID,
                                             jbyteArray arryCipherNewUserKey,
                                             jint nCipherNewUserKeyLength, jint nUserKeyType,
                                             jbyteArray arryCheckValue, jint nCheckValueLength) {
    hal_sys_info("+ native_pinpad_update_user_key_with_check");
    int nResult = ERR_NORMAL;
    if (g_pPinpadInstance == NULL)
        return ERR_NOT_OPENED;
    if (g_pPinpadInstance->update_user_key_with_check == NULL)
        return ERR_NO_IMPLEMENT;

    jbyte *pCipherNewUserKey = env->GetByteArrayElements(arryCipherNewUserKey, 0);
    jbyte *pCheckValue = env->GetByteArrayElements(arryCheckValue, 0);
    nResult = g_pPinpadInstance->update_user_key_with_check(nMasterKeyID,
                                                            nUserKeyID,
                                                            (unsigned char *) pCipherNewUserKey,
                                                            nCipherNewUserKeyLength, nUserKeyType,
                                                            (unsigned char *) pCheckValue,
                                                            nCheckValueLength);
    env->ReleaseByteArrayElements(arryCipherNewUserKey, pCipherNewUserKey, 0);
    env->ReleaseByteArrayElements(arryCheckValue, pCheckValue, 0);
    hal_sys_info("-native_pinpad_update_user_key_with_check,result=%d", nResult);
    return nResult;
}

int native_pinpad_update_master_key(JNIEnv *env, jclass obj, jint nMasterKeyID,
                                    jbyteArray arrayOldKey, jint nOldKeyLength,
                                    jbyteArray arrayNewKey,
                                    jint nNewKeyLength) {
    hal_sys_info("+native_pinpad_update_master_key");
    int nResult = ERR_NORMAL;
    if (g_pPinpadInstance == NULL)
        return ERR_NOT_OPENED;
    if (g_pPinpadInstance->update_master_key == NULL)
        return ERR_NO_IMPLEMENT;

    jbyte *pOldKey = env->GetByteArrayElements(arrayOldKey, 0);
    jbyte *pNewKey = env->GetByteArrayElements(arrayNewKey, 0);

    nResult = g_pPinpadInstance->update_master_key(nMasterKeyID,
                                                   (unsigned char *) pOldKey, nOldKeyLength,
                                                   (unsigned char *) pNewKey,
                                                   nNewKeyLength);

    env->ReleaseByteArrayElements(arrayOldKey, pOldKey, 0);
    env->ReleaseByteArrayElements(arrayNewKey, pNewKey, 0);
    hal_sys_info("-native_pinpad_update_master_key,result=%d", nResult);
    return nResult;
}

int native_pinpad_set_pin_length(JNIEnv *env, jclass obj, jint nLength,
                                 jint nFlag) {
    hal_sys_info("+ native_pinpad_set_pin_length");
    int nResult = ERR_NORMAL;
    if (g_pPinpadInstance == NULL)
        return ERR_NOT_OPENED;
    if (g_pPinpadInstance->set_pin_length == NULL)
        return ERR_NO_IMPLEMENT;
    nResult = g_pPinpadInstance->set_pin_length(nLength, nFlag);
    hal_sys_info("- native_pinpad_set_pin_length,result=%d", nResult);
    return nResult;
}

int native_pinpad_serial_no(JNIEnv *env, jclass obj, jbyteArray arryData) {
    hal_sys_info("+ native_pinpad_serial_no");
    int nResult = ERR_NORMAL;
    if (g_pPinpadInstance == NULL)
        return ERR_NOT_OPENED;
    if (g_pPinpadInstance->get_serial_number == NULL)
        return ERR_NO_IMPLEMENT;
    jbyte *pArryData = env->GetByteArrayElements(arryData, 0);
    int pArryDataLength = env->GetArrayLength(arryData);
    nResult = g_pPinpadInstance->get_serial_number((unsigned char *) pArryData,
                                                   pArryDataLength);
    env->ReleaseByteArrayElements(arryData, pArryData, 0);
    hal_sys_info("- native_pinpad_serial_no,result=%d", nResult);
    return nResult;
}

int native_pinpad_update_cipher_master_key(JNIEnv *env, jclass obj,
                                           jint nMasterKeyID, jbyteArray pCipherMasterKey,
                                           jint nCipherMasterKeyLen, jbyteArray pCheckValue,
                                           jint nCheckValueLen) {
    hal_sys_info("+ native_pinpad_update_cipher_master_key");
    int nResult = ERR_NORMAL;
    if (g_pPinpadInstance == NULL)
        return ERR_NOT_OPENED;
    if (g_pPinpadInstance->update_cipher_master_key == NULL)
        return ERR_NO_IMPLEMENT;
    jbyte *pArryData = env->GetByteArrayElements(pCipherMasterKey, 0);
    jbyte *pArryValue = env->GetByteArrayElements(pCheckValue, 0);
    nResult = g_pPinpadInstance->update_cipher_master_key(nMasterKeyID,
                                                          (unsigned char *) pArryData,
                                                          nCipherMasterKeyLen,
                                                          (unsigned char *) pArryValue,
                                                          nCheckValueLen);
    env->ReleaseByteArrayElements(pCipherMasterKey, pArryData, 0);
    env->ReleaseByteArrayElements(pCheckValue, pArryValue, 0);
    hal_sys_info("-native_pinpad_update_cipher_master_key,result=%d", nResult);
    return nResult;
}

int native_pinpad_update_cipher_master_key_E(JNIEnv *env, jclass obj,
                                             jint nMasterKeyID, jbyteArray pCipherMasterKey,
                                             jint nCipherMasterKeyLen, jbyteArray pCheckValue,
                                             jint nCheckValueLen,
                                             jint algoCheckValue) {
    hal_sys_info("+ native_pinpad_update_cipher_master_key_E");
    int nResult = ERR_NORMAL;
    if (g_pPinpadInstance == NULL)
        return ERR_NOT_OPENED;
    if (g_pPinpadInstance->update_cipher_master_key_E == NULL)
        return ERR_NO_IMPLEMENT;
    jbyte *pArryData = env->GetByteArrayElements(pCipherMasterKey, 0);
    jbyte *pArryValue = env->GetByteArrayElements(pCheckValue, 0);
    nResult = g_pPinpadInstance->update_cipher_master_key_E(nMasterKeyID,
                                                            (unsigned char *) pArryData,
                                                            nCipherMasterKeyLen,
                                                            (unsigned char *) pArryValue,
                                                            nCheckValueLen, algoCheckValue);
    env->ReleaseByteArrayElements(pCipherMasterKey, pArryData, 0);
    env->ReleaseByteArrayElements(pCheckValue, pArryValue, 0);
    hal_sys_info("- native_pinpad_update_cipher_master_key_E, result = %d",
                 nResult);
    return nResult;
}

int native_pinpad_update_user_key_with_check_E(JNIEnv *env, jclass obj,
                                               jint nMasterKeyID, jint nUserKeyID,
                                               jbyteArray arryCipherNewUserKey,
                                               jint nCipherNewUserKeyLength, jint nUserKeyType,
                                               jbyteArray arryCheckValue, jint nCheckValueLength,
                                               jint algoCheckValue) {
    hal_sys_info("+ native_pinpad_update_user_key_with_check_E");
    int nResult = ERR_NORMAL;
    if (g_pPinpadInstance == NULL)
        return ERR_NOT_OPENED;
    if (g_pPinpadInstance->update_user_key_with_check_E == NULL)
        return ERR_NO_IMPLEMENT;

    jbyte *pCipherNewUserKey = env->GetByteArrayElements(arryCipherNewUserKey, 0);
    jbyte *pCheckValue = env->GetByteArrayElements(arryCheckValue, 0);
    nResult = g_pPinpadInstance->update_user_key_with_check_E(nMasterKeyID,
                                                              nUserKeyID,
                                                              (unsigned char *) pCipherNewUserKey,
                                                              nCipherNewUserKeyLength, nUserKeyType,
                                                              (unsigned char *) pCheckValue,
                                                              nCheckValueLength, algoCheckValue);
    env->ReleaseByteArrayElements(arryCipherNewUserKey, pCipherNewUserKey, 0);
    env->ReleaseByteArrayElements(arryCheckValue, pCheckValue, 0);
    hal_sys_info("-native_pinpad_update_user_key_with_check_E, result = %d", nResult);
    return nResult;
}

void keyevent_notifier(int nCount, int nExtra) {
    hal_sys_info("+keyevent_notifier");
    JNIEnv *env = NULL;
    bool needDetach = false;
    if (g_jvm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        g_jvm->AttachCurrentThread(&env, NULL);
        needDetach = true;
    } else {
        hal_sys_error("Callback is running in java thread!!!");
    }

    jbyteArray elemt = env->NewByteArray(2);

    jbyte data[2];
    data[0] = (jbyte) nCount;
    data[1] = (jbyte) nExtra;

    env->SetByteArrayRegion(elemt, 0, 2, (const jbyte *) data);
    env->CallStaticVoidMethod(mcls, mmid, elemt);

    env->DeleteLocalRef(elemt);
    if (needDetach) {
        g_jvm->DetachCurrentThread();
    }
    hal_sys_info("-keyevent_notifier");
}

//return value: -1 has not find lib; -2 has not find pinpad_set_pinblock_callback in lib; -3 has not find PinpadCallback in Java code
int native_pinpad_set_pinblock_callback(JNIEnv *env, jclass obj) {
    hal_sys_info("+ native_pinpad_set_pinblock_callback()");
    if (g_pPinpadInstance == NULL)
        return ERR_NOT_OPENED;

    if (g_pPinpadInstance->set_pinblock_callback == NULL)
        return ERR_NO_IMPLEMENT;

    if (mcls == NULL)
        mcls = (jclass) env->NewGlobalRef(env->FindClass(g_pJNIREG_CLASS));
    mmid = env->GetStaticMethodID(mcls, "pinpadCallback", "([B)V");
    if (mmid == NULL)
        return -3;
    hal_sys_error("Do pinpad set_pinblock_callback");
    int nResult = g_pPinpadInstance->set_pinblock_callback(keyevent_notifier);
    if (nResult < 0) {
        hal_sys_error("error in set callback\n");
        nResult = -1;
    }
    hal_sys_info("- native_pinpad_set_pinblock_callback(),result = %d",
                 nResult);
    return nResult;
}


int native_pinpad_set_flag_allow_bypass_pin(JNIEnv *env, jclass obj, jint flag) {
    hal_sys_info("+ native_pinpad_set_flag_allow_bypass_pin");
    int nResult = ERR_NORMAL;
    if (g_pPinpadInstance == NULL)
        return ERR_NOT_OPENED;
    if (g_pPinpadInstance->set_flag_allow_bypass_pin == NULL)
        return ERR_NO_IMPLEMENT;

    nResult = g_pPinpadInstance->set_flag_allow_bypass_pin(flag);
    hal_sys_info("-native_pinpad_set_flag_allow_bypass_pin, result = %d", nResult);
    return nResult;
}

int native_pinpad_encrypt_string_with_mode(JNIEnv *env, jclass obj, jbyteArray pPlainText,
                                           jbyteArray pCipherTextBuffer,
                                           jint nMode, jbyteArray pIV, jint nIVLen) {
    hal_sys_info("+ native_pinpad_encrypt_string_with_mode");
    int nResult = ERR_NORMAL;
    if (g_pPinpadInstance == NULL)
        return ERR_NOT_OPENED;
    if (g_pPinpadInstance->encrypt_string_with_mode == NULL)
        return ERR_NO_IMPLEMENT;

    jbyte *pArrayPlainText = env->GetByteArrayElements(pPlainText, 0);
    jbyte *pArrayCipherTextBuffer = env->GetByteArrayElements(pCipherTextBuffer, 0);
    jbyte *pArrayIV;
    if (pIV == NULL) {
        pArrayIV = NULL;
    } else {
        pArrayIV = env->GetByteArrayElements(pIV, 0);
    }

    int nPlainText = env->GetArrayLength(pPlainText);
    int nCipherTextBuffer = env->GetArrayLength(pCipherTextBuffer);

    nResult = g_pPinpadInstance->encrypt_string_with_mode((unsigned char *) pArrayPlainText,
                                                          nPlainText,
                                                          (unsigned char *) pArrayCipherTextBuffer,
                                                          nCipherTextBuffer,
                                                          (unsigned int) nMode,
                                                          (unsigned char *) pArrayIV,
                                                          (unsigned int) nIVLen);

    env->ReleaseByteArrayElements(pPlainText, pArrayPlainText, 0);
    env->ReleaseByteArrayElements(pCipherTextBuffer, pArrayCipherTextBuffer, 0);
    if (pIV != NULL) {
        env->ReleaseByteArrayElements(pIV, pArrayIV, 0);
    }

    hal_sys_info("- native_pinpad_encrypt_string_with_mode, result = %d", nResult);
    return nResult;
}

int native_pinpad_get_hwserialno(JNIEnv *env, jclass obj, jbyteArray pResultBuffer) {
    hal_sys_info("+ native_pinpad_get_hwserialno");
    int nResult = ERR_NORMAL;
    if (g_pPinpadInstance == NULL)
        return ERR_NOT_OPENED;
    jbyte *pArrayResultBuffer = env->GetByteArrayElements(pResultBuffer, 0);
    int nResultBufferLength = env->GetArrayLength(pResultBuffer);
    hal_sys_info("+= native_pinpad_get_hwserialno %p ", g_pPinpadInstance->get_hwserialno);
    nResult = g_pPinpadInstance->get_hwserialno((unsigned char *) pArrayResultBuffer,
                                                (unsigned int) nResultBufferLength);
//	unsigned char strbuff[64];
//	nResult = g_pPinpadInstance->get_hwserialno(strbuff, 64);
    hal_sys_info("-= native_pinpad_get_hwserialno");
    env->ReleaseByteArrayElements(pResultBuffer, pArrayResultBuffer, 0);
    hal_sys_info("- native_pinpad_get_hwserialno, result = %d", nResult);
    return nResult;
}

int native_pinpad_get_mac_for_snk(JNIEnv *env, jclass obj, jbyteArray pUniqueSNbuffer,
                                  jbyteArray pRandomBuffer, jbyteArray pResultBuffer) {
    hal_sys_info("+ native_pinpad_get_mac_for_snk");
    int nResult = ERR_NORMAL;
    if (g_pPinpadInstance == NULL)
        return ERR_NOT_OPENED;
    jbyte *pArrayUniqueSNBuffer = env->GetByteArrayElements(pUniqueSNbuffer, 0);
    int nUniqueSNbufferLength = env->GetArrayLength(pUniqueSNbuffer);
    jbyte *pArrayRandomBuffer = env->GetByteArrayElements(pRandomBuffer, 0);
    int nRandomBufferLength = env->GetArrayLength(pRandomBuffer);
    jbyte *pArrayResultBuffer = env->GetByteArrayElements(pResultBuffer, 0);
    int nResultBufferLength = env->GetArrayLength(pResultBuffer);

    nResult = g_pPinpadInstance->get_mac_for_snk(
            (unsigned char *) pArrayUniqueSNBuffer, nUniqueSNbufferLength,
            (unsigned char *) pArrayRandomBuffer, nRandomBufferLength,
            (unsigned char *) pArrayResultBuffer, nResultBufferLength);

    env->ReleaseByteArrayElements(pUniqueSNbuffer, pArrayUniqueSNBuffer, 0);
    env->ReleaseByteArrayElements(pRandomBuffer, pArrayRandomBuffer, 0);
    env->ReleaseByteArrayElements(pResultBuffer, pArrayResultBuffer, 0);

    hal_sys_info("- native_pinpad_get_mac_for_snk, result = %d", nResult);
    return nResult;
}

void as_unsigned_char_array(JNIEnv *env, unsigned char *destArray, const jbyteArray srcArray) {
    jbyte *pArraySrcArray = env->GetByteArrayElements(srcArray, 0);
    int srcLength = env->GetArrayLength(srcArray);
    memcpy((void *) destArray, (const void *) pArraySrcArray,
           srcLength); // @suppress("Invalid arguments")
    env->ReleaseByteArrayElements(srcArray, pArraySrcArray, 0);

}
////////////////////////////////////////////////////////////////////////////////////////

int native_pinpad_get_mk_checkvalue(JNIEnv *env, jclass obj, jint nMKID, jint nAlgo,
                                    jbyteArray pCheckValueBuf, jint nCheckValueBufLen) {
    hal_sys_info("+ native_pinpad_get_mk_checkvalue");
    int nResult = ERR_NORMAL;
    if (g_pPinpadInstance == NULL)
        return ERR_NOT_OPENED;
    jbyte *pArrayCheckValueBuf = env->GetByteArrayElements(pCheckValueBuf, 0);

    nResult = g_pPinpadInstance->get_mk_checkvalue((unsigned int) nMKID, (unsigned int) nAlgo,
                                                   (unsigned char *) pArrayCheckValueBuf,
                                                   (unsigned int) nCheckValueBufLen);

    if (nResult > 0) {
        hal_sys_dump("CheckValueInJNI,get_mk_checkvalue", (unsigned char *) pArrayCheckValueBuf,
                     nResult);
    }

    env->ReleaseByteArrayElements(pCheckValueBuf, pArrayCheckValueBuf, 0);

    hal_sys_info("- native_pinpad_get_mk_checkvalue, result = %d", nResult);
    return nResult;
}

int native_pinpad_get_sk_checkvalue(JNIEnv *env, jclass obj, jint nMKID, jint nSKID, jint nAlgo,
                                    jbyteArray pCheckValueBuf, jint nCheckValueBufLen) {
    hal_sys_info("+ native_pinpad_get_sk_checkvalue");
    int nResult = ERR_NORMAL;
    if (g_pPinpadInstance == NULL)
        return ERR_NOT_OPENED;
    jbyte *pArrayCheckValueBuf = env->GetByteArrayElements(pCheckValueBuf, 0);

    nResult = g_pPinpadInstance->get_sk_checkvalue((unsigned int) nMKID, (unsigned int) nSKID,
                                                   (unsigned int) nAlgo,
                                                   (unsigned char *) pArrayCheckValueBuf,
                                                   (unsigned int) nCheckValueBufLen);
    if (nResult > 0) {
        hal_sys_dump("CheckValueInJNI,get_mk_checkvalue", (unsigned char *) pArrayCheckValueBuf,
                     nResult);
    }

    env->ReleaseByteArrayElements(pCheckValueBuf, pArrayCheckValueBuf, 0);

    hal_sys_info("- native_pinpad_get_sk_checkvalue, result = %d", nResult);
    return nResult;
}

int native_pinpad_verify_response_mac(JNIEnv *env, jclass obj, jbyteArray pData, jint nDataLen,
                                      jint nMacFlag, jbyteArray pMacData, jint nMacDataLen,
                                      jint nDirection) {
    hal_sys_info("+ native_pinpad_verify_response_mac");
    int nResult = ERR_NORMAL;
    if (g_pPinpadInstance == NULL)
        return ERR_NOT_OPENED;
    jbyte *pArrayData = env->GetByteArrayElements(pData, 0);
    jbyte *pArrayMacData = env->GetByteArrayElements(pMacData, 0);
    nResult = g_pPinpadInstance->verify_response_mac((unsigned char *) pArrayData,
                                                     (unsigned int) nDataLen,
                                                     (unsigned int) nMacFlag,
                                                     (unsigned char *) pArrayMacData,
                                                     (unsigned int) nMacDataLen,
                                                     (unsigned int) nDirection);
    env->ReleaseByteArrayElements(pData, pArrayData, 0);
    env->ReleaseByteArrayElements(pMacData, pArrayMacData, 0);
    hal_sys_info("- native_pinpad_verify_response_mac, result = %d", nResult);
    return nResult;
}

int native_pinpad_set_gui_configuration(JNIEnv *env, jclass obj, jint nFlag, jbyteArray pConfigData,
                                        jint nDataLen) {
    hal_sys_info("+ native_pinpad_set_gui_configuration");
    int nResult = ERR_NORMAL;
    if (g_pPinpadInstance == NULL)
        return ERR_NOT_OPENED;
    jbyte *pArrayData = env->GetByteArrayElements(pConfigData, 0);
    nResult = g_pPinpadInstance->set_gui_configuration(
            (unsigned int) nFlag,
            (unsigned char *) pArrayData,
            (unsigned int) nDataLen);
    env->ReleaseByteArrayElements(pConfigData, pArrayData, 0);
    hal_sys_info("- native_pinpad_set_gui_configuration, result = %d", nResult);
    return nResult;
}

static JNINativeMethod g_Methods[] = {
        {"open",                    "()I",           (void *) native_pinpad_open},
        {"close",                   "()I",           (void *) native_pinpad_close},
        {"showText",                "(I[BII)I",      (void *) native_pinpad_show_text},
        {"selectKey",               "(IIII)I",       (void *) native_pinpad_select_key},
        {"setPinLength",            "(II)I",         (void *) native_pinpad_set_pin_length},
        {"encrypt",                 "([BI[B)I",      (void *) native_pinpad_encrypt_string},
        {"calculatePINBlock",       "([BI[BII)I",    (void *) native_pinpad_calculate_pin_block},
        {"calculateMac",            "([BII[B)I",     (void *) native_pinpad_calculate_mac},
        {"updateUserKey",           "(II[BI)I",      (void *) native_pinpad_update_user_key},
        {"getSerialNo",             "([B)I",         (void *) native_pinpad_serial_no},
        {"updateUserKeyWithCheck",  "(II[BII[BI)I",  (void *) native_pinpad_update_user_key_with_check},
        {"updateMasterKey",         "(I[BI[BI)I",    (void *) native_pinpad_update_master_key},
        {"setPinblockCallback",     "()I",           (void *) native_pinpad_set_pinblock_callback},
        {"updateCipherMasterKey",   "(I[BI[BI)I",    (void *) native_pinpad_update_cipher_master_key},
        {"updateUserKeyWithCheckE", "(II[BII[BII)I", (void *) native_pinpad_update_user_key_with_check_E},
        {"updateCipherMasterKeyE",  "(I[BI[BII)I",   (void *) native_pinpad_update_cipher_master_key_E},
        {"setAllowBypassPinFlag",   "(I)I",          (void *) native_pinpad_set_flag_allow_bypass_pin},
        {"encryptWithMode",         "([B[BI[BI)I",   (void *) native_pinpad_encrypt_string_with_mode},
        {"getHwserialno",           "([B)I",         (void *) native_pinpad_get_hwserialno},
        {"getMacForSnk",            "([B[B[B)I",     (void *) native_pinpad_get_mac_for_snk},
        {"getMKCheckValue",         "(II[BI)I",      (void *) native_pinpad_get_mk_checkvalue},
        {"getSKCheckValue",         "(III[BI)I",     (void *) native_pinpad_get_sk_checkvalue},
        {"verifyResponseMac",       "([BII[BII)I",   (void *) native_pinpad_verify_response_mac},
        {"setGUIConfiguration",     "(I[BI)I",       (void *) native_pinpad_set_gui_configuration},
};


const char *pinpad_get_class_name() {
    return g_pJNIREG_CLASS;
}

//	+add by pengli
const char *get_class_name_internal() {
    return g_pJNIREG_CLASS_INTERNAL;
}

//	-add by pengli
JNINativeMethod *pinpad_get_methods(int *pCount) {
    *pCount = sizeof(g_Methods) / sizeof(g_Methods[0]);
    return g_Methods;
}
