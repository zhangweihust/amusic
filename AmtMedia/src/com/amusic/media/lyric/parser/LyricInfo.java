
package com.amusic.media.lyric.parser;

import java.util.List;


/**
 * KscInfo类是对一个歌词文件的抽象，这个类存储的有 歌词文件的歌曲名、演唱者、专辑名称以及每一行歌词的 起始播放时间与单字持续时间之间的一个映射等信息
 * 
 * @author root
 */
public class LyricInfo {
    
    private String lyrics;// 完整的歌词
    
    private String title;// 歌曲名

    private String singer;// 演唱者

    private String album;// 专辑
    
    private long duration = 0;//歌曲的总时间
    
    private boolean hasLyricMaker = false;

    private List<Sentence> infos;// 保存歌词信息和时间点一一对应的Map

    /**
     * @param infos 歌词信息和时间点的映射
     */
    public void setList(List<Sentence> infos) {
        this.infos = infos;
    }

    /**
     * @param title 歌曲名
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @param singer 演唱者
     */
    public void setSinger(String singer) {
        this.singer = singer;
    }

    /**
     * @param album 专辑
     */
    public void setAlbum(String album) {
        this.album = album;
    }

    /**
     * 获取歌词信息与时间点的Map
     * 
     * @return
     */
    public List<Sentence> getList() {
        return infos;
    }

    /**
     * @param title 歌曲名
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param singer 演唱者
     */
    public String getSinger() {
        return singer;
    }

    /**
     * @param album 专辑
     */
    public String getAlbum() {
        return album;
    }
    
    public void setLyricMaker(boolean flag) {
    	hasLyricMaker = flag;
    }
    
    public boolean hasLyricMaker() {
    	return hasLyricMaker;
    }

    /**
     * 
     * @return
     */
    public String getLyrics() {
        return lyrics;
    }

    /**
     * 
     * @param lyrics
     */
    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }
    
    public long getMusicDuration() {
    	return duration;
    }
    
    public void setMusicDuration(long duration) {
    	this.duration = duration;
    }
}
