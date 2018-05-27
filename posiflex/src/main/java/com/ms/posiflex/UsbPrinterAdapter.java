package com.ms.posiflex;


import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.util.ArrayList;

public class UsbPrinterAdapter {
    private UsbManager mUsbManager;
    public UsbDevice mDevice;
    private PendingIntent mPermissionIntent;
    UsbDeviceConnection connection;
    String TAG = "TAG";

    Context context;
    private UsbInterface usbInterface;
    private UsbEndpoint usbEndPointOut;
    private UsbEndpoint usbEndPointIn;
    public int maxPacketSize;

    private UsbReceiver mUsbReceiver;

    public UsbPrinterAdapter(Context context) {
        this.context = context;
        mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
    }

    public void setDevice(UsbDevice device) {
        this.mDevice = device;
    }

    public void createConn() {
        //this.mDevice = device;
        Log.e(TAG, "createConn");

        //Log.e("TAG", "UsbReceiver " + (mUsbReceiver != null));
        mUsbReceiver = new UsbReceiver();

        mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        context.registerReceiver(mUsbReceiver, filter);

        //mUsbManager.requestPermission(device, mPermissionIntent);
    }

    private static final String ACTION_USB_PERMISSION = "com.pradeep.usbprinter.USB_PERMISSION";
    /*private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "mUsbReceiver");
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    mDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (mDevice != null) {
                            //call method to set up device communication
                            Log.e(TAG, "PERMISSION " + mDevice);
                        }
                    } else {
                        Log.e(TAG, "permission denied for device " + mDevice);
                    }
                }
            }
        }
    };*/

