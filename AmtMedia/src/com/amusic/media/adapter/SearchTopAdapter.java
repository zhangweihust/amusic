package com.amusic.media.adapter;

import java.io.File;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.amusic.media.MediaApplication;
import com.amusic.media.R;
import com.amusic.media.download.DownloadJob;
import com.amusic.media.event.impl.MediaEventArgs;
import com.amusic.media.provider.MediaDatabaseHelper;
import com.amusic.media.provider.MediaDictionaryDatabaseHelper;
import com.amusic.media.screens.Screen;
import com.amusic.media.services.IMediaService;
import com.amusic.media.services.impl.ServiceManager;

public class SearchTopAdapter extends CursorAdapter {
	private LayoutInflater inflater;
	private final IMediaService mediaService;
	private final Screen screen;

	public SearchTopAdapter(Context context, Cursor c, Screen screen) {
		super(context, c);
		this.screen = screen;
		inflater = LayoutInflater.from(context);
		mediaService = ServiceManager.getMediaService();
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.screen_search_list_item, null);
		KMediaSongItem mediaSongItem = new KMediaSongItem();
		mediaSongItem.status = (TextView) view.findViewById(R.id.screen_search_list_item_status);
		mediaSongItem.singerTextView = (TextView) view.findViewById(R.id.screen_search_list_item_below);
		mediaSongItem.songTextView = (TextView) view.findViewById(R.id.screen_search_list_item_above);

		view.setTag(R.layout.screen_search_list_item, mediaSongItem);
		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final String song = cursor.getString(cursor.getColumnIndex(MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SONG));
		final String singer = cursor.getString(cursor.getColumnIndex(MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SINGER));
		final int songId = cursor.getInt(cursor.getColumnIndex(MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_ID));
		final int downloadId = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_ID_PREFIX + MediaDatabaseHelper.COLUMN_DOWNLOAD_ID));
		int status = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_STATUS));
		long size = cursor.getLong(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_TOTAL_SIZE));
		final KMediaSongItem mediaSongItem = (KMediaSongItem) view.getTag(R.layout.screen_search_list_item);
		final String fLyricPath = MediaApplication.lyricPath + "/" + singer.replace("/", "_") + "-" + song.replace("/", "_") + IMediaService.LYRICS_SUFFIX;
		final String lyricPath = fLyricPath + IMediaService.TMP_SUFFIX;
		final String fPath = MediaApplication.savePath + "/" + singer.replace("/", "_") + "-" + song.replace("/", "_") + IMediaService.AUDIO_SUFFIX;
		final String path = fPath + IMediaService.TMP_SUFFIX;
		DownloadJob downloadMusic;
		File file;
		ContentValues values = new ContentValues();
		switch (status) {
		case IMediaService.STATE_FINISHED:
			file = new File(fPath);
			if (file.exists()) {
				mediaSongItem.status.setText(IMediaService.PROGRESS_FINISHED);
			} else {
				mediaService.getMediaDB().deleteAudioFromDownload(downloadId);
				screen.refresh();
			}
			break;
		case IMediaService.STATE_BEGIN:
			synchronized (mediaService.getDmLock()) {
				downloadMusic = mediaService.getDownloadMap().get(downloadId);
			}
			if (downloadMusic != null) {
//				Handler handler = new Handler() {
//					@Override
//					public void handleMessage(Message msg) {
//						super.handleMessage(msg);
//						String[] args = (String[]) msg.obj;
//						mediaSongItem.status.setText(args[0]);
//					}
//				};
//				synchronized (screen.gethdLock()) {
//					screen.getHandlerIds().add(downloadId);
//				}
//				downloadMusic.registerHandler(handler);
			} else {
				status = IMediaService.STATE_PAUSE;
				values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_STATUS, IMediaService.STATE_PAUSE);
				mediaService.getMediaDB().updateDownloadAudio(downloadId, values);
			}
			file = new File(path);
			if (!file.exists()) {
				mediaSongItem.status.setText(IMediaService.PROGRESS_WAIT);
			} else {
				if (size != 0) {
					mediaSongItem.status.setText(String.valueOf(file.length() * 100 / size) + "%");
				} else {
					mediaSongItem.status.setText(IMediaService.PROGRESS_WAIT);
				}
			}
			mediaSongItem.status.setVisibility(View.VISIBLE);
			break;
		case IMediaService.STATE_PAUSE:
			file = new File(path);
			if (!file.exists()) {
				mediaSongItem.status.setText(IMediaService.PROGRESS_WAIT);
			} else {
				if (size != 0) {
					mediaSongItem.status.setText(String.valueOf(file.length() * 100 / size) + "%");
				} else {
					mediaSongItem.status.setText(IMediaService.PROGRESS_WAIT);
				}
			}
			mediaSongItem.status.setVisibility(View.VISIBLE);
			break;
		case IMediaService.STATE_WAIT:
			file = new File(path);
			if (file.exists()) {
				if (size != 0) {
					mediaSongItem.status.setText(String.valueOf(file.length() * 100 / size) + "%");
				} else {
					mediaSongItem.status.setText(IMediaService.PROGRESS_WAIT);
				}
			} else {
				mediaSongItem.status.setText(IMediaService.PROGRESS_WAIT);
			}
			mediaSongItem.status.setVisibility(View.VISIBLE);
			break;
		default:
			file = new File(fPath);
			if (file.exists()) {
				status = IMediaService.STATE_FINISHED;
				mediaSongItem.status.setText(IMediaService.PROGRESS_FINISHED);
				mediaSongItem.status.setVisibility(View.VISIBLE);
			} else {
				status = IMediaService.STATE_DEFAULT;
				mediaSongItem.status.setVisibility(View.GONE);
			}
			break;
		}
		final MediaEventArgs args = new MediaEventArgs();
		args.putExtra("song", song);
		args.putExtra("singer", singer);
		args.putExtra("songId", songId);
		args.putExtra("downloadId", downloadId);
		args.putExtra("path", path);
		args.putExtra("fPath", fPath);
		args.putExtra("status", status);
		args.putExtra("fLyricPath", fLyricPath);
		args.putExtra("lyricPath", lyricPath);
		args.putExtra("songType", MediaDatabaseHelper.SONG_TYPE_DICTIONARY);
		view.setTag(args);
		mediaSongItem.singerTextView.setText(singer);
		mediaSongItem.songTextView.setText(song);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return super.getView(position, null, parent);
	}

	private class KMediaSongItem {
		private TextView status;
		private TextView singerTextView;
		private TextView songTextView;

	}
}
