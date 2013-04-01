
package com.android.media.lyric.parser;

import java.util.ArrayList;

/**
 * LineInfo类主要用着对歌词文件中每一行歌词中的歌词内容 和每个字的持续时间等信息进行一个封装，从而使其可以作为
 * HashMap的值被传入，由于interval字段并没有对每个单字 的时间间隔进行分隔，所以在使用该字段时可以使用String类
 * 的split函数对其进行分割，从而提取每个单字的持续时间
 * 
 * @author root
 */
public class Sentence {
    
    // To adjust the offset time 
    public int whichLine = 0;
    
    public int currentIndex = 0;

    private long currentTime = 0;

    private long startTime = 0;

    private long endTime = 0;

    private String content = null;// 该行歌词的内容，形如：若活到十億晚

    private String interval = null;// 该行歌词中每个字的持续时间，形如：600,200,300,300,700,500

    private ArrayList<Integer> arrayListInterval;

    public Sentence(String content, long startTime, long endTime, String interval, int index, int whichLine) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.content = content;
        this.interval = interval;
        this.currentIndex = index;
        this.whichLine = whichLine;

        // 解析每一行中的每一个字的渲染时间；
        arrayListInterval = new ArrayList<Integer>();
        addIntervalToArray(this.interval);
    }

    public Sentence(String content2, int startTime2, int endTime2, int curIndex) {
    	this.startTime = startTime2;
		this.endTime = endTime2;
		this.content = content2;
		this.currentIndex = curIndex;
	}

	/**
     * 获得目前的歌词是哪一行。
     * 
     * @return
     */
    public int getwhichLine() {
        return whichLine;
    }
    
    /**
     * 获得当前的时间
     * 
     * @return
     */
    public int getCurrentIndex() {
        return currentIndex;
    }

    /**
     * 获得当前的时间
     * 
     * @return
     */
    public long getCurrentTime() {
        return currentTime;
    }

    /**
     * 获得开始的时间
     * 
     * @return
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * 获得结束的时间
     * 
     * @return
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * 获取该行歌词的内容
     * 
     * @return
     */
    public String getContent() {
        return content;
    }

    /**
     * 获取该行歌词中的单字持续时间的链表
     * 
     * @return
     */
    public String getInterval() {
        return interval;
    }

    /**
     * 获取该行歌词中的单字持续时间的链表
     * 
     * @return
     */
    public ArrayList<Integer> getIntervalList() {
        return arrayListInterval;
    }

    /**
     * 得到这个句子的时间长度,毫秒为单位
     * 
     * @return 长度
     */
    public long getDuring() {
        return endTime - startTime;
    }

    /**
     * 设置当前的时间
     * 
     * @return
     */
    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }

    /**
     * 获得开始的时间
     * 
     * @return
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * 获得结束的时间
     * 
     * @return
     */
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    /**
     * 设置该行歌词的内容
     * 
     * @param content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 设置该行歌词的单字持续时间
     * 
     * @param intervalList
     */
    public void setInterval(String interval) {
        this.interval = interval;
    }

    /**
     * 检查某个时间是否包含在某句中间
     * 
     * @param time 时间
     * @return 是否包含了
     */
    public boolean isInTime(long time) {
        return time >= startTime && time <= endTime;
    }

    /**
     * 将解析整句歌词分成每一个进行存放
     * 
     * @param content 整句的歌词
     */
    private void addIntervalToArray(String interval) {
        // arrayInterval
        String[] timeStr = interval.split(",");
        int num = 0;
        for (int i = 0; i < timeStr.length; i++) {
            int time = Integer.parseInt(timeStr[num++]);
            arrayListInterval.add(time);
        }
    }

	public void setIntervalList(ArrayList<Integer> arrayList) {
		// TODO Auto-generated method stub
		arrayListInterval = arrayList;
	}
}
