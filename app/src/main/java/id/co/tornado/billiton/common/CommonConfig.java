/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.tornado.billiton.common;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import id.co.tornado.billiton.R;

/**
 * @author indra
 */
public class CommonConfig {

    public static final String HTTP_SSL_PROTOCOL = "https";
    public static final String HTTP_NON_SSL_PROTOCOL = "http";
    public static final String HTTP_PROTOCOL = HTTP_NON_SSL_PROTOCOL;

    public static final String WS_SSL_PROTOCOL = "wss";
    private static final String WS_NON_SSL_PROTOCOL = "ws";
    public static final String WS_PROTOCOL = WS_NON_SSL_PROTOCOL;

    public static final int WS_SSL_PORT = 443;
//    public static final int WS_SSL_PORT = 80;
    private static final int WS_NORMAL_PORT = 80;
    public static final int WS_NON_SSL_PORT = WS_NORMAL_PORT;

    public static final String intDeSelKey = "H4sIAAAAAAAAADM207M00TO10DO0MAQAWnddcwwAAAA=";
    public static final String intProSelKey = "H4sIAAAAAAAAADM207M00TO10DO0MLKyMLAwAACERKhvEQAAAA==";
    public static final String intProBjKey = "H4sIAAAAAAAAAEtNSdZLSszLTspK0kvO18tMAQBgXEBjEQAAAA==";

    private static final String usKey = "H4sIAAAAAAAAAEs0NDI2MQUAXbC9fwYAAAA=";
    private static final String psKey = "H4sIAAAAAAAAALO0hAAAqpcHuggAAAA=";
    private static final String httpKey = "H4sIAAAAAAAAANN3DApKLS4BAJuCLF4HAAAA";
    private static final String wSocketKey = "H4sIAAAAAAAAANMvyS0GAJSVspMEAAAA";
    private static final String htPostKey = "H4sIAAAAAAAAANN3DApKLS7RTyzIBAB/30O2CwAAAA==";
    private static final String postPathKey = "H4sIAAAAAAAAAEssyAQAD9gFrQMAAAA=";

    public static final String chKey_1 = "H4sIAAAAAAAAADM0MTcytQAAI+Bl6gYAAAA=";
    public static final String chKey_2 = "H4sIAAAAAAAAALOwsLAAAB+/PgEEAAAA";
    public static final String chKey_3 = "H4sIAAAAAAAAADMytbA0MwYAwc5YuQYAAAA=";
    public static final String chKey_4 = "H4sIAAAAAAAAADMzs7QEAPHkY4UEAAAA";

//    public static final String PROSELIP = getVal(intProBjKey);
    public static final String PROSELIP = getVal(intProSelKey);
//    public static final String PROSELIP = getVal(intDeSelKey);

    public static final String[] FORM_MENU_KEY = {"type", "title", "id", "print", "print_text", "ver", "comps"};
    public static final String[] FORM_MENU_COMP_KEY = {"visible", "comp_type", "comp_id", "comp_opt", "comp_act", "seq", "comp_values"};
    public static final String VER_FILE = "menu_ver";
    public static final String SETTINGS_FILE = "settings";
    public static final String DB_FILE_UPDATE_NAME = "dbupdate.sql";
    public static final String BRANCH = "303514";

    public static final String USERNAME_ADMIN = getVal(usKey);
    public static final String PASS_ADMIN = getVal(psKey);

    public static final String DEV_SOCKET_IP = "-";
    public static final String DEV_SOCKET_PORT = "-";
    public static final String DEFAULT_DISCOUNT_TYPE = "Rupiah";
    public static final String EXPD = "2111";

    public static final String IS_SETTING = "isSetting";

    //profile selada
//    public static final String DEV_TERMINAL_ID = "14130002";
//    public static final String DEV_MERCHANT_ID = "000000000000";
//    public static final String INIT_MERCHANT_NAME = "AGEN BJB BISA";
//    public static final String INIT_MERCHANT_ADDRESS1 = "KANTOR PUSAT";
//    public static final String INIT_MERCHANT_ADDRESS2 = "BANDUNG";
//    public static final String INIT_REST_ACT = "S000025";

    //profile tnt
    public static final String DEV_TERMINAL_ID = "13030087";
    public static final String DEV_MERCHANT_ID = "00010025000001";
    public static final String INIT_MERCHANT_NAME = "Samsat Sumber";
    public static final String INIT_MERCHANT_ADDRESS1 = "Samsat";
    public static final String INIT_MERCHANT_ADDRESS2 = "BANDUNG";
    public static final String INIT_REST_ACT = "S000000";

    public static final String INIT_SIM_NUMBER = "000000";
    public static final String STORE_NAME = "AGEN";

    public static final String CODE_HOLDER_WIFI = "000000";

    public static final String CVA = "12600000";
    public static final String DEFAULT_SETTLEMENT_PASS = "1234";
    public static final String DEFAULT_MIN_BALANCE_BRIZZI = "2500";
    public static final String DEFAULT_MAX_MONTHLY_DEDUCT = "20000000";
    public static final Boolean DEBUG_MODE = false;
    public static final String SQUEN = "8977";
    public static final int CAPTURE_PINBLOCK = 52;
    public static final int CALLBACK_KEYPRESSED = 53;
    public static final int CALLBACK_RESULT = 54;
    public static final int CAPTURE_CANCEL = 55;
    public static final int FLAG_INUSE = 56;
    public static final int FLAG_READY = 57;
    public static final String VTB = "22300000";
    public static final int UPDATE_FLAG_RECEIVER = 58;
    public static final int MODE_CALCULATE = 59;
    public static final int CALLBACK_CANCEL = 60;
    public static final int CALLBACK_CANCEL_DONE = 61;
    public static final String ONE_BIN = "522184";
    public static final String IP = PROSELIP;
    public static final String HTTP_REST_URL = IP+getVal(httpKey);
    public static final String WEBSOCKET_URL = IP+getVal(wSocketKey);
    public static final String HTTP_POST = HTTP_PROTOCOL+ "://"+IP+getVal(htPostKey);
    public static final String POST_PATH = getVal(postPathKey);

