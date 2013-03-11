package com.amusic.media.dialog;

import java.io.UnsupportedEncodingException;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amusic.media.MediaApplication;
import com.amusic.media.R;
import com.amusic.media.event.impl.MediaEventArgs;
import com.amusic.media.event.impl.MediaEventTypes;
import com.amusic.media.screens.SearchScreen;
import com.amusic.media.services.IMediaService;
import com.amusic.media.services.impl.ServiceManager;

public class DialogDownloadbyWeb extends SearchScreen {

	private EditText audio_artist;
	private EditText audio_title;
//	private EditText audio_year;
//	private Spinner audio_genre;

	private Button btn_ok;
	private Button btn_cancel;

	private Dialog DownloadbyWebDialog;
	private String url = null;
	private int artistflag = 0;
	private int titleflag = 0;
	
	public DialogDownloadbyWeb(String url) {
		DownloadbyWebDialog = new Dialog(ServiceManager.getAmtMedia(), R.style.CustomDialog);
		DownloadbyWebDialog.setCanceledOnTouchOutside(true);
		DownloadbyWebDialog.setContentView(R.layout.dialog_download_music_from_web);
		init();
		this.url=url;
	}

	private void init() {
		audio_artist = (EditText) DownloadbyWebDialog.findViewById(R.id.audio_artist);
		audio_title = (EditText) DownloadbyWebDialog.findViewById(R.id.audio_title);

		btn_ok = (Button) DownloadbyWebDialog.findViewById(R.id.btn_ok);
		btn_cancel = (Button) DownloadbyWebDialog.findViewById(R.id.btn_cancel);

		btn_ok.setOnClickListener(btn_ok_listener);
		btn_cancel.setOnClickListener(btn_cancel_listener);
		audio_artist.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				 if(v.getId()==R.id.audio_artist) {
					titleflag = 0;
					if (artistflag == 0) {
						audio_artist.selectAll();
						artistflag=2;
						InputMethodManager imm = (InputMethodManager) ServiceManager
								.getAmtMedia().getSystemService(
										Context.INPUT_METHOD_SERVICE);
						imm.showSoftInput(v, 0);
					}
					if(artistflag!=0){
						artistflag--;
					}
				}
			}
		
		});
		audio_title.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				if (v.getId() == R.id.audio_title) {
					artistflag = 0;
					if (titleflag == 0) {
						audio_title.selectAll();
						titleflag=2;
						InputMethodManager imm = (InputMethodManager) ServiceManager
								.getAmtMedia().getSystemService(
										Context.INPUT_METHOD_SERVICE);
						imm.showSoftInput(v, 0);
					}
					if(titleflag!=0){
						titleflag--;
					}
				}
			}
		
		});
		 

	}
	public void show(String songname,String playername) {	
		    int mp3flag =0;
		    mp3flag = url.indexOf(".mp3");
		    String m_songname ="";
		    if(mp3flag >0){
		    	m_songname=url.substring(url.lastIndexOf("/"));
		    	m_songname =m_songname.replace("/", "");
		    	mp3flag =0;
		    	mp3flag = m_songname.indexOf(".mp3");
		    	if(mp3flag>0){
		    	    m_songname =m_songname.substring(0,mp3flag);
		    	    m_songname =m_songname+".mp3";
		    	}else{
		    		m_songname="";
		    	}
		    }else{
		    	m_songname=url.substring(url.lastIndexOf("/"));
		    	m_songname =m_songname.replace("/", "");
		    }
			//audio_title.setText(songname);
			String mSongNameutf8 = "";
			try {
				mSongNameutf8 = java.net.URLDecoder.decode(m_songname, "utf-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    audio_title.setText(mSongNameutf8);
			audio_artist.setText(playername);
			DownloadbyWebDialog.show();
	}

	public void dismiss() {
		DownloadbyWebDialog.dismiss();
	}

	private View.OnClickListener btn_ok_listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			artistflag =0;
			titleflag = 0;
			String new_name = audio_title.getText().toString().trim();
			String new_artist = audio_artist.getText().toString().trim();
			//if(new_name.equals("")||new_artist.equals("")){
			if(new_name.equals("")){
				Toast.makeText(MediaApplication.getContext(), MediaApplication.getContext().getString(R.string.screen_search_no_name),
						Toast.LENGTH_SHORT).show();

			}else{
				Toast.makeText(
						MediaApplication.getContext(),
						MediaApplication.getContext().getString(
								R.string.dialog_download_by_web_title),
						Toast.LENGTH_SHORT).show();				
				MediaEventArgs args = new MediaEventArgs();
				args.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_DOWNLOAD_FORM_WEB);
				args.putExtra("downloadResource", IMediaService.RESOURCE_SEARCH);
				int mp3flag =0;
				mp3flag = url.indexOf(".mp3");
				int zhangmenflag =0;
				zhangmenflag =url.indexOf("zhangmenshiting");
				if(mp3flag <0 && zhangmenflag<0){
					if(url.lastIndexOf(".") >0){
						String exname = url.substring(url.lastIndexOf("."),
								url.length());
						exname = exname.replace(".", "");
						args.putExtra("downloadType", exname);
					}else{
						String exname =url.substring(url.length()-3,
								url.length());
						args.putExtra("downloadType", exname);
					}
				}else{
					String exname ="mp3";
					args.putExtra("downloadType", exname);
				}
				if(new_name.indexOf(".")>0){
					new_name =new_name.substring(0,new_name.indexOf("."));
				}
				args.putExtra("song", new_name);
				args.putExtra("singer", new_artist);
				args.putExtra("url", url);
				mediaEventService.onMediaUpdateEvent(args);
				DownloadbyWebDialog.dismiss();
			}
		}
	};


	
	private View.OnClickListener btn_cancel_listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			artistflag =0;
			titleflag = 0;
			DownloadbyWebDialog.dismiss();
		}
	};
}
