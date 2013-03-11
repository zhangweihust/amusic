/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.amusic.media.dialog;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amusic.media.R;
import com.amusic.media.services.impl.ServiceManager;

/**
 * A on-screen hint is a view containing a little message for the user and will
 * be shown on the screen continuously.  This class helps you create and show
 * those.
 *
 * <p>
 * When the view is shown to the user, appears as a floating view over the
 * application.
 * <p>
 * The easiest way to use this class is to call one of the static methods that
 * constructs everything you need and returns a new OnScreenHint object.
 */
public class OnScreenHint {
    static final String TAG = "OnScreenHint";
    static final boolean LOCAL_LOGV = false;

    final Context mContext;
    int mGravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
    int mX, mY;
    float mHorizontalMargin;
    float mVerticalMargin;
    View mView;
    View mNextView;
    int mduration = 3*1000;

	private TimerTask task;
	private Timer mTimer;
    private final WindowManager.LayoutParams mParams =
            new WindowManager.LayoutParams();
    private final WindowManager mWM;
    private final Handler mHandler = new Handler();

    /**
     * Construct an empty OnScreenHint object.  You must call {@link #setView}
     * before you can call {@link #show}.
     *
     * @param context  The context to use.  Usually your
     *                 {@link android.app.Application} or
     *                 {@link android.app.Activity} object.
     */
    public OnScreenHint(Context context) {
        mContext = ServiceManager.getAmtMedia();
        mWM = (WindowManager) ServiceManager.getAmtMedia().getSystemService(Context.WINDOW_SERVICE);
        mY = ServiceManager.getAmtMedia().getResources().getDimensionPixelSize(
                R.dimen.hint_y_offset);

        mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        mParams.format = PixelFormat.TRANSLUCENT;
        mParams.windowAnimations = R.style.Animation_OnScreenHint;
        mParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
        mParams.setTitle("OnScreenHint");
        mduration = 3 * 1000;
    }

    /**
     * Show the view on the screen.
     */
    public void show() {
        if (mNextView == null) {
            throw new RuntimeException("setView must have been called");
        }
        mHandler.post(mShow);
    }

    /**
     * Close the view if it's showing.
     */
    public void cancel() {
        mHandler.post(mHide);
    }

    /**
     * Set the view to show.
     * @see #getView
     */
    public void setView(View view) {
        mNextView = view;
    }

    /**
     * Return the view.
     * @see #setView
     */
    public View getView() {
        return mNextView;
    }

    public void setPosition(int g, int x, int y){
    	mX = x;
    	mY = y;
    	mGravity = g;
    }
    
