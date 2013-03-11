package com.amusic.media.screens.impl;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.amusic.media.AmtMedia;
import com.amusic.media.R;
import com.amusic.media.event.IMediaEventArgs;
import com.amusic.media.event.impl.MediaEventArgs;
import com.amusic.media.event.impl.MediaEventTypes;
import com.amusic.media.screens.AmtScreen;
import com.amusic.media.services.impl.MediaPlayerService;
import com.amusic.media.services.impl.ServiceManager;
import com.amusic.media.utils.ToastUtil;

public class ScreenPlayMode extends AmtScreen implements OnClickListener {
	public static int temp = -1;
	private Button ok;
	private Button cancel;
	private static final int audioPlayOrderMode =1;
	private static final int audioPlayListcycleMode =2;
	private static final int audioPlaySingleMode =3;
	private static final int audioPlayShufflemMode =4;
	private RelativeLayout layoutPlayOrderMode;
	private RelativeLayout layoutPlayListcycleMode;
	private RelativeLayout layoutPlaySingleMode;
	private RelativeLayout layoutPlayShuffleMode;
	private CheckBox boxPlayOrderMode;
	private CheckBox boxPlayListcycleMode;
	private CheckBox boxPlaySingleMode;
	private CheckBox boxPlayShuffleMode;
	private static AmtMedia amtMedia;
	private int mode = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_play_mode);
		ok = (Button) findViewById(R.id.screen_play_mode_ok);
		cancel = (Button) findViewById(R.id.screen_play_mode_cancal);
		ok.setOnClickListener(this);
		cancel.setOnClickListener(this);
		
		boxPlayListcycleMode = (CheckBox) findViewById(R.id.screen_play_listcycle_mode);
		boxPlaySingleMode = (CheckBox) findViewById(R.id.screen_play_single_mode);
		boxPlayShuffleMode = (CheckBox) findViewById(R.id.screen_play_shuffle_mode);
		boxPlayOrderMode = (CheckBox) findViewById(R.id.screen_play_order_mode);
		
		layoutPlayListcycleMode = (RelativeLayout) findViewById(R.id.screen_play_listcycle_mode_layout);
		layoutPlaySingleMode = (RelativeLayout) findViewById(R.id.screen_play_single_mode_layout);
		layoutPlayShuffleMode = (RelativeLayout) findViewById(R.id.screen_play_shuffle_mode_layout);
		layoutPlayOrderMode = (RelativeLayout) findViewById(R.id.screen_play_order_mode_layout);
		
		layoutPlayListcycleMode.setOnClickListener(this);
		layoutPlaySingleMode.setOnClickListener(this);
		layoutPlayShuffleMode.setOnClickListener(this);
		layoutPlayOrderMode.setOnClickListener(this);
		
		refresh();
	}
	
	@Override
	public boolean refresh() {
		// TODO Auto-generated method stub
		SharedPreferences sharedata = getSharedPreferences("lastsong", Context.MODE_WORLD_WRITEABLE);
		int audioPlayMode = sharedata.getInt("flagAudioPlayMode", 0);
		if(ScreenAudioPlayer.playModeClick==0){			
			setCheckBoxState(audioPlayMode);
		}else{
			setCheckBoxState(ServiceManager.flagAudioPlayMode);
		} 
		return true;
	}
	
	private void setCheckBoxState(int mode){
		switch(mode){
		case R.id.screen_play_order_mode_layout:
		case audioPlayOrderMode:
			boxPlayOrderMode.setChecked(true);
			boxPlayListcycleMode.setChecked(false);
			boxPlaySingleMode.setChecked(false);
			boxPlayShuffleMode.setChecked(false);
			break;
		case R.id.screen_play_listcycle_mode_layout:
		case audioPlayListcycleMode:
			boxPlayOrderMode.setChecked(false);
			boxPlayListcycleMode.setChecked(true);
			boxPlaySingleMode.setChecked(false);
			boxPlayShuffleMode.setChecked(false);
			break;
		case R.id.screen_play_single_mode_layout:
		case audioPlaySingleMode:
			boxPlayOrderMode.setChecked(false);
			boxPlayListcycleMode.setChecked(false);
			boxPlaySingleMode.setChecked(true);
			boxPlayShuffleMode.setChecked(false);
		break;
		case R.id.screen_play_shuffle_mode_layout:
		case audioPlayShufflemMode:
			boxPlayOrderMode.setChecked(false);
			boxPlayListcycleMode.setChecked(false);
			boxPlaySingleMode.setChecked(false);
			boxPlayShuffleMode.setChecked(true);
		break;
		default:
			boxPlayOrderMode.setChecked(true);
			boxPlayListcycleMode.setChecked(false);
			boxPlaySingleMode.setChecked(false);
			boxPlayShuffleMode.setChecked(false);
		break;		
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		ServiceManager.getAmtMedia().getGoPlayerBtn().setVisibility(View.INVISIBLE);
		setScreenTitle(getString(R.string.screen_audio_player_mode_title));
	}

	@Override
	public boolean hasMenu() {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.screen_play_listcycle_mode_layout:
			if(!boxPlayListcycleMode.isChecked()){
				setCheckBoxState(v.getId());
				mode = audioPlayListcycleMode;
			}/*else{
				boxPlayListcycleMode.setChecked(false);
			}*/
			break;
		case R.id.screen_play_single_mode_layout:
			if(!boxPlaySingleMode.isChecked()){
				setCheckBoxState(v.getId());
				mode = audioPlaySingleMode;
			}/*else{
				boxPlaySingleMode.setChecked(false);
			}*/
			break;
		case R.id.screen_play_shuffle_mode_layout:
			if(!boxPlayShuffleMode.isChecked()){
				setCheckBoxState(v.getId());
				mode = audioPlayShufflemMode;
			}/*else{
				boxPlayShuffleMode.setChecked(false);
			}*/
			break;
		case R.id.screen_play_order_mode_layout:
			if(!boxPlayOrderMode.isChecked()){
				setCheckBoxState(v.getId());
				mode = audioPlayOrderMode;
			}/*else{
				boxPlayOrderMode.setChecked(false);
			}*/
			break;
		case R.id.screen_play_mode_ok:	
			if(!isHasOneChecked()){
				Toast toast = ToastUtil.getInstance().getToast(getString(R.string.screen_audio_player_mode_toast));
				toast.setDuration(Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}else{
				setPlayMode(mode);
				this.onBackPressed();
			}
			break;
		case R.id.screen_play_mode_cancal:
			switch(mode){
			case audioPlayOrderMode:
				boxPlayOrderMode.setChecked(true);
				boxPlayListcycleMode.setChecked(false);
				boxPlaySingleMode.setChecked(false);
				boxPlayShuffleMode.setChecked(false);
				break;
			case audioPlayListcycleMode:
				boxPlayOrderMode.setChecked(false);
				boxPlayListcycleMode.setChecked(true);
				boxPlaySingleMode.setChecked(false);
				boxPlayShuffleMode.setChecked(false);
				break;
			case audioPlaySingleMode:
				boxPlayOrderMode.setChecked(false);
				boxPlayListcycleMode.setChecked(false);
				boxPlaySingleMode.setChecked(true);
				boxPlayShuffleMode.setChecked(false);
			break;
			case audioPlayShufflemMode:
				boxPlayOrderMode.setChecked(false);
				boxPlayListcycleMode.setChecked(false);
				boxPlaySingleMode.setChecked(false);
				boxPlayShuffleMode.setChecked(true);
			break;
			default:
				boxPlayOrderMode.setChecked(true);
				boxPlayListcycleMode.setChecked(false);
				boxPlaySingleMode.setChecked(false);
				boxPlayShuffleMode.setChecked(false);
			break;		
			}
			this.onBackPressed();
			break;
	
		}
	}
	
	private void setPlayMode(int mode){
		IMediaEventArgs eventArgs = new MediaEventArgs();
		amtMedia =ServiceManager.getAmtMedia();
		Editor sharedata = amtMedia.getSharedPreferences("lastsong", Context.MODE_WORLD_WRITEABLE).edit();
		switch(mode){
		case audioPlayListcycleMode:
			ServiceManager.flagAudioPlayMode = audioPlayListcycleMode;
			MediaPlayerService.flagAudioPlayMode = MediaEventTypes.MEDIA_PLAY_MODE_LIST_CYCLE;
			mediaEventService.onMediaUpdateEvent(eventArgs.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAY_MODE_SINGLE_REPEAT_CANCLE));
			sharedata.putInt("flagAudioPlayMode",ServiceManager.flagAudioPlayMode);
			break;
		case audioPlaySingleMode:
			ServiceManager.flagAudioPlayMode = audioPlaySingleMode;
			MediaPlayerService.flagAudioPlayMode = MediaEventTypes.MEDIA_PLAY_MODE_SINGLE_REPEAT;
			mediaEventService.onMediaUpdateEvent(eventArgs.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAY_MODE_SINGLE_REPEAT));
			sharedata.putInt("flagAudioPlayMode",ServiceManager.flagAudioPlayMode);
			break;
		case audioPlayShufflemMode:
			ServiceManager.flagAudioPlayMode = audioPlayShufflemMode;
			MediaPlayerService.flagAudioPlayMode = MediaEventTypes.MEDIA_PLAY_MODE_SHUFFLE;
			mediaEventService.onMediaUpdateEvent(eventArgs.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAY_MODE_SHUFFLE_REPEAT));
			sharedata.putInt("flagAudioPlayMode",ServiceManager.flagAudioPlayMode);
			break;
		case audioPlayOrderMode:
			ServiceManager.flagAudioPlayMode = audioPlayOrderMode;
			MediaPlayerService.flagAudioPlayMode = MediaEventTypes.MEDIA_PLAY_MODE_ORDER;
			mediaEventService.onMediaUpdateEvent(eventArgs.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAY_MODE_SINGLE_REPEAT_CANCLE));
			sharedata.putInt("flagAudioPlayMode",ServiceManager.flagAudioPlayMode);
			break;
		}
		sharedata.commit();
		
	}
	private boolean isHasOneChecked(){
		boolean hasOneChecked = false;
		if(boxPlayOrderMode.isChecked() || boxPlayListcycleMode.isChecked() 
				|| boxPlaySingleMode.isChecked() || boxPlayShuffleMode.isChecked()){
			hasOneChecked = true;
		}
		return hasOneChecked;
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
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		refresh();
	}
}

