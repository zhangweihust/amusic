package com.amusic.media.model;


public class MenuItem {
	private int resId;
	private String resName;
	
	public MenuItem(){
		
	}
	public MenuItem(int resId,String resName){
		this.resId=resId;
		this.resName=resName;
	}
	public void setResId(int resId) {
		this.resId = resId;
	}
	
	public int getResId(){
		return this.resId;
	}
	public void setResName(String resName) {
		this.resName = resName;
	}
	
	public String getResName(){
		return this.resName;
	}
}
