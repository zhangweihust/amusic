package com.android.media.services.impl;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Stack;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.audiofx.Equalizer;
import android.media.audiofx.PresetReverb;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.amusic.media.R;
import com.android.media.AmtMedia;
import com.android.media.MediaApplication;
import com.android.media.dialog.OnScreenHint;
import com.android.media.event.IMediaEventArgs;
import com.android.media.event.IMediaEventHandler;
import com.android.media.event.impl.MediaEventArgs;
import com.android.media.event.impl.MediaEventTypes;
import com.android.media.ffmpeg.CustomMediaPlayer;
import com.android.media.ffmpeg.ExtAudioRecorder;
import com.android.media.lyric.player.LyricParserFactory;
import com.android.media.lyric.player.LyricPlayer;
import com.android.media.provider.MediaDatabaseHelper;
import com.android.media.screens.IScreen.ScreenType;
import com.android.media.screens.impl.ScreenAudio;
import com.android.media.screens.impl.ScreenAudioAlbumSongs;
import com.android.media.screens.impl.ScreenAudioFavorites;
import com.android.media.screens.impl.ScreenAudioPlayer;
import com.android.media.screens.impl.ScreenAudioPlaylistSongs;
import com.android.media.screens.impl.ScreenAudioRecentlySongs;
import com.android.media.screens.impl.ScreenAudioSingerSongs;
import com.android.media.screens.impl.ScreenAudioSongLyricsFullScreen;
import com.android.media.screens.impl.ScreenAudioSongs;
import com.android.media.screens.impl.ScreenKMedia;
import com.android.media.screens.impl.ScreenKMediaPlayer;
import com.android.media.services.IMediaEventService;
import com.android.media.services.IMediaPlayerService;
import com.android.media.services.IMediaService;
import com.android.media.services.INetworkService;
import com.android.media.services.IScreenService.Mark;
import com.android.media.toolbox.DETool;
import com.android.media.utils.Constant;


public class MediaPlayerService implements IMediaPlayerService, IMediaEventHandler {
	private Map<String, Integer> playingMarks;
	private Cursor mCursor;
	private CustomMediaPlayer mediaPlayer;
	private String lastPath;
	private IMediaEventService mediaEventService;
	private IMediaService mediaService;
	private INetworkService networkService;
	private String lyricPath;
	private String accompanyPath;
	private int mediaModel = 0;
	private int previousOrNextFlag =0;
	private LyricPlayer lyricplayer;
	private TelephonyManager mTelephonyManager;
	private PhoneStateListener mPhoneStateListener;
	private boolean inCall;
	private final byte[] cLock = new byte[0];
	private static ArrayList<Integer> random = new ArrayList<Integer>();
	private static int SHUFFLE_POSITION = 0;
	private static MediaEventArgs mediaEventArgs;
	private MediaEventArgs lyricArgs = new MediaEventArgs();
	public static MediaEventTypes flagAudioPlayMode = MediaEventTypes.MEDIA_PLAY_MODE_ORDER;
	public static MediaEventTypes flagMicMode = MediaEventTypes.MEDIA_MODE_MIC_DISABLE;
	public static MediaEventTypes flagSoundMode = MediaEventTypes.MEDIA_MODE_SOUND_ON;
	public static MediaEventTypes flagAccompanyMode = MediaEventTypes.MEDIA_MODE_ORIGINAL;
	public static MediaEventTypes flagAbRepeatMode = MediaEventTypes.MEDIA_MODE_REPEAT_BEGIN;
	public static MediaEventTypes flagUiControlMode = MediaEventTypes.MEDIA_PLAYER_BOTTOM_CONTROL_PAUSE_UI;
	public static MediaEventTypes flagEqualizerLevel = MediaEventTypes.EQULIZER_LEVEL;
	public static String directoryRecord = MediaApplication.savePath + "record/";
	public static String tmpFileName = "rec_wav";
	private static int flagAccompany = 1;
	private static int defaultAccompany = 0; //选择伴奏K歌或者原唱文件不能放，第一次进切伴唱的标识
	private static int flagSilencer = 1;
	private static boolean flagHaveAccompany = true;
	public static boolean flagMusicError = true; //歌曲不存在或者歌曲不能正常播放的标志位,false表示歌曲不存在
	public static boolean hasLyric = false;
	public static ScreenType typefinal=null;
	private String screenIdfianl=null;
	private int position_next;
	private DesktopLyricService desktopLyric = DesktopLyricService.getInstance();
	public static String lyrics;
	IMediaEventArgs args = new MediaEventArgs();
	private static String screenId;
	private static int    MediaPlayerErrorFlag=0;  //播放中出错的标识，用于监听setOnErrorListener的计数
	private static int    cursorPosition;   //歌在列表中当前的位置
	private static Integer    changeHighlight;
	private boolean desktopFlag = false;
	private double[] level=new double[5];
	private int mode;
	Handler mHandler;
	private Equalizer mEqualizer;
	private PresetReverb mPresetReverb;
	public static boolean eqInitState = true;
	double defaultlevel[][]={{300,0,0,0,300},{500,300,-200,400,400},{600,0,200,400,100},
			 {0,0,0,0,0},{300,0,0,200,-100},{400,100,900,300,0},{500,300,0,100,300},
			 {400,200,-200,200,500},{-100,200,500,100,-200},{500,300,-100,300,500}};
	Intent intent = new Intent();
	private float mRatio = (float) 1.0;
	private OnScreenHint mOnScreenHint;    //创建可控制的提示框	
	public static boolean isKMdieaOrRecord = false;
	private boolean isNeedDownloadLyric = false;
	
	private Runnable update = new Runnable() {
		public void run() {
			int position = (int) (mediaPlayer.getCurrentPosition()* mRatio);
			if (isNeedDownloadLyric && position > 2000) {
				mediaEventService.onMediaUpdateEvent(lyricArgs.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_LYRIC));
				isNeedDownloadLyric = false;
			}
			intent.setAction("com.amusic.media.MediaPlayerService");
			intent.putExtra("currentTime", position);
			MediaApplication.getContext().sendBroadcast(intent);
			mHandler.postDelayed(update, 1000);
		}
	};

	@Override
	public boolean start() {
		mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_audio_player_mode_order));
		mHandler = new Handler();
		mediaEventArgs = new MediaEventArgs();
		mediaEventService = ServiceManager.getMediaEventService();
		mediaService = ServiceManager.getMediaService();
		networkService = ServiceManager.getNetworkService();
		mediaPlayer = new CustomMediaPlayer();
		if(Integer.parseInt(Build.VERSION.SDK)>=9){
			try{
				mEqualizer = new Equalizer(0, mediaPlayer.getAudioSessionId());
				mEqualizer.setEnabled(true);
				setEqualizer();
				mPresetReverb = new PresetReverb(0,mediaPlayer.getAudioSessionId());
				mPresetReverb.setEnabled(true);
				setReverb();				
			}
			catch(Exception e){
				e.printStackTrace();
				eqInitState=false;
				MediaApplication.logD(MediaPlayerService.class, "Equalizer init exception------>"+e);
			
			}
			
		}
		
		playingMarks = new HashMap<String, Integer>();	
		//将本地存储的高亮信息加载到内存中
		SharedPreferences spf = MediaApplication.getContext().getSharedPreferences("Data",Context.MODE_WORLD_WRITEABLE);
		int length = spf.getInt("playingMarksLength", -1);
		String key = null;
		Integer value = null;
		String entry = null;
		for(int i = length - 1; i >= 0; i--){
			entry = spf.getString("playingMarks:"+i, null);
			key = entry.split(":")[0];
			if("null".equals(entry.split(":")[1])){
				value = -1;
			}else{
				value = Integer.parseInt(entry.split(":")[1]);
			}
			playingMarks.put(key, value);
		}
		
		LyricParserFactory factory = new LyricParserFactory();
		lyricplayer = new LyricPlayer(factory, mediaPlayer);
		try {
			mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mp) {
					synchronized (cLock) {
						String song ="";
						String singer="";
						String path="";
						int id =0;
						changeHighlight = IMediaPlayerService.NEED_TO_CHANGE_HIGHLIGHT;
						if (mCursor!=null&&mCursor.getCount()!=0) {
							id = mCursor.getInt(mCursor
									.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ID));
							song = mCursor.getString(mCursor
									.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_DISPALYNAME));
							singer = mCursor.getString(mCursor
									.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ARTISTNAME));
							path = mCursor.getString(mCursor
									.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_FILEPATH));
						}
						else if(mediaModel == MEDIA_MODEL_KMEDIA){
							song=ScreenKMedia.bzsongname;
							singer=ScreenKMedia.bzplayername;
							path = MediaApplication.accompanyPath +ScreenKMedia.bzplayername+ScreenKMedia.bzsongname+".bz";
						}
						args.putExtra("id", id);
						args.putExtra("song", song);
						args.putExtra("singer", singer);
						if (flagUiControlMode == MediaEventTypes.MEDIA_PLAYER_BOTTOM_CONTROL_PLAY_UI) {
							if (mediaModel != MEDIA_MODEL_KMEDIA) {
								if (flagAudioPlayMode == MediaEventTypes.MEDIA_PLAY_MODE_SINGLE_REPEAT) {
									mediaEventService.onMediaUpdateEvent(args
											.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAY_MODE_SINGLE_REPEAT));
								}
							}
							mediaPlayer.start();
							
							if (AmtMedia.s_goPlayerBtn_click_num == 0) {
								mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_PAUSE));
								AmtMedia.s_goPlayerBtn_click_num++;
							}
							
						}
						if (mediaModel != MEDIA_MODEL_KMEDIA) {
							if(path.contains("aMusic")){
								mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.ADD_TO_COUNT_LOCAL));
							}
						} else {
							mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.ADD_TO_COUNT_KMEDIA));
						}
