package com.amusic.media.screens.impl;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.amusic.media.R;
import com.amusic.media.adapter.AudioAlbumAdapter;
import com.amusic.media.event.IMediaEventArgs;
import com.amusic.media.event.IMediaEventHandler;
import com.amusic.media.model.ScreenArgs;
import com.amusic.media.provider.MediaDatabaseHelper;
import com.amusic.media.screens.AudioScreen;

public class ScreenAudioAlbums extends AudioScreen implements OnItemClickListener , IMediaEventHandler {

	private Cursor cursor;
	private AudioAlbumAdapter adapter;
	private String albunName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_audio_albums);
		setScreenTitle(getString(R.string.screen_audio_albums));
		list = (ListView) findViewById(R.id.screen_audio_albums_listview);
		list.setEmptyView(findViewById(R.id.empty));
		list.setOnItemClickListener(this);
		cursor = db.queryAlbums();
		startManagingCursor(cursor);
		adapter = new AudioAlbumAdapter(this, cursor, this);
		list.setAdapter(adapter);
		if (savedInstanceState != null) {
			ScreenArgs args = (ScreenArgs) savedInstanceState.getSerializable("args");
			savePosition = (Integer) args.getExtra("savePosition");
			list.setSelection(savePosition);
		}
		list.setOnScrollListener(this);
		mediaEventService.addEventHandler(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		albunName = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ALBUMNAME));
		ScreenArgs args = new ScreenArgs();
		args.putExtra("id", (int) id);
		args.putExtra("albunName", albunName);
		audioScreenService.show(ScreenAudioAlbumSongs.class, args);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		setScreenTitle(getString(R.string.screen_audio_albums));
		refresh();
		list.requestLayout();
	}
	
	@Override
	public boolean refresh() {
		cursor = db.queryAlbums();
		startManagingCursor(cursor);
		adapter.changeCursor(cursor);
		return true;
	}

	@Override
	public boolean onEvent(IMediaEventArgs args) {
		// TODO Auto-generated method stub
		switch (args.getMediaUpdateEventTypes()) {
		case  AUDIO_UPDATE_AUDIO_SONGS:		
			      cursor.requery();
			      startManagingCursor(cursor);
			break;
		}
		return false;
	}
}
