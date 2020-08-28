package com.cloudpos.jniinterface;

public class PrinterHtmlInterface {
    static {
        String fileName = "jni_cloudpos_printerhtml";
        System.loadLibrary(fileName);
    }

    /**
     * @param bufBitmap    bufBitmap
     * @param bitmapOffset start index
     * @param result       result
     * @param resultOffset start index
     * @param w            srcBmp width
     * @param h            srcBmp height
     *                     return value: the length of the valid data in result buffer.
     */
    public synchronized native static int bitmapGSVMSBToBufferRGB(byte[] bufBitmap, int bitmapOffset, byte[] result, int resultOffset, int w, int h);

    public synchronized native static int bitmapGSVMSBToBufferGray(byte[] bufBitmap, int bitmapOffset, byte[] result, int resultOffset, int w, int h);
}
