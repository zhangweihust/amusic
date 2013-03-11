package com.amusic.media.screens.impl;

import java.io.File;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;

import com.amusic.media.AmtMedia;
import com.amusic.media.MediaApplication;
import com.amusic.media.R;
import com.amusic.media.adapter.SkinAdapter;
import com.amusic.media.lyric.player.LyricPlayer;
import com.amusic.media.lyric.render.Lyric2Bmp;
import com.amusic.media.screens.AmtScreen;
import com.amusic.media.services.impl.ServiceManager;
import com.amusic.media.utils.Constant;
import com.amusic.media.utils.PicInfoUtil;
import com.amusic.media.utils.PreferencesUtil;
import com.amusic.media.view.CustomDialog;

public class ScreenSkin extends AmtScreen{
	private GridView gridView_radio;    //单选宫格
	private SkinAdapter skinAdapter;      //存储图片源的适配器(单选)
	private Drawable background;
    private SharedPreferences preferences;
    public static final String XML_NAME = "ScreenBackground";
	public static final String XML_BACKGROUND = "background";
	private String drawablePath;
	private CustomDialog dialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_skin);
		preferences = MediaApplication.getInstance().getSharedPreferences(XML_NAME,Context.MODE_WORLD_WRITEABLE);
		drawablePath = preferences.getString(XML_BACKGROUND, MediaApplication.DEFAULT_SKIN);
		 // 单选的宫格
        gridView_radio = (GridView) findViewById(R.id.gridview);
