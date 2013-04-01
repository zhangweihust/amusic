package com.android.media.screens;

import android.widget.AbsListView.OnScrollListener;

public class LyricScreen extends Screen implements OnScrollListener{
	public LyricScreen( ) {
		super(ScreenType.TYPE_AUDIO);
	}

	@Override
	public void onBackPressed() {
		getParent().onBackPressed();
	}
}
