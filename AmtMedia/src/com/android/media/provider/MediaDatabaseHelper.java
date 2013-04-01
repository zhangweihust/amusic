package com.android.media.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MediaDatabaseHelper extends SQLiteOpenHelper {
	public static final String NAME = "amt_media.db";
	private static final int version = 1;

	public static final String TAB_AUDIO = "amt_media_audio";

	public static final String COLUMN_AUDIO_ID = "_id";
	public static final String COLUMN_AUDIO_SYSTEM_ID = "system_id";
	public static final String COLUMN_AUDIO_PLAYLIST_ID = "playlist_id";

	private static final String CREATE_TABLE_AUDIO = " CREATE TABLE IF NOT EXISTS " + TAB_AUDIO + " ( " + COLUMN_AUDIO_ID + " INTEGER PRIMARY KEY, " + COLUMN_AUDIO_SYSTEM_ID + " LONG, "
			+ COLUMN_AUDIO_PLAYLIST_ID + " LONG " + " ); ";

	public static final String[] COLUMNS_AUDIO = { COLUMN_AUDIO_ID, COLUMN_AUDIO_SYSTEM_ID, COLUMN_AUDIO_PLAYLIST_ID };

	public static final String TAB_PLAYLIST = "amt_media_playlist";

	public static final String COLUMN_PLAYLIST_ID = "_id";
	public static final String COLUMN_PLAYLIST_NAME = "name";
	public static final String COLUMN_PLAYLIST_CREATE_TIME = "create_time";
	public static final String COLUMN_PLAYLIST_UPDATE_TIME = "update_time";

	private static final String CREATE_TABLE_PLAYLIST = " CREATE TABLE IF NOT EXISTS " + TAB_PLAYLIST + " ( " + COLUMN_PLAYLIST_ID + " INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL, " + COLUMN_PLAYLIST_NAME + " TEXT, "
			+ COLUMN_PLAYLIST_CREATE_TIME + " LONG, " + COLUMN_PLAYLIST_UPDATE_TIME + " LONG " + " ) ;";

	public static final String[] COLUMNS_PLAYLIST = { COLUMN_PLAYLIST_ID, COLUMN_PLAYLIST_NAME, COLUMN_PLAYLIST_CREATE_TIME, COLUMN_PLAYLIST_UPDATE_TIME };

	public static final String TAB_HISTORY = "amt_media_history";

	public static final String COLUMN_HISTORY_ID = "_id";
	public static final String COLUMN_HISTORY_MEDIA_ID = "media_id";
	public static final String COLUMN_HISTORY_PLAY_TIME = "play_time";
	public static final String COLUMN_HISTORY_MEDIA_TYPE = "media_type";
	public static final String COLUMN_HISTORY_UPDATE_TIME = "update_time";

	private static final String CREATE_TABLE_HISTORY = " CREATE TABLE IF NOT EXISTS " + TAB_HISTORY + " ( " + COLUMN_HISTORY_ID + " INTEGER PRIMARY KEY, " + COLUMN_HISTORY_MEDIA_ID + " LONG, "
			+ COLUMN_HISTORY_PLAY_TIME + " LONG, " + COLUMN_HISTORY_MEDIA_TYPE + " INTEGER DEFAULT '0', " + COLUMN_HISTORY_UPDATE_TIME + " LONG " + " ) ;";

	public static final String[] COLUMNS_HISTORY = { COLUMN_HISTORY_ID, COLUMN_HISTORY_MEDIA_ID, COLUMN_HISTORY_PLAY_TIME, COLUMN_HISTORY_MEDIA_TYPE, COLUMN_HISTORY_UPDATE_TIME };

	public static final String TAB_FAVORITY = "amt_media_favority";

	public static final String COLUMN_FAVORITY_ID = "_id";
	public static final String COLUMN_FAVORITY_MEDIA_ID = "media_id";
	public static final String COLUMN_FAVORITY_MEDIA_TYPE = "media_type";

	private static final String CREATE_TABLE_FAVORITY = " CREATE TABLE IF NOT EXISTS " + TAB_FAVORITY + " ( " + COLUMN_FAVORITY_ID + " INTEGER PRIMARY KEY, " + COLUMN_FAVORITY_MEDIA_ID + " LONG, "
			+ COLUMN_FAVORITY_MEDIA_TYPE + " INTEGER " + " ) ;";

	public static final int INDEX_FAVORITY_ID = 0;
	public static final int INDEX_FAVORITY_MEDIA_ID = 1;
	public static final int INDEX_FAVORITY_MEDIA_TYPE = 2;

	public static final String[] COLUMNS_FAVORITY = { COLUMN_FAVORITY_ID, COLUMN_FAVORITY_MEDIA_ID, COLUMN_FAVORITY_MEDIA_TYPE };

	public static final String TAB_DOWNLOAD = "amt_media_download";
	public static final String COLUMN_DOWNLOAD_ID_PREFIX = "d";
	public static final String COLUMN_DOWNLOAD_ID = "_id";
	public static final String COLUMN_DOWNLOAD_PATH = "path";
	public static final String COLUMN_DOWNLOAD_STATUS = "status";
	public static final String COLUMN_DOWNLOAD_ORDER = "download_order";
	public static final String COLUMN_DOWNLOAD_TOTAL_SIZE = "total_size";
	public static final String COLUMN_DOWNLOAD_CURRENT_SIZE = "download_size";
	public static final String COLUMN_DOWNLOAD_FINISH_DATE = "finish_date";
	public static final String COLUMN_DOWNLOAD_SONG_ID = "song_id";//字典里面歌曲的ID
	public static final String COLUMN_DOWNLOAD_URL = "url";
	public static final String COLUMN_DOWNLOAD_SONG_TYPE = "song_type";//字典的类型
	public static final String COLUMN_DOWNLOAD_RESOURCE = "resource";
	public static final String COLUMN_DOWNLOAD_TYPE = "type";//是伴奏还是原唱
	public static final String COLUMN_DOWNLOAD_SONG_NAME = "song";
	public static final String COLUMN_DOWNLOAD_SONG_SINGER = "singer";

	private static final String CREATE_TABLE_DOWNLOAD = " CREATE TABLE IF NOT EXISTS " + TAB_DOWNLOAD + " ( " + COLUMN_DOWNLOAD_ID + " INTEGER PRIMARY KEY, " + COLUMN_DOWNLOAD_PATH + " TEXT, "
			+ COLUMN_DOWNLOAD_STATUS + " INTEGER, " + COLUMN_DOWNLOAD_ORDER + " INTEGER, " + COLUMN_DOWNLOAD_TOTAL_SIZE + " LONG, " + COLUMN_DOWNLOAD_CURRENT_SIZE + " LONG, " + COLUMN_DOWNLOAD_FINISH_DATE + " LONG, " + COLUMN_DOWNLOAD_SONG_ID
			+ " INTEGER, " + COLUMN_DOWNLOAD_URL + " TEXT, " + COLUMN_DOWNLOAD_SONG_NAME + " TEXT, " + COLUMN_DOWNLOAD_SONG_SINGER + " TEXT, " + COLUMN_DOWNLOAD_SONG_TYPE + " INTEGER, " + COLUMN_DOWNLOAD_TYPE + " INTEGER, " + COLUMN_DOWNLOAD_RESOURCE + " INTEGER " + " ) ;";



	public static final int SONG_TYPE_DICTIONARY = 0;
	public static final int SONG_TYPE_CATEGORY = 1;
	public static final int DOWNLOAD_TYPE_ORIGINAL = 2;
	public static final int DOWNLOAD_TYPE_ACCOMPANY = 3;
	public static final int DOWNLOAD_TYPE_WEB = 4;

	public static final String TAB_COUNT_LOCAL = "amt_media_count_local";
	public static final String COLUMN_COUNT_LOCAL_ID = "_id";
	public static final String COLUMN_COUNT_LOCAL_SONG = "song";
	public static final String COLUMN_COUNT_LOCAL_SINGER = "singer";
	public static final String COLUMN_COUNT_LOCAL_COUNT = "count";

	private static final String CREATE_TABLE_COUNT_LOCAL = " CREATE TABLE IF NOT EXISTS " + TAB_COUNT_LOCAL + " ( " + COLUMN_COUNT_LOCAL_ID + " INTEGER PRIMARY KEY, " + COLUMN_COUNT_LOCAL_SONG
			+ " TEXT NOT NULL, " + COLUMN_COUNT_LOCAL_SINGER + " TEXT, " + COLUMN_COUNT_LOCAL_COUNT + " INTEGER " + " ) ";

	public static final String TAB_COUNT_KMEDIA = "amt_media_count_kmedia";
	public static final String COLUMN_COUNT_KMEDIA_ID = "_id";
	public static final String COLUMN_COUNT_KMEDIA_SONG = "song";
	public static final String COLUMN_COUNT_KMEDIA_SINGER = "singer";
	public static final String COLUMN_COUNT_KMEDIA_COUNT = "count";

	private static final String CREATE_TABLE_COUNT_KMEDIA = " CREATE TABLE IF NOT EXISTS " + TAB_COUNT_KMEDIA + " ( " + COLUMN_COUNT_KMEDIA_ID + " INTEGER PRIMARY KEY, " + COLUMN_COUNT_KMEDIA_SONG
			+ " TEXT NOT NULL, " + COLUMN_COUNT_KMEDIA_SINGER + " TEXT, " + COLUMN_COUNT_KMEDIA_COUNT + " INTEGER " + " ) ";

	public static final String TAB_COUNT_AUDIO_INFO_TASK = "amt_media_count_audio_info_task";
	public static final String COLUMN_COUNT_AUDIO_INFO_TASK_ID = "_id";
	public static final String COLUMN_COUNT_AUDIO_INFO_TASK_DATE = "date";
	public static final String COLUMN_COUNT_AUDIO_INFO_TASK_RESULT = "result";
	public static final String COLUMN_COUNT_AUDIO_INFO_TASK_DAY_TIMES = "day_times";

	private static final String CREATE_TABLE_COUNT_AUDIO_INFO_TASK = " CREATE TABLE IF NOT EXISTS  " + TAB_COUNT_AUDIO_INFO_TASK + " ( " + COLUMN_COUNT_AUDIO_INFO_TASK_ID + " INTEGER PRIMARY KEY, "
			+ COLUMN_COUNT_AUDIO_INFO_TASK_DATE + " INTEGER, " + COLUMN_COUNT_AUDIO_INFO_TASK_DAY_TIMES + " INTEGER, " + COLUMN_COUNT_AUDIO_INFO_TASK_RESULT + " INTEGER " + " ) ";

	public static final String TAB_COUNT_USER_ACTIVITY_INFO_TASK = "amt_media_count_user_info_activity";
	public static final String COLUMN_COUNT_USER_INFO_TACTIVITY_ID = "_id";
	public static final String COLUMN_COUNT_USER_INFO_ACTIVITY_DATE = "date";
	public static final String COLUMN_COUNT_USER_INFO_ACTIVITY_DAY_DATE = "day_date";
	public static final String COLUMN_COUNT_USER_INFO_ACTIVITY_RESULT = "result";
	public static final String COLUMN_COUNT_USER_INFO_ACTIVITY_TIMES = "times";
	public static final String COLUMN_COUNT_USER_INFO_ACTIVITY_CUMULATIVE_TIME = "cumulative_time";
	public static final String COLUMN_COUNT_USER_INFO_ACTIVITY_DAY_TIMES = "day_times";
	

	private static final String CREATE_TABLE_COUNT_USER_INFO_ACTIVITY = " CREATE TABLE IF NOT EXISTS " + TAB_COUNT_USER_ACTIVITY_INFO_TASK + " ( " + COLUMN_COUNT_USER_INFO_TACTIVITY_ID + " INTEGER PRIMARY KEY, "
			+ COLUMN_COUNT_USER_INFO_ACTIVITY_DATE + " INTEGER, "+ COLUMN_COUNT_USER_INFO_ACTIVITY_DAY_DATE + " INTEGER, " + COLUMN_COUNT_USER_INFO_ACTIVITY_RESULT + " INTEGER, " + COLUMN_COUNT_USER_INFO_ACTIVITY_TIMES + " INTEGER, "
			+ COLUMN_COUNT_USER_INFO_ACTIVITY_CUMULATIVE_TIME + " INTEGER, "+ COLUMN_COUNT_USER_INFO_ACTIVITY_DAY_TIMES + " INTEGER "+ " ) ";
	
	public static final String TAB_COUNT_USER_INFO_TASK = "amt_media_count_user_info_task";
	public static final String COLUMN_COUNT_USER_INFO_TASK_ID = "_id";
	public static final String COLUMN_COUNT_USER_INFO_TASK_TIMES = "times";
	public static final String COLUMN_COUNT_USER_INFO_TASK_DATE = "date";
	public static final String COLUMN_COUNT_USER_INFO_TASK_RESULT = "result";
	public static final String COLUMN_COUNT_USER_INFO_TASK_DAY_TIMES = "day_times";

	private static final String CREATE_TABLE_COUNT_USER_INFO_TASK = " CREATE TABLE IF NOT EXISTS " + TAB_COUNT_USER_INFO_TASK + " ( " + COLUMN_COUNT_USER_INFO_TASK_ID + " INTEGER PRIMARY KEY, "
			+ COLUMN_COUNT_USER_INFO_TASK_TIMES + " INTEGER, " + COLUMN_COUNT_USER_INFO_TASK_DATE + " INTEGER, " + COLUMN_COUNT_USER_INFO_TASK_DAY_TIMES + " INTEGER, "
			+ COLUMN_COUNT_USER_INFO_TASK_RESULT + " INTEGER " + " ) ";

	public static final String TAB_AMPLAY_SONGS = "amt_media_songs";
	public static final String COLUMN_AMPLAY_SONGS_ID = "_id";
	public static final String COLUMN_AMPLAY_SONGS_SID = "sid";
	public static final String COLUMN_AMPLAY_SONGS_DISPALYNAME = "display_name";
	public static final String COLUMN_AMPLAY_SONGS_ALBUMNAME = "albumname";
	public static final String COLUMN_AMPLAY_SONGS_ALBUMID = "albumid";
	public static final String COLUMN_AMPLAY_SONGS_ARTISTNAME = "artistname";
	public static final String COLUMN_AMPLAY_SONGS_SONGNAME = "songname";
	public static final String COLUMN_AMPLAY_SONGS_SIZE = "size";
	public static final String COLUMN_AMPLAY_SONGS_DATEADDED = "date_added";
	public static final String COLUMN_AMPLAY_SONGS_DURATION = "duration";
	public static final String COLUMN_AMPLAY_SONGS_EXTNAME = "extname";
	public static final String COLUMN_AMPLAY_SONGS_FILEPATH = "filepath";
	public static final String COLUMN_AMPLAY_SONGS_PARENTPATH = "parentpath";
	private static final String CRETAE_TABLE_AMPLAY_SONGS = " CREATE TABLE IF NOT EXISTS " + TAB_AMPLAY_SONGS + " ( " + COLUMN_AMPLAY_SONGS_ID + " INTEGER PRIMARY KEY , "
	+ COLUMN_AMPLAY_SONGS_SID + " INTEGER, " + COLUMN_AMPLAY_SONGS_DISPALYNAME + " TEXT, " + COLUMN_AMPLAY_SONGS_ALBUMNAME + " TEXT, " + COLUMN_AMPLAY_SONGS_SONGNAME + " TEXT, " + COLUMN_AMPLAY_SONGS_ALBUMID + " INTEGER, "
	+ COLUMN_AMPLAY_SONGS_ARTISTNAME + " TEXT, " + COLUMN_AMPLAY_SONGS_SIZE + " INTEGER, "+ COLUMN_AMPLAY_SONGS_DATEADDED + " INTEGER, " + COLUMN_AMPLAY_SONGS_DURATION + " INTEGER, " + COLUMN_AMPLAY_SONGS_EXTNAME + " TEXT, "
	+ COLUMN_AMPLAY_SONGS_FILEPATH + " TEXT, " + COLUMN_AMPLAY_SONGS_PARENTPATH + " TEXT " + ")";
	
	public static final String TAB_AMPLAY_SKINS = "amt_media_skins";
	public static final String COLUMN_AMPLAY_SKINS_ID = "_id";
	public static final String COLUMN_AMPLAY_SKINS_DISPALYNAME = "display_name";
	public static final String COLUMN_AMPLAY_SKINS_THUMBNAIL_FILENAME = "thumbnail_name";
	public static final String COLUMN_AMPLAY_SKINS_ALL_FILENAME = "all_name";
	private static final String CRETAE_TAB_AMPLAY_SKINS = " CREATE TABLE IF NOT EXISTS " + TAB_AMPLAY_SKINS + " ( " + COLUMN_AMPLAY_SKINS_ID + " INTEGER PRIMARY KEY , "
	+ COLUMN_AMPLAY_SKINS_DISPALYNAME + " TEXT, " + COLUMN_AMPLAY_SKINS_THUMBNAIL_FILENAME + " TEXT, " + COLUMN_AMPLAY_SKINS_ALL_FILENAME + " TEXT " + ")";
	
	
	public MediaDatabaseHelper(Context context) {
		super(context, NAME, null, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createTabs(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		createTabs(db);
	}

	private void createTabs(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_AUDIO);
		db.execSQL(CREATE_TABLE_PLAYLIST);
		db.execSQL(CREATE_TABLE_HISTORY);
		db.execSQL(CREATE_TABLE_FAVORITY);
		db.execSQL(CREATE_TABLE_DOWNLOAD);
		db.execSQL(CREATE_TABLE_COUNT_KMEDIA);
		db.execSQL(CREATE_TABLE_COUNT_LOCAL);
		db.execSQL(CREATE_TABLE_COUNT_AUDIO_INFO_TASK);
		db.execSQL(CREATE_TABLE_COUNT_USER_INFO_TASK);
		db.execSQL(CREATE_TABLE_COUNT_USER_INFO_ACTIVITY);
		db.execSQL(CRETAE_TABLE_AMPLAY_SONGS);
		db.execSQL(CRETAE_TAB_AMPLAY_SKINS);
	}

}
