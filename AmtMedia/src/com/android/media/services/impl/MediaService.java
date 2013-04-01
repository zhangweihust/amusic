package com.android.media.services.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.amusic.media.R;
import com.android.media.MediaApplication;
import com.android.media.download.DownloadJob;
import com.android.media.download.DownloadLyric;
import com.android.media.event.IMediaEventArgs;
import com.android.media.event.IMediaEventHandler;
import com.android.media.event.impl.MediaEventArgs;
import com.android.media.event.impl.MediaEventTypes;
import com.android.media.provider.MediaDatabaseHelper;
import com.android.media.provider.MediaDictionaryDatabaseHelper;
import com.android.media.provider.MediaManagerDB;
import com.android.media.provider.MediaScanner;
import com.android.media.services.IMediaEventService;
import com.android.media.services.IMediaService;
import com.android.media.task.DownloadTask;
import com.android.media.utils.ToastUtil;
import com.android.media.view.CustomDialog;

public class MediaService implements IMediaService, IMediaEventHandler {
	private final MediaManagerDB mediaDB = new MediaManagerDB(MediaApplication.getContext());
	private List<String> FilePathList = new ArrayList<String>();
	private final Map<Integer, DownloadJob> downloadMap = new HashMap<Integer, DownloadJob>();
	private final List<IMediaEventArgs> historySongs = new ArrayList<IMediaEventArgs>();
	private final byte[] hLock = new byte[0];
	private final byte[] dmLock = new byte[0];
	private final byte[] dlLock = new byte[0];
	private int downloadSize = 3;
	private final Context context = MediaApplication.getContext();
	private final MediaScanner mediaScanner = new MediaScanner(context);
	private IMediaEventService mediaEventService;
	private IMediaService mediaService;
	private Handler handler;
	private DownloadTask mDownloadTask;
	@Override
	public boolean start() {
		handler = ServiceManager.getAmtMediaHandler();
		mediaDB.open();
		mediaEventService = ServiceManager.getMediaEventService();
		mediaService = ServiceManager.getMediaService();
		mediaEventService.addEventHandler(this);
		return true;
	}

