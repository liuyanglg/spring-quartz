package com.lyf.timer.util;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/** 
 * 编码工具类 
 * 1.将byte[]转为各种进制的字符串 
 * 7.AES加密 
 * 8.AES加密为base 64 code 
 * 9.AES解密 
 * 10.将base 64 code AES解密 
 * @author LiuDingchao 
 */

public class AESUtil {
	public static final String key0 = "455e3a9af9d01fe39368bfe4f3769b0bed92c3a92992f7f40c4265faf4799f62e9405bc80115d0cb3aedb52d88241a45b9e25142d8cb4a87bd1192ff567c89b5d8e13eeda02b2b6442d59667217b3aedbe9953b2cc3132093d882a3094934a381690c958281870a185fe21390af9865a007771d116e12a3254ccbfbc9511f76a9b82fd48e4";
	public static final String key1 = "R1cAUtcAVzBwDHgN";
	public static final String key2 = "8HGUkCAUtzBwDHgN";
	public static final String key3 = "QkiUtbsd1zBwDHgN";
	
	public static void main(String[] args) throws Exception {
        String content = "merchantTaxnumMake</>signid</>validator</>dateTime";
        String encrypt = aesEncryptC(content, key1);
        System.out.println("加密后：" + encrypt);
        String decrypt = aesDecryptC("a0607fd447ad6001439a83e90b87126cfef219c9c3e435fc1c5990c862fe472415a0ab93985b00ae922ae58458b8ea1b6968eac337f2384f74e261d9fdb91e1c55108a284a6f9ac55a628655949026dd437343a6d5d3548386f36d0bffdad0d7", key1);
        System.out.println("解密后：" + decrypt);
    }   
	
	  
    /** 
     * AES加密
     * @param content 待加密的内容 
     * @param encryptKey 加密密钥 
     * @return 加密后的base 64 code 
     * @throws Exception
     */  
    public static String aesEncrypt(String content, String encryptKey) throws Exception {
        return parseByte2HexStr(aesEncryptToBytes(content, encryptKey));  
    } 
    
    /** 
     * AES解密 
     * @param encryptStr 待解密的base 64 code 
     * @param decryptKey 解密密钥 
     * @return 解密后的string 
     * @throws Exception
     */  
    public static String aesDecrypt(String encryptStr, String decryptKey) throws Exception {
        return aesDecryptByBytes(parseHexStr2Byte(encryptStr), decryptKey);  
    }  
   
    /** 
     * AES加密 ECB加密
     * @param encryptStr 待加密的base 64 code 
     * @param decryptKey 加密密钥 
     * @return 加密后的string 
     * @throws Exception
     */  
    public static String aesEncryptC(String encryptStr, String decryptKey) throws Exception {
        return parseByte2HexStr(aesEncryptToBytesC(encryptStr, decryptKey));  
    } 
    
    /** 
     * AES解密 ECB解密
     * @param encryptStr 待解密的base 64 code 
     * @param decryptKey 解密密钥 
     * @return 解密后的string 
     * @throws Exception
     */  
    public static String aesDecryptC(String encryptStr, String decryptKey) throws Exception {
        return aesDecryptByBytesC(parseHexStr2Byte(encryptStr), decryptKey);  
    } 
    
    public static String aesDecryptC(String encryptStr, String decryptKey, String encode) throws Exception {
        return aesDecryptByBytesC(parseHexStr2Byte(encryptStr), decryptKey,encode);  
    }
   
    /** 
     * AES加密  
     * @param content 待加密的内容 
     * @param encryptKey 加密密钥 
     * @return 加密后的byte[] 
     * @throws Exception
     */  
    public static byte[] aesEncryptToBytes(String content, String encryptKey) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom random= SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(encryptKey.getBytes());
        kgen.init(128, random);
  
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(kgen.generateKey().getEncoded(), "AES"));
          
        return cipher.doFinal(content.getBytes("utf-8"));  
    }
    
    /**将二进制转换成16进制 
     * @param buf 
     * @return 
     */  
    public static String parseByte2HexStr(byte buf[]) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < buf.length; i++) {  
                    String hex = Integer.toHexString(buf[i] & 0xFF);
                    if (hex.length() == 1) {  
                            hex = '0' + hex;  
                    }  
                    sb.append(hex.toUpperCase());  
            }  
            return sb.toString();  
    }  
      
    /** 
     * AES解密 
     * @param encryptBytes 待解密的byte[] 
     * @param decryptKey 解密密钥 
     * @return 解密后的String 
     * @throws Exception
     */  
    public static String aesDecryptByBytes(byte[] encryptBytes, String decryptKey) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom random= SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(decryptKey.getBytes());
        kgen.init(128, random); 
          
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(kgen.generateKey().getEncoded(), "AES"));
        byte[] decryptBytes = cipher.doFinal(encryptBytes);  
          
        return new String(decryptBytes);
    }  
    
    /**将16进制转换成2进制 
     * @param buf 
     * @return 
     */
    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1)  
                return null;  
        byte[] result = new byte[hexStr.length()/2];  
        for (int i = 0;i< hexStr.length()/2; i++) {  
                int high = Integer.parseInt(hexStr.substring(i*2, i*2+1), 16);
                int low = Integer.parseInt(hexStr.substring(i*2+1, i*2+2), 16);
                result[i] = (byte) (high * 16 + low);  
        }  
        return result;  
}
    
    /** 
     * AES加密 ECB
     * @param content 待加密的内容 
     * @param encryptKey 加密密钥 
     * @return 加密后的byte[] 
     * @throws Exception
     */ 
    public static byte[] aesEncryptToBytesC(String content, String encryptKey) {
        try {
            Cipher aesECB = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec key = new SecretKeySpec(encryptKey.getBytes(), "AES");
            aesECB.init(Cipher.ENCRYPT_MODE, key);
            byte[] result = aesECB.doFinal(content.getBytes("UTF-8"));
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        return null;
    }

      
    /** 
     * AES解密ECB
     * @param encryptBytes 待解密的byte[] 
     * @param decryptKey 解密密钥 
     * @return 解密后的String 
     * @throws Exception
     */ 
    public static String aesDecryptByBytesC(byte[] encryptBytes, String decryptKey) {
    	return aesDecryptByBytesC(encryptBytes, decryptKey,null);
    }
    /** 
     * AES解密ECB,返回utf8格式
     * @param encryptBytes 待解密的byte[] 
     * @param decryptKey 解密密钥 
     * @return 解密后的String 
     * @throws Exception
     */ 
    public static String aesDecryptByBytesC(byte[] encryptBytes, String decryptKey, String encode){
    	try {
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");// 创建密码器
			SecretKeySpec key = new SecretKeySpec(decryptKey.getBytes(), "AES");
			cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
			//byte[] result = parseHexStr2Byte(content);
			if(encode==null){
				return new String(cipher.doFinal(encryptBytes)); // 解密
			}else{
				return new String(cipher.doFinal(encryptBytes),encode); // 解密
			}
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    return null;
    }
}
