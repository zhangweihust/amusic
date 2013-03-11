package com.amusic.media.lyric.render;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Paint;
import android.graphics.Rect;

import com.amusic.media.lyric.parser.LyricInfo;
import com.amusic.media.lyric.parser.Sentence;

public class FullLyric2Bmp {
	
	public static final int LEFT   = 0;
	public static final int CENTER = 1;
	public static final int RIGHT  = 2;
	
	private static final int FONTCOLOR   = 0xFFADE5E6;
	private static final int FONTSIZE    = 18;
	
	public final static int LINESPACE = 20;//行间距
	public final static int UPLINE = 5;//上边界
	public final static int BOTTOMLINE = 20;//下边界
	public final static int LEFTLINE = 5;//左边界
	public final static int RIGHTLINE = 5;//右边界
	
	public FullLyric2Bmp(int width) {
		initPaint();
		mWidth = 0;
		mHeight = 0;
		mViewWidth = width;
		mRenderList = new ArrayList<RenderInfo>();
	}
	
	/**
	 * 初始化画笔
	 */
	private void initPaint() {
		mPaint = new Paint();

		mPaint.setColor(FONTCOLOR);
		mPaint.setTextSize(FONTSIZE);
		mPaint.setAntiAlias(true);
	}
	
	/**
	 * 将歌词转换为位图
	 * @return 转换之后的位图
	 */
	public void Convert2Bmp() {
		if (mLyricInfo == null) {
			return;
		}
		
		measureLyric();
	}
	
	public void setTextSize(int size) {
		mPaint.setTextSize(size);
	}
	/**
	 * 测量歌词信息，包括歌名和歌手名以及每一句歌词的宽度和位置等等
	 */
	private void measureLyric() {
		mWidth = 0;
		mHeight = 0;
//		mHeight += UPLINE;
		
		String songName = mLyricInfo.getTitle();
		measureSentence(songName);
		
		String singer = mLyricInfo.getSinger();
		measureSentence(singer);
//		mHeight += LINESPACE;
		
		List<Sentence> stclist = mLyricInfo.getList();
	    int length = stclist.size();
	    int index = 0;
	    while (index < length) {
	    	Sentence sentence = stclist.get(index);
	    	measureSentence(sentence);
	    	index++;
	    }
	    
	    mHeight += BOTTOMLINE;		
	}
	
	/**
	 * 测量歌词信息，并将其保存到一个链表中
	 * @param 歌词信息
	 */
	private void measureSentence(String str) {
		Rect rc = new Rect();
		RenderInfo renderinfo = new RenderInfo();
		mPaint.getTextBounds(str, 0, str.length(), rc);
		int width = (int) mPaint.measureText(str, 0, str.length());
		int height = rc.height();
		renderinfo.setTop(mHeight);
		if (width > mViewWidth) {
			renderinfo.setBreakLine(true);
			List<LineInfo> lineList = addElement(str);
		    renderinfo.setLineList(lineList);
		    width = 0;
		    for (int i = 0; i < lineList.size(); i++) {
		    	if (width < lineList.get(i).getWidth()) {
		    		width = lineList.get(i).getWidth();
		    	}
		    }
		    height = lineList.get(lineList.size() -1).getLineTop() - lineList.get(0).getLineTop() 
		             + lineList.get(lineList.size() -1).getHeight();
		}
				
		renderinfo.setWidth(width);
		renderinfo.setHeight(height);
		renderinfo.setIndex(-1);
		renderinfo.setDuration(0);
		renderinfo.setLyric(str);
		
		if (!renderinfo.isBreakLine()) {
			renderinfo.setBase(mHeight - rc.top);
		    mHeight += rc.height();
		}
		
		mHeight += LINESPACE;
		if (mWidth < width) {
			mWidth = width;
		}
		
		mRenderList.add(renderinfo);
	}
	
