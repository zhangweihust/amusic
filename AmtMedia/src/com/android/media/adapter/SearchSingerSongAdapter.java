package com.android.media.adapter;

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

import com.amusic.media.R;
import com.android.media.MediaApplication;
import com.android.media.download.DownloadJob;
import com.android.media.event.impl.MediaEventArgs;
import com.android.media.provider.MediaDatabaseHelper;
import com.android.media.provider.MediaDictionaryDatabaseHelper;
import com.android.media.screens.Screen;
import com.android.media.services.IMediaService;
import com.android.media.services.impl.ServiceManager;

public class SearchSingerSongAdapter extends CursorAdapter {
	private LayoutInflater inflater;
	private final IMediaService mediaService;
	private final Screen screen;

	public SearchSingerSongAdapter(Context context, Cursor c, Screen screen) {
		super(context, c);
		inflater = LayoutInflater.from(context);
		mediaService = ServiceManager.getMediaService();
		this.screen = screen;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.screen_search_list_item2, null);
		KMediaSingerSongItem mediaSingerSongItem = new KMediaSingerSongItem();
	//	mediaSingerSongItem.progressBar = (TextView) view.findViewById(R.id.screen_search_list_item_status);
		mediaSingerSongItem.songText = (TextView) view.findViewById(R.id.screen_search_list_item_above);
//		mediaSingerSongItem.albumText = (TextView) view.findViewById(R.id.screen_search_list_item_below);
//		mediaSingerSongItem.albumText.setVisibility(View.GONE);
		mediaSingerSongItem.songStatus=(TextView) view.findViewById(R.id.screen_search_list_item_status);
		//mediaSingerSongItem.songStatus.setVisibility(View.VISIBLE);
		view.setTag(R.layout.screen_search_list_item2, mediaSingerSongItem);
		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final KMediaSingerSongItem mediaSingerSongItem = (KMediaSingerSongItem) view.getTag(R.layout.screen_search_list_item2);
		final String song = cursor.getString(cursor.getColumnIndex(MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SONG));
		final String singer = cursor.getString(cursor.getColumnIndex(MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SINGER));
		final int songId = cursor.getInt(cursor.getColumnIndex(MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_ID));
		final Integer downloadId = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_ID_PREFIX + MediaDatabaseHelper.COLUMN_DOWNLOAD_ID));
		mediaSingerSongItem.downloadId = downloadId;
		MediaApplication.logD(SearchCategorySongAdapter.class, "bindView:"+ mediaSingerSongItem.downloadId + "/" + downloadId);
		int status = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_STATUS));
		long size = cursor.getLong(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_TOTAL_SIZE));
		final int downloadType = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_TYPE));
		final int downloadResource = cursor.getInt(cursor.getColumnIndex(MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_FROM));
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
					mediaSingerSongItem.songStatus.setText(screen.getString(R.string.screen_audio_download_all_finished));
					status = IMediaService.STATE_ALL_FINISHED;
				} else {
					mediaSingerSongItem.songStatus.setText(screen.getString(R.string.screen_audio_download_orginal_finished));
					status = IMediaService.STATE_ORIGINAL_FINISHED;
					if(downloadType == MediaDatabaseHelper.DOWNLOAD_TYPE_ACCOMPANY) {
//						MediaApplication.logD(MediaManagerDB.class, "下载表里面存在原唱伴奏，但是本地的伴奏被删除了");
						mediaService.getMediaDB().deleteAudioFromDownload(downloadId);
						screen.refresh();
					}
				}
				mediaSingerSongItem.songStatus.setVisibility(View.VISIBLE);
			} else {
//				MediaApplication.logD(MediaManagerDB.class, "下载表里面存在原唱伴奏，但是本地的原唱被删除了");
				mediaService.getMediaDB().deleteOriginalAndAccompanyFromDownload(songId);
				if(accompanyFile.exists()){
					accompanyFile.delete();
				}
				screen.refresh();
			}
			break;
		case IMediaService.STATE_BEGIN:
