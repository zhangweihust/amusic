package com.android.media.provider;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.android.media.MediaApplication;

public class MediaPreferenceManager {
	public static Object getPreference(PreferenceType type) {
		Object value = null;
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MediaApplication.getContext());
		switch (type) {
		case LAST_PLAYING:
		case PLAY_MARK:
			value = preferences.getString(type.toString(), null);
			break;
		}
		return value;
	}

	public static void setPreference(PreferenceType type, Object value) {
		Editor editor = PreferenceManager.getDefaultSharedPreferences(MediaApplication.getContext()).edit();
		switch (type) {
		case LAST_PLAYING:
		case PLAY_MARK:
			editor.putString(type.toString(), (String) value);
			break;
		}
		editor.commit();
	}

	public static enum PreferenceType {
		LAST_PLAYING("last_playing"), PLAY_MARK("play_mark");
		private String key;

		private PreferenceType(String key) {
			this.key = key;
		}

		@Override
		public String toString() {
			return key;
		}
	}
}
