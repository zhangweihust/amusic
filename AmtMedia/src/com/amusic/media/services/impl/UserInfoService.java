package com.amusic.media.services.impl;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.database.Cursor;

import com.amusic.media.provider.MediaDatabaseHelper;
import com.amusic.media.provider.MediaManagerDB;
import com.amusic.media.screens.impl.ScreenAudioSongError;
import com.amusic.media.screens.impl.ScreenSuggestionFeedback;
import com.amusic.media.services.IUserInfoService;
import com.amusic.media.thread.AudioInfoThread;
import com.amusic.media.thread.DeviceInfoThread;
import com.amusic.media.thread.SongErrorCountThread;
import com.amusic.media.thread.SuggestionCountThread;
import com.amusic.media.thread.UserActivityInfoThread;

public class UserInfoService implements IUserInfoService {
	private MediaManagerDB db;
	private int userFrequency = 7;
	private int audioFrequency = 7;
	private static final long COUNT_DURATION = 10 * 60 * 1000;
	private static final int COUNT_TIMES = 3;
	private static final int USER_COUNT_OK_TIMES = 3;
	private AudioInfoThread audioInfoThread;
	private DeviceInfoThread deviceInfoThread;
	private SuggestionCountThread suggestionCountThread;
	private SongErrorCountThread songErrorCountThread;
	private UserActivityInfoThread userActivityInfoThread;
	private String startTime;
	private String exitTime;

	@Override
	public boolean start() {
		db = ServiceManager.getMediaService().getMediaDB();
		Date nowDate = new Date();
		SimpleDateFormat formatTime = new SimpleDateFormat("dd:HH:mm");
		startTime = formatTime.format(nowDate);
		
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		int date = Integer.parseInt(format.format(nowDate));
		Cursor cursor = db.queryUserActivityInfo();
	
		if(cursor.moveToNext()){
			int oldDate = cursor.getInt(cursor.getColumnIndex((MediaDatabaseHelper.COLUMN_COUNT_USER_INFO_ACTIVITY_DATE)));
			if(date - userFrequency > oldDate){
				userActivityInfoThread = new UserActivityInfoThread(0, this);
				userActivityInfoThread.start();
			}else{
				if(cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_COUNT_USER_INFO_ACTIVITY_RESULT)) == 0){
					int times = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_COUNT_AUDIO_INFO_TASK_DAY_TIMES));
					userActivityInfoThread = new UserActivityInfoThread(times, this);
					userActivityInfoThread.start();
				}
			}
		}else{
			userActivityInfoThread = new UserActivityInfoThread(0, this);
			userActivityInfoThread.start();
		}
		cursor.close();
		cursor = db.queryCountAudioInfoTask();
		if (cursor.moveToNext()) {
			int oldDate = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_COUNT_AUDIO_INFO_TASK_DATE));
			if (date - audioFrequency > oldDate) {
				audioInfoThread = new AudioInfoThread(0, this);
				audioInfoThread.start();
			} else {
				if (cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_COUNT_AUDIO_INFO_TASK_RESULT)) == 0) {
					int times = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_COUNT_AUDIO_INFO_TASK_DAY_TIMES));
					audioInfoThread = new AudioInfoThread(times, this);
					audioInfoThread.start();
				}
			}
		} else {
			audioInfoThread = new AudioInfoThread(0, this);
			audioInfoThread.start();
		}
		cursor.close();
		cursor = db.queryCountUserInfoTask();
		if (cursor.moveToNext()) {
			int okTimes = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_COUNT_USER_INFO_TASK_TIMES));
			if (USER_COUNT_OK_TIMES > okTimes) {
				int oldDate = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_COUNT_USER_INFO_TASK_DATE));
				if (date > oldDate) {
					deviceInfoThread = new DeviceInfoThread(0, this);
					deviceInfoThread.start();
				} else {
					if (cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_COUNT_USER_INFO_TASK_RESULT)) == 0) {
						int dayTimes = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_COUNT_USER_INFO_TASK_DAY_TIMES));
						deviceInfoThread = new DeviceInfoThread(dayTimes, this);
						deviceInfoThread.start();
					}
				}
			}
		} else {
			deviceInfoThread = new DeviceInfoThread(0, this);
			deviceInfoThread.start();
		}
		cursor.close();
		if(!ScreenSuggestionFeedback.isSPNull()){
			suggestionCountThread = new SuggestionCountThread(this);
			suggestionCountThread.start();
		}
		if(!ScreenAudioSongError.isSPNull()){
			songErrorCountThread = new SongErrorCountThread(this);
			songErrorCountThread.start();
		}
		return true;
	}

	@Override
	public boolean stop() {
		Date nowDate = new Date();
		SimpleDateFormat formatTime = new SimpleDateFormat("dd:HH:mm");
		exitTime = formatTime.format(nowDate);
		
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		int dayDate = Integer.parseInt(format.format(nowDate));
		db.updateUserActivityInfo(dayDate, getTimestamp(startTime, exitTime));
		
		if (audioInfoThread != null) {
			audioInfoThread.stopThread();
		}
		if (deviceInfoThread != null) {
			deviceInfoThread.stopThread();
		}
		if (suggestionCountThread != null){
			suggestionCountThread.stopThread();
		}
		if (songErrorCountThread != null){
			songErrorCountThread.stopThread();
		}
		return true;
	}

	public int getTimestamp(String startTime, String exitTime){
		String[] startTimes = startTime.split(":");
		String[] exitTimes = exitTime.split(":");
		int startTimesToM = Integer.parseInt(startTimes[0]) * 24 * 60 + Integer.parseInt(startTimes[1]) * 60 + Integer.parseInt(startTimes[2]);
		int exitTimesToM = Integer.parseInt(exitTimes[0]) * 24 * 60 + Integer.parseInt(exitTimes[1]) * 60 + Integer.parseInt(exitTimes[2]);
		return (exitTimesToM - startTimesToM) ;/*/ 60.0f;*/
	}
	public long getCountDuration() {
		return COUNT_DURATION;
	}

	public int getCountTimes() {
		return COUNT_TIMES;
	}

	public void setCountFrequency(int audioFrequency) {
		this.audioFrequency = audioFrequency;
	}

}
