package com.amusic.media.screens.impl;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.amusic.media.R;
import com.amusic.media.event.impl.MediaEventArgs;
import com.amusic.media.event.impl.MediaEventTypes;
import com.amusic.media.lyric.player.LyricPlayer;
import com.amusic.media.lyric.render.PhoneKTVView;
import com.amusic.media.screens.AmtScreen;
import com.amusic.media.services.IMediaEventService;
import com.amusic.media.services.IMediaPlayerService;
import com.amusic.media.services.impl.ServiceManager;
import com.amusic.media.utils.Constant;

public class ScreenAudioSongLyrics extends AmtScreen{
	// private ImageView album;
	private PhoneKTVView ktvView;
	private final IMediaPlayerService mediaPlayerService;
	private final LyricPlayer lyricplayer;
   private ImageView singerView;
	public ScreenAudioSongLyrics() {
		this.mediaPlayerService = ServiceManager.getMediaplayerService();
		lyricplayer = mediaPlayerService.getLyricplayer();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_audio_song_lyrics);
		singerView = (ImageView) findViewById(R.id.screen_audio_song_lyrics_album);
		ktvView = (PhoneKTVView) findViewById(R.id.screen_audio_song_lyrics_lyrics);
		lyricplayer.setLyricView(ktvView);
		ScreenAudioPlayer.singerView = singerView;
		ScreenKMediaPlayer.singerView = singerView;
		ScreenRecordPlayer.singerView = singerView;
//		DesktopLyric dl = DesktopLyric.getInstance();
//		dl.showDesktopLyric();
		
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		
		if (ktvView != null) {
			ktvView.stop();
			ktvView.setRender(null);
		}
		super.onStop();
	}
	protected void onNewIntent(Intent intent) {
		if (ktvView != null) {
//			lyricplayer.clearLyricInfo();
		}
		singerView = (ImageView) findViewById(R.id.screen_audio_song_lyrics_album);
		ktvView = (PhoneKTVView) findViewById(R.id.screen_audio_song_lyrics_lyrics);
		lyricplayer.setLyricView(ktvView);
		ScreenAudioPlayer.singerView = singerView;
		ScreenKMediaPlayer.singerView = singerView;
		ScreenRecordPlayer.singerView = singerView;
//		if (this.getParent() instanceof ScreenAudioPlayer) {
//		    IMediaEventService mediaEventService = ServiceManager.getMediaEventService();
//			MediaEventArgs args = new MediaEventArgs();
//			mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_RELOAD_INFO));
//		}
		super.onNewIntent(intent);
//		lyricplayer.setLyricView(ktvView);
	}
	
	@Override
	protected void onDestroy() {
		if (ktvView != null) {
			ktvView.stop();
			ktvView.setRender(null);
		}
		super.onDestroy();
	}
	
    @Override
	protected void onRestart() {
//    	setContentView(R.layout.screen_audio_song_lyrics);	
		singerView = (ImageView) findViewById(R.id.screen_audio_song_lyrics_album);
		ktvView = (PhoneKTVView) findViewById(R.id.screen_audio_song_lyrics_lyrics);
		lyricplayer.setLyricView(ktvView);
		ScreenAudioPlayer.singerView = singerView;
		ScreenKMediaPlayer.singerView = singerView;
		ScreenRecordPlayer.singerView = singerView;
		if (this.getParent() instanceof ScreenAudioPlayer) {
		    IMediaEventService mediaEventService = ServiceManager.getMediaEventService();
			MediaEventArgs args = new MediaEventArgs();
			mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_RELOAD_INFO));
		}
		super.onRestart();
	}	
    
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	Constant.WHICH_LYRIC_PLAYER = 1;
    }
}
