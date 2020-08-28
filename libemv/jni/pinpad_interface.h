/*
 * pinpad_interface.h
 *
 *  Created on: 2012-8-2
 *      Author: yaomaobiao
 */

#ifndef PINPAD_INTERFACE_H_
#define PINPAD_INTERFACE_H_

#define KEY_TYPE_DUKPT		0
#define KEY_TYPE_TDUKPT		1
#define KEY_TYPE_MASTER		2
#define KEY_TYPE_PUBLIC		3
#define KEY_TYPE_FIX		5

#ifdef __cplusplus
extern "C"
{
#endif

typedef struct _tagMasterKeyInfo
{
    unsigned char nKeyType;
    unsigned char nMasterKeyID;
    unsigned char nKeyLen;
    unsigned char strReserved[1];
    unsigned char strMasterKey[24];
}MASTER_KEY_INFO;

typedef struct _tagDukptKeyInfo
{
    unsigned char nKeyType;
    unsigned char nDukptKeyID;
    unsigned char strReserved[2];
    unsigned char strInitialSN[8];
    unsigned char strCounter[4];
    unsigned char strInitKey[16];
}DUKPT_KEY_INFO;

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

/*
 * open the device
 * return value : 0 : success
 * 				  < 0 : error code
 */
typedef int (*pinpad_open)();
/*
 * close the device
 * return value : 0 : success
 * 				  < 0 : error code
 */
typedef int (*pinpad_close)();
/*
 * show text in the first line
 * param[in] : int nLineIndex : line index, from top to down
 * param[in] : char* strText : encoded string
 * param[in] : int nLength : length of string
 * param[in] : int nFlagSound : 0 : no voice prompt, 1 : voice prompt
 * return value : < 0 : error code, maybe, your display string is too long!
 * 				  >= 0 : success
 *
 */
typedef int (*pinpad_show_text)(int nLineIndex, char* strText, int nLength, int nFlagSound);
/*
 * select master key and user key
 * @param[in] : int nKeyType : 0 : dukpt, 1: Tdukpt, 2 : master key, 3 : public key, 4 : fix key
 * @param[in] : int nMasterKeyID : master key ID , [0x00, ..., 0x09], make sense only when nKeyType is master-session pair,
 * @param[in] : int nUserkeyID : user key ID, [0x00, 0x01], 		  make sense only when nKeyType is master-session pair,
 * @param[in] : int nAlgorith : 1 : 3DES
 * 							   0 : DES
 * return value : < 0 : error code
 * 				  >= 0 : success
 */
typedef int (*pinpad_select_key)(int nKeyType, int nMasterKeyID, int nUserKeyID, int nAlgorith);

/*
 * encrypt string using user key
 * @param[in] : unsigned char* pPlainText : plain text
 * @param[in] : int nTextLength : length of plain text
 * @param[out] : unsigned char* pCipherTextBuffer : buffer for saving cipher text
 * @param[in] : int CipherTextBufferLength : length of cipher text buffer
 * return value : < 0 : error code
 * 				  >= 0 : success, length of cipher text length
 */
typedef int (*pinpad_encrypt_string)(unsigned char* pPlainText, int nTextLength, unsigned char* pCipherTextBuffer, int nCipherTextBufferLength);


/*
 * encrypt string using user key
 * @param[in] : unsigned char* pPlainText : plain text
 * @param[in] : int nTextLength : length of plain text
 * @param[out] : unsigned char* pCipherTextBuffer : buffer for saving cipher text
 * @param[in] : int CipherTextBufferLength : length of cipher text buffer
 * @param[in] : unsigned int nMode :
 *					PINPAD_ENCRYPT_STRING_MODE_EBC  0
 *					PINPAD_ENCRYPT_STRING_MODE_CBC	1
 *					PINPAD_ENCRYPT_STRING_MODE_CFB	2
 *					PINPAD_ENCRYPT_STRING_MODE_OFB	3
 * @param[in] : unsigned char* PIV : initial vector, only for CBC, CFB, OFB mode
 * @param[in] : unsigned int nIVLen : length of IV, must be equal to block length according to the algorithm
 * return value : < 0 : error code
 * 				  >= 0 : success, length of cipher text length
 */
typedef int (*pinpad_encrypt_string_with_mode)(unsigned char* pPlainText, int nTextLength, unsigned char* pCipherTextBuffer, int nCipherTextBufferLength, unsigned int nMode, unsigned char* pIV, unsigned int nIVLen);


/*
 * calculate pin block
 * @param[in] : unsigned char* pASCIICardNumber : card number in ASCII format
 * @param[in] : int nCardNumberLength : length of card number
 * @param[out] : unsigned char* pPinBlockBuffer : buffer for saving pin block
 * @param[in] : int nPinBlockBufferLength : buffer length of pin block
 * @param[in] : int nTimeout_MS : timeout waiting for user input in milliseconds, if it is less than zero, then wait forever
 * param[in]   : int nFlagSound : 0 : no voice prompt, 1 : voice prompt
 * return value : < 0 : error code
 * 			      >= 0 : success, length of pin block
 */
typedef int (*pinpad_calculate_pin_block)(unsigned char* pASCIICardNumber, int nCardNumberLength, unsigned char* pPinBlockBuffer, int nPinBlockBufferLength, int nTimeout_MS, int nFlagSound);


/*
 * update the user key
 * @param[in] : int nMasterKeyID : master key id
 * @param[in] : int nUserKeyID : user key id
 * @param[in] : unsigned char* pCipherNewUserkey : new user key in cipher text
 * @param[in] : int nCipherNewUserKeyLength : length of new user key in cipher text
 * return value : < 0 : error code
 * 				  >= 0 : success
 */
typedef int (*pinpad_update_user_key)(int nMasterKeyID, int nUserKeyID, unsigned char* pCipherNewUserKey, int nCipherNewUserKeyLength);

/*
 * update the master key
 * @param[in] : int nMasterKeyID : master key ID
 * @param[in] : unsigned char* pOldKey, old key
 * @param[in] : int nOldKeyLength : length of old key, 8 or 16
 * @param[in] : unsigned char* pNewLey : new key
 * @param[in] : int nNewLeyLength : length of new key, 8 or 16
 * return value : < 0 : error code
 * 				  >= 0 : success
 */
typedef int (*pinpad_update_master_key)(int nMasterKeyID, unsigned char* pOldKey, int nOldKeyLength, unsigned char* pNewKey, int nNewKeyLength);

/*
 * set the max length of pin
 * @param[in] : int nLength : length >= 0 && length <= 0x0D
 * @param[in] : int nFlag : 1, max length
 * 							0, min length
 * return value : < 0 : error code
 * 				  >= 0 : success
 */
typedef int (*pinpad_set_pin_length)(int nLength, int nFlag);

// 输入字符数回调接口
typedef void(*KEYEVENT_NOTIFIER) (int nCount, int nExtra);
typedef int (*pinpad_set_pinblock_callback)(KEYEVENT_NOTIFIER);


//21号文取序列号
typedef int (*pinpad_get_hwserialno)(unsigned char* pData, unsigned int nBufferLength);
//21号文计算密文
typedef int (*pinpad_get_mac_for_snk)(unsigned char* pData, unsigned int nBufferLength, unsigned char* pRandomData, unsigned int nRandomDataLength, unsigned char* pMAB, unsigned int nMABLength);

typedef int (*pinpad_import_key)(IMPORT_KEY_DOWNLOAD_BLOCK *pBlock);

typedef int (*pinpad_get_auth_info)(IMPORT_KEY_UP_AUTH_INFO *pAuthInfo);

/*
 * New function, Added by SUN, 2016/08/02
 * for customer from Israel
 * @param[in] : unsigned int nFlag : 1 : allow bypass pin, No pin input is allowed, user can input Enter directly.
 * 									 0 : forbid bypass pin, No pin input is not allowed.
 * return value : < 0 : error code
 * 				  >= 0 : success
 */
typedef int (*pinpad_set_flag_allow_bypass_pin)(unsigned int nFlag);

#ifdef __cplusplus
}
#endif

#endif /* PINPAD_INTERFACE_H_ */
