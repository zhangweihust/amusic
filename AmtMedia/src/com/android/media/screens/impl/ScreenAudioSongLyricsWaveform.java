package com.android.media.screens.impl;

import android.os.Bundle;

import com.amusic.media.R;
import com.android.media.screens.AmtScreen;

public class ScreenAudioSongLyricsWaveform extends AmtScreen {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_audio_song_lyrics_waveform);
	}
}
