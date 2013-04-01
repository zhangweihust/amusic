package com.android.media.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.amusic.media.R;
import com.android.media.AmtMedia;
import com.android.media.event.IMediaEventArgs;
import com.android.media.event.impl.MediaEventArgs;
import com.android.media.event.impl.MediaEventTypes;
import com.android.media.ffmpeg.ExtAudioRecorder;
import com.android.media.screens.impl.ScreenKMediaPlayer;
import com.android.media.services.impl.ServiceManager;

public class DialogConfirmSaveRecord{

	private Button btn_yes;
	private Button btn_no;
	private Button btn_cancel;
	private RadioGroup radio_group;
    private RadioButton radio_button_mp3;
    private RadioButton radio_button_wav;
	private Dialog ConfirmSaveRecordDialog;
    private Context dialogContext;
    
	public DialogConfirmSaveRecord(Context context) {
		dialogContext = context;
		ConfirmSaveRecordDialog = new Dialog(context, R.style.CustomDialog);
		ConfirmSaveRecordDialog.setContentView(R.layout.dialog_confirm_save_record);
		ConfirmSaveRecordDialog.setCanceledOnTouchOutside(true);
		init();
	}

	private void init() {

		radio_group = (RadioGroup)ConfirmSaveRecordDialog.findViewById(R.id.btn_confirm_save_record_radio_group);
		radio_group.setOnCheckedChangeListener(new
		           RadioGroup.OnCheckedChangeListener(){
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
					}
			
		});
		radio_button_mp3 = (RadioButton)ConfirmSaveRecordDialog.findViewById(R.id.btn_mp3);
		radio_button_wav = (RadioButton)ConfirmSaveRecordDialog.findViewById(R.id.btn_wav);
		
		
		btn_yes = (Button) ConfirmSaveRecordDialog.findViewById(R.id.btn_confirm_save_record_yes);
		btn_no = (Button) ConfirmSaveRecordDialog.findViewById(R.id.btn_confirm_save_record_no);
		btn_cancel = (Button) ConfirmSaveRecordDialog.findViewById(R.id.btn_confirm_save_record_cancel);

		btn_yes.setOnClickListener(btn_yes_listener);
		btn_no.setOnClickListener(btn_no_listener);
		btn_cancel.setOnClickListener(btn_cancel_listener);

	}

	public void show() {
		if(!ConfirmSaveRecordDialog.isShowing()&&
				!ScreenKMediaPlayer.getInstance()
				.getDialogConvertPcmProcess()
				.getConvertPcmProcessDialog()
				.isShowing()){
			ConfirmSaveRecordDialog.show();	
		}
		
	}

	public void dismiss() {
		ConfirmSaveRecordDialog.dismiss();
	}

	private View.OnClickListener btn_yes_listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(R.id.btn_mp3 ==radio_group.getCheckedRadioButtonId()){
				ExtAudioRecorder.getInstanse().setExtraRecordFileInfo();
				if (ExtAudioRecorder.getInstanse().getIsRecording()){
					 ExtAudioRecorder.getInstanse().setSaveRecordFlag(true,0);
					 ExtAudioRecorder.getInstanse().stop();
				 }
				
				ScreenKMediaPlayer.stopMedia_save_as_record = true;
				IMediaEventArgs eventArgs = new MediaEventArgs();
				ServiceManager.getMediaEventService().onMediaUpdateEvent(eventArgs.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_STOP));
				dismiss();
				
				ScreenKMediaPlayer.getInstance().getDialogConvertPcmProcess().show();
				ExtAudioRecorder.getInstanse().setHandler(ScreenKMediaPlayer.getInstance().getDialogConvertPcmProcess().getHandler());
				ExtAudioRecorder.getInstanse().setDialogConvertPcmProcessContext(ScreenKMediaPlayer.getInstance().getDialogConvertPcmProcess());
				ExtAudioRecorder.getInstanse().setKMediaSCreenContext(dialogContext);
		}else if(R.id.btn_wav ==radio_group.getCheckedRadioButtonId()){
			ExtAudioRecorder.getInstanse().setExtraRecordFileInfo();
			if (ExtAudioRecorder.getInstanse().getIsRecording()){
				 ExtAudioRecorder.getInstanse().setSaveRecordFlag(true,1);
				 ExtAudioRecorder.getInstanse().stop();
			 }

			ScreenKMediaPlayer.stopMedia_save_as_record = true;
			IMediaEventArgs eventArgs = new MediaEventArgs();
			ServiceManager.getMediaEventService().onMediaUpdateEvent(eventArgs.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_STOP));
			dismiss();
			ServiceManager.finishScreenKMediaPlayer();
		   ServiceManager.getMediaEventService().onMediaUpdateEvent(eventArgs.setMediaUpdateEventTypes(MediaEventTypes.RECORD_UPDATE_DATA));
		}
			// 保存K歌之前的状态并设置状态让其可以播放上一次播放的歌曲
			ServiceManager.saveState();
			ServiceManager.isPlayed = false;
			AmtMedia.s_goPlayerBtn_click_num = -1;
	 }
	};
	
	private View.OnClickListener btn_no_listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub

			if(ExtAudioRecorder.getInstanse().getIsRecording()){
				 ExtAudioRecorder.getInstanse().setSaveRecordFlag(false,0);
				 ExtAudioRecorder.getInstanse().stop();
			 }
			ScreenKMediaPlayer.stopMedia_save_as_record = true;
			IMediaEventArgs eventArgs = new MediaEventArgs();
			ServiceManager.getMediaEventService().onMediaUpdateEvent(eventArgs.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_STOP));
			dismiss();
			ServiceManager.finishScreenKMediaPlayer();
			// 保存K歌之前的状态并设置状态让其可以播放上一次播放的歌曲
			ServiceManager.saveState();
			ServiceManager.isPlayed = false;
			AmtMedia.s_goPlayerBtn_click_num = -1;
		}
	};
	
	private View.OnClickListener btn_cancel_listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			dismiss();
		}
	};
	

}
