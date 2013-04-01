package com.android.media.screens;

import android.widget.AbsListView.OnScrollListener;

public class KMediaScreen extends Screen implements OnScrollListener {

	public KMediaScreen() {
		super(ScreenType.TYPE_KMEDIA);
	}

	@Override
	public void onBackPressed() {
		getParent().onBackPressed();
	}
}
