package com.amusic.media.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.Handler;

import com.amusic.media.MediaApplication;
import com.amusic.media.R;
import com.amusic.media.download.DownloadJob;
import com.amusic.media.download.DownloadLyric;
import com.amusic.media.event.IMediaEventArgs;
import com.amusic.media.event.impl.MediaEventArgs;
import com.amusic.media.services.IMediaService;
import com.amusic.media.services.INetworkService;
import com.amusic.media.services.impl.MediaPlayerService;
import com.amusic.media.services.impl.MediaService;
import com.amusic.media.services.impl.ServiceManager;
import com.amusic.media.task.DownloadTask;

public class ImageUtil {
public static int FREE_SD_SPACE_NEEDED_TO_CACHE = 100;
private static INetworkService networkService = ServiceManager.getNetworkService();
    /**
     * 通过歌手去下载歌星图片
     * @param singerName 歌手名字
     * @return
     */
	public static InputStream getRequest(String singerName, String tempFileName, Handler imageHandler) {		
		HttpEntityEnclosingRequestBase httpRequest = new HttpPost(IMediaService.DOWNLOAD_SERVER_BASE + IMediaService.DOWNLOAD_PICTURE_SERVER_ACTION);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("singer", singerName));
		try {
			httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
			
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				String strResult = EntityUtils.toString(httpResponse.getEntity());
				strResult = strResult.replace("\"", "");
				
				String pictureUrlPart = null;
				
				if (strResult.indexOf("{") + 1 <= strResult.lastIndexOf("}")) {
					strResult = strResult.substring(strResult.indexOf("{") + 1, strResult.lastIndexOf("}"));
					
					String[] items = strResult.split("\\},\\{");
					
					String[] parts = items[0].split(",");
					String[] datas = null;

					for (String part : parts) {
						datas = part.split(":");
						if (datas != null && datas.length == 2 && datas[0].equalsIgnoreCase("encryptname")) {
							pictureUrlPart = datas[1];
							break;
						}
					}
				}
				
				if (pictureUrlPart != null) {
					IMediaEventArgs args = new MediaEventArgs();
					args.putExtra("singerName", singerName);
					downloadImage(IMediaService.DOWNLOAD_SERVER_BASE + pictureUrlPart + ".gs", 0, tempFileName, args, imageHandler);
				}
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		
		return null;
	}
	
	/**
	 * 通过歌手下载图片并将下下来的图片保存为本地文件
	 * @param singerName 歌手名字
	 * @param fileName 文件名字
	 * @param tempFileName 中间文件名字
	 * @return
	 */
	public static void getDrawableFromUrl(String singerName, String fileName, String tempFileName, Handler imageHandler){
		if(Constant.PROHIBITED_TO_DOWNLOAD_PICTURE == Constant.PICTRUE_DOWNLOAD){
			return;
		} else if (Constant.ALLOWED_TO_DOWNLOAD_PICTURE_WITH_WIFI == Constant.PICTRUE_DOWNLOAD) {
			if(!(networkService.acquire(false) && networkService.getNetType() == ConnectivityManager.TYPE_WIFI)) {
				return;
			}
		}
		getRequest(singerName, tempFileName, imageHandler);
	}
	
	/**
	 * 若歌曲tag中的歌手为空的情况下，则先按文件名搜索歌词，如果搜索歌词返回结果只有一个
	 * 则将返回结果的歌手信息写如tag中并调用上面重载方法重新下载歌手图片
	 * @param songName 歌曲名字
	 * @param audiofilePath 歌曲路径
	 * @return
	 */
	public static void getDrawableFromUrl(String songName,String audiofilePath, Handler imageHandler){
		String song_Name = songName.replaceAll(" ", "").toLowerCase();
		song_Name = MediaPlayerService.splitTitle(song_Name);
		HttpEntityEnclosingRequestBase httpRequest = new HttpPost(IMediaService.DOWNLOAD_SERVER_BASE + IMediaService.DOWNLOAD_LYRICS_SERVER_ACTION);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("song", song_Name));
		params.add(new BasicNameValuePair("duration", String.valueOf(0)));

		try {
			httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
			
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				String strResult = EntityUtils.toString(httpResponse.getEntity());
				strResult = strResult.replace("\"", "");
				
				String pictureUrlPart = null;
				
				if (strResult.indexOf("{") + 1 <= strResult.lastIndexOf("}")) {
					strResult = strResult.substring(strResult.indexOf("{") + 1, strResult.lastIndexOf("}"));
					
					
					String[] items = strResult.split("\\},\\{");
					if (items.length > 1) {
						return;
					}
					
					String[] parts = strResult.split(",");
					String[] datas = null;

					for (String part : parts) {
						datas = part.split(":");
						if (datas != null && datas.length == 2 && datas[0].equalsIgnoreCase("originalname")) {
							pictureUrlPart = datas[1];
							String singer = DownloadLyric.utf2unicode(pictureUrlPart);
							int index = singer.indexOf('-');
							singer = singer.substring(index + 1);
							index = singer.indexOf('-');
							singer = singer.substring(0,index);
							String picturePath = MediaApplication.singerPicturesPath + singer + IMediaService.PICTURE_SUFFIX;
							String pictureTempPath = MediaApplication.singerPicturesPath + singer + IMediaService.TMP_SUFFIX;
							getDrawableFromUrl(singer,picturePath, pictureTempPath, imageHandler);
							//Bitmap drawable = BitmapFactory.decodeResource(MediaApplication.getContext().getResources(), R.drawable.screen_audio_default_singer_picture);
							MediaService.writeTag(audiofilePath, singer);
							//return drawable;
							return;
						}
					}
				}
				
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		//return BitmapFactory.decodeResource(MediaApplication.getContext().getResources(), R.drawable.screen_audio_default_singer_picture);
	}
    
	public static Bitmap acquireDrawable(String singerName, Handler imageHandler) throws MalformedURLException{
		Bitmap singerImage = null;
		String picturePath = MediaApplication.singerPicturesPath + singerName + IMediaService.PICTURE_SUFFIX;
		String pictureTempPath = MediaApplication.singerPicturesPath + singerName + IMediaService.TMP_SUFFIX;
		File pictureFile = new File(picturePath);
		if (pictureFile.exists()) {
			singerImage = BitmapFactory.decodeFile(picturePath);
			BitmapCache.getInstance().addCacheBitmap(singerImage, singerName);
		} else {
			ImageUtil.getDrawableFromUrl(singerName, picturePath, pictureTempPath, imageHandler);
		}
		return singerImage;
	}
	
	private static void downloadImage(String url, long startPos, String path, IMediaEventArgs args, final Handler imageHandler) {
		if(!MediaApplication.getInstance().getDownloadTaskList().contains(path)) {
			final DownloadJob mJob = new DownloadJob(args);
			mJob.setDownloadUrl(url);
			mJob.setDownloadStartPos(startPos);
			mJob.setPath(path);
			ServiceManager.getAmtMedia().getHandler().post(new Runnable() {
				@Override
				public void run() {
					DownloadTask mDownloadTask = new DownloadTask(mJob);
					mDownloadTask.registerHandler(imageHandler);
					mJob.setDownloadTask(mDownloadTask);
					mDownloadTask.execute(IMediaService.DOWNLOAD_START_ON_IMAGE);
				}
			});
		} else {
			MediaApplication.logD(ImageUtil.class, "getDownloadTaskList():" + path);
		}
	}
}
