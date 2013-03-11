package com.amusic.media.adapter;

import java.io.File;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.amusic.media.MediaApplication;
import com.amusic.media.R;
import com.amusic.media.download.DownloadJob;
import com.amusic.media.event.impl.MediaEventArgs;
import com.amusic.media.provider.MediaCategoryDatabaseHelper;
import com.amusic.media.provider.MediaDatabaseHelper;
import com.amusic.media.screens.Screen;
import com.amusic.media.services.IMediaService;
import com.amusic.media.services.impl.ServiceManager;

public class SearchCategorySongAdapter extends CursorAdapter {
	private final LayoutInflater inflater;
	private final IMediaService mediaService;
	private final Screen screen;

	public SearchCategorySongAdapter(Context context, Cursor c, Screen screen) {
		super(context, c);
		inflater = LayoutInflater.from(context);
		mediaService = ServiceManager.getMediaService();
		this.screen = screen;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		KMediaCategorySongItem categorySongItem = new KMediaCategorySongItem();
		View view = inflater.inflate(R.layout.screen_search_list_item2, null);
		categorySongItem.songTextView = (TextView) view.findViewById(R.id.screen_search_list_item_above);
		categorySongItem.status = (TextView) view.findViewById(R.id.screen_search_list_item_status);
		view.setTag(R.layout.screen_search_list_item2, categorySongItem);
		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final KMediaCategorySongItem categorySongItem = (KMediaCategorySongItem) view.getTag(R.layout.screen_search_list_item2);
		final String song = cursor.getString(cursor.getColumnIndex(MediaCategoryDatabaseHelper.COLUMN_CATEGORY_SONG));
		final String singer = cursor.getString(cursor.getColumnIndex(MediaCategoryDatabaseHelper.COLUMN_CATEGORY_SINGER));
		final int songId = cursor.getInt(cursor.getColumnIndex(MediaCategoryDatabaseHelper.COLUMN_CATEGORY_ID));
		final Integer downloadId = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_ID_PREFIX + MediaDatabaseHelper.COLUMN_DOWNLOAD_ID));
		categorySongItem.downloadId = downloadId;
		MediaApplication.logD(SearchCategorySongAdapter.class, "bindView:"+ categorySongItem.downloadId + "/" + downloadId);
		int status = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_STATUS));
		final int downloadType = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_TYPE));
		final int downloadResource = cursor.getInt(cursor.getColumnIndex(MediaCategoryDatabaseHelper.COLUMN_CATEGORY_FROM));
		long size = cursor.getLong(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_TOTAL_SIZE));
		final String fLyricPath = MediaApplication.lyricPath + "/" + singer.replace("/", "_") + "-" + song.replace("/", "_") + IMediaService.LYRICS_SUFFIX;
		final String lyricPath = fLyricPath + IMediaService.TMP_SUFFIX;
		final String fPath;
		final String path;
		final String fAccompanyPath;
		final String fOriginalPath;
		if (singer.equals("")){
			if(downloadType == MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL) {
				fPath = MediaApplication.savePath + song.replace("/", "_") + IMediaService.AUDIO_SUFFIX;
				path = fPath + IMediaService.TMP_SUFFIX;
			} else if (downloadType == MediaDatabaseHelper.DOWNLOAD_TYPE_ACCOMPANY){
				fPath = MediaApplication.accompanyPath + song.replace("/", "_") + IMediaService.ACCOMPANY_SUFFIX;
				path = fPath + IMediaService.TMP_SUFFIX;
			} else {
				fPath = "";
				path = "";
			}
			fAccompanyPath = MediaApplication.accompanyPath + song.replace("/", "_") + IMediaService.ACCOMPANY_SUFFIX;
			fOriginalPath = MediaApplication.savePath + song.replace("/", "_") + IMediaService.AUDIO_SUFFIX;
		} else {
			if(downloadType == MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL) {
				fPath = MediaApplication.savePath + singer.replace("/", "_") + "-" + song.replace("/", "_") + IMediaService.AUDIO_SUFFIX;
				path = fPath + IMediaService.TMP_SUFFIX;
			} else if (downloadType == MediaDatabaseHelper.DOWNLOAD_TYPE_ACCOMPANY){
				fPath = MediaApplication.accompanyPath + singer.replace("/", "_") + "-" + song.replace("/", "_") + IMediaService.ACCOMPANY_SUFFIX;
				path = fPath + IMediaService.TMP_SUFFIX;
			} else {
				fPath = "";
				path = "";
			}
			fAccompanyPath = MediaApplication.accompanyPath + singer.replace("/", "_") + "-" + song.replace("/", "_") + IMediaService.ACCOMPANY_SUFFIX;
			fOriginalPath = MediaApplication.savePath + singer.replace("/", "_") + "-" + song.replace("/", "_") + IMediaService.AUDIO_SUFFIX;
		}

		DownloadJob downloadMusic;
		File file, accompanyFile;
		ContentValues values = new ContentValues();
		switch (status) {
		case IMediaService.STATE_FINISHED:
			file = new File(fOriginalPath);
			accompanyFile = new File(fAccompanyPath);
			if (file.exists()) {
				if(accompanyFile.exists()){
					categorySongItem.status.setText(screen.getString(R.string.screen_audio_download_all_finished));
					status = IMediaService.STATE_ALL_FINISHED;
				} else {
					categorySongItem.status.setText(screen.getString(R.string.screen_audio_download_orginal_finished));
					status = IMediaService.STATE_ORIGINAL_FINISHED;
					if(downloadType == MediaDatabaseHelper.DOWNLOAD_TYPE_ACCOMPANY) {
						mediaService.getMediaDB().deleteAudioFromDownload(downloadId);
						screen.refresh();
					}
				}
				categorySongItem.status.setVisibility(View.VISIBLE);
			} else {
				mediaService.getMediaDB().deleteOriginalAndAccompanyFromDownload(songId);
				if(accompanyFile.exists()){
					accompanyFile.delete();
				}
				screen.refresh();
			}
			break;
		case IMediaService.STATE_BEGIN:
			file = new File(path);
			if (!file.exists()) {
				if(downloadType == MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL) {
					categorySongItem.status.setText(screen.getString(R.string.screen_audio_download_type_orginal) + IMediaService.PROGRESS_WAIT);
				} else  {
					categorySongItem.status.setText(screen.getString(R.string.screen_audio_download_type_accompany) + IMediaService.PROGRESS_WAIT);
				}
			} else {
				downloadMusic = mediaService.getDownloadMap().get(downloadId);
				if (downloadMusic != null) {//说明该记录正在下载进行中
					Handler handler = new Handler() {
						@Override
						public void handleMessage(Message msg) {
							super.handleMessage(msg);
							//screen.refresh();
							Bundle b = msg.getData();
							DownloadJob mJob = (DownloadJob) msg.obj;
							MediaApplication.logD(SearchCategorySongAdapter.class, "IMediaService.STATE_BEGIN:"+ categorySongItem.downloadId + "/" + mJob.getDownloadId());
							if(mJob != null)
							if (categorySongItem.downloadId == mJob.getDownloadId()) {
								if(mJob.getDownloadType() == MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL) {
									categorySongItem.status.setText(screen.getString(R.string.screen_audio_download_type_orginal) + String.valueOf(b.getLong("currentBytes") * 100 / b.getLong("totalBytes")) + "%");
								} else  {
									categorySongItem.status.setText(screen.getString(R.string.screen_audio_download_type_accompany) + String.valueOf(b.getLong("currentBytes") * 100 / b.getLong("totalBytes")) + "%");
								}
							} else {
								MediaApplication.logD(SearchCategorySongAdapter.class, "不是对应的HANDLER消息，不可以刷新");
							}
						}
					};
					synchronized (screen.gethdLock()) {
						screen.getHandlerIds().add(downloadId);
					}
					if(downloadMusic.getDownloadTask() != null)
						downloadMusic.getDownloadTask().registerHandler(handler);
				} else {//说明该记录没有进入下载，那么转为暂停
					status = IMediaService.STATE_PAUSE;
					values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_STATUS, IMediaService.STATE_PAUSE);
					mediaService.getMediaDB().updateDownloadAudio(downloadId, values);
				}
				if (size != 0) {//开始下载的记录，文件大小就不为0
					if(downloadType == MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL) {
						categorySongItem.status.setText(screen.getString(R.string.screen_audio_download_type_orginal) + String.valueOf(file.length() * 100 / size) + "%");
					} else  {
						categorySongItem.status.setText(screen.getString(R.string.screen_audio_download_type_accompany) + String.valueOf(file.length() * 100 / size) + "%");
					}
				} else {//处于等待下载的记录
					if(downloadType == MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL) {
						categorySongItem.status.setText(screen.getString(R.string.screen_audio_download_type_orginal) + IMediaService.PROGRESS_WAIT);
					} else  {
						categorySongItem.status.setText(screen.getString(R.string.screen_audio_download_type_accompany) + IMediaService.PROGRESS_WAIT);
					}
				}
			}
			break;
		case IMediaService.STATE_PAUSE:
			file = new File(path);
			if (!file.exists()) {
				if(downloadType == MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL) {
					categorySongItem.status.setText(screen.getString(R.string.screen_audio_download_type_orginal) + IMediaService.PROGRESS_WAIT);
				} else  {
					categorySongItem.status.setText(screen.getString(R.string.screen_audio_download_type_accompany) + IMediaService.PROGRESS_WAIT);
				}
			} else {
				if (size != 0) {
					if(downloadType == MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL) {
						categorySongItem.status.setText(screen.getString(R.string.screen_audio_download_type_orginal) + String.valueOf(file.length() * 100 / size) + "%");
					} else {
						categorySongItem.status.setText(screen.getString(R.string.screen_audio_download_type_accompany) + String.valueOf(file.length() * 100 / size) + "%");
					}
				} else {
					if(downloadType == MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL) {
						categorySongItem.status.setText(screen.getString(R.string.screen_audio_download_type_orginal) + IMediaService.PROGRESS_WAIT);
					} else  {
						categorySongItem.status.setText(screen.getString(R.string.screen_audio_download_type_accompany) + IMediaService.PROGRESS_WAIT);
					}
				}
			}
			break;
		case IMediaService.STATE_IN_QUEUE:
			if(downloadType == MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL) {
				categorySongItem.status.setText(screen.getString(R.string.screen_audio_download_type_orginal) + screen.getString(R.string.screen_audio_download_starting));
			} else  {
				categorySongItem.status.setText(screen.getString(R.string.screen_audio_download_type_accompany) + screen.getString(R.string.screen_audio_download_starting));
			}
			break;
		case IMediaService.STATE_WAIT:
			file = new File(path);
			if (file.exists()) {
				if (size != 0) {
					if(downloadType == MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL) {
						categorySongItem.status.setText(screen.getString(R.string.screen_audio_download_type_orginal) + String.valueOf(file.length() * 100 / size) + "%");
					} else {
						categorySongItem.status.setText(screen.getString(R.string.screen_audio_download_type_accompany) + String.valueOf(file.length() * 100 / size) + "%");
					}
				} else {
					if(downloadType == MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL) {
						categorySongItem.status.setText(screen.getString(R.string.screen_audio_download_type_orginal) + IMediaService.PROGRESS_WAIT);
					} else {
						categorySongItem.status.setText(screen.getString(R.string.screen_audio_download_type_accompany) + IMediaService.PROGRESS_WAIT);
					}
				}
			} else {
				if(downloadType == MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL) {
					categorySongItem.status.setText(screen.getString(R.string.screen_audio_download_type_orginal) + IMediaService.PROGRESS_WAIT);
				} else {
					categorySongItem.status.setText(screen.getString(R.string.screen_audio_download_type_accompany) + IMediaService.PROGRESS_WAIT);
				}
			}
			break;
		default://在下载表里面没有找到对应字典记录的进入
			file = new File(fOriginalPath);
			accompanyFile = new File(fAccompanyPath);
			if (file.exists()) {
				if(accompanyFile.exists()){
					categorySongItem.status.setText(screen.getString(R.string.screen_audio_download_all_finished));
					status = IMediaService.STATE_ALL_FINISHED;
				} else {
					status = IMediaService.STATE_ORIGINAL_FINISHED;
					categorySongItem.status.setText(screen.getString(R.string.screen_audio_download_orginal_finished));
				}
			} else {
				if(accompanyFile.exists()){
					accompanyFile.delete();
				}
				categorySongItem.status.setText(screen.getString(R.string.screen_audio_download_not_started));
			}
			break;
		}
		final MediaEventArgs args = new MediaEventArgs();
		args.putExtra("song", song);
		args.putExtra("singer", singer);
		args.putExtra("songId", songId);
		args.putExtra("downloadId", downloadId);
		args.putExtra("songType", MediaDatabaseHelper.SONG_TYPE_CATEGORY);
		args.putExtra("downloadType", downloadType);
		args.putExtra("path", path);
		args.putExtra("fPath", fPath);
		args.putExtra("status", status);
		args.putExtra("fLyricPath", fLyricPath);
		args.putExtra("lyricPath", lyricPath);
		args.putExtra("downloadResource", downloadResource);
		view.setTag(args);
		String songname=song+" - "+singer;
		if(singer.equals("")){
			songname=song;
		}
		categorySongItem.songTextView.setText(songname);
	}

	private class KMediaCategorySongItem {
		private TextView songTextView;
		private TextView status;
		private int downloadId;
	}

}
