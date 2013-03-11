package com.amusic.media.ffmpeg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder.AudioSource;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.widget.Toast;

import com.amusic.media.MediaApplication;
import com.amusic.media.R;
import com.amusic.media.audioconvert.AudioConvert;
import com.amusic.media.dialog.DialogConvertPcmProcess;
import com.amusic.media.event.IMediaEventArgs;
import com.amusic.media.event.impl.MediaEventArgs;
import com.amusic.media.event.impl.MediaEventTypes;
import com.amusic.media.model.SongInfo;
import com.amusic.media.screens.impl.ScreenKMediaPlayer;
import com.amusic.media.services.IMediaPlayerService;
import com.amusic.media.services.impl.MediaPlayerService;
import com.amusic.media.services.impl.ServiceManager;
import com.amusic.media.utils.Constant;
import com.amusic.media.utils.DiskInfoUtil;
import com.amusic.media.utils.ToastUtil;




public class ExtAudioRecorder {
    private final static int[] sampleRates = { 44100, 22050, 11025, 8000 };
    private static ExtAudioRecorder result = onCreat();
    public static ArrayList<SongInfo> recordSongs = new ArrayList<SongInfo>();
    
    public static boolean recordSupported = false;
    private RecordPlayThread recordPlayThread = null;
   
    private Handler handler;
    private Handler sendHandler = new Handler();
    
	public void setHandler(Handler handler) {
		this.handler = handler;
	}
	
	private DialogConvertPcmProcess dialogConvertPcmProcessContext = null;
	public void setDialogConvertPcmProcessContext(DialogConvertPcmProcess context){
		dialogConvertPcmProcessContext = context;
	}
	
	private Context KMediaSCreenContext;
	public void setKMediaSCreenContext(Context context){
		KMediaSCreenContext = context;
	}
	
	private  ConvertToMp3Thread convertToMp3Thread = null;
    private boolean convertProcessHasFinished = false;
    
    private boolean convertIsInProcess = false;
    public boolean getIsConverting(){
    	return convertIsInProcess;
    }
    public void setTrackStereoVolume(float l, float r) {
        if (audioTrack != null) {
            audioTrack.setStereoVolume(l, r);
        }
    }
    private boolean isRecordingFlag = false;//对外提供的一个接口用于外界得知是否在录音
    public boolean getIsRecording(){
    	return isRecordingFlag;
    }
    
    private boolean restartRecordPlayFlag = false;
    public boolean getRestartRecordPlayFlag(){
    	return restartRecordPlayFlag;
    }

    public static ExtAudioRecorder getInstanse() {
        return result;
    }
    
    private  String recordDuration = "";
    private  String recordDate = "";
    public void setRecordDuration(String duration){
    	recordDuration = duration;
    }
    public void setRecordDate(String date){
    	recordDate = date;
    }
    public String getRecordDuration(){
    	return recordDuration ;
    }
    public String getRecordDate(){
    	return recordDate ;
    }
    
    public void setExtraRecordFileInfo(){
		IMediaPlayerService mediaPlayerService  = ServiceManager.getMediaplayerService();
		MediaPlayer mediaPlayer = mediaPlayerService.getMediaPlayer();
		long playMs = mediaPlayer.getCurrentPosition();
		if(playMs < ScreenKMediaPlayer.currenttimeafterB){
			playMs = ScreenKMediaPlayer.currenttimeafterB;
		}
		
		DecimalFormat df = new DecimalFormat("00");
		String minutes = df.format(playMs/1000/60);
		
		String seconds = "";
		float sec = (float) ((playMs/1000.0) - (playMs/1000));
		if(sec>0.5){
			seconds = df.format((playMs/1000+1)%60);
		}else{
			seconds = df.format((playMs/1000)%60);
		}
		
		String duration =  minutes+":"+ seconds;
		Date now = new Date();
		DateFormat d1 = DateFormat.getDateInstance();
		String date = d1.format(now);
		setRecordDuration(duration);
		setRecordDate(date);
    }
    
