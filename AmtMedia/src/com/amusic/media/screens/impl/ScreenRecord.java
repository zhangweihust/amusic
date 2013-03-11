package com.amusic.media.screens.impl;

import java.io.File;
import java.util.ArrayList;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.amusic.media.R;
import com.amusic.media.adapter.RecordSongsAdapter;
import com.amusic.media.dialog.OnScreenHint;
import com.amusic.media.event.IMediaEventArgs;
import com.amusic.media.event.IMediaEventHandler;
import com.amusic.media.ffmpeg.ExtAudioRecorder;
import com.amusic.media.model.ScreenArgs;
import com.amusic.media.model.SongInfo;
import com.amusic.media.screens.RecordScreen;
import com.amusic.media.services.IMediaPlayerService;
import com.amusic.media.services.impl.ServiceManager;
import com.amusic.media.task.ScreenRecordTask;
import com.amusic.media.utils.Constant;
import com.amusic.media.view.CustomDialog;

public class ScreenRecord extends RecordScreen implements OnItemClickListener,IMediaEventHandler{
	private RecordSongsAdapter adapter;
	private static int INIT = 0;
	private static int REFRESH = 1;
	private Handler handler;
	private LinearLayout empty;
	private ArrayList<SongInfo> recordSongs=null;
	public static OnScreenHint mOnScreenHint;
	public static boolean isFirst = true;
	private static ScreenRecord screenRecordInstance= null;
	private Dialog dialog;
	public static ScreenRecord getScreenRecordInstance(){
		return screenRecordInstance;
	}

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		super.onCreate(savedInstanceState);
		handler = ServiceManager.getAmtMediaHandler();
		mediaEventService.addEventHandler(this);
		setContentView(R.layout.screen_record);
		setScreenTitle(getString(R.string.screen_home_tab_record));
		listView = (ListView) findViewById(R.id.screen_record_songs_listview);
		empty = (LinearLayout) findViewById(R.id.empty);
		adapter = new RecordSongsAdapter(this, this, listView);
		loadData(INIT);
		screenRecordInstance = this;
	}
	public void loadData(int type){
		ScreenRecordTask task = new ScreenRecordTask(this,type,false);
		task.execute();
	}
	public void initView(ArrayList<SongInfo> recordSongs){
		this.recordSongs = recordSongs;
		if(recordSongs != null && recordSongs.size() > 0){
			empty.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
		}else{
			setEmpty();
		}
		adapter.setRecordSongs(recordSongs);
		listView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		listView.setOnItemClickListener(this);
	}
	public void setEmpty(){
		empty.setVisibility(View.VISIBLE);
		listView.setVisibility(View.GONE);
	}
	@Override
	protected void onNewIntent(Intent intent) {
		
		super.onNewIntent(intent);
		setScreenTitle(getString(R.string.screen_home_tab_record));
		loadData(REFRESH);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if(mOnScreenHint != null){
			mOnScreenHint.cancel(); 
		}
	}
	@Override
	protected void onResume() {
		super.onResume();
		setScreenTitle(getString(R.string.screen_home_tab_record));
		ServiceManager.getAmtMedia().getGoBackBtn().setVisibility(View.INVISIBLE);
//		listView.setSelectionFromTop(0, 0);
		if(isFirst){
			isFirst = false;
		}else if(!isFirst && recordSongs == null || (recordSongs != null && recordSongs.size()==0)){
//			if(mOnScreenHint != null){
//				mOnScreenHint.cancel(); 
//			}
//			mOnScreenHint = OnScreenHint.makeText_Empty(this, false, ServiceManager.getAmtMedia().getResources().getString(R.string.record_content_empty_info0), ServiceManager.getAmtMedia().getResources().getString(R.string.record_content_empty_info1), ServiceManager.getAmtMedia().getResources().getString(R.string.record_content_empty_info2),ServiceManager.getAmtMedia().getResources().getString(R.string.record_content_empty_info3));   //设置toast要显示的信息
//	        mOnScreenHint.getView().setBackgroundResource(R.drawable.content_empty_left);
//	        mOnScreenHint.setPosition(Gravity.RIGHT|Gravity.BOTTOM,this.getWindowManager().getDefaultDisplay().getWidth()/8*5 - OnScreenHint.dip2px(this, 142) , ServiceManager.getAmtMedia().getResources().getDimensionPixelSize(
//	                R.dimen.hint_y_offset));
//	        mOnScreenHint.show();
			ServiceManager.getAmtMediaHandler().post(new Runnable(){
				@Override
				public void run() {
					if(mOnScreenHint != null){
						mOnScreenHint.cancel(); 
					}
					mOnScreenHint = OnScreenHint.makeText_Empty(ServiceManager.getAmtMedia(), false, ServiceManager.getAmtMedia().getResources().getString(R.string.record_content_empty_info0), ServiceManager.getAmtMedia().getResources().getString(R.string.record_content_empty_info1), ServiceManager.getAmtMedia().getResources().getString(R.string.record_content_empty_info2),ServiceManager.getAmtMedia().getResources().getString(R.string.record_content_empty_info3));   //设置toast要显示的信息
			        mOnScreenHint.getView().setBackgroundResource(R.drawable.content_empty_left);
			        mOnScreenHint.setPosition(Gravity.RIGHT|Gravity.BOTTOM,ServiceManager.getAmtMedia().getWindowManager().getDefaultDisplay().getWidth()/8*5 - OnScreenHint.dip2px(ServiceManager.getAmtMedia(), 142) , ServiceManager.getAmtMedia().getResources().getDimensionPixelSize(
			                R.dimen.hint_y_offset));
			        mOnScreenHint.show();
				}
		    });
		}
	}
	
	@Override
	public boolean refresh(ArrayList<SongInfo> recordSongs) {
		this.recordSongs = recordSongs;
//		setScreenTitle(getString(R.string.screen_home_tab_record));
		if(recordSongs !=null && recordSongs.size() > 0){
			listView.setVisibility(View.VISIBLE);
			empty.setVisibility(View.GONE);
			adapter.setRecordSongs(recordSongs);
			adapter.notifyDataSetChanged();
			mediaPlayerService.changeCorsor(db.makeRecordCursor(recordSongs),IMediaPlayerService.MEDIA_MODEL_LOCAL);
		}else{
			setEmpty();
		}
		return true;
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		if(!new File(adapter.getRecordSongs().get((int)id).getDirectory()).exists()){
			Toast.makeText(ScreenRecord.this, ScreenRecord.this.getString(R.string.screen_music_have_no_music), Toast.LENGTH_SHORT).show();
			loadData(REFRESH);
			return;
		};
		mediaPlayerService.changeCorsor(db.makeRecordCursor(adapter.getRecordSongs()),IMediaPlayerService.MEDIA_MODEL_LOCAL);
		ScreenArgs args = new ScreenArgs();
		args.putExtra("screenType", type);
		args.putExtra("position", position);
		args.putExtra("id", (int)id);
		args.putExtra("screenId", ScreenRecord.class.getCanonicalName());
		Constant.RECORD_COUNT++;
		amtScreenService.show(ScreenRecordPlayer.class, args, View.GONE);
//		Intent intent = new Intent();
//		intent.setClass(this, ScreenRecordPlayer.class);
//		intent.putExtra("args", args);
//		this.startActivity(intent);
	}
	
	private void showDialog(){
		final CustomDialog.Builder customBuilder = new CustomDialog.Builder(ServiceManager.getAmtMedia());
		customBuilder.setTitle(getResources().getString(R.string.editor_lyric_prompt))
		.setWhichViewVisible(CustomDialog.contentIsTextView).setMessage(getResources().getString(R.string.play_record_now))
		.setPositiveButton(getResources().getString(R.string.editor_lyric_ok), 
            		new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
	            	dialog.dismiss();
	        		mediaPlayerService.changeCorsor(db.makeRecordCursor(ExtAudioRecorder.recordSongs),IMediaPlayerService.MEDIA_MODEL_LOCAL);
	        		ScreenArgs args = new ScreenArgs();
	        		args.putExtra("screenType", type);
	        		args.putExtra("position", 0);
	        		args.putExtra("id", 0);
	        		args.putExtra("screenId", ScreenRecord.class.getCanonicalName());
	        		amtScreenService.show(ScreenRecordPlayer.class, args, View.GONE);
	            	
				}
            })
            .setNegativeButton(getResources().getString(R.string.editor_lyric_cancel), 
            		new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                	dialog.dismiss();
    			}
            });
		dialog = customBuilder.create();
		dialog.show();
	}
	@Override
	public boolean onEvent(final IMediaEventArgs args) {
		// TODO Auto-generated method stub
		switch(args.getMediaUpdateEventTypes()){
		case RECORD_UPDATE_DATA:
			handler.post(new Runnable() {
				@Override
				public void run() {
					loadData(REFRESH);
//					if("start_play_record".equals(args.getExtra("start_play_record"))){
//						showDialog();
//					}
				}
			});
			break;
		}
		return true;
	}
}
