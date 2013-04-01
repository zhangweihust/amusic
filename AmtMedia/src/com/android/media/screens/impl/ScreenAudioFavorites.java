package com.android.media.screens.impl;

import java.io.File;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.amusic.media.R;
import com.android.media.adapter.AudioFavoriteAdapter;
import com.android.media.event.IMediaEventArgs;
import com.android.media.event.IMediaEventHandler;
import com.android.media.model.ScreenArgs;
import com.android.media.screens.AudioScreen;
import com.android.media.services.IMediaPlayerService;
import com.android.media.services.impl.ServiceManager;

public class ScreenAudioFavorites extends AudioScreen implements OnItemClickListener, IMediaEventHandler {
	private AudioFavoriteAdapter adapter;
	private Cursor mCursor;
	private IMediaPlayerService mediaPlayerService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_audio_favorites);
		setScreenTitle(getString(R.string.screen_audio_favorites));
		mediaPlayerService = ServiceManager.getMediaplayerService();
		listView = (ListView) findViewById(R.id.screen_audio_favorites_listview);
		listView.setEmptyView(findViewById(R.id.empty));
		cursor = db.queryFavoriteAudios();
		startManagingCursor(cursor);
		adapter = new AudioFavoriteAdapter(this, cursor, this);
		listView.setAdapter(adapter);
		if (savedInstanceState != null) {
			ScreenArgs args = (ScreenArgs) savedInstanceState.getSerializable("args");
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
			Toast.makeText(ScreenAudioFavorites.this, ScreenAudioFavorites.this.getString(R.string.screen_music_have_no_music), Toast.LENGTH_SHORT).show();
			db.deleteAudio((int)id);
			return;
		};
		mCursor = db.queryFavoriteAudios();
		mediaPlayerService.changeCorsor(mCursor,IMediaPlayerService.MEDIA_MODEL_LOCAL);
		ScreenArgs args = new ScreenArgs();
		args.putExtra("screenType", type);
		args.putExtra("id", (int) id);
		args.putExtra("position", position);
		args.putExtra("screenId", ScreenAudioFavorites.class.getCanonicalName());
		
		ServiceManager.id = (int) id;
		ServiceManager.position = position;
		if (ServiceManager.methodName != null) {
		    ServiceManager.lastMethodName = ServiceManager.methodName;
		} else {
			SharedPreferences sharedata = ServiceManager.getAmtMedia().getSharedPreferences("lastsong", 0);
			ServiceManager.lastMethodName = sharedata.getString("methodName", null);
		}
		ServiceManager.methodName = "queryFavoriteAudios";
		ServiceManager.listId = null;
		amtScreenService.show(ScreenAudioPlayer.class, args, View.GONE);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		setScreenTitle(getString(R.string.screen_audio_favorites));
		refresh();
		listView.requestLayout();
	}

	@Override
	public boolean refresh() {
		cursor = db.queryFavoriteAudios();
		startManagingCursor(cursor);
		adapter.changeCursor(cursor);
//		listView.setSelectionFromTop(0, 0);
		return true;
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
