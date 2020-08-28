package com.cloudpos.jniinterface;

import com.wizarpos.jni.PinPadCallbackHandler;

/**
 * Created by Administrator on 2019/2/22.
 */

public class EMVJNIInterface {
	// EMV Function
	static
	{
		System.loadLibrary("wizarpos_emv_kernel");
	}
	public static IFuntionListener functionListener = null;
	public static void registerFunctionListener(IFuntionListener afunctionListener){
		functionListener = afunctionListener;
	}
	public static void emvProcessCallback(byte[] data)
	{
		functionListener.emvProcessCallback(data);
	}
	public static void cardEventOccured(int eventType)
	{
		functionListener.cardEventOccured(eventType);
	}

	// pinpad
	private static PinPadCallbackHandler emvOfflinePinCallbackHandler;
	public static int setEmvOfflinePinCallbackHandler(PinPadCallbackHandler handler) {
		emvOfflinePinCallbackHandler = handler;
		if (handler != null) {
			return setPinpadKeyEventCallback();
		}
		return 0;
	}

	public static void emvOfflinePinCallback(int nCount, int nExtra) {
		if (emvOfflinePinCallbackHandler != null) {
			emvOfflinePinCallbackHandler.processCallback(nCount, nExtra);
		}
	}

	// pinpad functions
	public native static int setPinpadKeyEventCallback();

	/**
	 * Load libEMVKernal.so from corresponding path
	 * @param libDir full path of the lib
	 * @param DirLength length of the libDir
	 */
	public native static byte loadEMVKernel(byte[] libDir,int DirLength);
	public native static byte exitEMVKernel();

	/**
	 * open reader and wait card
	 * @param reader    0 - all of readers
	 *			        1 - only contact reader
	 *			        2 - only contactless
	 * @return
	 *          < 0  Fail
	 *          >= 0 Success
	 *   (If select open all of readers, any open success return success)
	 */
	public native static int open_reader(int reader);

	public native static int open_reader_ex(int reader, int extraParam);	// extraParam : 0 - need look for card; 1 - Card Inserted
	/**
	 * close reader
	 * @param reader    0 - all of readers
	 *                  1 - only contact reader
	 *			        2 - only contactless reader
	 */
	public native static void close_reader(int reader);
	public native static int poweron_card();
	/**
	 * get current card type
	 * @return  1 - contact card
	 *			2 - contactless card
	 *		   -1 - no card
	 */
	public native static int get_card_type();

	/**
	 * get card ATR
	 * @param atr   the value of ATR
	 * @return      the length of ATR
	 */
	public native static int get_card_atr(byte[] atr);
	/**
	 * APDU command
	 * @param cmd               APDU command
	 * @param cmdLength         the length of APDU command
	 * @param respData          the value of card response
	 * @param respDataLength    accepted max length of card reasponse
	 * @return  >= 0 : the length of card response
	 *          < 0  : Fail
	 */
	public native static int transmit_card(byte[] cmd, int cmdLength, byte[] respData, int respDataLength);


	public native static int query_contact_card_presence();
	/**
	 * @param enable
	 *    0 - don't detach card when close contactless reader；
	 *    1 - waiting detach card when close contactless reader
	 *    2 - do not control
	 * */
	public native static void set_contactless_detach_enable(int enable);

	/**
	 * EMV kernel init, execute only one time when start application
	 */
	public native static void emv_kernel_initialize();                                                          // 0

	/**
	 * check the existence of data for the tag
	 * @param tag  tag name
	 * return value < 0  : the data not exist
	 *				>= 0 : the length of data
	 */
	public native static int emv_is_tag_present(int tag);                              	                        // 1

	/**
	 * get the data for the tag
	 * @param tag        tag name
	 * @param data       the value of the data
	 * @param dataLength accepted max length of the data
	 * @return 		< 0 : Fail
	 *				>= 0: the length of the data
	 */
	public native static int emv_get_tag_data(int tag, byte[] data, int dataLength);	                        // 2
	/**
	 * get the data for the tag list
	 * @param tagList 		 the list of the tags
	 * @param tagCount 		 the count of the tags
	 * @param data 			 the values of the data（TLV format）
	 * @param dataLength 	 accepted max length of the data
	 * @return 		< 0 : Fail
	 *				>= 0: the length of the data
	 */
	public native static int emv_get_tag_list_data(int[] tagList, int tagCount, byte[] data, int dataLength);	// 3
	/**
	 * set the data for the tag
	 * @param tag 	    tag name
	 * @param data 	    the value of the data
	 * @param dataLength the length of the data
	 * @return      < 0 : Fail
	 *				>= 0 : the tag的长度
	 */
	public native static int emv_set_tag_data(int tag, byte[] data, int dataLength);                     		// 4
	public native static int emv_preprocess_qpboc();                                                            // 5

