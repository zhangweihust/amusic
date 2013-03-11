package com.amusic.media.lyric.render;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

import com.amusic.media.utils.Constant;

/**
* 将单句歌词转换为图片，可以设置歌词颜色，歌词字体大小，获得转换之后的图片。
*
* @version 1.0 8 Dec 2011
* @author jiaming.wang@amusic.com
*/
public class Lyric2Bmp {
	public static final int LEFT   = 0;
	public static final int CENTER = 1;
	public static final int RIGHT  = 2;
	
	public static final int FONTCOLOR   = 0xFFADE5E6;
	public static final int RENDERCOLOR = 0xFF00B4FF;
	private static int FONTSIZE    = 18;
	
	public Lyric2Bmp() {
		initPaint();
	}
	
	/**
	 * 初始化字体画笔和渲染画笔
	 */
	private void initPaint() {
		mFontPaint = new Paint();
		mRenderPaint = new Paint();
		
		mFontColor = Constant.LYRICBACKGROUNDCOLOR;// MediaApplication.col0or_normal;
		mRenderColor = Constant.LYRICFOREGROUNDCOLOR;
		
		mFontPaint.setColor(mFontColor);
		mFontPaint.setTextSize(FONTSIZE);
		mFontPaint.setAntiAlias(true);
		
		mRenderPaint.setColor(mRenderColor);
		mRenderPaint.setTextSize(FONTSIZE);
		mRenderPaint.setAntiAlias(true);
	}
	
	/**
	 * 获得歌词生成图像的Bitmap对象，此对象是未经渲染的对象
	 * @return Bitmap
	 */
	public Bitmap getFontBitmap() {
		Bitmap bmp = Bitmap.createBitmap(mLyricWidth, mLyricHeight,  Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp);
		canvas.drawText(mLyric, 0, 0-mRect.top, mFontPaint);//基线对齐
		return bmp;
	}
	
	/**
	 * 获得渲染之后的Bitmap对象。
	 * @return Bitmap
	 */
	public Bitmap getRenderBitmap() {
		Bitmap bmp = Bitmap.createBitmap(mLyricWidth, mLyricHeight,  Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp);
		canvas.drawText(mLyric, 0, 0-mRect.top, mRenderPaint);//基线对齐
 
		return bmp;
	}
	
	/**
	 * 测量本句歌词子字符串的宽度和宽度
	 * @param startindex 要测量字符串的起始位置
	 * @param endindex 要测量字符串的终止位置
	 * @return 测量出的宽度
	 */
	public int getTextWidth(int startindex,int endindex) {
		return (int) mFontPaint.measureText(mLyric, startindex, endindex);
	}
	
	/**
	 * 在当前画笔下测量任意字符串的宽度
	 * @param str 要测量的字符串
	 * @return 测量字符串的宽度
	 */
	public int getTextWidth(String str){
		return (int) mFontPaint.measureText(str);
	}
	
	/**
	 * 测量任意字符串子串的宽度
	 * @param str 要测量的字符串
	 * @param startindex 起始位置
	 * @param endindex 终止位置
	 * @return 测量出的宽度
	 */
	public int getTextWidth(String str,int startindex,int endindex){
		return (int) mFontPaint.measureText(str, startindex, endindex);
	}
	
	/**
	 * 测量本句歌词生成图片的高度
	 * @return 高度
	 */
	public int getTextHeight(){
	    return mRect.height();	
	}
	
	/**
	 * 设置单句歌词字符串
	 * @param lyric 歌词字符串
	 */
	public void setLyric(String lyric) {
		mLyric = lyric;
		mLyricWidth = (int) mFontPaint.measureText(mLyric);
		mRect = new Rect();
		mFontPaint.getTextBounds(mLyric, 0, mLyric.length(), mRect);
		mLyricHeight = mRect.height();
		if (mLyricHeight == 0) {
			mLyricHeight = 4;
		}
	}
	
	/**
	 * 获取字体颜色
	 * @return 渲染颜色, ARGB
	 */
	public int getFontColor() {
		return mFontColor;
	}
	
	/**
	 * 设置字体颜色，ARGB 如白色不透明（0xffffffff)
	 * @param 字体颜色
	 */
	public void setFontColor(int fontColor) {
		mFontColor = fontColor;
		mFontPaint.setColor(fontColor);
	}
	
	/**
	 * 获取渲染颜色
	 * @return 渲染颜色，ARGB
	 */
	public int getRenderColor() {
		return mRenderColor;
	}
	
	/**
	 * 设置渲染颜色
	 * @param 渲染颜色，ARGB 如白色不透明（0xffffffff)
	 */
	public void setRenderColor(int renderColor) {
		mRenderColor = renderColor;
		mRenderPaint.setColor(renderColor);
	}

	/**
	 * 得到字体大小
	 * @return 字体大小
	 */
	public int getFontSize() {
		return mFontSize;
	}

	/**
	 * 设置字体大小
	 * @param 字体大小
	 */
	public void setFontSize(int fontSize) {
		mFontSize = fontSize;
		mFontPaint.setTextSize(fontSize);
		mRenderPaint.setTextSize(fontSize);
	}
	
	/**
	 * 设置字体
	 * @param 字体名称
	 */
	public void setFontName(String fontName) {
		mFontName = fontName;
		Typeface font = Typeface.create(mFontName, Typeface.NORMAL);
		mFontPaint.setTypeface(font);
		mRenderPaint.setTypeface(font);
	}
	
	public Paint getPaint() {
		return mFontPaint;
	}
	
	public Paint getRenderPaint() {
		return mRenderPaint;
	}
	
	private String mLyric;//一句歌词
	private String mFontName;//字体名称
	private int    mFontSize;//字体大小
	private int    mFontColor;//字体颜色
    private int    mRenderColor;//渲染颜色
    private int    mLyricWidth;//歌词生成图片之后的宽度
    private int    mLyricHeight;//歌词生成图片之后的高度
    private Rect   mRect;//歌词生成图片的边框
    private Paint  mFontPaint;//绘制歌词画笔
    private Paint  mRenderPaint;//渲染画笔
}
