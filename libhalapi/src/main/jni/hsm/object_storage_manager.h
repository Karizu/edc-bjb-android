#ifndef OBJECT_STORAGE_MANAGER_H
#define OBJECT_STORAGE_MANAGER_H

#include "hsm_object_property.h"

#ifdef __cplusplus
extern "C"
{
#endif


/*
 * open the hsm device(Hardware security module device)
 * return value : 0 : success
 * 				  < 0 : error code
 */
typedef int (*HSM_OSM_OPEN)();
/*
 * close the hsm device(Hardware security module device)
 * return value : 0 : success
 *                < 0 : error code
 */
typedef int (*HSM_OSM_CLOSE)();

/**
 * add new method for bank
 * */
/*
 * query the hardware security module device status
 * return value : 0 : success
 *                  < 0 : error code, is tampered
 *                  hsm_osm_query_status
 */
typedef int (*HSM_OSM_QUERY_STATUS)();
/*
* generate the rsa key pair
* @param[in]: HSM_OBJECT_PROPERTY* pObjectProperty: object property, include label and id
* @param[in]: unsigned int nKeyBitsLength:key length by bit, usually is 2048bits
* return value : = 0: success
                      < 0: error code
*/
typedef int(*HSM_OSM_GENERATE_KEYPAIR)(char* label, unsigned int nKeyBitsLength);
/*
 * save pubkey cert
 * @param[in] : char* pCertLabel: cert label
 * @param[in] : char* pPriKeyLabel: the correspond private key label
 * @param[in] : unsigned char* pObjectData : data
 * @param[in] : unsigned int nDataLength : data length
 * @param[in] : object data format : currently, we only support PEM format
 * return value : 0 : success
 *                < 0 : error code
 */
typedef int(*HSM_OSM_STORE_PUBKEY_CERT)(char* pCertLabel, char* pPriKeyLabel, unsigned char* pObjectData, unsigned int nDataLength, HSM_OBJECT_DATA_TYPE nDataType);
/*
 * save root certs
 * return value : 0 : success
 *                < 0 : error code
 */
typedef int(*HSM_OSM_STORE_ROOT_CERT)(unsigned int nProperty, char* pCertLabel, unsigned char* pObjectData, unsigned int nDataLength, HSM_OBJECT_DATA_TYPE nDataType);


/*
* private key do encrypt or decrypt
*@param[in]:HSM_OBJECT_PROPERTY* pObjectProperty:object property
*@param[in]:nEncDecFlag: process flag  0: encrypt  1:decrypt
*@param[in][out]:unsigned char* pData: input data buffer, and save the output data
*@param[in]:unsigned int nDataLength: intput data length
*return vale: <= 0: error
                      > 0: output data length
*/
typedef int(*HSM_OSM_PRIVATE_KEY_ENCRYPT_DECRYPT)(char* privKeyLabel, unsigned int nEncDecFlag, unsigned char* pData, unsigned int nDataLength,unsigned char* pResultData, unsigned int nResultDataLength);

/*
 * query the lable of specific type certificate  in the hardware secure module, every label separated by ':'
 * @param[in] : unsigned int nProperty: cert property
 * @param[in][out]: data buffer
 * @param[in] : length of data buffer
 * return value :<= 0 : error code
 *                > 0 : data length
 */
typedef int(*HSM_OSM_QUERY_CERT_LABELS)(unsigned int nProperty, unsigned char *pBuffer, unsigned int nBufferLength);


/*
 * delete all objects in hardware  security module
 * @param[in] : char* pPIN : PIN, reserved for future
 * @param[in] : unsigned int nPINLength : length of PIN, reserved for future
 * return value : 0 : success
 * 		      < 0 : error code
 */
typedef int(*HSM_OSM_RESET)(char* pPIN, unsigned int nPINLength);

typedef int(*HSM_OSM_QUERY_CERT_COUNT)(unsigned int nProperty);

typedef int(*HSM_OSM_QUERY_PRIVATE_KEY_LABELS)(unsigned char *pBuffer, unsigned int nBufferLength);

typedef int(*HSM_OSM_QUERY_PRIVATE_KEY_COUNT)();

typedef int(*HSM_OSM_DELETE_CERT)(unsigned int nProperty,char* label);

typedef int(*HSM_OSM_DELETE_PRIVATE_KEY)(char* label);

typedef int(*HSM_OSM_GET_CERT)(unsigned int nProperty, char* label, unsigned char* pDataBuffer, unsigned int nDataBufferLength, HSM_OBJECT_DATA_TYPE nDataType);

typedef int(*HSM_OSM_GET_PUBLIC_KEY)(char* label, unsigned char* pDataBuffer, unsigned int nDataBufferLength, HSM_OBJECT_DATA_TYPE nDataType);

typedef int(*HSM_OSM_GET_RANDOM)(unsigned char *pBuffer, unsigned int nBufferLength);
//hsm_osm_generate_csr
typedef int(*HSM_OSM_GENERATE_CSR)(char* pPriKeyLabel, char* pCommonName, unsigned char* pDataBuffer, unsigned int nDataBufferLength);

//hsm_osm_generate_pinpad_csr
typedef int(*HSM_OSM_GENERATE_PINPAD_CSR)(unsigned char* pDataBuffer, unsigned int nDataBufferLength);
/*
 * enable external sensor
 * @param[in]: unsigned int nSensorMask: indicate to enable which sensor, value must from 1 to 15.
 * BIT0: DRS0
 * BIT1: DRS1
 * BIT2: DRS2
 * BIT3: DRS3
 * return value : = 0 : success
 *                  ！= 0 :  fail
 */
typedef int (*HSM_OSM_ENABLE_SENSOR)(unsigned int nSensorMask);

/*
 * delete object from hardware security module
 * @param[in] : HSM_OBJECT_PROPERTY* pObjectProperty : object property
 * @param[in] : char* pPIN : PIN
 * @param[in] : unsigned int nPINLength : length of PIN
 * return value : 0 : success
 * 				  < 0 : error code
 */
typedef int (*HSM_OSM_DELETE_OBJECT)(HSM_OBJECT_PROPERTY* pObjectProperty, char* pPIN, unsigned int nPINLength);

/*
 * save object
 * @param[in] : HSM_OBJECT_PROPERTY* pObjectProperty : object property
 * @param[in] : unsigned char* pObjectData : data
 * @param[in] : unsigned int nDataLength : data length
 * @param[in] : object data format : currently, we only support PEM format
 * return value : 0 : success
 *                < 0 : error code
 */
typedef int (*HSM_OSM_SAVE_OBJECT)(HSM_OBJECT_PROPERTY* pObjectProperty, unsigned char* pObjectData, unsigned int nDataLength, HSM_OBJECT_DATA_TYPE nDataType);

/*
 * Update key
 * @param[in]: unsigned int nKeyID: key index, from 0 to 9.
 * @param[in]: KEY_TYPE nKeyType: indicate the key type:SM4, DES,3DES or AES.
 * @param[in]: unsigned char* pKeyBuffer: key data.
 * @param[in]: unsigned int nBufferLength: key data length.
 * return value : >= 0 : success
 *               <  0 : fail
 */
typedef int (*HSM_OSM_UPDATE_KEY)(unsigned int nKeyID, unsigned int nProperty, unsigned char* pKeyBuffer, unsigned int nBufferLength);

/*
 *  Key encrypt
 * @param[in]: unsigned int nKeyID: key index, from 0 to 9.
 * @param[in]: KEY_TYPE nKeyType: indicate the key type:SM4, DES, 3DES or AES.
 * @param[in]: unsigned int nMode: 0: ECB 1: CBC 2:CFB 3:OFB
 * @param[in][out]: unsigned char* pData: plain text to be encryt.
 * @param[in]: unsigned int nDataLength: plain text length.
 * @param[in]: unsigned char* pIV: initialize vector.
 * @param[in]: unsigned int nIVLength: initialize vector length.
 * return value : >= 0 : cipher text length
 *               <  0 : fail
 */
typedef int (*HSM_OSM_KEY_ENCRYPT)(unsigned int nKeyID, unsigned int nProperty, unsigned int nMode, unsigned char* pData, unsigned int nDataLength, unsigned char* pIV, unsigned int nIVLength);

/*
 *  Key decrypt
 * @param[in]: unsigned int nKeyID: key index, from 0 to 9.
 * @param[in]: KEY_TYPE nKeyType: indicate the key type:SM4, DES, 3DES or AES.
 * @param[in]: unsigned int nMode: 0: ECB 1: CBC 2:CFB 3:OFB
 * @param[in][out]: unsigned char* pData: plain text to be encryt.
 * @param[in]: unsigned int nDataLength: plain text length.
 * @param[in]: unsigned char* pIV: initialize vector.
 * @param[in]: unsigned int nIVLength: initialize vector length.
 * return value : >= 0 : cipher text length
 *               <  0 : fail
 */
typedef int (*HSM_OSM_KEY_DECRYPT)(unsigned int nKeyID, unsigned int nProperty, unsigned int nMode, unsigned char* pData, unsigned int nDataLength, unsigned char* pIV, unsigned int nIVLength);


/*
 *  Query key whether exist or not
 * @param[in]: unsigned int nKeyID: key index, from 0 to 9.
 * @param[in]: KEY_TYPE nKeyType: indicate the key type:SM4, DES, 3DES or AES.
 * return value : false : not exist
 *                     true : exist
 */
typedef bool (*HSM_OSM_IS_KEY_EXIST)(unsigned int nKeyID, unsigned int nProperty);


/*
 *  get flash id
 * @param[in][out]: unsigned char* pBuffer: data buffer
 * @param[int] : unsigned int nBufferLength : length of data buffer
 * return value : >= 0 : flash id length
 *               <  0 : fail
 */
typedef int(*HSM_OSM_GET_FLASH_ID)(unsigned char* pBuffer, unsigned int nBufferLength);


/*
 * Update sm4 key
 * @param[in]: unsigned int nKeyID: key index, from 0 to 2.
 * @param[in]: unsigned char* pKeyBuffer: key data.
 * @param[in]: unsigned int nBufferLength: key data length.
 * @param[in]: unsigned char* pSignature,: signature data.
 * @param[in]: unsigned int nSignatureLen: signature data length.
 * return value : >= 0 : success
 *               <  0 : fail
 */
typedef int(*HSM_OSM_UPDATE_SM4)(unsigned int nKeyID, unsigned char* pKeyBuffer, unsigned int nBufferLength, unsigned char* pSignature, unsigned int nSignatureLen);

/*
 *  Save x509 crl
 * @param[in]: char* pLabel: the label of crl
 * @param[in]: unsigned char* pData: crl data
 * @param[in]: unsigned int nDataLength: crl data length
 * @param[in] : nDataType : data type, currently only support PEM format
 * return value : >= : success
 *                     < 0 : fail
 */
typedef int(*HSM_OSM_SAVE_CRL)(char* pLabel, unsigned char* pData, unsigned int nDataLength, int nDataType);

/*
 *  Get x509 crl
 * @param[in]: char* pLabel: the label of crl
 * @param[in][out]: unsigned char* pBuffer: buffer to save crl data
 * @param[in]: unsigned int nBufferLength: buffer length
 * @param[in] : nDataType : data type, currently only support PEM format
 * return value : > 0 : success, return crl data length
 *                     <= 0 : fail
 */
typedef int(*HSM_OSM_GET_CRL)(char* pLabel, unsigned char* pBuffer, unsigned int nBufferLength, int nDataType);

/*
 * query the lable of crl  in the hardware secure module, every label separated by ':'
 * @param[in][out]: data buffer
 * @param[in] : length of data buffer
 * return value :< 0 : error code
 *                  >= 0 : data length
 */
typedef int(*HSM_OSM_QUERY_CRL_LABELS)(unsigned char *pBuffer, unsigned int nBufferLength);
#ifdef __cplusplus
}
#endif

#endif
