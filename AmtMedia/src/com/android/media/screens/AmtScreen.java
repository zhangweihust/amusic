package com.android.media.screens;
import android.os.Bundle;
import android.widget.AbsListView.OnScrollListener;

public class AmtScreen extends Screen implements OnScrollListener{

	public AmtScreen() {
		super(ScreenType.TYPE_AMT);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onBackPressed() {
		getParent().onBackPressed();
	}
}