    public static String getVal(String key){
        try {
            return decompress(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String setVal(String key){
        try {
            Log.d(key, compress(key));
            return compress(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static Object[] getOpt(String compOpt) {
        Object[] result = new Object[5];
        Boolean bool = compOpt.substring(1, 2).equals("1");
//        Log.d("COMP", bool.toString());
        result[CompOption.MANDATORY] = compOpt.substring(0, 1).equals("1");
        result[CompOption.DISABLED] = compOpt.substring(1, 2).equals("1");
        result[CompOption.TYPE] = Integer.parseInt(compOpt.substring(2, 3));
        result[CompOption.MIN_LENGTH] = Integer.parseInt(compOpt.substring(3, 6));
        result[CompOption.MAX_LENGTH] = Integer.parseInt(compOpt.substring(6, 9));
        return result;
    }

    public static Map<String, Integer> ICONS() {
        Map<String, Integer> icons = new HashMap<>();
        icons.put("Mini ATM", R.drawable.info_atm);
        icons.put("Informasi", R.drawable.cek_saldo);
        icons.put("Transfer", R.drawable.transfer_sesama);
        icons.put("Pemindahbukuan", R.drawable.transfer_sesama);
        icons.put("Transfer bank lain", R.drawable.transfer_antarbank);
        icons.put("Info Kode Bank", R.drawable.info_petunjuk);
        icons.put("Pembayaran", R.drawable.mb_isi_ulang_brizzi);
        icons.put("Pembayaran eSamsat Banten", R.drawable.icon_esamsat);
        icons.put("SAMSAT", R.drawable.mb_info_dplk);
        icons.put("Pembayaran eSamsat via Transfer", R.drawable.mb_info_dplk);
        icons.put("Logon", R.drawable.logon);
        icons.put("Settings", R.drawable.mb_info_mutasi);
        icons.put("Info Saldo", R.drawable.cek_saldo);
        icons.put("View Log",R.drawable.mb_info_poin);
        icons.put("Cetak Ulang Transaksi Terakhir",R.drawable.reprint);
        icons.put("Pembayaran eSamsat Debit", R.drawable.mb_isi_ulang_brizzi);
        icons.put("Reprint", R.drawable.print);
        icons.put("Mutasi Rekening", R.drawable.cek_mutasi);
        icons.put("Mutasi", R.drawable.cek_mutasi);
        icons.put("Tarik tunai", R.drawable.tarik_tunai);
        icons.put("Void", R.drawable.void_tarik);
        icons.put("Sale", R.drawable.sale);
        icons.put("Settlement", R.drawable.settelment);
        return icons;
    }

    public static final String CER = "\n" +
            ".954  AC: CN=COMODO ECC Certification Authority,O=COMODO CA Limited,L=Salford,ST=Greater Manchester,C=GB\n" +
            ".964  AC: CN=QuoVadis Root CA 2,O=QuoVadis Limited,C=BM\n" +
            ".964  AC: CN=D-TRUST Root Class 3 CA 2 2009,O=D-Trust GmbH,C=DE\n" +
            ".974  AC: OU=ApplicationCA,O=Japanese Government,C=JP\n" +
            ".974  AC: CN=SwissSign Platinum CA - G2,O=SwissSign AG,C=CH\n" +
            ".984  AC: CN=Swisscom Root CA 1,OU=Digital Certificate Services,O=Swisscom,C=ch\n" +
            ".984  AC: CN=Global Chambersign Root,OU=http://www.chambersign.org,O=AC Camerfirma SA CIF A82743287,C=EU\n" +
            ".994  AC: CN=Buypass Class 2 Root CA,O=Buypass AS-983163327,C=NO\n" +
            ".994  AC: CN=DigiCert High Assurance EV Root CA,OU=www.digicert.com,O=DigiCert Inc,C=US\n" +
            ".004  AC: CN=AffirmTrust Premium ECC,O=AffirmTrust,C=US\n" +
            ".004  AC: CN=AffirmTrust Premium,O=AffirmTrust,C=US\n" +
            ".014  AC: CN=KISA RootCA 1,OU=Korea Certification Authority Central,O=KISA,C=KR\n" +
            ".014  AC: CN=Microsec e-Szigno Root CA,OU=e-Szigno CA,O=Microsec Ltd.,L=Budapest,C=HU\n" +
            ".024  AC: CN=StartCom Certification Authority,OU=Secure Digital Certificate Signing,O=StartCom Ltd.,C=IL\n" +
            ".024  AC: CN=GeoTrust Primary Certification Authority,O=GeoTrust Inc.,C=US\n" +
            ".034  AC: C=ES,O=ACCV,OU=PKIACCV,CN=ACCVRAIZ1\n" +
            ".044  AC: CN=T-TeleSec GlobalRoot Class 3,OU=T-Systems Trust Center,O=T-Systems Enterprise Services GmbH,C=DE\n" +
            ".044  AC: CN=GlobalSign,O=GlobalSign,OU=GlobalSign Root CA - R3\n" +
            ".054  AC: CN=e-Guven Kok Elektronik Sertifika Hizmet Saglayicisi,O=Elektronik Bilgi Guvenligi A.S.,C=TR\n" +
            ".054  AC: 1.2.840.113549.1.9.1=#16176361406669726d6170726f666573696f6e616c2e636f6d,CN=Autoridad de Certificacion Firmaprofesional CIF A62634068,L=C/ Muntaner 244 Barcelona,C=ES\n" +
            ".064  AC: OU=DSTCA E1,O=Digital Signature Trust Co.,C=US\n" +
            ".064  AC: OU=Security Communication EV RootCA1,O=SECOM Trust Systems CO.\\,LTD.,C=JP\n" +
            ".074  AC: CN=OISTE WISeKey Global Root GA CA,OU=OISTE Foundation Endorsed,OU=Copyright (c) 2005,O=WISeKey,C=CH\n" +
            ".074  UC: CN=UTN-USERFirst-Hardware,OU=http://www.usertrust.com,O=The USERTRUST Network,L=Salt Lake City,ST=UT,C=US\n" +
            ".074  AC: CN=UTN-USERFirst-Hardware,OU=http://www.usertrust.com,O=The USERTRUST Network,L=Salt Lake City,ST=UT,C=US\n" +
            ".084  AC: CN=Class 2 Primary CA,O=Certplus,C=FR\n" +
            ".094  AC: CN=VeriSign Class 3 Public Primary Certification Authority - G3,OU=(c) 1999 VeriSign\\, Inc. - For authorized use only,OU=VeriSign Trust Network,O=VeriSign\\, Inc.,C=US\n" +
            ".104  AC: CN=NetLock Arany (Class Gold) Főtanúsítvány,OU=Tanúsítványkiadók (Certification Services),O=NetLock Kft.,L=Budapest,C=HU\n" +
            ".114  AC: 1.2.840.113549.1.9.1=#16177365727665722d6365727473407468617774652e636f6d,CN=Thawte Server CA,OU=Certification Services Division,O=Thawte Consulting cc,L=Cape Town,ST=Western Cape,C=ZA\n" +
            ".114  AC: CN=Deutsche Telekom Root CA 2,OU=T-TeleSec Trust Center,O=Deutsche Telekom AG,C=DE\n" +
            ".124  AC: CN=GeoTrust Primary Certification Authority - G2,OU=(c) 2007 GeoTrust Inc. - For authorized use only,O=GeoTrust Inc.,C=US\n" +
            ".134  AC: CN=AC Raíz Certicámara S.A.,O=Sociedad Cameral de Certificación Digital - Certicámara S.A.,C=CO\n" +
            ".134  AC: CN=SecureSign RootCA1,O=Japan Certification Services\\, Inc.,C=JP\n" +
            ".144  AC: CN=thawte Primary Root CA - G2,OU=(c) 2007 thawte\\, Inc. - For authorized use only,O=thawte\\, Inc.,C=US\n" +
            ".144  AC: CN=GlobalSign,O=GlobalSign,OU=GlobalSign Root CA - R2\n" +
            ".154  AC: CN=GeoTrust Primary Certification Authority - G3,OU=(c) 2008 GeoTrust Inc. - For authorized use only,O=GeoTrust Inc.,C=US\n" +
            ".154  AC: 1.2.840.113549.1.9.1=#1611696e666f4076616c69636572742e636f6d,CN=http://www.valicert.com/,OU=ValiCert Class 3 Policy Validation Authority,O=ValiCert\\, Inc.,L=ValiCert Validation Network\n" +
            ".164  AC: CN=DST Root CA X3,O=Digital Signature Trust Co.\n" +
            ".164  AC: CN=GeoTrust Global CA 2,O=GeoTrust Inc.,C=US\n" +
            ".174  AC: CN=AAA Certificate Services,O=Comodo CA Limited,L=Salford,ST=Greater Manchester,C=GB\n" +
            ".184  AC: O=(c) 2005 TÜRKTRUST Bilgi İletişim ve Bilişim Güvenliği Hizmetleri A.Ş.,L=ANKARA,C=TR,CN=TÜRKTRUST Elektronik Sertifika Hizmet Sağlayıcısı\n" +
            ".214  AC: CN=NetLock Expressz (Class C) Tanusitvanykiado,OU=Tanusitvanykiadok,O=NetLock Halozatbiztonsagi Kft.,L=Budapest,C=HU\n" +
            ".214  AC: OU=Equifax Secure Certificate Authority,O=Equifax,C=US\n" +
            ".214  AC: OU=DSTCA E2,O=Digital Signature Trust Co.,C=US\n" +
            ".224  AC: CN=VeriSign Class 3 Public Primary Certification Authority - G4,OU=(c) 2007 VeriSign\\, Inc. - For authorized use only,OU=VeriSign Trust Network,O=VeriSign\\, Inc.,C=US\n" +
            ".234  AC: CN=Microsoft Root Authority,OU=Microsoft Corporation,OU=Copyright (c) 1997 Microsoft Corp.\n" +
            ".234  AC: CN=Entrust.net Secure Server Certification Authority,OU=(c) 1999 Entrust.net Limited,OU=www.entrust.net/CPS incorp. by ref. (limits liab.),O=Entrust.net,C=US\n" +
            ".244  AC: CN=D-TRUST Root Class 3 CA 2 EV 2009,O=D-Trust GmbH,C=DE\n" +
            ".244  AC: CN=Hellenic Academic and Research Institutions RootCA 2011,O=Hellenic Academic and Research Institutions Cert. Authority,C=GR\n" +
            ".254  AC: CN=Chambers of Commerce Root - 2008,O=AC Camerfirma S.A.,2.5.4.5=#1309413832373433323837,L=Madrid (see current address at www.camerfirma.com/address),C=EU\n" +
            ".254  AC: C=ES,O=EDICOM,OU=PKI,CN=ACEDICOM Root\n" +
            ".264  AC: CN=VeriSign Class 3 Public Primary Certification Authority - G5,OU=(c) 2006 VeriSign\\, Inc. - For authorized use only,OU=VeriSign Trust Network,O=VeriSign\\, Inc.,C=US\n" +
            ".264  AC: CN=Certum Trusted Network CA,OU=Certum Certification Authority,O=Unizeto Technologies S.A.,C=PL\n" +
            ".274  AC: CN=Swisscom Root CA 2,OU=Digital Certificate Services,O=Swisscom,C=ch\n" +
            ".274  AC: CN=Buypass Class 3 CA 1,O=Buypass AS-983163327,C=NO\n" +
            ".284  AC: CN=TWCA Root Certification Authority,OU=Root CA,O=TAIWAN-CA,C=TW\n" +
            ".294  AC: 1.2.840.113549.1.9.1=#1612676c6f62616c30314069707363612e636f6d,CN=ipsCA Global CA Root,OU=ipsCA,O=IPS Certification Authority s.l. ipsCA,L=Madrid,ST=Madrid,C=ES\n" +
            ".294  AC: CN=SwissSign Gold CA - G2,O=SwissSign AG,C=CH\n" +
            ".304  AC: OU=Go Daddy Class 2 Certification Authority,O=The Go Daddy Group\\, Inc.,C=US\n" +
            ".314  AC: CN=VeriSign Universal Root Certification Authority,OU=(c) 2008 VeriSign\\, Inc. - For authorized use only,OU=VeriSign Trust Network,O=VeriSign\\, Inc.,C=US\n" +
            ".324  AC: CN=America Online Root Certification Authority 2,O=America Online Inc.,C=US\n" +
            ".324  AC: OU=TDC Internet Root CA,O=TDC Internet,C=DK\n" +
            ".334  AC: CN=DST ACES CA X6,OU=DST ACES,O=Digital Signature Trust,C=US\n" +
            ".334  AC: 1.2.840.113549.1.9.1=#1611696e666f4076616c69636572742e636f6d,CN=http://www.valicert.com/,OU=ValiCert Class 2 Policy Validation Authority,O=ValiCert\\, Inc.,L=ValiCert Validation Network\n" +
            ".344  AC: OU=Security Communication RootCA1,O=SECOM Trust.net,C=JP\n" +
            ".344  AC: CN=Equifax Secure eBusiness CA-1,O=Equifax Secure Inc.,C=US\n" +
            ".354  AC: CN=Entrust Root Certification Authority,OU=(c) 2006 Entrust\\, Inc.,OU=www.entrust.net/CPS is incorporated by reference,O=Entrust\\, Inc.,C=US\n" +
            ".354  AC: CN=Juur-SK,O=AS Sertifitseerimiskeskus,C=EE,1.2.840.113549.1.9.1=#1609706b6940736b2e6565\n" +
            ".364  AC: CN=GeoTrust Universal CA,O=GeoTrust Inc.,C=US\n" +
            ".364  AC: CN=SecureTrust CA,O=SecureTrust Corporation,C=US\n" +
            ".364  AC: CN=Hongkong Post Root CA 1,O=Hongkong Post,C=HK\n" +
            ".374  AC: CN=TÜBİTAK UEKAE Kök Sertifika Hizmet Sağlayıcısı - Sürüm 3,OU=Kamu Sertifikasyon Merkezi,OU=Ulusal Elektronik ve Kriptoloji Araştırma Enstitüsü - UEKAE,O=Türkiye Bilimsel ve Teknolojik Araştırma Kurumu - TÜBİTAK,L=Gebze - Kocaeli,C=TR\n" +
            ".374  AC: CN=StartCom Certification Authority G2,O=StartCom Ltd.,C=IL\n" +
            ".384  AC: CN=COMODO Certification Authority,O=COMODO CA Limited,L=Salford,ST=Greater Manchester,C=GB\n" +
            ".384  AC: OU=Equifax Secure eBusiness CA-2,O=Equifax Secure,C=US\n" +
            ".394  AC: CN=Wells Fargo Root Certificate Authority,OU=Wells Fargo Certification Authority,O=Wells Fargo,C=US\n" +
            ".394  AC: CN=Entrust Root Certification Authority - EC1,OU=(c) 2012 Entrust\\, Inc. - for authorized use only,OU=See www.entrust.net/legal-terms,O=Entrust\\, Inc.,C=US\n" +
            ".404  AC: OU=VeriSign Trust Network,OU=(c) 1998 VeriSign\\, Inc. - For authorized use only,OU=Class 3 Public Primary Certification Authority - G2,O=VeriSign\\, Inc.,C=US\n" +
            ".414  AC: 1.2.840.113549.1.9.1=#16197072656d69756d2d736572766572407468617774652e636f6d,CN=Thawte Premium Server CA,OU=Certification Services Division,O=Thawte Consulting cc,L=Cape Town,ST=Western Cape,C=ZA\n" +
            ".414  AC: CN=TC TrustCenter Class 3 CA II,OU=TC TrustCenter Class 3 CA,O=TC TrustCenter GmbH,C=DE\n" +
            ".424  AC: CN=QuoVadis Root Certification Authority,OU=Root Certification Authority,O=QuoVadis Limited,C=BM\n" +
            ".424  AC: CN=AddTrust External CA Root,OU=AddTrust External TTP Network,O=AddTrust AB,C=SE\n" +
            ".434  AC: CN=Certinomis - Autorité Racine,OU=0002 433998903,O=Certinomis,C=FR\n" +
            ".434  AC: OU=FNMT Clase 2 CA,O=FNMT,C=ES\n" +
            ".434  AC: CN=GlobalSign Root CA,OU=Root CA,O=GlobalSign nv-sa,C=BE\n" +
            ".444  AC: CN=TC TrustCenter Universal CA I,OU=TC TrustCenter Universal CA,O=TC TrustCenter GmbH,C=DE\n" +
            ".444  AC: CN=GeoTrust Universal CA 2,O=GeoTrust Inc.,C=US\n" +
            ".454  AC: CN=TC TrustCenter Class 2 CA II,OU=TC TrustCenter Class 2 CA,O=TC TrustCenter GmbH,C=DE\n" +
            ".454  AC: OU=RSA Security 2048 V3,O=RSA Security Inc\n" +
            ".454  AC: CN=Equifax Secure Global eBusiness CA-1,O=Equifax Secure Inc.,C=US\n" +
            ".464  AC: CN=Sonera Class2 CA,O=Sonera,C=FI\n" +
            ".464  AC: CN=Buypass Class 3 Root CA,O=Buypass AS-983163327,C=NO\n" +
            ".464  AC: 1.2.840.113549.1.9.1=#1610696e666f40652d737a69676e6f2e6875,CN=Microsec e-Szigno Root CA 2009,O=Microsec Ltd.,L=Budapest,C=HU\n" +
            ".474  AC: CN=Root CA Generalitat Valenciana,OU=PKIGVA,O=Generalitat Valenciana,C=ES\n" +
            ".474  AC: CN=thawte Primary Root CA,OU=(c) 2006 thawte\\, Inc. - For authorized use only,OU=Certification Services Division,O=thawte\\, Inc.,C=US\n" +
            ".484  AC: CN=Entrust.net Certification Authority (2048),OU=(c) 1999 Entrust.net Limited,OU=www.entrust.net/CPS_2048 incorp. by ref. (limits liab.),O=Entrust.net\n" +
            ".514  AC: OU=Starfield Class 2 Certification Authority,O=Starfield Technologies\\, Inc.,C=US\n" +
            ".514  AC: CN=WellsSecure Public Root Certificate Authority,OU=Wells Fargo Bank NA,O=Wells Fargo WellsSecure,C=US\n" +
            ".524  AC: CN=QuoVadis Root CA 3,O=QuoVadis Limited,C=BM\n" +
            ".524  AC: CN=TDC OCES CA,O=TDC,C=DK\n" +
            ".534  AC: OU=Class 3 Public Primary Certification Authority,O=VeriSign\\, Inc.,C=US\n" +
            ".534  AC: CN=thawte Primary Root CA - G3,OU=(c) 2008 thawte\\, Inc. - For authorized use only,OU=Certification Services Division,O=thawte\\, Inc.,C=US\n" +
            ".544  AC: CN=America Online Root Certification Authority 1,O=America Online Inc.,C=US\n" +
            ".544  AC: CN=GTE CyberTrust Global Root,OU=GTE CyberTrust Solutions\\, Inc.,O=GTE Corporation,C=US\n" +
            ".554  AC: CN=DigiCert Assured ID Root CA,OU=www.digicert.com,O=DigiCert Inc,C=US\n" +
            ".554  AC: CN=Buypass Class 2 CA 1,O=Buypass AS-983163327,C=NO\n" +
            ".554  AC: CN=Starfield Root Certificate Authority - G2,O=Starfield Technologies\\, Inc.,L=Scottsdale,ST=Arizona,C=US\n" +
            ".564  AC: CN=SwissSign Silver CA - G2,O=SwissSign AG,C=CH\n" +
            ".564  AC: CN=CNNIC ROOT,O=CNNIC,C=CN\n" +
            ".574  AC: 1.2.840.113549.1.9.1=#161469676361407367646e2e706d2e676f75762e6672,CN=IGC/A,OU=DCSSI,O=PM/SGDN,L=Paris,ST=France,C=FR\n" +
            ".574  AC: CN=Autoridad de Certificacion Firmaprofesional CIF A62634068,C=ES\n" +
            ".574  AC: CN=AffirmTrust Networking,O=AffirmTrust,C=US\n" +
            ".584  AC: O=Government Root Certification Authority,C=TW\n" +
            ".584  AC: OU=Security Communication RootCA2,O=SECOM Trust Systems CO.\\,LTD.,C=JP\n" +
            ".594  AC: CN=Staat der Nederlanden Root CA,O=Staat der Nederlanden,C=NL\n" +
            ".604  AC: CN=EC-ACC,OU=Jerarquia Entitats de Certificacio Catalanes,OU=Vegeu https://www.catcert.net/verarrel (c)03,OU=Serveis Publics de Certificacio,O=Agencia Catalana de Certificacio (NIF Q-0801176-I),C=ES\n" +
            ".614  AC: CN=Global Chambersign Root - 2008,O=AC Camerfirma S.A.,2.5.4.5=#1309413832373433323837,L=Madrid (see current address at www.camerfirma.com/address),C=EU\n" +
            ".624  AC: CN=KISA RootCA 3,OU=Korea Certification Authority Central,O=KISA,C=KR\n" +
            ".624  AC: CN=GeoTrust Global CA,O=GeoTrust Inc.,C=US\n" +
            ".634  AC: CN=VeriSign Class 4 Public Primary Certification Authority - G3,OU=(c) 1999 VeriSign\\, Inc. - For authorized use only,OU=VeriSign Trust Network,O=VeriSign\\, Inc.,C=US\n" +
            ".634  AC: CN=XRamp Global Certification Authority,O=XRamp Security Services Inc,OU=www.xrampsecurity.com,C=US\n" +
            ".644  UC: CN=UTN - DATACorp SGC,OU=http://www.usertrust.com,O=The USERTRUST Network,L=Salt Lake City,ST=UT,C=US\n" +
            ".644  AC: CN=UTN - DATACorp SGC,OU=http://www.usertrust.com,O=The USERTRUST Network,L=Salt Lake City,ST=UT,C=US\n" +
            ".644  AC: CN=Certigna,O=Dhimyotis,C=FR\n" +
            ".654  AC: CN=Starfield Services Root Certificate Authority - G2,O=Starfield Technologies\\, Inc.,L=Scottsdale,ST=Arizona,C=US\n" +
            ".654  AC: CN=Secure Global CA,O=SecureTrust Corporation,C=US\n" +
            ".654  AC: CN=TC TrustCenter Universal CA III,OU=TC TrustCenter Universal CA,O=TC TrustCenter GmbH,C=DE\n" +
            ".664  AC: CN=AffirmTrust Commercial,O=AffirmTrust,C=US\n" +
            ".664  AC: CN=Izenpe.com,O=IZENPE S.A.,C=ES\n" +
            ".674  AC: CN=SecureSign RootCA11,O=Japan Certification Services\\, Inc.,C=JP\n" +
            ".674  AC: C=TR,O=EBG Bilişim Teknolojileri ve Hizmetleri A.Ş.,CN=EBG Elektronik Sertifika Hizmet Sağlayıcısı\n" +
            ".674  AC: CN=Visa eCommerce Root,OU=Visa International Service Association,O=VISA,C=US\n" +
            ".684  AC: 1.2.840.113549.1.9.1=#1611696e666f4076616c69636572742e636f6d,CN=http://www.valicert.com/,OU=ValiCert Class 1 Policy Validation Authority,O=ValiCert\\, Inc.,L=ValiCert Validation Network\n" +
            ".684  AC: CN=Chambers of Commerce Root,OU=http://www.chambersign.org,O=AC Camerfirma SA CIF A82743287,C=EU\n" +
            ".694  AC: CN=NetLock Uzleti (Class B) Tanusitvanykiado,OU=Tanusitvanykiadok,O=NetLock Halozatbiztonsagi Kft.,L=Budapest,C=HU\n" +
            ".694  AC: OU=ePKI Root Certification Authority,O=Chunghwa Telecom Co.\\, Ltd.,C=TW\n" +
            ".704  AC: CN=Network Solutions Certificate Authority,O=Network Solutions L.L.C.,C=US\n" +
            ".704  AC: OU=VeriSign Trust Network,OU=(c) 1998 VeriSign\\, Inc. - For authorized use only,OU=Class 4 Public Primary Certification Authority - G2,O=VeriSign\\, Inc.,C=US\n" +
            ".714  UC: CN=UTN-USERFirst-Network Applications,OU=http://www.usertrust.com,O=The USERTRUST Network,L=Salt Lake City,ST=UT,C=US\n" +
            ".714  AC: CN=UTN-USERFirst-Network Applications,OU=http://www.usertrust.com,O=The USERTRUST Network,L=Salt Lake City,ST=UT,C=US\n" +
            ".714  AC: CN=Baltimore CyberTrust Root,OU=CyberTrust,O=Baltimore,C=IE\n" +
            ".724  AC: CN=NetLock Kozjegyzoi (Class A) Tanusitvanykiado,OU=Tanusitvanykiadok,O=NetLock Halozatbiztonsagi Kft.,L=Budapest,ST=Hungary,C=HU\n" +
            ".724  AC: CN=Certum CA,O=Unizeto Sp. z o.o.,C=PL\n" +
            ".724  AC: CN=Entrust Root Certification Authority - G2,OU=(c) 2009 Entrust\\, Inc. - for authorized use only,OU=See www.entrust.net/legal-terms,O=Entrust\\, Inc.,C=US\n" +
            ".734  AC: CN=CA Disig,O=Disig a.s.,L=Bratislava,C=SK\n" +
            ".734  AC: O=TÜRKTRUST Bilgi İletişim ve Bilişim Güvenliği Hizmetleri A.Ş. (c) Kasım 2005,L=Ankara,C=TR,CN=TÜRKTRUST Elektronik Sertifika Hizmet Sağlayıcısı\n" +
            ".744  AC: CN=Go Daddy Root Certificate Authority - G2,O=GoDaddy.com\\, Inc.,L=Scottsdale,ST=Arizona,C=US\n" +
            ".744  AC: CN=DigiCert Global Root CA,OU=www.digicert.com,O=DigiCert Inc,C=US\n" +
            ".744  AC: C=IL,O=ComSign,CN=ComSign Secured CA\n" +
            ".754  AC: CN=Cybertrust Global Root,O=Cybertrust\\, Inc\n" +
            ".754  AC: OU=certSIGN ROOT CA,O=certSIGN,C=RO\n" +
            ".754  SC: CN=A-Trust-nQual-03,OU=A-Trust-nQual-03,O=A-Trust Ges. f. Sicherheitssysteme im elektr. Datenverkehr GmbH,C=AT\n" +
            ".754  AC: CN=A-Trust-nQual-03,OU=A-Trust-nQual-03,O=A-Trust Ges. f. Sicherheitssysteme im elektr. Datenverkehr GmbH,C=AT\n" +
            ".764  AC: CN=Staat der Nederlanden Root CA - G2,O=Staat der Nederlanden,C=NL";

    public static int getIcon(String name) {
        return ICONS().containsKey(name) ? ICONS().get(name) : -1;
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    /**
     * Get Imei Device
     *
     * @param ctx
     * @return
     */
    public static String getImei(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

    /**
     * Get current location of device
     *
     * @param ctx Android Context
     * @return Array double of long and lat. Index 0 is longtitude and index 1 is latitude
     */
    public static double[] getLocation(Context ctx) {
        LocationManager lm = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double[] d = new double[2];
        d[0] = location.getLongitude();
        d[1] = location.getLatitude();
        return d;
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public static class ComponentType {
        //Component Type
        public static final int ListMenuItem = 0;
        public static final int TextView = 1;
        public static final int EditText = 2;
        public static final int PasswordField = 3;
        public static final int ComboBox = 4;
        public static final int CheckBox = 5;
        public static final int RadioButton = 6;
        public static final int Button = 7;
        public static final int MagneticSwipe = 8;
        public static final int ChipInsert = 9;
        public static final int TapCard = 10;
        public static final int SwipeInsert = 11;
        public static final int SwipeTap = 12;
        public static final int InsertTap = 13;
        public static final int SwipeInsertTap = 14;
    }

    public static class MenuType {
        //Menu Type
        public static final int ListMenu = 0;
        public static final int Form = 1;
        public static final int PopupBerhasil = 2;
        public static final int PopupGagal = 3;
        public static final int PopupLogout = 4;
        public static final int SecuredForm = 5;
    }

    public static class CompOption {
        public static final int MANDATORY = 0;
        public static final int DISABLED = 1;
        public static final int TYPE = 2;
        public static final int MIN_LENGTH = 3;
        public static final int MAX_LENGTH = 4;
    }

    public static Map<String, Integer> KNOWN_TAG_MAP() {
        Map<String, Integer> tagmap = new HashMap<>();
        //from FCI
        tagmap.put("6F", 0);
        tagmap.put("84", 1);
        tagmap.put("A5", 0);
        tagmap.put("50", 1);
        tagmap.put("87", 1);
        tagmap.put("9F38", 1);
        tagmap.put("5F2D", 1);
        tagmap.put("9F11", 1);
        tagmap.put("9F12", 1);
        tagmap.put("BF0C", 0);
        tagmap.put("9F4D", 1);
        //from FIS 010C
        tagmap.put("70", 0);
        tagmap.put("57", 1);
        tagmap.put("5F20", 1);
        tagmap.put("9F1F", 1);
        //from FIS 0114
//        tagmap.put("70", 0);
        tagmap.put("8F", 1);
        tagmap.put("92", 1);
        tagmap.put("9F32", 1);
        tagmap.put("9F4A", 1);
        //from FIS 0214
//        tagmap.put("70", 0);
        tagmap.put("90", 1);
        //from FIS 0314
//        tagmap.put("70", 0);
        tagmap.put("9F46", 1);
        tagmap.put("9F47", 1);
        tagmap.put("9F49", 1);
        //from FIS 011C
//        tagmap.put("70", 0);
        tagmap.put("5A", 1);
        tagmap.put("5F24", 1);
        tagmap.put("9F0D", 1);
        tagmap.put("9F0E", 1);
        tagmap.put("9F0F", 1);
        tagmap.put("5F25", 1);
        tagmap.put("5F34", 1);
        tagmap.put("9F07", 1);
        //from FIS 021C
//        tagmap.put("70", 0);
        tagmap.put("93", 1);
        //from FIS 0124
//        tagmap.put("70", 0);
        tagmap.put("9F08", 1);
        tagmap.put("8D", 1);
        tagmap.put("8E", 1);
        tagmap.put("5F28", 1);
        tagmap.put("9F6C", 1);
        //from FIS 0224
//        tagmap.put("70", 0);
        tagmap.put("8C", 1);

        // CDOL1
        tagmap.put("9F02", 1);
        tagmap.put("9F03", 1);
        tagmap.put("9F1A", 1);
        tagmap.put("95", 1);
        tagmap.put("5F2A", 1);
        tagmap.put("9A", 1);
        tagmap.put("9C", 1);
        tagmap.put("9F37", 1);
        tagmap.put("9F4C", 1);
        tagmap.put("9F45", 1);

        // AC Resp
        tagmap.put("77", 0);
        tagmap.put("80", 0);
        tagmap.put("9F27", 1);
        tagmap.put("9F36", 1);
        tagmap.put("9F26", 1);
        tagmap.put("9F10", 1);

        // CDOL2
        tagmap.put("8A", 1);
//        tagmap.put("95", 1);
//        tagmap.put("9F37", 1);

        // Issuer Response
        tagmap.put("91", 1);
        tagmap.put("8A", 1);
        tagmap.put("71", 0);
        tagmap.put("72", 0);
        tagmap.put("9F18", 1);
        tagmap.put("86", 1);
        return tagmap;
    }

    public static String NSICCS_CAPK_EXP = "03";
    public static String NSICCS_CAPK_MOD = "8B1DA85F7D036EC77EF63EF47DB606D81A32FDDC0392FCF126588C9FAAC39597BEAF8A6DA7D2ED97B944A4501EB09DE52A130E17852E61E615BC16A998E44C3930906CE4C90EFD5A7357D9913C25D87A1252B3094C3E8C8E69F0A3F3D1B71AA0E5FF01A54CFAE2B6C163CBABD77D352DD18947EE2C5D35BBC25A5E0218CC7CAF";


    public static final int ICC_PROCESS_STAGE_INIT = 0;
    public static final int ICC_PROCESS_STAGE_GEN = 1;
    public static final int ICC_PROCESS_STAGE_TX = 2;
    public static final int ICC_PROCESS_STAGE_FINISHED = 3;
    public static final int ICC_PROCESS_STAGE_CANCELED = 4;

    public static final int ICC_VALIDATION_INVALIDATED = 0;
    public static final int ICC_VALIDATION_OK = 1;
    public static final int ICC_VALIDATION_FAILED = -1;

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len/2];

        for(int i = 0; i < len; i+=2){
            data[i/2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }

        return data;
    }

    public static String compress(String string) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream(string.length());
        GZIPOutputStream gos = new GZIPOutputStream(os);
        gos.write(string.getBytes());
        gos.close();
        byte[] compressed = os.toByteArray();
        os.close();
        return Base64.encodeToString(compressed, Base64.NO_WRAP);
    }

    public static String decompress(String compressed) throws IOException {
        final int BUFFER_SIZE = 32;
        ByteArrayInputStream is = new ByteArrayInputStream(Base64.decode(compressed, Base64.NO_WRAP));
        GZIPInputStream gis = new GZIPInputStream(is, BUFFER_SIZE);
        StringBuilder string = new StringBuilder();
        byte[] data = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = gis.read(data)) != -1) {
            string.append(new String(data, 0, bytesRead));
        }
        gis.close();
        is.close();
        return string.toString();
    }

    public static int checkInstalledCertificates() {
        int value = 0;
        try {
            KeyStore ks = KeyStore.getInstance("AndroidCAStore");
            if (ks != null) {
                ks.load(null, null);
                Enumeration<String> aliases = ks.aliases();
                List<String> list = Collections.list(aliases);
                for (String alias: list) {
                    java.security.cert.X509Certificate cert = null;
                    try {
                        cert = (java.security.cert.X509Certificate) ks.getCertificate(alias);
                    } catch (KeyStoreException e) {
                        e.printStackTrace();
                    }

                    if(Objects.requireNonNull(cert).getIssuerDN().getName().contains("system")){}

                    System.out.println("Subject DN: " + cert.getSubjectDN());

                    if (CER.contains(cert.getIssuerDN().getName())){
                        Log.d("VERIFICATION CER", "TRUE");
                    } else {
                        Log.d("VERIFICATION CER", "FALSE");
                        System.out.println("AC: " + cert.getIssuerDN().getName());
                        return 1;
                    }
                }

//                for (int i = 0; i < list.size(); i++) {
//                    String alias = list.get(i);
//                    java.security.cert.X509Certificate cert = null;
//                    try {
//                        cert = (java.security.cert.X509Certificate) ks.getCertificate(alias);
//                    } catch (KeyStoreException e) {
//                        e.printStackTrace();
//                    }
//
//                    if(Objects.requireNonNull(cert).getIssuerDN().getName().contains("system")){}
//
//                    System.out.println("Subject DN: " + cert.getSubjectDN());
//
//                    if (CER.contains(cert.getIssuerDN().getName())){
//                        Log.d("VERIFICATION CER", "TRUE");
//                    } else {
//                        Log.d("VERIFICATION CER", "FALSE");
//                        System.out.println("AC: " + cert.getIssuerDN().getName());
//                        return 1;
//                    }
//                }

//                for (int i = size; i > size-1; i--) {
//                    String alias = list.get(i);
//                    java.security.cert.X509Certificate cert = null;
//                    try {
//                        cert = (java.security.cert.X509Certificate) ks.getCertificate(alias);
//                    } catch (KeyStoreException e) {
//                        e.printStackTrace();
//                    }
//                    //To print System Certs only
//                    if(Objects.requireNonNull(cert).getIssuerDN().getName().contains("system")){
////                        System.out.println("SC: " + cert.getIssuerDN().getName());
//                    }
//                    //To print User Certs only
//                    if(cert.getIssuerDN().getName().contains("user")){
////                        System.out.println("UC: " + cert.getIssuerDN().getName());
//                    }
//
//                    System.out.println("Subject DN: " + cert.getSubjectDN());
//
//                    if (CER.contains(cert.getIssuerDN().getName())){
//                        Log.d("VERIFICATION CER", "TRUE");
//                    } else {
//                        Log.d("VERIFICATION CER", "FALSE");
//                        System.out.println("AC: " + cert.getIssuerDN().getName());
//                        return 1;
//                    }
//                }

//                while (aliases.hasMoreElements()) {
//                    String alias = (String) aliases.nextElement();
//                    java.security.cert.X509Certificate cert = null;
//                    try {
//                        cert = (java.security.cert.X509Certificate) ks.getCertificate(alias);
//                    } catch (KeyStoreException e) {
//                        e.printStackTrace();
//                    }
//                    //To print System Certs only
//                    if(Objects.requireNonNull(cert).getIssuerDN().getName().contains("system")){
////                        System.out.println("SC: " + cert.getIssuerDN().getName());
//                    }
//                    //To print User Certs only
//                    if(cert.getIssuerDN().getName().contains("user")){
////                        System.out.println("UC: " + cert.getIssuerDN().getName());
//                    }
//
////                    System.out.println("AC: " + cert.getIssuerDN().getName());
//                    System.out.println("Subject DN: " + cert.getSubjectDN());
////                    System.out.println("Issuer DN: " + cert.getIssuerDN());
////                    System.out.println("Serial Number: " + cert.getSerialNumber());
//
//                    if (CER.contains(cert.getIssuerDN().getName())){
//                        Log.d("VERIFICATION CER", "TRUE");
//                    } else {
//                        Log.d("VERIFICATION CER", "FALSE");
//                        System.out.println("AC: " + cert.getIssuerDN().getName());
//                        return 1;
//                    }
//                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (java.security.cert.CertificateException e) {
            e.printStackTrace();
        }
        return value;
    }

}

