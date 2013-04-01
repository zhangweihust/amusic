package com.android.media.screens.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.amusic.media.R;
import com.android.media.MediaApplication;
import com.android.media.dialog.DialogLyricHelp;
import com.android.media.dialog.DialogLyricSave;
import com.android.media.dialog.OnScreenHint;
import com.android.media.download.UploadLyric;
import com.android.media.event.IMediaEventArgs;
import com.android.media.event.IMediaEventHandler;
import com.android.media.event.impl.MediaEventArgs;
import com.android.media.event.impl.MediaEventTypes;
import com.android.media.lyric.parser.TXTParser;
import com.android.media.lyric.player.LyricConfig;
import com.android.media.lyric.render.DesktopKTVView;
import com.android.media.lyric.render.LyricMakerView;
import com.android.media.screens.IScreen.ScreenType;
import com.android.media.services.IMediaEventService;
import com.android.media.services.impl.DesktopLyricService;
import com.android.media.services.impl.MediaPlayerService;
import com.android.media.services.impl.ServiceManager;
import com.android.media.toolbox.DETool;
import com.android.media.utils.Constant;
import com.android.media.view.CustomDialog;
import com.android.media.view.VerticalSeekBar2;
import com.android.media.view.VerticalSeekBar2.OnVertivalSeekBarChangeListener;

public class ScreenLyricSpeed extends Activity implements OnClickListener, IMediaEventHandler{

