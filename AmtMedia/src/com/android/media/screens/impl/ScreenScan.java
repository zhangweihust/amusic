package com.android.media.screens.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.amusic.media.R;
import com.android.media.MediaApplication;
import com.android.media.adapter.AudioScanDirAdapter;
import com.android.media.screens.AmtScreen;
import com.android.media.services.impl.ServiceManager;
import com.android.media.task.ScreenScanTask;

public class ScreenScan extends AmtScreen implements OnClickListener{
	public final static String duration_filter_time = "10";

	private LinearLayout allselectview;
	private ImageView selectimag;
	private View headview;
	private ListView dirscan;
	private Button ignorefile;
	private Button beginscan;
	private Button adddir;
	private TextView ignoretip;
	private AudioScanDirAdapter adapter;
	
	private List<String> filepathlist;
	private boolean[] filepathselected;
	private boolean ignoresmallfile;
	private boolean allselectflag = true;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		ignoresmallfile = true;
		
		setContentView(R.layout.screen_audio_scan);
		setScreenTitle(getString(R.string.app_name));
		dirscan = (ListView)findViewById(R.id.screen_scan_dir_scan);
		ignorefile = (Button)findViewById(R.id.screen_scan_ignore_file);
		beginscan = (Button)findViewById(R.id.screen_scan_begin_scan);
		adddir = (Button)findViewById(R.id.screen_scan_adddir);
		ignoretip = (TextView)findViewById(R.id.screen_scan_ignore_tip);
		setScreenTitle(ScreenScan.this.getResources().getString(R.string.screen_scan_title));
		beginscan.setText(R.string.screen_scan_begin_scan);
		adddir.setText(R.string.screen_scan_add_dir);
		
		headview = getLayoutInflater().inflate(R.layout.screen_scan_cancel_selected, null);
		allselectview = (LinearLayout)headview.findViewById(R.id.scan_all_select);
		selectimag = (ImageView)headview.findViewById(R.id.screen_scan_all_select_image);
		dirscan.addHeaderView(headview);
		
		String s = String.format(this.getResources().getString(R.string.screen_scan_ignore_tip), duration_filter_time);
		ignoretip.setText(s);
		
		adapter = new AudioScanDirAdapter(this);
		
		ignorefile.setOnClickListener(this);
		beginscan.setOnClickListener(this);
		adddir.setOnClickListener(this);
		ignoretip.setOnClickListener(this);
		allselectview.setOnClickListener(this);
		
		
		allselectview.setOnTouchListener(new OnTouchListener()
		{

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				switch(event.getAction())
				{
				case MotionEvent.ACTION_DOWN:
					allselectview.setBackgroundResource(R.drawable.screen_list_item_press);
					break;
				case MotionEvent.ACTION_UP:
					allselectflag = !allselectflag;
					adapter.setAllSelected(allselectflag);
					if (allselectflag)
					{
						selectimag.setBackgroundResource(R.drawable.screen_audio_playlist_edit_checked_true);
					}
					else
					{
						selectimag.setBackgroundResource(R.drawable.screen_audio_playlist_edit_checked_false);
					}
					allselectview.setBackgroundResource(R.drawable.screen_list_item);
					break;
				case MotionEvent.ACTION_CANCEL:
					allselectview.setBackgroundResource(R.drawable.screen_list_item);
					break;
				}
				
				return false;
			}
			
		});

		
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		ArrayList<String> adddirlist;
		
		super.onResume();
		setScreenTitle(ScreenScan.this.getResources().getString(R.string.screen_scan_title));
		ServiceManager.getAmtMedia().getGoPlayerBtn().setVisibility(View.INVISIBLE);
		filepathlist = mediaService.getRefreshPath();
		adddirlist = MediaApplication.getInstance().getScanAddDir();
		
		if (adddirlist != null)
		{
			Iterator<String> it = adddirlist.iterator();
			String addpath;
			while (it.hasNext())
			{
				addpath = it.next();
				if (!filepathlist.contains(addpath))
				{
					filepathlist.add(addpath);
				}
			}
		}
		adapter.setFilepathlist(filepathlist);
		dirscan.setAdapter(adapter);
		
		ignoresmallfile = true;
		allselectflag = true;
		ignorefile.setBackgroundResource(R.drawable.screen_audio_playlist_edit_checked_true);
		selectimag.setBackgroundResource(R.drawable.screen_audio_playlist_edit_checked_true);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		switch(v.getId())
		{
		case R.id.screen_scan_ignore_file:
		case R.id.screen_scan_ignore_tip:
			ignoresmallfile = !ignoresmallfile;
			if (ignoresmallfile)
			{
				ignorefile.setBackgroundResource(R.drawable.screen_audio_playlist_edit_checked_true);
			}
			else
			{
				ignorefile.setBackgroundResource(R.drawable.screen_audio_playlist_edit_checked_false);
			}
			break;
		case R.id.screen_scan_begin_scan:
			beginscan();
	        break;
		case R.id.screen_scan_adddir:
			MediaApplication.getInstance().setClearFlag(true);
			ServiceManager.getAmtScreenService().show(ScreenScanAddDir.class);
			break;
		}
	}
	
	@Override
	public boolean hasMenu() {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	public void beginscan()
	{
		final List<String> fileselectlist = new ArrayList<String>();
		
    	filepathselected = adapter.getFilepathSelected();
    	if (filepathselected != null)
    	{
    		for (int i = 0; i < filepathselected.length; i++)
    		{
    			if (filepathselected[i])
    			{
    				fileselectlist.add(filepathlist.get(i));
    			}
    		}
    	}
    	ScreenScanTask scantask = new ScreenScanTask(ServiceManager.getAmtMedia(),fileselectlist,ignoresmallfile);
    	scantask.execute();
	}
}
