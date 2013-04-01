package com.android.media.model;

import java.util.Comparator;

public class SongInfo{
	private String songName;
	private String singerName;
	private String directory;
    
	private String duration;
	private String date;
	private int times;
	public SongInfo(){
		
	}
	public SongInfo(String songName, String singerName, String directory) {
		this.songName = songName;
		this.singerName = singerName;
		this.directory = directory;
	}
	public SongInfo(String songName, String singerName, int times) {
		this.songName = songName;
		this.singerName = singerName;
		this.times = times;
	}
	public String getSongName() {
		return songName;
	}
	public void setSongName(String songName) {
		this.songName = songName;
	}
	public String getSingerName() {
		return singerName;
	}
	public void setSingerName(String singerName) {
		this.singerName = singerName;
	}
	public String getDirectory() {
		return directory;
	}
	public void setDirectory(String directory) {
		this.directory = directory;
	}
	
	public void setDuration(String duration){
		this.duration = duration;
	}
	public String getDuration(){
		return duration;
	}
	
	public void setDate(String date){
		this.date = date;
	}
	public String getDate(){
		return date;
	}
	public int getTimes() {
		return times;
	}
	public void setTimes(int times) {
		this.times = times;
	}
}
