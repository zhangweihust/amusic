package com.amusic.media.screens.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ViewFlipper;

import com.amusic.media.R;
import com.amusic.media.model.ScreenArgs;
import com.amusic.media.screens.SearchScreen;
import com.amusic.media.services.impl.ServiceManager;

public class ScreenSearch extends SearchScreen implements OnClickListener,
		OnGestureListener, OnTouchListener {

	// private LinearLayout top;
	private LinearLayout category;
	private LinearLayout singer;
	private LinearLayout song;
	private EditText content;
	private Button screen_search_start;
	private Button select_button;
	private Button baidu_button;
	private Button easou_button;
	private ImageView state_image;
	private ImageView select_image_bg;
	private int searchType = 1;
	private Dialog dialog = null;
	private int visibility = View.GONE;
	private ViewFlipper flipper;
	private View item1;
	private View item2;
	private View item3;
//	private View item4;
//	private View item5;
//	private View item6;
	private ImageView root1;
	private ImageView root2;
	private ImageView root3;
//	private ImageView root4;
//	private ImageView root5;
//	private ImageView root6;
	private List<ImageView> roots = new ArrayList<ImageView>();
	private GestureDetector gestureDetector;
	private Handler ScrollHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int count = flipper.getChildCount();
			flipper.setInAnimation(ScreenSearch.this, R.anim.help_in_rightleft);
			flipper.setOutAnimation(ScreenSearch.this,
					R.anim.help_out_rightleft);
			View view = flipper.getCurrentView();
			if (view.equals(flipper.getChildAt(count - 1))) {
				flipper.showNext();
				for (int i = 0; i < count; i++) {
					if (view.equals(flipper.getChildAt(i))) {
						roots.get(i).setImageResource(
								R.drawable.page_indicator);
						if (i == count - 1) {
							roots.get(0).setImageResource(
									R.drawable.page_indicator_focused);
						} else {
							roots.get(i + 1).setImageResource(
									R.drawable.page_indicator_focused);
						}
					}
				}
			} else {
				flipper.showNext();
				for (int i = 0; i < count; i++) {
					if (view.equals(flipper.getChildAt(i))) {
						roots.get(i).setImageResource(
								R.drawable.page_indicator);
						if (i == count - 1) {
							roots.get(0).setImageResource(
									R.drawable.page_indicator_focused);
						} else {
							roots.get(i + 1).setImageResource(
									R.drawable.page_indicator_focused);
						}
					}
				}
			}
			ScrollHandler.sendEmptyMessageDelayed(0, 5000);
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_search);
		setScreenTitle(getString(R.string.screen_home_tab_search));
		category = (LinearLayout) findViewById(R.id.screen_search_category);
		singer = (LinearLayout) findViewById(R.id.screen_search_singer);
		song = (LinearLayout) findViewById(R.id.screen_search_song);
		screen_search_start = (Button) findViewById(R.id.screen_search_start);
		content = (EditText) findViewById(R.id.screen_search_content);
		select_button = (Button) findViewById(R.id.select_image);
		baidu_button = (Button) findViewById(R.id.baidu_image);
		easou_button = (Button) findViewById(R.id.easo_image);
		state_image = (ImageView) findViewById(R.id.state_image);
		select_image_bg = (ImageView) findViewById(R.id.select_image_bg);
		// content.setOnClickListener(this);
		content.clearFocus();
		select_button.setOnClickListener(this);
		baidu_button.setOnClickListener(this);
		easou_button.setOnClickListener(this);
		state_image.setOnClickListener(this);

		category.setOnClickListener(this);
		singer.setOnClickListener(this);
		song.setOnClickListener(this);
		screen_search_start.setOnClickListener(this);

		flipper = (ViewFlipper) findViewById(R.id.screen_help_flipper);
		root1 = (ImageView) findViewById(R.id.screen_help_root_1);
		root2 = (ImageView) findViewById(R.id.screen_help_root_2);
		root3 = (ImageView) findViewById(R.id.screen_help_root_3);
//		root4 = (ImageView) findViewById(R.id.screen_help_root_4);
//		root5 = (ImageView) findViewById(R.id.screen_help_root_5);
//		root6 = (ImageView) findViewById(R.id.screen_help_root_6);
		roots.add(root1);
		roots.add(root2);
		roots.add(root3);
//		roots.add(root4);
//		roots.add(root5);
//		roots.add(root6);
		roots.get(0).setImageResource(R.drawable.page_indicator_focused);
		item1 = new View(this);
		item2 = new View(this);
		item3 = new View(this);
//		item4 = new View(this);
//		item5 = new View(this);
//		item6 = new View(this);
		item1.setBackgroundResource(R.drawable.screen_function_item1);
		item2.setBackgroundResource(R.drawable.screen_function_item2);
		item3.setBackgroundResource(R.drawable.screen_function_item3);
//		item4.setBackgroundResource(R.drawable.screen_function_item4);
//		item5.setBackgroundResource(R.drawable.screen_function_item5);
//		item6.setBackgroundResource(R.drawable.screen_function_item6);
		flipper.addView(item1);
		flipper.addView(item2);
		flipper.addView(item3);
