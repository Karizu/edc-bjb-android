#include <fcntl.h>
#include <dlfcn.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/stat.h>
#include "emv_kernal_interface.h"
#include "emv_kernal_jni_interface.h"
#include <android/log.h>
#include "hal_sys_log.h"


EMV_KERNEL_INSTANCE* g_emv_kernel_instance = NULL;

JavaVM *g_jvm2 = NULL;
jobject g_obj2 = NULL;
jclass g_cls = NULL;

const char* g_pJNIREG_CLASS = "com/cloudpos/jniinterface/EMVJNIInterface";

static void detachThread() {
	//Detach主线程
	if (g_jvm2->DetachCurrentThread() != JNI_OK) {
		hal_sys_error("DetachCurrentThread() failed");
	}
	hal_sys_error("DetachCurrentThread() OK");
}

jbyte native_load(JNIEnv * env, jclass obj,jbyteArray atr,jint atrlength)
{

	char *pError = NULL;

	jbyteArray aryBuffer = env->NewByteArray(atrlength+1);
	jbyte* tempPath = env->GetByteArrayElements(aryBuffer, 0);
	memset(tempPath,0,atrlength+1);
	jbyte* bData = env->GetByteArrayElements(atr, NULL);
	memcpy(tempPath,bData,atrlength);

	hal_sys_error("Kernalpath:%s\n", tempPath);
    hal_sys_error("g_pJNIREG_CLASS:%s\n", g_pJNIREG_CLASS);

	if(g_emv_kernel_instance == NULL)
	{
		void* pHandle = dlopen((const char *)tempPath, RTLD_LAZY);
		env->ReleaseByteArrayElements(atr, bData, 0);
		env->ReleaseByteArrayElements(aryBuffer, tempPath, 0);
		if (!pHandle)
		{
			hal_sys_error("can't open emv kernel: %s\n", dlerror());
			return -2;
		}

		g_emv_kernel_instance = new EMV_KERNEL_INSTANCE();
		g_emv_kernel_instance->pHandle = pHandle;

		// card functions
		g_emv_kernel_instance->open_reader = (OPEN_READER)dlsym(pHandle, "open_reader");
		if(g_emv_kernel_instance->open_reader == NULL)
		{
			hal_sys_error("can't open open_reader: %s\n", pError);
			return -1;
		}

        g_emv_kernel_instance->open_reader_ex = (OPEN_READER_EX)dlsym(pHandle, "open_reader_ex");
		if (g_emv_kernel_instance->open_reader_ex == NULL)
        {
            hal_sys_error("can't open open_reader_ex: %s\n", pError);
            return -1;
        }

		g_emv_kernel_instance->close_reader = (CLOSE_READER)dlsym(pHandle, "close_reader");
		if(g_emv_kernel_instance->close_reader == NULL)
		{
			hal_sys_error("can't open close_reader: %s\n", pError);
			return -1;
		}

		g_emv_kernel_instance->poweron_card = (POWERON_CARD)dlsym(pHandle, "poweron_card");
		if(g_emv_kernel_instance->poweron_card == NULL)
		{
			hal_sys_error("can't open poweron_card: %s\n", pError);
			return -1;
		}

		g_emv_kernel_instance->get_card_type = (GET_CARD_TYPE)dlsym(pHandle, "get_card_type");
		if(g_emv_kernel_instance->get_card_type == NULL)
		{
			hal_sys_error("can't open get_card_type: %s\n", pError);
			return -1;
		}

		g_emv_kernel_instance->get_card_atr = (GET_CARD_ATR)dlsym(pHandle, "get_card_atr");
		if(g_emv_kernel_instance->get_card_atr == NULL)
		{
			hal_sys_error("can't open get_card_atr: %s\n", pError);
			return -1;
		}

		g_emv_kernel_instance->transmit_card = (TRANSMIT_CARD)dlsym(pHandle, "transmit_card");
		if(g_emv_kernel_instance->transmit_card == NULL)
		{
			hal_sys_error("can't open transmit_card: %s\n", pError);
			return -1;
		}
		g_emv_kernel_instance->query_contact_card_presence = (QUERY_CONTACT_CARD_PRESENCE)dlsym(pHandle, "query_contact_card_presence");

		// EMV Functions
		// 0
		g_emv_kernel_instance->emv_kernel_initialize = (EMV_KERNEL_INITIALIZE)dlsym(pHandle, "emv_kernel_initialize");
		if(g_emv_kernel_instance->emv_kernel_initialize == NULL)
		{
			hal_sys_error("can't open emv_kernel_initialize: %s\n", pError);
			return -1;
		}

		// 1
		g_emv_kernel_instance->emv_is_tag_present = (EMV_IS_TAG_PRESENT)dlsym(pHandle, "emv_is_tag_present");
		if(g_emv_kernel_instance->emv_is_tag_present == NULL)
		{
			hal_sys_error("can't open emv_is_tag_present: %s\n", pError);
			return -1;
		}

		// 2
		g_emv_kernel_instance->emv_get_tag_data = (EMV_GET_TAG_DATA)dlsym(pHandle, "emv_get_tag_data");
		if(g_emv_kernel_instance->emv_get_tag_data == NULL)
		{
			hal_sys_error("can't open emv_get_tag_data: %s\n", pError);
			return -1;
		}

		// 3
		g_emv_kernel_instance->emv_get_tag_list_data = (EMV_GET_TAG_LIST_DATA)dlsym(pHandle, "emv_get_tag_list_data");
		if(g_emv_kernel_instance->emv_get_tag_list_data == NULL)
		{
			hal_sys_error("can't open emv_get_tag_list_data: %s\n", pError);
			return -1;
		}

		// 4
		g_emv_kernel_instance->emv_set_tag_data = (EMV_SET_TAG_DATA)dlsym(pHandle, "emv_set_tag_data");
		if(g_emv_kernel_instance->emv_set_tag_data == NULL)
		{
			hal_sys_error("can't open emv_set_tag_data: %s\n", pError);
			return -1;
		}

		// 5
		g_emv_kernel_instance->emv_preprocess_qpboc = (EMV_PREPROCESS_QPBOC)dlsym(pHandle, "emv_preprocess_qpboc");
		if(g_emv_kernel_instance->emv_preprocess_qpboc == NULL)
		{
			hal_sys_error("can't open emv_preprocess_qpboc: %s\n", pError);
			return -1;
		}

		// 6
		g_emv_kernel_instance->emv_trans_initialize = (EMV_TRANS_INITIALIZE)dlsym(pHandle, "emv_trans_initialize");
		if(g_emv_kernel_instance->emv_trans_initialize == NULL)
		{
			hal_sys_error("can't open emv_trans_initialize: %s\n", pError);
			return -1;
		}

		// 7
		g_emv_kernel_instance->emv_get_version_string = (EMV_GET_VERSION_STRING)dlsym(pHandle, "emv_get_version_string");
		if(g_emv_kernel_instance->emv_get_version_string == NULL)
		{
			hal_sys_error("can't open emv_get_version_string: %s\n", pError);
			return -1;
		}

		// 8
		g_emv_kernel_instance->emv_set_trans_amount = (EMV_SET_TRANS_AMOUNT)dlsym(pHandle, "emv_set_trans_amount");
		if(g_emv_kernel_instance->emv_set_trans_amount == NULL)
		{
			hal_sys_error("can't open emv_set_trans_amount: %s\n", pError);
			return -1;
		}

		// 9
		g_emv_kernel_instance->emv_set_other_amount = (EMV_SET_OTHER_AMOUNT)dlsym(pHandle, "emv_set_other_amount");
		if(g_emv_kernel_instance->emv_set_other_amount == NULL)
		{
			hal_sys_error("can't open emv_set_other_amount: %s\n", pError);
			return -1;
		}

		// 10
		g_emv_kernel_instance->emv_set_trans_type = (EMV_SET_TRANS_TYPE)dlsym(pHandle, "emv_set_trans_type");
		if(g_emv_kernel_instance->emv_set_trans_type == NULL)
		{
			hal_sys_error("can't open emv_set_trans_type: %s\n", pError);
			return -1;
		}

		// 11
		g_emv_kernel_instance->emv_set_kernel_type = (EMV_SET_KERNEL_TYPE)dlsym(pHandle, "emv_set_kernel_type");
		if(g_emv_kernel_instance->emv_set_kernel_type == NULL)
		{
			hal_sys_error("can't open emv_set_kernel_type: %s\n", pError);
			return -1;
		}
		g_emv_kernel_instance->emv_get_kernel_type = (EMV_GET_KERNEL_TYPE)dlsym(pHandle, "emv_get_kernel_type");


		// 12
		g_emv_kernel_instance->emv_process_next = (EMV_PROCESS_NEXT)dlsym(pHandle, "emv_process_next");
		if(g_emv_kernel_instance->emv_process_next == NULL)
		{
			hal_sys_error("can't open emv_process_next: %s\n", pError);
			return -1;
		}

		// 13
		g_emv_kernel_instance->emv_is_need_advice = (EMV_IS_NEED_ADVICE)dlsym(pHandle, "emv_is_need_advice");
		if(g_emv_kernel_instance->emv_is_need_advice == NULL)
		{
			hal_sys_error("can't open emv_is_need_advice: %s\n", pError);
			return -1;
		}

		// 14
		g_emv_kernel_instance->emv_is_need_signature = (EMV_IS_NEED_SIGNATURE)dlsym(pHandle, "emv_is_need_signature");
		if(g_emv_kernel_instance->emv_is_need_signature == NULL)
		{
			hal_sys_error("can't open emv_is_need_signature: %s\n", pError);
			return -1;
		}

		// 15
		g_emv_kernel_instance->emv_set_force_online = (EMV_SET_FORCE_ONLINE)dlsym(pHandle, "emv_set_force_online");
		if(g_emv_kernel_instance->emv_set_force_online == NULL)
		{
			hal_sys_error("can't open emv_set_force_online: %s\n", pError);
			return -1;
		}

		// 16
		g_emv_kernel_instance->emv_get_card_record = (EMV_GET_CARD_RECORD)dlsym(pHandle, "emv_get_card_record");
		if(g_emv_kernel_instance->emv_get_card_record == NULL)
		{
			hal_sys_error("can't open emv_get_card_record: %s\n", pError);
			return -1;
		}

		// 17
		g_emv_kernel_instance->emv_get_candidate_list = (EMV_GET_CANDIDATE_LIST)dlsym(pHandle, "emv_get_candidate_list");
		if(g_emv_kernel_instance->emv_get_candidate_list == NULL)
		{
			hal_sys_error("can't open emv_get_candidate_list: %s\n", pError);
			return -1;
		}

        g_emv_kernel_instance->emv_get_candidate_list_tlv = (EMV_GET_CANDIDATE_LIST_TLV)dlsym(pHandle, "emv_get_candidate_list_tlv");
        if(g_emv_kernel_instance->emv_get_candidate_list_tlv == NULL)
        {
            hal_sys_error("can't open emv_get_candidate_list_tlv: %s\n", pError);
            return -1;
        }

		// 18
		g_emv_kernel_instance->emv_set_candidate_list_result = (EMV_SET_CANDIDATE_LIST_RESULT)dlsym(pHandle, "emv_set_candidate_list_result");
		if(g_emv_kernel_instance->emv_set_candidate_list_result == NULL)
		{
			hal_sys_error("can't open emv_set_candidate_list_result: %s\n", pError);
			return -1;
		}

		// 19
		g_emv_kernel_instance->emv_set_id_check_result = (EMV_SET_ID_CHECK_RESULT)dlsym(pHandle, "emv_set_id_check_result");
		if(g_emv_kernel_instance->emv_set_id_check_result == NULL)
		{
			hal_sys_error("can't open emv_set_id_check_result: %s\n", pError);
			return -1;
		}

		// 20
		g_emv_kernel_instance->emv_set_online_pin_entered = (EMV_SET_ONLINE_PIN_ENTERED)dlsym(pHandle, "emv_set_online_pin_entered");
		if(g_emv_kernel_instance->emv_set_online_pin_entered == NULL)
		{
			hal_sys_error("can't open emv_set_online_pin_entered: %s\n", pError);
			return -1;
		}

        g_emv_kernel_instance->emv_set_bypass_pin = (EMV_SET_BYPASS_PIN)dlsym(pHandle, "emv_set_bypass_pin");
        if(g_emv_kernel_instance->emv_set_bypass_pin == NULL)
        {
            hal_sys_error("can't open emv_set_bypass_pin: %s\n", pError);
            return -1;
        }

		// 22
		g_emv_kernel_instance->emv_set_online_result = (EMV_SET_ONLINE_RESULT)dlsym(pHandle, "emv_set_online_result");
		if(g_emv_kernel_instance->emv_set_online_result == NULL)
		{
			hal_sys_error("can't open emv_set_online_result: %s\n", pError);
			return -1;
		}

		// 23
		g_emv_kernel_instance->emv_aidparam_clear = (EMV_AIDPARAM_CLEAR)dlsym(pHandle, "emv_aidparam_clear");
		if(g_emv_kernel_instance->emv_aidparam_clear == NULL)
		{
			hal_sys_error("can't open emv_aidparam_clear: %s\n", pError);
			return -1;
		}

		// 24
		g_emv_kernel_instance->emv_aidparam_add = (EMV_AIDPARAM_ADD)dlsym(pHandle, "emv_aidparam_add");
		if(g_emv_kernel_instance->emv_aidparam_add == NULL)
		{
			hal_sys_error("can't open emv_aidparam_add: %s\n", pError);
			return -1;
		}

        g_emv_kernel_instance->emv_contactless_aidparam_clear = (EMV_CONTACTLESS_AIDPARAM_CLEAR)dlsym(pHandle, "emv_contactless_aidparam_clear");
        if(g_emv_kernel_instance->emv_contactless_aidparam_clear == NULL)
        {
            hal_sys_error("can't open emv_contactless_aidparam_clear: %s\n", pError);
            return -1;
        }
        g_emv_kernel_instance->emv_contactless_aidparam_add = (EMV_CONTACTLESS_AIDPARAM_ADD)dlsym(pHandle, "emv_contactless_aidparam_add");
        if(g_emv_kernel_instance->emv_contactless_aidparam_add == NULL)
        {
            hal_sys_error("can't open emv_contactless_aidparam_add: %s\n", pError);
            return -1;
        }
		// 25
		g_emv_kernel_instance->emv_capkparam_clear = (EMV_CAPKPARAM_CLEAR)dlsym(pHandle, "emv_capkparam_clear");
		if(g_emv_kernel_instance->emv_capkparam_clear == NULL)
		{
			hal_sys_error("can't open emv_capkparam_clear: %s\n", pError);
			return -1;
		}

		// 26
		g_emv_kernel_instance->emv_capkparam_add = (EMV_CAPKPARAM_ADD)dlsym(pHandle, "emv_capkparam_add");
		if(g_emv_kernel_instance->emv_capkparam_add == NULL)
		{
			hal_sys_error("can't open emv_capkparam_add: %s\n", pError);
			return -1;
		}

		// 27
		g_emv_kernel_instance->emv_terminal_param_set_tlv = (EMV_TERMINAL_PARAM_SET_TLV)dlsym(pHandle, "emv_terminal_param_set_tlv");
		if(g_emv_kernel_instance->emv_terminal_param_set_tlv == NULL)
		{
			hal_sys_error("can't open emv_terminal_param_set_tlv: %s\n", pError);
			return -1;
		}
		g_emv_kernel_instance->emv_terminal_param_set_drl = (EMV_TERMINAL_PARAM_SET_DRL)dlsym(pHandle, "emv_terminal_param_set_drl");

		// 28
		g_emv_kernel_instance->emv_exception_file_clear = (EMV_EXCEPTION_FILE_CLEAR)dlsym(pHandle, "emv_exception_file_clear");
		if(g_emv_kernel_instance->emv_exception_file_clear == NULL)
		{
			hal_sys_error("can't open emv_exception_file_clear: %s\n", pError);
			return -1;
		}

		// 29
		g_emv_kernel_instance->emv_exception_file_add = (EMV_EXCEPTION_FILE_ADD)dlsym(pHandle, "emv_exception_file_add");
		if(g_emv_kernel_instance->emv_exception_file_add == NULL)
		{
			hal_sys_error("can't open emv_exception_file_add: %s\n", pError);
			return -1;
		}

		// 30
		g_emv_kernel_instance->emv_revoked_cert_clear = (EMV_REVOKED_CERT_CLEAR)dlsym(pHandle, "emv_revoked_cert_clear");
		if(g_emv_kernel_instance->emv_revoked_cert_clear == NULL)
		{
			hal_sys_error("can't open emv_revoked_cert_clear: %s\n", pError);
			return -1;
		}

		// 31
		g_emv_kernel_instance->emv_revoked_cert_add = (EMV_REVOKED_CERT_ADD)dlsym(pHandle, "emv_revoked_cert_add");
		if(g_emv_kernel_instance->emv_revoked_cert_add == NULL)
		{
			hal_sys_error("can't open emv_revoked_cert_add: %s\n", pError);
			return -1;
		}

		// 32
		g_emv_kernel_instance->emv_log_file_clear = (EMV_LOG_FILE_CLEAR)dlsym(pHandle, "emv_log_file_clear");
		if(g_emv_kernel_instance->emv_log_file_clear == NULL)
		{
			hal_sys_error("can't open emv_log_file_clear: %s\n", pError);
			return -1;
		}

		g_emv_kernel_instance->emv_set_kernel_attr = (EMV_SET_KERNEL_ATTR)dlsym(pHandle, "emv_set_kernel_attr");
        if(g_emv_kernel_instance->emv_set_kernel_attr == NULL)
        {
            hal_sys_error("can't open emv_set_kernel_attr: %s\n", pError);
            return -1;
        }
		g_emv_kernel_instance->set_contactless_detach_enable = (SET_CONTACTLESS_DETACH_ENABLE) dlsym(pHandle, "set_contactless_detach_enable");
		if(g_emv_kernel_instance->set_contactless_detach_enable == NULL)
		{
			hal_sys_error("can't open set_contactless_detach_enable: %s\n", pError);
			return -1;
		}

		g_emv_kernel_instance->emv_set_currency_symble = (EMV_SET_CURRENCY_SYMBLE)dlsym(pHandle, "emv_set_currency_symbol");
		if (g_emv_kernel_instance->emv_set_currency_symble == NULL)
		{
			hal_sys_error("can't open emv_set_currency_symble: %s\n", pError);
			return -1;
		}

		g_emv_kernel_instance->emv_set_anti_shake = reinterpret_cast<EMV_SET_ANTI_SHAKE>(dlsym(pHandle, "emv_set_anti_shake"));
		if (g_emv_kernel_instance->emv_set_anti_shake == NULL)
		{
			hal_sys_error("can't open emv_set_anti_shake:%s\n", pError);
			return -1;
		}

		g_emv_kernel_instance->emv_anti_shake_finish = reinterpret_cast<EMV_ANTI_SHAKE_FINISH>(dlsym(pHandle, "emv_anti_shake_finish"));
		if(g_emv_kernel_instance->emv_anti_shake_finish == NULL)
		{
			hal_sys_error("can't open emv_anti_shake_finish:%s\n", pError);
			return -1;
		}

		g_emv_kernel_instance->emv_set_force_aac = (EMV_SET_FORCE_AAC)dlsym(pHandle, "emv_set_force_aac");
		if(g_emv_kernel_instance->emv_set_force_aac == NULL)
		{
			hal_sys_error("can't open emv_set_force_aac: %s\n", pError);
			return -1;
		}

		g_emv_kernel_instance->emv_generate_pseudo_track1 = (EMV_GENERATE_PSEUDO_TRACK1)dlsym(pHandle, "emv_generate_pseudo_track1");
		if(g_emv_kernel_instance->emv_generate_pseudo_track1 == NULL)
		{
			hal_sys_error("can't open emv_generate_pseudo_track1: %s\n", pError);
			return -1;
		}

		g_emv_kernel_instance->emv_generate_pseudo_track2 = (EMV_GENERATE_PSEUDO_TRACK2)dlsym(pHandle, "emv_generate_pseudo_track2");
		if(g_emv_kernel_instance->emv_generate_pseudo_track2 == NULL)
		{
			hal_sys_error("can't open emv_generate_pseudo_track2: %s\n", pError);
			return -1;
		}

		g_emv_kernel_instance->emv_get_kernel_id = (EMV_GET_KERNEL_ID)dlsym(pHandle, "emv_get_kernel_id");
		if (g_emv_kernel_instance->emv_get_kernel_id == NULL)
		{
			hal_sys_error("can't open emv_get_kernel_id: %s\n", pError);
			return -1;
		}

		g_emv_kernel_instance->emv_get_process_type = (EMV_GET_PROCESS_TYPE)dlsym(pHandle, "emv_get_process_type");
		if (g_emv_kernel_instance->emv_get_process_type == NULL)
		{
			hal_sys_error("can't open emv_get_process_type: %s\n", pError);
			return -1;
		}
        g_emv_kernel_instance->emv_offlinepin_verified = (EMV_OFFLINEPIN_VERIFIED)dlsym(pHandle, "emv_offlinepin_verified");
		if (g_emv_kernel_instance->emv_offlinepin_verified == NULL)
		{
			hal_sys_error("can't open emv_offlinepin_verified: %s\n", pError);
			return -1;
		}
		g_emv_kernel_instance->emv_get_offlinepin_times = (EMV_GET_OFFLINEPIN_TIMES)dlsym(pHandle, "emv_get_offlinepin_times");
		if (g_emv_kernel_instance->emv_get_offlinepin_times == NULL)
		{
			hal_sys_error("can't open emv_get_offlinepin_times: %s\n", pError);
			return -1;
		}
		g_emv_kernel_instance->pinpad_set_keyevent_callback = (PINPAD_SET_KEYEVENT_CALLBACK)dlsym(pHandle, "pinpad_set_keyevent_callback");
		g_emv_kernel_instance->emv_get_kernel_checksum = reinterpret_cast<EMV_GET_KERNEL_CHECKSUM >(dlsym(pHandle, "emv_get_kernel_checksum"));
		g_emv_kernel_instance->emv_get_config_checksum = reinterpret_cast<EMV_GET_CONFIG_CHECKSUM >(dlsym(pHandle, "emv_get_config_checksum"));
	    g_emv_kernel_instance->set_display_language = reinterpret_cast<SET_DISPLAY_LANGUAGE >(dlsym(pHandle, "set_display_language"));
	}
	g_emv_kernel_instance->g_jni_env = env;
	g_emv_kernel_instance->g_jni_obj = obj;
	env->GetJavaVM(&g_jvm2);
	g_obj2 = env->NewGlobalRef(obj);
	jclass cls = env->FindClass((char*)g_pJNIREG_CLASS);
	g_cls = reinterpret_cast<jclass>(env->NewGlobalRef(cls));
	//g_cls = (jclass)env->NewGlobalRef(cls);
	if (g_cls == NULL)
	{
		hal_sys_error("FindClass() Error.....");
	}
	env->DeleteLocalRef(cls);
	return 0;
}

