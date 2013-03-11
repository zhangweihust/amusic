package com.amusic.media.screens.impl;


import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import com.amusic.media.R;
import com.amusic.media.adapter.EqualizerModeAdapter;
import com.amusic.media.event.IMediaEventArgs;
import com.amusic.media.event.impl.MediaEventArgs;
import com.amusic.media.event.impl.MediaEventTypes;
import com.amusic.media.screens.AmtScreen;
import com.amusic.media.services.impl.MediaPlayerService;
import com.amusic.media.services.impl.ServiceManager;
import com.amusic.media.view.VerticalSeekBar1;
import com.amusic.media.view.VerticalSeekBar1.OnVertivalSeekBarChangeListener;
import com.amusic.media.view.WaveFormView;


public class ScreenEqualizer extends AmtScreen implements OnVertivalSeekBarChangeListener{
	 private static final int EQUALIZER_MODE_CUSTOM=10;
	 private static final int EQUALIZER_MODE_NORMAL=0;
	 private static final int EQUALIZER_MODE_CLASSIC=1;
	 private static final int EQUALIZER_MODE_DANCE=2;
	 private static final int EQUALIZER_MODE_FLAT=3;
	 private static final int EQUALIZER_MODE_FOLK=4;
	 private static final int EQUALIZER_MODE_HEAVY_METAL=5;
	 private static final int EQUALIZER_MODE_HIP_HOP=6;
	 private static final int EQUALIZER_MODE_JAZZ=7;
	 private static final int EQUALIZER_MODE_POP=8;
	 private static final int EQUALIZER_MODE_ROCK=9;

