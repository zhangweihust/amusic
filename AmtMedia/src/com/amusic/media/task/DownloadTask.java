/*
 * Copyright (C) 2009 Teleca Poland Sp. z o.o. <android@teleca.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.amusic.media.task;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;

import com.amusic.media.MediaApplication;
import com.amusic.media.R;
import com.amusic.media.dialog.OnScreenHint;
import com.amusic.media.download.DownloadApk;
import com.amusic.media.download.DownloadJob;
import com.amusic.media.event.impl.MediaEventTypes;
import com.amusic.media.provider.MediaDatabaseHelper;
import com.amusic.media.services.IMediaEventService;
import com.amusic.media.services.IMediaService;
import com.amusic.media.services.INetworkService;
import com.amusic.media.services.impl.ServiceManager;
import com.amusic.media.toolbox.DETool;
import com.amusic.media.utils.BitmapCache;
import com.amusic.media.view.RemoteImageView;

public class DownloadTask extends AsyncTask<Integer, Integer, Boolean> {

	DownloadJob mJob;
	private final INetworkService networkService;
	private final IMediaEventService mediaEventService;
	private String DownloadUrl; // 下载的地址
	private int BAIDUCANT = -8; // 百度无法下载标识
	private int OK99CANT = -1; // esou无法下载标识
	private int SINGCANT = -2; // 5sing无法下载标识
	private int max_asc_map = 200; // 对应asc码表,总共分配了200，确保足够
	private int startnum = 0; // 0-9数字表
	private int endnum = 9; // 9的位置
	private int num_asc_map = 48; // 数字0的asc码是48，数字asc码表的起始值
	private int startA_Z = 10; // 0-9 10个数字排完，接着是大写的26个字幕；起始位置是0+10=10；
	private int endA_Z = 35; // Z的位置为10+26-1；
	private int A_Z_asc_map = 55; // A的asc码是65，但是算法中获取A的asc码需要加上startA_Z的值，所以起始值为65-10=55;
	private int starta_z = 36; // 接着是小写的a-z；起始位置是10+26=36；
	private int enda_z = 61; // Z的位置为10+26+26-1；
	private int a_z_asc_map = 61; // a的asc码是97，但是算法中获取a的asc码需要加上starta_z的值，所以起始值为97-36=61;
	private int[] asc_arr1 = new int[max_asc_map];
	private int[] asc_arr2 = new int[max_asc_map];
	private long interval = 2 * 1000;
	private int retryTimes = 20;
	private long totalSize = 0;
	private static String FinalUrl; // 挑选出来的下载的地址
	private static String FinalWmaUrl; // 挑选出来的下载的地址
	private static String commonUrl; // 一般的下载的地址
	private static float commonSize; // 一般的下载的文件大小
	private static String cleanUrl; // 全字匹配的下载的地址
	private static float cleanSize; // 全字匹配的下载的文件大小
	private static String originalUrl; // 含原版的下载的地址
	private static float originalSize; // 含原版的下载的文件大小
	private static String m_5singcookie=""; // 5sing的cookie
	public static final int MESSAGE_WHAT_DOWNLOADING = 1001;
	public static final int MESSAGE_WHAT_DOWNLOADED = 1002;
	public static final int MESSAGE_WHAT_DOWNLOADED_ERROR = 1003;
	
	public static final String XCODE_INFO_SERVER_URL = IMediaService.DOWNLOAD_SERVER_BASE + "xcode.cfg";
	/**
	 * The minimum amount of progress that has to be done before the progress
	 * bar gets updated
	 */
	public static final int MIN_PROGRESS_STEP = 1024 * 4;

	/**
	 * The minimum amount of time that has to elapse before the progress bar
	 * gets updated, in ms
	 */
	public static final long MIN_PROGRESS_TIME = 2000;

	public final static int ERROR_NONE = 0;
	public final static int ERROR_SD_NO_MEMORY = 1;
	public final static int ERROR_BLOCK_INTERNET = 2;
	public final static int ERROR_BLOCK_TEMPFILE_DELETE = 3;
	public final static int ERROR_BLOCK_NEED_TO_CHANGE_DOWNLOAD_RESOURCE = 4;
	public final static int ERROR_UNKONW = 5;
	public final static int TIME_OUT = 60000;
	private final static int BUFFER_SIZE = 1024 * 4;
	private final static int DOWNLOAD_VISIABLE = 1;
	private final static int DOWNLOAD_INVISIABLE = 2;
	private final static int DOWNLOAD_GETURL_ORIGINAL = 1;
	private final static int DOWNLOAD_GETURL_ACCOMPANY = 2;
	private int errStausCode = ERROR_NONE;
	private boolean interrupt = false;
	private RandomAccessFile outputStream;
	private long networkSpeed; // 网速
	private long previousTime;
	private long totalTime;
	private long downloadSize;
	private Throwable exception;
	HttpGet httpRequest;
	private int downloadType;
	private int downloadVisable = DOWNLOAD_INVISIABLE;
	ContentValues values = new ContentValues();
	private Handler progressHandler;
	SharedPreferences preferences;
	private Bitmap image;
	private OnScreenHint mOnScreenHint;
	public DownloadTask(DownloadJob job) {
		mJob = job;
		mediaEventService = ServiceManager.getMediaEventService();
		networkService = ServiceManager.getNetworkService();
		preferences = ServiceManager.getAmtMedia().getSharedPreferences(DownloadApk.XML_NAME,Context.MODE_WORLD_WRITEABLE);
		mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.get_network_failed));
	}

	private final class ProgressReportingRandomAccessFile extends RandomAccessFile {
		private int progress = 0;

		public ProgressReportingRandomAccessFile(File file, String mode) throws FileNotFoundException {
			super(file, mode);
		}

		@Override
		public void write(byte[] buffer, int offset, int count) throws IOException {
			super.write(buffer, offset, count);
			progress += count;
			publishProgress(progress);
		}
	}

	@Override
	public void onPreExecute() {
		MediaApplication.logD(MediaApplication.class,"开启一条下载线程1:" + mJob.getPath() + " size:" + mJob.getDownloadStartPos());
		previousTime = System.currentTimeMillis();
		MediaApplication.getInstance().getDownloadTaskList().add(mJob.getPath());
		super.onPreExecute();
	}

	@Override
	public Boolean doInBackground(Integer... params) {
		downloadType = params[0];
		try {
			switch(params[0]){
			case IMediaService.DOWNLOAD_START_ON_RANGE:
				downloadVisable = DOWNLOAD_VISIABLE;
			case IMediaService.DOWNLOAD_START_ON_APK:
			case IMediaService.DOWNLOAD_START_ON_IMAGE:
			case IMediaService.DOWNLOAD_START_ON_LYRICS:
			case IMediaService.DOWNLOAD_START_ON_LRC_LYRICS:	
			case IMediaService.DOWNLOAD_START_ON_SKIN:
				break;
			case IMediaService.DOWNLOAD_START_ON_ZERO:
				downloadVisable = DOWNLOAD_VISIABLE;
				getDownloadUrl(mJob);
				break;
			default:
				MediaApplication.logD(MediaApplication.class,"没理由进来");
				return false;
			}
			download(mJob);
			return true;
		} catch (SocketException e) {
			interrupt = true;
			exception = e;
			errStausCode = ERROR_UNKONW;
			MediaApplication.logD(MediaApplication.class,"SocketException:" + e);
			return false;
		} catch (Exception e) {
			interrupt = true;
			exception = e;
			errStausCode = ERROR_UNKONW;
			MediaApplication.logD(DownloadTask.class, "ERROR_UNKONW-->input = catch (Exception e) :" + e);
			return false;
		} 

	}

	@Override
	public void onPostExecute(Boolean result) {
		if(httpRequest != null)
		httpRequest.abort();
		MediaApplication.logD(DownloadTask.class, "onPostExecute:" + mJob.getPath());
		MediaApplication.getInstance().getDownloadTaskList().remove(mJob.getPath());
		if (downloadType == IMediaService.DOWNLOAD_START_ON_APK){
			MediaApplication.getInstance().setDownloadApkFlag(false);
		}
		if (interrupt) {
			if (errStausCode != ERROR_NONE) {
				if (downloadType == IMediaService.DOWNLOAD_START_ON_LYRICS){
					mJob.getArgs().putExtra("show", true);
					mediaEventService.onMediaUpdateEvent(mJob.getArgs().setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_LYRICS_ERROR));
				    return;
				} else if (downloadType == IMediaService.DOWNLOAD_START_ON_IMAGE) {
					mediaEventService.onMediaUpdateEvent(mJob.getArgs().setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_IMAGE_ERROR));
					return;
				} else if (downloadType == IMediaService.DOWNLOAD_START_ON_SKIN) {
					Message imageMessage = new Message();
					imageMessage.arg1 = 2;
					imageMessage.what = MESSAGE_WHAT_DOWNLOADED;
					if (progressHandler != null) 
					progressHandler.sendMessage(imageMessage);
					return;
				} else if (downloadType == IMediaService.DOWNLOAD_START_ON_APK) {
					Message imageMessage = new Message();
					imageMessage.what = MESSAGE_WHAT_DOWNLOADED_ERROR;
					if (progressHandler != null) 
					progressHandler.sendMessage(imageMessage);
					return;
				} else if (downloadType == IMediaService.DOWNLOAD_START_ON_LRC_LYRICS) {
					mediaEventService.onMediaUpdateEvent(mJob.getArgs().setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_LRC_LYRICS_ERROR));
				    return;
				}
				switch(errStausCode){
				case ERROR_BLOCK_INTERNET:
					MediaApplication.logD(DownloadTask.class, "download error: ERROR_BLOCK_INTERNET");
					mJob.getArgs().putExtra("downloadId", mJob.getDownloadId());
					mediaEventService.onMediaUpdateEvent(mJob.getArgs().setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_PAUSE));
//					mOnScreenHint.cancel();
//					mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.get_network_failed));
//					mOnScreenHint.show();
					ServiceManager.getAmtMediaHandler().post(new Runnable() {
						@Override
						public void run() {
							if(mOnScreenHint!=null){
							    mOnScreenHint.cancel();
							}
							mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.get_network_failed));
							mOnScreenHint.show();
						}
					});
					break;
				case ERROR_SD_NO_MEMORY:
					MediaApplication.logD(DownloadTask.class, "download error: ERROR_SD_NO_MEMORY");
					mJob.getArgs().putExtra("downloadId", mJob.getDownloadId());
					mediaEventService.onMediaUpdateEvent(mJob.getArgs().setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_PAUSE));
