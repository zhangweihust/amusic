package com.android.media.download;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

import com.android.media.MediaApplication;
import com.android.media.event.IMediaEventArgs;
import com.android.media.event.impl.MediaEventArgs;
import com.android.media.event.impl.MediaEventTypes;
import com.android.media.services.IMediaEventService;
import com.android.media.services.IMediaService;
import com.android.media.services.INetworkService;
import com.android.media.services.impl.ServiceManager;
import com.android.media.task.DownloadTask;

public class DownloadLyric {
	private final IMediaService mediaService;
	private final IMediaEventService mediaEventService;
	private final INetworkService networkService;
	private final int bufferSize = 1024;
	private IMediaEventArgs args;
	DownloadTask mDownloadTask = null;

	public DownloadLyric(IMediaEventArgs args) {
		this.networkService = ServiceManager.getNetworkService();
		this.mediaEventService = ServiceManager.getMediaEventService();
		this.mediaService = ServiceManager.getMediaService();
		this.args = args;
	}

	public void downloadLyrics() {
		if (!networkService.acquire(false)) {
			args.putExtra("show", false);
			mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_LYRICS_ERROR));
			return;
		}
		final String singer = (String) args.getExtra("singer_Name");
		final String song = (String) args.getExtra("song_Name");
		final long duration = (Long) args.getExtra("duration");
		final String filename = (String) args.getExtra("filename");
		final String audiofilePath = (String) args.getExtra("audiofilePath");
		boolean isNeedPopDialog = (Boolean) args.getExtra("isNeedPopDialog");
		
		//TODO
		String filePath = MediaApplication.lyricPath + singer.replace("/", "_") + "-" + song.replace("/", "_") + IMediaService.LYRICS_SUFFIX;
		String fname = MediaApplication.lyricPath + filename + IMediaService.LYRICS_SUFFIX;
		if(!fname.equals(filePath)){
			filePath = fname;	
		}
		
		OutputStream out = null;
		InputStream in = null;

		try {
			boolean success = false;
			HttpEntityEnclosingRequestBase httpRequest = new HttpPost(IMediaService.DOWNLOAD_SERVER_BASE + IMediaService.DOWNLOAD_LYRICS_SERVER_ACTION);
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("song", song));
			params.add(new BasicNameValuePair("singer", singer));
			params.add(new BasicNameValuePair("duration", String.valueOf(duration)));
			httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				String strResult = EntityUtils.toString(httpResponse.getEntity());
				strResult = strResult.replace("\"", "");
				String lyricUrlPart = null;
				
