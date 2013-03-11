package com.amusic.media.screens.impl;

import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amusic.media.R;
import com.amusic.media.download.DownloadApk;
import com.amusic.media.screens.AmtScreen;
import com.amusic.media.services.impl.ServiceManager;

public class ScreenVersion extends AmtScreen {

	private Button btn_check_update;
	private TextView versionCodeTV;
	private LinearLayout mLoadingLayout, mMainLayout;
	Handler handler;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_version);
		versionCodeTV=(TextView)findViewById(R.id.screen_version_code);
		btn_check_update=(Button)findViewById(R.id.screen_version_check_update);
		btn_check_update.setOnClickListener(btn_check_update_Listener);
		mLoadingLayout = (LinearLayout)findViewById(R.id.fullscreen_loading_style);
		mMainLayout = (LinearLayout)findViewById(R.id.linearLayout01);
		handler = new Handler(){
	            @Override
	            public void handleMessage(Message msg) {
	                // TODO Auto-generated method stub
	                super.handleMessage(msg);
	                if (msg.what==1){
	                    mLoadingLayout.setVisibility(View.GONE);
	                    mMainLayout.setVisibility(View.VISIBLE);
	        			btn_check_update.setVisibility(View.VISIBLE);
	                }                
	            }
	            
	        };
	     String verName="1.0";
	     try {
			verName=getPackageManager().getPackageInfo("com.amusic.media", 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	     versionCodeTV.setText(getString(R.string.screen_version_title)+verName);
	}
	
	private View.OnClickListener btn_check_update_Listener=new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			mMainLayout.setVisibility(View.GONE);
			btn_check_update.setVisibility(View.GONE);
			mLoadingLayout.setVisibility(View.VISIBLE);
			new Thread(new Runnable() {
				@Override
				public void run() {
					Looper.prepare();
					DownloadApk downloadApk = new DownloadApk(ScreenVersion.this, Looper.myLooper());
					downloadApk.updateApk(DownloadApk.MANUAL_UPDATE, handler);
					Looper.loop();
				}
			}).start();
		}
	};
	
	protected void onResume() {
		super.onResume();
		ServiceManager.getAmtMedia().getGoPlayerBtn().setVisibility(View.INVISIBLE);
		setScreenTitle(getString(R.string.screen_version_top_title));
	};
	
	@Override
	public boolean hasMenu() {
		// TODO Auto-generated method stub
		return true;
	}
	
}
