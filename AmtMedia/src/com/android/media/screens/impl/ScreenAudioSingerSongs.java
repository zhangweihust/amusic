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
import com.android.media.adapter.AudioSongListAdapter;
import com.android.media.event.IMediaEventArgs;
import com.android.media.event.IMediaEventHandler;
import com.android.media.model.ScreenArgs;
import com.android.media.screens.AudioScreen;
import com.android.media.services.IMediaPlayerService;
import com.android.media.services.impl.ServiceManager;

public class ScreenAudioSingerSongs extends AudioScreen implements OnItemClickListener, IMediaEventHandler {
	private AudioSongListAdapter adapter;
	private Integer singerId;
	private Cursor mCursor;
	private String singerName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_audio_songlist);
		ScreenArgs args = (ScreenArgs) getIntent().getSerializableExtra("args");
		singerId = (Integer) args.getExtra("id");
		singerName = (String) args.getExtra("singerName");
		listView = (ListView) findViewById(R.id.screen_audio_songlist_listview);
		listView.setEmptyView(findViewById(R.id.empty));
		cursor = db.querySingerAudios(singerName);
		cursor.moveToFirst();
		setScreenTitle(singerName);
		startManagingCursor(cursor);
		adapter = new AudioSongListAdapter(this, cursor, this);
		listView.setAdapter(adapter);
		if (savedInstanceState != null) {
			args = (ScreenArgs) savedInstanceState.getSerializable("args");
			savePosition = (Integer) args.getExtra("savePosition");
			listView.setSelection(savePosition);
		}
		listView.setOnScrollListener(this);
		listView.setOnItemClickListener(this);
		mediaEventService.addEventHandler(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if(!new File((String) view.getTag(R.layout.screen_audio)).exists()){
			Toast.makeText(ScreenAudioSingerSongs.this, ScreenAudioSingerSongs.this.getString(R.string.screen_music_have_no_music), Toast.LENGTH_SHORT).show();
//			db.deleteAudio((int)id);
			return;
		};
		mCursor = db.querySingerAudios(singerName);
		mediaPlayerService.changeCorsor(mCursor, IMediaPlayerService.MEDIA_MODEL_LOCAL);
		ScreenArgs args = new ScreenArgs();
		args.putExtra("screenType", type);
		args.putExtra("id", (int) id);
		args.putExtra("position", position);
		args.putExtra("screenId", ScreenAudioSingerSongs.class.getCanonicalName());
		
		ServiceManager.id = (int) id;
		ServiceManager.position = position;
		if (ServiceManager.methodName != null) {
		    ServiceManager.lastMethodName = ServiceManager.methodName;
		} else {
			SharedPreferences sharedata = ServiceManager.getAmtMedia().getSharedPreferences("lastsong", 0);
			ServiceManager.lastMethodName = sharedata.getString("methodName", null);
		}
		ServiceManager.methodName = "querySingerAudios";
		if (ServiceManager.listId != null) {
		    ServiceManager.lastListId = ServiceManager.listId;
		} else {
			SharedPreferences sharedata = ServiceManager.getAmtMedia().getSharedPreferences("lastsong", 0);
			ServiceManager.lastListId = sharedata.getString("listId", null);
		}
		ServiceManager.listId =  singerName;
		amtScreenService.show(ScreenAudioPlayer.class, args, View.GONE);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		ScreenArgs args = (ScreenArgs) intent.getSerializableExtra("args");
		singerId = (Integer) args.getExtra("id");
		singerName = (String) args.getExtra("singerName");
	}

	@Override
	protected void onResume() {
		super.onResume();
		setScreenTitle(singerName);
		refresh();
		listView.requestLayout();
	}

	@Override
	public boolean refresh() {
		if(singerName != null){
		cursor = db.querySingerAudios(singerName);
		startManagingCursor(cursor);
		adapter.changeCursor(cursor);
		}
		return true;
	}
	
	@Override
	public boolean onEvent(IMediaEventArgs args) {
		switch (args.getMediaUpdateEventTypes()) {
		case AUDIO_UPDATE_UI_HIGHTLIGHT:
			adapter.notifyDataSetChanged();
			break;
		case  AUDIO_UPDATE_AUDIO_SONGS:
			     cursor.requery();
			     startManagingCursor(cursor);
			break;
		}
		return false;
	}
}
