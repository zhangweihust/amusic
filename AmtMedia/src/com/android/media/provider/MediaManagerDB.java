package com.android.media.provider;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.provider.MediaStore;

import com.android.media.MediaApplication;
import com.android.media.event.IMediaEventArgs;
import com.android.media.event.impl.MediaEventArgs;
import com.android.media.event.impl.MediaEventTypes;
import com.android.media.model.SongInfo;
import com.android.media.screens.AudioScreen;
import com.android.media.screens.impl.ScreenAudio;
import com.android.media.screens.impl.ScreenAudioSongs;
import com.android.media.services.IMediaService;
import com.android.media.services.impl.MediaPlayerService;
import com.android.media.services.impl.ServiceManager;
import com.android.media.toolbox.DETool;

public class MediaManagerDB {
	private Context context;
	private ContentResolver contentResolver;
	private MediaDatabaseHelper mediaDatabaseHelper;
	private MediaDictionaryDatabaseHelper dictionaryDatabaseHelper;
	private MediaCategoryDatabaseHelper categoryDatabaseHelper;
	private SQLiteDatabase mediaDatabase;
	private SQLiteDatabase dictionaryDatabase;
	private SQLiteDatabase categoryDatabase;
	private ContentValues contentValues = new ContentValues();
	private static IMediaEventArgs AudioSongFreshArgs = new MediaEventArgs();
	
	public MediaManagerDB(Context context) {
		this.context = context;
		this.contentResolver = context.getContentResolver();
	}

	public void open() throws SQLiteException {
		mediaDatabaseHelper = new MediaDatabaseHelper(context);
		mediaDatabase = mediaDatabaseHelper.getWritableDatabase();
		dictionaryDatabaseHelper = new MediaDictionaryDatabaseHelper(context);
		dictionaryDatabase = dictionaryDatabaseHelper.getWritableDatabase();
		categoryDatabaseHelper = new MediaCategoryDatabaseHelper(context);
		categoryDatabase = categoryDatabaseHelper.getWritableDatabase();
	}

	public void close() {
		mediaDatabaseHelper.close();
		dictionaryDatabaseHelper.close();
		categoryDatabaseHelper.close();
	}

