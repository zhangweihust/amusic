package com.amusic.media.screens.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.DownloadListener;
import android.webkit.WebBackForwardList;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.amusic.media.MediaApplication;
import com.amusic.media.R;
import com.amusic.media.dialog.DialogDownloadbyWeb;
import com.amusic.media.model.ScreenArgs;
import com.amusic.media.screens.SearchScreen;

public class ScreenWebViewSongs extends SearchScreen{
	private WebView wv1;
	private DialogDownloadbyWeb DownloadbyWebDialog;
	private long nfileSize;
	private String content;
	private int searchType;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setScreenTitle(getString(R.string.screen_home_tab_search));
		setContentView(R.layout.screen_search_webview);
		ScreenArgs args = (ScreenArgs) getIntent().getSerializableExtra("args");
		content = (String) args.getExtra("content");
		searchType = (Integer) args.getExtra("searchType");
		wv1 = (WebView) findViewById(R.id.screen_search_wv);
		wv1.getSettings().setJavaScriptEnabled(true);  
		wv1.requestFocus();
		wv1.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    // TODO Auto-generated method stub
                    view.loadUrl(url);
                    view.getSettings().setJavaScriptEnabled(true);  
                    return true;
            }
         });    
		 wv1.setDownloadListener(new DownloadListener() {
	            public void onDownloadStart(String url, String userAgent,String contentDisposition, String mimetype,long contentLength) {
	                    //实现下载的代码
	                    Uri uri = Uri.parse(url);	                    
	                    String sType="";
	                    String smusicName="";
	                    String splayerName="";
//	                    sType=getFileType(uri.toString());
//	                    if(sType.equals("mp3")){
	                    	String numstr1 = content;
	                    	numstr1 = numstr1.toLowerCase();
	                    	numstr1 = numstr1.trim();
//	                    	while (numstr1.startsWith(" ")) {  
//	                    		numstr1 =numstr1.replaceFirst("　", "");   
//	                    		numstr1 = numstr1.trim();  
//	                    		
//	                    	}  
//	                    	String[] strArray=numstr1.split(" +");
//	                    	if(strArray.length>=2){
//	                    		
//	                    		smusicName =strArray[0];
//	                    		splayerName =strArray[1];
//	                    		}else if(strArray.length==1){	                    		
//	                    		smusicName =strArray[0];
//	                    		splayerName =strArray[0];
//	                    	}
	                    	smusicName =numstr1;
	                    	//splayerName =numstr1;
	                    	DownloadbyWebDialog = new DialogDownloadbyWeb(uri.toString());
	                    	DownloadbyWebDialog.show(smusicName,splayerName);
	        				int mp3flag =0;
	        				mp3flag = url.indexOf(".mp3");
	        				int zhangmenflag =0;
	        				zhangmenflag =url.indexOf("zhangmenshiting");
	        				if(mp3flag <0 && zhangmenflag<0){
	        					Toast.makeText(MediaApplication.getContext(), MediaApplication.getContext().getString(R.string.notmp3_download_by_web_title),
	        							Toast.LENGTH_SHORT).show();
	        				}
//
//	                    }else{
//	                    	Toast.makeText(MediaApplication.getContext(), getString(R.string.screen_search_from_baidu_title),
//	            					Toast.LENGTH_SHORT).show();	                    	
//	                    }

	                    //Intent intent = new Intent(Intent.ACTION_VIEW,uri);
	                    //startActivity(intent);
	                    }
	     });
		 
		 loadUrl();
			
	}
	
	private void loadUrl(){
		String numstr1 = content;
		numstr1 = content.toLowerCase();
		if(searchType==2){
			String MusicNameGB2312 = "";
			try {
				MusicNameGB2312 = java.net.URLEncoder.encode(numstr1, "utf8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// wv1.setBackgroundColor( 0x0000ffff );
			numstr1 = numstr1.replace(" ", "");
			if (numstr1.equals("") || numstr1.equals(null)) {
				wv1.loadUrl("http://mp3.easou.com/index.e?wver=t&l=215.6&sb=0&esid=BHpaHpVz40v");

			} else {
				wv1.loadUrl("http://mp3.easou.com/s.e?esid=cklaH2HqAVI&wver=t&l=61AA.1&q="
						+ MusicNameGB2312 + "&selectType=on");

			}
		}else {
			String MusicNameGB2312 = "";
			try {
				MusicNameGB2312 = java.net.URLEncoder.encode(numstr1, "GB2312");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			numstr1 = numstr1.replace(" ", "");
			if (numstr1.equals("") || numstr1.equals(null)) {
				wv1.loadUrl("http://mp3.baidu.com");

			} else {
				wv1.loadUrl("http://mp3.baidu.com/m?tn=baidump3mobile&ssid=&from=&bd_page_type=1&uid=&pu=&f=ms&ct=671088640&lf=&rn=20&lm=0&gate=33&word=" 
						+ MusicNameGB2312 );

			}
		}
	}
	
	
	private String getFileType(String sURL) {
		int nFileLength = -1;
		nfileSize =0;
		String sFileType ="";
		try {
			URL url = new URL(sURL);
			HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
			httpConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; U; Android 2.2; en-us; Nexus One Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
			int responseCode = httpConnection.getResponseCode();
			if (responseCode >= 400) {
				System.err.println("Error Code : " + responseCode);
				return ""; // -2 represent access is error
			}
			String sHeader;
			for (int i = 1;; i++) {
				sHeader = httpConnection.getHeaderFieldKey(i);
				if (sHeader != null) {
					if (sHeader.toLowerCase().equals("content-length")) {
						nFileLength = Integer.parseInt(httpConnection.getHeaderField(sHeader));
						nfileSize =nFileLength;
						//break;
					}
					if (sHeader.toLowerCase().equals("content-disposition")) {
						String parseString = httpConnection.getHeaderField(sHeader);
						Pattern p1 = Pattern.compile("\"([^\"]+)\"");
						Matcher m1 = p1.matcher(parseString);
						String Type="";
						if(m1.find()){
							String stmp="";
							stmp =m1.group();	
							stmp =stmp.replace("\"", "");
							Type=stmp.substring(stmp.lastIndexOf(".")+1);
						}
						
						sFileType=Type;
//						break;
					}
				} else
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sFileType;
	}
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		setScreenTitle(getString(R.string.screen_home_tab_search));
		ScreenArgs args = (ScreenArgs) intent.getSerializableExtra("args");
		content = (String) args.getExtra("content");
		searchType = (Integer) args.getExtra("searchType");
		loadUrl();
		super.onNewIntent(intent);
	}
	public boolean onKeyDown(int keyCode, KeyEvent event) { 
    	WebBackForwardList wl = wv1.copyBackForwardList();
	    if (wv1.canGoBack() && event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) { 
	    	if(wl.getCurrentIndex() > 1){
	    		wv1.goBack();
	    	}else{
	    		wv1.goBack();
	    		getParent().onBackPressed();
	    	}
	       return true;  
	    }
	    return super.onKeyDown(keyCode, event);  
	}  
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		setScreenTitle(getString(R.string.screen_home_tab_search));
	}

}
