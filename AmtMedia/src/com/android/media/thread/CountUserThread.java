package com.android.media.thread;

import java.text.SimpleDateFormat;
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

import com.android.media.provider.MediaManagerDB;
import com.android.media.services.IMediaService;
import com.android.media.services.IUserInfoService;
import com.android.media.services.impl.ServiceManager;
import com.android.media.utils.DeviceInformation;
import com.android.media.utils.DeviceInformation.InfoName;

public class CountUserThread extends Thread {
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
					db.updateCountUserInfoTask(date, 1);
					break;
				} else {
					db.updateCountUserInfoTask(date, 0);
				}
				sleep(countService.getCountDuration());
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				times--;
			}
		}
	}

	public CountUserThread(int times, IUserInfoService countService) {
		this.countService = countService;
		this.times = countService.getCountTimes() - times;
		db = ServiceManager.getMediaService().getMediaDB();
	}

	private boolean uploadCountToServer() {
		try {
			HttpEntityEnclosingRequestBase httpRequest = new HttpPost(IMediaService.USER_INFORMATION_SERVER);
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair(InfoName.IMEI.toString(), DeviceInformation.getInformation(InfoName.IMEI)));
			params.add(new BasicNameValuePair(InfoName.CPU_MAX_FREQUENCY.toString(), DeviceInformation.getInformation(InfoName.CPU_MAX_FREQUENCY)));
			params.add(new BasicNameValuePair(InfoName.CPU_MODEL.toString(), DeviceInformation.getInformation(InfoName.CPU_MODEL)));
			params.add(new BasicNameValuePair(InfoName.MEMORY_TOTAL.toString(), DeviceInformation.getInformation(InfoName.MEMORY_TOTAL)));
			params.add(new BasicNameValuePair(InfoName.PHONE_KTV_VERSION.toString(), DeviceInformation.getInformation(InfoName.PHONE_KTV_VERSION)));
			params.add(new BasicNameValuePair(InfoName.PHONE_MODEL.toString(), DeviceInformation.getInformation(InfoName.PHONE_MODEL)));
			params.add(new BasicNameValuePair(InfoName.SCREEN_RESOLUTION.toString(), DeviceInformation.getInformation(InfoName.SCREEN_RESOLUTION)));
			params.add(new BasicNameValuePair(InfoName.SYSTEM_VERSION.toString(), DeviceInformation.getInformation(InfoName.SYSTEM_VERSION)));
			httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public void stopThread() {
		stop = true;
	}
}