	private Button editLyricStart,editLyricState, editLyricHelp;
	private Dialog dialog;
	private TextView textView;
	private ImageButton goBackButton;
	private VerticalSeekBar2 seekBar;
	private LyricMakerView lyricmakerView = null;
	private DesktopLyricService desktopLyric;
	private MediaPlayer mp;
	private IMediaEventService mediaEventService = ServiceManager.getMediaEventService();;
	public static boolean isPaused = false;
	private UploadLyric uploadLyric = new UploadLyric();
	private WindowManager wm = (WindowManager) MediaApplication.getContext().getSystemService("window");
	private String lyricPath;
	private CustomDialog.Builder mCustomBuilder;
	WakeLock wakeLockSCreen = null; 
	private DialogLyricSave lyricDialog;
	private DialogLyricHelp helpDialog;
	private TelephonyManager mTelephonyManager;
	private PhoneStateListener mPhoneStateListener;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.screen_lyric_speed);	
		desktopLyric = DesktopLyricService.getInstance();
		goBackButton = (ImageButton) findViewById(R.id.screen_top_play_control_back);
		editLyricStart = (Button) findViewById(R.id.screen_edit_lyric_start);
		editLyricState = (Button) findViewById(R.id.screen_edit_lyric_state);
		editLyricHelp = (Button) findViewById(R.id.screen_edit_lyric_help);
		seekBar = (VerticalSeekBar2) findViewById(R.id.screen_seekBar);
		textView = (TextView) findViewById(R.id.screen_time);
		goBackButton.setOnClickListener(this);
		editLyricStart.setOnClickListener(this);
		editLyricState.setOnClickListener(this);
		editLyricHelp.setOnClickListener(this);
		
		seekBar.setOnVertivalSeekBarChangeListener(new OnVertivalSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(VerticalSeekBar2 VerticalSeekBar) {
				// TODO Auto-generated method stub
				//System.out.println("onStopTrackingTouch");
			}
			
			@Override
			public void onStartTrackingTouch(VerticalSeekBar2 VerticalSeekBar) {
				// TODO Auto-generated method stub
				//System.out.println("onStartTrackingTouch");
			}
			
			@Override
			public void onProgressChanged(VerticalSeekBar2 VerticalSeekBar,
					int progress, boolean fromUser) {
				// TODO Auto-generated method stub
				//System.out.println("onProgressChanged");
			}
		});
		seekBar.setMax(1000);
		
		lyricmakerView = (LyricMakerView) findViewById(R.id.screen_lyrics_maker);
		
		String lyrics = getIntent().getStringExtra("lyrics");
		String songPath =  getIntent().getStringExtra("songPath");
		String songName = getIntent().getStringExtra("songName");
		String singer =  getIntent().getStringExtra("singer");
		lyricPath = getIntent().getStringExtra("lyricPath");
		List<String> txtList = textParser(lyrics);
		lyricmakerView.setLyricList(txtList);
		IMediaEventArgs eventArgs = new MediaEventArgs();
		ServiceManager.getMediaEventService().onMediaUpdateEvent(eventArgs.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_PAUSE));
		
		mp = new MediaPlayer();
		mp.reset();
		try {
			mp.setDataSource(songPath);
			mp.prepare();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			OnScreenHint mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_audio_cannot_play));
			mOnScreenHint.cancel();
			mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_audio_cannot_play));
			mOnScreenHint.show();
			this.finish();
			return;
		}
		lyricmakerView.setMediaPlayer(mp);
		lyricmakerView.setSongName(songName);
		lyricmakerView.setSinger(singer);
		lyricmakerView.setLyricPath(lyricPath);
		mp.start();
		
		mp.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				if (lyricDialog != null && !lyricDialog.isShowing()) {
				    showDialog();
				}
			}
		});
		lyricmakerView.setHandler(handler);
		
		Resources res = getResources();
	    int seekBarlayoutWidth = (int) res.getDimension(R.dimen.lyric_speed_layout_width);
	    lyricmakerView.setSeekBarLayoutWidth(seekBarlayoutWidth);
	    
	    PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
	    wakeLockSCreen = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "SCreenOn");
		wakeLockSCreen.setReferenceCounted(false);
		mediaEventService.addEventHandler(this);
		
		mTelephonyManager = (TelephonyManager) MediaApplication.getContext().getSystemService(Context.TELEPHONY_SERVICE);
		mPhoneStateListener = new PhoneStateListener() {
			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				MediaApplication.logD(MediaPlayerService.class, "onCallStateChanged");
				if (state == TelephonyManager.CALL_STATE_RINGING || state == TelephonyManager.CALL_STATE_OFFHOOK) {
					if (mp.isPlaying()) {
						pauseMedia();
					}
				}
			}
		};
	}
	
	
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			int msgWhat = msg.what;
			setProgressAndShowTime(msgWhat);
		};
	};
	
	private void setProgressAndShowTime(int progress) {
		float time = (float) progress / 1000;
		String timeText = Float.toString(time);
		textView.setText(timeText);
		if (progress > 1000) {
			progress = 1000;
		}
		seekBar.setProgress(progress);
		
	}
	
	private List<String> textParser(String lyrics) {
		TXTParser txtParser = new TXTParser(lyrics,LyricConfig.paserWithString);
	    List<String> txtList = null;
		try {
			txtList = txtParser.parser();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return txtList;
	}
	

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		Constant.IS_DESKTOP_LYRIC_EXIT = false;
		if (desktopLyric.getDesktopView() != null) {
			if(Constant.IS_SHOW_DESKTOP_LYRIC){
				desktopLyric.setVisible(View.VISIBLE);
			}
			desktopLyric.setScreenLyricSpeed(false);
			if(MediaPlayerService.typefinal == ScreenType.TYPE_RECORD || MediaPlayerService.typefinal == ScreenType.TYPE_KMEDIA){
				desktopLyric.setVisible(View.GONE);
			}
			if (!ServiceManager.getMediaplayerService().getMediaPlayer()
					.isPlaying()) {
				desktopLyric.setVisible(View.GONE);
			}
			if(desktopLyric.isAmtMedia()){
				desktopLyric.setVisible(View.GONE);
			}
		}
		
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		isPaused = false;
		mp.stop();
		
		if (wakeLockSCreen != null && wakeLockSCreen.isHeld()) {
	    	wakeLockSCreen.release();
	    	wakeLockSCreen = null;
        }
		mediaEventService.removeEventHandler(this);
		super.onDestroy();
	}
	
	
	

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		pauseMedia();
		super.onPause();
	}
	
	private void pauseMedia() {
		editLyricState.setBackgroundResource(R.drawable.screen_edit_lyric_pause_selector);
		isPaused = true;
		mp.pause();
		lyricmakerView.pause();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
		} else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
		}
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		desktopLyric.setVisible(View.GONE);
		desktopLyric.setScreenLyricSpeed(true);
		View controllView = ((DesktopKTVView)(desktopLyric.getDesktopView())).getControllView();
		View settingView = ((DesktopKTVView)(desktopLyric.getDesktopView())).getSettingView();
		View closeDesktopLyricView = ((DesktopKTVView)(desktopLyric.getDesktopView())).getCloseDesktopLyricView();
		if(controllView != null){
			wm.removeView(controllView);
			((DesktopKTVView)(desktopLyric.getDesktopView())).setControllView(null);
		}
		if(settingView != null){
			wm.removeView(settingView);
			((DesktopKTVView)(desktopLyric.getDesktopView())).setSettingView(null);
		}
		if(closeDesktopLyricView != null){
			wm.removeView(closeDesktopLyricView);
			((DesktopKTVView)(desktopLyric.getDesktopView())).setCloseDesktopLyricView(null);
		}
		super.onResume();
		
		wakeLockSCreen.acquire();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()) {
		case R.id.screen_top_play_control_back:
			showDialog();
			break;
		case R.id.screen_edit_lyric_start:
			try {
				mp.stop();
				mp.prepare();
				mp.seekTo(0);
				mp.start();
				isPaused = false;
				lyricmakerView.start();
				editLyricState.setBackgroundResource(R.drawable.screen_edit_lyric_continu_selector);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				OnScreenHint mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_audio_cannot_play));
				mOnScreenHint.cancel();
				mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_audio_cannot_play));
				mOnScreenHint.show();
				this.finish();
				return;
			}
			setProgressAndShowTime(0);
			lyricmakerView.removeRunnable();
			lyricmakerView.startRender();
			break;
		case R.id.screen_edit_lyric_state:
			if (isPaused) {
				editLyricState.setBackgroundResource(R.drawable.screen_edit_lyric_continu_selector);
				isPaused = false;
				mp.start();
				lyricmakerView.start();
			} else {
				editLyricState.setBackgroundResource(R.drawable.screen_edit_lyric_pause_selector);
				isPaused = true;
				mp.pause();
				lyricmakerView.pause();
			}
			break;
		case R.id.screen_edit_lyric_help:
			if (helpDialog == null) {
				helpDialog = new DialogLyricHelp(this);
			}
			
			helpDialog.show();
			break;
		}
	}
	
	
	private void showUpLoadDialog(boolean isfailed) {
		String msgStr = "";
		if (isfailed) {
			msgStr = getResources().getString(R.string.screen_lyric_speed_upload_error);
		} else {
		    msgStr = getResources().getString(R.string.screen_lyric_speed_upload);
		}
		
		final CustomDialog.Builder customBuilder = new CustomDialog.Builder(this);
		customBuilder.setTitle(getResources().getString(R.string.editor_lyric_prompt))
		.setWhichViewVisible(CustomDialog.contentIsTextView).setMessage(msgStr)
		.setPositiveButton(getResources().getString(R.string.screen_lyric_speed_modify_yes), 
            		new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	dialog.dismiss();
            	upLoadLyric();
			}
        })
        .setNegativeButton(getResources().getString(R.string.screen_lyric_speed_modify_no), 
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                	dialog.dismiss();
                	ScreenLyricSpeed.this.finish();
    			}
            });
		mCustomBuilder = customBuilder;
		dialog = customBuilder.create();
		dialog.show();
	}
	

	private void showDialog() {
		/*final CustomDialog.Builder customBuilder = new CustomDialog.Builder(this);
		String msgStr = "";
		if (lyricmakerView != null && lyricmakerView.isLyricMakeOver()) {
			msgStr = getResources().getString(R.string.screen_lyric_speed_modify);
		} else {
			msgStr = getResources().getString(R.string.screen_lyric_speed_quit);
		}
		customBuilder.setTitle(getResources().getString(R.string.editor_lyric_prompt))
		.setWhichViewVisible(CustomDialog.contentIsTextView).setMessage(msgStr)
		.setPositiveButton(getResources().getString(R.string.screen_lyric_speed_modify_yes), 
            		new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
	            	dialog.dismiss();
	            	if (lyricmakerView != null ) {
	            		lyricmakerView.removeRunnable();
		            	if (lyricmakerView.isLyricMakeOver()) {
	                		 lyricmakerView.saveKscFile();
	                		 showUpLoadDialog(false);
	                	} else {
	                		ScreenLyricSpeed.this.finish(); 
	                	}
	            	}
				}
            })
            .setNegativeButton(getResources().getString(R.string.screen_lyric_speed_modify_no), 
            		new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                	dialog.dismiss();
                	if (lyricmakerView != null && lyricmakerView.isLyricMakeOver()) {
                		lyricmakerView.removeRunnable();
                		ScreenLyricSpeed.this.finish();
                	}
    			}
            });
		dialog = customBuilder.create();
		dialog.show();*/
		if (lyricDialog == null) {
		    lyricDialog = new DialogLyricSave(this);
		    lyricDialog.getBtnOK().setOnClickListener(btn_ok_listener);
			lyricDialog.getBtnCancle().setOnClickListener(btn_cancel_listener);
		} else if (lyricDialog.isShowing()) {
			lyricDialog.dismiss();
		}
		String msgStr = "";
		if (lyricmakerView != null && lyricmakerView.isLyricMakeOver()) {
			msgStr = getResources().getString(R.string.screen_lyric_speed_modify);
			lyricDialog.getLyricLine().setVisibility(View.VISIBLE);
			lyricDialog.getEditText().setVisibility(View.VISIBLE);
			lyricDialog.getEditText().setText("");
			lyricDialog.getLyricMakerName().setVisibility(View.VISIBLE);
		} else {
			lyricDialog.getLyricLine().setVisibility(View.GONE);
			lyricDialog.getEditText().setVisibility(View.GONE);
			lyricDialog.getLyricMakerName().setVisibility(View.GONE);
			msgStr = getResources().getString(R.string.screen_lyric_speed_quit);
		}
		lyricDialog.getLyricSave().setText(msgStr);
		lyricDialog.show();
	}
	
	private View.OnClickListener btn_ok_listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			String lyricmakerName = lyricDialog.getEditText().getText().toString();
			lyricmakerName = lyricmakerName.trim();
			lyricDialog.dismiss();
			if (lyricmakerView != null ) {
        		lyricmakerView.removeRunnable();
            	if (lyricmakerView.isLyricMakeOver()) {
            		if (!"".equals(lyricmakerName)) {
            		    lyricmakerView.setLyricMakerName(lyricmakerName);
            		}
            		lyricmakerView.saveKscFile();
            		showUpLoadDialog(false);
            	} else {
            		ScreenLyricSpeed.this.finish(); 
            	}
        	}
		}
	};

	private View.OnClickListener btn_cancel_listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			lyricDialog.dismiss();
			if (lyricmakerView != null && lyricmakerView.isLyricMakeOver()) {
        		lyricmakerView.removeRunnable();
        		ScreenLyricSpeed.this.finish();
        	}
		}
	};
	
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		showDialog();
	}
	
	

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	private void upLoadLyric() {
		new Thread() {
			@Override
			public void run() {
				// 将要上传的内容写入文件
				String tmpLyricFile = lyricPath.substring(0, lyricPath.indexOf(".ksc")) + ".uptmp";
				File kscFile = new File(tmpLyricFile);
				try {
					if (!kscFile.exists()) {
						kscFile.createNewFile();
					}
					FileWriter kscFileWriter = new FileWriter(kscFile);
					kscFileWriter.write(lyricmakerView.getLyricOverStr());
					kscFileWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				// 加密，并代替原来的歌词文件
				if (DETool.nativeCreateKsc(tmpLyricFile) != -1) {
					if (kscFile.exists()) {
						kscFile.delete();
					}
					String tmpPath = tmpLyricFile + ".ksc.tp";
					File tmpFile = new File(tmpPath);
				    boolean issuccessed = tmpFile.renameTo(new File(tmpLyricFile));
				}
				uploadLyric.uploadlyrics2(tmpLyricFile);
			};
		}.start();
	}
	
	private void upLoadLyricFile() {
		new Thread() {
			@Override
			public void run() {
				
				// 将要上传的内容写入文件
				String tmpLyricFile = lyricPath.substring(0, lyricPath.indexOf(".ksc")) + ".uptmp";
				File kscFile = new File(tmpLyricFile);
				try {
					if (!kscFile.exists()) {
						kscFile.createNewFile();
					}
					FileWriter kscFileWriter = new FileWriter(kscFile);
					kscFileWriter.write(lyricmakerView.getLyricOverStr());
					kscFileWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				// 加密，并代替原来的歌词文件
				if (DETool.nativeCreateKsc(tmpLyricFile) != -1) {
					if (kscFile.exists()) {
						kscFile.delete();
					}
					String tmpPath = tmpLyricFile + ".ksc.tp";
					File tmpFile = new File(tmpPath);
				    boolean issuccessed = tmpFile.renameTo(new File(tmpLyricFile));
				}
				uploadLyric.uploadLyricFile(tmpLyricFile);
				
				kscFile = new File(tmpLyricFile);
				if (kscFile.exists()) {
					kscFile.delete();
				}
				
			};
		}.start();
	}
	
	private byte[] EncryptStr(String str) {
		return DETool.nativeEncryptLongStr(str);
	}
	
	@Override
	public boolean onEvent(IMediaEventArgs args) {
		// TODO Auto-generated method stub
		switch (args.getMediaUpdateEventTypes()) {
		case AUDIO_UPLOAD_LRC_LYRIC_FINISH:
			ServiceManager.getAmtMediaHandler().post(new Runnable(){
				@Override
				public void run() {
					dialog.dismiss();
					ScreenLyricSpeed.this.finish();
					OnScreenHint mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_lyric_speed_upload_successed));
					mOnScreenHint.cancel();
					mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_lyric_speed_upload_successed));
					mOnScreenHint.show();
				}
	        });
			break;
		case AUDIO_UPLOAD_LRC_LYRICS_ERROR:
			ServiceManager.getAmtMediaHandler().post(new Runnable(){
				@Override
				public void run() {
					showUpLoadDialog(true);
				}
	        });
		case AUDIO_LYRIC_LINE_OVER:
			lyricmakerView.removeRunnable();
			setProgressAndShowTime(0);
			break;
		case AUDIO_LYRIC_MAKE_OVER:
			Log.d("=MMM=","AUDIO_LYRIC_MAKE_OVER");
			ServiceManager.getAmtMediaHandler().post(new Runnable(){
				@Override
				public void run() {
					Toast.makeText(ScreenLyricSpeed.this, ServiceManager.getAmtMedia().getString(R.string.screen_lyric_speed_make_over),
							Toast.LENGTH_SHORT).show();
				}
			});
		}
		return true;
	}
	
}