//						mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.LYRIC_PLAYER_START));
						mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_BEEN_SINGING));
						sendAudioDuration();
						mHandler.removeCallbacks(update);
						mHandler.post(update);
						mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
					}
				}
			});
			mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

				@Override
				public void onCompletion(MediaPlayer mp) {
					if (mediaModel == MEDIA_MODEL_KMEDIA) {
						if(ScreenKMediaPlayer.stopMedia_save_as_record == true){
							ScreenKMediaPlayer.stopMedia_save_as_record =false;
							
						}else{
							mediaEventService.onMediaUpdateEvent(args
								.setMediaUpdateEventTypes(MediaEventTypes.KMEDIA_FINISH_SAVE_RECORD));
						}
					}

				  mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_BOTTOM_CONTROL_STOP_UI));
					if (flagMusicError == false) {
						ServiceManager.getAmtMediaHandler().post(new Runnable() {
							@Override
							public void run() {
								if(mOnScreenHint!=null){
								    mOnScreenHint.cancel();
								}
								mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_audio_player_is_error_one));
								mOnScreenHint.show();
							}
						});
						return;
					}
    			  if (mCursor != null) {
						Integer id = (Integer) args.getExtra("id");
						if(id != null && typefinal != ScreenType.TYPE_RECORD){
						switch (flagAudioPlayMode) {
						case MEDIA_PLAY_MODE_SHUFFLE:	
							/*if(mCursor.getCount()>1){*/
							flagAudioPlayMode = MediaEventTypes.MEDIA_PLAY_MODE_SHUFFLE;
							args.putExtra("screenType", typefinal);
							args.putExtra("screenId", screenIdfianl);
							args.putExtra("position", position_next);
							mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_START));
							/*}*/
							break;

						case MEDIA_PLAY_MODE_LIST_CYCLE:
							flagAudioPlayMode = MediaEventTypes.MEDIA_PLAY_MODE_LIST_CYCLE;
							if(MediaPlayerErrorFlag==0||mCursor.getCount()>1){
									synchronized (cLock) {
										if (mCursor.moveToNext()) {
											cursorPosition++;
											playSong();
											
										} else {
											cursorPosition = 0;
											mediaEventService.onMediaUpdateEvent(args
													.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_TO_FIRST));
										}
									}
							}
							break;

						case MEDIA_PLAY_MODE_ORDER:
							flagAudioPlayMode = MediaEventTypes.MEDIA_PLAY_MODE_ORDER;
							if (!mCursor.isLast())
								mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_NEXT));
							break;

						case MEDIA_PLAY_MODE_SINGLE_REPEAT:
							if(MediaPlayerErrorFlag==0){
							    flagAudioPlayMode = MediaEventTypes.MEDIA_PLAY_MODE_SINGLE_REPEAT;
							    mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAY_MODE_SINGLE_REPEAT));
							}
							break;
						}
					}else if(id != null && typefinal == ScreenType.TYPE_RECORD){
						  mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_RECORD_MUSIC_PLAY_STOP));
					}
				}
				}
			});
			

			mediaPlayer.setOnErrorListener(new OnErrorListener() {
				public boolean onError(MediaPlayer mp, int what, int extra) {		
					//如果播放出错，则需要相应的停掉歌词部分，否则会一直出错；而且把错误标识增加，以免只有一首歌的情况播放错误会死循环；
					MediaPlayerErrorFlag++;
					if (lyricplayer.isStarted()) {
						lyricplayer.stopLyricPlayer();
					}
					return false;
				}
			});
			
            mediaPlayer.setOnSeekCompleteListener(new OnSeekCompleteListener() {	
				public void onSeekComplete(MediaPlayer mp) {
					mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.LYRIC_PLAYER_SEEK));
				}
			});
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
		mediaEventService.addEventHandler(this);
		mTelephonyManager = (TelephonyManager) MediaApplication.getContext().getSystemService(Context.TELEPHONY_SERVICE);
		mPhoneStateListener = new PhoneStateListener() {

			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				MediaApplication.logD(MediaPlayerService.class, "onCallStateChanged");
				if (state == TelephonyManager.CALL_STATE_RINGING || state == TelephonyManager.CALL_STATE_OFFHOOK) {
					if (mediaPlayer.isPlaying()) {
						inCall = true;
						mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_PAUSE));
					}
					if(desktopLyric.getVisibility() == View.VISIBLE){
						desktopLyric.setVisible(View.GONE);
						desktopFlag = true;
					}
				} else if (state == TelephonyManager.CALL_STATE_IDLE) {
					if (inCall) {
						inCall = false;
						mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_CONTINUE));
					}
					if(desktopFlag){
						desktopLyric.setVisible(View.VISIBLE);
						desktopFlag = false;
					}
				}
			}

		};
		return true;
	}

	@Override
	public boolean stop() {
		if (lyricplayer.isStarted()) {
			lyricplayer.stopLyricPlayer();
		}
		if (mCursor != null) {
			mCursor.close();
			mCursor = null;
		}
		lastPath = null;
		random.clear();
		if(ExtAudioRecorder.getInstanse().getAudioRecordObj()!=null){
			ExtAudioRecorder.getInstanse().getAudioRecordObj().release();
		}
		if(ExtAudioRecorder.getInstanse().getAudioTrackObj()!=null){
			ExtAudioRecorder.getInstanse().getAudioTrackObj().release();
		}
		savePlayingMarks();
		mediaPlayer.stop();
		mediaPlayer.reset();
		mediaPlayer.release();	
		if(Integer.parseInt(Build.VERSION.SDK)>=9 ){
			if( mEqualizer!=null)
				mEqualizer.release();			
			if (mPresetReverb!=null)
				mPresetReverb.release();
		}	
		
		mediaEventService.removeEventHandler(this);
		mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
		return true;
	}

	public void savePlayingMarks(){
		Map<String, Integer> playingMarks = ServiceManager.getMediaplayerService().getPlayingMarks();
		SharedPreferences spf = MediaApplication.getContext().getSharedPreferences("Data",Context.MODE_WORLD_WRITEABLE);
		Editor editor = spf.edit();
		Iterator<Entry<String,Integer>> iterator = playingMarks.entrySet().iterator();
		Entry<String,Integer> entry = null;
		int i = 0;
		while (iterator.hasNext()) {
			entry = iterator.next();
			entry.getKey();
			editor.putString("playingMarks:"+i, entry.getKey()+":"+entry.getValue()).commit();
			i++;
		}
		editor.putInt("playingMarksLength", i).commit();
	}
	
	private void playSong() {
		flagMusicError =true;
		synchronized (cLock) {
			if (mediaModel == MEDIA_MODEL_KMEDIA){
				ScreenKMediaPlayer.stopMedia_save_as_record = false;//reset the flag of ScreenKMediaPlayer.
			}
			MediaPlayerErrorFlag =0;
			if(changeHighlight == IMediaPlayerService.NEED_TO_CHANGE_HIGHLIGHT && typefinal != ScreenType.TYPE_RECORD){
				if (mCursor != null && mCursor.getCount() != 0) {
					int id = mCursor
							.getInt(mCursor
									.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ID));
					String path = mCursor.getString(mCursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_FILEPATH));
					if (screenId == null) {
						if (playingMarks.containsKey(ScreenAudioSongs.class.getCanonicalName())) {
							if (playingMarks.get(ScreenAudioSongs.class.getCanonicalName()) != null) {
								screenId = ScreenAudioSongs.class.getCanonicalName();
							}
						} else if (playingMarks.containsKey(ScreenAudioSingerSongs.class.getCanonicalName())) {
							if (playingMarks.get(ScreenAudioSingerSongs.class.getCanonicalName()) != null) {
								screenId = ScreenAudioSingerSongs.class.getCanonicalName();
							}
						} else if (playingMarks.containsKey(ScreenAudioAlbumSongs.class.getCanonicalName())) {
							if (playingMarks.get(ScreenAudioAlbumSongs.class.getCanonicalName()) != null) {
								screenId = ScreenAudioAlbumSongs.class.getCanonicalName();
							}
						} else if (playingMarks.containsKey(ScreenAudioPlaylistSongs.class.getCanonicalName())) {
							if (playingMarks.get(ScreenAudioPlaylistSongs.class.getCanonicalName()) != null) {
								screenId = ScreenAudioPlaylistSongs.class.getCanonicalName();
							}
						} else if (playingMarks.containsKey(ScreenAudioFavorites.class.getCanonicalName())) {
							if (playingMarks.get(ScreenAudioFavorites.class.getCanonicalName()) != null) {
								screenId = ScreenAudioFavorites.class.getCanonicalName();
							}
						} else if (playingMarks.containsKey(ScreenAudioRecentlySongs.class.getCanonicalName())) {
							if (playingMarks.get(ScreenAudioRecentlySongs.class.getCanonicalName()) != null) {
								screenId = ScreenAudioRecentlySongs.class.getCanonicalName();
							}
						} 
					}
					File f = new File(path);
					if(f.exists()){
						playingMarks.put(screenId, id);
					}
					mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_UPDATE_UI_HIGHTLIGHT));
				}
			}
			mRatio = 1;
			flagAccompany = 1;
			flagHaveAccompany = true;
			mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_BOTTOM_CONTROL_PLAY_UI));
			flagUiControlMode = MediaEventTypes.MEDIA_PLAYER_BOTTOM_CONTROL_PLAY_UI;
			mediaEventArgs.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_BOTTOM_CONTROL_PLAY_UI);
			String path="";
			//判断K歌模式中选择的是原唱K歌还是伴奏K歌，如果是原唱K歌，则进一步判断原唱是否存在，如果不存在，则按规则拼出原唱名字，方便走下面的流程；
			//如果存在，则从数据库中取出对应的原唱资源
			//如果是伴奏K歌，则拼一个不存在的地址，让其下面直接prepare失败，走catch
			if(ScreenKMediaPlayer.isOriginal==true){
				if (mCursor != null && mCursor.getCount() != 0) {
					path = mCursor
							.getString(mCursor
									.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_FILEPATH));
				} else {
					path = MediaApplication.savePath
							+ ScreenKMedia.bzplayername
							+ ScreenKMedia.bzsongname ;
				}
			}else{	
				path = MediaApplication.savePath
					+ ScreenKMedia.bzplayername
					+ ScreenKMedia.bzsongname;	
			}
			// 如果前一次是点“正在播放“按钮进去