//			MediaApplication.logD(SearchCategorySongAdapter.class, "IMediaService.STATE_BEGIN:");
			file = new File(path);
			if (!file.exists()) {
				//mediaService.getMediaDB().deleteAudioFromDownload(downloadId);
				if(downloadType == MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL) {
					mediaSingerSongItem.songStatus.setText(screen.getString(R.string.screen_audio_download_type_orginal) + IMediaService.PROGRESS_WAIT);
				} else  {
					mediaSingerSongItem.songStatus.setText(screen.getString(R.string.screen_audio_download_type_accompany) + IMediaService.PROGRESS_WAIT);
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
							MediaApplication.logD(SearchCategorySongAdapter.class, "IMediaService.STATE_BEGIN:"+ mediaSingerSongItem.downloadId + "/" + mJob.getDownloadId());
							if(mJob != null)
							if (mediaSingerSongItem.downloadId == mJob.getDownloadId()) {
								if(mJob.getDownloadType() == MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL) {
									MediaApplication.logD(SearchCategorySongAdapter.class, "IMediaService.STATE_BEGIN:"+ String.valueOf(b.getLong("currentBytes") * 100 / b.getLong("totalBytes")) + "%");
									mediaSingerSongItem.songStatus.setText(screen.getString(R.string.screen_audio_download_type_orginal) + String.valueOf(b.getLong("currentBytes") * 100 / b.getLong("totalBytes")) + "%");
								} else  {
									mediaSingerSongItem.songStatus.setText(screen.getString(R.string.screen_audio_download_type_accompany) + String.valueOf(b.getLong("currentBytes") * 100 / b.getLong("totalBytes")) + "%");
								}
							} else {
								MediaApplication.logD(SearchSingerSongAdapter.class, "不是对应的HANDLER消息，不可以刷新");
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
				if (size != 0) {
					if(downloadType == MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL) {
						mediaSingerSongItem.songStatus.setText(screen.getString(R.string.screen_audio_download_type_orginal) + String.valueOf(file.length() * 100 / size) + "%");
					} else  {
						mediaSingerSongItem.songStatus.setText(screen.getString(R.string.screen_audio_download_type_accompany) + String.valueOf(file.length() * 100 / size) + "%");
					}
					//mediaSingerSongItem.status.setText(String.valueOf(file.length() * 100 / size) + "%");
				} else {
					if(downloadType == MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL) {
						mediaSingerSongItem.songStatus.setText(screen.getString(R.string.screen_audio_download_type_orginal) + IMediaService.PROGRESS_WAIT);
					} else  {
						mediaSingerSongItem.songStatus.setText(screen.getString(R.string.screen_audio_download_type_accompany) + IMediaService.PROGRESS_WAIT);
					}
					//mediaSingerSongItem.status.setText(IMediaService.PROGRESS_WAIT);
				}
			}
			break;
		case IMediaService.STATE_PAUSE:
			file = new File(path);
			if (!file.exists()) {
				//mediaService.getMediaDB().deleteAudioFromDownload(downloadId);
				if(downloadType == MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL) {
					mediaSingerSongItem.songStatus.setText(screen.getString(R.string.screen_audio_download_type_orginal) + IMediaService.PROGRESS_WAIT);
				} else  {
					mediaSingerSongItem.songStatus.setText(screen.getString(R.string.screen_audio_download_type_accompany) + IMediaService.PROGRESS_WAIT);
				}
			} else {
				if (size != 0) {
					if(downloadType == MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL) {
						mediaSingerSongItem.songStatus.setText(screen.getString(R.string.screen_audio_download_type_orginal) + String.valueOf(file.length() * 100 / size) + "%");
					} else {
						mediaSingerSongItem.songStatus.setText(screen.getString(R.string.screen_audio_download_type_accompany) + String.valueOf(file.length() * 100 / size) + "%");
					}
					//mediaSingerSongItem.status.setText(String.valueOf(file.length() * 100 / size) + "%");
				} else {
					if(downloadType == MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL) {
						mediaSingerSongItem.songStatus.setText(screen.getString(R.string.screen_audio_download_type_orginal) + IMediaService.PROGRESS_WAIT);
					} else  {
						mediaSingerSongItem.songStatus.setText(screen.getString(R.string.screen_audio_download_type_accompany) + IMediaService.PROGRESS_WAIT);
					}
					//mediaSingerSongItem.status.setText(IMediaService.PROGRESS_WAIT);
				}
			}
			break;
		case IMediaService.STATE_IN_QUEUE:
			if(downloadType == MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL) {
				mediaSingerSongItem.songStatus.setText(screen.getString(R.string.screen_audio_download_type_orginal) + screen.getString(R.string.screen_audio_download_starting));
			} else  {
				mediaSingerSongItem.songStatus.setText(screen.getString(R.string.screen_audio_download_type_accompany) + screen.getString(R.string.screen_audio_download_starting));
			}
			break;
		case IMediaService.STATE_WAIT:
			file = new File(path);
			if (file.exists()) {
				if (size != 0) {
					if(downloadType == MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL) {
						mediaSingerSongItem.songStatus.setText(screen.getString(R.string.screen_audio_download_type_orginal) + String.valueOf(file.length() * 100 / size) + "%");
					} else {
						mediaSingerSongItem.songStatus.setText(screen.getString(R.string.screen_audio_download_type_accompany) + String.valueOf(file.length() * 100 / size) + "%");
					}
					//mediaSingerSongItem.status.setText(String.valueOf(file.length() * 100 / size) + "%");
				} else {
					if(downloadType == MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL) {
						mediaSingerSongItem.songStatus.setText(screen.getString(R.string.screen_audio_download_type_orginal) + IMediaService.PROGRESS_WAIT);
					} else {
						mediaSingerSongItem.songStatus.setText(screen.getString(R.string.screen_audio_download_type_accompany) + IMediaService.PROGRESS_WAIT);
					}
					//mediaSingerSongItem.status.setText(IMediaService.PROGRESS_WAIT);
				}
			} else {
				if(downloadType == MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL) {
					mediaSingerSongItem.songStatus.setText(screen.getString(R.string.screen_audio_download_type_orginal) + IMediaService.PROGRESS_WAIT);
				} else {
					mediaSingerSongItem.songStatus.setText(screen.getString(R.string.screen_audio_download_type_accompany) + IMediaService.PROGRESS_WAIT);
				}
			}
			break;
		default://DEFAULT and CANCEL
			file = new File(fOriginalPath);
			accompanyFile = new File(fAccompanyPath);
			if (file.exists()) {
				if(accompanyFile.exists()){
					mediaSingerSongItem.songStatus.setText(screen.getString(R.string.screen_audio_download_all_finished));
					status = IMediaService.STATE_ALL_FINISHED;
				} else {
					status = IMediaService.STATE_ORIGINAL_FINISHED;
					mediaSingerSongItem.songStatus.setText(screen.getString(R.string.screen_audio_download_orginal_finished));
				}
				mediaSingerSongItem.songStatus.setVisibility(View.VISIBLE);
			} else {
				if(accompanyFile.exists()){
					accompanyFile.delete();
//					MediaApplication.logD(MediaManagerDB.class, "下载表里面没有数据，但本地伴奏存在，原唱不在，删除伴奏");
				}
				mediaSingerSongItem.songStatus.setText(screen.getString(R.string.screen_audio_download_not_started));
			}
			break;
		}
		final MediaEventArgs args = new MediaEventArgs();
		args.putExtra("song", song);
		args.putExtra("singer", singer);
		args.putExtra("songId", songId);
		args.putExtra("downloadId", downloadId);
		args.putExtra("songType", MediaDatabaseHelper.SONG_TYPE_DICTIONARY);
		args.putExtra("downloadType", downloadType);
		args.putExtra("path", path);
		args.putExtra("fPath", fPath);
		args.putExtra("status", status);
		args.putExtra("fLyricPath", fLyricPath);
		args.putExtra("lyricPath", lyricPath);
		args.putExtra("downloadResource", downloadResource);
		view.setTag(args);
		mediaSingerSongItem.songText.setText(song);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return super.getView(position, null, parent);
	}

	private class KMediaSingerSongItem {
		//private TextView progressBar;
		private TextView songText;
		//private TextView albumText;
		private TextView songStatus;
		private int downloadId;
	}
}
