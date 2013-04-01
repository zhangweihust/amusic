package com.android.media.screens.impl;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.amusic.media.R;
import com.android.media.model.ScreenArgs;
import com.android.media.screens.AmtScreen;
import com.android.media.services.impl.ServiceManager;
import com.android.media.utils.ColorUtil;

public class ScreenHelp extends AmtScreen {
	private TextView tvVersionCode;
	private TextView tv1;
	private TextView tv2;
	private TextView tv3;
	private TextView tv4;
	private TextView tv5;
/*	private TextView tv6;
	private TextView tv7;
	private TextView tv8;*/
	private ScreenArgs args;
	public static final int SCREEN_HELP_VIEW1 = 1;
	public static final int SCREEN_HELP_VIEW2 = 2;
	public static final int SCREEN_HELP_VIEW3 = 3;
	public static final int SCREEN_HELP_VIEW4 = 4;
	public static final int SCREEN_HELP_VIEW5 = 5;
	public static final int SCREEN_HELP_VIEW6 = 6;
	public static final int SCREEN_HELP_VIEW7 = 7;
	public static final int SCREEN_HELP_VIEW8 = 8;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_help);
		tvVersionCode = (TextView) findViewById(R.id.screen_help_version_code);
		tv1 = (TextView) findViewById(R.id.screen_help_tv1);
		tv2 = (TextView) findViewById(R.id.screen_help_tv2);
		tv3 = (TextView) findViewById(R.id.screen_help_tv3);
		tv4 = (TextView) findViewById(R.id.screen_help_tv4);
		tv5 = (TextView) findViewById(R.id.screen_help_tv5);
/*		tv6 = (TextView) findViewById(R.id.screen_help_tv6);
		tv7 = (TextView) findViewById(R.id.screen_help_tv7);*/
	//	tv8 = (TextView) findViewById(R.id.screen_help_tv8);
		tv1.setOnClickListener(tv1Listener);
		tv2.setOnClickListener(tv2Listener);
		tv3.setOnClickListener(tv3Listener);
		tv4.setOnClickListener(tv4Listener);
		tv5.setOnClickListener(tv5Listener);
/*		tv6.setOnClickListener(tv6Listener);
		tv7.setOnClickListener(tv7Listener);*/
		//tv8.setOnClickListener(tv8Listener);
		String verName = "1.0";
		try {
			verName = getPackageManager().getPackageInfo(
					"com.amusic.media", 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		tvVersionCode.setText(getString(R.string.screen_help_title) + verName);
	}

	private View.OnClickListener tv1Listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			tv1.setTextColor(ColorUtil.HIGHLIGHT);
			args = new ScreenArgs();
			args.putExtra("screen_help_view", SCREEN_HELP_VIEW1);
			amtScreenService.show(ScreenHelpView.class, args);
		}
	};

	private View.OnClickListener tv2Listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			tv2.setTextColor(ColorUtil.HIGHLIGHT);
			args = new ScreenArgs();
			args.putExtra("screen_help_view", SCREEN_HELP_VIEW2);
			amtScreenService.show(ScreenHelpView.class, args);
		}
	};
	
	private View.OnClickListener tv3Listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			tv3.setTextColor(ColorUtil.HIGHLIGHT);
			args = new ScreenArgs();
			args.putExtra("screen_help_view", SCREEN_HELP_VIEW3);
			amtScreenService.show(ScreenHelpView.class, args);
		}
	};
	
	private View.OnClickListener tv4Listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			tv4.setTextColor(ColorUtil.HIGHLIGHT);
			args = new ScreenArgs();
			args.putExtra("screen_help_view", SCREEN_HELP_VIEW4);
			amtScreenService.show(ScreenHelpView.class, args);
		}
	};
	
	private View.OnClickListener tv5Listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			tv5.setTextColor(ColorUtil.HIGHLIGHT);
			args = new ScreenArgs();
			args.putExtra("screen_help_view", SCREEN_HELP_VIEW5);
			amtScreenService.show(ScreenHelpView.class, args);
		}
	};
/*	private View.OnClickListener tv6Listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			tv6.setTextColor(ColorUtil.HIGHLIGHT);
			args = new ScreenArgs();
			args.putExtra("screen_help_view", SCREEN_HELP_VIEW6);
			amtScreenService.show(ScreenHelpView.class, args);
		}
	};
	private View.OnClickListener tv7Listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			tv7.setTextColor(ColorUtil.HIGHLIGHT);
			args = new ScreenArgs();
			args.putExtra("screen_help_view", SCREEN_HELP_VIEW7);
			amtScreenService.show(ScreenHelpView.class, args);
		}
	};
	private View.OnClickListener tv8Listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			tv8.setTextColor(ColorUtil.HIGHLIGHT);
			args = new ScreenArgs();
			args.putExtra("screen_help_view", SCREEN_HELP_VIEW8);
			amtScreenService.show(ScreenHelpView.class, args);
		}
	};*/
	

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		setScreenTitle(getString(R.string.screen_home_menu_function));
		ServiceManager.getAmtMedia().getGoPlayerBtn().setVisibility(View.INVISIBLE);
	}

	@Override
	public boolean hasMenu() {
		// TODO Auto-generated method stub
		return true;
	}
}
