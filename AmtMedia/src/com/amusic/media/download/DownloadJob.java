package com.amusic.media.download;

import com.amusic.media.event.IMediaEventArgs;
import com.amusic.media.services.IMediaService;
import com.amusic.media.task.DownloadTask;

public class DownloadJob {
	private boolean rebegin;
	private String song;
	private String singer;
	private String path;
	private String finalPath;
	private Integer downloadId;
	private IMediaEventArgs args;
	private long downloadStartPos;
	private int downloadType;
	private int downloadResource;
	private String downloadUrl;
	private long downloadTotalSize;
	private int downloadStateFlag;
	private DownloadTask downloadTask;
	private int versionCode;
	private String cookie;    //5sing的cookie，登录下载用
	
	public DownloadJob() {
	}
	
	public DownloadJob(IMediaEventArgs args) {
		this.args = args;
	}

	public DownloadJob(IMediaEventArgs args, boolean rebegin) {
		this.args = args;
		this.rebegin = rebegin;
		this.singer = (String) args.getExtra("singer");
		this.song = (String) args.getExtra("song");
		this.path = (String) args.getExtra("path");
		this.downloadId = (Integer) args.getExtra("downloadId");
		this.downloadType = (Integer) args.getExtra("downloadType");
		this.downloadTotalSize = (Long) args.getExtra("size");
		this.downloadUrl = (String) args.getExtra("url");
		this.downloadStateFlag = IMediaService.STATE_BEGIN;
		this.downloadResource = (Integer) args.getExtra("resource");
	}

	
	public String getCookie() {
		return cookie;
	}

	public void setCookie(String szcookie) {
		this.cookie = szcookie;
	}


	public String getFinalPath() {
		return finalPath;
	}

	public void setFinalPath(String finalPath) {
		this.finalPath = finalPath;
	}

	public int getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

	public DownloadTask getDownloadTask() {
		return downloadTask;
	}

	public void setDownloadTask(DownloadTask downloadTask) {
		this.downloadTask = downloadTask;
	}

	public int getDownloadStateFlag() {
		return downloadStateFlag;
	}

	public void setDownloadStateFlag(int downloadStateFlag) {
		this.downloadStateFlag = downloadStateFlag;
	}

	public long getDownloadStartPos() {
		return downloadStartPos;
	}

	public void setDownloadStartPos(long downloadStartPos) {
		this.downloadStartPos = downloadStartPos;
	}

	public int getDownloadType() {
		return downloadType;
	}

	public void setDownloadType(int downloadType) {
		this.downloadType = downloadType;
	}

	public int getDownloadResource() {
		return downloadResource;
	}

	public void setDownloadResource(int downloadResource) {
		this.downloadResource = downloadResource;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public long getDownloadTotalSize() {
		return downloadTotalSize;
	}

	public void setDownloadTotalSize(long downloadTotalSize) {
		this.downloadTotalSize = downloadTotalSize;
	}

	public boolean isRebegin() {
		return rebegin;
	}

	public void setRebegin(boolean rebegin) {
		this.rebegin = rebegin;
	}

	public String getSong() {
		return song;
	}

	public void setSong(String song) {
		this.song = song;
	}

	public String getSinger() {
		return singer;
	}

	public void setSinger(String singer) {
		this.singer = singer;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Integer getDownloadId() {
		return downloadId;
	}

	public void setDownloadId(Integer downloadId) {
		this.downloadId = downloadId;
	}

	public IMediaEventArgs getArgs() {
		return args;
	}

	public void setArgs(IMediaEventArgs args) {
		this.args = args;
	}
	
	
}
