package com.amusic.media.lyric.render;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.SurfaceHolder;

import com.amusic.media.dialog.LyricModify;
import com.amusic.media.lyric.parser.LyricInfo;
import com.amusic.media.lyric.parser.Sentence;
import com.amusic.media.utils.Constant;

public class ScrollLyricRender implements Runnable{
	
    private static final float RENDERRATE = 20;
    private int mTextSize = 18;
	public ScrollLyricRender(FullLyricView lyricView,int width,int height,int textSize) {
		mLyricView = lyricView;
		mViewWidth = width;
		mViewHeight = height;
		mIsNewSentence = false;
		mSentenceList = null;
		mRenderList = null;
		mPaint = new Paint();
		mPaint.setTextSize(textSize);
		mPaint.setColor(Constant.LYRICFOREGROUNDCOLOR);
		mLyricTool = new Lyric2Bmp();
		mLyricTool.setFontSize(textSize);
		mTextSize = textSize;
		mSrcRect = new Rect();
		mDstRect = new RectF();
		mSurfaceHolder = mLyricView.getHolder();
	}
	
	public void run() {
		while ((!mLyricView.getIsSurfaceCreated() || !mLyricView.getIsStarting()) && mIsRunning ) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (mLyricView.getLyricInfo() == null) {
			return;
		}
		
		//生成整张图片并计算歌曲名和歌唱者信息的移动步长
		setKscInfo(mLyricView.getLyricInfo());
		
		if (mKscInfo.getList() == null || mKscInfo.getList().size() < 2) {
			return;
		}
		FullLyric2Bmp fullLyric2Bmp = new FullLyric2Bmp(mViewWidth);
		fullLyric2Bmp.setTextSize(mTextSize);
		fullLyric2Bmp.setLyricInfo(mKscInfo);
		fullLyric2Bmp.Convert2Bmp();
		mRenderList = fullLyric2Bmp.getRenderList();
		mListSize = mRenderList.size();
		
		if (mListSize < 2) {
			return;
		}

		mTop = 0;
		int startpos = mRenderList.get(2).getTop();
		int startTime = (int) mSentenceList.get(0).getStartTime();
		mTotalNum = (int) ((float) startTime / 1000.0 * RENDERRATE);
		mTotalNum = mTotalNum <= 0 ? 1 : mTotalNum;
		mStep = (float) (startpos / (float) mTotalNum);
		mNextTop = -1 * startpos;
		
		while(mIsRunning) {
			synchronized(this) {
				if (mIsPaused && !mIsNewSentence) {
					try {
						wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			long starttime = System.nanoTime();
			synchronized (lock) {
				// 先判断一下防止在执行上面代码时，外部调用stop函数
				if (!mIsRunning) {
					break;
				}
				if (mCurSentence != null && mIsNewSentence) {
					if (2+ mCurSentence.getCurrentIndex() >= mListSize) {
						mIsNewSentence = false;
						continue;
					}
					RenderInfo rdinfo =  mRenderList.get((int) (2+ mCurSentence.getCurrentIndex()));
					mTop = 0;				
					
					mNextTop = -1 * (rdinfo.getHeight() + FullLyric2Bmp.LINESPACE);
					mContext = rdinfo.getLyric();
					
					// 计算当前一句每一次移动的步长和需要移动多少次
					int duration = (int) (mCurSentence.getDuring() /** mRatio*/);
					int height = rdinfo.getHeight() + FullLyric2Bmp.LINESPACE;
					mTotalNum = (int) ((float) duration / 1000.0 * RENDERRATE);
					mTotalNum = mTotalNum <= 0 ? 1 : mTotalNum;
					mCurNum = (int) ((float) /*mRatio **/ (mCurSentence.getCurrentTime() - mCurSentence.getStartTime()) / 1000.0 * RENDERRATE);
					mStep = (float) (height / (float) mTotalNum);
					mTop -= mCurNum * mStep;
					mMoveX = 0;
					mLyricTool.setLyric(rdinfo.getLyric());
					if (mHighLightBmp != null && mHighLightBmp.isRecycled() == false) {
						mHighLightBmp.recycle();
					}
					mHighLightBmp = mLyricTool.getRenderBitmap();
					if (mCurNum == mTotalNum) {
						mMoveX = mHighLightBmp.getWidth();
					}
					mSrcRect.setEmpty();
					mDstRect.setEmpty();

					mIsNewSentence = false;					
				} else if (mCurSentence != null && mCurNum == mTotalNum) {			
					mStep = 0;
					mMoveX = mHighLightBmp.getWidth();
				} else {
					if (mTop - mStep < mNextTop) {
						mStep = 0;
					}
				    mTop -= mStep;
				}
				
				if (mCurSentence != null) {
					if (mCurNum > 0 && mCurNum < mTotalNum) {
						saveXposition();
					}
					getRenderRect();
				}
				doRender();
				mCurNum++;
			}
			long endtime = System.nanoTime();
			try {
				long sleeptime = 50000000 - (endtime - starttime);
				sleeptime = sleeptime < 0 ? 0 : sleeptime;
				long millis = sleeptime / 1000000;
				int nanos = (int) sleeptime % 1000000;
				Thread.sleep(millis,nanos);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}	
		}	
	}
	
	/**
	 * 全屏歌词屏幕触摸后调用该函数，将歌词向上或向下移动
	 * @param offset 手指滑动的距离
	 * @return
	 */
	public long scrollBy(int offset) {
		int base = roundHalfUp(mTop + offset);
		mLineNum = -1;
		int curIndex = 0;
		if (mCurSentence != null) {
			curIndex = (int) mCurSentence.getCurrentIndex() + 2;
		} 
		int listSize = 0;
		if (mRenderList != null) {
			listSize = mRenderList.size();
		} else {
			return 0;
		}
		
		int i = 0;		
		int height = 0;
		int newbase = 0;
		if (offset < 0) {//向上滑动
			for (i = curIndex; i + 2 < listSize; i++) {
				RenderInfo rdinfo =  mRenderList.get(i);
				height += rdinfo.getHeight();
				if (height > -1 * base) {
					newbase = (height + base) - rdinfo.getHeight();
					break;
				}
				height += FullLyric2Bmp.LINESPACE;
			}
			i = i < listSize -2 ? i - 2 :listSize - 3;
			if (i < 0) {
				newbase = base;
			}
		} else if (offset > 0) {//向下滑动
			height -= mRenderList.get((int) (curIndex)).getHeight();
			for (i = curIndex; i >= 0; i--) {
				RenderInfo rdinfo =  mRenderList.get((int) (i));
				height +=  rdinfo.getHeight();
				if (height >= base ) {
					newbase = base - height;
					break;
				}
				height += FullLyric2Bmp.LINESPACE;
			}
			i = i < 0? i - 1 : i - 2;
			if (i == -1)
			newbase -= mRenderList.get(1).getHeight();
		} else {
			return 0;
		}

		Sentence stc = null;
		long curTime = 0;
		
		
		if (i >= 0) {
		    stc = mSentenceList.get(i);
		    curTime = stc.getStartTime();
		    if (newbase < 0) {
			    RenderInfo rdinfo =  mRenderList.get(i + 2);
				height = rdinfo.getHeight();
				if (-1 * newbase > height) {
					curTime += (stc.getEndTime() - stc.getStartTime());
				} else {
				    curTime += (stc.getEndTime() - stc.getStartTime()) * ( -1 * newbase) / height;
				}
			}
			
		    stc.setCurrentTime(curTime);
		    mDragSentence = stc;
		    scrollRender(stc,newbase);
		} else if (i == -2){
			curTime = 0;
			scrollRender(stc,newbase);
		} else if (i == -1) {
			curTime = mSentenceList.get(0).getStartTime() / 2;
//			newbase -= mRenderList.get(1).getHeight();
			scrollRender(stc,newbase);
		}
		
//		long starttime = 0;
//		if (mCurSentence !=  null) {
//			starttime = mCurSentence.getStartTime();
//		}
//		long oldTime = (int) (starttime + mCurNum * 50);
//		return curTime - oldTime;
		return curTime;
	}
	
	
	private void scrollRender(Sentence stc,int base) {
		Canvas canvas = null;	
		try{
			// 清屏
			canvas = mSurfaceHolder.lockCanvas();
			
			if (canvas == null) {
				return;
			}
			canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG));
			canvas.drawColor(Color.TRANSPARENT,Mode.CLEAR);

			// 画全屏歌词及中间线
			canvas.drawLine(0, mViewHeight/2, mViewWidth, mViewHeight/2, mPaint);
			if (stc != null) {
				canvas.drawText(formatTime((int)stc.getCurrentTime()), 10, mViewHeight/2,mPaint);
			} else {
				canvas.drawText(formatTime(0), 10, mViewHeight/2,mPaint);
			}
			
			int top = roundHalfUp((mViewHeight >> 1 ) + base);
			int index = 0;
			if (stc != null) {
			    index = (int) (2+ stc.getCurrentIndex());
			}

			
			// 向下遍历歌词直至到达屏幕底部
			for (int i = index;i < mListSize;i++) {
				if (top > mViewHeight) {
					break;
				}
				RenderInfo rdinfo =  mRenderList.get(i);
				int startX = 0;
				if (rdinfo.isBreakLine()) {
					List<LineInfo> lineList = rdinfo.getLineList();
					for (int j = 0;j < lineList.size();j++) {
						String lyric = lineList.get(j).getLineStr();
						startX = (mViewWidth - lineList.get(j).getWidth()) >> 1;
						int base1 = lineList.get(j).getLineTop() - lineList.get(j).getBase();
						canvas.drawText(lyric, startX, top - base1 , mLyricTool.getPaint());
						top += lineList.get(j).getHeight() + FullLyric2Bmp.LINESPACE;
					}
				} else {
					String lyric = rdinfo.getLyric();
					startX = (mViewWidth - rdinfo.getWidth()) >> 1;
					int base1 = rdinfo.getTop() - rdinfo.getBase();
					canvas.drawText(lyric, startX, top - base1 , mLyricTool.getPaint());
					top += rdinfo.getHeight() + FullLyric2Bmp.LINESPACE;
				}
			}
			
			// 想上遍历歌词直至到达屏幕顶部
			top = roundHalfUp((mViewHeight >> 1) + base);
			for (int i = index - 1;i >= 0;i--) {
				int startX = 0;
				RenderInfo rdinfo =  mRenderList.get(i);
				if (rdinfo.isBreakLine()) {
					List<LineInfo> lineList = rdinfo.getLineList();
					for (int j = lineList.size() - 1;j >= 0;j--) {
						String lyric = lineList.get(j).getLineStr();
						startX = (mViewWidth - lineList.get(j).getWidth()) >> 1;
						int base1 = lineList.get(j).getLineTop() - lineList.get(j).getBase();
						top -= lineList.get(j).getHeight() + FullLyric2Bmp.LINESPACE;
						canvas.drawText(lyric, startX, top - base1 , mLyricTool.getPaint());	
					}
				} else {
					String lyric = rdinfo.getLyric();
					startX = (mViewWidth - rdinfo.getWidth()) >> 1;
					int base1 = rdinfo.getTop() - rdinfo.getBase();
					top -= rdinfo.getHeight() + FullLyric2Bmp.LINESPACE;
					canvas.drawText(lyric, startX, top - base1 , mLyricTool.getPaint());
				}
				if (top < 0) {
					break;
				}
			}
			
			if (stc != null) {
				float moveX = saveXposition_scroll(stc, calRenderNum(stc));
				getRenderRect_scroll(stc, moveX, base);
				renderHighLight(canvas, stc, base);
			}
			

		} finally {
			if (canvas != null) {
			    mSurfaceHolder.unlockCanvasAndPost(canvas);
			}
		}
	}
	
	private String formatTime(int times) {
		times /= 1000;
		int minutes = times / 60;
		int seconds = times % 60;
		minutes %= 60;
		return String.format("%02d:%02d", minutes, seconds);
		//return MessageFormat.format("{1,number,00}:{2,number,00}",  time / 1000 / 60 % 60, time / 1000 % 60);
	}
	
	private void renderHighLight(Canvas canvas, Sentence stc, float top) {
		if (canvas == null) {
			return;
		}
		// 歌词高亮
		if (mHighLightBmp != null) {
			if (stc == null || 2 + stc.getCurrentIndex() >= mRenderList.size() ) {
				return;
			}
			RenderInfo rdinfo =  mRenderList.get((int) (2+ stc.getCurrentIndex()));
			if (rdinfo.isBreakLine()) {
				List<LineInfo> lineList = rdinfo.getLineList();
				if (mLineNum != 0) {
					for (int i = 0; i < mLineNum; i++) {
						LineInfo lineIf = lineList.get(i);
						String lineStr = lineIf.getLineStr();

						int base = lineIf.getLineTop() - lineIf.getBase();
						int hltop = mViewHeight >> 1;
					    hltop += lineIf.getLineTop() - lineList.get(0).getLineTop();
						float startY = (float) (hltop + top);
						float startX = (mViewWidth - lineList.get(i).getWidth()) >> 1;
						canvas.drawText(lineStr,startX,startY -base,mLyricTool.getRenderPaint());
					}
				}
				if (mSingleLineBmp != null && !mSingleLineBmp.isRecycled()) {
				canvas.drawBitmap(mSingleLineBmp, mSrcRect, mDstRect, null);
				}
			} else {
				if (mHighLightBmp != null && !mHighLightBmp.isRecycled()) {
		        canvas.drawBitmap(mHighLightBmp, mSrcRect, mDstRect, null);
				}
			}
		}
	}
	
	private float saveXposition_scroll(Sentence stc, int curNum) {
		ArrayList<Integer> timelist = stc.getIntervalList();
		int listSize = timelist.size();
		int wordIndex = -1;
		int i = wordIndex;
		boolean isNewWord = false;
		
		int timeSum = 0;
		
		// 找到当前渲染的字
		while (curNum * 50 > timeSum && wordIndex < listSize - 1) {
			timeSum += timelist.get(++wordIndex);
			isNewWord = true;
		}
		
		int diffIndex = wordIndex - i;
		
		String conText = stc.getContent();
		int charIndex = 0;
		int preWidth = 0;
		int wordWidth = 0;
		float moveX = 0;
		
		if (isNewWord) {
			// 找到当前正在渲染的文字
			while (--diffIndex >= 0) {
				if (diffIndex == 0) {
					String prestr = conText.substring(0, charIndex);
					preWidth = mLyricTool.getTextWidth(prestr, 0, prestr.length());
				}
				if (charIndex < conText.length()) {
					int k = 0;
					if (conText.charAt(charIndex) > 0 && conText.charAt(charIndex) < 0xff && conText.charAt(charIndex) != ' ') {
						for (k = charIndex;k < conText.length();k++) {
							if ((conText.charAt(k) == ' ') || (conText.charAt(k) > 0xff)) {
								break;
							}
						}
						String str = null;
						// 如果是碰到空格的情况
						if (k < conText.length() && (conText.charAt(k) == ' ' || conText.charAt(k) == '　')) {
							int m = k + 1;
							// 连续空格。
							while((m < conText.length()) && (conText.charAt(m) == ' ' || conText.charAt(m) == '　')){
								m++;
							}
							str = conText.substring(charIndex, m);
							charIndex = m;
						} else {// 汉字的情况
							str = conText.substring(charIndex, k);
							charIndex = k;
						}
						wordWidth = mLyricTool.getTextWidth(str, 0, str.length());
					} else {// 汉字(包括中文空格)或英文空格的情况
						int m = charIndex + 1;
						while((m < conText.length()) && (conText.charAt(m) == ' ' || conText.charAt(m) == '　')) {
							m++;
						}
						String str = conText.substring(charIndex, m);
						charIndex = m;
						wordWidth = mLyricTool.getTextWidth(str, 0, str.length());
					}
				}
			}
			isNewWord = false;	
		}

		wordIndex = wordIndex < 0 ? 0 : wordIndex;
		// 确定当前画笔应该画到的位置
		int wordtime = timelist.get(wordIndex);
		wordtime = wordtime <= 0 ? 1 : wordtime;
		float ratio = (float) 0.0;
		ratio = (float) (curNum * 50 - timeSum + wordtime) / wordtime;
		ratio = ratio > 1 ? 1 : ratio;
		moveX = preWidth + wordWidth * ratio;
		return moveX;
	}
	
	private int calRenderNum(Sentence stc) {
		if (2+ stc.getCurrentIndex() >= mListSize) {
			return -1;
		}
		RenderInfo rdinfo =  mRenderList.get((int) (2+ stc.getCurrentIndex()));
		
		int totalNum = 0;

		int curNum = 0;
		// 计算当前一句每一次移动的步长和需要移动多少次
		int duration = (int) (stc.getDuring());
		totalNum = (int) ((float) duration / 1000.0 * RENDERRATE);
		totalNum = totalNum <= 0 ? 1 : totalNum;
		curNum = (int) ((float) (stc.getCurrentTime() - stc.getStartTime()) / 1000.0 * RENDERRATE);
		mLyricTool.setLyric(rdinfo.getLyric());
		if (mHighLightBmp != null && mHighLightBmp.isRecycled() == false) {
			mHighLightBmp.recycle();
		}
		mHighLightBmp = mLyricTool.getRenderBitmap();
	
		mSrcRect.setEmpty();
		mDstRect.setEmpty();
		return curNum;
	}
	
	private void getRenderRect_scroll(Sentence stc, float moveX, float base ) {
		
		float starty = 0;
		float startx = 0;
		int hltop = mViewHeight / 2;
		
		// 获得起始点位置
		RenderInfo rdinfo =  mRenderList.get((int) (2+ stc.getCurrentIndex()));
		
		// 渲染换行的情况，找到当前正在渲染的那一行
		if (rdinfo.isBreakLine()) {
			List<LineInfo> lineList = rdinfo.getLineList();
			int width = 0;
			int i = 0;
			for (i = 0; i < lineList.size(); i++) {
				width += lineList.get(i).getWidth();
				if ((int) moveX <= width) {
					moveX = moveX + lineList.get(i).getWidth() - width;
					
					if (i != mLineNum) {
						if (mSingleLineBmp != null && mSingleLineBmp.isRecycled() == false) {
							mSingleLineBmp.recycle();
						}
						mLyricTool.setLyric(lineList.get(i).getLineStr());
						mSingleLineBmp = mLyricTool.getRenderBitmap();
						mLineNum = i;
					}
					break;
				}
			}
			if (i >= lineList.size()) {
				i = lineList.size() - 1;
				if (mSingleLineBmp != null && mSingleLineBmp.isRecycled() == false) {
					mSingleLineBmp.recycle();
				}
				mLyricTool.setLyric(lineList.get(i).getLineStr());
				mSingleLineBmp = mLyricTool.getRenderBitmap();
			}
			hltop += lineList.get(i).getLineTop() - lineList.get(0).getLineTop();
			startx =  (mViewWidth- lineList.get(i).getWidth()) / 2;	
		} else {
			startx = (mViewWidth - mHighLightBmp.getWidth()) / 2;
		}
			
		starty = (float) (hltop + base);
	    
	    mSrcRect.top = 0;
	    mSrcRect.left = 0;
	    mSrcRect.bottom = mHighLightBmp.getHeight();
	    mSrcRect.right = (int) moveX;
	    
	    mDstRect.top = starty;
	    mDstRect.left = startx;
	    mDstRect.bottom = starty + mSrcRect.bottom;
	    mDstRect.right = startx + (int) moveX;	
	}
	
	private void saveXposition() {
		ArrayList<Integer> timelist = mCurSentence.getIntervalList();
		int listSize = timelist.size();
		int i = mWordIndex;
		
		// 找到当前渲染的字
		while (mCurNum * 50 > mTimeSum && mWordIndex < listSize - 1) {
			mTimeSum += timelist.get(++mWordIndex);
			mIsNewWord = true;
		}
		
		int diffIndex = mWordIndex - i;
		
		if (mIsNewWord) {
			// 找到当前正在渲染的文字
			while (--diffIndex >= 0) {
				if (diffIndex == 0) {
					String prestr = mContext.substring(0, mCharIndex);
					mPreWidth = mLyricTool.getTextWidth(prestr, 0, prestr.length());
				}
				if (mCharIndex < mContext.length()) {
					int k = 0;
					if (mContext.charAt(mCharIndex) > 0 && mContext.charAt(mCharIndex) < 0xff && mContext.charAt(mCharIndex) != ' ') {
						for (k = mCharIndex;k < mContext.length();k++) {
							if ((mContext.charAt(k) == ' ') || (mContext.charAt(k) > 0xff)) {
								break;
							}
						}
						String str = null;
						// 如果是碰到空格的情况
						if (k < mContext.length() && (mContext.charAt(k) == ' ' || mContext.charAt(k) == '　')) {
							int m = k + 1;
							// 连续空格。
							while((m < mContext.length()) && (mContext.charAt(m) == ' ' || mContext.charAt(m) == '　')){
								m++;
							}
							str = mContext.substring(mCharIndex, m);
							mCharIndex = m;
						} else {// 汉字的情况
							str = mContext.substring(mCharIndex, k);
						    mCharIndex = k;
						}
						mWordWidth = mLyricTool.getTextWidth(str, 0, str.length());
					} else {// 汉字(包括中文空格)或英文空格的情况
						int m = mCharIndex + 1;
						while((m < mContext.length()) && (mContext.charAt(m) == ' ' || mContext.charAt(m) == '　')) {
							m++;
						}
						String str = mContext.substring(mCharIndex, m);
						mCharIndex = m;
						mWordWidth = mLyricTool.getTextWidth(str, 0, str.length());
					}
				}
			}
			mIsNewWord = false;	
		}

		// 确定当前画笔应该画到的位置
		int wordtime = timelist.get(mWordIndex);
		wordtime = wordtime <= 0 ? 1 : wordtime;
		float ratio = (float) 0.0;
		ratio = (float) (mCurNum * 50 - mTimeSum + wordtime) / wordtime;
		ratio = ratio > 1 ? 1 : ratio;
		mMoveX = mPreWidth + mWordWidth * ratio;
	}
	
	/**
	 * 获得当前渲染变色的矩形区域
	 */
	private void getRenderRect() {
		if (mHighLightBmp == null || mCurSentence == null) {
			return;
		}
		float starty = 0;
		int hltop = mViewHeight / 2;
		float moveX = mMoveX;
		
		// 获得起始点位置
		RenderInfo rdinfo =  mRenderList.get((int) (2+ mCurSentence.getCurrentIndex()));
		
		// 渲染换行的情况，找到当前正在渲染的那一行
		if (rdinfo.isBreakLine()) {
			List<LineInfo> lineList = rdinfo.getLineList();
			int width = 0;
			int i = 0;
			for (i = 0; i < lineList.size(); i++) {
				width += lineList.get(i).getWidth();
				if ((int) mMoveX <= width) {
					moveX = mMoveX + lineList.get(i).getWidth() - width;
					if (i != mLineNum) {
						if (mSingleLineBmp != null && mSingleLineBmp.isRecycled() == false) {
							mSingleLineBmp.recycle();
						}
						mLyricTool.setLyric(lineList.get(i).getLineStr());
						mSingleLineBmp = mLyricTool.getRenderBitmap();
						mLineNum = i;
					}
					break;
				}
			}
			if (i >= lineList.size()) {
				i = lineList.size() - 1;
			}
			hltop += lineList.get(i).getLineTop() - lineList.get(0).getLineTop();
			mStartX =  (mViewWidth- lineList.get(i).getWidth()) / 2;	
		} else {
			mStartX = (mViewWidth - mHighLightBmp.getWidth()) / 2;
		}
			
		starty = (float) (hltop + mTop);
	    
	    mSrcRect.top = 0;
	    mSrcRect.left = 0;
	    mSrcRect.bottom = mHighLightBmp.getHeight();
	    mSrcRect.right = (int) moveX;
	    
	    mDstRect.top = starty;
	    mDstRect.left = mStartX;
	    mDstRect.bottom = starty + mSrcRect.bottom;
	    mDstRect.right = mStartX + (int) moveX;	    
	}
	
	public int roundHalfUp(float fnum) {
		return new BigDecimal(fnum).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
	}
	
	public void doRender() {
		Canvas canvas = null;
		
		try{
			//清屏
			canvas = mSurfaceHolder.lockCanvas();
			if (canvas == null || mRenderList == null) {
				return;
			}

			canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG));
			canvas.drawColor(Color.TRANSPARENT,Mode.CLEAR);
			
			if (LyricModify.isPop) {
				canvas.drawLine(0, mViewHeight/2, mViewWidth, mViewHeight/2, mPaint);
			}
			int index = 0;
			int top = roundHalfUp((mViewHeight >> 1 ) + mTop);
			if (mCurSentence != null) {
				index = (int) (mCurSentence.getCurrentIndex() + 2);
			}
			
			// 向下遍历歌词直至到达屏幕底部
			for (int i = index;i < mListSize;i++) {
				if (top > mViewHeight) {
					break;
				}

				RenderInfo rdinfo = mRenderList.get(i);
				int startX = 0;
				if (rdinfo.isBreakLine()) {
					List<LineInfo> lineList = rdinfo.getLineList();
					for (int j = 0;j < lineList.size();j++) {
						String lyric = lineList.get(j).getLineStr();
						startX = (mViewWidth - lineList.get(j).getWidth()) >> 1;
						int base = lineList.get(j).getLineTop() - lineList.get(j).getBase();
						canvas.drawText(lyric, startX, top - base , mLyricTool.getPaint());
						top += lineList.get(j).getHeight() + FullLyric2Bmp.LINESPACE;
					}
				} else {
					String lyric = rdinfo.getLyric();
					startX = (mViewWidth - rdinfo.getWidth()) >> 1;
					int base = rdinfo.getTop() - rdinfo.getBase();
					canvas.drawText(lyric, startX, top - base , mLyricTool.getPaint());
					top += rdinfo.getHeight() + FullLyric2Bmp.LINESPACE;
				}
			}
			
			// 想上遍历歌词直至到达屏幕顶部
			top = roundHalfUp((mViewHeight >> 1) + mTop);
			for (int i = index - 1;i >= 0;i--) {
				RenderInfo rdinfo = mRenderList.get(i);
				int startX = 0;
				if (rdinfo.isBreakLine()) {
					List<LineInfo> lineList = rdinfo.getLineList();
					for (int j = lineList.size() - 1;j >= 0;j--) {
						String lyric = lineList.get(j).getLineStr();
						startX = (mViewWidth - lineList.get(j).getWidth()) >> 1;
						int base = lineList.get(j).getLineTop() - lineList.get(j).getBase();
						top -= lineList.get(j).getHeight() + FullLyric2Bmp.LINESPACE;
						canvas.drawText(lyric, startX, top - base , mLyricTool.getPaint());	
					}
				} else {
					String lyric = rdinfo.getLyric();
					startX = (mViewWidth - rdinfo.getWidth()) >> 1;
					int base = rdinfo.getTop() - rdinfo.getBase();
					top -= rdinfo.getHeight() + FullLyric2Bmp.LINESPACE;
					canvas.drawText(lyric, startX, top - base , mLyricTool.getPaint());
				}
				if (top < 0) {
					break;
				}
			}
			
			// 歌词高亮
			if (mHighLightBmp != null) {
				if (mCurSentence == null || 2 + mCurSentence.getCurrentIndex() >= mRenderList.size() ) {
					return;
				}
				RenderInfo rdinfo =  mRenderList.get((int) (2+ mCurSentence.getCurrentIndex()));
				if (rdinfo.isBreakLine()) {
					List<LineInfo> lineList = rdinfo.getLineList();
					if (mLineNum != 0) {
						for (int i = 0; i < mLineNum; i++) {
							LineInfo lineIf = lineList.get(i);
							String lineStr = lineIf.getLineStr();

							int base = lineIf.getLineTop() - lineIf.getBase();
							int hltop = mViewHeight >> 1;
						    hltop += lineIf.getLineTop() - lineList.get(0).getLineTop();
							float startY = (float) (hltop + mTop);
							float startX = (mViewWidth - lineList.get(i).getWidth()) >> 1;
							canvas.drawText(lineStr,startX,startY -base,mLyricTool.getRenderPaint());
						}
					}
					canvas.drawBitmap(mSingleLineBmp, mSrcRect, mDstRect, null);

				} else {
			        canvas.drawBitmap(mHighLightBmp, mSrcRect, mDstRect, null);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (canvas != null) {
				mSurfaceHolder.unlockCanvasAndPost(canvas);
			}
		}
	}
	
	/**
	 * 设置当前正在唱的那一句歌词
	 * @param stc 歌词
	 */
	public void setCurSentence(Sentence stc) {
		synchronized (lock) {
			mCurSentence = stc;
			mIsNewSentence = true;
			mIsNewWord = true;
			mLineNum = -1;
			mCurNum = 0;
			mWordIndex = -1;
			mCharIndex = 0;
			mTimeSum = 0;
		}
		synchronized(this) {
    	    notify();
    	}
	}
	
	/**
	 * 设置歌词信息
	 * @param info
	 */
	public void setKscInfo(LyricInfo info) {
		synchronized (lock) {
			mKscInfo = info;
			if (info == null) {
				return;
			}
			mSentenceList = info.getList();
		}
	}
	
	public void startRender() {
		
		if (mIsRunning == true) {
			mIsRunning = false;
			Thread.yield();
		}
		synchronized (this) {
			this.notify();
		}	
		
	    mThread = new Thread(this);
	    mThread.setName("renderthreadFullLyricView#"+mThread.getId());
	    mIsRunning = true;
        mThread.start();
        mIsPaused = false;
	}
	
	private void clearScreen() {
		Canvas canvas = null;
		try{
			mCurSentence = null;
			//清屏
			canvas = mSurfaceHolder.lockCanvas();
			if (canvas == null) {
				return;
			}
			canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG));
			canvas.drawColor(Color.TRANSPARENT,Mode.CLEAR);
		} finally {
			if (canvas != null) {
				mSurfaceHolder.unlockCanvasAndPost(canvas);
			}
		}
		mCurSentence = null;
	}
	
	public void clearRender() {
//		mCurSentence = null;
	}
	
    public void pause() {
    	synchronized(this) {
    	    mIsPaused = true;
    	}
    }
    
    public void play() {
    	synchronized(this) {
    		mIsPaused = false;
    	    notify();
    	}
    }
    
    public boolean isPaused() {
    	return mIsPaused;
    }
    
    public void resetTop() {
    	mTop = 0;
    }
    
    public void stop() {
    	mIsRunning = false;
    	synchronized (this) {
			this.notify();
		}
    	
    	synchronized (lock) {
	    	if (mHighLightBmp != null && mHighLightBmp.isRecycled() == false) {
	    		mHighLightBmp.recycle();
	    		mHighLightBmp = null;
	    	}
	    	if (mRenderList != null) {
	    		mRenderList.clear();
	    		mRenderList = null;
	    		mListSize = 0;
	    	}
    	}
    	mCurSentence = null;
    	clearScreen();
    }
    
    public void prepareRender() {
    	if (mListSize < 2) {
			return;
		}

		mTop = 0;
		int startpos = mRenderList.get(2).getTop();
		int startTime = (int) mSentenceList.get(0).getStartTime();
		mTotalNum = (int) ((float) startTime / 1000.0 * RENDERRATE);
		mTotalNum = mTotalNum <= 0 ? 1 : mTotalNum;
		mStep = (float) (startpos / (float) mTotalNum);
		mNextTop = -1 * startpos;
		mCurSentence = null;
		if (mIsPaused) {
		    doRender();
		}
    }
    
    public void setRatio(float ratio) {
		// TODO Auto-generated method stub
    	synchronized (lock) {
			mRatio = ratio;
		}
	}
    
    public Sentence getDragSentence() {
    	return mDragSentence;
    }
    
    public void clearDragSentence() {
    	mDragSentence = null;
    }
    
    public void setColor(int fontColor, int renderColor) {
    	mLyricTool.setFontColor(fontColor);
    	mLyricTool.setRenderColor(renderColor);
    	mPaint.setColor(renderColor);
		setCurSentence(mCurSentence);
	}

	private Thread                mThread;//渲染线程
	private Sentence              mCurSentence;//当前一句歌词
	private Sentence              mDragSentence = null; // 拖拽位置的对应sentence
	private int                   mViewWidth;//view宽度
	private int                   mViewHeight;//view高度
	private FullLyricView         mLyricView;//屏幕view
	private Bitmap                mHighLightBmp;//一句高亮歌词的图片
	private Bitmap                mSingleLineBmp;//一行高亮歌词的图片，对于一句歌词有多行的情况
	private SurfaceHolder         mSurfaceHolder;//surfaceview操作对象
	private LyricInfo             mKscInfo;//歌词信息
	private float                 mTop;//当前歌词的top位置
	private float                 mNextTop;//下一句歌词的top线所对应的位置
	private Boolean               mIsRunning = true;//标志线程是否运行
	private List<Sentence>        mSentenceList;//该链表保存了整首歌的每一句歌词的信息
    private List<RenderInfo>      mRenderList;//该链表保存了整首歌的渲染信息
	private int                   mListSize;//链表大小
    private int                   mTotalNum;//该行歌词需要多少次渲染完成
    private int                   mCurNum;//当前已经渲染了多少次了
    private Paint                 mPaint;//画笔，用于画屏幕中间的红线
    private float                 mStep;//每一步上移的高度
    private byte[]                lock = new byte[0];//线程同步的锁
    private Boolean               mIsNewSentence;//是否是一句新的歌词
    private Lyric2Bmp             mLyricTool;//歌词测量工具
    private Rect                  mSrcRect;//高亮图片的矩形区域
    private RectF                 mDstRect;//目标矩形，即画到屏幕上的区域
    private float                 mMoveX;//渲染画笔的位置
    private String                mContext;//去掉角色信息之后的长度
    private int                   mWordIndex;//单词的索引
    private long                  mTimeSum;//表示单句歌词从开始到渲染字或单词的总时间
    private int                   mCharIndex;//歌词中字母的索引
    private Boolean               mIsNewWord;//是否是一个新的歌词
    private float                 mPreWidth;//渲染字前面的长度
    private float                 mWordWidth;//当前一个字或者单词的长度
    private float                 mStartX;//开始画的位置
    private int                   mLineNum = -1;//一句歌词过长时，会分成几行显示，该变量标志当前渲染的行数
    private Boolean               mIsPaused;//是否暂停
    private float                 mRatio = (float) 1.0;
	
}
