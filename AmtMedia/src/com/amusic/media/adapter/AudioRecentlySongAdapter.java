package com.amusic.media.adapter;

import java.io.File;

import android.content.ContentValues;
import android.content.Context;
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

import com.amusic.media.MediaApplication;
import com.amusic.media.R;
import com.amusic.media.dialog.DialogEditLyric;
import com.amusic.media.dialog.DialogSelectPlaylist;
import com.amusic.media.dialog.DialogSongAttributes;
import com.amusic.media.provider.MediaDatabaseHelper;
import com.amusic.media.provider.MediaManagerDB;
import com.amusic.media.screens.Screen;
import com.amusic.media.screens.impl.ScreenAudio;
import com.amusic.media.services.impl.ServiceManager;
import com.amusic.media.utils.PlaylistCreateUtils;

public class AudioRecentlySongAdapter extends CursorAdapter implements OnItemClickListener {

	private LayoutInflater inflater;
	private MediaManagerDB db;
	private ContentValues values = new ContentValues();
	private DialogSelectPlaylist dialogSelectPlaylist;
	private DialogSongAttributes dialogSongAttributes;
	private DialogEditLyric dialogEditLyric;
	private Screen screen;
	private Integer audioId;
	

	public AudioRecentlySongAdapter(Context context, Cursor c, Screen screen) {
		super(context, c);
		inflater = LayoutInflater.from(context);
		db = ServiceManager.getMediaService().getMediaDB();
		dialogSelectPlaylist = new DialogSelectPlaylist(screen);
		dialogSelectPlaylist.registerOnItemClickListener(this);
		dialogSongAttributes = new DialogSongAttributes(screen);
		dialogEditLyric = new DialogEditLyric(screen);
		this.screen = screen;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.screen_audio_recently_song_item, null);
		AudioRecentlySong recentlySong = new AudioRecentlySong();
		recentlySong.number = (TextView) view.findViewById(R.id.screen_audio_recently_song_number);
		recentlySong.error = (View) view.findViewById(R.id.screen_audio_recently_song_error);
		recentlySong.song = (TextView) view.findViewById(R.id.screen_audio_recently_song_name);
		recentlySong.duration = (TextView) view.findViewById(R.id.screen_audio_recently_song_duration);
		recentlySong.options = (Button) view.findViewById(R.id.screen_audio_recently_song_options);
		View popView = inflater.inflate(R.layout.screen_audio_song_options, null);
		recentlySong.optionsWindow = new PopupWindow(popView, ServiceManager.getAmtMedia().getResources().getDimensionPixelSize(R.dimen.popwindow_item_width), LayoutParams.WRAP_CONTENT);
		recentlySong.optionsWindow.setFocusable(true);
		recentlySong.optionsWindow.setBackgroundDrawable(new BitmapDrawable());
		recentlySong.popSong = (TextView) popView.findViewById(R.id.screen_audio_song_options_song);
		recentlySong.addTo = (Button) popView.findViewById(R.id.screen_audio_song_options_addto);
		recentlySong.ringtones = (Button) popView.findViewById(R.id.screen_audio_song_options_ringtones);
		recentlySong.attributes = (Button) popView.findViewById(R.id.screen_audio_song_options_attributes);
		recentlySong.makeLyric = (Button) popView.findViewById(R.id.screen_audio_song_options_makeLyric);
		recentlySong.delete = (Button) popView.findViewById(R.id.screen_audio_song_options_delete);
		recentlySong.favorite = (CheckBox) popView.findViewById(R.id.screen_audio_song_options_favorite);
		view.setTag(recentlySong);
		return view;
	}

	@Override
	public void bindView(View view, final Context context, Cursor cursor) {
		final AudioRecentlySong recentlySong = (AudioRecentlySong) view.getTag();
		audioId = screen.getHighlightId();
		final int id = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ID));
		if (audioId != null && audioId == id) {
			recentlySong.song.setTextColor(MediaApplication.color_highlight);
			recentlySong.duration.setTextColor(MediaApplication.color_highlight);
			screen.setHighlightScreenAudioId(ScreenAudio.SCREEN_AUDIO_RECENTLY);
		} else {
			recentlySong.song.setTextColor(Color.WHITE);
			recentlySong.duration.setTextColor(Color.WHITE);
		}
        view.setTag(R.layout.screen_audio, cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_FILEPATH)));
		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.screen_audio_recently_song_options:
					int[] location = new int[2];
					v.getLocationInWindow(location);
					int xoff = recentlySong.optionsWindow.getWidth();
					int yoff = ServiceManager.getAmtMedia().getResources().getDimensionPixelSize(R.dimen.popwindow_item_maginTop);
					int height = ServiceManager.getAmtMedia().getResources().getDimensionPixelSize(R.dimen.popwindow_item_height);
					if(location[1] + 5 * height > MediaApplication.getSoftInScreenHeight()){
						yoff = 5 * height;
					}
					recentlySong.options.setBackgroundResource(R.drawable.screen_audio_song_options_open_large);
					recentlySong.optionsWindow.showAsDropDown(v, -xoff, -yoff);
					break;
				case R.id.screen_audio_song_options_addto:
					recentlySong.optionsWindow.dismiss();
					dialogSelectPlaylist.show(id);
					break;
				case R.id.screen_audio_song_options_delete:
					recentlySong.optionsWindow.dismiss();
					PlaylistCreateUtils.showDelete(id, screen);
					break;
				case R.id.screen_audio_song_options_attributes:
					recentlySong.optionsWindow.dismiss();
					dialogSongAttributes.show(id);
					break;
				case R.id.screen_audio_song_options_makeLyric:
					recentlySong.optionsWindow.dismiss();
					dialogEditLyric.show();
					break;
				}
			}
		};
		recentlySong.optionsWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				recentlySong.options.setBackgroundResource(R.drawable.screen_audio_song_options_large);
			}
		});
		String name = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_DISPALYNAME));
		recentlySong.popSong.setText(name.substring(0, name.lastIndexOf(".")));
		recentlySong.song.setText(name.substring(0, name.lastIndexOf(".")));
		int duration = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_DURATION));
		recentlySong.duration.setText(formatTime(duration));
        int number = cursor.getPosition();
        String path = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_FILEPATH));
        File f = new File(path);
		if(!f.exists()){
			recentlySong.number.setVisibility(View.GONE);
			recentlySong.error.setVisibility(View.VISIBLE);
		} else {
			recentlySong.error.setVisibility(View.GONE);
			recentlySong.number.setVisibility(View.VISIBLE);
			recentlySong.number.setText(String.valueOf(number+1));
		}
