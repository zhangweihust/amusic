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
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amusic.media.R;
import com.android.media.MediaApplication;
import com.android.media.adapter.SearchCategorySongAdapter;
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

public class ScreenSearchCategorySongs extends SearchScreen implements OnClickListener, OnItemClickListener, IAsynMediaQuery, IMediaEventHandler {
	private View headerView; // 分页的布局
	private View footerView; 
	private ImageButton headerPrevious; // 上页
	private ImageButton headNext; // 下页
	private TextView headPageInfoText;
	private ImageButton footPrevious; // 上页
	private ImageButton footNext; // 下页
	private TextView footPageInfoText;
	private SearchCategorySongAdapter adapter;
	private int length = 20;
	private int page = 1;
	private Cursor cursor;
	private int totalPages;
	private final MediaHandler mediaHandler;
	private String pageInfo;
	private Dialog downloadDialog;
	private MediaEventArgs mediaEventrgs;
	private String pinyin;
	private TextView pinyinText;
	private String category;
	private String categoryType;
	private RelativeLayout filterRelativelayout;
	private int tmpSize = 0;
	private final INetworkService networkService;
	private boolean refreshFlag = false;
	public ScreenSearchCategorySongs() {
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
			category = (String) args.getExtra("category");
			categoryType = (String) args.getExtra("categoryType");
			pinyin = (String) args.getExtra("pinyin");
			page = (Integer) args.getExtra("page",page);
		} else {
			args = (ScreenArgs) getIntent().getSerializableExtra("args");
			category = (String) args.getExtra("category");
			categoryType = (String) args.getExtra("categoryType");
		}
		setScreenTitle(category);
		pinyinText = (TextView) findViewById(R.id.screen_search_filter);
		pinyinText.setHint(getResources().getString(R.string.screen_search_song_filter));
		filterRelativelayout = (RelativeLayout) findViewById(R.id.screen_search_filter_relativelayout);
		filterRelativelayout.setVisibility(View.GONE);
		listView = (ListView) findViewById(R.id.screen_search_list);
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
		headerPrevious.setOnClickListener(this);
		headNext.setOnClickListener(this);
		footPrevious.setOnClickListener(this);
		footNext.setOnClickListener(this);
		adapter = new SearchCategorySongAdapter(this, null, this);
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
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.screen_search_page_previous:
			if (page > 1) {
				page--;
				new MediaThread(mediaHandler, MediaQueryToken.QUERY_INIT).start();
				listView.setSelection(0);
			}
			break;
		case R.id.screen_search_page_next:
			if (page < totalPages) {
				page++;
				new MediaThread(mediaHandler, MediaQueryToken.QUERY_INIT).start();
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
		page = 1;
		ScreenArgs args = (ScreenArgs) intent.getSerializableExtra("args");
		category = (String) args.getExtra("category");
		categoryType = (String) args.getExtra("categoryType");
		setScreenTitle(category);
		Boolean goback = (Boolean) args.getExtra("goback");
		if (goback == null) {
			adapter.changeCursor(null);
			pinyinText.setText("");
		} else {
			listView.setSelection(0);    //定位到第一行
			new MediaThread(mediaHandler, MediaQueryToken.QUERY_INIT).start();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshFlag = true;
		setScreenTitle(category);
		adapter.notifyDataSetChanged();
		listView.setSelectionFromTop(0, 0);
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
		MediaApplication.logD(ScreenSearchCategorySongs.class, "QUERY_DATA");
		clearDownloadHandlers();
		int size = 0;
		switch (token) {
		case QUERY_INIT:
			size = db.queryCategoryAudiosCount(categoryType, pinyin);
			cursor = db.queryCategoryAudios(categoryType, pinyin, length * (page - 1), length);
			totalPages = (size + length - 1) / length;
			break;
		}
		tmpSize = size;
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
		int status = (Integer) mediaEventrgs.getExtra("status");
		if (status == IMediaService.STATE_DEFAULT) {
			mediaEventrgs.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD);
			String songName = (String) mediaEventrgs.getExtra("song");
			final String singer = (String) mediaEventrgs.getExtra("singer");
			final CustomDialog.Builder customBuilder = new CustomDialog.Builder(ScreenSearchCategorySongs.this.getParent().getParent());
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
								new Thread(new Runnable(){
									@Override
									public void run() {
										mediaEventService.onMediaUpdateEvent(mediaEventrgs);
									}}).start();
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
					}).setCheckBoxListener(new CompoundButton.OnCheckedChangeListener() {
						
						@Override
						public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
							// TODO Auto-generated method stub
							String toasttip;
							if (!checkifcandownloadaccompany(singer))
							{
								toasttip = getString(R.string.screen_audio_download_cannot_download_accompany);
								buttonView.setChecked(false);
								Toast.makeText(ScreenSearchCategorySongs.this,toasttip, Toast.LENGTH_SHORT).show();
							}
							
						}
					});
			if(Constant.IS_DOWNLOAD_ACCOMPANY && checkifcandownloadaccompany(singer)){
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
			final CustomDialog.Builder customBuilder = new CustomDialog.Builder(ScreenSearchCategorySongs.this.getParent().getParent());
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
			final CustomDialog.Builder customBuilder = new CustomDialog.Builder(ScreenSearchCategorySongs.this.getParent().getParent());
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
								args.putExtra("songName", songName);
								args.putExtra("singerName", singer);
//								Intent intent = new Intent();
//								intent.setClass(ScreenSearchCategorySongs.this, ScreenKMediaPlayer.class);
//								intent.putExtra("args", args);
//								ScreenSearchCategorySongs.this.startActivity(intent);
								ScreenKMediaPlayer.saveRecordCheckBoxFlag = Constant.IS_WRITE_RECORD_DATA;
								Constant.KMEDIA_COUNT++;
								ServiceManager.getAmtScreenService().show(ScreenKMediaPlayer.class, args, View.GONE);
							}
							else
							{
								Toast.makeText(ScreenSearchCategorySongs.this,getString(R.string.prompt_if_no_record_indatabase), Toast.LENGTH_SHORT).show();
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
							song + ScreenSearchCategorySongs.this.getString(R.string.screen_audio_download_in));
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
				if (!checkifcandownloadaccompany((String)mediaEventrgs.getExtra("singer")))
				{
					String toasttip = getString(R.string.screen_audio_download_cannot_download_accompany);
					Toast.makeText(ScreenSearchCategorySongs.this,toasttip, Toast.LENGTH_SHORT).show();
					if(downloadDialog !=null ){
						downloadDialog.dismiss();
						downloadDialog = null;
					}
					return;
				}
				int songId = (Integer) mediaEventrgs.getExtra("songId");
				if( db.accompanyInDownloads(songId) ){
					final String song = (String) mediaEventrgs.getExtra("song");
					ServiceManager.getAmtMediaHandler().post(new Runnable() {
						@Override
						public void run() {
							Toast toast = ToastUtil.getInstance().getToast(
									song + ScreenSearchCategorySongs.this.getString(R.string.screen_audio_download_in));
							toast.setDuration(Toast.LENGTH_SHORT);
							toast.setGravity(Gravity.CENTER, 0, 0);
							toast.show();
						}
					});
					return;
				}
				mediaEventrgs.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_ACCOMPANY);
				if (networkService.acquire(true)) {
					new Thread(new Runnable(){
						@Override
						public void run() {
							mediaEventService.onMediaUpdateEvent(mediaEventrgs);
						}}).start();
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
					Toast.makeText(ScreenSearchCategorySongs.this,getString(R.string.prompt_if_no_record_indatabase), Toast.LENGTH_SHORT).show();
				}
				
				break;
//			case CustomDialog.GO_BACK:
			default:
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
	}
	

	@Override
	public boolean refresh() {
		if (refreshFlag == true){
//			MediaApplication.logD(ScreenSearchCategorySongs.class, "queryData");
			new MediaThread(mediaHandler, MediaQueryToken.QUERY_INIT).start();
		}
		return true;
	}
	
	public boolean checkifcandownloadaccompany(String singer)
	{
		if (category.equals(getString(R.string.screen_search_category_item_hkat_name))
				|| category.equals(getString(R.string.screen_search_category_item_china_name))
				|| category.equals(getString(R.string.screen_search_category_item_love_name)))
			{
				if (singer.equals(""))
				{ 
					return false;
				}
			}
			else
			{
				return false;
			}
		return true;
	}
}
