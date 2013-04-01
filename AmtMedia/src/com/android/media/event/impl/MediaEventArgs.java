package com.android.media.event.impl;

import java.util.HashMap;

import com.android.media.event.IEventArgs;
import com.android.media.event.IMediaEventArgs;

public class MediaEventArgs implements IMediaEventArgs {
	private MediaEventTypes mediaEventTypes;
	private final HashMap<String, Object> extra;

	public MediaEventArgs() {
		this.extra = new HashMap<String, Object>();
	}

	public MediaEventTypes getMediaUpdateEventTypes() {
		return mediaEventTypes;
	}

	public MediaEventArgs setMediaUpdateEventTypes(MediaEventTypes mediaEventTypes) {
		this.mediaEventTypes = mediaEventTypes;
		return this;
	}

	public Object getExtra(String key) {
		return this.extra.get(key);
	}

	public IEventArgs putExtra(String key, Object value) {
		this.extra.put(key, value);
		return this;
	}

}
