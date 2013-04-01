package com.android.media;

import java.io.File;
import java.util.Stack;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amusic.media.R;



import com.android.media.adapter.MenuContentAdapter;
import com.android.media.dialog.DialogEditLyric;
import com.android.media.dialog.DialogEditLyricRadio;
import com.android.media.dialog.DialogHideOrExit;
import com.android.media.dialog.LyricColorPreference;
import com.android.media.dialog.LyricModify;
import com.android.media.dialog.SreenAudioSearchLyric;
import com.android.media.event.IMediaEventArgs;
import com.android.media.event.IMediaEventHandler;
import com.android.media.lyric.player.LyricPlayer;
import com.android.media.lyric.render.DesktopKTVView;
import com.android.media.model.MenuItem;
import com.android.media.model.ScreenArgs;
import com.android.media.provider.MediaDatabaseHelper;
import com.android.media.provider.MediaManagerDB;
import com.android.media.screens.IScreen;
import com.android.media.screens.IScreen.ScreenType;
import com.android.media.screens.impl.ScreenAudioAlbumSongs;
import com.android.media.screens.impl.ScreenAudioDownloadManager;
import com.android.media.screens.impl.ScreenAudioFavorites;
import com.android.media.screens.impl.ScreenAudioPlayer;
import com.android.media.screens.impl.ScreenAudioPlaylistSongs;
import com.android.media.screens.impl.ScreenAudioRecentlySongs;
import com.android.media.screens.impl.ScreenAudioSingerSongs;
import com.android.media.screens.impl.ScreenAudioSongError;
import com.android.media.screens.impl.ScreenAudioSongs;
import com.android.media.screens.impl.ScreenEqualizer;
import com.android.media.screens.impl.ScreenHelp;
import com.android.media.screens.impl.ScreenHome;
import com.android.media.screens.impl.ScreenKMediaPlayer;
import com.android.media.screens.impl.ScreenLogo;
import com.android.media.screens.impl.ScreenPlayMode;
import com.android.media.screens.impl.ScreenScan;
import com.android.media.screens.impl.ScreenSkin;
import com.android.media.screens.impl.ScreenSuggestionFeedback;
import com.android.media.screens.impl.ScreenTimingExit;
import com.android.media.screens.impl.ScreenVersion;
import com.android.media.screens.impl.SoftSetting;
import com.android.media.services.IAmtScreenService;
import com.android.media.services.IMediaEventService;
import com.android.media.services.IMediaPlayerService;
import com.android.media.services.IMediaService;
import com.android.media.services.impl.DesktopLyricService;
import com.android.media.services.impl.MediaPlayerService;
import com.android.media.services.impl.ServiceManager;
import com.android.media.utils.Constant;
import com.android.media.utils.PreferencesUtil;
import com.android.media.view.CustomDialog;

