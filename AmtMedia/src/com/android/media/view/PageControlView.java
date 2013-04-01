package com.android.media.view;



import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.amusic.media.R;
import com.android.media.MediaApplication;
import com.android.media.view.SkinScrollLayout.OnScreenChangeListener;




public class PageControlView extends LinearLayout {
	private Context context;

	private int count;

	public void bindScrollViewGroup(SkinScrollLayout scrollViewGroup) {
		this.count=scrollViewGroup.getChildCount();
		//System.out.println("count="+count);
		generatePageControl(scrollViewGroup.getCurrentScreenIndex());
		
		scrollViewGroup.setOnScreenChangeListener(new OnScreenChangeListener() {
			
			public void onScreenChange(int currentIndex) {
				// TODO Auto-generated method stub
				generatePageControl(currentIndex);
			}
		});
	}

	public PageControlView(Context context) {
		super(context);
		this.init(context);
	}
	public PageControlView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.init(context);
	}

	private void init(Context context) {
		this.context=context;
	}

	private void generatePageControl(int currentIndex) {
		
		int pageNum = 4; // 显示多少个 
		int pageNo = currentIndex +1; //第几页
		int pageSum = this.count; //总共多少页
		MediaApplication.logD(PageControlView.class, "pageNo:" + pageNo +"/" + pageSum);
		if(pageNo > pageSum) {
			return;
		} 
		this.removeAllViews();
		if(pageSum>1){
			int currentNum = (pageNo % pageNum == 0 ? (pageNo / pageNum) - 1  
	                 : (int) (pageNo / pageNum))   
	                 * pageNum; 
			
			 if (currentNum < 0)   
	             currentNum = 0;   
			 
			 if (pageNo > pageNum){
				 ImageView imageView = new ImageView(context);
				 imageView.setImageResource(R.drawable.zuo);
				 this.addView(imageView);
			 }
			 
			 
			 
			 for (int i = 0; i < pageNum; i++) {   
	             if ((currentNum + i + 1) > pageSum || pageSum < 2)   {
	            	 MediaApplication.logD(PageControlView.class, "currentNum:" + (currentNum + i + 1));
	            	 break;   
	             }
	             
	             ImageView imageView = new ImageView(context);
	             if(currentNum + i + 1 == pageNo){
	            	 imageView.setImageResource(R.drawable.page_indicator_focused);
	             }else{
	            	 imageView.setImageResource(R.drawable.page_indicator);
	             }
	             this.addView(imageView);
	         }   
			 
			 if (pageSum > (currentNum + pageNum)) {
				 ImageView imageView = new ImageView(context);
				 imageView.setImageResource(R.drawable.you);
				 this.addView(imageView);
			 }
		}
	}
}

