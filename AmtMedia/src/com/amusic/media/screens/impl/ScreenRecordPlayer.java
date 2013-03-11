package com.amusic.media.screens.impl;

import java.io.File;

import android.app.ActivityGroup;
import android.app.LocalActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.amusic.media.AmtMedia;
import com.amusic.media.MediaApplication;
import com.amusic.media.R;
import com.amusic.media.adapter.MenuContentAdapter;
import com.amusic.media.dialog.OnScreenHint;
import com.amusic.media.event.IMediaEventArgs;
import com.amusic.media.event.IMediaEventHandler;
import com.amusic.media.event.impl.MediaEventArgs;
import com.amusic.media.event.impl.MediaEventTypes;
import com.amusic.media.model.MenuItem;
import com.amusic.media.model.ScreenArgs;
import com.amusic.media.screens.IScreen;
import com.amusic.media.services.IAmtScreenService;
import com.amusic.media.services.IMediaEventService;
import com.amusic.media.services.IMediaPlayerService;
import com.amusic.media.services.IMediaService;
import com.amusic.media.services.impl.DesktopLyricService;
import com.amusic.media.services.impl.MediaPlayerService;
import com.amusic.media.services.impl.ServiceManager;
import com.amusic.media.task.DownloadTask;
import com.amusic.media.utils.Constant;
import com.amusic.media.utils.ImageUtil;
import com.amusic.media.view.CustomSeekBar;
import com.amusic.media.view.ScrollLayout;

public class ScreenRecordPlayer extends ActivityGroup implements IScreen, IMediaEventHandler {
	private ScrollLayout mRoot;
	private View mFirstView, mSecondView, mThirdView;
	private Integer id, position;
	private boolean isCurrentable = false;
	private ImageButton playBtn = null;
	private ImageButton abRepeatBtn = null;
	private ImageButton previousBtn = null;
	private ImageButton nextBtn = null;
	private ImageButton playRepeatBtn = null;
	private ImageButton goBackBtn = null;

	private String audioName = null;
	private String audioArtist = null;
	private String audioNameTemp = null;
	private String audioFilePath = null;
	private TextView playTimeTv = null;
	private TextView durationTimeTv = null;
	private CustomSeekBar seekbar = null;
	private TextView audioNameTv = null;
	private TextView audioArtistTv = null;
	private Drawable background;
	private Context context = null;
	private int currentPosition;
	private int duration;
	private int audioPlayOrderMode =1;
	private int audioPlayListcycleMode =2;
	private int audioPlaySingleMode =3;
	private int audioPlayShufflemMode =4;
	private final int STATE_PLAY = 1;
	private final int STATE_PAUSE = 2;
	private int flagMusic;
	private final int IMAGE_DOWNLOADED = 5;
	private int abRepeatBeginPostion, abRepeatEndPostion;
	private boolean abRepeatState = false;
	private DesktopLyricService desktopLyric;
	private LinearLayout rootView;
	private TextView userfull, tools, help;
	private PopupWindow popupMenuWindow;
	private ImageView userfullImg, toolsImg, helpImg;
	private GridView gridView;
	private final IAmtScreenService amtScreenService;
	private int titleIndex;
	private MenuContentAdapter[] menuContentAdapters = new MenuContentAdapter[3];
	public static final String XML_NAME = "ScreenBackground";
	public static final String XML_BACKGROUND = "background";
	WakeLock wakeLockSCreen = null;
	private SharedPreferences preferences;
	private PopupWindow promptWindow = null;
	public LocalActivityManager mLocalActivityManager;
	private IMediaEventService mediaEventService;
	private IMediaPlayerService mediaPlayerService;
	public static Bitmap singerImage = null;
	public static ImageView singerView = null;
	private WindowManager wm = (WindowManager) MediaApplication.getContext().getSystemService("window");
	IMediaEventArgs eventArgs = new MediaEventArgs();
	//保存当前播放模式，用于在随时间更新中判断如果当前模式没有发生变化，则不做相应的图标变化（菜单选择模式让图标跟着做对应的变化）
	
	public static int playModeClick =0;
	private OnScreenHint mOnScreenHint;
	
	private HeadsetPlugReceiver headsetPlugReceiver;
	