jbyte native_close(JNIEnv * env, jclass obj)
{
	if(g_emv_kernel_instance == NULL)
		return -1;
	dlclose(g_emv_kernel_instance->pHandle);
	delete g_emv_kernel_instance;
	g_emv_kernel_instance = NULL;
	return 0;
}

// 回调函数
static void emvProcessCallback(uchar* data)
{
	JNIEnv *env;
	jclass cls;

	if (g_jvm2->AttachCurrentThread(&env, NULL) != JNI_OK)
	{
		hal_sys_error("%s: AttachCurrentThread() failed", __FUNCTION__);
		return;
	}
	jmethodID method = env->GetStaticMethodID(g_cls, "emvProcessCallback", "([B)V");
	if (env->ExceptionCheck()) {
		hal_sys_error("jni can't find java emvProcessCallback");
        env->ExceptionDescribe();
		detachThread();
		return;
	}

	jbyteArray aryBuffer = env->NewByteArray(2);
	char * aryChar = (char*)env->GetByteArrayElements(aryBuffer, 0);
	aryChar[0] = data[0];
	aryChar[1] = data[1];
	env->ReleaseByteArrayElements(aryBuffer, (jbyte*) aryChar, 0);

	env->CallStaticVoidMethod( g_cls, method, aryBuffer);
	if (env->ExceptionCheck()) {
		hal_sys_error("jni can't call java emvProcessCallback");
		env->ExceptionDescribe();
		detachThread();
		return;
	}
}

