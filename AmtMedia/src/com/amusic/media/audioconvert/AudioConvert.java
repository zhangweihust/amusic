package com.amusic.media.audioconvert;

public class AudioConvert {

/*******************************************************************************
** convert .wav file to .mp3 file
** 
** input :
**        String wav : the .wav file (e.g. "/mnt/sdcard/test.wav")
**        byte[] mp3 : the output .mp3 file (e.g. "/mnt/sdcard/test.mp3")
** 
** return:
**        0 : convert success
**       -1 : something error occured
********************************************************************************/
    //public native int  convertAudio(String wav , byte[] mp3);    
	 public native int  convertAudio(String wav , String mp3);
/*******************************************************************************
** get the progress of the convertion
** 
** input : NULL
**
** return: 
**        String : the progress (e.g. 88 means the .wav file has converted 88%)
** 
********************************************************************************/   
    public native int  getCurrentDone();


/*******************************************************************************
** get the converter version
** 
** input : NULL
**
** return: 
**        String : the version of the converter (e.g. "3.99.4") 
**
********************************************************************************/    
    public native String  getLameVersion();    
/*
    static {
        System.loadLibrary("am_lame_jni");
    }
*/
    
    public native String  getMp3BufferSize();
    
    //during the process of convert, you can send some control info such as Save as wav directly or abort converting.
    //convert thread ended normally, we send the flag 0
    //Save as wav directly ---we send the flag 1
    //abort convert ---we send the flag 2
    public native int setControlInfo(int controlFlag);
}
