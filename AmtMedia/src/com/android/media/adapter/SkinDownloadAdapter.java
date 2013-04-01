package com.android.media.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amusic.media.R;
import com.android.media.MediaApplication;
import com.android.media.download.DownloadJob;
import com.android.media.event.IMediaEventArgs;
import com.android.media.event.impl.MediaEventArgs;
import com.android.media.services.IMediaService;
import com.android.media.task.DownloadTask;
import com.android.media.utils.BitmapCache;
@SuppressWarnings("rawtypes")
public class SkinDownloadAdapter extends BaseAdapter {
	private List<Map> mList;
	private Context mContext;
	public static final int APP_PAGE_SIZE = 6;
	

	public SkinDownloadAdapter(Context context, List<Map> list, int page) {
		mContext = context;
		mList = new ArrayList<Map>();
		int i = page * APP_PAGE_SIZE;
		int iEnd = i+APP_PAGE_SIZE;
		while ((i<list.size()) && (i<iEnd)) {
			mList.add(list.get(i));
			i++;
		}
	}
	public int getCount() {
		// TODO Auto-generated method stub
		return mList.size();
	}

	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mList.get(position);
	}

	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		Map appInfo = mList.get(position);
		final String displayName = appInfo.get("displayName").toString();
		final String thumbnailName = appInfo.get("thumbnailName").toString();
		final String allName = appInfo.get("allName").toString();
		final SkinItem skinItem;
		if (convertView == null) {
			View view = LayoutInflater.from(mContext).inflate(R.layout.screen_skin_item, null);
			skinItem = new SkinItem();
			skinItem.skinImageView = (ImageView) view.findViewById(R.id.image);
			skinItem.downloadedView = (ImageView) view.findViewById(R.id.downloaded);
			skinItem.skinDisplayNameView = (TextView) view.findViewById(R.id.title);
			skinItem.loadingView = (View) view.findViewById(R.id.loading);
			view.setTag(R.layout.screen_skin_item, skinItem);
			convertView = view;
		} else {
			skinItem = (SkinItem) convertView.getTag(R.layout.screen_skin_item);
		}
		skinItem.skinImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.screen_skin_more));
		skinItem.loadingView.setVisibility(View.GONE);
		skinItem.thumbnailDownloadUrl = IMediaService.DOWNLOAD_SERVER_BASE + thumbnailName;
		skinItem.allDownloadUrl = IMediaService.DOWNLOAD_SERVER_BASE + allName; 
		skinItem.allName = displayName;
		String savePath = MediaApplication.skinThumbnailPath + displayName + ".tmp";
		String finalPath = MediaApplication.skinThumbnailPath + displayName + ".pf";
		File thumbnailFile = new File(finalPath);
	    File allFile = new File(MediaApplication.skinPath + displayName +  ".pf");
	    Bitmap image;
	    if (BitmapCache.getInstance().getBitmapRefs().containsKey(finalPath)){
	    	image = BitmapCache.getInstance().getBitmap(finalPath);
			if(image != null) {
				MediaApplication.logD(SkinDownloadAdapter.class, "softReference.get() != null");
				skinItem.skinImageView.setImageBitmap(image);
			}
	    } else {
	    	MediaApplication.logD(SkinDownloadAdapter.class, "softReference.get() = null");
	    	if (thumbnailFile.exists()) {
				MediaApplication.logD(SkinDownloadAdapter.class, "thumbnailFile:exists:" + thumbnailFile.getName());
				image = BitmapCache.decodeBitmap(finalPath);
				skinItem.skinImageView.setImageBitmap(image);
				BitmapCache.getInstance().addCacheBitmap(image, finalPath);
			} else {
				MediaApplication.logD(SkinDownloadAdapter.class, "thumbnailFile:xxxxxxxxx:" + thumbnailFile.getName());
				final Handler handler = new Handler(){
					@Override
					public void handleMessage(Message msg) {
						if(msg.what == DownloadTask.MESSAGE_WHAT_DOWNLOADED) {
							MediaApplication.logD(SkinDownloadAdapter.class, "handleMessage:" + msg.what);
							Object obj = msg.obj;
							skinItem.loadingView.setVisibility(View.GONE);
							if(obj == null){
								skinItem.skinImageView.setImageBitmap(BitmapFactory.decodeResource(MediaApplication.getContext().getResources(), R.drawable.screen_skin_more));
							} else {
								skinItem.skinImageView.setImageBitmap((Bitmap)obj);
							}
						}
					}
				};
				if(!MediaApplication.getInstance().getDownloadTaskList().contains(savePath)) {
					IMediaEventArgs args = new MediaEventArgs();
					DownloadJob mJob = new DownloadJob(args);
					mJob.setDownloadUrl(skinItem.thumbnailDownloadUrl);
					mJob.setDownloadStartPos(0);
					mJob.setPath(savePath);
					mJob.setFinalPath(finalPath);
					DownloadTask mDownloadTask = new DownloadTask(mJob);
					mDownloadTask.registerHandler(handler);
					mJob.setDownloadTask(mDownloadTask);
					mDownloadTask.execute(IMediaService.DOWNLOAD_START_ON_SKIN);
					skinItem.loadingView.setVisibility(View.VISIBLE);
				}
		    }
	    }
		skinItem.skinDisplayNameView.setText(displayName);
		if(allFile.exists()){
			skinItem.downloadedView.setVisibility(View.VISIBLE);
		} else {
			skinItem.downloadedView.setVisibility(View.GONE);
		}
		return convertView;
	}

	public class SkinItem {
		private ImageView skinImageView;
		private TextView skinDisplayNameView;
		private String thumbnailDownloadUrl;
		public String allDownloadUrl;
		public View loadingView;
		public ImageView downloadedView;
		public String allName;
		
	}
}