static void cardEventOccured(int eventType)
{
	JNIEnv *env;
	if (g_jvm2->AttachCurrentThread(&env, NULL) != JNI_OK)
	{
		hal_sys_error("%s: AttachCurrentThread() failed", __FUNCTION__);
		return;
	}
	jmethodID method = env->GetStaticMethodID(g_cls, "cardEventOccured", "(I)V");
	if (env->ExceptionCheck()) {
		hal_sys_error("jni can't find java cardEventOccured");
		env->ExceptionDescribe();
		detachThread();
	   return;
	}
	env->CallStaticVoidMethod( g_cls, method,eventType);
	if (env->ExceptionCheck()) {
		hal_sys_error("jni can't call java cardEventOccured");
		env->ExceptionDescribe();
		detachThread();
		return;
	}
	detachThread();
}

void keyevent_notifier(int nCount, int nExtra)
{
    JNIEnv *env = NULL;

    hal_sys_error("keyevent_notifier %d, %d\n", nCount, nExtra);

    bool needDetach = false;
    if (g_jvm2->GetEnv((void**) &env, JNI_VERSION_1_6) != JNI_OK) {
        g_jvm2->AttachCurrentThread(&env, NULL);
        needDetach = true;
    } else {
        hal_sys_error("Callback is running in java thread!!!");
    }
    jmethodID mmid = env->GetStaticMethodID(g_cls, "emvOfflinePinCallback","(II)V");
    env->CallStaticVoidMethod(g_cls, mmid, nCount, nExtra);
    if (needDetach) {
        g_jvm2->DetachCurrentThread();
    }
}

