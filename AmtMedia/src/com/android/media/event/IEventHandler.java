package com.android.media.event;

public interface IEventHandler<TEventArgs> {
	boolean onEvent(TEventArgs args);
}
