package com.amusic.media.utils;

import java.io.File;
import java.io.IOException;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
    
/*
 * PicInfoUtil Example:
 * EditPicInfoUtil ep = new EditPicInfoUtil("/work/Share/Blue hills.jpg");
       System.out.println(ep.getPicInfo(EditPicInfoUtil.GET_AUTHOR));
 * */
public class PicInfoUtil {
	private String mFilepath = null;
	private String mAuthor = "";
	private String mTitle = "";
	private String mComment = "";
	private String mKeywords = "";
	private String mSubject = "";
	public static String GET_AUTHOR = "author";
	public static String GET_TITLE = "title";
	public static String GET_COMMENT = "comment";
	public static String GET_KEYWORDS = "keywords";
	public static String GET_SUBJECT = "subject";
	private static final String KEYWORD = "windowsxp";
	
	public PicInfoUtil(String filepath){
		mFilepath = filepath;
		if(mFilepath != null && !mFilepath.equals("")){
		File jpegFile = new File(mFilepath);  
        Metadata metadata;
		try {
			metadata = JpegMetadataReader.readMetadata(jpegFile);
			String tmp = "";
			String value = "";
	        for (Directory directory : metadata.getDirectories()) {
	    	    for (Tag tag : directory.getTags()) {
	    	    	tmp = "";
	    	    	tmp = tag.toString().toLowerCase().replaceAll(" ", "");
	    	    	if(tmp.contains(KEYWORD)&& tmp.contains("-")){
	    	    		value = tmp.substring(tmp.indexOf("-") + 1);
	    	         if(tmp.contains(GET_AUTHOR)){
	    	        	 mAuthor = value;
	    	         }else if(tmp.contains(GET_TITLE)){
	    	        	 mTitle = value;
	    	         }else if(tmp.contains(GET_COMMENT)){
	    	        	 mComment = value;
	    	         }else if(tmp.contains(GET_KEYWORDS)){
	    	        	 mKeywords = value;
	    	         }else if(tmp.contains(GET_SUBJECT)){
	    	        	 mSubject = value;
	    	         }else{
	    	        	 
	    	         }
	    	    	}
	    	    }
	        }
		} catch (JpegProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	}
	
	public String getPicInfo(String name){
		String value = "";
		if(name.equals(GET_AUTHOR)){
			value = mAuthor;
		}else if(name.equals(GET_COMMENT)){
			value = mComment;
		}else if(name.equals(GET_TITLE)){
			value = mTitle;
		}else if(name.equals(GET_KEYWORDS)){
			value = mKeywords;
		}else if(name.equals(GET_SUBJECT)){
			value = mSubject;
		}else{
			
		}
		return value;
	}


	
}
