package com.android.media.adapter;

import java.io.File;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CursorAdapter;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import android.widget.Toast;

import com.amusic.media.R;
import com.android.media.MediaApplication;
import com.android.media.dialog.DialogEditLyric;
import com.android.media.dialog.DialogSelectPlaylist;
import com.android.media.dialog.DialogSongAttributes;
import com.android.media.dialog.OnScreenHint;
import com.android.media.event.impl.MediaEventArgs;
import com.android.media.event.impl.MediaEventTypes;
import com.android.media.provider.MediaDatabaseHelper;
import com.android.media.provider.MediaManagerDB;
import com.android.media.screens.Screen;
import com.android.media.screens.impl.ScreenAudio;
import com.android.media.services.impl.ServiceManager;
import com.android.media.view.CustomDialog;

public class AudioFavoriteAdapter extends CursorAdapter implements OnItemClickListener {
	private Dialog dialog;
	private CheckBox checkBox;
	private final LayoutInflater inflater;
	private final MediaManagerDB db;
	private Screen screen;
	private DialogSelectPlaylist dialogSelectPlaylist;
	private DialogSongAttributes dialogSongAttributes;
	private DialogEditLyric dialogEditLyric;
	private Integer audioId;
	private OnScreenHint mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_audio_player_mode_order));;

	public AudioFavoriteAdapter(Context context, Cursor c, Screen screen) {
		super(context, c);
		inflater = LayoutInflater.from(context);
		db = ServiceManager.getMediaService().getMediaDB();
		this.screen = screen;
		dialogSelectPlaylist = new DialogSelectPlaylist(screen);
		dialogSelectPlaylist.registerOnItemClickListener(this);
		dialogSongAttributes = new DialogSongAttributes(screen);
		dialogEditLyric = new DialogEditLyric(screen);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.screen_audio_favorite_item, null);
		AudioFavoriteSong favoriteSong = new AudioFavoriteSong();
		// favoriteSong.playing = (ImageView) view.findViewById(R.id.screen_audio_playing);
		// favoriteSong.item = (LinearLayout) view.findViewById(R.id.screen_audio_favorite_song_item);
		favoriteSong.number = (TextView) view.findViewById(R.id.screen_audio_favorite_song_number);
		favoriteSong.error = (View) view.findViewById(R.id.screen_audio_favorite_song_error);
		favoriteSong.song = (TextView) view.findViewById(R.id.screen_audio_favorite_song_name);
		favoriteSong.duration=(TextView) view.findViewById(R.id.screen_audio_favorite_song_duration);
		//favoriteSong.singer = (TextView) view.findViewById(R.id.screen_audio_favorite_singer);
		favoriteSong.options = (Button) view.findViewById(R.id.screen_audio_favorite_song_options);
		View popView = inflater.inflate(R.layout.screen_audio_favorite_options, null);
		favoriteSong.optionsWindow = new PopupWindow(popView, ServiceManager.getAmtMedia().getResources().getDimensionPixelSize(R.dimen.popwindow_item_width), LayoutParams.WRAP_CONTENT);
		favoriteSong.optionsWindow.setFocusable(true);
		favoriteSong.optionsWindow.setBackgroundDrawable(new BitmapDrawable());
		favoriteSong.popSong = (TextView) popView.findViewById(R.id.screen_audio_favorite_options_song);
		favoriteSong.favorite = (CheckBox) popView.findViewById(R.id.screen_audio_favorite_options_favorite);
		favoriteSong.addTo = (Button) popView.findViewById(R.id.screen_audio_favorite_options_addto);
