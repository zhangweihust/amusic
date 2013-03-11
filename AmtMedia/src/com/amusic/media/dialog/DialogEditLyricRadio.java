package com.amusic.media.dialog;

import com.amusic.media.AmtMedia;
import com.amusic.media.MediaApplication;
import com.amusic.media.R;
import com.amusic.media.provider.MediaDatabaseHelper;
import com.amusic.media.services.IMediaService;
import com.amusic.media.services.impl.ServiceManager;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;

public class DialogEditLyricRadio  implements OnClickListener{
	
	private Button editor_lyric_ok;
	private Button editor_lyric_cancel;
	private RadioGroup lyricGroup;
	private Dialog editorLyricDialog;
	private String lyricPath = null;
	private String songPath = null;
	private LyricModify lyricModify;
	private OnScreenHint mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.lyric_is_downloading));
	public DialogEditLyricRadio() {
		editorLyricDialog = new Dialog(ServiceManager.getAmtMedia(), R.style.CustomDialog);
		editorLyricDialog.setContentView(R.layout.dialog_editor_lyric_radio);
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

		editor_lyric_ok = (Button) editorLyricDialog
				.findViewById(R.id.editor_lyric_ok);
		editor_lyric_cancel = (Button) editorLyricDialog
				.findViewById(R.id.editor_lyric_cancel);

		editor_lyric_ok.setOnClickListener(this);
		editor_lyric_cancel.setOnClickListener(this);

	}

	public void show() {
		editorLyricDialog.show();
	}
	

	public void dismiss() {
		editorLyricDialog.dismiss();
	}

	public LyricModify getLyricModify(){
		return this.lyricModify;
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub 
		switch(v.getId()){
		case R.id.editor_lyric_ok:
			if (R.id.lyric_to_modify == lyricGroup.getCheckedRadioButtonId()) {
				lyricModify = new LyricModify();
				lyricModify.show();
			} else if (R.id.lyric_to_edit == lyricGroup.getCheckedRadioButtonId()) {
				DialogEditLyric dialogEditLyric = new DialogEditLyric(ServiceManager.getAmtMedia());
				Cursor mCursor = ServiceManager.getMediaplayerService().getCursor();
				String songName = mCursor.getString(mCursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SKINS_DISPALYNAME));
				String songPath = mCursor.getString(mCursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_FILEPATH));
				dialogEditLyric.setSongName(songName);
				dialogEditLyric.setSongPath(songPath);
				dialogEditLyric.show();
			}
			break;
		case R.id.editor_lyric_cancel:
			break;
		}
		dismiss();
	}

}