	public  Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(msg.what == DownloadTask.MESSAGE_WHAT_DOWNLOADED) {
			switch (msg.what) {
			case IMAGE_DOWNLOADED:
				singerView.setImageBitmap(singerImage);
				break;
			default:
				break;
			}
			}
		}
	};
	private com.amusic.media.screens.impl.ScreenRecordPlayer.CurrentTimeReceiver currentTimeReceiver;

	public ScreenRecordPlayer() {
		this(true);
	}

	public ScreenRecordPlayer(boolean singleActivityMode) {
		mediaEventService = ServiceManager.getMediaEventService();
		mediaPlayerService = ServiceManager.getMediaplayerService();
		desktopLyric = DesktopLyricService.getInstance();
		amtScreenService = ServiceManager.getAmtScreenService();
		preferences = MediaApplication.getInstance().getSharedPreferences(XML_NAME,Context.MODE_WORLD_WRITEABLE);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
//		MediaApplication.logD(context.getClass(), "onCreate");
		isCurrentable = true;
		registerCurrentTimeReceiver();
		registerHeadsetPlugReceiver();
		mediaEventService.addEventHandler(this);
		mLocalActivityManager = getLocalActivityManager();
		ScreenArgs args = (ScreenArgs) getIntent().getSerializableExtra("args");
		id = (Integer) args.getExtra("id");
		position = (Integer) args.getExtra("position");
		eventArgs.putExtra("id", id);
		eventArgs.putExtra("changeHighlight", args.getExtra("changeHighlight"));
		eventArgs.putExtra("position", position);
		eventArgs.putExtra("screenType", args.getExtra("screenType"));
		eventArgs.putExtra("screenId", args.getExtra("screenId"));
		setContentView(R.layout.screen_record_player);
		ServiceManager.setRecordPlayer(this);
		mRoot = (ScrollLayout) findViewById(R.id.root);
		rootView = (LinearLayout) findViewById(R.id.screen_record_layout);
		//promptView = getLayoutInflater().inflate(R.layout.screen_audio_player_prompt, null);
		//promptWindow = new PopupWindow(promptView,LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		//nextAudioPromptTv = (TextView) promptView.findViewById(R.id.audio_prompt);
		audioNameTv = (TextView) findViewById(R.id.audio_name);
		audioArtistTv = (TextView) findViewById(R.id.audio_artist);
		playTimeTv = (TextView) findViewById(R.id.audio_playtime);
		durationTimeTv = (TextView) findViewById(R.id.audio_duration);
	    mOnScreenHint = OnScreenHint.makeText(this, getString(R.string.screen_audio_player_mode_order));
		playBtn = (ImageButton) findViewById(R.id.audio_play_btn);
		playBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (flagMusic) {
				case STATE_PLAY:
					pause();
					break;

				case STATE_PAUSE:
					play();
					break;
				}
			}
		});

		seekbar = (CustomSeekBar) findViewById(R.id.audio_seekbar);
		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				if(seekBar.getProgress()==seekBar.getMax()){
					seekbarChange(seekbar.getProgress()-900);
				}else{
					seekbarChange(seekbar.getProgress());
				}		
				String mtime ="";
				mtime =formatTime(seekBar.getProgress());
				mOnScreenHint.cancel();
				mOnScreenHint = OnScreenHint.makeText(context, mtime);
				mOnScreenHint.show();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				//currentPosition = progress;
				if (fromUser) {
					if (abRepeatState) {
						if (progress < abRepeatBeginPostion || progress > abRepeatEndPostion+1000) {
							abRepeatInit();
						}
					}

				}
			}
		});

