package com.android.media.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.ListView;

import com.android.media.MediaApplication;
import com.android.media.services.impl.MediaPlayerService;
import com.android.media.task.DownloadTask;
import com.android.media.utils.BitmapCache;
import com.android.media.utils.ImageUtil;


public class RemoteImageView extends ImageView{
	

	public RemoteImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public RemoteImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RemoteImageView(Context context) {
		super(context);
	}

	/**
	 * Position of the image in the mListView
	 */
	private int mPosition;

	/**
	 * ListView containg this image
	 */
	private ListView mListView;
	
	/**
	 * Default image shown while loading or on url not found
	 */
	private Integer mDefaultImage;
	
	
	private String mArtist;
	
	
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == DownloadTask.MESSAGE_WHAT_DOWNLOADED) {
			Object obj = msg.obj;
			MediaApplication.logD(RemoteImageView.class, "arg1 = " +msg.arg1 +"-----arg2=" + msg.arg2 + " ----position= "+ mPosition);
			if(msg.arg2 == mPosition){
				if(obj == null){
					loadDefaultImage();
				} else {
				    RemoteImageView.this.setImageBitmap((Bitmap)obj);
				}
			} else if (msg.arg1 == 1){
				MediaApplication.logD(RemoteImageView.class, "mArtist = " + mArtist +"-----" +msg.getData().getString("name"));
				if(mArtist.equals(msg.getData().getString("name"))){
					if(obj != null) 
					RemoteImageView.this.setImageBitmap((Bitmap)obj);
			    }
			}
		}
		}
	};

	/**
	 * Loads image from remote location
	 * 
	 * @param url eg. http://random.com/abz.jpg
	 */
	public void setImageUrl(final String artist) {
		loadDefaultImage();
//		if(mListView != null)
//			if(mPosition < mListView.getFirstVisiblePosition() || mPosition > mListView.getLastVisiblePosition())
//				return;
		new Thread() {
			public void run() {
				try {
					Message msg = new Message();
					msg.arg2 = mPosition;
					msg.what = DownloadTask.MESSAGE_WHAT_DOWNLOADED;
					if (BitmapCache.getInstance().getBitmapRefs().containsKey(mArtist)) {
						Bitmap image = BitmapCache.getInstance().getBitmap(mArtist);
						if (image != null) {
							msg.obj = image;
							handler.sendMessage(msg);
							return;
						}
					}
					MediaApplication.logD(RemoteImageView.class, "handler = " + handler);
					msg.obj = ImageUtil.acquireDrawable(mArtist, handler);
					handler.sendMessage(msg);
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
		}.start();
	}
	
	/**
	 * Sets default local image shown when remote one is unavailable
	 * 
	 * @param resid
	 */
	public void setDefaultImage(Integer resid){
		mDefaultImage = resid;
	}
	
	/**
	 * Loads default image
	 */
	private void loadDefaultImage(){
		if(mDefaultImage != null)
			setImageResource(mDefaultImage);
	}
	
	/**
	 * Loads image from remote location in the ListView
	 * 
	 * @param url eg. http://random.com/abz.jpg
	 * @param position ListView position where the image is nested
	 * @param listView ListView to which this image belongs
	 */
	public void setImageUrl(String artist, int position, ListView listView){
		mPosition = position;
		mListView = listView;
		mArtist = MediaPlayerService.splitTitle(artist).replaceAll(" ", "").toLowerCase();;
		setImageUrl(artist);
	}





}