    private boolean needSaveRecord = false;
    private int saveFormat = 0;//0-mp3  1-wav
    public void setSaveRecordFlag(boolean flag,int saveFormatFlag){
    	needSaveRecord = flag;
    	saveFormat = saveFormatFlag;
    }
	public static ExtAudioRecorder onCreat() {
		int i = 0;
		ExtAudioRecorder ext = null;
		do {
			ext = new ExtAudioRecorder(true, AudioSource.MIC, sampleRates[i], AudioFormat.CHANNEL_CONFIGURATION_STEREO,
					AudioFormat.ENCODING_PCM_16BIT);

		} while ((++i < sampleRates.length) & !(ext.getState() == ExtAudioRecorder.State.INITIALIZING));
		return ext;
	}

    /**
     * INITIALIZING : recorder is initializing; READY : recorder has been
     * initialized, recorder not yet started RECORDING : recording ERROR :
     * reconstruction needed STOPPED: reset needed
     */
    public enum State {
        INITIALIZING, READY, RECORDING, ERROR, STOPPED
    };

    private AudioConvert mConvert = new AudioConvert();
    public AudioConvert getAudioConvert(){
    	return mConvert;
    }
    
    // Output file path
    private String tmpfilePath = null;
    private String mp3filePath = null;

    public String getTmpFilePath(){
    	return tmpfilePath;
    }
    public String getMp3FilePath(){
    	return mp3filePath;
    }
    // Recorder state; see State
    private State state;

    // File writer (only in uncompressed mode)
    private RandomAccessFile randomAccessWriter = null;

    // Number of channels, sample rate, sample size(size in bits), buffer size,
    // audio source, sample size(see AudioFormat)
    private short nChannels;

    private int sRate;

    private short bSamples;

    // private int bufferSize;

    // Number of bytes written to file after header(only in uncompressed mode)
    // after stop() is called, this size is written to the header/data chunk in
    // the wave file
    private int payloadSize;

    private int recBufSize = 0;

    private int playBufSize = 0;

    private AudioRecord audioRecord = null;

    private AudioTrack audioTrack = null;
    
    public AudioRecord getAudioRecordObj(){
    	return audioRecord;
    }
    public AudioTrack getAudioTrackObj(){
    	return audioTrack;
    }

    Handler mHandler = new Handler();
    
    
    public int getCurrentDone() {
		return mConvert.getCurrentDone();
	}


    /**
     * Returns the state of the recorder in a RehearsalAudioRecord.State typed
     * object. Useful, as no exceptions are thrown.
     *
     * @return recorder state
     */
    public State getState() {
        return state;
    }

    private static short clamp16(int sample) {
        if (((sample >> 15) ^ (sample >> 31)) != 0)
            sample = 0x7FFF ^ (sample >> 31);
        return (short) sample;
    }

    private boolean isPaused = false;//是否暂停的标记
    public void pause(){
        isPaused = true;
    }
    public void resume(){
        isPaused = false;
    }
    public boolean getPausedState(){
        return isPaused;
    }
    
    public void restartRecordPlay(){
    	restartRecordPlayFlag = true;
    	ScreenKMediaPlayer.abRepeatFlag = 0;
    	if(recordPlayThread!=null){
    		recordPlayThread.stopThread();
    	}
    	
    }
    

