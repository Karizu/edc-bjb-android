/*************************************************************************
 > File Name: fingerprint_interface.h
 > Author:
 > Mail:
 > Created Time: Fri 15 Jan 2016 09:31:10 AM CST
 ************************************************************************/

#ifndef _FINGERPRINT_INTERFACE_H
#define _FINGERPRINT_INTERFACE_H
#ifdef __cplusplus
extern "C" {
#endif

/** new interface **/

typedef int (*fp_iso_open)(void);

typedef int (*fp_iso_close)(void);

typedef int (*fp_iso_cancel)(void);

typedef int (*fp_iso_enroll)(int nUserId, int nTimeOut_Ms);

typedef int (*fp_iso_verifyall)(int nTimeOut_Ms);

typedef int (*fp_iso_delallfingers)(void);

typedef int (*fp_iso_delfinger)(int nUserId);

typedef int (*fp_iso_verifyagainstuserid)(int nUserId, int nTimeOut_Ms);

typedef int (*fp_iso_verifyagainstfeature)(unsigned char *pFeature, int nFeatureLen, int nTimeOut_Ms);

typedef int (*fp_iso_listallfingers)(unsigned char *pData, int nDataLen, int *pFingersCount);

typedef int (*fp_iso_getuserfeature)(int nUserId, unsigned char *pFeaBuffer, int nFeaLength, int *pRealFeaLength, int type);

typedef int (*fp_iso_storefeature)(int nUserId, unsigned char *pFeature, int nFeatureLen);

typedef int (*fp_iso_get_fea)(unsigned char *pFeaBuffer, int nFeaLength, int *pRealFeaLength, int n_TimeOut_S, int type);

typedef int (*fp_iso_getImage)(unsigned char *pImgBuffer,int nImgLength, int *pRealImaLength, int *pImgWidth, int *pImgHeight, int type);

typedef int (*fp_iso_convertformat)(unsigned char *pFeaIn, int nFeaInLength, int nTypeIn, unsigned char *pFeaOut, int nFeaOutLength, int *pRealFeaLength, int nTypeOut);

typedef int (*fp_iso_getid)(void);

typedef int (*fp_iso_match)(unsigned char *pFeaBuffer1, int nFea1Length, unsigned char *pFealBuffer2, int nFea2Length);
#ifdef __cplusplus
}
#endif
#endif