//		recentlySong.singer.setText(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
		recentlySong.options.setOnClickListener(listener);
		recentlySong.addTo.setOnClickListener(listener);
		recentlySong.attributes.setOnClickListener(listener);
		recentlySong.makeLyric.setOnClickListener(listener);
		recentlySong.ringtones.setOnClickListener(listener);
		recentlySong.delete.setOnClickListener(listener);
		recentlySong.favorite.setOnCheckedChangeListener(null);
		if (db.audioIsFavorite(id)) {
			recentlySong.favorite.setChecked(true);
		} else {
			recentlySong.favorite.setChecked(false);
		}
		recentlySong.favorite.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					values.clear();
					values.put(MediaDatabaseHelper.COLUMN_FAVORITY_MEDIA_ID, id);
					db.addAudioToFavorite(values);
				} else {
					db.deleteAudioFromFavority(id);
				}
				ScreenAudio.refreshCount(ScreenAudio.REFRESH_FAVOURITES_COUNT);
			}
		});
	}
   
	private static String formatTime(int times) {
		times /= 1000;
		int minutes = times / 60;
		int seconds = times % 60;
		minutes %= 60;
		return String.format("%02d:%02d", minutes, seconds);
	}
	
	private class AudioRecentlySong {
		private TextView number;
		private View error;
		private TextView song;
		private TextView duration;
		private Button options;
		private PopupWindow optionsWindow;
		private Button addTo;
		private Button ringtones;
		private Button delete;
		private Button attributes;
		private Button makeLyric;
		private CheckBox favorite;
		private TextView popSong;
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