//        skinAdapter = new SkinAdapter(this, false, drawablePath);
//        gridView_radio.setAdapter(skinAdapter);
        gridView_radio.setSelector(new ColorDrawable(Color.TRANSPARENT));
        // 设置点击监听
        gridView_radio.setOnItemClickListener(new OnItemClickListener() {

			@Override
            public void onItemClick(AdapterView<?> arg0, View v,
                    int position, long arg3) {
				if(v.getTag() == null){
					amtScreenService.show(ScreenSkinWebView.class);
				} else {
					if(position == skinAdapter.getLastPosition()) {
						return;
					}
					File file = new File ((String)v.getTag());
	            	if (file.exists()) {
	            		skinAdapter.changeState(position);
	            		background = Drawable.createFromPath((String)v.getTag());
	            		PicInfoUtil pu = new PicInfoUtil((String)v.getTag());
	            		MediaApplication.logD(AmtMedia.class, preferences.getString(XML_BACKGROUND, MediaApplication.DEFAULT_SKIN));
	            		MediaApplication.logD(AmtMedia.class, "GET_TITLE:" + pu.getPicInfo(PicInfoUtil.GET_TITLE));
	    				MediaApplication.logD(AmtMedia.class, "GET_AUTHOR:" + pu.getPicInfo(PicInfoUtil.GET_AUTHOR));
	    				MediaApplication.logD(AmtMedia.class, "GET_COMMENT:" + pu.getPicInfo(PicInfoUtil.GET_COMMENT));
	    				MediaApplication.logD(AmtMedia.class, "GET_KEYWORDS:" + pu.getPicInfo(PicInfoUtil.GET_KEYWORDS));
	    				MediaApplication.logD(AmtMedia.class, "GET_SUBJECT:" + pu.getPicInfo(PicInfoUtil.GET_SUBJECT));
	    				if(pu.getPicInfo(PicInfoUtil.GET_SUBJECT) != null && !pu.getPicInfo(PicInfoUtil.GET_SUBJECT).equals("") && pu.getPicInfo(PicInfoUtil.GET_TITLE) != null && !pu.getPicInfo(PicInfoUtil.GET_TITLE).equals("")) {
	    					try{
	    						MediaApplication.color_highlight = Color.parseColor("#" + pu.getPicInfo(PicInfoUtil.GET_SUBJECT));
			    				MediaApplication.color_normal = Color.parseColor("#" + pu.getPicInfo(PicInfoUtil.GET_TITLE));
			    				MediaApplication.logD(AmtMedia.class, "color_highlight:" + MediaApplication.color_highlight);
			    				MediaApplication.logD(AmtMedia.class, "color_normal:" + MediaApplication.color_normal);
	    					} catch (Exception e) {
	    						MediaApplication.color_highlight = -16721665;
	    	    				MediaApplication.color_normal = -16777216;
	    					}
	    				}
	            	} else {
//	            		if(MediaApplication.DEFAULT_SKIN.equals((String)v.getTag())){
//	            			skinAdapter.changeState(0);
//	            		} else {
//	            			skinAdapter.changeState(position);
//	            		}
	            		skinAdapter.changeState(0);
	            		background = getResources().getDrawable(R.drawable.style_brilliant_starlight);
	            		MediaApplication.color_highlight = -16721665;
	    				MediaApplication.color_normal = -16777216;
	            	}
	            	PreferencesUtil.setSkinFontForegroundColorSP(MediaApplication.color_highlight);
	            	PreferencesUtil.setSkinFontBackgroundColorSP(MediaApplication.color_normal);
	            	MediaApplication.logD(AmtMedia.class, "setBackgroundDrawable:" + background);
	            	ServiceManager.getAmtMedia().getRootView().setBackgroundDrawable(background);
	            	preferences.edit().putString(XML_BACKGROUND, (String) v.getTag()).commit();
	            	
	            	LyricPlayer lyricplayer = ServiceManager.getMediaplayerService().getLyricplayer();
	            	if (lyricplayer.getPhoneKTVView() != null) {
	            		int fontColor = Lyric2Bmp.FONTCOLOR;// MediaApplication.col0or_normal;
	            		lyricplayer.getPhoneKTVView().setColor(fontColor,MediaApplication.color_highlight);
	            	} 
	            	
	            	if (lyricplayer.getFullLyricView() != null) {
	            		int fontColor = Lyric2Bmp.FONTCOLOR;// MediaApplication.col0or_normal;
	            		lyricplayer.getFullLyricView().setColor(fontColor,MediaApplication.color_highlight);
	            	}
	            	
	            	Constant.LYRICBACKGROUNDCOLOR = Lyric2Bmp.FONTCOLOR;
	        		Constant.LYRICFOREGROUNDCOLOR = MediaApplication.color_highlight;
	        		SharedPreferences sp = MediaApplication.getContext().getSharedPreferences("com.amusic.media_preferences",Context.MODE_WORLD_WRITEABLE);
	    			Editor editor = sp.edit();
	    			editor.putInt(Constant.SoftParametersSetting.lyric_foregroundColor_key, Constant.LYRICFOREGROUNDCOLOR);
	    			editor.putInt(Constant.SoftParametersSetting.lyric_backgroundColor_key, Constant.LYRICBACKGROUNDCOLOR);
	    			editor.commit();
				}
            }
        });
        gridView_radio.setOnItemLongClickListener(new OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View v,
					int position, long arg3) {
				if(v.getTag() != null){
					if(position != 0) {
						showDialog((String)v.getTag());
					}
				}
				return false;
			}});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		ServiceManager.getAmtMedia().getGoPlayerBtn()
				.setVisibility(View.INVISIBLE);
		setScreenTitle(getString(R.string.screen_skin_top_title));
		drawablePath = preferences.getString(XML_BACKGROUND, MediaApplication.DEFAULT_SKIN);
		skinAdapter = null;
		skinAdapter = new SkinAdapter(this, false, drawablePath);
		gridView_radio.setAdapter(skinAdapter);
		}

	
	public void showDialog(final String path)
	{
			CustomDialog.Builder customBuilder = new CustomDialog.Builder(ServiceManager.getAmtMedia());
			customBuilder.setTitle(getString(R.string.screen_scan_prompt))
			.setWhichViewVisible(CustomDialog.contentIsTextView)
			.setMessage(getString(R.string.screen_skin_delete_right_now))
			.setPositiveButton(getString(R.string.screen_scan_ok), 
	            		new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int which) {
	                	File file = new File(path);
	                	if(file.exists()) {
	                		file.delete();
	                	}
	                	MediaApplication.getInstance().getSkinsPath().remove(path);
	                	drawablePath = preferences.getString(XML_BACKGROUND, MediaApplication.DEFAULT_SKIN);
	            		skinAdapter = new SkinAdapter(ScreenSkin.this, false, drawablePath);
	            		gridView_radio.setAdapter(skinAdapter);
	                	dialog.dismiss();
	                }
	            })
	            .setNegativeButton(getString(R.string.screen_scan_cancel), 
	            		new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int which) {
	                	dialog.dismiss();
	                }
	            });
			dialog = customBuilder.create();
			dialog.show();	
		
	}
	
	@Override
	public boolean hasMenu() {
		// TODO Auto-generated method stub
		return true;
	}
}
