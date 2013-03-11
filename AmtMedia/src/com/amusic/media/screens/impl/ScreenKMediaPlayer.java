package com.amusic.media.screens.impl;

import java.io.File;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.Dialog;
import android.app.LocalActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.amusic.media.AmtMedia;
import com.amusic.media.MediaApplication;
import com.amusic.media.R;
import com.amusic.media.adapter.MenuContentAdapter;
import com.amusic.media.dialog.DialogConfirmSaveRecord;
import com.amusic.media.dialog.DialogConvertPcmProcess;
import com.amusic.media.dialog.DialogQuitCurrentKMusic;
import com.amusic.media.dialog.DialogStopConvertConfirm;
import com.amusic.media.event.IMediaEventArgs;
import com.amusic.media.event.IMediaEventHandler;
import com.amusic.media.event.impl.MediaEventArgs;
import com.amusic.media.event.impl.MediaEventTypes;
import com.amusic.media.ffmpeg.ExtAudioRecorder;
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
import com.amusic.media.view.CustomNoSeekSeekBar;
import com.amusic.media.view.ScrollLayout;


public class ScreenKMediaPlayer extends ActivityGroup implements IScreen, IMediaEventHandler, OnCheckedChangeListener, OnGestureListener{
	private ScrollLayout mRoot;
	private LinearLayout rootView;
	private SharedPreferences preferences;
	private Drawable background;
	private View /*mFirstView,*/ mSecondView, mThirdView;
	private Integer id, position;
	private RadioGroup radioderGroup;
	private CustomNoSeekSeekBar progressBar = null;
	private SeekBar micSeekBar = null;
	public static ImageView singerView = null;
	private RadioButton originalBtn = null;
	private RadioButton accompanyBtn = null;
	private ImageButton playBtn = null;
	private ImageButton abRepeatBtn = null;
	private ImageButton goBackBtn = null;
	private ImageButton repeatBtn = null;
	private HeadsetPlugReceiver headsetPlugReceiver;
	private String audioName = null;
	private String audioArtist = null;
	private String audioFilePath = null;
	private String audioNameTemp = null;
	private TextView playTimeTv = null;
	private TextView durationTimeTv = null;
	private TextView audioNameTv = null;
	private TextView audioArtistTv = null;
	private Activity context = null;
	public  static Activity Kmediacontext = null;
	private int currentPosition;
	private int duration;
	public static final String XML_NAME = "ScreenBackground";
	public static final String XML_BACKGROUND = "background";
	public final static int STATE_PLAY = 1;
	private static final int STATE_PAUSE = 2;
	public static int flagMusic;
	public int micVoluemProgress = 0;
	public final int maxVoluem = 15;
	private AudioManager mAudioManager = null;
	private int abRepeatBeginPostion, abRepeatEndPostion;
	private  boolean abRepeatState = false;
	public static int abRepeatFlag = 0;//
	public static int currenttimeafterB = 0;//
	private View mControlView = null;
	private PopupWindow mControlWindow = null;
	private Dialog dialog;
	private GestureDetector gestureDetector;
	public LocalActivityManager mLocalActivityManager;
	private IMediaEventService mediaEventService;
	private IMediaPlayerService mediaPlayerService;
	IMediaEventArgs eventArgs = new MediaEventArgs();
	private PopupWindow popupMenuWindow;
	private GridView gridView;
	private int titleIndex;
	private TextView userfull, tools, help;
	private ImageView userfullImg, toolsImg, helpImg;
	private final IAmtScreenService amtScreenService;
	private final IMediaService mediaService;
	private DesktopLyricService desktopLyric;
	private CurrentTimeReceiver currentTimeReceiver;
	private String songName;
	private String singerName;
	public static boolean isOriginal = true;
	public static boolean saveRecordCheckBoxFlag = true;
	private boolean bHeadsetState;
	private boolean bSeekbarLinkHeadset;
	private Button k_mic_img_btn;
	private static boolean bOutputExtraFlag  = true;
	public static boolean homeDownPause = false;
	private boolean dialogFlag = false;
	private boolean dialogQuitFlag = false;
	private DialogQuitCurrentKMusic  dialogQuitCurrentKMusic;
	public static ImageView[] dotImageViews = new ImageView[2];
	
