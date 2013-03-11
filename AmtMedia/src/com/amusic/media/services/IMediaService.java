package com.amusic.media.services;

import java.util.List;
import java.util.Map;

import com.amusic.media.download.DownloadJob;
import com.amusic.media.event.IMediaEventArgs;
import com.amusic.media.provider.MediaManagerDB;
import com.amusic.media.provider.MediaScanner;

public interface IMediaService extends IService {
	public static final String LYRICS_SUFFIX = ".ksc";
	public static final String PACKAGE_SUFFIX = ".apk";
	public static final String AUDIO_SUFFIX = ".mp3";
	public static final String ACCOMPANY_SUFFIX = ".bz";
	public static final String TMP_SUFFIX = ".tmp";
	public static final String PICTURE_SUFFIX = ".tp";
	public static final long PROGRESS_UPDATE_INTERVAL = 4000;
	public static final long TIME_HEX = 1000;

	public static final String PROGRESS_FINISHED = "100%";
	public static final String PROGRESS_WAIT = "0%";

	public static final int STATE_DEFAULT = 0;
	public static final int STATE_WAIT = 101;
	public static final int STATE_BEGIN = 102;
	public static final int STATE_PAUSE = 103;
	public static final int STATE_CANCEL = 104;
	public static final int STATE_FINISHED = 105;
	public static final int STATE_ORIGINAL_FINISHED = 106;
	public static final int STATE_ALL_FINISHED = 107;
	public static final int STATE_IN_QUEUE = 108;

	public static final int MSG_WHAT_CHARACTER = 201;
	public static final int MSG_WHAT_DELETE = 202;
	public static final int MSG_WHAT_WILD=203;
	public static final int MSG_WHAT_DELETE_LONGCLICK=204;
	
	public static final int MSG_WHAT_UPDATE = 401;

//	public static final String DOWNLOAD_SERVER_BASE = "http://10.51.15.33/";
	public static final String DOWNLOAD_SERVER_BASE = "http://player.archermind.com/";//"http://219.138.163.58/";
	public static final String SEND_AUDIO_TOP10_URL = DOWNLOAD_SERVER_BASE + "ci/index.php/ktvphone/setTopSongs";
	public static final String RERIVER_AUDIO_TOP10_URL = DOWNLOAD_SERVER_BASE + "ci/index.php/ktvphone/getTopSongs";
	public static final String SEND_KMEDIA_TOP10_URL = DOWNLOAD_SERVER_BASE + "ci/index.php/ktvphone/setKTop";
	public static final String RERIVER_KMEDIA_TOP10_URL = DOWNLOAD_SERVER_BASE + "ci/index.php/ktvphone/getKTop";
	public static final String USER_ACTIVITY_INFORMATION_SERVER = DOWNLOAD_SERVER_BASE + "ci/index.php/ktvphone/setUserActionInfo";
	public static final String USER_INFORMATION_SERVER = DOWNLOAD_SERVER_BASE + "ci/index.php/ktvphone/setClientInfo";
	public static final String DOWNLOAD_LYRICS_SERVER_ACTION = "ci/index.php/ktvphone/getSongInfo";
	public static final String DOWNLOAD_PICTURE_SERVER_ACTION = "ci/index.php/ktvphone/getSingerImage";
	public static final String SEND_SUGGESTION_FEEDBACK_URL = DOWNLOAD_SERVER_BASE + "ci/index.php/ktvphone/suggestionfeedback";
	public static final String CRASH_UPLOAD_SERVER_URL = DOWNLOAD_SERVER_BASE + "ci/index.php/ktvphone/sendCrashReports";
	public static final String SEND_SONG_ERROR_INFO = DOWNLOAD_SERVER_BASE + "ci/index.php/ktvphone/ErrInfos";
	public static final String GET_SONG_DOWNLOAD_URL = DOWNLOAD_SERVER_BASE + "ci/index.php/ktvphone/getSongsUrl";
	public static final String SEND_LYRICS_SERVER_ACTION = DOWNLOAD_SERVER_BASE + "ci/index.php/ktvphone/uploadKsc";
	public static final String SEND_LYRICSFILE_SERVER_ACTION = DOWNLOAD_SERVER_BASE + "ci/index.php/ktvphone/testUpload";
	public static final String DOWNLOAD_LRC_LYRICS_SERVER_ACTION = "ci/index.php/ktvphone/getLrcInfo";
	public static final int RESOURCE_BAIDU = 1;
	public static final int RESOURCE_SOUGOU = 2;
	public static final int RESOURCE_OK99 = 503;
	public static final int RESOURCE_5SING = 504;
	public static final int RESOURCE_SEARCH = 505;
	public static final int DOWNLOAD_WITH_ACCOMPANY = 601;
	public static final int DOWNLOAD_NOT_WITH_ACCOMPANY = 602;
	public static final int DOWNLOAD_START_ON_ZERO = 701;
	public static final int DOWNLOAD_START_ON_RANGE = 702;
	public static final int DOWNLOAD_START_ON_WEB = 703;
	public static final int DOWNLOAD_START_ON_APK = 704;
	public static final int DOWNLOAD_START_ON_IMAGE = 705;
	public static final int DOWNLOAD_START_ON_LYRICS = 706;
	public static final int DOWNLOAD_START_ON_SKIN = 707;
	public static final int DOWNLOAD_START_ON_LRC_LYRICS = 708;
	


	public MediaManagerDB getMediaDB();

	public byte[] getDmLock();

	public byte[] gethLock();

	public Map<Integer, DownloadJob> getDownloadMap();

	public List<IMediaEventArgs> getHistoryAudios();

	public int getDownloadSize();

	public void setDownloadSize(int downloadSize);

	public byte[] getDlLock();

	public MediaScanner getMediaScanner();

	public List<String> getRefreshPath();

	public void setRefreshPath();

}
