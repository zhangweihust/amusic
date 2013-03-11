package com.amusic.media.screens.impl;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.amusic.media.R;
import com.amusic.media.dialog.SreenAudioSearchLyric;
import com.amusic.media.lyric.player.LyricPlayer;
import com.amusic.media.lyric.render.FullLyricView;
import com.amusic.media.screens.AudioScreen;
import com.amusic.media.services.IMediaPlayerService;
import com.amusic.media.services.impl.ServiceManager;
import com.amusic.media.utils.ColorUtil;
import com.amusic.media.utils.Constant;

public class ScreenAudioSongLyricsFullScreen extends AudioScreen{
	private FullLyricView fullLyricView;
	private final IMediaPlayerService mediaPlayerService;
	private final LyricPlayer lyricplayer;
	private TextView tv;
	private SreenAudioSearchLyric searchLyric;
	public  static boolean isshowing = false; 
	
	public ScreenAudioSongLyricsFullScreen() {
		this.mediaPlayerService = ServiceManager.getMediaplayerService();
		lyricplayer = mediaPlayerService.getLyricplayer();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_audio_song_lyrics_fullscreen);
		fullLyricView = (FullLyricView) findViewById(R.id.screen_audio_song_lyrics_fullscreen);
        tv = (TextView) findViewById(R.id.screen_audio_song_lyrics_fullscreen_textview);
		tv.setOnClickListener(tvListener);
		tv.setVisibility(View.INVISIBLE);
		ScreenAudioPlayer.textView = tv;
		isshowing = false;
        lyricplayer.setFullLyricView(fullLyricView);
	}
	
	private View.OnClickListener tvListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			tv.setTextColor(ColorUtil.HIGHLIGHT);
			searchLyric = new SreenAudioSearchLyric();
			searchLyric.show();
		}
	};

	@Override
	protected void onNewIntent(Intent intent) {
		fullLyricView = (FullLyricView) findViewById(R.id.screen_audio_song_lyrics_fullscreen);
		tv = (TextView) findViewById(R.id.screen_audio_song_lyrics_fullscreen_textview);
		tv.setOnClickListener(tvListener);
		ScreenAudioPlayer.textView = tv;
		if (!isshowing) {
			tv.setVisibility(View.INVISIBLE);
		}
        lyricplayer.setFullLyricView(fullLyricView);
		super.onNewIntent(intent);
//		lyricplayer.setFullLyricView(fullLyricView);
	}
	
	@Override
	protected void onDestroy() {
		if (fullLyricView != null) {
			fullLyricView.stop();
			fullLyricView.setRender(null);
		}
		super.onDestroy();
	}
    @Override
	protected void onRestart() {
    	
//		setContentView(R.layout.screen_audio_song_lyrics_fullscreen);
		fullLyricView = (FullLyricView) findViewById(R.id.screen_audio_song_lyrics_fullscreen);
		tv = (TextView) findViewById(R.id.screen_audio_song_lyrics_fullscreen_textview);
		tv.setOnClickListener(tvListener);
		ScreenAudioPlayer.textView = tv;
		if (!isshowing) {
			tv.setVisibility(View.INVISIBLE);
		}
        lyricplayer.setFullLyricView(fullLyricView);
		super.onRestart();
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		if (fullLyricView != null) {
			fullLyricView.stop();
			fullLyricView.setRender(null);
		}
		super.onStop();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Constant.WHICH_LYRIC_PLAYER = 2;
	}

}