// card functions
jint native_open_reader(JNIEnv * env, jclass obj, jint reader)
{
	if(g_emv_kernel_instance == NULL)
	{
		if(g_emv_kernel_instance->open_reader == NULL)
		{
			hal_sys_error("jni invoke g_emv_kernel_instance->open_reader null\n");
		}
		return 0;
	}
	return g_emv_kernel_instance->open_reader(reader);
}

void native_close_reader(JNIEnv *env, jclass obj, jint reader)
{
	g_emv_kernel_instance->close_reader(reader);
}

jint native_poweron_card(JNIEnv *env, jclass obj)
{
	return g_emv_kernel_instance->poweron_card();
}

// card functions
jint native_open_reader_ex(JNIEnv * env, jclass obj, jint reader, jint extraParam)
{
    if (g_emv_kernel_instance == NULL
        || g_emv_kernel_instance->open_reader_ex == NULL)
    {
        return -1;
    }
    return g_emv_kernel_instance->open_reader_ex(reader, extraParam);
}

jint native_get_card_type(JNIEnv *env, jclass obj)
{
	return g_emv_kernel_instance->get_card_type();
}

jint native_get_card_atr(JNIEnv *env, jclass obj, jbyteArray atr)
{
	jbyte* bData = env->GetByteArrayElements(atr, NULL);
	jint iResult = g_emv_kernel_instance->get_card_atr((uchar *)bData);
	env->ReleaseByteArrayElements(atr, bData, 0);
	return iResult;
}

