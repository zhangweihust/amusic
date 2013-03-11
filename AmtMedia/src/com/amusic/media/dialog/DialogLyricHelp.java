package com.amusic.media.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;

import com.amusic.media.R;

public class DialogLyricHelp {
	private Dialog songInfoEditdlg;
	private Button btn_ok;
	private Button btn_cancel;

	public DialogLyricHelp(Context context) {
		songInfoEditdlg = new Dialog(context,
				R.style.CustomDialog);
		songInfoEditdlg.setCanceledOnTouchOutside(true);
		songInfoEditdlg.setContentView(R.layout.help_lyric_dialog);
		btn_ok = (Button) songInfoEditdlg.findViewById(R.id.positiveButton);
		btn_cancel = (Button) songInfoEditdlg.findViewById(R.id.negativeButton);
		
		btn_ok.setOnClickListener(btn_ok_listener);
		btn_cancel.setOnClickListener(btn_ok_listener);
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
	
	private View.OnClickListener btn_ok_listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			dismiss();
		}
	};
}
