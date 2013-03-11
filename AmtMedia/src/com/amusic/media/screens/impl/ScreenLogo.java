package com.amusic.media.screens.impl;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.widget.TextView;

import com.amusic.media.R;
import com.amusic.media.screens.AmtScreen;
import com.amusic.media.services.impl.ServiceManager;
import com.amusic.media.view.CustomDialog;

public class ScreenLogo extends AmtScreen {
	private SharedPreferences preferences;
	private final long time = 2000;
	
	private boolean isHasSD(){
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}
	private Runnable logo = new Runnable() {

		@Override
		public void run() {
			try {
				Thread.sleep(time);

				
				handler.post(new Runnable() {

					@Override
					public void run() {
						
						if(!isHasSD()){
							Dialog dialog;
							final CustomDialog.Builder customBuilder = new CustomDialog.Builder(ServiceManager.getAmtMedia());
							customBuilder.setTitle(ScreenLogo.this.getString(R.string.screen_check_sd_title))
							.setWhichViewVisible(CustomDialog.contentIsTextView)
							.setPositiveButton(ScreenLogo.this.getString(R.string.screen_check_sd_ok), 
					            		new DialogInterface.OnClickListener() {
					                public void onClick(DialogInterface dialog, int which) {
					                	ServiceManager.exit();
					                }
					            });
							
							dialog = customBuilder.create();
							TextView textView = customBuilder.getProgressTextView();
							textView.setText(ScreenLogo.this.getString(R.string.screen_check_sd_msg));
							textView.setTextColor(Color.WHITE);
							dialog.show();
							return ;
						}
						
						int number = preferences.getInt("am_stall_number", 0); // 0表示第一次安装
						if (number <= 0) {
//							amtScreenService.show(ScreenHelp.class, false, View.GONE);
//							preferences.edit().putInt("am_stall_number",1).commit();
							amtScreenService.show(ScreenHome.class);
						} else {
							amtScreenService.show(ScreenHome.class);
						}
						
					}
				});
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	};
	private final Handler handler;

	public ScreenLogo() {
		handler = new Handler();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_logo);
		preferences = getPreferences(MODE_WORLD_WRITEABLE);
		new Thread(logo).start();
	}

	@Override
	public boolean hasMenu() {
		// TODO Auto-generated method stub
		return true;
	}

}