//		favoriteSong.ringtones = (Button) popView.findViewById(R.id.screen_audio_favorite_options_ringtones);
		favoriteSong.remove = (Button) popView.findViewById(R.id.screen_audio_favorite_options_remove);
		favoriteSong.attribute = (Button) popView.findViewById(R.id.screen_audio_favorite_options_attributes);
		favoriteSong.makeLyric = (Button) popView.findViewById(R.id.screen_audio_favorite_options_makeLyric);
		view.setTag(favoriteSong);
		return view;
	}

	@Override
	public void bindView(View view, final Context context, Cursor cursor) {
		final AudioFavoriteSong favoriteSong = (AudioFavoriteSong) view.getTag();
		if (cursor.getPosition() % 2 == 0) {
			// favoriteSong.item.setBackgroundDrawable(null);
		} else {
			// favoriteSong.item.setBackgroundResource(R.drawable.screen_audio_playlist_item_bg);
		}
		audioId = screen.getHighlightId();
		final int id = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ID));
		final String songName = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SKINS_DISPALYNAME));
		final String songPath = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_FILEPATH));
		if (audioId != null && audioId == id) {
			favoriteSong.song.setTextColor(MediaApplication.color_highlight);
			favoriteSong.duration.setTextColor(MediaApplication.color_highlight);
			screen.setHighlightScreenAudioId(ScreenAudio.SCREEN_AUDIO_FAVORITES);
		} else {
			favoriteSong.song.setTextColor(Color.WHITE);
			favoriteSong.duration.setTextColor(Color.WHITE);
		}
		view.setTag(R.layout.screen_audio, cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_FILEPATH)));
		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.screen_audio_favorite_song_options:
					int[] location = new int[2];
					v.getLocationInWindow(location);
					int xoff = favoriteSong.optionsWindow.getWidth()/2 + ServiceManager.getAmtMedia().getResources().getDimensionPixelSize(R.dimen.popwindow_item_maginLeft);
					int yoff = ServiceManager.getAmtMedia().getResources().getDimensionPixelSize(R.dimen.popwindow_item_maginTop);
					int height = ServiceManager.getAmtMedia().getResources().getDimensionPixelSize(R.dimen.popwindow_item_height);
					if(location[1] + 5 * height > MediaApplication.getSoftInScreenHeight()){
						yoff = 5 * height;
					}
					favoriteSong.options.setBackgroundResource(R.drawable.screen_audio_song_options_open_large);
					favoriteSong.optionsWindow.showAsDropDown(v, -xoff, -yoff);
					break;
				case R.id.screen_audio_favorite_options_addto:
					favoriteSong.optionsWindow.dismiss();
					dialogSelectPlaylist.show(id);
					break;
				case R.id.screen_audio_favorite_options_remove:
					favoriteSong.optionsWindow.dismiss();
//					PlaylistCreateUtils.showDelete(id, screen);
					showDelete(id);
					break;
				case R.id.screen_audio_favorite_options_attributes:
					favoriteSong.optionsWindow.dismiss();
					dialogSongAttributes.show(id);
					break;
				case R.id.screen_audio_favorite_options_makeLyric:
					favoriteSong.optionsWindow.dismiss();
					dialogEditLyric.setSongName(songName);
					dialogEditLyric.setSongPath(songPath);
					dialogEditLyric.show();
					break;
//				case R.id.screen_audio_favorite_options_ringtones:
//					RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, ringtoneUri);
//					favoriteSong.optionsWindow.dismiss();
//					break;
				}
			}
		};
		
		favoriteSong.favorite.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!isChecked) {
					db.deleteAudioFromFavority(id);
                	ServiceManager.getMediaEventService().onMediaUpdateEvent(new MediaEventArgs()
					.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_UPDATE_FAVORITE_COUNT));
				}
				favoriteSong.optionsWindow.dismiss();
				screen.refresh();
			}
		});
		favoriteSong.optionsWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				favoriteSong.options.setBackgroundResource(R.drawable.screen_audio_song_options_large);
			}
		});
		String name = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_DISPALYNAME));
		favoriteSong.popSong.setText(name.substring(0, name.lastIndexOf(".")));
		favoriteSong.song.setText(name.substring(0, name.lastIndexOf(".")));
		int duration = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_DURATION));
		favoriteSong.duration.setText(formatTime(duration));
        int number = cursor.getPosition();
        String path = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_FILEPATH));
        File f = new File(path);
		if(!f.exists()){
			favoriteSong.number.setVisibility(View.GONE);
			favoriteSong.error.setVisibility(View.VISIBLE);
		} else {
			favoriteSong.error.setVisibility(View.GONE);
			favoriteSong.number.setVisibility(View.VISIBLE);
			favoriteSong.number.setText(String.valueOf(number+1));
		}
        //favoriteSong.singer.setText(cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ARTISTNAME)));
		favoriteSong.options.setOnClickListener(listener);
		favoriteSong.addTo.setOnClickListener(listener);
		favoriteSong.remove.setOnClickListener(listener);
		favoriteSong.attribute.setOnClickListener(listener);
		favoriteSong.makeLyric.setOnClickListener(listener);
