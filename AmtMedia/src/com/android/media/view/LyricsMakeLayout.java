package com.android.media.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class LyricsMakeLayout extends LinearLayout {

	public LyricsMakeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return this.getChildAt(1).onTouchEvent(event);
	}
}
