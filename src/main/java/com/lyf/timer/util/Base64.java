package com.lyf.timer.util;

import sun.misc.BASE64Decoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;

public class Base64 {
	
	// 将 s 进行 BASE64 编码 
	public static String getBASE64(String s) {
		if (s == null) return null; 
		return (new sun.misc.BASE64Encoder()).encode( s.getBytes() ).replace("\n", "").replace("\r", "");
	} 
	
	// 将 s 进行 BASE64 编码 
	public static String getXBASE64(byte[] data) {
		if (data == null) return null; 
		return (new sun.misc.BASE64Encoder()).encode(data).replace("\n", "").replace("\r", "");
	} 
	 
	// 将 BASE64 编码的字符串 s 进行解码

    /**
     * 有错误 请使用apache commons
     * @param s
     * @return
     */
	public static String getFromBASE64(String s) {
		if (s == null) return null; 
		BASE64Decoder decoder = new BASE64Decoder();
		
		try { 
			byte[] b = decoder.decodeBuffer(s); 
			return new String(b);
		}
		catch (Exception e) {
			return null;
		}
	} 
	
	
	//解压
	public static byte[] decompress(byte[] data) throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		decompress(bais, baos);

		data = baos.toByteArray();

		baos.flush();
		baos.close();

		bais.close();

		return data;
	}

	
	//解压
	public static void decompress(InputStream is, OutputStream os)
			throws Exception {
		GZIPInputStream gis = new GZIPInputStream(is);

		byte[] data = new byte[1024];
		int count;
		while ((count = gis.read(data, 0, 1024)) != -1) {
			os.write(data, 0, count);
		}

		gis.close();
	}

	
	
}
