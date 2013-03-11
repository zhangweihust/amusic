package com.amusic.media.screens;

import android.widget.AbsListView.OnScrollListener;
import android.widget.LinearLayout;

import com.amusic.media.adapter.MenuContentAdapter;
import com.amusic.media.services.impl.ServiceManager;
import com.amusic.media.utils.Constant;

public class SearchScreen extends Screen implements OnScrollListener {

	public static LinearLayout keyboard;

	public SearchScreen() {
		super(ScreenType.TYPE_SEARCH);
	}

	@Override
	public void onBackPressed() {
			getParent().onBackPressed();
	}
	
	@Override
	public boolean changMenuAdapter() {
		MenuContentAdapter[] menuContentAdapters = ServiceManager.getMenuContentAdapters();
		menuContentAdapters[Constant.MenuConstant.tools].getMenuData().remove(Constant.MenuConstant.menu_item_mode);
		return true;
	}
	
	@Override
	public boolean isMenuChanged() {
		// TODO Auto-generated method stub
		return true;
	}

}
