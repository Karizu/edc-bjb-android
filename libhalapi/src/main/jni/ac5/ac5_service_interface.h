#ifndef AC5_SERVICE_INTERFACE_H
#define AC5_SERVICE_INTERFACE_H
/*error codes:
 *	    produce key length error					-5
 *		can't find ID or label						-6
 *		cert Index error							-7
 *		encryption or decryption falth				-8
 *		can't save more certificate					-9
 *		length error								-10
 *		memory allocate error						-13
 *		other										-1
 */
#include <stdbool.h>

#ifdef __cplusplus
extern "C"
{
#endif

/*
 * open the ac5 device(ac5 module device)
 * return value : 0 : success
 * 				  < 0 : error code
 */
int ac5_open();

/*
 * open the ac5 device(ac5 module device)
 * return value : 0 : success
 * 				  < 0 : error code
 */
int ac5_close();

/*
 * close the ac5 device(ac5 module device)
 * return value : 0 : success
 * 				  < 0 : error code
 */
int ac5_reset();

/*
 * make the ac5 device sleep(ac5 module device)
 * return value : 0 : success
 * 				  < 0 : error code
 */
int ac5_sleep();

/*
 * wakeup the ac5 device(ac5 module device)
 * return value : 0 : success
 * 				  < 0 : error code
 */
int ac5_wakeup();

/*
 * touch the ac5 device(ac5 module device)
 * return value : 0 : success
 * 				  < 0 : error code
 */
typedef int (*AC5_TOUCH)(void);

/*
 * let the ac5 device selftest with sm4(ac5 module device)
 * return value : 0 : success
 * 				  < 0 : error code
 */
typedef int (*AC5_SELFTEST)(void);

/*
 * use the ac5 device crypt(ac5 module device)
 * return value : 0 : success
 * 				  < 0 : error code
 */
int ac5_set_active();

/*
 * no use the ac5 device crypt(ac5 module device)
 * return value : 0 : success
 * 				  < 0 : error code
 */
int ac5_set_deactive();

/*
 * test if crypt with the ac5 device(ac5 module device)
 * return value : 0 : success
 * 				  < 0 : error code
 */
int ac5_isactive();

/*
 * get the ac5 device soft version(ac5 module device)
 * return value : 0 : success
 * 				  < 0 : error code
 */
int ac5_getversion(unsigned char* pVer, unsigned int nVerLen);

/*
 * set the ac5 device algo sm4 encrypt(ac5 module device)
 * return value : 0 : success
 * 				  < 0 : error code
 */
int ac5_sm4_set_encrypt();

/*
 * set the ac5 device algo sm4 decrypt(ac5 module device)
 * return value : 0 : success
 * 				  < 0 : error code
 */
int ac5_sm4_set_decrypt();

/*
 * set the ac5 device algo sm4 ecb mode(ac5 module device)
 * return value : 0 : success
 * 				  < 0 : error code
 */
int ac5_sm4_set_ecb();

/*
 * set the ac5 device algo sm4 cbc mode(ac5 module device)
 * return value : 0 : success
 * 				  < 0 : error code
 */
int ac5_sm4_set_cbc();

/*
 * set the ac5 device algo sm4 key(ac5 module device)
 * return value : 0 : success
 * 				  < 0 : error code
 */
int ac5_sm4_set_key(unsigned char* pKey, unsigned int nKeyLen);

/*
 * set the ac5 device algo sm4 iv(ac5 module device)
 * return value : 0 : success
 * 				  < 0 : error code
 */
int ac5_sm4_set_iv(unsigned char* pIV, unsigned int nIVLen);

/*
 * sm4 crypt data with the ac5 device(ac5 module device)
 * return value : 0 : success
 * 				  < 0 : error code
 */
int ac5_sm4_crypt(unsigned char* pData, unsigned int nDataLen);

#ifdef __cplusplus
}
#endif


#endif
