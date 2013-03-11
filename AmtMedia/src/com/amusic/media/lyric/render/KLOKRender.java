package com.amusic.media.lyric.render;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Paint.Align;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.view.SurfaceHolder;

import com.amusic.media.R;
import com.amusic.media.lyric.parser.LyricInfo;
import com.amusic.media.lyric.parser.Sentence;
import com.amusic.media.services.IMediaPlayerService;
import com.amusic.media.services.impl.ServiceManager;
import com.amusic.media.utils.Constant;

/**
 * 歌词卡拉OK模式的渲染类。
 * 
 * @author jiaming.wang@amusic.com
 * @version 1.0 2011.12.09
 */
public class KLOKRender implements Runnable {

	private final static int DEFAULTFRAMERATE = 20;// 默认每秒钟渲染的次数
	private final static int LINESPACE = 10;// 行间距
	// private final static int TOPLINE = 5;// 上边界
	private final static int BOTTOMLINE = 5;// 下边界
	private final static int LEFTLINE = 20;// 左边界
	private final static int RIGHTLINE = 20;// 右边界
	private final static int UPLINE = 0;// 表示上面一行歌词
	private final static int DOWNLINE = 1;// 表示下面一行歌词
	public final static int UPSTATE = 0;// 表示在渲染上面一行的状态
	public final static int DOWNSTATE = 1;// 表示在渲染下面一行的状态
	public final static int OVERSTATE = 2;// 表示这一屏歌词已经渲染完成的状态
	
	private int mTextSize = 25;

	/**
	 * 构造函数,初始化mLyric成员。
	 */
	public KLOKRender(PhoneKTVView ktvView, int width, int height,int textSize) {
		mKTVView = ktvView;
		mViewWidth = width;
		mViewHeight = height;
		mIsNewLyric = false;
		mIsNewWord = false;
		mState = OVERSTATE;
		mLyric = new Lyric2Bmp();
		mLyric.setFontSize(textSize);
		mTextSize = textSize;
		mRectSrc = new Rect();
		mRectDst = new Rect();
		mRenderFramerate = DEFAULTFRAMERATE;
		mIsNeedReDraw = false;
		mKscInfo = null;
		mIsRunning = false;
		mSentenceList = null;

		mSurfaceHolder = mKTVView.getHolder();
		mMap = new LinkedHashMap<Double, Integer>();
	}

