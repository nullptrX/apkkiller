package io.github.nullptrx.editor;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Utils {

    public static byte[] byteConcat(byte[] src, byte[] subB, int start) {
        System.arraycopy(subB, 0, src, start, subB.length);
        return src;
    }

    public static int byte2int(byte[] res) {
        int targets = (res[0] & 0xff) | ((res[1] << 8) & 0xff00)
                | ((res[2] << 24) >>> 8) | (res[3] << 24);
        return targets;
    }

    public static byte[] int2Byte(int value) {
        byte[] src = new byte[4];
        src[3] = (byte) ((value >> 24) & 0xFF);
        src[2] = (byte) ((value >> 16) & 0xFF);
        src[1] = (byte) ((value >> 8) & 0xFF);
        src[0] = (byte) (value & 0xFF);
        return src;
    }

    public static byte[] shortToByte(short number) {
        int temp = number;
        byte[] b = new byte[2];
        for (int i = 0; i < b.length; i++) {
            b[i] = Integer.valueOf(temp & 0xff).byteValue();//
            temp = temp >> 8; // 向右移8位 
        }
        return b;
    }

    public static short byte2Short(byte[] b) {
        short s = 0;
        short s0 = (short) (b[0] & 0xff);
        short s1 = (short) (b[1] & 0xff);
        s1 <<= 8;
        s = (short) (s0 | s1);
        return s;
    }

    public static String bytesToHexString(byte[] src1) {
        byte[] src = reverseBytes(src1);
        StringBuilder stringBuilder = new StringBuilder("");
        if (src.length <= 0) {
            return null;
        }
        for (byte b : src) {
            int v = b & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv).append(" ");
        }
        return stringBuilder.toString();
    }

    public static char[] getChars(byte[] bytes) {
        Charset cs = StandardCharsets.UTF_8;
        ByteBuffer bb = ByteBuffer.allocate(bytes.length);
        bb.put(bytes);
        bb.flip();
        CharBuffer cb = cs.decode(bb);
        return cb.array();
    }

    public static byte[] addByte(byte[] src, byte[] add) {
        if (src == null) {
            return null;
        }
        if (add == null) {
            return src;
        }
        byte[] newsrc = new byte[src.length + add.length];
        System.arraycopy(src, 0, newsrc, 0, src.length);
        if (newsrc.length - src.length >= 0)
            System.arraycopy(add, 0, newsrc, src.length, newsrc.length - src.length);
        return newsrc;
    }

    public static byte[] insertByte(byte[] src, int start, byte[] insertB) {
        if (src == null) {
            return null;
        }
        if (start > src.length) {
            return null;
        }
        byte[] newB = new byte[src.length + insertB.length];
        if (start >= 0) System.arraycopy(src, 0, newB, 0, start);
        System.arraycopy(insertB, 0, newB, start, insertB.length);
        System.arraycopy(src, start, newB, start + insertB.length, src.length - start);
        return newB;
    }

    public static byte[] removeByte(byte[] src, int start, int len) {
        if (src == null) {
            return null;
        }
        if (start > src.length) {
            return null;
        }
        if ((start + len) > src.length) {
            return null;
        }
        if (start < 0) {
            return null;
        }
        if (len <= 0) {
            return null;
        }
        byte[] dest = new byte[src.length - len];
        if (start + 1 >= 0) System.arraycopy(src, 0, dest, 0, start + 1);
        int k = 0;
        for (int i = (start + len); i < src.length; i++) {
            dest[start + k] = src[i];
            k++;
        }
        return dest;
    }

    public static byte[] copyByte(byte[] src, int start, int len) {
        if (src == null) {
            return null;
        }
        if (start > src.length) {
            return null;
        }
        if ((start + len) > src.length) {
            return null;
        }
        if (start < 0) {
            return null;
        }
        if (len <= 0) {
            return null;
        }
        byte[] resultByte = new byte[len];
        System.arraycopy(src, 0 + start, resultByte, 0, len);
        return resultByte;
    }

    public static byte[] replaceBytes(byte[] src, byte[] bytes, int start) {
        if (src == null) {
            return null;
        }
        if (bytes == null) {
            return src;
        }
        if (start > src.length) {
            return src;
        }
        if ((start + bytes.length) > src.length) {
            return src;
        }
        byte[] replaceB = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            replaceB[i] = src[i + start];
            src[i + start] = bytes[i];
        }
        return src;
    }

    public static byte[] reverseBytes(byte[] bytess) {
        byte[] bytes = new byte[bytess.length];
        System.arraycopy(bytess, 0, bytes, 0, bytess.length);
        if (bytes.length % 2 != 0) {
            return bytes;
        }
        int i = 0, len = bytes.length;
        while (i < (len / 2)) {
            byte tmp = bytes[i];
            bytes[i] = bytes[len - i - 1];
            bytes[len - i - 1] = tmp;
            i++;
        }
        return bytes;
    }

    public static String filterStringNull(String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        byte[] strByte = str.getBytes();
        ArrayList<Byte> newByte = new ArrayList<Byte>();
        for (byte b : strByte) {
            if (b != 0) {
                newByte.add(b);
            }
        }
        byte[] newByteAry = new byte[newByte.size()];
        for (int i = 0; i < newByteAry.length; i++) {
            newByteAry[i] = newByte.get(i);
        }
        return new String(newByteAry);
    }

    public static String getStringFromByteAry(byte[] srcByte, int start) {
        if (srcByte == null) {
            return "";
        }
        if (start < 0) {
            return "";
        }
        if (start >= srcByte.length) {
            return "";
        }
        byte val = srcByte[start];
        int i = 1;
        ArrayList<Byte> byteList = new ArrayList<Byte>();
        while (val != 0) {
            byteList.add(srcByte[start + i]);
            val = srcByte[start + i];
            i++;
        }
        byte[] valAry = new byte[byteList.size()];
        for (int j = 0; j < byteList.size(); j++) {
            valAry[j] = byteList.get(j);
        }
        try {
            return new String(valAry, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.out.println("encode error:" + e.toString());
            return "";
        }
    }

}
