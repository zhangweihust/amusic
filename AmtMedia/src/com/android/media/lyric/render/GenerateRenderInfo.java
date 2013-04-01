package com.android.media.lyric.render;

import java.util.ArrayList;
import java.util.List;

import com.amusic.media.R;
import com.android.media.lyric.parser.Sentence;
import com.android.media.services.impl.ServiceManager;

import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Rect;

public class GenerateRenderInfo {
	
	private int mWidth; // 屏幕宽度
	private int mHeight; // 屏幕高度
    private List<String> mLyrics; //歌词链表
    private Paint mPaint; //画笔
    private int  mTop; // 一行歌词的top
    public final static int LINESPACE = 50;
    public final static int KERNING = 20;
    
	public GenerateRenderInfo(int width, int height,List<String> lyrics) {
		Resources res = ServiceManager.getAmtMedia().getResources();
	    int fontSize = (int) res.getDimension(R.dimen.lyric_make_fontSize);
		mWidth = width;
		mHeight = height;
		mLyrics = lyrics;
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setTextSize(fontSize);
	}
	
	public List<RenderInfoLyricMaker> generateList() {
		List<RenderInfoLyricMaker> rdinfoList = new ArrayList<RenderInfoLyricMaker>();
		for (int i = 0; i < mLyrics.size(); i++) {
			String lyricStr = mLyrics.get(i);
			lyricStr += "↙";
			RenderInfoLyricMaker rdinfo = new RenderInfoLyricMaker(i,lyricStr);
			getRenderInfo(rdinfo);
			rdinfoList.add(rdinfo);
		}
		return rdinfoList;
	}
	
	public void getRenderInfo(RenderInfoLyricMaker rdinfo) {
		String lyricStr = rdinfo.getLyric();
		int COMMON_CHARACTER = 0;
		int CHINESE_CHARACTER = 1;
		int SPACE_CHARACTER = 2;
		int preIndex = 0;
		int totalWidth = 0;
		int state = 0;
		int lineheight = 0;
		int linewidth = 0;
		boolean isBreak = false;
		List<WordInfo> wdinfoList = new ArrayList<WordInfo>();
		Rect rc = new Rect();
		mPaint.getTextBounds(lyricStr, 0, lyricStr.length(), rc);
		lineheight = rc.height();
		rdinfo.setTop(mTop);
		mTop -= rc.top;
		
		for (int i = 0; i < lyricStr.length(); i++) {
			if (lyricStr.charAt(i) >= 0 && lyricStr.charAt(i) <= 0xFF && lyricStr.charAt(i) != ' ') {
				// 前面不是普通英文字赋，则分割
				if (state != COMMON_CHARACTER) {
					int width = (int) mPaint.measureText(lyricStr, preIndex, i);
					totalWidth += width + KERNING;
					if (totalWidth > mWidth) {
						if (totalWidth - width - KERNING > linewidth) {
							linewidth = totalWidth - width - KERNING;
						}
						isBreak = true;
						totalWidth = width + KERNING;
						mTop += rc.height() + LINESPACE;
						lineheight += rc.height() + LINESPACE;
					}
					WordInfo wdinfo = new WordInfo(preIndex,i,totalWidth - width - KERNING,mTop);
					
					preIndex = i;
					
					wdinfoList.add(wdinfo);
				}
				state = COMMON_CHARACTER;
			} else if (lyricStr.charAt(i) == ' '||lyricStr.charAt(i) == '　') {// 是空格的情况，中文空格或英文空格
				state = SPACE_CHARACTER;
			} else {// 是中文(日韩)等
				state = CHINESE_CHARACTER;
				if (i == 0) { 
					continue;
				}
				int width = (int) mPaint.measureText(lyricStr, preIndex, i);
				totalWidth += width + KERNING;
				if (totalWidth > mWidth) {
					if (totalWidth - width - KERNING > linewidth) {
						linewidth = totalWidth - width - KERNING;
					}
					isBreak = true;
					totalWidth = width + KERNING;
					mTop += rc.height() + LINESPACE;
					lineheight += rc.height() + LINESPACE;
				}
				WordInfo wdinfo = new WordInfo(preIndex,i,totalWidth - width - KERNING,mTop);

				preIndex = i;
				
				wdinfoList.add(wdinfo);	
			}		
		}
		if (preIndex <= lyricStr.length() - 1) {
			int width = (int) mPaint.measureText(lyricStr, preIndex, lyricStr.length());
			totalWidth += width + KERNING;
			if (totalWidth > mWidth) {
				if (totalWidth - width - KERNING > linewidth) {
					linewidth = totalWidth - width - KERNING;
				}
				isBreak = true;
				totalWidth = width + KERNING;
				mTop += rc.height() + LINESPACE;
				lineheight += rc.height() + LINESPACE;
			}
			WordInfo wdinfo = new WordInfo(preIndex,lyricStr.length(),totalWidth - width - KERNING,mTop);
			
			wdinfoList.add(wdinfo);	
		}
		if (!isBreak) {
			linewidth = totalWidth;
		}
		rdinfo.setBreak(isBreak);
		rdinfo.setWidth(linewidth);
		rdinfo.setHeight(lineheight);
		
		rdinfo.setWordList(wdinfoList);
		mTop += rc.height() + LINESPACE + rc.top;
	}