jint native_transmit_card(JNIEnv *env, jclass obj, jbyteArray cmd, jint cmdLength, jbyteArray respData, jint respDataLength)
{
	jbyte* bCmd = env->GetByteArrayElements(cmd, NULL);
	jbyte* bRespData = env->GetByteArrayElements(respData, NULL);
	jint iResult = g_emv_kernel_instance->transmit_card((uchar *)bCmd, cmdLength, (uchar*)bRespData, respDataLength);
	env->ReleaseByteArrayElements(cmd, bCmd, 0);
	env->ReleaseByteArrayElements(respData, bRespData, 0);
	return iResult;
}

jint native_query_contact_card_presence(JNIEnv *env, jclass obj)
{
	if(g_emv_kernel_instance->query_contact_card_presence == NULL)
	{
		return -1;
	}
	return g_emv_kernel_instance->query_contact_card_presence();
}

void native_set_contactless_detach_enable(JNIEnv *env, jclass obj, jint enable)
{
    g_emv_kernel_instance->set_contactless_detach_enable(enable);
}

// pinpad functions
jint native_pinpad_set_keyevent_callback(JNIEnv *env, jclass obj)
{
    hal_sys_error("native_pinpad_set_keyevent_callback");
    if (g_emv_kernel_instance == NULL)
    {
        hal_sys_error("g_emv_kernel_instance is NULL");
        return -1;
    }
    if (g_emv_kernel_instance->pinpad_set_keyevent_callback == NULL)
    {
        hal_sys_error("g_emv_kernel_instance->pinpad_set_keyevent_callback is NULL");
        return -2;
    }

    jclass  mcls = env->FindClass(g_pJNIREG_CLASS);
    jmethodID mmid = env->GetStaticMethodID(mcls, "emvOfflinePinCallback","(II)V");
    if (mmid == NULL)
    {
        hal_sys_error("mmid is NULL");
        return -3;
    }
    hal_sys_error("Do pinpad set_pinblock_callback");
    int nResult = g_emv_kernel_instance->pinpad_set_keyevent_callback(keyevent_notifier);
    if (nResult < 0) {
        hal_sys_error("error in set callback\n");
        nResult = -1;
    }
    return nResult;
}

// emv functions
void native_emv_kernel_initialize(JNIEnv *env, jclass obj)
{
	EMV_INIT_PARAM initParam;
	memset(&initParam,0,sizeof(initParam));
	// 初始化回调函数
	initParam.pCardEventOccured = (CARD_EVENT_OCCURED)cardEventOccured;
	initParam.pEMVProcessCallback = (EMV_PROCESS_CALLBACK)emvProcessCallback;
	g_emv_kernel_instance->emv_kernel_initialize(&initParam);
	return;
}

// 1
jint native_emv_is_tag_present(JNIEnv *env, jclass obj, jint tag)
{
	return g_emv_kernel_instance->emv_is_tag_present(tag);
}

// 2
jint native_emv_get_tag_data(JNIEnv *env, jclass obj, jint tag, jbyteArray data, jint dataLength)
{
	jbyte* bData = env->GetByteArrayElements(data, NULL);
	jint iResult = g_emv_kernel_instance->emv_get_tag_data(tag, (uchar*)bData,dataLength);
	env->ReleaseByteArrayElements(data, bData, 0);
	return iResult;
}

// 3
jint native_emv_get_tag_list_data(JNIEnv *env, jclass obj, jintArray tagList, jint tagCount, jbyteArray data, jint dataLength)
{
	jint* iTagList = env->GetIntArrayElements(tagList, NULL);
	jbyte* bData = env->GetByteArrayElements(data, NULL);
	jint iResult = g_emv_kernel_instance->emv_get_tag_list_data((int*)iTagList, tagCount, (uchar*)bData, dataLength);
	env->ReleaseIntArrayElements(tagList, iTagList, 0);
	env->ReleaseByteArrayElements(data, bData, 0);
	return iResult;
}

// 4
jint native_emv_set_tag_data(JNIEnv *env, jclass obj, jint tag, jbyteArray data, jint dataLength)
{
	jbyte* bData = env->GetByteArrayElements(data, NULL);
	jint iResult = g_emv_kernel_instance->emv_set_tag_data(tag, (uchar*)bData, dataLength);
	env->ReleaseByteArrayElements(data, bData, 0);
	return iResult;
}

// 5
jint native_emv_preprocess_qpboc(JNIEnv *env, jclass obj)
{
	return g_emv_kernel_instance->emv_preprocess_qpboc();
}

// 6
void native_emv_trans_initialize(JNIEnv *env, jclass obj)
{
	EMV_INIT_PARAM initParam;
	memset(&initParam,0,sizeof(initParam));
	// 初始化回调函数
	initParam.pEMVProcessCallback = (EMV_PROCESS_CALLBACK)emvProcessCallback;
	g_emv_kernel_instance->emv_trans_initialize();
	return;
}

// 7
jint native_emv_get_version_string(JNIEnv *env, jclass obj, jbyteArray data, jint dataLength)
{
	jbyte* bData = env->GetByteArrayElements(data, NULL);
	jint iResult = g_emv_kernel_instance->emv_get_version_string((uchar*)bData, dataLength);
	env->ReleaseByteArrayElements(data, bData, 0);
	return iResult;
}

// 8
jint native_emv_set_trans_amount(JNIEnv *env, jclass obj, jbyteArray amount)
{
	jbyte* bAmount = env->GetByteArrayElements(amount, NULL);
	jint iResult = g_emv_kernel_instance->emv_set_trans_amount((uchar*)bAmount);
	env->ReleaseByteArrayElements(amount, bAmount, 0);
	return iResult;
}

// 9
jint native_emv_set_other_amount(JNIEnv *env, jclass obj, jbyteArray amount)
{
	jbyte* bAmount = env->GetByteArrayElements(amount, NULL);
	jint iResult = g_emv_kernel_instance->emv_set_other_amount((uchar*)bAmount);
	env->ReleaseByteArrayElements(amount, bAmount, 0);
	return iResult;
}

