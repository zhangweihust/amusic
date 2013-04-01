package com.android.media.services;

public interface IUserInfoService extends IService {
	public void setCountFrequency(int countFrequency);

	public int getCountTimes();

	public long getCountDuration();
}
