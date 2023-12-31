package cn.tongdun.plugin.utils;


import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Locale;
import java.util.Random;

public class StringEncryptionUtil {

    public static String decrypt(String ciphertext, int key1) {

        try {
            int length = ciphertext.length() / 2;
            char[] hexChars = ciphertext.toCharArray();
            byte[] stringByte = new byte[length];
            for (int i = 0; i < length; i++) {
                int pos = i * 2;
                stringByte[i] = (byte) ("0123456789abcdef".indexOf(hexChars[pos]) << 4 | "0123456789abcdef".indexOf(hexChars[pos + 1]));
            }
            byte keyXor = (byte) (key1 ^ 234);
            //System.out.println("解密前：" + Arrays.toString(stringByte));
            stringByte[0] = (byte) (stringByte[0] ^ 567);
            byte temp = stringByte[0]; //解密之后的数据
            for (int i = 1; i < stringByte.length; i++) {
                stringByte[i] = (byte) (stringByte[i] ^ temp ^ keyXor);
                temp = stringByte[i];
            }
            //System.out.println("解密后：" + Arrays.toString(stringByte));
            return new String(stringByte, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 字符串加密
     * String -> byte
     * 按byte加密，加密之后参与下一个字节的加密
     *
     * @param string 原始字符串
     * @param key1   key1
     * @param key2   key2
     * @param key3   key3
     * @return 加密后的字符串
     */
    public static String encryption(String string, int key1, int key2, int key3) {
        byte[] stringByte = string.getBytes(StandardCharsets.UTF_8);
        //System.out.println("string加密前：" + Arrays.toString(stringByte));
        byte keyXor = (byte) (key1 ^ key2);
        StringBuilder result = new StringBuilder();
        byte c = (byte) (stringByte[0] ^ key3);
        //System.out.println("string[0]加密后：" + c + " 转16进制" + String.format("%02x", c));
        result.append(String.format(Locale.US,"%02x", c));
        byte key4 = stringByte[0];
        for (int i = 1; i < stringByte.length; i++) {
            byte temp = stringByte[i];
            c = (byte) (stringByte[i] ^ key4 ^ keyXor);
            key4 = temp;
            //System.out.println("string[" + i + "]加密后：" + c + " 转16进制" + String.format("%02x", c));
            result.append(String.format(Locale.US,"%02x", c));
        }
        return result.toString();
    }

    public static int getRandomKey() {
        Random rand = new Random();
        return rand.nextInt(127);
    }

}
