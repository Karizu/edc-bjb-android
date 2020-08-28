#include "../terminal/terminal_jni_interface.h"

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

#include "../terminal/terminal_service_interface.h"
#include "hal_sys_log.h"

const char* g_pJNIREG_CLASS = "com/cloudpos/jniinterface/TerminalStatusInterface";
//	+add by pengli
const char* g_pJNIREG_CLASS_INTERNAL = "com/wizarpos/internal/jniinterface/TerminalStatusInterface";
//	-add by pengli


typedef struct terminal_interface {
	GET_CARD_STATISTIC get_card_statistic;
	void* pHandle;
} TERMINAL_INSTANCE;

static int ERR_HAS_OPENED = -254;
static int ERR_NORMAL = -251;
void throw_exception(JNIEnv* env, const char* method_name){
	char strData[32] = {0};
	const char* pString = "not found ";
	jthrowable exc = env->ExceptionOccurred();
	if (exc) {
		env->ExceptionDescribe();
		jclass newExcCls = env->FindClass("java/lang/NoSuchMethodException");
		if(0 != newExcCls){
			sprintf(strData, "%s%s", pString, method_name);
			env->ThrowNew(newExcCls, strData);
		}
	}
}

int native_usage_count(JNIEnv * env, jclass obj, int card_type) {
	hal_sys_info("+ native_usage_count , %d", card_type);
	int nResult = ERR_HAS_OPENED;
	TERMINAL_INSTANCE* g_pTerminalInstance = new TERMINAL_INSTANCE();
	void* pHandle = dlopen("/system/lib/libwizarposDriver.so", RTLD_LAZY);
	if (!pHandle) {
		hal_sys_error("%s\n", dlerror());
		return ERR_NORMAL;
	}
	g_pTerminalInstance->pHandle = pHandle;

	if (NULL == (g_pTerminalInstance->get_card_statistic = (GET_CARD_STATISTIC) dlsym(pHandle, "get_card_statistic"))) {
		throw_exception(env, "get_card_statistic");
	}
	unsigned int count = 0;
	hal_sys_info("0 native_usage_count ");
	nResult = g_pTerminalInstance->get_card_statistic(card_type, 0, &count);
	hal_sys_info("1 native_usage_count ");
	dlclose(pHandle);
	delete g_pTerminalInstance;
	g_pTerminalInstance = NULL;
	hal_sys_info("- native_usage_count, result = %d, count=%d.", nResult, count);
	if(nResult >=0){
		return count;
	}
	return nResult;
}


int native_fail_count(JNIEnv * env, jclass obj) {

	return 0;
}
static JNINativeMethod g_Methods[] = {
		{ "getUsageCount", 			"(I)I", 	(void*) native_usage_count },
		{ "getFailCount", 			"(I)I",		(void*) native_fail_count }
};

const char* get_class_name() {
	return g_pJNIREG_CLASS;
}
const char* get_class_name_internal()
{
	return g_pJNIREG_CLASS_INTERNAL;
}

JNINativeMethod* get_methods(int* pCount) {
	*pCount = sizeof(g_Methods) / sizeof(g_Methods[0]);
	return g_Methods;
}
