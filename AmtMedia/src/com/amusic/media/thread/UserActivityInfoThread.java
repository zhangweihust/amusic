package com.amusic.media.thread;

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

import android.database.Cursor;

import com.amusic.media.provider.MediaDatabaseHelper;
import com.amusic.media.provider.MediaManagerDB;
import com.amusic.media.services.IMediaService;
import com.amusic.media.services.INetworkService;
import com.amusic.media.services.IUserInfoService;
import com.amusic.media.services.impl.ServiceManager;
import com.amusic.media.utils.Constant;
import com.amusic.media.utils.DeviceInformation;
import com.amusic.media.utils.DeviceInformation.InfoName;

public class UserActivityInfoThread extends Thread {
	private int times;
	private IUserInfoService countService;
	private boolean stop = false;
	private final MediaManagerDB db;

	@Override
	public void run() {
		while (!stop && times > 0) {
			try {
				SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
				int date = Integer.parseInt(format.format(System.currentTimeMillis()));
				if (uploadCountToServer()) {
					db.updateUserActivityInfoTask(date, 1);
//					db.deleteUserActivityInfo();
					break;
				} else {
					db.updateUserActivityInfoTask(date, 0);
				}
				times--;
				sleep(countService.getCountDuration());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public UserActivityInfoThread(int times, IUserInfoService countService) {
		this.countService = countService;
		this.times = countService.getCountTimes() - times;
		db = ServiceManager.getMediaService().getMediaDB();
	}

	private boolean uploadCountToServer() {
		INetworkService networkService = ServiceManager.getNetworkService();
		if(networkService.acquire(false)){
			Cursor c = db.queryUserActivityInfo();
			int times = -1;
			int timesTamp = -1;
			if(c.moveToNext()){
				times = c.getInt(c.getColumnIndex(MediaDatabaseHelper.COLUMN_COUNT_USER_INFO_ACTIVITY_TIMES));
				timesTamp = c.getInt(c.getColumnIndex(MediaDatabaseHelper.COLUMN_COUNT_USER_INFO_ACTIVITY_CUMULATIVE_TIME));
			}
//			System.out.println("times="+times+" timesTamp="+timesTamp);
			c.close();
			if(times == -1 && timesTamp == -1){
				return true;
			}
			try {
				HttpEntityEnclosingRequestBase httpRequest = new HttpPost(IMediaService.USER_ACTIVITY_INFORMATION_SERVER);
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				String value = "{"+DeviceInformation.getInformation(InfoName.IMEI)+","+times+","+timesTamp+"}";
				params.add(new BasicNameValuePair(Constant.MediaWithServerConstant.request_parameter, value));
				httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
				HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
				if (httpResponse.getStatusLine().getStatusCode() == 200) {
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
	
	public void stopThread() {
		stop = true;
	}
}
