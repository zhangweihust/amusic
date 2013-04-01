package com.android.media.lyric.render;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.amusic.media.R;
import com.android.media.MediaApplication;
import com.android.media.lyric.parser.LyricInfo;
import com.android.media.lyric.parser.Sentence;
import com.android.media.services.IMediaPlayerService;
import com.android.media.services.impl.ServiceManager;
import com.android.media.utils.Constant;

public class PhoneKTVView extends SurfaceView implements SurfaceHolder.Callback {

	public static final int LYRIC_INFO_UNCHANGED = 0;
	public static final int LYRIC_INFO_CHANGED = 1;
	private int mTextSize = 18;

	public PhoneKTVView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		SurfaceHolder holder = this.getHolder();
		mIsSurfaceCreated = false;
		holder.addCallback(this);
		this.setWillNotDraw(false);
		this.setZOrderOnTop(true);
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PhoneKTVView);  
 
		mTextSize = (int) a.getDimension(R.styleable.PhoneKTVView_textSize, 18);
		holder.setFormat(PixelFormat.TRANSPARENT);
	}

	public PhoneKTVView(Context context) {
		super(context);
		SurfaceHolder holder = this.getHolder();
		mIsSurfaceCreated = false;
		holder.addCallback(this);
		this.setWillNotDraw(false);
		this.setZOrderOnTop(true);
		holder.setFormat(PixelFormat.TRANSPARENT);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
	}

	public void setRender(KLOKRender render) {
		mRender = render;
		mIsFirsttime = true;
	}

	private KLOKRender mRender = null;

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
		    
		    mWidth = ((WindowManager)MediaApplication.getInstance().getSystemService("window")).getDefaultDisplay().getWidth();//Constant.DESKTOP_LYRIC_WIDTH;
		    mHeight = this.getMeasuredHeight();
		    if (mRender != null) {
		    	mTextSize = mRender.getFontSize();
		    	mRender.stop();
		    	mRender = null;
		    }
	    	mRender = new KLOKRender(this, mWidth, mHeight,mTextSize);
			mRender.startRender();
			mRender.setColor(mFontColor, mRenderColor);
			setDestTopLyricStyle();
			mIsFirsttime = false;
			
			mIsStarting = true;
		} else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			mWidth = ((WindowManager)MediaApplication.getInstance().getSystemService("window")).getDefaultDisplay().getWidth();//mWidth = Constant.DESKTOP_LYRIC_WIDTH;
			mHeight = this.getMeasuredHeight();
		    if (mRender != null) {
		    	mTextSize = mRender.getFontSize();
		    	mRender.stop();
		    	mRender = null;
		    }
	    	mRender = new KLOKRender(this, mWidth, mHeight,mTextSize);
			mRender.startRender();
			mRender.setColor(mFontColor, mRenderColor);
			setDestTopLyricStyle();
			mIsFirsttime = false;
			
			mIsStarting = true;
		}
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		mWidth = this.getMeasuredWidth();
		mHeight = this.getMeasuredHeight();
		
		if (getKscInfo() == null || getKscInfo().getList() == null || getKscInfo().getList().size() == 0) {
			renderDefault();
		}

		if (mRender == null) {
			mRender = new KLOKRender(this, mWidth, mHeight,mTextSize);
			mRender.startRender();
			setDestTopLyricStyle();
			mIsFirsttime = false;
			
			mIsStarting = true;
		}
		mRender.setIsNeedReDraw(true);
		mIsSurfaceCreated = true;
		if (!mIsPaused) {
			resume();
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
		// mRender = null;
	}
	
	public void setFontSize(int fontSize) {
		if (mRender != null) {
			if (mKscinfo != null) {
			    mRender.setFontSize(fontSize);
			}
		}
	}
	
	public int getFontSize() {
		if (mRender != null) {
			mTextSize = mRender.getFontSize();
		}
		return mTextSize;
	}

	public void prepare(LyricInfo kscinfo) {
		mKscinfo = kscinfo;
	}
	
	public void setLyricInfo(LyricInfo kscinfo) {
		mKscinfo = kscinfo;
		if (mRender != null) {
			mRender.setKscInfo(kscinfo);
		}
	}

	public void prepare(LyricInfo kscinfo, Boolean isSameSong) {
		mKscinfo = kscinfo;
		mIsSameSong = isSameSong;
	}

	public Boolean getIsSameSong() {
		return mIsSameSong;
	}
	
	public void setIsSameSong(Boolean flag) {
		mIsSameSong = flag;
	}

	public LyricInfo getKscInfo() {
		return mKscinfo;
	}

	public void update(Sentence sentence, int cmd,boolean flag) {		
		if (mRender == null) {
			return;
		}

		if (mIsSurfaceCreated) {
			if (flag) {
			    pause();
			}
			mRender.setCurSentence(sentence, cmd);
		}
	}

	public void start() {
		if (!mIsFirsttime && mRender != null) {
			mRender.startRender();
		}
		mIsFirsttime = false;
		mIsStarting = true;
		
//		IMediaPlayerService mediaPlayerService = ServiceManager.getMediaplayerService();
//	    MediaPlayer mplayer = mediaPlayerService.getMediaPlayer();
//	    if (!mplayer.isPlaying()) {
//	    	Log.d("=KKK=","PhoneKTVView start in");
//	    	pause();
//	    }
	}
	
	public void renderDefault() {
		SurfaceHolder holder = this.getHolder();
		Canvas canvas = holder.lockCanvas();
		try {
		    if (canvas == null) {
		    	return;
		    }
		    canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
			Paint pt = new Paint();
			pt.setTextAlign(Align.CENTER);
			pt.setTextSize(mTextSize);
			pt.setColor(Constant.LYRICFOREGROUNDCOLOR);
			pt.setAntiAlias(true);
			String str = ServiceManager.getAmtMedia().getString(R.string.screen_default_lyric_view);
			canvas.drawText(str, mWidth / 2, mHeight / 2, pt);
		
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (canvas != null) {
			    holder.unlockCanvasAndPost(canvas);
			}
		}
	}

	public void pause() {
		if (mRender == null) {
			return;
		}
		mRender.pause();
	}

	public void stop() {
		mIsSameSong = false;
		mIsStarting = false;
		mKscinfo = null;
		if (mRender == null) {
			return;
		}
		mRender.stop();
		// mRender = null;
	}
	
	public void stop2() {
		mIsSameSong = false;
		mIsStarting = false;
		if (mRender == null) {
			return;
		}
		mRender.stop();
		// mRender = null;
	}

	public void resume() {
		if (mRender == null) {
			return;
		}
		mRender.resume();
	}

	public Boolean getIsStarting() {
		return mIsStarting;
	}
	
	public void setIsStarting(Boolean flag) {
		mIsStarting = flag;
	}

	public Boolean getIsSurfaceCreated() {
		return mIsSurfaceCreated;
	}

	public void clearRender() {
		if (mRender == null) {
			return;
		}
		mRender.clearRender();
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
	
	public void setColor(int fontColor,int renderColor) {
		if (mRender != null) {
		    mRender.setColor(fontColor, renderColor);
		}
		
		mFontColor = fontColor;
	    mRenderColor = renderColor;
	    IMediaPlayerService mediaPlayerService = ServiceManager.getMediaplayerService();
	    MediaPlayer mplayer = mediaPlayerService.getMediaPlayer();
		if (mKscinfo != null && mKscinfo.getList().size() != 0 && mplayer.getCurrentPosition() < mKscinfo.getList().get(0).getStartTime()) {
		    prepareRender();
		} 
		
		if (getKscInfo() == null || getKscInfo().getList() == null || getKscInfo().getList().size() == 0) {
			renderDefault();
		}
	}
	
	public void setDestTopLyricStyle() {
		if(Constant.IS_MEMORY_DESKTOP_LYRIC_FONT_COLOR && this instanceof DesktopKTVView){
			SharedPreferences sp = MediaApplication.getContext().getSharedPreferences("Data",Context.MODE_WORLD_WRITEABLE);
			mTextSize = sp.getInt("fontSize", mTextSize);
			mFontColor = sp.getInt("fontColor", mFontColor);
			mRenderColor = sp.getInt("renderColor", mRenderColor);
			if (mRender != null) {
				mRender.setColor(mFontColor, mRenderColor);
				mRender.setFontSize(mTextSize);
			}
		}
	}
	

	private LyricInfo mKscinfo = null;
	private Boolean mIsStarting = false;
	private Boolean mIsFirsttime = true;
	private Boolean mIsSurfaceCreated = false;
	private Boolean mIsSameSong = false;
	private Boolean mIsPaused = false;
	private int mWidth = 0;
	private int mHeight = 0;
	private int mFontColor = 0xFFADE5E6;
	public int getFontColor() {
		return mFontColor;
	}

	public void setFontColor(int mFontColor) {
		this.mFontColor = mFontColor;
	}

	public int getRenderColor() {
		return mRenderColor;
	}

	public void setRenderColor(int mRenderColor) {
		this.mRenderColor = mRenderColor;
	}

	private int mRenderColor = 0xFF00B4FF;

}
