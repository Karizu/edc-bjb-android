#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <dlfcn.h>
#include <semaphore.h>
#include <unistd.h>
#include <errno.h>
#include <pthread.h>
#include <jni.h>

#include <time.h>

#include "hal_sys_log.h"
#include "iso_fingerprint_jni_interface.h"
#include "iso_fingerprint_interface.h"

const char *g_pJNIREG_CLASS = "com/cloudpos/jniinterface/IsoFingerPrintInterface";
//	+add by pengli
const char *g_pJNIREG_CLASS_INTERNAL =
        "com/wizarpos/internal/jniinterface/IsoFingerPrintInterface";
//	-add by pengli


typedef struct fingerprint_interface {
    fp_iso_open open;
    fp_iso_close close;
    fp_iso_cancel cancel;
    fp_iso_match match;
    fp_iso_enroll enroll;
    fp_iso_verifyall verifyall;
    fp_iso_delallfingers delallfingers;
    fp_iso_delfinger delfinger;
    fp_iso_verifyagainstuserid verifyagainstuserid;
    fp_iso_verifyagainstfeature verifyagainstfeature;
    fp_iso_listallfingers listallfingers;
    fp_iso_getuserfeature getuserfeature;
    fp_iso_storefeature storefeature;
    fp_iso_get_fea get_fea_ext;
    fp_iso_getImage getImage;
    fp_iso_convertformat convertformat;
    fp_iso_getid getid;

    void *pHandle;
} FINGERPRINT_INSTANCE;

static FINGERPRINT_INSTANCE *g_pFingerPrintInstance = NULL;
static int ERR_NOT_OPENED = -255;
static int ERR_HAS_OPENED = -254;
static int ERR_NO_IMPLEMENT = -253;
static int ERR_INVALID_ARGUMENT = -252;
static int ERR_NORMAL = -251;

pthread_mutex_t pthread_mute;

void throw_exception(JNIEnv *env, const char *method_name) {
    hal_sys_info("invoke throw_exception() method_name = %s", method_name);
    char strData[32] = {0};
    const char *pString = "not found ";
    hal_sys_info("invoke throw_exception() 0");
    env->ExceptionDescribe();
    hal_sys_info("invoke throw_exception() 1");
    jclass newExcCls = env->FindClass("java/lang/NoSuchMethodException");
    if (0 != newExcCls) {
        hal_sys_info("invoke throw_exception() 2");
        sprintf(strData, "%s%s", pString, method_name);
        env->ThrowNew(newExcCls, strData);
        hal_sys_info("invoke throw_exception() end");
    }
}

#define assign(member, type, name) if (NULL == (g_pFingerPrintInstance->member = (type) dlsym(pHandle, methodName = name))) { hal_sys_error("can't find %s", methodName); }

void initOptinalMembers(void *pHandle) {
    char *methodName;
    assign(getid, fp_iso_getid, "fp_iso_getid");
    assign(enroll, fp_iso_enroll, "fp_iso_enroll");
    assign(cancel, fp_iso_cancel, "fp_iso_cancel");
    assign(getImage, fp_iso_getImage, "fp_iso_getImage");
    assign(delfinger, fp_iso_delfinger, "fp_iso_delfinger");
    assign(verifyall, fp_iso_verifyall, "fp_iso_verifyall");
    assign(get_fea_ext, fp_iso_get_fea, "fp_iso_get_fea");
    assign(storefeature, fp_iso_storefeature, "fp_iso_storefeature");
    assign(convertformat, fp_iso_convertformat, "fp_iso_convertformat");
    assign(delallfingers, fp_iso_delallfingers, "fp_iso_delallfingers");
    assign(listallfingers, fp_iso_listallfingers, "fp_iso_listallfingers");
    assign(getuserfeature, fp_iso_getuserfeature, "fp_iso_getuserfeature");
    assign(verifyagainstuserid, fp_iso_verifyagainstuserid, "fp_iso_verifyagainstuserid");
    assign(verifyagainstfeature, fp_iso_verifyagainstfeature, "fp_iso_verifyagainstfeature");
}

