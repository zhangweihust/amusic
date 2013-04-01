package com.android.media.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;

import com.amusic.media.R;
import com.android.media.AmtMedia;
import com.android.media.event.IMediaEventArgs;
import com.android.media.event.impl.MediaEventArgs;
import com.android.media.event.impl.MediaEventTypes;
import com.android.media.ffmpeg.ExtAudioRecorder;
import com.android.media.services.impl.ServiceManager;


public class DialogQuitCurrentKMusic {

	private Button btn_yes;
	private Button btn_no;
	private Dialog quitCurrentKMusicDialog;
	private Context context;

	public DialogQuitCurrentKMusic(Context context) {
		this.context = context;
		quitCurrentKMusicDialog = new Dialog(context, R.style.CustomDialog);
		quitCurrentKMusicDialog.setContentView(R.layout.dialog_quit_current_kmusic);
		quitCurrentKMusicDialog.setCanceledOnTouchOutside(true);
		init();
	}

	private void init() {
		btn_yes = (Button) quitCurrentKMusicDialog.findViewById(R.id.btn_quit_kmusic_yes);
		btn_no = (Button) quitCurrentKMusicDialog.findViewById(R.id.btn_quit_kmusic_no);
	
		btn_yes.setOnClickListener(btn_yes_listener);
		btn_no.setOnClickListener(btn_no_listener);

	}

	public void show() {
		if(!quitCurrentKMusicDialog.isShowing()){
			quitCurrentKMusicDialog.show();
		}
	}
	public void dismiss() {
		quitCurrentKMusicDialog.dismiss();
	}

	private View.OnClickListener btn_yes_listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			IMediaEventArgs eventArgs = new MediaEventArgs();
			ServiceManager.getMediaEventService().onMediaUpdateEvent(eventArgs.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_STOP));
			ExtAudioRecorder.getInstanse().stop();
			dismiss();
			ServiceManager.finishScreenKMediaPlayer();
			// 保存K歌之前的状态并设置状态让其可以播放上一次播放的歌曲
			ServiceManager.saveState();
			ServiceManager.isPlayed = false;
			AmtMedia.s_goPlayerBtn_click_num = -1;
		}
	};

	
	private View.OnClickListener btn_no_listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			dismiss();
		}
	};

}
