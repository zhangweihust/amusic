package com.android.media.services.impl;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.android.media.MediaApplication;
import com.android.media.task.SendCrashReportsTask;

public class CrashReportService extends Service {

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		MediaApplication.logD(CrashReportService.class, "CrashReportService onCreate");
		SendCrashReportsTask task = new SendCrashReportsTask(this);
		task.execute();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		MediaApplication.logD(CrashReportService.class, "CrashReportService onDestroy");
	}
	
	

}