	/**
	 * Init transaction data, execute this function every time before performing a emv transaction
	 */
	public native static void emv_trans_initialize();                                                           // 6
	/**
	 * Get EMV Kernel version
	 * @param data 	 the value of emv kernel version
	 * @param dataLength accepted max length of emv kernel version
	 * @return  the length of emv kernel verion
	 */
	public native static int emv_get_version_string(byte[] data, int dataLength);					            // 7
	/**
	 * Set transaction amount
	 * @param amount ascii character bytes and '\0' as ending mark
	 * @return  >=0 : Success; < 0 : Fail
	 */
	public native static int emv_set_trans_amount(byte[] amount); 							                    // 8  ASC 以分为单位
	/**
	 * Set other amount
	 * @param amount ascii character bytes and '\0' as ending mark
	 * @return  >=0 Success; < 0 Fail
	 */
	public native static int emv_set_other_amount(byte[] amount);						                        // 9

	/**
	 * Set transaction type before start
	 * @param transType
	 *  TRANS_GOODS_SERVICE     	0x00
	 *  TRANS_CASH              	0x01
	 *  TRANS_INQUIRY            	0x04
	 *  TRANS_TRANSFER           	0x05
	 *  TRANS_PAYMENT           	0x06
	 *  TRANS_ADMIN            	    0x07
	 *  TRANS_CASHBACK              0x09
	 *  TRANS_CARD_RECORD      	    0x0A
	 */
	public native static int emv_set_trans_type(byte transType);							                    //10
	/**
	 * @param kernelType 	1    EMV Contact Kernel
	 *                      2    EMV Contactless Kernel
	 *                      3    UPCASH Kernel for China Union Pay
	 */
	public native static int emv_set_kernel_type(byte kernelType);							                    //11

	public native static int emv_get_kernel_type();

	/**
	 * emv process next, execute this function to notify emv kernel to continue performing transaction
	 */
	public native static int emv_process_next();												                //12
	/**
	 * Is needed advice the transaction
	 * @return  1 need advice
	 *          0 not need advice
	 */
	public native static int emv_is_need_advice();							                                    //13
	/**
	 * Is needed sign the transaction
	 * @return  1 need sign
	 *          0 not need sign
	 */
	public native static int emv_is_need_signature();							                                //14
	/**
	 * Set the transaction force online
	 * @param flag： flag=1 Yes， flag = 0 No
	 */
	public native static int emv_set_force_online(int flag);							                        //15
	/**
	 * Read transaction record from the card
	 * @param data 		transaction record
	 * @param dataLength accepted max length for the transaction record
	 * @return 	 < 0 : Fail
	 *			 >= 0: record count
	 */
	public native static int emv_get_card_record(byte[] data, int dataLength);							        //16
	/**
	 * Get candidate application list
	 * @param data  application list as "LV" format
	 * @param dataLength accepted max length for application list
	 * @return  < 0 : Fail
	 *			>= 0: application count
	 */
	public native static int emv_get_candidate_list(byte[] data, int dataLength);
	/**
	 * Get candidate application list with TLV Format
	 * @param data  application list with "TLV" format
	 *                      Tag 4F: AID, It is the start of candidate record
	 *                      Tag 50: Application Label
	 *                      Tag 9F12: Application Preferred Name
	 * @param dataLength  accepted max length for application list
	 * @return  < 0 : Fail
	 *			>= 0: the length of data
	 */
	public native static int emv_get_candidate_list_tlv(byte[] data, int dataLength);
	/**
	 * Set the selected index for application selection
	 * @param index the selected index (started by 0)
	 * @return  < 0 : Fail
	 *			>= 0: Success
	 */
	public native static int emv_set_candidate_list_result(int index);							                //18
	/**
	 * Set the result for cardholder ID check
	 * ID Type（9F62）、ID Number(9F61)
	 * @param result  0: check Fail, 1:check success
	 * @return  < 0 : Fail
	 *			>= 0: Success
	 */
	public native static int emv_set_id_check_result(int result);							                    //19
	/**
	 * Set the result for Online PIN
	 * @param result  0: Online PIN not input, 1:Online PIN inputted
	 * @return  < 0 : Fail
	 *			>= 0: Success
	 */
	public native static int emv_set_online_pin_entered(int result);