	public void addDownloadAudioToSongs(ContentValues values) {
		mediaDatabase.insert(MediaDatabaseHelper.TAB_AMPLAY_SONGS, null, values);
		ScreenAudio.refreshCount(ScreenAudio.REFRESH_SONGS_COUNT);
		ServiceManager.getMediaEventService().onMediaUpdateEvent(AudioSongFreshArgs
    			.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_UPDATE_AUDIO_SONGS));		
	}

	
	public void updateDonwloadAudioToSongs(final String path, int tryNum) {
		try{
		MediaApplication.logD(MediaManagerDB.class, "updateDonwloadAudioToSongs:" + path);
			    Cursor c = queryAudioByData(path);
			    if (c != null && c.getCount() != 0){
			    	c.moveToFirst();
			    	contentValues.clear();
			    	contentValues.put(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ID, c.getInt(c.getColumnIndex(MediaStore.Audio.Media._ID)));
			    	contentValues.put(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_SID, c.getInt(c.getColumnIndex(MediaStore.Audio.Media._ID)));
			    	contentValues.put(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_DISPALYNAME, c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)));
			    	contentValues.put(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ALBUMNAME, c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)));
			    	contentValues.put(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ALBUMID, c.getInt(c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)));
			    	contentValues.put(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ARTISTNAME, c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)));
			    	contentValues.put(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_SIZE, c.getInt(c.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)));
			    	contentValues.put(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_DURATION, c.getInt(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)));
					String filepath = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
					contentValues.put(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_EXTNAME, filepath.substring(filepath.lastIndexOf(".") + 1));
					contentValues.put(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_FILEPATH, filepath);
					contentValues.put(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_PARENTPATH, filepath.substring(0,filepath.lastIndexOf("/") + 1));
					contentValues.put(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_SONGNAME, c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)));
					contentValues.put(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_DATEADDED, c.getInt(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)));
					mediaDatabase.update(MediaDatabaseHelper.TAB_AMPLAY_SONGS, contentValues, MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_FILEPATH
							+ " =? ", new String[] { path });
					MediaApplication.logD(MediaManagerDB.class, "updateDonwloadAudioToSongs:" +c.getInt(c.getColumnIndex(MediaStore.Audio.Media._ID)));
					c.close();
					ScreenAudio.refreshCount(ScreenAudio.REFRESH_SONGS_COUNT);
			    } else {
			    	if(tryNum == 1) {
			    		MediaApplication.logD(MediaManagerDB.class, "updateDonwloadAudioToSongs later");
				    	ServiceManager.getAmtMediaHandler().postDelayed(new Runnable(){
							@Override
							public void run() {
								updateDonwloadAudioToSongs(path, 2);
							}}, 6000);
			    	}
			    }
		}catch(Exception e){
			
		}
	}
	
	
	public Cursor queryAudios() {
		Cursor cursor = mediaDatabase.query(MediaDatabaseHelper.TAB_AMPLAY_SONGS, 
				null,
				null,
				null, 
				null, 
				null, 
				null);
		return cursor;
	}
	
	public Cursor queryAudioByData(String filePath){
		return contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Audio.Media.DATA + " =? ",
				new String[] { filePath }, null);
	}
	
	public Cursor queryAudioByPath(String filePath){
		return mediaDatabase.query(MediaDatabaseHelper.TAB_AMPLAY_SONGS, null, MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_FILEPATH + " =? ",
				new String[] { filePath }, null,null,null);
	}
	
	public Cursor queryAccompany(String where) {
		return mediaDatabase.query(MediaDatabaseHelper.TAB_AMPLAY_SONGS, null, where, null, null,null,null);
	}
	
	public Cursor queryAccompanys(String songName, String singer) {
		String filePath;
		if (singer.equals("")) {
			filePath = MediaApplication.savePath +  songName + IMediaService.AUDIO_SUFFIX;
		} else {
			filePath = MediaApplication.savePath + singer + "-" + songName + IMediaService.AUDIO_SUFFIX;
		}
		return mediaDatabase.query(MediaDatabaseHelper.TAB_AMPLAY_SONGS, null, MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_FILEPATH
				+ " =? ", new String[] { filePath }, null,null,null);
	}
	public boolean accompanyInDownloads(int  songId){
		Cursor c = mediaDatabase.query(MediaDatabaseHelper.TAB_DOWNLOAD, null, MediaDatabaseHelper.COLUMN_DOWNLOAD_SONG_ID
				+ " =? AND " + MediaDatabaseHelper.COLUMN_DOWNLOAD_TYPE + "=?",
				new String[] { String.valueOf(songId),  String.valueOf(MediaDatabaseHelper.DOWNLOAD_TYPE_ACCOMPANY)}, null,null,null);
		boolean exist = c.getCount() != 0 ? true : false;
		c.close();
		return exist;
	}

	public Cursor querySongById(int id) {
		//return mediaDatabase.rawQuery("select * from amt_media_songs where _id = ?", new String[]{ String.valueOf(id) });
		return mediaDatabase.query(MediaDatabaseHelper.TAB_AMPLAY_SONGS, null, MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ID
				+ " =? ", new String[] { String.valueOf(id) }, null, null, null);
	}
	
	public boolean updateSongById(int id, String new_name,String new_artist,String new_album){
		boolean ret;
		ContentValues values = new ContentValues();
		
		values.put(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_SONGNAME, new_name);
		values.put(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ARTISTNAME, new_artist);
		values.put(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ALBUMNAME, new_album);
		ret =  mediaDatabase.update(MediaDatabaseHelper.TAB_AMPLAY_SONGS, values, MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ID + " =? ",
				new String[] { String.valueOf(id) }) > 0;
				
		values.clear();
		values.put(MediaStore.Audio.Media.TITLE, new_name);
		values.put(MediaStore.Audio.Media.ARTIST, new_artist);
		values.put(MediaStore.Audio.Media.ALBUM, new_album);
		
		ret |= contentResolver.update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values, MediaStore.Audio.Media._ID + " =? ",
				new String[] { String.valueOf(id) }) > 0;
				
		return ret;
	}

	public Cursor queryPlaylists() {
		return mediaDatabase.query(MediaDatabaseHelper.TAB_PLAYLIST, MediaDatabaseHelper.COLUMNS_PLAYLIST, null, null, null, null,
				MediaDatabaseHelper.COLUMN_PLAYLIST_NAME);
	}

	public int queryPlaylists(int id){
		int playlistId = 0;
		Cursor cursor = mediaDatabase.query(MediaDatabaseHelper.TAB_PLAYLIST, MediaDatabaseHelper.COLUMNS_PLAYLIST, null, null, null, null,
				MediaDatabaseHelper.COLUMN_PLAYLIST_CREATE_TIME);
		if(cursor.moveToLast()){
			playlistId = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_PLAYLIST_ID));
		}
		cursor.close();
		return playlistId;
	}
	
	public Cursor queryPlaylistAudios(int id) {
		Cursor cursor = mediaDatabase.query(MediaDatabaseHelper.TAB_AUDIO, new String[] { MediaDatabaseHelper.COLUMN_AUDIO_SYSTEM_ID },
				MediaDatabaseHelper.COLUMN_AUDIO_PLAYLIST_ID + " =? ", new String[] { String.valueOf(id) }, null, null, null);
		int count = cursor.getCount();
		String where = null;
		if (count == 0) {
			where = " 1=2 ";
		} else if (count == 1) {
			if (cursor.moveToNext()) {
				where = MediaStore.Audio.Media._ID + "=" + String.valueOf(cursor.getLong(0));
			}
		} else if (count > 1) {
			StringBuffer whereStr = new StringBuffer();
			if (cursor.moveToFirst()) {
				whereStr.append(String.valueOf(cursor.getLong(0)));
				while (cursor.moveToNext()) {
					whereStr.append(" , " + String.valueOf(cursor.getLong(0)));
				}
				where = MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ID + " IN " + " ( " + whereStr.toString() + " ) ";
			}
		}
		where = where + " AND " + MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_DISPALYNAME + " like '%.mp3'";
		if (cursor != null) {
			cursor.close();
		}
		return mediaDatabase.query(MediaDatabaseHelper.TAB_AMPLAY_SONGS, null, where, null, null, null, null);
	}

	public Cursor querySkins(int pageID) {
		MediaApplication.logD(MediaManagerDB.class, "pageID:" + pageID);
		String sql= "select * from " + MediaDatabaseHelper.TAB_AMPLAY_SKINS +   
		         " Limit "+String.valueOf(24)+ " Offset " + String.valueOf(pageID); 
		return mediaDatabase.rawQuery(sql, null); 
			//return mediaDatabase.query(MediaDatabaseHelper.TAB_AMPLAY_SKINS, null, null, null, null, null, null);
		}
	
	public Cursor querySkins() {
			return mediaDatabase.query(MediaDatabaseHelper.TAB_AMPLAY_SKINS, null, null, null, null, null, null);
		}
	
	public boolean insertSkins(ContentValues values) {
		return mediaDatabase.insert(MediaDatabaseHelper.TAB_AMPLAY_SKINS, null, values) > 0;
	}

	
	public Cursor querySingers() {
	//	return mediaDatabase.rawQuery("select * from amt_media_songs group by artistname", null); 
		return mediaDatabase.query(MediaDatabaseHelper.TAB_AMPLAY_SONGS, null, null, null, MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ARTISTNAME, null, null);
	}

	public Cursor querySingerAudios(String artistName) {
		//return mediaDatabase.rawQuery("select * from amt_media_songs where artistname = ?", new String[]{artistName});
		return mediaDatabase.query(MediaDatabaseHelper.TAB_AMPLAY_SONGS, null, MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ARTISTNAME
				+ " =? ", new String[] { artistName }, null, null, null);
	}
	
	public int deleteSingerAudios(int id){
		return contentResolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.Audio.Media.ARTIST_ID
				+ " =? ", new String[] { String.valueOf(id) });
	}

	public Cursor queryAlbums() {
		return mediaDatabase.query(MediaDatabaseHelper.TAB_AMPLAY_SONGS, null, null, null, MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ALBUMNAME, null, null);
	}

	public Cursor queryAlbumAudios(String  albumName) {
		//return mediaDatabase.rawQuery("select * from amt_media_songs where albumname = ?", new String[]{albumName});
		return mediaDatabase.query(MediaDatabaseHelper.TAB_AMPLAY_SONGS, null, MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ALBUMNAME
				+ " =? ", new String[] { albumName }, null, null, null);
	}

	public Cursor queryFavoriteAudios() {
		Cursor cursor = mediaDatabase.query(MediaDatabaseHelper.TAB_FAVORITY, MediaDatabaseHelper.COLUMNS_FAVORITY, null, null, null, null, null);
		int count = cursor.getCount();
		String where = null;
		if (count == 0) {
			where = " 1=2 ";
		} else if (count == 1) {
			if (cursor.moveToNext()) {
				where = MediaStore.Audio.Media._ID + " = " + String.valueOf(cursor.getLong(MediaDatabaseHelper.INDEX_FAVORITY_MEDIA_ID));
			}
		} else if (count > 1) {
			StringBuffer whereStr = new StringBuffer();
			if (cursor.moveToFirst()) {
				whereStr.append(String.valueOf(cursor.getLong(MediaDatabaseHelper.INDEX_FAVORITY_MEDIA_ID)));
			}
			while (cursor.moveToNext()) {
				whereStr.append(" , " + String.valueOf(cursor.getLong(MediaDatabaseHelper.INDEX_FAVORITY_MEDIA_ID)));
			}
			where = MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ID + " IN " + " ( " + whereStr + " ) ";
		}
		where = where + " AND " + MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_DISPALYNAME + " like '%.mp3'";
		if (cursor != null) {
			cursor.close();
		}
		return mediaDatabase.query(MediaDatabaseHelper.TAB_AMPLAY_SONGS, null, where, null, null, null, null);
	}

	public Cursor queryRecentlyAudios() {
		return  mediaDatabase.query(MediaDatabaseHelper.TAB_AMPLAY_SONGS,
				null,
				null,
				new String[] { "0", "10" }, 
				null,
				null,
				MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_DATEADDED + " DESC " + " LIMIT ? , ? ");
	}
   public Cursor queryRecentlyAudiosById(){
	   return  mediaDatabase.query(MediaDatabaseHelper.TAB_AMPLAY_SONGS,
				null,
				null,
				new String[] { "0", "10" }, 
				null,
				null,
				MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ID + " DESC " + " LIMIT ? , ? ");
	   	   
   }
	public void deleteAudio(int id) {
	    mediaDatabase.delete(MediaDatabaseHelper.TAB_AMPLAY_SONGS, MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ID + "=?",
						new String[] { String.valueOf(id) });
	    ScreenAudio.refreshCount(ScreenAudio.REFRESH_SONGS_COUNT);
	}
	
	public void deleteAudio(String filePath) {
	    mediaDatabase.delete(MediaDatabaseHelper.TAB_AMPLAY_SONGS, MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_FILEPATH + "=?",
						new String[] { filePath });
	    ScreenAudio.refreshCount(ScreenAudio.REFRESH_SONGS_COUNT);
	}


	public boolean addAudioToFavorite(ContentValues values) {
		return mediaDatabase.insert(MediaDatabaseHelper.TAB_FAVORITY, null, values) > 0;
	}
	

	public boolean addPlaylist(ContentValues values) {
		String name = (String) values.get(MediaDatabaseHelper.COLUMN_PLAYLIST_NAME);
		Cursor cursor = mediaDatabase.query(MediaDatabaseHelper.TAB_PLAYLIST, new String[] { MediaDatabaseHelper.COLUMN_PLAYLIST_ID },
				MediaDatabaseHelper.COLUMN_PLAYLIST_NAME + " =? ", new String[] { name }, null, null, null);
		boolean exist = false;
		if (cursor.getCount() > 0) {
			exist = true;
		}
		cursor.close();
		if (exist) {
			return false;
		}
		return mediaDatabase.insert(MediaDatabaseHelper.TAB_PLAYLIST, null, values) > 0;
	}

	public boolean addAudioToPlaylist(ContentValues values) {
		Cursor cursor = mediaDatabase.query(MediaDatabaseHelper.TAB_AUDIO,
				null,
				MediaDatabaseHelper.COLUMN_AUDIO_SYSTEM_ID + "=? AND " + MediaDatabaseHelper.COLUMN_AUDIO_PLAYLIST_ID + "=?",
				new String[]{values.getAsString(MediaDatabaseHelper.COLUMN_AUDIO_SYSTEM_ID),values.getAsString(MediaDatabaseHelper.COLUMN_AUDIO_PLAYLIST_ID)}, 
				null,
				null,
				null);
		int cout = cursor.getCount();
		cursor.close();
		if (cout > 0)
		{
			return false;
		}
		return mediaDatabase.insert(MediaDatabaseHelper.TAB_AUDIO, null, values) > 0;
	}

	public boolean deleteAudioFromPlaylist(int id, int playlistId) {
		return mediaDatabase.delete(MediaDatabaseHelper.TAB_AUDIO, MediaDatabaseHelper.COLUMN_AUDIO_SYSTEM_ID + " =? " + " AND "
				+ MediaDatabaseHelper.COLUMN_AUDIO_PLAYLIST_ID + " =? ", new String[] { String.valueOf(id), String.valueOf(playlistId) }) > 0;
	}

	public boolean deleteAudioFromFavority(int id) {
		return mediaDatabase.delete(MediaDatabaseHelper.TAB_FAVORITY, MediaDatabaseHelper.COLUMN_FAVORITY_MEDIA_ID + " =? ",
				new String[] { String.valueOf(id) }) > 0;
	}

	public long addToDownload(ContentValues values) {
		values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_ORDER, getDownloadMaxOrder());
		return mediaDatabase.insert(MediaDatabaseHelper.TAB_DOWNLOAD, null, values);
	}

	public boolean inDownload(Integer id) {
		Cursor cursor = mediaDatabase.query(MediaDatabaseHelper.TAB_DOWNLOAD, new String[] { MediaDatabaseHelper.COLUMN_DOWNLOAD_ID },
				MediaDatabaseHelper.COLUMN_DOWNLOAD_ID + " =? ", new String[] { String.valueOf(id) }, null, null, null);
		boolean result = false;
		if (cursor.getCount() > 0) {
			result = true;
		}
		cursor.close();
		return result;
	}
	
	public boolean inDownload(String singer, String song) {
		Cursor cursor = mediaDatabase.query(MediaDatabaseHelper.TAB_DOWNLOAD, new String[] { MediaDatabaseHelper.COLUMN_DOWNLOAD_ID },
				MediaDatabaseHelper.COLUMN_DOWNLOAD_SONG_NAME + " =? " + " AND  " + MediaDatabaseHelper.COLUMN_DOWNLOAD_SONG_SINGER + " =? "
				+ " AND  " + MediaDatabaseHelper.COLUMN_DOWNLOAD_STATUS + " !=? ", 
				new String[] { singer, song , String.valueOf(IMediaService.STATE_FINISHED) }, null, null, null);
		boolean result = false;
		if (cursor.getCount() > 0) {
			result = true;
		}
		cursor.close();
		return result;
	}


	private int getDownloadMaxOrder() {
		Cursor cursor = mediaDatabase.query(MediaDatabaseHelper.TAB_DOWNLOAD, new String[] { MediaDatabaseHelper.COLUMN_DOWNLOAD_SONG_ID,
				MediaDatabaseHelper.COLUMN_DOWNLOAD_ORDER }, null, null, null, null, MediaDatabaseHelper.COLUMN_DOWNLOAD_ORDER + " desc "
				+ " limit 0 , 1 ");
		int order = 0;
		if (cursor.moveToNext()) {
			order = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_ORDER)) + 1;
		}
		cursor.close();
		return order;
	}

	private int getDownloadMinOrder(int id) {
		Cursor cursor = mediaDatabase.query(MediaDatabaseHelper.TAB_DOWNLOAD, new String[] { MediaDatabaseHelper.COLUMN_DOWNLOAD_SONG_ID,
				MediaDatabaseHelper.COLUMN_DOWNLOAD_ORDER }, null, null, null, null, MediaDatabaseHelper.COLUMN_DOWNLOAD_ORDER + " limit 0 , 1 ");
		int order = 0;
		if (cursor.moveToNext()) {
			if (cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_ID)) != id) {
				order = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_ORDER)) - 1;
			} else {
				order = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_ORDER));
			}
		}
		cursor.close();
		return order;
	}

	public boolean clearAllFinishedFromDownload() {
		mediaDatabase.delete(MediaDatabaseHelper.TAB_DOWNLOAD, MediaDatabaseHelper.COLUMN_DOWNLOAD_STATUS + " =? ",
				new String[] { String.valueOf(1) });
		return true;
	}

	public boolean cancelDownload(int id) {
		return mediaDatabase.delete(MediaDatabaseHelper.TAB_DOWNLOAD, MediaDatabaseHelper.COLUMN_DOWNLOAD_ID + " =? ",
				new String[] { String.valueOf(id) }) > 0;
	}

	public boolean updateDownloadAudio(int id, ContentValues values) {
		return mediaDatabase.update(MediaDatabaseHelper.TAB_DOWNLOAD, values, MediaDatabaseHelper.COLUMN_DOWNLOAD_ID + " =? ",
				new String[] { String.valueOf(id) }) > 0;
	}

	public boolean updateDownloadToTop(int id) {
		int order = getDownloadMinOrder(id);
		ContentValues values = new ContentValues();
		values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_ORDER, order);
		return mediaDatabase.update(MediaDatabaseHelper.TAB_DOWNLOAD, values, MediaDatabaseHelper.COLUMN_DOWNLOAD_ID + " =? ",
				new String[] { String.valueOf(id) }) > 0;
	}

	public boolean deletePlaylist(int id) {
		if (mediaDatabase.delete(MediaDatabaseHelper.TAB_PLAYLIST, MediaDatabaseHelper.COLUMN_PLAYLIST_ID + " =? ",
				new String[] { String.valueOf(id) }) > 0) {
			mediaDatabase.delete(MediaDatabaseHelper.TAB_AUDIO, MediaDatabaseHelper.COLUMN_AUDIO_PLAYLIST_ID + " =? ",
					new String[] { String.valueOf(id) });
			return true;
		}
		return false;
	}

	public Cursor queryDictionarySingers(String pinyin, int start, int length) {

		if (pinyin == null || pinyin.length() == 0) {
			return new MatrixCursor(new String[] {
					MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_ID,
					MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SINGER });
		}
		pinyin = pinyin.replace("?", "_");
		return dictionaryDatabase
				.query(MediaDictionaryDatabaseHelper.TAB_DICTIONARY,
						new String[] {
								MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_ID,
								MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SINGER },
						MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SINGER_PINYIN
								+ " LIKE ? ",
						new String[] { pinyin + "%", String.valueOf(start),
								String.valueOf(length) },
						MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SINGER,
						null,
						MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SINGER_PINYIN
								+ " limit ? , ? ");

	}

	public int queryDictionarySingersCount(String pinyin) {
		Cursor cursor = null;
		int count = 0;
		if (pinyin == null || pinyin.length() == 0) {
			return 0;
		}
		pinyin = pinyin.replace("?", "_");
		cursor = dictionaryDatabase
				.query(MediaDictionaryDatabaseHelper.TAB_DICTIONARY,
						new String[] { MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SINGER },
						MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SINGER_PINYIN
								+ " LIKE ? ", new String[] { pinyin + "%" },
						MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SINGER,
						null, null);
		count = cursor.getCount();
		cursor.close();
		return count;
	}

	public Cursor queryDictionaryAudios(String pinyin, int start, int length) {
		MatrixCursor matrixCursor = new MatrixCursor(new String[] { MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_ID,
				MediaDatabaseHelper.COLUMN_DOWNLOAD_ID_PREFIX + MediaDatabaseHelper.COLUMN_DOWNLOAD_ID, MediaDatabaseHelper.COLUMN_DOWNLOAD_STATUS,
				MediaDatabaseHelper.COLUMN_DOWNLOAD_TOTAL_SIZE, MediaDatabaseHelper.COLUMN_DOWNLOAD_PATH, MediaDatabaseHelper.COLUMN_DOWNLOAD_URL,
				MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SONG, MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SINGER, MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_FROM,
				MediaDatabaseHelper.COLUMN_DOWNLOAD_TYPE});
		Cursor dictionary = null;
		if (pinyin == null || pinyin.length() == 0) {
			return matrixCursor;
		} else {
			pinyin = pinyin.replace("?", "_");
			dictionary = dictionaryDatabase.query(MediaDictionaryDatabaseHelper.TAB_DICTIONARY, new String[] {
					MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_ID, MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SONG,
					MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SINGER, MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_FROM}, MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SONG_PINYIN
					+ " LIKE ? ", new String[] { pinyin + "%", String.valueOf(start), String.valueOf(length) }, null, null,
					MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SONG_PINYIN + " limit ? , ? ");
		}
		Cursor media;
		int songId;
		int downloadId;
		int downloadStatus;
		int downloadType;
		int downloadSize;
		String downloadPath;
		String downloadUrl;
		String dictionarySong;
		String dictionarySinger;
		String dictionaryFrom;
		while (dictionary.moveToNext()) {
			songId = dictionary.getInt(dictionary.getColumnIndex(MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_ID));
			media = mediaDatabase.query(MediaDatabaseHelper.TAB_DOWNLOAD, null, MediaDatabaseHelper.COLUMN_DOWNLOAD_SONG_ID + "= ? ",
					new String[] { String.valueOf(songId) }, null, null, MediaDatabaseHelper.COLUMN_DOWNLOAD_ORDER);
			if (media != null && media.getCount() == 2) {// 在下载表里面找到了对应的歌曲记录，一条是原唱的下载，一条是伴奏的下载。
				Map<Integer, Integer> compareMap = new HashMap<Integer, Integer>();
				for (media.moveToFirst(); !media.isAfterLast(); media.moveToNext()) {
					downloadPath = media.getString(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_PATH));
					downloadType = media.getInt(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_TYPE));
					downloadStatus = media.getInt(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_STATUS));
//					MediaApplication.logD(MediaManagerDB.class, "downloadType:" + downloadType + " downloadStatus:" + downloadStatus);
//					MediaApplication.logD(MediaManagerDB.class, "downloadPath:" + downloadPath);
					compareMap.put(downloadType, downloadStatus);
				}
				if (compareMap.get(MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL) == IMediaService.STATE_FINISHED
						&& compareMap.get(MediaDatabaseHelper.DOWNLOAD_TYPE_ACCOMPANY) == IMediaService.STATE_WAIT) {
					// 原唱下载完成但是伴奏还没有开始下载，显示原唱已经下载完成
					media.moveToFirst();
//					MediaApplication.logD(MediaManagerDB.class, "原唱下载完成但是伴奏还没有开始下载，显示原唱已经下载完成");
				} else if (compareMap.get(MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL) != IMediaService.STATE_FINISHED) {
					// 原唱没有下载完成，显示原唱的进度
					media.moveToFirst();
//					MediaApplication.logD(MediaManagerDB.class, "原唱没有下载完成，显示原唱的进度");
				} else if (compareMap.get(MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL) == IMediaService.STATE_FINISHED
						&& compareMap.get(MediaDatabaseHelper.DOWNLOAD_TYPE_ACCOMPANY) != IMediaService.STATE_WAIT) {
					// 伴奏开始下载，显示伴奏的进度
					media.moveToLast();
//					MediaApplication.logD(MediaManagerDB.class, "伴奏开始下载，显示伴奏的进度");
				}
				downloadType = media.getInt(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_TYPE));
				downloadStatus = media.getInt(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_STATUS));
