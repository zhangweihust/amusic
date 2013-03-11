package com.amusic.media.screens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.amusic.media.MediaApplication;
import com.amusic.media.download.DownloadJob;
import com.amusic.media.event.impl.MediaEventArgs;
import com.amusic.media.event.impl.MediaEventTypes;
import com.amusic.media.model.ScreenArgs;
import com.amusic.media.model.SongInfo;
import com.amusic.media.provider.MediaManagerDB;
import com.amusic.media.screens.impl.ScreenAudio;
import com.amusic.media.screens.impl.ScreenAudioPlaylists;
import com.amusic.media.services.IAmtScreenService;
import com.amusic.media.services.IAudioScreenService;
import com.amusic.media.services.IKMediaScreenService;
import com.amusic.media.services.IMediaEventService;
import com.amusic.media.services.IMediaPlayerService;
import com.amusic.media.services.IMediaService;
import com.amusic.media.services.IRecordScreenService;
import com.amusic.media.services.ISearchScreenService;
import com.amusic.media.services.impl.ServiceManager;

public class Screen extends Activity implements IScreen, OnScrollListener {
	private final List<Integer> handlerIds = new ArrayList<Integer>();

	private final byte[] hdLock = new byte[0];

	protected ScreenType type;
	protected final IMediaService mediaService;
	protected final IMediaEventService mediaEventService;
	protected final IMediaPlayerService mediaPlayerService;
	protected final IAudioScreenService audioScreenService;
	protected final IAmtScreenService amtScreenService;
	protected final IKMediaScreenService kMediaScreenService;
	protected final IRecordScreenService recordScreenService;
	protected final ISearchScreenService searchScreenService;
	protected int savePosition;
	protected ListView listView;
	protected ListView list;
	protected Cursor cursor;
	protected final MediaManagerDB db;
	protected final Handler handler;
	protected Map<Integer, Cursor> cursorMap =new HashMap<Integer, Cursor>();
	public Screen(ScreenType type) {
		this.type = type;
		handler = new Handler();
		db = ServiceManager.getMediaService().getMediaDB();
		this.mediaEventService = ServiceManager.getMediaEventService();
		this.mediaService = ServiceManager.getMediaService();
		this.mediaPlayerService = ServiceManager.getMediaplayerService();
		this.amtScreenService = ServiceManager.getAmtScreenService();
		this.audioScreenService = ServiceManager.getAudioScreenService();
		this.kMediaScreenService = ServiceManager.getKMediaScreenService();
		this.recordScreenService = ServiceManager.getRecordScreenService();
		this.searchScreenService = ServiceManager.getSearchScreenService();
	}

	public byte[] gethdLock() {
		return hdLock;
	}

	public List<Integer> getHandlerIds() {
		return handlerIds;
	}


	protected void clearDownloadHandlers() {
		DownloadJob downloadMusic;
		synchronized (gethdLock()) {
			synchronized (mediaService.getDmLock()) {
				Iterator<Integer> iterator = getHandlerIds().iterator();
				while (iterator.hasNext()) {
					Integer songId = iterator.next();
					iterator.remove();
					downloadMusic = mediaService.getDownloadMap().get(songId);
					if (downloadMusic != null && downloadMusic.getDownloadTask() != null) {
						downloadMusic.getDownloadTask().unregisterHandler();
					}
				}
			}
		}
	}


	@Override
	protected void onSaveInstanceState(Bundle outState) {
		ScreenArgs args = new ScreenArgs();
		args.putExtra("savePosition", savePosition);
		outState.putSerializable("args", args);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		switch (getScrollType()) {
		case SCROLL_TYPE_LISTVIEW:
			savePosition = listView.getFirstVisiblePosition();
			break;
		case SCROLL_TYPE_GRIDVIEW:
			savePosition = list.getFirstVisiblePosition();
			break;
		}

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

	}

	public boolean hasMenu() {
		return false;
	}

	public boolean refresh() {
		return false;
	}
	
	public boolean refresh(ArrayList<SongInfo> result){
		return false;
	}

	protected int getScrollType() {
		if (listView != null) {
			return SCROLL_TYPE_LISTVIEW;
		}
		if (list != null) {
			return SCROLL_TYPE_GRIDVIEW;
		}
		return SCROLL_TYPE_DEFAULT;
	}

	public void showOptions() {
		// TODO Auto-generated method stub

	}

	public void setScreenTitle(String title) {
		MediaEventArgs args = new MediaEventArgs();
		args.putExtra("screenTitle", title);
		mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.SCREEN_TITLE_REFRESH));
	}

	@Override
	public boolean currentable() {
		return true;
	}

	public Integer getHighlightId() {
		return mediaPlayerService.getPlayingMarks().get(this.getClass().getCanonicalName());
	}
	
	public Integer getHighlightPlaylistId() {
		return mediaPlayerService.getPlayingMarks().get(ScreenAudioPlaylists.class.getCanonicalName());
	}
	
	public Integer setHighlightPlaylistId(Integer playlistId) {
		return mediaPlayerService.getPlayingMarks().put(ScreenAudioPlaylists.class.getCanonicalName(), playlistId);
	}
	
	public Integer setHighlightScreenAudioId(int id) {
		return mediaPlayerService.getPlayingMarks().put(ScreenAudio.class.getCanonicalName(),id);
	}
	

	public ScreenType getScreenType() {
		return type;
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

	@Override
	protected void onResume() {
		ServiceManager.getAmtMedia().getGoBackBtn().setVisibility(View.VISIBLE);
		if (MediaApplication.getInstance().isVisible()) {
		    ServiceManager.getAmtMedia().getGoPlayerBtn().setVisibility(View.VISIBLE);
		}
		super.onResume();
	}
	
	
}
