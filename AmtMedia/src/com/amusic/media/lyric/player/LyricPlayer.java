package com.amusic.media.lyric.player;

import android.media.MediaPlayer;
import android.os.Handler;
import android.view.SurfaceView;

import com.amusic.media.dialog.LyricModify;
import com.amusic.media.event.impl.MediaEventArgs;
import com.amusic.media.event.impl.MediaEventTypes;
import com.amusic.media.lyric.parser.LyricInfo;
import com.amusic.media.lyric.parser.LyricParser;
import com.amusic.media.lyric.parser.Sentence;
import com.amusic.media.lyric.render.FullLyricView;
import com.amusic.media.lyric.render.PhoneKTVView;
import com.amusic.media.services.IMediaEventService;
import com.amusic.media.services.impl.ServiceManager;

public class LyricPlayer {

	/*
	 * status
	 */
	public class LyricPlayerStatus {
		private static final int PVP_ENGINE_STATE_IDLE = 1;

		private static final int PVP_ENGINE_STATE_INITIALIZED = 2;

		private static final int PVP_ENGINE_STATE_PREPARED = 3;

		private static final int PVP_ENGINE_STATE_STARTED = 4;

		private static final int PVP_ENGINE_STATE_PAUSED = 5;

		private static final int PVP_ENGINE_STATE_RESUME = 6;

		private static final int PVP_ENGINE_STATE_STOP = 7;

		private static final int PVP_ENGINE_STATE_SEEK = 8;

	}

	// thread Status
	private boolean isthreadRunning = false;

	// current status
	private int status = LyricPlayerStatus.PVP_ENGINE_STATE_IDLE;

	// last status for seeking
	private int lastStatus = LyricPlayerStatus.PVP_ENGINE_STATE_IDLE;

	// to seeking
	private boolean isInterrupted = false;

	private LyricParserFactory factory = null;

	private LyricParser parser = null;

	// 获得ksc歌词数据；
	private LyricInfo lyricInfo = null;

	// 当前索引
	private int currentIndex = -1;

	// 上一次的索引
	private int lastIndex = -1;
	
	private float ratio = (float) 1.0;
	
	private long lyricduration = 0;

	private MediaPlayer mp = null;

	private SurfaceView lyricView = null;

	// 桌面歌词added by jiaming.wang@amusic.com
	private SurfaceView desktopView = null;
    // 全屏歌词 added by jiaming.wang@amusic.com
    private SurfaceView fullView = null;
    
	//added by jiaming.wang@amusic.com
    private Boolean isktvviewCreated = false;
    private Boolean isdesktopCreated = false;
    private Boolean isfullviewCreated = false;

	// LyricPlayer 线程
	Thread lyricPlayerThread = null;

	// 视图和渲染器
	private PhoneKTVView phoneKtvlyricView = null;

	private PhoneKTVView desktoplyricView = null;
    
    private FullLyricView fulllyricView = null;
    
    // update给渲染类的cmd
    private int phoneKtvCmd = PhoneKTVView.LYRIC_INFO_UNCHANGED;
    

	public LyricInfo getLyricsInfo() {
		return lyricInfo;
	}

	public int getCurrentIndex() {
		return currentIndex;
	}

	/**
	 * get the all lyrics at last
	 * 
	 * @return new lyrics
	 */
	public String getLastLyrics() {
		if (parser != null) {
			return parser.getLyrcis();
		}
		return null;
	}

	/**
	 * Adjust the time offset of a single sentence lyrics
	 * 
	 * @param offsetTime
	 *            the offsetTime is in milliseconds
	 * @return new lyrics string, if error the return value is null
	 */
	public String adjustCurrentSentence(long offsetTime) {
		// control the parser
		String lyrics = null;
		if (parser != null && currentIndex >= 0) {
			lyrics = parser.adjustCurrentSentence(currentIndex, offsetTime);
			if (lyrics != null) {
				if (phoneKtvlyricView != null) {
					phoneKtvlyricView.prepare(lyricInfo);
					phoneKtvCmd = PhoneKTVView.LYRIC_INFO_CHANGED;
				}
                
                // added by jiaming.wang@amusic.com
                if (desktoplyricView != null){
                	desktoplyricView.prepare(lyricInfo);
                    phoneKtvCmd = PhoneKTVView.LYRIC_INFO_CHANGED; 
                }
                
                if (fulllyricView != null){
                	fulllyricView.prepare(lyricInfo);
                    phoneKtvCmd = PhoneKTVView.LYRIC_INFO_CHANGED; 
                }
            }
        }
        
		return lyrics;
	}

