package com.aphrodite.smartboard.utils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by Aphrodite 2020/7/18
 */
public class ByteUtils {

    /**
     * 10进制转16进制数
     *
     * @param i
     * @return
     */
    public static String integerToHex(int i) {
        return Integer.toHexString(i);
    }

    /**
     * 按照高低位将16进制数转换成Byte数组
     *
     * @param i
     * @return
     */
    public static int[] integerToArray(int i) {
        int high = i >> 8;
        int low = i & 0xff;
        int[] array = {high, low};
        return array;
    }

    /**
     * 高低位两个byte转换成16进制数
     *
     * @param high
     * @param low
     * @return
     */
    public static int twoByteToHex(byte high, byte low) {
        short s = 0;
        s = (short) (s ^ high);  //将b1赋给s的低8位
        s = (short) (s << 8);  //s的低8位移动到高8位
        s = (short) (s ^ low); //在b2赋给s的低8位
        return Math.abs(s);
    }

    public static int byteToInteger(byte[] res) {
        DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(res));
        try {
            return dataInputStream.readUnsignedShort();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static byte[] unsigned_short_2byte(int length) {
        byte[] targets = new byte[2];
        for (int i = 0; i < 2; i++) {
            int offset = (targets.length - 1 - i) * 8;
            targets[i] = (byte) ((length >>> offset) & 0xff);
        }
        return targets;
    }
}
