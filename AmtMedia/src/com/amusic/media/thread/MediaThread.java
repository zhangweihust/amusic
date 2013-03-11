package com.amusic.media.thread;

import android.os.Message;

import com.amusic.media.handler.MediaHandler;
import com.amusic.media.utils.IAsynMediaQuery;
import com.amusic.media.utils.IAsynMediaQuery.MediaQueryToken;

public class MediaThread extends Thread {
	private MediaHandler handler;
	private IAsynMediaQuery query;
	private MediaQueryToken token;

	@Override
	public void run() {
		super.run();
		synchronized (query) {
			query.queryData(token);
			Message message = new Message();
			message.obj = token;
			handler.sendMessage(message);
		}
	}

	public MediaThread(MediaHandler handler, MediaQueryToken token) {
		this.handler = handler;
		this.query = handler.getQuery();
		this.token = token;
	}

}
