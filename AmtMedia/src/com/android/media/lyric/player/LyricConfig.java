package com.android.media.lyric.player;

public class LyricConfig {
    
    // 歌词解析模式
    public static final int paserWithPath   = 0;
    public static final int paserWithString = 1;
    
    // lyric player的同步线程的间隔频率
    // 单位为毫秒
    public static final long lyricPlayerRunInterval = 50;
    
    // 渲染视图的配置
    private int viewW = 0;
    private int viewH = 0;
   
    public int getViewW() {
        return viewW;
    }

    public void setViewW(int viewW) {
        this.viewW = viewW;
    }

    public int getViewH() {
        return viewH;
    }

    public void setViewH(int viewH) {
        this.viewH = viewH;
    }
}
