package com.android.media.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MediaCategoryDatabaseHelper extends SQLiteOpenHelper {
	public MediaCategoryDatabaseHelper(Context context) {
		super(context, NAME, null, version);
	}

	public static final String NAME = "amt_media_category.db";
	public static final int version = 4;

	public static final String TAB_CATEGORY = "amt_media_category";
	public static final String COLUMN_CATEGORY_ID = "_id";
	public static final String COLUMN_CATEGORY_SINGER_PINYIN = "singer_pinyin";
	public static final String COLUMN_CATEGORY_SINGER = "singer";
	public static final String COLUMN_CATEGORY_SONG_PINYIN = "song_pinyin";
	public static final String COLUMN_CATEGORY_SONG = "song";
	public static final String COLUMN_CATEGORY_TYPE = "type";
	public static final String COLUMN_CATEGORY_FROM = "baidu_sogou";

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