//				MediaApplication.logD(MediaManagerDB.class, "被显示出来的是: download type:" + downloadType + " downloadStatus:" + downloadStatus);
				downloadId = media.getInt(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_ID));
				downloadSize = media.getInt(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_TOTAL_SIZE));
				downloadPath = media.getString(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_PATH));
				downloadUrl = media.getString(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_URL));
				dictionarySong = DETool.nativeDecryptStr(dictionary.getBlob(dictionary.getColumnIndex(MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SONG)));
				dictionarySinger = dictionary.getString(dictionary.getColumnIndex(MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SINGER));
				dictionaryFrom = dictionary.getString(dictionary.getColumnIndex(MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_FROM));
				matrixCursor.addRow(new Object[] { songId, downloadId, downloadStatus, downloadSize, downloadPath, downloadUrl, dictionarySong,
						dictionarySinger, dictionaryFrom, downloadType });
			} else if (media != null && media.getCount() == 1) {// 原唱已经在本地存在，但在下载表里面没有，所以下载表只有伴奏一条记录
				media.moveToFirst();
//				MediaApplication.logD(MediaManagerDB.class, "原唱已经在本地存在，但在下载表里面没有，所以下载表只有伴奏一条记录");
				downloadType = media.getInt(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_TYPE));
				downloadStatus = media.getInt(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_STATUS));
				downloadId = media.getInt(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_ID));
				downloadSize = media.getInt(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_TOTAL_SIZE));
				downloadPath = media.getString(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_PATH));
				downloadUrl = media.getString(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_URL));
				dictionarySong = DETool.nativeDecryptStr(dictionary.getBlob(dictionary.getColumnIndex(MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SONG)));
				dictionarySinger = dictionary.getString(dictionary.getColumnIndex(MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SINGER));
				dictionaryFrom = dictionary.getString(dictionary.getColumnIndex(MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_FROM));
				matrixCursor.addRow(new Object[] { songId, downloadId, downloadStatus, downloadSize, downloadPath, downloadUrl, dictionarySong,
						dictionarySinger, dictionaryFrom, downloadType });
			} else {// 没有在下载表里面找到对应的歌曲记录。
				dictionarySong = DETool.nativeDecryptStr(dictionary.getBlob(dictionary.getColumnIndex(MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SONG)));
				dictionarySinger = dictionary.getString(dictionary.getColumnIndex(MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SINGER));
				dictionaryFrom = dictionary.getString(dictionary.getColumnIndex(MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_FROM));
				matrixCursor.addRow(new Object[] { songId, null, IMediaService.STATE_DEFAULT, null, null, null, dictionarySong, dictionarySinger, dictionaryFrom, null });
			}
			media.close();
		}
		dictionary.close();
		return matrixCursor;
	}

	public int queryDictionaryAudiosCount(String pinyin) {
		if (pinyin == null || pinyin.length() == 0) {
			return 0;
		}
		pinyin = pinyin.replace("?", "_");
		Cursor cursor = dictionaryDatabase.query(MediaDictionaryDatabaseHelper.TAB_DICTIONARY,
				new String[] { MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SONG }, MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SONG_PINYIN
						+ " LIKE ? ", new String[] { pinyin + "%" }, null, null, null);
		int count = cursor.getCount();
		cursor.close();
		return count;
	}

	public Cursor queryDictionarySingerAudios(String singer, String pinyin, int start, int length) {
		MatrixCursor matrixCursor = new MatrixCursor(new String[] { MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_ID,
				MediaDatabaseHelper.COLUMN_DOWNLOAD_ID_PREFIX + MediaDatabaseHelper.COLUMN_DOWNLOAD_ID, MediaDatabaseHelper.COLUMN_DOWNLOAD_STATUS,
				MediaDatabaseHelper.COLUMN_DOWNLOAD_TOTAL_SIZE, MediaDatabaseHelper.COLUMN_DOWNLOAD_PATH, MediaDatabaseHelper.COLUMN_DOWNLOAD_URL,
				MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SONG, MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SINGER, MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_FROM,
				MediaDatabaseHelper.COLUMN_DOWNLOAD_TYPE  });
		Cursor dictionary;
		if (pinyin == null || pinyin.length() == 0) {
			dictionary = dictionaryDatabase
					.query(MediaDictionaryDatabaseHelper.TAB_DICTIONARY,
							new String[] {
									MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_ID,
									MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SONG,
									MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SINGER,
									MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_FROM},
							MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SINGER
									+ " = ? ",
							new String[] { singer, String.valueOf(start),
									String.valueOf(length) },
							null,
							null,
							MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SONG_PINYIN
									+ " limit ? , ? ");
		} else {
			pinyin = pinyin.replace("?", "_");
			dictionary = dictionaryDatabase.query(MediaDictionaryDatabaseHelper.TAB_DICTIONARY, new String[] {
					MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_ID, MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SONG,
					MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SINGER, MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_FROM }, MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SONG_PINYIN
					+ " LIKE  ? " + " AND  " + MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SINGER + " = ? ", new String[] { pinyin + "%",
					singer, String.valueOf(start), String.valueOf(length) }, null, null,
					MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SONG_PINYIN + " limit ? , ? ");
		}
		Cursor media;
		int songId;
		int downloadId;
		int downloadStatus;
		int downloadSize;
		int downloadType;
		String downloadPath;
		String downloadUrl;
		String dictionarySong;
		String dictionarySinger;
		String dictionaryFrom;
		while (dictionary.moveToNext()) {
			songId = dictionary.getInt(dictionary.getColumnIndex(MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_ID));
			media = mediaDatabase.query(MediaDatabaseHelper.TAB_DOWNLOAD, null, MediaDatabaseHelper.COLUMN_DOWNLOAD_SONG_ID + "= ? ",
					new String[] { String.valueOf(songId) }, null, null, MediaDatabaseHelper.COLUMN_DOWNLOAD_ORDER);
			if (media != null && media.getCount() == 2) {// 在下载表里面找到了对应的歌曲记录，一条是原唱的下载，一条是伴奏的下载。
				Map<Integer, Integer> compareMap = new HashMap<Integer, Integer>();
				for (media.moveToFirst(); !media.isAfterLast(); media.moveToNext()) {
					downloadPath = media.getString(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_PATH));
					downloadType = media.getInt(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_TYPE));
					downloadStatus = media.getInt(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_STATUS));
//					MediaApplication.logD(MediaManagerDB.class, "downloadType:" + downloadType + " downloadStatus:" + downloadStatus);
//					MediaApplication.logD(MediaManagerDB.class, "downloadPath:" + downloadPath);
					compareMap.put(downloadType, downloadStatus);
				}
				if (compareMap.get(MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL) == IMediaService.STATE_FINISHED
						&& compareMap.get(MediaDatabaseHelper.DOWNLOAD_TYPE_ACCOMPANY) == IMediaService.STATE_WAIT) {
					// 原唱下载完成但是伴奏还没有开始下载，显示原唱已经下载完成
					media.moveToFirst();
//					MediaApplication.logD(MediaManagerDB.class, "原唱下载完成但是伴奏还没有开始下载，显示原唱已经下载完成");
				} else if (compareMap.get(MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL) != IMediaService.STATE_FINISHED) {
					// 原唱没有下载完成，显示原唱的进度
					media.moveToFirst();
//					MediaApplication.logD(MediaManagerDB.class, "原唱没有下载完成，显示原唱的进度");
				} else if (compareMap.get(MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL) == IMediaService.STATE_FINISHED
						&& compareMap.get(MediaDatabaseHelper.DOWNLOAD_TYPE_ACCOMPANY) != IMediaService.STATE_WAIT) {
					// 伴奏开始下载，显示伴奏的进度
					media.moveToLast();
//					MediaApplication.logD(MediaManagerDB.class, "伴奏开始下载，显示伴奏的进度");
				}
				downloadType = media.getInt(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_TYPE));
				downloadStatus = media.getInt(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_STATUS));
