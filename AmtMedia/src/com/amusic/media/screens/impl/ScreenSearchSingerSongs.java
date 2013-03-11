package com.amusic.media.screens.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amusic.media.R;
import com.amusic.media.adapter.SearchSingerSongAdapter;
import com.amusic.media.dialog.OnScreenHint;
import com.amusic.media.event.IMediaEventArgs;
import com.amusic.media.event.IMediaEventHandler;
import com.amusic.media.event.impl.MediaEventArgs;
import com.amusic.media.event.impl.MediaEventTypes;
import com.amusic.media.handler.MediaHandler;
import com.amusic.media.model.ScreenArgs;
import com.amusic.media.screens.SearchScreen;
import com.amusic.media.services.IMediaPlayerService;
import com.amusic.media.services.IMediaService;
import com.amusic.media.services.INetworkService;
import com.amusic.media.services.impl.ServiceManager;
import com.amusic.media.thread.MediaThread;
import com.amusic.media.utils.Constant;
import com.amusic.media.utils.IAsynMediaQuery;
import com.amusic.media.utils.KeyboardUtil;
import com.amusic.media.utils.ToastUtil;
import com.amusic.media.view.CustomDialog;

public class ScreenSearchSingerSongs extends SearchScreen implements
		OnClickListener, OnItemClickListener, IMediaEventHandler,
		IAsynMediaQuery {
	private View headerView; // 分页的布局
	private View footerView; 
	private ImageButton headerPrevious; // 上页
	private ImageButton headNext; // 下页
	private TextView headPageInfoText;
	private ImageButton footPrevious; // 上页
	private ImageButton footNext; // 下页
	private TextView footPageInfoText;
	private SearchSingerSongAdapter adapter;
	private int length = 20;
	private int page = 1;
	private Cursor cursor;
	private int totalPages;
	private final MediaHandler mediaHandler;
	private String pageInfo;
	private Dialog downloadDialog;
	private String pinyin;
	private TextView pinyinText;
	private String singer;
	private MediaEventArgs mediaEventrgs;
	private int tmpSize=0;
	private final INetworkService networkService;
	private boolean refreshFlag = false;
	private OnScreenHint mOnScreenHint;
	private TextWatcher searchTextWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			pinyin = s.toString().trim();
			page = 1;
			new MediaThread(mediaHandler, MediaQueryToken.QUERY_INIT).start();
		}
	};

	public ScreenSearchSingerSongs() {
		mediaHandler = new MediaHandler(this);
		networkService = ServiceManager.getNetworkService();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_search_list);
		pageInfo = getString(R.string.screen_search_page_info);
		ScreenArgs args;
		if (savedInstanceState != null) {
			args = (ScreenArgs) savedInstanceState.getSerializable("args");
			singer = (String) args.getExtra("singer");
			pinyin = (String) args.getExtra("pinyin");
			page = (Integer) args.getExtra("page",page);
		} else {
			args = (ScreenArgs) getIntent().getSerializableExtra("args");
			singer = (String) args.getExtra("singer");
		}
		setScreenTitle(singer);
		keyboard = (LinearLayout) findViewById(R.id.screen_search_list_keyboard);
		headerView=getLayoutInflater().inflate(
				R.layout.screen_search_listview_page, null);
		footerView=getLayoutInflater().inflate(
				R.layout.screen_search_listview_page, null);
		listView = (ListView) findViewById(R.id.screen_search_list);
		headPageInfoText = (TextView)headerView.findViewById(R.id.screen_search_page_info);
		headerPrevious = (ImageButton)headerView.findViewById(R.id.screen_search_page_previous);
		headNext = (ImageButton)headerView.findViewById(R.id.screen_search_page_next);
		footPageInfoText = (TextView)footerView.findViewById(R.id.screen_search_page_info);
		footPrevious = (ImageButton)footerView.findViewById(R.id.screen_search_page_previous);
		footNext = (ImageButton)footerView.findViewById(R.id.screen_search_page_next);
		pinyinText = (TextView) findViewById(R.id.screen_search_filter);
		pinyinText.setHint(getResources().getString(R.string.screen_search_song_filter));
		pinyinText.addTextChangedListener(searchTextWatcher);
		pinyinText.setOnClickListener(this);
		pinyinText.setText(pinyin);
		headerPrevious.setOnClickListener(this);
		headNext.setOnClickListener(this);
		footPrevious.setOnClickListener(this);
		footNext.setOnClickListener(this);
		adapter = new SearchSingerSongAdapter(this, null, this);
		Handler keyboardHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				String pinyinStr = "";
				Editable editable;
				switch (msg.what) {
				case IMediaService.MSG_WHAT_CHARACTER:
					editable = pinyinText.getEditableText();
					if (editable != null) {
						pinyinStr = editable.toString();
					}
					pinyinText.setText(pinyinStr + (String) msg.obj);
					int left = msg.getData().getInt("left");
					if(mOnScreenHint != null){
						mOnScreenHint.cancel();
					}
					mOnScreenHint = OnScreenHint.makeText_keyboard(ScreenSearchSingerSongs.this, (String) msg.obj);
					View v = mOnScreenHint.getView();
					v.setBackgroundDrawable(null);
					int x = left + ScreenSearchSingerSongs.this.getWindowManager().getDefaultDisplay().getWidth()/20 - OnScreenHint.dip2px(ScreenSearchSingerSongs.this, 38);
					if(x < 0){
						x = 0;
					}
				    mOnScreenHint.setPosition(Gravity.LEFT|Gravity.BOTTOM, x, keyboard.getHeight());					  
				    mOnScreenHint.setDuration(300);
				    mOnScreenHint.show();
					break;
				case IMediaService.MSG_WHAT_DELETE:
					editable = pinyinText.getEditableText();
					if (editable != null) {
						pinyinStr = editable.toString();
					}
					int length = pinyinStr.length();
					if (length == 1) {
						pinyinStr = "";
					} else if (length > 1) {
						pinyinStr = pinyinStr.substring(0, length - 1);
					}
					pinyinText.setText(pinyinStr);
					int left_del = msg.getData().getInt("left");
					if(mOnScreenHint != null){
						mOnScreenHint.cancel();
					}
					mOnScreenHint = OnScreenHint.makeText_keyboard(ScreenSearchSingerSongs.this, (String) msg.obj);
					View v_del = mOnScreenHint.getView();
					TextView tv_del = (TextView)v_del.findViewById(R.id.custom_toast_on_screen_for_keyboard);
					tv_del.setTextSize(OnScreenHint.dip2px(ScreenSearchSingerSongs.this, 15));
					v_del.setBackgroundDrawable(null);
					int x_del = left_del + ScreenSearchSingerSongs.this.getWindowManager().getDefaultDisplay().getWidth()/10 - OnScreenHint.dip2px(ScreenSearchSingerSongs.this, 43);
					if(x_del < 0){
						x_del = 0;
					}
				    mOnScreenHint.setPosition(Gravity.LEFT|Gravity.BOTTOM, x_del, keyboard.getHeight());	  
				    mOnScreenHint.setDuration(300);
				    mOnScreenHint.show();
					break;
				case IMediaService.MSG_WHAT_WILD:
					keyboard.setVisibility(View.GONE);
					if(ScreenHome.tw!=null){
						ScreenHome.tw.setVisibility(View.VISIBLE);
					}
					break;
				case IMediaService.MSG_WHAT_DELETE_LONGCLICK:
					pinyinText.setText("");
					int left_del_long = msg.getData().getInt("left");
					if(mOnScreenHint != null){
						mOnScreenHint.cancel();
					}
					mOnScreenHint = OnScreenHint.makeText_keyboard(ScreenSearchSingerSongs.this, (String) msg.obj);
					View v_del_long = mOnScreenHint.getView();
					TextView tv_del_long = (TextView)v_del_long.findViewById(R.id.custom_toast_on_screen_for_keyboard);
					tv_del_long.setTextSize(OnScreenHint.dip2px(ScreenSearchSingerSongs.this, 15));
					v_del_long.setBackgroundDrawable(null);
					int x_del_long = left_del_long + ScreenSearchSingerSongs.this.getWindowManager().getDefaultDisplay().getWidth()/10 - OnScreenHint.dip2px(ScreenSearchSingerSongs.this, 43);
					if(x_del_long < 0){
						x_del_long = 0;
					}
				    mOnScreenHint.setPosition(Gravity.LEFT|Gravity.BOTTOM, x_del_long, keyboard.getHeight());	  
				    mOnScreenHint.setDuration(300);
				    mOnScreenHint.show();
					break;
				}
			}

		};
		new KeyboardUtil(this, keyboardHandler);
		registerForContextMenu(listView);
		mediaEventService.addEventHandler(this);
		new MediaThread(mediaHandler, MediaQueryToken.QUERY_INIT).start();
		listView.addHeaderView(headerView);
		listView.addFooterView(footerView);
		//adapter.notifyDataSetChanged();
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.screen_search_filter:
			if (keyboard != null&&keyboard.getVisibility()==View.GONE) {
				keyboard.setVisibility(View.VISIBLE);
				if(ScreenHome.tw!=null){
					ScreenHome.tw.setVisibility(View.GONE);
				}
			}
			break;
		case R.id.screen_search_page_previous:
			if (page > 1) {
				page--;
				new MediaThread(mediaHandler, MediaQueryToken.QUERY_INIT)
						.start();
				listView.setSelection(0);
			}
			break;
		case R.id.screen_search_page_next:
			if (page < totalPages) {
				page++;
				new MediaThread(mediaHandler, MediaQueryToken.QUERY_INIT)
						.start();
				listView.setSelection(0);
			}
			break;
		}
	}

	private void setStatus() {
		if (totalPages == 0) {
			page = 0;
		}
		headPageInfoText.setText(String.format(pageInfo, page, totalPages));
		footPageInfoText.setText(String.format(pageInfo, page, totalPages));
		if (page <= 1) {
			headerPrevious.setEnabled(false);
			footPrevious.setEnabled(false);
			headerPrevious.setVisibility(View.GONE);
			footPrevious.setVisibility(View.GONE);
		} else {
			headerPrevious.setEnabled(true);
			footPrevious.setEnabled(true);
			headerPrevious.setVisibility(View.VISIBLE);
			footPrevious.setVisibility(View.VISIBLE);
		}
		if (page >= totalPages) {
			headNext.setEnabled(false);
			footNext.setEnabled(false);
			headNext.setVisibility(View.GONE);
			footNext.setVisibility(View.GONE);
		} else {
			headNext.setEnabled(true);
			footNext.setEnabled(true);
			headNext.setVisibility(View.VISIBLE);
			footNext.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		keyboard = (LinearLayout) findViewById(R.id.screen_search_list_keyboard);
		pinyinText.setText("");
		setScreenTitle(singer);
		ScreenArgs args = (ScreenArgs) intent.getSerializableExtra("args");
		Boolean goback = (Boolean) args.getExtra("goback");
		if (goback == null) {
			adapter.changeCursor(null);
		} else {
			singer = (String) args.getExtra("singer");
			new MediaThread(mediaHandler, MediaQueryToken.QUERY_INIT).start();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshFlag = true;
		keyboard = (LinearLayout) findViewById(R.id.screen_search_list_keyboard);
		keyboard.setVisibility(View.GONE);
		setScreenTitle(singer);
		View tabhost = ScreenHome.tw;
		if (tabhost != null) {
			tabhost.setVisibility(View.VISIBLE);
		}
		adapter.notifyDataSetChanged();
		
	}

	@Override
	public void updateData(MediaQueryToken token) {
		switch (token) {
		case QUERY_INIT:
			adapter.changeCursor(cursor);
			setStatus();
			if(tmpSize==0){
				headerView.setVisibility(View.GONE);
				footerView.setVisibility(View.GONE);
			}else{
				headerView.setVisibility(View.VISIBLE);
				footerView.setVisibility(View.VISIBLE);
			}
			break;
		}
	}

	@Override
	public void queryData(MediaQueryToken token) {
		clearDownloadHandlers();
		int size=0;
		switch (token) {
		case QUERY_INIT:
			size = db.queryDictionarySingerAudiosCount(singer, pinyin);
			cursor = db.queryDictionarySingerAudios(singer, pinyin, length
					* (page - 1), length);
			totalPages = (size + length - 1) / length;
			break;
		}
		tmpSize=size;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		ScreenArgs args = new ScreenArgs();
		args.putExtra("pinyin", pinyin);
		args.putExtra("page", page);
		outState.putSerializable("args", args);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		mediaEventrgs = (MediaEventArgs) view.getTag();
		if(mediaEventrgs==null){
			return;
		}
		
		int status = (Integer) mediaEventrgs.getExtra("status");

		if (status == IMediaService.STATE_DEFAULT) {
			mediaEventrgs.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD);
			String songName = (String) mediaEventrgs.getExtra("song");
			final String singer = (String) mediaEventrgs.getExtra("singer");
			final CustomDialog.Builder customBuilder = new CustomDialog.Builder(ScreenSearchSingerSongs.this.getParent().getParent());
			customBuilder.setTitle(songName).setWhichViewVisible(CustomDialog.contentIsCheckBox).setMessage(R.string.screen_audio_download_onekey_prompt)
			.setCheckBoxText(getString(R.string.screen_audio_download_if_download_accompany))
					.setPositiveButton(getString(R.string.custom_dialog_button_ok), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if (customBuilder.getmCheckBox().isChecked()) {
//								MediaApplication.logD(ScreenSearchCategorySongs.class, "同时下载伴奏");
								if("".equals(singer)){
									mediaEventrgs.putExtra("downloadWithAccompany", IMediaService.DOWNLOAD_NOT_WITH_ACCOMPANY);
								} else {
									mediaEventrgs.putExtra("downloadWithAccompany", IMediaService.DOWNLOAD_WITH_ACCOMPANY);
								}
							} else {
								mediaEventrgs.putExtra("downloadWithAccompany", IMediaService.DOWNLOAD_NOT_WITH_ACCOMPANY);
//								MediaApplication.logD(ScreenSearchCategorySongs.class, "取消下载伴奏");
							}
							if (networkService.acquire(true)) {
								mediaEventService.onMediaUpdateEvent(mediaEventrgs);
							}
							if(downloadDialog !=null ){
								downloadDialog.dismiss();
								downloadDialog = null;
							}
						}
					}).setNegativeButton(getString(R.string.custom_dialog_button_cancel), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if(downloadDialog !=null ){
								downloadDialog.dismiss();
								downloadDialog = null;
							}
						}
					});
			if(Constant.IS_DOWNLOAD_ACCOMPANY && !"".equals(singer)){
				customBuilder.setCheckBoxChecked(true);
			}
			if (downloadDialog == null) {
				downloadDialog = customBuilder.create();
				downloadDialog.show();
			} else {
				if (!downloadDialog.isShowing()) {
					downloadDialog = customBuilder.create();
					downloadDialog.show();
				}
			}
		} else if (status == IMediaService.STATE_ORIGINAL_FINISHED) {
//			MediaApplication.logD(ScreenSearchCategorySongs.class, "status == IMediaService.STATE_ORIGINAL_FINISHED");
			String songName = (String) mediaEventrgs.getExtra("song");
			final CustomDialog.Builder customBuilder = new CustomDialog.Builder(ScreenSearchSingerSongs.this.getParent().getParent());
			customBuilder.setTitle(songName).setWhichViewVisible(CustomDialog.contentIsListView).setListViewData(getData())
			.setLayoutID(CustomDialog.LISTVIEW_ITEM_TEXTVIEW).setOnItemClickListener(itemClickListener);
			if (downloadDialog == null) {
				downloadDialog = customBuilder.create();
				downloadDialog.show();
			} else {
				if (!downloadDialog.isShowing()) {
					downloadDialog = customBuilder.create();
					downloadDialog.show();
				}
			}
		} else if (status == IMediaService.STATE_ALL_FINISHED) {
			final String songName = (String) mediaEventrgs.getExtra("song");
			final String singer = (String) mediaEventrgs.getExtra("singer");
			final CustomDialog.Builder customBuilder = new CustomDialog.Builder(ScreenSearchSingerSongs.this.getParent().getParent());
			customBuilder.setTitle(songName).setWhichViewVisible(CustomDialog.contentIsTextView).setMessage(getString(R.string.screen_audio_download_if_ktv))
					.setPositiveButton(getString(R.string.custom_dialog_button_ok), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if (ServiceManager.getMediaService().getMediaDB().checkifsongexists(songName) > 0)
							{
								mediaPlayerService.changeCorsor(db.queryAccompanys(songName, singer), IMediaPlayerService.MEDIA_MODEL_KMEDIA);
								ScreenArgs args = new ScreenArgs();
								args.putExtra("screenType", type);
								args.putExtra("position", 0);
								args.putExtra("screenId", ScreenSearchCategorySongs.class.getCanonicalName());
								args.putExtra("isOriginal", true);
								args.putExtra("songName", songName);
								args.putExtra("singerName", singer);
//								Intent intent = new Intent();
//								intent.setClass(ScreenSearchSingerSongs.this, ScreenKMediaPlayer.class);
//								intent.putExtra("args", args);
//								ScreenSearchSingerSongs.this.startActivity(intent);
								ScreenKMediaPlayer.saveRecordCheckBoxFlag = Constant.IS_WRITE_RECORD_DATA;
								Constant.KMEDIA_COUNT++;
								ServiceManager.getAmtScreenService().show(ScreenKMediaPlayer.class, args, View.GONE);
							}
							else
							{
								Toast.makeText(ScreenSearchSingerSongs.this,getString(R.string.prompt_if_no_record_indatabase), Toast.LENGTH_SHORT).show();
							}
							if(downloadDialog !=null ){
								downloadDialog.dismiss();
								downloadDialog = null;
							}
						}
					}).setNegativeButton(getString(R.string.custom_dialog_button_cancel), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if(downloadDialog !=null ){
								downloadDialog.dismiss();
								downloadDialog = null;
							}
						}
					});
			if (downloadDialog == null) {
				downloadDialog = customBuilder.create();
				customBuilder.getProgressTextView().setGravity(Gravity.CENTER);
				downloadDialog.show();
			} else {
				if (!downloadDialog.isShowing()) {
					downloadDialog = customBuilder.create();
					customBuilder.getProgressTextView().setGravity(Gravity.CENTER);
					downloadDialog.show();
				}
			}
		} else {
			final String song = (String) mediaEventrgs.getExtra("song");
			ServiceManager.getAmtMediaHandler().post(new Runnable() {

				@Override
				public void run() {
					Toast toast = ToastUtil.getInstance().getToast(
							 song + ScreenSearchSingerSongs.this.getString(R.string.screen_audio_download_in));
					toast.setDuration(Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
			});
		}
	}

	private List<Map<String, Object>> getData() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		map = new HashMap<String, Object>();
		map.put("title", getString(R.string.screen_audio_download_accompany));
		map.put("caseId", CustomDialog.DOWNLOAD_ACCOMPANY);
		list.add(map);
		map = new HashMap<String, Object>();
		map.put("title", getString(R.string.screen_audio_download_play_orginal));
		map.put("caseId", CustomDialog.ORIGINAL_AUDIO_PLAY);
		list.add(map);
//		map = new HashMap<String, Object>();
//		map.put("title", getString(R.string.screen_audio_download_go_back));
//		map.put("caseId", CustomDialog.GO_BACK);
//		list.add(map);
		return list;
	}
	
	private OnItemClickListener itemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
			CustomDialog.Builder.ViewHolder holer = (CustomDialog.Builder.ViewHolder) view.getTag();
			switch (holer.caseId) {
			case CustomDialog.DOWNLOAD_ACCOMPANY:
				int songId = (Integer) mediaEventrgs.getExtra("songId");
				if( db.accompanyInDownloads(songId) ){
					final String song = (String) mediaEventrgs.getExtra("song");
					ServiceManager.getAmtMediaHandler().post(new Runnable() {
						@Override
						public void run() {
							Toast toast = ToastUtil.getInstance().getToast(
									song + ScreenSearchSingerSongs.this.getString(R.string.screen_audio_download_in));
							toast.setDuration(Toast.LENGTH_SHORT);
							toast.setGravity(Gravity.CENTER, 0, 0);
							toast.show();
						}
					});
					return;
				}
				mediaEventrgs.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_ACCOMPANY);
				if (networkService.acquire(true)) {
				mediaEventService.onMediaUpdateEvent(mediaEventrgs);
				}
				break;
			case CustomDialog.ORIGINAL_AUDIO_PLAY:
				final String songName = (String) mediaEventrgs.getExtra("song");
				final String singer = (String) mediaEventrgs.getExtra("singer");
				if (ServiceManager.getMediaService().getMediaDB().checkifsongexists(songName) > 0)
				{
					mediaPlayerService.changeCorsor(db.queryAccompanys(songName, singer), IMediaPlayerService.MEDIA_MODEL_LOCAL);
					ScreenArgs args = new ScreenArgs();
					args.putExtra("screenType", type);
					args.putExtra("position", 0);
					args.putExtra("screenId", ScreenSearchCategorySongs.class.getCanonicalName());
					amtScreenService.show(ScreenAudioPlayer.class, args, View.GONE);
				}
				else
				{
					Toast.makeText(ScreenSearchSingerSongs.this,getString(R.string.prompt_if_no_record_indatabase), Toast.LENGTH_SHORT).show();
				}
				
				break;
			case CustomDialog.GO_BACK:
				break;
			}
			if(downloadDialog !=null ){
				downloadDialog.dismiss();
				downloadDialog = null;
			}
		}
	};

	@Override
	protected void onPause() {
		refreshFlag = false;
		super.onPause();
	}
	
	@Override
	public boolean onEvent(IMediaEventArgs args) {
		switch (args.getMediaUpdateEventTypes()) {
		case AUDIO_DOWNLOAD_CANCEL_UI:
		case AUDIO_DOWNLOAD_PAUSE_UI:
		case AUDIO_DOWNLOAD_FINISH_UI:
		case AUDIO_DOWNLOAD_BEGIN_UI:
		case AUDIO_DOWNLOAD_REBEGIN_UI:
		case AUDIO_DOWNLOAD_DELETE_UI:
		case AUDIO_RE_DOWNLOAD_UI:
		case AUDIO_DOWNLOAD_RESUME_UI:
		case AUDIO_DOWNLOAD_UI:
			new MediaThread(mediaHandler, MediaQueryToken.QUERY_INIT).start();
			break;
		}
		return true;
	}
	
	@Override
	public boolean refresh() {
		if (refreshFlag == true){
//			MediaApplication.logD(ScreenSearchSingerSongs.class, "queryData");
			new MediaThread(mediaHandler, MediaQueryToken.QUERY_INIT).start();
		}
		return true;
	}
}
