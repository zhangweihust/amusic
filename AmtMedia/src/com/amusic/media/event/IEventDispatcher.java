package com.amusic.media.event;

public interface IEventDispatcher<TEventHandler> {
	boolean addEventHandler(TEventHandler handler);

	boolean removeEventHandler(TEventHandler handler);
}
