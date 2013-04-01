package com.android.media.screens.impl;

import android.app.ActivityGroup;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.amusic.media.R;
import com.android.media.MediaApplication;
import com.android.media.download.DownloadApk;
import com.android.media.model.ScreenArgs;
import com.android.media.screens.IScreen;
import com.android.media.utils.Constant;

public class ScreenHome extends TabActivity implements IScreen, OnTabChangeListener {
	private TabHost mTabHost;
	private GestureDetector gestureDetector;
	private Intent mScreenAudioRoot;
	private Intent mScreenKMediaRoot;
	private Intent mScreenRecordRoot;
	private Intent mScreenSearchRoot;
	private static final int SWIPE_MIN_DISTANCE = 180;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200; 
	public static String tabAudio = "audio_tab";
	public static String tabKMedia = "kmedia_tab";
	public static String tabRecord = "record_tab";
	public static String tabSearch = "search_tab";
	public static String tabId = tabAudio;
	private View tabSpecView;
	private IScreen currentActivity;
	private TextView tabspectxt;
	private ImageView icon;
	private TabSpec tabSpec;
	public static View tw;
	int currentView = 0; 
	private Animation slideLeftIn;
	private Animation slideLeftOut;
	private Animation slideRightIn;
	private Animation slideRightOut;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_home);
		if(Constant.IS_AUTO_UPDATE) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					Looper.prepare();
					DownloadApk downloadApk = new DownloadApk(ScreenHome.this, Looper.myLooper());
					downloadApk.updateApk(DownloadApk.AUTO_UPDATE, null);
					Looper.loop();
				}
			}).start();
		}
		this.mScreenAudioRoot = new Intent(this, ScreenAudioRoot.class);
		this.mScreenKMediaRoot = new Intent(this, ScreenKMediaRoot.class);
		this.mScreenRecordRoot = new Intent(this, ScreenRecordRoot.class);
		this.mScreenSearchRoot = new Intent(this, ScreenSearchRoot.class);
		this.mScreenAudioRoot.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		this.mScreenKMediaRoot.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		this.mScreenRecordRoot.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		this.mScreenSearchRoot.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		this.mTabHost = getTabHost();
		this.mTabHost.setOnTabChangedListener(this);
		tw = getTabWidget();
		initTab();
		if (savedInstanceState != null) {
			ScreenArgs args = (ScreenArgs) savedInstanceState.getSerializable("args");
			tabId = (String) args.getExtra("tabId");
			currentView = getCurrentViewById(tabId);
			this.mTabHost.setCurrentTabByTag(tabId);
		}
		slideLeftIn = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
		slideLeftOut = AnimationUtils.loadAnimation(this, R.anim.slide_left_out);
		slideRightIn = AnimationUtils.loadAnimation(this, R.anim.slide_right_in);
		slideRightOut = AnimationUtils.loadAnimation(this, R.anim.slide_right_out);
		gestureDetector = new GestureDetector(new MyGestureDetector());
		
	}

	public boolean onTouchEvent(MotionEvent event) {
		//MediaApplication.logD(ScreenHome.class, "onTouchEvent:" + event.getAction());
		return gestureDetector.onTouchEvent(event);
	}
	
	private int getCurrentViewById(String tabId){
		if(tabAudio.equals(tabId)){
			return 0;
		} else if(tabKMedia.equals(tabId)){
			return 1;
		} else if(tabRecord.equals(tabId)){
			return 2;
		} else if(tabSearch.equals(tabId)){
			return 3;
        } else {
        	return 0;
        }
	}
	
	


	class MyGestureDetector extends SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
//			MediaApplication.logD(ScreenHome.class, "onTouchEvent:onFling" + e1);
//			MediaApplication.logD(ScreenHome.class, "onTouchEvent:onFling" + e2);
//			MediaApplication.logD(ScreenHome.class, "onTouchEvent:onFling" + velocityX);
//			MediaApplication.logD(ScreenHome.class, "onTouchEvent:onFling" + velocityY);
			TabHost tabHost = getTabHost();
			try {
				if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH){
					MediaApplication.logD(ScreenHome.class, "Exception");
					return false;
				}
				// right to left swipe
				if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					MediaApplication.logD(ScreenHome.class, "right");
					if (currentView == 3) {
						currentView = 0;
					} else {
						currentView++;
					}
					tabHost.getCurrentView().startAnimation(slideLeftOut);
					tabHost.setCurrentTab(currentView);
					tabHost.getCurrentView().startAnimation(slideLeftIn);
					return true;
				} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					MediaApplication.logD(ScreenHome.class, "left");
					if (currentView == 0) {
						currentView = 3;
					} else {
						currentView--;
					}
					tabHost.getCurrentView().startAnimation(slideRightOut);
					tabHost.setCurrentTab(currentView);
					tabHost.getCurrentView().startAnimation(slideRightIn);
					return true;
				}
			} catch (Exception e) {
				// nothing
				MediaApplication.logD(ScreenHome.class, "Exception");
			}
			return false;
		}
	}

	private void initTab() {
		mTabHost.addTab(buildTabSpec(tabAudio, R.string.screen_home_tab_audio, R.drawable.screen_tabhost_bottom_audio_selector, this.mScreenAudioRoot));
		mTabHost.addTab(buildTabSpec(tabKMedia, R.string.screen_home_tab_kmedia, R.drawable.screen_tabhost_bottom_kmedia_selector, this.mScreenKMediaRoot));
		mTabHost.addTab(buildTabSpec(tabRecord, R.string.screen_home_tab_record, R.drawable.screen_tabhost_bottom_record_selector, this.mScreenRecordRoot));
		mTabHost.addTab(buildTabSpec(tabSearch, R.string.screen_home_tab_search, R.drawable.screen_tabhost_bottom_search_selector, this.mScreenSearchRoot));
	}

	private TabSpec buildTabSpec(String tag, int stringId, int iconId, final Intent content) {
		tabSpecView = (LinearLayout) LayoutInflater.from(this).inflate(
				R.layout.screen_home_tabspec, null);
		tabspectxt = (TextView) tabSpecView.findViewById(R.id.tabspec_text);
		icon = (ImageView) tabSpecView.findViewById(R.id.tabspec_icon);
		icon.setImageResource(iconId);
		tabspectxt.setText(stringId);
		tabSpec = this.mTabHost.newTabSpec(tag).setIndicator(tabSpecView).setContent(content);
		return tabSpec;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		ScreenArgs args = new ScreenArgs();
		args.putExtra("tabId", tabId);
		outState.putSerializable("args", args);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		this.mTabHost.setCurrentTabByTag(tabId);
		currentView = getCurrentViewById(tabId);
		currentActivity = (IScreen) getLocalActivityManager().getCurrentActivity();
		getAmtCurrentActivity();
		currentActivity.refresh();
	}


	@Override
	public boolean hasMenu() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean currentable() {
		return false;
	}

	private void getAmtCurrentActivity() {
		if (!currentActivity.currentable()) {
			currentActivity = (IScreen) ((ActivityGroup) currentActivity).getLocalActivityManager().getCurrentActivity();
			getAmtCurrentActivity();
		}
	}

	@Override
	public boolean refresh() {
		return false;
	}

	@Override
	public void onTabChanged(String tabId) {
		this.tabId = tabId;
		currentView = getCurrentViewById(tabId);
		tw.setSoundEffectsEnabled(true);
		tw.performHapticFeedback(HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);  
		//tw.playSoundEffect(SoundEffectConstants.CLICK);
	}

	@Override
	public boolean changMenuAdapter() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isMenuChanged() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public View getTabhose(){
		return tw;
	}
}