public class AmtMedia extends ActivityGroup implements IMediaEventHandler,
		View.OnClickListener {

	private final IAmtScreenService amtScreenService;
	private final IMediaService mediaService;
	private final Handler handler;
	private TextView activityTitle;
	private IMediaEventService mediaEventService;
	private View goPlayerBtn;
	private LinearLayout editLyricLayout;
	private ImageButton goBackBtn;
	private PopupWindow popupMenuWindow;
	private GridView gridView;
	public static final String XML_NAME = "ScreenBackground";
	public static final String XML_BACKGROUND = "background";
	private int titleIndex;
	private LinearLayout rootView;
	private TextView userfull, tools, help;
	private ImageView userfullImg, toolsImg, helpImg;
	private DesktopLyricService desktopLyric;
	private MenuContentAdapter[] menuContentAdapters = new MenuContentAdapter[3];
	private IScreen currentActivity;
    public static int s_goPlayerBtn_click_num = -1;
    private WindowManager wm = (WindowManager) MediaApplication.getContext().getSystemService("window");
    private SharedPreferences preferences;
    private Drawable background;
    private SreenAudioSearchLyric searchLyric;
    private DialogEditLyricRadio lyricModify;
	private SdcardStateReceiver mSdcardStateReceiver;
	private LyricPlayer lyricplayer = null;

	public AmtMedia() {
		handler = new Handler();
		amtScreenService = ServiceManager.getAmtScreenService();
		mediaService = ServiceManager.getMediaService();
		desktopLyric = DesktopLyricService.getInstance();
		preferences = MediaApplication.getInstance().getSharedPreferences(XML_NAME,Context.MODE_WORLD_WRITEABLE);
	}

	private boolean isHasSD(){
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.amt_media);
		if(!isHasSD()){
//			Toast.makeText(this, "请插入sd卡，否则无法使用本程序",Toast.LENGTH_LONG).show();
		}
//		makeAdapters();
		initPopupMenu();
		ServiceManager.setAmtMedia(this);
		if (ServiceManager.isStarted()) {
			amtScreenService.show(ScreenHome.class);
		} else {
			if (!ServiceManager.start()) {
				ServiceManager.exit();
				return;
			}
			 amtScreenService.show(ScreenLogo.class, false, View.GONE);
		}
		mediaEventService = ServiceManager.getMediaEventService();
		mediaEventService.addEventHandler(this);
		activityTitle = (TextView) findViewById(R.id.screen_top_play_control_activityTitle);
		rootView = (LinearLayout) findViewById(R.id.amt_media_linearLayout);
		//皮肤加载选择:
		String skinPath = preferences.getString(XML_BACKGROUND, MediaApplication.DEFAULT_SKIN);
		if (skinPath.equals(MediaApplication.DEFAULT_SKIN)) {
			background =  getResources().getDrawable(R.drawable.style_brilliant_starlight);
		} else {
			background =  Drawable.createFromPath(skinPath);
			if (background == null) {
					background =  getResources().getDrawable(R.drawable.style_brilliant_starlight);
			}
		}
		MediaApplication.color_highlight = PreferencesUtil.getSkinFontForegroundColorSP();
		MediaApplication.color_normal = PreferencesUtil.getSkinFontBackgroundColorSP();
		Constant.LYRICFOREGROUNDCOLOR = PreferencesUtil.getLyricFontForegroundColorSP();
		rootView.setBackgroundDrawable(background);
		goPlayerBtn = (View) findViewById(R.id.screen_top_play_control_go_to_player);
		
		lyricplayer = ServiceManager.getMediaplayerService().getLyricplayer();
		SharedPreferences sharedata = getSharedPreferences("lastsong", 0);
		String methodName = sharedata.getString("methodName", null);
		if (methodName == null || methodName.equals("")) {
			MediaApplication.getInstance().setVisible(false);
			goPlayerBtn.setVisibility(View.INVISIBLE);
		} else {
			MediaApplication.getInstance().setVisible(true);
		}
		goPlayerBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (!ServiceManager.isPlayed) {
					SharedPreferences sharedata = getSharedPreferences("lastsong", 0);
					String methodName = sharedata.getString("methodName", null);
					int id = sharedata.getInt("id", 0);
					int position = sharedata.getInt("position", 0);
					String listId = sharedata.getString("listId", null);				
					if (methodName == null || s_goPlayerBtn_click_num != -1) {
						amtScreenService.show(ScreenAudioPlayer.class, View.GONE);
						return;
					}
					
					MediaManagerDB db = ServiceManager.getMediaService().getMediaDB();
					
					try {
						ScreenArgs args = new ScreenArgs();
						
						args.putExtra("screenType", ScreenType.TYPE_AUDIO);
						args.putExtra("id", (int) id);
						args.putExtra("position", position);
						args.putExtra("screenId", ScreenAudioSongs.class.getCanonicalName());
						args.putExtra("changeHighlight", IMediaPlayerService.DONT_CHANGE_HIGHLIGHT);
						Cursor cursor_test = db.querySongById(id);
						boolean isFileExist = true;
						if (cursor_test != null && cursor_test.getCount() != 0) {
						    cursor_test.moveToFirst();
						    String filepath = cursor_test.getString(cursor_test
									.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_FILEPATH));
						    File file = new File(filepath);
							if (!file.exists()) {
								isFileExist = false;
							}
						}
						
						if (methodName.equals("queryAudios")){
							Cursor mCursor = db.queryAudios();
							position = db.queryPositionById(mCursor, id);
							if (position != -1) {
								args.putExtra("position", position);
								if (isFileExist) {
								    s_goPlayerBtn_click_num++;
								}
							}
							args.putExtra("screenId", ScreenAudioSongs.class.getCanonicalName());
							ServiceManager.methodName = methodName;
							ServiceManager.getMediaplayerService().changeCorsor(mCursor, IMediaPlayerService.MEDIA_MODEL_LOCAL);	
						} else if(methodName.equals("queryPlaylistAudios")) {
							int listidTemp = Integer.parseInt(listId); 
							Cursor mCursor = db.queryPlaylistAudios(listidTemp);
							position = db.queryPositionById(mCursor, id);
							if (position != -1) {
//								args.putExtra("position", position);
								if (isFileExist) {
								    s_goPlayerBtn_click_num++;
								}
							}
							args.putExtra("screenId", ScreenAudioPlaylistSongs.class.getCanonicalName());
							args.putExtra("position", position);
							ServiceManager.methodName = methodName;
							ServiceManager.listId = listId;
							ServiceManager.getMediaplayerService().changeCorsor(mCursor, IMediaPlayerService.MEDIA_MODEL_LOCAL);
						} else if(methodName.equals("querySingerAudios")) {
							Cursor mCursor = db.querySingerAudios(listId);
							position = db.queryPositionById(mCursor, id);
							if (position != -1) {
								args.putExtra("position", position);
								if (isFileExist) {
								    s_goPlayerBtn_click_num++;
								}
							}
							args.putExtra("screenId", ScreenAudioSingerSongs.class.getCanonicalName());
							ServiceManager.methodName = methodName;
							ServiceManager.listId = listId;
							ServiceManager.getMediaplayerService().changeCorsor(mCursor, IMediaPlayerService.MEDIA_MODEL_LOCAL);
	                    } else if(methodName.equals("queryAlbumAudios")) {
	                    	Cursor mCursor = db.queryAlbumAudios(listId);
	                    	position = db.queryPositionById(mCursor, id);
							if (position != -1) {
								args.putExtra("position", position);
								if (isFileExist) {
								    s_goPlayerBtn_click_num++;
								}
							}
							args.putExtra("screenId", ScreenAudioAlbumSongs.class.getCanonicalName());
							ServiceManager.methodName = methodName;
							ServiceManager.listId = listId;
	                    	ServiceManager.getMediaplayerService().changeCorsor(mCursor, IMediaPlayerService.MEDIA_MODEL_LOCAL);
						} else if(methodName.equals("queryFavoriteAudios")) {
							Cursor mCursor = db.queryFavoriteAudios();
							position = db.queryPositionById(mCursor, id);
							if (position != -1) {
								args.putExtra("position", position);
								if (isFileExist) {
								    s_goPlayerBtn_click_num++;
								}
							}
							args.putExtra("screenId", ScreenAudioFavorites.class.getCanonicalName());
							ServiceManager.methodName = methodName;
							ServiceManager.getMediaplayerService().changeCorsor(mCursor, IMediaPlayerService.MEDIA_MODEL_LOCAL);
						} else if(methodName.equals("queryRecentlyAudios")) {
							Cursor mCursor = db.queryRecentlyAudios();
							position = db.queryPositionById(mCursor, id);
							if (position != -1) {
								args.putExtra("position", position);
								if (isFileExist) {
								    s_goPlayerBtn_click_num++;
								}
							}
							args.putExtra("screenId", ScreenAudioRecentlySongs.class.getCanonicalName());
							ServiceManager.methodName = methodName;
							ServiceManager.getMediaplayerService().changeCorsor(mCursor, IMediaPlayerService.MEDIA_MODEL_LOCAL);
						} else {
							args = null;
						}

						amtScreenService.show(ScreenAudioPlayer.class, args, View.GONE);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						amtScreenService.show(ScreenAudioPlayer.class, View.GONE);
						e.printStackTrace();
					}
				} else {
				    amtScreenService.show(ScreenAudioPlayer.class, View.GONE);
				}
			}
		});

		goBackBtn = (ImageButton) findViewById(R.id.screen_top_play_control_back);
		goBackBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				IScreen currentScreen = (IScreen) ServiceManager.getAmtMedia().getLocalActivityManager().getCurrentActivity();
				if(currentScreen.getClass().getCanonicalName().equals(ScreenHome.class.getCanonicalName())){
					if (ScreenHome.tabId.equals(ScreenHome.tabAudio)) {
						if (!ServiceManager.getAudioScreenService().goback()){
//							MediaApplication.logD(AmtMedia.class, "ServiceManager.getAudioScreenService().goback()" );
							onBackPressed();
						}
					} else if (ScreenHome.tabId.equals(ScreenHome.tabKMedia)) {
						if (!ServiceManager.getKMediaScreenService().goback())
							onBackPressed();
					} else if (ScreenHome.tabId.equals(ScreenHome.tabRecord)) {
						if (!ServiceManager.getRecordScreenService().goback())
							onBackPressed();
					} else if (ScreenHome.tabId.equals(ScreenHome.tabSearch)) {
						if (!ServiceManager.getSearchScreenService().goback())
							onBackPressed();
						// if (ScreenSearch.keyboard != null
						// && ScreenSearch.keyboard.getVisibility() ==
						// View.VISIBLE) {
						// ScreenSearch.keyboard.setVisibility(View.GONE);
						// }
						View tabhost = ScreenHome.tw;
						if (tabhost != null) {
							tabhost.setVisibility(View.VISIBLE);
						}
					}
				} else {
						onBackPressed();
				}
			}
		});
		registerSdcardStateReceiver();
	}

	@Override
	protected void onPause() {
		// // TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		Constant.IS_DESKTOP_LYRIC_EXIT = false;
		if (desktopLyric.getDesktopView() != null) {
			if(Constant.IS_SHOW_DESKTOP_LYRIC){
				desktopLyric.setVisible(View.VISIBLE);
			}
			desktopLyric.setAmtMedia(false);
			if(MediaPlayerService.typefinal == ScreenType.TYPE_RECORD || MediaPlayerService.typefinal == ScreenType.TYPE_KMEDIA){
				desktopLyric.setVisible(View.GONE);
			}
			if (!ServiceManager.getMediaplayerService().getMediaPlayer()
					.isPlaying()) {
				desktopLyric.setVisible(View.GONE);
			}
		}
		
		//System.out.println("onStop");
		ScreenKMediaPlayer  skm = ServiceManager.getkmediaPlayer();
		if(skm != null){
			MediaApplication.logD(skm.getClass(), "onPause");
			if (ScreenKMediaPlayer.flagMusic == ScreenKMediaPlayer.STATE_PLAY) {
				ScreenKMediaPlayer.homeDownPause = true;
				skm.pause();
			}
		}
		super.onStop();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		desktopLyric.setVisible(View.GONE);
		desktopLyric.setAmtMedia(true);
		View controllView = ((DesktopKTVView)(desktopLyric.getDesktopView())).getControllView();
		View settingView = ((DesktopKTVView)(desktopLyric.getDesktopView())).getSettingView();
		View closeDesktopLyricView = ((DesktopKTVView)(desktopLyric.getDesktopView())).getCloseDesktopLyricView();
		if(controllView != null){
			wm.removeView(controllView);
			((DesktopKTVView)(desktopLyric.getDesktopView())).setControllView(null);
		}
		if(settingView != null){
			wm.removeView(settingView);
			((DesktopKTVView)(desktopLyric.getDesktopView())).setSettingView(null);
		}
		if(closeDesktopLyricView != null){
			wm.removeView(closeDesktopLyricView);
			((DesktopKTVView)(desktopLyric.getDesktopView())).setCloseDesktopLyricView(null);
		}
		
		super.onResume();
		
		ScreenKMediaPlayer skm = ServiceManager.getkmediaPlayer();
		if(skm != null){
			MediaPlayerService.isKMdieaOrRecord = true;
			if(ScreenKMediaPlayer.homeDownPause == true){
				skm.play();
				ScreenKMediaPlayer.homeDownPause = false;
			}
			MediaApplication.logD(skm.getClass(), "onResume");
			if(skm.wakeLockThread != null){
				skm.wakeLockThread.acquire();
			}
			if(skm.wakeLockSCreen != null){
				skm.wakeLockSCreen.acquire();
			}
		}
		
	}

	@Override
	public void onBackPressed() {
		boolean flag = false;
		if(Constant.WHICH_PLAYER == 2){
			flag = true;
			ServiceManager.getkmediaPlayer().showDialog();
		}
		if(Constant.WHICH_PLAYER == 3){
			flag = true;
			ServiceManager.getRecordPlayer().backPressed();
		}
		if(flag){
			return;
		}

		currentActivity =  (IScreen) getLocalActivityManager()
		.getCurrentActivity();
		getAmtCurrentActivity();
		boolean lyricSpeedFlag = false;
		if("ScreenCreateLyric".equals(currentActivity.getClass().getSimpleName())){
			lyricSpeedFlag = true;
			Dialog dialog;
			final CustomDialog.Builder customBuilder = new CustomDialog.Builder(this);
			customBuilder.setTitle(getResources().getString(R.string.editor_lyric_prompt))
			.setWhichViewVisible(CustomDialog.contentIsTextView).setMessage(getResources().getString(R.string.screen_create_lyric_cancel_info))
			.setPositiveButton(getResources().getString(R.string.screen_lyric_speed_modify_yes), 
	            		new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
		            	dialog.dismiss();
		            	amtScreenService.goback();
					}
	            })
	            .setNegativeButton(getResources().getString(R.string.screen_lyric_speed_modify_no), 
	            		new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int which) {
	                	dialog.dismiss();
	    			}
	            });
			dialog = customBuilder.create();
			dialog.show();
		}
		if(lyricSpeedFlag) {
			return ;
		}
		if(amtScreenService.getBackList().isEmpty()){
			return;
		}
		if (!amtScreenService.goback()) {
				DialogHideOrExit dialogHideOrExit = new DialogHideOrExit();
				dialogHideOrExit.show();
		}
	}

	public Handler getHandler() {
		return handler;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		ServiceManager.makeAdapters();
		menuContentAdapters = ServiceManager.getMenuContentAdapters();
		if (event.getKeyCode() == KeyEvent.KEYCODE_MENU
				&& event.getAction() == KeyEvent.ACTION_UP) {
			currentActivity =  (IScreen) getLocalActivityManager()
			.getCurrentActivity();
			getAmtCurrentActivity();
			if("ScreenAudioPlayer".equals(currentActivity.getClass().getSimpleName()) || "ScreenKMediaPlayer".equals(currentActivity.getClass().getSimpleName())){
				if(Constant.WHICH_LYRIC_PLAYER == 2 && lyricplayer.getLyricsInfo() != null){
					MenuContentAdapter[] menuContentAdapters = ServiceManager.getMenuContentAdapters();
					menuContentAdapters[Constant.MenuConstant.userfull].getMenuData().add(Constant.MenuConstant.menu_item_search, 
							new MenuItem(R.drawable.menu_item_search, MediaApplication.getContext().getResources()
									.getString(R.string.screen_home_menu_search)));
//					menuContentAdapters[Constant.MenuConstant.userfull].getMenuData().add(Constant.MenuConstant.menu_item_delete, 
//							new MenuItem(R.drawable.menu_item_delete, MediaApplication.getContext().getResources()
//									.getString(R.string.screen_home_menu_delete)));
					menuContentAdapters[Constant.MenuConstant.userfull].getMenuData().add(Constant.MenuConstant.menu_item_modify, 
							new MenuItem(R.drawable.menu_item_modify, MediaApplication.getContext().getResources()
									.getString(R.string.screen_home_menu_lyric_tools)));
					
					menuContentAdapters[Constant.MenuConstant.userfull].getMenuData().add(Constant.MenuConstant.menu_item_song_problem -1, 
							new MenuItem(R.drawable.menu_item_song_problem, MediaApplication.getContext().getResources()
									.getString(R.string.screen_home_menu_song_problem)));
					
					menuContentAdapters[Constant.MenuConstant.userfull].getMenuData().add(Constant.MenuConstant.menu_item_color - 1, 
							new MenuItem(R.drawable.menu_item_color, MediaApplication.getContext().getResources()
									.getString(R.string.screen_home_menu_color)));
					
					if(Integer.parseInt(Build.VERSION.SDK)>=9 &&  MediaPlayerService.eqInitState == true){
						menuContentAdapters[Constant.MenuConstant.tools].getMenuData().add(Constant.MenuConstant.menu_item_share, 
								new MenuItem(R.drawable.menu_item_share, MediaApplication.getContext().getResources()
										.getString(R.string.screen_home_menu_share)));
					}else{
						menuContentAdapters[Constant.MenuConstant.tools].getMenuData().add(Constant.MenuConstant.menu_item_share - 1, 
								new MenuItem(R.drawable.menu_item_share, MediaApplication.getContext().getResources()
										.getString(R.string.screen_home_menu_share)));
					}
					
					menuContentAdapters[Constant.MenuConstant.userfull].getMenuData().remove(Constant.MenuConstant.menu_item_scanner);
					openOptionsMenu();
					return true;
				}
			}
			if (((IScreen) currentActivity).hasMenu()) {
				((Activity) currentActivity).openOptionsMenu();
			} else{
				if (((IScreen) currentActivity).isMenuChanged()){
					currentActivity.changMenuAdapter();
				}
				openOptionsMenu();
			}
			return true;
		} 
		return super.dispatchKeyEvent(event);
	}
	
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		currentActivity =  (IScreen) getLocalActivityManager().getCurrentActivity();
		if(currentActivity.onTouchEvent(event)){
			return true;
		} else {
			return super.dispatchTouchEvent(event);
		}
	}

	private void getAmtCurrentActivity() {
		if (!currentActivity.currentable()) {
			currentActivity = (IScreen) ((ActivityGroup)currentActivity)
					.getLocalActivityManager().getCurrentActivity();
			getAmtCurrentActivity();
		}
	}

	@Override
	public boolean onEvent(IMediaEventArgs args) {
		switch (args.getMediaUpdateEventTypes()) {
		case SCREEN_TITLE_REFRESH:
			String title = (String) args.getExtra("screenTitle");
			activityTitle.setText(title);
			break;
		}
		return true;
	}

	@Override
	protected void onDestroy() {

		if (mediaEventService != null) {
			mediaEventService.removeEventHandler(this);
		}
		if (popupMenuWindow != null) {
			if (popupMenuWindow.isShowing())
				popupMenuWindow.dismiss();
		}
		unregisterReceiver(mSdcardStateReceiver);
		super.onDestroy();
	}

	private void initPopupMenu() {
		LayoutInflater inflater = LayoutInflater.from(this);
		View content = inflater.inflate(R.layout.menu_layout, null);
		LinearLayout layout = (LinearLayout) content
				.findViewById(R.id.menu_layout);
		layout.setFocusable(true);
		layout.setFocusableInTouchMode(true);
		layout.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				AudioManager audioManager = (AudioManager) AmtMedia.this
                .getSystemService(AmtMedia.this.AUDIO_SERVICE);
				int currVolum = 0;
				int maxVolum = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
				if (keyCode == KeyEvent.KEYCODE_MENU && event.getAction() == KeyEvent.ACTION_UP) {
					popupMenuWindow.dismiss();
				}else if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && event.getAction() == KeyEvent.ACTION_UP){
					currVolum = audioManager
                    .getStreamVolume(AudioManager.STREAM_MUSIC);
					currVolum--;
					if (currVolum <= 0) {
						currVolum = 0;
					}
					audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currVolum,
                            1);
				}else if(keyCode == KeyEvent.KEYCODE_VOLUME_UP && event.getAction() == KeyEvent.ACTION_UP){
					currVolum = audioManager
                    .getStreamVolume(AudioManager.STREAM_MUSIC);
					currVolum++;
                 if (currVolum >= maxVolum) {
                	 currVolum = maxVolum;
                    }
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currVolum,
                            1);
				}
				return false;
			}
		});
		userfull = (TextView) content.findViewById(R.id.menu_userfull);
		userfullImg = (ImageView) content.findViewById(R.id.menu_userfull_img);
		tools = (TextView) content.findViewById(R.id.menu_tools);
		toolsImg = (ImageView) content.findViewById(R.id.menu_tools_img);
		help = (TextView) content.findViewById(R.id.menu_hlep);
		helpImg = (ImageView) content.findViewById(R.id.menu_hlep_img);
		userfull.setOnClickListener(this);
		userfull.setText(R.string.screen_menu_userfull);
		tools.setOnClickListener(this);
		tools.setText(R.string.screen_menu_tools);
		help.setOnClickListener(this);
		help.setText(R.string.screen_menu_help);
		gridView = (GridView) content.findViewById(R.id.menu_gridview);
		gridView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				int viewId = (Integer) ((RelativeLayout) view).getChildAt(1)
						.getTag();

				switch (viewId) {
				case R.drawable.menu_item_scanner:
					MediaApplication.getInstance().setContain(false);
					amtScreenService.show(ScreenScan.class);
					break;
				case R.drawable.menu_item_search:
					searchLyric = new SreenAudioSearchLyric();
					searchLyric.show();
					break;
				case R.drawable.menu_item_delete:
					break;
				case R.drawable.menu_item_modify:
					lyricModify = new DialogEditLyricRadio();
					lyricModify.show();
					break;
				case R.drawable.menu_item_song_problem:
					ScreenAudioSongError songErrorDialog = new ScreenAudioSongError(AmtMedia.this);
					songErrorDialog.show();
					break;
				case R.drawable.menu_item_skin:
					amtScreenService.show(ScreenSkin.class);
					break;
				case R.drawable.menu_item_color:
					LyricColorPreference LyricColorPreference = new LyricColorPreference(AmtMedia.this);
					LyricColorPreference.show();
					break;
				case R.drawable.menu_item_exit:
					ServiceManager.exit();
					break;
				case R.drawable.menu_item_timing:
					amtScreenService.show(ScreenTimingExit.class);
					break;
				case R.drawable.menu_item_mode:
					amtScreenService.show(ScreenPlayMode.class);
					break;
				case R.drawable.menu_item_download:
					amtScreenService.show(ScreenAudioDownloadManager.class);
					break;
				case R.drawable.menu_item_settings:
					amtScreenService.show(SoftSetting.class);
					break;
				case R.drawable.menu_item_version:
					amtScreenService.show(ScreenVersion.class);
					break;
				case R.drawable.menu_item_suggestion:
					amtScreenService.show(ScreenSuggestionFeedback.class);
					break;
				case R.drawable.menu_item_function:
					amtScreenService.show(ScreenHelp.class);
					break;
				case R.drawable.menu_item_equalizer:
					amtScreenService.show(ScreenEqualizer.class);
					break;
				case R.drawable.menu_item_share:
					goToShare();
					break;
				}
				popupMenuWindow.dismiss();
			}
		});

		popupMenuWindow = new PopupWindow(layout, LayoutParams.FILL_PARENT,
				this.getResources().getDimensionPixelSize(R.dimen.linearlayout_layout_height));