	private DialogConfirmSaveRecord dialogConfirmSaveRecord = null;
	public DialogConfirmSaveRecord getDialogConfirmSaveRecord(){
		return dialogConfirmSaveRecord;
	}
	private DialogConvertPcmProcess dialogConvertPcmProcess = null;
	public DialogConvertPcmProcess getDialogConvertPcmProcess(){
		return dialogConvertPcmProcess;
	}
	private DialogStopConvertConfirm dialogStopConvertConfirm = null;
	public DialogStopConvertConfirm getDialogStopConvertConfirm(){
		return dialogStopConvertConfirm;
	}
	
	private static ScreenKMediaPlayer scrKmPlayer = null;
	public static ScreenKMediaPlayer getInstance(){
		return scrKmPlayer;
	}
	
	public static boolean getOutputExtraFlag(){
		return bOutputExtraFlag;
	}
	
	public WakeLock wakeLockThread = null;
	public WakeLock wakeLockSCreen = null; 
	
	public ScreenKMediaPlayer() {
		this(true);
	}

	public static boolean stopMedia_save_as_record = false;
	public static boolean notify_kmedia_has_finished = false;
	
	public ScreenKMediaPlayer(boolean singleActivityMode) {
		mediaEventService = ServiceManager.getMediaEventService();
		mediaPlayerService = ServiceManager.getMediaplayerService();
		amtScreenService = ServiceManager.getAmtScreenService();
		mediaService = ServiceManager.getMediaService();
		desktopLyric = DesktopLyricService.getInstance();
		preferences = MediaApplication.getInstance().getSharedPreferences(XML_NAME,Context.MODE_WORLD_WRITEABLE);
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(msg.what == DownloadTask.MESSAGE_WHAT_DOWNLOADED) {
				Object obj = msg.obj;
				if (obj == null) {
					singerView.setImageBitmap(BitmapFactory.decodeResource(MediaApplication.getContext().getResources(), R.drawable.screen_audio_default_singer_picture));
					
				} else {
					singerView.setImageBitmap((Bitmap) obj);
				}
				}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		Kmediacontext =context;
		scrKmPlayer = this;
		dialogConfirmSaveRecord = new DialogConfirmSaveRecord(context);
		dialogConvertPcmProcess = new DialogConvertPcmProcess(context);
		dialogStopConvertConfirm = new DialogStopConvertConfirm(context);
		dialogQuitCurrentKMusic = new DialogQuitCurrentKMusic(context);
		mediaEventService.addEventHandler(this);
		registerHeadsetPlugReceiver();
		registerCurrentTimeReceiver();
		mLocalActivityManager = getLocalActivityManager();
		ScreenArgs args = (ScreenArgs) getIntent().getSerializableExtra("args");
		id = (Integer) args.getExtra("id");
		position = (Integer) args.getExtra("position");
		songName = (String) args.getExtra("songName");
		singerName = (String) args.getExtra("singerName");
		eventArgs.putExtra("id", id);
		eventArgs.putExtra("position", position);
		eventArgs.putExtra("screenType", args.getExtra("screenType"));
		eventArgs.putExtra("screenId", args.getExtra("screenId"));
		setContentView(R.layout.screen_kmedia_player);
		mRoot = (ScrollLayout) findViewById(R.id.root);
		rootView = (LinearLayout) findViewById(R.id.screen_kmedia_layout);
		gestureDetector = new GestureDetector(this);
		mControlView = getLayoutInflater().inflate(R.layout.screen_kmedia_player_mic_control, null);
		mControlWindow = new PopupWindow(mControlView,LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		micVoluemProgress = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		progressBar = (CustomNoSeekSeekBar) findViewById(R.id.k_player_seek_bar);
		audioNameTv = (TextView) findViewById(R.id.k_player_song_name);
		audioNameTv.setText(songName);
		audioArtistTv = (TextView) findViewById(R.id.k_player_singer);
		audioArtistTv.setText(singerName);
		playTimeTv = (TextView) findViewById(R.id.k_player_playtime);
		durationTimeTv = (TextView) findViewById(R.id.k_player_duration);
		radioderGroup = (RadioGroup) findViewById(R.id.screen_kmedia_radiogroup);
		radioderGroup.setOnCheckedChangeListener(this);
		originalBtn = (RadioButton) findViewById(R.id.screen_ktv_tab_original);
		accompanyBtn = (RadioButton) findViewById(R.id.screen_ktv_tab_accompany);
		dotImageViews[0] = (ImageView) findViewById(R.id.screen_audio_dot_1);
		dotImageViews[1] = (ImageView) findViewById(R.id.screen_audio_dot_2);
		dotImageViews[0].setVisibility(View.GONE);
		dotImageViews[1].setVisibility(View.GONE);
//		if(isOriginal){
//			originalBtn.setChecked(true);
//		}else{
//			accompanyBtn.setChecked(true);
//		}
		playBtn = (ImageButton) findViewById(R.id.k_player_play);
		playBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				switch (flagMusic) {
				case STATE_PLAY:
					pause();
					break;

				case STATE_PAUSE:
					if(ScreenKMediaPlayer.notify_kmedia_has_finished == false){
						play();
					}else{
						repeat();
					}
					break;
				}
			}
		});
		micSeekBar = (SeekBar) mControlView.findViewById(R.id.k_mic_sound);
		micSeekBar.setMax(maxVoluem);
		micSeekBar.setProgress(micVoluemProgress);
		micSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					micSoundChange((float) progress / maxVoluem);
				}
				if (!bSeekbarLinkHeadset) {
					// MediaApplication.getInstance().setMicFlag(false);
					Toast.makeText(context, getString(R.string.screen_kmedia_player_plug_headset),
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		k_mic_img_btn = (Button) mControlView.findViewById(R.id.k_mic_btn);
		k_mic_img_btn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (bHeadsetState){
					bHeadsetState = !bHeadsetState;
					k_mic_img_btn.setBackgroundResource(R.drawable.screen_kmedia_player_mic_on);
					bOutputExtraFlag = true;
					Toast.makeText(context, getString(R.string.screen_kmedia_player_mic_on), Toast.LENGTH_SHORT).show();
				}else{
					bHeadsetState = !bHeadsetState;
					k_mic_img_btn.setBackgroundResource(R.drawable.screen_kmedia_player_mic_off);
					bOutputExtraFlag = false;
					Toast.makeText(context, getString(R.string.screen_kmedia_player_mic_off), Toast.LENGTH_SHORT).show();
				}
			}
			
		});
		
		abRepeatBtn = (ImageButton) findViewById(R.id.k_player_abrepeat);
		switch (MediaPlayerService.flagAbRepeatMode) {
		case MEDIA_MODE_REPEAT_OVER:
			abRepeatBtn.setImageResource(R.drawable.screen_kmedia_player_abrepeat_over_selector);
			break;

		case MEDIA_MODE_REPEAT_BEGIN:
			abRepeatBtn.setImageResource(R.drawable.screen_kmedia_player_abrepeat_begin_selector);
			break;

		case MEDIA_MODE_REPEAT_END:
			abRepeatBtn.setImageResource(R.drawable.screen_kmedia_player_abrepeat_end_selector);
			progressBar.setFlagA(true, abRepeatBeginPostion);
			break;

		case MEDIA_MODE_REPEAT_PLAY:
			abRepeatBtn.setImageResource(R.drawable.screen_kmedia_player_abrepeat_play_selector);
			progressBar.setFlagA(true, abRepeatBeginPostion);
			progressBar.setFlagB(true, abRepeatEndPostion);
			break;
		}
		abRepeatBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				switch (MediaPlayerService.flagAbRepeatMode) {
				case MEDIA_MODE_REPEAT_OVER:
					abRepeatState = false;
					abRepeatFlag =2;
					progressBar.removeFlag();
					MediaPlayerService.flagAbRepeatMode = MediaEventTypes.MEDIA_MODE_REPEAT_BEGIN;
					abRepeatBtn.setImageResource(R.drawable.screen_kmedia_player_abrepeat_begin_selector);
					break;

				case MEDIA_MODE_REPEAT_BEGIN:
					MediaPlayer mplayer2 = mediaPlayerService
					.getMediaPlayer();
			        int curTime = mplayer2.getCurrentPosition();
					progressBar.setFlagA(true, curTime);
					abRepeatBeginPostion = curTime;
					MediaPlayerService.flagAbRepeatMode = MediaEventTypes.MEDIA_MODE_REPEAT_END;
					abRepeatBtn.setImageResource(R.drawable.screen_kmedia_player_abrepeat_end_selector);
					break;

				case MEDIA_MODE_REPEAT_END:
					MediaPlayer mplayer1 = mediaPlayerService
					.getMediaPlayer();
			        int curTime1 = mplayer1.getCurrentPosition();
					if (curTime1 < abRepeatBeginPostion) {

						Toast.makeText(MediaApplication.getContext(), context.getString(R.string.screen_audio_player_abrepeat_error),
								Toast.LENGTH_LONG).show();
					} else {
						progressBar.setFlagB(true, curTime1);
						abRepeatEndPostion = curTime1;
						MediaPlayerService.flagAbRepeatMode = MediaEventTypes.MEDIA_MODE_REPEAT_PLAY;
						abRepeatBtn.setImageResource(R.drawable.screen_kmedia_player_abrepeat_play_selector);
					}
					break;

				case MEDIA_MODE_REPEAT_PLAY:
					abRepeatState = true;
					abRepeatFlag =1;
					MediaPlayer mplayer = mediaPlayerService.getMediaPlayer();
					currenttimeafterB = mplayer.getCurrentPosition();
					seekbarChange(abRepeatBeginPostion);
					progressBar.changeButtonColor();
					MediaPlayerService.flagAbRepeatMode = MediaEventTypes.MEDIA_MODE_REPEAT_OVER;
					abRepeatBtn.setImageResource(R.drawable.screen_kmedia_player_abrepeat_over_selector);
					break;
				}

			}
		});

		goBackBtn = (ImageButton) findViewById(R.id.k_player_back_btn);
		goBackBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog();
			}
		});
		
		repeatBtn = (ImageButton) findViewById(R.id.k_player_repeat);
		repeatBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				repeat();
			}
		});
		initView();
		if (position != null){
			setup();
		}
		else {
			mediaEventService.onMediaUpdateEvent(new MediaEventArgs().setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_INFO_REFRESH));
