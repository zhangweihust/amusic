package com.amusic.media.screens;

import android.view.MotionEvent;

public interface IScreen {
	public static final int MENU_RE_DOWNLOAD = 1;
	public static final int MENU_DOWNLOAD_PAUSE = 2;
	public static final int MENU_DOWNLOAD_CONTINUE = 3;
	public static final int MENU_DOWNLOAD_CANCEL = 4;
	public static final int MENU_DOWNLOAD_DELETE = 5;
	public static final int SCROLL_TYPE_DEFAULT = 100;
	public static final int SCROLL_TYPE_LISTVIEW = 101;
	public static final int SCROLL_TYPE_GRIDVIEW = 102;

	public boolean hasMenu();

	public boolean currentable();
	
	public boolean changMenuAdapter();

	public boolean refresh();
	
	public boolean isMenuChanged();

	public enum ScreenType {
		TYPE_AMT, TYPE_AUDIO, TYPE_KMEDIA,TYPE_RECORD, TYPE_SEARCH
	}
	public boolean onTouchEvent(MotionEvent event);
}
