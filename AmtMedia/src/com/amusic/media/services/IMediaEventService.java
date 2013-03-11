package com.amusic.media.services;

import com.amusic.media.event.IMediaEventArgs;
import com.amusic.media.event.IMediaEventDispatcher;

public interface IMediaEventService extends IService, IMediaEventDispatcher {
	public boolean onMediaUpdateEvent(IMediaEventArgs args);
}
