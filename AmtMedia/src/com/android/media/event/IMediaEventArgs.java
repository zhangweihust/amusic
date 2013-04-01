package com.android.media.event;

import com.android.media.event.impl.MediaEventArgs;
import com.android.media.event.impl.MediaEventTypes;

public interface IMediaEventArgs extends IEventArgs {
	public MediaEventTypes getMediaUpdateEventTypes();

	public MediaEventArgs setMediaUpdateEventTypes(MediaEventTypes mediaEventTypes);
}