	@Override
	public boolean stop() {
		ContentValues values = new ContentValues();
		DownloadJob downloadMusic;
		synchronized (dmLock) {
			for (Map.Entry<Integer, DownloadJob> entry : downloadMap.entrySet()) {
				downloadMusic = entry.getValue();
				downloadMusic.setDownloadStateFlag(IMediaService.STATE_PAUSE);
				values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_STATUS, STATE_PAUSE);
				mediaDB.updateDownloadAudio(entry.getKey(), values);
			}
		}
		mediaDB.close();
		mediaEventService.removeEventHandler(this);
		return true;
	}

	private boolean  changeNextWaitDownload(Cursor cursor){
		/*
		 * 遍历等待下载的数据集，
		 * 1:如果第一条等待下载记录是原唱,就返回true可以继续获取数据封装成DownloadMusic并加入到下载MAP
		 * 2：如果第一条等待下载的记录是伴奏，查看下载表里伴奏对应的原唱是什么状态，如果是下载完成那么返回true开始下载这条伴奏
		 *    如果不是下载完成的状态，那么cursor.moveToNext()去取下一条等待下载的记录，并重复上面的判断操作。
		 * */
		int downloadType;
cursor_loop:
	while(true){
		downloadType = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_TYPE));
		int songId = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_SONG_ID)); 
		String singer = cursor.getString(cursor.getColumnIndex(MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SINGER));
		String song = cursor.getString(cursor.getColumnIndex(MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SONG));
		if(downloadType == MediaDatabaseHelper.DOWNLOAD_TYPE_ACCOMPANY) {
			if(!mediaDB.queryWaitDownloadAccompanyIsOk(songId, singer, song)) {//
				if (cursor.moveToNext()) {
					MediaApplication.logD(MediaService.class, "原唱还在下载，伴奏不能开始");
					continue cursor_loop;
				} else {
					cursor.moveToPrevious();
					MediaApplication.logD(MediaService.class, "遍历完毕，没有合适的进入下载队列");
					return false;
				}
			} else {
				MediaApplication.logD(MediaService.class, "伴奏:" + song + "进入下载队列");
				return true;
			}
		} else {
			MediaApplication.logD(MediaService.class, "原唱:" + song + "进入下载队列");
			return true;
		}
	}  
	}
	
	
	private void startDownload() {
		synchronized (dmLock) {
			if (downloadMap.size() < downloadSize) {
				DownloadJob downloadMusic;
				IMediaEventArgs args = new MediaEventArgs();
				Cursor cursor = mediaDB.queryDownloadWaitAudios();
				if (cursor.moveToNext()) {
					if(!changeNextWaitDownload(cursor)) {
						cursor.close();
						return;
					}
					if(cursor.isAfterLast() && cursor != null){
						MediaApplication.logD(MediaService.class, "遍历等待下载的数据集越界: count: " + cursor.getCount() + "position: " + cursor.getPosition());
						cursor.close();
						return;
					}
					Integer downloadId = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_ID));
					ContentValues values = new ContentValues();
					values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_STATUS, STATE_IN_QUEUE);
					mediaDB.updateDownloadAudio(downloadId, values);
					int downloadType = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_TYPE));
					int songId = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_SONG_ID)); 
					String song = cursor.getString(cursor.getColumnIndex(MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SONG));
					String singer = cursor.getString(cursor.getColumnIndex(MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SINGER));
					int status = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_STATUS));
					int resource = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_RESOURCE));
					long size = cursor.getLong(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_TOTAL_SIZE));
					String url = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_URL));
					String fPath = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_PATH));
					String path = fPath + TMP_SUFFIX;
					args.putExtra("song", song);
					args.putExtra("singer", singer);
					args.putExtra("path", path);
					args.putExtra("fPath", fPath);
					args.putExtra("status", status);
					args.putExtra("resource", resource);
					args.putExtra("size", size);
					args.putExtra("url", url);
					args.putExtra("downloadId", downloadId);
					args.putExtra("downloadType", downloadType);
					args.putExtra("songId", songId);
					if(downloadMap.containsKey(downloadId)){
						synchronized (dmLock) {
							downloadMusic = downloadMap.get(downloadId);
						}
						if (downloadMusic.getDownloadUrl() != null && downloadMusic.getDownloadUrl().length() > 0) {
							downloadMusic.setRebegin(true);
							args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_CONTINUE);
						} else {
							downloadMusic.setRebegin(true);
							args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_START);
						}
						mediaEventService.onMediaUpdateEvent(args);
						cursor.close();
						return;
					}
					downloadMusic = new DownloadJob(args, false);
					synchronized (dmLock) {
						downloadMap.put(downloadId, downloadMusic);
					}
					if (url != null && url.length() > 0) {
						downloadMusic.setRebegin(true);
						args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_CONTINUE);
					} else {
						downloadMusic.setRebegin(false);
						args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_START);
					}
					mediaEventService.onMediaUpdateEvent(args);
				}
				cursor.close();
			}
		}
	}
	

	private Dialog dialog;
	private MediaEventArgs args;
	private String gFilename;
	private long gDuration;
	private String gLyricPath;
	private String gfilePath;
	
	private OnItemClickListener itemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
			args = new MediaEventArgs();
			CustomDialog.Builder.ViewHolder holer = (CustomDialog.Builder.ViewHolder) view.getTag();
			String name = (String) holer.title.getText();
			String filename = gFilename;
			long duration = gDuration;
			String audiofilePath = gfilePath;
			String[] strs = name.split("——");

			if(strs.length != 2) {
				mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_ERROR));
				dialog.dismiss();
				return;
			}


			args.putExtra("song_Name", strs[0]);
			args.putExtra("singer_Name", strs[1]);
			args.putExtra("duration", duration);
			args.putExtra("lyricPath", gLyricPath);
			//args.putExtra("local",true);
			args.putExtra("filename",filename);
			args.putExtra("isNeedPopDialog", true);
			String singer = strs[1];
			
			writeTag(audiofilePath,singer);

			mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_LYRIC));

			dialog.dismiss();
		}
	};
	
	
	public static void writeTag(String filePath,String singer) {
		try {
			File sf = new File(filePath);
			if (sf != null && sf.exists()){
				AudioFile af = AudioFileIO.read(sf);
				AudioFileIO.delete(af);
				Tag t = null;
				t = af.getTag();
				String album = null;
				String year = null;
				String comment = null;
				String genre = null;
				String title = null;
				if(t != null){
					album = t.getFirst(FieldKey.ALBUM);
					year = t.getFirst(FieldKey.YEAR);
					comment = t.getFirst(FieldKey.COMMENT);
					genre = t.getFirst(FieldKey.GENRE);
					title = t.getFirst(FieldKey.TITLE);
				}
				Tag tag = null;
				if ((tag = af.createDefaultTag()) != null) {
					af.setTag(tag);
					tag.addField(FieldKey.ARTIST, singer);
					if(album !=null && !album.equals("")){
						tag.addField(FieldKey.ALBUM, album);
					}
					if(year !=null && !year.equals("")){
						tag.addField(FieldKey.YEAR, year);
					}
					if(comment !=null && !comment.equals("")){
						tag.addField(FieldKey.COMMENT, comment);
					}
					if(genre !=null && !genre.equals("")){
						tag.addField(FieldKey.GENRE, genre);
					}
					if(title !=null && !title.equals("")){
						tag.addField(FieldKey.TITLE, title);
					}
					}
					af.commit();
					MediaScanner mediaScanner = ServiceManager.getMediaScanner();
					mediaScanner.scan(new File(filePath));
					MediaManagerDB mediadb = ServiceManager.getMediaService().getMediaDB();
					mediadb.updatebysinger(filePath, singer);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TagException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ReadOnlyFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAudioFrameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CannotWriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CannotReadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	@Override
	public boolean onEvent(final IMediaEventArgs args) {
		File file;
		DownloadJob downloadMusic;
		final Integer downloadId;
		final Integer downloadWithAccompany;
		final Integer downloadType;
		final Integer songId;
		final String path;
		final String fPath;
		final String accompanyPath;
		final String fAccompanyPath;
		final String singer;
		final String song;
		final long size;
		final String url;
		final Integer songType;
		final boolean show;
		final Integer resource;
		final int status;
		ContentValues values = new ContentValues();
		switch (args.getMediaUpdateEventTypes()) {
		
		case AUDIO_DOWNLOAD_LYRIC_SELECT_UI:
			handler.post(new Runnable() {

				@SuppressWarnings("unchecked")
				@Override
				public void run() {

					List<String> lyricList = (List<String>) args
							.getExtra("lyricList");
					long duration = (Long) args.getExtra("duration");
					String filename = (String) args.getExtra("filename");
					String lyricPath = (String) args.getExtra("lyricPath"); 
					String filepath = (String) args.getExtra("audiofilePath");
					List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
					Cursor mCursor = ServiceManager.getMediaplayerService().getCursor();
					String curSongPath = "";
					if (mCursor != null && mCursor.getCount() != 0) {
						curSongPath = mCursor.getString(mCursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_FILEPATH));
					}
					

					for (int i = 0; i <  lyricList.size(); i++) {
						Map<String, Object> map = new HashMap<String, Object>();
//						System.out.println(lyricList.get(i));
						//Log.d("DownloadLyric","lyricList[" + i + "] = " + lyricList.get(i));
						map.put("title", lyricList.get(i));
						list.add(map);
					}

					//Log.d("DownloadLyric","filename = " + filename);
					gFilename = filename;
					gDuration = duration;
					gLyricPath = lyricPath;
					gfilePath = filepath;

					Context contextlrc = null;
					if (!(Boolean)args.getExtra("isKmedia")) {
					    contextlrc = ServiceManager.getAmtMedia();
					} else {
						contextlrc = ServiceManager.getkmediaPlayer();
					}
                    if (dialog!=null && dialog.isShowing()) {
						dialog.dismiss();
					}
					CustomDialog.Builder customBuilder = new CustomDialog.Builder(
							contextlrc);
					customBuilder
							.setTitle(filename/*context.getString(R.string.lyric_choose_please)*/)
							.setLayoutXml(R.layout.lyric_select_dialog)
							.setWhichViewVisible(CustomDialog.contentIsListView)
							/*.setCheckBoxText()*/
							.setListViewData(list)
							.setLayoutID(CustomDialog.LISTVIEW_ITEM_TEXTVIEW)
							.setOnItemClickListener(itemClickListener)
							.setPositiveButton(context.getString(R.string.custom_dialog_button_cancel),
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
//											stop();
											dialog.dismiss();
										}
									});
					dialog = customBuilder.create();
					if (filepath.equals(curSongPath)) {		
					    dialog.show();
					}
				}
			});

			break;		
		case AUDIO_DOWNLOAD_LYRIC_FINISH:
			mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_LYRICS_FINISH_UI));
			break;
		case AUDIO_DOWNLOAD_LYRICS_ERROR:
			show = (Boolean) args.getExtra("show");
			if (show) {
				handler.post(new Runnable() {

					@Override
					public void run() {
						Toast toast = ToastUtil.getInstance().getToast(context.getString(R.string.screen_audio_download_lyric_fail));
						toast.setDuration(Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
					}
				});
			}
			break;
		case AUDIO_DOWNLOAD_LYRIC:
			new Thread() {
				@Override
				public void run() {
					DownloadLyric downloadLyric = new DownloadLyric(args);
					downloadLyric.downloadLyrics();
				};
			}.start();
			break;
		case AUDIO_DOWNLOAD_ERROR:
			MediaApplication.logD(MediaService.class, "AUDIO_DOWNLOAD_ERROR");
			show = (Boolean) args.getExtra("show");
			if (show) {
				MediaApplication.logD(MediaService.class, "AUDIO_DOWNLOAD_ERROR----show");
				singer = (String) args.getExtra("singer");
				song = (String) args.getExtra("song");
				resource = (Integer) args.getExtra("resource");
				downloadType = (Integer) args.getExtra("downloadType");
				downloadId = (Integer) args.getExtra("downloadId");
				DownloadJob downloadErrorMusic;
				synchronized (dmLock) {
					downloadErrorMusic = downloadMap.get(downloadId);
				}
				if (downloadType == MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL) {
//					if (resource == RESOURCE_BAIDU) {
//						if (downloadErrorMusic != null) {
//							MediaApplication.logD(MediaService.class, "原唱百度下载失败，改用搜狗");
//							downloadErrorMusic.setDownloadStartPos(0);
//							downloadErrorMusic.setDownloadResource(RESOURCE_SOUGOU);
//							mDownloadTask = new DownloadTask(downloadErrorMusic);
//							downloadErrorMusic.setDownloadTask(mDownloadTask);
//							mDownloadTask.execute(DOWNLOAD_START_ON_ZERO);
//							break;
//						}
//					} 
				} else {
//					if (resource == RESOURCE_5SING) {
//						if (downloadErrorMusic != null) {
//							MediaApplication.logD(MediaService.class, "伴奏5SING下载失败，改用OK99");
//							downloadErrorMusic.setDownloadStartPos(0);
//							downloadErrorMusic.setDownloadResource(RESOURCE_OK99);
//							mDownloadTask = new DownloadTask(downloadErrorMusic);
//							downloadErrorMusic.setDownloadTask(mDownloadTask);
//							mDownloadTask.execute(DOWNLOAD_START_ON_ZERO);
//							break;
//						}
//					}
				}
				MediaApplication.logD(MediaService.class, "下载失败");
				handler.post(new Runnable() {
					@Override
					public void run() {
						Toast toast = ToastUtil.getInstance().getToast(song + context.getString(R.string.screen_audio_download_fail));
						toast.setDuration(Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
					}
				});
			}
			mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_PAUSE));
			break;
		case AUDIO_HISTORY_CLEAR:
			synchronized (hLock) {
				historySongs.clear();
			}
			break;
		case AUDIO_BEEN_SINGING:
			synchronized (hLock) {
				songId = (Integer) args.getExtra("id");
				Iterator<IMediaEventArgs> iterator = historySongs.iterator();
				IMediaEventArgs eventArgs;
				while (iterator.hasNext()) {
					eventArgs = iterator.next();
					if ((Integer) eventArgs.getExtra("id") == songId) {
						iterator.remove();
						break;
					}
				}
				historySongs.add(0, args);
			}
			mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_BEEN_SINGING_UI));
			break;
		case AUDIO_DOWNLOAD_RESUME:
			downloadId = (Integer) args.getExtra("downloadId");
			status = mediaDB.queryAudioStatusFromDownload(downloadId);
			if(status == IMediaService.STATE_BEGIN || status == IMediaService.STATE_FINISHED || status == -1 ){
				break;
			}
			values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_STATUS, STATE_WAIT);
			mediaDB.updateDownloadAudio(downloadId, values);
			synchronized (dmLock) {
			downloadMap.remove(downloadId);
		    }
			startDownload();
			mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_RESUME_UI));
			break;
			
		case AUDIO_DOWNLOAD_ACCOMPANY://原唱已经下载OK了，准备下载伴奏
			downloadId = (Integer) args.getExtra("downloadId");
			songId = (Integer) args.getExtra("songId");
			songType = (Integer) args.getExtra("songType");
			downloadType = (Integer) args.getExtra("downloadType");
			song = (String) args.getExtra("song");
			singer = (String) args.getExtra("singer");
			if (!mediaDB.inDownload(downloadId)) {
				if (mediaDB.inDownload(song, singer)) {
					ServiceManager.getAmtMediaHandler().post(new Runnable() {
						@Override
						public void run() {
							Toast toast = ToastUtil.getInstance().getToast(
									song + context.getString(R.string.screen_audio_download_in));
							toast.setDuration(Toast.LENGTH_SHORT);
							toast.setGravity(Gravity.CENTER, 0, 0);
							toast.show();
						}
					});
					break;
				}
				values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_SONG_ID, songId);
				values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_SONG_NAME, song);
				values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_SONG_SINGER, singer);
				values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_SONG_TYPE, songType);
				values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_RESOURCE, RESOURCE_5SING);
				values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_STATUS, STATE_WAIT);
				if (singer.equals("")){
					fAccompanyPath = MediaApplication.accompanyPath + song.replace("/", "_") + IMediaService.ACCOMPANY_SUFFIX;
					accompanyPath = fAccompanyPath + IMediaService.TMP_SUFFIX;
				} else {
					fAccompanyPath = MediaApplication.accompanyPath + singer.replace("/", "_") + "-" + song.replace("/", "_") + IMediaService.ACCOMPANY_SUFFIX;
					accompanyPath = fAccompanyPath + IMediaService.TMP_SUFFIX;
				}
				values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_PATH, fAccompanyPath);
				values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_TYPE, MediaDatabaseHelper.DOWNLOAD_TYPE_ACCOMPANY);
				mediaDB.addToDownload(values);
				MediaApplication.logD(MediaService.class, "本地原唱以前已经下载OK了，下载表里面没有原唱下载记录，准备下载伴奏");
				startDownload();
				mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_UI));
			} else if (mediaDB.inDownload(downloadId) && downloadType == MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL){
				values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_SONG_ID, songId);
				values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_SONG_NAME, song);
				values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_SONG_SINGER, singer);
				values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_SONG_TYPE, songType);
				values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_RESOURCE, RESOURCE_5SING);
				values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_STATUS, STATE_WAIT);
				if (singer.equals("")){
					fAccompanyPath = MediaApplication.accompanyPath + song.replace("/", "_") + IMediaService.ACCOMPANY_SUFFIX;
					accompanyPath = fAccompanyPath + IMediaService.TMP_SUFFIX;
				} else {
					fAccompanyPath = MediaApplication.accompanyPath + singer.replace("/", "_") + "-" + song.replace("/", "_") + IMediaService.ACCOMPANY_SUFFIX;
					accompanyPath = fAccompanyPath + IMediaService.TMP_SUFFIX;
				}
				values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_PATH, fAccompanyPath);
				values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_TYPE, MediaDatabaseHelper.DOWNLOAD_TYPE_ACCOMPANY);
				mediaDB.addToDownload(values);
				MediaApplication.logD(MediaService.class, "本地原唱已经下载OK了,下载表里面存在原唱下载记录，准备下载伴奏");
				mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_UI));
				startDownload();
			}
			break;
		case AUDIO_DOWNLOAD_FORM_WEB:
			//Log.e("AUDIO_DOWNLOAD_FORM_WEB","AUDIO_DOWNLOAD_FORM_WEB in");
			downloadId = (Integer) args.getExtra("downloadId");
			resource = (Integer) args.getExtra("downloadResource");
			song = (String) args.getExtra("song");
			singer = (String) args.getExtra("singer");
			url = (String) args.getExtra("url");
			String extype =(String) args.getExtra("downloadType");
