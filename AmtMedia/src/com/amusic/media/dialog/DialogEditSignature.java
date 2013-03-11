package com.amusic.media.dialog;

import java.io.File;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amusic.media.R;
import com.amusic.media.event.IMediaEventArgs;
import com.amusic.media.event.impl.MediaEventArgs;
import com.amusic.media.event.impl.MediaEventTypes;
import com.amusic.media.services.impl.ServiceManager;

public class DialogEditSignature{

	private Button btn_yes;
	private Button btn_cancel;

	private TextView music_name;
	private EditText self_signature_name;
	private Dialog EditSignatureDialog;
   private Context context;
    private String string_song_name;
    private String string_edit_signature_name;
    private String string_full_path;
    
	public DialogEditSignature(Context context,String songName,String edit_signature_Name,String fullDirectory) {
		this.context = context;
		EditSignatureDialog = new Dialog(context, R.style.CustomDialog);
		EditSignatureDialog.setContentView(R.layout.dialog_edit_signature);
		string_song_name = "";
		if(songName.contains("_")){
			string_song_name = songName.substring(0, songName.lastIndexOf("_"));
		}
		EditSignatureDialog.setCanceledOnTouchOutside(true);
		string_edit_signature_name = edit_signature_Name;
		string_full_path = fullDirectory;
		init();
	}

	private void init() {
		btn_yes = (Button) EditSignatureDialog.findViewById(R.id.screen_record_player_edit_signature_ok);
		btn_cancel = (Button) EditSignatureDialog.findViewById(R.id.screen_record_player_edit_signature_cancel);
		btn_yes.setOnClickListener(btn_yes_listener);
		btn_cancel.setOnClickListener(btn_cancel_listener);
		
		music_name = (TextView) EditSignatureDialog.findViewById(R.id.screen_record_player_music_name);
		self_signature_name = (EditText) EditSignatureDialog.findViewById(R.id.screen_record_player_self_signature);
		music_name.setText(string_song_name);
		self_signature_name.setText(string_edit_signature_name);
		self_signature_name.setSelection(string_edit_signature_name.length());
	}

	public void show() {
		EditSignatureDialog.show();
	}

	public void dismiss() {
		EditSignatureDialog.dismiss();
	}

	private View.OnClickListener btn_yes_listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			EditSignatureDialog.dismiss();
			String headStr = string_full_path.substring(0, string_full_path.indexOf(string_song_name)+string_song_name.length()+1);
			String tailStr = "";
			if(string_full_path.contains("_mp3") && string_full_path.lastIndexOf("_mp3")<string_full_path.length() ){
				tailStr = string_full_path.substring(string_full_path.lastIndexOf("_mp3"), string_full_path.length());
			}else if(string_full_path.contains("_wav") && string_full_path.lastIndexOf("_wav")<string_full_path.length()){
				tailStr = string_full_path.substring(string_full_path.lastIndexOf("_wav"), string_full_path.length());
			}
			
			
			// TODO Auto-generated method stub
        String stringSignature = self_signature_name.getText().toString();
        if(stringSignature.equals("")){
        	Toast.makeText(context, context.getString(R.string.screen_record_player_edit_signature_null_string_not_permitted), Toast.LENGTH_SHORT).show();
        	return;
        }
        if(stringSignature.contains("mp3")){
        	Toast.makeText(context, context.getString(R.string.screen_record_player_edit_signature_mp3_contained_string_not_permitted), Toast.LENGTH_SHORT).show();
        	return;
        }
        if(stringSignature.contains("wav")){
        	Toast.makeText(context, context.getString(R.string.screen_record_player_edit_signature_wav_contained_string_not_permitted), Toast.LENGTH_SHORT).show();
        	return;
        }
        String newRecordFileName = headStr + stringSignature + tailStr;
        File file = new File(newRecordFileName);
        (new File(string_full_path)).renameTo(file);
        IMediaEventArgs eventArgs = new MediaEventArgs();
		 ServiceManager.getMediaEventService().onMediaUpdateEvent(eventArgs.setMediaUpdateEventTypes(MediaEventTypes.RECORD_UPDATE_DATA));
		 
	 }
	};

	
	private View.OnClickListener btn_cancel_listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			dismiss();
		}
	};
	

}