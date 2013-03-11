package com.amusic.media.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.Button;

import com.amusic.media.R;
import com.amusic.media.screens.impl.ScreenAudio;
import com.amusic.media.screens.impl.ScreenHome;
import com.amusic.media.screens.impl.ScreenKMedia;
import com.amusic.media.screens.impl.ScreenRecord;
import com.amusic.media.screens.impl.ScreenSearch;
import com.amusic.media.services.IScreenService.Mark;
import com.amusic.media.services.impl.ServiceManager;


public class DialogHideOrExit {

	private Button btn_ok;
	private Button btn_cancel;
	private Button btn_hide;
	private Dialog exitOrHideDialog;

	public DialogHideOrExit() {
		exitOrHideDialog = new Dialog(ServiceManager.getAmtMedia(), R.style.CustomDialog);
		exitOrHideDialog.setContentView(R.layout.dialog_hide_or_exit);
		exitOrHideDialog.setCanceledOnTouchOutside(true);
		init();
	}

	private void init() {
		btn_ok = (Button) exitOrHideDialog.findViewById(R.id.btn_ok);
		btn_hide = (Button) exitOrHideDialog.findViewById(R.id.btn_hide);
		btn_cancel = (Button) exitOrHideDialog.findViewById(R.id.btn_cancel);
	
		btn_ok.setOnClickListener(btn_ok_listener);
		btn_hide.setOnClickListener(btn_hide_listener);
		btn_cancel.setOnClickListener(btn_cancel_listener);
		
		exitOrHideDialog.setOnDismissListener(new Dialog.OnDismissListener(){
			@Override
			public void onDismiss(DialogInterface dialog) {
				if (ScreenHome.tabId.equals(ScreenHome.tabAudio)) {
					ServiceManager.getAudioScreenService().getBackList().push(ScreenAudio.class.getCanonicalName());
					ServiceManager.getAudioScreenService().getMarks().push(new Mark(null, null));
				} else if (ScreenHome.tabId.equals(ScreenHome.tabKMedia)) {
					ServiceManager.getKMediaScreenService().getBackList().push(ScreenKMedia.class.getCanonicalName());
					ServiceManager.getKMediaScreenService().getMarks().push(new Mark(null, null));
				} else if (ScreenHome.tabId.equals(ScreenHome.tabRecord)) {
					ServiceManager.getRecordScreenService().getBackList().push(ScreenRecord.class.getCanonicalName());
					ServiceManager.getRecordScreenService().getMarks().push(new Mark(null, null));
				} else if (ScreenHome.tabId.equals(ScreenHome.tabSearch)) {
					ServiceManager.getSearchScreenService().getBackList().push(ScreenSearch.class.getCanonicalName());
					ServiceManager.getSearchScreenService().getMarks().push(new Mark(null, null));
				}
				ServiceManager.getAmtScreenService().getBackList().push(ScreenHome.class.getCanonicalName());
				ServiceManager.getAmtScreenService().getMarks().push(new Mark(null, null));
			}});
	}

	public void show() {
		exitOrHideDialog.show();
	}


	private View.OnClickListener btn_ok_listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			exitOrHideDialog.dismiss();
			ServiceManager.exit();
			
		}
	};

	
	private View.OnClickListener btn_hide_listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			
			exitOrHideDialog.dismiss();
			Context c = ServiceManager.getAmtMedia();
		
/*			ActivityManager am = (ActivityManager)c.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(2);
            
            if(list.size()==2){   
            	Intent i= new Intent();
            	ComponentName component = new ComponentName(list.get(1).topActivity.getPackageName(),list.get(1).topActivity.getClassName());
                i.setComponent(component);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
                try{
                    c.startActivity(i);  
                    }catch(Exception e){
                    	Intent ib= new Intent();
                        ib.setAction(Intent.ACTION_MAIN);
                        ib.addCategory(Intent.CATEGORY_HOME);
                        ib.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
                        c.startActivity(ib);  
            	}
            }else{*/
            	Intent i= new Intent();
                i.setAction(Intent.ACTION_MAIN);
                i.addCategory(Intent.CATEGORY_HOME);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
                c.startActivity(i);  
           /* } */  
               
           
		}
	};


	private View.OnClickListener btn_cancel_listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			exitOrHideDialog.dismiss();
			
		}
	};
	
	
}