// 10
jint native_emv_set_trans_type(JNIEnv *env, jclass obj, jbyte transType)
{
	return g_emv_kernel_instance->emv_set_trans_type(transType);
}

// 11
jint native_emv_set_kernel_type(JNIEnv *env, jclass obj, jbyte kernelType)
{
	return g_emv_kernel_instance->emv_set_kernel_type(kernelType);
}

jint native_emv_get_kernel_type(JNIEnv *env, jclass obj)
{
	if(g_emv_kernel_instance->emv_get_kernel_type == NULL)
	{
		return -1;
	}
	return g_emv_kernel_instance->emv_get_kernel_type();
}

// 12
jint native_emv_process_next(JNIEnv *env, jclass obj)
{
	return g_emv_kernel_instance->emv_process_next();
}

// 13
jint native_emv_is_need_advice(JNIEnv *env, jclass obj)
{
	return g_emv_kernel_instance->emv_is_need_advice();
}

// 14
jint native_emv_is_need_signature(JNIEnv *env, jclass obj)
{
	return g_emv_kernel_instance->emv_is_need_signature();
}

// 15
jint native_emv_set_force_online(JNIEnv *env, jclass obj, jint flag)
{
	return g_emv_kernel_instance->emv_set_force_online(flag);
}

// 16
jint native_emv_get_card_record(JNIEnv *env, jclass obj, jbyteArray data, jint dataLength)
{
	jbyte* bData = env->GetByteArrayElements(data,NULL);
	jint iResult = g_emv_kernel_instance->emv_get_card_record((uchar*)bData, dataLength);
	env->ReleaseByteArrayElements(data,bData,0);

	return iResult;
}

// 17
jint native_emv_get_candidate_list(JNIEnv *env, jclass obj, jbyteArray data, jint dataLength)
{
	jbyte* bData = env->GetByteArrayElements(data,NULL);
	jint iResult = g_emv_kernel_instance->emv_get_candidate_list((uchar*)bData, dataLength);
	env->ReleaseByteArrayElements(data,bData,0);
	return iResult;
}

jint native_emv_get_candidate_list_tlv(JNIEnv *env, jclass obj, jbyteArray data, jint dataLength)
{
    jbyte* bData = env->GetByteArrayElements(data,NULL);
    jint iResult = g_emv_kernel_instance->emv_get_candidate_list_tlv((uchar*)bData, dataLength);
    env->ReleaseByteArrayElements(data,bData,0);
    return iResult;
}

// 18
jint native_emv_set_candidate_list_result(JNIEnv *env, jclass obj, jint index)
{
	return g_emv_kernel_instance->emv_set_candidate_list_result(index);
}

// 19
jint native_emv_set_id_check_result(JNIEnv *env, jclass obj, jint result)
{
	return g_emv_kernel_instance->emv_set_id_check_result(result);
}

// 20
jint native_emv_set_online_pin_entered(JNIEnv *env, jclass obj, jint result)
{
	return g_emv_kernel_instance->emv_set_online_pin_entered(result);
}

jint native_emv_set_bypass_pin(JNIEnv *env, jclass obj, jint flag)
{
    return g_emv_kernel_instance->emv_set_bypass_pin(flag);
}

// 22
jint native_emv_set_online_result(JNIEnv *env, jclass obj, jint result, jbyteArray respCode, jbyteArray issuerRespData, jint issuerRespDataLength)
{
	jint iResult = -1;
	jbyte* bRespCode = env->GetByteArrayElements(respCode, NULL);
	if(issuerRespData == NULL || issuerRespDataLength == 0)
	{
		iResult = g_emv_kernel_instance->emv_set_online_result(result, (uchar *)bRespCode, NULL, 0);
	}
	else{
		jbyte* bIssuerRespData = env->GetByteArrayElements(issuerRespData, NULL);
		iResult = g_emv_kernel_instance->emv_set_online_result(result, (uchar *)bRespCode, (uchar *)bIssuerRespData, issuerRespDataLength);
		env->ReleaseByteArrayElements(issuerRespData, bIssuerRespData, 0);
	}

	env->ReleaseByteArrayElements(respCode, bRespCode, 0);
	return iResult;
}

// 23
jint native_emv_aidparam_clear(JNIEnv *env, jclass obj)
{
	return g_emv_kernel_instance->emv_aidparam_clear();
}

// 24
jint native_emv_aidparam_add(JNIEnv *env, jclass obj, jbyteArray data, jint dataLength)
{
	jbyte* bData = env->GetByteArrayElements(data, NULL);
	jint iResult = g_emv_kernel_instance->emv_aidparam_add((uchar *)bData, dataLength);
	env->ReleaseByteArrayElements(data, bData, 0);
	return iResult;
}

jint native_emv_contactless_aidparam_clear(JNIEnv *env, jclass obj)
{
    return g_emv_kernel_instance->emv_contactless_aidparam_clear();
}

jint native_emv_contactless_aidparam_add(JNIEnv *env, jclass obj, jbyteArray data, jint dataLength)
{
	jbyte* bData = env->GetByteArrayElements(data, NULL);
	jint iResult = g_emv_kernel_instance->emv_contactless_aidparam_add((uchar *)bData, dataLength);
	env->ReleaseByteArrayElements(data, bData, 0);
	return iResult;
}

// 25
jint native_emv_capkparam_clear(JNIEnv *env, jclass obj)
{
	return g_emv_kernel_instance->emv_capkparam_clear();
}

// 26
jint native_emv_capkparam_add(JNIEnv *env, jclass obj, jbyteArray data, jint dataLength)
{
	jbyte* bData = env->GetByteArrayElements(data, NULL);
	jint iResult = g_emv_kernel_instance->emv_capkparam_add((uchar *)bData, dataLength);
	env->ReleaseByteArrayElements(data, bData, 0);
	return iResult;
}

jint native_emv_terminal_param_set_tlv(JNIEnv *env, jclass obj, jbyteArray data, jint dataLength)
{
	jbyte* bData = env->GetByteArrayElements(data, NULL);
	jint iResult = g_emv_kernel_instance->emv_terminal_param_set_tlv((uchar *)bData, dataLength);
	env->ReleaseByteArrayElements(data, bData, 0);
	return iResult;
}

jint native_emv_terminal_param_set_drl(JNIEnv *env, jclass obj, jbyteArray data, jint dataLength)
{
	jint iResult = -1;
	if(g_emv_kernel_instance->emv_terminal_param_set_drl != NULL)
	{
		jbyte* bData = env->GetByteArrayElements(data, NULL);
		iResult = g_emv_kernel_instance->emv_terminal_param_set_drl((uchar *) bData, dataLength);
		env->ReleaseByteArrayElements(data, bData, 0);
	}
	return iResult;
}

jint native_set_display_language(JNIEnv *env, jclass obj, jint flag)
{
	jint iResult = -1;
	if(g_emv_kernel_instance->set_display_language != NULL)
	{
		iResult = g_emv_kernel_instance->set_display_language(flag);
	}
	return iResult;
}

