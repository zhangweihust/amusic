package com.amusic.media.screens.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.amusic.media.MediaApplication;
import com.amusic.media.R;
import com.amusic.media.adapter.AudioDownloadExpanableAdapter;
import com.amusic.media.event.IMediaEventArgs;
import com.amusic.media.event.IMediaEventHandler;
import com.amusic.media.event.impl.MediaEventArgs;
import com.amusic.media.event.impl.MediaEventTypes;
import com.amusic.media.handler.MediaHandler;
import com.amusic.media.screens.AmtScreen;
import com.amusic.media.services.IMediaService;
import com.amusic.media.services.impl.ServiceManager;
import com.amusic.media.thread.MediaThread;
import com.amusic.media.utils.IAsynMediaQuery;
import com.amusic.media.view.CustomDialog;

public class ScreenAudioDownloadManager extends AmtScreen implements IMediaEventHandler, IAsynMediaQuery {

	private final int GROUP_ID_DOWNLOAD_UNFINISHED = 0;
	private final int GROUP_ID_DOWNLOAD_FINISHED = 1;

	List<String> group; // 组列表
	List<List<String>> child; // 子列表
	private ExpandableListView expandableListView;
	private AudioDownloadExpanableAdapter expanableAdapter;
	TextView startPaushView;
	TextView restarView;
	TextView deteleView;
	Button button;
	Dialog dialog = null;
	private MediaEventArgs args;
	private boolean refreshFlag = false;
	private final MediaHandler mediaHandler;

