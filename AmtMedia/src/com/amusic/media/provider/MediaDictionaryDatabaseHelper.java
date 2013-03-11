package com.amusic.media.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MediaDictionaryDatabaseHelper extends SQLiteOpenHelper {
	public static final String NAME = "amt_media_dictionary.db";
	public static final int version = 4;

	public static final String TAB_DICTIONARY = "amt_media_dictionary";
	public static final String COLUMN_DICTIONARY_ID = "_id";
	public static final String COLUMN_DICTIONARY_SINGER_PINYIN = "singer_pinyin";
	public static final String COLUMN_DICTIONARY_SINGER = "singer";
	public static final String COLUMN_DICTIONARY_SONG_PINYIN = "song_pinyin";
	public static final String COLUMN_DICTIONARY_SONG = "song";
	public static final String COLUMN_DICTIONARY_FROM = "baidu_sogou";

	public MediaDictionaryDatabaseHelper(Context context) {
		super(context, NAME, null, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
}
