package com.android.media.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

import com.amusic.media.R;

public class CustomNoSeekSeekBar extends SeekBar {

	private boolean flagA = false ;
	private boolean flagB = false ;
	private int progressA, progressB;
	Bitmap bmpA, bmpB, bmpA_before, bmpB_before, bmpA_after, bmpB_after;
	Paint paint;
	private int color_after=0xFFFAC813;
	private int color_before=0xFFFFFFFF;
	private float circlePositionA_X, circlePositionA_Y, bitmapPositionA_X, bitmapPositionA_Y;
	private float circlePositionB_X, circlePositionB_Y, bitmapPositionB_X, bitmapPositionB_Y;
	
	public CustomNoSeekSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		bmpA_before = BitmapFactory.decodeResource(getResources(), R.drawable.screen_abrepeat_position_a_before);
		bmpB_before = BitmapFactory.decodeResource(getResources(), R.drawable.screen_abrepeat_position_b_before); 
		bmpA_after = BitmapFactory.decodeResource(getResources(), R.drawable.screen_abrepeat_position_a);
		bmpB_after = BitmapFactory.decodeResource(getResources(), R.drawable.screen_abrepeat_position_b); 
		bmpA = bmpA_before;
		bmpB = bmpB_before; 
        paint=new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color_before);
	}

	@Override
	protected synchronized void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(flagA == true){
			canvas.drawCircle(circlePositionA_X, circlePositionA_Y, 3, paint);
            canvas.drawBitmap(bmpA, bitmapPositionA_X, bitmapPositionA_Y, null); 
			
		}
		if (flagB == true){
			canvas.drawCircle(circlePositionB_X, circlePositionB_Y, 3, paint);
            canvas.drawBitmap(bmpB, bitmapPositionB_X, bitmapPositionB_Y, null); 
		}
		
	}
	
	public  void setFlagA( boolean flag, int progress){
		this.flagA = flag;
		this.progressA = progress;
		bmpA = bmpA_before;
		paint.setColor(color_before);
		circlePositionA_X = ((float)progressA/this.getMax())*(this.getMeasuredWidth()-this.getPaddingLeft()-this.getPaddingRight());
		circlePositionA_X = circlePositionA_X + this.getPaddingLeft();
		circlePositionA_Y = this.getPaddingTop() + (this.getHeight()-this.getPaddingTop())/2;
		bitmapPositionA_X = circlePositionA_X-bmpA.getWidth()/2;
		bitmapPositionA_Y = 0;
		this.invalidate();
	}

	public  void setFlagB( boolean flag, int progress){
		this.flagB = flag;
		this.progressB = progress;
		bmpB = bmpB_before;
		paint.setColor(color_before);
		circlePositionB_X = ((float)progressB/this.getMax())*(this.getMeasuredWidth()-this.getPaddingLeft()-this.getPaddingRight());
		circlePositionB_X = circlePositionB_X + this.getPaddingLeft();
		circlePositionB_Y = this.getPaddingTop() + (this.getHeight()-this.getPaddingTop())/2;
		bitmapPositionB_X = circlePositionB_X-bmpB.getWidth()/2;
		bitmapPositionB_Y = 0;
		this.invalidate();
	}

	public void changeButtonColor(){
		bmpA = bmpA_after;
		bmpB = bmpB_after;
		paint.setColor(color_after);
		this.invalidate();
	}
	
	public void removeFlag(){
		this.flagA = false;
		this.progressA = 0;
		this.flagB = false;
		this.progressB = 0;
		this.invalidate();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		//return super.onTouchEvent(event);
		return false;
	}
	
	
}
