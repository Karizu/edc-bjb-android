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
#include "ac5_service_interface.h"
#include "ac5_jni_interface.h"

const char* g_pJNIREG_CLASS = "com/cloudpos/jniinterface/AC5Interface";
const char* g_pJNIREG_CLASS_INTERNAL = "com/wizarpos/internal/jniinterface/AC5Interface";


typedef struct ac5_interface {
	AC5_TOUCH touch;
	AC5_SELFTEST selftest;
	void* pHandle;
} AC5_INSTANCE;

static AC5_INSTANCE* g_pAC5Instance = NULL;
static int ERR_NOT_OPENED = -255;
static int ERR_HAS_OPENED = -254;
static int ERR_NO_IMPLEMENT = -253;
static int ERR_INVALID_ARGUMENT = -252;
static int ERR_NORMAL = -251;

pthread_mutex_t pthread_mute;

int native_ac5_open(JNIEnv * env, jclass obj) {
	hal_sys_info("+ native_ac5_open");
	int nResult = ERR_HAS_OPENED;
	if (g_pAC5Instance == NULL) {
		void* pHandle = dlopen("/system/lib/libwizarposDriver.so", RTLD_LAZY);
		if (!pHandle) {
			hal_sys_error("%s\n", dlerror());
			return ERR_NORMAL;
		}
		g_pAC5Instance = new AC5_INSTANCE();
		g_pAC5Instance->pHandle = pHandle;

		char * methodName;
		if (NULL == (g_pAC5Instance->touch = (AC5_TOUCH) dlsym(pHandle, methodName = "ac5_touch"))
				|| NULL == (g_pAC5Instance->selftest = (AC5_SELFTEST) dlsym(pHandle, methodName = "ac5_selftest"))) {
			hal_sys_error("can't find %s", methodName);
			nResult = ERR_NO_IMPLEMENT;
			goto ac5_open_clean;
		}

		nResult = 0;
	}
	hal_sys_info("- native_ac5_open, result = %d", nResult);
	return nResult;

	ac5_open_clean:
		hal_sys_info("ac5_open_clean");
		delete g_pAC5Instance;
		g_pAC5Instance = NULL;
	hal_sys_info("- native_ac5_open, result = %d", nResult);
	return nResult;
}

int native_ac5_touch(JNIEnv * env, jclass obj) {
	hal_sys_info("+ native_ac5_touch");
	int nResult = ERR_NORMAL;
	if (g_pAC5Instance == NULL) {
		return ERR_NOT_OPENED;
	}
	nResult = g_pAC5Instance->touch();

	hal_sys_info("- native_ac5_touch, result = %d", nResult);
	return nResult;
}

int native_ac5_selftest(JNIEnv * env, jclass obj) {
	hal_sys_info("+ native_ac5_selftest");
	int nResult = ERR_NORMAL;
	if (g_pAC5Instance == NULL) {
		return ERR_NOT_OPENED;
	}
	nResult = g_pAC5Instance->selftest();

	hal_sys_info("- native_ac5_selftest, result = %d", nResult);
	return nResult;
}

int native_ac5_close(JNIEnv * env, jclass obj) {
	hal_sys_info("+ native_ac5_close");
	pthread_mutex_lock(&pthread_mute);
	int nResult = ERR_NORMAL;
	if (g_pAC5Instance == NULL) {
		pthread_mutex_unlock(&pthread_mute);
		return ERR_NOT_OPENED;
	}
	nResult = 0;
	dlclose(g_pAC5Instance->pHandle);
	delete g_pAC5Instance;
	g_pAC5Instance = NULL;
	pthread_mutex_unlock(&pthread_mute);
	hal_sys_info("- native_ac5_close, result = %d", nResult);
	return nResult;
}

static JNINativeMethod g_Methods[] = {
		{ "open", 			"()I", 		(void*) native_ac5_open },
		{ "touch", 			"()I", 		(void*) native_ac5_touch },
		{ "selftest", 		"()I", 		(void*) native_ac5_selftest },
		{ "close", 			"()I",		(void*) native_ac5_close } };

const char* ac5_get_class_name() {
	return g_pJNIREG_CLASS;
}
const char* get_class_name_internal()
{
	return g_pJNIREG_CLASS_INTERNAL;
}

JNINativeMethod* ac5_get_methods(int* pCount) {
	*pCount = sizeof(g_Methods) / sizeof(g_Methods[0]);
	return g_Methods;
}