    public void setDuration(int t){
        mduration = t;
    }
    
    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
    	final float scale = context.getResources().getDisplayMetrics().density;
    	return (int) (dpValue * scale + 0.5f);
    }
     
    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
    	final float scale = context.getResources().getDisplayMetrics().density;
    	return (int) (pxValue / scale + 0.5f);
    }
    /**
     * Make a standard hint that just contains a text view.
     *
     * @param context  The context to use.  Usually your
     *                 {@link android.app.Application} or
     *                 {@link android.app.Activity} object.
     * @param text     The text to show.  Can be formatted text.
     *
     */
    public static OnScreenHint makeText(Context context, CharSequence text) {
        OnScreenHint result = new OnScreenHint(context);

        LayoutInflater inflate =
                (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View v = inflate.inflate(R.layout.custom_toast_on_screen_hint, null);
        TextView tv = (TextView) v.findViewById(R.id.custom_toast_on_screen_hint_message);
        tv.setText(text);

        result.mNextView = v;
        return result;
    }

    public static OnScreenHint makeText_keyboard(Context context, CharSequence text) {
        OnScreenHint result = new OnScreenHint(context);

        LayoutInflater inflate =
                (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View v = inflate.inflate(R.layout.custom_toast_on_screen_hint, null);
        TextView tv = (TextView) v.findViewById(R.id.custom_toast_on_screen_for_keyboard);
        v.findViewById(R.id.custom_toast_on_screen_hint_message).setVisibility(View.GONE);
        tv.setVisibility(View.VISIBLE);
        tv.setText(text);

        result.mNextView = v;

        return result;
    }
    
    public static OnScreenHint makeText_Empty(Context context,boolean hasN, CharSequence text0, CharSequence text1, CharSequence text2, CharSequence text3) {
        OnScreenHint result = new OnScreenHint(context);

        LayoutInflater inflate =
                (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View v = inflate.inflate(R.layout.custom_toast_on_screen_hint, null);
        LinearLayout l = (LinearLayout) v.findViewById(R.id.custom_toast_on_screen_for_empty);
        v.findViewById(R.id.custom_toast_on_screen_hint_message).setVisibility(View.GONE);
        l.setVisibility(View.VISIBLE);
        TextView tv_0 = (TextView) l.findViewById(R.id.custom_toast_on_screen_for_empty_text0);
        tv_0.setText(text0);
        TextView tv_1 = (TextView) l.findViewById(R.id.custom_toast_on_screen_for_empty_text1);
        tv_1.setText(text1);
        final TextView tv_2 = (TextView) l.findViewById(R.id.custom_toast_on_screen_for_empty_text2);
        tv_2.setText(text2);
        TextView tv_3 = (TextView) l.findViewById(R.id.custom_toast_on_screen_for_empty_text3);
        tv_3.setText(text3); 
        if(hasN){
        	TextView tv_N = (TextView) l.findViewById(R.id.custom_toast_onscreen_for_empty_n);
        	tv_N.setVisibility(View.VISIBLE);
        }
        result.mNextView = v;

        return result;
    }
    /**
     * Make a standard hint that just contains a text view with the text from a
     * resource.
     *
     * @param context  The context to use.  Usually your
     *                 {@link android.app.Application} or
     *                 {@link android.app.Activity} object.
     * @param resId    The resource id of the string resource to use.  Can be
     *                 formatted text.
     *
     * @throws Resources.NotFoundException if the resource can't be found.
     */
    public static OnScreenHint makeText(Context context, int resId)
                                throws Resources.NotFoundException {
        return makeText(context, context.getResources().getText(resId));
    }

    /**
     * Update the text in a OnScreenHint that was previously created using one
     * of the makeText() methods.
     * @param resId The new text for the OnScreenHint.
     */
    public void setText(int resId) {
        setText(mContext.getText(resId));
    }

    /**
     * Update the text in a OnScreenHint that was previously created using one
     * of the makeText() methods.
     * @param s The new text for the OnScreenHint.
     */
    public void setText(CharSequence s) {
        if (mNextView == null) {
            throw new RuntimeException("This OnScreenHint was not "
                    + "created with OnScreenHint.makeText()");
        }
        TextView tv = (TextView) mNextView.findViewById(R.id.custom_toast_on_screen_hint_message);
        if (tv == null) {
            throw new RuntimeException("This OnScreenHint was not "
                    + "created with OnScreenHint.makeText()");
        }
        tv.setText(s);
    }

    private synchronized void handleShow() {
        if (mView != mNextView) {
            // remove the old view if necessary
            handleHide();
            mView = mNextView;
            final int gravity = mGravity;
            mParams.gravity = gravity;
            if ((gravity & Gravity.HORIZONTAL_GRAVITY_MASK)
                    == Gravity.FILL_HORIZONTAL) {
                mParams.horizontalWeight = 1.0f;
            }
            if ((gravity & Gravity.VERTICAL_GRAVITY_MASK)
                    == Gravity.FILL_VERTICAL) {
                mParams.verticalWeight = 1.0f;
            }
            mParams.x = mX;
            mParams.y = mY;
            mParams.verticalMargin = mVerticalMargin;
            mParams.horizontalMargin = mHorizontalMargin;
            if (mView.getParent() != null) {
                mWM.removeView(mView);
            }
            mWM.addView(mView, mParams);
        }
    }

    private synchronized void handleHide() {
        if (mView != null) {
            // note: checking parent() just to make sure the view has
            // been added...  i have seen cases where we get here when
            // the view isn't yet added, so let's try not to crash.
            if (mView.getParent() != null) {
                mWM.removeView(mView);
            }
            mView = null;
        }
    }
	public void exitTimer() {
		mTimer.cancel();
	}
    private final Runnable mShow = new Runnable() {
        public void run() {
            handleShow();
    		mTimer = new Timer();
    		task = new TimerTask() {
    			@Override
    			public void run() {
    				// TODO Auto-generated method stub
    				exitTimer();
    				handleHide();
    			}
    		};
    		mTimer.schedule(task, mduration);
            
        }
    };

    private final Runnable mHide = new Runnable() {
        public void run() {
        	if(mTimer!=null){
        		mTimer.cancel();
        	}
            handleHide();
        }
    };
}

