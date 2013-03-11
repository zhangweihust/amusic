package com.amusic.media.task;

import java.io.File;
import java.util.ArrayList;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.view.Gravity;

import com.amusic.media.R;
import com.amusic.media.dialog.OnScreenHint;
import com.amusic.media.model.SongInfo;
import com.amusic.media.screens.impl.ScreenRecord;
import com.amusic.media.services.impl.MediaPlayerService;
import com.amusic.media.services.impl.ServiceManager;

public class ScreenRecordTask extends AsyncTask<Void, Void, ArrayList<SongInfo>> {
	private ScreenRecord context;
	private int type;
	private static int INIT = 0;
	private static int REFRESH = 1;
	private String suffix = ".mp3";
	private ProgressDialog progressDialog;
	private boolean IsNeedDialog;
	
	public ScreenRecordTask(ScreenRecord context,int type,boolean needdialog){
		this.context = context;
		this.type = type;
		this.IsNeedDialog = needdialog;
		if (needdialog)
		{
			progressDialog = new ProgressDialog(ServiceManager.getAmtMedia());
		}
	}
	
	@Override
	protected void onPreExecute() {
		if (IsNeedDialog)
		{
			progressDialog.show();
		}
		
		super.onPreExecute();
	}
	
	@Override
	protected ArrayList<SongInfo> doInBackground(Void... params) {
		return getRecordList();
	}
	
	public ArrayList<SongInfo> getRecordList(){
		String recordPath = MediaPlayerService.directoryRecord;
		File dir = new File(recordPath);
		if(!dir.exists()){
			dir.mkdir();
		}
		File[] files = dir.listFiles();
		ArrayList<SongInfo> recordSongs = null;
		if(files != null && files.length > 0){
			recordSongs = new ArrayList<SongInfo>();
			boolean flag = false;
			for(int i=0;i<files.length;i++){
				
				if(!files[i].getName().contains("_mp3") && !files[i].getName().contains("_wav")){
					continue;
				}
				if(files[i].getName().contains("_mp3")){
					if(!files[i].getName().substring(0, files[i].getName().lastIndexOf("_mp3")).contains("-")){
						continue;
					}
				}
				
				if(files[i].getName().contains("_wav")){
					if(!files[i].getName().substring(0, files[i].getName().lastIndexOf("_wav")).contains("-")){
						continue;
					}
				}
				String songName = null;
				try{
					if(files[i].getName().contains("_mp3")){
						songName = files[i].getName().substring(0, files[i].getName().lastIndexOf("_mp3"));
					}else if(files[i].getName().contains("_wav")){
						songName = files[i].getName().substring(0, files[i].getName().lastIndexOf("_wav"));
					}
				  
				}catch(Exception e){
					e.printStackTrace();
					File tmp = new File(files[i].getAbsolutePath());
					tmp.delete();
				}
				if(!songName.contains("-")){
					continue;
				}
				
				SongInfo songInfo = new SongInfo();
				String tmp = makeSong(songName)[1];
				songInfo.setSongName(tmp);
				
				if(tmp.indexOf("_")==-1){
					continue;
				}

				if(tmp.indexOf("_")==tmp.length()-1){
					continue;
				}
				songInfo.setSingerName(makeSong(songName)[0]);
				songInfo.setDirectory(files[i].getAbsolutePath());
				recordSongs.add(songInfo);
				
			}

		}
		return recordSongs;
	}
	private String[] makeSong(String songName){
		String[] song = new String[2];
		song[0] = "";
		song[1] = "";
		if(songName.contains("-")){
			song[0] = songName.substring(0, songName.indexOf("-"));
		}
		if(songName.indexOf("-")+1<songName.length()){
			song[1] = songName.substring(songName.indexOf("-")+1, songName.length());
		}
		
		return song;
	}
//	private String makeSongName(String songName){
//		String regularExpression = "[-\\s_]";
//		String temp = null;
//		int mark = -1;
//		if(songName != null){
//			songName = songName.substring(0, songName.lastIndexOf("."));
//			for(int i = songName.length()-1 ;i >= 0; i--){
//				temp = ((Character)songName.charAt(i)).toString();
//				if(temp.matches(regularExpression)){
//					mark = i;
//					break;
//				}
//			}
//			if(mark != -1){
//				songName = songName.substring(mark+1);
//			}
//		}
//		return songName;
//	}
	@Override
	protected void onPostExecute(ArrayList<SongInfo> result) {
		if (IsNeedDialog)
		{
			if(progressDialog.isShowing()){
				progressDialog.dismiss();
				progressDialog = null;
			}
		}
		if(type == INIT){
			context.initView(result);
			if(result == null ||result.size()==0){
				if(ScreenRecord.mOnScreenHint != null){
					ScreenRecord.mOnScreenHint.cancel();
				}
				ScreenRecord.mOnScreenHint = OnScreenHint.makeText_Empty(context, false, ServiceManager.getAmtMedia().getResources().getString(R.string.record_content_empty_info0), ServiceManager.getAmtMedia().getResources().getString(R.string.record_content_empty_info1), ServiceManager.getAmtMedia().getResources().getString(R.string.record_content_empty_info2),ServiceManager.getAmtMedia().getResources().getString(R.string.record_content_empty_info3));   //设置toast要显示的信息
				ScreenRecord.mOnScreenHint.getView().setBackgroundResource(R.drawable.content_empty_left);
				ScreenRecord.mOnScreenHint.setPosition(Gravity.RIGHT|Gravity.BOTTOM,context.getWindowManager().getDefaultDisplay().getWidth()/8*5 - OnScreenHint.dip2px(context, 142) , ServiceManager.getAmtMedia().getResources().getDimensionPixelSize(
	                R.dimen.hint_y_offset));
				ScreenRecord.mOnScreenHint.show();
			}
		}else if(type == REFRESH){
			context.refresh(result);
		}
		super.onPostExecute(result);
	}

}
