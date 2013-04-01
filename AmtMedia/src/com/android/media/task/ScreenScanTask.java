package com.android.media.task;

import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.amusic.media.R;
import com.android.media.AmtMedia;
import com.android.media.MediaApplication;
import com.android.media.event.IMediaEventArgs;
import com.android.media.event.impl.MediaEventArgs;
import com.android.media.event.impl.MediaEventTypes;
import com.android.media.provider.MediaDatabaseHelper;
import com.android.media.provider.MediaManagerDB;
import com.android.media.screens.impl.ScreenAudio;
import com.android.media.screens.impl.ScreenRecord;
import com.android.media.services.impl.ServiceManager;
import com.android.media.view.CustomDialog;


public class ScreenScanTask extends AsyncTask<String, Integer, Void> {
	
	private CustomDialog.Builder customBuilder;
	private CustomDialog progressDialog;
	private Context context;
	private List<String> fileselectlist;
	private boolean ignoresmallfile;
	private boolean cancelscan = false;
	
	private int scannum = 0;
	private int addnum = 0;
	private int sumsongs = 0;
	private int ignorenum = 0;
	

	public ScreenScanTask(Context context,List<String> fileselectlist,boolean ignoresmallfile)
	{
		this.context = context;
		this.fileselectlist = fileselectlist;
		this.ignoresmallfile = ignoresmallfile;
		customBuilder = new CustomDialog.Builder(context);
		customBuilder.setTitle(context.getString(R.string.screen_scan_title))
		.setMessage(context.getString(R.string.screen_scan_preper))
		.setWhichViewVisible(CustomDialog.contentIsProgressBar)
        .setPositiveButton(context.getString(R.string.screen_scan_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	dialog.dismiss();          	
            	cancelscan = true;
            }
        });
	}
	
	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		
		progressDialog = customBuilder.create();
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				// TODO Auto-generated method stub
				cancelscan = true;
			}
		});
		progressDialog.show();
		super.onPreExecute();
	}
	
	@Override
	protected Void doInBackground(String... params) {
		// TODO Auto-generated method stub

		MediaManagerDB mediadb = ServiceManager.getMediaService().getMediaDB();
		ContentValues cv = new ContentValues();
		Cursor cursor = mediadb.querySystemAllmp3();
		mediadb.scandeletesing();
		String filepath;
		int duration;
		int listsize = fileselectlist.size();
		sumsongs = cursor.getCount();
		
		while (cursor.moveToNext() && !cancelscan)
		{
			filepath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
			
			for (int i = 0;  i < listsize; i++)
			{
				if (filepath.substring(0,filepath.lastIndexOf("/")).equals(fileselectlist.get(i)))
				{
					duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
					if(ignoresmallfile && duration < 10000) 
					{
						ignorenum++;
					}
					else
					{
						cv.clear();
						cv.put(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ID, cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
						cv.put(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_SID, cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
						cv.put(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_DISPALYNAME, cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)));
						cv.put(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ALBUMNAME, cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)));
						cv.put(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ALBUMID, cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)));
						cv.put(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ARTISTNAME, cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)));
						cv.put(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_SIZE, cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)));
						cv.put(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_DURATION, duration);
						cv.put(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_EXTNAME, filepath.substring(filepath.lastIndexOf(".") + 1));
						cv.put(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_FILEPATH, filepath);
						cv.put(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_PARENTPATH, filepath.substring(0,filepath.lastIndexOf("/") + 1));
						cv.put(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_SONGNAME, cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)));
						cv.put(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_DATEADDED, cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)));
						if (mediadb.scaninsertsing(cv) > -1)
						{
							addnum++;
						}
					}
					
					break;
				}	
			}
			scannum++;
			publishProgress(scannum);
		}
		
		return null;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		// TODO Auto-generated method stub
		customBuilder.setProgressBar((int) (values[0] * 100 / sumsongs));
		customBuilder.getProgressTextView().setText(String.format(context.getResources().getString(R.string.screen_scan_total_songs), String.valueOf(sumsongs), String.valueOf(values[0])));
		super.onProgressUpdate(values);
	}
	
	@Override
	protected void onPostExecute(Void result) {
		// TODO Auto-generated method stub
		customBuilder.setProgressBar(100);
		ScreenAudio.refreshCount(ScreenAudio.REFRESH_SONGS_COUNT);
		Toast.makeText(context, String.format(context.getResources().getString(R.string.screen_scan_success_songs), String.valueOf(addnum), String.valueOf(ignorenum)), Toast.LENGTH_SHORT).show();
		progressDialog.dismiss();
		if (!cancelscan){
			if(ScreenRecord.getScreenRecordInstance()!=null){
				ScreenRecord.getScreenRecordInstance().loadData(1);
			}
			ServiceManager.getMediaEventService()
			.onMediaUpdateEvent(new MediaEventArgs()
					.setMediaUpdateEventTypes(MediaEventTypes.KMEDIA_UPDATE_DATA));
			
		}
		
		
		//扫描完后判断当前正在播放的歌曲是否被扫描进数据库，如果没有则停止播放
		MediaManagerDB mediadb = ServiceManager.getMediaService().getMediaDB();
		Cursor cursor = mediadb.queryCurPlaySong(MediaApplication.getInstance().getCurSongId());
		if (cursor != null && cursor.getCount() <= 0)
		{
			IMediaEventArgs eventArgs = new MediaEventArgs();
			ServiceManager.getMediaEventService().onMediaUpdateEvent(eventArgs.setMediaUpdateEventTypes(MediaEventTypes.MEDIA_PLAYER_STOP));
		
			// 扫描后之前正在播放的歌曲没被扫描进来，让“正在播放“按钮不可见。
			ServiceManager.getAmtMedia().getGoPlayerBtn().setVisibility(View.INVISIBLE);
			MediaApplication.getInstance().setVisible(false);
			ServiceManager.isPlayed = false;
			AmtMedia.s_goPlayerBtn_click_num = -1;
			Editor sharedata = ServiceManager.getAmtMedia().getSharedPreferences("lastsong", 0).edit();	
			sharedata.putString("listId",""); 
			sharedata.putInt("id",0);
			sharedata.putString("methodName", "");
			
			sharedata.putInt("position",0);
			sharedata.commit(); 
		}
		cursor.close();
		
		((Activity) context).onBackPressed();
		
		super.onPostExecute(result);
		
	}

}
