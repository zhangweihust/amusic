package com.amusic.media.screens.impl;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.amusic.media.R;
import com.amusic.media.model.ScreenArgs;
import com.amusic.media.screens.AmtScreen;
import com.amusic.media.services.impl.ServiceManager;

public class ScreenHelpView extends AmtScreen {
	private TextView title;
	private TextView content;
	private int code;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_help_view);
		title = (TextView) findViewById(R.id.screen_help_view_title);
		content = (TextView) findViewById(R.id.screen_help_view_content);
		ScreenArgs args = (ScreenArgs) getIntent().getSerializableExtra("args");
		code = (Integer) args.getExtra("screen_help_view");
		onFresh(code);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		ScreenArgs args = (ScreenArgs) intent.getSerializableExtra("args");
		code = (Integer) args.getExtra("screen_help_view",code);
		onFresh(code);
	}

	protected void onResume() {
		super.onResume();
		setScreenTitle(getString(R.string.screen_home_menu_function));
		ServiceManager.getAmtMedia().getGoPlayerBtn().setVisibility(View.INVISIBLE);
	};

	@Override
	public boolean hasMenu() {
		// TODO Auto-generated method stub
		return true;
	}
	
	private void onFresh(int code){
		switch (code) {
		case ScreenHelp.SCREEN_HELP_VIEW1:
			title.setText(getString(R.string.screen_help_tv1));
			content.setText(getString(R.string.screen_help_view_content1));
			break;
		case ScreenHelp.SCREEN_HELP_VIEW2:
			title.setText(getString(R.string.screen_help_tv2));
			content.setText(getString(R.string.screen_help_view_content2));
			break;
		case ScreenHelp.SCREEN_HELP_VIEW3:
			title.setText(getString(R.string.screen_help_tv3));
			content.setText(getString(R.string.screen_help_view_content3));
			break;
		case ScreenHelp.SCREEN_HELP_VIEW4:
			title.setText(getString(R.string.screen_help_tv4));
			content.setText(getString(R.string.screen_help_view_content4));
			break;
		case ScreenHelp.SCREEN_HELP_VIEW5:
			title.setText(getString(R.string.screen_help_tv5));
			content.setText(getString(R.string.screen_help_view_content5));
			break;
/*		case ScreenHelp.SCREEN_HELP_VIEW6:
			title.setText(getString(R.string.screen_help_tv6));
			content.setText(getString(R.string.screen_help_view_content6));
			break;
		case ScreenHelp.SCREEN_HELP_VIEW7:
			title.setText(getString(R.string.screen_help_tv7));
			content.setText(getString(R.string.screen_help_view_content7));
			break;
		case ScreenHelp.SCREEN_HELP_VIEW8:
			title.setText(getString(R.string.screen_help_tv8));
			content.setText(getString(R.string.screen_help_view_content8));
			break;*/
		default:
			break;
		}
	}
}
