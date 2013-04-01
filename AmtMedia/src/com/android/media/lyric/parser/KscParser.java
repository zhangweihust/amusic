
package com.android.media.lyric.parser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.amusic.media.R;
import com.android.media.dialog.OnScreenHint;
import com.android.media.lyric.player.LyricUtil;
import com.android.media.services.impl.ServiceManager;

/**
 * KscParser是对歌词文件进行解析的一个解析器 这个类负责对歌词文件中的每一行歌词进行逐行解 析，然后把解析出来的信息保存在一个成员变量
 * maps中，这个变量最后会被用于构造KscInfo对象 客户羰获取KscInfo对象后就可以从个对象中读取相关 信息了
 * 
 * @author root
 */
public class KscParser extends LyricParser {
    private LyricInfo kscinfo = new LyricInfo(); // 用户保存歌词文件信息的对象

    private List<Sentence> list = new ArrayList<Sentence>();// 用户保存所有的歌词和时间点信息间的映射关系的Map

    
   
    private OnScreenHint mOnScreenHint;
    public KscParser(String strPathOrLyrics, int parserMode) {
        this.parserMode = parserMode;
        if (parserMode == 0) {
            this.path = strPathOrLyrics;
        } else if (parserMode == 1) {
            this.lycris = strPathOrLyrics;
        }
        
        ServiceManager.getAmtMediaHandler().post(new Runnable() {
    		@Override
    		public void run() {
    			mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.modify_lyric_error));
    		}
    	});
        
        // set lyrics to LyricInfo object 
        kscinfo.setLyrics(this.lycris);
    }

    /**
     * 根据输入的文件路径，解析这个文件，并返回一个 保存有歌词文件信息的KscInfo对象
     * 
     * @param path 路径
     * @return
     * @throws Exception
     */
    public LyricInfo parser() throws Exception {

        InputStream in = null;

        if (parserMode == 0) // path
        {
            in = readLrcFile(path);
        } else if (parserMode == 1) // lyrics
        {
            in = new ByteArrayInputStream(lycris.getBytes());
        }

        kscinfo = parser(in);

        return kscinfo;

    }

    /**
     * 得到当前正在播放的那一句的下标 不可能找不到，因为最开头要加一句 自己的句子 ，所以加了以后就不可能找不到了
     * 
     * @return 下标
     */
    public int getNowSentenceIndex(long time) {
    	int listSize = list.size();
        for (int i = 0; i < listSize; i++) {
            if (list.get(i).isInTime(time)) {
                return i;
            }
        }
        // throw new RuntimeException("竟然出现了找不到的情况！");
        return -1;
    }
    
    /**
     * @param time 当前歌词的时间轴
     * @return null
     */
    public int updateIndex(long time) {
    	if (list.size() <= 0) {
    		return -1;
    	}
        // 歌词序号
        int index = -1;
        index = getNowSentenceIndex(time);
        if (index != -1) {
            list.get(index).setCurrentTime(time);
        }

        return index;
    }
    
    /**
     * 更新，该函数主要用于在抢占情况下更新，如seek和界面创建的时候，如果时间在两句之间，则更新为前面一句
     */
    public int updateIndex(long time, boolean isSeek) {
    	if (!isSeek) {
    		return updateIndex(time);
    	}
    	
    	if (list.size() == 0) {
    		return -1;
    	}
    	
    	for (int i = 0; i < list.size(); i++) {
            if (time < list.get(i).getStartTime()) {
            	if (i == 0) {
            		return updateIndex(time);
            	}
            	if (list.get(i - 1).isInTime(time)) {
            		list.get(i - 1).setCurrentTime(time);
                } else {
                	ArrayList<Integer> timeList = list.get(i - 1).getIntervalList();
                	int totaltime = 0;
                	for(Integer total:timeList) {
                		totaltime += total;
                	}
            	    list.get(i - 1).setCurrentTime(list.get(i - 1).getStartTime() + totaltime);
                }
                return i - 1;
            }
        }
    	
    	if (time < list.get(list.size() - 1).getEndTime()) {
    		list.get(list.size() - 1).setCurrentTime(time);
    	} else {
    		list.get(list.size() - 1).setCurrentTime(list.get(list.size() - 1).getEndTime());
    	}
    	
    	return list.size() - 1;
    }

    /**
     * 将输入流中的信息解析，返回一个KscInfo对象
     * 
     * @param inputStream 输入流
     * @return 解析好的LrcInfo对象
     * @throws IOException
     */
    private LyricInfo parser(InputStream inputStream) throws IOException {
        // 三层包装
        InputStreamReader inr = new InputStreamReader(inputStream);
        BufferedReader reader = new BufferedReader(inr);
        // 一行一行的读，每读一行，解析一行
        String line = null;
        int whichLine = 0;
        boolean isRepeat = false;
        
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("karaoke := CreateKaraokeObject")) {
            	if (isRepeat) {
            		break;
            	}
            	isRepeat = true;
            }
            parserLine(line , whichLine++);
        }
        // 全部解析完后，设置info
        kscinfo.setList(list);
        return kscinfo;
    }

    /**
     * 根据文件路径，读取文件，返回一个输入流
     * 
     * @param path 路径
     * @return 输入流
     * @throws FileNotFoundException
     */
    private InputStream readLrcFile(String path) throws FileNotFoundException {
        File f = new File(path);
        InputStream ins = new FileInputStream(f);
        return ins;
    }

    /**
     * 利用正则表达式解析每行具体语句 并在解析完该语句后，将解析出来 的信息设置在KscInfo对象中 并将每一行的信息存储在maps成员变量中
     * 
     * @param str
     */
    private void parserLine(String str, int whichLine) {

        // 取得歌曲名信息
        if (str.startsWith("karaoke.songname :=")) {
            String title = str.substring("karaoke.songname :=".length() + 1, str.length() - 2);
            kscinfo.setTitle(title);

        }// 取得歌手信息
        else if (str.startsWith("karaoke.singer :=")) {
            String singer = str.substring("karaoke.singer :=".length() + 1, str.length() - 2);
             kscinfo.setSinger(singer);

        } // 取得歌词长度
        else if (str.startsWith("karaoke.duration :=")) {
        	String durationStr = str.substring("karaoke.duration :=".length() + 1, str.length() - 2);
        	long duration = Long.parseLong(durationStr);
        	kscinfo.setMusicDuration(duration);
        } 
        else if (str.startsWith("karaoke.lyricmaker :=")) {
        	kscinfo.setLyricMaker(true);
        }
        // 通过正则取得每句歌词信息
        else if (str.startsWith("karaoke.add")) {
            /*
             * 设置正则规则 ,通过正则表达式中的捕获组概念，对要获取的信息进行分组，比如正则规则中的字句
             * (\\d{2}:\\d{2}\\.\\d{3})就代表每一行歌词的起始时间，它被分配的组号是1。而(.*)分别代表
             * 该行歌词内容和单字持续时间等信息。它们分别被分配组号3和4
             */
            String reg = "^karaoke\\.add\\('(\\d{2}:\\d{2}\\.\\d{3})',\\s*'(\\d{2}:\\d{2}\\.\\d{3})',\\s*"
                    + "'(.*)',\\s*'(.*)'\\);";
            // 编译
            Pattern pattern = Pattern.compile(reg);
            Matcher matcher = pattern.matcher(str);
            boolean b = matcher.matches();
            if (b) {
                // 获取起始时间
                String startTime = matcher.group(1);
                // 获取终止时间
                String endTime = matcher.group(2);
                // 获取该行歌词内容
                String content = matcher.group(3);
                // 获取该行歌词中单字的持续时间
                String interval = matcher.group(4);

                // 构造LineInfo对象，把歌词内容和时间点信息封装在一起
                Sentence lineinfo = new Sentence(content, strToLong(startTime), strToLong(endTime),
                        interval, list.size(), whichLine);

                // 把相关信息加入Map从而建立关联，这样就可以通过起始时间来获取信息了
                list.add(lineinfo);
            } else {
                //System.out.println("no");
            }

        }
    }

    /**
     * 将解析得到的表示时间的字符转化为Long型
     * 
     * @param group 字符形式的时间点
     * @return Long形式的时间
     */
    private long strToLong(String timeStr) {
        // 因为给如的字符串的时间格式为XX:XX.XX,返回的long要求是以毫秒为单位
        // 1:使用：分割
        // 2：使用.分割
        String[] s = timeStr.split(":");
        int min = Integer.parseInt(s[0]);
        String[] ss = s[1].split("\\.");
        int sec = Integer.parseInt(ss[0]);
        int mill = Integer.parseInt(ss[1]);
        return min * 60 * 1000 + sec * 1000 + mill;
    }
    
    /**
     * 
     * @param time
     * @return
     */
    public String timeToString(long time) {
        String minuteString;
        String secondString;
        String millString;
        
        long minute = time / (60 * 1000);
        long second = (time % (60 * 1000))/1000;
        long mill = (time % (60 * 1000))%1000;

        if (minute < 10) {
            minuteString = "0" + minute;
        } else {
            minuteString = Long.toString(minute);
        }
        if (second < 10) {
            secondString = "0" + second;
        } else {
            secondString = Long.toString(second);
        }
        if (mill < 10) {
            millString = "00" + mill;
        } else if ((mill >= 10) && (mill < 100)) {
            millString = "0" + mill;
        } else {
            millString = Long.toString(mill);
        }

        return (minuteString + ":" + secondString + "." + millString);
    }
    
    /**
     * Adjust the time offset of a single sentence lyrics
     * 
     * @param index the index of current and later lyrics
     * @param offsetTime the offsetTime is in milliseconds
     * @return
     */
    public String adjustCurrentSentence( int index, long offsetTime ){
        BufferedReader in = new BufferedReader(new InputStreamReader(LyricUtil.String2InputStream(lycris)));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        try {
            int currentLine = 0;
            while ((line = in.readLine()) != null) {   
                if (list.get(index).getwhichLine() == (currentLine++)){
                    line = replaceLine(index, line, offsetTime);
                    if (line == null){
                        return null;
                    }
                }
                
                buffer.append(line);
                buffer.append("\n"); // 特别注意每一句的换行符号不能去掉。
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        // set lyrics to LyricInfo object 
        this.lycris = buffer.toString();
        kscinfo.setLyrics(this.lycris);
        return buffer.toString();   
    }
    
    /**
     * Adjusted the time offset from the current index 
     * to the end about lyrics
     * 
     * @param offsetTime the offsetTime is in milliseconds
     * @return
     */
    public String adjustFromCurrent( int index, long offsetTime ){
        BufferedReader in = new BufferedReader(new InputStreamReader(LyricUtil.String2InputStream(lycris)));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        try {
            int currentLine = 0;
            boolean isFromCurrent = false;
            while ((line = in.readLine()) != null) {   
                if (list.get(index).getwhichLine() == (currentLine++)){
                    isFromCurrent = true;
                }
                
                if(isFromCurrent){
                    line = replaceLine(index, line, offsetTime);
                    if (line == null){
                        return null;
                    }
                }
                    
                buffer.append(line);
                buffer.append("\n"); // 特别注意每一句的换行符号不能去掉。
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        // set lyrics to LyricInfo object 
        this.lycris = buffer.toString();
        kscinfo.setLyrics(this.lycris);
        return buffer.toString();  
    }
    
    /**
     * Adjusted the time offset to the current index 
     * to the end about lyrics
     * 
     * @param offsetTime the offsetTime is in milliseconds
     * @return
     */
    public String adjustToCurrent( int index, long offsetTime ){
        BufferedReader in = new BufferedReader(new InputStreamReader(LyricUtil.String2InputStream(lycris)));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        try {
            int currentLine = 0;
            boolean isToCurrent = true;
            while ((line = in.readLine()) != null) {   
                if (list.get(index).getwhichLine() == (currentLine++)){
                    isToCurrent = false;
                }
                
                if(isToCurrent){
                    line = replaceLine(index, line, offsetTime);
                    if (line == null){
                        return null;
                    }
                }
                    
                buffer.append(line);
                buffer.append("\n"); // 特别注意每一句的换行符号不能去掉。
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        // set lyrics to LyricInfo object 
        this.lycris = buffer.toString();
        kscinfo.setLyrics(this.lycris);
        return buffer.toString();  
    }
    
    /**
     * Adjust the time offset of all lyrics
     * 
     * @param offsetTime the offsetTime is in milliseconds
     * @return
     */
    public String adjustAll( long offsetTime ){
        BufferedReader in = new BufferedReader(new InputStreamReader(LyricUtil.String2InputStream(lycris)));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        try {
            int index = 0;
            int currentLine = 0;
            int listSize = list.size();
            boolean isBeginReplace = false;
            while ((line = in.readLine()) != null) {   
                if (!isBeginReplace && list.get(0).getwhichLine() == (currentLine++)){
                    isBeginReplace = true;
                }
                if(isBeginReplace){
                	if (index >= listSize) {
                		break;
                	}
                    line = replaceLine(index++, line, offsetTime);
                    if (line == null){
                        return null;
                    }
                }
                
                buffer.append(line);
                buffer.append("\n"); // 特别注意每一句的换行符号不能去掉。
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        
        // set lyrics to LyricInfo object 
        this.lycris = buffer.toString();
        kscinfo.setLyrics(this.lycris);
        return buffer.toString();  
    }
    
    public void clearList() {
    	list.clear();
    }
    
    /**
     * 
     * @param index
     * @param line
     * @param offsetTime
     * @return
     */
    public String replaceLine(int index, String line, long offsetTime ){
        // check the offsetTime
        if ( (list.get(index).getStartTime() + offsetTime) < 0 ){
			ServiceManager.getAmtMediaHandler().post(new Runnable() {
	    		@Override
	    		public void run() {
	    			if(mOnScreenHint != null)
	    			mOnScreenHint.cancel();
	    			mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.modify_lyric_error));
	    			mOnScreenHint.show();
	    		}
	    	});
            return null;
        }
        
        // 改写当前行。
        line = line.replace(timeToString( list.get(index).getStartTime()), 
                timeToString( list.get(index).getStartTime() + offsetTime ) );
        line = line.replace(timeToString( list.get(index).getEndTime()), 
                timeToString( list.get(index).getEndTime() + offsetTime ) );
        
        // 改写list
        list.get(index).setStartTime( list.get(index).getStartTime() + offsetTime );
        list.get(index).setEndTime( list.get(index).getEndTime() + offsetTime );
        return line;
    }

}
