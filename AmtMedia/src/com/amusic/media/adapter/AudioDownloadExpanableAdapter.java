package com.amusic.media.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.amusic.media.MediaApplication;
import com.amusic.media.R;
import com.amusic.media.download.DownloadJob;
import com.amusic.media.event.impl.MediaEventArgs;
import com.amusic.media.provider.MediaDatabaseHelper;
import com.amusic.media.screens.Screen;
import com.amusic.media.services.IMediaService;
import com.amusic.media.services.impl.ServiceManager;
import com.amusic.media.task.DownloadTask;

public class AudioDownloadExpanableAdapter extends BaseExpandableListAdapter {

	private final int GROUP_ID_DOWNLOAD_UNFINISHED = 0;
	private final int GROUP_ID_DOWNLOAD_FINISHED = 1;
	private LayoutInflater inflater;
	private final IMediaService mediaService;
	private final Screen screen;

	List<String> group; // 组列表
	List<ArrayList<AudioValueDownloadItem>> child; // 子列表
	private Map<Integer, Cursor> childCursorMap;

	public AudioDownloadExpanableAdapter(Context context, Map<Integer, Cursor> cursorMap, Screen screen) {
		mediaService = ServiceManager.getMediaService();
		inflater = LayoutInflater.from(context);
		this.screen = screen;
		setchildCursors(cursorMap);
		initializeData(context);
		childCursorMap = new HashMap<Integer, Cursor>();
	}

	public void setchildCursors(Map<Integer, Cursor> cursorMap) {
		childCursorMap = cursorMap;
	}