//			size = (Long) args.getExtra("size");
			if (!mediaDB.inDownload(downloadId)) {
				if (mediaDB.inDownload(song, singer)) {
					ServiceManager.getAmtMediaHandler().post(new Runnable() {

						@Override
						public void run() {
							Toast toast = ToastUtil.getInstance().getToast(
									song + context.getString(R.string.screen_audio_download_in));
							toast.setDuration(Toast.LENGTH_SHORT);
							toast.setGravity(Gravity.CENTER, 0, 0);
							toast.show();
						}
					});
					break;
				}
				values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_SONG_NAME, song);
				values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_SONG_SINGER, singer);
				if(extype.equals("mp3")){
					if(singer.length()!=0){
						fPath = MediaApplication.savePath + singer.replace("/", "_") + "-" + song.replace("/", "_") + IMediaService.AUDIO_SUFFIX;
					}else{
						fPath = MediaApplication.savePath + song.replace("/", "_") + IMediaService.AUDIO_SUFFIX;
					}
				}else{
					if(singer.length()!=0){
						fPath = MediaApplication.savePath + singer.replace("/", "_") + "-" + song.replace("/", "_") + "." + extype;
					}else{
						fPath = MediaApplication.savePath + song.replace("/", "_") + "." + extype;
					}
				}
				path = fPath + IMediaService.TMP_SUFFIX;
				values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_PATH, fPath);
				values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_RESOURCE, resource);
				values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_URL, url);
