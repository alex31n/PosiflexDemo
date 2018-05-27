package com.ms.posiflex;


import android.content.Context;
import android.content.SharedPreferences;

public class PrinterPref {

    public static final String PREF_NAME = "PRINTER_PREF";
    public static final String PREF_KEY_DEVICE_NAME = "DEVICE_NAME";
    public static final String PREF_KEY_DEVICE_ID = "DEVICE_ID";
    public static final String PREF_KEY_VENDOR_ID = "VENDOR_ID";
    public static final String PREF_KEY_PRODUCT_ID = "PRODUCT_ID";
    public static final String PREF_KEY_PRINTER_TYPE = "PRINTER_TYPE";

    public static final int PRINTER_TYPE_SUMNI = 1;
    public static final int PRINTER_TYPE_POSIFLEX = 2;

    private final Context context;
    SharedPreferences pref;

    private PrinterPref(Context context){
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);
    }

    public static PrinterPref newInstance(Context context) {
        return new PrinterPref(context);
    }

    public boolean setDeviceName(String name){
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(PREF_KEY_DEVICE_NAME, name);
        return editor.commit();
    }

    public String getDeviceName(){
        return pref.getString(PREF_KEY_DEVICE_NAME,"");
    }
    public boolean setPrinterType(int i){
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(PREF_KEY_PRINTER_TYPE, i);
        return editor.commit();
    }

    /**
     * 1 for Sumni,
     * 2 for Posiflex
    */
    public int getPrinterType(){
        return pref.getInt(PREF_KEY_PRINTER_TYPE,0);
    }

    public static final String[] printers = {"SUMNI", "POSIFLEX"};


    public boolean clear(){
        return pref.edit().clear().commit();
    }
}
