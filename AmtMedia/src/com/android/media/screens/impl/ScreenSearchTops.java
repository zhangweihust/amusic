package com.android.media.screens.impl;

import android.app.Dialog;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.amusic.media.R;
import com.android.media.adapter.SearchSongAdapter;
import com.android.media.event.IMediaEventArgs;
import com.android.media.event.IMediaEventHandler;
import com.android.media.event.impl.MediaEventArgs;
import com.android.media.handler.MediaHandler;
import com.android.media.model.ScreenArgs;
import com.android.media.screens.SearchScreen;
import com.android.media.thread.MediaThread;
import com.android.media.utils.IAsynMediaQuery;

public class ScreenSearchTops extends SearchScreen implements OnClickListener, OnItemClickListener, IMediaEventHandler, IAsynMediaQuery {
	private Button previous;
	private Button next;
	private SearchSongAdapter adapter;
	private int length = 20;
	private int page = 1;
	private Cursor cursor;
	private int totalPages;
	private final MediaHandler mediaHandler;
	private String pageInfo;
	private Dialog downloadDialog;
	private RadioGroup downloadrRadioGroup;
	private Button downloadOk;
	private Button downloadCancel;
	private MediaEventArgs args;
	private String pinyin;
	private TextView pinyinText;
	private TextView pageInfoText;
	private LinearLayout keyboard;
	private LinearLayout search;
	