//			int position = -1;
//			if (mediaModel == MEDIA_MODEL_LOCAL && AmtMedia.s_goPlayerBtn_click_num == 1) {
//				SharedPreferences sharedata = ServiceManager.getAmtMedia().getSharedPreferences("lastsong", 0);
//				position = sharedata.getInt("position", 0);
//				AmtMedia.s_goPlayerBtn_click_num++;
//			}
			
			//需要修改判断本地歌曲和K歌的模式，需要修改lastpath，判断是否在同一个列表
			boolean isDiffSong = !path.equals(lastPath);
			mediaPlayer.setAudioFilePath(path);

			/*
			 * 第一个条件：如果两首歌的路径明不同，且与上一次退出前最后播放的歌曲不同则肯定重新播放
			 * 第二个条件：mediaModel == MEDIA_MODEL_KMEDIA K歌界面重新播放
			 * 第三个条件：previousOrNextFlag==1 发了 上一首/下一首消息 重新播放
			 * 第四个条件：首先不是录音界面进去，如果不满足第一个条件，但是是从不同的分类中进去的（全部歌曲，歌手分裂，专辑分类等等）需要重新播放
			 * 第五个条件：首先不是录音界面进去，如果不满足第一个条件，也是从相同的分类中进去的，但是列表id不同。也需要重新播放
			 */

			if (isDiffSong
					|| mediaModel == MEDIA_MODEL_KMEDIA 
					|| previousOrNextFlag==1
					|| (typefinal == ScreenType.TYPE_AUDIO && !isDiffSong && ServiceManager.methodName != null && !ServiceManager.methodName.equals(ServiceManager.lastMethodName))
					|| (typefinal == ScreenType.TYPE_AUDIO && !isDiffSong && ServiceManager.methodName != null && ServiceManager.methodName.equals(ServiceManager.lastMethodName )
							&& ServiceManager.listId != null && !ServiceManager.listId.equals(ServiceManager.lastListId))
					) {
				previousOrNextFlag=0;
//				if (mediaPlayer != null) {
//					mediaPlayer.stop();
//				}

				lastPath = path;
				mTelephonyManager.listen(null, PhoneStateListener.LISTEN_CALL_STATE);
				//判断传入的地址是否能播放，如果不能播放，则走catch，如果能放，则这时判断是否是随机模式，如果是，则计算随机的下一首歌歌名；
				try {
					
					if (lyricplayer != null && lyricplayer.isStarted()) {
						lyricplayer.stopLyricPlayer();
					}
					mediaPlayer.reset();
					mediaPlayer.setDataSource(path);
					mediaPlayer.prepare();
					changePosition();
					mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.LYRIC_PLAYER_START));
					defaultAccompany =1;
					if (mediaModel != MEDIA_MODEL_KMEDIA) {
						if (typefinal == ScreenType.TYPE_AUDIO) {
							ServiceManager.position = cursorPosition;
						} 					
						if (flagAudioPlayMode == MediaEventTypes.MEDIA_PLAY_MODE_SHUFFLE) {
							shuffleNum();
						}
					}
				} catch (Exception e) {
					//播放失败，判断当前的模式，如果不是K歌模式，然后播放下一首；这里会进一步判断是否是随机播放模式，如果是，则重新计算一个新的下一首，然后播放下一首；
					//这里还判断了当前的列表是否大于1；如果只有1首或者不存在，则直接弹出提示，说明此歌曲不能播放；
					if (mediaModel != MEDIA_MODEL_KMEDIA && typefinal == ScreenType.TYPE_AUDIO) {
						if(mCursor.getCount()>1){		
					    changePosition();
						ServiceManager.getAmtMediaHandler().post(new Runnable() {
								@Override
								public void run() {
									if(mOnScreenHint!=null){
									    mOnScreenHint.cancel();
									}
									mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_audio_player_is_error_one));
									mOnScreenHint.show();
								}
							});
							
						if (flagAudioPlayMode == MediaEventTypes.MEDIA_PLAY_MODE_SHUFFLE) {
							shuffleNum();
						}
						flagMusicError =false;
//						mediaEventService
//								.onMediaUpdateEvent(args
//										.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_NEXT));
						}else{	
							ServiceManager.getAmtMediaHandler().post(new Runnable() {
								@Override
								public void run() {
									if(mOnScreenHint!=null){
									    mOnScreenHint.cancel();
									}
									mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_music_is_error_one));
									mOnScreenHint.show();
								}
							});

						}
					}
					//如果是K歌模式，则直接调切伴奏操作；同时把defaultAccompany标识置为0，方便后面发送歌词信息及录音信息；
					else{	
						if(ScreenKMediaPlayer.isOriginal==true){
						Toast.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_audio_player_no_original),
								Toast.LENGTH_SHORT).show();
						}
						defaultAccompany =0;
						mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.ORIGINAL_ERROR));
					}
				}
			}// 需要修改lastpath，判断是否在同一个列表；如果是同一个位置同一首，则继续播放；
			else if (path.equals(lastPath) && !mediaPlayer.isPlaying()) {
				sendAudioDuration();
				mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_CONTINUE));
			} 
			else {
				reloadAudioInfo();
			}
		}
	}

	public void changeCorsor(Cursor cursor, int mediaModel) {
		this.mediaModel = mediaModel;
		mCursor = cursor;
		random.clear();
	}
	
	public Cursor getCursor() {
		return mCursor;
	}

	@Override
	public CustomMediaPlayer getMediaPlayer() {
		return mediaPlayer;
	}

	public static String splitTitle(String s) {
		return s.replace("'", "").replace("‘", "").replace(",", "-")
				.replace(",", "-").replace("，", "-").replace(".", "")
				.replace("。", "-").replace(";", "-").replace("；", "-")
				.replace(":", "-").replace("：", "-").replace("(", "-")
				.replace(")", "-").replace("（", "-").replace("）", "-")
				.replace("[", "-").replace("]", "-").replace("【", "-")
				.replace("】", "-").replace("{", "-").replace("}", "-")
				.replace("『", "-").replace("』", "-").replace("<", "-")
				.replace(">", "-").replace("《", "-").replace("》", "-")
				.replace("\"", "-").replace("“", "-").replace("”", "-")
				.replace("_", "-").replace("——", "-").replace("—", "-")
				.replace("+", "-").replace("|", "-").replace("&", "-")
				.replace("$", "-");
	}
	
	@Override
	public boolean onEvent(final IMediaEventArgs args) {
		switch (args.getMediaUpdateEventTypes()) {
		case AUDIO_DOWNLOAD_LYRICS_FINISH_UI:
			if (lyricPath.equals((String) args.getExtra("lyricPath"))) {
				mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.LYRIC_PLAYER_START));
			}
			break;
		case LYRIC_PLAYER_START:
			synchronized (cLock) {
				if (lyricplayer.isStarted()) {
					lyricplayer.stopLyricPlayer();
				}
				lyricplayer.clearLyricInfo();
				long duration =0;
				String name="";
				String filePath = "";
				
				// 原唱存在的情况则首先得到歌曲的歌名等信息，以供下载歌词
				if (mCursor!=null &&mCursor.getCount()!=0) {
					duration = mCursor
							.getLong(mCursor
									.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_DURATION));
					name = mCursor
							.getString(mCursor
									.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_DISPALYNAME));
					name = name.substring(0, name.lastIndexOf("."));
					filePath = mCursor
					.getString(mCursor
							.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_FILEPATH));
				}
				else{
					name =ScreenKMedia.bzplayername+ScreenKMedia.bzsongname;
				}
				if (typefinal == ScreenType.TYPE_RECORD) {

					break;
				}
				lyricPath = MediaApplication.lyricPath + name + IMediaService.LYRICS_SUFFIX;
				if(findFile(lyricPath)){
					/* final String */lyrics = new String(DETool.nativeGetKsc(lyricPath));
				}else{
					lyrics=null;
				}
				
				// 若本地不存在这首歌的歌词则去下载
				if (lyrics == null) {
					MediaPlayerService.hasLyric = true;
					
					if(Constant.PROHIBITED_TO_DOWNLOAD_LYRIC == Constant.LYRIC_DOWNLOAD){
						break;
					} else if (Constant.ALLOWED_TO_DOWNLOAD_LYRIC_WITH_WIFI == Constant.LYRIC_DOWNLOAD) {
						if(!(networkService.acquire(false) && networkService.getNetType() == ConnectivityManager.TYPE_WIFI)) {
							break;
						}
					}
					File kscFile = new File(lyricPath);
					kscFile.delete();
					lyricplayer.clearLyricInfo();
					
					if (!desktopLyric.isAmtMedia()){
						if(Constant.IS_SHOW_DESKTOP_LYRIC){
							if(!Constant.IS_DESKTOP_LYRIC_EXIT){
								desktopLyric.setVisible(View.VISIBLE);
							}
						}
					}else{
						desktopLyric.setVisible(View.GONE);
					}

					String[] strs = name.split("-");
					String song = "";
					String singer = "";
					
				    boolean tagLocal = false;
				    // 读取数据库中歌名与歌手名，来自mp3 Tag信息
					String tagsong = "";
					String tagsinger = "";
					
					// 读取数据库中歌名与歌手名，来自mp3 Tag信息
					if (mCursor!=null &&mCursor.getCount()!=0) {
					    tagsong = mCursor.getString(mCursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_SONGNAME));
					    tagsinger = mCursor.getString(mCursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ARTISTNAME));
					}
					
					
					// 首先判断歌名，是不是符合我们对歌曲的命名方式，若符合则用歌名直接搜索歌词
				    if (strs.length == 2) {
						singer = strs[0];
						singer = singer.replace("_", "/");
						song = strs[1];
						if(singer.equals(tagsinger) && song.equals(tagsong)) {
							tagLocal = true;
						}
					} 
                    if(!tagLocal) {					
						// 不是通过AM下载的歌曲
						
						// 获取文件名/tag并分割,将分割后的合并
						song = splitTitle(tagsong) + "-" + splitTitle(name);
						
						if(tagsinger.equals("<unknown>")) {
							tagsinger = "";	
						}
						singer = tagsinger.replace("_", "/");
				
					}
					/*args.putExtra("lyricPath", lyricPath);
					args.putExtra("song_Name", song);
					args.putExtra("singer_Name", singer);
					args.putExtra("duration", duration);
		            args.putExtra("filename", name);
		            args.putExtra("audiofilePath", filePath);
		            args.putExtra("isNeedPopDialog", false);
				    args.putExtra("isKmedia", mediaModel == MEDIA_MODEL_KMEDIA);*/
				    lyricArgs.putExtra("lyricPath", lyricPath);
				    lyricArgs.putExtra("song_Name", song);
				    lyricArgs.putExtra("singer_Name", singer);
				    lyricArgs.putExtra("duration", duration);
				    lyricArgs.putExtra("filename", name);
				    lyricArgs.putExtra("audiofilePath", filePath);
				    lyricArgs.putExtra("isNeedPopDialog", true);
				    lyricArgs.putExtra("isKmedia", mediaModel == MEDIA_MODEL_KMEDIA);
				    isNeedDownloadLyric = true;
				    
				    args.putExtra("show", false);
					mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_LYRICS_ERROR));
