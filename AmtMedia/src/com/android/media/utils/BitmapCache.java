package com.android.media.utils;

import java.io.File;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Hashtable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * 
 * 该类用于图片缓存，防止内存溢出
 */
public class BitmapCache {
	static private BitmapCache cache;
	/** 用于Chche内容的存储 */
	private Hashtable<String, BtimapRef> bitmapRefs;
	/** 垃圾Reference的队列（所引用的对象已经被回收，则将该引用存入队列中） */
	private ReferenceQueue<Bitmap> q;

	
	
	public Hashtable<String, BtimapRef> getBitmapRefs() {
		return bitmapRefs;
	}

	public void setBitmapRefs(Hashtable<String, BtimapRef> bitmapRefs) {
		this.bitmapRefs = bitmapRefs;
	}

	/**
	 * 继承SoftReference，使得每一个实例都具有可识别的标识。
	 */
	private class BtimapRef extends SoftReference<Bitmap> {
		private String _key ;

		public BtimapRef(Bitmap bmp, ReferenceQueue<Bitmap> q, String path) {
			super(bmp, q);
			_key = path;
		}
	}

	private BitmapCache() {
		bitmapRefs = new Hashtable<String, BtimapRef>();
		q = new ReferenceQueue<Bitmap>();

	}

	/**
	 * 取得缓存器实例
	 */
	public static BitmapCache getInstance() {
		if (cache == null) {
			cache = new BitmapCache();
		}
		return cache;

	}

	/**
	 * 以软引用的方式对一个Bitmap对象的实例进行引用并保存该引用
	 */
	public void addCacheBitmap(Bitmap bmp, String path) {
		cleanCache();// 清除垃圾引用
		BtimapRef ref = new BtimapRef(bmp, q, path);
		bitmapRefs.put(path, ref);
	}

	/**
	 * 依据所指定的drawable下的图片资源ID号（可以根据自己的需要从网络或本地path下获取），重新获取相应Bitmap对象的实例
	 */
	public Bitmap getBitmap(String path) {
		Bitmap bmp = null;
	    BtimapRef ref = (BtimapRef) bitmapRefs.get(path);
	    bmp = (Bitmap) ref.get();
		return bmp;
	}
	
	
	  public static Bitmap decodeBitmap(String path)
	    {
	        BitmapFactory.Options options = new BitmapFactory.Options();
	        options.inJustDecodeBounds = true;
	        // 通过这个bitmap获取图片的宽和高&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;
	        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
/*	        if (bitmap == null)
	        {
	           // System.out.println("bitmap为空");
	        }*/
	        float realWidth = options.outWidth;
	        float realHeight = options.outHeight;
	       // System.out.println("真实图片高度：" + realHeight + "宽度:" + realWidth);
	        // 计算缩放比&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;
	        int scale = (int) ((realHeight > realWidth ? realHeight : realWidth) / 100);
	        if (scale <= 0)
	        {
	            scale = 1;
	        }
	        options.inSampleSize = scale;
	        options.inJustDecodeBounds = false;
	        // 注意这次要把options.inJustDecodeBounds 设为 false,这次图片是要读取出来的。&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;
	        bitmap = BitmapFactory.decodeFile(path, options);
	        if (bitmap == null)
	        {
	           // System.out.println("bitmap为空:" + path);
	            new File(path).delete();
	        } else {
	        	 int w = bitmap.getWidth();
	             int h = bitmap.getHeight();
	             //System.out.println("缩略图高度：" + h + "宽度:" + w);
	        }
	        return bitmap;
	    }
	

	private void cleanCache() {
		BtimapRef ref = null;
		while ((ref = (BtimapRef) q.poll()) != null) {
			bitmapRefs.remove(ref._key);
		}
	}

	// 清除Cache内的全部内容
	public void clearCache() {
		cleanCache();
		bitmapRefs.clear();
		System.gc();
		System.runFinalization();
	}

}