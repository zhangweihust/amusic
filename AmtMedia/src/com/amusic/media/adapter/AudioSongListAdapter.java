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

public class AudioSongListAdapter extends CursorAdapter implements OnItemClickListener {

	private LayoutInflater inflater;
	private ContentValues values = new ContentValues();
	private MediaManagerDB db;
	private DialogSelectPlaylist dialogSelectPlaylist;
	private DialogSongAttributes dialogSongAttributes;
	private DialogEditLyric dialogEditLyric;
	private Integer audioId;
	private Screen screen;
	

	public AudioSongListAdapter(Context context, Cursor c, Screen screen) {
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
		View view = inflater.inflate(R.layout.screen_audio_songlist_item, null);
		SongList songList = new SongList();
		songList.number = (TextView) view.findViewById(R.id.screen_audio_songlist_number);
		songList.error = (View) view.findViewById(R.id.screen_audio_songlist_error);
		songList.song = (TextView) view.findViewById(R.id.screen_audio_songlist_name);
		songList.duration = (TextView) view.findViewById(R.id.screen_audio_songlist_duration);
		songList.options = (Button) view.findViewById(R.id.screen_audio_songlist_options);
		View popView = inflater.inflate(R.layout.screen_audio_song_options, null);
		songList.optionsWindow = new PopupWindow(popView, ServiceManager.getAmtMedia().getResources().getDimensionPixelSize(R.dimen.popwindow_item_width), LayoutParams.WRAP_CONTENT);
		songList.optionsWindow.setFocusable(true);
		songList.optionsWindow.setBackgroundDrawable(new BitmapDrawable());
		songList.popSong = (TextView) popView.findViewById(R.id.screen_audio_song_options_song);
		songList.delete = (Button) popView.findViewById(R.id.screen_audio_song_options_delete);
		songList.addTo = (Button) popView.findViewById(R.id.screen_audio_song_options_addto);
		songList.ringtones = (Button) popView.findViewById(R.id.screen_audio_song_options_ringtones);
		songList.attributes = (Button) popView.findViewById(R.id.screen_audio_song_options_attributes);
		songList.makeLyric = (Button) popView.findViewById(R.id.screen_audio_song_options_makeLyric);
		songList.favorite = (CheckBox) popView.findViewById(R.id.screen_audio_song_options_favorite);
		view.setTag(songList);
		return view;
	}

	@Override
	public void bindView(View view, final Context context, Cursor cursor) {
		final SongList songList = (SongList) view.getTag();
		final int id = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ID));
		String path = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_FILEPATH));
		final String songName = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SKINS_DISPALYNAME));
		final String songPath = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_FILEPATH));
		view.setTag(R.layout.screen_audio, cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_FILEPATH)));
		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.screen_audio_songlist_options:
					int[] location = new int[2];
					v.getLocationInWindow(location);
					int xoff = songList.optionsWindow.getWidth();
					int yoff = ServiceManager.getAmtMedia().getResources().getDimensionPixelSize(R.dimen.popwindow_item_maginTop);
					int height = ServiceManager.getAmtMedia().getResources().getDimensionPixelSize(R.dimen.popwindow_item_height);
					if(location[1] + 5 * height > MediaApplication.getSoftInScreenHeight()){
						yoff = 5 * height;
					}
					songList.options.setBackgroundResource(R.drawable.screen_audio_song_options_open_large);
					songList.optionsWindow.showAsDropDown(v, -xoff, -yoff);
					break;
				case R.id.screen_audio_song_options_addto:
					songList.optionsWindow.dismiss();
					dialogSelectPlaylist.show(id);
					break;
				case R.id.screen_audio_song_options_delete:
					songList.optionsWindow.dismiss();
					PlaylistCreateUtils.showDelete(id, screen);
					break;
				case R.id.screen_audio_song_options_attributes:
					songList.optionsWindow.dismiss();
					dialogSongAttributes.show(id);
					break;
				case R.id.screen_audio_song_options_makeLyric:
					songList.optionsWindow.dismiss();
					dialogEditLyric.setSongName(songName);
					dialogEditLyric.setSongPath(songPath);
					dialogEditLyric.show();
					break;
				}
			}
		};
		songList.optionsWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				songList.options.setBackgroundResource(R.drawable.screen_audio_song_options_large);
			}
		});
		String name = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_DISPALYNAME));
		songList.popSong.setText(name.substring(0, name.lastIndexOf(".")));
		songList.song.setText(name.substring(0, name.lastIndexOf(".")));
		int duration = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_DURATION));
		songList.duration.setText(formatTime(duration));
        int number = cursor.getPosition();
		audioId = screen.getHighlightId();
		if (audioId != null && id == audioId) {
			songList.song.setTextColor(MediaApplication.color_highlight);
			songList.duration.setTextColor(MediaApplication.color_highlight);
		} else {
			songList.song.setTextColor(Color.WHITE);
			songList.duration.setTextColor(Color.WHITE);
		}
		File f = new File(path);
		if(!f.exists()){
			songList.number.setVisibility(View.GONE);
			songList.error.setVisibility(View.VISIBLE);
		} else {
			songList.error.setVisibility(View.GONE);
			songList.number.setVisibility(View.VISIBLE);
			songList.number.setText(String.valueOf(number+1));
		}
		songList.options.setOnClickListener(listener);
		songList.delete.setOnClickListener(listener);
		songList.addTo.setOnClickListener(listener);
		songList.attributes.setOnClickListener(listener);
		songList.makeLyric.setOnClickListener(listener);
		songList.ringtones.setOnClickListener(listener);
		songList.favorite.setOnCheckedChangeListener(null);
		if (db.audioIsFavorite(id)) {
			songList.favorite.setChecked(true);
		} else {
			songList.favorite.setChecked(false);
		}
		songList.favorite.setOnCheckedChangeListener(new OnCheckedChangeListener() {

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
	
	
	private static String formatTime(int times) {
		times /= 1000;
		int minutes = times / 60;
		int seconds = times % 60;
		minutes %= 60;
		return String.format("%02d:%02d", minutes, seconds);
	}

	private class SongList {
		private TextView number;
		private View error;
		private TextView song;
		private TextView duration;
		private CheckBox favorite;
		private Button options;
		private PopupWindow optionsWindow;
		private Button delete;
		private Button addTo;
		private Button ringtones;
		private Button attributes;
		private Button makeLyric;
		private TextView popSong;
	}

	@Override
	public void changeCursor(Cursor cursor) {
		audioId = screen.getHighlightId();
		super.changeCursor(cursor);
	}
}
