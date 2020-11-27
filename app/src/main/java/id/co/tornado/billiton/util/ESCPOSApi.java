package id.co.tornado.billiton.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.wizarpos.apidemo.jniinterface.PrinterInterface;
import com.wizarpos.apidemo.printer.FontSize;
import com.wizarpos.apidemo.printer.PrintSize;
import com.wizarpos.htmllibrary.PrinterBitmapUtil;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.BitSet;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.cloudpos.DeviceException;
import com.cloudpos.POSTerminal;
import com.cloudpos.printer.Format;
import com.cloudpos.printer.PrinterDevice;

import id.co.tornado.billiton.R;

/**
 * Created by indra on 30/11/15.
 */
public class ESCPOSApi {

    public static final byte[] INITIALIZE_PRINTER = new byte[]{0x1B, 0x40};

    public static final byte[] PRINT_AND_FEED_PAPER = new byte[]{0x0A};

    public static final byte[] SELECT_BIT_IMAGE_MODE = new byte[]{(byte) 0x1B, (byte) 0x2A};
    public static final byte[] SET_LINE_SPACING = new byte[]{0x1B, 0x33};
    public static final byte[] CENTER_ALIGN = {0x1B, 0x61, 1};
    public static final byte[] LEFT_ALIGN = {0x1B, 0x61, 0};
    public static final byte[] RIGHT_ALIGN = {0x1B, 0x61, 2};
    public static final byte[] TEXT_BOLD = {0x1B, 0x45};

    public static final byte[] LINE_SPACING_24DOTS = buildPOSCommand(SET_LINE_SPACING, (byte) 24);
    public static final byte[] LINE_SPACING_30DOTS = buildPOSCommand(SET_LINE_SPACING, (byte) 30);


    public static int maxBitsWidth = 255;

    public static byte[] setLineSpacing(int size) {
        return buildPOSCommand(SET_LINE_SPACING, (byte) size);
    }

    private static byte[] buildPOSCommand(byte[] command, byte... args) {
        byte[] posCommand = new byte[command.length + args.length];

        System.arraycopy(command, 0, posCommand, 0, command.length);
        System.arraycopy(args, 0, posCommand, command.length, args.length);

        return posCommand;
    }

