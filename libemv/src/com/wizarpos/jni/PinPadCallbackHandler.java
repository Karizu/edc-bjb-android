package com.wizarpos.jni;

public interface PinPadCallbackHandler
{
	public void processCallback(byte[] data);
	public void processCallback(int nCount, int nExtra);
}
