package com.amusic.media.dialog;

import java.util.List;

import android.app.Dialog;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.amusic.media.MediaApplication;
import com.amusic.media.R;
import com.amusic.media.lyric.parser.LyricInfo;
import com.amusic.media.lyric.parser.LyricParser;
import com.amusic.media.lyric.parser.Sentence;
import com.amusic.media.lyric.player.LyricParserFactory;
import com.amusic.media.model.ScreenArgs;
import com.amusic.media.screens.impl.ScreenCreateLyric;
import com.amusic.media.screens.impl.ScreenLyricSpeed;
import com.amusic.media.services.IMediaService;
import com.amusic.media.services.impl.MediaPlayerService;
import com.amusic.media.services.impl.ServiceManager;
import com.amusic.media.toolbox.DETool;

public class AudioDialogEditLyric implements OnClickListener{

	private Button editor_lyric_ok;
	private Button editor_lyric_cancel;
	private RadioGroup lyricGroup;
	private RadioButton get_lyric_from_web;
	private RadioButton new_make_lyric;
	private Dialog editorLyricDialog;
	private String songPath;
	private String lyricPath;
	private String songName;
	private String singer;
	private LyricInfo lyricInfo;
	private String txtLyric;
	private OnScreenHint mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_create_lyric_null));

	public AudioDialogEditLyric() {
		editorLyricDialog = new Dialog(ServiceManager.getAmtMedia(), R.style.CustomDialog);
		editorLyricDialog.setContentView(R.layout.dialog_editor_lyric);
		editorLyricDialog.setCanceledOnTouchOutside(true);
		init();
	}

	private void init() {

		lyricGroup = (RadioGroup) editorLyricDialog
				.findViewById(R.id.editor_lyric_group);
		lyricGroup
				.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
					}

				});
		get_lyric_from_web = (RadioButton) editorLyricDialog
				.findViewById(R.id.get_lyric_from_web);
		new_make_lyric = (RadioButton) editorLyricDialog
				.findViewById(R.id.new_make_lyric);

		editor_lyric_ok = (Button) editorLyricDialog
				.findViewById(R.id.editor_lyric_ok);
		editor_lyric_cancel = (Button) editorLyricDialog
				.findViewById(R.id.editor_lyric_cancel);
		
		get_lyric_from_web.setText(MediaApplication.getContext().getResources().getString(R.string.editor_lyric_text));
		new_make_lyric.setText(MediaApplication.getContext().getResources().getString(R.string.editor_lyric_time));
		
		editor_lyric_ok.setOnClickListener(this);
		editor_lyric_cancel.setOnClickListener(this);

	}

	public void show() {
		editorLyricDialog.show();
	}
	
	public boolean isShowing(){
		return editorLyricDialog.isShowing();
	}

	public void dismiss() {
		editorLyricDialog.dismiss();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.editor_lyric_ok:
			if (R.id.get_lyric_from_web == lyricGroup.getCheckedRadioButtonId()) {
				ScreenArgs args = new ScreenArgs();
				args.putExtra("lyricPath", lyricPath);
				args.putExtra("songPath", songPath);
				args.putExtra("isNative", true);
				ServiceManager.getAmtScreenService().show(ScreenCreateLyric.class, args);
			} else if (R.id.new_make_lyric == lyricGroup
					.getCheckedRadioButtonId()) {
				parser();
				getLyricStr();
				if (lyricInfo == null) {
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
//					mOnScreenHint.cancel();
//					mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_create_lyric_native_null));
//					mOnScreenHint.show();
					break;
				}
        		Intent intent = new Intent();
        		intent.putExtra("lyrics", txtLyric);
        		intent.putExtra("songPath",songPath);
        		intent.putExtra("songName", songName);
        		intent.putExtra("singer", singer);
        		intent.putExtra("lyricPath", lyricPath);
        		intent.setClass(ServiceManager.getAmtMedia(), ScreenLyricSpeed.class);
        		ServiceManager.getAmtMedia().startActivity(intent);
			}
			break;
		case R.id.editor_lyric_cancel:
			break;
		}
		dismiss();
	}
	
	private String getLyricStr() {
		String TxtLyrics = "";
		if (lyricInfo == null) {
			return null;
		}
		List<Sentence> sentenceList = lyricInfo.getList();
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
		songName = lyricInfo.getTitle();
		singer = lyricInfo.getSinger();
	}
	
	public void setSongName(String songName) {
		String song_Name = songName;
		song_Name = song_Name.substring(0, song_Name.lastIndexOf("."));
		lyricPath = MediaApplication.lyricPath + song_Name + IMediaService.LYRICS_SUFFIX; 
	}
	
	public void setSongPath(String songPath) {
		this.songPath = songPath;
	}

}
