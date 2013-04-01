package com.android.media.screens.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

import com.amusic.media.R;
import com.android.media.MediaApplication;
import com.android.media.adapter.SkinDownloadAdapter;
import com.android.media.adapter.SkinDownloadAdapter.SkinItem;
import com.android.media.dialog.DialogCheckSignature;
import com.android.media.download.DownloadJob;
import com.android.media.event.IMediaEventArgs;
import com.android.media.event.impl.MediaEventArgs;
import com.android.media.provider.MediaDatabaseHelper;
import com.android.media.screens.AmtScreen;
import com.android.media.services.IMediaService;
import com.android.media.services.impl.ServiceManager;
import com.android.media.task.DownloadTask;
import com.android.media.utils.VersionUtil;
import com.android.media.view.PageControlView;
import com.android.media.view.SkinScrollLayout;
import com.android.media.view.SkinScrollLayout.OnScreenChangeListenerDataLoad;

public class ScreenSkinWebView extends AmtScreen {
	private SkinScrollLayout mScrollLayout;
	private static final float APP_PAGE_SIZE = 6.0f;
	private Context mContext;
	private PageControlView pageControl;
	public MyHandler myHandler;
	private Handler handler;
	public int n=0;
	private DataLoading dataLoad;
	private boolean flag = false;
	private DialogCheckSignature mDialogCheckSignature;
	private final  String SKIN_URL = IMediaService.DOWNLOAD_SERVER_BASE + "skin.cfg";
	private final int DOWNLOAD_THUMB_SKIN_JSON_OK = 0;
	private final int DOWNLOAD_THUMB_SKIN_JSON_ERROR = -1;
	private final int DOWNLOAD_ALL_SKIN_OK = 1;
	private final int DOWNLOAD_ALL_SKIN_ERROR = 2;
	private int skinNum;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.screen_skin_more);
		dataLoad = new DataLoading();
		mScrollLayout = (SkinScrollLayout)findViewById(R.id.ScrollLayoutTest);
		pageControl = (PageControlView) findViewById(R.id.pageControl);
		myHandler = new MyHandler(this,1);
		Cursor cursorCount = db.querySkins();
		skinNum = cursorCount.getCount();
		cursorCount.close();
		handler = new DownloadHandler();
		//起一个线程更新数据
		MyThread m = new MyThread();
		new Thread(m).start();
	} 
	
	/**
	 * gridView 的onItemLick响应事件
	 */
	public OnItemClickListener listener = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> arg0, View view, int arg2,
				long arg3) {
			SkinDownloadAdapter.SkinItem skinItem = (SkinItem) view.getTag(R.layout.screen_skin_item);
			File file = new File(MediaApplication.skinPath + skinItem.allName + ".pf");
			String savePath = MediaApplication.skinPath + skinItem.allName + ".tmp";
			if (!file.exists()) {
				if(!MediaApplication.getInstance().getDownloadTaskList().contains(savePath)) {
					IMediaEventArgs args = new MediaEventArgs();
					DownloadJob mJob = new DownloadJob(args);
					mJob.setDownloadUrl(skinItem.allDownloadUrl);
					mJob.setDownloadStartPos(0);
					mJob.setPath(savePath);
					mJob.setFinalPath(MediaApplication.skinPath + skinItem.allName + ".pf");
					DownloadTask mDownloadTask = new DownloadTask(mJob);
					mDownloadTask.registerHandler(new DownloadHandler(skinItem));
					mJob.setDownloadTask(mDownloadTask);
					mDownloadTask.execute(IMediaService.DOWNLOAD_START_ON_SKIN);
					skinItem.loadingView.setVisibility(View.VISIBLE);
				}
			}
		}
	};
	// 更新后台数据
	class MyThread implements Runnable {
		public void run() {
			String msglist = "1";
			Message msg = new Message();
			Bundle b = new Bundle();// 存放数据
			b.putString("rmsg", msglist);
			msg.setData(b);
			ScreenSkinWebView.this.myHandler.sendMessage(msg); // 向Handler发送消息,更新UI

		}
	}

	class MyHandler extends Handler {
		private ScreenSkinWebView mContext;
		public MyHandler(Context conn,int a) {
			mContext = (ScreenSkinWebView)conn;
		}

		public MyHandler(Looper L) {
			super(L);
		}

		// 子类必须重写此方法,接受数据
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			Bundle b = msg.getData();
			String rmsg = b.getString("rmsg");
			if ("1".equals(rmsg)) {
				 cursor = db.querySkins(n);
			     startManagingCursor(cursor);
			     if (cursor == null || cursor.getCount() == 0) {
			    	 if(n !=0 || skinNum != 0){
			    		 Toast.makeText(ScreenSkinWebView.this, getString(R.string.screen_skin_last_one), Toast.LENGTH_SHORT).show();
			    		 return;
			    	 } else {
			    		 flag = true;
			    		 mDialogCheckSignature = new DialogCheckSignature(getString(R.string.screen_skin_more), getString(R.string.screen_skin_downloading));
			    		 mDialogCheckSignature.show();
				         ParseSkinJson(); 
			    	 }
			     } else {
			    	 flag = false;
			        List<Map> list = new ArrayList<Map>();
			        for (cursor.moveToFirst(); !cursor.isAfterLast(); 
			        cursor.moveToNext()) {
			        	 n++;
			        	 Map map = new HashMap();
					     map.put("displayName", cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SKINS_DISPALYNAME)));
					     map.put("thumbnailName", cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SKINS_THUMBNAIL_FILENAME)));
					     map.put("allName", cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SKINS_ALL_FILENAME)));
					     list.add(map);
			        }
			        int pageNo = (int)Math.ceil( cursor.getCount()/APP_PAGE_SIZE);
					for (int i = 0; i < pageNo; i++) {
						GridView appPage = new GridView(mContext);
						// get the "i" page data
						appPage.setAdapter(new SkinDownloadAdapter(mContext, list, i));
						appPage.setNumColumns(3);
						appPage.setGravity(Gravity.CENTER);
						appPage.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
						appPage.setVerticalSpacing(10);
						appPage.setHorizontalSpacing(10);
						appPage.setColumnWidth(90);
						appPage.setOnItemClickListener(listener);
						appPage.setSelector(new ColorDrawable(Color.TRANSPARENT));
						mScrollLayout.addView(appPage);
					}
					//加载分页
					pageControl.bindScrollViewGroup(mScrollLayout);
					//加载分页数据
					dataLoad.bindScrollViewGroup(mScrollLayout);
			     }
			     
				}
			}

		}
	
	
	
	public void ParseSkinJson() {
		new Thread (new Runnable(){
			@Override
			public void run() {
				String json = VersionUtil.getVerjson(SKIN_URL);
				MediaApplication.logD(ScreenSkinWebView.class, json);
				if (json == null){
					Message msg = new Message();
					msg.arg1 = DOWNLOAD_THUMB_SKIN_JSON_ERROR;
				    msg.what = DownloadTask.MESSAGE_WHAT_DOWNLOADED;
					handler.sendMessage(msg);
					return;
				}
				JSONObject jsonObject;
				try {
					jsonObject = new JSONObject(json).getJSONObject("skin");
					JSONArray jsonArray = jsonObject.getJSONArray("skinlist");
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject jsonObject2 = (JSONObject) jsonArray.opt(i);
						int skinId = Integer.parseInt(jsonObject2.getString("ID"));
						MediaApplication.logD(ScreenSkinWebView.class, "skinId:" + skinId + "  skinNum:" + skinNum);
						if(skinId > skinNum) {
							ContentValues values = new ContentValues();
							values.put(MediaDatabaseHelper.COLUMN_AMPLAY_SKINS_ID, Integer.parseInt(jsonObject2.getString("ID")));
							values.put(MediaDatabaseHelper.COLUMN_AMPLAY_SKINS_DISPALYNAME, jsonObject2.getString("description"));
							values.put(MediaDatabaseHelper.COLUMN_AMPLAY_SKINS_THUMBNAIL_FILENAME, jsonObject2.getString("thumbUrl"));
							values.put(MediaDatabaseHelper.COLUMN_AMPLAY_SKINS_ALL_FILENAME, jsonObject2.getString("trueUrl"));
							db.insertSkins(values);
						}
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
				Message msg = new Message();
				msg.arg1 = DOWNLOAD_THUMB_SKIN_JSON_OK;
			    msg.what = DownloadTask.MESSAGE_WHAT_DOWNLOADED;
				handler.sendMessage(msg);
			}
        	
        }).start();
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		ServiceManager.getAmtMedia().getGoPlayerBtn()
				.setVisibility(View.INVISIBLE);
		setScreenTitle(getString(R.string.screen_skin_more));
		if(mScrollLayout.getChildCount() == 0){
			ParseSkinJson(); 
		}
    }
	
	
	
	public class DownloadHandler extends Handler {
		private SkinDownloadAdapter.SkinItem skinItem;
		
		DownloadHandler(){}
		
		DownloadHandler(SkinDownloadAdapter.SkinItem skinItem){
			this.skinItem = skinItem;
		}
		
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(msg.what == DownloadTask.MESSAGE_WHAT_DOWNLOADED) {
				switch(msg.arg1){
				case DOWNLOAD_THUMB_SKIN_JSON_OK:
					if(flag == true) {
						MyThread m = new MyThread();
						new Thread(m).start();
						mDialogCheckSignature.dismiss();
	    			}
					break;
				case DOWNLOAD_THUMB_SKIN_JSON_ERROR:
					Toast.makeText(ScreenSkinWebView.this, getString(R.string.get_network_failed), Toast.LENGTH_SHORT).show();
					break;
				case DOWNLOAD_ALL_SKIN_OK:
					skinItem.loadingView.setVisibility(View.GONE);
					skinItem.downloadedView.setVisibility(View.VISIBLE);
					String path = msg.getData().getString("path");
					if(!MediaApplication.getInstance().getSkinsPath().contains(path))
					MediaApplication.getInstance().getSkinsPath().add(path);
					break;
				case DOWNLOAD_ALL_SKIN_ERROR:
					skinItem.loadingView.setVisibility(View.GONE);
					skinItem.downloadedView.setVisibility(View.GONE);
					break;	
				}
			}
    	}
	}
	
	
	
	
	//分页数据
	class DataLoading {
		private int count;
		public void bindScrollViewGroup(SkinScrollLayout scrollViewGroup) {
			this.count=scrollViewGroup.getChildCount();
			scrollViewGroup.setOnScreenChangeListenerDataLoad(new OnScreenChangeListenerDataLoad() {
				public void onScreenChange(int currentIndex) {
					generatePageControl(currentIndex);
				}
			});
		}
		
		private void generatePageControl(int currentIndex){
			//如果到最后一页，就加载24条记录
			if(count==currentIndex+1){
				MyThread m = new MyThread();
				new Thread(m).start();
			}
		}
	}
	
	@Override
	public boolean hasMenu() {
		// TODO Auto-generated method stub
		return true;
	}
}
