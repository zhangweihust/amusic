package com.android.media.screens.impl;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.amusic.media.R;
import com.android.media.MediaApplication;
import com.android.media.event.IMediaEventArgs;
import com.android.media.event.IMediaEventHandler;
import com.android.media.event.impl.MediaEventArgs;
import com.android.media.event.impl.MediaEventTypes;
import com.android.media.model.ScreenArgs;
import com.android.media.screens.AudioScreen;
import com.android.media.services.IMediaEventService;
import com.android.media.services.impl.ServiceManager;
import com.android.media.view.CustomDialog;

public class ScreenAudio extends AudioScreen implements OnClickListener,
		IMediaEventHandler {
	private ImageButton playlistsPlaying;
	private ImageButton songsPlaying;
	private ImageButton singersPlaying;
	private ImageButton albumsPlaying;
	private ImageButton favoritesPlaying;
	private ImageButton recentlyPlaying;
	
	private static TextView songsName;
	private static TextView singersName;
	private static TextView albumsName;
	private static TextView playlistsName;
	private static TextView favoritesName;
	private static TextView recentlyName;

	private TextView songsCount;
	private TextView singersCount;
	private TextView albumsCount;
	private TextView playlistsCount;
	private TextView favoritesCount;
	private TextView recentlyCount;
	
	public static final int REFRESH_SONGS_COUNT = 1;
	public static final int REFRESH_PLAYLISTS_COUNT = 2;
	public static final int REFRESH_FAVOURITES_COUNT = 3;

	private static IMediaEventArgs screenAudioFreshArgs = new MediaEventArgs();
	private IMediaEventService mediaEventService;
	
	private boolean needscan = false;
	private Dialog dialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_audio);
		setScreenTitle(getString(R.string.screen_home_tab_audio));
		mediaEventService = ServiceManager.getMediaEventService();
		mediaEventService.addEventHandler(this);
		playlistsPlaying = (ImageButton) findViewById(R.id.screen_audio_playlists_playing);
		songsPlaying = (ImageButton) findViewById(R.id.screen_audio_songs_playing);
		singersPlaying = (ImageButton) findViewById(R.id.screen_audio_singers_playing);
		albumsPlaying = (ImageButton) findViewById(R.id.screen_audio_albums_playing);
		favoritesPlaying = (ImageButton) findViewById(R.id.screen_audio_favorites_playing);
		recentlyPlaying = (ImageButton) findViewById(R.id.screen_audio_recently_playing);
		playlistsPlaying.setOnClickListener(this);
		songsPlaying.setOnClickListener(this);
		singersPlaying.setOnClickListener(this);
		albumsPlaying.setOnClickListener(this);
		favoritesPlaying.setOnClickListener(this);
		recentlyPlaying.setOnClickListener(this);
		
		songsName=(TextView) findViewById(R.id.screen_audio_songs_name);
		singersName=(TextView) findViewById(R.id.screen_audio_singers_name);
		albumsName=(TextView) findViewById(R.id.screen_audio_albums_name);
		playlistsName=(TextView) findViewById(R.id.screen_audio_playlists_name);
		favoritesName=(TextView) findViewById(R.id.screen_audio_favorites_name);
		recentlyName=(TextView) findViewById(R.id.screen_audio_recently_name);

		songsCount = (TextView) findViewById(R.id.screen_audio_songs_count);
		singersCount = (TextView) findViewById(R.id.screen_audio_singers_count);
		albumsCount = (TextView) findViewById(R.id.screen_audio_albums_count);
		playlistsCount = (TextView) findViewById(R.id.screen_audio_playlists_count);
		favoritesCount = (TextView) findViewById(R.id.screen_audio_favorites_count);
		recentlyCount = (TextView) findViewById(R.id.screen_audio_recently_count);
		Cursor c=db.queryAudios();
		int count=c.getCount();
		c.close();
		if (count <= 0)
		{
			needscan = true;
		}
		songsCount.setText(count + getString(R.string.screen_audio_songs_num));
		c=db.querySingers();
		count=c.getCount();
		c.close();
		singersCount.setText(count+ getString(R.string.screen_audio_singers_num));
		c=db.queryAlbums();
		count=c.getCount();
		c.close();
		albumsCount.setText(count + getString(R.string.screen_audio_albums_num));
		c=db.queryPlaylists();
		count=c.getCount();
		c.close();
		playlistsCount.setText(count+ getString(R.string.screen_audio_playlists_num));
		c=db.queryFavoriteAudios();
		count=c.getCount();
		c.close();
		favoritesCount.setText(count + getString(R.string.screen_audio_favourites_num));
		c=db.queryRecentlyAudios();
		count=c.getCount();
		c.close();
		recentlyCount.setText(count + getString(R.string.screen_audio_recently_num));

		scantipifnosong();
		highlight();
	}

	private void highlight() {
		
			playlistsPlaying.setBackgroundResource(R.drawable.screen_audio_playlists_bg_select);
			albumsPlaying.setBackgroundResource(R.drawable.screen_audio_albums_bg_select);
			songsPlaying.setBackgroundResource(R.drawable.screen_audio_songs_bg_select);
			singersPlaying.setBackgroundResource(R.drawable.screen_audio_singers_bg_select);
			favoritesPlaying.setBackgroundResource(R.drawable.screen_audio_favorites_bg_select);
			recentlyPlaying.setBackgroundResource(R.drawable.screen_audio_recently_bg_select);
			
			songsName.setTextColor(MediaApplication.color_normal);
			singersName.setTextColor(MediaApplication.color_normal);
			albumsName.setTextColor(MediaApplication.color_normal);
			playlistsName.setTextColor(MediaApplication.color_normal);
			favoritesName.setTextColor(MediaApplication.color_normal);
			recentlyName.setTextColor(MediaApplication.color_normal);	
			
			songsCount.setTextColor(MediaApplication.color_normal);
			singersCount.setTextColor(MediaApplication.color_normal);
			albumsCount.setTextColor(MediaApplication.color_normal);
			playlistsCount.setTextColor(MediaApplication.color_normal);
			favoritesCount.setTextColor(MediaApplication.color_normal);
			recentlyCount.setTextColor(MediaApplication.color_normal);	
			Integer id = getHighlightId();	
	    if (id != null) {
			switch (id) {
			case SCREEN_AUDIO_PLAYLISTS:
				playlistsPlaying.setBackgroundResource(R.drawable.screen_audio_item_playlists_press_bg);
				playlistsName.setTextColor(MediaApplication.color_highlight);
				playlistsCount.setTextColor(MediaApplication.color_highlight);
				break;
			case SCREEN_AUDIO_ALBUMS:
				albumsPlaying.setBackgroundResource(R.drawable.screen_audio_item_albums_press_bg);
				albumsName.setTextColor(MediaApplication.color_highlight);
				albumsCount.setTextColor(MediaApplication.color_highlight);
				break;
			case SCREEN_AUDIO_FAVORITES:
				favoritesPlaying.setBackgroundResource(R.drawable.screen_audio_item_favorites_press_bg);
				favoritesName.setTextColor(MediaApplication.color_highlight);
				favoritesCount.setTextColor(MediaApplication.color_highlight);
				break;
			case SCREEN_AUDIO_RECENTLY:
				recentlyPlaying.setBackgroundResource(R.drawable.screen_audio_item_recently_press_bg);
				recentlyName.setTextColor(MediaApplication.color_highlight);
				recentlyCount.setTextColor(MediaApplication.color_highlight);
				break;
			case SCREEN_AUDIO_SINGERS:
				singersPlaying.setBackgroundResource(R.drawable.screen_audio_item_singers_press_bg);
				singersName.setTextColor(MediaApplication.color_highlight);
				singersCount.setTextColor(MediaApplication.color_highlight);
				break;
			case SCREEN_AUDIO_SONGS:
				songsPlaying.setBackgroundResource(R.drawable.screen_audio_item_songs_press_bg);
				songsName.setTextColor(MediaApplication.color_highlight);
				songsCount.setTextColor(MediaApplication.color_highlight);
				break;
			}
		}
	}

	@Override
	public void onClick(View v) {
		ScreenArgs args = new ScreenArgs();
		switch (v.getId()) {
		case R.id.screen_audio_playlists_playing:
			audioScreenService.show(ScreenAudioPlaylists.class, args.putExtra("id", SCREEN_AUDIO_PLAYLISTS));
			break;
		case R.id.screen_audio_songs_playing:
			audioScreenService.show(ScreenAudioSongs.class, args.putExtra("id", SCREEN_AUDIO_SONGS));
			break;
		case R.id.screen_audio_singers_playing:
			audioScreenService.show(ScreenAudioSingers.class, args.putExtra("id", SCREEN_AUDIO_SINGERS));
			break;
		case R.id.screen_audio_albums_playing:
			audioScreenService.show(ScreenAudioAlbums.class, args.putExtra("id", SCREEN_AUDIO_ALBUMS));
			break;
		case R.id.screen_audio_favorites_playing:
			audioScreenService.show(ScreenAudioFavorites.class, args.putExtra("id", SCREEN_AUDIO_FAVORITES));
			break;
		case R.id.screen_audio_recently_playing:
			audioScreenService.show(ScreenAudioRecentlySongs.class, args.putExtra("id", SCREEN_AUDIO_RECENTLY));
			break;
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setScreenTitle(getString(R.string.screen_home_tab_audio));
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		setScreenTitle(getString(R.string.screen_home_tab_audio));
		ServiceManager.getAmtMedia().getGoBackBtn().setVisibility(View.INVISIBLE);
		highlight();
	}
	
	public static final int SCREEN_AUDIO_PLAYLISTS = 0;
	public static final int SCREEN_AUDIO_SONGS = 1;
	public static final int SCREEN_AUDIO_SINGERS = 2;
	public static final int SCREEN_AUDIO_ALBUMS = 3;
	public static final int SCREEN_AUDIO_FAVORITES = 4;
	public static final int SCREEN_AUDIO_RECENTLY = 5;

	@Override
	public boolean onEvent(IMediaEventArgs args) {
		//Log.i("ScreenAudio","onEvent()"+args.getMediaUpdateEventTypes());
		Cursor c = null;
		int count = -1;
		switch (args.getMediaUpdateEventTypes()) {
		case AUDIO_UPDATE_PLAYLIST_COUNT:
			c = db.queryPlaylists();
			count = c.getCount();
			c.close();
			playlistsCount.setText(count + getString(R.string.screen_audio_playlists_num));
			break;
		case AUDIO_UPDATE_FAVORITE_COUNT:
			c = db.queryFavoriteAudios();
			count = c.getCount();
			c.close();
			favoritesCount.setText(count + getString(R.string.screen_audio_favourites_num));
			break;
		case AUDIO_UPDATE_SONGS_COUNT:
			c = db.queryAudios();
			count = c.getCount();
			c.close();
			songsCount.setText(count + getString(R.string.screen_audio_songs_num));
			break;
		case AUDIO_UPDATE_SINGER_COUNT:
			c=db.querySingers();
			count=c.getCount();
			c.close();
			singersCount.setText(count+ getString(R.string.screen_audio_singers_num));
			break;
		case AUDIO_UPDATE_ABLUM_COUNT:
			c=db.queryAlbums();
			count=c.getCount();
			c.close();
			albumsCount.setText(count + getString(R.string.screen_audio_albums_num));
			break;
		case AUDIO_UPDATE_RECENTLY_COUNT:
			c=db.queryRecentlyAudios();
			count=c.getCount();
			c.close();
			recentlyCount.setText(count + getString(R.string.screen_audio_recently_num));
			break;
		}
		return true;
	}
	
	public static void refreshCount(int type){
		switch(type){
		case REFRESH_SONGS_COUNT:
			ServiceManager.getMediaEventService().onMediaUpdateEvent(screenAudioFreshArgs
	    			.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_UPDATE_SONGS_COUNT));
	    	ServiceManager.getMediaEventService().onMediaUpdateEvent(screenAudioFreshArgs
	    			.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_UPDATE_SINGER_COUNT));
	    	ServiceManager.getMediaEventService().onMediaUpdateEvent(screenAudioFreshArgs
	    			.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_UPDATE_ABLUM_COUNT));
	    	ServiceManager.getMediaEventService().onMediaUpdateEvent(screenAudioFreshArgs
	    	    	.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_UPDATE_RECENTLY_COUNT));
	    	ServiceManager.getMediaEventService().onMediaUpdateEvent(screenAudioFreshArgs
	    	    	.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_UPDATE_FAVORITE_COUNT));
			break;
		case REFRESH_PLAYLISTS_COUNT:
			ServiceManager.getMediaEventService().onMediaUpdateEvent(screenAudioFreshArgs
	    			.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_UPDATE_PLAYLIST_COUNT));
			break;
		case REFRESH_FAVOURITES_COUNT:
			ServiceManager.getMediaEventService().onMediaUpdateEvent(screenAudioFreshArgs
	    			.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_UPDATE_FAVORITE_COUNT));
			break;
		}
		
	}
	
	/* 根据本地有无歌曲提示用户进行扫描 */
	public void scantipifnosong()
	{
		if (needscan)
		{
			CustomDialog.Builder customBuilder = new CustomDialog.Builder(ServiceManager.getAmtMedia());
			customBuilder.setTitle(getString(R.string.screen_scan_prompt))
			.setWhichViewVisible(CustomDialog.contentIsTextView)
			.setMessage(getString(R.string.screen_scan_right_now))
			.setPositiveButton(getString(R.string.screen_scan_ok), 
	            		new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int which) {
	                	dialog.dismiss();
	                	ServiceManager.getAmtScreenService().show(ScreenScan.class);
	                }
	            })
	            .setNegativeButton(getString(R.string.screen_scan_cancel), 
	            		new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int which) {
	                	dialog.dismiss();
	                }
	            });
			dialog = customBuilder.create();
			dialog.show();	
		}
	}
	
}
