package com.android.media;

import java.io.File;
import java.util.ArrayList;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Environment;
import android.util.Log;
import android.view.WindowManager;

import com.android.media.provider.MediaManagerDB;
import com.android.media.services.impl.ServiceManager;
import com.android.media.utils.Constant;

public class MediaApplication extends Application {
	private static MediaApplication instance;
//	private HashMap<String, SoftReference<Drawable>> imageCache;
//	private HashMap<String, SoftReference<Bitmap>> bitmapCache;
	public final static String TAG = "aMusic";
	private WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
	private static int SCREEN_WIDTH;
	private static int SCREEN_HEIGHT;
	private static int SOFT_IN_SCREEN_WIDTH;
	private static int SOFT_IN_SCREEN_HEIGHT;
	private boolean micFlag = false;
	private boolean downloadApkFlag = false;
	private ArrayList<String> adddirlist;
	private boolean clearflag;
	private boolean isPlaying_Buttom_Visible = false;
	private String curSongName;
	private String curSongArtist;
	private Integer curSongId;
	private ArrayList<String> skinsPath;
	private ArrayList<String> downloadTaskList;
	//文件存储路径
	public static String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
	public static String savePath = sdcard + "/aMusic/";
	public static String ScanSavePath = sdcard + "/aMusic";
	public static String deleteRecordPath = "[\\s\\S]*?/sdcard/aMusic/record";
	public static String lyricPath = savePath + "lyrics/";
	public static String packagePath = savePath + "package/";
	public static String accompanyPath = savePath + "accompany/";
	public static String singerPicturesPath = savePath + "pictures/";
	public static String crashPath = savePath + "crash/";
	public static String skinPath = savePath + "skins/";
	public static String skinThumbnailPath = savePath + "skins/thumbnails/";
	public static String DEFAULT_SKIN;
	public static int color_normal = 0xFF1F0A07;
	public static int color_highlight = 0xFF1F0A07;
	public static boolean networkIsOk = false;
	
	public ArrayList<String> getSkinsPath() {
		return skinsPath;
	}

	public boolean isDownloadApkFlag() {
		return downloadApkFlag;
	}

	public void setDownloadApkFlag(boolean downloadApkFlag) {
		this.downloadApkFlag = downloadApkFlag;
	}

	public void setMicFlag(boolean micFlag) {
		this.micFlag = micFlag;
	}
	
	public boolean getMicFlag() {
		return micFlag;
	}
	
	public void setVisible(boolean isvisible) {
		isPlaying_Buttom_Visible = isvisible;
	}
	
	public boolean isVisible() {
		return isPlaying_Buttom_Visible;
	}

	public WindowManager.LayoutParams getWmParams() {
		return wmParams;
	}

	@Override
	public void onCreate() {
		DEFAULT_SKIN = "灿烂星空.jpg";
//		imageCache = new HashMap<String, SoftReference<Drawable>>();
//		bitmapCache = new HashMap<String, SoftReference<Bitmap>>();
		instance = this;
		skinsPath = new ArrayList<String>();
		downloadTaskList = new ArrayList<String>();
		traverseSkinFolder(skinPath);
		SOFT_IN_SCREEN_WIDTH=((WindowManager)getSystemService("window")).getDefaultDisplay().getWidth();
		SOFT_IN_SCREEN_HEIGHT=((WindowManager)getSystemService("window")).getDefaultDisplay().getHeight();
		super.onCreate();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		//System.out.println("onConfigurationChanged");
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		SCREEN_WIDTH=((WindowManager)getSystemService("window")).getDefaultDisplay().getWidth();
		SCREEN_HEIGHT=((WindowManager)getSystemService("window")).getDefaultDisplay().getHeight();
		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			Constant.DESKTOP_LYRIC_WIDTH = ((WindowManager)getSystemService("window")).getDefaultDisplay().getWidth();
//			wmParams.width = Constant.DESKTOP_LYRIC_WIDTH;
			
//			wmParams.x = 0;
			
//			WindowManager wm = (WindowManager) getContext()
//			.getApplicationContext().getSystemService("window");
//			DesktopLyricService desktopLyric = DesktopLyricService.getInstance();
//			if(desktopLyric != null && desktopLyric.getDesktopView() != null){
//				wm.updateViewLayout(desktopLyric.getDesktopView(), wmParams);
//			}
//			Log.d("=III=","MediaApplication onConfigurationChanged width = " + wmParams.width);
		}else if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
//			Constant.DESKTOP_LYRIC_WIDTH = ((WindowManager)getSystemService("window")).getDefaultDisplay().getWidth();
//			wmParams.width = Constant.DESKTOP_LYRIC_WIDTH;
//			Log.d("=III=","MediaApplication onConfigurationChanged width = " + wmParams.width);
//			wmParams.x = (MediaApplication.getScreenWidth() - wmParams.width) / 2;
//			WindowManager wm = (WindowManager) getContext()
//			.getApplicationContext().getSystemService("window");
//			DesktopLyricService desktopLyric = DesktopLyricService.getInstance();
//			if(desktopLyric != null && desktopLyric.getDesktopView() != null){
//				wm.updateViewLayout(desktopLyric.getDesktopView(), wmParams);
//			}
		}
	}
	
	public WindowManager.LayoutParams getWmManager() {
		return wmParams;
	}

	public static Context getContext() {
		return instance;
	}

