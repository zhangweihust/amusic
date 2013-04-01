package com.android.media.screens.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amusic.media.R;
import com.android.media.MediaApplication;
import com.android.media.screens.AmtScreen;
import com.android.media.services.IMediaService;
import com.android.media.services.INetworkService;
import com.android.media.services.impl.ServiceManager;
import com.android.media.utils.DeviceInformation;
import com.android.media.utils.ToastUtil;
import com.android.media.utils.DeviceInformation.InfoName;

public class ScreenSuggestionFeedback extends AmtScreen implements OnClickListener{

	private Button btn_ok;
	private Button btn_cancel;
	private EditText et_suggestion;
	private EditText et_QQ;
	private EditText et_tel;
	public final static String PREF = "Suggestions";
	public final static String INFO_KEY = "Suggestion";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_suggestion_feedback);	
		setScreenTitle(getString(R.string.screen_suggestion_feedback_bar));
		et_suggestion = (EditText) this.findViewById(R.id.et_suggestion);
		et_QQ = (EditText) this.findViewById(R.id.et_QQ);
		et_tel = (EditText) this.findViewById(R.id.et_tel);
		
		btn_ok = (Button) this.findViewById(R.id.btn_ok);
		btn_cancel = (Button) this.findViewById(R.id.btn_cancel);
		
		btn_ok.setOnClickListener(this);
		btn_cancel.setOnClickListener(this);
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		ServiceManager.getAmtMedia().getGoPlayerBtn().setVisibility(View.INVISIBLE);
		setScreenTitle(getString(R.string.screen_suggestion_feedback_bar));
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.btn_ok:
			String suggestion = "";
			String qq = "";
			String tel = "";
			String imei = "";
			String data = "";
			suggestion = et_suggestion.getText().toString().trim();
			if(suggestion ==null ||suggestion.equals("") || suggestion.length()>300 || suggestion.length()<5){
				Toast toast = ToastUtil.getInstance().getToast(getString(R.string.screen_suggestion_feedback_info1));
				toast.setDuration(Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}else{
				qq = et_QQ.getText().toString().trim();
				tel = et_tel.getText().toString().trim();
				imei = DeviceInformation.getInformation(InfoName.IMEI);
				data = "imei=" + imei + "&qq=" + qq + "&tel=" + tel + "&suggestion=" + suggestion;	
				sendOrSave(data);
				et_suggestion.setText("");
				et_QQ.setText("");
				et_tel.setText("");
				this.onBackPressed();
			}
			break;
		case R.id.btn_cancel:
			this.onBackPressed();
			break;
		}
	}

	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(this.et_QQ.getWindowToken(), 0);
		Activity parent = getParent();
        if( parent != null){
            parent.onBackPressed();
        }
	}

	public void sendOrSave(String data){
		String info = getString(R.string.screen_suggestion_feedback_info2);
		if(!send(data)){
			save(data);
		}else{
			info = getString(R.string.screen_suggestion_feedback_info3);
		}
		Toast toast = ToastUtil.getInstance().getToast(info);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
	
	public static boolean send(String url){
		 INetworkService networkService = ServiceManager.getNetworkService();
		 if (networkService.acquire(true)) {
			 if(sendSuggestion(url)){
				return true;   
			 }
		 }
		 return false;
	}
	
	private void save(String data){
	     SharedPreferences sp = null;
	     sp = this.getSharedPreferences(PREF, 0);
		 SharedPreferences.Editor editor = sp.edit();
		 Random random = new Random();
		 editor.putString(INFO_KEY + Math.abs(random.nextInt())%100, data);
		 editor.commit();
	}
	
	public static  boolean isSPNull(){
		Context c = MediaApplication.getContext();
		SharedPreferences sp = c.getSharedPreferences(ScreenSuggestionFeedback.PREF, 0);
	    Map<String, ?>  map = sp.getAll();
	    if(map.isEmpty()){
	    	return true; 
	    }       
	    return false;
	}
	
	public static void delete(String key){
		Context c = MediaApplication.getContext();
		SharedPreferences sp = c.getSharedPreferences(ScreenSuggestionFeedback.PREF, 0);
		SharedPreferences.Editor editor = sp.edit();
		editor.remove(key);
		editor.commit();
	}
	
	private static boolean sendSuggestion(String data){
		URL u;
		PrintWriter out = null;
		try {
			u = new URL(IMediaService.SEND_SUGGESTION_FEEDBACK_URL);
			HttpURLConnection httpConn = (HttpURLConnection) u.openConnection();
			HttpURLConnection.setFollowRedirects(true);
			httpConn.setRequestMethod("POST");
			httpConn.setConnectTimeout(5000);
			httpConn.setDoOutput(true);
			httpConn.setDoInput(true);
			out = new PrintWriter(httpConn.getOutputStream());
			//发送请求参数
			out.print(data);
			//flush输出流的缓冲
			out.flush();
			if(httpConn.getResponseCode()==200){
				int length = 0;
				ByteArrayOutputStream bos=new ByteArrayOutputStream();
				InputStream is = httpConn.getInputStream();
				while((length=is.read())!=-1)
				{
					bos.write(length);
				}
				String result = new String(bos.toByteArray());
				if(result.trim().equals("0")){
					return true;
				}
			}

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if (out != null)
			{
				out.close();
			}
		}
		return false;
	}
	
	@Override
	public boolean hasMenu() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(this.et_QQ.getWindowToken(), 0);
		super.onPause();
	}
	
	
	
	
	
}
