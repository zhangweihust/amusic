package com.android.media.adapter;
import java.io.File;
import java.util.Vector;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amusic.media.R;
import com.android.media.MediaApplication;
import com.android.media.services.impl.ServiceManager;
import com.android.media.utils.BitmapCache;

public class SkinAdapter extends BaseAdapter{   
    private Vector<Picture> mImageIds = new Vector<Picture>();  // 定义一个向量作为图片源
    private Vector<Boolean> mImage_bs = new Vector<Boolean>();  // 定义一个向量作为选中与否容器
    private SharedPreferences preferences;
    public static final String XML_NAME = "ScreenBackground";
	public static final String XML_BACKGROUND = "background";
    private int lastPosition = -1;      //记录上一次选中的图片位置，-1表示未选中任何图片
    private boolean multiChoose;        //表示当前适配器是否允许多选
	private LayoutInflater inflater;
    private Picture picture;
    private Bitmap image;
    public SkinAdapter(Context c, boolean isMulti, String pathString){
        multiChoose = isMulti;
        inflater = LayoutInflater.from(c);  
        preferences = MediaApplication.getInstance().getSharedPreferences(XML_NAME,Context.MODE_WORLD_WRITEABLE);
		for (int i = 0; i < MediaApplication.getInstance().getSkinsPath().size(); i++) {
			String filePath = MediaApplication.getInstance().getSkinsPath().get(i);
			File file = new File(filePath);
			if (BitmapCache.getInstance().getBitmapRefs().containsKey(filePath)){
				image = BitmapCache.getInstance().getBitmap(filePath);
				if(image != null) {
					MediaApplication.logD(SkinAdapter.class, "softReference.get() != null");
					picture = new Picture(file.getName().substring(0, file.getName().lastIndexOf(".")), image, MediaApplication.getInstance().getSkinsPath().get(i));
					mImageIds.add(picture);
				}
			}  else {
				if (file.exists()) {
					MediaApplication.logD(SkinAdapter.class, "file.exists()");
	        		image = BitmapCache.decodeBitmap(filePath);
	        		if(image == null) {
	        			MediaApplication.logD(SkinAdapter.class, "图片存在，但不能压缩");
						MediaApplication.getInstance().getSkinsPath().remove(filePath);
						i--;
	        		} else {
	        			BitmapCache.getInstance().addCacheBitmap(image, filePath);
		        		picture = new Picture(file.getName().substring(0, file.getName().lastIndexOf(".")), image, MediaApplication.getInstance().getSkinsPath().get(i));
		        		mImageIds.add(picture);
	        		}
				} else {
					if (file.getName().equals(MediaApplication.DEFAULT_SKIN)) {
						MediaApplication.logD(SkinAdapter.class, "MediaApplication.DEFAULT_SKIN()");
						picture = new Picture(file.getName().substring(0, file.getName().lastIndexOf(".")), BitmapFactory.decodeResource(c.getResources(), R.drawable.style_brilliant_starlight), MediaApplication.getInstance().getSkinsPath().get(i));
					    mImageIds.add(picture);
					} else {
						MediaApplication.logD(SkinAdapter.class, "图片不存在");
						MediaApplication.getInstance().getSkinsPath().remove(filePath);
						i--;
					}
				}
			}
		}
		
		picture = new Picture(c.getString(R.string.screen_skin_more), BitmapFactory.decodeResource(c.getResources(), R.drawable.screen_skin_more), null);
		mImageIds.add(picture);
        for(int i=0; i < MediaApplication.getInstance().getSkinsPath().size() + 1; i++)
            mImage_bs.add(false);
        int position = MediaApplication.getInstance().getSkinsPath().indexOf(pathString);
        if (position == -1) {
        	mImage_bs.setElementAt(true, 0);
        	ServiceManager.getAmtMedia().getRootView().setBackgroundDrawable(c.getResources().getDrawable(R.drawable.style_brilliant_starlight));
        	preferences.edit().putString(XML_BACKGROUND, MediaApplication.DEFAULT_SKIN).commit();
        	position = 0;
        } else {
        	 mImage_bs.setElementAt(true, position);
        }
        lastPosition = position;
    }
     
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mImageIds.size();
    }
 
    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }
 
    @Override
    public long getItemId(int position) {
         return position;
    }
 
    
    
    public int getLastPosition() {
		return lastPosition;
	}

	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	ViewHolder viewHolder; 
        if (convertView == null)
        {
        	convertView = inflater.inflate(R.layout.screen_skin_item, null); 
        	viewHolder = new ViewHolder(); 
            viewHolder.title = (TextView) convertView.findViewById(R.id.title); 
            viewHolder.image = (ImageView) convertView.findViewById(R.id.image); 
            viewHolder.selectedImage = (ImageView) convertView.findViewById(R.id.selected); 
            convertView.setTag(R.layout.screen_skin_item,viewHolder); 
        }
        else
        {
        	viewHolder = (ViewHolder) convertView.getTag(R.layout.screen_skin_item); 
        }
        viewHolder.title.setText(mImageIds.elementAt(position).getTitle()); 
        viewHolder.image.setImageBitmap(mImageIds.elementAt(position).getImage());
        if(mImage_bs.elementAt(position)) {
        	viewHolder.selectedImage.setVisibility(View.VISIBLE);
        } else {
        	viewHolder.selectedImage.setVisibility(View.GONE);
        }
        convertView.setTag(mImageIds.elementAt(position).getPath());
        return convertView;
    }
 
    // 修改选中的状态
    public void changeState(int position){
        // 多选时
        if(multiChoose == true){   
            mImage_bs.setElementAt(!mImage_bs.elementAt(position), position);   //直接取反即可   
        }
        // 单选时
        else{                      
            if(lastPosition != -1)
                mImage_bs.setElementAt(false, lastPosition);    //取消上一次的选中状态
            mImage_bs.setElementAt(!mImage_bs.elementAt(position), position);   //直接取反即可
            lastPosition = position;        //记录本次选中的位置
        }
        notifyDataSetChanged();     //通知适配器进行更新
    }
    
    class ViewHolder 
    { 
        public TextView title; 
        public ImageView image; 
        public ImageView selectedImage;
    } 
    
    class Picture 
    { 
        private String title; 
        private Bitmap image;
        private String path;
        
        public Picture() 
        { 
            super(); 
        } 
        
        public Picture(String title, Bitmap image, String path) 
        { 
            super(); 
            this.title = title; 
            this.image = image; 
            this.path = path;
        } 
     
        public String getTitle() 
        { 
            return title; 
        } 
     
        public void setTitle(String title) 
        { 
            this.title = title; 
        } 
     
        public Bitmap getImage() 
        { 
            return image; 
        } 
     
        public void setImageId(Bitmap image) 
        { 
            this.image = image; 
        }

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		} 
    } 
    
    
}