	/**
	 * Set acceptance for Bypass Offline PIN
	 * @param flag 0: Disable bypass pin
	 *             1: Allowed bypass pin
	 * @return   < 0 : Fail
	 *			>= 0: Success
	 */
	public native static int emv_set_bypass_pin(int flag);

	/**
	 * Set the result for online certification
	 * @param result   -1:communication failed, 0: host refused, 1: host accepted
	 * @param respCode  2 bytes response code from the host
	 * @param issuerRespData  the emv data from the host
	 * @param issuerRespDataLength  the length of the emv data from the host
	 * @return value	< 0 : Fail
	 *					>= 0: Success
	 */
	public native static int emv_set_online_result(int result, byte[] respCode, byte[] issuerRespData, int issuerRespDataLength); // 22

	/**
	 * Clear AID parameters
	 * @return  >=0: Success; < 0: Fail
	 */
	public native static int emv_aidparam_clear();                             							        //23
	/**
	 * @param data  see form below，format is TLV
	 *  ----------------------------------------------------------------------------------------------------------------------
	 *  |  tag          Format	        length（byte）	    name
	 *  ----------------------------------------------------------------------------------------------------------------------
	 *  |  9F06         b	            5－16	            AID
	 *  |  DF01         b	            1	                Application selection Indicator（ASI）
	 *  |  9F08         b	            2	                Application version number
	 *  |  DF11         b	            5	                TAC－Default
	 *  |  DF12         b	            5	                TAC－Online
	 *  |  DF13         b	            5	                TAC－Denial
	 *  |  9F1B         b	            4	                Terminal floor limit
	 *  |  DF15         b	            4	                Threshold value for Biased Random Selection
	 *  |  DF16         cn	            1	                Maximum Target Percentage to be used for Biased Random Selection
	 *  |  DF17         cn	            1	                Target Percentage to be used for Random Selection
	 *  |  DF14         b	            Var.	            Default DDOL
	 *  |  DF18         b	            1	                Ability for Online PIN
	 *  |  50           an	            1-16	            Application Label
	 *  |  9F12         an	            1-16	            Application Preferred Name
	 *  |  87           b	            1	                Application Priority Indicator
	 *  |  9F16         an	            15	                Merchant Identifier
	 *  |  9F01         n	            6-11	            Acquirer Identifier
	 *  |  9F15         n	            4	                MCC
	 *  |  9F3C         n	            3	                Transaction Reference Currency Code
	 *  |  9F3D         n	            1	                Transaction Reference Currency Exponent
	 *  |  DF22         b	            Var.	            Default TDOL
	 *  |  DF19         n	            6	                Contactless Floor Limit
	 *  |  DF20         n	            6	                Contactless Limit
	 *  |  DF21         n	            6	                CVM Limit
	 *  |  DF810C       n	            1	                Kernel ID
	 *  |  DF8118       b	            1	                C2: CVM Capability – CVM Required
	 *  |  DF8119       b	            1	                C2: CVM Capability – No CVM Required
	 *  |  DF811B       b	            2	                C2: kernel configuration
	 *  |  DF811E       b	            1	                C2: Mag-stripe CVM Capability – CVM Required
	 *  |  DF8124       n	            6	                C2: Reader Contactless transaction limit (No On-device CVM)
	 *  |  DF8125       n	            6	                C2: Reader Contactless transaction limit (On-device CVM)
	 *  |  DF812C       b	            1	                C2: Mag-stripe CVM Capability – No CVM Required
	 *  |  EF07         n	            1	                Is US Common Debit AID 0 – No; 1 - Yes
	 *  |  EF08         n	            1	                Is apply to NSICCS (Indonesia) 0 - No; 1 - Yes
	 *  ----------------------------------------------------------------------------------------------------------------------
	 *              * C2 – Only for Mastercard Paypass
	 * @param dataLength the length of the data
	 * @return  < 0 : Fail
	 *			>= 0: Success
	 */
	public native static int emv_aidparam_add(byte[] data, int dataLength);							            //24

