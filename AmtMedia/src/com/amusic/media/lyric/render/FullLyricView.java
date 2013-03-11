package com.amusic.media.lyric.render;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Paint.Align;
import android.graphics.PorterDuff.Mode;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.VelocityTracker;

import com.amusic.media.R;
import com.amusic.media.dialog.AudioDialogEditLyric;
import com.amusic.media.dialog.DialogEditLyricRadio;
import com.amusic.media.lyric.parser.LyricInfo;
import com.amusic.media.lyric.parser.Sentence;
import com.amusic.media.lyric.player.LyricPlayer;
import com.amusic.media.provider.MediaDatabaseHelper;
import com.amusic.media.screens.impl.ScreenAudioSongLyricsFullScreen;
import com.amusic.media.screens.impl.ScreenKMediaPlayer;
import com.amusic.media.services.IMediaPlayerService;
import com.amusic.media.services.impl.ServiceManager;

public class FullLyricView extends SurfaceView implements SurfaceHolder.Callback{

	private int mTextSize = 18;
	private boolean isKmedia = false;
	private VelocityTracker mVelocityTracker;
	private AudioDialogEditLyric audioDialogEditLyric;
	
	private float y;
	private float oldY;
	private long time;
	private long oldTime;
	private boolean longFlag = true;
	
