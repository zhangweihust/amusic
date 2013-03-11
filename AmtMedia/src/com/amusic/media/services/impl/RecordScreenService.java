package com.amusic.media.services.impl;

import java.util.Stack;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.amusic.media.R;
import com.amusic.media.model.ScreenArgs;
import com.amusic.media.screens.IScreen;
import com.amusic.media.screens.impl.ScreenRecordRoot;
import com.amusic.media.services.IRecordScreenService;

public class RecordScreenService implements IRecordScreenService {
	private Stack<String> backList;
	private Stack<Mark> marks;
	private String lastScreenId;

	@Override
	public boolean show(Class<? extends IScreen> screen, boolean addToBack, ScreenArgs args, int visibility) {
		ScreenRecordRoot recordRoot = ServiceManager.getRecordRoot();
		if (args == null) {
			args = new ScreenArgs();
		}
		Intent intent = new Intent(recordRoot, screen);
		intent.putExtra("args", args);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		String screen_id = screen.getCanonicalName();
		Window window = recordRoot.getLocalActivityManager().startActivity(screen_id, intent);
		if (window != null) {
			if (addToBack) {
				backList.push(screen_id);
				Integer id = (Integer) args.getExtra("id");
				marks.push(new Mark(lastScreenId, id));
				lastScreenId = screen_id;
			}
			View element = window.getDecorView();
			LinearLayout root = (LinearLayout) recordRoot.findViewById(R.id.screen_record_root_view);
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
		final IScreen screen = (IScreen) ServiceManager.getRecordRoot().getLocalActivityManager().getActivity(screen_id);
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
