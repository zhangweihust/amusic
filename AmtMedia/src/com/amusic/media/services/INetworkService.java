package com.amusic.media.services;

public interface INetworkService extends IService {
	public boolean acquire(boolean show);
	public int getNetType();
}