//			if (MediaPlayerService.getArgs() != null) {
//				MediaEventArgs mediaEventArgs = MediaPlayerService.getArgs();
//				if (mediaEventArgs.getExtra("audioName") != null && mediaEventArgs.getExtra("audioArtist") != null) {
//					String name = (String) mediaEventArgs.getExtra("audioName");
//					String artist = (String) mediaEventArgs.getExtra("audioArtist");
					uiRefresh(audioName, audioArtist);
//				}
//			}
			if (mediaPlayerService.getMediaPlayer().isPlaying()) {
				flagMusic = STATE_PLAY;
				playBtn.setImageResource(R.drawable.screen_audio_player_pause_selector);
			} else {
				flagMusic = STATE_PAUSE;
				playBtn.setImageResource(R.drawable.screen_audio_player_play_selector);
			}
		}
		Toast.makeText(MediaApplication.getContext(), getString(R.string.screen_kmedia_player_ktv_begin),
				Toast.LENGTH_SHORT).show();
		
		ServiceManager.setKmediaPlayer(this);
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		wakeLockThread = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ConvertThread");
		wakeLockThread.setReferenceCounted(false);
		 
		wakeLockSCreen = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "SCreenOn");
		wakeLockSCreen.setReferenceCounted(false);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(dialogFlag){
			dialogConfirmSaveRecord.show();
			dialogFlag = false;
		}
		if(dialogQuitFlag){
			dialogQuitCurrentKMusic.show();
			dialogQuitFlag = false;
		}
		MediaPlayerService.isKMdieaOrRecord = true;
