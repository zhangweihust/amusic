package com.android.media.utils;

public class Constant {
	
	public static final class MenuConstant{
		public static final int userfull = 0;
		public static final int menu_item_scanner = 0;
		public static final int menu_item_skin = 1;
		public static final int menu_item_search = 2;
//		public static final int menu_item_delete = 2;
		public static final int menu_item_modify = 3;
		public static final int menu_item_song_problem = 4;
		public static final int menu_item_color = 5;
		public static final int menu_item_exit = 6;
		public static final int tools = 1;
		public static final int menu_item_timing = 0;
		public static final int menu_item_mode = 1;
		public static final int menu_item_download = 2;
		public static final int menu_item_equalizer = 3;
		public static final int menu_item_share = 4;
		public static final int menu_item_edit = 5;
		public static final int menu_item_setting = 6;
		public static final int help = 2;
		public static final int menu_item_version = 0;
		public static final int menu_item_suggestion = 1;
	}
	
	public static final class MediaWithServerConstant{
		public static final String request_parameter = "dbstr";
		public static final String request_top = "top";
		public static final String singer = "singer";
		public static final String song = "song";
		public static final String times = "times";
	}
	
	
	public static final class SoftParametersSetting{
		public static final String pictrue_download_key = "pictrue_download_value";
		public static final String lyric_download_key = "lyric_download_value";
		public static final String music_pause_controll_key = "music_pause_controll_value";
		public static final String lashing_controll_key = "lashing_controll_value";
		public static final String special_lashing_controll_key = "special_lashing_controll_value";
		public static final String sensitivity_lashing_controll_key = "sensitivity_lashing_controll_value";
		public static final String sensitivity_lashing_progress_key = "sensitivity_lashing_progress_value";
		public static final String desktop_lyric_key = "desktop_lyric_value";
		public static final String desktop_lyric_font_color_key = "desktop_lyric_font_color_value";
		public static final String skin_fontColor_key = "skin_fontColor_value";
		public static final String skin_foregroundColor_key = "skin_foregroundColor_value";
		public static final String skin_backgroundColor_key = "skin_backgroundColor_value";
		public static final String lyric_foregroundColor_key = "lyric_foregroundColor_value";
		public static final String lyric_backgroundColor_key = "lyric_backgroundColor_value";
		public static final String accompany_download_key = "accompany_download_value";
		public static final String start_record_key = "start_record_value";
		public static final String sound_sync_key = "sound_sync_value";
		public static final String sound_gain_control_key = "sound_gain_control_value";
		public static final String status_bar_key = "status_bar_value";
		public static final String auto_update_key = "auto_update_value";
		public static final String net_setting_key = "net_setting_value";
	}
	
	public static final int DEFAULT_SINGER_PICTURE = 0;
	public static final int DEFAULT_SINGER_ALBUM_PICTRUE = 1;
	public static boolean closeDesktopLyricFlag = false;
	public static int DESKTOP_LYRIC_WIDTH;
	public static int PICTRUE_DOWNLOAD = Integer.parseInt(PreferencesUtil.getPictrueSP());
	public static int LYRIC_DOWNLOAD = Integer.parseInt(PreferencesUtil.getLyricSP());
	public static boolean IS_MUSIC_PAUSE_CONTROLL = PreferencesUtil.getMusicPauseControllSP();
	public static boolean IS_LASHING_CONTROLL = PreferencesUtil.getlashingControllSP();
	public static boolean IS_SPECIAL_LASHING_CONTROLL = PreferencesUtil.getspecialLashingControllSP();
	public static boolean IS_SHOW_DESKTOP_LYRIC = PreferencesUtil.getDesktopLyricSP();
	public static boolean IS_MEMORY_DESKTOP_LYRIC_FONT_COLOR = PreferencesUtil.getDesktopLyricFontColorSP();
	public static boolean IS_WRITE_RECORD_DATA  = PreferencesUtil.getstartRecordSP();
	public static boolean IS_SOUND_SYCN = PreferencesUtil.getSoundSyncSP();
	public static boolean IS_SHOW_STATUS_BAR  = PreferencesUtil.getStatusBarSP();
	public static boolean IS_DOWNLOAD_ACCOMPANY = PreferencesUtil.getAccompanyDownloadSP();
	public static boolean IS_AUTO_UPDATE = PreferencesUtil.getAutoUpdateSP();
	public static int SPECIAL_LASHING_PROGRESS = PreferencesUtil.getsensitivityLashingProgressSP();
	public static int GAIN_CONTROL_PROGRESS = PreferencesUtil.getGainControlProgressSP();
	public static int FOREGROUNDCOLOR = PreferencesUtil.getSkinFontForegroundColorSP();
	public static int BACKGROUNDCOLOR = PreferencesUtil.getSkinFontBackgroundColorSP();
	public static int LYRICFOREGROUNDCOLOR = PreferencesUtil.getLyricFontForegroundColorSP();
	public static int LYRICBACKGROUNDCOLOR = PreferencesUtil.getLyricFontBackgroundColorSP();
	public static boolean IS_DESKTOP_LYRIC_EXIT = false;
	public static final int PROHIBITED_TO_DOWNLOAD_LYRIC = 1;
	public static final int ALLOWED_TO_DOWNLOAD_LYRIC = 2;
	public static final int ALLOWED_TO_DOWNLOAD_LYRIC_WITH_WIFI = 3;
	public static final int PROHIBITED_TO_DOWNLOAD_PICTURE = 1;
	public static final int ALLOWED_TO_DOWNLOAD_PICTURE = 2;
	public static final int ALLOWED_TO_DOWNLOAD_PICTURE_WITH_WIFI = 3;
	public static int WHICH_PLAYER;
	public static int WHICH_LYRIC_PLAYER;
	public static int KMEDIA_COUNT;
	public static int RECORD_COUNT;

}
