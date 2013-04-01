package com.android.media.screens.impl;

import java.io.File;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.amusic.media.R;
import com.android.media.MediaApplication;
import com.android.media.adapter.AudioRecentlySongAdapter;
import com.android.media.event.IMediaEventArgs;
import com.android.media.event.IMediaEventHandler;
import com.android.media.model.ScreenArgs;
import com.android.media.provider.MediaDatabaseHelper;
import com.android.media.screens.AudioScreen;
import com.android.media.services.IMediaPlayerService;
import com.android.media.services.impl.ServiceManager;

public class ScreenAudioRecentlySongs extends AudioScreen implements OnItemClickListener, IMediaEventHandler {
	private AudioRecentlySongAdapter adapter;
	private Cursor mCursor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_audio_recently_songs);
		setScreenTitle(getString(R.string.screen_audio_recently));
		listView = (ListView) findViewById(R.id.screen_audio_recently_songs_listview);
		cursor = db.queryRecentlyAudios();
		listView.setEmptyView(findViewById(R.id.empty));
		startManagingCursor(cursor);
		adapter = new AudioRecentlySongAdapter(this, cursor, this);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
		mediaEventService.addEventHandler(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if(!new File((String) view.getTag(R.layout.screen_audio)).exists()){
			Toast.makeText(ScreenAudioRecentlySongs.this, ScreenAudioRecentlySongs.this.getString(R.string.screen_music_have_no_music), Toast.LENGTH_SHORT).show();
			db.deleteAudio((int)id);
			return;
		};
		mCursor = db.queryRecentlyAudios();
		mediaPlayerService.changeCorsor(mCursor, IMediaPlayerService.MEDIA_MODEL_LOCAL);
		ScreenArgs args = new ScreenArgs();
		args.putExtra("screenType", type);
		args.putExtra("id", (int) id);
		args.putExtra("position", position);
		args.putExtra("screenId", ScreenAudioRecentlySongs.class.getCanonicalName());
		
		ServiceManager.id = (int) id;
		ServiceManager.position = position;
		if (ServiceManager.methodName != null) {
		    ServiceManager.lastMethodName = ServiceManager.methodName;
		} else {
			SharedPreferences sharedata = ServiceManager.getAmtMedia().getSharedPreferences("lastsong", 0);
			ServiceManager.lastMethodName = sharedata.getString("methodName", null);
		}
		ServiceManager.methodName = "queryRecentlyAudios";
		ServiceManager.listId = null;
		
		amtScreenService.show(ScreenAudioPlayer.class, args, View.GONE);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		MediaApplication.logD(ScreenAudioRecentlySongs.class, "onNewIntent");
	}

	@Override
	protected void onResume() {
		super.onResume();
		MediaApplication.logD(ScreenAudioRecentlySongs.class, "onResume");
		setScreenTitle(getString(R.string.screen_audio_recently));
		refresh();
		listView.requestLayout();
	}

	@Override
	public boolean refresh() {
		cursor = db.queryRecentlyAudios();
		startManagingCursor(cursor);
		adapter.changeCursor(cursor);
		return true;
	}

	@Override
	public boolean onEvent(IMediaEventArgs args) {
		switch (args.getMediaUpdateEventTypes()) {
		case AUDIO_UPDATE_UI_HIGHTLIGHT:
			adapter.notifyDataSetChanged();
			break;
		case  AUDIO_UPDATE_AUDIO_SONGS:
			cursor = db.queryRecentlyAudiosById();	
			startManagingCursor(cursor);
			adapter.changeCursor(cursor);
			break;	
		}
		return false;
	}
	
	
}
