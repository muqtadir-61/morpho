/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author muqtadir32903
 */


import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

class Utility {
  static byte[] hexStringToByteArray(String s) {
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2)
      data[i / 2] = 
        (byte)((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16)); 
    return data;
  }
  
  static String byteArrayToHexString(byte[] array) {
    StringBuilder hexString = new StringBuilder();
    for (byte b : array) {
      int intVal = b & 0xFF;
      if (intVal < 16)
        hexString.append("0"); 
      hexString.append(Integer.toHexString(intVal));
    } 
    return hexString.toString();
  }
  
  static byte[] decryptTemplate(byte[] cipherText, byte[] key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
    SecretKey secretKey = new SecretKeySpec(key, "DESede");
    IvParameterSpec iv = new IvParameterSpec(Arrays.copyOf(cipherText, 8));
    Cipher decipher = Cipher.getInstance("DESede/CBC/NoPadding");
    decipher.init(2, secretKey, iv);
    byte[] plainText = decipher.doFinal(Arrays.copyOfRange(cipherText, 8, cipherText.length));
    return plainText;
  }
  
  static byte[] encrypt(byte[] cipherText, byte[] key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
    SecretKey secretKey = new SecretKeySpec(key, "DESede");
    IvParameterSpec iv = new IvParameterSpec(Arrays.copyOf(cipherText, 8));
    Cipher decipher = Cipher.getInstance("DESede/CBC/NoPadding");
    decipher.init(1, secretKey, iv);
    byte[] plainText = decipher.doFinal(Arrays.copyOfRange(cipherText, 8, cipherText.length));
    return plainText;
  }
  
  static byte[] intToByteArray(int a) {
    byte[] ret = new byte[4];
    ret[3] = (byte)(a & 0xFF);
    ret[2] = (byte)(a >> 8 & 0xFF);
    ret[1] = (byte)(a >> 16 & 0xFF);
    ret[0] = (byte)(a >> 24 & 0xFF);
    return ret;
  }
  
  static String swapEndianness(String len) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i <= len.length() - 2; i += 2)
      result.append((new StringBuilder(len.substring(i, i + 2))).reverse()); 
    len = result.reverse().toString();
    return len;
  }
  
  static String onesComplement(String length) {
    long valL = Long.parseLong(length, 16);
    valL ^= 0xFFFFFFFFFFFFFFFFL;
    length = String.format("%08X", new Object[] { Long.valueOf(valL) });
    length = length.substring(length.length() - 8, length.length());
    return length;
  }
  
  static String convertHexToString(String hex) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < hex.length() - 1; i += 2) {
      String output = hex.substring(i, i + 2);
      int decimal = hexStringToInt(output);
      sb.append((char)decimal);
    } 
    return sb.toString();
  }
  
  static int hexStringToInt(String value) {
    return Integer.parseInt(value, 16);
  }
}

