package com.amusic.media.view;

import android.app.ActivityGroup;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.amusic.media.R;
import com.amusic.media.event.IMediaEventArgs;
import com.amusic.media.event.impl.MediaEventArgs;
import com.amusic.media.lyric.render.FullLyricView;
import com.amusic.media.screens.impl.ScreenAudioPlayer;
import com.amusic.media.screens.impl.ScreenAudioSongLyrics;
import com.amusic.media.screens.impl.ScreenAudioSongLyricsFullScreen;
import com.amusic.media.screens.impl.ScreenKMediaPlayer;
import com.amusic.media.services.IMediaEventService;
import com.amusic.media.services.impl.ServiceManager;
import com.amusic.media.utils.Constant;

public class ScrollLayout extends ViewGroup {
	public static final String FIRST_INTENT_TAG = "first";
	public static final String SECOND_INTENT_TAG = "second";
	public static final String THIRD_INTENT_TAG = "third";
	public static final int FIRST_VIEW = 0;
	public static final int SECOND_VIEW = 1;
	public static final int THIRD_VIEW = 2;

	private static final String TAG = "ScrollLayout";

	private Scroller mScroller;

	private VelocityTracker mVelocityTracker;

	private static final int SHOWING_VIEW = 0;

	private int mWidth;

	private static final int TOUCH_STATE_REST = 0;

	private static final int TOUCH_STATE_SCROLLING = 1;

	private static final int SNAP_VELOCITY = 600;

	private int mTouchState = TOUCH_STATE_REST;

	private int mTouchSlop;

	private float mLastMotionX;

	private ActivityGroup mContext;
	
	private int mCurrentScreen = 0;
	
	private IMediaEventService mediaEventService;
	
	IMediaEventArgs eventArgs = new MediaEventArgs();	
	
	public ScrollLayout(Context context, AttributeSet attrs) {

		this(context, attrs, 0);
		mContext = (ActivityGroup) context;
		mediaEventService = ServiceManager.getMediaEventService();

	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
	}

	public ScrollLayout(Context context, AttributeSet attrs, int defStyle) {

		super(context, attrs, defStyle);

		mContext = (ActivityGroup) context;

		mScroller = new Scroller(context);

		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

	}

	@Override
	protected void attachViewToParent(View child, int index, LayoutParams params) {

		super.attachViewToParent(child, index, params);
	}

	@Override
	public void addView(View child) {

		super.addView(child);
	}

