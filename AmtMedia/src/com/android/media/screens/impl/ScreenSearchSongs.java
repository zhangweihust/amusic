package com.android.media.screens.impl;

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
import com.android.media.adapter.SearchSongAdapter;
import com.android.media.dialog.OnScreenHint;
import com.android.media.event.IMediaEventArgs;
import com.android.media.event.IMediaEventHandler;
import com.android.media.event.impl.MediaEventArgs;
import com.android.media.event.impl.MediaEventTypes;
import com.android.media.handler.MediaHandler;
import com.android.media.model.ScreenArgs;
import com.android.media.screens.SearchScreen;
import com.android.media.services.IMediaPlayerService;
import com.android.media.services.IMediaService;
import com.android.media.services.INetworkService;
import com.android.media.services.impl.ServiceManager;
import com.android.media.thread.MediaThread;
import com.android.media.utils.Constant;
import com.android.media.utils.IAsynMediaQuery;
import com.android.media.utils.KeyboardUtil;
import com.android.media.utils.ToastUtil;
import com.android.media.view.CustomDialog;

public class ScreenSearchSongs extends SearchScreen implements OnClickListener, IMediaEventHandler
		,OnItemClickListener, IAsynMediaQuery {
	private View headerView; // 分页的布局
	private View footerView; 
	private ImageButton headerPrevious; // 上页
	private ImageButton headNext; // 下页
	private TextView headPageInfoText;
	private ImageButton footPrevious; // 上页
	private ImageButton footNext; // 下页
	private TextView footPageInfoText;
	private SearchSongAdapter adapter;
	private int length = 20;
	private int page = 1;
	private Cursor cursor;
	private int totalPages;
	private final MediaHandler mediaHandler;
	private String pageInfo;
	private Dialog downloadDialog;
	private String pinyin;
	private LinearLayout empty;
	private TextView pinyinText;
	
	private MediaEventArgs mediaEventrgs;
	private int tmpSize=0;
	private final INetworkService networkService;
	private boolean refreshFlag = false;
	private OnScreenHint mOnScreenHint;
	public static OnScreenHint mOnScreenHint_null;
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

	public ScreenSearchSongs() {
		mediaHandler = new MediaHandler(this);
		networkService = ServiceManager.getNetworkService();
	}
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_search_list);
		setScreenTitle(getString(R.string.screen_search_songs_title));
		pageInfo = getString(R.string.screen_search_page_info);
		if (savedInstanceState != null) {
			ScreenArgs args = (ScreenArgs) savedInstanceState
					.getSerializable("args");
			pinyin = (String) args.getExtra("pinyin");
			page = (Integer) args.getExtra("page",page);
		}

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
		empty = (LinearLayout) findViewById(R.id.empty);
		pinyinText.addTextChangedListener(searchTextWatcher);
		pinyinText.setOnClickListener(this);
		pinyinText.setText(pinyin);
		headerPrevious.setOnClickListener(this);
		headNext.setOnClickListener(this);
		footPrevious.setOnClickListener(this);
		footNext.setOnClickListener(this);
		adapter = new SearchSongAdapter(this, null, this);
		listView.addHeaderView(headerView);
		listView.addFooterView(footerView);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
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
					mOnScreenHint = OnScreenHint.makeText_keyboard(ScreenSearchSongs.this, (String) msg.obj);
					View v = mOnScreenHint.getView();
					v.setBackgroundDrawable(null);
					int x = left + ScreenSearchSongs.this.getWindowManager().getDefaultDisplay().getWidth()/20 -  OnScreenHint.dip2px(ScreenSearchSongs.this, 38);
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
					mOnScreenHint = OnScreenHint.makeText_keyboard(ScreenSearchSongs.this, (String) msg.obj);
					View v_del = mOnScreenHint.getView();
					TextView tv_del = (TextView)v_del.findViewById(R.id.custom_toast_on_screen_for_keyboard);
					tv_del.setTextSize(OnScreenHint.dip2px(ScreenSearchSongs.this, 15));
					v_del.setBackgroundDrawable(null);
					int x_del = left_del + ScreenSearchSongs.this.getWindowManager().getDefaultDisplay().getWidth()/10 - OnScreenHint.dip2px(ScreenSearchSongs.this, 43);
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
					mOnScreenHint = OnScreenHint.makeText_keyboard(ScreenSearchSongs.this, (String) msg.obj);
					View v_del_long = mOnScreenHint.getView();
					TextView tv_del_long = (TextView)v_del_long.findViewById(R.id.custom_toast_on_screen_for_keyboard);
					tv_del_long.setTextSize(OnScreenHint.dip2px(ScreenSearchSongs.this, 15));
					v_del_long.setBackgroundDrawable(null);
					int x_del_long = left_del_long + ScreenSearchSongs.this.getWindowManager().getDefaultDisplay().getWidth()/10 - OnScreenHint.dip2px(ScreenSearchSongs.this, 43);
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
		if(mOnScreenHint_null != null){
			mOnScreenHint_null.cancel(); 
		}
		int location = ServiceManager.getAmtMedia().getResources().getDimensionPixelOffset(R.dimen.audio_info_layout_height) + ServiceManager.getAmtMedia().getResources().getDimensionPixelOffset(R.dimen.search_content_layout_height)*3/2;
		mOnScreenHint_null = OnScreenHint.makeText_Empty(this, true, ServiceManager.getAmtMedia().getResources().getString(R.string.search_bar_empty_info0), ServiceManager.getAmtMedia().getResources().getString(R.string.search_bar_empty_info1), ServiceManager.getAmtMedia().getResources().getString(R.string.search_bar_empty_info2),ServiceManager.getAmtMedia().getResources().getString(R.string.search_bar_empty_info3));   //设置toast要显示的信息
        mOnScreenHint_null.getView().setBackgroundResource(R.drawable.content_empty_left_top);
        mOnScreenHint_null.setPosition(Gravity.LEFT|Gravity.TOP,0, location);
        mOnScreenHint_null.show();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.screen_search_filter:
			if (keyboard != null && keyboard.getVisibility() == View.GONE) {
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
		setScreenTitle(getString(R.string.screen_search_songs_title));
		ScreenArgs args = (ScreenArgs) intent.getSerializableExtra("args");
		Boolean goback = (Boolean) args.getExtra("goback");	
		if (goback == null) {
			adapter.changeCursor(null);
			pinyinText.setText("");
		} else {
			new MediaThread(mediaHandler, MediaQueryToken.QUERY_INIT).start();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshFlag = true;
		keyboard = (LinearLayout) findViewById(R.id.screen_search_list_keyboard);
		keyboard.setVisibility(View.GONE);
		setScreenTitle(getString(R.string.screen_search_songs_title));
		if(ScreenHome.tw!=null){
			ScreenHome.tw.setVisibility(View.VISIBLE);
		}
		adapter.notifyDataSetChanged();
		String tvText = pinyinText.getText().toString().trim();
		if(tvText == null || tvText.equals("")){
			if(mOnScreenHint_null != null){
				mOnScreenHint_null.cancel(); 
			}
			int location = ServiceManager.getAmtMedia().getResources().getDimensionPixelOffset(R.dimen.audio_info_layout_height) + ServiceManager.getAmtMedia().getResources().getDimensionPixelOffset(R.dimen.search_content_layout_height)*3/2;
			mOnScreenHint_null = OnScreenHint.makeText_Empty(this, true, ServiceManager.getAmtMedia().getResources().getString(R.string.search_bar_empty_info0), ServiceManager.getAmtMedia().getResources().getString(R.string.search_bar_empty_info1), ServiceManager.getAmtMedia().getResources().getString(R.string.search_bar_empty_info2),ServiceManager.getAmtMedia().getResources().getString(R.string.search_bar_empty_info3));   //设置toast要显示的信息
	        mOnScreenHint_null.getView().setBackgroundResource(R.drawable.content_empty_left_top);
	        mOnScreenHint_null.setPosition(Gravity.LEFT|Gravity.TOP,0, location);
	        mOnScreenHint_null.show();
		}
	}

	@Override
	public void updateData(MediaQueryToken token) {
		switch (token) {
		case QUERY_INIT:
			adapter.changeCursor(cursor);
			setStatus();
			if(tmpSize==0){
				if(pinyinText.getText() != null && pinyinText.getText().length() > 0){
					setEmpty();
				}else{
					headerView.setVisibility(View.GONE);
					footerView.setVisibility(View.GONE);
					empty.setVisibility(View.GONE);
				}
			}else{
				headerView.setVisibility(View.VISIBLE);
				footerView.setVisibility(View.VISIBLE);
				empty.setVisibility(View.GONE);
			}
			break;
		}
	}
	
	private void setEmpty(){
		headerView.setVisibility(View.GONE);
		footerView.setVisibility(View.GONE);
		empty.setVisibility(View.VISIBLE);
	}

	@Override
	public void queryData(MediaQueryToken token) {
		clearDownloadHandlers();
		int size=0;
		switch (token) {
		case QUERY_INIT:
			size = db.queryDictionaryAudiosCount(pinyin);
			cursor = db.queryDictionaryAudios(pinyin, length * (page - 1),
					length);
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
			return ;
		}
		int status = (Integer) mediaEventrgs.getExtra("status");

		if (status == IMediaService.STATE_DEFAULT) {
			mediaEventrgs.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD);
			String songName = (String) mediaEventrgs.getExtra("song");
			final String singer = (String) mediaEventrgs.getExtra("singer");
			final CustomDialog.Builder customBuilder = new CustomDialog.Builder(ScreenSearchSongs.this.getParent().getParent());
			customBuilder.setTitle(songName).setWhichViewVisible(CustomDialog.contentIsCheckBox).setMessage(R.string.screen_audio_download_onekey_prompt)
			.setCheckBoxText(getString(R.string.screen_audio_download_if_download_accompany))
					.setPositiveButton(getString(R.string.custom_dialog_button_ok), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if (customBuilder.getmCheckBox().isChecked()) {
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
			final CustomDialog.Builder customBuilder = new CustomDialog.Builder(ScreenSearchSongs.this.getParent().getParent());
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
			final CustomDialog.Builder customBuilder = new CustomDialog.Builder(ScreenSearchSongs.this.getParent().getParent());
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
//								intent.setClass(ScreenSearchSongs.this, ScreenKMediaPlayer.class);
//								intent.putExtra("args", args);
//								ScreenSearchSongs.this.startActivity(intent);
								ScreenKMediaPlayer.saveRecordCheckBoxFlag = Constant.IS_WRITE_RECORD_DATA;
								Constant.KMEDIA_COUNT++;
								ServiceManager.getAmtScreenService().show(ScreenKMediaPlayer.class, args, View.GONE);
							}
							else
							{
								Toast.makeText(ScreenSearchSongs.this,getString(R.string.prompt_if_no_record_indatabase), Toast.LENGTH_SHORT).show();
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
							song + ScreenSearchSongs.this.getString(R.string.screen_audio_download_in));
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
									song + ScreenSearchSongs.this.getString(R.string.screen_audio_download_in));
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
					Toast.makeText(ScreenSearchSongs.this,getString(R.string.prompt_if_no_record_indatabase), Toast.LENGTH_SHORT).show();
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
	protected void onPause() {
		refreshFlag = false;
		super.onPause();
		if(mOnScreenHint_null != null){
			mOnScreenHint_null.cancel(); 
		}
	}
	
	@Override
	public boolean refresh() {
		if (refreshFlag == true){
//			MediaApplication.logD(ScreenSearchSongs.class, "queryData");
			new MediaThread(mediaHandler, MediaQueryToken.QUERY_INIT).start();
		}
		return true;
	}
}
