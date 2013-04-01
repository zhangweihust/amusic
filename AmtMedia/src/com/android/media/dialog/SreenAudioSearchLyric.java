package com.android.media.dialog;

import java.io.File;

import android.app.Dialog;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amusic.media.R;
import com.android.media.MediaApplication;
import com.android.media.event.impl.MediaEventArgs;
import com.android.media.event.impl.MediaEventTypes;
import com.android.media.provider.MediaDatabaseHelper;
import com.android.media.services.IMediaEventService;
import com.android.media.services.IMediaService;
import com.android.media.services.impl.MediaPlayerService;
import com.android.media.services.impl.ServiceManager;
import com.android.media.utils.Constant;

public class SreenAudioSearchLyric {
	private Dialog songInfoEditdlg;
	private Button btn_ok;
	private Button btn_cancel;
	private EditText audio_artist;
	private EditText audio_title;

	private String orginal_name = "";
	private String orginal_artist = "";
	
	private Cursor mCursor = null;
	private OnScreenHint mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.lyric_download_tips1));

	public SreenAudioSearchLyric() {
		songInfoEditdlg = new Dialog(ServiceManager.getAmtMedia(),
				R.style.CustomDialog);
		songInfoEditdlg.setCanceledOnTouchOutside(true);
		songInfoEditdlg.setContentView(R.layout.screen_audio_modify_song_info);
		init();
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
			
			if (mCursor != null && mCursor.getCount() != 0) {
				IMediaEventService mediaEventService = ServiceManager.getMediaEventService();
				MediaEventArgs args = new MediaEventArgs();
				
				String song_Name = MediaPlayerService.splitTitle(audio_title.getText().toString());
				String singer_Name = MediaPlayerService.splitTitle(audio_artist.getText().toString());
				String name = "";
				String filePath = "";
				String lyricPath = "";
				
				long duration = 0;
				duration = mCursor.getLong(mCursor
						.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_DURATION));
				
				name = mCursor.getString(mCursor
						.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_DISPALYNAME));
		        filePath = mCursor.getString(mCursor
				        .getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_FILEPATH));
		        name = name.substring(0, name.lastIndexOf("."));
		        
		        lyricPath = MediaApplication.lyricPath + name + IMediaService.LYRICS_SUFFIX;
		        File kscFile = new File(lyricPath);
				kscFile.delete();
				
				args.putExtra("lyricPath", lyricPath);
				args.putExtra("song_Name", song_Name);
				args.putExtra("singer_Name", singer_Name);
				args.putExtra("duration", duration);
	            args.putExtra("filename", name);
	            args.putExtra("audiofilePath", filePath);
	            args.putExtra("isKmedia", false);
	            args.putExtra("isNeedPopDialog", true);
				mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_LYRIC));
			}
			
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
		mCursor = ServiceManager.getMediaplayerService().getCursor();
		
		if (mCursor != null && mCursor.getCount() != 0) {

			orginal_name = mCursor.getString(mCursor
					.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_SONGNAME));
			
			orginal_artist = mCursor.getString(mCursor
					.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ARTISTNAME));
		
			
			if (orginal_artist == null || orginal_artist.equals("<unknown>") || orginal_artist.equals("未知歌手")) {
				audio_artist.setText("");
			} else
				audio_artist.setText(orginal_artist);

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

}