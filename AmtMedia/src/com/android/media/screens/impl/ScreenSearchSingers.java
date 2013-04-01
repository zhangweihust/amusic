package com.android.media.screens.impl;

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

import com.amusic.media.R;
import com.android.media.adapter.SearchSingerAdapter;
import com.android.media.dialog.OnScreenHint;
import com.android.media.handler.MediaHandler;
import com.android.media.model.ScreenArgs;
import com.android.media.screens.SearchScreen;
import com.android.media.services.IMediaService;
import com.android.media.services.impl.ServiceManager;
import com.android.media.thread.MediaThread;
import com.android.media.utils.IAsynMediaQuery;
import com.android.media.utils.KeyboardUtil;

public class ScreenSearchSingers extends SearchScreen implements
		OnClickListener, OnItemClickListener, IAsynMediaQuery {
	private View headerView; // 分页的布局
	private View footerView; 
	private ImageButton headerPrevious; // 上页
	private ImageButton headNext; // 下页
	private TextView headPageInfoText;
	private ImageButton footPrevious; // 上页
	private ImageButton footNext; // 下页
	private TextView footPageInfoText;
	private SearchSingerAdapter adapter;
	private int length = 20;
	private int page = 1;
	private Cursor cursor;
	private int totalPages;
	private final MediaHandler mediaHandler;
	private TextView pinyinText;
	private LinearLayout empty;
	private String pageInfo;
	private String pinyin="";
	private int tmpSize=0;
/*	private TabHost tabhost;*/
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

	public ScreenSearchSingers() {
		mediaHandler = new MediaHandler(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_search_list);
		setScreenTitle(getString(R.string.screen_search_singers_title));
		pageInfo = getString(R.string.screen_search_page_info);
		if (savedInstanceState != null) {
			ScreenArgs args = (ScreenArgs) savedInstanceState
					.getSerializable("args");
			pinyin = (String) args.getExtra("pinyin");
			page = (Integer) args.getExtra("page",page);
		}
		keyboard = (LinearLayout) findViewById(R.id.screen_search_list_keyboard);
		//bigWord = (TextView)keyboard.findViewById(R.id.screen_search_keyboard_big_word);
/*		ViewParent p1= ServiceManager.getSearchRoot().findViewById(R.id.screen_search_root_view).getParent();
		ViewParent p2= p1.getParent();
		ViewParent p3= p2.getParent();
		ViewParent p4= p3.getParent();
		tabhost= (TabHost)p4.getParent();*/
/*		tabhost = (TabHost) ScreenHome.tw;*/
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
		pinyinText.setHint(getResources().getString(R.string.screen_search_singer_filter));
		empty = (LinearLayout) findViewById(R.id.empty);
		pinyinText.addTextChangedListener(searchTextWatcher);
		pinyinText.setOnClickListener(this);
		pinyinText.setText(pinyin);
		headerPrevious.setOnClickListener(this);
		headNext.setOnClickListener(this);
		footPrevious.setOnClickListener(this);
		footNext.setOnClickListener(this);
		adapter = new SearchSingerAdapter(this, null);
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
					mOnScreenHint = OnScreenHint.makeText_keyboard(ScreenSearchSingers.this, (String) msg.obj);
					View v = mOnScreenHint.getView();
					v.setBackgroundDrawable(null);
					int x = left + ScreenSearchSingers.this.getWindowManager().getDefaultDisplay().getWidth()/20 - OnScreenHint.dip2px(ScreenSearchSingers.this, 43);
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
					mOnScreenHint = OnScreenHint.makeText_keyboard(ScreenSearchSingers.this, (String) msg.obj);
					View v_del = mOnScreenHint.getView();
					TextView tv_del = (TextView)v_del.findViewById(R.id.custom_toast_on_screen_for_keyboard);
					tv_del.setTextSize(OnScreenHint.dip2px(ScreenSearchSingers.this, 15));
					v_del.setBackgroundDrawable(null);
					int x_del = left_del + ScreenSearchSingers.this.getWindowManager().getDefaultDisplay().getWidth()/10 - OnScreenHint.dip2px(ScreenSearchSingers.this, 43);
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
					mOnScreenHint = OnScreenHint.makeText_keyboard(ScreenSearchSingers.this, (String) msg.obj);
					View v_del_long = mOnScreenHint.getView();
					TextView tv_del_long = (TextView)v_del_long.findViewById(R.id.custom_toast_on_screen_for_keyboard);
					tv_del_long.setTextSize(OnScreenHint.dip2px(ScreenSearchSingers.this, 15));
					v_del_long.setBackgroundDrawable(null);
					int x_del_long = left_del_long + ScreenSearchSingers.this.getWindowManager().getDefaultDisplay().getWidth()/10 - OnScreenHint.dip2px(ScreenSearchSingers.this, 43);
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
		new MediaThread(mediaHandler, MediaQueryToken.QUERY_INIT).start();
		listView.addHeaderView(headerView);
		listView.addFooterView(footerView);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
		
		if(mOnScreenHint_null != null){
			mOnScreenHint_null.cancel(); 
		}
		int location = ServiceManager.getAmtMedia().getResources().getDimensionPixelOffset(R.dimen.audio_info_layout_height) + ServiceManager.getAmtMedia().getResources().getDimensionPixelOffset(R.dimen.search_content_layout_height)*3/2;
		mOnScreenHint_null = OnScreenHint.makeText_Empty(this, true, ServiceManager.getAmtMedia().getResources().getString(R.string.search_bar_empty_info0), ServiceManager.getAmtMedia().getResources().getString(R.string.search_bar_empty_info4), ServiceManager.getAmtMedia().getResources().getString(R.string.search_bar_empty_info2),ServiceManager.getAmtMedia().getResources().getString(R.string.search_bar_empty_info3));   //设置toast要显示的信息
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
		setScreenTitle(getString(R.string.screen_search_singers_title));
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
		keyboard = (LinearLayout) findViewById(R.id.screen_search_list_keyboard);
		keyboard.setVisibility(View.GONE);
		setScreenTitle(getString(R.string.screen_search_singers_title));
		if(ScreenHome.tw!=null){
			ScreenHome.tw.setVisibility(View.VISIBLE);
		}
		String tvText = pinyinText.getText().toString().trim();
		if(tvText == null || tvText.equals("")){
			if(mOnScreenHint_null != null){
				mOnScreenHint_null.cancel(); 
			}
			int location = ServiceManager.getAmtMedia().getResources().getDimensionPixelOffset(R.dimen.audio_info_layout_height) + ServiceManager.getAmtMedia().getResources().getDimensionPixelOffset(R.dimen.search_content_layout_height)*3/2;
			mOnScreenHint_null = OnScreenHint.makeText_Empty(this, true, ServiceManager.getAmtMedia().getResources().getString(R.string.search_bar_empty_info0), ServiceManager.getAmtMedia().getResources().getString(R.string.search_bar_empty_info4), ServiceManager.getAmtMedia().getResources().getString(R.string.search_bar_empty_info2),ServiceManager.getAmtMedia().getResources().getString(R.string.search_bar_empty_info3));   //设置toast要显示的信息
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
		int size = 0;
		if (pinyin == null || pinyin == "" || pinyin.length() == 0) {
			size=0;
			cursor=null;
		} else {
			switch (token) {
			case QUERY_INIT:
				size = db.queryDictionarySingersCount(pinyin);
				cursor = db.queryDictionarySingers(pinyin, length * (page - 1),
						length);
				totalPages = (size + length - 1) / length;
				startManagingCursor(cursor);
				break;
			}
		}
		tmpSize=size;
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
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		String singer = (String) view.getTag();
		if(singer==null){
			return;
		}
		ScreenArgs args = new ScreenArgs();
		args.putExtra("singer", singer);
		args.putExtra("goback", true);
		searchScreenService.show(ScreenSearchSingerSongs.class, args);
	}

	@Override
	public boolean refresh() {
		new MediaThread(mediaHandler, MediaQueryToken.QUERY_INIT).start();
		return true;
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if(mOnScreenHint_null != null){
			mOnScreenHint_null.cancel(); 
		}
	}
}
