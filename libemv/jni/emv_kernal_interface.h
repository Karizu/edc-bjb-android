#include <jni.h>
#ifndef uchar
#define uchar unsigned char
#endif

#ifndef ushort
#define ushort unsigned short
#endif

#ifndef uint
#define uint unsigned int
#endif

#define STATUS_ERROR 0
#define STATUS_CONTINUE 1
#define STATUS_COMPLETION 2

#define APPROVE_OFFLINE 1
#define APPROVE_ONLINE  2
#define DECLINE_OFFLINE 3
#define DECLINE_ONLINE  4

typedef struct
{
	uchar  aidLength;
	uchar aid[16];			// Application Identifier
	uchar appLable[16+1];		// Application Label
	/** Application Preferred Name */
	uchar appPreferredName[16+1];
	uchar appPriority;		// Application Priority
	/** Terminal Floor Limit */
	uchar termFloorLimit[4];   // Hex
	/** Terminal Action Code - Default */
	uchar termActionCodeDefault[5];
	/** Terminal Action Code - Denial */
	uchar termActionCodeDenial[5];
	/** Terminal Action Code - Online */
	uchar termActionCodeOnline[5];
	/** Target Percentage */
	uchar targetPercentage;
	/** threshold Value */
	uchar thresholdValue[4];	 // Hex
	/** Maximum Target Percentage */
	uchar maxTargetPercentage;
	/** Acquirer Identifier */
	uchar acquirerId[6];
	/** Merchant Category Code */
	uchar mechantCategoryCode[2];
	/** Merchant Identifier */
	uchar merchantId[15];
	/** Application Version Number */
	uchar appVersionNumber[2];
	/** Point-of-Service(POS) Entry Mode */
	uchar posEntryMode;	
	/** Transaction Reference Currency Code */
	uchar transReferCurrencyCode[2];
	/** Transaction Reference Currency Exponent */
	uchar transReferCurrencyExponent;
	/** Default Dynamic Data Authentication Data Object List(DDOL) */
	uchar defaultDDOLLength;
	uchar defaultDDOL[128];	
	uchar defaultTDOLLength;
	/** Default Transaction Certificate Data Object List(TDOL) */
	uchar defaultTDOL[128];
	// supportOnlinePin[0] = 0 means the Application unsupported 
	// online PIN, any other value means the Application supported 
	// online PIN
	uchar supportOnlinePin;
	// supportAIDPartial[0] = 0 means the Application unsupported AID // Partial, any other value means the Application supported AID // Partial
	uchar supportAIDPartial;
	/** Option effective Flag */
	uchar optionEffectiveFlag;
}AIDParam;

typedef struct
{
	/** Registered Application Provider Identifier */
	uchar rid[5];
	/** Certificate Authority Public Key Index */
	uchar capki;
	/** Hash Algorithm Indicator */
	uchar hashInd;
	/** Certificate Authority Public Key Algorithm Indicator */
	uchar arithInd;
	/** The Length of Certificate Authority Public Key Modulus */
	u_int32_t modulLen;
	/** Certificate Authority Public Key Modulus */
	uchar modul[248];
	/** The Length of Certificate Authority Public Key Exponent */
	uchar exponentLen;
	/** Certificate Authority Public Key Exponent */
	uchar exponent[3];
	/** Certificate Authority Public Key Check Sum */
	uchar checkSum[20];
	/** Certificate Expiration Date */
	uchar expiry[8];
}CAPKParam;

typedef struct
{
	uchar terminal_country_code[2];             // 9F1A: Terminal Country Code
	uchar TID[8];								// 9F1C
	uchar IFD[8];                               // 9F1E: IFD Serial Number
	uchar transaction_currency_code[2];			// 5F2A
	uchar terminal_capabilities[3];             // 9F33
	uchar terminal_type[1];						// 9F35
	uchar transaction_currency_exponent[1];		// 5F36
	uchar additional_terminal_capabilities[5];  // 9F40
	uchar merchantNameLength;
	uchar merchantName[20]; // 9F4E 
	uchar ECTermTransLimit[6]; // 9F7B
}TERMINAL_INFO;

typedef struct
{
	/** PAN */
	uchar cardNo[19];
	/** PAN Sequence Number */
	uchar panSequence;
}ExceptionFile;

