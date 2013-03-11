package com.amusic.media.lyric.render;

import java.util.List;

import com.amusic.media.MediaApplication;
import com.amusic.media.screens.impl.ScreenLyricSpeed;

import android.content.Context;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

public class LyricMakerView extends SurfaceView implements SurfaceHolder.Callback {
	
	private boolean mIsSurfaceCreated = false;
	private LyricMakerRender mRender = null;
	private boolean mIsPaused = false;
	private List<String> mLyricList = null;
	private MediaPlayer mplayer = null;
	private int mXpos = 0;
	private int mYpos = 0;
	private String songName;
	private String singer;
	private String lyricPath = null;
	private Handler handler = null;
	private int seekBarLayoutWidth = 0;
	

	public LyricMakerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		SurfaceHolder holder = this.getHolder();
		holder.addCallback(this);
		
		this.setWillNotDraw(false);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		int width = this.getWidth();
		int height = this.getHeight();
		if (mRender == null) {
			mRender = new LyricMakerRender(this,width,height);
			mRender.startRender();
		} else {
			mRender.doRender(WordInfo.IDLE_STATE);
		}
		mIsSurfaceCreated = true;
		if (!mIsPaused) {
			resume();
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
        if (ScreenLyricSpeed.isPaused) {
        	return true;
        }
        mXpos =  (int) event.getX() - seekBarLayoutWidth;
        if (mXpos < 0) {
        	return true;
        }
	    switch (event.getAction()) {
	        case MotionEvent.ACTION_DOWN:
	        	mYpos =  (int) event.getY();
	            int curPosition = 0;
	            if (mplayer != null) {
	            	curPosition = mplayer.getCurrentPosition();
	            }
	            if (mRender != null) {
	            	mRender.onDownTouchEvent(mXpos,mYpos,curPosition,WordInfo.DOWN_STATE);
	            }
	            break;
	        case MotionEvent.ACTION_MOVE:
	            mYpos =  (int) event.getY();
	            int curPosition1 = 0;
	            if (mplayer != null) {
	            	curPosition1 = mplayer.getCurrentPosition();
	            }
	            if (mRender != null) {
	            	mRender.onDownTouchEvent(mXpos,mYpos,curPosition1,WordInfo.MOVE_STATE);
	            }
	            break;
	        case MotionEvent.ACTION_UP:
	        	int curPositionUp = 0;
	        	if (mplayer != null) {
	            	curPositionUp = mplayer.getCurrentPosition();
	            }
	        	if (mRender != null) {
	            	mRender.onUpTouchEvent(curPositionUp);
	            }
	            break;
	        case MotionEvent.ACTION_CANCEL:
	        	break;
	    }
	    return true;
	}

	private void resume() {
		// TODO Auto-generated method stub
		
	}
	
	public void setLyricList(List<String> list) {
		mLyricList = list;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mIsSurfaceCreated = false;
	}

	public boolean getIsSurfaceCreated() {
		return mIsSurfaceCreated;
	}

	public List<String> getLyricList() {
		return mLyricList;
	}
	
	public void setMediaPlayer(MediaPlayer mp) {
		this.mplayer = mp;
	}
	
	public void setSongName(String songName) {
		this.songName = songName;
	}
	
	public String getSongName() {
		return this.songName;
	}
	
	public void setSinger(String singer) {
		this.singer = singer;
	}
	
	public String getSinger() {
		return this.singer;
	}
	
	public int getDuration() {
		return mplayer.getDuration();
	}
	
	public String getLyricPath() {
		return this.lyricPath;
	}

	public void setLyricPath(String lyricPath) {
		this.lyricPath  = lyricPath;
	}
	
	public boolean isLyricMakeOver() {
		if (mRender != null) {
			return mRender.isLyricMakeOver();
		}
		return false;
	}
	
	public void saveKscFile() {
		if (mRender != null) {
			mRender.writeKscFile();
		}
	}
	
	public void setHandler(Handler handler) {
		this.handler = handler;
	}
	
	public Handler getHandler() {
		return this.handler;
	}
	
	public void removeRunnable() {
		if (mRender != null) {
			mRender.removeRunnable();
		}
	}
	
	public void pause() {
		if (mRender != null) {
			mRender.removeRunnable();
		}
	}
	
	public void start() {
		if (mRender != null) {
			mRender.startRunnable();
		}
	}
	
	public void setSeekBarLayoutWidth(int width) {
		seekBarLayoutWidth = width;
	}
	
	public String getLyricOverStr() {
		if (mRender != null) {
			return mRender.getLyricOverStr();
		}
		return null;
	}
	
	public void refreshView() {
		if (mRender != null) {
			mRender.doRender(WordInfo.IDLE_STATE);
		}
	}
	
	public void startRender() {
		if (mRender != null) {
			mRender.startRender();
		}
	}
	
	public void setLyricMakerName(String name) {
		if (mRender != null) {
			mRender.setLyricMakerName(name);
		}
	}
}
