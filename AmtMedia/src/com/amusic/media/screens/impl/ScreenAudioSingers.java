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
import com.amusic.media.adapter.AudioSingerAdapter;
import com.amusic.media.event.IMediaEventArgs;
import com.amusic.media.event.IMediaEventHandler;
import com.amusic.media.model.ScreenArgs;
import com.amusic.media.provider.MediaDatabaseHelper;
import com.amusic.media.screens.AudioScreen;

public class ScreenAudioSingers extends AudioScreen implements OnItemClickListener, IMediaEventHandler {

	private AudioSingerAdapter adapter;
	private Cursor cursor;
	private ListView listView;
	private String singerName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_audio_singers);
		setScreenTitle(getString(R.string.screen_audio_singers));
		listView =  (ListView) findViewById(R.id.screen_audio_singers_listview);
		listView.setOnItemClickListener(this);
		listView.setEmptyView(findViewById(R.id.empty));
		cursor = db.querySingers();
		startManagingCursor(cursor);
		adapter = new AudioSingerAdapter(this, cursor, this, listView);
		listView.setAdapter(adapter);
		if (savedInstanceState != null) {
			ScreenArgs args = (ScreenArgs) savedInstanceState.getSerializable("args");
			savePosition = (Integer) args.getExtra("savePosition");
			listView.setSelection(savePosition);
		}
		listView.setOnScrollListener(this);
		mediaEventService.addEventHandler(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		cursor.moveToPosition(position);
		singerName = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ARTISTNAME));
		ScreenArgs args = new ScreenArgs();
		args.putExtra("id", (int) id);
		args.putExtra("singerName", singerName);
		audioScreenService.show(ScreenAudioSingerSongs.class, args);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}
		@Override
	protected void onResume() {
		super.onResume();
		setScreenTitle(getString(R.string.screen_audio_singers));
		refresh();
		listView.requestLayout();
	}
	@Override
	public boolean refresh() {
		setScreenTitle(getString(R.string.screen_audio_singers));
		cursor = db.querySingers();
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
