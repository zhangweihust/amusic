package com.android.media.screens.impl;
import android.app.ActivityGroup;
import android.os.Bundle;

import com.amusic.media.R;
import com.android.media.screens.IScreen;
import com.android.media.services.impl.ServiceManager;

public class ScreenRecordRoot extends ActivityGroup implements IScreen{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_record_root);
		ServiceManager.setRecordRoot(this);
		ServiceManager.getRecordScreenService().show(ScreenRecord.class);
	}

	@Override
	public void onBackPressed() {
		if (!ServiceManager.getRecordScreenService().goback()) {
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
