package com.android.media.screens.impl;

import java.io.File;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.amusic.media.R;
import com.android.media.adapter.AudioSongAdapter;
import com.android.media.event.IMediaEventArgs;
import com.android.media.event.IMediaEventHandler;
import com.android.media.model.ScreenArgs;
import com.android.media.screens.AudioScreen;
import com.android.media.services.IMediaPlayerService;
import com.android.media.services.impl.ServiceManager;

public class ScreenAudioSongs extends AudioScreen implements
		OnItemClickListener, IMediaEventHandler {
	private AudioSongAdapter adapter;
	private Cursor mCursor;
//	private  CursorContentObserver mObserver;
//	private  MyContentObserver mObserver;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setScreenTitle(getString(R.string.screen_audio_songs));
		setContentView(R.layout.screen_audio_songs);
		listView = (ListView) findViewById(R.id.screen_audio_songs_listview);
		listView.setEmptyView(findViewById(R.id.empty));
		cursor = db.queryAudios();
		startManagingCursor(cursor);
////		mObserver= new CursorContentObserver(cursor);
////		cursor.registerContentObserver(mObserver);
//		mObserver= new MyContentObserver(new Handler());
//		cursor.registerContentObserver(mObserver);
//		Log.i("observer", "----------------cursor registercontentobserver");
		adapter = new AudioSongAdapter(this, cursor, this);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
		mediaEventService.addEventHandler(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if(!new File((String) view.getTag(R.layout.screen_audio)).exists()){
			Toast.makeText(ScreenAudioSongs.this, ScreenAudioSongs.this.getString(R.string.screen_music_have_no_music), Toast.LENGTH_SHORT).show();
//			db.deleteAudio((int)id);
			return;
		};
		mCursor = db.queryAudios();
		mediaPlayerService.changeCorsor(mCursor, IMediaPlayerService.MEDIA_MODEL_LOCAL);
		ScreenArgs args = new ScreenArgs();
		args.putExtra("screenType", type);
		args.putExtra("id", (int) id);
		args.putExtra("position", position);
		args.putExtra("screenId", ScreenAudioSongs.class.getCanonicalName());
		ServiceManager.id = (int) id;
		ServiceManager.position = position;
		if (ServiceManager.methodName != null) {
		    ServiceManager.lastMethodName = ServiceManager.methodName;
		} else {
			SharedPreferences sharedata = ServiceManager.getAmtMedia().getSharedPreferences("lastsong", 0);
			ServiceManager.lastMethodName = sharedata.getString("methodName", null);
		}
		ServiceManager.methodName = "queryAudios";
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
		setScreenTitle(getString(R.string.screen_audio_songs));
		refresh();
		listView.requestLayout();
	}

	@Override
	public boolean refresh() {
		setScreenTitle(getString(R.string.screen_audio_songs));
		cursor = db.queryAudios();
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
		case  AUDIO_UPDATE_AUDIO_SONGS:
			 	cursor.requery();
			    startManagingCursor(cursor);
			break;
		}
		return false;
	}

//   private class MyContentObserver extends ContentObserver{
////    private Cursor cursor;
//
//
//	public MyContentObserver(Handler handler) {
//		super(handler);
//		// TODO Auto-generated constructor stub
//	}
//
//	@Override
//	public void onChange(boolean selfChange) {
//		// TODO Auto-generated method stub
//		super.onChange(selfChange);
//		Log.i("observer", "----------------onchange");
//		refreshData();
//	}
//	
//	
//   }
   
//   private void refreshData(){
//	   if(cursor.isClosed()){
//		   Log.i("observer", "----------------cursor iscolsed");
//		   return ;
//	    }
//	    cursor.requery();
////	    adapter.notifyDataSetChanged();
//	    Log.i("observer", "----------------cursor requery");
//   }
//   @Override
// 　　protected void onDestroy() {
// 　　super.onDestroy();
// 　　if (mCursor != null) {
// 　　mCursor.unregisterContentObserver(mObserver);
// 　　mCursor.close();
// 　　}
// 　　}

//@Override
//protected void onDestroy() {
//	// TODO Auto-generated method stub
//	super.onDestroy();
//	if(cursor !=null){
//		cursor.unregisterContentObserver(mObserver);
//		cursor.close();
//		}
//}
   
}
