package com.android.media.download;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;


import android.util.Log;

import com.android.media.event.IMediaEventArgs;
import com.android.media.event.impl.MediaEventArgs;
import com.android.media.event.impl.MediaEventTypes;
import com.android.media.services.IMediaEventService;
import com.android.media.services.IMediaService;
import com.android.media.services.INetworkService;
import com.android.media.services.impl.ServiceManager;
import com.android.media.task.DownloadTask;

public class DownloadLrcLyric {

	private String songName = "";
	private String singger = "";
	private final int bufferSize = 1024;
	private String lyricPath = "";
	private final IMediaEventService mediaEventService;
	private final INetworkService networkService;
	
	public DownloadLrcLyric(String name,String sig) {
		songName = name;
		singger = sig;
		this.mediaEventService = ServiceManager.getMediaEventService();
		this.networkService = ServiceManager.getNetworkService();
	}
	
	public void setSongName(String name) {
		songName = name;
	}
	
	public void setSingger(String sger) {
		singger = sger;
	}
	
	public void setLyricPath(String lrcPath) {
		lyricPath = lrcPath;
	}
	
	public void downloadlyrics() {
		MediaEventArgs args = new MediaEventArgs();
		if (!networkService.acquire(false)) {
			mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_LRC_LYRICS_ERROR));
			return;
		}
		try {
			HttpEntityEnclosingRequestBase httpRequest = new HttpPost(IMediaService.DOWNLOAD_SERVER_BASE + IMediaService.DOWNLOAD_LRC_LYRICS_SERVER_ACTION);
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("song", songName));
			params.add(new BasicNameValuePair("singer", singger));
			httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				String strResult = EntityUtils.toString(httpResponse.getEntity());
				strResult = strResult.replace("\"", "");
				String lyricUrlPart = null;
				
				List<String> lyricList = new ArrayList<String>();
				if (strResult.indexOf("{") + 1 <= strResult.lastIndexOf("}")) {
					strResult = strResult.substring(strResult.indexOf("{") + 1, strResult.lastIndexOf("}"));
					
					String[] items = strResult.split("\\},\\{");
					
					if(items.length > 1) {
						String lsong = null;
						String lsinger = null;
						String lname = null;
						String lencryptname = null;
						for(String item : items) {
							String[] parts = item.split(",");
							String[] datas = null;
							lencryptname = "";
							lsong = "";
							lsinger = "";
							for(String part : parts) {
								datas = part.split(":");// part : song:\u4f20\u5947
								if(datas != null && datas[0].equalsIgnoreCase("song")) {
									if(datas.length < 2) {
										continue;
									}
									lsong = datas[1];
								} else if(datas != null && datas[0].equalsIgnoreCase("singer")) {
									if(datas.length < 2) {
										continue;
									}
									lsinger = datas[1];
								} else if (datas != null && datas[0].equalsIgnoreCase("encryptname")) {
									if(datas.length < 2) {
										continue;
									}
									lencryptname = datas[1];
								}
							}
							
							if (lsong.equals("") || lsinger.equals("")) {
								continue;
							}
							lname = DownloadLyric.utf2unicode(lsong) + "——" + DownloadLyric.utf2unicode(lsinger) + "——" + lencryptname;
							lyricList.add(lname);
							
						}
						int size = lyricList.size();
						if(size > 1) {
							
							args.putExtra("lyricList", lyricList);
							mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_LRC_LYRIC_SELECT_UI));
							return;
						} else if(size == 1) {
							lyricUrlPart = lyricList.get(0);
						}
					} 
	
					
					if (items.length == 0) {
						return;
					}
					String[] parts = items[0].split(",");
					String[] datas = null;

					for (String part : parts) {
						datas = part.split(":");
						if (datas != null && datas.length == 2 && datas[0].equalsIgnoreCase("encryptname")) {
							lyricUrlPart = datas[1];
							break;
						}
					}
				}
				if (lyricUrlPart != null) {
					lyricPath = lyricPath.substring(0, lyricPath.lastIndexOf("."));
					lyricPath += ".lrc.gz";
					downloadLyrics(IMediaService.DOWNLOAD_SERVER_BASE + lyricUrlPart + ".lc", 0, lyricPath, args);
					return;
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		}
		mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_LRC_LYRICS_ERROR));
	}
	
	public static void downloadLyrics(String url, long startPos, String path, IMediaEventArgs args) {
		final DownloadJob mJob = new DownloadJob(args);
		mJob.setDownloadUrl(url);
		mJob.setDownloadStartPos(startPos);
		mJob.setPath(path);
		ServiceManager.getAmtMedia().getHandler().post(new Runnable() {
			@Override
			public void run() {
				DownloadTask mDownloadTask = new DownloadTask(mJob);
				mJob.setDownloadTask(mDownloadTask);
				mDownloadTask.execute(IMediaService.DOWNLOAD_START_ON_LRC_LYRICS);
			}
		});
	}
}