	/**
	 * 测量歌词信息，并将其保存到一个链表中
	 * @param 歌词信息
	 */
	private void measureSentence(Sentence stc) {
		Rect rc = new Rect();
		RenderInfo renderinfo = new RenderInfo();
		String str = stc.getContent();
		mPaint.getTextBounds(str, 0, str.length(), rc);		
		int width = (int) mPaint.measureText(str, 0, str.length());
		int height = rc.height();
		renderinfo.setTop(mHeight);	
		
		if (width > mViewWidth) {
			renderinfo.setBreakLine(true);
			List<LineInfo> lineList = addElement(str);
		    renderinfo.setLineList(lineList);
		    int listSize = lineList.size();
		    width = 0;
		    for (int i = 0; i < listSize; i++) {
		    	if (width < lineList.get(i).getWidth()) {
		    		width = lineList.get(i).getWidth();
		    	}
		    }
		    height = lineList.get(listSize -1).getLineTop() - lineList.get(0).getLineTop() + lineList.get(listSize -1).getHeight();
		}
		
		renderinfo.setWidth(width);
		renderinfo.setHeight(height);
		renderinfo.setIndex((int) stc.getCurrentIndex());
		renderinfo.setDuration((int) stc.getDuring());
		renderinfo.setLyric(str);
		
		if (!renderinfo.isBreakLine()) {
			renderinfo.setBase(mHeight - rc.top);
		    mHeight += rc.height();
		}
		mHeight += LINESPACE;
		// 将最长一句的宽度作为大图片的宽度
		if (mWidth < width) {
			mWidth = width;
		}
		mRenderList.add(renderinfo);		
	}
	
	private List<LineInfo> addElement(String lyricStr) {
		List<LineInfo> lineList = new ArrayList<LineInfo>();
		int listSize = lineList.size();

		int startWordIndex = 0;//当前一行开始的文字或单词的游标，对应着后面的渲染时间，即渲染单位
		int endWordIndex = 0;//当前一行的结束的文字或单词的游标。
		
		int COMMON_CHARACTER = 0;
		int CHINESE_CHARACTER = 1;
		int SPACE_CHARACTER = 2;
		int preIndex = 0;

		int preSpaceIndex = 0;
		int preChineseIndex = 0;
		int state = 0;
		int width = 0;
		for (int i = 0; i < lyricStr.length(); i++) {
			if (lyricStr.charAt(i) >= 0 && lyricStr.charAt(i) <= 0xFF && lyricStr.charAt(i) != ' ') {		
				width = (int) mPaint.measureText(lyricStr, preIndex, i + 1);				
				if (width > mViewWidth) {
					if (preSpaceIndex != 0) { // 前面有空格的情况
						LineInfo lineInfo = new LineInfo(startWordIndex,endWordIndex,mHeight);
						String lineStr = lyricStr.substring(preIndex, preSpaceIndex + 1);
						lineInfo.setLineStr(lineStr);
						preIndex = preSpaceIndex + 1;
						preSpaceIndex = 0;
						startWordIndex = endWordIndex;
						
						addLineInfo(lineInfo,lineList,lineStr);
					} else if (preChineseIndex != 0) { //前面有汉字
						LineInfo lineInfo = new LineInfo(startWordIndex,endWordIndex,mHeight);
						String lineStr = lyricStr.substring(preIndex, preChineseIndex + 1);
						lineInfo.setLineStr(lineStr);
						preIndex = preChineseIndex + 1;
						preChineseIndex = 0;
						startWordIndex = endWordIndex;
						
						addLineInfo(lineInfo,lineList,lineStr);
					} else { //前面都是英文
						LineInfo lineInfo = new LineInfo(startWordIndex,endWordIndex+1,mHeight);
						String lineStr = lyricStr.substring(preIndex, i - 1);
						lineInfo.setLineStr(lineStr);
						preIndex = i - 1;
						
						addLineInfo(lineInfo,lineList,lineStr);
					}
				}
				if (state != COMMON_CHARACTER) {
					endWordIndex++;
				}
				state = COMMON_CHARACTER;
			} else if (lyricStr.charAt(i) == ' ') {// 是空格的情况
				state = SPACE_CHARACTER;
				preSpaceIndex = i;
			} else {//是中文(日韩)等
				state = CHINESE_CHARACTER;
				if (i == preIndex) {
					continue;
				}
				width = (int) mPaint.measureText(lyricStr, preIndex, i + 1);
				if (width > mViewWidth) {
					if (preSpaceIndex != 0) { // 前面有空格的情况
						LineInfo lineInfo = new LineInfo(startWordIndex,endWordIndex,mHeight);
						String lineStr = lyricStr.substring(preIndex, preSpaceIndex + 1);
						lineInfo.setLineStr(lineStr);
						preIndex = preSpaceIndex + 1;
						preSpaceIndex = 0;
						startWordIndex = endWordIndex;
						
						addLineInfo(lineInfo,lineList,lineStr);
					} else {
						LineInfo lineInfo = new LineInfo(startWordIndex,endWordIndex + 1,mHeight);
						String lineStr = lyricStr.substring(preIndex, i - 1);
						lineInfo.setLineStr(lineStr);
						preIndex = i - 1;
						
						addLineInfo(lineInfo,lineList,lineStr);
					}
				}
				preChineseIndex = i;
				endWordIndex++;		
			}
		}
		
		if (preIndex <= lyricStr.length() - 1) {
			LineInfo lineInfo = new LineInfo(startWordIndex,listSize,mHeight);
			String lineStr = lyricStr.substring(preIndex, lyricStr.length());
			lineInfo.setLineStr(lineStr);
			
			addLineInfo(lineInfo,lineList,lineStr);	
		}
	
		mHeight -= LINESPACE;
		return lineList;
	}
	