//					mOnScreenHint.cancel();
//					mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.sd_card_not_enough_space));
//					mOnScreenHint.show();
					ServiceManager.getAmtMediaHandler().post(new Runnable() {
						@Override
						public void run() {
							if(mOnScreenHint!=null){
							    mOnScreenHint.cancel();
							}
							mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.sd_card_not_enough_space));
							mOnScreenHint.show();
						}
					});
				    break;
				case ERROR_BLOCK_TEMPFILE_DELETE:
					MediaApplication.logD(DownloadTask.class, "download error: ERROR_BLOCK_TEMPFILE_DELETE");
					mJob.getArgs().putExtra("downloadId", mJob.getDownloadId());
					mediaEventService.onMediaUpdateEvent(mJob.getArgs().setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_PAUSE));
					break;
				case ERROR_UNKONW:
					MediaApplication.logD(DownloadTask.class, "download error: ERROR_UNKONW");
					mJob.getArgs().putExtra("downloadId", mJob.getDownloadId());
					mediaEventService.onMediaUpdateEvent(mJob.getArgs().setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_PAUSE));
					break;
				case ERROR_BLOCK_NEED_TO_CHANGE_DOWNLOAD_RESOURCE:
					MediaApplication.logD(DownloadTask.class, "download error: ERROR_BLOCK_NEED_TO_CHANGE_DOWNLOAD_RESOURCE");
					mJob.getArgs().putExtra("show", true);
					mJob.getArgs().putExtra("resource", mJob.getDownloadResource());
					mJob.getArgs().putExtra("downloadType", mJob.getDownloadType());
					mJob.getArgs().putExtra("downloadId", mJob.getDownloadId());
					mediaEventService.onMediaUpdateEvent(mJob.getArgs().setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_ERROR));
					break;
				
				}
			}
			return;
		}
		if (exception != null) {
			MediaApplication.logD(DownloadTask.class, "Download failed. " + exception);
			mJob.getArgs().putExtra("downloadId", mJob.getDownloadId());
			mediaEventService.onMediaUpdateEvent(mJob.getArgs().setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_PAUSE));
			return;
		}
	}

	@Override
	public void onCancelled() {
		super.onCancelled();
		interrupt = true;
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		totalTime = System.currentTimeMillis() - previousTime;
		downloadSize = progress[0];
		networkSpeed = downloadSize / totalTime;

	}
	
	
	/** 
     *  异常自动恢复处理 
     *  使用HttpRequestRetryHandler接口实现请求的异常恢复 
     */  
    private static HttpRequestRetryHandler requestRetryHandler = new HttpRequestRetryHandler() {  
        // 自定义的恢复策略  
        public synchronized boolean retryRequest(IOException exception, int executionCount, HttpContext context) {  
            // 设置恢复策略，在发生异常时候将自动重试3次  
        	MediaApplication.logD(DownloadTask.class, "在发生异常时候将自动重试3次:" + executionCount);
            if (executionCount > 3) {    
                // 超过最大次数则不需要重试    
            	MediaApplication.logD(DownloadTask.class, "超过最大次数则不需要重试");
                return false;    
            }    
            if (exception instanceof NoHttpResponseException) {    
                // 服务停掉则重新尝试连接    
                return true;    
            }    
            if (exception instanceof SSLHandshakeException) {    
                // SSL异常不需要重试    
                return false;    
            }   
            HttpRequest request = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);  
            boolean idempotent = (request instanceof HttpEntityEnclosingRequest);  
            if (!idempotent) {  
                // 请求内容相同则重试  
                return true;  
            }  
            return false;  
        }  
    };  

	private long download(final DownloadJob mJob) throws Exception {
		MediaApplication.logD(MediaApplication.class,"开启一条下载线程2:" + mJob.getPath() + " size:" + mJob.getDownloadStartPos());
		if (!isOnline()) {
			errStausCode = ERROR_BLOCK_INTERNET;
			interrupt = true;
			return 0l;
		}
		if (mJob.getDownloadUrl() == null || "".equals(mJob.getDownloadUrl()) ){
			errStausCode = ERROR_BLOCK_NEED_TO_CHANGE_DOWNLOAD_RESOURCE;
			interrupt = true;
			return 0l;
		}
		String url = mJob.getDownloadUrl();
		url = url.replace("\r", ""); 
		url = url.replace("\n", "");
		mJob.setDownloadUrl(url);
		MediaApplication.logD(DownloadTask.class, mJob.getPath() + ": " + mJob.getDownloadUrl());
		httpRequest = new HttpGet(mJob.getDownloadUrl());
		HttpParams httpParameters = new BasicHttpParams();
		// 请求超时
		int timeoutConnection = 30000;
		HttpConnectionParams.setConnectionTimeout(httpParameters,
				timeoutConnection);
		// Set the default socket timeout (SO_TIMEOUT)
		// 接收返回数据超时
		int timeoutSocket = 30000;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
//		DefaultHttpRequestRetryHandler retryHandler = new DefaultHttpRequestRetryHandler(3, true);
		DefaultHttpClient client = new DefaultHttpClient(httpParameters);
		client.setHttpRequestRetryHandler(requestRetryHandler);
		HttpResponse httpResponse = client.execute(httpRequest);
		MediaApplication.logD(MediaApplication.class, "getStatusCode():"
				+ httpResponse.getStatusLine().getStatusCode());
		if (httpResponse.getStatusLine().getStatusCode() >= 400) {
			errStausCode = ERROR_BLOCK_NEED_TO_CHANGE_DOWNLOAD_RESOURCE;
			interrupt = true;
			return 0l;
		}
		HttpEntity httpEntity = httpResponse.getEntity();
        int length = ( int ) httpEntity.getContentLength();
        MediaApplication.logD(DownloadTask.class, mJob.getPath() + ": size:" + length);
		if(mJob.getDownloadResource() == IMediaService.RESOURCE_5SING){
			//httpConnection.setRequestProperty("Cookie", mJob.getCookie());
		}
		totalSize = length;
		MediaApplication.logD(DownloadTask.class, "totalSize :" + totalSize + "##currentSize : " + mJob.getDownloadStartPos());
		if (totalSize <= 0) {
			errStausCode = ERROR_BLOCK_NEED_TO_CHANGE_DOWNLOAD_RESOURCE;
			interrupt = true;
			return 0l;
		}
		mJob.setDownloadTotalSize(totalSize);
		if (downloadType == IMediaService.DOWNLOAD_START_ON_APK){
			MediaApplication.getInstance().setDownloadApkFlag(true);
			preferences.edit().putInt(DownloadApk.XML_KEY_TOTAL_SIZE, (int)totalSize).commit();
			preferences.edit().putInt(DownloadApk.XML_KEY_VERSION_CODE, mJob.getVersionCode()).commit();
		}
		if (downloadVisable == DOWNLOAD_VISIABLE){
			mJob.getArgs().putExtra("size", totalSize);
			mJob.getArgs().putExtra("url", mJob.getDownloadUrl());
		    mediaEventService.onMediaUpdateEvent(mJob.getArgs().setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_BEGIN));
		}
		File file = new File(mJob.getPath());
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		} else {
			mJob.setDownloadStartPos(file.length());//有一种情况就是程序装之前本地aMusic文件下面有没有下载完的临时文件。
		}
		if (mJob.getDownloadStartPos() > 0 && totalSize > 0 && totalSize > mJob.getDownloadStartPos()) {
			String sProperty = "bytes=" + mJob.getDownloadStartPos() + "-";
			if(httpRequest != null)
				httpRequest.abort();
			httpRequest = new HttpGet(mJob.getDownloadUrl());   
			httpRequest.addHeader("Range", sProperty);
			httpResponse = client.execute(httpRequest);  
			MediaApplication.logD(MediaApplication.class,"getStatusCode():" + httpResponse.getStatusLine().getStatusCode());
			if(httpResponse.getStatusLine().getStatusCode() >= 400){
				errStausCode = ERROR_BLOCK_NEED_TO_CHANGE_DOWNLOAD_RESOURCE;
				interrupt = true;
				return 0l;
			}
			httpEntity = httpResponse.getEntity();
			if(mJob.getDownloadResource() == IMediaService.RESOURCE_5SING){
				//httpConnection.setRequestProperty("Cookie", mJob.getCookie());
			}
			int Total_Size = ( int ) httpEntity.getContentLength();
			if (Total_Size == totalSize) {//特殊处理，当不支持断点续传的时候
				if(file.exists()){
					file.delete();
				}
			}
			if (Total_Size <= 0) {
				MediaApplication.logD(DownloadTask.class, "ERROR_UNKONW-->Total_Size <= 0 :");
				errStausCode = ERROR_UNKONW;
				interrupt = true;
				return 0l;
			}
			totalSize = Total_Size;
			MediaApplication.logD(DownloadTask.class,"File is not complete, download now.");
			MediaApplication.logD(DownloadTask.class,"剩下的文件长度:" + httpEntity.getContentLength() + " totalSize:" + totalSize);
		} else if (totalSize == mJob.getDownloadStartPos()) {
			MediaApplication.logD(DownloadTask.class, "Output file already exists. Skipping download..");
			mJob.setDownloadStateFlag(IMediaService.STATE_FINISHED);
			if (downloadVisable == DOWNLOAD_VISIABLE){
				mediaEventService.onMediaUpdateEvent(mJob.getArgs().setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_FINISH));
			}
			return 0l;
		}

		long storage = getAvailableStorage();
		MediaApplication.logD(DownloadTask.class, "storage:" + storage + " totalSize:" + totalSize);
		if (mJob.getDownloadTotalSize() - mJob.getDownloadStartPos() > storage) {
			errStausCode = ERROR_SD_NO_MEMORY;
			interrupt = true;
			return 0l;
		}
		try {
			outputStream = new ProgressReportingRandomAccessFile(file, "rw");
		} catch (FileNotFoundException e) {
			errStausCode = ERROR_BLOCK_TEMPFILE_DELETE;
			interrupt = true;
			return 0l;
		}

		publishProgress(0, (int) totalSize);

		InputStream input = null;
		try {
			input = httpEntity.getContent();
		} catch (IOException ex) {
			if(httpRequest != null)
			publishProgress(0);
			MediaApplication.logD(DownloadTask.class, "ERROR_UNKONW-->input = httpEntity.getContent() :" + ex);
			errStausCode = ERROR_UNKONW;
			interrupt = true;
			return 0l;
		}

		int bytesCopied = copy(input, outputStream, file ,httpRequest);
		publishProgress(0);
        MediaApplication.logD(DownloadTask.class, " bytesCopied :" + bytesCopied + " totalSize :" + totalSize );
        if (downloadVisable == DOWNLOAD_VISIABLE){
        	values.put(MediaDatabaseHelper.COLUMN_DOWNLOAD_CURRENT_SIZE, bytesCopied);
    		ServiceManager.getMediaService().getMediaDB().updateDownloadAudio(mJob.getDownloadId(), values);
        }
		if (bytesCopied == totalSize){
			MediaApplication.logD(DownloadTask.class, "Download completed successfully.");
			mJob.setDownloadStateFlag(IMediaService.STATE_FINISHED);
			if (downloadVisable == DOWNLOAD_VISIABLE){
				ServiceManager.getAmtMediaHandler().post(new Runnable(){
					@Override
					public void run() {
						mediaEventService.onMediaUpdateEvent(mJob.getArgs().setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_FINISH));
					}});
			} else {
				//TODO
			}
			if (downloadType == IMediaService.DOWNLOAD_START_ON_APK){
				MediaApplication.getInstance().setDownloadApkFlag(false);
				if (progressHandler != null) {
					Message message = new Message();
					Bundle bundle = new Bundle();
					bundle.putLong("currentBytes", mJob.getDownloadTotalSize());
					bundle.putLong("totalBytes", mJob.getDownloadTotalSize());
					bundle.putString("filePath", mJob.getPath());
					message.setData(bundle);
					message.what = MESSAGE_WHAT_DOWNLOADED;
					progressHandler.sendMessage(message);
				}
			} else if (downloadType == IMediaService.DOWNLOAD_START_ON_LYRICS) {
				new Thread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						mediaEventService.onMediaUpdateEvent(mJob.getArgs().setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_LYRIC_FINISH));
					}
					
				}).start();
				
			} else if (downloadType == IMediaService.DOWNLOAD_START_ON_IMAGE) {
				String picturePath = MediaApplication.singerPicturesPath + mJob.getArgs().getExtra("singerName") + IMediaService.PICTURE_SUFFIX;
				String pictureTempPath = MediaApplication.singerPicturesPath + mJob.getArgs().getExtra("singerName") + IMediaService.TMP_SUFFIX;
				File tmpfile = new File(pictureTempPath);
				if (0 == DETool.nativeGetPic(pictureTempPath, picturePath)) {
					tmpfile.delete();
					File pictureFile = new File(picturePath);
					if (pictureFile.exists()) {
						image = BitmapFactory.decodeFile(picturePath);
						BitmapCache.getInstance().addCacheBitmap(image, mJob.getArgs().getExtra("singerName").toString());
						Message imageMessage = new Message();
						Bundle data = new Bundle();
						data.putCharSequence("name", mJob.getArgs().getExtra("singerName").toString());
						imageMessage.setData(data);
						imageMessage.obj = image;
						imageMessage.arg1 = 1;
						imageMessage.what = MESSAGE_WHAT_DOWNLOADED;
						if (progressHandler != null) 
						progressHandler.sendMessage(imageMessage);
					}
				} else {
					tmpfile.delete(); 
					File tpPicFile = new File(picturePath);
					tpPicFile.delete();
			    }
			} else if (downloadType == IMediaService.DOWNLOAD_START_ON_SKIN) {
				File tmpFile = new File(mJob.getPath());
				File finalFile = new File (mJob.getFinalPath());
				if(tmpFile.length() != 0 && tmpFile.exists()) {
					tmpFile.renameTo((finalFile));
					if(MediaApplication.skinThumbnailPath.equals(tmpFile.getParent()+"/")){
						MediaApplication.logD(DownloadTask.class, " SkintmpFile :" + tmpFile.getParent());
						image = BitmapCache.decodeBitmap(mJob.getFinalPath());
						BitmapCache.getInstance().addCacheBitmap(image, mJob.getFinalPath());
					}
					Message imageMessage = new Message();
					imageMessage.obj = image;
					imageMessage.arg1 = 1;
					imageMessage.what = MESSAGE_WHAT_DOWNLOADED;
					Bundle data = new Bundle();
					data.putCharSequence("path", mJob.getFinalPath());
					imageMessage.setData(data);
					if (progressHandler != null) 
					progressHandler.sendMessage(imageMessage);
				} else {
					Message imageMessage = new Message();
					imageMessage.what = MESSAGE_WHAT_DOWNLOADED;
					imageMessage.arg1 = 2;
					if (progressHandler != null) 
					progressHandler.sendMessage(imageMessage);
				}
			} else if (downloadType == IMediaService.DOWNLOAD_START_ON_LRC_LYRICS) {
				new Thread(new Runnable(){
					@Override
					public void run() {
						// TODO Auto-generated method stub
						mediaEventService.onMediaUpdateEvent(mJob.getArgs().setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_LRC_LYRIC_FINISH));
					}
				}).start();
			}
		} else {
			MediaApplication.logD(DownloadTask.class, "ERROR_UNKONW-->bytesCopied == totalSize :");
			errStausCode = ERROR_UNKONW;
			interrupt = true;
		}
		return bytesCopied;
	}

	public int copy(InputStream input, RandomAccessFile out, File file, HttpGet httpRequest) throws Exception, IOException {
		byte[] buffer = new byte[BUFFER_SIZE];
		MediaApplication.logD(DownloadTask.class, "开始下载:" + file.getName());
		BufferedInputStream in = new BufferedInputStream(input, BUFFER_SIZE);
		MediaApplication.logD(DownloadTask.class, "--------------------tmpfile-length: " + out.length());
		out.seek(out.length());
		int bytesSoFar = 0;
		int bytesNotified = bytesSoFar;
		long timeLastNotification = 0;
		long errorBlockTimePreviousTime = -1, expireTime = 0;
		try {
			while (!interrupt) {
				int bytesRead = 0;
				
				if (!file.exists()) {
					errStausCode = ERROR_BLOCK_TEMPFILE_DELETE;
					interrupt = true;
					break;
				}
				if(!MediaApplication.networkIsOk) {
					MediaApplication.logD(DownloadTask.class, "SEND : !isOnline()");
					interrupt = true;
					errStausCode = ERROR_BLOCK_INTERNET;
					break;
				} 
				bytesRead = in.read(buffer, 0, BUFFER_SIZE);
				if (bytesRead == -1) {
					MediaApplication.logD(DownloadTask.class, "SEND : AUDIO_DOWNLOAD_DATA_OVER");
					break;
				}
				out.write(buffer, 0, bytesRead);
				bytesSoFar += bytesRead;

				long now = System.currentTimeMillis();

				if (bytesSoFar - bytesNotified > MIN_PROGRESS_STEP && now - timeLastNotification > MIN_PROGRESS_TIME) {
					bytesNotified = bytesSoFar;
					timeLastNotification = now;
					if (progressHandler != null) {
						Message message = new Message();
						message.what = MESSAGE_WHAT_DOWNLOADING;
						Bundle bundle = new Bundle();
						bundle.putLong("currentBytes", bytesSoFar + mJob.getDownloadStartPos());
						bundle.putLong("totalBytes", mJob.getDownloadTotalSize());
						bundle.putString("filePath", mJob.getPath());
						message.setData(bundle);
						message.obj = mJob;
						progressHandler.sendMessage(message);
					}
				}


				if (bytesRead == 0) {
					if (progressHandler != null) {
						Message message = new Message();
						message.what = MESSAGE_WHAT_DOWNLOADING;
						Bundle bundle = new Bundle();
						bundle.putLong("currentBytes", bytesSoFar + mJob.getDownloadStartPos());
						bundle.putLong("totalBytes", mJob.getDownloadTotalSize());
						bundle.putString("filePath", mJob.getPath());
						message.setData(bundle);
						message.obj = mJob;
						progressHandler.sendMessage(message);
					}
					MediaApplication.logD(DownloadTask.class, "SEND : bytesRead == 0");
					if (errorBlockTimePreviousTime > 0) {
						expireTime = System.currentTimeMillis() - errorBlockTimePreviousTime;
						if (expireTime > TIME_OUT) {
							MediaApplication.logD(DownloadTask.class, "expireTime > TIME_OUT");
							errStausCode = ERROR_UNKONW;
							interrupt = true;
							break;
						}
					} else {
						errorBlockTimePreviousTime = System.currentTimeMillis();
					}
				} else {
					expireTime = 0;
					errorBlockTimePreviousTime = -1;
				}
			}
			MediaApplication.logD(DownloadTask.class, "SEND : !isOUT");
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				MediaApplication.logD(DownloadTask.class, "ERROR_UNKONW-->out.close(); :");
				errStausCode = ERROR_UNKONW;
			}
			try {
				in.close();
			} catch (IOException e) {
				MediaApplication.logD(DownloadTask.class, "ERROR_UNKONW-->in.close(); :");
				errStausCode = ERROR_UNKONW;
			}
		}
		MediaApplication.logD(DownloadTask.class, "SEND : !isOUT:" +bytesSoFar);
		return bytesSoFar;
	}

	/*
	 * 获取 SD 卡内存
	 */
	public static long getAvailableStorage() {
		String storageDirectory = null;
		storageDirectory = Environment.getExternalStorageDirectory().toString();

	//	Log.v(null, "getAvailableStorage. storageDirectory : " + storageDirectory);

		try {
			StatFs stat = new StatFs(storageDirectory);
			long avaliableSize = ((long) stat.getAvailableBlocks() * (long) stat.getBlockSize());
		//	Log.v(null, "getAvailableStorage. avaliableSize : " + avaliableSize);
			return avaliableSize;
		} catch (RuntimeException ex) {
		//	Log.e(null, "getAvailableStorage - exception. return 0");
			return 0;
		}
	}

	private boolean isOnline() {
		try {
			ConnectivityManager cm = (ConnectivityManager) MediaApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo ni = cm.getActiveNetworkInfo();
			return ni != null ? ni.isConnectedOrConnecting() : false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private String getXml(String urlstr) {
		String encodingstr = "gb2312";
		URL url;
		try {
			url = new URL(urlstr);

			/*********************************************/

			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), encodingstr));
			StringBuffer content = new StringBuffer();
			String readerLine;
			String buf;

			while ((readerLine = reader.readLine()) != null) {
				content.append(readerLine + "\n");
			}

			/********************** 获取head节点中的内容 ********************/
			/*
			 * int headStartIndex = content.indexOf("<head>"); int headEndIndex
			 * = content.indexOf("</head>"); if (headStartIndex != 1 &&
			 * headEndIndex != -1) {
			 * System.out.println(content.substring(headStartIndex, headEndIndex
			 * + 7)); } else { System.out.println("没有head节点"); }
			 */
			/********************* 获取body节点中的内容 *********************/
			int bodyStartIndex = 0;
			// int bodyStartIndex = content.indexOf("<body>");
			int bodyEndIndex = content.indexOf("</body>");
			if (bodyStartIndex != 1 && bodyEndIndex != -1) {
				// System.out.println(content.substring(bodyStartIndex,
				// bodyEndIndex + 7));
				buf = content.substring(bodyStartIndex, bodyEndIndex + 7);
				// System.out.println(buf);
				return buf;
			} else {
				// System.out.println("没有body节点");
				return "error";
			}

		} catch (Exception e) {
			e.printStackTrace();
			return "error";
		}
	}

	private String getXml(String urlstr, String encodestr) {
		String encodingstr = encodestr;
		URL url;
		try {
			url = new URL(urlstr);

			/*********************************************/

			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), encodingstr));
			StringBuffer content = new StringBuffer();
			String readerLine;
			String buf;

			while ((readerLine = reader.readLine()) != null) {
				content.append(readerLine + "\n");
			}

			/********************** 获取head节点中的内容 ********************/
			/*
			 * int headStartIndex = content.indexOf("<head>"); int headEndIndex
			 * = content.indexOf("</head>"); if (headStartIndex != 1 &&
			 * headEndIndex != -1) {
			 * System.out.println(content.substring(headStartIndex, headEndIndex
			 * + 7)); } else { System.out.println("没有head节点"); }
			 */
			/********************* 获取body节点中的内容 *********************/
			int bodyStartIndex = 0;
			// int bodyStartIndex = content.indexOf("<body>");
			int bodyEndIndex = content.indexOf("</body>");
			if (bodyStartIndex != 1 && bodyEndIndex != -1) {
				// System.out.println(content.substring(bodyStartIndex,
				// bodyEndIndex + 7));
				buf = content.substring(bodyStartIndex, bodyEndIndex + 7);
				// System.out.println(buf);
				return buf;
			} else {
				// System.out.println("没有body节点");
				return "error";
			}

		} catch (Exception e) {
			e.printStackTrace();
			return "error";
		}
	}
	private  String getXml2(String url,String encoding) {
		String xml = null;
		try {
			URL u = new URL(url);
			HttpURLConnection httpConn = (HttpURLConnection) u.openConnection();
			HttpURLConnection.setFollowRedirects(true);
			httpConn.setRequestMethod("GET");
			httpConn.setRequestProperty(
					"User-Agent",
					"Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.2.13) Gecko/20110222 Gentoo /Firefox/3.6.13");
			// httpConn.setRequestProperty("Cookie","BAIDUID=6372795DE39436AF8D97BD8EE05B894C:FG=1");
			// httpConn.setRequestProperty("User-Agent","Mozilla/5.0 (iPad; U; CPU OS 3_2_1 like Mac OS X; zh-cn) AppleWebKit/531.21.10 (KHTML, like Gecko) Version/4.0.4 Mobile/7B405 Safari/531.21.10");
			InputStream is = httpConn.getInputStream();
			// System.out.println("httpContentEncoding :" +
			// httpConn.getContentEncoding());

			if (httpConn.getContentEncoding() != null
					&& httpConn.getContentEncoding().equals("gzip")) {
				GZIPInputStream gzin = new GZIPInputStream(is);
				int length = 0;
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				while ((length = gzin.read()) != -1) {
					bos.write(length);
				}
				xml = new String(bos.toString(encoding));
				// System.out.println("this is xml : " + xml);
			} else {
				int length = 0;
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				while ((length = is.read()) != -1) {
					bos.write(length);
				}
				xml = new String(bos.toString(encoding));
				// System.out.println("this is xml : " + xml);
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			xml = "error";
		}
		return xml;
	}
	public static String getXml(String url, String encode, String cookie) {
		String xml = null;
		try {
			URL u = new URL(url);
			HttpURLConnection httpConn = (HttpURLConnection) u.openConnection();
			HttpURLConnection.setFollowRedirects(true);
			httpConn.setRequestMethod("GET");
			httpConn.setRequestProperty(
					"User-Agent",
					"Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.2.13) Gecko/20110222 Gentoo Firefox/3.6.13)");
			httpConn.setRequestProperty("Cookie", cookie);
			InputStream is = httpConn.getInputStream();
			if (httpConn.getContentEncoding() != null
					&& httpConn.getContentEncoding().equals("gzip")) {
				GZIPInputStream gzin = new GZIPInputStream(is);
				int length = 0;
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				while ((length = gzin.read()) != -1) {
					bos.write(length);
				}
				xml = new String(bos.toString(encode));
				// System.out.println("this is xml : " + xml);
			} else {
				int length = 0;
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				while ((length = is.read()) != -1) {
					bos.write(length);
				}
				xml = new String(bos.toString(encode));
				// System.out.println("this is xml : " + xml);
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			xml = "error";
		}
		return xml;
	}
	public long getDownloadUrl(DownloadJob mJob) throws IOException {
		if (!isOnline()) {
			mJob.getArgs().putExtra("show", false);
			mediaEventService.onMediaUpdateEvent(mJob.getArgs().setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_ERROR));
			return 0;
		}
		long fileSize = -1;
		if (mJob.getDownloadResource() == IMediaService.RESOURCE_SOUGOU) {
			fileSize = GetFileSizeForMobileFromSogou(mJob.getSong(), mJob.getSinger());
		} else if (mJob.getDownloadResource() == IMediaService.RESOURCE_BAIDU) {
			fileSize = GetFileSizeForMobile(mJob.getSong(), mJob.getSinger());
		} else if (mJob.getDownloadResource() == IMediaService.RESOURCE_OK99) {
			//登录下载模式，需要用豆豆下载或者vip下载
			fileSize = GetFileSizeAboutAccompanimentFrom5singafterauth(mJob.getSong(), mJob.getSinger());
		} else if (mJob.getDownloadResource() == IMediaService.RESOURCE_5SING) {
			fileSize = GetFileSizeAboutAccompanimentFrom5sing(mJob.getSong(), mJob.getSinger());
		}
		mJob.setDownloadUrl(DownloadUrl);
		mJob.setCookie(m_5singcookie);
		return fileSize ;
	}


	private long getFileSize(String sURL) {
		long nEndPos = 0;
		try {
			URL url = new URL(sURL);
			int times = 0;
			// connect
			while (nEndPos <= 0 && times < retryTimes) {
				HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
				// getfilesize
				nEndPos = getSize(sURL);
				httpConnection.disconnect();
				times++;
				Thread.sleep(interval);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		return nEndPos;
	}

	// get file size
	private long getSize(String sURL) {
		int nFileLength = -1;
		try {
			URL url = new URL(sURL);
			HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
			httpConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; U; Android 2.2; en-us; Nexus One Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
			int responseCode = httpConnection.getResponseCode();
			if (responseCode >= 400) {
				System.err.println("Error Code : " + responseCode);
				return -2; // -2 represent access is error
			}
			String sHeader;
			for (int i = 1;; i++) {
				sHeader = httpConnection.getHeaderFieldKey(i);
				if (sHeader != null) {
					if (sHeader.toLowerCase().equals("content-length")) {
						nFileLength = Integer.parseInt(httpConnection.getHeaderField(sHeader));
						break;
					}
				} else
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// System.out.println(nFileLength);
		return nFileLength;
	}


	private void init(int head, int bottom, int middle) {
		for (int i = head; i <= bottom; i++) {
			asc_arr1[i] = i + middle;
			asc_arr2[i + middle] = i;
		}

	}

	private String decode(String url, int sertim) {
		long len = url.length();
		String decurl = "";
		int key = 0;
		key = sertim % 26;
		if (key < 0) {
			key = 1;
		}

		init(startnum, endnum, num_asc_map);
		init(startA_Z, endA_Z, A_Z_asc_map);
		init(starta_z, enda_z, a_z_asc_map);
		for (int i = 0; i < len; i++) {
			char word = url.charAt(i);
			int wordasc = (int) word;
			if (((47 < wordasc) && (wordasc < 58)) || ((64 < wordasc) && (wordasc < 91)) || ((96 < wordasc) && (wordasc < 123))) {
				int pos = asc_arr2[wordasc] - key;
				if (pos < 0)
					pos += 62;
				word = (char) (asc_arr1[pos]);
			}
			decurl += word;
		}
		return decurl;

	}

//	private long GetFileSizeForMobileFromSogou(String sMusicName, String sPlayerName) throws UnsupportedEncodingException {
//		DownloadUrl = "";
//		long ret = 0;
//		sMusicName = sMusicName.toLowerCase();
//		String MusicNameGB2312 = "";
//		MusicNameGB2312 = java.net.URLEncoder.encode(sMusicName, "GB2312");
//		String sPlayerNameGB2312 = "";
//		sPlayerNameGB2312 = java.net.URLEncoder.encode(sPlayerName, "GB2312");
//		String name_music = "";
//		name_music = MusicNameGB2312 + "+" + sPlayerNameGB2312;
//		// first url
//		String first_html = "";
//		String firstxml = "";
//		first_html = "http://mp3.sogou.com/music.so?query=" + name_music + "&class=1";
//		firstxml = getXml(first_html, "utf8");
//		Pattern p = Pattern.compile("onclick=\"window.open\\('([^']+)'");
//		Matcher m = p.matcher(firstxml);
//		String path_test1 = "";
//		String path_test2 = "";
//		String path_test3 = "";
//		// check the song is right
//		while (m.find()) {
//			path_test1 = m.group();
//			// System.out.println(path_test1);
//			//path_test1 = m.group();
//			// System.out.println(path_test1);
//			Pattern p1 = Pattern.compile("&query=([^&]+)&");
//			Matcher m1 = p1.matcher(path_test1);
//			if (m1.find()) {
//				path_test2 = m1.group();
//				path_test2 = path_test2.replace("&query=", "");
//				path_test2 = path_test2.replace("&", "");
//				path_test2 = path_test2.toLowerCase();
//				MusicNameGB2312 = MusicNameGB2312.toLowerCase();
//				// System.out.println(path_test2);
//				if (path_test2.equals(MusicNameGB2312)) {
//					path_test3 = path_test1;
//					break;
//				}
//			}
//		}
//		if (!path_test3.equals("")) {
//			Pattern p2 = Pattern.compile("\\('([^']+)'");
//			Matcher m2 = p2.matcher(path_test3);
//			if (m2.find()) {
//				path_test2 = m2.group();
//			} else {
//				return -3;
//			}
//			path_test2 = path_test2.replace("(", "");
//			path_test2 = path_test2.replace("'", "");
//			// System.out.println(path_test2);
//			path_test3 = "http://mp3.sogou.com" + path_test2;
//
//			String testxml2 = "";
//			testxml2 = getXml(path_test3, "utf8");
//			String path_test5 = "";
//			String path_test6 = "";
//			String path_test7 = "";
//			Pattern p4 = Pattern.compile("<div class=\"dl\"><a href=\"([^\"]+)\"");
//			Matcher m4 = p4.matcher(testxml2);
//			if (m4.find()) {
//				path_test5 = m4.group().toString();
//			} else {
//				return -4;
//			}
//			// System.out.println(path_test5);
//			Pattern p5 = Pattern.compile("<a href=\"([^\"]+)\"");
//			Matcher m5 = p5.matcher(path_test5);
//			if (m5.find()) {
//				path_test6 = m5.group().toString();
//			} else {
//				return -5;
//			}
//			Pattern p6 = Pattern.compile("\"([^\"]+)\"");
//			Matcher m6 = p6.matcher(path_test6);
//			if (m6.find()) {
//				path_test7 = m6.group().toString().replace("\"", "");
//			} else {
//				return -6;
//			}
//			DownloadUrl = path_test7;
//			ret = getFileSize(path_test7);
//		} else {
//			return -7;
//		}
//		return ret;
//	}
	private  long GetFileSizeForMobileFromSogou(String sMusicName,
			String sPlayerName)
			throws UnsupportedEncodingException {
		DownloadUrl = "";
		long ret = 0;
		sMusicName = sMusicName.toLowerCase();
		String MusicNameGB2312 = "";
		MusicNameGB2312 = java.net.URLEncoder.encode(sMusicName, "GB2312");
		String sPlayerNameGB2312 = "";
		sPlayerNameGB2312 = java.net.URLEncoder.encode(sPlayerName, "GB2312");
		String name_music = "";
		name_music = MusicNameGB2312 + "+" + sPlayerNameGB2312;
		// first url
		String first_html = "";
		String firstxml = "";
		String path_test0 = "";
		String path_test1 = "";
		String path_test2 = "";
		String path_test3 = "";
		first_html = "http://mp3.sogou.com/music.so?query=" + name_music
				+ "&class=1";
		firstxml = getXml(first_html, "utf8");
		Pattern p0 = Pattern
				.compile("<tr id=\"musicmc[\\s\\S]*?</td>\\s*</tr>");
		Matcher m0 = p0.matcher(firstxml);
		while (m0.find()) {
			path_test0 = m0.group();
			int mp3flag = 0;
			mp3flag = path_test0.indexOf("<td>mp3</td>");
			if (mp3flag > 0) {
				Pattern p = Pattern
						.compile("onclick=\"window.open\\('([^']+)'");
				Matcher m = p.matcher(path_test0);

				// check the song is right
				if (m.find()) {
					path_test1 = m.group();
					Pattern p1 = Pattern.compile("&query=([^&]+)&");
					Matcher m1 = p1.matcher(path_test1);
					if (m1.find()) {
						path_test2 = m1.group();
						path_test2 = path_test2.replace("&query=", "");
						path_test2 = path_test2.replace("&", "");
						path_test2 = path_test2.toLowerCase();
						MusicNameGB2312 = MusicNameGB2312.toLowerCase();
						// System.out.println(path_test2);
						if (path_test2.equals(MusicNameGB2312)) {
							path_test3 = path_test1;
							break;
						}
					}
				}
			}
		}
		if (!path_test3.equals("")) {
			Pattern p2 = Pattern.compile("\\('([^']+)'");
			Matcher m2 = p2.matcher(path_test3);
			if (m2.find()) {
				path_test2 = m2.group();
			} else {
				return -3;
			}
			path_test2 = path_test2.replace("(", "");
			path_test2 = path_test2.replace("'", "");
			// System.out.println(path_test2);
			path_test3 = "http://mp3.sogou.com" + path_test2;

			String testxml2 = "";
			testxml2 = getXml(path_test3, "utf8");
			String path_test5 = "";
			String path_test6 = "";
			String path_test7 = "";
			Pattern p4 = Pattern
					.compile("<div class=\"dl\"><a href=\"([^\"]+)\"");
			Matcher m4 = p4.matcher(testxml2);
			if (m4.find()) {
				path_test5 = m4.group().toString();
			} else {
				return -4;
			}
			// System.out.println(path_test5);
			Pattern p5 = Pattern.compile("<a href=\"([^\"]+)\"");
			Matcher m5 = p5.matcher(path_test5);
			if (m5.find()) {
				path_test6 = m5.group().toString();
			} else {
				return -5;
			}
			Pattern p6 = Pattern.compile("\"([^\"]+)\"");
			Matcher m6 = p6.matcher(path_test6);
			if (m6.find()) {
				path_test7 = m6.group().toString().replace("\"", "");
			} else {
				return -6;
			}
			DownloadUrl = path_test7;
			ret = getFileSize(path_test7);
		} else {
			return -7;
		}
		return ret;
	}
	public long GetFileSizeForMobile(String sMusicName, String sPlayerName) throws UnsupportedEncodingException {
//		System.out.println(sMusicName);
//		System.out.println(sPlayerName);
//		DownloadUrl = "";
//		System.out.println(sMusicName);
//		System.out.println(sPlayerName);
//		sMusicName = sMusicName.toLowerCase();
//		String MusicNameGB2312 = "";
//		MusicNameGB2312 = java.net.URLEncoder.encode(sMusicName, "GB2312");
//		String sPlayerNameGB2312 = "";
//		sPlayerNameGB2312 = java.net.URLEncoder.encode(sPlayerName, "GB2312");
//		String name_music = "";
//		name_music = MusicNameGB2312 + "+" + sPlayerNameGB2312;
//		// first url
//		String first_html = "";
//		first_html = "http://mp3.baidu.com/m?tn=baidump3mobile&ssid=&from=&bd_page_type=1&uid=&pu=&f=ms&ct=671088640&lf=&rn=20&lm=0&gate=33&word=" + name_music;
//		// first page info
//		String firstxml = "";
//		String path1 = "";
//		firstxml = getXml(first_html);
//		Pattern p = Pattern.compile("<li>\\s*<a href=\\s*\"([^\"]+)\"");
//		Matcher m = p.matcher(firstxml);
//		String path_test1 = "";
//		String path_test2 = "";
//		String path_test3 = "";
//		String path_test4 = "";
//		String path_test5 = "";
//		String path_test6 = "";
//		// check the song is right
//		while (m.find()) {
//			path_test1 = m.group();
//			// System.out.println(path_test1);
//			Pattern p1 = Pattern.compile("&si=([^;]+);");
//			Matcher m1 = p1.matcher(path_test1);
//			if (m1.find()) {
//				path_test2 = m1.group();
//				path_test2 = path_test2.replace("&si=", "");
//				path_test2 = path_test2.replace(";", "");
//				// System.out.println(path_test2);
//			}
//			if (path_test2.equals(MusicNameGB2312)) {
//				path_test3 = path_test1;
//				break;
//			}
//		}
//		// check the list contain the right song
//		if (!path_test3.equals("")) {
//			Pattern p2 = Pattern.compile("\"([^\"]+)\"");
//			Matcher m2 = p2.matcher(path_test3);
//			if (m2.find()){
//				path_test4 = m2.group().replace("\"", "");
//			}
//			path_test4 =path_test4.replace(" ", "+");
//			path1 = getXml(path_test4);
//			// get the second page info:path1
//			String Downpath1 = "";
//			Pattern p4 = Pattern.compile("var\\s*encurl\\s*=\\s*\"([^\"]+)\"");
//			Matcher m4 = p4.matcher(path1);
//			if (m4.find())
//				Downpath1 = m4.group();
//			// System.out.println(Downpath1);
//			//
//			Pattern p5 = Pattern.compile("\"([^\"]+)\"");
//			Matcher m5 = p5.matcher(Downpath1);
//			if (m5.find())
//				path_test4 = m5.group().replace("\"", "");
//			// System.out.println(path_test4);
//			//
//			Pattern p6 = Pattern.compile("var\\s*sertim\\s*=\\s*([^,]+),");
//			Matcher m6 = p6.matcher(path1);
//			if (m6.find())
//				path_test5 = m6.group();
//			// System.out.println(path_test5);
//
//			Pattern p7 = Pattern.compile("=\\s*([^,]+),");
//			Matcher m7 = p7.matcher(path_test5);
//			if (m7.find())
//				path_test6 = m7.group().replace(",", "");
//			path_test6 = path_test6.replace("=", "");
//			path_test6 = path_test6.replace(" ", "");
//			// System.out.println(path_test6);
//			String DecUrl = "";
//			if(path_test6==""){
//				return BAIDUCANT;
//			}
//			DecUrl = decode(path_test4, Integer.parseInt(path_test6));
//			// modify by hh 20120208
//			String DecUrl_name_music = "";
//			DecUrl_name_music = java.net.URLEncoder.encode(DecUrl, "GB2312");
//			DecUrl_name_music = DecUrl_name_music.replace("%2F", "/");
//			DecUrl_name_music = DecUrl_name_music.replace("%3A", ":");
//			DecUrl_name_music = DecUrl_name_music.replace("%3F", "?");
//			DecUrl_name_music = DecUrl_name_music.replace("%3D", "=");
//			DecUrl_name_music = DecUrl_name_music.replace("amp%3B", "&");
//			DecUrl_name_music = DecUrl_name_music.replace("%25", "%");
//
//			String Downpath2 = "";
//			String Downpath3 = "";
//			Pattern p8 = Pattern.compile("var\\s*subulrs\\s*=\\s*([^,]+),");
//			Matcher m8 = p8.matcher(path1);
//			if (m8.find()) {
//				Downpath2 = m8.group();
//				// System.out.println(Downpath2);
//			} else {
//				/*
//				 * long ret = GetFileSizeForMobileFromSogou(sMusicName, sPlayerName); if (ret < 0) { // System.out.println("无法下载"); }
//				 */
//				return BAIDUCANT;
//			}
//			Pattern p9 = Pattern.compile("\"([^\"]+)\"");
//			Matcher m9 = p9.matcher(Downpath2);
//			if (m9.find())
//				Downpath3 = m9.group().replace("\"", "");
//			String DecUrl2 = "";
//			DecUrl2 = decode(Downpath3, Integer.parseInt(path_test6));
//			String DecUrl_name_music2 = "";
//			DecUrl_name_music2 = java.net.URLEncoder.encode(DecUrl2, "GB2312");
//			DecUrl_name_music2 = DecUrl_name_music2.replace("%2F", "/");
//			DecUrl_name_music2 = DecUrl_name_music2.replace("%3A", ":");
//			DecUrl_name_music2 = DecUrl_name_music2.replace("%3F", "?");
//			DecUrl_name_music2 = DecUrl_name_music2.replace("%3D", "=");
//			DecUrl_name_music2 = DecUrl_name_music2.replace("%26amp%3B", "&");
//			DecUrl_name_music2 = DecUrl_name_music2.replace("%25", "%");
//
//			String baidutag = "http://zhangmenshiting.baidu.com";
//			String checkbuf = "";
//			String checkbuf2 = "";
//			int declen = 0;
//			int declen2 = 0;
//			declen = DecUrl_name_music.length();
//			declen2 = DecUrl_name_music2.length();
//			if (DecUrl_name_music != "" && DecUrl_name_music != null && declen > baidutag.length()) {
//				checkbuf = DecUrl_name_music.substring(0, baidutag.length());
//			}
//			if (DecUrl_name_music2 != "" && DecUrl_name_music2 != null && declen2 > baidutag.length()) {
//				checkbuf2 = DecUrl_name_music2.substring(0, baidutag.length());
//			}
//			if (checkbuf.equals(baidutag)) {
//				long filesize = getFileSize(DecUrl_name_music);
//				if (filesize > 0) {
//					DownloadUrl = DecUrl_name_music;
//				}
//				return filesize;
//			} else if (checkbuf2.equals(baidutag)) {
//				long filesize = getFileSize(DecUrl_name_music2);
//				if (filesize > 0) {
//					DownloadUrl = DecUrl_name_music2;
//				}
//				return filesize;
//			} else {
//				return BAIDUCANT;// 百度无法下载
//			}
//		}
//		// if path_est3 !="";
//		// can't find the right song in the list,then try sogou
//		else {
//			return BAIDUCANT; // 百度无法下载
//		}
		DownloadUrl ="";
		String url = getDownloadUrlFromServer(sMusicName,sPlayerName,DOWNLOAD_GETURL_ORIGINAL);
		if (!url.equals("null"))
		{
			DownloadUrl = url;
			return 0;
		}
		
		return BAIDUCANT;
		
	}

//	public long GetFileSizeAboutAccompaniment(String sMusicName, String sPlayerName) throws UnsupportedEncodingException {
//		String sMusicnameUTF8 = "";
//		sMusicnameUTF8 = java.net.URLEncoder.encode(sMusicName, "utf8");
//		String sPlayernameUTF8 = "";
//		sPlayernameUTF8 = java.net.URLEncoder.encode(sPlayerName, "utf8");
//		String sAccompanimentUTF8 = "";
//		sAccompanimentUTF8 = java.net.URLEncoder.encode("伴奏", "utf8");
//		String sMusicAndPlayerName = "";
//		sMusicAndPlayerName = sMusicnameUTF8 + "+" + sPlayernameUTF8 + "+" + sAccompanimentUTF8;
//		String FirstUrl = "";
//		FirstUrl = "http://m.easou.com/s.e?actType=1&q=" + sMusicAndPlayerName + "&esid=HecaHJgNkXk&l=216.3&wver=c";
//		String firstxml = "";
//		firstxml = getXml(FirstUrl, "utf8");
//		String testpath1 = "";
//		String testpath2 = "";
//		String testpath3 = "";
//		String testpath4 = "";
//		String finallypath = "";
//		Pattern p = Pattern.compile("<a href=[\\s\\S]*?<form\\s*action=");
//		Matcher m = p.matcher(firstxml);
//		if (m.find()) {
//			testpath1 = m.group();
//			Pattern p1 = Pattern.compile("<a href=[\\s\\S]*?</a>");
//			Matcher m1 = p1.matcher(testpath1);
//			while (m1.find()) {
//				testpath2 = m1.group();
//				Pattern p2 = Pattern.compile(">[\\s\\S]*?</a>");
//				Matcher m2 = p2.matcher(testpath2);
//				if (m2.find()) {
//					testpath3 = m2.group();
//					int findplayer = testpath3.indexOf(sPlayerName);
//					int Accompaniment = testpath3.indexOf("伴奏");
//					if ((findplayer >= 0) && (Accompaniment >= 0)) {
//						Pattern p3 = Pattern.compile("\"([^\"]+)\"");
//						Matcher m3 = p3.matcher(testpath2);
//						if (m3.find()) {
//							testpath4 = m3.group();
//							testpath4 = testpath4.replace("\"", "");
//							testpath4 = testpath4.replace("amp;", "");
//							testpath4 = testpath4.replace("%3A", ":");
//							testpath4 = testpath4.replace("%2F", "/");
//							finallypath = "http://m.easou.com" + testpath4;
//							break;
//						}
//					}
//				}
//			}
//		}
//		String secondxml = "";
//		secondxml = getXml(finallypath, "utf8");
//		String testpath5 = "";
//		String testpath6 = "";
//		String testpath7 = "";
//		String testpath8 = "";
//		Pattern p4 = Pattern.compile("<a\\s*href=[\\s\\S]*?<br/>\\s*<div>");
//		Matcher m4 = p4.matcher(secondxml);
//		if (m4.find()) {
//			testpath5 = m4.group();
//			int findtype1 = testpath5.indexOf("保真");
//			int findtype2 = testpath5.indexOf("高质");
//			int findtype3 = testpath5.indexOf("普通");
//			String keyword = "";
//			if (findtype1 >= 0) {
//				keyword = "保真";
//			} else if (findtype2 >= 0) {
//				keyword = "高质";
//			} else if (findtype3 >= 0) {
//				keyword = "普通";
//			} else {
//				keyword = "压缩";
//			}
//			Pattern p5 = Pattern.compile("<a\\s*href=[\\s\\S]*?</a>");
//			Matcher m5 = p5.matcher(testpath5);
//			while (m5.find()) {
//				testpath6 = m5.group();
//				int findkeyword = testpath6.indexOf(keyword);
//				if (findkeyword >= 0) {
//					Pattern p6 = Pattern.compile("\"([^\"]+)\"");
//					Matcher m6 = p6.matcher(testpath6);
//					if (m6.find()) {
//						testpath7 = m6.group();
//						testpath7 = testpath7.replace("\"", "");
//						testpath7 = testpath7.replace("amp;", "");
//						testpath7 = testpath7.replace("%3A", ":");
//						testpath7 = testpath7.replace("%2F", "/");
//						testpath8 = "http://m.easou.com" + testpath7;
//						break;
//					}
//				}
//			}
//		}
//		String thxml = "";
//		thxml = getXml(testpath8, "utf8");
//		String testpath9 = "";
//		String testpath10 = "";
//		Pattern p7 = Pattern.compile("url=[\\s\\S]*?/>");
//		Matcher m7 = p7.matcher(thxml);
//		if (m7.find()) {
//			testpath9 = m7.group();
//			Pattern p8 = Pattern.compile("url=([^\"]+)\"");
//			Matcher m8 = p8.matcher(testpath9);
//			if (m8.find()) {
//				testpath10 = m8.group();
//				testpath10 = testpath10.replace("url=", "");
//				testpath10 = testpath10.replace("\"", "");
//				testpath10 = testpath10.replace("amp;", "");
//				testpath10 = testpath10.replace("%3A", ":");
//				testpath10 = testpath10.replace("%2F", "/");
//				long filesize = getFileSize(testpath10);
//
//				if (filesize > 1024 * 1024 * 0.5) {
//					DownloadUrl = testpath10;
//					return filesize;
//				} else {
//					return OK99CANT;
//				}
//			} else {
//				return OK99CANT;
//			}
//		} else {
//			return OK99CANT;
//		}
//	}
	//获取5sing页面上的文件大小，方便取最大的一个
	public static String getMusicSizeFrom5sing(String xml) {
		String MusicSize = "";
		String data1 = "";
		String data2 = "";
		Pattern p1 = Pattern.compile("<td class=\"text_lt\">[\\s\\S]*?</td>");
		Matcher m1 = p1.matcher(xml);
		while (m1.find()) {
			data1 = m1.group();
			int SizeFlag = 0;
			SizeFlag = data1.indexOf("MB");
			if (SizeFlag > 0) {
				Pattern p2 = Pattern.compile(">[\\s\\S]*?<");
				Matcher m2 = p2.matcher(data1);
				if (m2.find()) {
					data2 = m2.group();
					data2 = data2.replace(">", "");
					data2 = data2.replace("<", "");
					data2 = data2.replace("MB", "");
					MusicSize = data2;
					break;
				}
			}
		}
		return MusicSize;
	}
    //取页面上的5sing的第一个页面上的链接地址
	public static String getMusicUrlFrom5sing(String xml) {
		String MusicUrl = "";
		String data1 = "";
		String data2 = "";
		String data3 = "";
		Pattern p1 = Pattern.compile("<td><a href=[\\s\\S]*?</a></td>");
		Matcher m1 = p1.matcher(xml);
		while (m1.find()) {
			data1 = m1.group();
			int downflag = data1.indexOf("下载");
			if (downflag > 0) {
				Pattern p2 = Pattern.compile("<td><a href=\"([^\"]+)\"");
				Matcher m2 = p2.matcher(data1);
				if (m2.find()) {
					data2 = m2.group();
					Pattern p3 = Pattern.compile("\"([^\"]+)\"");
					Matcher m3 = p3.matcher(data2);
					if (m3.find()) {
						data3 = m3.group();
						data3 = data3.replace("\"", "");
						MusicUrl = data3;
						break;
					}
				}
			}
		}
		return MusicUrl;
	}

	// 首先匹配名字，判断去除了歌手名后的歌曲名那一栏是否含有正确的歌曲名，如果没有，则找下一个；
	// 如果有正确的，则继续进一步判断是否是含有“原版伴奏”，全字匹配或者其他，然后获取到一个条件则停止获取相同条件的；
	// 最终最多可能获取到三个结果，然后互相比较大小，取最大的一个；
	public  String GetDownloadUrlFrom5ting(String sMusicName,
			String sPlayerName) throws UnsupportedEncodingException {
		sMusicName = sMusicName.toLowerCase();
		sPlayerName = sPlayerName.toLowerCase();
		String MusicAndPlayerName = "";
		MusicAndPlayerName = sMusicName + " " + sPlayerName;
		String MusicAndPlayerNameGB2312 = "";
		MusicAndPlayerNameGB2312 = java.net.URLEncoder.encode(
				MusicAndPlayerName, "utf8");
		String first_html = "";
		first_html = "http://sou.5sing.com/sbz.aspx?key="
				+ MusicAndPlayerNameGB2312 + "&kind=1";
		// System.out.println(first_html);
		String firstxml = "";
		firstxml = getXml(first_html, "utf8");
		// System.out.println(firstxml);
		firstxml = firstxml.replace("\n", "");
		String musicSize = "";
		String musicUrl = "";
		String data1 = "";
		String data2 = "";
		String data3 = "";
		String data4 = "";
		String data5 = "";
		String data6 = "";
		String szname = "";
		boolean beforeflag = false;
		boolean afterflag = false;
		String beforechar = ""; // 歌名前的字符串
		String afterchar = ""; // 歌名前的字符串
		FinalUrl = "";
		FinalWmaUrl = "";
		commonUrl = "";
		cleanUrl = "";
		originalUrl = ""; // 初始化三个值
		commonSize = 0;
		cleanSize = 0;
		originalSize = 0; // 初始化三个值
		Pattern p1 = Pattern.compile("<tr>\\s*<td>[\\s\\S]*?</td>\\s*</tr>");
		Matcher m1 = p1.matcher(firstxml);
		while (m1.find()) {
			data1 = m1.group();
			musicSize = "";
			musicSize = getMusicSizeFrom5sing(data1); // 歌曲大小
			musicUrl = "";
			musicUrl = getMusicUrlFrom5sing(data1); // 歌曲路径
			Pattern pname = Pattern.compile("title=\"([^\"]+)\"");
			Matcher mname = pname.matcher(data1);
			if (mname.find()) {
				szname = mname.group();
				Pattern p5 = Pattern.compile("\"([^\"]+)\"");
				Matcher m5 = p5.matcher(szname);
				if (m5.find()) {
					data5 = m5.group();
					data5 = data5.replace("\"", ""); // 歌名
					// data5 =data5.replace(" ", ""); //歌名去除空格
					data5 = data5.toLowerCase(); // 全部转小写
					data5 = data5.replace(sPlayerName, ""); // 歌名去除歌手名
					//System.out.println(data5);
					int musicNameFlag = 0;
					int playerFlag = 0;
					int mp3Flag = 0;
					musicNameFlag = data5.indexOf(sMusicName);
					playerFlag =data1.indexOf(sPlayerName);
					mp3Flag = data1.indexOf("mp3");
					int errorFlag = 0;
					int ybFlag = 0; // 原版flag;
					errorFlag = data5.indexOf("笛") + data5.indexOf("改版")
							+ data5.indexOf("摇滚") + data5.indexOf("歌曲")+ data5.indexOf("单曲");// 过滤的质量不好的伴奏干扰
					if (errorFlag == -5 && musicNameFlag >= 0 && playerFlag >=0) {
						char[] ch = data5.toCharArray();
						char c = 0, d = 0;
						if (musicNameFlag == 0) {
							if (musicNameFlag + sMusicName.length() < data5
									.length()) {
								d = ch[musicNameFlag + sMusicName.length()];
								afterchar = String.valueOf(d);
//								afterflag = isChinese(d); // 暂时觉得没做中文判断，替代的做了一些特殊符号的判断
								if (afterchar.equals("：")
										|| afterchar.equals("（")
										|| afterchar.equals("-")
										|| afterchar.equals("—")
										|| afterchar.equals(" ")
										|| afterchar.equals("(")
										|| afterchar.equals("_")) {

									if (mp3Flag < 0) {
										if (FinalWmaUrl.length() == 0) {
											FinalWmaUrl = musicUrl;
										}
									} else {
										// 继续做事情
										ybFlag = data5.indexOf("原版伴奏");
										if (ybFlag >= 0) {
											if (originalUrl.length() == 0) {
												originalUrl = musicUrl;
												if(musicSize!=""){
													originalSize = Float.valueOf(musicSize);
												}else{
													originalSize=0;
												}
											}
										} else if (commonUrl.length() == 0) {
											commonUrl = musicUrl;
											if(musicSize!=""){
												commonSize = Float.valueOf(musicSize);
											}else{
												commonSize=0;
											}
										}
									}
								} else {
									// 继续循环
								}
							} else {
								if (mp3Flag < 0) {
									if (FinalWmaUrl.length() == 0) {
										FinalWmaUrl = musicUrl;
									}
								} else {
									if (cleanUrl.length() == 0) {
										// 全字匹配情况
										cleanUrl = musicUrl;
										if(musicSize!=""){
										    cleanSize = Float.valueOf(musicSize);
										}else{
											cleanSize=0;
										}
									}
								}
							}
						} else {
							c = ch[musicNameFlag - 1];
							beforechar = String.valueOf(c);
//							beforeflag = isChinese(c);// 暂时没做中文判断，替代换成了判断一些特殊字符
							if (musicNameFlag + sMusicName.length() < data5
									.length()) {
								d = ch[musicNameFlag + sMusicName.length()];
								afterchar = String.valueOf(d);
								if (beforechar.equals("：")
										|| beforechar.equals("）")
										|| beforechar.equals("-")
										|| beforechar.equals("—")
										|| beforechar.equals(" ")
										|| beforechar.equals(")")
										|| beforechar.equals("_")) {
									if (afterchar.equals("：")
											|| afterchar.equals("（")
											|| afterchar.equals("-")
											|| afterchar.equals("—")
											|| afterchar.equals(" ")
											|| afterchar.equals("(")
											|| afterchar.equals("_")) {
										if (mp3Flag < 0) {
											if (FinalWmaUrl.length() == 0) {
												FinalWmaUrl = musicUrl;
											}
										} else {
											// 继续做事情
											ybFlag = data5.indexOf("原版伴奏");
											if (ybFlag >= 0) {
												if (originalUrl.length() == 0) {
													originalUrl = musicUrl;
													if(musicSize!=""){
														originalSize = Float.valueOf(musicSize);
													}else{
														originalSize=0;
													}
												}
											} else if (commonUrl.length() == 0) {
												commonUrl = musicUrl;
												if(musicSize!=""){
													commonSize = Float.valueOf(musicSize);
												}else{
													commonSize=0;
												}
											}
										}
									} else {
										// 继续循环
									}
								} else {
									// 继续循环
								}
							} else {
								if (beforechar.equals("：")
										|| beforechar.equals("）")
										|| beforechar.equals("-")
										|| beforechar.equals("—")
										|| beforechar.equals(" ")
										|| beforechar.equals(")")
										|| beforechar.equals("_")) {
									if (mp3Flag < 0) {
										if (FinalWmaUrl.length() == 0) {
											FinalWmaUrl = musicUrl;
										}
									} else {
										// 继续做事情
										ybFlag = data5.indexOf("原版伴奏");
										if (ybFlag >= 0) {
											if (originalUrl.length() == 0) {
												originalUrl = musicUrl;
												if(musicSize!=""){
													originalSize = Float.valueOf(musicSize);
												}else{
													originalSize=0;
												}
											}
										} else if (commonUrl.length() == 0) {
											commonUrl = musicUrl;
											if(musicSize!=""){
												commonSize = Float.valueOf(musicSize);
											}else{
												commonSize=0;
											}
										}
									}
								} else {
									// 继续循环
								}
							}
						}
					} else {
						// 继续循环
					}
				}
			}
		}
		if (originalUrl.length() > 0 && cleanUrl.length() > 0
				&& commonUrl.length() > 0) {
			if (cleanSize >= commonSize) {
				if (cleanSize > originalSize) {
					FinalUrl = cleanUrl;
				} else {
					FinalUrl = originalUrl;
				}
			} else {
				if (commonSize > originalSize) {
					FinalUrl = commonUrl;
				} else {
					FinalUrl = originalUrl;
				}
			}
		} else if (originalUrl.length() > 0) { // 判断有原版伴奏的字样；
			if (cleanUrl.length() > 0) {
				if (cleanSize > originalSize) {
					FinalUrl = cleanUrl;
				} else {
					FinalUrl = originalUrl;
				}
			} else if (commonUrl.length() > 0) {
				if (commonSize > originalSize) {
					FinalUrl = commonUrl;
				} else {
					FinalUrl = originalUrl;
				}
			} else {
				FinalUrl = originalUrl;
			}
		} else { // 没有原版字样;
			if (cleanUrl.length() > 0 && commonUrl.length() > 0) {
				if (cleanSize >= commonSize) {
					FinalUrl = cleanUrl;
				} else {
					FinalUrl = commonUrl;
				}
			} else if (cleanUrl.length() > 0) {
				FinalUrl = cleanUrl;
			} else if (commonUrl.length() > 0) {
				FinalUrl = commonUrl;
			} else {
				// 取wma
				if (FinalWmaUrl.length() > 0) {
					FinalUrl = FinalWmaUrl;
				} else {
					// 都找不到；
					FinalUrl = "error";
				}
			}
		}
		return FinalUrl;
	}
//	public long GetFileSizeAboutAccompanimentFrom5sing(String sMusicName, String sPlayerName) throws UnsupportedEncodingException {
//		sMusicName = sMusicName.toLowerCase();
//		sPlayerName = sPlayerName.toLowerCase();
//		String MusicAndPlayerName = "";
//		MusicAndPlayerName = sMusicName + " " + sPlayerName + " " + "伴奏";
//		String MusicAndPlayerNameGB2312 = "";
//		MusicAndPlayerNameGB2312 = java.net.URLEncoder.encode(MusicAndPlayerName, "GB2312");
//		String first_html = "";
//		first_html = "http://m.5sing.com/Search/Index/bz/" + MusicAndPlayerNameGB2312;
//		String firstxml = "";
//		firstxml = getXml(first_html, "utf8");
//		Pattern p = Pattern.compile("<div\\s*class=\"space_list\">[\\s\\S]*?</a>\\s*</p>\\s*</div>");
//		Matcher m = p.matcher(firstxml);
//		String path_test1 = "";
//		String path_test2 = "";
//		String path_test3 = "";
//		String path_test4 = "";
//		String path_test5 = "";
//		String path_test6 = "";
//		String path_test7 = "";
//		String checkMusicName="";
//		String checkMusicFlag="";
//		if (m.find()) {
//			path_test1 = m.group();
//			Pattern p1 = Pattern.compile("<p>\\s*<a\\s*href=[\\s\\S]*?</a></p>");
//			Matcher m1 = p1.matcher(path_test1);
//			while (m1.find()) {
//				path_test2 = m1.group();
//				checkMusicName = path_test2.replace("<p>", "");
//				Pattern pcheckMusicName = Pattern
//						.compile(">[\\s\\S]*?</a></p>");
//				Matcher mcheckMusicName = pcheckMusicName
//						.matcher(checkMusicName);
//				if (mcheckMusicName.find()) {
//					checkMusicFlag = mcheckMusicName.group();
//					int testtt = checkMusicFlag.indexOf(sMusicName);
//					if (testtt >= 0) {
//						Pattern p2 = Pattern.compile("<a\\s*href=\"([^\"]+)\"");
//						Matcher m2 = p2.matcher(path_test2);
//						if (m2.find()) {
//							path_test3 = m2.group();
//							Pattern p3 = Pattern.compile("\"([^\"]+)\"");
//							Matcher m3 = p3.matcher(path_test3);
//							if (m3.find()) {
//								path_test4 = m3.group();
//								path_test4 = path_test4.replace("\"", "");
//							}
//						}
//						break;
//					}
//				}
//			}
//			String secondhtml = "";
//			secondhtml = "http://m.5sing.com" + path_test4;
//			String secondxml = "";
//			secondxml = getXml(secondhtml, "utf8");
//			Pattern p4 = Pattern.compile("<audio\\s*src=[\\s\\S]*?</audio>");
//			Matcher m4 = p4.matcher(secondxml);
//			if (m4.find()) {
//				path_test5 = m4.group();
//				Pattern p5 = Pattern.compile("<audio\\s*src=\"([^\"]+)\"");
//				Matcher m5 = p5.matcher(path_test5);
//				if (m5.find()) {
//					path_test6 = m5.group();
//					Pattern p6 = Pattern.compile("\"([^\"]+)\"");
//					Matcher m6 = p6.matcher(path_test6);
//					if (m6.find()) {
//						path_test7 = m6.group();
//						path_test7 = path_test7.replace("\"", "");
//						long filesize1 = getFileSize(path_test7);
//						if (filesize1 > 1024 * 1024) {
//							DownloadUrl = path_test7;
//							return filesize1;
//						} else {
//							return SINGCANT;
//						}
//					} else {
//						return SINGCANT;
//					}
//				} else {
//					return SINGCANT;
//				}
//			} else {
//				return SINGCANT;
//			}
//		} else {
//			return SINGCANT;
//		}
//	}
	public static String postauth(String usernameAndPassword)
			throws IOException {
		/**
		 * 首先要和URL下的URLConnection对话。 URLConnection可以很容易的从URL得到。比如： // Using
		 * java.net.URL and //java.net.URLConnection
		 * 
		 * 使用页面发送请求的正常流程：在页面http://www.faircanton.com/message/loginlytebox.
		 * asp中输入用户名和密码，然后按登录，
		 * 跳转到页面http://www.faircanton.com/message/check.asp进行验证 验证的的结果返回到另一个页面
		 * 
		 * 使用java程序发送请求的流程：使用URLConnection向http://www.faircanton.com/message/
		 * check.asp发送请求 并传递两个参数：用户名和密码 然后用程序获取验证结果
		 */
		URL url = new URL("http://www.5sing.com/Popup/Login.aspx");
		URLConnection connection = url.openConnection();
		/**
		 * 然后把连接设为输出模式。URLConnection通常作为输入来使用，比如下载一个Web页。
		 * 通过把URLConnection设为输出，你可以把数据向你个Web页传送。下面是如何做：
		 */
		connection.setDoOutput(true);
		/**
		 * 最后，为了得到OutputStream，简单起见，把它约束在Writer并且放入POST信息中，例如： ...
		 */
		OutputStreamWriter out = new OutputStreamWriter(
				connection.getOutputStream(), "8859_1");
		out.write("__EVENTTARGET=&__EVENTARGUMENT=&__VIEWSTATE=%2FwEPDwUJNTMwMTc4MjMyZBgBBR5fX0NvbnRyb2xzUmVxdWlyZVBvc3RCYWNrS2V5X18WAQUGSXNTYXZleQeRUebp1KVeqi6teBH7W%2FJeOgc%3D&__EVENTVALIDATION=%2FwEWBQKX%2Fr%2BRDwKl1bKzCQK1qbSRCwKW8v7gAwLT%2Fr7ABJJKl0ZYaqfwREntUdzqb19cMv%2BC&"
				+ usernameAndPassword + "&Button=%E7%99%BB+%E5%BD%95"); // 向页面传递数据。post的关键所在！
		// remember to clean up
		out.flush();
		out.close();
		/**
		 * 这样就可以发送一个看起来象这样的POST： POST /jobsearch/jobsearch.cgi HTTP 1.0 ACCEPT:
		 * text/plain Content-type: application/x-www-form-urlencoded
		 * Content-length: 99 username=bob password=someword
		 */
		// 一旦发送成功，用以下方法就可以得到服务器的回应：
		String sCurrentLine;
		String sTotalString;
		sCurrentLine = "";
		sTotalString = "";
		InputStream l_urlStream;
		l_urlStream = connection.getInputStream();
		// 传说中的三层包装阿！
		BufferedReader l_reader = new BufferedReader(new InputStreamReader(
				l_urlStream));
		while ((sCurrentLine = l_reader.readLine()) != null) {
			sTotalString += sCurrentLine + "/r/n";

		}
		String setCookie = connection.getHeaderField("Set-Cookie");
		String cookie = setCookie.substring(0, setCookie.indexOf(";"));
		// System.out.println(cookie);
		// System.out.println(sTotalString);
		return cookie;
	}
	//通过豌豆夹下载
	public  String GetDownloadUrlfor5sing(String html) {
		String data1 = "";
		String data2 = "";
		Pattern p1 = Pattern.compile("<p><a href=\"([^\"]+)\"");
		Matcher m1 = p1.matcher(html);
		if (m1.find()) {
			data1 = m1.group();
			Pattern p2 = Pattern.compile("\"([^\"]+)\"");
			Matcher m2 = p2.matcher(data1);
			if (m2.find()) {
				data2 = m2.group();
				data2 = data2.replace("\"", "");
			}
		}
		return data2;
	}
	//走正常的下载流程
	public  String GetDownloadUrlbydd(String html) {
		String data1 = "";
		String data2 = "";
		String data3 = "";
		Pattern p1 = Pattern.compile("<div class=\"sing_view_bnt\">\\s*<a href=\"([^\"]+)\"");
		Matcher m1 = p1.matcher(html);
		if (m1.find()) {
			data1 = m1.group();
			Pattern p2 = Pattern.compile("<a href=\"([^\"]+)\"");
			Matcher m2 = p2.matcher(data1);
			if (m2.find()) {
				data2 = m2.group();
				Pattern p3 = Pattern.compile("\"([^\"]+)\"");
				Matcher m3 = p3.matcher(data2);
				if (m3.find()) {
					data3 = m3.group();
					data3 = data3.replace("\"", "");
				}
			}
			
		}
		return data3;
	}
	public long GetFileSizeAboutAccompanimentFrom5sing(String sMusicName, String sPlayerName) throws IOException {
//		DownloadUrl ="";
//		String firsturl = "";
//		firsturl = GetDownloadUrlFrom5ting(sMusicName,sPlayerName);
//		if(firsturl.equals("error")){
//			return SINGCANT;
//		}else{
//			String cookie = "";
//			cookie = postauth("txtUserName=hhhhao&txtPassword=hh123456");//输入用户名密码
//			m_5singcookie =cookie;
//			String html1;
//			html1 = getXml(firsturl, "utf-8", cookie);
//			String downloadurl = "";//最终的下载地址
//			downloadurl = GetDownloadUrlfor5sing(html1);
//			int ddFlag =0; //豆豆不足的标识
//			int unFlag =0; //禁止下载的标识
//			ddFlag =downloadurl.indexOf("豆豆");
//			unFlag =downloadurl.indexOf("禁止下载");
//			if(ddFlag <0 && unFlag<0){
//				//long filesize = getFileSize(downloadurl);
////				long filesize = getSize(downloadurl);
////				if (filesize > 0) {
//					DownloadUrl = downloadurl;
//					return 0;
////				}else{
////					return SINGCANT;
////				}
//			}else if(ddFlag >=0){
//				return SINGCANT;
//			}else{
//				//禁止下载的不做记录；
//				return SINGCANT;
//			}
//		}
		DownloadUrl ="";
		String url = getDownloadUrlFromServer(sMusicName,sPlayerName,DOWNLOAD_GETURL_ACCOMPANY);
		if (!url.equals("null"))
		{
			DownloadUrl = url;
			return 0;
		}
		return SINGCANT;
	}
	//用登录的方式下载
	public long GetFileSizeAboutAccompanimentFrom5singafterauth(String sMusicName, String sPlayerName) throws IOException {
		DownloadUrl ="";
		String cookie = "";
		cookie = postauth("txtUserName=hhhhao&txtPassword=hh123456");//输入用户名密码
		m_5singcookie =cookie;
		String url = getDownloadUrlFromServer(sMusicName,sPlayerName,DOWNLOAD_GETURL_ACCOMPANY);
		if (!url.equals("null"))
		{
			DownloadUrl = url;
			return 0;
		}
		return SINGCANT;
//		String firsturl = "";
//		firsturl = GetDownloadUrlFrom5ting(sMusicName,sPlayerName);
//		if(firsturl.equals("error")){
//			return SINGCANT;
//		}else{
//			String cookie = "";
//			cookie = postauth("txtUserName=hhhhao&txtPassword=hh123456");//输入用户名密码
//			m_5singcookie =cookie;
//			String html1;
//			html1 = getXml(firsturl, "utf-8", cookie);
//			String downloadurl = "";//最终的下载地址
//			downloadurl = GetDownloadUrlbydd(html1);
//			int ddFlag =0; //豆豆不足的标识
//			int unFlag =0; //禁止下载的标识
//			ddFlag =downloadurl.indexOf("豆豆");
//			unFlag =downloadurl.indexOf("禁止下载");
//			if(ddFlag <0 && unFlag<0){
//				//long filesize = getFileSize(downloadurl);
//				//if (filesize > 0) {
//					DownloadUrl = downloadurl;
//					return 0;
//				//}else{
//				//	return SINGCANT;
//				//}
//			}else if(ddFlag >=0){
//				return SINGCANT;
//			}else{
//				//禁止下载的不做记录；
//				return SINGCANT;
//			}
//		}
	}
	public void registerHandler(Handler handler) {
		this.progressHandler = handler;
	}

	public void unregisterHandler() {
		progressHandler = null;
	}

	public long getNetworkSpeed() {
		return networkSpeed;
	}
	
	public static String getXcodeFromPath(String path){
		String json = null;
		URL url;
		try {
			url = new URL(path);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5 * 1000);
			conn.setRequestMethod("GET");
			if (conn.getResponseCode() == 200) {
				InputStream is = conn.getInputStream();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				int len = 0;
				while ((len = is.read(buffer)) != -1)
				{
					baos.write(buffer,0,len);
				}
				json = baos.toString();
			} 
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
 }
	
	public String getDownloadUrlFromServer(String sMusicName, String sPlayerName,int downloadtype)
	{
		String url = "";
		if (ServiceManager.getNetworkService().acquire(true))
		{
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("song", sMusicName));
			params.add(new BasicNameValuePair("singer", sPlayerName));
			params.add(new BasicNameValuePair("flag", String.valueOf(downloadtype)));
			HttpEntityEnclosingRequestBase httpRequest = new HttpPost(IMediaService.GET_SONG_DOWNLOAD_URL);
			try
			{
				httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
				HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
				if (httpResponse.getStatusLine().getStatusCode() == 200) 
				{
					url = EntityUtils.toString(httpResponse.getEntity());
				} 
			}catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return url;
	}
	
}
