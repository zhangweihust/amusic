package com.amusic.media.services.impl;

import java.util.ArrayList;
import java.util.Map;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RemoteViews;

import com.amusic.media.AmtMedia;
import com.amusic.media.MediaApplication;
import com.amusic.media.R;
import com.amusic.media.adapter.MenuContentAdapter;
import com.amusic.media.dialog.DialogEditLyricRadio;
import com.amusic.media.dialog.DialogStopConvertConfirm;
import com.amusic.media.dialog.LyricModify;
import com.amusic.media.event.IMediaEventArgs;
import com.amusic.media.event.impl.MediaEventArgs;
import com.amusic.media.event.impl.MediaEventTypes;
import com.amusic.media.listener.ShakeListener;
import com.amusic.media.model.MenuItem;
import com.amusic.media.provider.MediaCategoryDatabaseHelper;
import com.amusic.media.provider.MediaDictionaryDatabaseHelper;
import com.amusic.media.provider.MediaScanner;
import com.amusic.media.screens.impl.ScreenAudioAlbumSongs;
import com.amusic.media.screens.impl.ScreenAudioFavorites;
import com.amusic.media.screens.impl.ScreenAudioPlaylistSongs;
import com.amusic.media.screens.impl.ScreenAudioRecentlySongs;
import com.amusic.media.screens.impl.ScreenAudioRoot;
import com.amusic.media.screens.impl.ScreenAudioSingerSongs;
import com.amusic.media.screens.impl.ScreenAudioSongs;
import com.amusic.media.screens.impl.ScreenKMediaPlayer;
import com.amusic.media.screens.impl.ScreenKMediaRoot;
import com.amusic.media.screens.impl.ScreenRecordPlayer;
import com.amusic.media.screens.impl.ScreenRecordRoot;
import com.amusic.media.screens.impl.ScreenSearchRoot;
import com.amusic.media.services.IAmtScreenService;
import com.amusic.media.services.IAudioScreenService;
import com.amusic.media.services.IDesktopLyricService;
import com.amusic.media.services.IExceptionService;
import com.amusic.media.services.IKMediaScreenService;
import com.amusic.media.services.IMediaEventService;
import com.amusic.media.services.IMediaPlayerService;
import com.amusic.media.services.IMediaService;
import com.amusic.media.services.INetworkService;
import com.amusic.media.services.INotificatioService;
import com.amusic.media.services.IRecordScreenService;
import com.amusic.media.services.ISearchScreenService;
import com.amusic.media.services.IUserInfoService;
import com.amusic.media.utils.BitmapCache;
import com.amusic.media.utils.Constant;
import com.amusic.media.utils.MediaDatabaseUtil;

public class ServiceManager extends Service {
	private static boolean firstTime;

	public static boolean isFirstTime() {
		return firstTime;
	}

