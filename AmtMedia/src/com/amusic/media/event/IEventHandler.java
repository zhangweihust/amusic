package com.amusic.media.event;

public interface IEventHandler<TEventArgs> {
	boolean onEvent(TEventArgs args);
}