int native_fingerprint_open(JNIEnv *env, jclass obj) {
    hal_sys_info("+ native_iso_fingerprint_open");
    int nResult = ERR_HAS_OPENED;
    if (g_pFingerPrintInstance == NULL) {
        //										  libwizarposDriver.so
        void *pHandle = dlopen("/system/lib/libwizarposDriver.so", RTLD_LAZY);
        if (!pHandle) {
            hal_sys_error("%s\n", dlerror());
            return ERR_NORMAL;
        }
        g_pFingerPrintInstance = new FINGERPRINT_INSTANCE();
        g_pFingerPrintInstance->pHandle = pHandle;
        char * methodName;
        if (NULL == (g_pFingerPrintInstance->open = (fp_iso_open) dlsym(pHandle,methodName = "fp_iso_open"))
            || NULL == (g_pFingerPrintInstance->close = (fp_iso_close) dlsym(pHandle,methodName = "fp_iso_close"))
            || NULL == (g_pFingerPrintInstance->match = (fp_iso_match) dlsym(pHandle,methodName = "fp_iso_match"))) {
            hal_sys_error("can't find %s", methodName);
            nResult = ERR_NO_IMPLEMENT;
            goto fingerprint_init_clean;
        }
        initOptinalMembers(pHandle);
        nResult = g_pFingerPrintInstance->open();
        if (nResult < 0) {
            goto fingerprint_init_clean;
        }
    }
    hal_sys_info("- native_iso_fingerprint_open, result = %d", nResult);
    return nResult;

    fingerprint_init_clean:
    hal_sys_info("iso_fingerprint_init_clean");
    dlclose(g_pFingerPrintInstance->pHandle);
    delete g_pFingerPrintInstance;
    g_pFingerPrintInstance = NULL;
    hal_sys_info("- native_iso_fingerprint_open, result = %d", nResult);
    return nResult;
}

int native_fingerprint_close(JNIEnv *env, jclass obj) {
    hal_sys_info("+ native_iso_fingerprint_close");
    pthread_mutex_lock(&pthread_mute);
    int nResult = ERR_NORMAL;
    if (g_pFingerPrintInstance == NULL) {
        pthread_mutex_unlock(&pthread_mute);
        return ERR_NOT_OPENED;
    }
    nResult = g_pFingerPrintInstance->close();
    dlclose(g_pFingerPrintInstance->pHandle);
    delete g_pFingerPrintInstance;
    g_pFingerPrintInstance = NULL;
    pthread_mutex_unlock(&pthread_mute);
    hal_sys_info("- native_iso_fingerprint_close, result = %d", nResult);
    return nResult;
}

int native_fingerprint_cancel(JNIEnv * env, jclass obj) {
    hal_sys_info("+ native_iso_fingerprint_cancel");
    int nResult = ERR_NORMAL;
    if (g_pFingerPrintInstance == NULL)
        return ERR_NOT_OPENED;
    nResult = g_pFingerPrintInstance->cancel();
    hal_sys_info("- native_iso_fingerprint_cancel, result = %d", nResult);
    return nResult;
}

int native_fingerprint_match(JNIEnv *env, jclass obj, jbyteArray pFeaBuffer1,
                             jint nFea1Length, jbyteArray pFeaBuffer2, jint nFea2Length) {
    hal_sys_info("+ native_iso_fingerprint_match");
    int nResult = ERR_NORMAL;
    if (g_pFingerPrintInstance == NULL)
        return ERR_NOT_OPENED;
    jbyte *bFeaBuffer1 = env->GetByteArrayElements(pFeaBuffer1, NULL);
    jbyte *bFeaBuffer2 = env->GetByteArrayElements(pFeaBuffer2, NULL);
    nResult = g_pFingerPrintInstance->match((unsigned char *) bFeaBuffer1,
                                            nFea1Length, (unsigned char *) bFeaBuffer2,
                                            nFea2Length);
    env->ReleaseByteArrayElements(pFeaBuffer1, bFeaBuffer1, 0);
    env->ReleaseByteArrayElements(pFeaBuffer2, bFeaBuffer2, 0);
    hal_sys_info("- native_iso_fingerprint_match, result = %d", nResult);
    return nResult;
}


int native_fingerprint_enroll(JNIEnv *env, jclass obj, jint nUserId, jint nTimeOut_Ms) {
    hal_sys_info("+ native_iso_fingerprint_enroll");
    int nResult = ERR_NORMAL;
    if (g_pFingerPrintInstance == NULL)
        return ERR_NOT_OPENED;
    if (NULL == (g_pFingerPrintInstance->enroll)) {
        throw_exception(env, "enroll");
    }
    nResult = g_pFingerPrintInstance->enroll(nUserId, nTimeOut_Ms);
    hal_sys_info("- native_iso_fingerprint_enroll, result = %d", nResult);
    return nResult;
}

