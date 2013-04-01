package com.android.media.view;



import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.view.View;
import android.widget.LinearLayout;

public class WaveFormView extends View{
    private Paint mPaint=null;
    private LinearLayout mlayout;
    private double[] y;
    double[] ax=new double[4],ay=new double[4];
	double[] bx=new double[4],by=new double[4];
	double[] x=new double[5];
	double w=4.0;
	double[][] points=new double[1024][2];
	private int screenwidth;
	
	public WaveFormView(Context context,LinearLayout layout,int width) {
		super(context);
		// TODO Auto-generated constructor stub
		mPaint=new Paint();
		this.mlayout=layout;
		this.y=new double[5];
		screenwidth=width;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Style.STROKE);
		mPaint.setColor(Color.WHITE);
		
		int centerY=mlayout.getHeight()/2-4;
		int centerX=screenwidth/2;
		if(screenwidth<=240){
			centerX=screenwidth/2-10;
		}else{
			centerX=screenwidth/2-20;
		}
		
		double t1 = mlayout.getWidth()/6;
		double t2 = (mlayout.getHeight()/2)*7/12/15;
	
		x[0]=centerX-2*t1;
		x[1]=centerX-1*t1;
		x[2]=centerX;
		x[3]=centerX+1*t1;
		x[4]=centerX+2*t1;
		
		ax[0]=x[0]+(x[1]-x[0])/w;
		ay[0]=y[0]+(y[1]-y[0])/w;
		bx[0]=x[1]-(x[2]-x[0])/w;
		by[0]=y[1]-(y[2]-y[0])/w;
		
		ax[1]=x[1]+(x[2]-x[0])/w;
		ay[1]=y[1]+(y[2]-y[0])/w;
		bx[1]=x[2]-(x[3]-x[1])/w;
		by[1]=y[2]-(y[3]-y[1])/w;
		
		ax[2]=x[2]+(x[3]-x[1])/w;
		ay[2]=y[2]+(y[3]-y[1])/w;
		bx[2]=x[3]-(x[4]-x[2])/w;
		by[2]=y[3]-(y[4]-y[2])/w;
		
		ax[3]=x[3]+(x[4]-x[2])/w;
		ay[3]=y[3]+(y[4]-y[2])/w;
		bx[3]=x[4]-(x[4]-x[3])/w;
		by[3]=y[4]-(y[4]-y[3])/w;
		
		int a=0;
		for(int i=0;i<ax.length;i++){
			 for(double t=0;t<=1;t+=0.005){
		    	   points[a][0]=(1-t)*(1-t)*(1-t)*x[i]+3*t*(1-t)*(1-t)*ax[i]+
			               3*t*t*(1-t)*bx[i]+t*t*t*x[i+1];
		    	   points[a][1]=(1-t)*(1-t)*(1-t)*y[i]+3*t*(1-t)*(1-t)*ay[i]+
	                       3*t*t*(1-t)*by[i]+t*t*t*y[i+1];
		    	   a++;
		       }			
		}
		
	   for(int i=0;i<points.length;i++){
		   if(points[i][0]<20){
			 continue;			   
		   }
		   canvas.drawPoint((float)points[i][0], (float)(centerY-t2*points[i][1]),mPaint);
	   }
      
	}
	
   public void sety(double[] y){
		
		this.y=y;
	
	}
   
   public double min(double a,double b){
	   if(a>=b){
		   return b;
	   }else{
		   return a;
	   }
   }
   public double abs(double a){
	   if(a>=0){
		   return a;
	   }else{
		   
		   return a*(-1.0);
	   }
   }

}
