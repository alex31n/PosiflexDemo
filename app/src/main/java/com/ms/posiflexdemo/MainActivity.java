package com.ms.posiflexdemo;

import android.hardware.usb.UsbDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.ms.posiflex.UsbPrinter;
import com.ms.posiflex.UsbPrinterAdapter;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Spinner spinner;
    UsbPrinterAdapter usbPrinterAdapter;

    ArrayList<UsbDevice> usbDeviceList = new ArrayList<>();
    ArrayList<String> usbDeviceNameList = new ArrayList<>();
    UsbPrinter printer;

    final String TAG = "PRINTER";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spinner = (Spinner) findViewById(R.id.spinner);

        usbPrinterAdapter = new UsbPrinterAdapter(this);
        printer = new UsbPrinter(this);
        spinnerUpdate();

    }

    private void spinnerUpdate() {

        usbDeviceList = usbPrinterAdapter.getUsbDevices();

        for (UsbDevice device : usbDeviceList) {
            usbDeviceNameList.add("USB " + device.getDeviceName());
        }


        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, usbDeviceNameList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


    }

    public void testPrint() {
        //String name = usbDeviceNameList;
        UsbDevice device = usbDeviceList.get(spinner.getSelectedItemPosition());
        Log.e("TAG", "Device " + (device != null));
        //usbPrinterAdapter.getDevice(device);
        printer.setDevice(device);

        String testText = "This is test print";

        try {
            printer.openConnection();
            printer.printText("TEST PRINT\n", UsbPrinter.Align.CENTER, UsbPrinter.Style.LARGE);
            printer.printText("--------------------\n", UsbPrinter.Align.CENTER, UsbPrinter.Style.LARGE);
            printer.lines(1);

            printer.printText(testText + "\n", UsbPrinter.Align.LEFT, UsbPrinter.Style.NORMAL);
            printer.lines(1);
            printer.printText("EXTRA LARGE\n", UsbPrinter.Align.LEFT, UsbPrinter.Style.EXTRA_LARGE);
            printer.lines(1);
            printer.printText("LARGE TEXT\n", UsbPrinter.Align.LEFT, UsbPrinter.Style.LARGE);
            printer.lines(1);
            printer.printText("MEDIUM TEXT\n", UsbPrinter.Align.LEFT, UsbPrinter.Style.MEDIUM);
            printer.lines(1);
            printer.printText("BOLD TEXT\n", UsbPrinter.Align.LEFT, UsbPrinter.Style.BOLD);
            printer.lines(1);

            printer.printText("ALIGN LEFT TEXT\n", UsbPrinter.Align.LEFT, UsbPrinter.Style.NORMAL);
            printer.lines(1);
            printer.printText("ALIGN CENTER TEXT\n", UsbPrinter.Align.CENTER, UsbPrinter.Style.NORMAL);
            printer.lines(1);
            printer.printText("ALIGN RIGHT TEXT\n", UsbPrinter.Align.RIGHT, UsbPrinter.Style.NORMAL);
            printer.lines(3);
            printer.cutPaper();

            printer.closeConnection();

        } catch (IOException e) {
            Log.e(TAG, "Print error ", e);

        }

    }

    private void connectPrinter(){
        UsbDevice device = usbDeviceList.get(spinner.getSelectedItemPosition());
        printer.setDevice(device);
        printer.openConnection();
        printer.closeConnection();
    }

    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_connect:
                connectPrinter();
                break;
            case R.id.btn_print:
                testPrint();
                break;
        }
    }
}