    public void clearFile(String path){
        try {
            FileOutputStream fos = new FileOutputStream(new File(path));
            fos.write("".getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    class RecordPlayThread extends Thread {
        private boolean isLooping = true;// 是否录放的标记
        private long gCounter = 0;

        public void run() {
            try {
            	if(recBufSize<0){
            		recordSupported = false;
            		ServiceManager.getAmtMedia().runOnUiThread(new Runnable(){
						@Override
						public void run() {
							Toast toast = ToastUtil.getInstance().getToast(ServiceManager.getAmtMedia().getString(R.string.extaudiorecorder_info));
							toast.setDuration(Toast.LENGTH_SHORT);
							toast.setGravity(Gravity.CENTER, 0, 0);
							toast.show();
						}});
                	return;
                }
            	  recordSupported = true;
                short[] pcm = new short[recBufSize >> 1];
                int[] pcm_amplify = new int[recBufSize >> 1];
                // 写入文件buffer
                byte[] buffer = new byte[recBufSize];
                audioRecord.startRecording();// 开始录制
                audioTrack.play();// 开始播放
                //Log.e("zhangjingtest","RecordPlayThread successfully start!");

                ScreenKMediaPlayer.notify_kmedia_has_finished = false;
                ScreenKMediaPlayer.abRepeatFlag = 0;
                isPaused = false;
                while (isLooping) {
                	  isRecordingFlag = true;
                    gCounter++;
                    if (gCounter == 40) {
                        gCounter = 0;
                        audioTrack.flush();
                    }
                   AudioManager mAudioManager = null;
                   mAudioManager = (AudioManager) MediaApplication.getContext().getSystemService(Context.AUDIO_SERVICE);
                   float vol = (float) (/*(float)mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)/15*/Constant.GAIN_CONTROL_PROGRESS/100.0);
                   /*float vol_rev = 1;
                   if(vol>=0.25){
                      vol_rev = (float) (1.0/vol);
                   }*/
                   float vol_rev = 8*vol;
                    // 从MIC保存数据到缓冲区
                    int shortReadResult = audioRecord.read(pcm, 0,
                            recBufSize >> 1);

//                    Log.d(RecordPlayThread.class.getName(),
//                            "shortReadResult is " + shortReadResult);
                    if(MediaApplication.getInstance().getMicFlag()){
                        for (int i = 0; i < shortReadResult; i++) {
                        	//pcm_amplify[i] = (int) ((int)pcm[i]*2*vol_rev);
                        pcm_amplify[i] = (int) ((int)pcm[i]*vol_rev);
                        pcm[i] = clamp16(pcm_amplify[i]);
                        }
                    }

                    
                    if (gCounter != 0 && 
                    		MediaApplication.getInstance().getMicFlag() &&
                    		ScreenKMediaPlayer.getOutputExtraFlag() &&
                    		!getPausedState() &&
                    		Constant.IS_SOUND_SYCN) {
                        // 写入数据即播放
                    	
                        audioTrack.write(pcm, 0, shortReadResult);
                    }

                    for (int n = 0; n < pcm.length; n++) {
                        buffer[2 * n] = (byte) (pcm[n] & 0xFF);
                        buffer[2 * n + 1] = (byte) ((pcm[n] >> 8) & 0xFF);
                    }
                    //Log.e("zhangjingtest","RecordPlayThread may be continue for next loop");
                    //Log.e("zhangjingtest","RecordPlayThread getPausedState() = "+ getPausedState() + "   saveRecordCheckBoxFlag = " + ScreenKMediaPlayer.saveRecordCheckBoxFlag);
                    if(getPausedState()||!ScreenKMediaPlayer.saveRecordCheckBoxFlag){
                        continue;
                    }
                    MediaPlayer mplayer = ServiceManager.getMediaplayerService().getMediaPlayer();
                    int currentMediaTime = mplayer.getCurrentPosition();
                   // Log.e("zhangjingtest","RecordPlayThread notify_kmedia_has_finished="+ScreenKMediaPlayer.notify_kmedia_has_finished
                   // 		+"	abRepeatFlag="+ScreenKMediaPlayer.abRepeatFlag);
                    if(!ScreenKMediaPlayer.notify_kmedia_has_finished){
                    	if(ScreenKMediaPlayer.abRepeatFlag==0 ||
                        		(ScreenKMediaPlayer.abRepeatFlag==2 && currentMediaTime>=ScreenKMediaPlayer.currenttimeafterB)
                        	){
                            try {
                            	 ScreenKMediaPlayer.abRepeatFlag = 0;
                            	 if(randomAccessWriter!=null){
                                    randomAccessWriter.write(buffer); // Write buffer to
                            	 }
                                // file
                                payloadSize += buffer.length;
                                diskCheckSize += buffer.length;

                                if (diskCheckSize > diskCheckIntervalSize) {
                                    diskCheckSize = 0;
                                    if (DiskInfoUtil.getInstance().getSdcardAvailableSize() <= diskSizeUnderFlow) {
                                        if (mOnDiskSizeUnderflowListener != null) {
                                            mOnDiskSizeUnderflowListener
                                                    .OnDiskSizeUnderflow();
                                        }
                                    }
                                }
                            } catch (IOException e) {
                              //  Log
                              //          .e(ExtAudioRecorder.class.getName(),
                               //                 "Error occured in updateListener, recording is aborted");
                                // stop();
                                (new File(tmpfilePath)).delete();
                                (new File(mp3filePath)).delete();
                            }
                        }

                    }
                     

                }

                // 停止录音及回放
                audioTrack.stop();
                audioRecord.stop();
                ScreenKMediaPlayer.notify_kmedia_has_finished = false;
                ScreenKMediaPlayer.abRepeatFlag = 0;
               

                if(ScreenKMediaPlayer.saveRecordCheckBoxFlag && randomAccessWriter!=null){
                	try {
                        randomAccessWriter.seek(4); // Write size to RIFF header
                        randomAccessWriter.writeInt(Integer
                                .reverseBytes(36 + payloadSize));

                        randomAccessWriter.seek(40); // Write size to
                        // Subchunk2Size
                        // field
                        randomAccessWriter.writeInt(Integer
                                .reverseBytes(payloadSize));

                        randomAccessWriter.close();
                        randomAccessWriter = null;
                    } catch (IOException e) {
                        state = ExtAudioRecorder.State.ERROR;
                    }
                }
                

                state = ExtAudioRecorder.State.STOPPED;
                if(!restartRecordPlayFlag){
                    if(needSaveRecord && ScreenKMediaPlayer.saveRecordCheckBoxFlag){
                    	if(saveFormat == 0){
                    		//save as mp3 file 
                    		sendHandler.postDelayed(runable,1000);
                    		convertToMp3Thread = new ConvertToMp3Thread();
                    		convertToMp3Thread.start();
                    	}else if(saveFormat == 1){
                    		//save as wav file
                    		 String filePathMp3 = mp3filePath;
            				 String durationString = ExtAudioRecorder.getInstanse().getRecordDuration();
            				 if(durationString.contains(":")){
            					 filePathMp3 = filePathMp3+"_"+ExtAudioRecorder.getInstanse()
                				 .getRecordDate()+"_"+durationString.substring(0, durationString.indexOf(":"))
                				 +"_"+durationString.substring(durationString.indexOf(":")+1,durationString.length());
            					 filePathMp3 = filePathMp3 + ".mp3";
            				 }
            				 
            				 
            				 if(filePathMp3.contains("_mp3") && filePathMp3.contains(".mp3")){
            					 filePathMp3 = filePathMp3.replace("_mp3", "_wav");
            					 filePathMp3 = filePathMp3.replace(".mp3", ".wav");
            				 }
                			File file = new File(filePathMp3);
                			if(file.exists()){
                				file.delete();
                			}
                			(new File(tmpfilePath)).renameTo(file);
                			IMediaEventArgs eventArgs = new MediaEventArgs();
 	     					ServiceManager.getMediaEventService().onMediaUpdateEvent(eventArgs.setMediaUpdateEventTypes(MediaEventTypes.RECORD_UPDATE_DATA));
                    	}

                    }else{
                    	File file1 = new File(mp3filePath);
                    	File file2 = new File(tmpfilePath);
                    	if(file1.exists()){
                    		file1.delete();
                    	}
                    	if(file2.exists()){
                    		file2.delete();
                    	}	
                    }
                }else{
                	if(ScreenKMediaPlayer.saveRecordCheckBoxFlag){
                		clearFile(tmpfilePath);
                    	ExtAudioRecorder.getInstanse().prepare();
                	}
                	
                	ExtAudioRecorder.getInstanse().start();
                	restartRecordPlayFlag = false;
                }


            } catch (IllegalStateException e) {
                e.printStackTrace();
                //Log.e(ExtAudioRecorder.class.getName(),"The Recording Device is working illegally,please restart it");
            }
        }

        public void stopThread() {
        	isRecordingFlag = isLooping = false;
        }


    };
    
    Runnable runable = new Runnable(){


		@Override
		public void run() {
	    	File file=new File(tmpfilePath);
	    	float length = (float) (file.length()*1.0/1024/1024); //unit Mb
	    	DecimalFormat df1 = new DecimalFormat("0.00");
	    	DecimalFormat df2 = new DecimalFormat("00.00");
	    	String db_length =null;
	    	if(length<10){
	    		db_length = df1.format(length);
	    	}else if(length>=10){
	    		db_length = df2.format(length);
	    	}
    
			if(handler != null){
   			 Message msg = new Message();
		          Bundle bd = new Bundle();
		          bd.putString("origin_data_size", db_length+"M");
		          bd.putString("output_data_size", mConvert.getMp3BufferSize());
		          bd.putInt("percent_done", mConvert.getCurrentDone());
		          msg.setData(bd);
		          handler.sendMessage(msg);
   		     }
			if(!convertProcessHasFinished){
				sendHandler.postDelayed(runable, 1000);
			}else{
				convertIsInProcess = false;
				dialogConvertPcmProcessContext.getConvertPcmProcessDialog().dismiss();
				
				Toast toast = ToastUtil.getInstance().getToast(ServiceManager.getAmtMedia().getString(R.string.convert_finished));
				toast.setDuration(Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				
				ServiceManager.finishScreenKMediaPlayer();
				convertProcessHasFinished = false;
			}
			
		}
    	
    };

    private String[] makeSong(String songName){
		String[] song = new String[2];
		song[0] = "";
		song[1] = "";
		if(songName.contains("-")){
			song[0] = songName.substring(0, songName.indexOf("-"));
		}
		if(songName.indexOf("-")+1<songName.length()){
			song[1] = songName.substring(songName.indexOf("-")+1, songName.length());
		}
		
		return song;
	}
    // end

    private ArrayList<SongInfo> getRecordList(String absolutePath){
    	/*
		String recordPath = MediaPlayerService.directoryRecord;
		File dir = new File(recordPath);
		if(!dir.exists()){
			dir.mkdir();
		}
		File[] files = dir.listFiles();
		ArrayList<SongInfo> recordSongs = null;
		if(files != null && files.length > 0){
			recordSongs = new ArrayList<SongInfo>();
			boolean flag = false;
			for(int i=0;i<files.length;i++){
				
				if(!files[i].getName().contains("_mp3") && !files[i].getName().contains("_wav")){
					continue;
				}
				if(files[i].getName().contains("_mp3")){
					if(!files[i].getName().substring(0, files[i].getName().lastIndexOf("_mp3")).contains("-")){
						continue;
					}
				}
				
				if(files[i].getName().contains("_wav")){
					if(!files[i].getName().substring(0, files[i].getName().lastIndexOf("_wav")).contains("-")){
						continue;
					}
				}
				String songName = null;
				try{
					if(files[i].getName().contains("_mp3")){
						songName = files[i].getName().substring(0, files[i].getName().lastIndexOf("_mp3"));
					}else if(files[i].getName().contains("_wav")){
						songName = files[i].getName().substring(0, files[i].getName().lastIndexOf("_wav"));
					}
				  
				}catch(Exception e){
					e.printStackTrace();
					File tmp = new File(files[i].getAbsolutePath());
					tmp.delete();
				}
				if(!songName.contains("-")){
					continue;
				}
				
				SongInfo songInfo = new SongInfo();
				String tmp = makeSong(songName)[1];
				songInfo.setSongName(tmp);
				
				if(tmp.indexOf("_")==-1){
					continue;
				}

				if(tmp.indexOf("_")==tmp.length()-1){
					continue;
				}
				songInfo.setSingerName(makeSong(songName)[0]);
				songInfo.setDirectory(files[i].getAbsolutePath());
				recordSongs.add(songInfo);
				
			}

		}*/
    	String recordPath = MediaPlayerService.directoryRecord;
    	String songName = "";
    	try{
			if(absolutePath.contains("_mp3")){
				songName = absolutePath.substring(MediaPlayerService.directoryRecord.length(), absolutePath.lastIndexOf("_mp3"));
			}
		  
		}catch(Exception e){
			e.printStackTrace();
			File tmp = new File(absolutePath);
			tmp.delete();
		}
		if(songName.contains("-")){
			SongInfo songInfo = new SongInfo();
			songInfo.setSongName(makeSong(songName)[1]);
			songInfo.setSingerName(makeSong(songName)[0]);
			songInfo.setDirectory(absolutePath);
			recordSongs.add(songInfo);
		}

		return recordSongs;
	}
    class  ConvertToMp3Thread extends Thread{
    	 public void run() {
    		        convertIsInProcess = true;
    				 String filePathTmp = tmpfilePath;
    				 String filePathMp3 = mp3filePath;
    				 String durationString = ExtAudioRecorder.getInstanse().getRecordDuration();
    				 if(durationString.contains(":")){
    					 filePathMp3 = filePathMp3+"_"+ExtAudioRecorder.getInstanse()
        				 .getRecordDate()+"_"+durationString.substring(0, durationString.indexOf(":"))
        				 +"_"+durationString.substring(durationString.indexOf(":")+1,durationString.length());
    					 filePathMp3 = filePathMp3 + ".mp3";
    				 }
    				 
    					 try{
    						 mConvert.setControlInfo((int)0);
    						 //byte[] mp3Bytes = filePathMp3.getBytes();
     	     			 	//int ret = mConvert.convertAudio(filePathTmp,mp3Bytes);
                            
      	     			 	int ret = mConvert.convertAudio(filePathTmp,filePathMp3);
     	     			 	convertProcessHasFinished = true;//the flag which identifies the convert process has finished.
     	     			 	if(ret ==0){
     	     			 		(new File(filePathTmp)).delete();//convert ended normally.
     	     			 		recordSongs.clear();
     	     			 		recordSongs = getRecordList(filePathMp3);
     	     			 		IMediaEventArgs eventArgs = new MediaEventArgs();
//     	     			 		eventArgs.putExtra("start_play_record", "start_play_record");
     	     					ServiceManager.getMediaEventService().onMediaUpdateEvent(eventArgs.setMediaUpdateEventTypes(MediaEventTypes.RECORD_UPDATE_DATA));

     	     			 	}else if(ret ==1){
     	     			 		//convert ended because it was interrupted by clicking save as wav button.
     	     			 		(new File(filePathMp3)).delete();
     	     			 		if(filePathMp3.contains("_mp3") && filePathMp3.contains(".mp3")){
     	     			 			filePathMp3 = filePathMp3.replace("_mp3", "_wav");
     	     			 			filePathMp3 = filePathMp3.replace(".mp3",".wav");
     	     			 		}
     	     			 		(new File(filePathTmp)).renameTo(new File(filePathMp3));
								IMediaEventArgs eventArgs = new MediaEventArgs();
     	     					ServiceManager.getMediaEventService().onMediaUpdateEvent(eventArgs.setMediaUpdateEventTypes(MediaEventTypes.RECORD_UPDATE_DATA));
     	     			 	}else if(ret ==2){
     	     			 		//convert ended because it was interrupted by clicking stop convert button.
     	     			 		(new File(filePathTmp)).delete();
     	     			 		(new File(filePathMp3)).delete();
     	     			 	}
     	     		 }catch(Exception e){
     	     			 e.printStackTrace();
     	     		  } 
    			 
     	     		convertIsInProcess = false;
    		
    	 }
    }
    /**
     * Default constructor Instantiates a new recorder, in case of compressed
     * recording the parameters can be left as 0. In case of errors, no
     * exception is thrown, but the state is set to ERROR
     */
    private ExtAudioRecorder(boolean uncompressed, int audioSource,
            int sampleRate, int channelConfig, int audioFormat) {
        try {
            if (audioFormat == AudioFormat.ENCODING_PCM_16BIT) {
                bSamples = 16;
            } else {
                bSamples = 8;
            }

            if (channelConfig == AudioFormat.CHANNEL_CONFIGURATION_MONO) {
                nChannels = 1;
            } else {
                nChannels = 2;
            }

            sRate = sampleRate;

            // -----------------------------------------
            recBufSize = AudioRecord.getMinBufferSize(sampleRate,
                    channelConfig, audioFormat);

            playBufSize = AudioTrack.getMinBufferSize(sampleRate,
                    channelConfig, audioFormat);

            audioRecord = new AudioRecord(audioSource, sampleRate,
                    channelConfig, audioFormat, recBufSize);

            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
                    channelConfig, audioFormat, playBufSize,
                    AudioTrack.MODE_STREAM);
            // ------------------------------------------

            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED){
            	throw new Exception("AudioRecord initialization failed");
            }
                

            if (audioTrack.getState() != AudioTrack.STATE_INITIALIZED)
                throw new Exception("AudioTrack initialization failed");

            tmpfilePath = null;
            mp3filePath = null;
            state = State.INITIALIZING;

        } catch (Exception e) {
            if (e.getMessage() != null) {
               // Log.e(ExtAudioRecorder.class.getName(), e.getMessage());
            } else {
               // Log.e(ExtAudioRecorder.class.getName(),
               //         "Unknown error occured while initializing recording");
            }
            state = State.ERROR;
        }
        
    }

    /**
     * Sets output file path, call directly after construction/reset.
     *
     * @param output
     *            file path
     */
    public void setOutputFile(String argPath1,String argPath2) {
        try {
            //if (state == State.INITIALIZING) 
        	{
                tmpfilePath = argPath1;
                mp3filePath = argPath2;
            }
        } catch (Exception e) {
            if (e.getMessage() != null) {
               // Log.e(ExtAudioRecorder.class.getName(), e.getMessage());
            } else {
               // Log.e(ExtAudioRecorder.class.getName(),
                //        "Unknown error occured while setting output path");
            }
            state = State.ERROR;
        }
    }

    /**
     * Prepares the recorder for recording, in case the recorder is not in the
     * INITIALIZING state and the file path was not set the recorder is set to
     * the ERROR state, which makes a reconstruction necessary. In case
     * uncompressed recording is toggled, the header of the wave file is
     * written. In case of an exception, the state is changed to ERROR
     */
    public void prepare() {
        try {

                    // write file header
	                 if(randomAccessWriter!=null){
	                 randomAccessWriter.close();
	                 randomAccessWriter = null;
	                 }
	                 File file1 = new File (MediaPlayerService.directoryRecord);
	                 if(!file1.exists()){
	                	 file1.mkdirs();
	                 }

	                 
                    randomAccessWriter = new RandomAccessFile(tmpfilePath, "rw");
                    
                    randomAccessWriter.setLength(0); // Set file length to
                    // 0, to prevent
                    // unexpected behavior
                    // in case the file
                    // already existed
                    randomAccessWriter.writeBytes("RIFF");
                    randomAccessWriter.writeInt(0); // Final file size not
                    // known yet, write 0
                    randomAccessWriter.writeBytes("WAVE");
                    randomAccessWriter.writeBytes("fmt ");
                    randomAccessWriter.writeInt(Integer.reverseBytes(16)); //Sub-
                                                                            // chunk
                    // size,
                    // 16
                    // for
                    // PCM
                    randomAccessWriter
                            .writeShort(Short.reverseBytes((short) 1)); // AudioFormat
                                                                        // ,
                    // 1
                    // for
                    // PCM
                    randomAccessWriter
                            .writeShort(Short.reverseBytes(nChannels));// Number
                    // of
                    // channels,
                    // 1
                    // for
                    // mono,
                    // 2
                    // for
                    // stereo
                    randomAccessWriter.writeInt(Integer.reverseBytes(sRate)); // Sample
                    // rate
                    randomAccessWriter.writeInt(Integer.reverseBytes(sRate
                            * bSamples * nChannels / 8)); // Byte rate,
                    // SampleRate*NumberOfChannels*BitsPerSample/8
                    randomAccessWriter.writeShort(Short
                            .reverseBytes((short) (nChannels * bSamples / 8))); // Block
                    // align,
                    // NumberOfChannels*BitsPerSample/8
                    randomAccessWriter.writeShort(Short.reverseBytes(bSamples)); // Bits
                    // per
                    // sample
                    randomAccessWriter.writeBytes("data");
                    randomAccessWriter.writeInt(0); // Data chunk size not
                    // known yet, write 0

                    state = State.READY;
                 
            

        } catch (Exception e) {
            if (e.getMessage() != null) {
               // Log.e(ExtAudioRecorder.class.getName(), e.getMessage());
            } else {
              //  Log.e(ExtAudioRecorder.class.getName(),
               //         "Unknown error occured in prepare()");
            }
            state = State.ERROR;
        }
    }

    /**
     * Starts the recording, and sets the state to RECORDING. Call after
     * prepare().
     */
    public void start() {
    	     //Log.e("zhangjingtest","RecordPlayThread start in!");
            payloadSize = 0;
            diskCheckSize = 0;
            recordPlayThread = new RecordPlayThread();// 开一条线程边录边放
            recordPlayThread.start();
            state = State.RECORDING; 
    }

    /**
     * Stops the recording, and sets the state to STOPPED. In case of further
     * usage, a reset is needed. Also finalizes the wave file in case of
     * uncompressed recording.
     */
    public void stop() {
        if (state == State.RECORDING) {
            recordPlayThread.stopThread();

        } else {
           // Log.e(ExtAudioRecorder.class.getName(),
            //        "stop() called on illegal state");
            state = State.ERROR;
        }
    }

    public int getPayloadSize() {
        return payloadSize;
    }

    public static int DEFAULT_DISKSIZE_UNDER_FLOW = 1024 * 1024 * 50;

    public static int DEFAULT_CHECK_INTERVAL_SIZE = 1024 * 1024 * 10;

    private int diskCheckSize;

    private int diskSizeUnderFlow = DEFAULT_DISKSIZE_UNDER_FLOW;

    private int diskCheckIntervalSize = DEFAULT_CHECK_INTERVAL_SIZE;

    public void setDiskSizeUnderFlow(int size) {
        diskSizeUnderFlow = size;
    }

    public void setDiskCheckIntervalSize(int size) {
        diskCheckIntervalSize = size;
    }

    public interface OnDiskSizeUnderflowListener {
        void OnDiskSizeUnderflow();
    }

    /**
     * Register a callback to be invoked when an info/warning is available.
     *
     * @param listener
     *            the callback that will be run
     */
    public void setOnDiskSizeUnderflowListener(
            OnDiskSizeUnderflowListener listener) {
        mOnDiskSizeUnderflowListener = listener;
    }

    private OnDiskSizeUnderflowListener mOnDiskSizeUnderflowListener = null;
}


