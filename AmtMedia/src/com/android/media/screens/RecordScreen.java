package com.android.media.screens;

import android.widget.AbsListView.OnScrollListener;

public class RecordScreen extends Screen implements OnScrollListener {

	public RecordScreen() {
		super(ScreenType.TYPE_RECORD);
	}

	@Override
	public void onBackPressed() {
		getParent().onBackPressed();
	}
}