	public void setTextSize(int size) {
		mPaint.setTextSize(size);
	}

}

class RenderInfoLyricMaker {
	private int mLineNo; // 歌词的列号
	private int mWidth;  // 这行歌词的宽度
	private int mHeight; // 歌词的高度
	private int mTop;    // 歌词top对应的位置
	private String mLyric = null;  // 歌词字符串
	private boolean isBreak = false; // 是否歌词过长需要多行显示
	private float [] mPos = null; // 歌词中每个字对应的位置，x,y
	private List<WordInfo> mWordList = null; // 歌词中每个字的信息。
	private Sentence mSentence = null;
	
	public RenderInfoLyricMaker(int lineno, String lyric) {
		mLineNo = lineno;
		mLyric = lyric;
	}
	
	public void setWidth(int width) {
		mWidth = width;
	}
	
	public void setHeight(int height) {
		mHeight = height;
	}
	
	
	public int getWidth() {
		return mWidth;
	}
	
	public int getHeight() {
		return mHeight;
	}
	
	public int getLineNo() {
		return mLineNo;
	}
	
	public void setWordList(List<WordInfo> wordlist) {
		mWordList = wordlist;
	}
	
	public List<WordInfo> getWordList() {
		return mWordList;
	}
	
	public String getLyric() {
		return mLyric;
	}
	
	public void setTop(int top) {
		mTop = top;
	}
	
	public int getTop() {
		return mTop;
	}
	
	public void setBreak(boolean flag) {
		isBreak = flag;
	}
	
	public boolean isBreak() {
		return isBreak;
	}
	
	public void setPos(float [] pos) {
		mPos = pos;
	}
	
	public float [] getPos() {
		return mPos;
	}
	
	public Sentence getSentence() {
		return mSentence;
	}
	
	public void setSentence(Sentence stc) {
		mSentence = stc;
	}
}

class WordInfo {
	public int mStartIndex;
	public int mEndIndex;
	public int mXpos;
	public int mYpos;
	private boolean mIsClicked = false;
	private long mStartTime = 0;
	public int mEventState = 0;
	public static final int IDLE_STATE = 0;
	public static final int DOWN_STATE = 1;
	public static final int MOVE_STATE = 2;
	
	
	public WordInfo(int start, int end, int xpos, int ypos) {
		mStartIndex = start;
		mEndIndex = end;
		mXpos = xpos;
		mYpos = ypos;
	}
	
	public int getStartIndex() {
		return mStartIndex;
	}
	
	public int getEndIndex() {
		return mEndIndex;
	}
	
	public int getXpos() {
		return mXpos;
	}
	
	public int getYpos() {
		return mYpos;
	}
	
	public void setClicked(boolean isClicked) {
		mIsClicked = isClicked;
	}
	
	public boolean isClicked() {
		return mIsClicked;
	}
	
	public void setStartTime(long startTime) {
		mStartTime = startTime;
	}
	
	public long getStartTime() {
		return mStartTime;
	}
}
