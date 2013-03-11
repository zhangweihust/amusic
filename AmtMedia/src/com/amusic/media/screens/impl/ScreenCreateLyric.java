package com.amusic.media.screens.impl;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.amusic.media.MediaApplication;
import com.amusic.media.R;
import com.amusic.media.dialog.OnScreenHint;
import com.amusic.media.download.DownloadLrcLyric;
import com.amusic.media.event.IMediaEventArgs;
import com.amusic.media.event.IMediaEventHandler;
import com.amusic.media.event.impl.MediaEventArgs;
import com.amusic.media.event.impl.MediaEventTypes;
import com.amusic.media.lyric.parser.LyricInfo;
import com.amusic.media.lyric.parser.LyricParser;
import com.amusic.media.lyric.parser.Sentence;
import com.amusic.media.lyric.player.LyricParserFactory;
import com.amusic.media.model.ScreenArgs;
import com.amusic.media.provider.MediaDatabaseHelper;
import com.amusic.media.provider.MediaManagerDB;
import com.amusic.media.screens.AmtScreen;
import com.amusic.media.screens.impl.ScreenLyricSpeed;
import com.amusic.media.services.IMediaEventService;
import com.amusic.media.services.IMediaService;
import com.amusic.media.services.impl.MediaPlayerService;
import com.amusic.media.services.impl.ServiceManager;
import com.amusic.media.toolbox.DETool;
import com.amusic.media.view.CustomDialog;

public class ScreenCreateLyric extends AmtScreen implements OnClickListener,IMediaEventHandler{

	private Button screen_create_lyric_ok;
	private Button screen_create_lyricc_cancel;
	private Button btn_ok;
	private Button btn_cancel;
	private EditText screen_create_lyric_content;
	private Dialog dialog;
	private Dialog searchdialog;
	private TextView textView;
	private boolean saveflag;
	private String lyricPath = null;
	private String songPath = null;
	private String songName = null;
	private String singer = null;
	private LyricInfo lyricInfo;
	private String txtLyric = null;
	private final Context context = MediaApplication.getContext();
	private IMediaEventService mediaEventService;
	private List<String> lyricList = null;
	private OnScreenHint mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_create_lyric_null));
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_create_lyric);	
		setScreenTitle(getString(R.string.screen_create_lyric_title));
		screen_create_lyric_content = (EditText) this.findViewById(R.id.screen_create_lyric_content);
		screen_create_lyric_content.setTextSize(18);
//		Resources res = getResources();
//
//		float fontSize = res.getDimension(R.dimen.screen_audio_song_lyrics_lyrics_textSize);
//		screen_create_lyric_content.setTextSize(fontSize);
		
		screen_create_lyric_ok = (Button) this.findViewById(R.id.screen_create_lyric_ok);
		screen_create_lyricc_cancel = (Button) this.findViewById(R.id.screen_create_lyricc_cancel);
		
		screen_create_lyric_ok.setOnClickListener(this);
		screen_create_lyricc_cancel.setOnClickListener(this);
		
		ScreenArgs args = (ScreenArgs) getIntent().getSerializableExtra("args");
		boolean isNative = (Boolean) args.getExtra("isNative", false);
		lyricPath = (String) args.getExtra("lyricPath", null);
	    songPath = (String) args.getExtra("songPath", null);
	    lyricInfo = null;
		if (isNative) {
		    parser();
		    screen_create_lyric_content.setText(getLyricStr());
		}
		mediaEventService = ServiceManager.getMediaEventService();
		mediaEventService.addEventHandler(this);
	}
	
	private String getLyricStr() {
		String TxtLyrics = "";
		if (lyricInfo == null) {
			return null;
		}
		List<Sentence> sentenceList = lyricInfo.getList();
		if (lyricInfo.hasLyricMaker()) {
			sentenceList.remove(0);
		}
		for (Sentence stc : sentenceList) {
			TxtLyrics += stc.getContent();
			TxtLyrics += "\n";
		}
		this.txtLyric = TxtLyrics;
		return TxtLyrics;
	}

	private void parser() {
		if (lyricPath == null) {
			return;
		}
		String lyrics = null;
		if(MediaPlayerService.findFile(lyricPath)){
			lyrics = new String(DETool.nativeGetKsc(lyricPath));
		}
		if (lyrics == null) {
			ServiceManager.getAmtMediaHandler().post(new Runnable() {
				@Override
				public void run() {
					if(mOnScreenHint!=null){
					    mOnScreenHint.cancel();
					}
					mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_create_lyric_native_null));
					mOnScreenHint.show();
				}
			});
