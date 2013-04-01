package com.android.media.lyric.render;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import android.widget.Button;
import android.widget.ImageView;

import com.amusic.media.R;
import com.android.media.AmtMedia;
import com.android.media.MediaApplication;
import com.android.media.event.IMediaEventArgs;
import com.android.media.event.impl.MediaEventArgs;
import com.android.media.event.impl.MediaEventTypes;
import com.android.media.services.IMediaEventService;
import com.android.media.services.impl.DesktopLyricService;
import com.android.media.services.impl.ServiceManager;
import com.android.media.utils.Constant;

public class DesktopKTVView extends PhoneKTVView implements
		View.OnClickListener {
	private float mTouchStartY;
	private float y;
	private float oldY;
	private View controllView;
	private View settingView;
	private View closeDesktopLyricView;

	private Context context;
	private WindowManager wm = (WindowManager) getContext()
			.getApplicationContext().getSystemService("window");
	private WindowManager.LayoutParams wmParams = ((MediaApplication) getContext()
			.getApplicationContext()).getWmParams();
	private WindowManager.LayoutParams controllViewparams = new WindowManager.LayoutParams();
	private WindowManager.LayoutParams settingViewparams = new WindowManager.LayoutParams();
	private WindowManager.LayoutParams closeDesktopLyricparams = new WindowManager.LayoutParams();
	private ImageView setting;
	private ImageView previous;
	private ImageView state;
	private ImageView next;
	private ImageView exit;
	
	private ImageView fontLarge;
	private ImageView fontSmall;
	private ImageView desktopLogo;
	private ImageView fontColor1;
	private ImageView fontColor2;
	private ImageView fontColor3;
	private ImageView fontColor4;
	private ImageView fontColor5;
	
	private Button closeOk;
	private Button closeCancel;
	private static IMediaEventService mediaEventService;

	private static int TOP = 0;
	
	private int oldWmParams;
	private boolean flag = false;

	public DesktopKTVView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		this.context = context;
		this.setZOrderOnTop(false);
		mediaEventService = ServiceManager.getMediaEventService();
	}

	public DesktopKTVView(Context context) {
		super(context);
		this.context = context;
		this.setZOrderOnTop(false);
		mediaEventService = ServiceManager.getMediaEventService();
	}

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
			if(controllView == null && settingView == null){
				updateViewPosition();
			}
			break;
		case MotionEvent.ACTION_UP:
			if(controllView == null && settingView == null){
				updateViewPosition();
			}
			if (Math.abs(y - oldY) <= 5) {
				if(settingView == null){
					if(controllView == null){
						showControllView();
					}else{
						wm.removeView(controllView);
						controllView = null;
					}
				}
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
			
			setting = (ImageView) controllView.findViewById(R.id.desktop_lyric_set);
			previous = (ImageView) controllView.findViewById(R.id.audio_previous);
			state = (ImageView) controllView.findViewById(R.id.audio_state);
			next = (ImageView) controllView.findViewById(R.id.audio_next);
			exit = (ImageView) controllView.findViewById(R.id.audio_exit);

			setting.setOnClickListener(this);
			previous.setOnClickListener(this);
			state.setOnClickListener(this);
			next.setOnClickListener(this);
			exit.setOnClickListener(this);

			if(ServiceManager.getMediaplayerService().getMediaPlayer()
					.isPlaying()){
				state.setBackgroundResource(R.drawable.screen_audio_player_pause_selector);
			}else{
				state.setBackgroundResource(R.drawable.screen_audio_player_play_selector);
			}
			addControllView();
			handler.sendEmptyMessageDelayed(0, 3000);
		}
	}
	
	private void showSettingView(){
		if(settingView == null){
			LayoutInflater inflater = LayoutInflater.from(context);
			settingView = inflater.inflate(R.layout.screen_desktop_setting_view, null);
			
			fontLarge = (ImageView) settingView.findViewById(R.id.desktop_lyric_font_large);
			fontSmall = (ImageView) settingView.findViewById(R.id.desktop_lyric_font_small);
			fontColor1 = (ImageView) settingView.findViewById(R.id.desktop_lyric_color1);
			fontColor2 = (ImageView) settingView.findViewById(R.id.desktop_lyric_color2);
			fontColor3 = (ImageView) settingView.findViewById(R.id.desktop_lyric_color3);
			fontColor4 = (ImageView) settingView.findViewById(R.id.desktop_lyric_color4);
			fontColor5 = (ImageView) settingView.findViewById(R.id.desktop_lyric_color5);
			desktopLogo = (ImageView) settingView.findViewById(R.id.desktop_lyric_logo);

			fontLarge.setOnClickListener(this);
			fontSmall.setOnClickListener(this);
			desktopLogo.setOnClickListener(this);
			fontColor1.setOnClickListener(this);
			fontColor2.setOnClickListener(this);
			fontColor3.setOnClickListener(this);
			fontColor4.setOnClickListener(this);
			fontColor5.setOnClickListener(this);
			
			addSettingView();
		}
		
	}
	
	private void addSettingView(){
		settingViewparams.type = WindowManager.LayoutParams.TYPE_PHONE;
		settingViewparams.format = 1;
		settingViewparams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		settingViewparams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
//		settingViewparams.width = Constant.DESKTOP_LYRIC_WIDTH/*LayoutParams.FILL_PARENT*/;
		settingViewparams.width = getResources().getDimensionPixelSize(R.dimen.LinearLayout_desktop_setting_layout_width);
		settingViewparams.height = getResources().getDimensionPixelSize(R.dimen.LinearLayout_desktop_setting_layout_height);
//		settingViewparams.x = (MediaApplication.getScreenWidth() - controllViewparams.width) / 2;
		settingViewparams.y = wmParams.y - getResources().getDimensionPixelSize(R.dimen.LinearLayout_desktop_lev1_Spacing_SettingView);
		if(settingViewparams.y < 0){
			settingViewparams.y = controllViewparams.y + getResources().getDimensionPixelSize(R.dimen.LinearLayout_desktop_lev1_Spacing_ControllView);
			if(settingViewparams.y + settingViewparams.height >= MediaApplication.getScreenHeight()){
				flag = true;
				oldWmParams = wmParams.y;
				wmParams.y = 0;
				controllViewparams.y = wmParams.y + getResources().getDimensionPixelSize(R.dimen.LinearLayout_desktop_lev1_Spacing_ControllView);
				settingViewparams.y = controllViewparams.y + getResources().getDimensionPixelSize(R.dimen.LinearLayout_desktop_lev1_Spacing_ControllView);
				wm.updateViewLayout(this, wmParams);
				wm.removeView(controllView);
				showControllView();
				addControllView();
//				wm.updateViewLayout(this, controllViewparams);
//				wm.updateViewLayout(this, settingViewparams);
			}
		}else if(wmParams.y + wmParams.height + getResources().getDimensionPixelSize(R.dimen.LinearLayout_desktop_lev1_Spacing_SettingView) >= MediaApplication.getScreenHeight()){
			if(controllViewparams.y > wmParams.y){
				settingViewparams.y = wmParams.y - getResources().getDimensionPixelSize(R.dimen.LinearLayout_desktop_lev1_Spacing_SettingView);
			}else{
				settingViewparams.y = controllViewparams.y - getResources().getDimensionPixelSize(R.dimen.LinearLayout_desktop_lev1_Spacing_SettingView);
			}
		}
		wm.addView(settingView, settingViewparams);
	}
	private void addControllView(){
		controllViewparams.type = WindowManager.LayoutParams.TYPE_PHONE;
		controllViewparams.format = 1;
		controllViewparams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		controllViewparams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
//		controllViewparams.width = Constant.DESKTOP_LYRIC_WIDTH/*LayoutParams.FILL_PARENT*/;
		controllViewparams.width = getResources().getDimensionPixelSize(R.dimen.LinearLayout_desktop_setting_layout_width);
		controllViewparams.height = getResources().getDimensionPixelSize(R.dimen.LinearLayout_desktop_lev1_layout_height);
//		controllViewparams.x = (MediaApplication.getScreenWidth() - controllViewparams.width) / 2;
		controllViewparams.y = wmParams.y + getResources().getDimensionPixelSize(R.dimen.LinearLayout_desktop_lev1_Spacing_ControllView);
		if(wmParams.y + wmParams.height + getResources().getDimensionPixelSize(R.dimen.LinearLayout_desktop_lev1_Spacing_ControllView) >= MediaApplication.getScreenHeight()){
			controllViewparams.y = wmParams.y - getResources().getDimensionPixelSize(R.dimen.LinearLayout_desktop_lev1_Spacing_ControllView);
		}
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

	private void closeDesktopLyric(){
		LayoutInflater inflater = LayoutInflater.from(context);
		closeDesktopLyricView = inflater.inflate(R.layout.desktop_lyric_exit_dialog, null);
		closeOk = (Button) closeDesktopLyricView.findViewById(R.id.close_ok);
		closeCancel = (Button) closeDesktopLyricView.findViewById(R.id.close_cancel);
		closeOk.setOnClickListener(this);
		closeCancel.setOnClickListener(this);
		closeDesktopLyricparams.type = WindowManager.LayoutParams.TYPE_PHONE;
		closeDesktopLyricparams.format = 1;
		closeDesktopLyricparams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		closeDesktopLyricparams.gravity = Gravity.CENTER;
		closeDesktopLyricparams.width = LayoutParams.WRAP_CONTENT;
		closeDesktopLyricparams.height = LayoutParams.WRAP_CONTENT;
		wm.addView(closeDesktopLyricView, closeDesktopLyricparams);
	}
	@Override
	public void onClick(View v) {
		handler.removeMessages(0);
		switch (v.getId()) {
		case R.id.desktop_lyric_set:
			if(settingView != null){
				wm.removeView(settingView);
				settingView = null;
				if(flag){
					flag = false;
					wmParams.y = oldWmParams;
					wm.updateViewLayout(this, wmParams);
					wm.removeView(controllView);
					showControllView();
					addControllView();
				}
				handler.sendEmptyMessageDelayed(0, 3000);
			}else{
				showSettingView();
			}
			break;
		case R.id.audio_previous:
			if(settingView == null){
				handler.sendEmptyMessageDelayed(0, 3000);
			}
			IMediaEventArgs previousArgs = new MediaEventArgs();
			previousArgs.putExtra("id", -1);
			mediaEventService
					.onMediaUpdateEvent(previousArgs
							.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_PREVIOUS));
			state.setBackgroundResource(R.drawable.screen_audio_player_pause_selector);
			break;
		case R.id.audio_state:
			if(settingView == null){
				handler.sendEmptyMessageDelayed(0, 3000);
			}
			if(ServiceManager.getMediaplayerService().getMediaPlayer()
					.isPlaying()){
				mediaEventService
				.onMediaUpdateEvent(new MediaEventArgs()
						.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_PAUSE));
				state.setBackgroundResource(R.drawable.screen_audio_player_play_selector);
				
			}else{
				mediaEventService
				.onMediaUpdateEvent(new MediaEventArgs()
						.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_CONTINUE));
				state.setBackgroundResource(R.drawable.screen_audio_player_pause_selector);
			}
			break;
		case R.id.audio_next:
			if(settingView == null){
				handler.sendEmptyMessageDelayed(0, 3000);
			}
			IMediaEventArgs nextArgs = new MediaEventArgs();
			nextArgs.putExtra("id", -1);
			mediaEventService
					.onMediaUpdateEvent(nextArgs
							.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_NEXT));
			state.setBackgroundResource(R.drawable.screen_audio_player_pause_selector);
			break;
		case R.id.audio_exit:
			closeDesktopLyric();
			break;
		case R.id.close_ok:
			Constant.IS_SHOW_DESKTOP_LYRIC = false;
			Constant.closeDesktopLyricFlag = true;
			SharedPreferences sp = context.getSharedPreferences("com.amusic.media_preferences",Context.MODE_WORLD_WRITEABLE);
			Editor editor = sp.edit();
			editor.putBoolean(Constant.SoftParametersSetting.desktop_lyric_key, false);
			editor.commit();
			Constant.IS_DESKTOP_LYRIC_EXIT = true;
			DesktopLyricService.getInstance().setVisible(View.GONE);
			if(settingView != null){
				wm.removeView(settingView);
				settingView = null;
			}
			wm.removeView(controllView);
			controllView = null;
			wm.removeView(closeDesktopLyricView);
			closeDesktopLyricView = null;
			break;
		case R.id.close_cancel:
			Constant.IS_DESKTOP_LYRIC_EXIT = true;
			DesktopLyricService.getInstance().setVisible(View.GONE);
			if(settingView != null){
				wm.removeView(settingView);
				settingView = null;
			}
			wm.removeView(controllView);
			controllView = null;
			wm.removeView(closeDesktopLyricView);
			closeDesktopLyricView = null;
			break;
		case R.id.desktop_lyric_font_large:
			setFontSize(getFontSize() + 2);
			saveDesktopLyricStyle();
			break;
		case R.id.desktop_lyric_font_small:
			setFontSize(getFontSize() - 2);
			saveDesktopLyricStyle();
			break;
		case R.id.desktop_lyric_logo:
			Intent intent = new Intent();
			ComponentName componentName = new ComponentName(context, AmtMedia.class);
			intent.setComponent(componentName);
			intent.setAction(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			context.startActivity(intent);
			break;
		case R.id.desktop_lyric_color1:
			setColor(0xFFAFE5E7,0xFF00AAFF);
			saveDesktopLyricStyle();
			break;
		case R.id.desktop_lyric_color2:
			setColor(0xFFFFFFFF,0xFFFE7800);
			saveDesktopLyricStyle();
			break;
		case R.id.desktop_lyric_color3:
			setColor(0xFFFFF700,0xFF25FE01);
			saveDesktopLyricStyle();
			break;
		case R.id.desktop_lyric_color4:
			setColor(0xFFFFF700,0xFF00AAFF);
			saveDesktopLyricStyle();
			break;
		case R.id.desktop_lyric_color5:
			setColor(0xFFFFF700,0xFFFF01BB);
			saveDesktopLyricStyle();
			break;
		}
		
	}
	public void saveDesktopLyricStyle(){
		if(Constant.IS_MEMORY_DESKTOP_LYRIC_FONT_COLOR){
			SharedPreferences sp = MediaApplication.getContext().getSharedPreferences("Data",Context.MODE_WORLD_WRITEABLE);
			int fontSize = getFontSize();
			int fontColor = getFontColor();
			int renderColor = getRenderColor();
			Editor editor = sp.edit();
			editor.putInt("fontSize", fontSize);
			editor.putInt("fontColor", fontColor);
			editor.putInt("renderColor", renderColor);
			editor.commit();
		}
	}
	public View getControllView() {
		return controllView;
	}
	
	public void setControllView(View controllView){
		this.controllView = controllView;
	}

	public View getSettingView() {
		return settingView;
	}

	public void setSettingView(View settingView){
		this.settingView = settingView;
	}
	
	public View getCloseDesktopLyricView() {
		return closeDesktopLyricView;
	}

	public void setCloseDesktopLyricView(View closeDesktopLyricView) {
		this.closeDesktopLyricView = closeDesktopLyricView;
	}

}