	private void addLineInfo(LineInfo lineIf,List<LineInfo> lineList,String lineStr) {
		Rect rc = new Rect();
		mPaint.getTextBounds(lineStr, 0, lineStr.length(), rc);
		lineIf.setBase(mHeight - rc.top); 
		mHeight += rc.height() + LINESPACE;
		
		int widthTemp = (int) mPaint.measureText(lineStr);
		lineIf.setWidth(widthTemp);
		lineIf.setHeight(rc.height());
		
		lineList.add(lineIf);
	}

	public LyricInfo getLyricInfo() {
		return mLyricInfo;
	}

	public void setLyricInfo(LyricInfo lyricInfo) {
		this.mLyricInfo = lyricInfo;
	}
	
	/**
	 * 得到保存渲染信息的列表，此函数得在调用完Convert2Bmp之后调用才能得到整个链表
	 * @return 渲染信息列表
	 */
	public List<RenderInfo> getRenderList() {
		return mRenderList;
	}
	
	private LyricInfo              mLyricInfo;
	private Paint                mPaint;
	private int                  mWidth;
	private int                  mHeight;
	private List<RenderInfo>     mRenderList;
	private int                  mViewWidth;
	
}

class RenderInfo {
	
	public int getIndex() {
		return mIndex;
	}
	public void setIndex(int index) {
		this.mIndex = index;
	}
	public int getHeight() {
		return mHeight;
	}
	public void setHeight(int height) {
		this.mHeight = height;
	}
	public int getWidth() {
		return mWidth;
	}
	public void setWidth(int width) {
		this.mWidth = width;
	}
	public int getDuration() {
		return mDuration;
	}
	public void setDuration(int duration) {
		this.mDuration = duration;
	}
	public int getTop() {
		return mTop;
	}
	public void setTop(int top) {
		this.mTop = top;
	}
	public int getBase() {
		return mBase;
	}
	public void setBase(int base) {
		this.mBase = base;
	}
	public String getLyric() {
		return mLyric;
	}
	public void setLyric(String lyric) {
		this.mLyric = lyric;
	}
	
	public List<LineInfo> getLineList() {
		return mLineList;
	}
	public void setLineList(List<LineInfo> list) {
		mLineList = list;
	}
	
	public Boolean isBreakLine() {
		return mBreakLine;
	}
	public void setBreakLine(Boolean flag) {
		mBreakLine = flag;
	}
	
	private int                mIndex;
	private int                mHeight;
	private int                mWidth;
	private int                mDuration;
	private int                mTop;
	private int                mBase;
	private String             mLyric;
	private List<LineInfo>     mLineList;
	private Boolean            mBreakLine = false;
	
}

class LineInfo {
	
	public LineInfo(int startindex,int endindex,int top) {
		mStartWordIndex = startindex;
		mEndWordIndex = endindex;
		mLineTop = top;
	}
	
	public int getStartWordIndex() {
		return mStartWordIndex;
	}
	public int getEndWordIndex() {
		return mEndWordIndex;
	}
	public int getLineTop() {
		return mLineTop;
	}
	public void setLineStr(String str) {
		mLineStr = str;
	}
    public String getLineStr() {
    	return mLineStr;
    }
    public void setIsContinue(Boolean iscontinue) {
    	mIsContinue = iscontinue;
    }
    public Boolean getIsContinue() {
    	return mIsContinue;
    }
    public void setWidth(int width) {
    	mWidth = width;
    }
    public int getWidth() {
    	return mWidth;
    }
    public void setHeight(int height) {
    	mHeight = height;
    }
    public int getHeight() {
    	return mHeight;
    }
    public int getBase() {
    	return mBase;
    }
    public void setBase(int base) {
    	this.mBase = base;
    }
	private int         mStartWordIndex;
	private int         mEndWordIndex;
	private int         mLineTop;
	private int         mWidth;
	private int         mHeight;
	private int         mBase;
	private String      mLineStr;
	private Boolean     mIsContinue;
}