//			mOnScreenHint.cancel();
//			mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_create_lyric_native_null));
//			mOnScreenHint.show();
			lyricInfo = null;
			return;
		}
		LyricParserFactory factory = new LyricParserFactory();
		LyricParser parser = factory.createLyricsParser(lyrics, ".ksc");
		try {
			lyricInfo = parser.parser();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (lyricInfo != null) {
			songName = lyricInfo.getTitle();
			singer = lyricInfo.getSinger();
		}
	}
	

	@Override
	protected void onResume() {
		super.onResume();
		ServiceManager.getAmtMedia().getGoPlayerBtn().setVisibility(View.INVISIBLE);
		setScreenTitle(getString(R.string.screen_create_lyric_title));
	}
	
	private void initWhenNoLyric() {
		if (lyricInfo != null) {
			return;
		}
		MediaManagerDB mediadb = ServiceManager.getMediaService().getMediaDB();
		Cursor cursor = mediadb.queryAudioByPath(songPath);
		
		if (cursor == null || cursor.getCount() == 0) {
			songName = "Unknown";
			singer = "Unknown";
			return;
		}
		cursor.moveToFirst();
		songName = cursor.getString(cursor
				.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_SONGNAME));
		singer = cursor.getString(cursor
				.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ARTISTNAME));
		
		if (songName == null || "".equals(songName)) {
			songName = "Unknown";
		}
		
		if (singer == null || "".equals(singer)) {
			singer = "Unknown";
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.screen_create_lyric_ok:
			saveflag = true;
			String str = screen_create_lyric_content.getText().toString();
			if (str != null && !"".equals(str.trim())) {
				showDialog(getResources().getString(R.string.screen_create_lyric_ok_info));
			} else {
				ServiceManager.getAmtMediaHandler().post(new Runnable() {
					@Override
					public void run() {
						if(mOnScreenHint!=null){
						    mOnScreenHint.cancel();
						}
						mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_create_lyric_null));
						mOnScreenHint.show();
					}
				});
