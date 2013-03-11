package com.amusic.media.event;

public interface IEventArgs {
	public Object getExtra(String key);

	public IEventArgs putExtra(String key, Object value);
}
