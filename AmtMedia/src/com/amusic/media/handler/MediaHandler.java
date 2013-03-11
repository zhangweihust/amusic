package com.amusic.media.handler;

import android.os.Handler;
import android.os.Message;

import com.amusic.media.utils.IAsynMediaQuery;
import com.amusic.media.utils.IAsynMediaQuery.MediaQueryToken;

public class MediaHandler extends Handler {
	private IAsynMediaQuery query;

	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		query.updateData((MediaQueryToken) msg.obj);
	}

	public MediaHandler(IAsynMediaQuery query) {
		super();
		this.query = query;
	}

	public IAsynMediaQuery getQuery() {
		return query;
	}
}
