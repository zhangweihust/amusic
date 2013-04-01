package com.android.media.services;

import com.android.media.event.IMediaEventArgs;
import com.android.media.event.IMediaEventDispatcher;

public interface IMediaEventService extends IService, IMediaEventDispatcher {
	public boolean onMediaUpdateEvent(IMediaEventArgs args);
}
