package com.l;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {
    private static StringBuilder xmlSb = new StringBuilder();


    public static void main(String[] args) throws IOException {
        //load xml
        byte[] byteSrc = null;
        FileInputStream fileInputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            fileInputStream = new FileInputStream("xml/AndroidManifest.xml");
            byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while (((len = fileInputStream.read(buffer)) != -1)) {
                byteArrayOutputStream.write(buffer, 0, len);
            }
            byteSrc = byteArrayOutputStream.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            fileInputStream.close();
            byteArrayOutputStream.close();
        }

        //parse xml header
        parseXmlHeader(byteSrc);
    }

    private static void parseXmlHeader(byte[] byteSrc) {
        byte[] xmlMagic = Utils.copyByte(byteSrc, 0, 4);
        System.out.println("magic:" + Utils.bytesToHexString(xmlMagic));
        byte[] xmlSize = Utils.copyByte(byteSrc, 4, 4);
        System.out.println("xml size:"+Utils.bytesToHexString(xmlSize));

        xmlSb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        xmlSb.append("\n");
    }
}
