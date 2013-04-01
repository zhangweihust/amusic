package com.android.media.dialog;

import com.amusic.media.R;
import com.android.media.services.impl.ServiceManager;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class DialogLyricSave {
	private Dialog songInfoEditdlg;
	private Button btn_ok;
	private Button btn_cancel;
	private EditText lyric_maker;
	private TextView lyric_maker_name;
	private TextView lyric_line;
	private TextView lyric_save;

	private String orginal_name = "";
	private String orginal_artist = "";
	public DialogLyricSave(Context context) {
		songInfoEditdlg = new Dialog(context,
				R.style.CustomDialog);
		songInfoEditdlg.setCanceledOnTouchOutside(true);
		songInfoEditdlg.setContentView(R.layout.screen_audio_save_lyric);
		init();
	}
	
	public void init() {
		lyric_maker_name = (TextView)songInfoEditdlg.findViewById(R.id.lyric_maker_name);
		lyric_line = (TextView)songInfoEditdlg.findViewById(R.id.lyric_audio_line);
		btn_ok = (Button) songInfoEditdlg.findViewById(R.id.btn_save_ok);
		btn_cancel = (Button) songInfoEditdlg.findViewById(R.id.btn_save_cancel);

		lyric_save = (TextView)songInfoEditdlg.findViewById(R.id.lyric_maker_save);
		lyric_maker = (EditText) songInfoEditdlg.findViewById(R.id.audio_lyric_artist);
	}
	
	public Button getBtnOK() {
		return btn_ok;
	}
	
	public Button getBtnCancle() {
		return btn_cancel;
	}
	
	public TextView getLyricMakerName() {
		return lyric_maker_name;
	}
	
	public TextView getLyricLine() {
		return lyric_line;
	}
	
	public TextView getLyricSave() {
		return lyric_save;
	}
	
	public EditText getEditText() {
		return lyric_maker;
	}
	
	public void show() {
		songInfoEditdlg.show();
	}
	
	public void dismiss() {
		songInfoEditdlg.dismiss();
	}
	
	public boolean isShowing() {
		return songInfoEditdlg.isShowing();
	}

}
