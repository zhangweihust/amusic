package com.android.media.lyric.render;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.amusic.media.R;
import com.android.media.MediaApplication;
import com.android.media.event.impl.MediaEventArgs;
import com.android.media.event.impl.MediaEventTypes;
import com.android.media.lyric.parser.Sentence;
import com.android.media.screens.impl.ScreenLyricSpeed;
import com.android.media.services.IMediaEventService;
import com.android.media.services.impl.ServiceManager;
import com.android.media.toolbox.DETool;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;

public class LyricMakerRender  implements Runnable {

	public LyricMakerRender(LyricMakerView view,int width,int height) {
		mLyricView = view;
		mViewWidth = width;
		mViewHeight = height;
		
		Resources res = ServiceManager.getAmtMedia().getResources();
	    int fontSize = (int) res.getDimension(R.dimen.lyric_make_fontSize);
		
		mPaint = new Paint();
		mPaint.setColor(0xFFADE5E6);
		mPaint.setTextSize(fontSize);
		
		mRenderPaint = new Paint();
		mRenderPaint.setColor(0xFF00B4FF);
		mRenderPaint.setTextSize(fontSize);
		
		mCurPaint = new Paint();
		mCurPaint.setColor(0xFFFFF700);
		mCurPaint.setTextSize(fontSize);
		
		mSurfaceHolder = mLyricView.getHolder();
    }

	public void startRender() {
		Thread mThread = new Thread(this);
	    mThread.setName("renderthread#"+mThread.getId());
	    mIsRunning = true;
	    mLineNo = 0;
	    mLastLineNo = 0;
	    mLastLineOfCurSentence = 0;
	    mIsPaused = false;
	    mLastWordNo = 0;
	    mLineDiff = 0;
	    mFirstWord = 0;
	    isLyricMakeOver = false;
	    mState = IDLE_STATE;
        mThread.start();
	}

	@Override
	public void run() {
		while (!mLyricView.getIsSurfaceCreated() || (mLyricView.getLyricList() == null)) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		setLyricList(mLyricView.getLyricList());
		GenerateRenderInfo renderList = new GenerateRenderInfo(mViewWidth,mViewHeight,mLyricList);
		mRenderInfoList = renderList.generateList();
		handler = mLyricView.getHandler();
		doRender(WordInfo.IDLE_STATE);
	}

	public void doRender(int state) {
        Canvas canvas = null;
		
		try{
			if (mLineNo > mRenderInfoList.size()) {
				return;
			}
			// 清屏
			canvas = mSurfaceHolder.lockCanvas();
			if (canvas == null) {
				return;
			}
			canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG));
			canvas.drawColor(Color.TRANSPARENT,Mode.CLEAR);
			
			int top = (mViewHeight >> 1) - mLineDiff;
			int diff = 0;
			
			if (mLineNo == mRenderInfoList.size()) {
				diff = top - mRenderInfoList.get(mLastLineNo).getTop() - mRenderInfoList.get(mLastLineNo).getHeight();
			} else {
			    diff = top - mRenderInfoList.get(mLineNo).getTop();
			}
			
			// 渲染当前一句和后面的歌词。
			for (int i = mLineNo; i < mRenderInfoList.size(); i++) {
				RenderInfoLyricMaker rdinfo = mRenderInfoList.get(i);
				if (rdinfo.getTop() + diff > mViewHeight) {
					break;
				}
				List<WordInfo> wdinfoList = rdinfo.getWordList();
				int num = 0;
				for(WordInfo wdinfo:wdinfoList) {
					if (i == mLineNo) {
						num++;
					}
					boolean isClicked = wdinfo.isClicked() && (state != WordInfo.MOVE_STATE || num != mLastWordNo || isSentenceOver);
					Paint pt = isClicked ? mCurPaint : mPaint;
				    canvas.drawText(rdinfo.getLyric(), wdinfo.mStartIndex, wdinfo.mEndIndex, wdinfo.mXpos, wdinfo.mYpos + diff, pt);
				}
			}
			