//					mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_LYRIC));
				} else {				
					// 若本地存在歌词则直接加载歌词
					lyricplayer.prepareLyricPlayer(lyrics, ".ksc");
					lyricplayer.startLyricPlayer();
					MediaPlayerService.hasLyric = false;
					isNeedDownloadLyric = false;
					ServiceManager.getAmtMediaHandler().post(new Runnable() {
						@Override
						public void run() {
							ScreenAudioPlayer.textView.setVisibility(View.INVISIBLE);
							ScreenAudioSongLyricsFullScreen.isshowing = false;
						}
					});
					
					
					if (!desktopLyric.isAmtMedia()) {
						if(Constant.IS_SHOW_DESKTOP_LYRIC){
							if(!Constant.IS_DESKTOP_LYRIC_EXIT){
								desktopLyric.setVisible(View.VISIBLE);
							}
						}
					}else{
						desktopLyric.setVisible(View.GONE);
					}
				}
			}
			break;
		case LYRIC_ERROR_DELETE:
			String name="";			
			// 原唱存在的情况则首先得到歌曲的歌名等信息，以供下载歌词
			if (mCursor!=null &&mCursor.getCount()!=0) {
				name = mCursor
						.getString(mCursor
								.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_DISPALYNAME));
				name = name.substring(0, name.lastIndexOf("."));	
			}
			else{
				name =ScreenKMedia.bzplayername+ScreenKMedia.bzsongname;
			}
			if (typefinal == ScreenType.TYPE_RECORD) {
				break;
			}
			lyricPath = MediaApplication.lyricPath + name + IMediaService.LYRICS_SUFFIX;
			
			if (findFile(lyricPath)) {
				//Log.d("=EEE=","LYRIC_ERROR_DELETE");
				File kscFile = new File(lyricPath);
				kscFile.delete();
			}
			