	/**
	 * Adjusted the time offset from the current index to the end about lyrics
	 * 
	 * @param offsetTime
	 *            the offsetTime is in milliseconds
	 * @return new lyrics string, if error the return value is null
	 */
	public String adjustFromCurrent(long offsetTime) {
		// control the parser
		String lyrics = null;
		if (parser != null && currentIndex >= 0) {
			lyrics = parser.adjustFromCurrent(currentIndex, offsetTime);
			if (lyrics != null) {
				if (phoneKtvlyricView != null) {
					phoneKtvlyricView.prepare(lyricInfo);
					phoneKtvCmd = PhoneKTVView.LYRIC_INFO_CHANGED;
				}

				// added by jiaming.wang@amusic.com
				if (desktoplyricView != null) {
					desktoplyricView.prepare(lyricInfo);
					phoneKtvCmd = PhoneKTVView.LYRIC_INFO_CHANGED;
				}
                
                if (fulllyricView != null){
                	fulllyricView.prepare(lyricInfo);
                    phoneKtvCmd = PhoneKTVView.LYRIC_INFO_CHANGED; 
                }
            }
        }  
		return lyrics;
	}

	/**
	 * Adjusted the time offset to the current index to the end about lyrics
	 * 
	 * @param offsetTime
	 *            the offsetTime is in milliseconds
	 * @return new lyrics string, if error the return value is null
	 */
	public String adjustToCurrent(long offsetTime) {
		// control the parser
		String lyrics = null;
		if (parser != null && currentIndex >= 0) {
			lyrics = parser.adjustToCurrent(currentIndex, offsetTime);
			if (lyrics != null) {
				if (phoneKtvlyricView != null) {
					phoneKtvlyricView.prepare(lyricInfo);
					phoneKtvCmd = PhoneKTVView.LYRIC_INFO_CHANGED;
				}

				// added by jiaming.wang@amusic.com for desktoplyric
				if (desktoplyricView != null) {
					desktoplyricView.prepare(lyricInfo);
					phoneKtvCmd = PhoneKTVView.LYRIC_INFO_CHANGED;
				}
                
                if (fulllyricView != null){
                	fulllyricView.prepare(lyricInfo);
                    phoneKtvCmd = PhoneKTVView.LYRIC_INFO_CHANGED; 
                }
            }
        }  
		return lyrics;
	}

