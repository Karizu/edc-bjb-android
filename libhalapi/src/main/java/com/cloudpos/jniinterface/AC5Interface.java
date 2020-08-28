package com.cloudpos.jniinterface;

public class AC5Interface {
	static{
		String fileName = "jni_cloudpos_ac5";
		JNILoad.jniLoad(fileName);
	}
	
    public native static int open();
    public native static int close();
    
    /*
     * touch the ac5 device(ac5 module device)
     * return value : 0 : success
     * 				  < 0 : error code
     */
    public native static int touch();
    /*
     * let the ac5 device selftest with sm4(ac5 module device)
     * return value : 0 : success
     * 				  < 0 : error code
     */
    public  native static int selftest();

}
