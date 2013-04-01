package com.android.media.services.impl;

import java.util.Iterator;
import java.util.Stack;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.amusic.media.R;
import com.android.media.MediaApplication;
import com.android.media.model.ScreenArgs;
import com.android.media.screens.IScreen;
import com.android.media.screens.impl.ScreenAudio;
import com.android.media.screens.impl.ScreenAudioPlaylistEdit;
import com.android.media.screens.impl.ScreenAudioPlaylists;
import com.android.media.screens.impl.ScreenAudioRoot;
import com.android.media.services.IAudioScreenService;

public class AudioScreenService implements IAudioScreenService {
	private Stack<String> backList;
	private Stack<Mark> marks;
	private String lastScreenId;

	@Override
	public boolean show(Class<? extends IScreen> screen, boolean addToBack, ScreenArgs args, int visibility) {
		ScreenAudioRoot audioRoot = ServiceManager.getAudioRoot();
		if (args == null) {
			args = new ScreenArgs();
		}
		Intent intent = new Intent(audioRoot, screen);
		intent.putExtra("args", args);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		String screen_id = screen.getCanonicalName();
		Window window = audioRoot.getLocalActivityManager().startActivity(screen_id, intent);
		if (window != null) {
			if (addToBack) {
				//��ΪScreenAudioRoot��ת��ScreenAudio��ʱ��û�д�args������marks.push��һ��NULL.
				MediaApplication.logD(MediaPlayerService.class, "%%%%%%%%%%%%%%fffffff%%%%%%%%%%%%%%%%%%%%%%:" + lastScreenId);
				backList.push(screen_id);
				Integer id = (Integer) args.getExtra("id");
				if(ScreenAudioPlaylistEdit.class.getCanonicalName().equals(lastScreenId)){
					MediaApplication.logD(MediaPlayerService.class, "%%%%%%%%%%%%%%fffffff%%%%%%%%%%%%%%%%%%%%%%");
					lastScreenId = ScreenAudioPlaylists.class.getCanonicalName();
				}
				marks.push(new Mark(lastScreenId, id));
				lastScreenId = screen_id;
			}
			MediaApplication.logD(MediaPlayerService.class, "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
			Iterator<Mark> iterator = marks.iterator();
			while (iterator.hasNext()) {
				Mark mark = iterator.next();
				MediaApplication.logD(MediaPlayerService.class, mark.getScreenId() + "----" + mark.getId());
			}
			MediaApplication.logD(MediaPlayerService.class, "---------------------------------------------");
			Iterator<String> iterator1 = backList.iterator();
			while (iterator1.hasNext()) {
				String mark = iterator1.next();
				MediaApplication.logD(MediaPlayerService.class, mark);
			}
			MediaApplication.logD(MediaPlayerService.class, "---------------------------------------------");
			View element = window.getDecorView();
			LinearLayout root = (LinearLayout) audioRoot.findViewById(R.id.screen_audio_root_view);
			RelativeLayout bottom = (RelativeLayout) ServiceManager.getAmtMedia().findViewById(R.id.screen_top_play_control);
			if (visibility == View.VISIBLE) {
				bottom.setVisibility(View.VISIBLE);
			} else if (visibility == View.GONE) {
				bottom.setVisibility(View.GONE);
			}
			root.removeAllViews();
			root.addView(element, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean start() {
		backList = new Stack<String>();
		marks = new Stack<Mark>();
		return true;
	}

	@Override
	public boolean stop() {
		backList = null;
		marks = null;
		return true;
	}

	@Override
	public boolean goback() {
		if (backList.isEmpty()) {
			return false;
		}
		backList.pop();
		marks.pop();

		if (backList.isEmpty()) {
			return false;
		}
		String screen_id = backList.pop();
		MediaApplication.logD(AudioScreenService.class, "backlist回退:" + screen_id);
		Mark mark = marks.pop();
		MediaApplication.logD(AudioScreenService.class, "Marks回退:" + mark.getScreenId() + "///" + mark.getId());
		if(ScreenAudio.class.getCanonicalName().equals(mark.getScreenId())){
			marks.push(new Mark(mark.getScreenId(), mark.getId()));
		}
		ScreenArgs args = new ScreenArgs();
		args.putExtra("id", mark.getId());
		args.putExtra("goback", true);
		return show(screen_id, true, args);
	}

	@Override
	public boolean show(Class<? extends IScreen> screen) {
		return show(screen, true);
	}

	@Override
	public boolean show(String screen_id, boolean addToBack) {
		return show(screen_id, addToBack, null);
	}

	@Override
	public boolean show(String screen_id, boolean addToBack, ScreenArgs args) {
		final IScreen screen = (IScreen) ServiceManager.getAudioRoot().getLocalActivityManager().getActivity(screen_id);
		return show(screen.getClass(), addToBack, args, View.VISIBLE);
	}

	@Override
	public boolean show(String screen_id) {

		return show(screen_id, true);
	}

	@Override
	public boolean show(Class<? extends IScreen> screen, boolean addToBack) {
		return show(screen, addToBack, null, View.VISIBLE);
	}

	@Override
	public boolean show(Class<? extends IScreen> screen, ScreenArgs args) {
		return show(screen, true, args, View.VISIBLE);
	}

	@Override
	public boolean show(Class<? extends IScreen> screen, ScreenArgs args, int visibility) {
		return show(screen, true, args, visibility);
	}

	@Override
	public boolean show(Class<? extends IScreen> screen, int visibility) {
		return show(screen, true, null, visibility);
	}

	@Override
	public boolean show(Class<? extends IScreen> screen, boolean addToBack, int visibility) {
		return show(screen, addToBack, null, visibility);
	}

	public Stack<Mark> getMarks() {
		return marks;
	}

	@Override
	public Stack<String> getBackList() {
		return backList;
	}
}