//				mOnScreenHint.cancel();
//				mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_create_lyric_null));
//				mOnScreenHint.show();
			}
			break;
		case R.id.screen_create_lyricc_cancel:
			saveflag = false;
			showDialog(getResources().getString(R.string.screen_create_lyric_cancel_info));
			break;
		case R.id.positiveButton:
			dialog.dismiss();
        	if(saveflag){
        		initWhenNoLyric();
        		Intent intent = new Intent();
        		intent.putExtra("lyrics", screen_create_lyric_content.getText().toString());
        		intent.putExtra("songPath",songPath);
        		intent.putExtra("songName", songName);
        		intent.putExtra("singer", singer);
        		intent.putExtra("lyricPath", lyricPath);
        		intent.setClass(ScreenCreateLyric.this, ScreenLyricSpeed.class);
        		ScreenCreateLyric.this.startActivity(intent);
        	}else{
        		ServiceManager.getAmtScreenService().goback();
        	}
			break;
		case R.id.negativeButton:
			dialog.dismiss();
			break;
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		screen_create_lyric_content.setText("");
		ScreenArgs args = (ScreenArgs) intent.getSerializableExtra("args");
		boolean isNative = (Boolean) args.getExtra("isNative", false);
		lyricPath = (String) args.getExtra("lyricPath", null);
	    songPath = (String) args.getExtra("songPath", null);
	    lyricInfo = null;
		if (isNative) {
		    parser();
		    screen_create_lyric_content.setText(getLyricStr());
		}
		super.onNewIntent(intent);
	}

	private void showDialog(String info) {
		if (dialog == null) {
		    dialog = new Dialog(ServiceManager.getAmtMedia(), R.style.CustomDialog);
		}
		dialog.setCanceledOnTouchOutside(true);
		dialog.setContentView(R.layout.edit_lyric_dialog);
		textView = (TextView) dialog.findViewById(R.id.textView);
		btn_ok = (Button) dialog.findViewById(R.id.positiveButton);
		btn_cancel = (Button) dialog.findViewById(R.id.negativeButton);
		btn_ok.setOnClickListener(this);
		btn_cancel.setOnClickListener(this);
		textView.setText(info);
		dialog.show();
	}
	
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		Activity parent = getParent();
        if( parent != null){
            parent.onBackPressed();
        }
	}
	
	@Override
	public boolean hasMenu() {
		// TODO Auto-generated method stub
		return true;
	}

	private OnItemClickListener itemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
			if (lyricList == null || lyricList.size() < position) {
				return;
			}
			
			String[] strs = lyricList.get(position).split("——");

			if(strs.length != 3) {
				return;
			}
			
			String lyricPathTemp = lyricPath.substring(0, lyricPath.lastIndexOf("."));
			lyricPathTemp += ".lrc.gz";
			MediaEventArgs args = new MediaEventArgs();
			DownloadLrcLyric.downloadLyrics(IMediaService.DOWNLOAD_SERVER_BASE + strs[2] + ".lc", 0, lyricPathTemp, args);
			searchdialog.dismiss();
		}
	};
	@Override
	public boolean onEvent(final IMediaEventArgs args) {
		// TODO Auto-generated method stub
		switch (args.getMediaUpdateEventTypes()) {
		case AUDIO_DOWNLOAD_LRC_LYRIC_FINISH:
			setTxtLyric();
			break;
		case AUDIO_DOWNLOAD_LRC_LYRICS_ERROR:
			ServiceManager.getAmtMediaHandler().post(new Runnable(){
				@Override
				public void run() {
					if(null != mOnScreenHint){
						mOnScreenHint.cancel();
					}
					mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.lyric_download_fail));
					mOnScreenHint.show();
				}
		    });
			break;
		case AUDIO_DOWNLOAD_LRC_LYRIC_SELECT_UI:
			ServiceManager.getAmtMediaHandler().post(new Runnable() {

				@SuppressWarnings("unchecked")
				@Override
				public void run() {
					lyricList = (List<String>) args.getExtra("lyricList");
					List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

					for (int i = 0; i <  lyricList.size(); i++) {
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("title", lyricList.get(i).substring(0, lyricList.get(i).lastIndexOf("——")));
						list.add(map);
					}
					String title = "";
					try {
						if (songPath != null) {
						    title = songPath.substring(songPath.lastIndexOf("/") + 1,songPath.lastIndexOf("."));
						}
					} catch (Exception e) {
						title = "Unknown";
					}
					
					Context contextlrc = ServiceManager.getAmtMedia();
					
                    if (searchdialog!=null && searchdialog.isShowing()) {
                    	searchdialog.dismiss();
					}
					CustomDialog.Builder customBuilder = new CustomDialog.Builder(
							contextlrc);
					customBuilder
							.setTitle(title/*context.getString(R.string.lyric_choose_please)*/)
							.setLayoutXml(R.layout.lyric_select_dialog)
							.setWhichViewVisible(CustomDialog.contentIsListView)
							.setListViewData(list)
							.setLayoutID(CustomDialog.LISTVIEW_ITEM_TEXTVIEW)
							.setOnItemClickListener(itemClickListener)
							.setPositiveButton(context.getString(R.string.custom_dialog_button_cancel),
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											dialog.dismiss();
										}
									});
					searchdialog = customBuilder.create();
					searchdialog.show();
				}
			});

			break;
		}
		return true;
	}

	private void setTxtLyric() {
		String lrcPath = lyricPath.substring(0, lyricPath.lastIndexOf("."));
		lrcPath += ".lrc.gz";
		if (DETool.nativeUncompressLrc(lrcPath) != 0) {
			return;
		}
		lrcPath = lrcPath.substring(0, lrcPath.lastIndexOf("."));
		try {
			InputStream in = new BufferedInputStream(new FileInputStream(lrcPath));
			BufferedReader bin = new BufferedReader(new InputStreamReader(in));
		    StringBuffer buffer = new StringBuffer();
		    String line = "";
		    while ((line = bin.readLine()) != null){
		        buffer.append(line);
		        buffer.append("\n");
		    }
		    final String txtText = buffer.toString();
		    ServiceManager.getAmtMediaHandler().post(new Runnable(){
				@Override
				public void run() {
					screen_create_lyric_content.setText(txtText);
				}
		    });
		    
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