//		previousBtn = (ImageButton) findViewById(R.id.audio_previous_btn);
//		previousBtn.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				
//			}
//		});
//
//		nextBtn = (ImageButton) findViewById(R.id.audio_next_btn);
//		nextBtn.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				
//			}
//		});

		abRepeatBtn = (ImageButton) findViewById(R.id.audio_abrepeat_btn);
		//需要修改
		switch (MediaPlayerService.flagAbRepeatMode) {
		case MEDIA_MODE_REPEAT_OVER:
			abRepeatBtn.setImageResource(R.drawable.screen_kmedia_player_abrepeat_over_selector);
			break;

		case MEDIA_MODE_REPEAT_BEGIN:
			abRepeatBtn.setImageResource(R.drawable.screen_kmedia_player_abrepeat_begin_selector);
			break;

		case MEDIA_MODE_REPEAT_END:
			abRepeatBtn.setImageResource(R.drawable.screen_kmedia_player_abrepeat_end_selector);
			seekbar.setFlagA(true, abRepeatBeginPostion);
			break;

		case MEDIA_MODE_REPEAT_PLAY:
			abRepeatBtn.setImageResource(R.drawable.screen_kmedia_player_abrepeat_play_selector);
			seekbar.setFlagA(true, abRepeatBeginPostion);
			seekbar.setFlagB(true, abRepeatEndPostion);
			break;
		}
		abRepeatBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				switch (MediaPlayerService.flagAbRepeatMode) {
				case MEDIA_MODE_REPEAT_OVER:
					abRepeatState = false;
					seekbar.removeFlag();
					MediaPlayerService.flagAbRepeatMode = MediaEventTypes.MEDIA_MODE_REPEAT_BEGIN;
					abRepeatBtn.setImageResource(R.drawable.screen_kmedia_player_abrepeat_begin_selector);
					break;

				case MEDIA_MODE_REPEAT_BEGIN:
					MediaPlayer mplayer = mediaPlayerService
					.getMediaPlayer();
			        int curTime = mplayer.getCurrentPosition();
					seekbar.setFlagA(true, curTime);
					abRepeatBeginPostion = curTime;
					MediaPlayerService.flagAbRepeatMode = MediaEventTypes.MEDIA_MODE_REPEAT_END;
					abRepeatBtn.setImageResource(R.drawable.screen_kmedia_player_abrepeat_end_selector);
					break;

				case MEDIA_MODE_REPEAT_END:
					MediaPlayer mplayer1 = mediaPlayerService
					.getMediaPlayer();
			        int curTime1 = mplayer1.getCurrentPosition();
					if (curTime1 < abRepeatBeginPostion){
						mOnScreenHint.cancel();
						mOnScreenHint = OnScreenHint.makeText(context, getString(R.string.screen_audio_player_abrepeat_error));
						mOnScreenHint.show();
					} else {
						//seekbar.setFlagB(true, seekbar.getProgress());
						seekbar.setFlagB(true, curTime1);
						abRepeatEndPostion = curTime1;
						MediaPlayerService.flagAbRepeatMode = MediaEventTypes.MEDIA_MODE_REPEAT_PLAY;
						abRepeatBtn.setImageResource(R.drawable.screen_kmedia_player_abrepeat_play_selector);
					}
					break;

				case MEDIA_MODE_REPEAT_PLAY:
					abRepeatState = true;
					seekbarChange(abRepeatBeginPostion);
					seekbar.changeButtonColor();
					MediaPlayerService.flagAbRepeatMode = MediaEventTypes.MEDIA_MODE_REPEAT_OVER;
					abRepeatBtn.setImageResource(R.drawable.screen_kmedia_player_abrepeat_over_selector);
					break;
				}

			}
		});

		 goBackBtn = (ImageButton) findViewById(R.id.audio_play_back_btn);
		 goBackBtn.setOnClickListener(new View.OnClickListener() {
			 @Override
			 public void onClick(View v) {
				 backPressed();
			 }
		 });
         //需要修改
		playRepeatBtn = (ImageButton) findViewById(R.id.audio_play_repeat_btn);
		
		playRepeatBtn.setImageResource(R.drawable.screen_kmedia_player_repeat_selector);

		playRepeatBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				repeat();
			}
		});
		SharedPreferences sharedata = getSharedPreferences("lastsong", 0);
		int audioPlayMode = sharedata.getInt("flagAudioPlayMode", 0);
		
		initView();
		if (position != null)
			setup();
		else {
			mediaEventService.onMediaUpdateEvent(new MediaEventArgs().setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_INFO_REFRESH));
			if (MediaPlayerService.getArgs() != null) {
				MediaEventArgs mediaEventArgs = MediaPlayerService.getArgs();
				if (mediaEventArgs.getExtra("audioName") != null && mediaEventArgs.getExtra("audioArtist") != null) {
					String name = (String) mediaEventArgs.getExtra("audioName");
					String artist = (String) mediaEventArgs.getExtra("audioArtist");
					uiRefresh(name, artist);
				}
			}
			if (mediaPlayerService.getMediaPlayer().isPlaying()) {
				flagMusic = STATE_PLAY;
				playBtn.setImageResource(R.drawable.screen_audio_player_pause_selector);
			} else {
				flagMusic = STATE_PAUSE;
				playBtn.setImageResource(R.drawable.screen_audio_player_play_selector);
			}
		}
		
	}

	private void setup() {
		flagMusic = STATE_PLAY;
		playBtn.setImageResource(R.drawable.screen_audio_player_pause_selector);
		mediaEventService.onMediaUpdateEvent(eventArgs.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_START));
	}

	private void pause() {
		flagMusic = STATE_PAUSE;
		playBtn.setImageResource(R.drawable.screen_audio_player_play_selector);
		mediaEventService.onMediaUpdateEvent(eventArgs.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_PAUSE));
	}

	private void play() {
		flagMusic = STATE_PLAY;
		playBtn.setImageResource(R.drawable.screen_audio_player_pause_selector);
	    mediaEventService.onMediaUpdateEvent(eventArgs.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_CONTINUE));

	}

	 private void stop() {		 
	 playBtn.setImageResource(R.drawable.screen_audio_player_play_selector);
	 mediaEventService.onMediaUpdateEvent(new MediaEventArgs().setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_STOP));
	 }

	private void seekbarChange(int progress) {
		MediaEventArgs args = new MediaEventArgs();
		args.putExtra("seek", progress);
		mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_MOVE_PROGRESS));
	}

	public void abRepeatInit() {
		abRepeatEndPostion = 0;
		abRepeatBeginPostion = 0;
		abRepeatState = false;
		seekbar.removeFlag();
		MediaPlayerService.flagAbRepeatMode = MediaEventTypes.MEDIA_MODE_REPEAT_BEGIN;
		abRepeatBtn.setImageResource(R.drawable.screen_kmedia_player_abrepeat_begin_selector);
	}

	public void initView() {

		mRoot.removeAllViews();
//		Intent firstIntent = new Intent(this, ScreenAudioSongLyricsWaveform.class);
//		firstIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//		mFirstView = activityToView(this, firstIntent, ScrollLayout.FIRST_INTENT_TAG);
//		mFirstView.setTag(ScrollLayout.FIRST_INTENT_TAG);
//		mRoot.addView(mFirstView);

//		Intent secondIntent = new Intent(this, ScreenAudioSongLyrics.class);
//		secondIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//		mSecondView = activityToView(this, secondIntent, ScrollLayout.SECOND_INTENT_TAG);
//		mSecondView.setTag(ScrollLayout.SECOND_INTENT_TAG);
//		mRoot.addView(mSecondView);

		Intent thirdIntent = new Intent(this, ScreenAudioSongLyricsFullScreen.class);
		thirdIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		mThirdView = activityToView(this, thirdIntent, ScrollLayout.THIRD_INTENT_TAG);
		mThirdView.setTag(ScrollLayout.THIRD_INTENT_TAG);
		mRoot.addView(mThirdView);

	}

	public View activityToView(Context parent, Intent intent, String tag) {

		Window w = mLocalActivityManager.startActivity(tag, intent);
		View wd = w != null ? w.getDecorView() : null;
		if (wd != null) {
			wd.setVisibility(View.VISIBLE);
			wd.setFocusableInTouchMode(true);
			((ViewGroup) wd).setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
		}
		return wd;

	}

	@Override
	protected void onNewIntent(Intent intent) {
//		MediaApplication.logD(context.getClass(), "onNewIntent");
		ScreenArgs args = (ScreenArgs) intent.getSerializableExtra("args");
		id = (Integer) args.getExtra("id");
		position = (Integer) args.getExtra("position");
		eventArgs.putExtra("id", id);
		eventArgs.putExtra("position", position);
		eventArgs.putExtra("screenType", args.getExtra("screenType"));
		eventArgs.putExtra("screenId", args.getExtra("screenId"));
		eventArgs.putExtra("changeHighlight", args.getExtra("changeHighlight"));
		if(position != null)
		setup();
	}

	private static String formatTime(int times) {
		times /= 1000;
		int minutes = times / 60;
		int seconds = times % 60;
		minutes %= 60;
		return String.format("%02d:%02d", minutes, seconds);
		//return MessageFormat.format("{1,number,00}:{2,number,00}",  time / 1000 / 60 % 60, time / 1000 % 60);
	}
	
	
