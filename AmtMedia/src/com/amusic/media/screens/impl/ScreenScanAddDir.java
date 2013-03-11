package com.amusic.media.screens.impl;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.amusic.media.MediaApplication;
import com.amusic.media.R;
import com.amusic.media.adapter.AudioScanAddDirAdapter;
import com.amusic.media.screens.AmtScreen;
import com.amusic.media.services.impl.ServiceManager;

public class ScreenScanAddDir extends AmtScreen implements OnClickListener{

	private AudioScanAddDirAdapter adddiradapter;
	private ListView filelistview;
	private Button add;
	private Button goback;
	private LinearLayout Lineartip;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_audio_scan);
		filelistview = (ListView)findViewById(R.id.screen_scan_dir_scan);
		Lineartip = (LinearLayout)findViewById(R.id.linearLayout2);
		Lineartip.setVisibility(View.INVISIBLE);
		add = (Button)findViewById(R.id.screen_scan_begin_scan);
		goback = (Button)findViewById(R.id.screen_scan_adddir);
		
		add.setText(R.string.screen_scan_adddir_add_str);
		goback.setText(R.string.screen_scan_adddir_goback_str);
		
		adddiradapter = new AudioScanAddDirAdapter(this,filelistview);
		
		add.setOnClickListener(this);
		goback.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		setScreenTitle(ScreenScanAddDir.this.getResources().getString(R.string.screen_scan_add_scan_dir));
		adddiradapter.refresh();
		filelistview.setAdapter(adddiradapter);
		ServiceManager.getAmtMedia().getGoPlayerBtn().setVisibility(View.INVISIBLE);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		ArrayList<String> adddirlist;
		
		switch(v.getId())
		{
		case R.id.screen_scan_begin_scan:
			adddirlist = adddiradapter.getAddDir();
			MediaApplication.getInstance().addScanAddDir(adddirlist);
			ServiceManager.getAmtMedia().onBackPressed();
			break;
			
		case R.id.screen_scan_adddir:
			ServiceManager.getAmtMedia().onBackPressed();
			break;
			
		default:
			break;
		}
	}
	
	@Override
	public boolean hasMenu() {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		this.finish();
	}

}
