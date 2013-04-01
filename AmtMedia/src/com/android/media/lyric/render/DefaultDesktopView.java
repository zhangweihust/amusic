package com.android.media.lyric.render;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.amusic.media.R;
import com.android.media.MediaApplication;
import com.android.media.event.impl.MediaEventArgs;
import com.android.media.event.impl.MediaEventTypes;
import com.android.media.services.IMediaEventService;
import com.android.media.services.impl.ServiceManager;

public class DefaultDesktopView extends TextView implements
View.OnClickListener{
	private float mTouchStartY;
	private float y;
	private float oldY;
	private View controllView;
	private Context context;
	private WindowManager wm = (WindowManager) getContext()
			.getApplicationContext().getSystemService("window");
	private WindowManager.LayoutParams wmParams = ((MediaApplication) getContext()
			.getApplicationContext()).getWmParams();
	private WindowManager.LayoutParams controllViewparams = new WindowManager.LayoutParams();
	private ImageView previous;
	private ImageView pause;
	private ImageView next;
	private ImageView exit;
	private static IMediaEventService mediaEventService;
	private int textSize = 18;
	private static int TOP = 0;
	
	public DefaultDesktopView(Context context) {
		super(context);
		this.context = context;
		mediaEventService = ServiceManager.getMediaEventService();
	}

	public DefaultDesktopView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		mediaEventService = ServiceManager.getMediaEventService();
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PhoneKTVView);  
		textSize = (int) a.getDimension(R.styleable.PhoneKTVView_textSize, textSize);
		a.recycle();
	}

//	@Override
//	protected void onDraw(Canvas canvas) {
//		// TODO Auto-generated method stub
//		super.onDraw(canvas);
//	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (TOP == 0) {
			Rect outRect = new Rect();
			this.getWindowVisibleDisplayFrame(outRect);
			TOP = outRect.top;
		}
		y = event.getRawY() - TOP;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			oldY = event.getRawY() - TOP;
			mTouchStartY = event.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			updateViewPosition();
			break;
		case MotionEvent.ACTION_UP:
			updateViewPosition();
			if (Math.abs(y - oldY) <= 5) {
				showControllView();
			}
			mTouchStartY = 0;
			break;
		}
		return true;
	}
	
	private void updateViewPosition() {
		wmParams.y = (int) (y - mTouchStartY);
		wm.updateViewLayout(this, wmParams);
	}
	
	private void showControllView() {
		if (controllView == null) {
			LayoutInflater inflater = LayoutInflater.from(context);
			controllView = inflater.inflate(R.layout.screen_controllview, null);
			previous = (ImageView) controllView
					.findViewById(R.id.audio_previous);
			pause = (ImageView) controllView.findViewById(R.id.audio_state);
			next = (ImageView) controllView.findViewById(R.id.audio_next);
			exit = (ImageView) controllView.findViewById(R.id.audio_exit);

			previous.setOnClickListener(this);
			pause.setOnClickListener(this);
			next.setOnClickListener(this);
			exit.setOnClickListener(this);

			addControllView();
			handler.sendEmptyMessageDelayed(0, 3000);
		}
	}
	
	private void addControllView(){
		controllViewparams.type = WindowManager.LayoutParams.TYPE_PHONE;
		controllViewparams.format = 1;
		controllViewparams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		controllViewparams.gravity = Gravity.LEFT | Gravity.TOP;
		controllViewparams.width = LayoutParams.FILL_PARENT;
		controllViewparams.height = getResources().getDimensionPixelSize(R.dimen.LinearLayout_desktop_lev1_layout_height);
		controllViewparams.x = (MediaApplication.getScreenWidth() - controllViewparams.width) / 2;
		controllViewparams.y = wmParams.y - getResources().getDimensionPixelSize(R.dimen.LinearLayout_desktop_lev1_Spacing_ControllView);
		wm.addView(controllView, controllViewparams);
	}

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (controllView != null) {
				wm.removeView(controllView);
				controllView = null;
			}
		};
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.audio_previous:
			mediaEventService
					.onMediaUpdateEvent(new MediaEventArgs()
							.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_PREVIOUS));
			break;
		case R.id.audio_state:
			DefaultDesktopView.this.setVisibility(View.GONE);
			mediaEventService
					.onMediaUpdateEvent(new MediaEventArgs()
							.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_PAUSE));
			break;
		case R.id.audio_next:
			mediaEventService
					.onMediaUpdateEvent(new MediaEventArgs()
							.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_NEXT));
			break;
		case R.id.audio_exit:
			break;
		}
		wm.removeView(controllView);
		controllView = null;
	}
}
