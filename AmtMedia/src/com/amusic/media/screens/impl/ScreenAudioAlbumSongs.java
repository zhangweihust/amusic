package com.amusic.media.screens.impl;

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
import com.amusic.media.adapter.AudioSongListAdapter;
import com.amusic.media.event.IMediaEventArgs;
import com.amusic.media.event.IMediaEventHandler;
import com.amusic.media.model.ScreenArgs;
import com.amusic.media.screens.AudioScreen;
import com.amusic.media.services.IMediaPlayerService;
import com.amusic.media.services.impl.ServiceManager;

public class ScreenAudioAlbumSongs extends AudioScreen implements OnItemClickListener, IMediaEventHandler {
	private AudioSongListAdapter adapter;
	private Integer albumId;
	private Cursor mCursor;
	private String albunName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_audio_songlist);
		ScreenArgs args = (ScreenArgs) getIntent().getSerializableExtra("args");
		albumId = (Integer) args.getExtra("id");
		albunName = (String) args.getExtra("albunName");
		listView = (ListView) findViewById(R.id.screen_audio_songlist_listview);
		cursor = db.queryAlbumAudios(albunName);
		setScreenTitle(albunName);
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
			Toast.makeText(ScreenAudioAlbumSongs.this, ScreenAudioAlbumSongs.this.getString(R.string.screen_music_have_no_music), Toast.LENGTH_SHORT).show();
			db.deleteAudio((int)id);
			return;
		};
		mCursor = db.queryAlbumAudios(albunName);
		mediaPlayerService.changeCorsor(mCursor, IMediaPlayerService.MEDIA_MODEL_LOCAL);
		ScreenArgs args = new ScreenArgs();
		args.putExtra("screenType", type);
		args.putExtra("id", (int) id);
		args.putExtra("position", position);
		args.putExtra("screenId", ScreenAudioAlbumSongs.class.getCanonicalName());
		
		ServiceManager.id = (int) id;
		ServiceManager.position = position;
		if (ServiceManager.methodName != null) {
		    ServiceManager.lastMethodName = ServiceManager.methodName;
		} else {
			SharedPreferences sharedata = ServiceManager.getAmtMedia().getSharedPreferences("lastsong", 0);
			ServiceManager.lastMethodName = sharedata.getString("methodName", null);
		}
		ServiceManager.methodName = "queryAlbumAudios";
		if (ServiceManager.listId != null) {
		    ServiceManager.lastListId = ServiceManager.listId;
		} else {
			SharedPreferences sharedata = ServiceManager.getAmtMedia().getSharedPreferences("lastsong", 0);
			ServiceManager.lastListId = sharedata.getString("listId", null);
		}
		ServiceManager.listId = albunName;
		amtScreenService.show(ScreenAudioPlayer.class, args, View.GONE);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		ScreenArgs args = (ScreenArgs) intent.getSerializableExtra("args");
		albumId = (Integer) args.getExtra("id");
		albunName = (String) args.getExtra("albunName");
	}

	@Override
	protected void onResume() {
		super.onResume();
		setScreenTitle(albunName);
		refresh();
		listView.requestLayout();
	}
	
	@Override
	public boolean refresh() {
		setScreenTitle(albunName);
		if(albunName != null){
			cursor = db.queryAlbumAudios(albunName);
			startManagingCursor(cursor);
			adapter.changeCursor(cursor);
		}
//		listView.setSelectionFromTop(0, 0);
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