typedef struct
{
	/** Registered Application Provider Identifier */
	uchar rid[5];
	/** Certificate Authority Public Key Index */
	uchar capki;
}RevokedCert;

// 回调函数
typedef void (*EMV_PROCESS_CALLBACK)(uchar *data);

/*
 * 侦听卡片事件，当有卡片靠近或插入的时候，触发该事件
 * 即 当调用OPEN_READER后，如果有卡片靠近或插入时，回调该函数；
 * @param[in] eventType	 : SMART_CARD_EVENT_INSERT_CARD = 0;
 *												 : SMART_CARD_EVENT_REMOVE_CARD = 1;
 */
typedef void (*CARD_EVENT_OCCURED)(int eventType);

typedef void (*KEYEVENT_NOTIFIER)(int nCount, int nExtra);
/*
 * 打开读卡器，并等待读卡（开始轮询卡片）
 * @param[in] reader : 阅读器类型
 : 0 打开所有读卡器
 : 1 只打开接触式读卡器
 : 2 只打开非接触式读卡器
 * return value						 : = -1 打开读卡器失败 （如果需要打开两个读卡器，只要有一个成功，就报成功）
 */
typedef int (*OPEN_READER)(int reader);
typedef int (*OPEN_READER_EX)(int reader, int extraParam);
typedef void (*CLOSE_READER)(int reader);
typedef int (*POWERON_CARD)();
typedef int (*GET_CARD_TYPE)();
typedef int (*GET_CARD_ATR)(uchar* pATR);
typedef int (*TRANSMIT_CARD)(uchar* cmd, int cmdLength, uchar* respData, int respDataLength);
typedef int (*QUERY_CONTACT_CARD_PRESENCE)();
typedef void (*SET_CONTACTLESS_DETACH_ENABLE)(int enable);

// pinpad functions
typedef int (*PINPAD_SET_KEYEVENT_CALLBACK)(KEYEVENT_NOTIFIER pRevokeFuncPointer);
// EMV Functions
typedef struct
{
	CARD_EVENT_OCCURED pCardEventOccured; // 卡事件回调函数
	EMV_PROCESS_CALLBACK pEMVProcessCallback;
}EMV_INIT_PARAM;
typedef void (*EMV_KERNEL_INITIALIZE)(EMV_INIT_PARAM *pInitData);
typedef int (*EMV_IS_TAG_PRESENT)(int tag);                                     // 1
typedef int (*EMV_GET_TAG_DATA)(int tag, uchar *data, int dataLength);          // 2
typedef int (*EMV_GET_TAG_LIST_DATA)(int *tagList, int tagCount, uchar *data, int dataLength);// 3
typedef int (*EMV_SET_TAG_DATA)(int tag, uchar *data, int dataLength);          // 4
typedef int (*EMV_PREPROCESS_QPBOC)();                                          // 5
typedef void (*EMV_TRANS_INITIALIZE)();                                          // 6
typedef int (*EMV_GET_VERSION_STRING)(uchar *data, int dataLength);             // 7
typedef int (*EMV_SET_TRANS_AMOUNT)(uchar *amount);                             // 8
typedef int (*EMV_SET_OTHER_AMOUNT)(uchar *amount);                             // 9
typedef int (*EMV_SET_TRANS_TYPE)(uchar transType);                             //10
typedef int (*EMV_SET_KERNEL_TYPE)(uchar kernelType);                           //11
typedef int (*EMV_GET_KERNEL_TYPE)();
typedef int (*EMV_GET_PROCESS_TYPE)();
typedef int (*EMV_GET_KERNEL_ID)();
typedef int (*EMV_PROCESS_NEXT)();                                              //12
typedef int (*EMV_IS_NEED_ADVICE)();                                            //13
typedef int (*EMV_IS_NEED_SIGNATURE)();                                         //14
typedef int (*EMV_SET_FORCE_ONLINE)(int flag);                                  //15
typedef int (*EMV_GET_CARD_RECORD)(uchar *data, int dataLength);                //16
typedef int (*EMV_GET_CANDIDATE_LIST)(uchar *data, int dataLength);             //17
typedef int (*EMV_GET_CANDIDATE_LIST_TLV)(uchar *data, int dataLength);
typedef int (*EMV_SET_CANDIDATE_LIST_RESULT)(int index);                        //18
typedef int (*EMV_SET_ID_CHECK_RESULT)(int result);                             //19
typedef int (*EMV_SET_ONLINE_PIN_ENTERED)(int result);                          //20
typedef int (*EMV_SET_BYPASS_PIN)(int flag);
typedef int (*EMV_SET_ONLINE_RESULT)(int result, uchar *respCode, uchar *issuerRespData, int issuerRespDataLength); //22

