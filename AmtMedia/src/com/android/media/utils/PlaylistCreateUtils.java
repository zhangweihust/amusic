package com.android.media.utils;

import java.io.File;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.widget.CheckBox;

import com.amusic.media.R;
import com.android.media.dialog.OnScreenHint;
import com.android.media.provider.MediaDatabaseHelper;
import com.android.media.provider.MediaManagerDB;
import com.android.media.screens.Screen;
import com.android.media.screens.impl.ScreenAudio;
import com.android.media.services.impl.ServiceManager;
import com.android.media.view.CustomDialog;

public class PlaylistCreateUtils {
	private static CheckBox checkBox;
	private static Dialog dialog;
	private static OnScreenHint mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_audio_player_mode_order));
	public static void showDelete(final int id, final Screen screen){
		final MediaManagerDB db = ServiceManager.getMediaService().getMediaDB();
		Context context = ServiceManager.getAmtMedia();
		final CustomDialog.Builder customBuilder = new CustomDialog.Builder(context);
		customBuilder.setTitle(context.getResources().getString(R.string.screen_delete_dialog))
		.setWhichViewVisible(CustomDialog.contentIsCheckBox)
		.setCheckBoxText(context.getResources().getString(R.string.screen_delete_dialog_file))
		.setPositiveButton(context.getResources().getString(R.string.screen_delete_dialog_ok), 
            		new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                	Cursor cursor = db.querySongById(id);
                	if(checkBox.isChecked()){		
    					if(cursor.moveToNext()){
    						String fileName = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_FILEPATH));
    						if (fileName !=null && fileName.equals(ServiceManager.getMediaplayerService().getMediaPlayer().getAudioFilePath()) ) {
//    							Toast.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_audio_player_cannot_delete),
//    									Toast.LENGTH_SHORT).show();
    							if(mOnScreenHint!=null){
    							    mOnScreenHint.cancel();
    							}
    							mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_audio_player_cannot_delete));
    							mOnScreenHint.show();
    							return;
    						} else {
								File file = new File(fileName);
								if(file.exists()){
									file.delete();
									ServiceManager.getMediaScanner().scanOneFile(fileName);
								} 
								cursor.close();
								db.deleteAudio(id);
    						}
    					}
                	} else {
                		if(cursor.moveToNext()){
    						String fileName = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_FILEPATH));
    						if (fileName !=null && fileName.equals(ServiceManager.getMediaplayerService().getMediaPlayer().getAudioFilePath()) ) {
    							if(mOnScreenHint!=null){
    							    mOnScreenHint.cancel();
    							}
    							mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_audio_player_cannot_delete));
    							mOnScreenHint.show();
    							return;
    						} else {
    							db.deleteAudio(id);
    						}
    					}
                	}
                	
                	dialog.dismiss();               	
                	screen.refresh();
                	ScreenAudio.refreshCount(ScreenAudio.REFRESH_FAVOURITES_COUNT);
                	ScreenAudio.refreshCount(ScreenAudio.REFRESH_SONGS_COUNT);
                	mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_audio_player_delete_song_success));
					mOnScreenHint.show();
                }
            }).setNegativeButton(context.getResources().getString(R.string.screen_delete_dialog_cancel), 
            		new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                	dialog.dismiss();
                }
            });
		dialog = customBuilder.create();
		checkBox = customBuilder.getmCheckBox();
		dialog.show();
		
	} 
}
