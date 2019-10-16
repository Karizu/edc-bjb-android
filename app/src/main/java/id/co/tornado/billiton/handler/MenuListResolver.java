/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.tornado.billiton.handler;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
/**
 *
 * @author Ahmad
 */
public class MenuListResolver {

    private Cursor c;
    private Cursor t;
    private DataBaseHelper helperDb;
    private JSONObject jroot;

    public void menuListResolver() {
    }

    public JSONObject loadMenu(Context context, String menuId, JSONObject data) throws Exception, JSONException {
        helperDb = new DataBaseHelper(context);
        SQLiteDatabase clientDB = null;
        String menuTitle="";
        jroot = new JSONObject();
        try {
            helperDb.openDataBase();
            clientDB = helperDb.getActiveDatabase();
            String Select = "select * from screen where screen_id='"+menuId+"'";
            t = clientDB.rawQuery(Select, null);
            JSONObject screen = new JSONObject();
            if (t.moveToFirst()) {
                menuTitle = t.getString(t.getColumnIndex("screen_title"));
                String screenType = t.getString(t.getColumnIndex("screen_type_id"));
                if (!screenType.equals("0")) {
                    //throw new Exception("Menu "+menuId+" tidak terdaftar, hubungi administrator");
                }
                screen.put("type", screenType);
                String actionUrl = t.getString(t.getColumnIndex("action_url"));
                if (actionUrl!=null||actionUrl!="null") {
                    screen.put("action_url", actionUrl);
                }
                screen.put("title", menuTitle);
                screen.put("id", menuId);
                screen.put("ver", t.getString(t.getColumnIndex("version")));
                screen.put("print", t.getString(t.getColumnIndex("print")));
                screen.put("print_text", t.getString(t.getColumnIndex("print_text")));
            }
            Select = "select * from screen_component b, component c "
                    + "where b.comp_id = c.comp_id "
                    + "and b.screen_id='"+menuId+"' "
//                    + "and c.visible='t'"
                    + "order by b.sequence";
            c = clientDB.rawQuery(Select, null);
            JSONArray comp = new JSONArray();
            JSONObject comps = new JSONObject();
            JSONObject component = new JSONObject();
            boolean skipComponent = false;
            if (c.moveToFirst()) {
                do {
                    component = new JSONObject();
                    skipComponent = false;
                    component.put("visible", String.valueOf(!(c.getString(c.getColumnIndex("visible")).equals("f"))));
                    String compType = c.getString(c.getColumnIndex("component_type_id"));
                    component.put("comp_type", compType);
                    component.put("comp_id", c.getString(c.getColumnIndex("comp_id")));
                    String nama = c.getString(c.getColumnIndex("comp_lbl"));
                    String valuePrint = "";
                    JSONObject compValues = new JSONObject();
                    JSONArray compValue = new JSONArray();
                    JSONObject cmVal = new JSONObject();
                    //Log.v("BUTTON", nama+" "+ String.valueOf(!(c.getString(c.getColumnIndex("visible")).equals("f"))));
                    int cType = Integer.valueOf(compType);
                    switch (cType) {
                        case 6 :
                            component.put("comp_act", c.getString(c.getColumnIndex("comp_act")));
                            break;
                        case 0 :
                            component.put("comp_act", c.getString(c.getColumnIndex("comp_act")));
                            break;
                        case 5 :
                            component.put("comp_act", c.getString(c.getColumnIndex("comp_act")));
                            break;
                        case 4 :
                            component.put("comp_act", c.getString(c.getColumnIndex("comp_act")));
                            break;
                        case 2 :
                            String isMandatory = String.valueOf(c.getInt(c.getColumnIndex("mandatory")));
                            String isDisabled = String.valueOf(c.getInt(c.getColumnIndex("disabled")));
                            String inputType = c.getString(c.getColumnIndex("comp_content_type"));
                            String minLength = String.format("%03d", c.getInt(c.getColumnIndex("min_length")));
                            String maxLength = String.format("%03d", c.getInt(c.getColumnIndex("max_length")));
                            component.put("comp_opt", isMandatory.concat(isDisabled).concat(inputType).concat(minLength).concat(maxLength));
                            break;
                        case 3 :
                            String isMandator = String.valueOf(c.getInt(c.getColumnIndex("mandatory")));
                            String isDisable = String.valueOf(c.getInt(c.getColumnIndex("disabled")));
                            String inputTyp = c.getString(c.getColumnIndex("comp_content_type"));
                            String minLengt = String.format("%03d", c.getInt(c.getColumnIndex("min_length")));
                            String maxLengt = String.format("%03d", c.getInt(c.getColumnIndex("max_length")));
                            component.put("comp_opt", isMandator.concat(isDisable).concat(inputTyp).concat(minLengt).concat(maxLengt));
                            break;
                    }
                    component.put("seq", String.valueOf(c.getInt(c.getColumnIndex("sequence"))));
                    String[] compTypeFilter = {"1","2","3","4","5","6"};
                    if (data.has("messageId")&&(Arrays.asList(compTypeFilter).contains(compType))) {
                        JSONObject jValue = data;
                        compValues = new JSONObject();
                        compValue = new JSONArray();
                        cmVal = new JSONObject();
                        String tidyFieldname = c.getString(c.getColumnIndex("comp_act"));
                        String fieldPrefix = "";
                        valuePrint = "";
                        if (tidyFieldname!=null) {
                            if (tidyFieldname.startsWith("[")) {
                                fieldPrefix = tidyFieldname.substring(0, tidyFieldname.indexOf("]")+1);
                                tidyFieldname = tidyFieldname.substring(tidyFieldname.indexOf("]")+1);
                            }
                            if (tidyFieldname.contains("+")) {
                                String[] usedFields = tidyFieldname.split("\\+");
                                double sumFields = 0;
                                for (int nf=0;nf<usedFields.length;nf++) {
                                    String cf = ((String) jValue.get(usedFields[nf])).replace(" ","");
                                    if (cf.matches("-?\\d+(\\.\\d+)?")) {
                                        if (menuId.equals("543220F") && usedFields[nf].equals("nominal")) {
                                            sumFields += (Double.parseDouble(cf) * 100);
                                        } else if (menuId.equals("543310F") && usedFields[nf].equals("nominal")) {
                                            sumFields += (Double.parseDouble(cf) * 100);
                                        } else if (menuId.equals("543220F") && usedFields[nf].equals("nom_rptok")) {
                                            sumFields += (Double.parseDouble(cf) * -1);
                                        } else if (menuId.equals("543310F") && usedFields[nf].equals("nom_rptok")) {
                                            sumFields += (Double.parseDouble(cf) * -1);
                                        } else {
                                            sumFields += Double.parseDouble(cf);
                                        }
                                    }
                                }
                                valuePrint = String.valueOf((int) sumFields);
                            } else {
                                if (tidyFieldname.equals("")) {
                                    valuePrint = "";
                                } else {
                                    try {
                                        valuePrint = (String) jValue.get(tidyFieldname);
                                    } catch (Exception e) {
                                        Log.e("MLR", "Error assign value : " + e.getMessage());
                                        valuePrint = "";
                                    }
                                }
                            }
                            if (valuePrint!=null) {
                                if (menuId.equals("523000F")) {
                                    if (tidyFieldname.startsWith("tgl_")) {
                                        try {
                                            String mnval = (String) jValue.get(tidyFieldname.replace("tgl_", "jm_"));
                                            String amt = (String) jValue.get(tidyFieldname.replace("tgl_", "amount_"));
                                            if (amt.matches("-?\\d+(\\.\\d+)?")) {
                                                double d = Double.parseDouble(amt);
                                                d = d/100;
                                                DecimalFormatSymbols idrFormat = new DecimalFormatSymbols(Locale.getDefault());
                                                idrFormat.setDecimalSeparator(',');
                                                idrFormat.setGroupingSeparator('.');
                                                DecimalFormat formatter = new DecimalFormat("###,###,##0", idrFormat);
                                                amt = "Rp " + formatter.format(d);
                                            }
                                            valuePrint += "\t" + mnval + "\t" + amt;
                                        }catch (Exception e) {
                                            //pass
                                        }
                                    }
                                }
                                String mnemonic = "";
                                if (tidyFieldname.equals("Absen OK")) {
                                    skipComponent = true;
                                }
                                if (tidyFieldname.equals("late")&&valuePrint.equals("")) {
                                    skipComponent = true;
                                }
                                if (tidyFieldname.equals("jmlkwh")) {
                                    valuePrint = valuePrint.replaceFirst("^0+(?!$)", "");
                                }
                                if (menuId.equals("543220F") && tidyFieldname.equals("nama")) {
                                    valuePrint = valuePrint.substring(0,20);
                                }
                                if (menuId.equals("543310F") && tidyFieldname.equals("nama")) {
                                    valuePrint = valuePrint.substring(0,20);
                                }
                                if (menuId.equals("543220F") && tidyFieldname.equals("reference")) {
                                    valuePrint = valuePrint.substring(0,20);
                                }
                                if (menuId.equals("543310F") && tidyFieldname.equals("reference")) {
                                    valuePrint = valuePrint.substring(0,20);
                                }
                                if (menuId.startsWith("544") && tidyFieldname.equals("nokk")) {
                                    //KTA ada yg kurang dari 12 digit (diasumsikan 16 digit CC)
                                    if (valuePrint.length()>16) {
                                        valuePrint = valuePrint.substring(0, 16);
                                    }
                                }
                                if (tidyFieldname.startsWith("nom")
                                        ||tidyFieldname.startsWith("sal")
                                        ||tidyFieldname.startsWith("amo")
                                        ||tidyFieldname.equals("fee")) {
                                    valuePrint = valuePrint.trim();
                                    if (valuePrint.equals("")) {
                                        valuePrint = "0";
                                    }
                                    if (valuePrint.startsWith("+")) {
                                        valuePrint = valuePrint.substring(1);
                                        mnemonic = "+";
                                    }
                                    if (valuePrint.startsWith("-")) {
                                        valuePrint = valuePrint.substring(1);
                                        mnemonic = "-";
                                    }
                                }
                                if (compType.equals("1")
                                        &&(valuePrint.matches("-?\\d+(\\.\\d+)?"))
                                        &&(tidyFieldname.startsWith("nom")
                                        ||tidyFieldname.startsWith("sal")
                                        ||tidyFieldname.startsWith("amo")
                                        ||tidyFieldname.startsWith("cor"))) {
                                    double d = Double.parseDouble(valuePrint);
                                    DecimalFormatSymbols idrFormat = new DecimalFormatSymbols(Locale.getDefault());
                                    idrFormat.setDecimalSeparator(',');
                                    idrFormat.setGroupingSeparator('.');
                                    if (tidyFieldname.startsWith("sal") && !menuId.equals("544110F")) {
                                        d = d/100;
                                    }
                                    if (tidyFieldname.startsWith("amo")
                                            &&(menuId.startsWith("55"))
                                            &&(menuId.endsWith("F"))
                                            &&(!menuId.startsWith("558"))) {
                                        d = d/100;
                                    }
                                    if (tidyFieldname.startsWith("nom")
                                            &&(menuId.startsWith("548"))
                                            &&(menuId.endsWith("F"))) {
                                        d = d/100;
                                    }
                                    if (tidyFieldname.startsWith("nominal")
                                            &&(menuId.startsWith("543"))
                                            &&(!menuId.endsWith("0"))) {
                                        d = d/100;
                                    }
                                    if (menuId.equals("2A1000F") && tidyFieldname.startsWith("nom")) {
                                        d = d/100;
                                    }
                                    if (menuId.equals("544110F") && tidyFieldname.startsWith("nom")) {
                                        d = d/100;
                                    }
                                    if (menuId.equals("544100F") && tidyFieldname.startsWith("nom")) {
                                        d = d/100;
                                    }
                                    if (menuId.equals("543120F") && tidyFieldname.equals("nominal")) {
                                        d = d*100;
                                    }
                                    if (menuId.equals("543210F") && tidyFieldname.equals("nominal")) {
                                        d = d*100;
                                    }
                                    if (menuId.equals("543220F") && tidyFieldname.equals("nominal")) {
                                        d = d*100;
                                    }
                                    if (menuId.equals("543310F") && tidyFieldname.equals("nominal")) {
                                        d = d*100;
                                    }
                                    if (menuId.equals("543220F") && tidyFieldname.equals("nom_rptok")) {
                                        d = d/100;
                                    }
                                    if (menuId.equals("543310F") && tidyFieldname.equals("nom_rptok")) {
                                        d = d/100;
                                    }
                                    if (menuId.equals("543220E") && tidyFieldname.equals("nominal")) {
                                        d = d*100;
                                    }
                                    if (menuId.equals("543310F") && tidyFieldname.equals("nom_admin")) {
                                        d = d/100;
                                    }
                                    if (menuId.equals("543220F") && tidyFieldname.equals("nom_admin")) {
                                        d = d/100;
                                    }
                                    if (menuId.equals("543120F") && tidyFieldname.equals("nom_admin")) {
                                        d = d*10;
                                    }
                                    if (menuId.equals("543120F") && tidyFieldname.equals("nominal")) {
                                        d = d/100;
                                    }
                                    if (tidyFieldname.startsWith("amount_")) {
                                        d = d/100;
                                    }
                                    if (menuId.startsWith("910") && tidyFieldname.startsWith("nom_")) {
                                        d = d/100;
                                    }
                                    DecimalFormat formatter = new DecimalFormat("###,###,##0", idrFormat);
                                    valuePrint = formatter.format(d);
                                    if (menuId.startsWith("910") && tidyFieldname.startsWith("nom_")) {
                                        valuePrint = "Rp "  + formatter.format(d);
                                    }
                                }
                                if (mnemonic.equals("-")) {
                                    valuePrint = mnemonic+valuePrint;
                                }
                                if (menuId.equals("544110F") && tidyFieldname.equals("due_date")) {
                                    if (valuePrint.length()==8) {
                                        String[] namaBulan = {"Bulan", "Januari", "Februari", "Maret", "April", "Mei", "Juni",
                                                "Juli", "Agustus", "September", "Oktober", "November", "Desember"};
                                        String tahun = valuePrint.substring(0,4);
                                        int bln = Integer.parseInt(valuePrint.substring(4,6));
                                        int tgl = Integer.parseInt(valuePrint.substring(6,8));
                                        String tanggal = String.valueOf(tgl) + " " + namaBulan[bln] + " " + tahun;
                                        valuePrint = tanggal;
                                    }
                                }
                                if (tidyFieldname.equals("statusafter")) {
                                    if (valuePrint.equals("aa")) {
                                        valuePrint = "AKTIF";
                                    } else {
                                        valuePrint = "NON AKTIF";
                                    }
                                }

                                if (tidyFieldname.equals("lama_pasif")) {
                                    if (valuePrint!=null) {
                                        if (!valuePrint.equals("")) {
                                            if (valuePrint.startsWith("0")) {
                                                valuePrint = valuePrint.substring(1);
                                            }
                                            if (!valuePrint.endsWith("bln")) {
                                                valuePrint += " bln";
                                            }
                                        }
                                    }
                                }
                                if (tidyFieldname.equals("periode_input")) {
                                    String outValue = "";
                                    if (valuePrint!=null) {
                                        if (valuePrint.length() > 1) {
                                            outValue += valuePrint.substring(0, 2);
                                        } else {
                                            outValue += "  ";
                                        }
                                        outValue += "/";
                                        if (valuePrint.length() > 5) {
                                            outValue += valuePrint.substring(2);
                                        } else {
                                            outValue += "  ";
                                        }
                                    }
                                    valuePrint = outValue;
                                }
                                if (tidyFieldname.equals("norekzakat")) {
                                    if (valuePrint!=null) {
                                        if (valuePrint.length()>15) {
                                            valuePrint = valuePrint.substring(0,15);
                                        }
                                    }
                                }
                                if (tidyFieldname.equals("swcode")) {
                                    String switcher = "";
                                    switch (valuePrint) {
                                        case "00" :
                                            switcher = "(BRI)";
                                            break;
                                        case "01" :
                                            switcher = "(LINK)";
                                            break;
                                        case "02" :
                                            switcher = "(PRIMA)";
                                            break;
                                        case "03" :
                                            switcher = "(BERSAMA)";
                                            break;
                                        default :
                                            break;
                                    }
                                    if (!switcher.equals("")) {
                                        valuePrint = switcher;
                                    }
                                }
                                if (menuId.equals("514200F")&&tidyFieldname.equals("no_hp")) {
                                    String maskedHp = "";
                                    maskedHp = valuePrint.substring(1,valuePrint.indexOf(" "));
                                    int hpLength = maskedHp.length();
                                    maskedHp = "************************" + maskedHp.substring(hpLength-4);
                                    maskedHp = maskedHp.substring(maskedHp.length()-hpLength);
                                    valuePrint = maskedHp;
                                }
                                if (tidyFieldname.equals("flight_data")) {
                                    String parsedValues = parseFlightData(valuePrint);
//                                    Log.d("FPRS", parsedValues);
                                    valuePrint = parsedValues;
                                }
                            } else {
                                if (tidyFieldname.equals("late")) {
                                    skipComponent = true;
                                }
                            }
                        } else {
                            valuePrint = "";
                        }
                        if (!valuePrint.startsWith("\n")) {
                            valuePrint = valuePrint.trim();
                        }
                        valuePrint = fieldPrefix+valuePrint;
                        cmVal.put("value", valuePrint);
                        cmVal.put("print", valuePrint);
                        compValue.put(cmVal);
                        compValues.put("comp_value", compValue);
                        component.put("comp_values", compValues);
                    } else if (data.has("msg_rc")&&(Arrays.asList(compTypeFilter).contains(compType))) {
                            compValues = new JSONObject();
                            compValue = new JSONArray();
                            cmVal = new JSONObject();
                            valuePrint = (String) data.get("msg_resp");
                            valuePrint = valuePrint.trim();
                            cmVal.put("value", valuePrint);
                            cmVal.put("print", valuePrint);
                            compValue.put(cmVal);
                            compValues.put("comp_value", compValue);
                            component.put("comp_values", compValues);
                    } else {
                        if (cType==1) {
                            compValues = new JSONObject();
                            compValue = new JSONArray();
                            cmVal = new JSONObject();
                            valuePrint = "";
                            cmVal.put("value", valuePrint);
                            cmVal.put("print", valuePrint);
                            compValue.put(cmVal);
                            compValues.put("comp_value", compValue);
                            component.put("comp_values", compValues);
                        }
                    }
                    if (nama.contains("___")) {
                        component.put("comp_lbl", nama.replaceAll("___", valuePrint));
                        compValues = new JSONObject();
                        compValue = new JSONArray();
                        cmVal = new JSONObject();
                        cmVal.put("value", "");
                        cmVal.put("print", "");
                        compValue.put(cmVal);
                        compValues.put("comp_value", compValue);
                        component.put("comp_values", compValues);
                    } else {
                        component.put("comp_lbl", nama);
                    }
                    if (!skipComponent) {
                        comp.put(component);
                    }
                } while (c.moveToNext());
            }
            comps.put("comp", comp);
            screen.put("comps", comps);
//            if (menuId.equals("291000F")||menuId.equals("292000F")||menuId.equals("2A2000F")) {
            if (data.has("server_date")) {
                screen.put("server_date", data.get("server_date"));
            }
            if (data.has("server_time")) {
                screen.put("server_time", data.get("server_time"));
            }
            if (data.has("server_ref")) {
                screen.put("server_ref", data.get("server_ref"));
            }
//            }
            jroot.put("screen", screen);
            if (data.has("server_ref")) {
                jroot.put("server_ref", data.get("server_ref"));
            }
            if (data.has("server_date")) {
                jroot.put("server_date", data.get("server_date"));
            }
            if (data.has("server_time")) {
                jroot.put("server_time", data.get("server_time"));
            }
            if (data.has("server_air")) {
                jroot.put("server_air", data.get("server_air"));
            }
            if (c!=null) {
                c.close();
                c = null;
            }
            if (t!=null) {
                t.close();
                t = null;
            }
            if (clientDB!=null) {
                clientDB.close();
                clientDB = null;
            }
            helperDb.close();
            helperDb = null;
        } catch (Exception e) {
            if (c!=null) {
                c.close();
                c = null;
            }
            if (t!=null) {
                t.close();
                t = null;
            }
            if (clientDB!=null) {
                if (clientDB.isOpen()) {
                    clientDB.close();
                }
                clientDB = null;
            }
            if (helperDb!=null) {
                helperDb.close();
                helperDb = null;
            }
            Log.e("ERR", "Loading menu @ID:"+menuId);
            e.printStackTrace();
        }
    return jroot;
    }