//		if(homeDownPause == true){
//			play();
//			homeDownPause = false;
//		}
		Constant.WHICH_PLAYER = 2;
//		MediaApplication.logD(context.getClass(), "onResume");
//		wakeLockThread.acquire();
//		wakeLockSCreen.acquire();
	
	}

	
	public void initView() {

		mRoot.removeAllViews();
//		Intent firstIntent = new Intent(this, ScreenAudioSongLyricsWaveform.class);
//		firstIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//		mFirstView = activityToView(this, firstIntent, ScrollLayout.FIRST_INTENT_TAG);
//		mFirstView.setTag(ScrollLayout.FIRST_INTENT_TAG);
//		mRoot.addView(mFirstView);

		Intent secondIntent = new Intent(this, ScreenAudioSongLyrics.class);
		secondIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		mSecondView = activityToView(this, secondIntent, ScrollLayout.SECOND_INTENT_TAG);
		mSecondView.setTag(ScrollLayout.SECOND_INTENT_TAG);
		mRoot.addView(mSecondView);

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

	private static String formatTime(int times) {
		times /= 1000;
		int minutes = times / 60;
		int seconds = times % 60;
		minutes %= 60;
		return String.format("%02d:%02d", minutes, seconds);
		//return MessageFormat.format("{1,number,00}:{2,number,00}",  time / 1000 / 60 % 60, time / 1000 % 60);
	}
    //需要修改何时MEDIA_MODE_RECORD_PLAY
	private void setup() {
		flagMusic = STATE_PLAY;
		playBtn.setImageResource(R.drawable.screen_audio_player_pause_selector);
		mediaEventService.onMediaUpdateEvent(eventArgs.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_START));
		//Log.e("zhangjingtest","zhangjing RecordPlayThread is starting");
		ServiceManager.getMediaplayerService().getMediaPlayer().startRecordPlay();
	}

	public void pause() {
		if (ExtAudioRecorder.getInstanse().getIsRecording()){
			ExtAudioRecorder.getInstanse().pause();
		}
		flagMusic = STATE_PAUSE;
		playBtn.setImageResource(R.drawable.screen_audio_player_play_selector);
		mediaEventService.onMediaUpdateEvent(eventArgs.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_PAUSE));
	}

	public void play() {
		if (ExtAudioRecorder.getInstanse().getIsRecording()){
			ExtAudioRecorder.getInstanse().resume();
		}
		flagMusic = STATE_PLAY;
		playBtn.setImageResource(R.drawable.screen_audio_player_pause_selector);
		mediaEventService.onMediaUpdateEvent(eventArgs.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_CONTINUE));
	}

	private void repeat() {
		flagMusic = STATE_PLAY;
		playBtn.setImageResource(R.drawable.screen_audio_player_pause_selector);
		mediaEventService.onMediaUpdateEvent(eventArgs.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_MODE_REPEAT));
		abRepeatInit();
		if(saveRecordCheckBoxFlag){
			ExtAudioRecorder.getInstanse().restartRecordPlay();
		}
		
	}
	
	private void stop() {
		playBtn.setImageResource(R.drawable.screen_audio_player_play_selector);
		mediaEventService.onMediaUpdateEvent(eventArgs.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_STOP));
	}

	private void seekbarChange(int progress) {
		eventArgs.putExtra("seek", progress);
		mediaEventService.onMediaUpdateEvent(eventArgs.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_MOVE_PROGRESS));
	}
	
	private void micSoundChange(float progress) {
		eventArgs.putExtra("seek", progress);
		mediaEventService.onMediaUpdateEvent(eventArgs.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_MIC_VOLUEM_MOVE_PROGRESS));
	}

	public void latestOne() {
		flagMusic = STATE_PLAY;
		playBtn.setImageResource(R.drawable.screen_audio_player_pause_selector);
		mediaEventService.onMediaUpdateEvent(eventArgs.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_PREVIOUS));
	}

	public void nextOne() {
		flagMusic = STATE_PLAY;
		playBtn.setImageResource(R.drawable.screen_audio_player_pause_selector);
		mediaEventService.onMediaUpdateEvent(eventArgs.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_NEXT));
	}

	public void abRepeatInit() {
		abRepeatEndPostion = 0;
		abRepeatBeginPostion = 0;
		abRepeatState = false;
		abRepeatFlag =0;
		currenttimeafterB =0;
		progressBar.removeFlag();
		MediaPlayerService.flagAbRepeatMode = MediaEventTypes.MEDIA_MODE_REPEAT_BEGIN;
		abRepeatBtn.setImageResource(R.drawable.screen_kmedia_player_abrepeat_begin_selector);
	}

	private void uiRefresh(String audioNameString, String audioArtistString) {
//		audioNameTv.setText(audioNameString);
		audioNameTemp = audioNameString;
		ServiceManager.setCurSongPrompt(context.getString(R.string.notification_current_song) + audioNameString);
		MediaApplication.getInstance().setCurSonginfo(audioNameString,audioArtistString);
		if (audioArtistString == null || audioArtistString.equals("<unknown>") || audioArtistString.equals("") || audioArtistString.equals("未知歌手")) {
			audioArtistTv.setText(context.getString(R.string.screen_audio_player_audio_artist_unknown));
			new Thread() {
				@Override
				public void run() {
					try {
					    ImageUtil.getDrawableFromUrl(audioNameTemp, audioFilePath, mHandler);
						Message message = mHandler.obtainMessage();
						message.what = DownloadTask.MESSAGE_WHAT_DOWNLOADED;
						mHandler.sendMessage(message);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.start();
		} else {
			audioArtistTv.setText(audioArtist);
			audioArtist = audioArtistString.replaceAll(" ", "").toLowerCase();
			new Thread() {
				@Override
				public void run() {
					try {
						audioArtist = MediaPlayerService.splitTitle(audioArtist);
						String picturePath = MediaApplication.singerPicturesPath + audioArtist + IMediaService.PICTURE_SUFFIX;
						String pictureTempPath = MediaApplication.singerPicturesPath + audioArtist + IMediaService.TMP_SUFFIX;
						File pictureFile = new File(picturePath);
						Bitmap singerImage = null;
						Message message = mHandler.obtainMessage();
						message.what = DownloadTask.MESSAGE_WHAT_DOWNLOADED;
						if (pictureFile.exists()) {
							singerImage = BitmapFactory.decodeFile(picturePath);
							message.obj = singerImage;
							mHandler.sendMessage(message);
						} else {
							mHandler.sendMessage(message);
							ImageUtil.getDrawableFromUrl(audioArtist, picturePath, pictureTempPath, mHandler);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.start();
		}
	}
	

	@Override
	protected void onNewIntent(Intent intent) {
		MediaApplication.logD(context.getClass(), "onNewIntent");
		ScreenArgs args = (ScreenArgs) intent.getSerializableExtra("args");
		id = (Integer) args.getExtra("id");
		position = (Integer) args.getExtra("position");
		eventArgs.putExtra("id", id);
		eventArgs.putExtra("position", position);
		eventArgs.putExtra("screenType", args.getExtra("screenType"));
		eventArgs.putExtra("screenId", args.getExtra("screenId"));
		if(position != null)
		setup();
	}

	@Override
	public boolean onEvent(IMediaEventArgs args) {
		//MediaApplication.logD(MediaPlayerService.class, "getMediaUpdateEventTypes() :" + args.getMediaUpdateEventTypes().toString());
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
			progressBar.setMax(duration);
			durationTimeTv.setText(formatTime(duration));
			break;
		case KMEDIA_FINISH_SAVE_RECORD:
			Toast.makeText(context, context.getString(R.string.screen_kmedia_player_ktv_end),Toast.LENGTH_SHORT )
			.show();
			if(saveRecordCheckBoxFlag && ExtAudioRecorder.recordSupported){
				notify_kmedia_has_finished = true;
				dialogFlag = true;
				if(Constant.WHICH_PLAYER == 2){
					dialogConfirmSaveRecord.show();
				}
			}else{
				if(ExtAudioRecorder.getInstanse()!= null && ExtAudioRecorder.getInstanse().getIsRecording()){
					 ExtAudioRecorder.getInstanse().stop();
				 }
				ServiceManager.saveState();
				ServiceManager.isPlayed = false;
				AmtMedia.s_goPlayerBtn_click_num = -1;
				dialogQuitFlag = true;
				if(Constant.WHICH_PLAYER == 2){
					dialogQuitCurrentKMusic.show();
				}
				//ServiceManager.finishScreenKMediaPlayer();
			}
			
			break;
		case MUSIC_UPDATE:
			abRepeatInit();
			flagMusic = STATE_PLAY;
			MediaPlayerService.flagAccompanyMode = MediaEventTypes.MEDIA_MODE_ORIGINAL;
			playBtn.setImageResource(R.drawable.screen_audio_player_pause_selector);
			audioArtist = (String) args.getExtra("audioArtist");
			audioName = (String) args.getExtra("audioName");
			audioFilePath = (String) args.getExtra("filepath");
			
			if (audioName != null) {
			    int index = audioName.lastIndexOf('.');
			    songName = audioName.substring(0,index);
			}
			uiRefresh(songName, audioArtist);
			break;
		case MUSIC_RELOAD:
			duration = (Integer) args.getExtra("durationTime");
			progressBar.setMax(duration);
			durationTimeTv.setText(formatTime(duration));
			audioArtist = (String) args.getExtra("audioArtist");
			audioName = (String) args.getExtra("audioName");
			audioFilePath = (String) args.getExtra("filepath");
			String song_Name = audioName;
			if (audioName != null) {
			    int index = audioName.lastIndexOf('.');
			    song_Name = audioName.substring(0,index);
			}
			uiRefresh(song_Name, audioArtist);
			break;
		case ACCOMPANY_ERROR:
			originalBtn.setChecked(true);
			break;
		case ORIGINAL_ERROR:
			accompanyBtn.setChecked(true);
			break;
		case ACTION_DOWN:
			if (MediaApplication.getInstance().getMicFlag()) {
//				showController();
			}
			break;
//		case AUDIO_DOWNLOAD_IMAGE_FINISH:
//			if(audioArtist.equals(args.getExtra("singerName"))){
//				String picturePath = MediaApplication.singerPicturesPath + audioArtist + IMediaService.PICTURE_SUFFIX;
//				File pictureFile = new File(picturePath);
//				if (pictureFile.exists()) {
//					singerImage = Drawable.createFromPath(picturePath);
//					MediaApplication.getInstance().getImageCache().put(audioArtist, new SoftReference<Drawable>(singerImage));
//					singerView.setBackgroundDrawable(singerImage);
//					Message message = mHandler.obtainMessage();
//					mHandler.sendMessage(message);
//				}
//			}
//			break;
		}
		return true;
	}

	@Override
	public boolean hasMenu() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean currentable() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean refresh() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		MediaPlayer mplayer = mediaPlayerService.getMediaPlayer();
		int curTime = mplayer.getCurrentPosition();
		ColorStateList colorwhite = (ColorStateList) getResources().getColorStateList(R.color.white);
		ColorStateList colorgray = (ColorStateList) getResources().getColorStateList(R.color.screen_home_tab_color);
		
		switch (checkedId) {
		case R.id.screen_ktv_tab_original:
			originalBtn.setTextColor(colorwhite);
			accompanyBtn.setTextColor(colorgray);
			eventArgs.putExtra("original_seek", curTime);
			eventArgs.putExtra("original_duration", mplayer.getDuration());
			mediaEventService.onMediaUpdateEvent(eventArgs.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_MODE_ORIGINAL));
			break;
		case R.id.screen_ktv_tab_accompany:
			originalBtn.setTextColor(colorgray);
			accompanyBtn.setTextColor(colorwhite);
			eventArgs.putExtra("accompany_seek", curTime);
			eventArgs.putExtra("accompany_duration", mplayer.getDuration());
			mediaEventService.onMediaUpdateEvent(eventArgs.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_MODE_ACCOMPANY));
			break;
		}
	}
	



	@Override
	protected void onDestroy() {
		Constant.WHICH_PLAYER = 0;
		MediaApplication.logD(context.getClass(), "onDestroy");
		mediaEventService.removeEventHandler(this);
		if (dialog != null && dialog.isShowing())
			dialog.dismiss();
		if (mControlWindow != null && mControlWindow.isShowing()){
			mControlWindow.dismiss();
		}
		unregisterReceiver(currentTimeReceiver);
	    unregisterReceiver(headsetPlugReceiver);
	    if (wakeLockThread != null && wakeLockThread.isHeld()) {
	    	wakeLockThread.release();
	    	wakeLockThread = null;
         }
	    if (wakeLockSCreen != null && wakeLockSCreen.isHeld()) {
	    	wakeLockSCreen.release();
	    	wakeLockSCreen = null;
         }
		super.onDestroy();
	}

	public void showDialog() {
		if(saveRecordCheckBoxFlag && ExtAudioRecorder.recordSupported){
			dialogConfirmSaveRecord.show();
		}else{
			dialogQuitCurrentKMusic.show();
		}
	}

	
public class HeadsetPlugReceiver extends BroadcastReceiver {
		

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.hasExtra("state")) {
			if (intent.getIntExtra("state", 0) == 0) {
				bHeadsetState = false;       //seekbar上的按钮是否监听到耳塞的标记
				bSeekbarLinkHeadset = false; //seekbar进度条是否监听到耳塞的标记
//				MediaApplication.logD(context.getClass(), "mic off");
				k_mic_img_btn
						.setBackgroundResource(R.drawable.screen_kmedia_player_mic_off);
				MediaApplication.getInstance().setMicFlag(false);
					
				if(Constant.IS_MUSIC_PAUSE_CONTROLL &&mediaPlayerService.getMediaPlayer().isPlaying()){
					pause();
				}
				
			} else if (intent.getIntExtra("state", 0) == 1) {
				bHeadsetState = true;
				bSeekbarLinkHeadset = true;
//				MediaApplication.logD(context.getClass(), "mic on");
				Toast.makeText(context, getString(R.string.screen_kmedia_player_mic_on), Toast.LENGTH_SHORT)
						.show();
				micSeekBar.setProgress(10);
				k_mic_img_btn
						.setBackgroundResource(R.drawable.screen_kmedia_player_mic_on);
				MediaApplication.getInstance().setMicFlag(true);
			}
		}

	}

}
	

	private void registerHeadsetPlugReceiver() {
		headsetPlugReceiver = new HeadsetPlugReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("android.intent.action.HEADSET_PLUG");
		ScreenKMediaPlayer.this.registerReceiver(headsetPlugReceiver, intentFilter);
	}

	@Override
	public boolean onDown(MotionEvent e) {
		
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		if (MediaApplication.getInstance().getMicFlag()) {
//			showController();
		}
		return false;
	}
	
//	private void showController() {
//		mHandler.postDelayed(update, 3000);
//		if (mControlWindow != null && !mControlWindow.isShowing()) {
//			mControlWindow.showAsDropDown(findViewById(R.id.kmedia_info_relativeLayout));
//			
//		} else {
//			mHandler.removeCallbacks(update);
//			mHandler.postDelayed(update, 3000);
//		}
//	}
	
	 @Override
	    public boolean onTouchEvent(MotionEvent event) {
	        return gestureDetector.onTouchEvent(event);
	    }
	 
	 private Runnable update = new Runnable() {
			public void run() {
				if (mControlWindow != null && mControlWindow.isShowing()){
					mControlWindow.dismiss();
				}
			}
		};
			

		@Override
		public boolean changMenuAdapter() {
			// TODO Auto-generated method stub
			MenuContentAdapter[] menuContentAdapters = ServiceManager.getMenuContentAdapters();
			menuContentAdapters[Constant.MenuConstant.userfull].getMenuData().add(Constant.MenuConstant.menu_item_search, 
					new MenuItem(R.drawable.menu_item_search, MediaApplication.getContext().getResources()
							.getString(R.string.screen_home_menu_search)));
//			menuContentAdapters[Constant.MenuConstant.userfull].getMenuData().add(Constant.MenuConstant.menu_item_delete, 
//					new MenuItem(R.drawable.menu_item_delete, MediaApplication.getContext().getResources()
//							.getString(R.string.screen_home_menu_delete)));
			menuContentAdapters[Constant.MenuConstant.userfull].getMenuData().add(Constant.MenuConstant.menu_item_song_problem - 1, 
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
			
			return true;
		}

		@Override
		public boolean isMenuChanged() {
			// TODO Auto-generated method stub
			return true;
		}
 
		
		public class CurrentTimeReceiver extends BroadcastReceiver {
			@Override
			public void onReceive(Context context, Intent intent) {
				currentPosition = intent.getIntExtra("currentTime", 0);
				progressBar.setProgress(currentPosition);
				String currentPositionString = formatTime(currentPosition);
				playTimeTv.setText(currentPositionString);
				if (abRepeatState) {
					if (currentPositionString.equals(formatTime(abRepeatEndPostion)) || currentPositionString.equals(formatTime(abRepeatEndPostion+1000))) {
						seekbarChange(abRepeatBeginPostion);
					}
				}
			}
		}
		
		private void registerCurrentTimeReceiver() {
			currentTimeReceiver = new CurrentTimeReceiver();
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction("com.amusic.media.MediaPlayerService");
			ScreenKMediaPlayer.this.registerReceiver(currentTimeReceiver, intentFilter);
		}

		@Override
		protected void onPause() {
			super.onPause();
			dialogFlag = false;
			Constant.WHICH_PLAYER = 0;
//			MediaApplication.logD(context.getClass(), "onPause");
//			if (flagMusic == STATE_PLAY) {
//				homeDownPause = true;
//				pause();
//			}
			
		}
		
		@Override
		public void onBackPressed() {
			// TODO Auto-generated method stub
			showDialog();
		}
		
}
