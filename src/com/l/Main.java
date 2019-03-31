package com.l;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class Main {
    private static StringBuilder xmlSb = new StringBuilder();
    private static int stringChunkOffset = 8;//xml header一共8个字节
    private static ArrayList<String> stringContentList;


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
        //parse string chunk
        parseStringChunk(byteSrc);
    }

    private static void parseStringChunk(byte[] byteSrc) {
        //String Chunk type
        byte[] chunkTagByte = Utils.copyByte(byteSrc, stringChunkOffset, 4);
        System.out.println("string Chunk type:" + Utils.bytesToHexString(chunkTagByte));
        //Chunk size
        byte[] chunkSizeByte = Utils.copyByte(byteSrc, 12, 4);
        int chunkSize = Utils.byte2int(chunkSizeByte);
        System.out.println("Chunk size Hex:" + Utils.bytesToHexString(chunkSizeByte) + "  Chunk size:" + chunkSize);
        //String Count
        byte[] chunkStringCountByte = Utils.copyByte(byteSrc, 16, 4);
        int chunkStringCount = Utils.byte2int(chunkStringCountByte);
        System.out.println("String size Hex:" + Utils.bytesToHexString(chunkStringCountByte) + "  String size:" + chunkStringCount);

        stringContentList = new ArrayList<String>(chunkStringCount);

        //这里需要注意的是，后面的四个字节是Style的内容，然后紧接着的四个字节(unknown/flag)始终是0，所以我们需要直接过滤这8个字节
        //String Offset 相对于String Chunk的起始位置0x00000008
        byte[] chunkStringOffsetByte = Utils.copyByte(byteSrc, 28, 4);

        //这个偏移值是相对于StringChunk的头部位置
        int stringContentStart = 8 + Utils.byte2int(chunkStringOffsetByte);//这里的8是指header size为8
        System.out.println("start:"+stringContentStart);

        //String Content
        byte[] chunkStringContentByte = Utils.copyByte(byteSrc, stringContentStart, chunkSize);

        /**
         * 在解析字符串的时候有个问题，就是编码：UTF-8和UTF-16,如果是UTF-8的话是以00结尾的，如果是UTF-16的话以00 00结尾的
         */
        /**
         * 此处代码是用来解析AndroidManifest.xml文件的
         */
        //这里的格式是：偏移值开始的两个字节是字符串的长度，接着是字符串的内容，后面跟着两个字符串的结束符00
        byte[] firstStringSizeByte = Utils.copyByte(chunkStringContentByte, 0, 2);
        //一个字符对应两个字节
        int firstStringSize = Utils.byte2Short(firstStringSizeByte)*2;
        System.out.println("size:"+firstStringSize);
        byte[] firstStringContentByte = Utils.copyByte(chunkStringContentByte, 2, firstStringSize+2);//firstStringSize+2的2应该是结束符
        String firstStringContent = new String(firstStringContentByte);
        stringContentList.add(Utils.filterStringNull(firstStringContent));
        System.out.println("first string:"+Utils.filterStringNull(firstStringContent));

        //将字符串都放到ArrayList中
        int endStringIndex = 2+firstStringSize+2;
        while(stringContentList.size() < chunkStringCount){
            //一个字符对应两个字节，所以要乘以2
            int stringSize = Utils.byte2Short(Utils.copyByte(chunkStringContentByte, endStringIndex, 2))*2;
            String str = new String(Utils.copyByte(chunkStringContentByte, endStringIndex+2, stringSize+2));
            System.out.println("str:"+Utils.filterStringNull(str));
            stringContentList.add(Utils.filterStringNull(str));
            endStringIndex += (2+stringSize+2);
        }

    }

    private static void parseXmlHeader(byte[] byteSrc) {
        System.out.println("-----Xml header-----");
        byte[] xmlMagic = Utils.copyByte(byteSrc, 0, 4);
        System.out.println("magic:" + Utils.bytesToHexString(xmlMagic));
        byte[] xmlSize = Utils.copyByte(byteSrc, 4, 4);
        System.out.println("xml size:" + Utils.bytesToHexString(xmlSize));

        xmlSb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        xmlSb.append("\n");
    }
}