int native_fingerprint_verifyAll(JNIEnv *env, jclass obj, jint nTimeOut_Ms) {
    hal_sys_info("+ native_iso_fingerprint_verifyAll");
    int nResult = ERR_NORMAL;
    if (g_pFingerPrintInstance == NULL)
        return ERR_NOT_OPENED;
    if (NULL == (g_pFingerPrintInstance->verifyall)) {
        throw_exception(env, "verifyAll");
    }
    nResult = g_pFingerPrintInstance->verifyall(nTimeOut_Ms);
    hal_sys_info("- native_iso_fingerprint_verifyAll, result = %d", nResult);
    return nResult;
}

int native_fingerprint_delAllFingers(JNIEnv *env, jclass obj) {
    hal_sys_info("+ native_iso_fingerprint_delAllFingers");
    int nResult = ERR_NORMAL;
    if (g_pFingerPrintInstance == NULL)
        return ERR_NOT_OPENED;
    if (NULL == (g_pFingerPrintInstance->delallfingers)) {
        throw_exception(env, "delAllFingers");
    }
    nResult = g_pFingerPrintInstance->delallfingers();
    hal_sys_info("- native_iso_fingerprint_delAllFingers, result = %d", nResult);
    return nResult;
}

int native_fingerprint_delFinger(JNIEnv *env, jclass obj, jint nUserId) {
    hal_sys_info("+ native_iso_fingerprint_delFinger");
    int nResult = ERR_NORMAL;
    if (g_pFingerPrintInstance == NULL)
        return ERR_NOT_OPENED;
    if (NULL == (g_pFingerPrintInstance->delfinger)) {
        throw_exception(env, "delFinger");
    }
    nResult = g_pFingerPrintInstance->delfinger(nUserId);
    hal_sys_info("- native_iso_fingerprint_delFinger, result = %d", nResult);
    return nResult;
}

int
native_fingerprint_verifyAgainstUserId(JNIEnv *env, jclass obj, jint nUserId, jint nTimeOut_Ms) {
    hal_sys_info("+ native_iso_fingerprint_verifyAgainstUserId");
    int nResult = ERR_NORMAL;
    if (g_pFingerPrintInstance == NULL)
        return ERR_NOT_OPENED;
    if (NULL == (g_pFingerPrintInstance->verifyagainstuserid)) {
        throw_exception(env, "verifyAgainstUserId");
    }
    nResult = g_pFingerPrintInstance->verifyagainstuserid(nUserId, nTimeOut_Ms);
    hal_sys_info("- native_iso_fingerprint_verifyAgainstUserId, result = %d", nResult);
    return nResult;
}

int native_fingerprint_verifyAgainstFeature(JNIEnv *env, jclass obj, jbyteArray pFeature,
                                            jint nTimeOut_Ms) {
    hal_sys_info("+ native_iso_fingerprint_verifyAgainstFeature");
    int nResult = ERR_NORMAL;
    if (g_pFingerPrintInstance == NULL)
        return ERR_NOT_OPENED;
    if (NULL == (g_pFingerPrintInstance->verifyagainstfeature)) {
        throw_exception(env, "verifyAgainstFeature");
    }
    jbyte *bFeaBuffer = env->GetByteArrayElements(pFeature, NULL);
    int length = env->GetArrayLength(pFeature);
    nResult = g_pFingerPrintInstance->verifyagainstfeature((unsigned char *) bFeaBuffer, length,
                                                           nTimeOut_Ms);
    env->ReleaseByteArrayElements(pFeature, bFeaBuffer, 0);
    hal_sys_info("- native_iso_fingerprint_verifyAgainstFeature, result = %d", nResult);
    return nResult;
}

int native_fingerprint_listAllFingers(JNIEnv *env, jclass obj, jintArray pData) {
    hal_sys_info("+ native_iso_fingerprint_listAllFingers");
    int nResult = ERR_NORMAL;
    if (g_pFingerPrintInstance == NULL)
        return ERR_NOT_OPENED;
    if (NULL == (g_pFingerPrintInstance->listallfingers)) {
        throw_exception(env, "listAllFingerStatus");// 获得存在指纹的ids
    }
    jint *bData = env->GetIntArrayElements(pData, NULL);
    int length = env->GetArrayLength(pData);
    int pRealFeaLength = -1;
    nResult = g_pFingerPrintInstance->listallfingers((unsigned char *) bData, length,
                                                     &pRealFeaLength);
    env->ReleaseIntArrayElements(pData, bData, 0);
    hal_sys_info("- native_iso_fingerprint_listAllFingers, result = %d", nResult);
    return nResult;
}