//			mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.LYRIC_PLAYER_START));
		case LYRIC_PLAYER_STOP:
			if (lyricplayer.isStarted()) {
				lyricplayer.stopLyricPlayer();
			}
			break;
		case LYRIC_PLAYER_PAUSE:
			if (lyricplayer.isStarted()) {
				lyricplayer.pauseLyricPlayer();
			}
			break;
		case LYRIC_PLAYER_CONTINUE:
			if (lyricplayer.isStarted()) {
				lyricplayer.resumeLyricPlayer();
			}
			break;
		case LYRIC_PLAYER_SEEK:
			if (lyricplayer != null) {
				lyricplayer.seekLyricPlayer();
			}
			break;
		case AUDIO_RELOAD_INFO:
			if (typefinal == ScreenType.TYPE_AUDIO) {
				reloadAudioInfo();
			}
			break;
		case MEDIA_PLAYER_START:
			synchronized (cLock) {
				Integer id = (Integer) args.getExtra("id");
				int position = (Integer) args.getExtra("position");
				ScreenType type1 = (ScreenType) args.getExtra("screenType");
				String screenId1 = (String) args.getExtra("screenId");
				//保存当前的type；以判断当前是处于什么模式；
				if(type1!=null){
					typefinal=type1;
				}else{
					type1 =typefinal;
				}
				//保存当前的播放位置；以备关闭后下次播放直接播放此位置；
				cursorPosition = position;
				//保存当前的screenId；以备关闭后下次播放直接播放此位置；
				if(screenId1!=null){
					screenIdfianl=screenId1;
				}else{
					screenId1=screenIdfianl;
				}
				//如果是K歌，则列表只有一条记录，直接播放当前的；如果不是K歌，则move到当前的位置，且做高亮显示；
				if(id == null){
					if (mCursor.moveToFirst()) {
						changeHighlight = IMediaPlayerService.DONT_CHANGE_HIGHLIGHT;
						playSong();
					}else{
						changeHighlight = IMediaPlayerService.DONT_CHANGE_HIGHLIGHT;
						playSong();
					}
				} else {
					if (mCursor.moveToPosition(position)) {
						if(typefinal == ScreenType.TYPE_RECORD){
							changeHighlight = IMediaPlayerService.DONT_CHANGE_HIGHLIGHT;
							playSong();
						} else {
							if(args.getExtra("changeHighlight") == null){
								mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_PLAYING_MARKS));
								changeHighlight = IMediaPlayerService.NEED_TO_CHANGE_HIGHLIGHT;
							} else {
								args.putExtra("changeHighlight", null);
								changeHighlight = IMediaPlayerService.DONT_CHANGE_HIGHLIGHT;
							} 
							playSong();
						}
						
					}
				}
				
			}
			break;
		case MEDIA_PLAYER_PAUSE:
			if (mediaPlayer.isPlaying()) {
				mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.LYRIC_PLAYER_PAUSE));
				mediaPlayer.pause();
				
				if(ExtAudioRecorder.getInstanse()!=null){
					ExtAudioRecorder.getInstanse().pause();
				}
				mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_BOTTOM_CONTROL_PAUSE_UI));
				flagUiControlMode = MediaEventTypes.MEDIA_PLAYER_BOTTOM_CONTROL_PAUSE_UI;
				mediaEventArgs.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_BOTTOM_CONTROL_PAUSE_UI);
			}
			break;
		case MEDIA_PLAYER_CONTINUE:
			if (!mediaPlayer.isPlaying()) {
				mediaPlayer.start();
				if(ExtAudioRecorder.getInstanse()!=null){
					ExtAudioRecorder.getInstanse().resume();
				}
				mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_BOTTOM_CONTROL_PLAY_UI));
				flagUiControlMode = MediaEventTypes.MEDIA_PLAYER_BOTTOM_CONTROL_PLAY_UI;
				mediaEventArgs.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_BOTTOM_CONTROL_PLAY_UI);
				mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.LYRIC_PLAYER_CONTINUE));
			}
			break;
		case MEDIA_PLAYER_STOP:
			lastPath = null;
			//mediaPlayer.stop();
			mediaPlayer.reset();
			if (lyricplayer != null) {
			    lyricplayer.clearLyricInfo();
			}
			mTelephonyManager.listen(null, PhoneStateListener.LISTEN_CALL_STATE);
			mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_BOTTOM_CONTROL_STOP_UI));
			mediaEventArgs.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_BOTTOM_CONTROL_STOP_UI);
			mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.LYRIC_PLAYER_STOP));
			//将通知中正在播放的信息去掉
			((NotificationService)ServiceManager.getNotificatioservice()).setCurSongPrompt("");
			break;
		//增加了一个上一首下一首的标识，只要这个标识为1，则重新加载歌曲；	
		case MEDIA_PLAYER_PREVIOUS:
			synchronized (cLock) {
				previousOrNextFlag =1;
				if (mCursor != null)
					if (flagAudioPlayMode == MediaEventTypes.MEDIA_PLAY_MODE_SHUFFLE) {
						args.putExtra("screenType", typefinal);
						args.putExtra("screenId", screenIdfianl);
						int next = random.indexOf(mCursor.getPosition())-1;
						if(next < 0){
							
						}else{
							args.putExtra("id", random.get(next));
							args.putExtra("position", random.get(next));
							cursorPosition = random.get(next);
							if (mCursor.moveToPosition(random.get(next))) {
								playSong();
							}
							//mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_START));
						}
					}else {
						if (mCursor.moveToPrevious()) {
							cursorPosition--;
							playSong();
						} else {
							if (mCursor.moveToLast()) {
								cursorPosition = mCursor.getCount() - 1;
								playSong();
							}
						}
					}

			}
			break;
		//增加了一个上一首下一首的标识，只要这个标识为1，则重新加载歌曲；
		case MEDIA_PLAYER_NEXT:
			changeHighlight = IMediaPlayerService.NEED_TO_CHANGE_HIGHLIGHT;
			synchronized (cLock) {				
				if(mediaModel == MEDIA_MODEL_KMEDIA)
					break;
				previousOrNextFlag =1;
				if (mCursor != null)
					if (flagAudioPlayMode == MediaEventTypes.MEDIA_PLAY_MODE_SHUFFLE) {
						args.putExtra("screenType", typefinal);
						args.putExtra("id", position_next);
						args.putExtra("screenId", screenIdfianl);
						args.putExtra("position", position_next);
						cursorPosition = position_next;
						if (mCursor.moveToPosition(position_next)) {
							playSong();
						}
						//mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_START));
					}
					else {
						if (mCursor.moveToNext()) {
							cursorPosition++;
							playSong();
						} else {
							if (mCursor.moveToFirst()) {
								cursorPosition = 0;
								playSong();
							}
						}
					}
			}
			break;
		case MEDIA_PLAYER_TO_FIRST:
			synchronized (cLock) {
				if (mCursor != null)
					if (mCursor.moveToFirst()) {
						cursorPosition = 0;
						playSong();
					}
			}
			break;
		case MEDIA_PLAYER_MOVE_PROGRESS:
			int seek = (Integer) args.getExtra("seek");
			if(seek<0){
				seek =0;
			}			 
			mediaPlayer.seekTo(seek);

			break;
		case MEDIA_PLAYER_MIC_VOLUEM_MOVE_PROGRESS:
			float mic_voluem_seek = (Float) args.getExtra("seek");
			mediaPlayer.setKTVMicrophoneVolumeLocal(mic_voluem_seek, mic_voluem_seek);
			break;
		case MEDIA_MODE_ACCOMPANY:
			changeAccompany(args);			
			break;
		case MEDIA_MODE_ORIGINAL:
			changeOriginal(args);			
			break;
		case MEDIA_MODE_REPEAT:
			mediaPlayer.seekTo(0);
			mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_CONTINUE));
			break;
		//点击播放模式切换时会发送下一首的提示消息；	
		case MEDIA_PLAY_MODE_SINGLE_REPEAT:
			if(typefinal != ScreenType.TYPE_RECORD){
			mediaPlayer.setLooping(true);
			synchronized (cLock) {
				if (mCursor != null) {
					sendAudioDuration();
				}
			}			
			}
			break;
		//点击播放模式切换时会发送下一首的提示消息；	
		case MEDIA_PLAY_MODE_SINGLE_REPEAT_CANCLE:
			mediaPlayer.setLooping(false);
			synchronized (cLock) {
				if (mCursor != null) {
					sendAudioDuration();
				}
			}
			break;
		//增加了一个点击随机模式的消息响应；如果是随机模式，则会算一个随机位置，方便下一首播放；	
		case MEDIA_PLAY_MODE_SHUFFLE_REPEAT:
			mediaPlayer.setLooping(false);
			synchronized (cLock) {
				if (mCursor != null) {
					shuffleNum();
					sendAudioDuration();
				}
			}
			break;
		case MEDIA_PLAYER_INFO_REFRESH:
			synchronized (cLock) {
				if (mCursor != null) {
					sendAudioDuration();
				}
			}
			break;
		case AUDIO_PLAYING_MARKS:
			ScreenType type = (ScreenType) args.getExtra("screenType");
			Stack<Mark> marks = null;
			screenId = (String) args.getExtra("screenId");
			switch (type) {
			case TYPE_AMT:
				marks = ServiceManager.getAmtScreenService().getMarks();
				break;
			case TYPE_AUDIO:
				marks = ServiceManager.getAudioScreenService().getMarks();
				break;
			case TYPE_KMEDIA:
				marks = ServiceManager.getKMediaScreenService().getMarks();
			case TYPE_RECORD:
				marks = ServiceManager.getRecordScreenService().getMarks();
			case TYPE_SEARCH:
				marks = ServiceManager.getSearchScreenService().getMarks();
				break;
			}
			if (playingMarks.containsKey(ScreenAudio.class.getCanonicalName())) {
				int topScreenId = playingMarks.get(ScreenAudio.class.getCanonicalName());
				playingMarks.clear();
				playingMarks.put(ScreenAudio.class.getCanonicalName(), topScreenId);
			} else {
				playingMarks.clear();
			}
			Mark mark = null;
			Iterator<Mark> iterator = marks.iterator();
			MediaApplication.logD(MediaPlayerService.class, "###################################");
			while (iterator.hasNext()) {
				mark = iterator.next();
				playingMarks.put(mark.getScreenId(), mark.getId());
				MediaApplication.logD(MediaPlayerService.class, mark.getScreenId() + "----" + mark.getId());
			}