//		popupMenuWindow = new PopupWindow(layout, LayoutParams.FILL_PARENT , LayoutParams.WRAP_CONTENT);	
		popupMenuWindow.setFocusable(true);
		popupMenuWindow.setOutsideTouchable(true);
		popupMenuWindow.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.menu_bg));
		popupMenuWindow.update();
	}

	
	 private void goToShare() {
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			//System.out.println("typefinal is : " + MediaPlayerService.typefinal);
			String textMessage = "";
			String filename = ServiceManager.getMediaplayerService().getMediaPlayer().getAudioFilePath();
			filename = filename.substring(filename.lastIndexOf("/")+1);
			if(ScreenType.TYPE_AUDIO == MediaPlayerService.typefinal){
				textMessage = getResources().getString(R.string.menu_share_audio).toString() + filename + getResources().getString(R.string.menu_share_info).toString();
			}else if(ScreenType.TYPE_KMEDIA == MediaPlayerService.typefinal){
				textMessage = getResources().getString(R.string.menu_share_kmedia).toString() + filename + getResources().getString(R.string.menu_share_info).toString();
			}else if(ScreenType.TYPE_RECORD == MediaPlayerService.typefinal){
				textMessage = getResources().getString(R.string.menu_share_record).toString() + filename + getResources().getString(R.string.menu_share_info).toString();
			}else{
				textMessage = getResources().getString(R.string.menu_share_others).toString() + filename + getResources().getString(R.string.menu_share_info).toString();
			}
			intent.putExtra(Intent.EXTRA_TEXT, textMessage);
			AmtMedia.this.startActivity(Intent.createChooser(intent,
					getResources().getString(R.string.screen_home_menu_share).toString()));
		}
	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		IScreen currentActivity = (IScreen) getLocalActivityManager()
				.getCurrentActivity();
		String currentName = currentActivity.getClass().getCanonicalName();
		currentName = currentName.substring(currentName.lastIndexOf(".") + 1);
		// if(!"ScreenLogo".equals(currentName)){
		if (popupMenuWindow != null) {
			if (popupMenuWindow.isShowing()) {
				popupMenuWindow.dismiss();
			} else {
				userfullImg.setVisibility(View.VISIBLE);
				toolsImg.setVisibility(View.INVISIBLE);
				helpImg.setVisibility(View.INVISIBLE);
				titleIndex = R.id.menu_userfull;
				gridView.setAdapter(menuContentAdapters[0]);
				popupMenuWindow.showAtLocation(
						findViewById(R.id.amt_media_root_view), Gravity.BOTTOM,
						0, 0);
			}
		}
		// }
		return false;
	}

	@Override
	public void onClick(View v) {
		titleIndex = v.getId();
		switch (titleIndex) {
		case R.id.menu_userfull:
			userfullImg.setVisibility(View.VISIBLE);
			toolsImg.setVisibility(View.INVISIBLE);
			helpImg.setVisibility(View.INVISIBLE);
			gridView.setAdapter(menuContentAdapters[0]);
			break;
		case R.id.menu_tools:
			userfullImg.setVisibility(View.INVISIBLE);
			toolsImg.setVisibility(View.VISIBLE);
			helpImg.setVisibility(View.INVISIBLE);
			gridView.setAdapter(menuContentAdapters[1]);
			break;
		case R.id.menu_hlep:
			userfullImg.setVisibility(View.INVISIBLE);
			toolsImg.setVisibility(View.INVISIBLE);
			helpImg.setVisibility(View.VISIBLE);
			gridView.setAdapter(menuContentAdapters[2]);
			break;
		}
	}

	public ImageButton getGoBackBtn() {
		return goBackBtn;
	}
	
	public View getGoPlayerBtn() {
		return goPlayerBtn;
	}
	
	public LinearLayout getEditLyricLayout() {
		return editLyricLayout;
	}
	
	public View getRootView() {
		return rootView;
	}
	
	public DialogEditLyricRadio getLyricModifyDialog() {
		return lyricModify;
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
//		String languageToLoad  = "zh";
//		Locale locale = new Locale(languageToLoad);
//		Locale.setDefault(locale);
//		Configuration config = getResources().getConfiguration();
//		DisplayMetrics metrics = getResources().getDisplayMetrics();
//		config.locale = Locale.SIMPLIFIED_CHINESE;
//		getResources().updateConfiguration(config, metrics);
		}
	
	public class SdcardStateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			ServiceManager.exit();
		}
	}
	
	private void registerSdcardStateReceiver() {
		mSdcardStateReceiver = new SdcardStateReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
		intentFilter.addDataScheme("file");
		AmtMedia.this.registerReceiver(mSdcardStateReceiver, intentFilter);
	}
}