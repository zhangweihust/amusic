package com.android.media.lyric.parser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.android.media.lyric.player.LyricConfig;

import android.util.Log;


public class TXTParser {
	private List<String> list = new ArrayList<String>();
	private String path = null;
	private String lyrics = null;
	private int parserMode = 0;
	
	public TXTParser(String strPathOrLyrics,int parserMode) {
		 this.parserMode = parserMode;
	        if (parserMode == 0) {
	            this.path = strPathOrLyrics;
	        } else if (parserMode == 1) {
	            this.lyrics = strPathOrLyrics;
	        }
	}
	
	public List<String> parser()  throws Exception {
		InputStream in = null;
		if (parserMode == LyricConfig.paserWithPath) // path
        {
            in = readLrcFile(path);
        } else if (parserMode == LyricConfig.paserWithString) // lyrics
        {
            in = new ByteArrayInputStream(lyrics.getBytes());
        }
		
		// 三层包装
		InputStreamReader inr = new InputStreamReader(in);
        BufferedReader reader = new BufferedReader(inr);
        // 一行一行的读，每读一行，解析一行
        String line = null;
        int i = 0;
        while ((line = reader.readLine()) != null) {
//        	Log.e("<<<<<<<<<",i++ + " line : " + line);
        	if (line.equals("")) {
        		continue;
        	}
            list.add(line);
        }
        
		return list;
	}

	private InputStream readLrcFile(String path2) throws FileNotFoundException  {
		File f = new File(path);
        InputStream ins = new FileInputStream(f);
        return ins;
	}
}
