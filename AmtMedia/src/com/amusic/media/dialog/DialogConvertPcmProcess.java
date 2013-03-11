package com.amusic.media.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amusic.media.R;
import com.amusic.media.ffmpeg.ExtAudioRecorder;
import com.amusic.media.screens.impl.ScreenKMediaPlayer;
import com.amusic.media.view.CustomNoSeekSeekBar;

public class DialogConvertPcmProcess{

   private float input_wavFileSize = 0;
   private float output_mp3FileSize = 0;
	private Button btn_save_as_wav;
	private Button btn_cancel;
	private Dialog convertPcmProcessDialog;
   private TextView textOriginFileSize;
   private TextView textMp3FileSize;
   private CustomNoSeekSeekBar progressBar;
   private TextView progressPercentTextView;
   private Context dialogContext;
   private convertProcessHandler handler;
   
	public convertProcessHandler getHandler() {
	   return handler;
    }
    
	public Dialog getConvertPcmProcessDialog(){
		return convertPcmProcessDialog;
	}

	public DialogConvertPcmProcess(Context context) {
		dialogContext = context;
		convertPcmProcessDialog = new Dialog(context,R.style.CustomDialog);
		convertPcmProcessDialog.setContentView(R.layout.dialog_convertpcm_process);
		init();
		this.handler = new convertProcessHandler();
	}

	private void init() {
		btn_save_as_wav = (Button)convertPcmProcessDialog.findViewById(R.id.dialog_convert_pcm_process_save_as_wav);
		btn_cancel = (Button)convertPcmProcessDialog.findViewById(R.id.dialog_convert_pcm_process_stop_convert);
		textOriginFileSize = (TextView)convertPcmProcessDialog.findViewById(R.id.dialog_convert_pcm_process_text_origin_file);
		progressPercentTextView = (TextView)convertPcmProcessDialog.findViewById(R.id.dialog_convert_pcm_process_percent);
		textMp3FileSize = (TextView)convertPcmProcessDialog.findViewById(R.id.dialog_convert_pcm_process_text_mp3_file);
		progressBar = (CustomNoSeekSeekBar)convertPcmProcessDialog.findViewById(R.id.dialog_convert_pcm_process_seek_bar);
		btn_save_as_wav.setOnClickListener(btn_save_as_wav_listener);
		btn_cancel.setOnClickListener(btn_cancel_listener);
		convertPcmProcessDialog.setOnDismissListener(dialog_dismiss_listener);
		convertPcmProcessDialog.setCanceledOnTouchOutside(false);
	}

	public void show() {
		if(!convertPcmProcessDialog.isShowing() ){
			convertPcmProcessDialog.show();
		}
		
	}

	public void dismiss() {
		convertPcmProcessDialog.dismiss();
	}

	private Dialog.OnDismissListener dialog_dismiss_listener = new Dialog.OnDismissListener(){

		@Override
		public void onDismiss(DialogInterface dialog) {
			// TODO Auto-generated method stub
			if(ExtAudioRecorder.getInstanse().getIsConverting()){
				ScreenKMediaPlayer.getInstance().getDialogStopConvertConfirm().show();
			}
			
		}
		
	};
	private View.OnClickListener btn_save_as_wav_listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			ExtAudioRecorder.getInstanse().getAudioConvert().setControlInfo((int)1);
		}
	};


	
	private View.OnClickListener btn_cancel_listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			ScreenKMediaPlayer.getInstance().getDialogStopConvertConfirm().show();
			convertPcmProcessDialog.dismiss();
		}
	};
    
	
	//在对UI进行更新时，执行时所在的线程为主UI线程    
    class convertProcessHandler extends Handler{//继承Handler类时，必须重写handleMessage方法     
        public convertProcessHandler(){}  
        public convertProcessHandler(Looper lo){  
            super(lo);  
        }  
        @Override  
        public void handleMessage(Message msg) {  
            super.handleMessage(msg); 
            
            Bundle b = msg.getData();//Obtains a Bundle of arbitrary data associated with this event  
              
               String origin_data_size = b.getString("origin_data_size");
               String output_data_size = b.getString("output_data_size");
               int percent_done = b.getInt("percent_done");
               textOriginFileSize.setText(origin_data_size);
               textMp3FileSize.setText(output_data_size);
               progressBar.setProgress(percent_done);
               progressPercentTextView.setText(dialogContext.getString(R.string.dialog_convert_pcm_process_text4)+percent_done+"%");
            //String textStr0 = textView.getText().toString();    
            //String textStr1 = b.getString("textStr");  
            //HandlerActivity.this.textView.setText(textStr0+" "+textStr1);//更改TextView中的值    
              
            //int barValue = b.getInt("barValue");  
            //HandlerActivity.this.progressBar.setProgress(barValue);//更改进度条当中的值    
        }     
    }   
}