/*			mark = ServiceManager.getAmtScreenService().getMarks().peek();
			playingMarks.put(mark.getScreenId(), mark.getId());*/
			break;
		case EQULIZER_LEVEL:
			if(Integer.parseInt(Build.VERSION.SDK)>=9){
				setEqualizer();
			}
			break;
	   case REVERB_MODE:
		   if(Integer.parseInt(Build.VERSION.SDK)>=9){
				setReverb();
			}
		}
		return true;
	}

	private void reloadAudioInfo() {
		synchronized (cLock) {
			if(mCursor!=null &&mCursor.getCount()!=0){
			String audioName = mCursor.getString(mCursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_DISPALYNAME));
			String audioArtist = mCursor.getString(mCursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ARTISTNAME));
			args.putExtra("audioName", audioName);
			args.putExtra("audioArtist", audioArtist);
			args.putExtra("durationTime", mediaPlayer.getDuration());
			mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.MUSIC_RELOAD));
			}
		}
	}
    
	private void changePosition() {
		synchronized (cLock) {
			if(mCursor!=null &&mCursor.getCount()!=0){
			String audioName = mCursor.getString(mCursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_DISPALYNAME));
			String audioArtist = mCursor.getString(mCursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ARTISTNAME));
			String fileName = mCursor.getString(mCursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_FILEPATH));
			args.putExtra("audioName", audioName);
			args.putExtra("audioArtist", audioArtist);
			args.putExtra("filepath",fileName);
			setArgs(audioName, audioArtist);
			mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.MUSIC_UPDATE));
			
			if(mediaModel == MEDIA_MODEL_KMEDIA){
				if(!audioName.contains("-") && audioName.contains(".mp3") && !audioArtist.equalsIgnoreCase("<unknown>")){
					audioName = audioArtist + "-"+ audioName;
				}
				makeNewRecordFileName(audioArtist,audioName);
			 }
			}
			
		}
	}

	private void makeNewRecordFileName(String audioArtist_para,String audioName_para){
		long i =1;
		for(;;){
			boolean continue_to_make = false;
			String strPathWav = null;
			String strPath = null;
			DecimalFormat df = new DecimalFormat("000");

			if(audioArtist_para.equalsIgnoreCase("<unknown>")){
				strPath = "未知歌手-"+audioName_para.substring(0, audioName_para.lastIndexOf(".mp3"))+"_录音"+df.format(i)+"_mp3";
			}
			else{
				strPath = audioName_para.substring(0, audioName_para.lastIndexOf(".mp3"))+"_录音"+df.format(i)+"_mp3";
			}
				
			strPath = MediaPlayerService.directoryRecord+strPath;
			if(strPath.contains("_mp3")){
				strPathWav = strPath.replace("_mp3", "_wav");
			}
			i++;
			String recordPath = MediaPlayerService.directoryRecord;
			File dir = new File(recordPath);
			if(!dir.exists()){
				dir.mkdir();
				mediaPlayer.setRecordFilePath(strPath);
			}else{
				File[] files = dir.listFiles();
				if(files==null){
					mediaPlayer.setRecordFilePath(strPath);
				}else{
					for(int j=0;j<files.length;j++){
						if(files[j].getAbsolutePath().contains(strPath)|| files[j].getAbsolutePath().contains(strPathWav)){
							continue_to_make = true;
							break;
						}
					}
					if(continue_to_make==true){
						continue;
					}else{
						mediaPlayer.setRecordFilePath(strPath);
						break;
					}
				}
			}
		}
	
	}
	//在这个方法里，会计算下一首的歌曲名；所以分为4个模式，对应我们的四个模式；
	private void sendAudioDuration() {
		synchronized (cLock) {
			if ((mediaModel == MEDIA_MODEL_LOCAL || typefinal == ScreenType.TYPE_RECORD)&&(mCursor.getCount() != 0)){
			if (flagAudioPlayMode == MediaEventTypes.MEDIA_PLAY_MODE_SINGLE_REPEAT) {
				mCursor.moveToPosition(cursorPosition);
				String audioName = mCursor.getString(mCursor
						.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_DISPALYNAME));
				args.putExtra("audioName", audioName);
				// String audioName =
			} else if (flagAudioPlayMode == MediaEventTypes.MEDIA_PLAY_MODE_ORDER) {
				if (mCursor.moveToNext()) {
					String audioName = mCursor
							.getString(mCursor
									.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_DISPALYNAME));
					args.putExtra("audioName", audioName);
					//mCursor.moveToPrevious();
				} else {
					args.putExtra(
							"audioName",
							MediaApplication.getContext().getString(
									R.string.screen_audio_player_is_last_one));
				}				
				mCursor.moveToPosition(cursorPosition);
			}else if(flagAudioPlayMode == MediaEventTypes.MEDIA_PLAY_MODE_LIST_CYCLE){
				if (mCursor.moveToNext()) {
					String audioName = mCursor
							.getString(mCursor
									.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_DISPALYNAME));
					args.putExtra("audioName", audioName);
					//mCursor.moveToPrevious();
					mCursor.moveToPosition(cursorPosition);
				} else {
					if(mCursor.moveToFirst()) {
						String audioName = mCursor
								.getString(mCursor
										.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_DISPALYNAME));
						args.putExtra("audioName", audioName);
					}
					mCursor.moveToLast();
				}
			}
			else if (flagAudioPlayMode == MediaEventTypes.MEDIA_PLAY_MODE_SHUFFLE) {
				if (mCursor.moveToPosition(position_next)) {
					String audioName = mCursor
							.getString(mCursor
									.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_DISPALYNAME));
					args.putExtra("audioName", audioName);
					mCursor.moveToPosition(cursorPosition);
				}

			}
			}
			int duration = (int) (mediaPlayer.getDuration()* mRatio);
			args.putExtra("durationTime", duration);
			mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.MUSIC_DURATION));
		}
	}

	public LyricPlayer getLyricplayer() {
		return lyricplayer;
	}

	public String getLyricPath() {
		return lyricPath;
	}
    //计算随机数，随机模式下会调用
	private void shuffleNum() {
		synchronized (cLock) {
			//System.out.println("mCursor count is " + mCursor.getPosition());
			Random rand = new Random(System.currentTimeMillis());
			if ((mCursor.getCount() == 0) || (random.size() !=0 && random.indexOf(mCursor.getPosition()) != (random.size()-1))){
				return;
			}else if(mCursor.getCount() == 1){
				position_next = 0;
				args.putExtra("screenType", typefinal);
				args.putExtra("screenId", screenIdfianl);
				return;
			}
			int size = mCursor.getCount();
			if(random.size() == 0){
				//System.out.println("random size 0 , position : " + mCursor.getPosition());
				for(int i=0; i< size; i++){
					if(mCursor.getPosition() != i){
						random.add(i);
					}
				}
				random.add(mCursor.getPosition());
				SHUFFLE_POSITION = mCursor.getPosition();
			}
			
			int next = rand.nextInt(size/2);
			position_next = random.get(next);
			args.putExtra("screenType", typefinal);
			args.putExtra("screenId", screenIdfianl);
			//System.out.println("position_next : " + position_next);
			random.remove(next);
			random.add(position_next);
			/*int position = 0;
			//System.out.println("random size " +  random.size());
			if(random.size() == 0){
				while(random.size()< mCursor.getCount()){
					position = rand.nextInt(mCursor.getCount());
					if(!random.contains(position)){
						random.add(position);
						System.out.println("~~~~~p~~~~~ " + position);
					}
				}
				SHUFFLE_POSITION = 0;	
			}
				position_next = random.get(SHUFFLE_POSITION);
				if ((SHUFFLE_POSITION+1) == mCursor.getCount()) {
					random.clear();
					SHUFFLE_POSITION = 0;
				}else{
					SHUFFLE_POSITION = SHUFFLE_POSITION + 1;
				}

//				args.putExtra("id", position);
//				args.putExtra("position", position);
				args.putExtra("screenType", typefinal);
				args.putExtra("screenId", screenIdfianl);
//				mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_START));

		//	}
*/
		}
	}

	public static MediaEventArgs getArgs() {
		return mediaEventArgs;
	}

	public void setArgs(String audioName, String audioArtist) {
		mediaEventArgs.putExtra("audioName", audioName);
		mediaEventArgs.putExtra("audioArtist", audioArtist);
	}
    
	public void changeAccompany(IMediaEventArgs args) {		
		MediaPlayerErrorFlag=0; 
		String strSongName = null;
		String strArtistName = null;
		boolean isPrepared = false;
		//判断如果是原唱K歌，而且本地原唱存在的情况下，则会通过数据库查询获取伴唱名；如果不是，则通过点击列表获取伴奏名；
		if(mCursor != null&&mCursor.getCount()!=0 && ScreenKMediaPlayer.isOriginal==true )
		{
			mCursor.moveToFirst();
			String name = mCursor.getString(mCursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_DISPALYNAME));
			name = name.substring(0, name.lastIndexOf("."));
			accompanyPath = MediaApplication.accompanyPath + name + ".bz";
		}
		//增加了直接伴奏过来的写录音文件的处理；
		else{
			accompanyPath= MediaApplication.accompanyPath + ScreenKMedia.bzplayername+ScreenKMedia.bzsongname+".bz";
			if(ScreenKMedia.bzplayername==""|| ScreenKMedia.bzplayername==null){
				strSongName = "未知歌手-"+ScreenKMedia.bzsongname+".mp3";
				strArtistName = "未知歌手";
			}else{
				strSongName = ScreenKMedia.bzplayername+ScreenKMedia.bzsongname+".mp3";
				strArtistName = ScreenKMedia.bzplayername.replace("-", "");
			}
			try{
				makeNewRecordFileName(strArtistName,strSongName);
			}catch(Exception ext){
				ext.printStackTrace();
			}		
		}
		//进入之后，把K歌模式复原成默认的原唱K歌；
		ScreenKMediaPlayer.isOriginal=true;
		if (findFile(accompanyPath)) {			
			flagAccompany = 0;
			int accompany_seek = (Integer) args.getExtra("accompany_seek");
			int accompany_duration = (Integer) args.getExtra("accompany_duration");
			try {
				if (lyricplayer != null && defaultAccompany!=0) {
					lyricplayer.pauseLyricPlayer();
				}
				mediaPlayer.reset();
//				mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.LYRIC_PLAYER_STOP));
				mediaPlayer.setDataSource(accompanyPath);
				int curDuration = accompany_duration;
				try{
				    mediaPlayer.prepare();
				    
				    //如果是第一次进来，则需要启动歌词部分及图片部分；
				    if(defaultAccompany==0){
				    	try{
					    	args.putExtra("audioName", strSongName);
							args.putExtra("audioArtist", strArtistName);
							args.putExtra("filepath",accompanyPath);
							setArgs(strSongName, strArtistName);
							mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.MUSIC_UPDATE));
						    mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.LYRIC_PLAYER_START));
						    
						    if (mCursor!=null &&mCursor.getCount()!=0) {
						    	accompany_duration = (int) mCursor
										.getLong(mCursor
												.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_DURATION));
							} else {
								accompany_duration = mediaPlayer.getDuration();
							}
				    	} catch(Exception e) {
				    		accompany_duration = mediaPlayer.getDuration();
				    		e.printStackTrace();
				    	}
					    defaultAccompany++;
					}