	public native static int emv_contactless_aidparam_clear();
	public native static int emv_contactless_aidparam_add(byte[] data, int dataLength);							            //24

	/**
	 * Clear CAPK parameters
	 * @return  >=0 Success; < 0 Fail
	 */
	public native static int emv_capkparam_clear();							                                    //25
	/**
	 * @param data  see form below，format is TLV
	 *  ----------------------------------------------------------------------------------------------------------------------
	 *  | tag           Format          length(byte)        Name
	 *  ----------------------------------------------------------------------------------------------------------------------
	 *  | 9F06          b               5                   RID
	 *  | 9F22          b               1                   Certification Authority Public Key Index
	 *  | DF05          n8              8                   Certification Authority Public Key Expiration Date
	 *  | DF06          b               1                   Certification Authority Public Key hash Algorithm Indicator
	 *  | DF07          b               1                   Certification Authority Public Key  Algorithm Indicator
	 *  | DF02          b               Var.                Certification Authority Public Key Modulus
	 *  | DF04          b               1 or 3              Certification Authority Public Key Exponent
	 *  | DF03          b               Var.                Certification Authority Public Key Checksum
	 *  ----------------------------------------------------------------------------------------------------------------------
	 * @param dataLength  the length of the data
	 * @return   < 0 : Fail
	 *		     >= 0: Success
	 */
	public native static int emv_capkparam_add(byte[] data, int dataLength);    			                    //26

	/**
	 * Set EMV terminal parameters by TLV
	 * @param TerminalParam see the table for tlv
	 *  ------------------------------------------------------------------------------------------------------------------------
	 *  | Supported Tag	        Description
	 *  | ----------------------------------------------------------------------------------------------------------------------
	 *  | 5F2A	                Transaction Currency Code
	 *  | 5F36	                Transaction Currency Exponent
	 *  | 9F16	                Merchant Identification
	 *  | 9F1A	                Terminal Country Code
	 *  | 9F1C	                Terminal Identification
	 *  | 9F1E	                IFD Serial Number
	 *  | 9F33	                Terminal Capabilities
	 *  | 9F35	                Terminal Type
	 *  | 9F40	                Additional Terminal Capabilities
	 *  | 9F4E	                Merchant Name and Location
	 *  | 9F66	                TTQ first byte
	 *  | DF19	                Contactless floor limit
	 *  | DF20	                Contactless transaction limit
	 *  | DF21	                CVM limit
	 *  | DF8104	            Balance Read Before Gen AC (C2)
	 *  | DF8105	            Balance Read After Gen AC (C2)
	 *  | DF811C	            Max Lifetime of Torn Transaction Log Record (C2)
	 *  | DF811D	            Max Number of Torn Transaction Log Records (C2)
	 *  | DF812D	            Message Hold Time (C2)
	 *  | DF8132	            Minimum Relay Resistance Grace Period (C2)
	 *  | DF8133	            Maximum Relay Resistance Grace Period (C2)
	 *  | DF8134	            Terminal Expected Transmission Time For Relay Resistance C-APDU (C2)
	 *  | DF8135	            Terminal Expected Transmission Time For Relay Resistance R-APDU (C2)
	 *  | DF8136	            Relay Resistance Accuracy Threshold (C2)
	 *  | DF8137	            Relay Resistance Transmission Time Mismatch Threshold (C2)
	 *  | EF01	                Status check support: 0 – No; 1 – Support
	 *  | EF02	                Zero check support: 0 – No; 1 – Support
	 *  | EF03	                Authorization Type For American Expresspay(C4): 0-Immediate; 1-Delayed
	 *  | EF04	                CDCVM support: 0 – No; 1 – Support
	 *  | EF05	                Extended Selection: 0 – No; 1 – Support
	 *  | EF06	                Priority of US Common Debit AID：0 – The priority of US Common Debit AID is lower than Global AID; 1 – The priority of US Common Debit AID is higher than Global AID
	 *  ----------------------------------------------------------------------------------------------------------------------
	 * @param paramLength the length of the data
	 * @return      < 0 : Fail
	 *              >= 0: Success
	 */
	public native static int emv_terminal_param_set_tlv(byte[] TerminalParam, int paramLength);

	public native static int emv_terminal_param_set_drl(byte[] TerminalParam, int paramLength);