//				values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_TOTAL_SIZE, size);
				values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_STATUS, STATE_WAIT);
				values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_TYPE, MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL);
				mediaDB.addToDownload(values);
				startDownload();
				mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_UI));
			}
			break;
		case AUDIO_DOWNLOAD:
			downloadId = (Integer) args.getExtra("downloadId");
			songId = (Integer) args.getExtra("songId");
			songType = (Integer) args.getExtra("songType");
			//path = (String) args.getExtra("path");
			resource = (Integer) args.getExtra("downloadResource");
			downloadWithAccompany = (Integer) args.getExtra("downloadWithAccompany");
			song = (String) args.getExtra("song");
			singer = (String) args.getExtra("singer");
			if (!mediaDB.inDownload(downloadId)) {
				if (mediaDB.inDownload(song, singer)) {
					ServiceManager.getAmtMediaHandler().post(new Runnable() {
						@Override
						public void run() {
							Toast toast = ToastUtil.getInstance().getToast(
									song + context.getString(R.string.screen_audio_download_in));
							toast.setDuration(Toast.LENGTH_SHORT);
							toast.setGravity(Gravity.CENTER, 0, 0);
							toast.show();
						}
					});
					break;
				}
				values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_SONG_NAME, song);
				values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_SONG_SINGER, singer);
				values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_SONG_ID, songId);
				values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_SONG_TYPE, songType);
				if (singer.equals("")){
					fPath = MediaApplication.savePath + song.replace("/", "_") + IMediaService.AUDIO_SUFFIX;
					path = fPath + IMediaService.TMP_SUFFIX;
				} else {
					fPath = MediaApplication.savePath + singer.replace("/", "_") + "-" + song.replace("/", "_") + IMediaService.AUDIO_SUFFIX;
					path = fPath + IMediaService.TMP_SUFFIX;
				}
				values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_PATH, fPath);
				values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_RESOURCE, resource);
				values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_STATUS, STATE_WAIT);
				values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_TYPE, MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL);
				mediaDB.addToDownload(values);
				if(downloadWithAccompany == IMediaService.DOWNLOAD_WITH_ACCOMPANY){
					if (singer.equals("")){
						fAccompanyPath = MediaApplication.accompanyPath + song.replace("/", "_") + IMediaService.ACCOMPANY_SUFFIX;
						accompanyPath = fAccompanyPath + IMediaService.TMP_SUFFIX;
					} else {
						fAccompanyPath = MediaApplication.accompanyPath + singer.replace("/", "_") + "-" + song.replace("/", "_") + IMediaService.ACCOMPANY_SUFFIX;
						accompanyPath = fAccompanyPath + IMediaService.TMP_SUFFIX;
					}
					values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_RESOURCE, RESOURCE_5SING);
					values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_PATH, fAccompanyPath);
					values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_TYPE, MediaDatabaseHelper.DOWNLOAD_TYPE_ACCOMPANY);
					mediaDB.addToDownload(values);
					MediaApplication.logD(MediaService.class, "向下载数据库分别插入一条原唱和伴奏的下载");
				} else {
					MediaApplication.logD(MediaService.class, "向下载数据库插入一条原唱的下载");
				}
				startDownload();
				mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_UI));
			}
			break;
		case AUDIO_DOWNLOAD_CONTINUE:
			handler.post(new Runnable() {
				@Override
				public void run() {
					String preUrl = (String) args.getExtra("url");
					String downloadUrl;
					int xcode_pos = preUrl.indexOf("?xcode=");
					if (xcode_pos > 0) {
						downloadUrl = preUrl.substring(0,
								preUrl.indexOf("?xcode="))
								+ DownloadTask
										.getXcodeFromPath(DownloadTask.XCODE_INFO_SERVER_URL);
					} else {
						downloadUrl = preUrl;
					}
					long size = (Long) args.getExtra("size");
					int downloadId = (Integer) args.getExtra("downloadId");
					String path = (String) args.getExtra("path");
					MediaApplication.logD(MediaApplication.class, "断点续传：path  "
							+ size);
					final DownloadJob download;
					download = new DownloadJob(args, true);
					synchronized (dmLock) {
						downloadMap.put(downloadId, download);
					}
					download.setDownloadStartPos(new File(path).length());
					download.setDownloadTotalSize(size);
					download.setDownloadUrl(downloadUrl);
					mDownloadTask = new DownloadTask(download);
					download.setDownloadTask(mDownloadTask);
					mDownloadTask.execute(DOWNLOAD_START_ON_RANGE);
				}
			});
			break;
		case AUDIO_DOWNLOAD_FINISH:
			downloadId = (Integer) args.getExtra("downloadId");
			downloadType = (Integer) args.getExtra("downloadType");
			song = (String) args.getExtra("song");
			singer = (String) args.getExtra("singer");
			fPath = (String) args.getExtra("fPath");
			path = (String) args.getExtra("path");
			resource = (Integer) args.getExtra("resource");
			MediaApplication.logD(MediaService.class, "下载完成 , path:" + fPath);
			
			file = new File(path);
			File finished = new File(fPath);
			if(finished.exists()) {
				MediaApplication.logD(MediaService.class, "finished.exists()");
				finished.delete();
				mediaDB.deleteAudio(fPath);
				mediaScanner.deleteFile(finished);
			} else {
				mediaDB.deleteAudio(fPath);
				mediaScanner.deleteFile(finished);
			}
			file.renameTo((finished));
			
			values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_FINISH_DATE, System.currentTimeMillis());
			values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_PATH, fPath);
			values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_STATUS, STATE_FINISHED);