    private static BitSet getBitsImageData(Bitmap image) {
        int threshold = 127;
        int index = 0;
        int dimenssions = image.getWidth() * image.getHeight();
        BitSet imageBitsData = new BitSet(dimenssions);

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int color = image.getPixel(x, y);
                int red = (color & 0x00ff0000) >> 16;
                int green = (color & 0x0000ff00) >> 8;
                int blue = color & 0x000000ff;
                int luminance = (int) (red * 0.3 + green * 0.59 + blue * 0.11);
                //dots[index] = (luminance < threshold);
                imageBitsData.set(index, (luminance < threshold));
                index++;
            }
        }

        return imageBitsData;
    }

    public static void printImage(Bitmap image) {
        BitSet imageBits = getBitsImageData(image);

        byte widthLSB = (byte) (image.getWidth() & 0xFF);
        byte widthMSB = (byte) ((image.getWidth() >> 8) & 0xFF);
        byte[] selectBitImageModeCommand = buildPOSCommand(SELECT_BIT_IMAGE_MODE, (byte) 33, widthLSB, widthMSB);

        int resultJni = PrinterInterface.PrinterOpen();
        if (resultJni < 0) {
//            Log.e("App", "don't  open twice this devices");
            PrinterInterface.PrinterClose();
            return;
        }
        PrinterInterface.PrinterBegin();
        printCommands(INITIALIZE_PRINTER);
        printCommands(LINE_SPACING_24DOTS);
        printCommands(CENTER_ALIGN);
        int offset = 0;
        while (offset < image.getHeight()) {
            printCommands(selectBitImageModeCommand);

            int imageDataLineIndex = 0;
            byte[] imageDataLine = new byte[3 * image.getWidth()];

            for (int x = 0; x < image.getWidth(); ++x) {
                for (int k = 0; k < 3; ++k) {
                    byte slice = 0;
                    for (int b = 0; b < 8; ++b) {
                        int y = (((offset / 8) + k) * 8) + b;
                        int i = (y * image.getWidth()) + x;
                        boolean v = false;
                        if (i < imageBits.length()) {
                            v = imageBits.get(i);
                        }
                        slice |= (byte) ((v ? 1 : 0) << (7 - b));
                    }

                    imageDataLine[imageDataLineIndex + k] = slice;
                }

                imageDataLineIndex += 3;
            }

            printCommands(imageDataLine);
            offset += 24;
            printCommands(PRINT_AND_FEED_PAPER);
        }


        printCommands(LINE_SPACING_30DOTS);
        PrinterInterface.PrinterEnd();
        PrinterInterface.PrinterClose();
    }

    public static void printInlineImage(Bitmap image) {
        printCommands(LINE_SPACING_24DOTS);
        printCommands(CENTER_ALIGN);

        PrinterBitmapUtil.printBitmap(image, 0, 0, true);

        printCommands(LINE_SPACING_30DOTS);
    }

    public static void printStruk(Bitmap image, List<PrintSize> data) {
        BitSet imageBits = getBitsImageData(image);

        byte widthLSB = (byte) (image.getWidth() & 0xFF);
        byte widthMSB = (byte) ((image.getWidth() >> 8) & 0xFF);

        // COMMANDS
        byte[] selectBitImageModeCommand = buildPOSCommand(SELECT_BIT_IMAGE_MODE, (byte) 33, widthLSB, widthMSB);

        int resultJni = PrinterInterface.PrinterOpen();
        if (resultJni < 0) {
//            Log.e("App", "don't  open twice this devices");
            PrinterInterface.PrinterClose();
            return;
        }
        PrinterInterface.PrinterBegin();
        printCommands(INITIALIZE_PRINTER);
//        printCommands(LINE_SPACING_24DOTS);
//        printCommands(CENTER_ALIGN);
//        int offset = 0;
//        while (offset < image.getHeight()) {
//            printCommands(selectBitImageModeCommand);
//
//            int imageDataLineIndex = 0;
//            byte[] imageDataLine = new byte[3 * image.getWidth()];
//
//            for (int x = 0; x < image.getWidth(); ++x) {
//                for (int k = 0; k < 3; ++k) {
//                    byte slice = 0;
//                    for (int b = 0; b < 8; ++b) {
//                        int y = (((offset / 8) + k) * 8) + b;
//                        int i = (y * image.getWidth()) + x;
//                        boolean v = false;
//                        if (i < imageBits.length()) {
//                            v = imageBits.get(i);
//                        }
//                        slice |= (byte) ((v ? 1 : 0) << (7 - b));
//                    }
//
//                    imageDataLine[imageDataLineIndex + k] = slice;
//                }
//
//                imageDataLineIndex += 3;
//            }
//
//            printCommands(imageDataLine);
//            offset += 24;
//            printCommands(PRINT_AND_FEED_PAPER);
//        }

//        printCommands(LINE_SPACING_30DOTS);

        printInlineImage(image);

        printCommands("\n");
        printCommandsSmall("MERCHANT LOREM IPSUM\n");
        printCommandsSmall("JL. ANTAH BERANTAH\n");
        printCommandsSmall("NO 34A\n");
        printCommandsSmall("KOTA BERANTAH\n");
        printCommands("\n");
        printCommands(LEFT_ALIGN);
        String sameLine = addSpaceBetween("TID : 123456", "MID : 123456789", false);
        printCommandsSmall(sameLine + "\n");
        sameLine = addSpaceBetween("BATCH : 000000", "TRACE NO : 002102", false);
        printCommandsSmall(sameLine + "\n");
        Date d = new Date();
        sameLine = addSpaceBetween("TANGGAL : " + printDate(d), "JAM : " + printTime(d), false);
        printCommandsSmall(sameLine + "\n");
        printCommands("\n");
        for (PrintSize pz : data) {
//            Log.d("zzzz", pz.getMessage());
            if (pz.getMessage().equals("START_FOOTER")) {
                printCommands(CENTER_ALIGN);
            } else if (pz.getMessage().equals("START FOOTER")) {
                printCommands(CENTER_ALIGN);
            } else if (pz.getMessage().equals("STOP_FOOTER")) {
                printCommands(LEFT_ALIGN);
            } else if (pz.getMessage().equals("STOP FOOTER")) {
                printCommands(LEFT_ALIGN);
            }
            if (pz.getMessage().equals("Info Kuota Bansos")) {
            } else {
                printCommands(pz);
            }
        }
        printCommands("\n");
        printCommands(CENTER_ALIGN);
        printCommandsSmall("----CUSTOMER COPY----\n");
        printCommands("\n");
        printCommands("\n");
        printCommands("\n");
        printCommands("\n");
        printCommands("\n");
        PrinterInterface.PrinterEnd();
        PrinterInterface.PrinterClose();
    }

    public static void printStruk(Bitmap image, List<PrintSize> data, List<String> mdata,
                                  String tid, String mid, String stan, int pcopy, String svrRef,
                                  String svrDate, String svrTime, String cardType, String cardNumber,
                                  String screenLoader, String batchNumber, String svrAppr, String storeName,
                                  String val1, String val2, String prosel, Context context) {
        PrinterDevice printerDevice = (PrinterDevice) POSTerminal.getInstance(context).getDevice(
                "cloudpos.device.printer");
        String str = "";
        try {

            Log.i("PRINT STRUK", "The printer is being opened, please later...");
            printerDevice.open();

            Log.i("PRINT STRUK", "The printer device has been successfully opened");
            Format format = new Format();
            try {
                if (printerDevice.queryStatus() == printerDevice.STATUS_OUT_OF_PAPER) {
                    Log.i("PRINT STRUK", "The printer is short of paper.");

//                    Toast.makeText(context, "The printer is short of paper", Toast.LENGTH_SHORT).show();
                } else if (printerDevice.queryStatus() == printerDevice.STATUS_PAPER_EXIST) {

                    Log.i("PRINT STRUK", "The printer is in a normal state and starts to print...");
                    Thread thread = new Thread(() -> {
                        // TODO Auto-generated method stub
                        try {

                            format.setParameter("align", "center");
                            format.setParameter("bold", "false");
                            format.setParameter("size", "small");
                            if (Build.SERIAL.contains("WP18041")) {
                                format.setParameter("bold", "true");//format.setParameter("size", "medium");
                            }

                            try {
                                Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_bjb);
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
                                printerDevice.printBitmap(format, bitmap);
                                printerDevice.printText("\n");

                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                            //ADD STORE NAME HEADER
                            if (!storeName.equals("AGEN")) {
                                printerDevice.printlnText(format, storeName);
                            }

                            for (String md : mdata) {
                                printerDevice.printlnText(format, md);
                            }

                            if (pcopy > 2) {
                                printerDevice.printText("\n");
                                printerDevice.printlnText(format, "**********DUPLICATE**********");
                            }

                            format.clear();
                            format.setParameter("align", "left");
                            format.setParameter("size", "small");
                            if (Build.SERIAL.contains("WP18041")) {
                                format.setParameter("bold", "true");//format.setParameter("size", "medium");
                            }

                            printerDevice.printText("\n");

                            String sameLine = addSpaceBetween("TERMINAL ID: ", tid, false);
                            printerDevice.printlnText(format, sameLine);

                            sameLine = addSpaceBetween("MERCHANT ID: ", mid, false);
                            printerDevice.printlnText(format, sameLine);
                            printerDevice.printText("\n");
                            Boolean isBjbCard = true;
                            if (!cardNumber.equals("")) {
                                printerDevice.printText(format, cardType);
                                if (!cardType.contains("FLY")) {
                                    format.clear();
                                    format.setParameter("align", "left");
                                    format.setParameter("size", "small");
                                    if (Build.SERIAL.contains("WP18041")) {
                                        format.setParameter("bold", "true");//format.setParameter("size", "medium");
                                    }
                                    format.setParameter("bold", "true");

                                    printerDevice.printText(format, cardNumber);

                                    format.clear();
                                    format.setParameter("align", "left");
                                    format.setParameter("size", "small");
                                    if (Build.SERIAL.contains("WP18041")) {
                                        format.setParameter("bold", "true");//format.setParameter("size", "medium");
                                    }
                                }
                                printerDevice.printText("\n");
                            }
                            Date d = new Date();
                            String strDate = printDate(d);
                            String strTime = printTime(d);
                            if (svrDate.length() == 4) {
                                strDate = grabDate(d, svrDate);
                            } else if (svrDate.length() == 10) {
                                strDate = grabSQLDate(d, svrDate);
                            }
                            if (svrTime.length() == 6) {
                                strTime = grabTime(d, svrTime);
                            } else if (svrTime.length() == 8) {
                                strTime = svrTime;
                            }
                            printerDevice.printlnText(format, strDate + ", " + strTime);
                            sameLine = addSpaceBetween("BATCH  : " + batchNumber, "TRACE NO : " + stan, false);
//                            sameLine = addSpaceBetween("BATCH: ", batchNumber, false);
//                            printerDevice.printlnText(format, sameLine);
//                            sameLine = addSpaceBetween("TRACE NO: ", stan, false);
                            printerDevice.printlnText(format, sameLine);

                            if (!svrRef.equals("000000000000")) {
                                sameLine = addSpaceBetween("REF NO : " + svrRef, "APPR : " + svrAppr, false);
//                                sameLine = addSpaceBetween("REF NO: ", svrRef, false);
//                                printerDevice.printlnText(format, sameLine);
//                                sameLine = addSpaceBetween("APPR: ", svrAppr, false);
                                printerDevice.printlnText(format, sameLine);
                            }

                            boolean footerPrintingIsOn = false;
                            for (PrintSize pz : data) {
                                if (!footerPrintingIsOn){
//                                    format.clear();
                                    format.setParameter("align", "left");
                                    format.setParameter("size", "small");
                                    if (Build.SERIAL.contains("WP18041")) {
                                        format.setParameter("bold", "true");//format.setParameter("size", "medium");
                                    }
                                    else{
                                        format.setParameter("bold", "false");
                                    }
                                }
                                else{
                                    format.setParameter("size", "small");
                                    if (Build.SERIAL.contains("WP18041")) {
                                        format.setParameter("bold", "true");//format.setParameter("size", "medium");
                                    }
                                    else {
                                        format.setParameter("bold", "false");
                                    }
                                }

                                if (pz.getMessage().equals("START_FOOTER")) {
                                    format.setParameter("align", "center");
                                    footerPrintingIsOn = true;
                                } else if (pz.getMessage().equals("START FOOTER")) {
                                    format.setParameter("align", "center");
                                    footerPrintingIsOn = true;
                                } else if (pz.getMessage().equals("STOP_FOOTER")) {
                                    format.setParameter("align", "left");
                                    footerPrintingIsOn = false;
                                } else if (pz.getMessage().equals("STOP FOOTER")) {
                                    format.setParameter("align", "left");
                                    footerPrintingIsOn = false;
                                }

                                else if (pz.getMessage().contains("INFO KUOTA BERHASIL")) {

                                    format.setParameter("bold", "true");
                                    format.setParameter("align", "center");
//                                    printerDevice.printText(format, pz.getMessage());
                                    if (Build.SERIAL.contains("WP18041")) {
                                        printerDevice.printText(format, pz.getMessage().toUpperCase());
                                    }
                                    else{
                                        printerDevice.printText(format, pz.getMessage());
                                    }
                                } else if (pz.getMessage().contains("Transaksi Berhasil")) {
                                    String content = pz.getMessage().substring(0, pz.getMessage().indexOf("Transaksi Berhasil"));

                                    format.setParameter("bold", "true");
                                    format.setParameter("align", "center");
                                    if (Build.SERIAL.contains("WP18041")) {
                                        printerDevice.printText(format, pz.getMessage().toUpperCase());
                                    }
                                    else{
                                        printerDevice.printText(format, pz.getMessage());
                                    }
                                } else if (pz.getMessage().contains("TRANSAKSI BERHASIL")) {

                                    format.setParameter("bold", "true");
                                    format.setParameter("align", "center");
                                    if (Build.SERIAL.contains("WP18041")) {
                                        printerDevice.printText(format, pz.getMessage().toUpperCase());
                                    }
                                    else{
                                        printerDevice.printText(format, pz.getMessage());
                                    }
                                }
                                else if (pz.getMessage().contains("PROSES AWAL PEMBUKAAN REKENING BSA")) {
                                    format.setParameter("bold", "true");
                                    format.setParameter("align", "left");
                                    if (Build.SERIAL.contains("WP18041")) {
                                        printerDevice.printText(format, pz.getMessage().toUpperCase());
                                    }
                                    else{
                                        printerDevice.printText(format, pz.getMessage());
                                    }
                                } else if (pz.getMessage().contains("Pembayaran PKB/SWDKLLJ/BBNKB/PNBP")) {

                                    format.setParameter("bold", "true");
                                    format.setParameter("align", "left");
                                    if (Build.SERIAL.contains("WP18041")) {
                                        printerDevice.printText(format, pz.getMessage().toUpperCase());
                                    }
                                    else{
                                        printerDevice.printText(format, pz.getMessage());
                                    }
                                }
                                //set mini banking cust copy wihtout fee
                                else if (screenLoader.equals("MA0023F") || screenLoader.equals("MA0002F")
                                        || screenLoader.equals("MA0041F") || screenLoader.equals("MA0043F")
                                        || screenLoader.equals("MA0010F") || screenLoader.equals("MA0012F")
                                        || screenLoader.equals("MA0031F") || screenLoader.equals("MA0033F")
                                        || screenLoader.equals("POC002R") || screenLoader.equals("MA0050F")
                                        || screenLoader.equals("MA0051F")
                                ) {
                                    if (prosel.contains("182") || prosel.contains("181")) {
                                        Boolean isFlag;
                                        if (pz.getMessage().contains("Biaya Admin") || pz.getMessage().contains("Val1")) {
                                            if (pcopy == 0) {
                                                //skip
                                            } else {
                                                if (pz.getMessage().contains("Val1")) {

                                                    format.setParameter("align", "left");
                                                    if (Build.SERIAL.contains("WP18041")) {
                                                        if (val1.equals(" ")) {
                                                            printerDevice.printlnText(format, val1.toUpperCase());
                                                        }
                                                    } else {
                                                        printerDevice.printlnText(format, val1);
                                                    }
                                                } else {
                                                    switch (pz.getFontSize()){
                                                        case TITLE:
                                                            format.setParameter("bold", "true");
                                                            format.setParameter("size", "medium");
                                                            break;
                                                        default:
                                                            break;
                                                    }
                                                    if (Build.SERIAL.contains("WP18041")) {
                                                        if (!pz.getMessage().equals(" ")) {
                                                            printerDevice.printText(format, pz.getMessage().toUpperCase());
                                                        }
                                                    } else {
                                                        printerDevice.printText(format, pz.getMessage());
                                                    }
                                                }
                                            }
                                        } else if (pz.getMessage().contains("Total") || pz.getMessage().contains("Val2")) {
                                            if (pcopy == 0) {
                                                //skip
                                            } else {
                                                if (pz.getMessage().contains("Val2")) {
                                                    format.setParameter("align", "left");
//                                                    printerDevice.printlnText(format, val2);
                                                    if (Build.SERIAL.contains("WP18041")) {
                                                        if (!pz.getMessage().equals(" ")) {
                                                            printerDevice.printlnText(format, val2.toUpperCase());
                                                        }
                                                    } else {
                                                        printerDevice.printlnText(format, val2);
                                                    }
                                                } else {
                                                    switch (pz.getFontSize()){
                                                        case TITLE:
                                                            format.setParameter("bold", "true");
                                                            format.setParameter("size", "medium");
                                                            break;
                                                        default:
                                                            break;
                                                    }
                                                    if (Build.SERIAL.contains("WP18041")) {
                                                        if (!pz.getMessage().equals(" ")) {
                                                            printerDevice.printText(format, pz.getMessage().toUpperCase());
                                                        }
                                                    } else {
                                                        printerDevice.printText(format, pz.getMessage());
                                                    }
                                                }
                                            }
                                        } else {
                                            switch (pz.getFontSize()){
                                                case TITLE:
                                                    format.setParameter("bold", "true");
                                                    format.setParameter("size", "medium");
                                                    break;
                                                default:
                                                    break;
                                            }
                                            if (Build.SERIAL.contains("WP18041")) {
                                                if (!pz.getMessage().equals(" ")) {
                                                    printerDevice.printText(format, pz.getMessage().toUpperCase());
                                                }
                                            } else {
                                                printerDevice.printText(format, pz.getMessage());
                                            }
                                        }
                                    } else {
                                        switch (pz.getFontSize()){
                                            case TITLE:
                                                format.setParameter("bold", "true");
                                                format.setParameter("size", "medium");
                                                break;
                                            default:
                                                break;
                                        }
                                        if (Build.SERIAL.contains("WP18041")) {
                                            if (!pz.getMessage().equals(" ")) {
                                                printerDevice.printText(format, pz.getMessage().toUpperCase());
                                            }
                                        } else {
                                            printerDevice.printText(format, pz.getMessage());
                                        }
                                    }
                                } else {
//                                    pz.getFontSize()
                                    switch (pz.getFontSize()) {
                                        case TITLE:
                                            format.setParameter("bold", "true");
                                            format.setParameter("size", "medium");
                                            break;
                                        default:
                                            break;
                                    }
                                    if (Build.SERIAL.contains("WP18041")) {
                                        if (!pz.getMessage().equals(" ")) {
                                            printerDevice.printText(format, pz.getMessage().toUpperCase());
                                        }
                                    } else {
                                        printerDevice.printText(format, pz.getMessage());
                                    }
                                }
                            }
                            format.setParameter("align", "center");

                            String copyTypeText = "--DUPLICATE COPY--\n";
                            if (pcopy == 0) {
                                copyTypeText = "--CUSTOMER COPY--\n";
                                if (screenLoader.equals("71000FF") || screenLoader.equals("721000F") || screenLoader.equals("731000F")) {
                                    copyTypeText = "--AGENT COPY--\n";
                                }
                            } else if (pcopy == 1) {
                                copyTypeText = "--BANK COPY--\n";
                            } else if (pcopy == 2) {
                                copyTypeText = "--MERCHANT COPY--\n";
                                if (screenLoader.equals("71000FF") || screenLoader.equals("721000F") || screenLoader.equals("731000F")) {
                                    copyTypeText = "--CUSTOMER COPY--\n";
                                }
                            } else {
                                copyTypeText = "--DUPLICATE COPY--\n";
                            }
                            printerDevice.printlnText(format, copyTypeText);
                            printerDevice.printText("\n");
                            printerDevice.printText("\n");
                            printerDevice.printText("\n");
                            printerDevice.printText("\n");

//                            printerDevice.printText(format, getPrintLabelValue("Total", "Rp " + MethodUtil.toCurrencyFormat(Long.toString(amount)), false, true));
//                            printerDevice.printlnText(format, "Supported by bank bjb");

                            try {
                                printerDevice.close();
                                Log.i("PRINT STRUK", "The printing device is closed successfully.");
                            } catch (DeviceException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                                Log.i("PRINT STRUK", "The printing device closed failed.");
                            } catch (Exception e){
                                e.printStackTrace();
                            }

                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    });
                    thread.start();
                } else {
//                    Toast.makeText(context, "The printer is short of paper", Toast.LENGTH_SHORT).show();
                }
            } catch (DeviceException de) {
                Log.i("PRINT STRUK", "Check the failure of the printer state.");
                de.printStackTrace();
            }
        } catch (DeviceException de) {
            de.printStackTrace();
            Log.i("PRINT STRUK", "The printer device failed to open.");

        }
    }

    public static void printSettlement(Bitmap image, List<PrintSize> data, List<String> mdata,
                                       String tid, String mid, String stan, String svrDate, String svrTime, Context context) {
        PrinterDevice printerDevice = (PrinterDevice) POSTerminal.getInstance(context).getDevice(
                "cloudpos.device.printer");
        String str = "";
        try {

            Log.i("PRINT STRUK", "The printer is being opened, please later...");
            printerDevice.open();

            Log.i("PRINT STRUK", "The printer device has been successfully opened");
            Format format = new Format();
            try {
                if (printerDevice.queryStatus() == printerDevice.STATUS_OUT_OF_PAPER) {
                    Log.i("PRINT STRUK", "The printer is short of paper.");

//                    Toast.makeText(context, "The printer is short of paper", Toast.LENGTH_SHORT).show();
                } else if (printerDevice.queryStatus() == printerDevice.STATUS_PAPER_EXIST) {

                    Log.i("PRINT STRUK", "The printer is in a normal state and starts to print...");
                    Thread thread = new Thread(() -> {
                        // TODO Auto-generated method stub
                        try {
                            format.setParameter("align", "center");
                            format.setParameter("bold", "false");
                            format.setParameter("size", "small");
                            if (Build.SERIAL.contains("WP18041")) {
                                format.setParameter("bold", "true");//format.setParameter("size", "medium");
                            }

                            try {
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                image.compress(Bitmap.CompressFormat.PNG, 90, stream);
                                printerDevice.printBitmap(format, image);
                                printerDevice.printText("\n");

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            for (String md : mdata) {
                                printerDevice.printlnText(format, md);
                            }

                            format.clear();
                            format.setParameter("align", "left");
                            format.setParameter("size", "small");
                            if (Build.SERIAL.contains("WP18041")) {
                                format.setParameter("bold", "true");//format.setParameter("size", "medium");
                            }

                            printerDevice.printText("\n");

                            String sameLine = addSpaceBetween("TERMINAL ID: ", tid, false);
                            printerDevice.printlnText(format, sameLine);

                            sameLine = addSpaceBetween("MERCHANT ID: ", mid, false);
                            printerDevice.printlnText(format, sameLine);
                            printerDevice.printText("\n");

                            Date d = new Date();
                            String strDate = printDate(d);
                            String strTime = printTime(d);
                            if (svrDate.length() == 4) {
                                strDate = grabDate(d, svrDate);
                            } else if (svrDate.length() == 10) {
                                strDate = grabSQLDate(d, svrDate);
                            }
                            if (svrTime.length() == 6) {
                                strTime = grabTime(d, svrTime);
                            } else if (svrTime.length() == 8) {
                                strTime = svrTime;
                            }
                            printerDevice.printlnText(format, "DATE/TIME    : " + strDate + ", " + strTime);

                            boolean footerPrintingIsOn = false;
                            for (PrintSize pz : data) {
                                if (!footerPrintingIsOn){
//                                    format.clear();
                                    format.setParameter("align", "left");
                                    format.setParameter("size", "small");
                                    if (Build.SERIAL.contains("WP18041")) {
                                        format.setParameter("bold", "true");//format.setParameter("size", "medium");
                                    }
                                    else{
                                        format.setParameter("bold", "false");
                                    }
                                }
                                else{
                                    format.setParameter("size", "small");
                                    if (Build.SERIAL.contains("WP18041")) {
                                        format.setParameter("bold", "true");//format.setParameter("size", "medium");
                                    }
                                    else {
                                        format.setParameter("bold", "false");
                                    }
                                }

                                if (pz.getMessage().equals("START_FOOTER")) {
                                    format.setParameter("align", "center");
                                    footerPrintingIsOn = true;
                                } else if (pz.getMessage().equals("START FOOTER")) {
                                    format.setParameter("align", "center");
                                    footerPrintingIsOn = true;
                                } else if (pz.getMessage().equals("STOP_FOOTER")) {
                                    format.setParameter("align", "left");
                                    footerPrintingIsOn = false;
                                } else if (pz.getMessage().equals("STOP FOOTER")) {
                                    format.setParameter("align", "left");
                                    footerPrintingIsOn = false;
                                } else {
                                    switch (pz.getFontSize()){
                                        case TITLE:
                                            format.setParameter("bold", "true");
                                            format.setParameter("size", "medium");
                                            break;
                                        default:
                                            break;
                                    }
                                    if (Build.SERIAL.contains("WP18041")) {
                                        if (!pz.getMessage().equals(" ")) {
                                            printerDevice.printText(format, pz.getMessage().toUpperCase());
                                        }
                                    } else {
                                        printerDevice.printText(format, pz.getMessage());
                                    }
                                }
                            }
                            format.setParameter("align", "center");

                            printerDevice.printText("\n");
                            printerDevice.printText("\n");
                            printerDevice.printText("\n");
                            printerDevice.printText("\n");

                            try {
                                printerDevice.close();
                                Log.i("PRINT STRUK", "The printing device is closed successfully.");
                            } catch (DeviceException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                                Log.i("PRINT STRUK", "The printing device closed failed.");
                            } catch (Exception e){
                                e.printStackTrace();
                            }

                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    });
                    thread.start();
                } else {
//                    Toast.makeText(context, "The printer is short of paper", Toast.LENGTH_SHORT).show();
                }
            } catch (DeviceException de) {
                Log.i("PRINT STRUK", "Check the failure of the printer state.");
                de.printStackTrace();
            }
        } catch (DeviceException de) {
            de.printStackTrace();
            Log.i("PRINT STRUK", "The printer device failed to open.");

        }

    }

    public static void printReport(Bitmap image, List<PrintSize> data, List<String> mdata, String tid, String mid, String stan, String storeName, Context context) {
        PrinterDevice printerDevice = (PrinterDevice) POSTerminal.getInstance(context).getDevice(
                "cloudpos.device.printer");
        String str = "";
        try {

            Log.i("PRINT STRUK", "The printer is being opened, please later...");
            printerDevice.open();

            Log.i("PRINT STRUK", "The printer device has been successfully opened");
            Format format = new Format();
            try {
                if (printerDevice.queryStatus() == printerDevice.STATUS_OUT_OF_PAPER) {
                    Log.i("PRINT STRUK", "The printer is short of paper.");

//                    Toast.makeText(context, "The printer is short of paper", Toast.LENGTH_SHORT).show();
                } else if (printerDevice.queryStatus() == printerDevice.STATUS_PAPER_EXIST) {

                    Log.i("PRINT STRUK", "The printer is in a normal state and starts to print...");
                    Thread thread = new Thread(() -> {
                        // TODO Auto-generated method stub
                        try {
                            format.setParameter("align", "center");
                            format.setParameter("bold", "false");
                            format.setParameter("size", "small");
                            if (Build.SERIAL.contains("WP18041")) {
                                format.setParameter("bold", "true");//format.setParameter("size", "medium");
                            }

                            try {
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                image.compress(Bitmap.CompressFormat.PNG, 90, stream);
                                printerDevice.printBitmap(format, image);
                                printerDevice.printText("\n");

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            //ADD STORE NAME HEADER
                            if (!storeName.equals("AGEN")) {
                                printerDevice.printlnText(format, storeName);
                            }

                            for (String md : mdata) {
                                printerDevice.printlnText(format, md);
                            }

                            format.clear();
                            format.setParameter("align", "left");
                            format.setParameter("size", "small");
                            if (Build.SERIAL.contains("WP18041")) {
                                format.setParameter("bold", "true");//format.setParameter("size", "medium");
                            }

                            printerDevice.printText("\n");

                            String sameLine = addSpaceBetween("TERMINAL ID: ", tid, false);
                            printerDevice.printlnText(format, sameLine);

                            sameLine = addSpaceBetween("MERCHANT ID: ", mid, false);
                            printerDevice.printlnText(format, sameLine);
                            printerDevice.printText("\n");

                            Date d = new Date();
                            printerDevice.printlnText(format, "PRINT DATE/TIME : " + printDate(d) + " " + printTime(d));
                            if (!stan.equals("")) {
                                printerDevice.printlnText(format, "REPORT DATE     : " + stan);
                            }
                            printCommands("\n");

                            boolean footerPrintingIsOn = false;
                            for (PrintSize pz : data) {
                                if (!footerPrintingIsOn){
//                                    format.clear();
                                    format.setParameter("align", "left");
                                    format.setParameter("size", "small");
                                    if (Build.SERIAL.contains("WP18041")) {
                                        format.setParameter("bold", "true");//format.setParameter("size", "medium");
                                    }
                                    else{
                                        format.setParameter("bold", "false");
                                    }
                                }
                                else{
                                    format.setParameter("size", "small");
                                    if (Build.SERIAL.contains("WP18041")) {
                                        format.setParameter("bold", "true");//format.setParameter("size", "medium");
                                    }
                                    else {
                                        format.setParameter("bold", "false");
                                    }
                                }

                                if (pz.getMessage().equals("START_FOOTER")) {
                                    format.setParameter("align", "center");
                                    footerPrintingIsOn = true;
                                } else if (pz.getMessage().equals("START FOOTER")) {
                                    format.setParameter("align", "center");
                                    footerPrintingIsOn = true;
                                } else if (pz.getMessage().equals("STOP_FOOTER")) {
                                    format.setParameter("align", "left");
                                    footerPrintingIsOn = false;
                                } else if (pz.getMessage().equals("STOP FOOTER")) {
                                    format.setParameter("align", "left");
                                    footerPrintingIsOn = false;
                                } else {
                                    switch (pz.getFontSize()){
                                        case TITLE:
                                            format.setParameter("bold", "true");
                                            format.setParameter("size", "medium");
                                            break;
                                        default:
                                            break;
                                    }
                                    if (pz.getMessage().contains(":")) {
                                        if (Build.SERIAL.contains("WP18041")) {
                                            printerDevice.printText(format, setHalfSide(pz.getMessage(), false, false).toUpperCase());
                                        }
                                        else{
                                            printerDevice.printText(format, setHalfSide(pz.getMessage(), false, false));
                                        }
                                    } else if (pz.getMessage().contains("Rp")) {
                                        if (Build.SERIAL.contains("WP18041")) {
                                            printerDevice.printText(format, setHalfSide(pz.getMessage(), false, true).toUpperCase());
                                        }
                                        else{
                                            printerDevice.printText(format, setHalfSide(pz.getMessage(), false, true));
                                        }
                                    }
                                    else{
                                        if (Build.SERIAL.contains("WP18041")) {
                                            if (!pz.getMessage().equals(" ")) {
                                                printerDevice.printText(format, pz.getMessage().toUpperCase());
                                            }
                                        } else {
                                            printerDevice.printText(format, pz.getMessage());
                                        }
                                    }
                                }
                            }
                            format.setParameter("align", "center");

                            printerDevice.printText("\n");
                            printerDevice.printText("\n");
                            printerDevice.printText("\n");
                            printerDevice.printText("\n");

                            try {
                                printerDevice.close();
                                Log.i("PRINT STRUK", "The printing device is closed successfully.");
                            } catch (DeviceException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                                Log.i("PRINT STRUK", "The printing device closed failed.");
                            } catch (Exception e){
                                e.printStackTrace();
                            }

                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    });
                    thread.start();
                } else {
//                    Toast.makeText(context, "The printer is short of paper", Toast.LENGTH_SHORT).show();
                }
            } catch (DeviceException de) {
                Log.i("PRINT STRUK", "Check the failure of the printer state.");
                de.printStackTrace();
            }
        } catch (DeviceException de) {
            de.printStackTrace();
            Log.i("PRINT STRUK", "The printer device failed to open.");

        }
    }

    public static void printDetailReport(Bitmap image, List<PrintSize> data, List<String> mdata, String tid, String mid, String stan, String storeName, Context context) {
        PrinterDevice printerDevice = (PrinterDevice) POSTerminal.getInstance(context).getDevice(
                "cloudpos.device.printer");
        String str = "";
        try {

            Log.i("PRINT STRUK", "The printer is being opened, please later...");
            printerDevice.open();

            Log.i("PRINT STRUK", "The printer device has been successfully opened");
            Format format = new Format();
            try {
                if (printerDevice.queryStatus() == printerDevice.STATUS_OUT_OF_PAPER) {
                    Log.i("PRINT STRUK", "The printer is short of paper.");

//                    Toast.makeText(context, "The printer is short of paper", Toast.LENGTH_SHORT).show();
                } else if (printerDevice.queryStatus() == printerDevice.STATUS_PAPER_EXIST) {

                    Log.i("PRINT STRUK", "The printer is in a normal state and starts to print...");
                    Thread thread = new Thread(() -> {
                        // TODO Auto-generated method stub
                        try {
                            format.setParameter("align", "center");
                            format.setParameter("bold", "false");
                            format.setParameter("size", "small");
                            if (Build.SERIAL.contains("WP18041")) {
                                format.setParameter("bold", "true");//format.setParameter("size", "medium");
                            }

                            try {
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                image.compress(Bitmap.CompressFormat.PNG, 90, stream);
                                printerDevice.printBitmap(format, image);
                                printerDevice.printText("\n");

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            //ADD STORE NAME HEADER
                            if (!storeName.equals("AGEN")) {
                                printerDevice.printlnText(format, storeName);
                            }

                            for (String md : mdata) {
                                printerDevice.printlnText(format, md);
                            }

                            format.clear();
                            format.setParameter("align", "left");
                            format.setParameter("size", "small");
                            if (Build.SERIAL.contains("WP18041")) {
                                format.setParameter("bold", "true");//format.setParameter("size", "medium");
                            }

                            printerDevice.printText("\n");

                            String sameLine = addSpaceBetween("TERMINAL ID: ", tid, false);
                            printerDevice.printlnText(format, sameLine);

                            sameLine = addSpaceBetween("MERCHANT ID: ", mid, false);
                            printerDevice.printlnText(format, sameLine);
                            printerDevice.printText("\n");

                            Date d = new Date();

                            printerDevice.printlnText(format, "PRINT DATE/TIME : " + printDate(d) + " " + printTime(d));
                            if (!stan.equals("")) {
                                printerDevice.printlnText(format, "REPORT DATE     : " + stan);

                            }
                            printCommands("\n");

                            boolean footerPrintingIsOn = false;
                            for (PrintSize pz : data) {
                                if (!footerPrintingIsOn){
//                                    format.clear();
                                    format.setParameter("align", "left");
                                    format.setParameter("size", "small");
                                    if (Build.SERIAL.contains("WP18041")) {
                                        format.setParameter("bold", "true");//format.setParameter("size", "medium");
                                    }
                                    else{
                                        format.setParameter("bold", "false");
                                    }
                                }
                                else{
                                    format.setParameter("size", "small");
                                    if (Build.SERIAL.contains("WP18041")) {
                                        format.setParameter("bold", "true");//format.setParameter("size", "medium");
                                    }
                                    else{
                                        format.setParameter("bold", "false");
                                    }
                                }

                                if (pz.getMessage().equals("START_FOOTER")) {
                                    format.setParameter("align", "center");
                                    footerPrintingIsOn = true;
                                } else if (pz.getMessage().equals("START FOOTER")) {
                                    format.setParameter("align", "center");
                                    footerPrintingIsOn = true;
                                } else if (pz.getMessage().equals("STOP_FOOTER")) {
                                    format.setParameter("align", "left");
                                    footerPrintingIsOn = false;
                                } else if (pz.getMessage().equals("STOP FOOTER")) {
                                    format.setParameter("align", "left");
                                    footerPrintingIsOn = false;
                                } else {
                                    switch (pz.getFontSize()){
                                        case TITLE:
                                            format.setParameter("bold", "true");
                                            format.setParameter("size", "medium");
                                            break;
                                        default:
                                            break;
                                    }
                                    if (pz.getMessage().contains("|:")) {
//                                        printerDevice.printText(format, setHalfDetailSide(pz.getMessage(), false, false));
                                        if (Build.SERIAL.contains("WP18041")) {
                                            printerDevice.printText(format, setHalfDetailSide(pz.getMessage(), false, false).toUpperCase());
                                        }
                                        else{
                                            printerDevice.printText(format, setHalfDetailSide(pz.getMessage(), false, false));
                                        }
                                    } else if (pz.getMessage().contains(":|")) {
//                                        printerDevice.printText(format, setHalfDetailSide(pz.getMessage(), false, true));
                                        if (Build.SERIAL.contains("WP18041")) {
                                            printerDevice.printText(format, setHalfDetailSide(pz.getMessage(), false, true).toUpperCase());
                                        }
                                        else{
                                            printerDevice.printText(format, setHalfDetailSide(pz.getMessage(), false, true));
                                        }
                                    }
                                    else{
                                        if (Build.SERIAL.contains("WP18041")) {
                                            if (!pz.getMessage().equals(" ")) {
                                                printerDevice.printText(format, pz.getMessage().toUpperCase());
                                            }
                                        } else {
                                            printerDevice.printText(format, pz.getMessage());
                                        }
                                    }
                                }
                            }
                            format.setParameter("align", "center");

                            printerDevice.printText("\n");
                            printerDevice.printText("\n");
                            printerDevice.printText("\n");
                            printerDevice.printText("\n");

                            try {
                                printerDevice.close();
                                Log.i("PRINT STRUK", "The printing device is closed successfully.");
                            } catch (DeviceException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                                Log.i("PRINT STRUK", "The printing device closed failed.");
                            } catch (Exception e){
                                e.printStackTrace();
                            }

                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    });
                    thread.start();
                } else {
//                    Toast.makeText(context, "The printer is short of paper", Toast.LENGTH_SHORT).show();
                }
            } catch (DeviceException de) {
                Log.i("PRINT STRUK", "Check the failure of the printer state.");
                de.printStackTrace();
            }
        } catch (DeviceException de) {
            de.printStackTrace();
            Log.i("PRINT STRUK", "The printer device failed to open.");

        }
    }

    private static String printDate(Date d) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        return sdf.format(d);
    }

    private static String printTime(Date d) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(d);
    }

    private static String grabSQLDate(Date d, String svrDate) {
        SimpleDateFormat idf = new SimpleDateFormat("yyyyMMdd");
        Date sv = new Date();
        try {
            sv = idf.parse(svrDate);
        } catch (Exception e) {
            return printDate(d);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        return sdf.format(sv);
    }

    private static String grabDate(Date d, String svrDate) {
        SimpleDateFormat ydf = new SimpleDateFormat("yyyy");
        String year = ydf.format(d) + svrDate;
        SimpleDateFormat idf = new SimpleDateFormat("yyyyMMdd");
        Date sv = new Date();
        try {
            sv = idf.parse(year);
        } catch (Exception e) {
            return printDate(d);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        return sdf.format(sv);
    }

    private static String grabTime(Date d, String svrTime) {
        SimpleDateFormat idf = new SimpleDateFormat("HHmmss");
        Date sv = new Date();
        try {
            sv = idf.parse(svrTime);
        } catch (Exception e) {
            return printDate(d);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(sv);
    }

    private static String printFullDate(Date d) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
        return sdf.format(d);
    }

    private static String addSpaceBetween(String txt1, String txt2, boolean normal) {
        String space = " ";
//        int lineSize = 32;
//        if(!normal){
//            lineSize = 42;
//        }
        int lineSize = 32;
        if (!normal) {
            lineSize = 42;
        }
        int totalLen = txt1.length() + txt2.length();
        for (int i = 0; i < lineSize; i++) {
            totalLen = txt1.length() + txt2.length() + space.length();
            if (totalLen == lineSize)
                break;
            space += " ";
        }
        return txt1 + space + txt2;
    }

    private static String setHalfSide(String txt, boolean normal, boolean right) {
        String space = " ";
        int lineSize = 32;
        if (!normal) {
            lineSize = 42;
        }
        int totalLen = lineSize / 2;
        String cnt = "";
        if (right) {
            totalLen = 10;
            txt = txt.substring(3);
        } else {
            totalLen = 23;
            cnt = "   " + txt.substring(txt.indexOf(":") + 1);
            cnt = cnt.substring(cnt.length() - 3);
            txt = txt.substring(0, txt.indexOf(":") - 1);
        }
        for (int i = 0; i < totalLen; i++) {
            if (right) {
                txt = space + txt;
            } else {
                txt = txt + space;
            }
        }
        if (right) {
            txt = txt.substring(txt.length() - totalLen);
        } else {
            txt = txt.substring(0, totalLen) + cnt + "  Rp";
        }
        return txt;
    }

    private static String setHalfDetailSide(String txt, boolean normal, boolean right) {
        String space = " ";
        int lineSize = 32;
        if (!normal) {
            lineSize = 42;
        }
        int totalLen = lineSize / 2;
        if (right) {
            txt = txt.substring(2);
        } else {
            txt = txt.substring(0, txt.indexOf("|:"));
        }
        for (int i = 0; i < totalLen; i++) {
            if (right) {
                txt = space + txt;
            } else {
                txt = txt + space;
            }
        }
        if (right) {
            txt = txt.substring(txt.length() - totalLen);
        } else {
            txt = txt.substring(0, totalLen);
        }
        return txt;
    }

    private static void printCommands(byte[] data) {
        PrinterInterface.PrinterWrite(data, data.length);
    }

    private static void printCommands(String data) {
        byte[] cmds = new byte[]{0x1B, 0x21, 0x00};
        printCommands(cmds);
        data = "  " + data;
        printCommands(data.getBytes());
    }

    private static void printBold_2Commands(String data) {
        printCommands(new PrintSize(FontSize.BOLD_2, data));
    }

    private static void printBoldCommands(String data) {
        printCommands(new PrintSize(FontSize.BOLD, data));
    }

    private static void printCommands(PrintSize pz) {
        printCommands(pz.getFontSize().getByte());
        printCommands(pz.getMessage().getBytes());
    }

    private static void printCommandsSmall(String data) {
        byte[] cmds = new byte[]{0x1B, 0x21, 0x01};
        printCommands(cmds);
        data = " " + data;
        printCommands(data.getBytes());
    }
}