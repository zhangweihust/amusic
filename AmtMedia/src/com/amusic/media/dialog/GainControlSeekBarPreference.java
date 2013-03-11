package com.amusic.media.dialog;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.amusic.media.R;
import com.amusic.media.utils.Constant;

public class GainControlSeekBarPreference extends DialogPreference implements
OnSeekBarChangeListener {
	private SeekBar seekBar;
	private int progress;
	private SharedPreferences sp;
	public GainControlSeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		this.progress = progress;
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void onBindDialogView(View view) {
		// TODO Auto-generated method stub
		super.onBindDialogView(view);
		seekBar = (SeekBar) view.findViewById(R.id.seekBar1);
		seekBar.setProgress(Constant.GAIN_CONTROL_PROGRESS);
		seekBar.setOnSeekBarChangeListener(this); 
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		// TODO Auto-generated method stub
		if(positiveResult){
			sp = getSharedPreferences();
			Editor editor = sp.edit();
			editor.putInt(Constant.SoftParametersSetting.sound_gain_control_key, progress);
			editor.commit();
			Constant.GAIN_CONTROL_PROGRESS = progress;
		}else{
			
		}
	}
	
}