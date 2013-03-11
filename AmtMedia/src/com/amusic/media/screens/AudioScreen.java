package com.amusic.media.screens;

import android.widget.AbsListView.OnScrollListener;

public class AudioScreen extends Screen implements OnScrollListener {

	public AudioScreen( ) {
		super(ScreenType.TYPE_AUDIO);
	}

	@Override
	public void onBackPressed() {
		getParent().onBackPressed();
	}

}