     public ScreenAudioDownloadManager() {
 		mediaHandler = new MediaHandler(this);
 	}
 	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_audio_download);
		setScreenTitle(getString(R.string.screen_home_menu_download_manager));
		Cursor unfinishedcursor=db.queryDownloadingAudios();
		startManagingCursor(unfinishedcursor);
		Cursor finishedcursor = db.queryDownloadOkAudios();
		startManagingCursor(finishedcursor);
		cursorMap.put(GROUP_ID_DOWNLOAD_UNFINISHED, unfinishedcursor);
		cursorMap.put(GROUP_ID_DOWNLOAD_FINISHED, finishedcursor);
		expanableAdapter = new AudioDownloadExpanableAdapter(this, cursorMap, this);
		expandableListView = (ExpandableListView) findViewById(R.id.list);
		expandableListView.setAdapter(expanableAdapter);
		expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				
				args = (MediaEventArgs) v.getTag();
				int status = (Integer) args.getExtra("status");
				String songName = (String) args.getExtra("song");
				CustomDialog.Builder customBuilder = new CustomDialog.Builder(ScreenAudioDownloadManager.this);
				customBuilder.setTitle(songName).setWhichViewVisible(CustomDialog.contentIsListView).setListViewData(getData(status))
						.setLayoutID(CustomDialog.LISTVIEW_ITEM_TEXTVIEW).setOnItemClickListener(itemClickListener);
				if (dialog == null) {
					dialog = customBuilder.create();
					dialog.show();
				} else {
					if (!dialog.isShowing()) {
						dialog = customBuilder.create();
						dialog.show();
					}
				}
				return false;
			}
		});
		mediaEventService.addEventHandler(this);
	}

	private List<Map<String, Object>> getData(int status) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		switch (status) {
		case IMediaService.STATE_PAUSE:
		case IMediaService.STATE_IN_QUEUE:
			map.put("title", getString(R.string.screen_audio_download_continue));
			map.put("caseId", CustomDialog.RESUME_DOWNLOAD);
			list.add(map);
			break;
		case IMediaService.STATE_BEGIN:
			map.put("title", getString(R.string.screen_audio_download_pause));
			map.put("caseId", CustomDialog.PAUSE_DOWNLOAD);
			list.add(map);
			break;
		}
		if (status != IMediaService.STATE_FINISHED) {
			map = new HashMap<String, Object>();
			map.put("title", getString(R.string.screen_audio_download_cancel));
			map.put("caseId", CustomDialog.CANCEL_DOWNLOAD);
			list.add(map);
		} else if (status == IMediaService.STATE_FINISHED){
			map = new HashMap<String, Object>();
			map.put("title", getString(R.string.screen_audio_download_delete));
			map.put("caseId", CustomDialog.DELETE_DOWNLOAD);
			list.add(map);
		}
		return list;
	}

	private OnItemClickListener itemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
			CustomDialog.Builder.ViewHolder holer = (CustomDialog.Builder.ViewHolder) view.getTag();
			switch (holer.caseId) {
			case CustomDialog.RESUME_DOWNLOAD:
				new Thread(new Runnable(){
					@Override
					public void run() {
						mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_RESUME));
					}}).start();
				break;
			case CustomDialog.PAUSE_DOWNLOAD:
				new Thread(new Runnable(){
					@Override
					public void run() {
						mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_PAUSE));
					}}).start();
				break;
			case CustomDialog.CANCEL_DOWNLOAD:
				new Thread(new Runnable(){
					@Override
					public void run() {
						mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_CANCEL));
					}}).start();
				break;
			case CustomDialog.DELETE_DOWNLOAD:
				new Thread(new Runnable(){
					@Override
					public void run() {
						mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_DELETE));
					}}).start();
				break;
			}
			expanableAdapter.notifyDataSetChanged();
			if(dialog !=null ){
				dialog.dismiss();
				dialog = null;
			}
		}
	};

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setScreenTitle(getString(R.string.screen_home_menu_download_manager));
		new MediaThread(mediaHandler, MediaQueryToken.QUERY_INIT).start();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		refreshFlag = true;
		setScreenTitle(getString(R.string.screen_home_menu_download_manager));
		ServiceManager.getAmtMedia().getGoPlayerBtn().setVisibility(View.INVISIBLE);
	}
	
	@Override
	protected void onPause() {
		refreshFlag = false;
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onEvent(IMediaEventArgs args) {
		switch (args.getMediaUpdateEventTypes()) {
		case AUDIO_DOWNLOAD_CANCEL_UI:
		case AUDIO_DOWNLOAD_PAUSE_UI:
		case AUDIO_DOWNLOAD_FINISH_UI:
		case AUDIO_DOWNLOAD_BEGIN_UI:
		case AUDIO_DOWNLOAD_REBEGIN_UI:
		case AUDIO_DOWNLOAD_DELETE_UI:
		case AUDIO_RE_DOWNLOAD_UI:
		case AUDIO_DOWNLOAD_RESUME_UI:
		case AUDIO_DOWNLOAD_UI:
			synchronized(this){
				 new MediaThread(mediaHandler, MediaQueryToken.QUERY_INIT).start();
			}
			break;
		}
		return true;
	}

	

	@Override
	public void queryData(MediaQueryToken token) {
		MediaApplication.logD(ScreenAudioDownloadManager.class, "QUERY_DATA");
		clearDownloadHandlers();
		switch (token) {
		case QUERY_INIT:
			Cursor unfinishedcursor = db.queryDownloadingAudios();
			startManagingCursor(unfinishedcursor);
			Cursor finishedcursor = db.queryDownloadOkAudios();
			startManagingCursor(finishedcursor);
			cursorMap.put(GROUP_ID_DOWNLOAD_UNFINISHED, unfinishedcursor);
			cursorMap.put(GROUP_ID_DOWNLOAD_FINISHED, finishedcursor);
			break;
		}
	}

	@Override
	public void updateData(MediaQueryToken token) {
		switch (token) {
		case QUERY_INIT:
			expanableAdapter.setchildCursors(cursorMap);
			expanableAdapter.initializeData(ScreenAudioDownloadManager.this);
			expanableAdapter.notifyDataSetChanged();
			break;
		}
	}

	@Override
	public boolean refresh() {
		if (refreshFlag == true){
			new MediaThread(mediaHandler, MediaQueryToken.QUERY_INIT).start();
		}
		return true;
	}
	
	@Override
	public boolean hasMenu() {
		// TODO Auto-generated method stub
		return true;
	}
	
}
