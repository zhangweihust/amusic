package com.amusic.media.ffmpeg;

import java.io.File;

import android.media.MediaPlayer;
import android.os.Environment;
import android.os.StatFs;
import android.widget.Toast;

import com.amusic.media.MediaApplication;
import com.amusic.media.R;
import com.amusic.media.screens.impl.ScreenKMediaPlayer;
import com.amusic.media.services.impl.MediaPlayerService;

public class CustomMediaPlayer extends MediaPlayer {

	// add by chengyi1.zhao 20120227
	// begin
	//modified by jing1.zhang
	public void setRecordFilePath(String RecordFilePath){
		recordMp3FilePath = RecordFilePath;
	}
	
	public void setAudioFilePath(String audioFilePath) {
		this.audioFilePath = audioFilePath;
	}
	
	public String getAudioFilePath() {
		return audioFilePath;
	}
    private String recordMp3FilePath = null;
    private String audioFilePath = null;
	
   
	private ExtAudioRecorder extAudioRecorder = null; // Uncompressed recording
														// (WAV);

	private long diskSizeUnderFlow = ExtAudioRecorder.DEFAULT_DISKSIZE_UNDER_FLOW;
	//private int diskCheckIntervalSize = ExtAudioRecorder.DEFAULT_CHECK_INTERVAL_SIZE;
	//private OnDiskSizeUnderflowListener mOnDiskSizeUnderflowListener = null;
	
	
	
	public void setKTVMicrophoneVolumeLocal(float l, float r) {
		if (extAudioRecorder != null) {
			extAudioRecorder.setTrackStereoVolume(l, r);
		}
	}

	public void startRecordPlay() {
		
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){//判断是否插入sd卡
			File filePath = Environment.getExternalStorageDirectory();//获得sdcard的路径
			StatFs stat = new StatFs(filePath.getPath());//创建StatFs对象，这个对象很重要SD卡的信息就靠它获取了
			long blockSize= stat.getBlockSize();//获得block的大小
			int availableBlocks = stat.getAvailableBlocks();//获得可用block数
			//float totalBlocks = stat.getBlockCount();//获得总block数
			long availableSizeInBytes = blockSize*availableBlocks;
			if(availableSizeInBytes <= ExtAudioRecorder.DEFAULT_DISKSIZE_UNDER_FLOW){
				Toast.makeText(MediaApplication.getContext(), MediaApplication.getContext().getString(R.string.sd_card_not_enough_space),
						Toast.LENGTH_SHORT).show();
			}
				
		}/*由于刚开始整个应用启动的时候已经判断了sd卡不存在的情况，所以在这里没有必要加sd卡不存在的判断了；
		else{
			Toast.makeText(MediaApplication.getContext(), MediaApplication.getContext().getString(R.string.sd_card_not_insert_please_insert_for_recording),
					Toast.LENGTH_SHORT).show();
			return;
		}*/
		   //Log.e("zhangjingtest","RecordPlayThread startRecordPlay in!");
			extAudioRecorder = ExtAudioRecorder.getInstanse();
			String tmpfileName = MediaPlayerService.directoryRecord+MediaPlayerService.tmpFileName;
			String mp3fileName = recordMp3FilePath;
			extAudioRecorder.setOutputFile(tmpfileName,mp3fileName);
			//extAudioRecorder.setDiskSizeUnderFlow(diskSizeUnderFlow);
			//extAudioRecorder.setDiskCheckIntervalSize(diskCheckIntervalSize);
			//extAudioRecorder.setOnDiskSizeUnderflowListener(mOnDiskSizeUnderflowListener);
			if(ScreenKMediaPlayer.saveRecordCheckBoxFlag){
				extAudioRecorder.prepare();
			}
			
			extAudioRecorder.start();
	}

	// end

	public boolean isStartRecording() {
		if (extAudioRecorder != null) {
			if (extAudioRecorder.getPayloadSize() > 0) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}


	/*
	public void setOnDiskSizeUnderflowListener(
			OnDiskSizeUnderflowListener listener) {
		mOnDiskSizeUnderflowListener = listener;
	}

	public void setDiskSizeUnderFlow(int size) {
		diskSizeUnderFlow = size;
	}
	
	public void setDiskCheckIntervalSize(int size) {
		diskCheckIntervalSize = size;
	}*/

	
}