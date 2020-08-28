package com.wizarpos.jni;

public class MsrInterface
{
	static
	{
		System.loadLibrary("wizarpos_magnetic_stripe_reader");
	}
    public native static int open();
    public native static int close();
    public native static int poll(int nTimout);
    public native static int cancelPoll();
    public native static int getTrackError(int nTrackIndex);
    public native static int getTrackDataLength(int nTrackIndex);
    public native static int getTrackData(int nTrackIndex,byte[] byteArry,int nLength);

}