	/**
	 * 在画布上不停地渲染歌词。
	 */
	public void doRender() {
		Canvas canvas = null;
		try {
			if (mIsNeedReDraw) {
				canvas = mSurfaceHolder.lockCanvas();
			} else {
				canvas = mSurfaceHolder.lockCanvas(new Rect(mRectDst));
			}

			if (canvas == null || mCurSentence == null) {
				return;
			}
			canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
			canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
			
			if (mIsNeedReDraw) {
				if (mFontBitmapUp != null) {
				    canvas.drawBitmap(mFontBitmapUp, mUpLeft, mUpTop, null);
				}
				if (mFontBitmapDown != null) {
				    canvas.drawBitmap(mFontBitmapDown, mDownLeft, mDownTop, null);
				}
//				mIsNeedReDraw = false;
			}

			// 不断的渲染变色
			if (mState == UPSTATE && mRenderBitmapUp != null) {
				canvas.drawBitmap(mRenderBitmapUp, mRectSrc, mRectDst, null);
			} else if (mState == DOWNSTATE && mRenderBitmapDown != null) {
				canvas.drawBitmap(mRenderBitmapDown, mRectSrc, mRectDst, null);
			} else {
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (canvas != null)
				mSurfaceHolder.unlockCanvasAndPost(canvas);
		}
		
		
		if (mIsNeedReDraw) {
			try {
				canvas = mSurfaceHolder.lockCanvas();
				
				if (canvas == null) {
					return;
				}
				canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
	
//				canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
				
				canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
				if (mFontBitmapUp != null) {
				    canvas.drawBitmap(mFontBitmapUp, mUpLeft, mUpTop, null);
				}
				if (mFontBitmapDown != null) {
				    canvas.drawBitmap(mFontBitmapDown, mDownLeft, mDownTop, null);
				}
				mIsNeedReDraw = false;
	
				// 不断的渲染变色
				// 不断的渲染变色
				if (mState == UPSTATE && mRenderBitmapUp != null) {
					canvas.drawBitmap(mRenderBitmapUp, mRectSrc, mRectDst, null);
				} else if (mState == DOWNSTATE && mRenderBitmapDown != null) {
					canvas.drawBitmap(mRenderBitmapDown, mRectSrc, mRectDst, null);
				} else {
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (canvas != null)
					mSurfaceHolder.unlockCanvasAndPost(canvas);
			}
		}
	}

	/**
	 * 给mRectSrc和mRectDst赋值
	 * 
	 * @param bmp
	 *            需要渲染的bitmap
	 * @param left
	 *            在视图中的left边界
	 * @param topzhang
	 *            在视图中的top边界
	 */
	private void getRenderRect(Bitmap bmp, int left, int top) {
		mRectSrc.left = 0;
		mRectSrc.right = (int) (mMoveX + mStepWidth);
		mRectSrc.top = 0;
		mRectSrc.bottom = bmp.getHeight();

		if (mRectSrc.right > bmp.getWidth()) {
			mRectSrc.right = bmp.getWidth();
		}

		mRectDst.left = mRectSrc.left + left;
		mRectDst.right = mRectSrc.right + left;
		mRectDst.top = mRectSrc.top + top;
		mRectDst.bottom = mRectSrc.bottom + top;
	}

	/**
	 * 获得静态图片的左顶点位置。
	 * 
	 * @param canvas
	 *            画布
	 */
	private void getBackgroundImagePos() {
		int upwidth = 0;
		int downwidth = 0;

		if (mFontBitmapUp != null) {
			upwidth = mFontBitmapUp.getWidth();
		}
		if (mFontBitmapDown != null) {
			downwidth = mFontBitmapDown.getWidth();
		}

		mUpTop = (mViewHeight / 2 - mTextSize) / 2;
		mDownTop = mViewHeight / 2 + mUpTop;

		/*
		 * 上面的一行，如果文字宽度和左边界（5个像素）之和小于视图宽度 
		 * 或者文字宽度大于视图宽度则文字左对齐到左边界 
		 * 如果文字宽度小于视图宽度但是文字宽度和左边界（5个像素）之和大于视图宽度 
		 * 则将文字右对齐屏幕右边。
		 */
		if (upwidth < mViewWidth - LEFTLINE || upwidth > mViewWidth) {
			mUpLeft = LEFTLINE;
		} else {
			mUpLeft = mViewWidth - upwidth;
		}

		/*
		 * 下面一行，如果文字宽度与右边界（5个像素）之和小于视图宽度则右对齐到右边界 如果文字宽度小于视图宽度，但是文字宽度与右边界（5个像素）之和大于视图宽度则右对齐到屏幕右边 如果文字宽度大于视图宽度则左对齐到左边界
		 */
		if (downwidth < mViewWidth - RIGHTLINE) {
			mDownLeft = mViewWidth - downwidth - RIGHTLINE;
		} else if (downwidth < mViewWidth) {
			mDownLeft = mViewWidth - downwidth;
		} else {
			mDownLeft = LEFTLINE;
		}
	}

	/**
	 * 生成歌词图片
	 */
	private void saveBitmap(Sentence lineInfo, int linenum) {
		String lyricStr = lineInfo.getContent();
		String lyricFinalStr = lyricStr.replaceAll("\\[|\\]", "");
		mLyric.setLyric(lyricFinalStr);

		if (linenum == UPLINE) {
			if (mFontBitmapUp != null && mFontBitmapUp.isRecycled() == false) {
				mFontBitmapUp.recycle();
			}
			if (mRenderBitmapUp != null && mRenderBitmapUp.isRecycled() == false) {
				mRenderBitmapUp.recycle();
			}
			mFontBitmapUp = mLyric.getFontBitmap();
			mRenderBitmapUp = mLyric.getRenderBitmap();
		} else if (linenum == DOWNLINE) {
			if (mFontBitmapDown != null && mFontBitmapDown.isRecycled() == false) {
				mFontBitmapDown.recycle();
			}
			if (mRenderBitmapDown != null && mRenderBitmapDown.isRecycled() == false) {
				mRenderBitmapDown.recycle();
			}
			mFontBitmapDown = mLyric.getFontBitmap();
			mRenderBitmapDown = mLyric.getRenderBitmap();
		} else {
			return;
		}
	}

	/**
	 * 获取每个渲染单位的字形图像的宽度信息。
	 * 
	 * @param lineInfo
	 *            一行歌词的歌词信息对象
	 * @param map
	 *            保存一个渲染单位字形图像的宽度和持续时间的map对象
	 */
	private Iterator<Entry<Double, Integer>> getGlyphInfo(Sentence lineInfo, Map<Double, Integer> map) {
		String lyricStr = lineInfo.getContent();

		ArrayList<Integer> times = lineInfo.getIntervalList();
		map.clear();// 清除map中保存的上一句的信息，防止对于抢占情况下渲染当前行有干扰。

		int starttime = (int) lineInfo.getStartTime();
		int curtime = (int) lineInfo.getCurrentTime();
		int durationtime = curtime - starttime;
		int totaltime = 0;
		int index = 0;
		// 定位到当前渲染的一个字
		while (index < times.size()) {
			totaltime += times.get(index);
			if (totaltime >= durationtime) {
				break;
			}
			index++;
		}
		
		if (index == times.size()) {
			index--;
		}
		mMoveX = (float) 0.0;
		int timesize = times.size();
		int num = 0;
		double width = 0.0;

		int COMMON_CHARACTER = 0;
		int CHINESE_CHARACTER = 1;
		int SPACE_CHARACTER = 2;
		int preIndex = 0;
		int state = 0;
		for (int i = 0; i < lyricStr.length(); i++) {
			if (lyricStr.charAt(i) >= 0 && lyricStr.charAt(i) <= 0xFF && lyricStr.charAt(i) != ' ') {
				// 前面有空格，则分割
				if (state != COMMON_CHARACTER) {
					width = mLyric.getTextWidth(lyricStr, preIndex, i);
					preIndex = i;
					while (map.containsKey(width)) {// 如果map中存在此key，则改变width,找到一个代替的key值，这里保证整数部分是相同的。
						width += 0.01;
					}
					if (num >= timesize) {
						break;
					}
					int time = times.get(num++);
					map.put(width, time);
				}
				state = COMMON_CHARACTER;
			} else if (lyricStr.charAt(i) == ' '||lyricStr.charAt(i) == '　') {// 是空格的情况，中文空格或英文空格
				state = SPACE_CHARACTER;
			} else {// 是中文(日韩)等
				state = CHINESE_CHARACTER;
				if (i == preIndex) {
					continue;
				}
				width = mLyric.getTextWidth(lyricStr, preIndex, i);
				preIndex = i;
				while (map.containsKey(width)) {// 如果map中存在此key，则改变width,找到一个代替的key值，这里保证整数部分是相同的。
					width += 0.01;
				}
				if (num >= timesize) {
					break;
				}
				int time = times.get(num++);
				map.put(width, time);
			}
		}

		if (preIndex <= lyricStr.length() - 1) {
			width = mLyric.getTextWidth(lyricStr, preIndex, lyricStr.length());
			while (map.containsKey(width)) {// 如果map中存在此key，则改变width,找到一个代替的key值，这里保证整数部分是相同的。
				width += 0.01;
			}
			if (num < timesize) {
				int time = times.get(num++);
				map.put(width, time);
			}
		}

		// 定位到当前一个字的起始时间
		Iterator<Entry<Double, Integer>> iter = map.entrySet().iterator();
		int startWordTime = 0;
		int startWordPos = 0;
		for (int i = 0; i < index; i++) {
			if (iter.hasNext()) {
				startWordPos += iter.next().getKey().intValue();
			}
			startWordTime += times.get(i);
		}

		// 定位到当前一个字的具体像素
		int durationWordTime = durationtime - startWordTime;
		if (!iter.hasNext()) {
			return null;
		}
		saveSleepTime(iter, map);
		int rendernum = 0;
		try {
		    rendernum = (int) (durationWordTime * 1000000L / mSleepDurationNanos);
		} catch (ArithmeticException e) {
			rendernum = 1;
		}
		float renderwidth = mStepWidth * rendernum;
		mMoveX += startWordPos + renderwidth;
		mCurNum = rendernum;
		return iter;
	}

	/**
	 * 获得当前状态，正在渲染上面一行，下面一行，或者停止渲染
	 * 
	 * @return
	 */
	public int getState() {
		return mState;
	}

	/**
	 * 设置渲染频率，每秒钟多少次
	 * 
	 * @param renderFramerate
	 *            渲染频率
	 */
	public void setRenderFramerate(int renderFramerate) {
		mRenderFramerate = renderFramerate;
	}

	public void setKscInfo(LyricInfo kscinfo) {
		synchronized (lock) {
			mKscInfo = kscinfo;
			if (mKscInfo == null) {
				return;
			}
			mSentenceList = mKscInfo.getList();
		}
	}

	/**
	 * 设置当前要渲染的一句歌词的歌词信息
	 * 
	 * @param curSentence
	 */
	public void setCurSentence(Sentence curSentence, int cmd) {
//		if (cmd == PhoneKTVView.LYRIC_INFO_CHANGED) {
//			setKscInfo(mKTVView.getKscInfo());
//		}
		
		if (mSentenceList == null || curSentence == null) {
			return;
		}
		mCurSentence = curSentence;
		int curIndex = (int) mCurSentence.getCurrentIndex();

		synchronized (lock) {
			mIsNewLyric = true;

			if (curIndex != mSentenceIndex || curIndex == 0 || mIsRefresh) {
				mCurSentence = curSentence;
				mSentenceIndex = curIndex;
				saveBitmap(mCurSentence, curIndex % 2);
				mIsRefresh = false;
			}

			// 抢占式的更新已经渲染的哪一行，不管其是否渲染完成。
			if (curIndex + 1 < mSentenceList.size()) {
				Sentence sentence = mSentenceList.get(curIndex + 1);
				mSentenceIndex++;
				saveBitmap(sentence, (curIndex + 1) % 2);
			} else {
				if ((curIndex + 1) % 2 == 0) {
					if (mFontBitmapUp != null) {
						mFontBitmapUp.eraseColor(0x00000000);
					}
					if (mRenderBitmapUp != null) {
						mRenderBitmapUp.eraseColor(0x00000000);
					}
				} else {
					if (mFontBitmapDown != null) {
						mFontBitmapDown.eraseColor(0x00000000);
					}
					if (mRenderBitmapDown != null) {
						mRenderBitmapDown.eraseColor(0x00000000);
					}
				}
			}

			getBackgroundImagePos();

			mRectSrc.setEmpty();
			mRectDst.setEmpty();

			mIsNeedReDraw = true;
			// doRender();

			if (mCurSentence.getCurrentIndex() % 2 == 0) {
				mState = UPSTATE;
			} else {
				mState = DOWNSTATE;
			}
		}

		synchronized (this) {
			notify();
		}
	}

	/**
	 * 保存当前渲染一次后需要休眠的时间值， 以及保存当前一次渲染需要渲染的步长 和当前渲染的那个字总共需要渲染多少次
	 * 
	 * @param it
	 *            map的迭代器
	 * @param map
	 *            保存歌词信息的map
	 */
	private void saveSleepTime(Iterator<Entry<Double, Integer>> it, Map<Double, Integer> map) {
		Double widthTemp = it.next().getKey();
		Integer durationTemp = map.get(widthTemp);
		int width = widthTemp.intValue();
		int duration = (int) (durationTemp/* * mRatio*/);

		mTotalNum = new BigDecimal((duration / 1000.0 * mRenderFramerate)).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
		mTotalNum = mTotalNum == 0 ? 1 : mTotalNum;
		mStepWidth = (float) width / mTotalNum;
		// mSleepDurationMillis = duration / mTotalNum;
		mSleepDurationNanos = duration * 1000000L / mTotalNum;
	}

	/**
	 * 首先判断是否到了需要有走马灯效果的时候，若是，则改变相关变量让渲染实现走马灯效果
	 */
	private void showRevolvingDoor() {
		if (mState == UPSTATE) {
			// 需要走马灯效果的情况
			if (mRevolvingDoor && mMoveX > 0.7 * mViewWidth) {
				mUpLeft -= 5;
				if (mUpLeft < mViewWidth - mFontBitmapUp.getWidth()) {
					mUpLeft = mViewWidth - mFontBitmapUp.getWidth();
					// mRevolvingDoor = false;
				}
				mIsNeedReDraw = true;
			}
		} else if (mState == DOWNSTATE) {
			// 需要走马灯效果的情况
			if (mRevolvingDoor && mMoveX > 0.7 * mViewWidth) {
				mDownLeft -= 5;
				if (mDownLeft < mViewWidth - mFontBitmapDown.getWidth()) {
					mDownLeft = mViewWidth - mFontBitmapDown.getWidth();
					// mRevolvingDoor = false;
				}
				mIsNeedReDraw = true;
			}
		} else {
			return;
		}
	}
	
	private int findStartSentence(long curTime) {
		if (mSentenceList.size() == 0) {
			return -1;
		}
		for (int i = 0; i < mSentenceList.size(); i++) {
			if (curTime < mSentenceList.get(i).getStartTime()) {
				if (i == 0) {
					return -1;
				}
				mSentenceList.get(i - 1).setCurrentTime(curTime);
				return i - 1;
			}
		}
		
		if (curTime < mSentenceList.get(mSentenceList.size() - 1).getEndTime()) {
			mSentenceList.get(mSentenceList.size() - 1).setCurrentTime(curTime);
    	} else {
    		mSentenceList.get(mSentenceList.size() - 1).setCurrentTime(mSentenceList.get(mSentenceList.size() - 1).getEndTime());
    	}
		return mSentenceList.size() - 1;
	}

	public void run() {
		// TODO Auto-generated method stub
		// 等待UI启动歌词渲染
		while ((!mKTVView.getIsStarting() || !mKTVView.getIsSurfaceCreated()) && mIsRunning) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if (mKTVView.getKscInfo() == null) {
			mKTVView.stop();
			return;
		}
		setKscInfo(mKTVView.getKscInfo());

		if (!mKTVView.getIsSameSong() && mKscInfo != null) {
			IMediaPlayerService mediaPlayerService = ServiceManager.getMediaplayerService();
    		MediaPlayer mplayer = mediaPlayerService.getMediaPlayer();
    		if (mKscInfo.getList().size() != 0 && mplayer.getCurrentPosition() < mKscInfo.getList().get(0).getStartTime()) {
			    prepareRender();
			    mState = OVERSTATE;
    		} else {
    			if (mKscInfo.getList().size() == 0) {
    				mKTVView.stop();
    				return;
    			}
    			int index = findStartSentence(mplayer.getCurrentPosition());
    			if (index != -1) {
    				setCurSentence(mSentenceList.get(index),PhoneKTVView.LYRIC_INFO_UNCHANGED);
    			}
    		}
		}	

		// 线程循环
		while (mIsRunning) {
			synchronized (this) {
				if (mState == OVERSTATE) {
					try {
						wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

			long starttime = System.nanoTime();

			synchronized (lock) {
				if (mCurSentence == null) {
					mState = OVERSTATE;
					continue;
				}

				// 是新的一句歌词么？
				if (mIsNewLyric) {
					mIsNewWord = false;
					// mRoleWidth = 0;
					mRevolvingDoor = false;
					mIsNewLyric = false;
					mMap.clear();

					mIter = getGlyphInfo(mCurSentence, mMap);
					if (mIter == null) {
						mState = OVERSTATE;
						continue;
					}
				    if ((mFontBitmapUp != null && mFontBitmapUp.getWidth() > mViewWidth && mState == UPSTATE)
				    	|| (mFontBitmapDown != null && mFontBitmapDown.getWidth() > mViewWidth && mState == DOWNSTATE)) {
						mRevolvingDoor = true;
					} else {
						mRevolvingDoor = false;
					}
				}

				// 是新的一个单词或者新的一个字么？
				if (mIsNewWord) {
					mCurNum = 0;
					mIsNewWord = false;

					if (mState != OVERSTATE) {
						if (mIter.hasNext()) {
							saveSleepTime(mIter, mMap);
						} else {
							mRectSrc.setEmpty();
							mRectDst.setEmpty();

							mState = OVERSTATE;
							continue;
						}
					} else {
						continue;
					}
				}

				// 先判断是否到了需要走马灯效果的时候，如果到了则实现走马灯效果
				showRevolvingDoor();

				// 得到Bitmap中的源rect和要画到view的目标rect
				if (mState == UPSTATE) {
					getRenderRect(mRenderBitmapUp, mUpLeft, mUpTop);
				} else if (mState == DOWNSTATE) {
					getRenderRect(mRenderBitmapDown, mDownLeft, mDownTop);
				} else {
					continue;
				}

				if (mCurNum++ >= mTotalNum - 1) {
					mIsNewWord = true;
				}
				mMoveX += mStepWidth;
				doRender();
				if (mIsPaused) {
					mState = OVERSTATE;
				}
			}

			long endtime = System.nanoTime();

			// 休眠一段时间后，渲染线程继续工作
			try {
				long sleeptime = mSleepDurationNanos - (endtime - starttime);
				sleeptime = sleeptime < 0 ? 0 : sleeptime;
				long millis = sleeptime / 1000000;
				int nanos = (int) sleeptime % 1000000;

				Thread.sleep(millis, nanos);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public Boolean startRender() {
		if (mIsRunning == true) {
			mIsRunning = false;
			Thread.yield();
		}
		synchronized (this) {
			this.notify();
		}

		if (mIsRunning == false) {
			mState = OVERSTATE;
			mIsRunning = true;
			mThread = new Thread(this);
			mThread.setName("renderthread#" + mThread.getId());
			mIsPaused = false;
			mThread.start();
			mCurSentence = null;
			return true;
		}
		return false;
	}

	/**
	 * 第一句歌词还没到来时候的渲染操作，渲染歌曲名字和歌唱者
	 */
	public void prepareRender() {
		Canvas canvas = null;
		try {
			canvas = mSurfaceHolder.lockCanvas();
			if (canvas == null || mKscInfo ==null) {
				return;
			}

			// 初始化paint
			canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
			canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
			Paint paint = new Paint();
			paint.setTextSize(mTextSize);
			paint.setAntiAlias(true);
			paint.setColor(Constant.LYRICFOREGROUNDCOLOR);

			// 获得歌曲名字的字形图像
			String songtitle = mKscInfo.getTitle();
			if (songtitle != null) {
				Rect rect1 = new Rect();
				paint.getTextBounds(songtitle, 0, songtitle.length(), rect1);
				int width1 = rect1.width();
				int startX1 = (mViewWidth - width1) / 2;
				canvas.drawText(songtitle, startX1, BOTTOMLINE - rect1.top, paint);// 第一行在surfaceview上留一个边界作为上边节
			}
			// 获取歌唱者的字形图像
			String singer = mKscInfo.getSinger();
			if (singer != null) {
				Rect rect2 = new Rect();
				paint.getTextBounds(singer, 0, singer.length(), rect2);		
				int width2 = rect2.width();
				int startX2 = (mViewWidth - width2) / 2;	
				canvas.drawText(singer, startX2, mViewHeight / 2 + LINESPACE / 2 - rect2.top, paint);// 第二行与第一行之间有行距。
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (canvas != null)
				mSurfaceHolder.unlockCanvasAndPost(canvas);
		}
	}

	/**
	 * 暂停歌词渲染
	 */
	public void pause() {
		synchronized (lock) {
			mState = OVERSTATE;
			mIsPaused = true;
		}
	}

	/**
	 * 恢复歌词渲染
	 */
	public void resume() {
		mIsPaused = false;
		if (mCurSentence == null) {
			return;
		}
		mState = (int) (mCurSentence.getCurrentIndex() % 2);
		synchronized (this) {
			this.notify();
		}
	}

	/**
	 * 停止歌词渲染
	 */
	public void stop() {
		mIsRunning = false;
		synchronized (this) {
			this.notify();
		}
		synchronized (lock) {
			mMoveX = 0;
			mRectSrc.setEmpty();
			mRectDst.setEmpty();
			mCurSentence = null;
			mIsNeedReDraw = false;
			mSentenceIndex = 0;
			mState = OVERSTATE;
		}
		
		Canvas canvas = null;
		try {
			canvas = mSurfaceHolder.lockCanvas();
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
			canvas.drawText(str, mViewWidth / 2, mViewHeight / 2, pt);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (canvas != null) {
				if (canvas != null)
					mSurfaceHolder.unlockCanvasAndPost(canvas);
			}
		}
	}

	public void clearRender() {
		synchronized (lock) {
			mCurSentence = null;
		}
	}

	public void setIsNeedReDraw(Boolean isNeedReDraw) {
		this.mIsNeedReDraw = isNeedReDraw;
	}

	public Boolean isPaused() {
		return mIsPaused;
	}
	
	public void setRatio(float ratio) {
		// TODO Auto-generated method stub
		synchronized (lock) {
			mRatio = ratio;
		}
	}
	
	public void setFontSize(int fontSize) {
		if (fontSize > mViewHeight / 2) {
			fontSize = mViewHeight / 2;
		}
		if (fontSize < 12) {
			fontSize = 12;
		}
		mLyric.setFontSize(fontSize);
		mTextSize = fontSize;
		mIsRefresh = true;
		setCurSentence(mCurSentence,PhoneKTVView.LYRIC_INFO_UNCHANGED);
	}
	
	public int getFontSize() {
		return mTextSize;
	}
	
	public void setColor(int fontColor,int renderColor) {
		mLyric.setFontColor(fontColor);
		mLyric.setRenderColor(renderColor);
		mIsRefresh = true;
		setCurSentence(mCurSentence,PhoneKTVView.LYRIC_INFO_UNCHANGED);
	}

	private Sentence mCurSentence;// 当前行的歌词信息
	private Lyric2Bmp mLyric; // 歌词字符串转图片对象
	private Bitmap mFontBitmapUp; // 上面一行未渲染的位图
	private Bitmap mRenderBitmapUp;// 上面一行已渲染的位图
	private Bitmap mFontBitmapDown;// 下面一行未渲染的位图
	private Bitmap mRenderBitmapDown;// 下面一行已渲染的位图
	private Map<Double, Integer> mMap;// 保存一行歌词字形图像的宽度信息和每个字的持续时间
	private int mRenderFramerate;// 渲染频率
	private int mViewWidth;// 视图宽度
	private int mViewHeight;// 视图高度
	private int mUpTop;// 上面一行歌词的top边界位置
	private int mUpLeft;// 上面一行歌词的left边界位置
	private int mDownTop;// 下面一行歌词的top边界位置
	private int mDownLeft;// 下面一行歌词的left边界位置
	private PhoneKTVView mKTVView;// 视图
	private Boolean mIsNewLyric;// 是否是新一句歌词
	private Boolean mIsNewWord;// 是否是新的一个字或者新单词
	private Iterator<Entry<Double, Integer>> mIter;// mMap的迭代器
	private int mState;// 当前渲染状态，是在渲染上面一行还是下面一行
	private float mStepWidth;// 当前一次渲染需要渲染的宽度
	private long mSleepDurationNanos;// 渲染完当前一次后需要休眠的纳秒时间
	private int mTotalNum;// 渲染一个字或者一个单词需要渲染多少次
	private int mCurNum;// 渲染一个字或者一个单词已经渲染了多少次
	private float mMoveX;// 当前渲染到的位置
	private Rect mRectSrc;// Bitmap中要渲染的rect
	private Rect mRectDst;// 渲染在视图的rect
	private Boolean mRevolvingDoor;// 是否需要走马灯渲染效果
	private Thread mThread;// 休眠线程
	private Boolean mIsNeedReDraw;// 是否需要重画
	private List<Sentence> mSentenceList;// 该链表保存了整首歌的每一句歌词的信息
	private LyricInfo mKscInfo;// 歌词信息，包括歌曲名，歌唱者和所有歌词
	private int mSentenceIndex;// 歌词的索引号
	private byte[] lock = new byte[0];// 线程同步的锁
	private SurfaceHolder mSurfaceHolder;// surfaceview操作对象
	// private int mRoleWidth;// 角色信息("(男)"，"(女)"等)的长度，初始值为-1
	private Boolean mIsRunning;// 线程是否运行。
	private Boolean mIsPaused = false;// 是否暂停
	private float mRatio = (float) 1.0;
	private boolean mIsRefresh = false;

	
}
