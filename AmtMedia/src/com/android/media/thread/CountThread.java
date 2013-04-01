package com.android.media.thread;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
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

import android.database.Cursor;

import com.android.media.provider.MediaDatabaseHelper;
import com.android.media.provider.MediaManagerDB;
import com.android.media.services.IMediaService;
import com.android.media.services.IUserInfoService;
import com.android.media.services.impl.ServiceManager;

public class CountThread extends Thread {
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

	public CountThread(int times, IUserInfoService countService) {
		this.countService = countService;
		this.times = countService.getCountTimes() - times;
		db = ServiceManager.getMediaService().getMediaDB();
	}

	private boolean uploadCountToServer() {
		try {
			HttpEntityEnclosingRequestBase httpRequest = new HttpPost(IMediaService.SEND_AUDIO_TOP10_URL);
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("dbstr", URLEncoder.encode(requestString(db.queryCountLocalSongs()).toString())));
//			params.add(new BasicNameValuePair("kmedia_top10", URLEncoder.encode(requestString(db.queryCountKMediaSongs()).toString())));
			httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
			
			int state = httpResponse.getStatusLine().getStatusCode();
			String str = getDataFromServer();
			if (state == 200) {
				return true;
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	private String getDataFromServer(){
		HttpURLConnection connection;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			connection = (HttpURLConnection) new URL(IMediaService.RERIVER_AUDIO_TOP10_URL).openConnection();
			connection.setRequestMethod("POST");
			int state = connection.getResponseCode();
			if (state == 200) {
				InputStream in = connection.getInputStream();
				byte[] buffer = new byte[1024];
				int len = 0;
				while ((len = in.read(buffer)) > 0) {
					out.write(buffer, 0, len);
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		String data = new String(out.toByteArray());
		return data;
	
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
			count = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_COUNT_KMEDIA_COUNT));
			sb.append(singer+","+song+","+count);
			sb.append("}").append(",");
		}
		if(!"".equals(sb.toString())){
			sb.deleteCharAt(sb.length()-1);
		}
		cursor.close();
		return sb;
	}
	public void stopThread() {
		stop = true;
	}
}