//			values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_SIZE, PROGRESS_FINISHED);
			mediaDB.updateDownloadAudio(downloadId, values);
			
			if (downloadType == MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL) {
					if (accept(fPath)) {
						File sf = new File(fPath);
						if (sf != null && sf.exists()) {
							MediaApplication.logD(MediaService.class, "STATE_FINISHED: receive--"+ downloadId +"##"+ fPath);
							buidValues(song, singer, fPath);
							AudioFile af = null;
							try {
								if(resource != IMediaService.RESOURCE_SEARCH){
									af = AudioFileIO.read(sf);
									AudioFileIO.delete(af);
									Tag t = null;
									t = af.getTag();
									String album = null;
									String year = null;
									String comment = null;
									String genre = null;
									if(t != null){
										album = t.getFirst(FieldKey.ALBUM);
										year = t.getFirst(FieldKey.YEAR);
										comment = t.getFirst(FieldKey.COMMENT);
										genre = t.getFirst(FieldKey.GENRE);
									}
									Tag tag = null;
									if ((tag = af.createDefaultTag()) != null) {
										af.setTag(tag);
										tag.addField(FieldKey.TITLE, song);
										tag.addField(FieldKey.ARTIST, singer);
										if(album !=null && !album.equals("")){
											tag.addField(FieldKey.ALBUM, album);
										}
										if(year !=null && !year.equals("")){
											tag.addField(FieldKey.YEAR, year);
										}
										if(comment !=null && !comment.equals("")){
											tag.addField(FieldKey.COMMENT, comment);
										}
										if(genre !=null && !genre.equals("")){
											tag.addField(FieldKey.GENRE, genre);
										}
										af.commit();
									}
								}
							} catch (CannotReadException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (TagException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (ReadOnlyFileException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (InvalidAudioFrameException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (CannotWriteException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
			}else{
				mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.KMEDIA_UPDATE_DATA));
			}
			mediaScanner.scan(finished); 
			synchronized (dmLock) {
				downloadMap.remove(downloadId);
			}
			mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_FINISH_UI));
			startDownload();
			break;
		case AUDIO_DOWNLOAD_PAUSE:
			downloadId = (Integer) args.getExtra("downloadId");
			status = mediaDB.queryAudioStatusFromDownload(downloadId);
			if(status == IMediaService.STATE_FINISHED || status == -1 ){
				break;
			}
			synchronized (dmLock) {
				downloadMusic = downloadMap.get(downloadId);
			}
			if (downloadMusic != null) {
				if(downloadMusic.getDownloadTask() != null) {
				downloadMusic.getDownloadTask().cancel(true);
				downloadMusic.setDownloadStateFlag(IMediaService.STATE_PAUSE);
				}
			}
			synchronized (dmLock) {
				downloadMap.remove(downloadId);
			}//暂停了应该让出队列
			values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_STATUS, STATE_PAUSE);
			mediaDB.updateDownloadAudio(downloadId, values);
			mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_PAUSE_UI));
			startDownload();
			break;
		case AUDIO_DOWNLOAD_BEGIN:
			downloadId = (Integer) args.getExtra("downloadId");
			size = (Long) args.getExtra("size");
			url = (String) args.getExtra("url");
			MediaApplication.logD(MediaService.class, "下载正式开始, size : " + size + " url:" + url);
			values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_STATUS, STATE_BEGIN);
			values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_TOTAL_SIZE, size);
			values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_URL, url);
			mediaDB.updateDownloadAudio(downloadId, values);
			mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_BEGIN_UI));
			break;
		case AUDIO_DOWNLOAD_REBEGIN:
			downloadId = (Integer) args.getExtra("downloadId");
			values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_STATUS, STATE_BEGIN);
			mediaDB.updateDownloadAudio(downloadId, values);
			mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_REBEGIN_UI));
			break;
		case AUDIO_DOWNLOAD_CANCEL:
			downloadId = (Integer) args.getExtra("downloadId");
			status = mediaDB.queryAudioStatusFromDownload(downloadId);
			if(status == IMediaService.STATE_FINISHED || status == -1 ){
				break;
			}
			path = (String) args.getExtra("path");
			downloadType = (Integer) args.getExtra("downloadType");
			songId = (Integer) args.getExtra("songId");
			synchronized (dmLock) {
				downloadMusic = downloadMap.remove(downloadId);
			}
			if (downloadMusic != null) {
				if(downloadMusic.getDownloadTask() != null) {
					downloadMusic.getDownloadTask().cancel(true);
					downloadMusic.setDownloadStateFlag(IMediaService.STATE_CANCEL);
				}
			}
			file = new File(path);
			file.delete();
			mediaScanner.deleteFile(file);
			if (downloadType ==  MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL){
				if(songId == 0){
					mediaDB.deleteAudioFromDownload(downloadId);
				} else {
					mediaDB.deleteOriginalAndAccompanyFromDownload(songId);
				}
			} else {
				mediaDB.deleteAudioFromDownload(downloadId);
			}
			startDownload();
			mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_CANCEL_UI));
			break;
		case AUDIO_DOWNLOAD_DELETE:
			downloadId = (Integer) args.getExtra("downloadId");
