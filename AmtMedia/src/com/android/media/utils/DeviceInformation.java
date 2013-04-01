package com.android.media.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

import com.android.media.MediaApplication;

public class DeviceInformation {
	public static String getInformation(InfoName infoName) {
		String value = "";
		String str1 = null;
		String str2 = null;
		String[] arrayOfString = null;
		FileReader fr = null;
		BufferedReader localBufferedReader = null;
		DisplayMetrics dm = MediaApplication.getContext().getResources().getDisplayMetrics();
		switch (infoName) {
		case IMEI:
			TelephonyManager tm = (TelephonyManager) MediaApplication.getContext().getSystemService(Context.TELEPHONY_SERVICE);
			value = tm.getDeviceId();
			break;
		case SYSTEM_VERSION:
			value = android.os.Build.VERSION.RELEASE;
			break;
		case PHONE_KTV_VERSION:
			try {
				PackageInfo packageInfo = MediaApplication.getContext().getPackageManager().getPackageInfo("com.amusic.media", PackageManager.GET_CONFIGURATIONS);
				value = packageInfo.versionName;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			break;
		case CPU_MODEL:
			str1 = "/proc/cpuinfo";
			try {
				fr = new FileReader(str1);
				localBufferedReader = new BufferedReader(fr, 8192);
				str2 = localBufferedReader.readLine();
				arrayOfString = str2.split("\\s+");
				for (int i = 2; i < arrayOfString.length; i++) {
					value += arrayOfString[i] + " ";
				}
			} catch (IOException e) {
			} finally {
				try {
					if (localBufferedReader != null) {
						localBufferedReader.close();
					}
					if (fr != null) {
						fr.close();
					}
				} catch (Exception e) {

				}
			}
			break;
		case CPU_MAX_FREQUENCY:
			ProcessBuilder cmd;
			try {
			String[] args = { "/system/bin/cat",
			"/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq" };
			cmd = new ProcessBuilder(args);
			Process process = cmd.start();
			InputStream in = process.getInputStream();
			byte[] re = new byte[24];
			while (in.read(re) != -1) {
				value = value + new String(re);
			}
			in.close();
			} catch (IOException ex) {
			ex.printStackTrace();
			value = "N/A";
			}
			value.trim();
			break;
		case MEMORY_TOTAL:
			str1 = "/proc/meminfo";
			try {
				fr = new FileReader(str1);
				localBufferedReader = new BufferedReader(fr);
				str2 = localBufferedReader.readLine();
				arrayOfString = str2.split(":");
				value = arrayOfString[1].trim();

			} catch (IOException e) {
			} finally {
				try {
					if (localBufferedReader != null) {
						localBufferedReader.close();
					}
					if (fr != null) {
						fr.close();
					}
				} catch (Exception e) {

				}
			}
			break;
		case SCREEN_RESOLUTION:
			value = String.valueOf(dm.widthPixels) + "*" + String.valueOf(dm.heightPixels);
			break;
		case SCREEN_DENSITYDPI:
			value = String.valueOf(dm.densityDpi);
			break;
		case PHONE_MODEL:
			value = android.os.Build.MODEL;
			break;
		}
		return value;
	}

	public enum InfoName {
		IMEI("imei"), SYSTEM_VERSION("osVersion"), PHONE_KTV_VERSION("softVersion"), CPU_MODEL("cpuModel"), CPU_MAX_FREQUENCY("cpuClk"), 
		MEMORY_TOTAL("memSize"), SCREEN_DENSITYDPI("windowDensityDpi"), SCREEN_RESOLUTION("windowSize"), PHONE_MODEL(
				"machModel");
		private String name;

		private InfoName(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}
}