    class UsbReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "mUsbReceiver");
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    mDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (mDevice != null) {
                            //call method to set up device communication
                            Log.e(TAG, "PERMISSION " + mDevice);
                            openConnection();
                            print(bytes);
                            closeConnection();
                        }
                    } else {
                        Log.e(TAG, "permission denied for device " + mDevice);
                    }
                }
            }

        }
    }


    public void unregisterReceiver() {
        try {
            if (mUsbReceiver != null) {
                context.unregisterReceiver(mUsbReceiver);
                //mUsbManager = null;
            }
        } catch (Exception ignored) {
        }
    }

    public ArrayList<UsbDevice> getUsbDevices() {
        ArrayList<UsbDevice> deviceList = new ArrayList<>();
        // check for existing devices
        for (UsbDevice device : mUsbManager.getDeviceList().values()) {
            deviceList.add(device);
        }
        return deviceList;
    }

    public UsbDevice getDevice(String name) {
        ArrayList<UsbDevice> deviceList = getUsbDevices();
        for (UsbDevice device : deviceList) {
            if (device.getDeviceName().equals(name)) {
                return device;
            }
        }

        return null;
    }

    public String translateDeviceClass(int deviceClass) {
        switch (deviceClass) {
            case UsbConstants.USB_CLASS_APP_SPEC:
                return "Application specific USB class";
            case UsbConstants.USB_CLASS_AUDIO:
                return "USB class for audio devices";
            case UsbConstants.USB_CLASS_CDC_DATA:
                return "USB class for CDC devices (communications device class)";
            case UsbConstants.USB_CLASS_COMM:
                return "USB class for communication devices";
            case UsbConstants.USB_CLASS_CONTENT_SEC:
                return "USB class for content security devices";
            case UsbConstants.USB_CLASS_CSCID:
                return "USB class for content smart card devices";
            case UsbConstants.USB_CLASS_HID:
                return "USB class for human interface devices (for example, mice and keyboards)";
            case UsbConstants.USB_CLASS_HUB:
                return "USB class for USB hubs";
            case UsbConstants.USB_CLASS_MASS_STORAGE:
                return "USB class for mass storage devices";
            case UsbConstants.USB_CLASS_MISC:
                return "USB class for wireless miscellaneous devices";
            case UsbConstants.USB_CLASS_PER_INTERFACE:
                return "USB class indicating that the class is determined on a per-interface basis";
            case UsbConstants.USB_CLASS_PHYSICA:
                return "USB class for physical devices";
            case UsbConstants.USB_CLASS_PRINTER:
                return "USB class for printers";
            case UsbConstants.USB_CLASS_STILL_IMAGE:
                return "USB class for still image devices (digital cameras)";
            case UsbConstants.USB_CLASS_VENDOR_SPEC:
                return "Vendor specific USB class";
            case UsbConstants.USB_CLASS_VIDEO:
                return "USB class for video devices";
            case UsbConstants.USB_CLASS_WIRELESS_CONTROLLER:
                return "USB class for wireless controller devices";
            default:
                return "Unknown USB class!";
        }
    }

    private Thread thread = null;
    byte[] bytes;
    public void print(final byte[] bytes) {
        this. bytes = bytes;
        if (!mUsbManager.hasPermission(mDevice)) {
            //createConn();
            mUsbManager.requestPermission(mDevice, mPermissionIntent);
            return;
        }

        printInUsbDevice(bytes);

        /*thread = new Thread(new Runnable() {
            @Override
            public void run() {
                printInUsbDevice(bytes);
                thread.interrupt();
            }

        });
        thread.start();*/
    }

    private boolean printInUsbDevice(byte[] bytes) {
        //Log.e(TAG, "printInUsbDevice");
        if (connection == null || usbEndPointOut == null) {
            Log.e("TAG", "Connection is not open yet!");
            return false;
        }

        boolean bool;

        int paramInt = this.connection.bulkTransfer(this.usbEndPointOut, bytes, bytes.length, 0);
        bool = paramInt > 0;

        return bool;
    }

    /*private boolean connectToUsbDevice() {
        Boolean localBoolean = Boolean.valueOf(mUsbManager.hasPermission(this.mDevice));

        //Log.e(TAG, "setupUsbComm 1");
        if (localBoolean) {
            usbInterface();
            this.connection = mUsbManager.openDevice(this.mDevice);
            if (this.connection != null) {
                this.connection.claimInterface(this.usbInterface, true);
            }
            return true;
        }

        mUsbManager.requestPermission(this.mDevice, this.mPermissionIntent);

        return false;
    }*/

    private void usbInterface() {

        //Log.e(TAG, "usbInterface  " + mDevice.getInterfaceCount());
        for (int i = 0; i < mDevice.getInterfaceCount(); i++) {
            this.usbInterface = mDevice.getInterface(i);
            //this.usbEndPointOut = usbInterface.getEndpoint(0);

            Log.e(TAG, "usbInterface  " + i + "    EndpointCount " + usbInterface.getEndpointCount() + "");
            for (int j = 0; j < usbInterface.getEndpointCount(); j++) {
                //Log.e(TAG, "Endpoint "+ usbInterface.getEndpoint(j));
                UsbEndpoint tempUsbInterface = usbInterface.getEndpoint(j);
                //Log.e(TAG, "Endpoint type "+ tempUsbInterface.getType());

                if (tempUsbInterface.getType() == 2 && tempUsbInterface.getAddress() == 1) {
                    this.usbEndPointOut = tempUsbInterface;
                    this.maxPacketSize = tempUsbInterface.getMaxPacketSize();
                    return;
                }

            }


        }
    }

    public static byte[] stringToBytesASCII(String paramString) {
        byte[] arrayOfByte = new byte[paramString.length()];
        int i = 0;
        while (i < arrayOfByte.length) {
            arrayOfByte[i] = ((byte) paramString.charAt(i));
            i += 1;
        }
        return arrayOfByte;
    }

    public boolean openConnection() {

        if (mDevice == null) {
            Log.e("TAG", "Device not found");
            return false;
        }

        Boolean localBoolean = mUsbManager.hasPermission(this.mDevice);

        if (localBoolean) {
            usbInterface();
            this.connection = mUsbManager.openDevice(this.mDevice);
            if (this.connection != null) {
                boolean r = this.connection.claimInterface(this.usbInterface, true);
                Log.e("TAG", "connection.claimInterface " + r);
            }
            return true;
        }

        createConn();
        mUsbManager.requestPermission(this.mDevice, this.mPermissionIntent);

        return false;
    }

    public void closeConnection() {
        unregisterReceiver();
        connection = null;
        mDevice = null;
    }
}