	/**
	 * 初始化组、子列表数据
	 */
	public void initializeData(Context context) {
		group = new ArrayList<String>();
		child = new ArrayList<ArrayList<AudioValueDownloadItem>>();
		String groupName = "";
		for (Integer groupId : childCursorMap.keySet()) {
			Cursor childCursor = childCursorMap.get(groupId);
			if (GROUP_ID_DOWNLOAD_UNFINISHED == groupId) {
				groupName = context.getString(R.string.screen_download_tab_unfinished);
			} else if (GROUP_ID_DOWNLOAD_FINISHED == groupId) {
				groupName = context.getString(R.string.screen_download_tab_finished);
			}
			ArrayList<AudioValueDownloadItem> childitem = new ArrayList<AudioValueDownloadItem>();
			if (childCursor != null) {
				for (childCursor.moveToFirst(); !childCursor.isAfterLast(); childCursor.moveToNext()) {
					AudioValueDownloadItem audioValueDownloadItem = new AudioValueDownloadItem();
					audioValueDownloadItem.downloadType = childCursor.getInt(childCursor.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_TYPE));
					audioValueDownloadItem.song = childCursor.getString(childCursor.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_SONG_NAME));
					audioValueDownloadItem.singer = childCursor.getString(childCursor.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_SONG_SINGER));
					audioValueDownloadItem.fpath = childCursor.getString(childCursor.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_PATH));
					audioValueDownloadItem.songType = childCursor.getInt(childCursor.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_SONG_TYPE));
					audioValueDownloadItem.songId = childCursor.getInt(childCursor.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_SONG_ID));
					audioValueDownloadItem.downloadId = childCursor.getInt(childCursor.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_ID));
					audioValueDownloadItem.status = childCursor.getInt(childCursor.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_STATUS));
					audioValueDownloadItem.size = childCursor.getLong(childCursor.getColumnIndex(MediaDatabaseHelper.COLUMN_DOWNLOAD_TOTAL_SIZE));
					childitem.add(audioValueDownloadItem);
				}
			}
			addAudioInfo(groupName, childitem);
		}

	}

	private void addAudioInfo(String g, ArrayList<AudioValueDownloadItem> valueItem) {
		group.add(g);
		child.add(valueItem);
	}

	private class AudioDownloadItem {
		private TextView title;
		private TextView status;
		private TextView size;
		private TextView speed;
		private SeekBar downloadProgress;
		private View layout;
	}

	public class AudioValueDownloadItem {
		public String song;
		public String singer;
		public int songType;
		public int songId;
		public Integer downloadId;
		public int status;
		public long size;
		public int downloadType;
		public long speed;
		public String fpath;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		String string = group.get(groupPosition);
		View view = inflater.inflate(R.layout.screen_search_download_group, null);
		TextView downGroupInfoView = (TextView) view.findViewById(R.id.screen_search_download_above);
		ImageView downGroupIndicator = (ImageView) view.findViewById(R.id.search_download_group_Indicator);
		downGroupInfoView.setText(string);
		if (isExpanded) {
			downGroupIndicator.setImageResource(R.drawable.download_open);
		} else {
			downGroupIndicator.setImageResource(R.drawable.download_close);
		}
		return view;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return child.get(groupPosition).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		final AudioValueDownloadItem childItem = child.get(groupPosition).get(childPosition);
		final AudioDownloadItem audioDownloadItem;
		//if (null == convertView) {
			convertView = inflater.inflate(R.layout.screen_search_download_child, null);
			audioDownloadItem = new AudioDownloadItem();
			audioDownloadItem.title = (TextView) convertView.findViewById(R.id.download_title);
			audioDownloadItem.status = (TextView) convertView.findViewById(R.id.download_status);
			audioDownloadItem.size = (TextView) convertView.findViewById(R.id.download_size);
			audioDownloadItem.speed = (TextView) convertView.findViewById(R.id.download_speed);
			audioDownloadItem.downloadProgress = (SeekBar) convertView.findViewById(R.id.download_progress);
			audioDownloadItem.downloadProgress.setEnabled(false);
			audioDownloadItem.layout = (View) convertView.findViewById(R.id.downloading_info);
//			convertView.setTag(R.layout.screen_search_download_child, audioDownloadItem);
//	    } else {
//	    	audioDownloadItem = (AudioDownloadItem) convertView.getTag(R.layout.screen_search_download_child);
//	    }
		
		String fPath;
		String path;
		final String downloadType;
		if(childItem.singer.equals("")){
			if (childItem.downloadType == MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL) {
				fPath = MediaApplication.savePath + childItem.song.replace("/", "_")
						+ IMediaService.AUDIO_SUFFIX;
				path = fPath + IMediaService.TMP_SUFFIX;
				downloadType = screen.getString(R.string.screen_audio_download_type_orginal);
			} else if (childItem.downloadType == MediaDatabaseHelper.DOWNLOAD_TYPE_ACCOMPANY) {
				fPath = MediaApplication.accompanyPath + childItem.song.replace("/", "_")
						+ IMediaService.ACCOMPANY_SUFFIX;
				path = fPath + IMediaService.TMP_SUFFIX;
				downloadType = screen.getString(R.string.screen_audio_download_type_accompany);
			} else {
				fPath = "";
				path = "";
				downloadType = "";
			}
			audioDownloadItem.title.setText(childItem.song + "(" + downloadType + ")");
		} else {
			if (childItem.downloadType == MediaDatabaseHelper.DOWNLOAD_TYPE_ORIGINAL) {
				fPath = MediaApplication.savePath + childItem.singer.replace("/", "_") + "-" + childItem.song.replace("/", "_")
						+ IMediaService.AUDIO_SUFFIX;
				path = fPath + IMediaService.TMP_SUFFIX;
				downloadType = screen.getString(R.string.screen_audio_download_type_orginal);
			} else if (childItem.downloadType == MediaDatabaseHelper.DOWNLOAD_TYPE_ACCOMPANY) {
				fPath = MediaApplication.accompanyPath + childItem.singer.replace("/", "_") + "-" + childItem.song.replace("/", "_")
						+ IMediaService.ACCOMPANY_SUFFIX;
				path = fPath + IMediaService.TMP_SUFFIX;
				downloadType = screen.getString(R.string.screen_audio_download_type_accompany);
			} else {
				fPath = "";
				path = "";
				downloadType = "";
			}
			audioDownloadItem.title.setText(childItem.singer + "-" + childItem.song + "(" + downloadType + ")");
		}
		
		DownloadJob downloadMusic;
		File file;
		ContentValues values = new ContentValues();
		switch (childItem.status) {
		case IMediaService.STATE_FINISHED:
			file = new File(childItem.fpath);
			if (!file.exists()) {
				mediaService.getMediaDB().deleteAudioFromDownload(childItem.downloadId);
				MediaApplication.logD(AudioDownloadExpanableAdapter.class, "IMediaService.STATE_FINISHED,你是本地文件不存在，删除下载表里面的记录：" + fPath);
			} else {
				//audioDownloadItem.downloadProgress.setProgress(100);
				String str = screen.getString(R.string.screen_audio_download_finished);
				str = str + "( " +String.format("%.2f M", (float)file.length() / (1024 * 1024)) + " )";
				audioDownloadItem.downloadProgress.setVisibility(View.GONE);
				audioDownloadItem.status.setText(str);
				audioDownloadItem.layout.setVisibility(View.GONE);
				
			}
			break;
		case IMediaService.STATE_PAUSE:
			file = new File(path);
			audioDownloadItem.layout.setVisibility(View.VISIBLE);
			audioDownloadItem.downloadProgress.setVisibility(View.VISIBLE);
			audioDownloadItem.size.setVisibility(View.VISIBLE);
			if (!file.exists()) {
				//audioDownloadItem.status.setText(downloadType + IMediaService.PROGRESS_WAIT);
				audioDownloadItem.downloadProgress.setProgress(0);
				audioDownloadItem.status.setText(screen.getString(R.string.screen_audio_download_paused));
				audioDownloadItem.size.setText(screen.getString(R.string.screen_audio_download_unknow_size));
				audioDownloadItem.speed.setVisibility(View.GONE);
			} else {
				if (childItem.size != 0) {
					long progressValue = file.length() * 100 / childItem.size;
					//audioDownloadItem.status.setText(downloadType + String.valueOf(progressValue) + "%");
					audioDownloadItem.downloadProgress.setProgress((int) progressValue);
					audioDownloadItem.status.setText(screen.getString(R.string.screen_audio_download_paused));
					audioDownloadItem.size.setText(Formatter.formatFileSize(screen, file.length()) + "/" + Formatter.formatFileSize(screen, childItem.size));
					audioDownloadItem.speed.setVisibility(View.GONE);
				} else {
					//audioDownloadItem.status.setText(downloadType + IMediaService.PROGRESS_WAIT);
					audioDownloadItem.downloadProgress.setProgress(0);
					audioDownloadItem.status.setText(screen.getString(R.string.screen_audio_download_paused));
					audioDownloadItem.size.setText(screen.getString(R.string.screen_audio_download_unknow_size));
					audioDownloadItem.speed.setVisibility(View.GONE);
				}
			}
			break;
		case IMediaService.STATE_BEGIN:
			audioDownloadItem.layout.setVisibility(View.VISIBLE);
			audioDownloadItem.downloadProgress.setVisibility(View.VISIBLE);
			audioDownloadItem.size.setVisibility(View.VISIBLE);
			audioDownloadItem.speed.setVisibility(View.VISIBLE);
				file = new File(path);
//				if (!file.exists()) {
//					//audioDownloadItem.status.setText(downloadType + IMediaService.PROGRESS_WAIT);
//					audioDownloadItem.downloadProgress.setProgress(0);
//					audioDownloadItem.status.setText(screen.getString(R.string.screen_audio_download_starting));
//					audioDownloadItem.size.setText(screen.getString(R.string.screen_audio_download_unknow_size));
//					audioDownloadItem.speed.setVisibility(View.GONE);
//				} else {
					synchronized (mediaService.getDmLock()) {
						downloadMusic = mediaService.getDownloadMap().get(childItem.downloadId);
					}
					if (downloadMusic == null) {
						values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_STATUS, IMediaService.STATE_PAUSE);
						mediaService.getMediaDB().updateDownloadAudio(childItem.downloadId, values);
						screen.refresh();
					} else {
						Handler handler = new Handler() {
							@Override
							public void handleMessage(Message msg) {
								super.handleMessage(msg);
							//	screen.refresh();
								Bundle b = msg.getData();
								DownloadJob mJob = (DownloadJob) msg.obj;
								audioDownloadItem.speed.setVisibility(View.VISIBLE);
								audioDownloadItem.size.setText(Formatter.formatFileSize(screen, b.getLong("currentBytes")) + "/" + Formatter.formatFileSize(screen, b.getLong("totalBytes")));
								audioDownloadItem.downloadProgress.setProgress((int)(b.getLong("currentBytes")*100/b.getLong("totalBytes")));
								audioDownloadItem.status.setText(screen.getString(R.string.screen_audio_downloading));
								if(mJob != null && mJob.getDownloadTask() != null) {
									//MediaApplication.logD(DownloadTask.class,"mJob:" + mJob.getSong() + "    mJob.speed:" + mJob.getDownloadTask().getNetworkSpeed());
									audioDownloadItem.speed.setText(mJob.getDownloadTask().getNetworkSpeed() + "KB/s");
								} else {
									MediaApplication.logD(DownloadTask.class,"mJob:" + mJob + "    mJob.getDownloadTask():" + mJob.getDownloadTask());
								}
							}
						};
						synchronized (screen.gethdLock()) {
							screen.getHandlerIds().add(childItem.downloadId);
						}
						if(downloadMusic.getDownloadTask() != null)
							downloadMusic.getDownloadTask().registerHandler(handler);
						    audioDownloadItem.speed.setText(downloadMusic.getDownloadTask().getNetworkSpeed() + "KB/s");
					}
					if (childItem.size != 0 && file.exists()) {
						long progressValue = file.length() * 100 / childItem.size;
						audioDownloadItem.downloadProgress.setProgress((int) progressValue);
						audioDownloadItem.status.setText(screen.getString(R.string.screen_audio_downloading));
						audioDownloadItem.size.setText(Formatter.formatFileSize(screen, file.length()) + "/" + Formatter.formatFileSize(screen, childItem.size));
					} else {
						audioDownloadItem.downloadProgress.setProgress(0);
						audioDownloadItem.status.setText(screen.getString(R.string.screen_audio_download_starting));
						audioDownloadItem.size.setText(screen.getString(R.string.screen_audio_download_unknow_size));
						audioDownloadItem.speed.setVisibility(View.GONE);
					}
				
			//}
			break;
		case IMediaService.STATE_WAIT:
			audioDownloadItem.layout.setVisibility(View.VISIBLE);
			audioDownloadItem.downloadProgress.setVisibility(View.VISIBLE);
			audioDownloadItem.size.setVisibility(View.VISIBLE);
			file = new File(path);
			if (file.exists()) {
				if (childItem.size != 0) {
					long progressValue = file.length() * 100 / childItem.size;
					//audioDownloadItem.status.setText(downloadType + String.valueOf(progressValue) + "%");
					audioDownloadItem.downloadProgress.setProgress((int) progressValue);
					audioDownloadItem.status.setText(screen.getString(R.string.screen_audio_download_waiting));
					audioDownloadItem.size.setText(Formatter.formatFileSize(screen, file.length()) + "/" + Formatter.formatFileSize(screen, childItem.size));
					audioDownloadItem.speed.setVisibility(View.GONE);
				} else {
					//audioDownloadItem.status.setText(downloadType + IMediaService.PROGRESS_WAIT);
					audioDownloadItem.downloadProgress.setProgress(0);
					audioDownloadItem.status.setText(screen.getString(R.string.screen_audio_download_waiting));
					audioDownloadItem.size.setText(screen.getString(R.string.screen_audio_download_unknow_size));
					audioDownloadItem.speed.setVisibility(View.GONE);
				}
			} else {
				//audioDownloadItem.status.setText(downloadType + IMediaService.PROGRESS_WAIT);
				audioDownloadItem.downloadProgress.setProgress(0);
				audioDownloadItem.status.setText(screen.getString(R.string.screen_audio_download_waiting));
				audioDownloadItem.size.setText(screen.getString(R.string.screen_audio_download_unknow_size));
				audioDownloadItem.speed.setVisibility(View.GONE);
			}
			break;
		default:
			audioDownloadItem.layout.setVisibility(View.VISIBLE);
			audioDownloadItem.downloadProgress.setVisibility(View.VISIBLE);
			audioDownloadItem.size.setVisibility(View.VISIBLE);
			file = new File(path);
			if (file.exists()) {
				if (childItem.size != 0) {
					long progressValue = file.length() * 100 / childItem.size;
					//audioDownloadItem.status.setText(downloadType + String.valueOf(progressValue) + "%");
					audioDownloadItem.downloadProgress.setProgress((int) progressValue);
					audioDownloadItem.status.setText(screen.getString(R.string.screen_audio_download_starting));
					audioDownloadItem.size.setText(Formatter.formatFileSize(screen, file.length()) + "/" + Formatter.formatFileSize(screen, childItem.size));
					audioDownloadItem.speed.setVisibility(View.GONE);
				} else {
					//audioDownloadItem.status.setText(downloadType + IMediaService.PROGRESS_WAIT);
					audioDownloadItem.downloadProgress.setProgress(0);
					audioDownloadItem.status.setText(screen.getString(R.string.screen_audio_download_starting));
					audioDownloadItem.size.setText(screen.getString(R.string.screen_audio_download_unknow_size));
					audioDownloadItem.speed.setVisibility(View.GONE);
				}
			} else {
				//audioDownloadItem.status.setText(downloadType + IMediaService.PROGRESS_WAIT);
				audioDownloadItem.downloadProgress.setProgress(0);
				audioDownloadItem.status.setText(screen.getString(R.string.screen_audio_download_starting));
				audioDownloadItem.size.setText(screen.getString(R.string.screen_audio_download_unknow_size));
				audioDownloadItem.speed.setVisibility(View.GONE);
			}
			break;
		}
		final MediaEventArgs args = new MediaEventArgs();
		args.putExtra("song", childItem.song);
		args.putExtra("singer", childItem.singer);
		args.putExtra("songId", childItem.songId);
		args.putExtra("status", childItem.status);
		args.putExtra("path", path);
		args.putExtra("fPath", fPath);
		args.putExtra("size", childItem.size);
		args.putExtra("songType", childItem.songType);
		args.putExtra("downloadId", childItem.downloadId);
		args.putExtra("downloadType", childItem.downloadType);
		convertView.setTag(args);
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return child.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return group.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return group.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return 0;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

}
