package com.android.media.dialog;

import java.io.File;

import android.app.Dialog;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amusic.media.R;
import com.android.media.MediaApplication;
import com.android.media.download.DownloadLrcLyric;
import com.android.media.event.impl.MediaEventArgs;
import com.android.media.event.impl.MediaEventTypes;
import com.android.media.model.ScreenArgs;
import com.android.media.provider.MediaDatabaseHelper;
import com.android.media.provider.MediaManagerDB;
import com.android.media.screens.impl.ScreenCreateLyric;
import com.android.media.services.IMediaEventService;
import com.android.media.services.IMediaService;
import com.android.media.services.impl.MediaPlayerService;
import com.android.media.services.impl.ServiceManager;
import com.android.media.utils.Constant;

public class SearchLrcLyric {

	private Dialog songInfoEditdlg;
	private Button btn_ok;
	private Button btn_cancel;
	private EditText audio_artist;
	private EditText audio_title;

	private String orginal_name = "";
	private String orginal_artist = "";
	
	private Cursor mCursor = null;
	private String songPath = "";
	private String lyricPath = "";
	private OnScreenHint mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.lyric_download_tips1));

	public SearchLrcLyric() {
		songInfoEditdlg = new Dialog(ServiceManager.getAmtMedia(),
				R.style.CustomDialog);
		songInfoEditdlg.setCanceledOnTouchOutside(true);
		songInfoEditdlg.setContentView(R.layout.screen_audio_modify_song_info);
		init();
	}
	
	public void setSongPath(String path) {
		songPath = path;
	}
	
	public void setLyricPath(String lrcPath) {
		lyricPath = lrcPath;
	}

	public void init() {
		LinearLayout layout = (LinearLayout) songInfoEditdlg.findViewById(R.id.tv_audio_album_layout);
		layout.setVisibility(View.GONE);
		TextView line = (TextView)songInfoEditdlg.findViewById(R.id.tv_audio_line);
		line.setVisibility(View.GONE);
		btn_ok = (Button) songInfoEditdlg.findViewById(R.id.btn_ok);
		btn_cancel = (Button) songInfoEditdlg.findViewById(R.id.btn_cancel);

		btn_ok.setOnClickListener(btn_ok_listener);
		btn_cancel.setOnClickListener(btn_cancel_listener);

		audio_artist = (EditText) songInfoEditdlg
				.findViewById(R.id.audio_artist);
		audio_title = (EditText) songInfoEditdlg.findViewById(R.id.audio_title);
	}

	private View.OnClickListener btn_ok_listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if(Constant.PROHIBITED_TO_DOWNLOAD_LYRIC == Constant.LYRIC_DOWNLOAD){
				dismiss();
				ServiceManager.getAmtMediaHandler().post(new Runnable() {
					@Override
					public void run() {
						if(mOnScreenHint!=null){
						    mOnScreenHint.cancel();
						}
						mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.lyric_download_tips1));
						mOnScreenHint.show();
					}
				});
//				mOnScreenHint.cancel();
//				mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.lyric_download_tips1));
//				mOnScreenHint.show();
				return;
			} else if (Constant.ALLOWED_TO_DOWNLOAD_LYRIC_WITH_WIFI == Constant.LYRIC_DOWNLOAD) {
				if(!(ServiceManager.getNetworkService().acquire(false) && ServiceManager.getNetworkService().getNetType() == ConnectivityManager.TYPE_WIFI)) {
					dismiss();
					ServiceManager.getAmtMediaHandler().post(new Runnable() {
						@Override
						public void run() {
							if(mOnScreenHint!=null){
							    mOnScreenHint.cancel();
							}
							mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.lyric_download_tips2));
							mOnScreenHint.show();
						}
					});
//					mOnScreenHint.cancel();
//					mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.lyric_download_tips2));
//					mOnScreenHint.show();
					return;
				}
			}
			String songName = MediaPlayerService.splitTitle(audio_title.getText().toString());;
			if (songName == null || "".equals(songName)) {
				ServiceManager.getAmtMediaHandler().post(new Runnable() {
					@Override
					public void run() {
						if(mOnScreenHint!=null){
						    mOnScreenHint.cancel();
						}
						mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.lyric_songname_null));
						mOnScreenHint.show();
					}
				});
//				mOnScreenHint.cancel();
//				mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.lyric_songname_null));
//				mOnScreenHint.show();
				return;
			}
			ServiceManager.getAmtMediaHandler().post(new Runnable() {
				@Override
				public void run() {
					if(mOnScreenHint!=null){
					    mOnScreenHint.cancel();
					}
					mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.lyric_is_downloading));
					mOnScreenHint.show();
				}
			});
//			mOnScreenHint.cancel();
//			mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.lyric_is_downloading));
//			mOnScreenHint.show();
			
			downloadLrcLyric();
			dismiss();
		}
	};

	private View.OnClickListener btn_cancel_listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			dismiss();
		}
	};
	

	public void show() {
		MediaManagerDB mediadb = ServiceManager.getMediaService().getMediaDB();
		mCursor = mediadb.queryAudioByPath(songPath);
		if (mCursor != null && mCursor.getCount() != 0) {

			mCursor.moveToFirst();
			orginal_name = mCursor.getString(mCursor
					.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_SONGNAME));
			
			orginal_artist = mCursor.getString(mCursor
					.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ARTISTNAME));
			
			if (orginal_artist == null || orginal_artist.equals("<unknown>") || orginal_artist.equals("未知歌手")) {
				audio_artist.setText("");
			} else {
				audio_artist.setText(orginal_artist);
			}

			if (orginal_name == null || orginal_name.equals("<unknown>") || orginal_name.equals("未知歌手")) {
				audio_title.setText("");
			} else {
				audio_title.setText(orginal_name);
			}
		}
		songInfoEditdlg.show();
	}

	public void dismiss() {
		songInfoEditdlg.dismiss();
	}
	
	private void downloadLrcLyric() {
		String songName = MediaPlayerService.splitTitle(audio_title.getText().toString());;
		String singer = MediaPlayerService.splitTitle(audio_artist.getText().toString());
		
		final DownloadLrcLyric downlrc = new DownloadLrcLyric(songName,singer);
		downlrc.setLyricPath(lyricPath);
		new Thread() {
			@Override
			public void run() {
		        downlrc.downloadlyrics();
			}
		}.start();
	}
}
