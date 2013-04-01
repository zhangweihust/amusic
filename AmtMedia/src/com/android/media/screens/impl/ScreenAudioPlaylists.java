package com.android.media.screens.impl;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.amusic.media.R;
import com.android.media.adapter.AudioPlaylistAdapter;
import com.android.media.dialog.OnScreenHint;
import com.android.media.event.impl.MediaEventArgs;
import com.android.media.event.impl.MediaEventTypes;
import com.android.media.model.ScreenArgs;
import com.android.media.provider.MediaDatabaseHelper;
import com.android.media.screens.AudioScreen;
import com.android.media.services.impl.ServiceManager;
import com.android.media.view.CustomDialog;

public class ScreenAudioPlaylists extends AudioScreen implements OnItemClickListener, OnClickListener {
	private Button createPlaylist;
	private EditText createPlaylistName;
	private Dialog dialog;
	private AudioPlaylistAdapter adapter;
	private String playlistName;
	private static OnScreenHint mOnScreenHint;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_audio_playlists);
		setScreenTitle(getString(R.string.screen_audio_playlists));
		createPlaylist = (Button) findViewById(R.id.screen_audio_playlist_create);
		createPlaylist.setOnClickListener(this);
		listView = (ListView) findViewById(R.id.screen_audio_playlists_listview);
		listView.setEmptyView(findViewById(R.id.empty));
		cursor = db.queryPlaylists();
		startManagingCursor(cursor);
		adapter = new AudioPlaylistAdapter(this, cursor, this);
		listView.setAdapter(adapter);
		if (savedInstanceState != null) {
			ScreenArgs args = (ScreenArgs) savedInstanceState.getSerializable("args");
			savePosition = (Integer) args.getExtra("savePosition");
			listView.setSelection(savePosition);
		}
		listView.setOnScrollListener(this);
		listView.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		playlistName = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_PLAYLIST_NAME));
		ScreenArgs args = new ScreenArgs();
		args.putExtra("id", (int) id);
		args.putExtra("playlistName", playlistName);
		audioScreenService.show(ScreenAudioPlaylistSongs.class, args);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.screen_audio_playlist_create:
			final CustomDialog.Builder customBuilder = new CustomDialog.Builder(ServiceManager.getAmtMedia());
			customBuilder.setTitle(ScreenAudioPlaylists.this.getString(R.string.screen_create_new_playlist))
			.setWhichViewVisible(CustomDialog.contentIsEditText)
			.setPositiveButton(ScreenAudioPlaylists.this.getString(R.string.screen_create_new_playlist_add), 
	            		new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int which) {
	        			String name = createPlaylistName.getEditableText().toString().trim();
	        			if (name != null && name.length() > 0) {
	        				ContentValues values = new ContentValues();
	        				values.put(MediaDatabaseHelper.COLUMN_PLAYLIST_NAME, name);
	        				values.put(MediaDatabaseHelper.COLUMN_PLAYLIST_CREATE_TIME, System.currentTimeMillis());
	        				values.put(MediaDatabaseHelper.COLUMN_PLAYLIST_UPDATE_TIME, System.currentTimeMillis());
	        				boolean bool=db.addPlaylist(values);
	        				if (!bool) {
	        					ServiceManager.getAmtMediaHandler().post(new Runnable() {

	        						@Override
	        						public void run() {
	        							mOnScreenHint=OnScreenHint.makeText(ScreenAudioPlaylists.this,getString(R.string.screen_audio_playlists_exist));
	        							mOnScreenHint.show();
	        						}
	        					});
	        				}else{
	    	        			mediaEventService.onMediaUpdateEvent(new MediaEventArgs()
								.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_UPDATE_PLAYLIST_COUNT));
	        				}
	        			}
	        			createPlaylistName.setText("");
	        			dialog.dismiss();
	        			if (name != null && name.length() > 0) {
	        				refresh();
	        			}}
	            })
	            .setNegativeButton(ScreenAudioPlaylists.this.getString(R.string.screen_create_new_playlist_cancel), 
	            		new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int which) {
	                	createPlaylistName.setText("");
	                	dialog.dismiss();
	    			}
	            });
			dialog = customBuilder.create();
			createPlaylistName = customBuilder.getEditText();
			dialog.show();			
			break;
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setScreenTitle(getString(R.string.screen_audio_playlists));
		refresh();
	}

	@Override
	protected void onResume() {
		super.onResume();
		setScreenTitle(getString(R.string.screen_audio_playlists));
		listView.requestLayout();
	}

	@Override
	public boolean refresh() {
		cursor = db.queryPlaylists();
		startManagingCursor(cursor);
		adapter.changeCursor(cursor);
//		listView.setSelectionFromTop(0, 0);
		return true;
	}
}
