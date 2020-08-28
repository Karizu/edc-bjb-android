package com.wizarpos.jni;

public class PrinterInterface 
{
	/*native interface */
	static
	{
		System.loadLibrary("wizarpos_printer");
	}
	
	public native static int open();
	public native static int close();
	public native static int begin();
	public native static int end();
	public native static int write(byte arryData[], int nDataLength);

}
