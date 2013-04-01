package com.android.media.screens.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.amusic.media.R;
import com.android.media.MediaApplication;
import com.android.media.dialog.OnScreenHint;
import com.android.media.provider.MediaDatabaseHelper;
import com.android.media.provider.MediaManagerDB;
import com.android.media.services.IMediaService;
import com.android.media.services.impl.ServiceManager;
import com.android.media.view.CustomDialog;

public class ScreenAudioSongError {
	private Context context;
	private CustomDialog.Builder customBuilder;
	private CustomDialog progressDialog;
	private MediaApplication MediaApp;
	private CheckBox songValue,accompanyValue,lyricsValue,pictureValue;
	private int selectnum = 0;
//	private int successnum = 0;
	
//	private static final int SONG_ERROR_TYPE = 1;
//	private static final int ACCOMPANY_ERROR_TYPE = 2;
//	private static final int LYRICS_ERROR_TYPE = 3;
//	private static final int PICTURE_ERROR_TYPE = 4;
	
	public final static String SONGERRORINFO = "SongErrorInfo";
	
	public ScreenAudioSongError(final Context context)
	{
		this.context = context;
		MediaApp = MediaApplication.getInstance();
		
		View v = LayoutInflater.from(context).inflate(R.layout.dialog_song_error, null);
		TextView info = (TextView)v.findViewById(R.id.song_error_current_song);
		songValue = (CheckBox)v.findViewById(R.id.song_error_song_error);
		accompanyValue = (CheckBox)v.findViewById(R.id.song_error_accompany_error);
		lyricsValue = (CheckBox)v.findViewById(R.id.song_error_lyrics_error);
		pictureValue = (CheckBox)v.findViewById(R.id.song_error_picture_error);
		
		OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked)
				{
					selectnum++;
				}
				else
				{
					selectnum--;
				}
			}
		};

		songValue.setOnCheckedChangeListener(listener);
		accompanyValue.setOnCheckedChangeListener(listener);
		lyricsValue.setOnCheckedChangeListener(listener);
		pictureValue.setOnCheckedChangeListener(listener);
		
		info.setText(context.getString(R.string.songerrorinfo_current_song) + MediaApp.getCurSongName() + "_" + MediaApp.getcurSongArtist());
		
		customBuilder = new CustomDialog.Builder(context);
		customBuilder.setTitle(context.getString(R.string.songerrorinfo_dialog_title)).setContentView(v)
		.setPositiveButton(context.getString(R.string.screen_scan_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	if(songValue.isChecked() || accompanyValue.isChecked() || lyricsValue.isChecked() || pictureValue.isChecked())
            	{
            		sendErrorInfo(songValue.isChecked(),accompanyValue.isChecked(),lyricsValue.isChecked(),pictureValue.isChecked());
            	}
            	dialog.dismiss();          	
            }
        })
        .setNegativeButton(context.getString(R.string.screen_scan_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	dialog.dismiss();          	
            }
        });
		progressDialog = customBuilder.create();
	}
	
	
	public void show()
	{
		if (progressDialog != null)
		{
			progressDialog.show();
		}
	}
	
	public void sendErrorInfo(final boolean songerr,final boolean accerr,final boolean lycerr,final boolean picerr)
	{
		MediaManagerDB mediadb = ServiceManager.getMediaService().getMediaDB();
		final Cursor cursor = mediadb.queryCurPlaySong(MediaApplication.getInstance().getCurSongId());
		if (cursor.moveToNext())
		{
			new Thread("sendErrorInfo"){
				public void run() {
					String filename = cursor.getString(cursor.getColumnIndexOrThrow(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_DISPALYNAME));
					String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_SONGNAME));
					String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ARTISTNAME));
					String duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_DURATION));
					String taginfo = title + "==" + artist + "==" + duration;
					cursor.close();
					
					if (!SendErrorInfo(songerr,accerr,lycerr,picerr,filename,taginfo))
					{
						String value = "";
						if (songerr)
						{
							value += "1";
						}
						else
						{
							value += "0";
						}
						if (accerr)
						{
							value += "1";
						}
						else
						{
							value += "0";
						}
						if (lycerr)
						{
							value += "1";
						}
						else
						{
							value += "0";
						}
						if (picerr)
						{
							value += "1";
						}
						else
						{
							value += "0";
						}
						
						saveErrorInfo(filename + "&" + taginfo,value);
					}
				}
			}.start();
		}
	}
	
	public static String HttpSendErrorInfo(boolean songerr,boolean accerr,boolean lycerr,boolean picerr,String filename,String taginfo)
	{
		String ret = "";
		if (ServiceManager.getNetworkService().acquire(true))
		{
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			if (songerr)
			{
				params.add(new BasicNameValuePair("typeA", "1"));
			}
			else
			{
				params.add(new BasicNameValuePair("typeA", "0"));
			}
			if (accerr)
			{
				params.add(new BasicNameValuePair("typeB", "1"));
			}
			else
			{
				params.add(new BasicNameValuePair("typeB", "0"));
			}
			if (lycerr)
			{
				params.add(new BasicNameValuePair("typeC", "1"));
			}
			else
			{
				params.add(new BasicNameValuePair("typeC", "0"));
			}
			if (picerr)
			{
				params.add(new BasicNameValuePair("typeD", "1"));
			}
			else
			{
				params.add(new BasicNameValuePair("typeD", "0"));
			}
			
			params.add(new BasicNameValuePair("filename", filename));
			params.add(new BasicNameValuePair("taginfo", taginfo));
			HttpEntityEnclosingRequestBase httpRequest = new HttpPost(IMediaService.SEND_SONG_ERROR_INFO);
			try
			{
				httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
				HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
				if (httpResponse.getStatusLine().getStatusCode() == 200) 
				{
					ret = EntityUtils.toString(httpResponse.getEntity());
					return ret;
				} 
			}catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return ret;
	}
	
	private Handler handler = new Handler()
	{
		public void handleMessage(Message msg) 
		{
			OnScreenHint mOnScreenHint = new OnScreenHint(context);
			if (msg.what == 1)
			{
        		mOnScreenHint = OnScreenHint.makeText(context, context.getString(R.string.songerrorinfo_sendsuccess_prompt));   //设置toast要显示的信息
        		mOnScreenHint.show(); 
			}
			else
			{
				mOnScreenHint = OnScreenHint.makeText(context, context.getString(R.string.songerrorinfo_sendfailed_prompt));   //设置toast要显示的信息
        		mOnScreenHint.show(); 
			}
		};
	};
	
	public boolean SendErrorInfo(boolean songerr,boolean accerr,boolean lycerr,boolean picerr,String filename,String taginfo)
	{
		String retstr = HttpSendErrorInfo(songerr,accerr,lycerr,picerr,filename,taginfo);
		
		Message msg = handler.obtainMessage();
		boolean ret = false;
		if (retstr.equals(String.valueOf(selectnum)))
		{
			msg.what = 1;
			ret = true;
		}
		else
		{
			msg.what = 2;
		}
		handler.sendMessage(msg);
		
		return ret;
	}
	
	public void saveErrorInfo(String songtag,String value)
	{
		SharedPreferences.Editor editor = MediaApplication.getContext().getSharedPreferences(SONGERRORINFO,Context.MODE_PRIVATE).edit();
		editor.putString(songtag + "|" + System.currentTimeMillis(), value);
		editor.commit();
	}
	
	public static void deleteErrorInfo(String key)
	{
		SharedPreferences.Editor editor = MediaApplication.getContext().getSharedPreferences(SONGERRORINFO,Context.MODE_PRIVATE).edit();
		editor.remove(key);
		editor.commit();
	}
	
	public static  boolean isSPNull(){
		SharedPreferences sp = MediaApplication.getContext().getSharedPreferences(SONGERRORINFO, Context.MODE_PRIVATE);
	    Map<String, ?>  map = sp.getAll();
	    if(map.isEmpty()){
	    	return true; 
	    }       
	    return false;
	}
}