//		favoriteSong.ringtones.setOnClickListener(listener);

	}
	private static String formatTime(int times) {
		times /= 1000;
		int minutes = times / 60;
		int seconds = times % 60;
		minutes %= 60;
		return String.format("%02d:%02d", minutes, seconds);
		//return MessageFormat.format("{1,number,00}:{2,number,00}",  time / 1000 / 60 % 60, time / 1000 % 60);
	}
	
	public void showDelete(final int id){
		final MediaManagerDB db = ServiceManager.getMediaService().getMediaDB();
		final CustomDialog.Builder customBuilder = new CustomDialog.Builder(ServiceManager.getAmtMedia());
		customBuilder.setTitle(screen.getString(R.string.screen_delete_dialog))
		.setWhichViewVisible(CustomDialog.contentIsCheckBox)
		.setCheckBoxText(screen.getString(R.string.screen_delete_dialog_file))
		.setPositiveButton(screen.getString(R.string.screen_delete_dialog_ok), 
            		new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                	Cursor cursor = db.querySongById(id);
                	if(checkBox.isChecked()){  					
    					if(cursor.moveToNext()){
    						String filePath = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_FILEPATH));
    						if (filePath !=null && filePath.equals(ServiceManager.getMediaplayerService().getMediaPlayer().getAudioFilePath()) ) {
    							ServiceManager.getAmtMediaHandler().post(new Runnable() {
    								@Override
    								public void run() {
    									if(mOnScreenHint!=null){
    									    mOnScreenHint.cancel();
    									}
    									mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_audio_player_cannot_delete));
    									mOnScreenHint.show();
    								}
    							});
//    							mOnScreenHint.cancel();
//    							mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_audio_player_cannot_delete));
//    							mOnScreenHint.show();
    							return;
    						}else{
    							File file = new File(filePath);
        						if(file.exists()){
        							file.delete();
        							ServiceManager.getMediaScanner().scanOneFile(filePath);
        						}
        						cursor.close();
        						db.deleteAudio(id);    							
    						}
    					}
    			}else{
                    	if(cursor.moveToNext()){
        						String fileName = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_FILEPATH));
        						if (fileName !=null && fileName.equals(ServiceManager.getMediaplayerService().getMediaPlayer().getAudioFilePath()) ) {
        							ServiceManager.getAmtMediaHandler().post(new Runnable() {
        								@Override
        								public void run() {
        									if(mOnScreenHint!=null){
        									    mOnScreenHint.cancel();
        									}
        									mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_audio_player_cannot_delete));
        									mOnScreenHint.show();
        								}
        							});
//        							mOnScreenHint.cancel();
//        							mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_audio_player_cannot_delete));
//        							mOnScreenHint.show();
        							return;
        				} else {
        					      db.deleteAudioFromFavority(id);
        						}
        					}                   	   					
    					}
                	dialog.dismiss();
                	screen.refresh();
                	ScreenAudio.refreshCount(ScreenAudio.REFRESH_FAVOURITES_COUNT);
                	ScreenAudio.refreshCount(ScreenAudio.REFRESH_SONGS_COUNT);
					ServiceManager.getAmtMediaHandler().post(new Runnable() {
						@Override
						public void run() {
							if(mOnScreenHint!=null){
							    mOnScreenHint.cancel();
							}
							mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_audio_player_delete_song_success));
							mOnScreenHint.show();
						}
					});
//                	mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_audio_player_delete_song_success));
//					mOnScreenHint.show();
                }
            }).setNegativeButton(screen.getString(R.string.screen_delete_dialog_cancel), 
            		new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                	dialog.dismiss();
                }
            });
		dialog = customBuilder.create();
		checkBox = customBuilder.getmCheckBox();
		dialog.show();
	} 
	private class AudioFavoriteSong {
		private TextView number;
		private View error;
		private TextView song;
		private TextView duration;
		//private TextView singer;
		private Button options;
		private PopupWindow optionsWindow;
		private Button addTo;
//		private Button ringtones;
		private Button remove;
		private Button attribute;
		private Button makeLyric;
		private TextView popSong;
		private CheckBox favorite;
		// private LinearLayout item;
		// private ImageView playing;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Cursor cursor = db.queryPlaylistAudios((int) id);
		int auId=dialogSelectPlaylist.getAudioId();
		boolean bool = false; // 该歌曲是否已经存在该列表中，如果存在，返回true 反之，返回false
		 while (cursor.moveToNext()) {
			int systemId = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_SID));
			if (systemId == auId) {
				bool = true;
				break;
			}
		}
		if (!bool) {
			ContentValues values = new ContentValues();
			values.put(MediaDatabaseHelper.COLUMN_AUDIO_PLAYLIST_ID, id);
			values.put(MediaDatabaseHelper.COLUMN_AUDIO_SYSTEM_ID,
					dialogSelectPlaylist.getAudioId());
			db.addAudioToPlaylist(values);
			Toast.makeText(screen, screen.getString(R.string.screen_create_new_addto_success),Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(screen, screen.getString(R.string.screen_create_new_playlist_exist),Toast.LENGTH_LONG).show();
		}
		dialogSelectPlaylist.dismiss();
	}
	

	@Override
	public void changeCursor(Cursor cursor) {
		audioId = screen.getHighlightId();
		super.changeCursor(cursor);
	}
}
