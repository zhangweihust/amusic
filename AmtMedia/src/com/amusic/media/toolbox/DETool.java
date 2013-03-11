package com.amusic.media.toolbox;

public class DETool {
	public static native String nativeGetElement(String name);

	public static native byte[] nativeGetKsc(String name);

	public static native byte[] nativeEncryptStr(String str);
	
	public static native byte[] nativeEncryptLongStr(String str);

	public static native String nativeDecryptStr(byte[] bt);
	
	public static native int nativeCreateKsc(String name); 
	
	public static native int nativeUncompressLrc(String name);
	
	public static native int nativeGetPic(String infile, String outfile);
	
}
