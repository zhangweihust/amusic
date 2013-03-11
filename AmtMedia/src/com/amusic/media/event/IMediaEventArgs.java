package com.amusic.media.event;

import com.amusic.media.event.impl.MediaEventArgs;
import com.amusic.media.event.impl.MediaEventTypes;

public interface IMediaEventArgs extends IEventArgs {
	public MediaEventTypes getMediaUpdateEventTypes();

	public MediaEventArgs setMediaUpdateEventTypes(MediaEventTypes mediaEventTypes);
}
