/*
 * hal_pinpad_interface.h
 *
 *  Created on: 2012-8-2
 *      Author: yaomaobiao
 */

#ifndef HAL_PINPAD_INTERFACE_H_
#define HAL_PINPAD_INTERFACE_H_

#define KEY_TYPE_DUKPT		0
#define KEY_TYPE_TDUKPT		1
#define KEY_TYPE_MASTER		2
#define KEY_TYPE_TRANSFER	3
#define KEY_TYPE_FIX		4
#define KEY_TYPE_TDUKPT2009	5

#define PINPAD_ENCRYPT_STRING_MODE_ECB	0
#define PINPAD_ENCRYPT_STRING_MODE_CBC	1
#define PINPAD_ENCRYPT_STRING_MODE_CFB	2
#define PINPAD_ENCRYPT_STRING_MODE_OFB	3

#define PINPAD_PINBLOCK_FORMAT_ISO0		0
#define PINPAD_PINBLOCK_FORMAT_ISO1		1
#define PINPAD_PINBLOCK_FORMAT_ISO2		2
#define PINPAD_PINBLOCK_FORMAT_ISO3		3
#define PINPAD_PINBLOCK_FORMAT_ISO4		4
#define PINPAD_PINBLOCK_FORMAT_SM4		5


#define MAX_CARD_NUMBER_LENGTH		19
#define MIN_CARD_NUMBER_LENGTH		13

#define PINPAD_MAX_PIN_LENGTH		12
#define PINPAD_MIN_PIN_LENGTH		4

#ifdef __cplusplus
extern "C"
{
#endif

#define PINPAD_TYPE_NL_PP60			0
#define PINPAD_TYPE_NL_PP70			0
#define PINPAD_TYPE_NL_SP10			0
#define PINPAD_TYPE_SY_P90			1
#define PINPAD_TYPE_NL_GP710		2
#define PINPAD_TYPE_SEUIC_K820V3	3

typedef struct _tagMasterKeyInfo
{
	unsigned char nKeyType;
	unsigned char nMasterKeyID;
	unsigned char nKeyLen;
	unsigned char strReserved[1];
	unsigned char strMasterKey[32];
}MASTER_KEY_INFO;

typedef struct _tagDukptKeyInfo
{
	unsigned char nKeyType;
	unsigned char nDukptKeyID;
	unsigned char nKeyUsage;
	unsigned char strReserved[1];
	unsigned char strInitialSN[8];
	unsigned char strCounter[4];
	unsigned char strInitKey[16];
}DUKPT_KEY_INFO;

typedef enum _tagSessionKeyUsage
{
	SESSION_KEY_USAGE_pin,
	SESSION_KEY_USAGE_mac,
	SESSION_KEY_USAGE_data,
	SESSION_KEY_USAGE_undef = 0xFF
}SESSION_KEY_USAGE;

typedef enum _tagAlgoCheckValue
{
	ALGO_CHECK_VALUE_default,
	ALGO_CHECK_VALUE_se919
}ALGO_CHECK_VALUE;

/*
 *int nCount : the count of characters that are supposed to be showed in the user own soft pinpad 
 *int nExtra : extra message.
 */
typedef void (*HAL_KEYEVENT_NOTIFIER)(void* pUserData, int nCount, int nExtra);


#define HAL_PINPAD_AUTH_RANDOM_LENGTH		32
#define HAL_PINPAD_AUTH_SIGNATURE_LENGTH	256
#define HAL_PINPAD_MAX_PEM_CERT_LENGTH		4096
#define HAL_PINPAD_MAX_SN_LENGTH			31

typedef struct _tagImportKeyDownloadBlock
{
	unsigned int nPemCertLen;
	unsigned char strPemCert[HAL_PINPAD_MAX_PEM_CERT_LENGTH];
	unsigned char strRandom[HAL_PINPAD_AUTH_RANDOM_LENGTH];
	unsigned char strCipherKeyInfo[HAL_PINPAD_AUTH_SIGNATURE_LENGTH];
	unsigned char strSignature[HAL_PINPAD_AUTH_SIGNATURE_LENGTH];
}IMPORT_KEY_DOWNLOAD_BLOCK;

typedef struct _tagImportKeyUpAuthInfo
{
	unsigned int nPemCertLen;
	unsigned char strPemCert[HAL_PINPAD_MAX_PEM_CERT_LENGTH];
	unsigned char strRandom[HAL_PINPAD_AUTH_RANDOM_LENGTH];
	unsigned char nSNCount;
	unsigned char strSN[HAL_PINPAD_MAX_SN_LENGTH];
	unsigned char strSignature[HAL_PINPAD_AUTH_SIGNATURE_LENGTH];
}IMPORT_KEY_UP_AUTH_INFO;

typedef struct _tagGetCheckValueInfo
{
	/* Master Key ID */
	unsigned int nMKID;
	/* Session Key ID */
	unsigned int nSKID;
	/* 0 : Master Key, 1 : Session Key */
	unsigned short nKeySel; 
	/* 0 : default, 1 : Se919 */
	unsigned short nCheckValueType;
	/* 0 : DEA, 1 : TDEA, 2 : SM4, 3 : AES */
	unsigned short nAlgoSel;
	unsigned char strCheckValue[4];
}GET_CHECKVALUE_INFO;


#ifdef __cplusplus
}
#endif

#endif /* HAL_PINPAD_INTERFACE_H_ */
