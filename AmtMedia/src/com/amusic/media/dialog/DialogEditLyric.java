package com.amusic.media.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.amusic.media.MediaApplication;
import com.amusic.media.R;
import com.amusic.media.download.DownloadLrcLyric;
import com.amusic.media.model.ScreenArgs;
import com.amusic.media.provider.MediaDatabaseHelper;
import com.amusic.media.provider.MediaManagerDB;
import com.amusic.media.screens.impl.ScreenCreateLyric;
import com.amusic.media.services.IMediaService;
import com.amusic.media.services.impl.ServiceManager;



public class DialogEditLyric implements OnClickListener{

	private Button editor_lyric_ok;
	private Button editor_lyric_cancel;
	private RadioGroup lyricGroup;
	private RadioButton get_lyric_from_web;
	private RadioButton new_make_lyric;
	private Dialog editorLyricDialog;
	private String lyricPath = null;
	private String songPath = null;
	private OnScreenHint mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.lyric_is_downloading));
	public DialogEditLyric(Context context) {
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
		
		get_lyric_from_web.setText(MediaApplication.getContext().getResources().getString(R.string.get_lyric_from_web));
		new_make_lyric.setText(MediaApplication.getContext().getResources().getString(R.string.new_make_lyric));

		editor_lyric_ok.setOnClickListener(this);
		editor_lyric_cancel.setOnClickListener(this);

	}

	public void show() {
		editorLyricDialog.show();
	}
	
	public void setSongName(String songName) {
		String song_Name = songName;
		song_Name = song_Name.substring(0, song_Name.lastIndexOf("."));
		lyricPath = MediaApplication.lyricPath + song_Name + IMediaService.LYRICS_SUFFIX; 
	}
	
	public void setSongPath(String songPath) {
		this.songPath = songPath;
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
				SearchLrcLyric searchLrc = new SearchLrcLyric();
				searchLrc.setSongPath(songPath);
				searchLrc.setLyricPath(lyricPath);
				searchLrc.show();
				
				ScreenArgs args = new ScreenArgs();
				args.putExtra("lyricPath", lyricPath);
				args.putExtra("songPath", songPath);
				args.putExtra("isNative", false);
				ServiceManager.getAmtScreenService().show(ScreenCreateLyric.class, args);
			} else if (R.id.new_make_lyric == lyricGroup
					.getCheckedRadioButtonId()) {
				ScreenArgs args = new ScreenArgs();
				args.putExtra("lyricPath", lyricPath);
				args.putExtra("songPath", songPath);
				args.putExtra("isNative", true);
				ServiceManager.getAmtScreenService().show(ScreenCreateLyric.class, args);
			}
			break;
		case R.id.editor_lyric_cancel:
			break;
		}
		dismiss();
	}
	
}