	/**
	 * Clear Exception File
	 * @return  >=0 Success; < 0 Fail
	 */
	public native static int emv_exception_file_clear();							                            //28

	/**
	 * Add Exception File
	 * @param exceptFile 20 bytes byte-array, 19 bytes card number and 1 byte Pan Sequence
	 * @return  >=0 Success; < 0 Fail
	 */
	public native static int emv_exception_file_add(byte[] exceptFile);							                //29

	/**
	 * Clear Revoked Certicates
	 * @return >=0 Success; < 0 Fail
	 */
	public native static int emv_revoked_cert_clear();							                                //30

	/**
	 * Add revoked Certificate
	 * @param revokedCert 6 bytes byte-array, 5 bytes RID and 1 byte CAPK-index
	 * @return >=0 Success; < 0 Fail
	 */
	public native static int emv_revoked_cert_add(byte[] revokedCert);							                //31

	public native static int emv_log_file_clear();                                                              //32
	/**
	 * @param data is less or equal 2 bytes,
	 *  Byte 1:
	 *  bit 8 Enable auto perform UPCASH for contact card.
	 *  bit 7 Force select CUP application.
	 *  bit 6 Force check app version in FDDA for CUP contactless.
	 *  bit 5 Force online with Cash & CashBack for Visa contacltess.
	 *  bit 4 Subsequent Bypass PIN entry
	 *  bit 3 Disable PayWave AUC check.
	 *  bit 2 RFU
	 *  bit 1 RFU
	 *
	 *  Byte 2:
	 *  bit 8 Enable contactless AID select.
	 *  bit 7 RFU
	 *  bit 6 RFU
	 *  bit 5 RFU
	 *  bit 4 RFU
	 *  bit 3 RFU
	 *  bit 2 RFU
	 *  bit 1 RFU
	 */
	public native static int emv_set_kernel_attr(byte[] data, int dataLength);
	public native static int emv_set_currency_symbol(byte[] data, int dataLength);
	public native static void emv_set_anti_shake(int flag);

	/**
	 * anti_shake_finish
	 * @param flag 0 - Not found Contact or Mag card, continue contactless transaction
	 *             1 - found Contact or Mag card, terminate contactless transaction
	 * */
	public native static void emv_anti_shake_finish(int flag);

	/**
	 * Get Kernel checksum
	 * @param data 	        the value of emv kernel checksum
	 * @param dataLength    accepted max length
	 * @return the length of kernel checksum
	 */
	public native static int emv_get_kernel_checksum(byte[] data, int dataLength);
	/**
	 * Get Configuration checksum
	 * @param data 	        the value of configuration checksum
	 * @param dataLength    accepted max length
	 * @return  the length of configuration checksum
	 */
	public native static int emv_get_config_checksum(byte[] data, int dataLength);
	/**
	 * Set the transaction Force AAC for first generate AC
	 * @param flag flag=1 Yes, flag = 0 No
	 */
	public native static int emv_set_force_aac(int flag);

	/**
	 * generate pseudo track1
	 * @param data value to save preudo track1
	 * @param dataLength data length
	 * @return the real length of preudo track1 data or an error code(<0)
	 */
	public native static int emv_generate_pseudo_track1(byte[] data, int dataLength);

	/**
	 * generate pseudo track2
	 * @param data value to save preudo track2
	 * @param dataLength data length
	 * @return the real length of preudo track2 data or an error code(<0)
	 */
	public native static int emv_generate_pseudo_track2(byte[] data, int dataLength);

	/**
	 * get contactless kernel id
	 * @return 0-contact
	 *         2-MasterCard
	 *         3-VISA
	 *         4-AMEX
	 *         5-JCB
	 *         6-DISCOVER
	 *         7-CUP
	 *         8-GEMALTO PURE Contactless
	 */
	public native static int emv_get_kernel_id();

	/**
	 * get process type of contactless card
	 * @return 2-EMV mode
	 *         3-MSD mode
	 */
	public native static int emv_get_process_type();

	/**
	 * is offline verified
	 * @return -1 - NO(Wrong PIN)
	 *          1 - YES
	 */
	public native static int emv_offlinepin_verified();

	/**
	 * return the times of offline pin entry
	 */
	public native static int emv_get_offlinepin_times();

	/**
	 * @param flag 0 English, 1 Spanish, 2 - Russian
	 */
	public native static int set_display_language(int flag);
}
