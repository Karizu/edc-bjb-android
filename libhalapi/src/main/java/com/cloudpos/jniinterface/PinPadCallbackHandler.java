package com.cloudpos.jniinterface;

public interface PinPadCallbackHandler {
    /**
     * Call back method.
     * 
     * @param data : callback data from the driver, data[0] is the count of *, data[1] is other parameters
     */
	public void processCallback(byte[] data);
}
