package com.android.media.thread;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.media.MediaApplication;
import com.android.media.screens.impl.ScreenSuggestionFeedback;
import com.android.media.services.IUserInfoService;

public class SuggestionCountThread extends Thread {
	private int times;
	private IUserInfoService countService;
	private boolean stop = false;

	@Override
	public void run() {
		while (!stop && times > 0) {
			try {
	        	String data = null;
	        	String skey = null;
				Context context = MediaApplication.getContext();
				SharedPreferences sp = context.getSharedPreferences(ScreenSuggestionFeedback.PREF, 0);
			    Map<String, ?>  map = sp.getAll();
			    Set<String> key = map.keySet();
			    Iterator<String> it = key.iterator();
			    while(it.hasNext()) {
		            skey = it.next();
		            data = (String) map.get(skey);
		    		if(ScreenSuggestionFeedback.send(data)){
		    			ScreenSuggestionFeedback.delete(skey);
		    		}	           
		        }
			    
				if(ScreenSuggestionFeedback.isSPNull()){
					this.stopThread();
				}
				times--;
				sleep(countService.getCountDuration());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public SuggestionCountThread(IUserInfoService countService) {
		this.countService = countService;
		times = countService.getCountTimes();
	}

	public void stopThread() {
		stop = true;
	}
}