//				String s = new String(strResult.getBytes("UNICODE"),"UNICODE");
//				strResult = s;

				/*strResult = ===== 1 ========== 2 ========== 4 ========== 5 =========singer = "xx" or 0
				========= 6 ========== 7 ========== 9 ========== 12 =====
				[{"song":"\u4f20\u5947","singer":""},{"song":"\u4f20\u5947","singer":"aimini"},
				{"song":"\u4f20\u5947","singer":"amini"},
				{"song":"\u4f20\u5947","singer":"\u4e54\u7ef4\u6021"},
				{"song":"\u4f20\u5947","singer":"\u51e4\u51f0\u4f20\u5947"},
										.........
										.........
										.........
										.........
				{"song":"\u4f20\u5947","singer":"\u9ea6\u514b\u6447\u6eda"},
				{"song":"\u4f20\u5947","singer":"\u9ec4\u7acb\u884c"},
				{"song":"\u4f20\u5947","singer":"\u9f9a\u73a5"}]
				<div style="border:1px solid #990000;padding-left:20px;margin:0 0 10px 0;">
*/
				List<String> lyricList = new ArrayList<String>();
				if (strResult.indexOf("{") + 1 <= strResult.lastIndexOf("}")) {
					strResult = strResult.substring(strResult.indexOf("{") + 1, strResult.lastIndexOf("}"));
					
					String[] items = strResult.split("\\},\\{");
					
					if(items.length > 1) {
						String lsong = null;
						String lsinger = null;
						String lname = null;
						for(String item : items) {
							String[] parts = item.split(",");
							String[] datas = null;
							lsong = "";
							lsinger = "";
							for(String part : parts) {
								datas = part.split(":");// part : song:\u4f20\u5947
								if(datas != null && datas[0].equalsIgnoreCase("song")) {
									if(datas.length < 2) {
										continue;
									}
									lsong = datas[1];
								} else if(datas != null && datas[0].equalsIgnoreCase("singer")) {
									if(datas.length < 2) {
										continue;
									}
									lsinger = datas[1];
								}
							}
							
							if (lsong.equals("") || lsinger.equals("")) {
								continue;
							}
							lname = utf2unicode(lsong) + "——" + utf2unicode(lsinger);
							lyricList.add(lname);
							
						}
						int size = lyricList.size();
						if(size > 1) {
							MediaEventArgs args1 = new MediaEventArgs();
							if (isNeedPopDialog) {
								args1.putExtra("duration",duration);
								args1.putExtra("lyricList", lyricList);
								args1.putExtra("filename", filename);
								args1.putExtra("isKmedia",(Boolean)args.getExtra("isKmedia"));
								args1.putExtra("lyricPath", (String) args.getExtra("lyricPath"));
								args1.putExtra("isNeedPopDialog", false);
								args1.putExtra("audiofilePath", audiofilePath);
								mediaEventService.onMediaUpdateEvent(args1.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_LYRIC_SELECT_UI));
							} else {
								args1.putExtra("show", false);
								mediaEventService.onMediaUpdateEvent(args1.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_LYRICS_ERROR));
							}
							return;
						} else if(size == 1) {
							lyricUrlPart = lyricList.get(0);
						}
					} 
	
					String[] parts = strResult.split(",");
					String[] datas = null;

					for (String part : parts) {
						datas = part.split(":");
						if (datas != null && datas.length == 2 && datas[0].equalsIgnoreCase("encryptname")) {
							lyricUrlPart = datas[1];
							break;
						}
					}
				}
				if (lyricUrlPart != null) {
//					HttpURLConnection connection = (HttpURLConnection) new URL(IMediaService.DOWNLOAD_SERVER_BASE + lyricUrlPart + ".gc").openConnection();
//					in = connection.getInputStream();
//					File file = new File(filePath);
//					if (file.exists()) {
//						file.delete();
//					}
//					file.getParentFile().mkdirs();
//					file.createNewFile();
//					out = new FileOutputStream(file);
//					in = connection.getInputStream();
//					byte[] buffer = new byte[bufferSize];
//					int read = 0;
//					while ((read = in.read(buffer)) > 0) {
//						out.write(buffer, 0, read);
//					}
//					success = true;
					downloadLyrics(IMediaService.DOWNLOAD_SERVER_BASE + lyricUrlPart + ".gc", 0, filePath, args);
					
				} else {
					args.putExtra("show", true);
					mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_LYRICS_ERROR));
				}
			}
//			if (success) {
//				mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_LYRIC_FINISH));
//			} else {
//				args.putExtra("show", true);
//				mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_LYRICS_ERROR));
//			}
			return;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		args.putExtra("show", true);
		mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_LYRICS_ERROR));
	}
	
	public static String utf2unicode(String strScr) {
		if (strScr == null) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		int i = 0;
		while(i < strScr.length()) {
			String temp = strScr.substring(i);
			if (temp.startsWith("\\u")) {
				char c = (char) Integer.valueOf(temp.substring(2, 6),16).intValue();
				
				sb.append(c);
				i += 6;
			} else {
				sb.append(strScr.charAt(i));
				i++;
			}
		}
		return sb.toString();
	}
	
	private void downloadLyrics(String url, long startPos, String path, IMediaEventArgs args) {
		final DownloadJob mJob = new DownloadJob(args);
		mJob.setDownloadUrl(url);
		mJob.setDownloadStartPos(startPos);
		mJob.setPath(path);
		ServiceManager.getAmtMedia().getHandler().post(new Runnable() {
			@Override
			public void run() {
				if (mDownloadTask != null) {
					mDownloadTask.onCancelled();
				}
				mDownloadTask = new DownloadTask(mJob);
				mJob.setDownloadTask(mDownloadTask);
				mDownloadTask.execute(IMediaService.DOWNLOAD_START_ON_LYRICS);
			}
		});
	}

}
