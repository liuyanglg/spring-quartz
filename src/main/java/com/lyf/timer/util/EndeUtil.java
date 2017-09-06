package com.lyf.timer.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

public class EndeUtil {
	
    private KeyGenerator keygen;

    // SecretKey 负责保存对称密钥

    private SecretKey deskey;

    // Cipher负责完成加密或解密工作

    private Cipher c;

    // 该字节数组负责保存加密的结果

    private byte[] cipherByte;

	

    
    public void EndeUtil() throws NoSuchAlgorithmException, NoSuchPaddingException {
    	
    	        Security.addProvider(new com.sun.crypto.provider.SunJCE());
    	
    	        // 实例化支持DES算法的密钥生成器(算法名称命名需按规定，否则抛出异常)
    	
    	        keygen = KeyGenerator.getInstance("DES");
    	
    	        // 生成密钥
    	
    	        deskey = keygen.generateKey();
    	
    	        // 生成Cipher对象,指定其支持的DES算法
    	
    	      c = Cipher.getInstance("DES");
    	
    	    }
    	
    	 

	
	
	/**
	 * 将byte数组转换为表示16进制的字符串
	 * @param arrB 需要转换的byte数组
	 * @return 16进制表示的字符串
	 * @throws Exception
	 */
	
	public String byteArr2HexStr(byte[] arrB) throws Exception {
		int bLen = arrB.length;
		//每个字符占用两个字节，所以字符串的长度需是数组长度的2倍
		StringBuffer strBuffer = new StringBuffer(bLen*2);
		for(int i=0; i != bLen; ++i){
			int intTmp = arrB[i];
			//把负数转化为正数
			while(intTmp < 0){
				intTmp = intTmp + 256;//因为字一个字节是8位，从低往高数，第9位为符号为，加256，相当于在第九位加1
			}
			//小于0F的数据需要在前面补0，(因为原来是一个字节，现在变成String是两个字节，如果小于0F的话，说明最大也盛不满第一个字节。第二个需补充0)
			if(intTmp < 16){
				strBuffer.append("0");
			}
			strBuffer.append(Integer.toString(intTmp,16));
		}
		return strBuffer.toString();
	}
	
	
	/**
	 * 将表示16进制的字符串转化为byte数组
	 * @param hexStr
	 * @return
	 * @throws Exception
	 */
	public  byte[] hexStr2ByteArr(String hexStr) throws Exception {
		byte[] arrB = hexStr.getBytes();
		int bLen = arrB.length;
		byte[] arrOut = new byte[bLen/2];
		for(int i=0; i<bLen; i = i+2){
			String strTmp = new String(arrB,i,2);
			arrOut[i/2] = (byte) Integer.parseInt(strTmp,16);
		}
		return arrOut;
	}
	/*
	 * 
	 * 
	 * 
	 *  文件生产byte[]
	 */


	 public  byte[] getBytes(String filePath){
	        byte[] buffer = null;  
	        try {  
	            File file = new File(filePath);
	            FileInputStream fis = new FileInputStream(file);
	            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
	            byte[] b = new byte[1000];  
	            int n;  
	            while ((n = fis.read(b)) != -1) {  
	                bos.write(b, 0, n);  
	            }  
	            fis.close();  
	            bos.close();  
	            buffer = bos.toByteArray();  
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();  
	        } catch (IOException e) {
	            e.printStackTrace();  
	        }  
	        return buffer;  
	    }  
	  
	    /** 
	     * 根据byte数组，生成文件 
	     */  
	    public  void getFile(byte[] bfile, String filePath, String fileName) {
	        BufferedOutputStream bos = null;
	        FileOutputStream fos = null;
	        File file = null;
	        try {  
	            File dir = new File(filePath);
	            if(!dir.exists()&&dir.isDirectory()){//判断文件目录是否存在  
	                dir.mkdirs();  
	            }  
	            file = new File(filePath+"\\"+fileName);
	            fos = new FileOutputStream(file);
	            bos = new BufferedOutputStream(fos);
	            bos.write(bfile);  
	        } catch (Exception e) {
	            e.printStackTrace();  
	        } finally {  
	            if (bos != null) {  
	                try {  
	                    bos.close();  
	                } catch (IOException e1) {
	                    e1.printStackTrace();  
	                }  
	            }  
	            if (fos != null) {  
	                try {  
	                    fos.close();  
	                } catch (IOException e1) {
	                    e1.printStackTrace();  
	                }  
	            }  
	        }  
	    }
	
	
	    
	    
	    
	
	  public static void main(String[] args) throws Exception
	 
	    {
		  EndeUtil endetutil=new  EndeUtil();
		  String spath="E:\\config\\jdbcbatch2.properties";

		  byte[] bfile = endetutil.getBytes(spath);
		
		  String temp=new String(bfile).toString();
		  
		  System.out.println("初始数据 " +temp);
		  String tempja= endetutil.encrypt(temp);
	      
			
		  byte[] bfiles16=tempja.getBytes();
		  

		   endetutil.getFile(bfiles16,"E:\\config\\jami","jdbc(1922.168.10.30)Dserv.properties");
		 
		  
		  
		  
	    }
	  
	  
	  
	  public static String encrypt(String ssoToken)
      {  
        try  
        {  
          byte[] _ssoToken = ssoToken.getBytes("ISO-8859-1");  
          String name = new String();
         // char[] _ssoToken = ssoToken.toCharArray();  
          for (int i = 0; i < _ssoToken.length; i++) {  
              int asc = _ssoToken[i];  
              _ssoToken[i] = (byte) (asc + 27);  
              name = name + (asc + 27) + "%";  
          }  
          return name;  
        }catch(Exception e)
        {  
          e.printStackTrace() ;  
          return null;  
        }  
      }  
	  
	  
	  
	    public static String decrypt(String ssoToken)
	      {  
	        try  
	        {  
	          String name = new String();
	          java.util.StringTokenizer st=new java.util.StringTokenizer(ssoToken,"%");  
	          while (st.hasMoreElements()) {  
	            int asc =  Integer.parseInt((String)st.nextElement()) - 27;
	            name = name + (char)asc;  
	          }  
	  
	          return name;  
	        }catch(Exception e)
	        {  
	          e.printStackTrace() ;  
	          return null;  
	        }  
	      }  
	  
	  
	  
	    
	
}
	 
   
   



