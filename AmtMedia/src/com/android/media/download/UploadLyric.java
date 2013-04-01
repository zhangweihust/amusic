package com.android.media.download;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;


import com.amusic.media.R;
import com.android.media.dialog.OnScreenHint;
import com.android.media.event.impl.MediaEventArgs;
import com.android.media.event.impl.MediaEventTypes;
import com.android.media.services.IMediaEventService;
import com.android.media.services.IMediaService;
import com.android.media.services.impl.ServiceManager;

public class UploadLyric {
	private final int TIME_OUT = 10 * 1000;
	
	public boolean uploadlyrics(String fileName) {
		try {
			HttpEntityEnclosingRequestBase lyricHttpRequest = new HttpPost(IMediaService.SEND_LYRICS_SERVER_ACTION);
			List<NameValuePair> lyricParams = new ArrayList<NameValuePair>();
			File file = new File(fileName);
            FileInputStream in = new FileInputStream(file);
            byte[] buffer = new byte[(int) file.length() + 100];
            int length = in.read(buffer);
//            String lyrics = Base64.encodeToString(buffer, 0, length,Base64.DEFAULT);
			String lyrics = new String(buffer,0,length,"UTF-8");
			
			File kscFile = new File("/sdcard/amt_player/lyrics/littletiger.txt");
//			if (!kscFile.exists()) {
//				kscFile.createNewFile();
//			}
			FileWriter kscFileWriter = new FileWriter(kscFile);
			kscFileWriter.write(lyrics);
			kscFileWriter.close();
		
//			String lyrics = new String(bt,"UTF-8");
			lyricParams.add(new BasicNameValuePair("content",lyrics));
			lyricHttpRequest.setEntity(new UrlEncodedFormEntity(lyricParams, HTTP.UTF_8));
			HttpResponse lyricHttpResponse = new DefaultHttpClient().execute(lyricHttpRequest);
//			in.close();
			int lyricState = lyricHttpResponse.getStatusLine().getStatusCode();
			if (lyricState == 200) {
				return true;
			} else {
			}
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean uploadLyricFile(String filePath) {
		String result = null;
        String BOUNDARY = UUID.randomUUID().toString(); // 边界标识 随机生成
        String LINE_END = "\r\n";
        String PREFIX = "--";
        String CONTENT_TYPE = "multipart/form-data"; // 内容类型
        String CHARSET = "utf-8";
 
        try {
            URL url = new URL(IMediaService.SEND_LYRICSFILE_SERVER_ACTION);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(TIME_OUT );
            conn.setConnectTimeout(TIME_OUT);
            conn.setDoInput(true); // 允许输入流
            conn.setDoOutput(true); // 允许输出流
            conn.setUseCaches(false); // 不允许使用缓存
            conn.setRequestMethod("POST"); // 请求方式
            conn.setRequestProperty("Charset", "UTF-8"); // 设置编码
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary="
                    + BOUNDARY);
  
            File file = new File(filePath);
            if (file != null) {
                
                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                StringBuffer sb = new StringBuffer();
                sb.append(PREFIX);
                sb.append(BOUNDARY);
                sb.append(LINE_END);
                /**
                 * 这里重点注意： name里面的值为服务端需要key 只有这个key 才可以得到对应的文件
                 * filename是文件的名字，包含后缀名的 比如:abc.png
                 */
 
                sb.append("Content-Disposition: form-data; name=\"attachment\"; filename=\""
                        + file.getName() + "\"" + LINE_END);
                sb.append("Content-Type: multipart/form-data; charset=" + CHARSET + LINE_END);
                sb.append(LINE_END);
                dos.write(sb.toString().getBytes());
                InputStream is = new FileInputStream(file);
                byte[] bytes = new byte[1024];
                int len = 0;
                while ((len = is.read(bytes)) != -1) {
                    dos.write(bytes, 0, len);
                }
                is.close();
                dos.write(LINE_END.getBytes());
                byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
                dos.write(end_data);
                dos.flush();
                dos.close();
                
                int res = conn.getResponseCode();
                
                InputStream input = conn.getInputStream();
                StringBuffer sb1 = new StringBuffer();
                int ss;
                while ((ss = input.read()) != -1) {
                    sb1.append((char) ss);
                }
                result = sb1.toString();
                if (res == 200) {
                	return true;
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		return false;
	}
	
	public boolean uploadlyrics2(String fileName) {
		MediaEventArgs args = new MediaEventArgs();
		IMediaEventService mediaEventService = ServiceManager.getMediaEventService();
		
		try {
            URL url = new URL(IMediaService.SEND_LYRICS_SERVER_ACTION);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(TIME_OUT );
            conn.setConnectTimeout(TIME_OUT);
            conn.setDoInput(true); // 允许输入流
            conn.setDoOutput(true); // 允许输出流
            conn.setUseCaches(false); // 不允许使用缓存
            conn.setRequestMethod("POST"); // 请求方式
            conn.setRequestProperty("Charset", "UTF-8"); // 设置编码
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Content-Type", "application/octet-stream");

            File file = new File(fileName);
            if (file != null) {
                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                InputStream is = new FileInputStream(file);
                byte[] bytes = new byte[1024];
                int len = 0;
                while ((len = is.read(bytes)) != -1) {
                    dos.write(bytes, 0, len);
                }
                
                dos.flush();  
                dos.close();
                
                int lyricState = conn.getResponseCode();
    			if (lyricState == 200) {
    				mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_UPLOAD_LRC_LYRIC_FINISH));
    				return true;
    			} else {
//    				ServiceManager.getAmtMediaHandler().post(new Runnable(){
//    					@Override
//    					public void run() {
//    						OnScreenHint mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_lyric_speed_upload_error));
//    						mOnScreenHint.cancel();
//    						mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_lyric_speed_upload_error));
//    						mOnScreenHint.show();
//    					}
//    			    });
    				mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_UPLOAD_LRC_LYRICS_ERROR));
    			}
            }
		 }catch (MalformedURLException e) {
//			 ServiceManager.getAmtMediaHandler().post(new Runnable(){
//					@Override
//					public void run() {
//						OnScreenHint mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_lyric_speed_upload_error));
//						mOnScreenHint.cancel();
//						mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_lyric_speed_upload_error));
//						mOnScreenHint.show();
//					}
//			 });
			 mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_UPLOAD_LRC_LYRICS_ERROR));
	         e.printStackTrace();
         } catch (IOException e) {
//        	 ServiceManager.getAmtMediaHandler().post(new Runnable(){
//					@Override
//					public void run() {
//						OnScreenHint mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_lyric_speed_upload_error));
//						mOnScreenHint.cancel();
//						mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_lyric_speed_upload_error));
//						mOnScreenHint.show();
//					}
//			 });
        	 mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_UPLOAD_LRC_LYRICS_ERROR));
             e.printStackTrace();
         }
		return false;
	}

}