	static {
		System.loadLibrary("am_lame_jni");
		System.loadLibrary("ktvtoolbox");
	}
	private static boolean started;
	private static AmtMedia amtMedia;
	private static ScreenAudioRoot audioRoot;
	private static ScreenKMediaRoot kMediaRoot;
	private static ScreenRecordRoot recordRoot;
	private static ScreenSearchRoot searchRoot;
	private static ScreenKMediaPlayer kmediaPlayer;
	private static ScreenRecordPlayer screenRecordPlayer;
	public  static String listId;
	public  static String lastListId;
	public  static String methodName;
	public  static String lastMethodName;
	public  static int id;
	public  static int position;
	public  static int flagAudioPlayMode;
	public  static boolean isPlayed = false;
	private static final IAmtScreenService amtScreenService = new AmtScreenService();
	private static final IAudioScreenService audioScreenService = new AudioScreenService();
	private static final IKMediaScreenService kMediaScreenService = new KMediaScreenService();
	private static final IRecordScreenService recordScreenService = new RecordScreenService();
	private static final ISearchScreenService searchScreenService = new SearchScreenService();
	private static final IMediaService mediaService = new MediaService();
	private static final IMediaPlayerService mediaPlayerService = new MediaPlayerService();
	private static final IMediaEventService mediaEventService = new MediaEventService();
	private static final INotificatioService notificatioService = new NotificationService();
	private static final INetworkService networkService = new NetworkService();
	private static final IExceptionService exceptionService = new ExceptionService();
	private static final IUserInfoService countService = new UserInfoService();
	private static final IDesktopLyricService desktopLyricService = DesktopLyricService.getInstance();
	private static MediaScanner mediaScanner;
	private static final String dictionary_base_fileName = "amt_media_";
	private static final int dictionary_part_count = 2;
	private static final String category_base_fileName = "amt_media_category_";
	private static final int category_part_count = 1;
	private static MenuContentAdapter[] menuContentAdapters = new MenuContentAdapter[3];
	public static String DATABASE_PATH;
	private static ShakeListener mShaker;
	private static Notification notification;
	public static boolean isStarted() {
		return started;
	}

	
	@Override
	public void onCreate() {
		super.onCreate();
		int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk >= 6 ){		
			notification = new Notification(
					R.drawable.amtplayer_notification, null, 0);
			Intent intent = new Intent();
			ComponentName componentName = new ComponentName(MediaApplication.getInstance(), AmtMedia.class);
			intent.setComponent(componentName);
			intent.setAction(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			PendingIntent pendingIntent = PendingIntent.getActivity(MediaApplication.getInstance(), 0, intent, 0);
			notification.contentView = new RemoteViews(MediaApplication.getInstance().getPackageName(),R.layout.screen_notification);
			notification.contentIntent = pendingIntent;
			notification.flags = Notification.FLAG_ONGOING_EVENT; // 放在正在进行里面
			startForeground(NotificationService.notificationId, notification);
		} else 
			this.setForeground(true);
	}
	
	




	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}


	public static boolean start() {
		if (ServiceManager.started) {
			return true;
		}
		DATABASE_PATH = String.format("data/data/%s/databases", MediaApplication.getContext().getPackageName());
		boolean dictionaryNotExists = MediaDatabaseUtil.copyDatabase(dictionary_base_fileName, dictionary_part_count, DATABASE_PATH, MediaDictionaryDatabaseHelper.NAME);
		boolean categoryNotExists = MediaDatabaseUtil.copyDatabase(category_base_fileName, category_part_count, DATABASE_PATH, MediaCategoryDatabaseHelper.NAME);
		firstTime = dictionaryNotExists && categoryNotExists;
		Context context = MediaApplication.getContext();
		context.startService(new Intent(context, ServiceManager.class));
		mediaScanner = new MediaScanner(context);
		makeAdapters();
		boolean success = true;
		success &= exceptionService.start();
		success &= mediaEventService.start();
		success &= amtScreenService.start();
		success &= audioScreenService.start();
		success &= kMediaScreenService.start();
		success &= recordScreenService.start();
		success &= searchScreenService.start();
		success &= mediaService.start();
		success &= mediaPlayerService.start();
		success &= notificatioService.start();
		success &= networkService.start();
		success &= countService.start();
		success &= desktopLyricService.start();
		if (success) {
			started = true;
		}
		if(Constant.IS_LASHING_CONTROLL){
			registerListener();
		}
		return success;
	}

	public static void registerListener(){
		mShaker = new ShakeListener(amtMedia);  
        mShaker.setOnShakeListener(new ShakeListener.OnShakeListener() {  
            public void onShake() {
            	IMediaEventArgs args = new MediaEventArgs();
    			args.putExtra("id", -1);
    			mediaEventService
    					.onMediaUpdateEvent(args
    							.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_NEXT));
            }

			@Override
			public void onNextShake() {
            	IMediaEventArgs nextArgs = new MediaEventArgs();
    			nextArgs.putExtra("id", -1);
    			mediaEventService
				.onMediaUpdateEvent(nextArgs
						.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_NEXT));
			} 

			@Override
			public void onPreShake() {
				IMediaEventArgs preArgs = new MediaEventArgs();
    			preArgs.putExtra("id", -1);
    			mediaEventService
				.onMediaUpdateEvent(preArgs
						.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_PREVIOUS));
			}
        });  
        
	}
	
	public static void unregisterListener(){
		mShaker.pause();
	}
	
	public static boolean stop() {
		if (!started) {
			return true;
		}
		boolean success = true;
		success &= countService.stop();
		success &= mediaEventService.stop();
		success &= mediaPlayerService.stop();
		success &= mediaService.stop();
		success &= amtScreenService.stop();
		success &= audioScreenService.stop();
		success &= kMediaScreenService.stop();
		success &= recordScreenService.stop();
		success &= searchScreenService.stop();
		success &= notificatioService.stop();
		success &= networkService.stop();
		success &= exceptionService.stop();
		success &= desktopLyricService.stop();
		if (success) {
			started = false;
		}
		if(Constant.IS_LASHING_CONTROLL){
			unregisterListener();
		}
		BitmapCache.getInstance().clearCache();
		Context context = MediaApplication.getContext();
		context.stopService(new Intent(context, ServiceManager.class));
		return success;
	}
	

	public static void makeAdapters() {
		ArrayList<MenuItem> userfull = new ArrayList<MenuItem>();
		userfull.add(new MenuItem(R.drawable.menu_item_scanner, amtMedia.getResources()
				.getString(R.string.screen_home_menu_scanner)));
		userfull.add(new MenuItem(R.drawable.menu_item_skin, amtMedia.getResources()
				.getString(R.string.screen_home_menu_skin)));
		userfull.add(new MenuItem(R.drawable.menu_item_exit, amtMedia.getResources()
				.getString(R.string.screen_home_menu_exit)));
		menuContentAdapters[Constant.MenuConstant.userfull] = new MenuContentAdapter(amtMedia, userfull);

		ArrayList<MenuItem> tools = new ArrayList<MenuItem>();
		tools.add(new MenuItem(R.drawable.menu_item_timing, amtMedia.getResources()
				.getString(R.string.screen_home_menu_timing)));
		tools.add(new MenuItem(R.drawable.menu_item_mode, amtMedia.getResources()
				.getString(R.string.screen_home_menu_mode)));
		tools.add(new MenuItem(R.drawable.menu_item_download, amtMedia.getResources()
				.getString(R.string.screen_home_menu_download_manager)));
		if(Integer.parseInt(Build.VERSION.SDK)>=9 &&  MediaPlayerService.eqInitState == true){
			tools.add(new MenuItem(R.drawable.menu_item_equalizer, amtMedia.getResources()
					.getString(R.string.screen_home_menu_equalizer_settings)));
		}		
		tools.add(new MenuItem(R.drawable.menu_item_settings, amtMedia.getResources()
				.getString(R.string.screen_home_menu_system_settings)));
		menuContentAdapters[Constant.MenuConstant.tools] = new MenuContentAdapter(amtMedia, tools);

		ArrayList<MenuItem> help = new ArrayList<MenuItem>();
		help.add(new MenuItem(R.drawable.menu_item_version, amtMedia.getResources()
				.getString(R.string.screen_home_menu_version)));
		help.add(new MenuItem(R.drawable.menu_item_suggestion, amtMedia.getResources()
				.getString(R.string.screen_home_menu_suggestion)));
		help.add(new MenuItem(R.drawable.menu_item_function, amtMedia.getResources()
				.getString(R.string.screen_home_menu_function)));
		menuContentAdapters[Constant.MenuConstant.help] = new MenuContentAdapter(amtMedia, help);	
	}
	
	public static MenuContentAdapter[] getMenuContentAdapters(){
		return menuContentAdapters;
	}
	
	public static AmtMedia getAmtMedia() {
		return amtMedia;
	}
	
	public static ScreenKMediaPlayer getkmediaPlayer() {
		return kmediaPlayer;
	}
	
	public static ScreenRecordPlayer getRecordPlayer() {
		return screenRecordPlayer;
	}

	public static void setAmtMedia(AmtMedia amtMedia) {
		ServiceManager.amtMedia = amtMedia;
	}


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public static IAmtScreenService getAmtScreenService() {
		return amtScreenService;
	}

	public static IMediaService getMediaService() {
		return mediaService;
	}

	public static MediaScanner getMediaScanner() {
		return mediaScanner;
	}

	public static void exit() {
		saveState();
		stop();
		amtMedia.finish();
		System.exit(0);
	}

	public static IMediaPlayerService getMediaplayerService() {
		return mediaPlayerService;
	}

	public static IMediaEventService getMediaEventService() {
		return mediaEventService;
	}
	
	public static INotificatioService getNotificatioservice() {
		return notificatioService;
	}

	public static Handler getAmtMediaHandler() {
		return amtMedia.getHandler();
	}

	public static INetworkService getNetworkService() {
		return networkService;
	}

	public static ScreenAudioRoot getAudioRoot() {
		return audioRoot;
	}

	public static void setAudioRoot(ScreenAudioRoot audioRoot) {
		ServiceManager.audioRoot = audioRoot;
	}

	public static ScreenKMediaRoot getKMediaRoot() {
		return kMediaRoot;
	}

	public static void setKMediaRoot(ScreenKMediaRoot kMediaRoot) {
		ServiceManager.kMediaRoot = kMediaRoot;
	}
	
	public static void setKmediaPlayer(ScreenKMediaPlayer kMediaPlayer) {
		ServiceManager.kmediaPlayer = kMediaPlayer;
	}
	
	public static void setRecordPlayer(ScreenRecordPlayer screenRecordPlayer) {
		ServiceManager.screenRecordPlayer = screenRecordPlayer;
	}

	public static IAudioScreenService getAudioScreenService() {
		return audioScreenService;
	}

	public static IKMediaScreenService getKMediaScreenService() {
		return kMediaScreenService;
	}

	public static void setRecordRoot(ScreenRecordRoot screenRecordRoot) {
		recordRoot = screenRecordRoot;
	}

	public static void setSearchRoot(ScreenSearchRoot screenSearchRoot) {
		searchRoot = screenSearchRoot;
	}

	public static ScreenRecordRoot getRecordRoot() {
		return recordRoot;
	}

	public static ScreenSearchRoot getSearchRoot() {
		return searchRoot;
	}

	public static IRecordScreenService getRecordScreenService() {
		return recordScreenService;
	}

	public static ISearchScreenService getSearchScreenService() {
		return searchScreenService;
	}

	public static IUserInfoService getCountService() {
		return countService;
	}
	

	public static IDesktopLyricService getDesktopLyricService() {
		return desktopLyricService;
	}
	
	public static void saveState() {
		Editor sharedata = amtMedia.getSharedPreferences("lastsong", 0).edit();
		sharedata.putInt("flagAudioPlayMode",flagAudioPlayMode);
		if (!isPlayed) {
			return;
		}
		  
		if (methodName != null) {
			sharedata.putString("listId",listId);
			Map<String, Integer> playingMarks = ServiceManager.getMediaplayerService().getPlayingMarks();
			if (playingMarks.containsKey(ScreenAudioSongs.class.getCanonicalName())) {
				if (playingMarks.get(ScreenAudioSongs.class.getCanonicalName()) != null) {
                     id = playingMarks.get(ScreenAudioSongs.class.getCanonicalName());
				}
			} else if (playingMarks.containsKey(ScreenAudioSingerSongs.class.getCanonicalName())) {
				if (playingMarks.get(ScreenAudioSingerSongs.class.getCanonicalName()) != null) {
					//Log.d("=QQQ=","id = " + playingMarks.get(ScreenAudioAlbumSongs.class.getCanonicalName()));
					id = playingMarks.get(ScreenAudioSingerSongs.class.getCanonicalName());
				}
			} else if (playingMarks.containsKey(ScreenAudioAlbumSongs.class.getCanonicalName())) {
				if (playingMarks.get(ScreenAudioAlbumSongs.class.getCanonicalName()) != null) {
					//Log.d("=QQQ=","id = " + playingMarks.get(ScreenAudioAlbumSongs.class.getCanonicalName()));
					id = playingMarks.get(ScreenAudioAlbumSongs.class.getCanonicalName());
				}
			} else if (playingMarks.containsKey(ScreenAudioPlaylistSongs.class.getCanonicalName())) {
				if (playingMarks.get(ScreenAudioPlaylistSongs.class.getCanonicalName()) != null) {
					id = playingMarks.get(ScreenAudioPlaylistSongs.class.getCanonicalName());
				}
			} else if (playingMarks.containsKey(ScreenAudioFavorites.class.getCanonicalName())) {
				if (playingMarks.get(ScreenAudioFavorites.class.getCanonicalName()) != null) {
					id = playingMarks.get(ScreenAudioFavorites.class.getCanonicalName());
				}
			} else if (playingMarks.containsKey(ScreenAudioRecentlySongs.class.getCanonicalName())) {
				if (playingMarks.get(ScreenAudioRecentlySongs.class.getCanonicalName()) != null) {
					id = playingMarks.get(ScreenAudioRecentlySongs.class.getCanonicalName());
				}
			} 
			sharedata.putInt("id",id);
			sharedata.putString("methodName", methodName);
		}
		
		sharedata.putInt("position",position);
		sharedata.commit();  
	}
	
	public static void finishScreenKMediaPlayer(){
		//System.out.println("***finishScreenKMediaPlayer***");
		Window window = amtMedia.getLocalActivityManager().destroyActivity(ScreenKMediaPlayer.class.getCanonicalName()+Constant.KMEDIA_COUNT, true);
		LinearLayout root = (LinearLayout) amtMedia.findViewById(R.id.amt_media_root_view);
		if(window != null){
			root.removeView(window.getDecorView());
		}
		DialogStopConvertConfirm dialog = getkmediaPlayer().getDialogStopConvertConfirm();
		if(dialog != null){
			dialog.dismiss();
		}
		DialogEditLyricRadio dialogEditLyricRadio = amtMedia.getLyricModifyDialog();
		if(dialogEditLyricRadio != null){
			LyricModify lyricModify = dialogEditLyricRadio.getLyricModify();
			if(lyricModify != null){
				lyricModify.dismiss();
			}
		}
		Constant.WHICH_PLAYER = 0;
		amtScreenService.goback();
	}
	
	public static void finishScreenRecordPlayer(){
//		System.out.println("***finishScreenRecordPlayer***");	
		Window window = amtMedia.getLocalActivityManager().destroyActivity(ScreenRecordPlayer.class.getCanonicalName()+Constant.RECORD_COUNT, true);
		LinearLayout root = (LinearLayout) amtMedia.findViewById(R.id.amt_media_root_view);
		if(window != null){
			root.removeView(window.getDecorView());
		}
		Constant.WHICH_PLAYER = 0;
		amtScreenService.goback();
	}
	
	public static void setCurSongPrompt(String curSongName)
	{
		if (notification != null)
		{
			notification.contentView.setTextViewText(R.id.screen_nitification_current_song, curSongName);
			((NotificationManager) amtMedia
			.getSystemService(Context.NOTIFICATION_SERVICE)).notify(NotificationService.notificationId, notification);
		}
	}
	
}