int native_fingerprint_getUserFeature(JNIEnv *env, jclass obj, int nUserId, jbyteArray pFeature,
                                      jint type) {
    hal_sys_info("+ native_iso_fingerprint_getUserFeature");
    int nResult = ERR_NORMAL;
    if (g_pFingerPrintInstance == NULL)
        return ERR_NOT_OPENED;
    if (NULL == (g_pFingerPrintInstance->getuserfeature)) {
        throw_exception(env, "getUserFeature");
    }
    jbyte *bFeaBuffer = env->GetByteArrayElements(pFeature, NULL);
    int length = env->GetArrayLength(pFeature);
    int pRealFeaLength = -1;
    nResult = g_pFingerPrintInstance->getuserfeature(nUserId, (unsigned char *) bFeaBuffer, length,
                                                     &pRealFeaLength, type);
    hal_sys_info("iso_getUserFeature result= %d, realLength = %d", nResult, pRealFeaLength);
    env->ReleaseByteArrayElements(pFeature, bFeaBuffer, 0);
    if (nResult >= 0) {
        nResult = pRealFeaLength;
    }
    hal_sys_info("- native_iso_fingerprint_getUserFeature, result = %d", nResult);
    return nResult;
}

int native_fingerprint_storeFeature(JNIEnv *env, jclass obj, int nUserId, jbyteArray pFeature) {
    hal_sys_info("+ native_iso_fingerprint_storeFeature");
    int nResult = ERR_NORMAL;
    if (g_pFingerPrintInstance == NULL)
        return ERR_NOT_OPENED;
    if (NULL == (g_pFingerPrintInstance->storefeature)) {
        throw_exception(env, "storeFeature");
    }
    jbyte *bFeaBuffer = env->GetByteArrayElements(pFeature, NULL);
    int length = env->GetArrayLength(pFeature);
    nResult = g_pFingerPrintInstance->storefeature(nUserId, (unsigned char *) bFeaBuffer, length);
    env->ReleaseByteArrayElements(pFeature, bFeaBuffer, 0);
    hal_sys_info("- native_iso_fingerprint_storeFeature, result = %d", nResult);
    return nResult;
}

int native_fingerprint_getFeaExt(JNIEnv *env, jclass obj, jbyteArray pFeature, jint n_TimeOut_S,
                                 jint nType) {
    hal_sys_info("+ native_iso_fingerprint_getFeaExt");
    int nResult = ERR_NORMAL;
    if (g_pFingerPrintInstance == NULL)
        return ERR_NOT_OPENED;
    if (NULL == (g_pFingerPrintInstance->get_fea_ext)) {
        throw_exception(env, "getFeaExt");
    }
    jbyte *bFeaBuffer = env->GetByteArrayElements(pFeature, NULL);
    int length = env->GetArrayLength(pFeature);
    int pRealFeaLength = -1;
    nResult = g_pFingerPrintInstance->get_fea_ext((unsigned char *) bFeaBuffer, length,
                                                  &pRealFeaLength, n_TimeOut_S, nType);
    env->ReleaseByteArrayElements(pFeature, bFeaBuffer, 0);
//	hal_sys_info("get_fea_ext result=" + nResult + ", realLength = " + pRealFeaLength);
    if (nResult >= 0) {
        nResult = pRealFeaLength;
    }
    hal_sys_info("- native_iso_fingerprint_getFeaExt, result = %d", nResult);
    return nResult;
}

int native_fingerprint_getImage(JNIEnv *env, jclass obj, jbyteArray pImgBuffer, jint pImgWidth,
                                jint pImgHeight, jint ntype) {
    hal_sys_info("+ native_iso_fingerprint_getImage");
    int nResult = ERR_NORMAL;
    if (g_pFingerPrintInstance == NULL)
        return ERR_NOT_OPENED;
    if (NULL == (g_pFingerPrintInstance->getImage)) {
        throw_exception(env, "getImage");
    }
    jbyte *bImgBuffer = env->GetByteArrayElements(pImgBuffer, NULL);
    int length = env->GetArrayLength(pImgBuffer);
    int pRealFeaLength = -1;
    nResult = g_pFingerPrintInstance->getImage((unsigned char *) bImgBuffer, length,
                                               &pRealFeaLength, &pImgWidth, &pImgHeight, ntype);
    env->ReleaseByteArrayElements(pImgBuffer, bImgBuffer, 0);
//	hal_sys_info("getImage result = " + nResult + ", realLength = " + pRealFeaLength + ", width = " + pImgWidth + ", height = " + pImgHeight);
    if (nResult >= 0) {
        nResult = pRealFeaLength;
    }
    hal_sys_info("- native_iso_fingerprint_getImage, result = %d", nResult);
    return nResult;
}