	 private static final int REVERB_MODE_NONE=0;
	 private static final int REVERB_MODE_SMALLROOM=1;
	 private static final int REVERB_MODE_MEDIUMROOM=2;
	 private static final int REVERB_MODE_LARGEROOM=3;
	 private static final int REVERB_MODE_MEDIUMHall=4;
	 private static final int REVERB_MODE_LARGEHALL=5;
	 private static final int REVERB_MODE_PLATE=6;
	 private TextView currentmodeTv;
	 private Button optionBt;
	 private TextView currentReverbTv;
	 private Button reverb_optionBt;
	 private PopupWindow popupWindow; 
	 private PopupWindow reverbpopupWindow; 
	 private View view; 
	 private ListView mode;
	 private List<String> modes=new ArrayList<String>();
	 private List<String> Mode_reverb =new ArrayList<String>();
	 private int currentmode;
	 private int currentreverb;
	 private VerticalSeekBar1 seekbar1;
	 private VerticalSeekBar1 seekbar2;
	 private VerticalSeekBar1 seekbar3;
	 private VerticalSeekBar1 seekbar4;
	 private VerticalSeekBar1 seekbar5;
	 private WaveFormView waveform;
	 private LinearLayout mlayout;
	 private double[] y=new double[5];
	 private int screenWidth,screenHeight; 
	 double customlevel[]={0,0,0,0,0};
	 double defaultlevel[][]={{300,0,0,0,300},{500,300,-200,400,400},{600,0,200,400,100},
			 {0,0,0,0,0},{300,0,0,200,-100},{400,100,900,300,0},{500,300,0,100,300},
			 {400,200,-200,200,500},{-100,200,500,100,-200},{500,300,-100,300,500}};
	 double currentlevel[]={0,0,0,0,0};
	 short minEqualizer=-1500;
	 public int seekbar1_id,seekbar2_id,seekbar3_id,seekbar4_id,seekbar5_id;
	 private int flag=0;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_equalizer_settings);
		
		String[] list=this.getResources().getStringArray(R.array.equalizer_mode);
     	for(int i=0;i<list.length;i++){
 			modes.add(list[i]);	
 		}
        list=this.getResources().getStringArray(R.array.reverb_mode);
        for(int i=0;i<list.length;i++){
        	Mode_reverb.add(list[i]);
        }
		currentmodeTv=(TextView) findViewById(R.id.screen_euqlizer_setting_currentmode);
		optionBt=(Button) findViewById(R.id.screen_equalizer_settings_options);
		optionBt.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				optionBt.setBackgroundResource(R.drawable.screen_audio_song_options_open_large);
				 showWindow(v); 				 
				
			}
		});
		currentReverbTv=(TextView) findViewById(R.id.screen_euqlizer_setting_current_reverb);
		reverb_optionBt=(Button) findViewById(R.id.screen_equalizer_settings_reverb_options);
		reverb_optionBt.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				reverb_optionBt.setBackgroundResource(R.drawable.screen_audio_song_options_open_large);
				showReverbWindow(v);
			}
		});
	  		
		seekbar1=(VerticalSeekBar1) findViewById(R.id.screen_equalizer_settings_seekbar1);
		seekbar1.setOnVertivalSeekBarChangeListener(this);
		seekbar2=(VerticalSeekBar1) findViewById(R.id.screen_equalizer_settings_seekbar2);
		seekbar2.setOnVertivalSeekBarChangeListener(this);
		seekbar3=(VerticalSeekBar1) findViewById(R.id.screen_equalizer_settings_seekbar3);
		seekbar3.setOnVertivalSeekBarChangeListener(this);
		seekbar4=(VerticalSeekBar1) findViewById(R.id.screen_equalizer_settings_seekbar4);
		seekbar4.setOnVertivalSeekBarChangeListener(this);
		seekbar5=(VerticalSeekBar1) findViewById(R.id.screen_equalizer_settings_seekbar5);		
		seekbar5.setOnVertivalSeekBarChangeListener(this);
		
		seekbar1_id=seekbar1.getId();
	    seekbar2_id=seekbar2.getId();
	    seekbar3_id=seekbar3.getId();
		seekbar4_id=seekbar4.getId();
		seekbar5_id=seekbar5.getId();
		mlayout=(LinearLayout) findViewById(R.id.screen_equalizer_settings_waveform);
		Display display = getWindowManager().getDefaultDisplay();
		screenWidth = display.getWidth();
		screenHeight = display.getHeight();
		waveform=new WaveFormView(this,mlayout,screenWidth);				
	    mlayout.removeAllViews();
	    mlayout.addView(waveform);	
	    refresh();
		
	}

		
	private void showWindow(View parent) { 
		 
        if (popupWindow == null) { 
        	
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
            view = layoutInflater.inflate(R.layout.screen_equalizer_settings_options, null);     
            mode = (ListView) view.findViewById(R.id.screen_equalizer_settings_options_list);    		
            EqualizerModeAdapter adapter = new EqualizerModeAdapter(this, modes); 
            mode.setAdapter(adapter);               		
            // 创建一个PopuWidow对象 
    		int popwindowheight=screenHeight*7/10;
    		int popwindowidth=screenWidth*7/10;
//            Log.i("equalizer", "-----------------popwindowheight:"+popwindowheight);
    		popupWindow = new PopupWindow(view, popwindowidth, popwindowheight); 

        } 
        
        popupWindow.setFocusable(true);       
        popupWindow.setBackgroundDrawable(new BitmapDrawable()); 
        int  xoff = popupWindow.getWidth(); 
        popupWindow.showAsDropDown(parent, -xoff, 0); 
//        popupWindow.showAtLocation(parent,Gravity.CENTER, 0, 0);
//        Log.i("ScreenEqualizer", "------------------------------------------show");
      
        mode.setOnItemClickListener(new OnItemClickListener() { 
 
            @Override 
            public void onItemClick(AdapterView<?> adapterView, View view, 
                    int position, long id) { 
            	
//            	Toast.makeText(ScreenEqualizer.this, ""+position, 1000) .show();             	
            	if(position==10){
              		     currentmode=EQUALIZER_MODE_CUSTOM;  
              		     flag=0;
//              		   for(int i=0;i<customlevel.length;i++)
//              			 Log.i("equalizer", "-----------select---------customlevel["+i+"]="+customlevel[i]);
              		     setSeekBarProgress(customlevel);
              		     currentmodeTv.setText(modes.get(position));
              	}else{
              		switch(position){           	 
             	   case 0 :   currentmode=EQUALIZER_MODE_NORMAL;break;
             	   case 1 :   currentmode=EQUALIZER_MODE_CLASSIC;break;
             	   case 2 :   currentmode=EQUALIZER_MODE_DANCE;break;
             	   case 3 :   currentmode=EQUALIZER_MODE_FLAT;break;            	                    
             	   case 4 :   currentmode=EQUALIZER_MODE_FOLK;break;          	                 
             	   case 5 :   currentmode=EQUALIZER_MODE_HEAVY_METAL;break;
             	   case 6 :   currentmode=EQUALIZER_MODE_HIP_HOP;break;
             	   case 7 :   currentmode=EQUALIZER_MODE_JAZZ;break;
             	   case 8 :   currentmode=EQUALIZER_MODE_POP;break;
             	   case 9 :   currentmode=EQUALIZER_MODE_ROCK;break;
        	   
             	  }
              	  flag=1;
             	  setSeekBarProgress(defaultlevel[currentmode]);
             	  currentmodeTv.setText(modes.get(position));
                  setEqualizerMode(currentmode);
              		
              		
              	}
              
                if (popupWindow != null) { 
                    popupWindow.dismiss(); 
                } 
            } 
        });     
        
        popupWindow .setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				optionBt.setBackgroundResource(R.drawable.screen_audio_song_options_large);
			}
		});
   }
	
	private void showReverbWindow(View parent){
		
        if (reverbpopupWindow == null) { 
        	
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
            view = layoutInflater.inflate(R.layout.screen_equalizer_settings_options, null);     
            mode = (ListView) view.findViewById(R.id.screen_equalizer_settings_options_list); 
            TextView titleTv=(TextView) view.findViewById(R.id.screen_equalizer_settings_options_title);
            titleTv.setText(getResources().getString(R.string.reverb_setting));
            EqualizerModeAdapter adapter = new EqualizerModeAdapter(this, Mode_reverb); 
            mode.setAdapter(adapter);               		
            // 创建一个PopuWidow对象 
    		int popwindowheight=screenHeight*7/10;
    		int popwindowidth=screenWidth*7/10;
//            Log.i("equalizer", "-----------------popwindowheight:"+popwindowheight);
    		reverbpopupWindow = new PopupWindow(view, popwindowidth, popwindowheight); 
        } 
        reverbpopupWindow.setFocusable(true);       
        reverbpopupWindow.setBackgroundDrawable(new BitmapDrawable()); 
        int  xoff = reverbpopupWindow.getWidth(); 
//        popupWindow.showAsDropDown(parent, -xoff, 0); 
        reverbpopupWindow.showAtLocation(parent,Gravity.CENTER, 0, 0);
//        Log.i("ScreenEqualizer", "------------------------------------------show");
      
        mode.setOnItemClickListener(new OnItemClickListener() { 
 
            @Override 
            public void onItemClick(AdapterView<?> adapterView, View view, 
                    int position, long id) { 
            
              		switch(position){           	 
             	   case 0 :   currentreverb=REVERB_MODE_NONE;break;
             	   case 1 :   currentreverb=REVERB_MODE_SMALLROOM;break;
             	   case 2 :   currentreverb=REVERB_MODE_MEDIUMROOM;break;
             	   case 3 :   currentreverb=REVERB_MODE_LARGEROOM;break;            	                    
             	   case 4 :   currentreverb=REVERB_MODE_MEDIUMHall;break;        	                 
             	   case 5 :   currentreverb=REVERB_MODE_LARGEHALL;break;
             	   case 6 :   currentreverb=REVERB_MODE_PLATE;break;
        	   
             	  }
             	  currentReverbTv.setText(Mode_reverb.get(currentreverb));
                  setReverbMode( currentreverb);
                          
                if (reverbpopupWindow != null) { 
                	reverbpopupWindow.dismiss(); 
                } 
            } 
        });     
        
        reverbpopupWindow .setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				reverb_optionBt.setBackgroundResource(R.drawable.screen_audio_song_options_large);
			}
		});
        
        
        
		
		
	}
	private void setSeekBarProgress(double[] values){
		
		seekbar1.setProgress((int)(values[0]-minEqualizer));
		seekbar2.setProgress((int)(values[1]-minEqualizer));
		seekbar3.setProgress((int)(values[2]-minEqualizer));
		seekbar4.setProgress((int)(values[3]-minEqualizer));
		seekbar5.setProgress((int)(values[4]-minEqualizer));
		
		
		
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		ServiceManager.getAmtMedia().getGoPlayerBtn().setVisibility(View.INVISIBLE);
		setScreenTitle(getString(R.string.screen_equalizer_settings_title));
	}
	
	private void setEqualizersettings(int mode,double[] level){
	
	    Editor sharedata = ServiceManager.getAmtMedia().getSharedPreferences("equalizersettings", Context.MODE_WORLD_WRITEABLE).edit();	  
	    sharedata.putInt("flagEqualizerMode",mode);
	    sharedata.putInt("flagEqualizerlevel0", (int)level[0]);
		sharedata.putInt("flagEqualizerlevel1", (int)level[1]);
		sharedata.putInt("flagEqualizerlevel2", (int)level[2]);
		sharedata.putInt("flagEqualizerlevel3", (int)level[3]);
		sharedata.putInt("flagEqualizerlevel4", (int)level[4]);	
		sharedata.commit();
		
		IMediaEventArgs eventArgs = new MediaEventArgs();
    	MediaPlayerService.flagEqualizerLevel = MediaEventTypes.EQULIZER_LEVEL;
    	mediaEventService.onMediaUpdateEvent(eventArgs.setMediaUpdateEventTypes(MediaEventTypes.EQULIZER_LEVEL) );
		
	}
   
	private void setEqualizerMode(int mode){
		
		Editor sharedata = ServiceManager.getAmtMedia().getSharedPreferences("equalizersettings", Context.MODE_WORLD_WRITEABLE).edit();
		sharedata.putInt("flagEqualizerMode",mode);
		sharedata.commit();
		
		IMediaEventArgs eventArgs = new MediaEventArgs();
    	MediaPlayerService.flagEqualizerLevel = MediaEventTypes.EQULIZER_LEVEL;
    	mediaEventService.onMediaUpdateEvent(eventArgs.setMediaUpdateEventTypes(MediaEventTypes.EQULIZER_LEVEL) );
		
	}
	
	private void setReverbMode(int reverbMode){
		
		Editor sharedata = ServiceManager.getAmtMedia().getSharedPreferences("equalizersettings", Context.MODE_WORLD_WRITEABLE).edit();
		sharedata.putInt("flagReverbMode",reverbMode);
		sharedata.commit();
		
		IMediaEventArgs eventArgs = new MediaEventArgs();
//    	MediaPlayerService.flagEqualizerLevel = MediaEventTypes.REVERB_MODE;
    	mediaEventService.onMediaUpdateEvent(eventArgs.setMediaUpdateEventTypes(MediaEventTypes.REVERB_MODE) );
	}
     @Override
	public boolean refresh() {
		// TODO Auto-generated method stub
    	 
	        SharedPreferences sharedata = ServiceManager.getAmtMedia().getSharedPreferences("equalizersettings", Context.MODE_WORLD_WRITEABLE);			
	        currentmode=sharedata.getInt("flagEqualizerMode", 0);
			currentmodeTv.setText(modes.get(currentmode));
//			Log.d("ScreenEqualizer", "-----------read---------------currentmode:"+modes.get(currentmode));
			customlevel[0]=sharedata.getInt("flagEqualizerlevel0", 0);
			customlevel[1]=sharedata.getInt("flagEqualizerlevel1", 0);
			customlevel[2]=sharedata.getInt("flagEqualizerlevel2", 0);
			customlevel[3]=sharedata.getInt("flagEqualizerlevel3", 0);
			customlevel[4]=sharedata.getInt("flagEqualizerlevel4", 0);	
//			for(int i=0;i<customlevel.length;i++)
//			Log.d("ScreenEqualizer", "---------read-----------customlevel["+i+"]="+customlevel[i]);
			
			if(currentmode==EQUALIZER_MODE_CUSTOM){	
				for(int i=0;i<customlevel.length;i++){
					currentlevel[i]=customlevel[i];																
				}
//				Log.i("equalizer", "--------------------------set-------custom");	
//				for(int i=0;i<currentlevel.length;i++)
//					Log.i("equalizer", "---------read-----------currentlevel["+i+"]="+currentlevel[i]);
				setSeekBarProgress(currentlevel);
			}else{
				for(int i=0;i<currentlevel.length;i++){
					currentlevel[i]=defaultlevel[currentmode][i];
														
				}
//				Log.i("equalizer", "--------------------------set-------default");	
				setSeekBarProgress(currentlevel);
			}	
			currentreverb=sharedata.getInt("flagReverbMode", 0);
			currentReverbTv.setText(Mode_reverb.get(currentreverb));
			
		return true;
	}

     @Override
   protected void onNewIntent(Intent intent) {
	// TODO Auto-generated method stub
	   super.onNewIntent(intent);
    	refresh();
   }

	@Override
	public void onProgressChanged(VerticalSeekBar1 VerticalSeekBar,
			int progress, boolean fromUser) {
		// TODO Auto-generated method stub	
	        int id=0;
		    if(VerticalSeekBar.getId()==seekbar1_id){
                id=0;
			}else if(VerticalSeekBar.getId()==seekbar2_id){
		        id=1;
			}else if(VerticalSeekBar.getId()==seekbar3_id){
			    id=2;
			}else if(VerticalSeekBar.getId()==seekbar4_id){
			    id=3;
			}else if(VerticalSeekBar.getId()==seekbar5_id){
			    id=4;
			}		   
		    currentlevel[id]=progress+minEqualizer;	    
		    if(currentmode==EQUALIZER_MODE_CUSTOM ){
		    	if(flag==1){
		    		for(int i=0;i<currentlevel.length;i++){
			    		customlevel[i]=currentlevel[i];
//			    		Log.i("equalizer", "---------------save---------customlevel["+i+"]="+customlevel[i]);			    	    
		    		}				    		
		    	}else{			    		
		    		customlevel[id]=currentlevel[id];	
		    	}		    	
		    	setEqualizersettings(EQUALIZER_MODE_CUSTOM,customlevel);
		    }		    		   
			 for(int i=0;i<currentlevel.length;i++){
					y[i]=((currentlevel[i]-minEqualizer)/100-15);
//					Log.i("equalizer", "-----------------y["+i+"]="+y[i]);	
				}
				    waveform.sety(y);
					waveform.invalidate();			 
//					Log.i("Equalizer","---------------------invalidate");
			
	}


	@Override
	public void onStartTrackingTouch(VerticalSeekBar1 VerticalSeekBar) {
		// TODO Auto-generated method stub
		currentmodeTv.setText(getString(R.string.custom));
		currentmode=EQUALIZER_MODE_CUSTOM;	
		flag=1;
	}


	@Override
	public void onStopTrackingTouch(VerticalSeekBar1 VerticalSeekBar) {
		// TODO Auto-generated method stub		
	} 
	
	@Override
	public boolean hasMenu() {
		// TODO Auto-generated method stub
		return true;
	}
}