// 28
jint native_emv_exception_file_clear(JNIEnv *env, jclass obj)
{
	return g_emv_kernel_instance->emv_exception_file_clear();
}

// 29
jint native_emv_exception_file_add(JNIEnv *env, jclass obj, jbyteArray data)
{
	jbyte* bData = env->GetByteArrayElements(data, NULL);
	jint iResult = g_emv_kernel_instance->emv_exception_file_add((uchar *)bData);
	env->ReleaseByteArrayElements(data, bData, 0);
	return iResult;
}

// 30
jbyte native_emv_revoked_cert_clear(JNIEnv *env, jclass obj)
{
	return g_emv_kernel_instance->emv_revoked_cert_clear();
}

// 31
jbyte native_emv_revoked_cert_add(JNIEnv *env, jclass obj, jbyteArray data)
{
	jbyte* bData = env->GetByteArrayElements(data, NULL);
	jint iResult = g_emv_kernel_instance->emv_revoked_cert_add((uchar *)bData);
	env->ReleaseByteArrayElements(data, bData, 0);
	return iResult;
}

// 32
jint native_emv_log_file_clear(JNIEnv *env, jclass obj)
{
	return g_emv_kernel_instance->emv_log_file_clear();
}

jint native_emv_set_kernel_attr(JNIEnv *env, jclass obj, jbyteArray data, jint dataLen)
{
	jbyte *bData = env->GetByteArrayElements(data, NULL);
	jint iResult = g_emv_kernel_instance->emv_set_kernel_attr((uchar *)bData, dataLen);
	env->ReleaseByteArrayElements(data, bData, 0);
	return iResult;
}

jint native_emv_set_currency_symbol(JNIEnv *env, jclass obj, jbyteArray data, jint dataLen)
{
	jbyte *bData = env->GetByteArrayElements(data, NULL);
	jint iResult = g_emv_kernel_instance->emv_set_currency_symble(reinterpret_cast<unsigned char *>(bData), dataLen);
	env->ReleaseByteArrayElements(data, bData, 0);
	return iResult;
}


void native_emv_set_anti_shake(JNIEnv *env, jclass obj, jint flag)
{
	if( g_emv_kernel_instance == NULL || g_emv_kernel_instance->emv_set_anti_shake == NULL)
	{
		return;
	}
	return g_emv_kernel_instance->emv_set_anti_shake(flag);
}

void native_emv_anti_shake_finish(JNIEnv *env, jclass obj, jint flag)
{
	if(g_emv_kernel_instance == NULL || g_emv_kernel_instance->emv_anti_shake_finish == NULL)
	{
		return;
	}
	return g_emv_kernel_instance->emv_anti_shake_finish(flag);
}

jint native_emv_get_kernel_checksum(JNIEnv *env, jclass obj, jbyteArray data, jint dataLen)
{
	if(g_emv_kernel_instance->emv_get_kernel_checksum == NULL)
	{
		return -1;
	}
	jbyte *bData = env->GetByteArrayElements(data, NULL);
	jint iResult = g_emv_kernel_instance->emv_get_kernel_checksum(reinterpret_cast<unsigned char *>(bData), dataLen);
	env->ReleaseByteArrayElements(data, bData, 0);
	return iResult;
}

jint native_emv_get_config_checksum(JNIEnv *env, jclass obj, jbyteArray data, jint dataLen)
{
	if(g_emv_kernel_instance->emv_get_config_checksum == NULL)
	{
		return -1;
	}
	jbyte *bData = env->GetByteArrayElements(data, NULL);
	jint iResult = g_emv_kernel_instance->emv_get_config_checksum(reinterpret_cast<unsigned char *>(bData), dataLen);
	env->ReleaseByteArrayElements(data, bData, 0);
	return iResult;
}

jint native_emv_set_force_aac(JNIEnv *env, jclass obj, jint flag)
{
	return g_emv_kernel_instance->emv_set_force_aac(flag);
}

jint native_emv_generate_pseudo_track1(JNIEnv *env, jclass obj, jbyteArray data, jint dataLength)
{
	if(   g_emv_kernel_instance == NULL
		  || g_emv_kernel_instance->emv_generate_pseudo_track1 == NULL
			)
	{
		return -1;
	}
	jbyte* bData = env->GetByteArrayElements(data, NULL);
	jint iResult = g_emv_kernel_instance->emv_generate_pseudo_track1((uchar*)bData, dataLength);
	env->ReleaseByteArrayElements(data, bData, 0);
	return iResult;
}

jint native_emv_generate_pseudo_track2(JNIEnv *env, jclass obj, jbyteArray data, jint dataLength)
{
	if(   g_emv_kernel_instance == NULL
		  || g_emv_kernel_instance->emv_generate_pseudo_track2 == NULL
			)
	{
		return -1;
	}
	jbyte* bData = env->GetByteArrayElements(data, NULL);
	jint iResult = g_emv_kernel_instance->emv_generate_pseudo_track2((uchar*)bData, dataLength);
	env->ReleaseByteArrayElements(data, bData, 0);
	return iResult;
}

jint native_emv_get_kernel_id(JNIEnv *env, jclass obj)
{
    if(   g_emv_kernel_instance == NULL
          || g_emv_kernel_instance->emv_get_kernel_id == NULL
            )
    {
        return -1;
    }

    return g_emv_kernel_instance->emv_get_kernel_id();
}

jint native_emv_get_process_type(JNIEnv *env, jclass obj)
{
    if(   g_emv_kernel_instance == NULL
          || g_emv_kernel_instance->emv_get_process_type == NULL
            )
    {
        return -1;
    }

    return g_emv_kernel_instance->emv_get_process_type();
}

jint native_emv_offlinepin_verified(JNIEnv *env, jclass obj)
{
	if(   g_emv_kernel_instance == NULL
	   || g_emv_kernel_instance->emv_offlinepin_verified == NULL
	  )
	{
		hal_sys_error("g_emv_kernel_instance->emv_offlinepin_verified = NULL");
		return 0;
	}
	return g_emv_kernel_instance->emv_offlinepin_verified();
}

jint native_emv_get_offlinepin_times(JNIEnv *env, jclass obj)
{
	if(   g_emv_kernel_instance == NULL
	   || g_emv_kernel_instance->emv_get_offlinepin_times == NULL
	  )
	{
		hal_sys_error("g_emv_kernel_instance->emv_get_offlinepin_times = NULL");
		return 0;
	}
	return g_emv_kernel_instance->emv_get_offlinepin_times();
}