//	private String getVoluemProgress(int currentVoluem) {
//		StringBuilder sb = new StringBuilder();
//		int progressAmount = (int) (currentVoluem * 100 / maxVoluem);
//		sb.append(progressAmount);
//		sb.append("%");
//		return sb.toString();
//	}

	@Override
	public boolean onEvent(IMediaEventArgs args) {
		switch (args.getMediaUpdateEventTypes()) {
		case MEDIA_PLAYER_BOTTOM_CONTROL_PLAY_UI:
			flagMusic = STATE_PLAY;
			playBtn.setImageResource(R.drawable.screen_audio_player_pause_selector);
			break;
		case MEDIA_PLAYER_BOTTOM_CONTROL_STOP_UI:
			if(abRepeatEndPostion!=0){
				abRepeatInit();
			}
			flagMusic = STATE_PAUSE;
			playBtn.setImageResource(R.drawable.screen_audio_player_play_selector);
			break;
		case MEDIA_PLAYER_BOTTOM_CONTROL_PAUSE_UI:
			flagMusic = STATE_PAUSE;
			playBtn.setImageResource(R.drawable.screen_audio_player_play_selector);
			break;
		case MUSIC_DURATION:
			duration = (Integer) args.getExtra("durationTime");
			audioName = (String) args.getExtra("audioName");
			//String resultsText = String.format(context.getResources().getString(R.string.screen_audio_next_song), audioName); 
			//nextAudioPromptTv.setText(resultsText);
			seekbar.setMax(duration);
			durationTimeTv.setText(formatTime(duration));
			break;
		case MUSIC_CURRENT:
		
			break;
		case MUSIC_UPDATE:
			if (isCurrentable == true) {
				if (promptWindow != null && promptWindow.isShowing()) {
					promptWindow.dismiss();
				}
			}
			abRepeatInit();
			flagMusic = STATE_PLAY;
			MediaPlayerService.flagAccompanyMode = MediaEventTypes.MEDIA_MODE_ORIGINAL;
			playBtn.setImageResource(R.drawable.screen_audio_player_pause_selector);
			audioArtist = (String) args.getExtra("audioArtist");
			audioName = (String) args.getExtra("audioName");
			audioFilePath = (String) args.getExtra("filepath");
			String songName = audioName;
			if (audioName != null) {
				int index = audioName.lastIndexOf('.');
				songName = audioName.substring(0,index);
			}
			uiRefresh(songName, audioArtist);
			break;
		case MUSIC_RELOAD:
			duration = (Integer) args.getExtra("durationTime");
			seekbar.setMax(duration);
			durationTimeTv.setText(formatTime(duration));
			audioArtist = (String) args.getExtra("audioArtist");
			audioName = (String) args.getExtra("audioName");
			audioFilePath = (String) args.getExtra("filepath");
			String song_Name = audioName;
			if (audioName != null) {
				int ix = audioName.lastIndexOf('.');
				song_Name = audioName.substring(0,ix);
			}
			uiRefresh(song_Name, audioArtist);
			break;
		case MEDIA_RECORD_MUSIC_PLAY_STOP:
//			backPressed();
			break;
//		case AUDIO_DOWNLOAD_IMAGE_FINISH:
//			if(audioArtist.equals(args.getExtra("singerName"))){
//				String picturePath = MediaApplication.singerPicturesPath + audioArtist + IMediaService.PICTURE_SUFFIX;
//				File pictureFile = new File(picturePath);
//				if (pictureFile.exists()) {
//					singerImage = Drawable.createFromPath(picturePath);
//					MediaApplication.getInstance().getImageCache().put(audioArtist, new SoftReference<Drawable>(singerImage));
//					singerView.setBackgroundDrawable(singerImage);
//					Message message = mHandler.obtainMessage(IMAGE_DOWNLOADED);
//					mHandler.sendMessage(message);
//				}
//			}
//			break;
		}
		return true;
	}
	@Override
	public void onBackPressed() {
		backPressed();
	}
	
	public void backPressed(){
		// 保存K歌之前的状态并设置状态让其可以播放上一次播放的歌曲
		ServiceManager.saveState();
		ServiceManager.isPlayed = false;
		AmtMedia.s_goPlayerBtn_click_num = -1;
		
		IMediaEventArgs eventArgs = new MediaEventArgs();
		ServiceManager.getMediaEventService().onMediaUpdateEvent(eventArgs.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_STOP));
		if (wakeLockSCreen != null && wakeLockSCreen.isHeld()) {
	    	wakeLockSCreen.release();
	    	wakeLockSCreen = null;
        }
		ServiceManager.finishScreenRecordPlayer();
	}
	
	private  void uiRefresh(String audioNameString, String audioArtistString) {
		audioNameTv.setText(audioNameString);
		audioNameTemp = audioNameString;
		if ( audioArtistString == null || audioArtistString.equals("<unknown>") || audioArtistString.equals("") || audioArtistString.equals("未知歌手")) {
			audioArtistTv.setText(context.getString(R.string.screen_audio_player_audio_artist_unknown));
//			singerImage = BitmapFactory.decodeResource(MediaApplication.getContext().getResources(), R.drawable.screen_audio_default_singer_picture);
//			Message message = mHandler.obtainMessage(IMAGE_DOWNLOADED);
//			message.what = DownloadTask.MESSAGE_WHAT_DOWNLOADED;
//			mHandler.sendMessage(message);
//			new Thread() {
//				@Override
//				public void run() {
//					try {
////						audioName = MediaPlayerService.splitTitle(audioName);
//					    singerImage = ImageUtil.getDrawableFromUrl(audioNameTemp, audioFilePath, mHandler);
//						Message message = mHandler.obtainMessage(IMAGE_DOWNLOADED);
//						message.what = DownloadTask.MESSAGE_WHAT_DOWNLOADED;
//						mHandler.sendMessage(message);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			}.start();
		} else {
			audioArtistTv.setText(audioArtist);
			audioArtist = audioArtistString.replaceAll(" ", "").toLowerCase();
//			singerImage = BitmapFactory.decodeResource(MediaApplication.getContext().getResources(), R.drawable.screen_audio_default_singer_picture);
//			Message message = mHandler.obtainMessage(IMAGE_DOWNLOADED);
//			message.what = DownloadTask.MESSAGE_WHAT_DOWNLOADED;
//			mHandler.sendMessage(message);
//			new Thread() {
//				@Override
//				public void run() {
//					try {
//						audioArtist = MediaPlayerService.splitTitle(audioArtist);
//						String picturePath = MediaApplication.singerPicturesPath + audioArtist + IMediaService.PICTURE_SUFFIX;
//						String pictureTempPath = MediaApplication.singerPicturesPath + audioArtist + IMediaService.TMP_SUFFIX;
//						File pictureFile = new File(picturePath);
//						if (pictureFile.exists()) {
//							singerImage = BitmapFactory.decodeFile(picturePath);
//						} else {
//							ImageUtil.getDrawableFromUrl(audioArtist, picturePath, pictureTempPath, mHandler);
//							singerImage = BitmapFactory.decodeResource(MediaApplication.getContext().getResources(), R.drawable.screen_audio_default_singer_picture);
//						}
//						Message message = mHandler.obtainMessage(IMAGE_DOWNLOADED);
//						message.what = DownloadTask.MESSAGE_WHAT_DOWNLOADED;
//						mHandler.sendMessage(message);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			}.start();
		}
	}

	@Override
	public boolean hasMenu() {
		return false;
	}
	

	@Override
	public boolean currentable() {
		return true;
	}

	@Override
	public boolean refresh() {
		return false;
	}

	@Override
	protected void onPause() {
		Constant.WHICH_PLAYER = 0;
		if (promptWindow != null && promptWindow.isShowing()){
			promptWindow.dismiss();
		}
		isCurrentable = false;
		
//		ServiceManager.saveState();
//		ServiceManager.isPlayed = false;
//		AmtMedia.s_goPlayerBtn_click_num = -1;
//		
//		ServiceManager.getAmtMedia().onBackPressed();
		
		super.onPause();
		if (wakeLockSCreen != null && wakeLockSCreen.isHeld()) {
	    	wakeLockSCreen.release();
	    	wakeLockSCreen = null;
        }
	}

	@Override
	protected void onResume() {
		MediaPlayerService.isKMdieaOrRecord = true;
		isCurrentable = true;
//		MediaApplication.logD(context.getClass(), "onResume");
		Constant.WHICH_PLAYER = 3;
		super.onResume();
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		wakeLockSCreen = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "SCreenOn");
		wakeLockSCreen.setReferenceCounted(false);
		wakeLockSCreen.acquire();
	}

	@Override
	public boolean changMenuAdapter() {
		// TODO Auto-generated method stub
		MenuContentAdapter[] menuContentAdapters = ServiceManager.getMenuContentAdapters();
//		menuContentAdapters[Constant.MenuConstant.userfull].getMenuData().add(Constant.MenuConstant.menu_item_delete, 
//				new MenuItem(R.drawable.menu_item_delete, MediaApplication.getContext().getResources()
//						.getString(R.string.screen_home_menu_delete)));
		menuContentAdapters[Constant.MenuConstant.userfull].getMenuData().remove(Constant.MenuConstant.menu_item_scanner);
		
		menuContentAdapters[Constant.MenuConstant.tools].getMenuData().add(Constant.MenuConstant.menu_item_share, 
				new MenuItem(R.drawable.menu_item_share, MediaApplication.getContext().getResources()
						.getString(R.string.screen_home_menu_share)));
		
		return true;
	}

	@Override
	public boolean isMenuChanged() {
		// TODO Auto-generated method stub
		return true;
	}
	
	private void repeat() {
		flagMusic = STATE_PLAY;
		playBtn.setImageResource(R.drawable.screen_audio_player_pause_selector);
		mediaEventService.onMediaUpdateEvent(eventArgs.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_MODE_REPEAT));
		abRepeatInit();
	}
	
	public class CurrentTimeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			currentPosition = intent.getIntExtra("currentTime", 0);
			seekbar.setProgress(currentPosition);

			String currentPositionString = formatTime(currentPosition);
			playTimeTv.setText(currentPositionString);
			if (abRepeatState) {
				if (currentPositionString.equals(formatTime(abRepeatEndPostion)) || currentPositionString.equals(formatTime(abRepeatEndPostion+1000))) {
					seekbarChange(abRepeatBeginPostion);					
				}
			} else {
				if(isCurrentable == true){
					if ((float) currentPosition / duration >= 0.8) {
						if (MediaPlayerService.flagAudioPlayMode == MediaEventTypes.MEDIA_PLAY_MODE_SINGLE_REPEAT) {
							MediaPlayer mplayer = mediaPlayerService
									.getMediaPlayer();
							int curTime = mplayer.getCurrentPosition();
							if (mplayer.getDuration() - (float) curTime < 1000) {
								abRepeatInit();
							}
						}
						if (promptWindow != null && !promptWindow.isShowing()) {
							promptWindow.showAsDropDown(findViewById(R.id.audio_info));
						}

					} else {
						if (promptWindow != null && promptWindow.isShowing()){
							promptWindow.dismiss();
						}
					}
				}
			}
		}
	}
	
	private void registerCurrentTimeReceiver() {
		currentTimeReceiver = new CurrentTimeReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.amusic.media.MediaPlayerService");
		MediaApplication.getContext().registerReceiver(currentTimeReceiver, intentFilter);
	}

	@Override
	protected void onDestroy() {
//		MediaApplication.logD(context.getClass(), "onDestroy");
		mediaEventService.removeEventHandler(this);
		Constant.WHICH_PLAYER = 0;
		unregisterReceiver(headsetPlugReceiver);
		super.onDestroy();
		
	}
	
	public class HeadsetPlugReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(Constant.IS_MUSIC_PAUSE_CONTROLL && mediaPlayerService.getMediaPlayer().isPlaying()){
				if (intent.hasExtra("state")) {
					if (intent.getIntExtra("state", 0) == 0) {
						pause();
					}
				}
			}

		}
	}
		

	private void registerHeadsetPlugReceiver() {
		headsetPlugReceiver = new HeadsetPlugReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("android.intent.action.HEADSET_PLUG");
		ScreenRecordPlayer.this.registerReceiver(headsetPlugReceiver, intentFilter);
	}

}