	/**
	 * Adjust the time offset of all lyrics
	 * 
	 * @param offsetTime
	 *            the offsetTime is in milliseconds
	 * @return new lyrics string, if error the return value is null
	 */
	public String adjustAll(long offsetTime) {
		// control the parser
		if (offsetTime == 0) {
			return null;
		}
		String lyrics = null;
		if (parser != null) {
			synchronized (this) {
				lyrics = parser.adjustAll(offsetTime);
				
				if (lyrics != null) {
					try {
						parser.clearList();

						lyricInfo = parser.parser();

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (phoneKtvlyricView != null) {
	//					phoneKtvlyricView.prepare(lyricInfo);
						phoneKtvlyricView.setLyricInfo(lyricInfo);
						phoneKtvCmd = PhoneKTVView.LYRIC_INFO_CHANGED;
					}
	
					// added by jiaming.wang@archermind.com for desktoplyric
					if (desktoplyricView != null) {
						phoneKtvlyricView.setLyricInfo(lyricInfo);
						phoneKtvCmd = PhoneKTVView.LYRIC_INFO_CHANGED;
					}
	                
	                if (fulllyricView != null){
	                	fulllyricView.setLyricInfo(lyricInfo);
	                    phoneKtvCmd = PhoneKTVView.LYRIC_INFO_CHANGED; 
	                }
	                
	                if (lyricInfo != null && lyricInfo.getList() != null && lyricInfo.getList().size() != 0 
	    					&& mp.getCurrentPosition() > lyricInfo.getList().get(0).getStartTime()) {
	    				seekLyricPlayer();
	    			}
	            }
			}
//			seekLyricPlayer();
        }
        return lyrics;
    }
    
    /**
     * 
     * @param factory
     * @param mp
     */
	public LyricPlayer(LyricParserFactory factory, MediaPlayer mp) {
		this.factory = factory;
		this.mp = mp;
		setStatus(LyricPlayerStatus.PVP_ENGINE_STATE_INITIALIZED);
	}

	/**
	 * @param path
	 * @return
	 */
	public LyricParser prepareLyricPlayer(String path) {
		setStatus(LyricPlayerStatus.PVP_ENGINE_STATE_PREPARED);
		parser = factory.createLyricsParser(path);
        
        implementPlayer();

		return parser;
	}

	/**
	 * @param lyrics
	 * @param type
	 * @return
	 */
	public LyricParser prepareLyricPlayer(String lyrics, String type) {
		if (lyrics == null) {
			lyricInfo = null;
			return null;
		}
		setStatus(LyricPlayerStatus.PVP_ENGINE_STATE_PREPARED);
		parser = factory.createLyricsParser(lyrics, type);
        
        implementPlayer();
        
		return parser;
	}

	/**
	 * @return true：启动成功； false：启动失败
	 */
	public void startLyricPlayer() {

		if (isthreadRunning == false) {
        	if (lyricInfo != null) {
	        	if (lyricInfo.getList() != null && lyricInfo.getList().size() != 0 && mp.getCurrentPosition() > lyricInfo.getList().get(0).getStartTime()) {
	        		if (phoneKtvlyricView != null) {
	        		    phoneKtvlyricView.setIsSameSong(true);
	        		}
	        		if (desktoplyricView != null) {
	        		    desktoplyricView.setIsSameSong(true);
	        		}
	        	} else {
	        		if (phoneKtvlyricView != null) {
	        		    phoneKtvlyricView.setIsSameSong(false);
	        		}
	        		if (desktoplyricView != null) {
	        		    desktoplyricView.setIsSameSong(false);
	        		}
	        	}
        	} else {
        		return;
        	}
        	
        	lyricduration = lyricInfo.getMusicDuration();
            
        	if (phoneKtvlyricView != null) {
                phoneKtvlyricView.start();
        	}

			// added by jiaming.wang@amusic.com for desktoplyric
			if (desktoplyricView != null) {
        	    desktoplyricView.start();
			}
            
			if (fulllyricView != null) {
                fulllyricView.start();
			}

			isthreadRunning = true;
			setStatus(LyricPlayerStatus.PVP_ENGINE_STATE_STARTED);
			lyricPlayerThread = new Thread(new UIUpdateThread());
			lyricPlayerThread.setName("LyricPlayer.UIUpdateThread" + lyricPlayerThread.getId());
			lyricPlayerThread.start();
		}
	}

	/**
     * 
     */
	public void pauseLyricPlayer() {
		if (LyricPlayerStatus.PVP_ENGINE_STATE_PAUSED == getStatus()) {
			return;
		}
		setStatus(LyricPlayerStatus.PVP_ENGINE_STATE_PAUSED);
		if (phoneKtvlyricView != null) {
		    phoneKtvlyricView.pause();
		}
		// added by jiaming.wang@amusic.com for desktoplyric
		if (desktoplyricView != null) {
		    desktoplyricView.pause();
		}
        
		if (fulllyricView != null) {
            fulllyricView.pause();
		}

	}

	/**
     * 
     */
	public void resumeLyricPlayer() {
		if (LyricPlayerStatus.PVP_ENGINE_STATE_RESUME == getStatus()) {
			return;
		}
		setStatus(LyricPlayerStatus.PVP_ENGINE_STATE_RESUME);
		
		if (phoneKtvlyricView != null) {
		    phoneKtvlyricView.resume();
		}
		
		if (desktoplyricView != null) {
			desktoplyricView.resume();
		}
        
		if (fulllyricView != null) {
            fulllyricView.resume();
		}
    }

	/**
     * 
     */
	public void seekLyricPlayer() {
		if (isthreadRunning == true) {
			lastStatus = getStatus();
			setStatus(LyricPlayerStatus.PVP_ENGINE_STATE_SEEK);
			if (lastStatus == LyricPlayerStatus.PVP_ENGINE_STATE_SEEK) {
				lastStatus = LyricPlayerStatus.PVP_ENGINE_STATE_STARTED;
			}
			
			if (phoneKtvlyricView != null) {
			    phoneKtvlyricView.clearRender();
			}
			// added by jiaming.wang@amusic.com for desktoplyric
			if (desktoplyricView != null) {
			    desktoplyricView.clearRender();
			}
			
			if (fulllyricView != null) {
//			    fulllyricView.clearRender();
			}
		}
	}

	/**
     * 
     */
	public void stopLyricPlayer() {
		LyricModify.downNum = 0;
		LyricModify.upNum = 0;
		if (isthreadRunning == true) {
			setStatus(LyricPlayerStatus.PVP_ENGINE_STATE_STOP);
			isthreadRunning = false;
			if (phoneKtvlyricView != null) {
				phoneKtvlyricView.stop();
			}

			// added by jiaming.wang@amusic.com for desktoplyric
			if (desktoplyricView != null) {
				desktoplyricView.stop();
			}

            if (fulllyricView != null) {
            	fulllyricView.stop();
            }
            
			// can't start at once;
			try {
				Thread.sleep(LyricConfig.lyricPlayerRunInterval);

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			lyricInfo = null;
		}
	}

	private int getStatus() {
		return status;
	}

	private void setStatus(int status) {
		this.status = status;
	}

	private void implementPlayer() {
		try {
			lyricInfo = parser.parser();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (lyricInfo != null && (lyricInfo.getList() == null || lyricInfo.getList().size() == 0)) {
			IMediaEventService mediaEventService = ServiceManager.getMediaEventService();
			MediaEventArgs args = new MediaEventArgs();
			mediaEventService.onMediaUpdateEvent(args.setMediaUpdateEventTypes(MediaEventTypes.LYRIC_ERROR_DELETE));
		}
		// 建立渲染器和视图
		// 此部分未来就可以扩充使用不同的view或render
		phoneKtvlyricView = (PhoneKTVView) lyricView;
		phoneKtvlyricView.prepare(lyricInfo);
		// added by jiaming.wang@amusic.com for desktoplyric
		desktoplyricView = (PhoneKTVView) desktopView;
		desktoplyricView.prepare(lyricInfo);
        fulllyricView = (FullLyricView)fullView;
        fulllyricView.prepare(lyricInfo);
	}

	// 实现Runnable接口；
	public class UIUpdateThread implements Runnable {
		long time = LyricConfig.lyricPlayerRunInterval; // 滚动速度

		public void run() {

			while (isthreadRunning) {
				if (mp == null) {
					break;
				} else {
					// 对索引值的修改需要同步
					synchronized (LyricPlayer.this) {
                        if ( (lastStatus == LyricPlayerStatus.PVP_ENGINE_STATE_SEEK 
                        		|| getStatus() == LyricPlayerStatus.PVP_ENGINE_STATE_SEEK )
                                && false == isInterrupted ) {
                        	
//                        	long musicduration = mp.getDuration();
//    						long diff = musicduration - lyricduration;
//                            
//                            // 字幕索引
//                            currentIndex = parser.updateIndex(mp.getCurrentPosition() - diff,true);

                        	
                        	long curPosition = (long) (/*ratio * */ mp.getCurrentPosition());
                        	currentIndex = parser.updateIndex(curPosition,true);
//                        	Log.d("=DDD=","ratio = " + ratio + " currentIndex = " + currentIndex + " curPosition = " + curPosition);
                            if (currentIndex != -1) {
                                // 打断机制
                                isInterrupted = true;
//                                mHandler.post(mUpdateResults);
                                doUpdate();
                                lastIndex = currentIndex;
                                
                            } else {
                            	if (lyricInfo.getList().size() <= 0) {
                            		currentIndex = lastIndex;
                            		continue;
                            	}
                            	setStatus(lastStatus);
                            	if (phoneKtvlyricView != null) {
                            	    phoneKtvlyricView.prepareRender();
                            	}
                            	
                            	if (desktoplyricView != null) {
                            		desktoplyricView.prepareRender();
                            	}
                            	
                            	if (fulllyricView != null) {
                            		fulllyricView.prepareRender();
                            	}
                            }
                        } else if (phoneKtvlyricView != null && (isktvviewCreated != phoneKtvlyricView.getIsSurfaceCreated())
                        		|| (desktoplyricView != null && isdesktopCreated != desktoplyricView.getIsSurfaceCreated())
                        		|| (fulllyricView != null && isfullviewCreated != fulllyricView.getIsSurfaceCreated())) {
                        	isktvviewCreated = phoneKtvlyricView.getIsSurfaceCreated();
                    		isdesktopCreated = desktoplyricView.getIsSurfaceCreated();
                    		isfullviewCreated = fulllyricView.getIsSurfaceCreated();
                            
//                    		long musicduration = mp.getDuration();
//    						long diff = musicduration - lyricduration;
//                    		// 字幕索引
//                            currentIndex = parser.updateIndex(mp.getCurrentPosition()-diff,true);

                    		long curPosition = (long) (/*ratio * */mp.getCurrentPosition());
                        	currentIndex = parser.updateIndex(curPosition,true);
                            if (currentIndex != -1) {
                            	
                            	try {
            						Thread.sleep(time / 2);

            					} catch (InterruptedException e) {
            						// TODO Auto-generated catch block
            						e.printStackTrace();
            					}
                                // 打断机制
//                                mHandler.post(mUpdateResults);
            					doUpdate();
                                
                                try {
            						Thread.sleep(time / 2);

            					} catch (InterruptedException e) {
            						// TODO Auto-generated catch block
            						e.printStackTrace();
            					}
                                lastIndex = currentIndex;
                            } else {
                            	if (phoneKtvlyricView != null) {
                            	    phoneKtvlyricView.prepareRender();
                            	}
                            	
                            	if (desktoplyricView != null) {
                            		desktoplyricView.prepareRender();
                            	}
                            	
                            	if (fulllyricView != null) {
                            		fulllyricView.prepareRender();
                            	}
                            }
                        } else if ((getStatus() == LyricPlayerStatus.PVP_ENGINE_STATE_STARTED)
                                || (getStatus() == LyricPlayerStatus.PVP_ENGINE_STATE_RESUME)) {
                            
//                        	long musicduration = mp.getDuration();
//    						long diff = musicduration - lyricduration;
//                        	// 字幕索引
//                          currentIndex = parser.updateIndex(mp.getCurrentPosition()-diff);

                        	long curPosition = (long) (/*ratio * */mp.getCurrentPosition());
                        	int curIndex = parser.updateIndex(curPosition);
                        	
							if ((curIndex != lastIndex) && (curIndex != -1)) {
								currentIndex = curIndex;
//								mHandler.post(mUpdateResults);
								doUpdate();
								lastIndex = curIndex;
							}
						}
					}

					try {
						Thread.sleep(time);

					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			// reset
			// 对索引值的修改需要同步
			synchronized (LyricPlayer.this) {
				// 当前索引
				currentIndex = -1;
				// 初始化保持和currentIndex不同
				lastIndex = -1;
				// reset seek所使用的标志变量
				isInterrupted = false;
				lastStatus = LyricPlayerStatus.PVP_ENGINE_STATE_IDLE;

				// 线程的变量重置
				lyricPlayerThread = null;
			}
		}
	}
	
	private void doUpdate() {

		if (currentIndex != -1) {
			if (lyricInfo == null || lyricInfo.getList() == null || lyricInfo.getList().size() <= currentIndex) {
				return;
			}
			Sentence stc = lyricInfo.getList().get(currentIndex);

			if (phoneKtvlyricView != null) {
				phoneKtvlyricView.update(stc, phoneKtvCmd,getStatus() == LyricPlayerStatus.PVP_ENGINE_STATE_PAUSED);
			}

			// added by jiaming.wang@archermind.com for desktoplyric
			if (desktoplyricView != null) {
				desktoplyricView.update(stc, phoneKtvCmd,getStatus() == LyricPlayerStatus.PVP_ENGINE_STATE_PAUSED);
			}
         // added by jiaming.wang@archermind.com for desktoplyric  
//				Log.d("=DDD=","LyricPlayer fulllyricView update " + fulllyricView.toString());
            if (fulllyricView != null) {
            	fulllyricView.update(stc,getStatus() == LyricPlayerStatus.PVP_ENGINE_STATE_PAUSED);
            }
            
            if (isInterrupted) {
				// 待更新以后，马上恢复到正常的情况
				isInterrupted = false;
				setStatus(lastStatus);
				lastStatus = LyricPlayerStatus.PVP_ENGINE_STATE_IDLE;
			}
            
            phoneKtvCmd = PhoneKTVView.LYRIC_INFO_UNCHANGED;
		}
	}

	Handler mHandler = new Handler();

	Runnable mUpdateResults = new Runnable() {
		public void run() {
			// 扩展功能是将获得的参数传递给view

			synchronized (LyricPlayer.this) {
				if (currentIndex != -1) {
					if (lyricInfo == null || lyricInfo.getList() == null || lyricInfo.getList().size() <= currentIndex) {
						return;
					}
					Sentence stc = lyricInfo.getList().get(currentIndex);
					if (phoneKtvlyricView != null) {
						phoneKtvlyricView.update(stc, phoneKtvCmd,getStatus() == LyricPlayerStatus.PVP_ENGINE_STATE_PAUSED);
					}

					// added by jiaming.wang@amusic.com for desktoplyric
					if (desktoplyricView != null) {
						// Test code;
						desktoplyricView.update(stc, phoneKtvCmd,getStatus() == LyricPlayerStatus.PVP_ENGINE_STATE_PAUSED);
						phoneKtvCmd = PhoneKTVView.LYRIC_INFO_UNCHANGED;

						if (isInterrupted) {
							// 待更新以后，马上恢复到正常的情况
							isInterrupted = false;
							lastStatus = LyricPlayerStatus.PVP_ENGINE_STATE_IDLE;
						}
					}
                 // added by jiaming.wang@amusic.com for desktoplyric                     
                    if (fulllyricView != null) {
                    	fulllyricView.update(stc,getStatus() == LyricPlayerStatus.PVP_ENGINE_STATE_PAUSED);
                        phoneKtvCmd = PhoneKTVView.LYRIC_INFO_UNCHANGED;
                        if (isInterrupted) {
                            // 待更新以后，马上恢复到正常的情况
                            isInterrupted = false;
                            lastStatus = LyricPlayerStatus.PVP_ENGINE_STATE_IDLE;
                        }
                    }
				}
			}
		}
	};

    public void setLyricView(SurfaceView lyricView) {
    	if (this.lyricView != null) {
    		phoneKtvlyricView = (PhoneKTVView)lyricView;
    		if (lyricInfo != null) {
    		    if (lyricInfo.getList() != null && lyricInfo.getList().size() != 0 && mp.getCurrentPosition() > lyricInfo.getList().get(0).getStartTime()) {
                    phoneKtvlyricView.prepare(lyricInfo,true);
    		    }else {
    			    phoneKtvlyricView.prepare(lyricInfo,false);
    		    }
    		    if (!phoneKtvlyricView.getIsStarting()) {
    		    	phoneKtvlyricView.start();
    		    	seekLyricPlayer();
    		    }
    		}
    	}
        this.lyricView = lyricView;
    }
    public void clearLyricInfo() {
    	if (phoneKtvlyricView != null) {
    		phoneKtvlyricView.renderDefault();
    	}
    	
    	if (desktoplyricView != null) {
    		desktoplyricView.renderDefault();
    	}
    	lyricInfo = null;
    }

	// added by jiaming.wang@amusic.com for desktoplyric
	public void setDesktopView(SurfaceView lyricView) {
		this.desktopView = lyricView;
	}
    
    // added by jiaming.wang@amusic.com for fulllyricview
    public void setFullLyricView(SurfaceView fullView) {
    	if (this.fullView != null) {
    		if (fulllyricView != null) {
    			fulllyricView.stop();
    		}
    		fulllyricView = (FullLyricView)fullView;
    		if (lyricInfo != null) {
    		    fulllyricView.prepare(lyricInfo);
    		    if (!fulllyricView.getIsStarting()) {
    		    	fulllyricView.start();
    		    	seekLyricPlayer();
    		    }
    		}
    	}
    	this.fullView = fullView;
    }

	public boolean isStarted() {
		return isthreadRunning;
	}
	
	public PhoneKTVView getPhoneKTVView() {
		return (PhoneKTVView) lyricView;
	}
	
	public FullLyricView getFullLyricView() {
		return (FullLyricView) fullView;
	}
	
	
	public void setColor(int fontColor,int renderColor) {
		if (phoneKtvlyricView != null) {
		    phoneKtvlyricView.setColor(fontColor,renderColor);
		}
		
		if (fulllyricView != null) {
            fulllyricView.setColor(fontColor,renderColor);
		}
	}
	public void setRatio(float ratio) {
		this.ratio = ratio;
		
		float ratio_ = 1 / ratio;
		
		if (phoneKtvlyricView != null) {
		    phoneKtvlyricView.setRatio(ratio_);
		}
		
		if (desktoplyricView != null) {
		    desktoplyricView.setRatio(ratio_);
		}
        
		if (fulllyricView != null) {
            fulllyricView.setRatio(ratio_);
		}
	}
}