//					else if (lyricplayer != null) {
//				    	lyricplayer.resumeLyricPlayer();
//					}
				    
				    isPrepared = true;			    
				    
				    curDuration = mediaPlayer.getDuration();
				}catch (Exception e){	
					//如果失败；则切回原唱；		
					if(defaultAccompany==0){
						Toast.makeText(MediaApplication.getContext(), MediaApplication.getContext().getString(R.string.screen_audio_player_no_accompany),
								Toast.LENGTH_SHORT).show();
					}else{
//						if (ScreenKMediaPlayer.Kmediacontext != null) {
//							if (mOnScreenHint != null) {
//								mOnScreenHint.cancel();
//							}
//							mOnScreenHint = OnScreenHint
//									.makeText(
//											ScreenKMediaPlayer.Kmediacontext,
//											ScreenKMediaPlayer.Kmediacontext
//													.getString(R.string.screen_audio_player_no_accompany));
//							mOnScreenHint.show();
							ServiceManager.getAmtMediaHandler().post(new Runnable() {
								@Override
								public void run() {
									if(mOnScreenHint!=null){
									    mOnScreenHint.cancel();
									}
									mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_audio_player_no_accompany));
									mOnScreenHint.show();
								}
							});
//						}
					}
					mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.ACCOMPANY_ERROR));
				} 
				if(accompany_duration<=1000){
				    accompany_duration=mediaPlayer.getDuration();
				}
				
				if (curDuration <= 1000) {
					curDuration = accompany_duration;
				}
				
				mRatio = (float)accompany_duration / (float)curDuration;