//	public HashMap<String, SoftReference<Drawable>> getImageCache() {
//		return imageCache;
//	}
//	
//	
//
//	public HashMap<String, SoftReference<Bitmap>> getBitmapCache() {
//		return bitmapCache;
//	}


	public static MediaApplication getInstance() {
		return instance;
	}
	
	public static int getScreenWidth(){
		return SCREEN_WIDTH;
	}
	
	public static int getScreenHeight(){
		return SCREEN_HEIGHT;
	}
	
	public static int getSoftInScreenWidth(){
		return SOFT_IN_SCREEN_WIDTH;
	}
	
	public static int getSoftInScreenHeight(){
		return SOFT_IN_SCREEN_HEIGHT;
	}
	
	public static int getSDKVersion() {
		return Integer.valueOf(android.os.Build.VERSION.SDK);
	}
	
	public static void logD(Class context, String info){
		//Log.d(TAG, context.getSimpleName() + " --> " +info);
	}
	
	public void addScanAddDir(ArrayList<String> adddir)
	{
		if (adddirlist == null)
		{
			adddirlist = new ArrayList<String>(adddir);
		}
		else
		{
			adddirlist.addAll(adddir);
		}
	}
	
	public ArrayList<String> getScanAddDir()
	{
		return adddirlist;
	}
	
	public void clearScanAddDir()
	{
		if (adddirlist != null)
		{
			adddirlist.clear();
		}
	}
	
	public void setContain(boolean flag)
	{
		if (!flag)
		{
			clearScanAddDir();
		}
	}
	
	public void setClearFlag(boolean flag)
	{
		clearflag = flag;
	}
	
	public boolean getClearFlag()
	{
		return clearflag;
	}
	
	public void setCurSonginfo(String curSongName,String curSongArtist)
	{
		this.curSongName = curSongName;
		this.curSongArtist = curSongArtist;
		MediaManagerDB mediadb = ServiceManager.getMediaService().getMediaDB();
		this.curSongId = mediadb.querySongId(curSongName + ".mp3",curSongArtist);
	}
	
	public String getCurSongName()
	{
		return curSongName;
	}
	
	public String getcurSongArtist()
	{
		return curSongArtist;
	}
	
	public Integer getCurSongId()
	{
		return curSongId == null ? -1 : curSongId;
	}
	
	
	public ArrayList<String> getDownloadTaskList() {
		return downloadTaskList;
	}

	public void setDownloadTaskList(ArrayList<String> downloadTaskList) {
		this.downloadTaskList = downloadTaskList;
	}

	public void traverseSkinFolder(String path) {
		skinsPath.add(DEFAULT_SKIN);
		File file = new File(path);
		if (!file.exists()) {
			return;
		}
		if (!file.isDirectory()) {
			return;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempList[i]);
			} else {
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile() && temp.getName().endsWith(".pf")) {
				skinsPath.add(temp.getAbsolutePath());
			}
			if (temp.isDirectory()) {
			    continue;
			}
		}
	}
}
