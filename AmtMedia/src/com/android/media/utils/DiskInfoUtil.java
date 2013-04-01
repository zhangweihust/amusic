package com.android.media.utils;

import java.io.File;

import android.os.Environment;
import android.os.StatFs;

public class DiskInfoUtil {
	private static DiskInfoUtil instance;
	
    private StatFs sdcardStatFs = null;
    
    private int sdcardblockSize = 0;
	
	private DiskInfoUtil(){
    	File sdcard = Environment.getExternalStorageDirectory();
    	sdcardStatFs = new StatFs(sdcard.getPath());
    	sdcardblockSize = sdcardStatFs.getBlockSize();
	}
	
	public static DiskInfoUtil getInstance(){
		if(instance==null){
			instance = new DiskInfoUtil();
		}
		return instance;
	}
	
	public int getSdcardAvailableSize() {
    	/**
    	 * statFs.getAvaliableBlocks方法可以返回尚未使用的block的数量
    	 */
    	int avaliableBlocks = sdcardStatFs.getAvailableBlocks();

    	return avaliableBlocks*sdcardblockSize;
    }
}