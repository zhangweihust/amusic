package com.android.media.services.impl;

import java.util.Stack;
import java.util.concurrent.CopyOnWriteArrayList;

import com.android.media.event.IMediaEventArgs;
import com.android.media.event.IMediaEventHandler;
import com.android.media.event.impl.MediaEventTypes;
import com.android.media.services.IMediaEventService;

public class MediaEventService implements IMediaEventService {
	private CopyOnWriteArrayList<IMediaEventHandler> mediaEventHandlers = new CopyOnWriteArrayList<IMediaEventHandler>();
    private Stack<MediaEventTypes> mStack;
	@Override
	public boolean start() {
    	mStack = new Stack<MediaEventTypes>();
		return true;
	}

	@Override
	public boolean stop() {
		mediaEventHandlers.clear();
		mStack.clear();
		mStack = null;
		return true;
	}

	@Override
	public boolean addEventHandler(IMediaEventHandler handler) {
		if (mediaEventHandlers.contains(handler)) {
			return false;
		}
		return mediaEventHandlers.add(handler);
	}

	@Override
	public boolean removeEventHandler(IMediaEventHandler handler) {
		return mediaEventHandlers.remove(handler);
	}

	private synchronized void updateEvent(final IMediaEventArgs args) {
		mStack.push(args.getMediaUpdateEventTypes());
		for (final IMediaEventHandler handler : this.mediaEventHandlers) {
			args.setMediaUpdateEventTypes(mStack.peek());
            handler.onEvent(args);
		}
		mStack.pop();
	}

	@Override
	public boolean onMediaUpdateEvent(IMediaEventArgs args) {
		updateEvent(args);
		return true;
	}

}