int native_fingerprint_convertFormat(JNIEnv *env, jclass obj, jbyteArray pFeaIn, jint ntypeIn,
                                     jbyteArray pFeaOut, int nTypeOut) {
    hal_sys_info("+ native_iso_fingerprint_convertFormat");
    int nResult = ERR_NORMAL;
    if (g_pFingerPrintInstance == NULL)
        return ERR_NOT_OPENED;
    if (NULL == (g_pFingerPrintInstance->convertformat)) {
        throw_exception(env, "convertFormat");
    }
    jbyte *bFeaIn = env->GetByteArrayElements(pFeaIn, NULL);
    int feaInlength = env->GetArrayLength(pFeaIn);
    jbyte *bFeaOut = env->GetByteArrayElements(pFeaOut, NULL);
    int feaOutlength = env->GetArrayLength(pFeaOut);

    int pRealFeaLength = -1;
    nResult = g_pFingerPrintInstance->convertformat((unsigned char *) bFeaIn, feaInlength, ntypeIn,
                                                    (unsigned char *) bFeaOut, feaOutlength,
                                                    &pRealFeaLength, nTypeOut);
    hal_sys_info("- native_iso_fingerprint_convertFormat, result = %d, pRealFeaLength=%d", nResult,
                 pRealFeaLength);
    env->ReleaseByteArrayElements(pFeaIn, bFeaIn, 0);
    env->ReleaseByteArrayElements(pFeaOut, bFeaOut, 0);
//	hal_sys_info("convertFormat result=" + nResult + ", realLength = " + pRealFeaLength );
    if (nResult >= 0) {
        nResult = pRealFeaLength;
    }
    hal_sys_info("- native_iso_fingerprint_convertFormat, result = %d", nResult);
    return nResult;
}

int native_fingerprint_getId(JNIEnv *env, jclass obj) {
    hal_sys_info("+ native_iso_fingerprint_getId");
    int nResult = ERR_NORMAL;
    if (g_pFingerPrintInstance == NULL)
        return ERR_NOT_OPENED;
    if (NULL == (g_pFingerPrintInstance->getid)) {
        throw_exception(env, "getId");
    }
    nResult = g_pFingerPrintInstance->getid();
    hal_sys_info("- native_iso_fingerprint_getId, result = %d", nResult);
    return nResult;
}

jboolean native_is_opened(JNIEnv *env, jclass obj) {
    jboolean isOpened = JNI_FALSE;
    hal_sys_info("native_iso_is_opened() is called\n");
    if (g_pFingerPrintInstance != NULL) {
        isOpened = JNI_TRUE;
    }
    hal_sys_info("native_iso_is_opened() end result = %d", isOpened);
    return isOpened;
}

static JNINativeMethod g_Methods[] = {
        {"open",                 "()I",       (void *) native_fingerprint_open},
        {"close",                "()I",       (void *) native_fingerprint_close},
        {"cancel",               "()I",       (void *) native_fingerprint_cancel},
        {"match",                "([BI[BI)I", (void *) native_fingerprint_match},
        {"enroll",               "(II)I",     (void *) native_fingerprint_enroll},
        {"verifyAll",            "(I)I",      (void *) native_fingerprint_verifyAll},
        {"delAllFingers",        "()I",       (void *) native_fingerprint_delAllFingers},
        {"delFinger",            "(I)I",      (void *) native_fingerprint_delFinger},
        {"verifyAgainstUserId",  "(II)I",     (void *) native_fingerprint_verifyAgainstUserId},
        {"verifyAgainstFeature", "([BI)I",    (void *) native_fingerprint_verifyAgainstFeature},
        {"listAllFingersStatus", "([I)I",     (void *) native_fingerprint_listAllFingers},
        {"getUserFeature",       "(I[BI)I",   (void *) native_fingerprint_getUserFeature},
        {"storeFeature",         "(I[B)I",    (void *) native_fingerprint_storeFeature},
        {"getFeaExt",            "([BII)I",   (void *) native_fingerprint_getFeaExt},
        {"getImage",             "([BIII)I",  (void *) native_fingerprint_getImage},
        {"convertFormat",        "([BI[BI)I", (void *) native_fingerprint_convertFormat},
        {"getId",                "()I",       (void *) native_fingerprint_getId},
        {"isOpened",             "()Z",       (void *) native_is_opened},

};

const char *iso_fingerprint_get_class_name() {
    return g_pJNIREG_CLASS;
}

const char *get_class_name_internal() {
    return g_pJNIREG_CLASS_INTERNAL;
}

JNINativeMethod *iso_fingerprint_get_methods(int *pCount) {
    *pCount = sizeof(g_Methods) / sizeof(g_Methods[0]);
    return g_Methods;
}
