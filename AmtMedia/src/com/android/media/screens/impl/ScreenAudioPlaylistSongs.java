package com.android.media.screens.impl;

import java.io.File;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.amusic.media.R;
import com.android.media.adapter.AudioPlaylistSongAdapter;
import com.android.media.event.IMediaEventArgs;
import com.android.media.event.IMediaEventHandler;
import com.android.media.model.ScreenArgs;
import com.android.media.screens.AudioScreen;
import com.android.media.services.IMediaPlayerService;
import com.android.media.services.impl.ServiceManager;

public class ScreenAudioPlaylistSongs extends AudioScreen implements OnItemClickListener, OnClickListener, IMediaEventHandler {
	private Button addSongs;
	private AudioPlaylistSongAdapter adapter;
	private int playlistId = -1;
	private String playlistName;
	private Cursor mCursor;
	private String tmpPlaylistName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_audio_playlist_songs);
		addSongs = (Button) findViewById(R.id.screen_audio_songlist_add_songs);
		addSongs.setOnClickListener(this);
		ScreenArgs args = (ScreenArgs) getIntent().getSerializableExtra("args");
		playlistId = (Integer) args.getExtra("id");
		playlistName = (String) args.getExtra("playlistName");
		tmpPlaylistName=playlistName;
		listView = (ListView) findViewById(R.id.screen_audio_playlist_songs_listview);
		listView.setEmptyView(findViewById(R.id.empty));
		cursor = db.queryPlaylistAudios(playlistId);
		setScreenTitle(playlistName);
		startManagingCursor(cursor);
		adapter = new AudioPlaylistSongAdapter(this, cursor, this, playlistId);
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
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		ScreenArgs args = (ScreenArgs) intent.getSerializableExtra("args");
		playlistId = (Integer) args.getExtra("id");
		playlistName = (String) args.getExtra("playlistName");
		if(playlistName==null||playlistName==""){
			playlistName=tmpPlaylistName;
		}
		setScreenTitle(playlistName);
		refresh();
	}

	@Override
	protected void onResume() {
		super.onResume();
		setScreenTitle(playlistName);
		listView.requestLayout();
	}

	@Override
	public boolean refresh() {
		cursor = db.queryPlaylistAudios(playlistId);
		startManagingCursor(cursor);
		adapter.changeCursor(cursor, playlistId);
//		listView.setSelectionFromTop(0, 0);
		return true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.screen_audio_songlist_add_songs:
			ScreenArgs args = new ScreenArgs();
			args.putExtra("goBack", true);
			args.putExtra("id", playlistId);
			args.putExtra("playlistName", playlistName);
			tmpPlaylistName = playlistName;
			audioScreenService.show(ScreenAudioPlaylistEdit.class, args);
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if(!new File((String) view.getTag(R.layout.screen_audio)).exists()){
			Toast.makeText(ScreenAudioPlaylistSongs.this, ScreenAudioPlaylistSongs.this.getString(R.string.screen_music_have_no_music), Toast.LENGTH_SHORT).show();
			db.deleteAudio((int)id);
			return;
		};
		mCursor = db.queryPlaylistAudios(playlistId);
		mediaPlayerService.changeCorsor(mCursor, IMediaPlayerService.MEDIA_MODEL_LOCAL);
		ScreenArgs args = new ScreenArgs();
		args.putExtra("screenType", type);
		args.putExtra("id", (int) id);
		args.putExtra("position", position);
		args.putExtra("screenId", ScreenAudioPlaylistSongs.class.getCanonicalName());
		
		ServiceManager.id = (int) id;
		ServiceManager.position = position;
		if (ServiceManager.methodName != null) {
		    ServiceManager.lastMethodName = ServiceManager.methodName;
		} else {
			SharedPreferences sharedata = ServiceManager.getAmtMedia().getSharedPreferences("lastsong", 0);
			ServiceManager.lastMethodName = sharedata.getString("methodName", null);
		}
		ServiceManager.methodName = "queryPlaylistAudios";
		if (ServiceManager.listId != null) {
		    ServiceManager.lastListId = ServiceManager.listId;
		} else {
			SharedPreferences sharedata = ServiceManager.getAmtMedia().getSharedPreferences("lastsong", 0);
			ServiceManager.lastListId = sharedata.getString("listId", null);
		}
		ServiceManager.listId = String.valueOf(playlistId);
		
		amtScreenService.show(ScreenAudioPlayer.class, args, View.GONE);
	}
	
	@Override
	public boolean onEvent(IMediaEventArgs args) {
		switch (args.getMediaUpdateEventTypes()) {
		case AUDIO_UPDATE_UI_HIGHTLIGHT:
			adapter.notifyDataSetChanged();
			break;
		}
		return false;
	}
}
