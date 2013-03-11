
package com.amusic.media.lyric.parser;

import java.util.ArrayList;


abstract public class LyricParser {
    protected int parserMode;
    protected String path;
    protected String name;
    protected String lycris;

    ArrayList<String> toppings = new ArrayList<String>();

    public int getParserMode() {
        return parserMode;
    }

    public String getPath() {
        return path;
    }

    public String getLyrcis() {
        return lycris;
    }

    public String getName() {
        return name;
    }

    /**
     * 将时钟转换为字符型
     */
    public String toString() {
        // code to display content;
        StringBuffer display = new StringBuffer();
        String sParserMode = Integer.toString(parserMode);
        display.append("---- " + name + " ----\n");
        display.append(sParserMode + "\n");
        display.append(path + "\n");
        display.append(name + "\n");
        display.append(lycris + "\n");
        for (int i = 0; i < toppings.size(); i++) {
            display.append((String)toppings.get(i) + "\n");
        }
        return display.toString();
    }

    /**
     * 根据输入的文件路径，解析这个文件，并返回一个 保存有歌词文件信息的KscInfo对象
     * 
     * @param path 路径
     * @return
     * @throws Exception
     */
    public abstract LyricInfo parser() throws Exception;

    /**
     * 得到当前正在播放的那一句的下标 不可能找不到，因为最开头要加一句 自己的句子 ，所以加了以后就不可能找不到了
     * 
     * @return 下标
     */
    public abstract int getNowSentenceIndex(long time);

    /**
     * @param time 当前歌词的时间轴
     * @return null
     */
    public abstract int updateIndex(long time);
    
    public abstract int updateIndex(long time,boolean isSeek);
    
    /**
     * Adjust the time offset of a single sentence lyrics
     * 
     * @param index the index of current and later lyrics
     * @param offsetTime the offsetTime is in milliseconds
     * @return
     */
    public abstract String adjustCurrentSentence( int index, long offsetTime );
    
    /**
     * Adjusted the time offset from the current index 
     * to the end about lyrics
     * 
     * @param offsetTime the offsetTime is in milliseconds
     * @return
     */
    public abstract String adjustFromCurrent( int index, long offsetTime );
    
    /**
     * Adjusted the time offset to the current index 
     * to the end about lyrics
     * 
     * @param offsetTime the offsetTime is in milliseconds
     * @return
     */
    public abstract String adjustToCurrent( int index, long offsetTime );
    
    /**
     * Adjust the time offset of all lyrics
     * 
     * @param offsetTime the offsetTime is in milliseconds
     * @return
     */
    public abstract String adjustAll( long offsetTime );
    
    /**
     * 清除列表
     */
    public abstract void clearList();
}
