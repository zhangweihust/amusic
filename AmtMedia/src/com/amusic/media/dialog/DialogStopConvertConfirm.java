package com.amusic.media.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;

import com.amusic.media.R;
import com.amusic.media.ffmpeg.ExtAudioRecorder;
import com.amusic.media.screens.impl.ScreenKMediaPlayer;

public class DialogStopConvertConfirm{
	private Button btn_ok;
	private Button btn_cancel;

	private Dialog stopConvertConfirmDialog;
	
   private boolean stopConvert = false;
   public boolean getStopConvertState(){
	   return stopConvert;
   }

   
	public DialogStopConvertConfirm(Context context) {
		
		stopConvertConfirmDialog = new Dialog(context,R.style.CustomDialog);
		stopConvertConfirmDialog.setContentView(R.layout.dialog_stop_convert_confirm);
		stopConvertConfirmDialog.setCanceledOnTouchOutside(true);
		init();
		
	}

	private void init() {
		btn_ok = (Button)stopConvertConfirmDialog.findViewById(R.id.btn_stop_convert_confirm);
		btn_cancel = (Button)stopConvertConfirmDialog.findViewById(R.id.btn_stop_convert_cancel);
		btn_ok.setOnClickListener(btn_ok_listener);
		btn_cancel.setOnClickListener(btn_cancel_listener);
		stopConvertConfirmDialog.setOnDismissListener(dialog_dismiss_listener);
	}

	public void show() {
		if(!stopConvertConfirmDialog.isShowing()){
			stopConvertConfirmDialog.show();
		}
		
	}

	public void dismiss() {
		stopConvertConfirmDialog.dismiss();
	}

	private View.OnClickListener btn_ok_listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			stopConvertConfirmDialog.dismiss();
			ExtAudioRecorder.getInstanse().getAudioConvert().setControlInfo((int)2);
			stopConvert  = true;
			
		}
	};
	

	
	private View.OnClickListener btn_cancel_listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			stopConvertConfirmDialog.dismiss();
			//presiousDialog.show();
			ScreenKMediaPlayer.getInstance().getDialogConvertPcmProcess().show();
		}
	};
	
	private Dialog.OnDismissListener dialog_dismiss_listener = new Dialog.OnDismissListener(){

		@Override
		public void onDismiss(DialogInterface dialog) {
			// TODO Auto-generated method stub
			if(ExtAudioRecorder.getInstanse().getIsConverting() && stopConvert == false){
				ScreenKMediaPlayer.getInstance().getDialogConvertPcmProcess().show();
			}
		}
		
	};
	
}
