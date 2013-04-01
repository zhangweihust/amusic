package com.android.media.dialog;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.amusic.media.R;
import com.android.media.utils.Constant;
import com.android.media.utils.PreferencesUtil;

public class SeekBarPreference extends DialogPreference implements
OnSeekBarChangeListener {
	private SeekBar seekBar;
	private int progress;
	private SharedPreferences sp;
	private boolean isOver = false;
	private int realProgress;
	public SeekBarPreference(Context context, AttributeSet attrs) {
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
		if(isOver){
			seekBar.setProgress(realProgress);
			isOver = false;
		}else{
			seekBar.setProgress(PreferencesUtil.getsensitivityLashingProgressSP());
		}
		seekBar.setOnSeekBarChangeListener(this); 
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		// TODO Auto-generated method stub
		if(positiveResult){
			sp = getSharedPreferences();
			Editor editor = sp.edit();
			editor.putInt(Constant.SoftParametersSetting.sensitivity_lashing_progress_key, progress);
			editor.commit();
			realProgress = progress;
			Constant.SPECIAL_LASHING_PROGRESS = progress;
			if(Constant.SPECIAL_LASHING_PROGRESS >= 90){
				Constant.SPECIAL_LASHING_PROGRESS = 90;
				isOver = true;
			}
		}else{
			
		}
	}
	
}