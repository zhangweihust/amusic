package com.amusic.media.services.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.util.DisplayMetrics;

import com.amusic.media.MediaApplication;
import com.amusic.media.services.IExceptionService;

public class ExceptionService implements IExceptionService {
	
	private static DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss"); 
	//用来存储设备信息和异常信息
	private static  Map<String, String> infos = new HashMap<String, String>();
    /** 错误报告文件的扩展名 */  
	private static final String CRASH_REPORTER_EXTENSION = ".log";
	
	@Override
	public boolean start() {
		AMTException.getInstance().init();
		return true;
	}

	@Override
	public boolean stop() {
		return true;
	}

	private static class AMTException implements UncaughtExceptionHandler {

		private static AMTException instance;

		@Override
		public void uncaughtException(Thread thread, Throwable ex) {
			savePlayingMarks();
			collectDeviceInfo(ServiceManager.getAmtMedia());
			saveCrashInfo2File(ex);
			ServiceManager.getAmtMedia().startService(new Intent(ServiceManager.getAmtMedia(), CrashReportService.class));
			ServiceManager.exit();
		}

		public void savePlayingMarks(){
			Map<String, Integer> playingMarks = ServiceManager.getMediaplayerService().getPlayingMarks();
			SharedPreferences spf = MediaApplication.getContext().getSharedPreferences("Data",Context.MODE_WORLD_WRITEABLE);
			Editor editor = spf.edit();
			Iterator<Entry<String,Integer>> iterator = playingMarks.entrySet().iterator();
			Entry<String,Integer> entry = null;
			int i = 0;
			while (iterator.hasNext()) {
				entry = iterator.next();
				entry.getKey();
				editor.putString("playingMarks:"+i, entry.getKey()+":"+entry.getValue()).commit();
				i++;
			}
			editor.putInt("playingMarksLength", i).commit();
		}
		
		public void init() {
			Thread.setDefaultUncaughtExceptionHandler(this);
		}

		public static AMTException getInstance() {
			if (instance == null) {
				instance = new AMTException();
			}
			return instance;
		}
		
		
		/**
		 * 收集设备参数信息
		 * @param ctx
		 */
		public void collectDeviceInfo(Context ctx) {
			try {
				PackageManager pm = ctx.getPackageManager();
				PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
				if (pi != null) {
					String versionName = pi.versionName == null ? "null" : pi.versionName;
					String versionCode = pi.versionCode + "";
					infos.put("versionName", versionName);
					infos.put("versionCode", versionCode);
				}
				String versionSDK = Integer.valueOf(android.os.Build.VERSION.SDK).toString();
				String phoneModel = android.os.Build.MODEL;
				infos.put("versionSDK", versionSDK);
				infos.put("phoneModel", phoneModel);
			} catch (NameNotFoundException e) {
			}
			 DisplayMetrics dm=new DisplayMetrics();
			 ServiceManager.getAmtMedia().getWindowManager().getDefaultDisplay().getMetrics(dm);
			 float width = dm.widthPixels;
			 float height = dm.heightPixels;
			 float density = dm.densityDpi;
			 infos.put("width", Float.toString(width));
			 infos.put("height", Float.toString(height));
			 infos.put("density", Float.toString(density));
			 
		}
		

		/**
		 * 保存错误信息到文件中
		 * 
		 * @param ex
		 * @return	返回文件名称,便于将文件传送到服务器
		 */
		private String saveCrashInfo2File(Throwable ex) {
			StringBuffer sb = new StringBuffer();
			for (Map.Entry<String, String> entry : infos.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				sb.append(key + "=" + value + "\n");
			}
			
			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			ex.printStackTrace(printWriter);
			Throwable cause = ex.getCause();
			while (cause != null) {
				cause.printStackTrace(printWriter);
				cause = cause.getCause();
			}
			printWriter.close();
			String result = writer.toString();
			sb.append(result);
			try {
				long timestamp = System.currentTimeMillis();
				String time = formatter.format(new Date());
				String fileName = "crash-" + time + "-" + timestamp + CRASH_REPORTER_EXTENSION;
				if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
					String path = MediaApplication.crashPath;
					File dir = new File(path);
					if (!dir.exists()) {
						dir.mkdirs();
					}
					FileOutputStream fos = new FileOutputStream(path + fileName);
					fos.write(sb.toString().getBytes());
					fos.close();
				}
				return fileName;
			} catch (Exception e) {
			}
			return null;
		}

	}
		
}