//				accompany_seek = mediaPlayer.getDuration() - (accompany_duration - accompany_seek);
//				accompany_seek = (int) (accompany_seek * ((float)curDuration / (float)accompany_duration));
				if (accompany_seek > curDuration) {
					accompany_seek = curDuration - (accompany_duration - accompany_seek);
				}
				//增加了对时间的异常处理；
				if(accompany_seek <=0){
					accompany_seek =0;
				}else if(accompany_duration<=0){
					accompany_seek=mediaPlayer.getCurrentPosition();
				}	
				args.putExtra("seek", accompany_seek);
				mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_MOVE_PROGRESS));
				if (lyricplayer != null) {
//			    	lyricplayer.setRatio(mRatio);
				    if (isPrepared) {
				    	lyricplayer.resumeLyricPlayer();
				    }
				}
				//mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_BOTTOM_CONTROL_PLAY_UI));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			if(defaultAccompany==0){
				Toast.makeText(MediaApplication.getContext(), MediaApplication.getContext().getString(R.string.screen_audio_player_no_accompany),
						Toast.LENGTH_SHORT).show();
			}else{
//				if (ScreenKMediaPlayer.Kmediacontext != null) {
//					if (mOnScreenHint != null) {
//						mOnScreenHint.cancel();
//					}
//					mOnScreenHint = OnScreenHint
//							.makeText(
//									ScreenKMediaPlayer.Kmediacontext,
//									ScreenKMediaPlayer.Kmediacontext
//											.getString(R.string.screen_audio_player_no_accompany));
//					mOnScreenHint.show();
					ServiceManager.getAmtMediaHandler().post(new Runnable() {
						@Override
						public void run() {
							if(mOnScreenHint!=null){
							    mOnScreenHint.cancel();
							}
							mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_audio_player_no_accompany));
							mOnScreenHint.show();
						}
					});
//				}
			}
			mediaEventService.onMediaUpdateEvent(args
					.setMediaUpdateEventTypes(MediaEventTypes.ACCOMPANY_ERROR));
			flagHaveAccompany = false;
			// mediaPlayer.setAudioMode(0);
			flagSilencer = 0;
		}

	}

	public void changeOriginal(IMediaEventArgs args) {
		MediaPlayerErrorFlag=0;
		boolean isPrepared = false;
		//如果原唱不存在；则直接返回；
		if (mCursor == null||mCursor.getCount()==0){
			if(defaultAccompany==0){
				Toast.makeText(MediaApplication.getContext(), MediaApplication.getContext().getString(R.string.screen_audio_player_no_original),
						Toast.LENGTH_SHORT).show();
			}else{
//				if (ScreenKMediaPlayer.Kmediacontext != null) {
//					if (mOnScreenHint != null) {
//						mOnScreenHint.cancel();
//					}
//					mOnScreenHint = OnScreenHint
//							.makeText(
//									ScreenKMediaPlayer.Kmediacontext,
//									ScreenKMediaPlayer.Kmediacontext
//											.getString(R.string.screen_audio_player_no_original));
//					mOnScreenHint.show();
//				}
				ServiceManager.getAmtMediaHandler().post(new Runnable() {
					@Override
					public void run() {
						if(mOnScreenHint!=null){
						    mOnScreenHint.cancel();
						}
						mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_audio_player_no_original));
						mOnScreenHint.show();
					}
				});
			}
			mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.ORIGINAL_ERROR));
			return;
		}
		mCursor.moveToFirst();
		if (flagSilencer == 0) {
			// mediaPlayer.setAudioMode(1);
			flagSilencer = 1;
		} else {
			flagAccompany = 0;
			String path = mCursor.getString(mCursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_FILEPATH));
			int original_seek = (Integer) args.getExtra("original_seek");
			int original_duration = (Integer) args.getExtra("original_duration");
			int curDuration = original_duration; 
			try {
				if (lyricplayer != null && defaultAccompany!=0 ) {
					lyricplayer.pauseLyricPlayer();
				}
				mediaPlayer.reset();
//				mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.LYRIC_PLAYER_STOP));
				mediaPlayer.setDataSource(path);
				mediaPlayer.prepare();
				//如果从伴奏K歌部分而且失败，第一次跳进来，则需要启动歌词相关处理；
				if(defaultAccompany==0){
					try{
						changePosition();
						mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.LYRIC_PLAYER_START));
					}catch(Exception e){
						e.printStackTrace();
					}
					defaultAccompany++;
				}
//				else if (lyricplayer != null ) {
//					lyricplayer.resumeLyricPlayer();
//				}
				
				curDuration = mediaPlayer.getDuration();
				isPrepared = true;
					//mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_BOTTOM_CONTROL_PLAY_UI));
			} catch (Exception e) {
				if(defaultAccompany==0){
					Toast.makeText(MediaApplication.getContext(), MediaApplication.getContext().getString(R.string.screen_audio_player_no_original),
							Toast.LENGTH_SHORT).show();
				}else{
//					if (ScreenKMediaPlayer.Kmediacontext != null) {
//						if (mOnScreenHint != null) {
//							mOnScreenHint.cancel();
//						}
//						mOnScreenHint = OnScreenHint
//								.makeText(
//										ScreenKMediaPlayer.Kmediacontext,
//										ScreenKMediaPlayer.Kmediacontext
//												.getString(R.string.screen_audio_player_no_original));
//						mOnScreenHint.show();
//					}
					ServiceManager.getAmtMediaHandler().post(new Runnable() {
						@Override
						public void run() {
							if(mOnScreenHint!=null){
							    mOnScreenHint.cancel();
							}
							mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_audio_player_no_original));
							mOnScreenHint.show();
						}
					});
				}
				mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.ORIGINAL_ERROR));
			//	mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_NEXT));
			//	e.printStackTrace();
			} finally {
				//original_seek = mediaPlayer.getDuration() - (original_duration - original_seek);
//				original_seek = (int) (original_seek * ((float)curDuration / (float)original_duration));
				mRatio = 1;
				if (original_seek > curDuration) {
					original_seek = curDuration - (original_duration - original_seek);
				}
				if(original_seek <=0){
					original_seek =0;
				}else if(original_duration<=0){
					original_seek =mediaPlayer.getCurrentPosition();
				}
				args.putExtra("seek", original_seek);
				mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_MOVE_PROGRESS));
				if (lyricplayer != null) {
			    	lyricplayer.setRatio(mRatio);
			    	if (isPrepared) {
			    		lyricplayer.resumeLyricPlayer();;
			    	}
				}
			}
		}
	}

	public static boolean findFile(String path) {
		File file = new File(path);
		if (file.exists()) {
			return true;
		}
		return false;
	}


	public byte[] getCLock() {
		return cLock;
	}

	public Map<String, Integer> getPlayingMarks() {
		return playingMarks;
	}
	
	public void setEqualizer(){		
		SharedPreferences sharedata = ServiceManager.getAmtMedia().getSharedPreferences("equalizersettings", Context.MODE_WORLD_WRITEABLE);			
		mode=sharedata.getInt("flagEqualizerMode", 0);    
		if(mode==10){
			 level[0]=sharedata.getInt("flagEqualizerlevel0", 0);
			 level[1]=sharedata.getInt("flagEqualizerlevel1", 0);
			 level[2]=sharedata.getInt("flagEqualizerlevel2", 0);
			 level[3]=sharedata.getInt("flagEqualizerlevel3", 0);
			 level[4]=sharedata.getInt("flagEqualizerlevel4", 0); 			 
		}else{
			for(int i=0;i<level.length;i++){
				level[i]=defaultlevel[mode][i]	;										
			}
		}
		  mEqualizer.setBandLevel((short)0, (short)level[0]);
		  mEqualizer.setBandLevel((short)1, (short)level[1]);
		  mEqualizer.setBandLevel((short)2, (short)level[2]);
		  mEqualizer.setBandLevel((short)3, (short)level[3]);
		  mEqualizer.setBandLevel((short)4, (short)level[4]);
   }

	public void setReverb(){
		SharedPreferences sharedata = ServiceManager.getAmtMedia().getSharedPreferences("equalizersettings", Context.MODE_WORLD_WRITEABLE);			
		int reverbmode=sharedata.getInt("flagReverbMode", 0); 
		mPresetReverb.setPreset((short)reverbmode);		
	}
}