	@Override
	public void requestChildFocus(View child, View focused) {

		//Log.d("requestChildFocus", "child = " + child);

		super.requestChildFocus(child, focused);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {

		int childLeft = 0;

		final int childCount = getChildCount();

		for (int i = 0; i < childCount; i++) {

			final View childView = getChildAt(i);

			if (childView.getVisibility() != View.GONE) {

				final int childWidth = childView.getMeasuredWidth();

				childView.layout(childLeft, 0,

				childLeft + childWidth, childView.getMeasuredHeight());

				childLeft += childWidth;

			}

		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		final int width = MeasureSpec.getSize(widthMeasureSpec);

		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		if (widthMode != MeasureSpec.EXACTLY) {

			throw new IllegalStateException("ScrollLayout only can mCurScreen run at EXACTLY mode!");

		}

		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

		if (heightMode != MeasureSpec.EXACTLY) {

			throw new IllegalStateException("ScrollLayout only can run at EXACTLY mode!");

		}

		// The children are given the same width and height as the scrollLayout

		final int count = getChildCount();

		for (int i = 0; i < count; i++) {

			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);

		}
		scrollTo(mCurrentScreen * width, 0);

	}

	/**
	 * 
	 * According to the position of current layout
	 * 
	 * scroll to the destination page...............
	 */

	public void snapToDestination() {

		setMWidth();

		final int destScreen = (getScrollX() + mWidth / 2) / mWidth;

		snapToScreen(destScreen);

	}

	private void setMWidth() {
		if (mWidth == 0) {
			mWidth = getWidth();
		}
	}

	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if(Constant.WHICH_PLAYER == 1){
				ScreenAudioPlayer.dotImageViews[0].setVisibility(View.GONE);
				ScreenAudioPlayer.dotImageViews[1].setVisibility(View.GONE);
			}
			if(Constant.WHICH_PLAYER == 2){
				ScreenKMediaPlayer.dotImageViews[0].setVisibility(View.GONE);
				ScreenKMediaPlayer.dotImageViews[1].setVisibility(View.GONE);
			}
		};
	};
	public void snapToScreen(int whichScreen) {
		handler.sendEmptyMessageDelayed(0, 3000);
		// get the valid layout page

		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
		mCurrentScreen = whichScreen;

		setMWidth();

		int scrollX = getScrollX();
		int startWidth = whichScreen * mWidth;

		if (scrollX != startWidth) {

			int delta = 0;
			int startX = 0;

			if (whichScreen > SHOWING_VIEW) {
//				setPre();
				delta = startWidth - scrollX;
				startX = mWidth - startWidth + scrollX;
				if(Constant.WHICH_PLAYER == 1){
					ScreenAudioPlayer.dotImageViews[1].setImageResource(
							R.drawable.page_indicator_focused);
					ScreenAudioPlayer.dotImageViews[0].setImageResource(
							R.drawable.page_indicator);
				}else if(Constant.WHICH_PLAYER == 2){
					ScreenKMediaPlayer.dotImageViews[1].setImageResource(
							R.drawable.page_indicator_focused);
					ScreenKMediaPlayer.dotImageViews[0].setImageResource(
							R.drawable.page_indicator);
				}else if(Constant.WHICH_PLAYER == 3){
					
				}

			} else if (whichScreen < SHOWING_VIEW) {
				setNext();
				delta = -scrollX;
				startX = scrollX + mWidth;
			} else {
				startX = scrollX;
				delta = startWidth - scrollX;
				if(Constant.WHICH_PLAYER == 1){
					ScreenAudioPlayer.dotImageViews[0].setImageResource(
							R.drawable.page_indicator_focused);
					ScreenAudioPlayer.dotImageViews[1].setImageResource(
							R.drawable.page_indicator);
				}else if(Constant.WHICH_PLAYER == 2){
					ScreenKMediaPlayer.dotImageViews[0].setImageResource(
							R.drawable.page_indicator_focused);
					ScreenKMediaPlayer.dotImageViews[1].setImageResource(
							R.drawable.page_indicator);
				}else if(Constant.WHICH_PLAYER == 3){
					
				}
			}

			mScroller.startScroll(startX, 0, delta, 0, Math.abs(delta) * 2);

			invalidate(); // Redraw the layout

		}

		startCurrentView();

	}

	private void startCurrentView() {

		String viewTag = (String) getChildAt(mCurrentScreen).getTag();

//		if (TextUtils.equals(viewTag, FIRST_INTENT_TAG)) {
//			mContext.getLocalActivityManager().startActivity(FIRST_INTENT_TAG, new Intent(mContext, ScreenAudioSongLyricsWaveform.class));
//		} else if (TextUtils.equals(viewTag, SECOND_INTENT_TAG)) {
//			mContext.getLocalActivityManager().startActivity(SECOND_INTENT_TAG, new Intent(mContext, ScreenAudioSongLyrics.class));
//		} else {
//			mContext.getLocalActivityManager().startActivity(THIRD_INTENT_TAG, new Intent(mContext, ScreenAudioSongLyricsFullScreen.class));
//		}

	    if (TextUtils.equals(viewTag, SECOND_INTENT_TAG)) {
			mContext.getLocalActivityManager().startActivity(SECOND_INTENT_TAG, new Intent(mContext, ScreenAudioSongLyrics.class));
		} else {
			mContext.getLocalActivityManager().startActivity(THIRD_INTENT_TAG, new Intent(mContext, ScreenAudioSongLyricsFullScreen.class));
		}
	    
		// mContext.mHandler.sendMessage(message);
	}

	public void setToScreen(int whichScreen) {

		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));

		scrollTo(whichScreen * mWidth, 0);

		if (whichScreen > SHOWING_VIEW) {
			setPre();
		} else if (whichScreen < SHOWING_VIEW) {
			setNext();
		}

	}

	public View getCurScreen() {

		return this.getChildAt(mCurrentScreen);

	}

	@Override
	public void computeScroll() {

		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();

		}

	}

	private void setNext() {
		int count = this.getChildCount();
		View view = getChildAt(count - 1);
		removeViewAt(count - 1);
		addView(view, 0);
	}

	private void setPre() {
		int count = this.getChildCount();
		View view = getChildAt(0);
		removeViewAt(0);
		addView(view, count - 1);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(Constant.WHICH_PLAYER == 1){
			ScreenAudioPlayer.dotImageViews[0].setVisibility(View.VISIBLE);
			ScreenAudioPlayer.dotImageViews[1].setVisibility(View.VISIBLE);
		}
		if(Constant.WHICH_PLAYER == 2){
			ScreenKMediaPlayer.dotImageViews[0].setVisibility(View.VISIBLE);
			ScreenKMediaPlayer.dotImageViews[1].setVisibility(View.VISIBLE);
		}
		if (FullLyricView.mIsMoved) {
			return false;
		}

		if (mVelocityTracker == null) {

			mVelocityTracker = VelocityTracker.obtain();

		}

		mVelocityTracker.addMovement(event);

		final int action = event.getAction();

		final float x = event.getX();

		switch (action) {

		case MotionEvent.ACTION_DOWN:

			//Log.d(TAG, "event down!");
			//mediaEventService.onMediaUpdateEvent(eventArgs.setMediaUpdateEventTypes(MediaEventTypes.ACTION_DOWN));

			if (!mScroller.isFinished()) {

				mScroller.abortAnimation();

			}

			mLastMotionX = x;

			break;

		case MotionEvent.ACTION_MOVE:

			int deltaX = (int) (mLastMotionX - x);

			mLastMotionX = x;

			scrollBy(deltaX, 0);

			break;

		case MotionEvent.ACTION_UP:

			// if (mTouchState == TOUCH_STATE_SCROLLING) {

			final VelocityTracker velocityTracker = mVelocityTracker;

			velocityTracker.computeCurrentVelocity(1000);

			int velocityX = (int) velocityTracker.getXVelocity();

			//Log.d(TAG, "velocityX:" + velocityX + "; event : up");

			if (velocityX > SNAP_VELOCITY) {

				// Fling enough to move left

				//Log.d(TAG, "snap left");

				snapToScreen(SHOWING_VIEW - 1);

			} else if (velocityX < -SNAP_VELOCITY

			&& SHOWING_VIEW < getChildCount() - 1) {

				// Fling enough to move right

				//Log.d(TAG, "snap right");

				snapToScreen(SHOWING_VIEW + 1);

			} else {

				snapToDestination();

			}

			if (mVelocityTracker != null) {

				mVelocityTracker.recycle();

				mVelocityTracker = null;

			}

			// }

			mTouchState = TOUCH_STATE_REST;
			break;

		case MotionEvent.ACTION_CANCEL:

			mTouchState = TOUCH_STATE_REST;

			break;

		}

		return true;

	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {

		//Log.d(TAG, "onInterceptTouchEvent-slop:" + mTouchSlop);

		final int action = ev.getAction();

		if ((action == MotionEvent.ACTION_MOVE) &&

		(mTouchState != TOUCH_STATE_REST)) {

			return true;

		}

		final float x = ev.getX();

		switch (action) {

		case MotionEvent.ACTION_MOVE:

			final int xDiff = (int) Math.abs(mLastMotionX - x);

			if (xDiff > mTouchSlop && FullLyricView.mIsMoved) {

				mTouchState = TOUCH_STATE_SCROLLING;

			}

			break;

		case MotionEvent.ACTION_DOWN:

			mLastMotionX = x;

			mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;

			break;

		case MotionEvent.ACTION_CANCEL:

		case MotionEvent.ACTION_UP:

			mTouchState = TOUCH_STATE_REST;

			break;

		}

		return mTouchState != TOUCH_STATE_REST;

	}

	@Override
	protected void onAttachedToWindow() {

		//Log.d("Windows", "onAttachedToWindow -- >" + getChildAt(SHOWING_VIEW).toString());

		startCurrentView();
		super.onAttachedToWindow();
	}

	@Override
	public void dispatchWindowFocusChanged(boolean hasFocus) {

		//Log.d("Windows", "dispatchWindowFocusChanged -- >" + getChildAt(SHOWING_VIEW).toString());
		super.dispatchWindowFocusChanged(hasFocus);
	}

	@Override
	public void dispatchWindowVisibilityChanged(int visibility) {
		//Log.d("Windows", "dispatchWindowVisibilityChanged -- >" + getChildAt(SHOWING_VIEW).toString());

		super.dispatchWindowVisibilityChanged(visibility);
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {

		//Log.d("Windows", "onWindowFocusChanged -- >" + getChildAt(SHOWING_VIEW).toString());

		super.onWindowFocusChanged(hasWindowFocus);
	}

	@Override
	protected void onWindowVisibilityChanged(int visibility) {

		//Log.d("Windows", "onWindowVisibilityChanged -- >" + getChildAt(SHOWING_VIEW).toString());

		super.onWindowVisibilityChanged(visibility);
	}

	@Override
	protected void onDetachedFromWindow() {
		try {
			super.onDetachedFromWindow();
		} catch (IllegalArgumentException e) {
		}
	}

}