//				MediaApplication.logD(MediaManagerDB.class, "被显示出来的是: download type:" + downloadType + " downloadStatus:" + downloadStatus);
				downloadId = media.getInt(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_ID));
				downloadSize = media.getInt(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_TOTAL_SIZE));
				downloadPath = media.getString(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_PATH));
				downloadUrl = media.getString(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_URL));
				dictionarySong = DETool.nativeDecryptStr(dictionary.getBlob(dictionary.getColumnIndex(MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SONG)));
				dictionarySinger = dictionary.getString(dictionary.getColumnIndex(MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SINGER));
				dictionaryFrom = dictionary.getString(dictionary.getColumnIndex(MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_FROM));
				matrixCursor.addRow(new Object[] { songId, downloadId, downloadStatus, downloadSize, downloadPath, downloadUrl, dictionarySong,
						dictionarySinger, dictionaryFrom, downloadType });
			} else if (media != null && media.getCount() == 1) {// 原唱已经在本地存在，但在下载表里面没有，所以下载表只有伴奏一条记录
				media.moveToFirst();
//				MediaApplication.logD(MediaManagerDB.class, "原唱已经在本地存在，但在下载表里面没有，所以下载表只有伴奏一条记录");
				downloadType = media.getInt(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_TYPE));
				downloadStatus = media.getInt(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_STATUS));
				downloadId = media.getInt(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_ID));
				downloadSize = media.getInt(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_TOTAL_SIZE));
				downloadPath = media.getString(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_PATH));
				downloadUrl = media.getString(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_URL));
				dictionarySong = DETool.nativeDecryptStr(dictionary.getBlob(dictionary.getColumnIndex(MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SONG)));
				dictionarySinger = dictionary.getString(dictionary.getColumnIndex(MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SINGER));
				dictionaryFrom = dictionary.getString(dictionary.getColumnIndex(MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_FROM));
				matrixCursor.addRow(new Object[] { songId, downloadId, downloadStatus, downloadSize, downloadPath, downloadUrl, dictionarySong,
						dictionarySinger, dictionaryFrom, downloadType });
			} else {// 没有在下载表里面找到对应的歌曲记录。
				dictionarySong = DETool.nativeDecryptStr(dictionary.getBlob(dictionary.getColumnIndex(MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SONG)));
				dictionarySinger = dictionary.getString(dictionary.getColumnIndex(MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SINGER));
				dictionaryFrom = dictionary.getString(dictionary.getColumnIndex(MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_FROM));
				matrixCursor.addRow(new Object[] { songId, null, IMediaService.STATE_DEFAULT, null, null, null, dictionarySong, dictionarySinger, dictionaryFrom, null });
			}
			media.close();
		}
		dictionary.close();
		return matrixCursor;
	}

	public Cursor queryDownloadingAudios() {
		Cursor downloadCursor = mediaDatabase.query(MediaDatabaseHelper.TAB_DOWNLOAD, null, MediaDatabaseHelper.COLUMN_DOWNLOAD_STATUS + " <>? ",
				new String[] { String.valueOf(IMediaService.STATE_FINISHED) }, null, null, MediaDatabaseHelper.COLUMN_DOWNLOAD_ORDER);
		return downloadCursor;
	}
	
	public Cursor queryDownloadWaitAudios() {
		Cursor downloadCursor = mediaDatabase.query(MediaDatabaseHelper.TAB_DOWNLOAD, null, MediaDatabaseHelper.COLUMN_DOWNLOAD_STATUS + " =? ",
				new String[] { String.valueOf(IMediaService.STATE_WAIT) }, null, null, MediaDatabaseHelper.COLUMN_DOWNLOAD_ORDER);
		return downloadCursor;
	}

	public Cursor queryDownloadOkAudios() {
		Cursor downloadCursor = mediaDatabase.query(MediaDatabaseHelper.TAB_DOWNLOAD, null, MediaDatabaseHelper.COLUMN_DOWNLOAD_STATUS + " =? ",
				new String[] { String.valueOf(IMediaService.STATE_FINISHED), "0", "10" }, null, null, MediaDatabaseHelper.COLUMN_DOWNLOAD_FINISH_DATE + " DESC " + " LIMIT ? , ? ");
		return downloadCursor;
	}
	
	
	public boolean queryWaitDownloadAccompanyIsOk(int songId, String singer, String song) {//查询下载表里面与伴奏相同songId的原唱下载状态是否已经完成。
		Cursor cursor = mediaDatabase.query(MediaDatabaseHelper.TAB_DOWNLOAD, new String[] { MediaDatabaseHelper.COLUMN_DOWNLOAD_STATUS }, MediaDatabaseHelper.COLUMN_DOWNLOAD_SONG_ID + " =? "+ " AND " + MediaDatabaseHelper.COLUMN_DOWNLOAD_TYPE + " =? ",
				new String[] { String.valueOf(songId), String.valueOf(MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL)}, null, null, null);
		if(cursor != null && cursor.getCount() == 1) {
			cursor.moveToFirst();
			if(cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_STATUS)) == IMediaService.STATE_FINISHED) {
				cursor.close();
				return true;
			}
		} else {
			String fPath;
			if(singer.equals("")){
				fPath = MediaApplication.savePath + song.replace("/", "_") + ServiceManager.getMediaService().AUDIO_SUFFIX;
			} else {
				fPath = MediaApplication.savePath + singer.replace("/", "_") + "-" + song.replace("/", "_") + ServiceManager.getMediaService().AUDIO_SUFFIX;
			}
			if((new File(fPath)).exists()){
				cursor.close();
				return true;
			}
		}
		cursor.close();
		return false;
	}


	public boolean deleteAudioFromDownload(int id) {
		return mediaDatabase.delete(MediaDatabaseHelper.TAB_DOWNLOAD, MediaDatabaseHelper.COLUMN_DOWNLOAD_ID + " =? ",
				new String[] { String.valueOf(id) }) > 0;
	}
	
	public int queryAudioStatusFromDownload(int id) {
		Cursor cursor= mediaDatabase.query(MediaDatabaseHelper.TAB_DOWNLOAD, new String[] { MediaDatabaseHelper.COLUMN_DOWNLOAD_STATUS }, MediaDatabaseHelper.COLUMN_DOWNLOAD_ID + " =? ",
				new String[] { String.valueOf(id) }, null, null, null);
		int status = -1;
		if(cursor != null && cursor.getCount() != 0){
			cursor.moveToFirst();
			status = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_STATUS)); 
		}
		cursor.close();
		return status;
	}

	public boolean deleteOriginalAndAccompanyFromDownload(int songId) {
		return mediaDatabase.delete(MediaDatabaseHelper.TAB_DOWNLOAD, MediaDatabaseHelper.COLUMN_DOWNLOAD_SONG_ID + " =? ", new String[] { String.valueOf(songId) }) > 0;
	}
	

	public int queryDictionarySingerAudiosCount(String singer, String pinyin) {

		Cursor cursor;
		if (pinyin == null || pinyin.length() == 0) {
			cursor = dictionaryDatabase.query(MediaDictionaryDatabaseHelper.TAB_DICTIONARY,
					new String[] { MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_ID }, MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SINGER
							+ " =? ", new String[] { singer }, null, null, null);
		} else {
			pinyin = pinyin.replace("?", "_");
			cursor = dictionaryDatabase.query(MediaDictionaryDatabaseHelper.TAB_DICTIONARY,
					new String[] { MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_ID }, MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SINGER
							+ " = ? " + " AND " + MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SONG_PINYIN + " LIKE ? ", new String[] { singer,
							pinyin + "%" }, null, null, null);
		}
		int count = cursor.getCount();
		cursor.close();
		return count;
	}

	private static final String SQL_SINGERS_COUNT = " SELECT COUNT ( DISTINCT " + MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SINGER + ") FROM "
			+ MediaDictionaryDatabaseHelper.TAB_DICTIONARY;

	private static final String SQL_SONGS_COUNT = " SELECT COUNT (" + MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_ID + ") FROM "
			+ MediaDictionaryDatabaseHelper.TAB_DICTIONARY;

	private static final String SQL_CATEGORY_SONGS_COUNT = " SELECT COUNT (" + MediaCategoryDatabaseHelper.COLUMN_CATEGORY_ID + ") FROM "
			+ MediaCategoryDatabaseHelper.TAB_CATEGORY;

	public int querySingersCount() {
		Cursor cursor = dictionaryDatabase.rawQuery(SQL_SINGERS_COUNT, null);
		int count = 0;
		if (cursor.moveToNext()) {
			count = cursor.getInt(0);
		}
		cursor.close();
		return count;
	}

	public int queryAudiosCount() {
		Cursor cursor = dictionaryDatabase.rawQuery(SQL_SONGS_COUNT, null);
		int count = 0;
		if (cursor.moveToNext()) {
			count = cursor.getInt(0);
		}
		cursor.close();
		return count;
	}

	public int queryHistoryAudiosCount() {
		return ServiceManager.getMediaService().getHistoryAudios().size();
	}

	public Cursor queryCategoryAudios(String type, String pinyin, int start, int length) {

		MatrixCursor matrixCursor = new MatrixCursor(new String[] { MediaCategoryDatabaseHelper.COLUMN_CATEGORY_ID,
				MediaDatabaseHelper.COLUMN_DOWNLOAD_ID_PREFIX + MediaDatabaseHelper.COLUMN_DOWNLOAD_ID, MediaDatabaseHelper.COLUMN_DOWNLOAD_STATUS,
				MediaDatabaseHelper.COLUMN_DOWNLOAD_TOTAL_SIZE, MediaDatabaseHelper.COLUMN_DOWNLOAD_PATH, MediaDatabaseHelper.COLUMN_DOWNLOAD_URL,
				MediaCategoryDatabaseHelper.COLUMN_CATEGORY_SONG, MediaCategoryDatabaseHelper.COLUMN_CATEGORY_SINGER, MediaCategoryDatabaseHelper.COLUMN_CATEGORY_FROM,
				MediaDatabaseHelper.COLUMN_DOWNLOAD_TYPE });
		Cursor category;
		if (pinyin == null || pinyin.length() == 0) {
			category = categoryDatabase.query(MediaCategoryDatabaseHelper.TAB_CATEGORY, new String[] {
					MediaCategoryDatabaseHelper.COLUMN_CATEGORY_ID, MediaCategoryDatabaseHelper.COLUMN_CATEGORY_SONG,
					MediaCategoryDatabaseHelper.COLUMN_CATEGORY_SINGER, MediaCategoryDatabaseHelper.COLUMN_CATEGORY_FROM}, MediaCategoryDatabaseHelper.COLUMN_CATEGORY_TYPE + " LIKE ? ",
					new String[] { "%" + type, String.valueOf(start), String.valueOf(length) }, null, null,
					MediaCategoryDatabaseHelper.COLUMN_CATEGORY_ID + " limit ? , ? ");
		} else {
			pinyin = pinyin.replace("?", "_");
			category = categoryDatabase.query(MediaCategoryDatabaseHelper.TAB_CATEGORY, new String[] {
					MediaCategoryDatabaseHelper.COLUMN_CATEGORY_ID, MediaCategoryDatabaseHelper.COLUMN_CATEGORY_SONG,
					MediaCategoryDatabaseHelper.COLUMN_CATEGORY_SINGER, MediaCategoryDatabaseHelper.COLUMN_CATEGORY_FROM }, MediaCategoryDatabaseHelper.COLUMN_CATEGORY_SONG_PINYIN + " LIKE  ? "
					+ " AND  " + MediaCategoryDatabaseHelper.COLUMN_CATEGORY_TYPE + " LIKE ? ",
					new String[] { pinyin + "%", "%" + type, String.valueOf(start), String.valueOf(length) }, null, null,
					MediaCategoryDatabaseHelper.COLUMN_CATEGORY_SONG_PINYIN + " limit ? , ? ");
		}
		Cursor media;
		int songId;
		int downloadId;
		int downloadStatus;
		int downloadSize;
		int downloadType;
		String downloadPath;
		String downloadUrl;
		String categorySong;
		String categorySinger;
		String categoryFrom;
		while (category.moveToNext()) {
			songId = category.getInt(category.getColumnIndex(MediaCategoryDatabaseHelper.COLUMN_CATEGORY_ID));
			media = mediaDatabase.query(MediaDatabaseHelper.TAB_DOWNLOAD, null, MediaDatabaseHelper.COLUMN_DOWNLOAD_SONG_ID + "= ? ",
					new String[] { String.valueOf(songId) }, null, null, MediaDatabaseHelper.COLUMN_DOWNLOAD_ORDER);
			if (media != null && media.getCount() == 2) {// 在下载表里面找到了对应的歌曲记录，一条是原唱的下载，一条是伴奏的下载。
				Map<Integer, Integer> compareMap = new HashMap<Integer, Integer>();
				for (media.moveToFirst(); !media.isAfterLast(); media.moveToNext()) {
					downloadPath = media.getString(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_PATH));
					downloadType = media.getInt(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_TYPE));
					downloadStatus = media.getInt(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_STATUS));
//					MediaApplication.logD(MediaManagerDB.class, "downloadType:" + downloadType + " downloadStatus:" + downloadStatus);
//					MediaApplication.logD(MediaManagerDB.class, "downloadPath:" + downloadPath);
					compareMap.put(downloadType, downloadStatus);
				}
				if (compareMap.get(MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL) == IMediaService.STATE_FINISHED
						&& compareMap.get(MediaDatabaseHelper.DOWNLOAD_TYPE_ACCOMPANY) == IMediaService.STATE_WAIT) {
					// 原唱下载完成但是伴奏还没有开始下载，显示原唱已经下载完成
					media.moveToFirst();
//					MediaApplication.logD(MediaManagerDB.class, "原唱下载完成但是伴奏还没有开始下载，显示原唱已经下载完成");
				} else if (compareMap.get(MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL) != IMediaService.STATE_FINISHED) {
					// 原唱没有下载完成，显示原唱的进度
					media.moveToFirst();
//					MediaApplication.logD(MediaManagerDB.class, "原唱没有下载完成，显示原唱的进度");
				} else if (compareMap.get(MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL) == IMediaService.STATE_FINISHED
						&& compareMap.get(MediaDatabaseHelper.DOWNLOAD_TYPE_ACCOMPANY) != IMediaService.STATE_WAIT) {
					// 伴奏开始下载，显示伴奏的进度
					media.moveToLast();
//					MediaApplication.logD(MediaManagerDB.class, "伴奏开始下载，显示伴奏的进度");
				}
				downloadType = media.getInt(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_TYPE));
				downloadStatus = media.getInt(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_STATUS));
