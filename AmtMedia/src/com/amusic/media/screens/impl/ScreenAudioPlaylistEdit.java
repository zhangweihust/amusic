package com.amusic.media.screens.impl;

import java.util.HashSet;
import java.util.Set;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.amusic.media.R;
import com.amusic.media.adapter.AudioPlaylistEditAdapter;
import com.amusic.media.dialog.OnScreenHint;
import com.amusic.media.model.ScreenArgs;
import com.amusic.media.provider.MediaDatabaseHelper;
import com.amusic.media.screens.AudioScreen;
import com.amusic.media.services.impl.ServiceManager;

public class ScreenAudioPlaylistEdit extends AudioScreen implements OnClickListener {

	private ListView listView;
	private Button ok;
	private Button selectAll;
	private Button cancelAll;
	private int playlistId = -1;
	private Set<Integer> set;
	private AudioPlaylistEditAdapter adapter;
	private static OnScreenHint mOnScreenHint;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_audio_playlist_edit);
		set = new HashSet<Integer>();
		ok = (Button) findViewById(R.id.screen_audio_playlist_edit_ok);
		selectAll = (Button) findViewById(R.id.screen_audio_playlist_edit_selectall);
		cancelAll = (Button) findViewById(R.id.screen_audio_playlist_edit_cancelall);
		ok.setOnClickListener(this);
		selectAll.setOnClickListener(this);
		cancelAll.setOnClickListener(this);
		ScreenArgs args = (ScreenArgs) getIntent().getSerializableExtra("args");
		playlistId = (Integer) args.getExtra("id");
		listView = (ListView) findViewById(R.id.screen_audio_playlist_edit_listview);
		listView.setEmptyView(findViewById(R.id.empty));
		cursor = db.queryAudios();
		startManagingCursor(cursor);
		adapter = new AudioPlaylistEditAdapter(this, cursor, set);
		listView.setAdapter(adapter);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.screen_audio_playlist_edit_ok:
			ContentValues values = new ContentValues();
			for (int id : set) {
				values.clear();
				values.put(MediaDatabaseHelper.COLUMN_AUDIO_PLAYLIST_ID, playlistId);
				values.put(MediaDatabaseHelper.COLUMN_AUDIO_SYSTEM_ID, id);
				db.addAudioToPlaylist(values);
			}
			audioScreenService.goback();		
//			mOnScreenHint=OnScreenHint.makeText(ServiceManager.getAmtMedia(),getString(R.string.screen_audio_playlists_add_success));
//			mOnScreenHint.show();
			ServiceManager.getAmtMediaHandler().post(new Runnable() {
				@Override
				public void run() {
					if(mOnScreenHint!=null){
					    mOnScreenHint.cancel();
					}
					mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_audio_playlists_add_success));
					mOnScreenHint.show();
				}
			});
			break;
		case R.id.screen_audio_playlist_edit_selectall:
			adapter.selectAll();
			selectAll.setVisibility(View.GONE);
			cancelAll.setVisibility(View.VISIBLE);
			break;
		case R.id.screen_audio_playlist_edit_cancelall:
			adapter.cancelAll();
			cancelAll.setVisibility(View.GONE);
			selectAll.setVisibility(View.VISIBLE);
			break;
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		adapter.cancelAll();
		cancelAll.setVisibility(View.GONE);
		selectAll.setVisibility(View.VISIBLE);
		cursor = db.queryAudios();
		startManagingCursor(cursor);
		adapter.changeCursor(cursor);
		ScreenArgs args = (ScreenArgs) intent.getSerializableExtra("args");
		playlistId = (Integer) args.getExtra("id");
//		listView.setSelectionFromTop(0, 0);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		listView.setSelection(0);
	}
}