//			fPath = (String) args.getExtra("fPath");
//			file = new File(fPath);
//			file.delete();
//			mediaScanner.deleteFile(file);
			mediaDB.deleteAudioFromDownload(downloadId);
			mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_DELETE_UI));
			break;
		case AUDIO_DOWNLOAD_START:
			//Log.e("AUDIO_DOWNLOAD_START","AUDIO_DOWNLOAD_START in");
			song = (String) args.getExtra("song");
			singer = (String) args.getExtra("singer");
			url = (String) args.getExtra("url");
			size = (Long) args.getExtra("size");
			downloadId = (Integer) args.getExtra("downloadId");
			resource = (Integer) args.getExtra("resource");
			downloadType = (Integer) args.getExtra("downloadType");
			//values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_STATUS, STATE_WAIT);// 本来就是STATE_WAIT
			//mediaDB.updateDownloadAudio(downloadId, values);
			new Thread() {
				@Override
				public void run() {
					try {
						final DownloadJob downloadMusic;
						synchronized (dmLock) {
							downloadMusic = downloadMap.get(downloadId);
						}
						if (downloadMusic != null) {
							switch (downloadType) {
							case MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL:
//								if (resource == RESOURCE_BAIDU) {
//									MediaApplication.logD(MediaService.class, "start a download, type : MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL");
//									downloadMusic.setDownloadResource(RESOURCE_BAIDU);
//									downloadMusic.setDownloadStartPos(0);
//									handler.post(new Runnable(){
//										@Override
//										public void run() {
//											mDownloadTask = new DownloadTask(downloadMusic);
//											downloadMusic.setDownloadTask(mDownloadTask);
//											mDownloadTask.execute(DOWNLOAD_START_ON_ZERO);
//										}});
//									
//									break;
//								} else if (resource == RESOURCE_SEARCH) {
//									MediaApplication.logD(MediaService.class, "start a download, type : MediaDatabaseHelper.DOWNLOAD_TYPE_WEB");
//									//downloadMusic.downloadFile(song, singer, size, url);
//									downloadMusic.setDownloadResource(RESOURCE_SEARCH);
//									downloadMusic.setDownloadStartPos(0);
//									downloadMusic.setDownloadTotalSize(size);
//									downloadMusic.setDownloadUrl(url);
//									downloadMusic.setSinger(singer);
//									downloadMusic.setSong(song);
//									handler.post(new Runnable(){
//										@Override
//										public void run() {
//											mDownloadTask = new DownloadTask(downloadMusic);
//											downloadMusic.setDownloadTask(mDownloadTask);
//											mDownloadTask.execute(DOWNLOAD_START_ON_ZERO);
//										}});
//									break;
//								}
								MediaApplication.logD(MediaService.class, "start a download, type : MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL");
								//downloadMusic.setDownloadResource(RESOURCE_BAIDU);
								downloadMusic.setDownloadStartPos(0);
								handler.post(new Runnable(){
									@Override
									public void run() {
										mDownloadTask = new DownloadTask(downloadMusic);
										downloadMusic.setDownloadTask(mDownloadTask);
										mDownloadTask.execute(DOWNLOAD_START_ON_ZERO);
									}});
								
								break;
							case MediaDatabaseHelper.DOWNLOAD_TYPE_ACCOMPANY:
								MediaApplication.logD(MediaService.class, "start a download, type : MediaDatabaseHelper.DOWNLOAD_TYPE_ACCOMPANY");
								downloadMusic.setDownloadResource(RESOURCE_5SING);
								downloadMusic.setDownloadStartPos(0);
								handler.post(new Runnable(){
									@Override
									public void run() {
										mDownloadTask = new DownloadTask(downloadMusic);
										downloadMusic.setDownloadTask(mDownloadTask);
										mDownloadTask.execute(DOWNLOAD_START_ON_ZERO);
									}});
								break;

							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.start();
			break;
		case ADD_TO_COUNT_LOCAL:
			song = (String) args.getExtra("song");
			singer = (String) args.getExtra("singer");
			mediaDB.addCountLocalSong(song, singer);
			break;
		case ADD_TO_COUNT_KMEDIA:
			song = (String) args.getExtra("song");
			singer = (String) args.getExtra("singer");
			mediaDB.addCountKMediaSong(song, singer);
			break;
		}
		return true;
	}



	public MediaManagerDB getMediaDB() {
		return mediaDB;
	}

	@Override
	public Map<Integer, DownloadJob> getDownloadMap() {
		return downloadMap;
	}

	public List<IMediaEventArgs> getHistoryAudios() {
		return historySongs;
	}

	public int getDownloadSize() {
		return downloadSize;
	}

	public void setDownloadSize(int downloadSize) {
		this.downloadSize = downloadSize;
	}

	public byte[] getDmLock() {
		return dmLock;
	}

	public byte[] gethLock() {
		return hLock;
	}

	public byte[] getDlLock() {
		return dlLock;
	}



	public MediaScanner getMediaScanner() {
		return mediaScanner;
	}

	public List<String> getRefreshPath() {
		setRefreshPath();
		return FilePathList;
	}

	public void setRefreshPath() {
		String s;
		FilePathList.clear();
		Cursor cursor = mediaDB.querySongFile();
		int pos;

		while (cursor.moveToNext())
		{
			if ((s = getFilePath(cursor.getString(0))) != null && !FilePathList.contains(s))
			{
				if (!s.matches(MediaApplication.deleteRecordPath))
				{
					FilePathList.add(s);
				}
			}
		}
		
		pos = FilePathList.indexOf(MediaApplication.ScanSavePath);
		if (pos > 0) 	
		{
			s = FilePathList.get(0);
			FilePathList.set(0, MediaApplication.ScanSavePath);
			FilePathList.set(pos, s);
		}
	}
	
	public String getFilePath(String fullpath)
	{
		String s = null;
		int  endpos = fullpath.lastIndexOf("/");
		if (endpos > -1)
		{
			 s = fullpath.substring(0, endpos);
		}
		return s;
	}

	
	public boolean accept(String filename) {
		final String[] styles = { ".mp3"};
		int start = filename.lastIndexOf(".");
		if (start != -1) {
			String style = filename.substring(start);
			for (String s : styles) {
				if (s.equals(style.toLowerCase())) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void buidValues(String songName, String singerName, final String filePath){
		ContentValues contentValues = new ContentValues();
		String displayName;
		if(singerName.equals("")){
			displayName = songName.replace("/", "_") + IMediaService.AUDIO_SUFFIX;
		} else {
			displayName = singerName.replace("/", "_") + "-" + songName.replace("/", "_") + IMediaService.AUDIO_SUFFIX;
		}
		contentValues.put(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_DISPALYNAME, displayName);
    	contentValues.put(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ARTISTNAME, singerName);
    	contentValues.put(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_FILEPATH, filePath);
    	contentValues.put(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_SONGNAME, songName);
    	contentValues.put(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ALBUMNAME, "aMusic");
    	mediaDB.addDownloadAudioToSongs(contentValues);
    	handler.postDelayed(new Runnable(){
			@Override
			public void run() {
				mediaDB.updateDonwloadAudioToSongs(filePath, 1);
			}}, 6000);
	}

}
