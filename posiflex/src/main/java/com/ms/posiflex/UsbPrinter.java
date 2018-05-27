package com.ms.posiflex;


import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.util.Log;


import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class UsbPrinter {

    private final UsbPrinterAdapter printerAdapter;
    PrinterPref pref;
    private byte[] mFormat;

    int maxLength = 64;

    public enum Align {LEFT, CENTER, RIGHT}

    public enum Style {EXTRA_LARGE, LARGE, MEDIUM, NORMAL, SMALL, BOLD}

    public static UsbPrinter getInstance(Context context) {
        UsbPrinter printer = new UsbPrinter(context);
        return printer;
    }

    public UsbPrinter(Context context) {
        mFormat = normal();
        printerAdapter = new UsbPrinterAdapter(context);
        pref = PrinterPref.newInstance(context);

        init();
    }

    private void init() {

        Log.e("TAG", "pref.getDeviceName() " + pref.getDeviceName());

        if (printerAdapter.mDevice == null && !pref.getDeviceName().isEmpty()) {
            UsbDevice device = printerAdapter.getDevice(pref.getDeviceName());
            printerAdapter.setDevice(device);
            //maxLength = printerAdapter.
        }

    }

    public void setDevice(UsbDevice device) {
        printerAdapter.setDevice(device);
    }

    public void openConnection() {
        init();
        printerAdapter.openConnection();
    }

    public void closeConnection() {
        printerAdapter.closeConnection();
    }

    public static class Builder {
        private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        public Builder() {
            outputStream = new ByteArrayOutputStream();

            try {
                outputStream.write(normal());
                outputStream.write(leftAlign());
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }

        public Builder setBold() throws IOException {
            outputStream.write(bold());
            return this;
        }

        public Builder setNormalText() throws IOException {
            outputStream.write(normal());
            return this;
        }

        public Builder addText(String text) throws IOException {
            outputStream.write(text.getBytes());
            return this;
        }

        public Builder addLineBreak(int line) throws IOException {

            /*for (int i = 0; i < line; i++) {
                outputStream.write(textToBytes("\n"));
            }*/

            String t = "";
            for (int i = 0; i < line; i++) {
                t+="\n";
            }
            outputStream.write(textToBytes(t));

            return this;
        }

        public Builder addLineBreak() throws IOException {
            outputStream.write(textToBytes("\n"));

            return this;
        }

        public Builder setTextSizeExtraLarge() throws IOException {
            outputStream.write(new byte[]{29, 33, 35});
            return this;
        }

        public Builder setTextSizeMedium() throws IOException {
            outputStream.write(new byte[]{29, 33, 16});
            return this;
        }

        public Builder setTextSizeSmall() throws IOException {
            outputStream.write(small());
            return this;
        }

        public Builder setTextSizeLarge() throws IOException {
            /*outputStream.write((byte) (0x10 | mFormat[2]));
            outputStream.write((byte) (0x20 | mFormat[2]));*/
            outputStream.write(largeHeight());
            outputStream.write(largeHeight());
            return this;
        }

        public Builder setAlignment(Align align) throws IOException {
            switch (align) {
                case CENTER:
                    outputStream.write(centerAlign());
                    break;
                case RIGHT:
                    outputStream.write(rightAlign());
                    break;
                case LEFT:
                default:
                    outputStream.write(leftAlign());
                    break;

            }
            return this;
        }

        public Builder cut() throws IOException {
            outputStream.write(cutByte());
            return this;
        }

        public byte[] getBytes() {
            return outputStream.toByteArray();
        }
    }

    private static byte[] normal() {
        return new byte[]{27, 33, 0};
    }

    private static byte[] bold() {
        //mFormat[2] = ((byte) (0x8 | mFormat[2]));
        return new byte[]{0x1B, 0x21, 0x08};  // 1- only bold text
    }

    private static byte[] small() {
        //mFormat[2] = ((byte) (0x1 | mFormat[2]));
        return new byte[]{0x1B, 0x21, 0x1};
    }

    private static byte[] medium() {
        return new byte[]{29, 33, 16};
    }

    private static byte[] extraLarge() {
        return new byte[]{29, 33, 35};
    }

    private static byte[] getStyleByte(Style style) throws IOException {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        switch (style) {
            case EXTRA_LARGE:
                stream.write(extraLarge());
                break;
            case LARGE:
                stream.write(largeHeight());
                stream.write(largeWidth());
                break;
            case MEDIUM:
                stream.write(medium());
                break;
            case SMALL:
                stream.write(small());
                break;
            case BOLD:
                stream.write(bold());
                break;
            case NORMAL:
            default:
                stream.write(normal());
                break;
        }

        return stream.toByteArray();
    }

    private static byte[] getAlignByte(Align align) throws IOException {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        switch (align) {
            case CENTER:
                stream.write(centerAlign());
                break;
            case RIGHT:
                stream.write(rightAlign());
                break;
            case LEFT:
            default:
                stream.write(leftAlign());
                break;

        }

        return stream.toByteArray();
    }

    private static byte[] textBuild(String text, Align align, Style style) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write(normal());

        /*switch (align) {
            case CENTER:
                stream.write(centerAlign());
                break;
            case RIGHT:
                stream.write(rightAlign());
                break;
            case LEFT:
            default:
                stream.write(leftAlign());
                break;

        }*/
        /*switch (style) {
            case EXTRA_LARGE:
                stream.write(extraLarge());
                break;
            case LARGE:
                stream.write(largeHeight());
                stream.write(largeWidth());
                break;
            case MEDIUM:
                stream.write(medium());
                break;
            case SMALL:
                stream.write(small());
                break;
            case BOLD:
                stream.write(bold());
                break;
            case NORMAL:
            default:
                stream.write(normal());
                break;

        }*/

        stream.write(getAlignByte(align));
        stream.write(getStyleByte(style));

        stream.write(textToBytes(text));

        return stream.toByteArray();
    }

    private static byte[] textToBytes(String text) {
        return text.getBytes();
    }

    private static byte[] largeHeight() {
        //mFormat[2] = ((byte) (0x10 | mFormat[2]));
        return new byte[]{27, 33, 0x10};
    }

    private static byte[] largeWidth() {
        /*mFormat[2] = ((byte) (0x20 | mFormat[2]));
        return mFormat;*/
        return new byte[]{27, 33, 0x20};

    }

    public static byte[] underlined() {
        //mFormat[2] = ((byte) (0x80 | mFormat[2]));

        return new byte[]{27, 33, 0x8};
    }

    private static byte[] rightAlign() {
        /*mFormat = new byte[]{0x1B, 'a', 0x02};
        return mFormat;
        */
        return new byte[]{0x1B, 'a', 0x02};
    }

    private static byte[] leftAlign() {
        /*mFormat = new byte[]{0x1B, 'a', 0x00};
        return mFormat;
        */
        return new byte[]{0x1B, 'a', 0x00};
    }

    private static byte[] centerAlign() {
        /*mFormat = new byte[]{0x1B, 'a', 0x01};
        return mFormat;
        */
        return new byte[]{0x1B, 'a', 0x01};
    }

    private static byte[] cutByte() {
        return new byte[]{0x1D, 0x56, 0x41, 0x10};
    }

    public void print(byte[] b) {
        printerAdapter.print(b);
    }

    public void cutPaper() {
        print(cutByte());
    }

    public void print(String text) {
        print(textToBytes(text));
    }

    public void printText(String text) throws IOException {
        print(textBuild(text, Align.LEFT, Style.NORMAL));
    }

    public void printText(String text, Align align) throws IOException {
        print(textBuild(text, align, Style.NORMAL));
    }
    public void printText(String text, Style style) throws IOException {
        print(textBuild(text, Align.LEFT, style));
    }

    public void printText(String text, Align align, Style style) throws IOException {
        print(textBuild(text, align, style));
    }



    public void print(Builder builder) {
        print(builder.getBytes());
    }

    public void printAndCut(String text) throws IOException {
        print(text);
        cutPaper();
    }

    public void lines(int lines) {
        String text = "";
        for (int i = 0; i < lines; i++) {
            text += "\n";
        }
        print(text.getBytes());
    }

    /*public void printAndCut() throws IOException {
        addLineBreak(1);
        this.outputStream.write(cutByte());
        print(this.outputStream.toByteArray());

    }*/

    /*public String columnText(String text, int length, Align align) {
        int spLength = length - text.length();
        switch (align) {
            case CENTER:
                int sp = spLength / 2;
                text = leftText(text, sp);

                // add extra one space if spLength is odd
                if (spLength % 2 == 1) {
                    sp++;
                }
                text = rightText(text, sp);
                break;

            case RIGHT:
                text = rightText(text, spLength);
                break;

            case LEFT:
            default:
                text = leftText(text, spLength);
                break;

        }

        return text;
    }

    private String leftText(String text, int sp) {
        for (int i = 0; i < sp; i++) {
            text = text + " ";
        }

        return text;
    }

    private String rightText(String text, int sp) {
        for (int i = 0; i < sp; i++) {
            text = " " + text;
        }

        return text;
    }*/

    public void printColumn(ColumnBuilder builder) {
        print(builder.getBytes());
        lines(1);
    }

    public static class ColumnBuilder {

        //private ArrayList<String> column = new ArrayList<>();
        private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        public ColumnBuilder() throws IOException {
            outputStream.write(normal());
        }

        public ColumnBuilder addColumn(String text, int length, Align align, Style style) throws IOException {
            //column.add(columnText(text, length, align));
            String t = columnText(text, length, align);
            outputStream.write(getStyleByte(style));
            outputStream.write(textToBytes(t));
            return this;
        }

        public ColumnBuilder addColumn(String text, int length, Align align) throws IOException {
            //column.add(columnText(text, length, align));
            String t = columnText(text, length, align);
            outputStream.write(normal());
            outputStream.write(textToBytes(t));
            return this;
        }


        public ColumnBuilder build() {

            return this;
        }

        public byte[] getBytes() {
            return outputStream.toByteArray();
        }

        private String columnText(String text, int length, Align align) {

            if (text.length()>length){

                return text.substring(0, length-3)+"...";

            }else if (text.length() == length){

                return text;

            }

            int spLength = length - text.length();
            switch (align) {
                case CENTER:
                    int sp = spLength / 2;
                    text = leftText(text, sp);

                    // add extra one space if spLength is odd
                    if (spLength % 2 == 1) {
                        sp++;
                    }
                    text = rightText(text, sp);
                    break;

                case RIGHT:
                    text = rightText(text, spLength);
                    break;

                case LEFT:
                default:
                    text = leftText(text, spLength);
                    break;

            }

            return text;
        }

        private String leftText(String text, int sp) {
            for (int i = 0; i < sp; i++) {
                text = text + " ";
            }

            return text;
        }

        private String rightText(String text, int sp) {
            for (int i = 0; i < sp; i++) {
                text = " " + text;
            }

            return text;
        }
    }


}