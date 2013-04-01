package com.android.media.services;

import java.util.Map;

import android.database.Cursor;

import com.android.media.ffmpeg.CustomMediaPlayer;
import com.android.media.lyric.player.LyricPlayer;

public interface IMediaPlayerService extends IService {
	// public FFMpegPlayer getMediaPlayer();

	public CustomMediaPlayer getMediaPlayer();

	public void changeCorsor(Cursor cursor, int mediaModel);

	public LyricPlayer getLyricplayer();

	public String getLyricPath();

	public byte[] getCLock();
	
	public Cursor getCursor();

	public Map<String, Integer> getPlayingMarks();

	public static final int MEDIA_MODEL_LOCAL = 0;

	public static final int MEDIA_MODEL_KMEDIA = 1;
	
	public static final int NEED_TO_CHANGE_HIGHLIGHT = 0;

	public static final int DONT_CHANGE_HIGHLIGHT = 1;
	
}
