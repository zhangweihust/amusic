package com.amusic.media.services.impl;

import java.util.Stack;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.amusic.media.AmtMedia;
import com.amusic.media.R;
import com.amusic.media.model.ScreenArgs;
import com.amusic.media.screens.IScreen;
import com.amusic.media.screens.impl.ScreenAudioPlayer;
import com.amusic.media.screens.impl.ScreenKMediaPlayer;
import com.amusic.media.screens.impl.ScreenRecordPlayer;
import com.amusic.media.services.IAmtScreenService;
import com.amusic.media.utils.Constant;

public class AmtScreenService implements IAmtScreenService {
	private Stack<String> backList;
	private Stack<Mark> marks;
	private String lastScreenId;
	
	@Override
	public Stack<String> getBackList() {
		return backList;
	}

	@Override
	public boolean show(Class<? extends IScreen> screen, boolean addToBack, ScreenArgs args, int visibility) {
		String screen_id = screen.getCanonicalName();
/*		if (backList.contains(screen_id)) {
			return true;
		}*/
		AmtMedia amtMedia = ServiceManager.getAmtMedia();
		if (args == null) {
			args = new ScreenArgs();
		}
		Intent intent = new Intent(amtMedia, screen);
		intent.putExtra("args", args);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		
		if(screen_id.equals(ScreenAudioPlayer.class.getCanonicalName()) || screen_id.equals(ScreenKMediaPlayer.class.getCanonicalName())
				|| screen_id.equals(ScreenRecordPlayer.class.getCanonicalName())) {
			if(screen_id.equals(ScreenKMediaPlayer.class.getCanonicalName())){
				screen_id += Constant.KMEDIA_COUNT;
			}
			if(screen_id.equals(ScreenRecordPlayer.class.getCanonicalName())){
				screen_id += Constant.RECORD_COUNT;
			}
			visibility = View.GONE;
		}

		Window window = amtMedia.getLocalActivityManager().startActivity(screen_id, intent);
		if (window != null) {
			if (addToBack) {
				//ScreenLogo��ת��ScreenHome�����ʱ��û�д�args������marks.push��һ��NULL.
				backList.push(screen_id);
				Integer id = (Integer) args.getExtra("id");
				if (args.getExtra("screenId") != null) {
					lastScreenId = (String) args.getExtra("screenId");
				}
				marks.push(new Mark(lastScreenId, id));
				lastScreenId = screen_id;
			}
			View element = window.getDecorView();
			LinearLayout root = (LinearLayout) amtMedia.findViewById(R.id.amt_media_root_view);
			RelativeLayout bottom = (RelativeLayout) amtMedia.findViewById(R.id.screen_top_play_control);
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
		Mark mark = marks.pop();
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
		IScreen screen = (IScreen) ServiceManager.getAmtMedia().getLocalActivityManager().getActivity(screen_id);
		if (screen == null) {
			try {
				@SuppressWarnings("unchecked")
				Class<? extends IScreen> screenClazz = (Class<? extends IScreen>) Class.forName(screen_id);
				return show(screenClazz, addToBack, args, View.VISIBLE);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
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

	@Override
	public Stack<Mark> getMarks() {
		return marks;
	}

}