	private TextWatcher searchTextWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			pinyin = s.toString().trim();
			page = 1;
			new MediaThread(mediaHandler, MediaQueryToken.QUERY_INIT).start();
		}
	};

	public ScreenSearchTops() {
		mediaHandler = new MediaHandler(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_search_list);
		setScreenTitle(getString(R.string.screen_search_tops_title));
		pageInfo = getString(R.string.screen_search_page_info);
		if (savedInstanceState != null) {
			ScreenArgs args = (ScreenArgs) savedInstanceState.getSerializable("args");
			pinyin = (String) args.getExtra("pinyin");
			page = (Integer) args.getExtra("page",page);
		}
		/*keyboard = (LinearLayout) findViewById(R.id.screen_search_list_keyboard);
		search = (LinearLayout) findViewById(R.id.screen_search_filter_relativelayout);
		listView = (ListView) findViewById(R.id.screen_search_list);
		pinyinText = (TextView) findViewById(R.id.screen_search_content);
		pageInfoText = (TextView) findViewById(R.id.screen_search_page_info);
		previous = (Button) findViewById(R.id.screen_search_previous_page);
		next = (Button) findViewById(R.id.screen_search_next_page);
		pinyinText.addTextChangedListener(searchTextWatcher);
		pinyinText.setText(pinyin);
		search.setVisibility(View.GONE);
		keyboard.setVisibility(View.GONE);
		previous.setOnClickListener(this);
		next.setOnClickListener(this);
		downloadOk.setOnClickListener(this);
		downloadCancel.setOnClickListener(this);
		adapter = new SearchSongAdapter(this, null, this);
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
		mediaEventService.addEventHandler(this);*/
	}

	@Override
	public void onClick(View v) {
/*		switch (v.getId()) {
		case R.id.screen_search_previous_page:
			if (page > 1) {
				page--;
				new MediaThread(mediaHandler, MediaQueryToken.QUERY_INIT).start();
			}
			break;
		case R.id.screen_search_next_page:
			if (page < totalPages) {
				page++;
				new MediaThread(mediaHandler, MediaQueryToken.QUERY_INIT).start();
			}
			break;
		}*/
	}
	/*
	private void setStatus() {
		if (totalPages == 0) {
			page = 0;
		}
		pageInfoText.setText(String.format(pageInfo, page, totalPages));
		if (page <= 1) {
			previous.setEnabled(false);
		} else {
			previous.setEnabled(true);
		}
		if (page >= totalPages) {
			next.setEnabled(false);
		} else {
			next.setEnabled(true);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setScreenTitle(getString(R.string.screen_audio_item_songs_name));
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
	public void updateData(MediaQueryToken token) {
		switch (token) {
		case QUERY_INIT:
			adapter.changeCursor(cursor);
			setStatus();
			break;
		}
	}

	@Override
	public void queryData(MediaQueryToken token) {
		clearDownloadHandlers();
		int size=0;
		switch (token) {
		case QUERY_INIT:
			size = db.queryDictionaryAudiosCount(pinyin);
			cursor = db.queryDictionaryAudios(pinyin, length * (page - 1), length);
			totalPages = (size + length - 1) / length;
			startManagingCursor(cursor);
			break;
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		MediaEventArgs args = (MediaEventArgs) info.targetView.getTag();
		Integer status = (Integer) args.getExtra("status");
		switch (status) {
		case IMediaService.STATE_BEGIN:
			menu.add(0, MENU_DOWNLOAD_PAUSE, MENU_DOWNLOAD_PAUSE, "pause");
			menu.add(0, MENU_DOWNLOAD_CANCEL, MENU_DOWNLOAD_CANCEL, "cancel");
			menu.add(0, MENU_RE_DOWNLOAD, MENU_RE_DOWNLOAD, "re download");
			break;
		case IMediaService.STATE_FINISHED:
			menu.add(0, MENU_RE_DOWNLOAD, MENU_RE_DOWNLOAD, "re download");
			menu.add(0, MENU_DOWNLOAD_DELETE, MENU_DOWNLOAD_DELETE, "delete");
			break;
		case IMediaService.STATE_PAUSE:
			menu.add(0, MENU_DOWNLOAD_CONTINUE, MENU_DOWNLOAD_CONTINUE, "continue");
			menu.add(0, MENU_DOWNLOAD_CANCEL, MENU_DOWNLOAD_CANCEL, "cancel");
			menu.add(0, MENU_RE_DOWNLOAD, MENU_RE_DOWNLOAD, "re download");
			break;
		case IMediaService.STATE_WAIT:
			menu.add(0, MENU_DOWNLOAD_CANCEL, MENU_DOWNLOAD_CANCEL, "cancel");
			break;
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		MediaEventArgs args = (MediaEventArgs) info.targetView.getTag();
		switch (item.getItemId()) {
		case MENU_RE_DOWNLOAD:
			args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_RE_DOWNLOAD);
			downloadDialog.show();
			break;
		case MENU_DOWNLOAD_PAUSE:
			mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_PAUSE));
			break;
		case MENU_DOWNLOAD_CONTINUE:
			mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_RESUME));
			break;
		case MENU_DOWNLOAD_CANCEL:
			mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_CANCEL));
			break;
		case MENU_DOWNLOAD_DELETE:
			mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_DELETE));
			break;
		}
		return true;
	}

	@Override
	protected void onDestroy() {
		mediaEventService.removeEventHandler(this);
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
		args = (MediaEventArgs) view.getTag();
		int status = (Integer) args.getExtra("status");

		if (status == IMediaService.STATE_DEFAULT) {
			args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD);
		} else {
			final String song = (String) args.getExtra("song");
			final String singer = (String) args.getExtra("singer");
			ServiceManager.getAmtMediaHandler().post(new Runnable() {

				@Override
				public void run() {
					Toast toast = ToastUtil.getInstance().getToast(singer + "-" + song + ScreenSearchTops.this.getString(R.string.screen_audio_download_in));
					toast.setDuration(Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
			});
		}
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
		case AUDIO_DOWNLOAD_UI:
			new MediaThread(mediaHandler, MediaQueryToken.QUERY_INIT).start();
			break;
		}
		return true;
	}

	@Override
	public boolean refresh() {
		new MediaThread(mediaHandler, MediaQueryToken.QUERY_INIT).start();
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && keyboard.getVisibility() == View.VISIBLE) {
			keyboard.setVisibility(View.GONE);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}*/

	@Override
	public boolean onEvent(IMediaEventArgs args) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void queryData(MediaQueryToken token) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateData(MediaQueryToken token) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		
	}
}
