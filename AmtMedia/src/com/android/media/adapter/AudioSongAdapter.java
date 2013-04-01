package com.android.media.adapter;

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

import com.amusic.media.R;
import com.android.media.MediaApplication;
import com.android.media.dialog.DialogEditLyric;
import com.android.media.dialog.DialogSelectPlaylist;
import com.android.media.dialog.DialogSongAttributes;
import com.android.media.provider.MediaDatabaseHelper;
import com.android.media.provider.MediaManagerDB;
import com.android.media.screens.Screen;
import com.android.media.screens.impl.ScreenAudio;
import com.android.media.services.impl.ServiceManager;
import com.android.media.utils.PlaylistCreateUtils;

public class AudioSongAdapter extends CursorAdapter implements OnItemClickListener {

	private LayoutInflater inflater;
	private MediaManagerDB db;
	private ContentValues values = new ContentValues();
	private DialogSelectPlaylist dialogSelectPlaylist;
	private DialogSongAttributes dialogSongAttributes;
	private DialogEditLyric dialogEditLyric;
	private Screen screen;

	private Integer audioId;
	
	private Context context;
	

	public AudioSongAdapter(Context context, Cursor c, Screen screen) {
		super(context, c);
		this.context=context;
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
		View view = inflater.inflate(R.layout.screen_audio_song_item, null);
		AudioSong audioSong = new AudioSong();
		audioSong.number=(TextView) view.findViewById(R.id.screen_audio_song_number);
		audioSong.error=(View) view.findViewById(R.id.screen_audio_song_error);
		audioSong.song = (TextView) view.findViewById(R.id.screen_audio_song_name);
		audioSong.duration=(TextView) view.findViewById(R.id.screen_audio_song_duration);
		audioSong.options = (Button) view.findViewById(R.id.screen_audio_song_options);
		View popView = inflater.inflate(R.layout.screen_audio_song_options, null);
		audioSong.optionsWindow = new PopupWindow(popView, ServiceManager.getAmtMedia().getResources().getDimensionPixelSize(R.dimen.popwindow_item_width), LayoutParams.WRAP_CONTENT);
		audioSong.optionsWindow.setFocusable(true);
		audioSong.optionsWindow.setBackgroundDrawable(new BitmapDrawable());
		audioSong.popSong = (TextView) popView.findViewById(R.id.screen_audio_song_options_song);
		audioSong.addTo = (Button) popView.findViewById(R.id.screen_audio_song_options_addto);
		audioSong.delete = (Button) popView.findViewById(R.id.screen_audio_song_options_delete);
		audioSong.makeLyric = (Button) popView.findViewById(R.id.screen_audio_song_options_makeLyric);
		audioSong.attributes = (Button) popView.findViewById(R.id.screen_audio_song_options_attributes);
		audioSong.favorite = (CheckBox) popView.findViewById(R.id.screen_audio_song_options_favorite);
		view.setTag(audioSong);
		return view;
	}

	@Override
	public void bindView(View view, final Context context, Cursor cursor) {
		final AudioSong audioSong = (AudioSong) view.getTag();
		final int id = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ID));
		final String songName = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SKINS_DISPALYNAME));
		final String songPath = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_FILEPATH));
		 audioId = screen.getHighlightId();
		 if (audioId != null && audioId == id) {
			 audioSong.song.setTextColor(MediaApplication.color_highlight);
			 audioSong.duration.setTextColor(MediaApplication.color_highlight);
			 screen.setHighlightScreenAudioId(ScreenAudio.SCREEN_AUDIO_SONGS);
		 } else {
			 audioSong.song.setTextColor(Color.WHITE);
			 audioSong.duration.setTextColor(Color.WHITE);
		 }
		 view.setTag(R.layout.screen_audio, cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_FILEPATH)));
		 OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.screen_audio_song_options:
					int[] location = new int[2];
					v.getLocationInWindow(location);
					int xoff = audioSong.optionsWindow.getWidth();
					int yoff = ServiceManager.getAmtMedia().getResources().getDimensionPixelSize(R.dimen.popwindow_item_maginTop);
					int height = ServiceManager.getAmtMedia().getResources().getDimensionPixelSize(R.dimen.popwindow_item_height);
					if(location[1] + 5 * height > MediaApplication.getSoftInScreenHeight()){
						yoff = 5 * height;
					}
					audioSong.options.setBackgroundResource(R.drawable.screen_audio_song_options_open_large);
					audioSong.optionsWindow.showAsDropDown(v, -xoff, -yoff);
					break;
				case R.id.screen_audio_song_options_addto:
					audioSong.optionsWindow.dismiss();
					dialogSelectPlaylist.show(id);
					break;
				case R.id.screen_audio_song_options_delete:
					audioSong.optionsWindow.dismiss();
					PlaylistCreateUtils.showDelete(id, screen);
					break;
				case R.id.screen_audio_song_options_attributes:
					audioSong.optionsWindow.dismiss();
					dialogSongAttributes.show(id);
					break;
				case R.id.screen_audio_song_options_makeLyric:
					audioSong.optionsWindow.dismiss();
					dialogEditLyric.setSongName(songName);
					dialogEditLyric.setSongPath(songPath);
					dialogEditLyric.show();
					break;
				}
			}
		};
		audioSong.optionsWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				audioSong.options.setBackgroundResource(R.drawable.screen_audio_song_options_large);
			}
		});
		audioSong.options.setOnClickListener(listener);
		audioSong.addTo.setOnClickListener(listener);
		audioSong.delete.setOnClickListener(listener);
		audioSong.attributes.setOnClickListener(listener);
		audioSong.makeLyric.setOnClickListener(listener);
		audioSong.favorite.setOnCheckedChangeListener(null);
		if (db.audioIsFavorite(id)) {
			audioSong.favorite.setChecked(true);
		} else {
			audioSong.favorite.setChecked(false);
		}
		audioSong.favorite.setOnCheckedChangeListener(new OnCheckedChangeListener() {

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
		String name = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_DISPALYNAME));
		audioSong.popSong.setText(name.substring(0, name.lastIndexOf(".")));
		audioSong.song.setText(name.substring(0, name.lastIndexOf(".")));
		int duration = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_DURATION));
		audioSong.duration.setText(formatTime(duration));
        int number = cursor.getPosition();
        String path = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_FILEPATH));
        File f = new File(path);
		if(!f.exists()){
			audioSong.number.setVisibility(View.GONE);
			audioSong.error.setVisibility(View.VISIBLE);
		} else {
			audioSong.error.setVisibility(View.GONE);
			audioSong.number.setVisibility(View.VISIBLE);
			audioSong.number.setText(String.valueOf(number+1));
		}
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
			Toast.makeText(context, screen.getString(R.string.screen_create_new_playlist_exist),Toast.LENGTH_LONG).show();
		}
		dialogSelectPlaylist.dismiss();
	}

	private static String formatTime(int times) {
		times /= 1000;
		int minutes = times / 60;
		int seconds = times % 60;
		minutes %= 60;
		return String.format("%02d:%02d", minutes, seconds);
	}
	
	private class AudioSong {
		private TextView number;
		private View error;
		private TextView song;
		private TextView duration;
		private Button options;
		private PopupWindow optionsWindow;
		private Button addTo;
		private Button delete;
		private Button attributes;
		private Button makeLyric;
		private CheckBox favorite;
		private TextView popSong;
	}

	@Override
	public void changeCursor(Cursor cursor) {
	    audioId = screen.getHighlightId();
		super.changeCursor(cursor);
	}
}
