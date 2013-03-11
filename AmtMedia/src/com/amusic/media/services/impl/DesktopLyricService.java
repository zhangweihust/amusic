package com.amusic.media.services.impl;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;

import com.amusic.media.MediaApplication;
import com.amusic.media.R;
import com.amusic.media.lyric.player.LyricPlayer;
import com.amusic.media.lyric.render.DefaultDesktopView;
import com.amusic.media.lyric.render.DesktopKTVView;
import com.amusic.media.services.IDesktopLyricService;
import com.amusic.media.services.IMediaPlayerService;

public class DesktopLyricService implements IDesktopLyricService {
	private WindowManager.LayoutParams wmParams;
	private static DesktopLyricService instance;
	private WindowManager wm;
	private DesktopKTVView desktopView;
	private DefaultDesktopView defaultDesktopView;
	private int visibility;
//	private static int SCREEN_HEIGHT;
	private static int SCREEN_WIDTH;
	private boolean isAmtMedia = true;
	private boolean isScreenLyricSpeed = false;
	

	private boolean isDefaultDesktopView = false;
	private LayoutInflater inflater = LayoutInflater.from(MediaApplication.getContext());
	private DesktopLyricService(){
		wm = (WindowManager) MediaApplication.getContext().getSystemService("window");
//		SCREEN_HEIGHT = wm.getDefaultDisplay().getHeight();
		SCREEN_WIDTH = wm.getDefaultDisplay().getWidth();
	}
	public static DesktopLyricService getInstance(){
		if(instance==null){
			instance = new DesktopLyricService();
		}
		return instance;
	}
	
	
	public void showDesktopLyric(View view, int x, int y) {
		if(view instanceof DesktopKTVView){
			isDefaultDesktopView = false;
		}else if(view instanceof DefaultDesktopView){
			isDefaultDesktopView = true;
		}
		wmParams = MediaApplication.getInstance().getWmParams();
		wmParams.type = WindowManager.LayoutParams.TYPE_PHONE  ;
		wmParams.format = 1;
		wmParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

		wmParams.gravity = Gravity.LEFT | Gravity.TOP;
		wmParams.width = LayoutParams.FILL_PARENT;
		wmParams.height = MediaApplication.getContext().getResources().getDimensionPixelSize(R.dimen.LinearLayout_desktop_lev1_layout_height);
		wmParams.x = (MediaApplication.getScreenWidth() - wmParams.width) / 2;
		wmParams.y = y;
		
		wm.addView(view, wmParams);
	}
	
	@Override
	public boolean start() {
		View content = inflater.inflate(R.layout.desktop_lyric_view, null, false);
		desktopView = (DesktopKTVView) content.findViewById(R.id.screen_desktop_lyrics);
		content = inflater.inflate(R.layout.default_desktop_lyric_view, null, false);
		defaultDesktopView = (DefaultDesktopView) content.findViewById(R.id.screen_default_desktop_lyrics);
		IMediaPlayerService mediaPlayerService = ServiceManager.getMediaplayerService();
		LyricPlayer lyricplayer = mediaPlayerService.getLyricplayer();
		lyricplayer.setDesktopView(desktopView);
		showDesktopLyric(desktopView,0, 200);
		return true;
	}
	
	public void showDefaultDesktopView(){
		defaultDesktopView.setVisibility(View.VISIBLE);
		removeView();
		showDesktopLyric(defaultDesktopView,0, 200);
	}
	
	public void showDesktopLyric(){
		desktopView.setVisibility(View.VISIBLE);
		removeView();
		showDesktopLyric(desktopView,0, 200);

	}

	public void removeDesktopLyric() {
		removeView();
		defaultDesktopView = null;
		desktopView = null;
	}
	
	private void removeView(){
		if(isDefaultDesktopView){
			wm.removeView(defaultDesktopView);
		}else{
			wm.removeView(desktopView);
		}
	}
	public void setVisible(int visibility){
		this.visibility = visibility;
		desktopView.setVisibility(visibility);
		defaultDesktopView.setVisibility(visibility);
	}
	
	public View getDesktopView(){
		return desktopView;
	}
	
	public View getDefaultDesktopView(){
		return defaultDesktopView;
	}
	
	public void setAmtMedia(boolean isAmtMedia){
		this.isAmtMedia = isAmtMedia;
	}
	
	public boolean isAmtMedia(){
		return this.isAmtMedia;
	}
	
	public boolean isDefaultDesktopView(){
		return this.isDefaultDesktopView;
	}

	public void setScreenLyricSpeed(boolean isScreenLyricSpeed) {
		this.isScreenLyricSpeed = isScreenLyricSpeed;
	}
	
	public int getVisibility(){
		return this.visibility;
	}

	@Override
	public boolean stop() {
		removeDesktopLyric();
		return true;
	}

	
}