    private String parseFlightData(String inData) {
        String outData = "";
        outData += parseFlightRow(inData.substring(0,23));
        if (inData.length()>45) {
            outData += parseFlightRow(inData.substring(23,46));
        }
        if (inData.length()>68) {
            outData += parseFlightRow(inData.substring(46,69));
        }
//        Log.d("FDAT" , outData);
        return outData;
    }

    private String parseFlightRow(String inRow) {
        String outRow = "\n";
        String seat = String.valueOf(Integer.parseInt(inRow.substring(0,2))) + " seat";
        String carrier = inRow.substring(2,4);
        String flightClass = inRow.substring(4,5);
        String flightFrom = inRow.substring(5,8);
        String flightDest = inRow.substring(8,11);
        String flightNum = "    ";
        String flightDate = "";
        String flightTime = "";
        if (inRow.substring(11,23).matches("-?\\d+(\\.\\d+)?")) {
            flightNum = inRow.substring(11, 15);
            flightDate = inRow.substring(15, 17) + "/" + inRow.substring(17, 19);
            flightTime = inRow.substring(19, 21) + ":" + inRow.substring(21, 23);
        }
        outRow += carrier + " " + flightNum + " " + flightClass + " " + flightFrom + "-" +
                flightDest + " " + flightDate + " " + flightTime + " : " + seat;
//        Log.d("FROW" , outRow);
        return outRow;
    }

    public JSONObject loadMenu(Context context, String menuId) throws Exception, JSONException {
        JSONObject dummy = new JSONObject();
        return loadMenu(context, menuId, dummy);
    }

}
