package com.amusic.media.thread;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;

import com.amusic.media.MediaApplication;
import com.amusic.media.screens.impl.ScreenAudioSongError;
import com.amusic.media.services.IUserInfoService;

public class SongErrorCountThread extends Thread {
	private int times;
	private IUserInfoService countService;
	private static final String ThreadName = "SongErrorCountThread";
	private boolean stop = false;

	@Override
	public void run() {
		while (!stop && times > 0) {
			try {
	        	String data = null;
	        	String skey = null;
	        	String filename,taginfo;
	        	boolean []bselect = new boolean[4];
	        	int selectnum = 0;
				Context context = MediaApplication.getContext();
				SharedPreferences sp = context.getSharedPreferences(ScreenAudioSongError.SONGERRORINFO, context.MODE_PRIVATE);
			    Map<String, ?>  map = sp.getAll();
			    Set<String> key = map.keySet();
			    Iterator<String> it = key.iterator();
			    while(it.hasNext()) {
		            skey = it.next();
		            data = (String) map.get(skey);
		            selectnum = 0;
		            char[] selectdata = data.toCharArray();
		            if (selectdata.length == 4)
		            {
		            	for (int i = 0; i < selectdata.length; i++)
		            	{
		            		bselect[i] = false;
		            		if (selectdata[i] == '1')
		            		{
		            			bselect[i] = true;
		            			selectnum++;
		            		}
		            	}
		            }
		            String songinfo = skey.substring(0,skey.indexOf("|"));
		            filename = songinfo.substring(0, songinfo.indexOf("&"));
		            taginfo = songinfo.substring(songinfo.indexOf("&") + 1);
		    		if(ScreenAudioSongError.HttpSendErrorInfo(bselect[0],bselect[1],bselect[2],bselect[3], filename, taginfo).equals(String.valueOf(selectnum))){
		    			ScreenAudioSongError.deleteErrorInfo(skey);
		    		}	           
		        }
			    if(ScreenAudioSongError.isSPNull()){
			    	break;
				}
				times--;
				sleep(countService.getCountDuration());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public SongErrorCountThread(IUserInfoService countService) {
		this.setName(ThreadName);
		this.countService = countService;
		times = countService.getCountTimes();
	}
	
	public void stopThread() {
		stop = true;
	}

}