typedef int (*EMV_AIDPARAM_CLEAR)();                                            //23
typedef int (*EMV_AIDPARAM_ADD)(uchar *data, int dataLength);
typedef int (*EMV_CONTACTLESS_AIDPARAM_CLEAR)();                                            //23
typedef int (*EMV_CONTACTLESS_AIDPARAM_ADD)(uchar *data, int dataLength);
typedef int (*EMV_CAPKPARAM_CLEAR)();                                           //25
typedef int (*EMV_CAPKPARAM_ADD)(uchar *data, int dataLength);                  //26
typedef int (*EMV_TERMINAL_PARAM_SET_TLV)(uchar *TerminalParam, int paramLength);
typedef int (*EMV_TERMINAL_PARAM_SET_DRL)(uchar *drlParam, int paramLength);

typedef int (*EMV_EXCEPTION_FILE_CLEAR)();                                      //28
typedef int (*EMV_EXCEPTION_FILE_ADD)(uchar *exceptFile);                       //29
typedef int (*EMV_REVOKED_CERT_CLEAR)();                                        //30
typedef int (*EMV_REVOKED_CERT_ADD)( uchar* BlackCert);                         //31

typedef int (*EMV_LOG_FILE_CLEAR)();                                            //32
typedef int (*EMV_SET_KERNEL_ATTR)(uchar* data, int dataLen);
typedef int (*EMV_SET_CURRENCY_SYMBLE)(uchar *data, int dataLen);
typedef void (*EMV_SET_ANTI_SHAKE)(int flag);
typedef void (*EMV_ANTI_SHAKE_FINISH)(int flag);

typedef int (*EMV_GET_KERNEL_CHECKSUM)(uchar* data, int dataLen);
typedef int (*EMV_GET_CONFIG_CHECKSUM)(uchar* data, int dataLen);
typedef int (*EMV_SET_FORCE_AAC)(int flag);

typedef int (*EMV_GENERATE_PSEUDO_TRACK1)(uchar *data, int dataLength);
typedef int (*EMV_GENERATE_PSEUDO_TRACK2)(uchar *data, int dataLength);


typedef int (*EMV_OFFLINEPIN_VERIFIED)();
typedef int (*EMV_GET_OFFLINEPIN_TIMES)();
typedef int (*EMV_GET_LAST_CMD)(uchar type, uchar *data, int dataLength);

typedef int (*SET_DISPLAY_LANGUAGE)(int flag);

