package com.amusic.media.screens.impl;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.view.View;

import com.amusic.media.R;
import com.amusic.media.dialog.GainControlSeekBarPreference;
import com.amusic.media.dialog.SkinFontColorPreference;
import com.amusic.media.dialog.SwingMusicSeekBarPreference;
import com.amusic.media.event.impl.MediaEventArgs;
import com.amusic.media.event.impl.MediaEventTypes;
import com.amusic.media.screens.IScreen;
import com.amusic.media.services.impl.NotificationService;
import com.amusic.media.services.impl.ServiceManager;
import com.amusic.media.utils.Constant;

public class SoftSetting extends PreferenceActivity implements IScreen,
	OnPreferenceChangeListener {
	private ListPreference pictrueSP;
	private ListPreference lyricSP;
	private CheckBoxPreference musicPauseControllSP;
	private CheckBoxPreference lashingControllSP;
	private CheckBoxPreference specialLashingControllSP;
	private SwingMusicSeekBarPreference sensitivityLashingControllSP;
	public static CheckBoxPreference desktopLyricSP;
	private CheckBoxPreference desktopLyricFontColorSP;
	private SkinFontColorPreference skinFontColorPreference;
	private CheckBoxPreference accompanyDownloadSP;
	private CheckBoxPreference startRecordSP;
	private CheckBoxPreference soundSyncSP;
	private GainControlSeekBarPreference gainControlSP;
//	private CheckBoxPreference statusBarSP;
	private CheckBoxPreference autoUpdateSP;
//	private Preference netSettingSP;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.software_parameters_setting);
		refresh();
		pictrueSP = (ListPreference) findPreference(Constant.SoftParametersSetting.pictrue_download_key);
		lyricSP = (ListPreference) findPreference(Constant.SoftParametersSetting.lyric_download_key);
		musicPauseControllSP = (CheckBoxPreference) findPreference(Constant.SoftParametersSetting.music_pause_controll_key);
		lashingControllSP = (CheckBoxPreference) findPreference(Constant.SoftParametersSetting.lashing_controll_key);
		specialLashingControllSP = (CheckBoxPreference) findPreference(Constant.SoftParametersSetting.special_lashing_controll_key);
		sensitivityLashingControllSP = (SwingMusicSeekBarPreference) findPreference(Constant.SoftParametersSetting.sensitivity_lashing_controll_key);
		desktopLyricSP = (CheckBoxPreference) findPreference(Constant.SoftParametersSetting.desktop_lyric_key);
		desktopLyricFontColorSP = (CheckBoxPreference) findPreference(Constant.SoftParametersSetting.desktop_lyric_font_color_key);
		skinFontColorPreference = (SkinFontColorPreference) findPreference(Constant.SoftParametersSetting.skin_fontColor_key);
		accompanyDownloadSP = (CheckBoxPreference) findPreference(Constant.SoftParametersSetting.accompany_download_key);
		startRecordSP = (CheckBoxPreference) findPreference(Constant.SoftParametersSetting.start_record_key);
		soundSyncSP = (CheckBoxPreference) findPreference(Constant.SoftParametersSetting.sound_sync_key);
		gainControlSP = (GainControlSeekBarPreference) findPreference(Constant.SoftParametersSetting.sound_gain_control_key);
//		statusBarSP = (CheckBoxPreference) findPreference(Constant.SoftParametersSetting.status_bar_key);
		autoUpdateSP = (CheckBoxPreference) findPreference(Constant.SoftParametersSetting.auto_update_key);
//		netSettingSP = (Preference) findPreference(Constant.SoftParametersSetting.net_setting_key);
		getListView().setBackgroundColor(Color.BLACK);
//		getListView().setCacheColorHint(Color.parseColor("#00000000"));
		pictrueSP.setOnPreferenceChangeListener(this);
		lyricSP.setOnPreferenceChangeListener(this);
		musicPauseControllSP.setOnPreferenceChangeListener(this);
		lashingControllSP.setOnPreferenceChangeListener(this);
		specialLashingControllSP.setOnPreferenceChangeListener(this);
		sensitivityLashingControllSP.setOnPreferenceChangeListener(this);
		desktopLyricSP.setOnPreferenceChangeListener(this);
		desktopLyricFontColorSP.setOnPreferenceChangeListener(this);
		skinFontColorPreference.setOnPreferenceChangeListener(this);
		accompanyDownloadSP.setOnPreferenceChangeListener(this);
		startRecordSP.setOnPreferenceChangeListener(this);
		soundSyncSP.setOnPreferenceChangeListener(this);
		gainControlSP.setOnPreferenceChangeListener(this);
//		statusBarSP.setOnPreferenceChangeListener(this);
		autoUpdateSP.setOnPreferenceChangeListener(this);
//		netSettingSP.setOnPreferenceChangeListener(this);
		
	}

	@Override
	public boolean hasMenu() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean currentable() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean changMenuAdapter() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean refresh() {
		// TODO Auto-generated method stub
		MediaEventArgs args = new MediaEventArgs();
		args.putExtra("screenTitle", getResources().getString(R.string.soft_parameters_setting));
		ServiceManager.getAmtMedia().getGoPlayerBtn().setVisibility(View.INVISIBLE);
		ServiceManager.getAmtMedia().getGoBackBtn().setVisibility(View.VISIBLE);
		ServiceManager.getMediaEventService().onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.SCREEN_TITLE_REFRESH));
		return true;
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		refresh();
		super.onNewIntent(intent);
	}

	@Override
	public boolean isMenuChanged() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(Constant.closeDesktopLyricFlag){
			desktopLyricSP.setChecked(false);
			Constant.closeDesktopLyricFlag = false;
		}
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		// TODO Auto-generated method stub
		String preferenceKey = preference.getKey();
		if (preferenceKey
				.equals(Constant.SoftParametersSetting.pictrue_download_key)) {
			Constant.PICTRUE_DOWNLOAD = Integer.parseInt(newValue.toString());
	
		} else if (preferenceKey
				.equals(Constant.SoftParametersSetting.lyric_download_key)) {
			Constant.LYRIC_DOWNLOAD = Integer.parseInt(newValue.toString());
		
		} else if (preferenceKey
				.equals(Constant.SoftParametersSetting.music_pause_controll_key)) {
			Constant.IS_MUSIC_PAUSE_CONTROLL = Boolean.parseBoolean(newValue.toString());

		}else if (preferenceKey
				.equals(Constant.SoftParametersSetting.lashing_controll_key)) {
			Constant.IS_LASHING_CONTROLL = Boolean.parseBoolean(newValue.toString());
			if(Constant.IS_LASHING_CONTROLL){
				ServiceManager.registerListener();
			}else{
				ServiceManager.unregisterListener();
			}

		}else if (preferenceKey
				.equals(Constant.SoftParametersSetting.special_lashing_controll_key)) {
			Constant.IS_SPECIAL_LASHING_CONTROLL = Boolean.parseBoolean(newValue.toString());

		}else if (preferenceKey
				.equals(Constant.SoftParametersSetting.sensitivity_lashing_controll_key)) {

		}else if (preferenceKey
				.equals(Constant.SoftParametersSetting.desktop_lyric_key)) {
			Constant.IS_SHOW_DESKTOP_LYRIC = Boolean.parseBoolean(newValue.toString());

		}else if (preferenceKey
				.equals(Constant.SoftParametersSetting.desktop_lyric_font_color_key)) {
			Constant.IS_MEMORY_DESKTOP_LYRIC_FONT_COLOR = Boolean.parseBoolean(newValue.toString());

		} else if (preferenceKey
				.equals(Constant.SoftParametersSetting.accompany_download_key)) {
			Constant.IS_DOWNLOAD_ACCOMPANY = Boolean.parseBoolean(newValue.toString());
		
		} else if (preferenceKey
				.equals(Constant.SoftParametersSetting.start_record_key)) {
			Constant.IS_WRITE_RECORD_DATA = Boolean.parseBoolean(newValue.toString());
		
		} else if (preferenceKey
				.equals(Constant.SoftParametersSetting.sound_sync_key)) {
			Constant.IS_SOUND_SYCN = Boolean.parseBoolean(newValue.toString());
		
		}else if (preferenceKey
				.equals(Constant.SoftParametersSetting.sound_gain_control_key)) {
		
		}
//		else if (preferenceKey
//				.equals(Constant.SoftParametersSetting.status_bar_key)) {
//			Constant.IS_SHOW_STATUS_BAR = Boolean.parseBoolean(newValue.toString());
//			if(Constant.IS_SHOW_STATUS_BAR){
//				((NotificationService)ServiceManager.getNotificatioservice()).showNotification();
//			}else{
//				((NotificationService)ServiceManager.getNotificatioservice()).dismissNotification();
//			}
		
//		} 
		else if (preferenceKey
				.equals(Constant.SoftParametersSetting.auto_update_key)) {
			Constant.IS_AUTO_UPDATE = Boolean.parseBoolean(newValue.toString());
			
		} else if (preferenceKey
				.equals(Constant.SoftParametersSetting.net_setting_key)) {

		}
		return true;
	}
	
	@Override
	public void onBackPressed() {
		getParent().onBackPressed();
	}

}
