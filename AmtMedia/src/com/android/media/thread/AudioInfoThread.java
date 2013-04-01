package com.android.media.thread;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.database.Cursor;

import com.android.media.model.SongInfo;
import com.android.media.provider.MediaDatabaseHelper;
import com.android.media.provider.MediaManagerDB;
import com.android.media.services.IMediaService;
import com.android.media.services.INetworkService;
import com.android.media.services.IUserInfoService;
import com.android.media.services.impl.ServiceManager;
import com.android.media.utils.Constant;

public class AudioInfoThread extends Thread {
	private int times;
	private IUserInfoService countService;
	private boolean stop = false;
	private final MediaManagerDB db;
	private int stand = 10;

	@Override
	public void run() {
		while (!stop && times > 0) {
			try {
				SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
				int date = Integer.parseInt(format.format(System.currentTimeMillis()));
				if (uploadCountToServer()) {
					db.updateCountAudioInfoTask(date, 1);
					db.deleteCountLocalSong();
					db.deleteCountKMediaSong();
					break;
				} else {
					db.updateCountAudioInfoTask(date, 0);
				}
				sleep(countService.getCountDuration());
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				times--;
			}
		}
	}

	public AudioInfoThread(int times, IUserInfoService countService) {
		this.countService = countService;
		this.times = countService.getCountTimes() - times;
		db = ServiceManager.getMediaService().getMediaDB();
	}

	private boolean uploadCountToServer() {
		INetworkService networkService = ServiceManager.getNetworkService();
		if(networkService.acquire(false)){
			try {
				HttpEntityEnclosingRequestBase audioHttpRequest = new HttpPost(IMediaService.SEND_AUDIO_TOP10_URL);
				List<NameValuePair> audioParams = new ArrayList<NameValuePair>();
				audioParams.add(new BasicNameValuePair(Constant.MediaWithServerConstant.request_parameter, requestString(db.queryCountLocalSongs()).toString()));
				audioHttpRequest.setEntity(new UrlEncodedFormEntity(audioParams, HTTP.UTF_8));
				HttpResponse audioHttpResponse = new DefaultHttpClient().execute(audioHttpRequest);
				
				HttpEntityEnclosingRequestBase kMediaHttpRequest = new HttpPost(IMediaService.SEND_KMEDIA_TOP10_URL);
				List<NameValuePair> kMediaParams = new ArrayList<NameValuePair>();
				kMediaParams.add(new BasicNameValuePair(Constant.MediaWithServerConstant.request_parameter, requestString(db.queryCountKMediaSongs()).toString()));
				kMediaHttpRequest.setEntity(new UrlEncodedFormEntity(kMediaParams, HTTP.UTF_8));
				HttpResponse kMediaHttpResponse = new DefaultHttpClient().execute(kMediaHttpRequest);
				
				int audioState = audioHttpResponse.getStatusLine().getStatusCode();
				int kMediaState = kMediaHttpResponse.getStatusLine().getStatusCode();
				if (audioState == 200 && kMediaState == 200) {
					return true;
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	public static ArrayList<SongInfo> getTopFromServer(String url,int count){
		ArrayList<SongInfo> songList = new ArrayList<SongInfo>();
		try {
			HttpEntityEnclosingRequestBase httpResponseRequest = new HttpPost(url);
			List<NameValuePair> audioParams = new ArrayList<NameValuePair>();
			audioParams.add(new BasicNameValuePair(Constant.MediaWithServerConstant.request_top, "+count+"));
			httpResponseRequest.setEntity(new UrlEncodedFormEntity(audioParams, HTTP.UTF_8));
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpResponseRequest);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				 String strResult = EntityUtils.toString(httpResponse.getEntity());
				 if (strResult.indexOf("{") + 1 <= strResult.lastIndexOf("}")) {
						strResult = strResult.substring(strResult.indexOf("{") + 1, strResult.lastIndexOf("}"));
						String[] items = strResult.split("\\},\\{");
						String singer = null;
						String song = null;
						int times = 0;
							if(items.length > 1){
								for(String item : items) {
									JSONObject json = new JSONObject("{"+item+"}");
									singer = json.getString(Constant.MediaWithServerConstant.singer);
									song = json.getString(Constant.MediaWithServerConstant.song);
									times = json.getInt(Constant.MediaWithServerConstant.times);
									SongInfo songInfo = new SongInfo(song, singer, times);
									songList.add(songInfo);
								}
							}else if(items.length == 1){
								JSONObject json = new JSONObject("{"+strResult+"}");
								singer = json.getString(Constant.MediaWithServerConstant.singer);
								song = json.getString(Constant.MediaWithServerConstant.song);
								times = json.getInt(Constant.MediaWithServerConstant.times);
								SongInfo songInfo = new SongInfo(song, singer, times);
								songList.add(songInfo);
							}
						}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return songList;
	}
	private StringBuffer requestString(Cursor cursor){
		int k = 0;
		String singer;
		String song;
		String count;
		StringBuffer sb = new StringBuffer();
		while(cursor.moveToNext() && k++ < stand){
			sb.append("{");
			singer = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_COUNT_KMEDIA_SINGER));
			song = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_COUNT_KMEDIA_SONG));
			song = song.substring(0, song.lastIndexOf("."));
			if(song.contains("-")){
				song = song.substring(song.indexOf("-")+1);
			}
			count = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_COUNT_KMEDIA_COUNT));
			singer = makeStr(singer);
			song = makeStr(song);
			sb.append(singer+","+song+","+count);
			sb.append("}").append(",");
		}
		if(!"".equals(sb.toString())){
			sb.deleteCharAt(sb.length()-1);
		}
		cursor.close();
		return sb;
	}
	
	public String makeStr(String str){
		if(str != null){
			if(str.contains(",")){
				str.replace(",", "");
				return str;
			}else if(str.contains("，")){
				str.replace("，", "");
				return str;
			}
		}
		return str;
	}
	
	public void stopThread() {
		stop = true;
	}
}