static JNINativeMethod g_Methods[] =
{
	{"loadEMVKernel",					"([BI)B",		    (void*)native_load},
	{"exitEMVKernel",					"()B",				(void*)native_close},
	// card functions
	{"open_reader",   					"(I)I",			    (void*)native_open_reader},
	{"close_reader",					"(I)V",				(void*)native_close_reader},
	{"poweron_card",					"()I",				(void*)native_poweron_card},
    {"open_reader_ex",   				"(II)I",			(void*)native_open_reader_ex},
    {"get_card_type",					"()I",				(void*)native_get_card_type},
	{"get_card_atr",					"([B)I",	        (void*)native_get_card_atr},
	{"transmit_card",					"([BI[BI)I",	    (void*)native_transmit_card},
	{"query_contact_card_presence",		"()I",				(void*)native_query_contact_card_presence},
	{"set_contactless_detach_enable",   "(I)V",			    (void*)native_set_contactless_detach_enable},
    // pinpad functions
    {"setPinpadKeyEventCallback",		"()I",				(void*)native_pinpad_set_keyevent_callback},
	// emv Functions
	{"emv_kernel_initialize",		   	"()V",	       	    (void*)native_emv_kernel_initialize},   // 0
	{"emv_set_kernel_attr",             "([BI)I",           (void*)native_emv_set_kernel_attr},
	{"emv_is_tag_present",				"(I)I",				(void*)native_emv_is_tag_present},		// 1
	{"emv_get_tag_data",				"(I[BI)I",			(void*)native_emv_get_tag_data},		// 2
	{"emv_get_tag_list_data",			"([II[BI)I",		(void*)native_emv_get_tag_list_data},	// 3
	{"emv_set_tag_data",				"(I[BI)I",			(void*)native_emv_set_tag_data},		// 4
	{"emv_preprocess_qpboc",			"()I",	        	(void*)native_emv_preprocess_qpboc},    // 5

	{"emv_trans_initialize",		   	"()V",	       	    (void*)native_emv_trans_initialize},    // 6
	{"emv_get_version_string",			"([BI)I",			(void*)native_emv_get_version_string},  // 7
	{"emv_set_trans_amount",			"([B)I",			(void*)native_emv_set_trans_amount},    // 8
	{"emv_set_other_amount",			"([B)I",			(void*)native_emv_set_other_amount},    // 9
	{"emv_set_trans_type",				"(B)I",				(void*)native_emv_set_trans_type},      // 10
	{"emv_set_kernel_type",				"(B)I",				(void*)native_emv_set_kernel_type},     // 11
	{"emv_get_kernel_type",		   	"()I",	       	        (void*)native_emv_get_kernel_type},

	{"emv_process_next",				"()I",	            (void*)native_emv_process_next},        // 12
	{"emv_get_process_type",            "()I",              (void*)native_emv_get_process_type},

	{"emv_is_need_advice",		    	"()I",				(void*)native_emv_is_need_advice},      // 13
	{"emv_is_need_signature",			"()I",				(void*)native_emv_is_need_signature},   // 14

	{"emv_set_force_online",			"(I)I",				(void*)native_emv_set_force_online},    // 15
	{"emv_get_card_record",		    	"([BI)I",			(void*)native_emv_get_card_record},     // 16
	{"emv_get_candidate_list",	    	"([BI)I",	        (void*)native_emv_get_candidate_list},  // 17
    {"emv_get_candidate_list_tlv",   	"([BI)I",	        (void*)native_emv_get_candidate_list_tlv},
	{"emv_set_candidate_list_result",	"(I)I",	            (void*)native_emv_set_candidate_list_result},  // 18
	{"emv_set_id_check_result",		    "(I)I",				(void*)native_emv_set_id_check_result},        // 19
	{"emv_set_online_pin_entered",	    "(I)I",	            (void*)native_emv_set_online_pin_entered},
    {"emv_set_bypass_pin",	            "(I)I",	            (void*)native_emv_set_bypass_pin},   // 21
    {"emv_set_online_result",	        "(I[B[BI)I",	    (void*)native_emv_set_online_result},          // 22
	{"emv_aidparam_clear",				"()I",				(void*)native_emv_aidparam_clear},             // 23
	{"emv_aidparam_add",				"([BI)I",			(void*)native_emv_aidparam_add},               // 24
    {"emv_contactless_aidparam_clear",	"()I",				(void*)native_emv_contactless_aidparam_clear},             // 23
    {"emv_contactless_aidparam_add",	"([BI)I",			(void*)native_emv_contactless_aidparam_add},
	{"emv_capkparam_clear",				"()I",				(void*)native_emv_capkparam_clear},            // 25
	{"emv_capkparam_add",				"([BI)I",			(void*)native_emv_capkparam_add},              // 26
	{"emv_terminal_param_set_tlv",		"([BI)I",	        (void*)native_emv_terminal_param_set_tlv},
	{"emv_terminal_param_set_drl",		"([BI)I",	        (void*)native_emv_terminal_param_set_drl},
	{"emv_exception_file_clear",		"()I",				(void*)native_emv_exception_file_clear},       // 28
	{"emv_exception_file_add",			"([B)I",		    (void*)native_emv_exception_file_add},         // 29
	{"emv_revoked_cert_clear",			"()I",				(void*)native_emv_revoked_cert_clear},         // 30
	{"emv_revoked_cert_add",			"([B)I",		    (void*)native_emv_revoked_cert_add},           // 31
	{"emv_log_file_clear",		        "()I",				(void*)native_emv_log_file_clear},             // 32

	{"emv_set_currency_symbol",			"([BI)I",			(void*)native_emv_set_currency_symbol},
	{"emv_set_anti_shake",              "(I)V",   			(void*)native_emv_set_anti_shake},
	{"emv_anti_shake_finish",           "(I)V",   			(void*)native_emv_anti_shake_finish},
	{"emv_get_kernel_checksum",			"([BI)I",			(void*)native_emv_get_kernel_checksum},
	{"emv_get_config_checksum",			"([BI)I",			(void*)native_emv_get_config_checksum},
	{"emv_set_force_aac",			    "(I)I",				(void*)native_emv_set_force_aac},
	{"emv_generate_pseudo_track1",      "([BI)I",	        (void*)native_emv_generate_pseudo_track1},
	{"emv_generate_pseudo_track2",      "([BI)I",	        (void*)native_emv_generate_pseudo_track2},
    {"emv_get_kernel_id",               "()I",              (void*)native_emv_get_kernel_id},
    {"emv_offlinepin_verified",			"()I",				(void*)native_emv_offlinepin_verified},
	{"emv_get_offlinepin_times",		"()I",				(void*)native_emv_get_offlinepin_times},
    {"set_display_language",	            "(I)I",	            (void*)native_set_display_language}
};

const char* emv_kernal_get_class_name()
{
	return (char*)g_pJNIREG_CLASS;
}

JNINativeMethod* emv_kernal_get_methods(int* pCount)
{
	*pCount = sizeof(g_Methods) /sizeof(g_Methods[0]);
	return g_Methods;
}
