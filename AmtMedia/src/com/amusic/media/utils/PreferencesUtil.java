package com.amusic.media.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.amusic.media.MediaApplication;

public class PreferencesUtil {
	public static String getPictrueSP(){
		 return PreferenceManager.getDefaultSharedPreferences(MediaApplication.getContext()).getString(Constant.SoftParametersSetting.pictrue_download_key, "2");
	}
	public static String getLyricSP(){
		return PreferenceManager.getDefaultSharedPreferences(MediaApplication.getContext()).getString(Constant.SoftParametersSetting.lyric_download_key, "2");
	}
	public static boolean getMusicPauseControllSP(){
		return PreferenceManager.getDefaultSharedPreferences(MediaApplication.getContext()).getBoolean(Constant.SoftParametersSetting.music_pause_controll_key, true);
	}
	public static boolean getlashingControllSP(){
		return PreferenceManager.getDefaultSharedPreferences(MediaApplication.getContext()).getBoolean(Constant.SoftParametersSetting.lashing_controll_key, false);
	}
	public static boolean getspecialLashingControllSP(){
		return PreferenceManager.getDefaultSharedPreferences(MediaApplication.getContext()).getBoolean(Constant.SoftParametersSetting.special_lashing_controll_key, false);
	}
	public static int getsensitivityLashingProgressSP(){
		return PreferenceManager.getDefaultSharedPreferences(MediaApplication.getContext()).getInt(Constant.SoftParametersSetting.sensitivity_lashing_progress_key, 60);
	}
	public static int getGainControlProgressSP(){
		return PreferenceManager.getDefaultSharedPreferences(MediaApplication.getContext()).getInt(Constant.SoftParametersSetting.sound_gain_control_key, 60);
	}
	public static int getSkinFontForegroundColorSP(){
		return PreferenceManager.getDefaultSharedPreferences(MediaApplication.getContext()).getInt(Constant.SoftParametersSetting.skin_foregroundColor_key, -16721665);
	}
	public static int getSkinFontBackgroundColorSP(){
		return PreferenceManager.getDefaultSharedPreferences(MediaApplication.getContext()).getInt(Constant.SoftParametersSetting.skin_backgroundColor_key, -16777216);
	}
	
	public static int getLyricFontForegroundColorSP(){
		return PreferenceManager.getDefaultSharedPreferences(MediaApplication.getContext()).getInt(Constant.SoftParametersSetting.lyric_foregroundColor_key, MediaApplication.color_highlight);
	}
	public static int getLyricFontBackgroundColorSP(){
		return PreferenceManager.getDefaultSharedPreferences(MediaApplication.getContext()).getInt(Constant.SoftParametersSetting.lyric_backgroundColor_key, 0xFFADE5E6);
	}
	public static void setSkinFontForegroundColorSP(int foregroundColor){
		SharedPreferences sp = MediaApplication.getContext().getSharedPreferences("com.amusic.media_preferences",Context.MODE_WORLD_WRITEABLE);
		Editor editor = sp.edit();
		editor.putInt(Constant.SoftParametersSetting.skin_foregroundColor_key, foregroundColor);
		editor.commit();
	}
	public static void setSkinFontBackgroundColorSP(int backgroundColor){
		SharedPreferences sp = MediaApplication.getContext().getSharedPreferences("com.amusic.media_preferences",Context.MODE_WORLD_WRITEABLE);
		Editor editor = sp.edit();
		editor.putInt(Constant.SoftParametersSetting.skin_backgroundColor_key, backgroundColor);
		editor.commit();
	}
	public static boolean getDesktopLyricSP(){
		return PreferenceManager.getDefaultSharedPreferences(MediaApplication.getContext()).getBoolean(Constant.SoftParametersSetting.desktop_lyric_key, false);
	}
	public static boolean getDesktopLyricFontColorSP(){
		return PreferenceManager.getDefaultSharedPreferences(MediaApplication.getContext()).getBoolean(Constant.SoftParametersSetting.desktop_lyric_font_color_key, true);
	}
	public static boolean getAccompanyDownloadSP(){
		return PreferenceManager.getDefaultSharedPreferences(MediaApplication.getContext()).getBoolean(Constant.SoftParametersSetting.accompany_download_key, true);
	}
	public static boolean getstartRecordSP(){
		return PreferenceManager.getDefaultSharedPreferences(MediaApplication.getContext()).getBoolean(Constant.SoftParametersSetting.start_record_key, true);
	}
	public static boolean getSoundSyncSP(){
		return PreferenceManager.getDefaultSharedPreferences(MediaApplication.getContext()).getBoolean(Constant.SoftParametersSetting.sound_sync_key, false);
	}
	public static boolean getStatusBarSP(){
		return PreferenceManager.getDefaultSharedPreferences(MediaApplication.getContext()).getBoolean(Constant.SoftParametersSetting.status_bar_key, true);
	}
	public static boolean getAutoUpdateSP(){
		return PreferenceManager.getDefaultSharedPreferences(MediaApplication.getContext()).getBoolean(Constant.SoftParametersSetting.auto_update_key, true);
	}
}
