package com.amusic.media.utils;

public interface IAsynMediaQuery {
	public void queryData(MediaQueryToken token);

	public void updateData(MediaQueryToken token);

	public enum MediaQueryToken {
		QUERY_INIT, QUERY_UPDATE
	}
}