typedef struct emv_kernel_interface
{
	// card functions
	OPEN_READER                     open_reader;
	OPEN_READER_EX                  open_reader_ex;
	CLOSE_READER                    close_reader;
	POWERON_CARD                    poweron_card;
	GET_CARD_TYPE                   get_card_type;
	GET_CARD_ATR                    get_card_atr;
	TRANSMIT_CARD                   transmit_card;
	QUERY_CONTACT_CARD_PRESENCE     query_contact_card_presence;
    SET_CONTACTLESS_DETACH_ENABLE   set_contactless_detach_enable;
	// pinpad functions
	PINPAD_SET_KEYEVENT_CALLBACK    pinpad_set_keyevent_callback;
	// emv functions
	EMV_KERNEL_INITIALIZE			emv_kernel_initialize;
    EMV_SET_KERNEL_ATTR             emv_set_kernel_attr;
	EMV_IS_TAG_PRESENT				emv_is_tag_present;    		// 1
	EMV_GET_TAG_DATA				emv_get_tag_data;      		// 2
	EMV_GET_TAG_LIST_DATA			emv_get_tag_list_data; 		// 3
	EMV_SET_TAG_DATA				emv_set_tag_data;      		// 4
	EMV_PREPROCESS_QPBOC            emv_preprocess_qpboc;       // 5

	EMV_TRANS_INITIALIZE			emv_trans_initialize;      	// 6
	EMV_GET_VERSION_STRING			emv_get_version_string;		// 7
	EMV_SET_TRANS_AMOUNT			emv_set_trans_amount;  		// 8
	EMV_SET_OTHER_AMOUNT			emv_set_other_amount;  		// 9
	EMV_SET_TRANS_TYPE				emv_set_trans_type;    		//10
	EMV_SET_KERNEL_TYPE				emv_set_kernel_type;    	//11
    EMV_GET_KERNEL_TYPE				emv_get_kernel_type;
	EMV_PROCESS_NEXT				emv_process_next;     		//12
	EMV_IS_NEED_ADVICE				emv_is_need_advice;		    //13
	EMV_IS_NEED_SIGNATURE			emv_is_need_signature;	    //14
	EMV_SET_FORCE_ONLINE			emv_set_force_online;       //15
	EMV_GET_CARD_RECORD				emv_get_card_record;		//16
	EMV_GET_CANDIDATE_LIST          emv_get_candidate_list;		//17
    EMV_GET_CANDIDATE_LIST_TLV      emv_get_candidate_list_tlv;
	EMV_SET_CANDIDATE_LIST_RESULT   emv_set_candidate_list_result;	//18
	EMV_SET_ID_CHECK_RESULT         emv_set_id_check_result;    //19
    EMV_SET_ONLINE_PIN_ENTERED      emv_set_online_pin_entered; //20
    EMV_SET_BYPASS_PIN              emv_set_bypass_pin;
	EMV_SET_ONLINE_RESULT           emv_set_online_result;      //22

	EMV_AIDPARAM_CLEAR				emv_aidparam_clear;  		//23
	EMV_AIDPARAM_ADD				emv_aidparam_add;    		//24
    EMV_CONTACTLESS_AIDPARAM_CLEAR	emv_contactless_aidparam_clear;
	EMV_CONTACTLESS_AIDPARAM_ADD	emv_contactless_aidparam_add;    		//24
	EMV_CAPKPARAM_CLEAR				emv_capkparam_clear; 		//25
	EMV_CAPKPARAM_ADD				emv_capkparam_add;   		//26
	EMV_TERMINAL_PARAM_SET_TLV		emv_terminal_param_set_tlv;
    EMV_TERMINAL_PARAM_SET_DRL      emv_terminal_param_set_drl;
	EMV_EXCEPTION_FILE_CLEAR		emv_exception_file_clear;	//28
	EMV_EXCEPTION_FILE_ADD			emv_exception_file_add;		//29
	EMV_REVOKED_CERT_CLEAR			emv_revoked_cert_clear;		//30
	EMV_REVOKED_CERT_ADD			emv_revoked_cert_add;		//31
	EMV_LOG_FILE_CLEAR              emv_log_file_clear;         //32

	EMV_SET_CURRENCY_SYMBLE			emv_set_currency_symble;

	EMV_SET_ANTI_SHAKE              emv_set_anti_shake;
	EMV_ANTI_SHAKE_FINISH           emv_anti_shake_finish;
	EMV_GET_KERNEL_CHECKSUM         emv_get_kernel_checksum;
	EMV_GET_CONFIG_CHECKSUM         emv_get_config_checksum;
	EMV_SET_FORCE_AAC               emv_set_force_aac;
	EMV_GENERATE_PSEUDO_TRACK1      emv_generate_pseudo_track1;
	EMV_GENERATE_PSEUDO_TRACK2      emv_generate_pseudo_track2;

    EMV_GET_KERNEL_ID               emv_get_kernel_id;
    EMV_GET_PROCESS_TYPE            emv_get_process_type;
	EMV_OFFLINEPIN_VERIFIED         emv_offlinepin_verified;
	EMV_GET_OFFLINEPIN_TIMES        emv_get_offlinepin_times;
	EMV_GET_LAST_CMD				emv_get_last_cmd;

	SET_DISPLAY_LANGUAGE            set_display_language;
	void*	pHandle;
	JNIEnv * g_jni_env;
	jclass g_jni_obj;
}EMV_KERNEL_INSTANCE;
