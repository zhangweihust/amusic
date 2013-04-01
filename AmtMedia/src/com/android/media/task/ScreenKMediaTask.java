package com.android.media.task;

import java.io.File;
import java.text.CollationKey;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.view.Gravity;

import com.amusic.media.R;
import com.android.media.MediaApplication;
import com.android.media.dialog.OnScreenHint;
import com.android.media.model.SongInfo;
import com.android.media.screens.impl.ScreenKMedia;
import com.android.media.services.impl.ServiceManager;

public class ScreenKMediaTask extends AsyncTask<Void, Void, ArrayList<SongInfo>> {
	private ScreenKMedia context;
	private int type;
	private static int INIT = 0;
	private static int REFRESH = 1;
	private String suffix = ".bz";
	private ProgressDialog progressDialog;
	private boolean isNeedProgressDialog = true;
	
	public ScreenKMediaTask(ScreenKMedia context,int type,boolean isNeedProgressDialog){
		this.context = context;
		this.type = type;
		this.isNeedProgressDialog = isNeedProgressDialog;
		if(isNeedProgressDialog){
			progressDialog = new ProgressDialog(ServiceManager.getAmtMedia());
		}
	}
	
	@Override
	protected void onPreExecute() {
		if(isNeedProgressDialog){
			progressDialog.show();
		}
		super.onPreExecute();
	}
	
	@Override
	protected ArrayList<SongInfo> doInBackground(Void... params) {
		return getAccompanyList();
	}
	
	public ArrayList<SongInfo> getAccompanyList(){
		String accompanyPath = MediaApplication.accompanyPath;
		File dir = new File(accompanyPath);
		if(!dir.exists()){
			dir.mkdir();
		}
		File[] files = dir.listFiles();
		ArrayList<SongInfo> kmediaSongs = null;
		if(files != null && files.length > 0){
			kmediaSongs = new ArrayList<SongInfo>();
			for(int i=0;i<files.length;i++){
				String songName = files[i].getName();
				if(suffix.equals(songName.substring(songName.lastIndexOf(".")))){
					SongInfo songInfo = new SongInfo();
					songInfo.setSongName(makeSong(songName)[0]);
					songInfo.setSingerName(makeSong(songName)[1]);
					songInfo.setDirectory(files[i].getAbsolutePath());
					kmediaSongs.add(songInfo);
				}
			}

		}
		
		Comparator<SongInfo> comparator = new Comparator<SongInfo>() {
			private Collator collator = Collator.getInstance();
			@Override
			public int compare(SongInfo lhs, SongInfo rhs) {
				// TODO Auto-generated method stub
				CollationKey key1=collator.getCollationKey(lhs.getSongName().toLowerCase());
				CollationKey key2=collator.getCollationKey(rhs.getSongName().toLowerCase());
				return key1.compareTo(key2);
			}
		};
		
		if(kmediaSongs != null && kmediaSongs.size() > 1){
			SongInfo[] tempkmediaSongs = (SongInfo[]) kmediaSongs.toArray(new SongInfo[kmediaSongs.size()]);
			Arrays.sort(tempkmediaSongs, comparator);
			ArrayList<SongInfo> realkmediaSongs = new ArrayList<SongInfo>();
			for(int i = 0; i< tempkmediaSongs.length; i++){
				realkmediaSongs.add(tempkmediaSongs[i]);
			}
			return realkmediaSongs;
		}else{
			return kmediaSongs;
		}
	}
	
	
	
	private String[] makeSong(String songName){
		String[] song = new String[2];
		if(songName.contains("-")){
			song[0] = songName.substring(songName.lastIndexOf("-")+1, songName.lastIndexOf("."));
			song[1] = songName.substring(0, songName.lastIndexOf("-"));
		}else{
			song[0] = songName.substring(0, songName.lastIndexOf("."));
			song[1] = "";
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
		if(isNeedProgressDialog){
			if(progressDialog.isShowing()){
				progressDialog.dismiss();
				progressDialog = null;
			}
		}
		if(type == INIT){
			context.initView(result);
			if(result == null ||result.size()==0){
				if(ScreenKMedia.mOnScreenHint != null){
					ScreenKMedia.mOnScreenHint.cancel();
				}
				ScreenKMedia.mOnScreenHint = OnScreenHint.makeText_Empty(context, false, ServiceManager.getAmtMedia().getResources().getString(R.string.kmedia_content_empty_info0), ServiceManager.getAmtMedia().getResources().getString(R.string.kmedia_content_empty_info1), ServiceManager.getAmtMedia().getResources().getString(R.string.kmedia_content_empty_info2),ServiceManager.getAmtMedia().getResources().getString(R.string.kmedia_content_empty_info3));   //设置toast要显示的信息
				ScreenKMedia.mOnScreenHint.getView().setBackgroundResource(R.drawable.content_empty_right);
				ScreenKMedia.mOnScreenHint.setPosition(Gravity.RIGHT|Gravity.BOTTOM,context.getWindowManager().getDefaultDisplay().getWidth()/8-OnScreenHint.dip2px(context, 49), ServiceManager.getAmtMedia().getResources().getDimensionPixelSize(
		                R.dimen.hint_y_offset));
				ScreenKMedia.mOnScreenHint.show();
			}
		}else if(type == REFRESH){
			context.refresh(result);
		}
		super.onPostExecute(result);
	}

}