//				MediaApplication.logD(MediaManagerDB.class, "被显示出来的是: download type:" + downloadType + " downloadStatus:" + downloadStatus);
				downloadId = media.getInt(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_ID));
				downloadSize = media.getInt(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_TOTAL_SIZE));
				downloadPath = media.getString(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_PATH));
				downloadUrl = media.getString(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_URL));
				categorySong = DETool.nativeDecryptStr(category.getBlob(category.getColumnIndex(MediaCategoryDatabaseHelper.COLUMN_CATEGORY_SONG)));
				categorySinger = category.getString(category.getColumnIndex(MediaCategoryDatabaseHelper.COLUMN_CATEGORY_SINGER));
				categoryFrom = category.getString(category.getColumnIndex(MediaCategoryDatabaseHelper.COLUMN_CATEGORY_FROM));
				matrixCursor.addRow(new Object[] { songId, downloadId, downloadStatus, downloadSize, downloadPath, downloadUrl, categorySong,
						categorySinger, categoryFrom, downloadType });
			} else if (media != null && media.getCount() == 1) {// 原唱已经在本地存在，但在下载表里面没有，所以下载表只有伴奏一条记录
				media.moveToFirst();
				//MediaApplication.logD(MediaManagerDB.class, "原唱已经在本地存在，但在下载表里面没有，所以下载表只有伴奏一条记录");
				downloadType = media.getInt(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_TYPE));
				downloadStatus = media.getInt(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_STATUS));
				downloadId = media.getInt(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_ID));
				downloadSize = media.getInt(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_TOTAL_SIZE));
				downloadPath = media.getString(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_PATH));
				downloadUrl = media.getString(media.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_URL));
				categorySong = DETool.nativeDecryptStr(category.getBlob(category.getColumnIndex(MediaCategoryDatabaseHelper.COLUMN_CATEGORY_SONG)));
				categorySinger = category.getString(category.getColumnIndex(MediaCategoryDatabaseHelper.COLUMN_CATEGORY_SINGER));
				categoryFrom = category.getString(category.getColumnIndex(MediaCategoryDatabaseHelper.COLUMN_CATEGORY_FROM));
				matrixCursor.addRow(new Object[] { songId, downloadId, downloadStatus, downloadSize, downloadPath, downloadUrl, categorySong,
						categorySinger, categoryFrom, downloadType });
			} else {// 没有在下载表里面找到对应的歌曲记录。
				categorySong = DETool.nativeDecryptStr(category.getBlob(category.getColumnIndex(MediaCategoryDatabaseHelper.COLUMN_CATEGORY_SONG)));
				categorySinger = category.getString(category.getColumnIndex(MediaCategoryDatabaseHelper.COLUMN_CATEGORY_SINGER));
				categoryFrom = category.getString(category.getColumnIndex(MediaCategoryDatabaseHelper.COLUMN_CATEGORY_FROM));
				matrixCursor.addRow(new Object[] { songId, null, IMediaService.STATE_DEFAULT, null, null, null, categorySong, categorySinger, categoryFrom, null });
			}
			media.close();
		}
		category.close();
		return matrixCursor;
	}

	public int queryCategoryAudiosCount(String type, String pinyin) {

		int count = 0;
		String where;
		String[] args;
		if (pinyin == null || pinyin.trim().length() == 0) {
			where = " WHERE " + MediaCategoryDatabaseHelper.COLUMN_CATEGORY_TYPE + " LIKE  ? ";
			args = new String[] { "%" + type };
		} else {
			pinyin = pinyin.replace("?", "_");
			where = " WHERE " + MediaCategoryDatabaseHelper.COLUMN_CATEGORY_SONG_PINYIN + " LIKE ? " + " AND "
					+ MediaCategoryDatabaseHelper.COLUMN_CATEGORY_TYPE + " LIKE  ? ";
			args = new String[] { pinyin + "%", "%" + type };
		}
		Cursor cursor = categoryDatabase.rawQuery(SQL_CATEGORY_SONGS_COUNT + where, args);
		if (cursor.moveToNext()) {
			count = cursor.getInt(0);
		}
		cursor.close();
		return count;
	}

	public int queryCategoryAudiosCount() {
		int count = 0;
		Cursor cursor = categoryDatabase.rawQuery(SQL_CATEGORY_SONGS_COUNT, null);
		if (cursor.moveToNext()) {
			count = cursor.getInt(0);
		}
		cursor.close();
		return count;
	}

	public boolean addCountLocalSong(String song, String singer) {
		Integer id = inCountLocal(song, singer);
		ContentValues values;
		if (id != null) {
			values = new ContentValues();
			values.put(MediaDatabaseHelper.COLUMN_COUNT_LOCAL_SONG, song);
			values.put(MediaDatabaseHelper.COLUMN_COUNT_LOCAL_SINGER, singer);
			values.put(MediaDatabaseHelper.COLUMN_COUNT_LOCAL_COUNT, queryCountLocalSongCount(song, singer) + 1);
			return updateCountLocalSong(id, values);
		} else {
			values = new ContentValues();
			values.put(MediaDatabaseHelper.COLUMN_COUNT_LOCAL_SONG, song);
			values.put(MediaDatabaseHelper.COLUMN_COUNT_LOCAL_SINGER, singer);
			values.put(MediaDatabaseHelper.COLUMN_COUNT_LOCAL_COUNT, 1);
			return insertCountLocalSong(values);
		}
	}

	private Integer inCountLocal(String song, String singer) {
		Cursor cursor = mediaDatabase.query(MediaDatabaseHelper.TAB_COUNT_LOCAL, new String[] { MediaDatabaseHelper.COLUMN_COUNT_LOCAL_ID },
				MediaDatabaseHelper.COLUMN_COUNT_LOCAL_SONG + " =? " + " AND " + MediaDatabaseHelper.COLUMN_COUNT_LOCAL_SINGER + " =? ",
				new String[] { song, singer }, null, null, null);
		Integer id = null;
		if (cursor.moveToNext()) {
			id = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_COUNT_LOCAL_ID));
		}
		cursor.close();
		return id;
	}

	private boolean insertCountLocalSong(ContentValues values) {
		return mediaDatabase.insert(MediaDatabaseHelper.TAB_COUNT_LOCAL, null, values) > 0;
	}

	private int queryCountLocalSongCount(String song, String singer) {
		Cursor cursor = mediaDatabase.query(MediaDatabaseHelper.TAB_COUNT_LOCAL, new String[] { MediaDatabaseHelper.COLUMN_COUNT_LOCAL_COUNT },
				MediaDatabaseHelper.COLUMN_COUNT_LOCAL_SONG + " =? " + " AND " + MediaDatabaseHelper.COLUMN_COUNT_LOCAL_SINGER + " =? ",
				new String[] { song, singer }, null, null, null);
		int count = 0;
		if (cursor.moveToNext()) {
			count = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_COUNT_LOCAL_COUNT));
		}
		cursor.close();
		return count;
	}

	private boolean updateCountLocalSong(int id, ContentValues values) {
		return mediaDatabase.update(MediaDatabaseHelper.TAB_COUNT_LOCAL, values, MediaDatabaseHelper.COLUMN_COUNT_LOCAL_ID + " =? ",
				new String[] { String.valueOf(id) }) > 0;
	}

	public Cursor queryCountLocalSongs() {
		return mediaDatabase.query(MediaDatabaseHelper.TAB_COUNT_LOCAL, new String[] { MediaDatabaseHelper.COLUMN_COUNT_LOCAL_ID,
				MediaDatabaseHelper.COLUMN_COUNT_LOCAL_SONG, MediaDatabaseHelper.COLUMN_COUNT_LOCAL_SINGER,
				MediaDatabaseHelper.COLUMN_COUNT_LOCAL_COUNT }, null, null, null, null, MediaDatabaseHelper.COLUMN_COUNT_LOCAL_COUNT);
	}

	public boolean deleteCountLocalSong() {
		return mediaDatabase.delete(MediaDatabaseHelper.TAB_COUNT_LOCAL, null, null) > 0;
	}

	public boolean addCountKMediaSong(String song, String singer) {
		Integer id = inCountKMedia(song, singer);
		ContentValues values;
		if (id != null) {
			values = new ContentValues();
			values.put(MediaDatabaseHelper.COLUMN_COUNT_KMEDIA_SONG, song);
			values.put(MediaDatabaseHelper.COLUMN_COUNT_KMEDIA_SINGER, singer);
			values.put(MediaDatabaseHelper.COLUMN_COUNT_KMEDIA_COUNT, queryCountKMediaSongCount(song, singer) + 1);
			return updateCountKMediaSong(id, values);
		} else {
			values = new ContentValues();
			values.put(MediaDatabaseHelper.COLUMN_COUNT_KMEDIA_SONG, song);
			values.put(MediaDatabaseHelper.COLUMN_COUNT_KMEDIA_SINGER, singer);
			values.put(MediaDatabaseHelper.COLUMN_COUNT_KMEDIA_COUNT, 1);
			return insertCountKMediaSong(values);
		}
	}

	private Integer inCountKMedia(String song, String singer) {
		Cursor cursor = mediaDatabase.query(MediaDatabaseHelper.TAB_COUNT_KMEDIA, new String[] { MediaDatabaseHelper.COLUMN_COUNT_KMEDIA_ID },
				MediaDatabaseHelper.COLUMN_COUNT_KMEDIA_SONG + " =? " + " AND " + MediaDatabaseHelper.COLUMN_COUNT_KMEDIA_SINGER + " =? ",
				new String[] { song, singer }, null, null, null);
		Integer id = null;
		if (cursor.moveToNext()) {
			id = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_COUNT_KMEDIA_ID));
		}
		cursor.close();
		return id;
	}

	private boolean insertCountKMediaSong(ContentValues values) {
		return mediaDatabase.insert(MediaDatabaseHelper.TAB_COUNT_KMEDIA, null, values) > 0;
	}

	private int queryCountKMediaSongCount(String song, String singer) {
		Cursor cursor = mediaDatabase.query(MediaDatabaseHelper.TAB_COUNT_KMEDIA, new String[] { MediaDatabaseHelper.COLUMN_COUNT_KMEDIA_COUNT },
				MediaDatabaseHelper.COLUMN_COUNT_KMEDIA_SONG + " =? " + " AND " + MediaDatabaseHelper.COLUMN_COUNT_KMEDIA_SINGER + " =? ",
				new String[] { song, singer }, null, null, null);
		int count = 0;
		if (cursor.moveToNext()) {
			count = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_COUNT_KMEDIA_COUNT));
		}
		cursor.close();
		return count;
	}

	private boolean updateCountKMediaSong(int id, ContentValues values) {
		return mediaDatabase.update(MediaDatabaseHelper.TAB_COUNT_KMEDIA, values, MediaDatabaseHelper.COLUMN_COUNT_KMEDIA_ID + " =? ",
				new String[] { String.valueOf(id) }) > 0;
	}

	public Cursor queryCountKMediaSongs() {
		return mediaDatabase.query(MediaDatabaseHelper.TAB_COUNT_KMEDIA, new String[] { MediaDatabaseHelper.COLUMN_COUNT_KMEDIA_ID,
				MediaDatabaseHelper.COLUMN_COUNT_KMEDIA_SONG, MediaDatabaseHelper.COLUMN_COUNT_KMEDIA_SINGER,
				MediaDatabaseHelper.COLUMN_COUNT_KMEDIA_COUNT }, null, null, null, null, MediaDatabaseHelper.COLUMN_COUNT_KMEDIA_COUNT);
	}

	public boolean deleteCountKMediaSong() {
		return mediaDatabase.delete(MediaDatabaseHelper.TAB_COUNT_KMEDIA, null, null) > 0;
	}

	private boolean insertUserActivityInfoTask(int date, int result) {
		ContentValues values = new ContentValues();
		values.put(MediaDatabaseHelper.COLUMN_COUNT_USER_INFO_ACTIVITY_DATE, date);
		values.put(MediaDatabaseHelper.COLUMN_COUNT_USER_INFO_ACTIVITY_DAY_TIMES, 1);
		values.put(MediaDatabaseHelper.COLUMN_COUNT_USER_INFO_ACTIVITY_RESULT, result);
		return mediaDatabase.insert(MediaDatabaseHelper.TAB_COUNT_USER_ACTIVITY_INFO_TASK, null, values) > 0;
	}

	public boolean updateUserActivityInfo(int mDayDate, Integer mTimesTamp){
		Integer times = null;
		Integer timesTamp = null;
		Integer dayDate = null;
		Cursor cursor = queryUserActivityInfo();
		if(cursor.moveToNext()){
			times = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_COUNT_USER_INFO_ACTIVITY_TIMES));
			timesTamp = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_COUNT_USER_INFO_ACTIVITY_CUMULATIVE_TIME));
			dayDate = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_COUNT_USER_INFO_ACTIVITY_DAY_DATE));
		}
		cursor.close();
		ContentValues values;
		if(times != null && timesTamp != null){
			values = new ContentValues();
			if(mDayDate - dayDate >= 1){
				values.put(MediaDatabaseHelper.COLUMN_COUNT_USER_INFO_ACTIVITY_TIMES, times + 1);
				dayDate = mDayDate;
				values.put(MediaDatabaseHelper.COLUMN_COUNT_USER_INFO_ACTIVITY_DAY_DATE, dayDate);
			}else{
				values.put(MediaDatabaseHelper.COLUMN_COUNT_USER_INFO_ACTIVITY_TIMES, times);
			}
			values.put(MediaDatabaseHelper.COLUMN_COUNT_USER_INFO_ACTIVITY_CUMULATIVE_TIME, timesTamp+mTimesTamp);
			return mediaDatabase.update(MediaDatabaseHelper.TAB_COUNT_USER_ACTIVITY_INFO_TASK, values, null, null) > 0;
		}else{
			return insertCountAudioInfo(mDayDate, 1, mTimesTamp);
		}
		
	}
	
	public boolean insertCountAudioInfo(int dayDate, int times, Integer timesTamp){
		ContentValues values = new ContentValues();
		values.put(MediaDatabaseHelper.COLUMN_COUNT_USER_INFO_ACTIVITY_DAY_DATE, dayDate);
		values.put(MediaDatabaseHelper.COLUMN_COUNT_USER_INFO_ACTIVITY_TIMES, times);
		values.put(MediaDatabaseHelper.COLUMN_COUNT_USER_INFO_ACTIVITY_CUMULATIVE_TIME, timesTamp);
		return mediaDatabase.insert(MediaDatabaseHelper.TAB_COUNT_USER_ACTIVITY_INFO_TASK, null, values) > 0;
	}
	
	public boolean updateUserActivityInfoTask(int date, int result) {
		Cursor cursor = queryCountAudioInfoTask();
		Integer oldDate = null;
		int times = 0;
		if (cursor.moveToNext()) {
			oldDate = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_COUNT_USER_INFO_ACTIVITY_DATE));
			times = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_COUNT_USER_INFO_ACTIVITY_DAY_TIMES));
		}
		cursor.close();
		ContentValues values;
		if (oldDate != null) {
			if (date > oldDate) {
				times = 0;
			}
			values = new ContentValues();
			values.put(MediaDatabaseHelper.COLUMN_COUNT_USER_INFO_ACTIVITY_DATE, date);
			values.put(MediaDatabaseHelper.COLUMN_COUNT_USER_INFO_ACTIVITY_DAY_TIMES, times + 1);
			values.put(MediaDatabaseHelper.COLUMN_COUNT_USER_INFO_ACTIVITY_RESULT, result);
			return mediaDatabase.update(MediaDatabaseHelper.TAB_COUNT_USER_ACTIVITY_INFO_TASK, values, null, null) > 0;
		} else {
			return insertCountAudioInfoTask(date, result);
		}
	}
	
	private boolean insertCountAudioInfoTask(int date, int result) {
		ContentValues values = new ContentValues();
		values.put(MediaDatabaseHelper.COLUMN_COUNT_AUDIO_INFO_TASK_DATE, date);
		values.put(MediaDatabaseHelper.COLUMN_COUNT_AUDIO_INFO_TASK_DAY_TIMES, 1);
		values.put(MediaDatabaseHelper.COLUMN_COUNT_AUDIO_INFO_TASK_RESULT, result);
		return mediaDatabase.insert(MediaDatabaseHelper.TAB_COUNT_AUDIO_INFO_TASK, null, values) > 0;
	}

	public boolean updateCountAudioInfoTask(int date, int result) {
		Cursor cursor = queryCountAudioInfoTask();
		Integer oldDate = null;
		int times = 0;
		if (cursor.moveToNext()) {
			oldDate = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_COUNT_AUDIO_INFO_TASK_DATE));
			times = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_COUNT_AUDIO_INFO_TASK_DAY_TIMES));
		}
		cursor.close();
		ContentValues values;
		if (oldDate != null) {
			if (date > oldDate) {
				times = 0;
			}
			values = new ContentValues();
			values.put(MediaDatabaseHelper.COLUMN_COUNT_AUDIO_INFO_TASK_DATE, date);
			values.put(MediaDatabaseHelper.COLUMN_COUNT_AUDIO_INFO_TASK_DAY_TIMES, times + 1);
			values.put(MediaDatabaseHelper.COLUMN_COUNT_AUDIO_INFO_TASK_RESULT, result);
			return mediaDatabase.update(MediaDatabaseHelper.TAB_COUNT_AUDIO_INFO_TASK, values, null, null) > 0;
		} else {
			return insertCountAudioInfoTask(date, result);
		}
	}

	public Cursor queryCountAudioInfoTask() {
		return mediaDatabase.query(MediaDatabaseHelper.TAB_COUNT_AUDIO_INFO_TASK, null, null, null, null, null, null);
	}

	private boolean insertCountUserInfoTask(int date, int result) {
		ContentValues values = new ContentValues();
		values.put(MediaDatabaseHelper.COLUMN_COUNT_USER_INFO_TASK_TIMES, result == 1 ? 1 : 0);
		values.put(MediaDatabaseHelper.COLUMN_COUNT_USER_INFO_TASK_DAY_TIMES, 1);
		values.put(MediaDatabaseHelper.COLUMN_COUNT_USER_INFO_TASK_DATE, date);
		values.put(MediaDatabaseHelper.COLUMN_COUNT_USER_INFO_TASK_RESULT, result);
		return mediaDatabase.insert(MediaDatabaseHelper.TAB_COUNT_USER_INFO_TASK, null, values) > 0;
	}

	public boolean updateCountUserInfoTask(int date, int result) {
		Cursor cursor = queryCountUserInfoTask();
		Integer oldDate = null;
		int dayTimes = 0;
		int times = 0;
		if (cursor.moveToNext()) {
			oldDate = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_COUNT_USER_INFO_TASK_DATE));
			dayTimes = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_COUNT_USER_INFO_TASK_DAY_TIMES));
			times = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_COUNT_USER_INFO_TASK_TIMES));
		}
		cursor.close();
		ContentValues values;
		if (oldDate != null) {
			if (date > oldDate) {
				dayTimes = 0;
			}
			values = new ContentValues();
			values.put(MediaDatabaseHelper.COLUMN_COUNT_USER_INFO_TASK_DATE, date);
			values.put(MediaDatabaseHelper.COLUMN_COUNT_USER_INFO_TASK_DAY_TIMES, dayTimes + 1);
			values.put(MediaDatabaseHelper.COLUMN_COUNT_USER_INFO_TASK_TIMES, result == 1 ? times + 1 : times);
			values.put(MediaDatabaseHelper.COLUMN_COUNT_USER_INFO_TASK_RESULT, result);
			return mediaDatabase.update(MediaDatabaseHelper.TAB_COUNT_USER_INFO_TASK, values, null, null) > 0;
		} else {
			return insertCountUserInfoTask(date, result);
		}
	}

	public Cursor queryCountUserInfoTask() {
		return mediaDatabase.query(MediaDatabaseHelper.TAB_COUNT_USER_INFO_TASK, new String[] {
				MediaDatabaseHelper.COLUMN_COUNT_USER_INFO_TASK_RESULT, MediaDatabaseHelper.COLUMN_COUNT_USER_INFO_TASK_TIMES,
				MediaDatabaseHelper.COLUMN_COUNT_USER_INFO_TASK_DAY_TIMES, MediaDatabaseHelper.COLUMN_COUNT_USER_INFO_TASK_DATE, }, null, null, null,
				null, null);
	}

	public boolean deletePlaylistSongs(int id) {
		return mediaDatabase.delete(MediaDatabaseHelper.TAB_AUDIO, MediaDatabaseHelper.COLUMN_AUDIO_PLAYLIST_ID + " =? ",
				new String[] { String.valueOf(id) }) > 0;
	}

	public boolean renamePlaylist(int id, ContentValues values) {
		String name = (String) values.get(MediaDatabaseHelper.COLUMN_PLAYLIST_NAME);
		Cursor cursor = mediaDatabase.query(MediaDatabaseHelper.TAB_PLAYLIST, new String[] { MediaDatabaseHelper.COLUMN_PLAYLIST_ID },
				MediaDatabaseHelper.COLUMN_PLAYLIST_NAME + " =? " + " AND " + MediaDatabaseHelper.COLUMN_PLAYLIST_ID + " <> ? ", new String[] { name,
						String.valueOf(id) }, null, null, null);
		boolean exist = false;
		if (cursor.getCount() > 0) {
			exist = true;
		}
		cursor.close();
		if (exist) {
			return false;
		}
		return mediaDatabase.update(MediaDatabaseHelper.TAB_PLAYLIST, values, MediaDatabaseHelper.COLUMN_PLAYLIST_ID + " =? ",
				new String[] { String.valueOf(id) }) > 0;
	}

	public boolean audioIsFavorite(int id) {
		Cursor cursor = mediaDatabase.query(MediaDatabaseHelper.TAB_FAVORITY, new String[] { MediaDatabaseHelper.COLUMN_FAVORITY_MEDIA_ID },
				MediaDatabaseHelper.COLUMN_FAVORITY_MEDIA_ID + " =? ", new String[] { String.valueOf(id) }, null, null, null);
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				cursor.close();
				return true;
			}
			cursor.close();
		}
		return false;
	}

	public Cursor queryCategories() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Cursor queryUserActivityInfo(){
		return mediaDatabase.query(MediaDatabaseHelper.TAB_COUNT_USER_ACTIVITY_INFO_TASK, null, null, null, null,null,null);
	}
	
	public int deleteUserActivityInfo(){
		return mediaDatabase.delete(MediaDatabaseHelper.TAB_COUNT_USER_ACTIVITY_INFO_TASK,null,null);
	}
	
	public Cursor querySongFile()
	{
		return contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
				new String[] {MediaStore.Audio.Media.DATA}, 
				MediaStore.Audio.Media.ALBUM_KEY + " != ?", 
				new String[]{"record1202056869"},
				MediaStore.Audio.Media.DATA);
	}
	
	public Cursor makeRecordCursor(ArrayList<SongInfo> recordSongs) {
		MatrixCursor matrixCursor = new MatrixCursor(new String[] { MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ID, MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_DISPALYNAME, MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ARTISTNAME,
				MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_FILEPATH, MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_DURATION});
		if(recordSongs!=null){
			for(int i = 0; i < recordSongs.size(); i++){
				String songName = recordSongs.get(i).getSongName();
				String singerName = recordSongs.get(i).getSingerName();
				songName =songName+".mp3";
				String path = recordSongs.get(i).getDirectory();
				matrixCursor.addRow(new Object[] { i ,songName, singerName, path, 0});
			}
			return matrixCursor;
		}
		return null;
		
	}
	
	public Cursor querySystemAllmp3()
	{
		String selection = MediaStore.Audio.Media.DURATION + " > 0 and " 
		+ MediaStore.Audio.Media.MIME_TYPE + " = ? and "
		+ MediaStore.Audio.Media.DATA + " not like ?";
		return contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				null, 
				selection, 
				new String[]{"audio/mpeg",MediaPlayerService.directoryRecord + "%"}, 
				MediaStore.Audio.Media._ID);
	}
	
	public long scaninsertsing(ContentValues values)
	{
		return mediaDatabase.insert(MediaDatabaseHelper.TAB_AMPLAY_SONGS, null, values);
	}
	
	public int scandeletesing()
	{
		return mediaDatabase.delete(MediaDatabaseHelper.TAB_AMPLAY_SONGS, null, null);
	}
	
	public int updatebysinger(String filename,String singer)
	{
		ContentValues values = new ContentValues();
		values.put(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ARTISTNAME, singer);
		return mediaDatabase.update(MediaDatabaseHelper.TAB_AMPLAY_SONGS, values,  MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_FILEPATH + " = ? ", new String[]{filename});
		
	}
	
	public int checkifsongexists(String singname)
	{
		int count = 0;
		
		Cursor cursor = mediaDatabase.query(MediaDatabaseHelper.TAB_AMPLAY_SONGS, 
				new String[]{MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_SONGNAME}, 
				MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_SONGNAME + " = ? ", 
				new String[]{singname}, null, null, null);
		count = cursor.getCount();
		cursor.close();
		
		return count;
	}
	
	public int queryPositionById(Cursor cursor,int id) {
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            if (id == cursor.getLong(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ID))) {
                return cursor.getPosition();
            }
		}
		return -1;
	}
	
	public Cursor queryCurPlaySong(Integer id){
		return mediaDatabase.query(MediaDatabaseHelper.TAB_AMPLAY_SONGS,
				null, 
				MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ID + " = ?",
				new String[]{id.toString()}, 
				null,
				null,
				null);
	}
	
	public Integer querySongId(String displayname,String artistname){
		Integer ret = new Integer(-1);
		Cursor cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				null,
				MediaStore.Audio.Media.DISPLAY_NAME + " = ? AND " + MediaStore.Audio.Media.ARTIST + " = ? ",
				new String[]{displayname , artistname}, 
				null);
		
		if (cursor.getCount() > 0)
		{
			cursor.moveToNext();
			ret =  new Integer(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
		}
		cursor.close();
		
		return ret;
	}
}