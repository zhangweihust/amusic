package com.android.media.dialog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;

import com.amusic.media.R;
import com.android.media.AmtMedia;
import com.android.media.MediaApplication;
import com.android.media.lyric.player.LyricPlayer;
import com.android.media.provider.MediaDatabaseHelper;
import com.android.media.services.impl.ServiceManager;
import com.android.media.toolbox.DETool;

public class LyricModify implements OnClickListener{
	private PopupWindow lyricModifyPW;
	private Button lyric_modify_edit;
	private Button lyric_modify_cancel;
	private Button lyric_modify_up;
	private Button lyric_modify_down;
	private Button lyric_modify_ok;
	private LyricPlayer lyricPlayer = null;
	public static int upNum = 0;
	public static int downNum = 0;
	private View layout;
	private boolean isOK = false;
	public  static boolean isPop = false;
	public LyricModify() {
		LayoutInflater inflater = LayoutInflater.from(ServiceManager.getAmtMedia());
		layout = inflater.inflate(R.layout.lyric_modify_dialog, null);
		lyricModifyPW = new PopupWindow(layout, LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		
		lyricModifyPW.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss() {
				if (isPop) {
				    doCancle();
				}
			}
		});
		lyricModifyPW.setFocusable(true);
		lyricModifyPW.setOutsideTouchable(false);
		lyricModifyPW.setBackgroundDrawable(new BitmapDrawable());
		lyricPlayer = ServiceManager.getMediaplayerService().getLyricplayer();
		init();
		
	}

	public void init() {
		lyric_modify_cancel = (Button) layout.findViewById(R.id.lyric_modify_cancel);
		lyric_modify_up = (Button) layout.findViewById(R.id.lyric_modify_up);
		lyric_modify_down = (Button) layout.findViewById(R.id.lyric_modify_down);
		lyric_modify_ok = (Button) layout.findViewById(R.id.lyric_modify_ok);
		
		lyric_modify_cancel.setOnClickListener(this);
		lyric_modify_up.setOnClickListener(this);
		lyric_modify_down.setOnClickListener(this);
		lyric_modify_ok.setOnClickListener(this);
	} 

	public void show() {
		isPop = true;
		LayoutInflater inflater = LayoutInflater.from(ServiceManager.getAmtMedia());
		View parent = inflater.inflate(R.layout.screen_audio, null);
		lyricModifyPW.showAtLocation(parent, Gravity.RIGHT,0, -MediaApplication.getContext().getResources().getDimensionPixelSize(R.dimen.screen_audio_song_lyrics_offsetY));
	}
	
	public void dismiss(){
		if(lyricModifyPW.isShowing()){
			lyricModifyPW.dismiss();
		}
	}
	
	public void doCancle() {
//		String lyrics = lyricPlayer.getLyricsInfo().getLyrics();
		lyricPlayer.adjustAll(500 * upNum - 500 * downNum);
		isPop = false;
		upNum = 0;
		downNum = 0;
	}
	
	/**
	 * 
	 */
	public void doSave() {
		String lyricPath = ServiceManager.getMediaplayerService().getLyricPath();
		String lyrics = lyricPlayer.getLyricsInfo().getLyrics();
		String txtPath = lyricPath.substring(0, lyricPath.indexOf(".ksc"));
		File lyricFile = new File(lyricPath);
		File txtFile = new File(txtPath);
		
		try {
			if (!txtFile.exists()) {
				txtFile.createNewFile();
			}
			FileWriter outFileWrite = new FileWriter(txtFile,true);
			outFileWrite.write(lyrics);
			outFileWrite.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if (DETool.nativeCreateKsc(txtPath) != -1) {
			if (lyricFile.exists()) {
				lyricFile.delete();
			}
			
			String tmpPath = lyricPath + ".tp";
			File tmpFile = new File(tmpPath);
		    tmpFile.renameTo(new File(lyricPath));
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.lyric_modify_cancel:
			isPop = false;
			lyricModifyPW.dismiss();
			doCancle();
			break;
		case R.id.lyric_modify_up:
			if (lyricPlayer.adjustAll(-500) != null) {
				upNum++;
			}
			break;
		case R.id.lyric_modify_down:
			if (lyricPlayer.adjustAll(500) != null) {
				downNum++;
			}
			break;
		case R.id.lyric_modify_ok:
			isPop = false;
			lyricModifyPW.dismiss();
			
			doSave();
			upNum = 0;
			downNum = 0;
			break;
		}
	}

}