package com.android.media.screens.impl;
import android.app.ActivityGroup;
import android.os.Bundle;
import android.view.View;

import com.amusic.media.R;
import com.android.media.screens.IScreen;
import com.android.media.screens.SearchScreen;
import com.android.media.services.impl.ServiceManager;

public class ScreenSearchRoot extends ActivityGroup implements IScreen {
	private View tabhost;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_search_root);
		ServiceManager.setSearchRoot(this);
		ServiceManager.getSearchScreenService().show(ScreenSearch.class);
	}

	@Override
	public void onBackPressed() {
		if (SearchScreen.keyboard != null
				&& SearchScreen.keyboard.getVisibility() == View.VISIBLE) {
			SearchScreen.keyboard.setVisibility(View.GONE);
			tabhost = ScreenHome.tw;;
			if (tabhost != null) {
				tabhost.findViewById(android.R.id.tabs).setVisibility(View.VISIBLE);
			}
			return;
		}
		if (!ServiceManager.getSearchScreenService().goback()) {
			ServiceManager.getAmtMedia().onBackPressed();
		}
	}

	@Override
	public boolean hasMenu() {
		return false;
	}

	@Override
	public boolean currentable() {
		return false;
	}

	@Override
	public boolean refresh() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean changMenuAdapter() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isMenuChanged() {
		// TODO Auto-generated method stub
		return false;
	}
}