	public FullLyricView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		SurfaceHolder holder = this.getHolder();
		holder.addCallback(this);
		this.setWillNotDraw(false);
		this.setZOrderOnTop(true);
		holder.setFormat(PixelFormat.TRANSPARENT);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FullLyricView);  
		 
		mTextSize = (int) a.getDimension(R.styleable.FullLyricView_textSize1, 18);
		ScreenAudioSongLyricsFullScreen fullLyricActivity = (ScreenAudioSongLyricsFullScreen)context;
		
		if (fullLyricActivity.getParent() instanceof ScreenKMediaPlayer) {
			isKmedia = true;
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}
	
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		int width = this.getMeasuredWidth();
		int height = this.getMeasuredHeight();
		
		if (mLyricInfo == null || mLyricInfo.getList() == null || mLyricInfo.getList().size() == 0) {
			//Log.d("=GGG=","surfaceCreated renderDefault");
			renderDefault();
		}
		if (mRender == null) {
			mRender = new ScrollLyricRender(this,width,height,mTextSize);
			mRender.startRender();
			mIsFirsttime = false;
			
			mIsStarting = true;
		}
		mIsSurfaceCreated = true;
		if (!mIsPaused) {
			resume();
		}
	}

	private void renderDefault() {
		// TODO Auto-generated method stub
		SurfaceHolder holder = this.getHolder();
		Canvas canvas = holder.lockCanvas();
		try {
		    if (canvas == null) {
		    	return;
		    }
		    canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
		
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (canvas != null) {
			    holder.unlockCanvasAndPost(canvas);
			}
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		mIsSurfaceCreated = false;
		if (mRender != null) {
			if (!mRender.isPaused()) {
				mIsPaused = false;
			    mRender.pause();
			} else {
				mIsPaused = true;
			}
		}
	}
	
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			//System.out.println("handleMessage");
			/*Cursor mCursor = ServiceManager.getMediaplayerService().getCursor();
			String songName = mCursor.getString(mCursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SKINS_DISPALYNAME));
			String songPath = mCursor.getString(mCursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_FILEPATH));
			audioDialogEditLyric = new AudioDialogEditLyric();
			audioDialogEditLyric.setSongName(songName);
			audioDialogEditLyric.setSongPath(songPath);
			audioDialogEditLyric.show();*/
			new DialogEditLyricRadio().show();
		};
	};
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (isKmedia || mLyricInfo == null ) {
			return false;
		}
		
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);
		y = event.getY();
	    mY = (int) event.getY();
	    if (mRender == null) {
	    	return true;
	    }
	    switch (event.getAction()) {
	        case MotionEvent.ACTION_DOWN:
	        	longFlag = true;
//	        	mRender.pause();
	        	oldTime = System.currentTimeMillis();
	        	new Thread(){
	        		@Override
	        		public void run() {
	        			while(longFlag){
		        			time = System.currentTimeMillis();
		        			if((time - oldTime) >= 1000){
		        				handler.sendEmptyMessage(0);
		        				longFlag = false;
		        			}
	        			}
	        		}
	        	}.start();
	        	oldY = event.getY();
	            mTouchStartY =  (int) event.getY(); 
	            retoffset = -1;
	            break;
	        case MotionEvent.ACTION_MOVE:
	        	if (mIsInvalid) {
		        	final VelocityTracker velocityTracker = mVelocityTracker;
					velocityTracker.computeCurrentVelocity(1000);
					int velocityX = (int) velocityTracker.getXVelocity();
					int velocityY = (int) velocityTracker.getYVelocity();
					if (Math.abs(velocityX) < 20 && Math.abs(velocityY) < 20) {
						break;
					} else {	
						longFlag = false;
						mIsInvalid = false;
						mTouchStartY =  (int) event.getY(); 
						if (Math.abs(velocityX) > Math.abs(velocityY)) {
							mIsMoved = true;
						} else {
							mRender.pause();
							mVerticalMoved = true;
						}
					}
	        	}
	        	int offset = mY - mTouchStartY;	
	        	if (offset != 0 && mVerticalMoved) {
	        	    retoffset = mRender.scrollBy(offset);
	        	}
	            break;
	        case MotionEvent.ACTION_UP:
	        	longFlag = false;
	        	mIsMoved = false;
	        	mIsInvalid = true;
	        	mVerticalMoved = false;
	        
	            mTouchStartY = 0;
	        	IMediaPlayerService mediaPlayerService = ServiceManager.getMediaplayerService();
	    		MediaPlayer mplayer = mediaPlayerService.getMediaPlayer();
	    		if (retoffset >= 0 && mRender.isPaused()) {
	    		    mplayer.seekTo((int) retoffset);
	    		}
//		    	LyricPlayer lyricPlayer = mediaPlayerService.getLyricplayer();
//		    	lyricPlayer.seekLyricPlayer();
	    		if (mRender.getDragSentence() != null && mRender.isPaused()) {
	    			mRender.resetTop();
	    			mRender.setCurSentence(mRender.getDragSentence());
	    			mRender.clearDragSentence();
	    		}
	        	if (!mIsPaused) {
	        	    mRender.play();
	        	}
	            break;
	        case MotionEvent.ACTION_CANCEL:
	        	mIsMoved = false;
	        	mIsInvalid = true;
	        	mVerticalMoved = false;
	        	if (!mIsPaused) {
	        	    mRender.play();
	        	}
	        	break;
	    }
	    return true;
	}
	
	public LyricInfo getLyricInfo() {
		return mLyricInfo;
	}

	public void prepare(LyricInfo lyricInfo) {
		this.mLyricInfo = lyricInfo;
	}
	
	public void setLyricInfo(LyricInfo lyricInfo) {
		this.mLyricInfo = lyricInfo;
		if (mRender != null) {
			mRender.setKscInfo(lyricInfo);
		}
	}
	
	public Boolean getIsSurfaceCreated() {
		return mIsSurfaceCreated;
	}
	
	public void update(Sentence stc,boolean flag) {
		if (mRender != null) {
			if (!mVerticalMoved) {
				if (flag || mVerticalMoved) {
				    pause();
				}
			    mRender.setCurSentence(stc);
			}
		}
	}
	
	public void pause() {
		mIsPaused = true;
		if (mRender != null) {
		    mRender.pause();
		}
	}
	
	public void resume() {
		mIsPaused = false;
		if (mRender != null) {
		    mRender.play();
		}
	}
	
	public void stop() {
		if (mRender != null) {
			mRender.stop();
		}
		mLyricInfo = null;
		mIsStarting = false;
	}
	
	public void clearRender() {
		if (mRender == null) {
			return;
		}
		mRender.clearRender();
	}
	
	public void setRender(ScrollLyricRender render) {
		mRender = render;
		mIsFirsttime = true;
	}
	
	public void start() {
		if (!mIsFirsttime && mRender != null) {
			mRender.startRender();
		}
		mIsPaused = false;
		mIsFirsttime = false;
		mIsStarting = true;
	}
	
	public void setColor(int fontColor,int renderColor) {
		if (mRender != null) {
			mRender.setColor(fontColor, renderColor);
		}
	}
	
	public Boolean getIsStarting() {
		return mIsStarting;
	}
	
	public void prepareRender() {
		if (mRender != null) {
			mRender.prepareRender();
		}
	}
	
	public void setRatio(float ratio) {
		// TODO Auto-generated method stub
		if (mRender != null) {
			mRender.setRatio(ratio);
		}
	}
	
	private ScrollLyricRender     mRender;
	private LyricInfo             mLyricInfo;
	private Boolean               mIsSurfaceCreated = false;
	private int                   mTouchStartY;
	private int                   mY;
	private long                  retoffset;
	private boolean               mIsStarting;
	private boolean               mIsPaused = false;
	private boolean               mIsFirsttime = true;
	public static boolean         mIsMoved = false;
	private boolean               mVerticalMoved = false;
	private boolean               mIsInvalid = true;

}