			// 渲染已经点击过的歌词。
			for (int i = mLineNo - 1; i >= 0; i--) {
				RenderInfoLyricMaker rdinfo = mRenderInfoList.get(i);
				if (rdinfo.getTop() + rdinfo.getHeight() + diff < 0) {
					break;
				}
				List<WordInfo> wdinfoList = rdinfo.getWordList();
				for(WordInfo wdinfo:wdinfoList) {
				    canvas.drawText(rdinfo.getLyric(), wdinfo.mStartIndex, wdinfo.mEndIndex, wdinfo.mXpos, wdinfo.mYpos + diff, mRenderPaint);
				}
			}
		} finally {
			if (canvas != null) {
				mSurfaceHolder.unlockCanvasAndPost(canvas);
			}
		}
	}
	
	private void renderWhenMove(int xPos) {
		Rect dstrc = new Rect(mDstRect);
		dstrc.right = xPos;
		
		Rect srcrc = new Rect();
		srcrc.left = 0;
		srcrc.top = 0;
		srcrc.right = dstrc.width();
		srcrc.bottom = dstrc.height();
		
		Canvas canvas = null;
		
		try {
		    canvas = mSurfaceHolder.lockCanvas(dstrc);
		    
		    canvas.drawColor(Color.TRANSPARENT,Mode.CLEAR);
		    canvas.drawBitmap(mCurBmp, srcrc, dstrc, mRenderPaint);
		} finally {
			if (canvas != null) {
				mSurfaceHolder.unlockCanvasAndPost(canvas);
			}
		}
	}
	
	private void getRectAndBmp() {
		RenderInfoLyricMaker rdinfo = mRenderInfoList.get(mLineNo);
		List<WordInfo> wdinfoList = rdinfo.getWordList();
		WordInfo wdinfo = wdinfoList.get(mLastWordNo);
		String lineLyrics = rdinfo.getLyric();
		mCurWord = lineLyrics.substring(wdinfo.getStartIndex(),wdinfo.getEndIndex());
		int top = (mViewHeight >> 1) - mLineDiff;
		int diff = 0;
		if (mLineNo == mRenderInfoList.size()) {
			diff = top - mRenderInfoList.get(mLastLineNo).getTop() - mRenderInfoList.get(mLastLineNo).getHeight();
		} else {
		    diff = top - mRenderInfoList.get(mLineNo).getTop();
		}
		
		Rect rc = new Rect();
		mCurPaint.getTextBounds(mCurWord, 0, mCurWord.length(), rc);
		int width = (int) mRenderPaint.measureText(mCurWord);
		mCurBmp = Bitmap.createBitmap(width, rc.height(),  Config.ARGB_8888);
		Canvas canvas = new Canvas(mCurBmp);
		canvas.drawText(mCurWord, 0, 0-rc.top, mCurPaint);//基线对齐
		
		mDstRect.left = wdinfo.getXpos();
		mDstRect.right = mDstRect.left + width;
		mDstRect.top = wdinfo.mYpos + diff + rc.top;
		mDstRect.bottom = mDstRect.top + rc.height();
	
	}

	public void setLyricList(List<String> list) {
		mLyricList = list;
	}
	
	public void pause() {
		mIsPaused = true;
	}
	
	public void resume() {
		synchronized(this) {
    		mIsPaused = false;
    	    notify();
    	}
	}
	
	public void stop() {
		mIsRunning = false;
	}

	public void onDownTouchEvent(int xPos, int yPos,int position,int state) {
		if (mRenderInfoList == null || mLineNo >= mRenderInfoList.size()) {
			return;
		}
		
		RenderInfoLyricMaker rdinfo = mRenderInfoList.get(mLineNo);
		List<WordInfo> wdinfoList = rdinfo.getWordList();
		Rect rc = new Rect();
		mPaint.getTextBounds(rdinfo.getLyric(),0, rdinfo.getLyric().length(), rc);
		int height = rc.height() + + GenerateRenderInfo.LINESPACE;
		
		int endIndex = findNextLine(wdinfoList,mLastWordNo);
		
		endIndex = endIndex == -1 ? wdinfoList.size() : endIndex;
		int j = mLastWordNo;
		for(;j < endIndex;j++) {
			WordInfo wdinfo = wdinfoList.get(j);
			if (wdinfo.getXpos() > xPos) {
				break;
			}
		}
		if (/*j == mLastWordNo && */ state == WordInfo.MOVE_STATE && xPos > mLastXPos) {//
			if (xPos > mDstRect.left && xPos < mDstRect.right) {
				renderWhenMove(xPos);
				mLastXPos = xPos;
			}
		}
		
		if ((mLastLineNo == mLineNo && j <= mLastWordNo) || mIsLastWord) { // 已经点击过
			return;
		}
		
		isSentenceOver = false;
		mLastXPos = xPos;
		for (int m = mLastWordNo; m < j; m++) {
			wdinfoList.get(m).setClicked(true);
		}
		
		msgWhat = 0;
		handler.removeCallbacks(update);
		handler.post(update);
		
		// 设置使当前文字生效的是点击时间还是滑动事件
		wdinfoList.get(j - 1).mEventState = state;
		writeWordTime(mLineNo,mLastWordNo,j,position);
		isPressed = true;
		int temp = mLastWordNo;
		if (j == wdinfoList.size()) {
			isPressed = false;
			mLastLineOfCurSentence = 0;
			mIsLastWord = true;
			mLastLineNo = mLineNo;
			isSentenceOver = true;
//			writeSentence(position);
			IMediaEventService mediaEventService = ServiceManager.getMediaEventService();
			MediaEventArgs args = new MediaEventArgs();
			mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_LYRIC_LINE_OVER));
		} else if (j != endIndex) {
			mLastLineNo = mLineNo;
			if ((endIndex - mFirstWord) > 0) {
				mLineDiff += (j- mLastWordNo) * height / (endIndex - mFirstWord);
			}
			getRectAndBmp();
			mLastWordNo = j;
		} else {
			getRectAndBmp();
			mLastWordNo = j;
			mIsLastWord = true;
		}
		
		
	    doRender(state);
	    if (j != wdinfoList.size() && j == endIndex) {
	    	mLastWordNo = temp;
	    }
	}
	
	/**
	 * 将当前一次点击的时间写入文字数据结构中当作起始时间
	 * @param lineNo 歌词索引
	 * @param start 起始单词
	 * @param end 结束单词
	 * @param position 点击时的媒体时间
	 */
	private void writeWordTime(int lineNo,int start,int end,int position) {
		RenderInfoLyricMaker rdinfo = mRenderInfoList.get(lineNo);
		List<WordInfo> wordIfList = rdinfo.getWordList();
		if (end - start == 1) { //当前字之前没有没有点过的字了，即没有跳字，则当前媒体时间为点击字的起始时间。
			wordIfList.get(start).setStartTime(position); 
		} else if (start != 0){ //前面还有字未曾点击，前面也有字已经点击过，那么将这些文字做平滑处理，即时间间隔一样
			int startTime = (int) wordIfList.get(start - 1).getStartTime();
			int stepTime = (position - startTime) / (end - start);
			int num = 0;
			for (int i = start;i < end;i++) {
			    wordIfList.get(i).setStartTime(startTime + (++num) * stepTime);
			}
		} else { //前面所有字都未曾点击
			if (lineNo != 0) {
				RenderInfoLyricMaker rdinfo1 = mRenderInfoList.get(lineNo - 1);
				int startTime = (int) (rdinfo1.getSentence().getEndTime() + 100);
				if (startTime > position) {
					startTime = position - 50;
				}
				
				int stepTime = (position - startTime) / (end - start);
				int num = 0;
				for (int i = start;i < end;i++) {
				    wordIfList.get(i).setStartTime(startTime + (++num) * stepTime);
				}
			} else {
				int startTime = 100;
				
				int stepTime = (position - startTime) / (end - start);
				int num = 0;
				for (int i = start;i < end;i++) {
				    wordIfList.get(i).setStartTime(startTime + (++num) * stepTime);
				}
			}
		}
	}
	
	/**
	 * 查找下一行的位置
	 * @param list 存储一句歌词每个单词信息的列表
	 * @param start 开始的索引
	 * @return 返回从start开心下一句歌词的索引
	 */
	private int findNextLine(List<WordInfo> list,int start) {
		if (start < 0 || start >= list.size()) {
			return -1;
		}
		int top = list.get(start).getYpos();
		int i = start;
		for(;i < list.size();i++) {
			if(list.get(i).getYpos() != top) {
				return i;
			}
		}
		return -1;
	}
	
	public void onUpTouchEvent(int position) {
		if (mLineNo >= mRenderInfoList.size()) {
			return;
		}
		RenderInfoLyricMaker rdinfo = mRenderInfoList.get(mLineNo);
		List<WordInfo> wdinfoList = rdinfo.getWordList();
		Rect rc = new Rect();
		mPaint.getTextBounds(rdinfo.getLyric(),0, rdinfo.getLyric().length(), rc);
		int height = rc.height();
		
		
        int endIndex = findNextLine(wdinfoList,mLastWordNo);
		endIndex = endIndex == -1 ? wdinfoList.size() : endIndex;
		if (wdinfoList.get(wdinfoList.size() - 1).isClicked()) {
			writeSentence(position);
			mLastLineOfCurSentence = 0;
			mLastWordNo = 0;
			mLastLineNo = mLineNo;
			mLineNo++;
			mFirstWord = 0;
			mIsLastWord = false;
			mLineDiff = 0;
			
			if (mLineNo == mRenderInfoList.size()) {
				IMediaEventService mediaEventService = ServiceManager.getMediaEventService();
				MediaEventArgs args = new MediaEventArgs();
				mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_LYRIC_MAKE_OVER));
				isLyricMakeOver = true;
				isPressed = false;
			}
		} else if (wdinfoList.get(endIndex - 1).isClicked()) {
			mLineDiff = (height + GenerateRenderInfo.LINESPACE) * (mLastLineOfCurSentence + 1);
			mLastLineOfCurSentence++;
			mLastWordNo = endIndex;
			mLastLineNo = mLineNo;
			mFirstWord = endIndex;
			mIsLastWord = false;
			mDstRect.setEmpty();
		}
		
		doRender(WordInfo.IDLE_STATE);
	}
	
	private void writeSentence(int position) {
		RenderInfoLyricMaker rdinfo = mRenderInfoList.get(mLastLineNo);
		List<WordInfo> wordIfList = rdinfo.getWordList();
		int startTime = (int) wordIfList.get(0).getStartTime();
		int endTime = position;
		int curIndex = rdinfo.getLineNo();
		String content = rdinfo.getLyric();
		Sentence stc = new Sentence(content,startTime,endTime,curIndex);
		int wordListSize = wordIfList.size();
		wordIfList.get(wordListSize - 1).setStartTime(position);
		ArrayList<Integer> arrayList = new ArrayList<Integer>();
		for (int i = 1; i < wordListSize; i++) {
			int time = (int) (wordIfList.get(i).getStartTime() - wordIfList.get(i - 1).getStartTime());
			arrayList.add(time);
		}
		
//		arrayList.add((int) (endTime - wordIfList.get(wordListSize - 1).getStartTime()));
		stc.setIntervalList(arrayList);
		rdinfo.setSentence(stc);
	}
	
	private String writeLyricMakerName() {
		if (lyricMakerName == null || "".equals(lyricMakerName)) {
			return "";
		}
		RenderInfoLyricMaker rdinfo = mRenderInfoList.get(0);
		Sentence stc = rdinfo.getSentence();
		int startTime = (int) stc.getStartTime();
		String lyricAuthor = ServiceManager.getAmtMedia().getString(R.string.screen_audio_lyric_maker_name) + lyricMakerName;
		
		GenerateRenderInfo renderList = new GenerateRenderInfo(mViewWidth,mViewHeight,mLyricList);
		RenderInfoLyricMaker rdinfotmp = new RenderInfoLyricMaker(0,lyricAuthor);
		renderList.getRenderInfo(rdinfotmp);
		List<WordInfo> wordIfList = rdinfotmp.getWordList();
		int lyricLen = wordIfList.size();
		
		int stime = 0;
		int etime = 0;
		ArrayList<Integer> arrayList = new ArrayList<Integer>();
		if (startTime < 1000) {
			stime = 0;
			etime = startTime;
			
		} else if (startTime < 5000) {
			stime = 1000;
			etime = startTime;
		} else {
			stime = 3000;
			etime = startTime;
		}
		int step = (etime - stime) / lyricLen;
		
		for (int i = 0; i < lyricLen; i++) {
			int time = step;
			arrayList.add(time);
		}
		
		String lineoutStr = "karaoke.add('";
		lineoutStr += formatTime(stime);
		lineoutStr += "', '";
		lineoutStr += formatTime(etime);
		lineoutStr += "', '";
		lineoutStr += lyricAuthor;
		lineoutStr += "', '";
		
		for (int k = 0; k < lyricLen; k++) {
			lineoutStr += arrayList.get(k);
			if (k != lyricLen -1) {
				lineoutStr += ",";
			}
		}
		lineoutStr += "');\n";
		return lineoutStr;
	}
	
	public void writeKscFile() {
		String outStr = new String();
		outStr += "karaoke := CreateKaraokeObject\n";
		outStr += "karaoke.rows := 2;\n";
		outStr += "karaoke.clear;\n";
		outStr += "karaoke.songname :='" + mLyricView.getSongName() + "';\n";
		outStr += "karaoke.singer :='" + mLyricView.getSinger() + "';\n";
		if (lyricMakerName != null && !"".equals(lyricMakerName)) {
			outStr += "karaoke.lyricmaker :='" + lyricMakerName + "';\n";
		}
		
		outStr += "karaoke.duration :='" + mLyricView.getDuration() + "';\n\n\n";
		lyricMakerOver = "";
		lyricMakerOver += outStr;
		int listSize = mRenderInfoList.size();
		if (listSize <= 0) {
			return;
		}
		String lyricAuthor = writeLyricMakerName();
	    outStr += lyricAuthor;
	    lyricMakerOver += lyricAuthor;
		String lineoutStr = "";
		for (int i = 0;i < listSize; i++) {
			lineoutStr = "karaoke.add('";
			RenderInfoLyricMaker rdinfo = mRenderInfoList.get(i);
			Sentence stc = rdinfo.getSentence();
			int startTime = (int) stc.getStartTime();
			lineoutStr += formatTime(startTime);
			lineoutStr += "', '";
			lineoutStr += formatTime((int) stc.getEndTime());
			lineoutStr += "', '";
			if (stc.getContent().length() <= 0) {
				lineoutStr += "";
			} else {
			    lineoutStr += stc.getContent().substring(0,stc.getContent().length() - 1);
			}
			lineoutStr += "', '";
			ArrayList<Integer> arrayList = stc.getIntervalList();
			int arrayListSize = arrayList.size();
			for (int k = 0; k < arrayListSize; k++) {
				lineoutStr += arrayList.get(k);
				if (k != arrayListSize -1) {
					lineoutStr += ",";
				}
			}
			lineoutStr += "');";
			String evStateStr = "";
			List<WordInfo> wdList = rdinfo.getWordList();
			int wdListSize = wdList.size();
			for (int m = 0; m < wdListSize; m++) {
				evStateStr += wdList.get(m).mEventState ;
				if (m != arrayListSize -1) {
					evStateStr += ",";
				}
				System.out.print(wdList.get(m).mEventState);
			}
			lyricMakerOver += lineoutStr + evStateStr;
			outStr += lineoutStr + "\n";
			lyricMakerOver += "\n";
		}
		String lyricPath = mLyricView.getLyricPath();
		String txtPath = lyricPath.substring(0, lyricPath.indexOf(".ksc"));
		File lyricFile = new File(lyricPath);
		
		try {
			File kscFile = new File(txtPath);
			if (!kscFile.exists()) {
				kscFile.createNewFile();
			}
			FileWriter kscFileWriter = new FileWriter(kscFile);
			kscFileWriter.write(outStr);
			kscFileWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// 加密，并代替原来的歌词文件
		if (DETool.nativeCreateKsc(txtPath) != -1) {
			if (lyricFile.exists()) {
				lyricFile.delete();
			}
			
			String tmpPath = lyricPath + ".tp";
			File tmpFile = new File(tmpPath);
		    tmpFile.renameTo(new File(lyricPath));
		}
		
	}
	
	public String getLyricOverStr() {
		return lyricMakerOver;
	}
	
	private String formatTime(int times) {
		times /= 1000;
		int minutes = times / 60;
		int seconds = times % 60;
		minutes %= 60;
		int millisecond = times % 1000;
		return String.format("%02d:%02d.%03d", minutes, seconds,millisecond);
	}
	
	public boolean isLyricMakeOver() {
		return isLyricMakeOver;
	}
	
	public void setHandler(Handler handler) {
		this.handler = handler;
	}
	
	private Runnable update = new Runnable() {
		public void run() {
			handler.sendEmptyMessage(msgWhat * 100);
			msgWhat++;
			handler.postDelayed(update, 100);
		}
	};
	
	public void removeRunnable() {
		handler.removeCallbacks(update);
	}
	
	public void startRunnable() {
		if (isPressed) {
			handler.post(update);
		}
	}
	
	public void setLyricMakerName(String name) {
		lyricMakerName = name;
	}
	
	private LyricMakerView mLyricView;
	private int mViewWidth;
	private int mViewHeight;
	private Paint mPaint;
	private Paint mRenderPaint;
	private Paint mCurPaint;
	private SurfaceHolder mSurfaceHolder;
	private boolean mIsRunning;
	private boolean mIsPaused;
	private boolean mIsLastWord = false;
	private List<String> mLyricList;
	private List<RenderInfoLyricMaker> mRenderInfoList;
	private int mFirstWord = 0;
	private int mLineNo; // 标志当前歌词是第几句
	private int mLastLineNo;// 上一次点击是第几句
	private int mLastLineOfCurSentence;// 上一次点击到当前歌词的第几行
	private int mLastWordNo; //上一次渲染到哪个字
	private int mState; //当前状态
	private int mLineDiff;// 一句歌词中单行的高度偏移
	private boolean isSentenceOver = false; //当前一句歌词是否点击完毕
	private final static int IDLE_STATE = 0; // 未初始化状态
	private final static int SCROLL_STATE = 1; //滚动状态
	private boolean isLyricMakeOver = false;
	private Handler handler = null;
	private int msgWhat = 0;
	private boolean isPressed = false;
	private String lyricMakerOver = "";
	private String lyricMakerName = "";
	private int mLastXPos = 0;
	private Rect mDstRect = new Rect();
	private Bitmap mCurBmp;
	private String mCurWord;
	
}