//		flipper.addView(item4);
//		flipper.addView(item5);
//		flipper.addView(item6);
		gestureDetector = new GestureDetector(this);
		flipper.setLongClickable(true);
		flipper.setOnTouchListener(this);
	}

	@Override
	public void onClick(View v) {
		ScreenArgs args = new ScreenArgs();
		switch (v.getId()) {
		case R.id.screen_search_start: // 点击搜索
			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(this.content.getWindowToken(), 0);
			args.putExtra("content", content.getText().toString());
			args.putExtra("searchType", searchType);
			searchScreenService.show(ScreenWebViewSongs.class, args);
			break;
		case R.id.screen_search_category:
			searchScreenService.show(ScreenSearchCategories.class, args);
			break;
		case R.id.screen_search_singer:
			args.putExtra("goback", true);
			searchScreenService.show(ScreenSearchSingers.class, args);
			break;
		case R.id.screen_search_song:
			args.putExtra("goback", true);
			searchScreenService.show(ScreenSearchSongs.class, args);
			break;
		case R.id.state_image:
		case R.id.select_image:
			if (visibility == View.GONE) {
				visibility = View.VISIBLE;
			} else {
				visibility = View.GONE;
			}
			controllImage(visibility);
			break;
		case R.id.baidu_image:
			content.setHint(R.string.screen_search_baidu_content);
			state_image.setBackgroundResource(R.drawable.baidu_image);
			controllImage(View.GONE);
			visibility = View.GONE;
			searchType = 1;
			break;
		case R.id.easo_image:
			content.setHint(R.string.screen_search_easou_content);
			state_image.setBackgroundResource(R.drawable.easo_image);
			controllImage(View.GONE);
			visibility = View.GONE;
			searchType = 2;
			break;
		}
	}

	private void controllImage(int visibility) {
		select_image_bg.setVisibility(visibility);
		baidu_button.setVisibility(visibility);
		easou_button.setVisibility(visibility);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setScreenTitle(getString(R.string.screen_home_tab_search));
		content.clearFocus();
	}

	@Override
	protected void onResume() {
		super.onResume();
		setScreenTitle(getString(R.string.screen_home_tab_search));
		ServiceManager.getAmtMedia().getGoBackBtn()
				.setVisibility(View.INVISIBLE);
		content.clearFocus();
		ScrollHandler.sendEmptyMessageDelayed(0, 5000);
	}

	private List<Map<String, Object>> getData() {

		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("title",
				getString(R.string.dialog_download_music_by_web_songname));
		map.put("caseId", -1);
		list.add(map);
		map = new HashMap<String, Object>();
		map.put("title",
				getString(R.string.dialog_download_music_by_web_player));
		map.put("caseId", -1);
		list.add(map);

		return list;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		ScrollHandler.removeMessages(0);
		int count = flipper.getChildCount();
		if (e2.getX() - e1.getX() > 0) {
			flipper.setInAnimation(this, R.anim.help_in_leftright);
			flipper.setOutAnimation(this, R.anim.help_out_leftright);
			View view = flipper.getCurrentView();
			if (view.equals(flipper.getChildAt(0))) {
				flipper.showPrevious();
				for (int i = 0; i < count; i++) {
					if (view.equals(flipper.getChildAt(i))) {
						roots.get(i).setImageResource(
								R.drawable.page_indicator);
						if (i == 0) {
							roots.get(count - 1).setImageResource(
									R.drawable.page_indicator_focused);
						} else {
							roots.get(i - 1).setImageResource(
									R.drawable.page_indicator_focused);
						}
					}
				}

			} else {
				flipper.showPrevious();
				for (int i = 0; i < count; i++) {
					if (view.equals(flipper.getChildAt(i))) {
						roots.get(i).setImageResource(
								R.drawable.page_indicator);
						if (i == 0) {
							roots.get(count - 1).setImageResource(
									R.drawable.page_indicator_focused);
						} else {
							roots.get(i - 1).setImageResource(
									R.drawable.page_indicator_focused);
						}
					}
				}
			}
		} else if (e1.getX() - e2.getX() > 0) {
			flipper.setInAnimation(this, R.anim.help_in_rightleft);
			flipper.setOutAnimation(this, R.anim.help_out_rightleft);
			View view = flipper.getCurrentView();
			if (view.equals(flipper.getChildAt(count - 1))) {

				flipper.showNext();
				for (int i = 0; i < count; i++) {
					if (view.equals(flipper.getChildAt(i))) {
						roots.get(i).setImageResource(
								R.drawable.page_indicator);
						if (i == count - 1) {
							roots.get(0).setImageResource(
									R.drawable.page_indicator_focused);
						} else {
							roots.get(i + 1).setImageResource(
									R.drawable.page_indicator_focused);
						}
					}
				}

			} else {
				flipper.showNext();
				for (int i = 0; i < count; i++) {
					if (view.equals(flipper.getChildAt(i))) {
						roots.get(i).setImageResource(
								R.drawable.page_indicator);
						if (i == count - 1) {
							roots.get(0).setImageResource(
									R.drawable.page_indicator_focused);
						} else {
							roots.get(i + 1).setImageResource(
									R.drawable.page_indicator_focused);
						}
					}
				}
			}
		}
		ScrollHandler.sendEmptyMessageDelayed(0, 5000);

		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return gestureDetector.onTouchEvent(event);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		ScrollHandler.removeMessages(0);
		super.onPause();
	}
}
