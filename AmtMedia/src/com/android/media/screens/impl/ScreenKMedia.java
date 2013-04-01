package com.android.media.screens.impl;

import java.io.File;
import java.util.ArrayList;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.amusic.media.R;
import com.android.media.MediaApplication;
import com.android.media.adapter.KMediaSongAdapter;
import com.android.media.dialog.OnScreenHint;
import com.android.media.event.IMediaEventArgs;
import com.android.media.event.IMediaEventHandler;
import com.android.media.model.ScreenArgs;
import com.android.media.model.SongInfo;
import com.android.media.provider.MediaDatabaseHelper;
import com.android.media.screens.KMediaScreen;
import com.android.media.services.IMediaPlayerService;
import com.android.media.services.impl.ServiceManager;
import com.android.media.task.ScreenKMediaTask;
import com.android.media.utils.Constant;
import com.android.media.view.CustomDialog;

public class ScreenKMedia extends KMediaScreen implements OnItemClickListener, IMediaEventHandler, OnClickListener{
	private KMediaSongAdapter adapter;
	private static int INIT = 0;
	private static int REFRESH = 1;
	private String suffix = ".mp3";
	private String accomSuffix = ".bz";
	public  static String bzsongname = "";
	public static String bzplayername = "";
	private LinearLayout empty;
	private boolean load = false;
	private Handler handler;
	private Dialog dialog, deleteDialog;
	private String songName;
	private String singerName;
	private int position;
	private ArrayList<SongInfo> kmediaSongs=null;
	private CheckBox save;
	public static OnScreenHint mOnScreenHint;
	private boolean isDownloadUpdateData = false;
	public static boolean isFirst = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		handler = ServiceManager.getAmtMediaHandler();
		mediaEventService.addEventHandler(this);
		setContentView(R.layout.screen_kmedia);
		setScreenTitle(getString(R.string.screen_home_tab_kmedia));
		listView = (ListView) findViewById(R.id.screen_kmedia_songs_listview);
		empty = (LinearLayout) findViewById(R.id.empty);
		adapter = new KMediaSongAdapter(this, this, listView);
		Cursor cursor = ServiceManager.getMediaService().getMediaDB()
				.queryAccompany(null);
		if (cursor.getCount() > 0) {
			isFirst = true;
			loadData(INIT, true);
			load = true;
		}else{
//			if(mOnScreenHint != null){
//				mOnScreenHint.cancel(); 
//			}
//			mOnScreenHint = OnScreenHint.makeText_Empty(this, false, ServiceManager.getAmtMedia().getResources().getString(R.string.kmedia_content_empty_info0), ServiceManager.getAmtMedia().getResources().getString(R.string.kmedia_content_empty_info1), ServiceManager.getAmtMedia().getResources().getString(R.string.kmedia_content_empty_info2),ServiceManager.getAmtMedia().getResources().getString(R.string.kmedia_content_empty_info3));   //设置toast要显示的信息
//	        mOnScreenHint.getView().setBackgroundResource(R.drawable.content_empty_right);
//	        mOnScreenHint.setPosition(Gravity.RIGHT|Gravity.BOTTOM,this.getWindowManager().getDefaultDisplay().getWidth()/8-OnScreenHint.dip2px(this, 49), ServiceManager.getAmtMedia().getResources().getDimensionPixelSize(
//	                R.dimen.hint_y_offset));
//	        mOnScreenHint.show();
			ServiceManager.getAmtMediaHandler().post(new Runnable(){
				@Override
				public void run() {
					if(mOnScreenHint != null){
						mOnScreenHint.cancel(); 
					}
					mOnScreenHint = OnScreenHint.makeText_Empty(ServiceManager.getAmtMedia(), false, ServiceManager.getAmtMedia().getResources().getString(R.string.kmedia_content_empty_info0), ServiceManager.getAmtMedia().getResources().getString(R.string.kmedia_content_empty_info1), ServiceManager.getAmtMedia().getResources().getString(R.string.kmedia_content_empty_info2),ServiceManager.getAmtMedia().getResources().getString(R.string.kmedia_content_empty_info3));   //设置toast要显示的信息
			        mOnScreenHint.getView().setBackgroundResource(R.drawable.content_empty_right);
			        mOnScreenHint.setPosition(Gravity.RIGHT|Gravity.BOTTOM,ServiceManager.getAmtMedia().getWindowManager().getDefaultDisplay().getWidth()/8-OnScreenHint.dip2px(ServiceManager.getAmtMedia(), 49), ServiceManager.getAmtMedia().getResources().getDimensionPixelSize(
			                R.dimen.hint_y_offset));
			        mOnScreenHint.show();
				}
		    });
		}
		cursor.close();
	}

	public void loadData(int type, boolean isNeedProgressDialog) {
		ScreenKMediaTask task = new ScreenKMediaTask(this, type,
				isNeedProgressDialog);
		task.execute();
	}

	public void initView(ArrayList<SongInfo> kmediaSongs) {
		this.kmediaSongs = kmediaSongs;
		if (kmediaSongs != null && kmediaSongs.size() > 0) {
			empty.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
		} else {
			setEmpty();
		}
		adapter.setkmediaSongs(kmediaSongs);
		listView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		listView.setOnItemClickListener(this);
	}

	public void setEmpty() {
		empty.setVisibility(View.VISIBLE);
		listView.setVisibility(View.GONE);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		songName = (String) ((TextView) view
				.findViewById(R.id.screen_kmedia_song_song)).getText();
		singerName = (String) ((TextView) view
				.findViewById(R.id.screen_kmedia_song_singer)).getText();
		this.position = position;
		bzsongname = songName;
		bzplayername = singerName;
		bzplayername = bzplayername.equals("") ? "" : bzplayername + "-";
		if(isAccompanimentExit()){
			showDialog();
		}else{
//			mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_accompaniment_is_error_one));
//			mOnScreenHint.show();
			ServiceManager.getAmtMediaHandler().post(new Runnable(){
				@Override
				public void run() {
					if(mOnScreenHint != null){
						mOnScreenHint.cancel();
					}
					mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_accompaniment_is_error_one));
					mOnScreenHint.show();
				}
		    });
		}
	}
	private boolean isAccompanimentExit(){
		String realSingerName = singerName.equals("") ? "" : singerName + "-";
		String directory = MediaApplication.accompanyPath + realSingerName + songName + accomSuffix;
		File dir = new File(directory);
		return dir.exists();
	}
	public void showDialog() {
		dialog = new Dialog(ServiceManager.getAmtMedia(), R.style.CustomDialog);
		dialog.setCanceledOnTouchOutside(true);
		dialog.setContentView(R.layout.screen_kmedia_song_options);
		TextView popSong = (TextView) dialog.findViewById(R.id.screen_kmedia_song_options_song);
		Button delete = (Button) dialog.findViewById(R.id.screen_kmedia_song_options_delete);
		Button accompaniment = (Button) dialog.findViewById(R.id.screen_kmedia_song_options_accompaniment);
		Button original = (Button) dialog.findViewById(R.id.screen_kmedia_song_options_original);
		Button back = (Button) dialog.findViewById(R.id.screen_kmedia_song_options_back);
		save = (CheckBox) dialog.findViewById(R.id.screen_kmedia_song_options_save);
		save.setChecked(Constant.IS_WRITE_RECORD_DATA);
		Button editSignature = (Button)dialog.findViewById(R.id.screen_kmedia_song_options_edit_signature);
		editSignature.setVisibility(View.GONE);
		dialog.show();
		popSong.setText(songName);
		delete.setOnClickListener(this);
		original.setOnClickListener(this);
		accompaniment.setOnClickListener(this);
		back.setOnClickListener(this);
		save.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					ScreenKMediaPlayer.saveRecordCheckBoxFlag = true;
				} else {
					ScreenKMediaPlayer.saveRecordCheckBoxFlag = false;
				}
			}
		});
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setScreenTitle(getString(R.string.screen_home_tab_kmedia));
		loadData(REFRESH, true);
	}

	@Override
	protected void onResume() {
		super.onResume();
		setScreenTitle(getString(R.string.screen_home_tab_kmedia));
		ServiceManager.getAmtMedia().getGoBackBtn()
				.setVisibility(View.INVISIBLE);
		if (!load) {
			Cursor cursor = ServiceManager.getMediaService().getMediaDB()
					.queryAccompany(null);
			if (cursor.getCount() > 0) {
				loadData(INIT, true);
				load = true;
			} else {
				setEmpty();
			}
			cursor.close();
		} else {
			// listView.setSelectionFromTop(0, 0);
		}
		if(isFirst){
			isFirst = false;
		}else if(!isFirst && kmediaSongs == null || (kmediaSongs != null && kmediaSongs.size()==0)){
//			if(mOnScreenHint != null){
//				mOnScreenHint.cancel(); 
//			}
//			mOnScreenHint = OnScreenHint.makeText_Empty(this, false, ServiceManager.getAmtMedia().getResources().getString(R.string.kmedia_content_empty_info0), ServiceManager.getAmtMedia().getResources().getString(R.string.kmedia_content_empty_info1), ServiceManager.getAmtMedia().getResources().getString(R.string.kmedia_content_empty_info2),ServiceManager.getAmtMedia().getResources().getString(R.string.kmedia_content_empty_info3));   //设置toast要显示的信息
//	        mOnScreenHint.getView().setBackgroundResource(R.drawable.content_empty_right);
//	        mOnScreenHint.setPosition(Gravity.RIGHT|Gravity.BOTTOM,this.getWindowManager().getDefaultDisplay().getWidth()/8-OnScreenHint.dip2px(this, 49), ServiceManager.getAmtMedia().getResources().getDimensionPixelSize(
//	                R.dimen.hint_y_offset));
//	        mOnScreenHint.show();
			ServiceManager.getAmtMediaHandler().post(new Runnable(){
				@Override
				public void run() {
					if(mOnScreenHint != null){
						mOnScreenHint.cancel(); 
					}
					mOnScreenHint = OnScreenHint.makeText_Empty(ServiceManager.getAmtMedia(), false, ServiceManager.getAmtMedia().getResources().getString(R.string.kmedia_content_empty_info0), ServiceManager.getAmtMedia().getResources().getString(R.string.kmedia_content_empty_info1), ServiceManager.getAmtMedia().getResources().getString(R.string.kmedia_content_empty_info2),ServiceManager.getAmtMedia().getResources().getString(R.string.kmedia_content_empty_info3));   //设置toast要显示的信息
			        mOnScreenHint.getView().setBackgroundResource(R.drawable.content_empty_right);
			        mOnScreenHint.setPosition(Gravity.RIGHT|Gravity.BOTTOM,ServiceManager.getAmtMedia().getWindowManager().getDefaultDisplay().getWidth()/8-OnScreenHint.dip2px(ServiceManager.getAmtMedia(), 49), ServiceManager.getAmtMedia().getResources().getDimensionPixelSize(
			                R.dimen.hint_y_offset));
			        mOnScreenHint.show();
				}
		    });
		}
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
	public boolean refresh(ArrayList<SongInfo> kmediaSongs) {
		this.kmediaSongs = kmediaSongs;
		if (!isDownloadUpdateData) {
			setScreenTitle(getString(R.string.screen_home_tab_kmedia));
		}
		isDownloadUpdateData = false;
		if(kmediaSongs !=null && kmediaSongs.size() > 0){
			empty.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
			adapter.setkmediaSongs(kmediaSongs);
			adapter.notifyDataSetChanged();
		}else{
			setEmpty();
		}
		return true;
	}

	@Override
	public boolean onEvent(IMediaEventArgs args) {
		// TODO Auto-generated method stub
		switch (args.getMediaUpdateEventTypes()) {
		case KMEDIA_UPDATE_DATA:
			isDownloadUpdateData = true;
			handler.post(new Runnable() {
				@Override
				public void run() {
					if (load) {
						loadData(REFRESH, false);
					} else {
						loadData(INIT, false);
						load = true;
					}
				}
			});
			break;
		}
		return true;
	}

	@Override
	public void onClick(View v) {
		dialog.dismiss();
		switch (v.getId()) {
		case R.id.screen_kmedia_song_options_original:
			if (save.isChecked()) {
				ScreenKMediaPlayer.saveRecordCheckBoxFlag = true;
			} else {
				ScreenKMediaPlayer.saveRecordCheckBoxFlag = false;
			}
			ScreenKMediaPlayer.isOriginal = true;
			kmediaSong(true);
			break;
		case R.id.screen_kmedia_song_options_accompaniment:
			if (save.isChecked()) {
				ScreenKMediaPlayer.saveRecordCheckBoxFlag = true;
			} else {
				ScreenKMediaPlayer.saveRecordCheckBoxFlag = false;
			}
			ScreenKMediaPlayer.isOriginal = false;
			kmediaSong(false);
			break;
		case R.id.screen_kmedia_song_options_delete:
			deleteDialog();
			break;
		case R.id.screen_kmedia_song_options_back:
			break;
		}

	}

	private void deleteDialog() {
		final CustomDialog.Builder customBuilder = new CustomDialog.Builder(
				ServiceManager.getAmtMedia());
		customBuilder
				.setTitle(getResources().getString(R.string.delete_propt))
				.setWhichViewVisible(CustomDialog.contentIsTextView)
				.setMessage(getResources().getString(R.string.delete_sure_or_not))
				.setPositiveButton(
						getResources().getString(
								R.string.screen_delete_dialog_ok),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								String directory = kmediaSongs.get(position)
										.getDirectory();
								File file = new File(directory);
								if (file.exists()) {
									file.delete();
									kmediaSongs.remove(position);
									if (kmediaSongs.size() > 0) {
										adapter.notifyDataSetChanged();
									} else {
										setEmpty();
									}
								}					
								deleteDialog.dismiss();
//								mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_audio_player_delete_accompaniment_success));
//								mOnScreenHint.show();
								ServiceManager.getAmtMediaHandler().post(new Runnable(){
									@Override
									public void run() {
										if(mOnScreenHint != null){
											mOnScreenHint.cancel();
										}
										mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_audio_player_delete_accompaniment_success));
										mOnScreenHint.show();
									}
							    });
							}
						})
				.setNegativeButton(
						getResources().getString(
								R.string.screen_delete_dialog_cancel),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								deleteDialog.dismiss();
							}
						});
		deleteDialog = customBuilder.create();
		customBuilder.getProgressTextView().setGravity(Gravity.CENTER);
		deleteDialog.show();
	}

	private void kmediaSong(boolean isOriginal) {
		String realSingerName = singerName.equals("") ? "" : singerName + "-";
		ServiceManager
				.getMediaplayerService()
				.changeCorsor(
						db.queryAccompany(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_FILEPATH
								+ " = "
								+ "'"
								+ MediaApplication.savePath
								+ realSingerName + songName + suffix + "'"),
						IMediaPlayerService.MEDIA_MODEL_KMEDIA);
		ScreenArgs args = new ScreenArgs();
		args.putExtra("screenType", type);
		args.putExtra("position", position);
		args.putExtra("screenId", ScreenKMedia.class.getCanonicalName());
		args.putExtra("songName", songName);
		args.putExtra("singerName", singerName);
//		Intent intent = new Intent();
//		intent.setClass(this, ScreenKMediaPlayer.class);
//		intent.putExtra("args", args);
//		this.startActivity(intent);	
		Constant.KMEDIA_COUNT++;
		ServiceManager.getAmtScreenService().show(ScreenKMediaPlayer.class, args, View.GONE);
	}
}