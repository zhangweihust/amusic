package com.amusic.media.adapter;

import java.util.Set;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amusic.media.R;
import com.amusic.media.provider.MediaDatabaseHelper;

public class AudioPlaylistEditAdapter extends CursorAdapter {

	private LayoutInflater inflater;
	private Set<Integer> set;
	private Cursor cursor;

	public AudioPlaylistEditAdapter(Context context, Cursor c, Set<Integer> set) {
		super(context, c);
		inflater = LayoutInflater.from(context);
		this.set = set;
		this.cursor = c;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.screen_audio_playlist_edit_item, null);
		AudioPlaylistEditSong playlistEditSong = new AudioPlaylistEditSong();
		playlistEditSong.item = (LinearLayout) view.findViewById(R.id.screen_audio_playlist_edit_item);
		playlistEditSong.song = (TextView) view.findViewById(R.id.screen_audio_playlist_edit_song);
		playlistEditSong.number=(TextView) view.findViewById(R.id.screen_audio_playlist_edit_number);
		playlistEditSong.duration=(TextView) view.findViewById(R.id.screen_audio_playlist_edit_duration);
		playlistEditSong.addToPlaylist = (CheckBox) view.findViewById(R.id.screen_audio_playlist_edit_add);
		view.setTag(playlistEditSong);
		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final AudioPlaylistEditSong playlistEditSong = (AudioPlaylistEditSong) view.getTag();
		String name = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_DISPALYNAME));
		playlistEditSong.song.setText(name.substring(0, name.lastIndexOf(".")));
		int duration = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_DURATION));
		playlistEditSong.duration.setText(formatTime(duration));
        int number = cursor.getPosition();
        playlistEditSong.number.setText(String.valueOf(number+1));
		final int audioId = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ID));
		if (set.contains(audioId)) {
			playlistEditSong.addToPlaylist.setChecked(true);
		} else {
			playlistEditSong.addToPlaylist.setChecked(false);
		}
		playlistEditSong.item.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!playlistEditSong.addToPlaylist.isChecked()){
					playlistEditSong.addToPlaylist.setChecked(true);
					set.add(audioId);
				}else{
					playlistEditSong.addToPlaylist.setChecked(false);
					set.remove(audioId);
				}
			}
		});
	}

	public void selectAll() {
		if (cursor.moveToFirst()) {
			set.add(cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ID)));
			while (cursor.moveToNext()) {
				set.add(cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ID)));
			}
		}
		notifyDataSetChanged();
	}

	public void cancelAll() {
		if (cursor.moveToFirst()) {
			set.remove(cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ID)));
			while (cursor.moveToNext()) {
				set.remove(cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ID)));
			}
		}
		notifyDataSetChanged();
	}

	@Override
	public void changeCursor(Cursor cursor) {
		set.clear();
		this.cursor = cursor;
		super.changeCursor(cursor);
	}

	
	private static String formatTime(int times) {
		times /= 1000;
		int minutes = times / 60;
		int seconds = times % 60;
		minutes %= 60;
		return String.format("%02d:%02d", minutes, seconds);
	}

	private class AudioPlaylistEditSong {
		private TextView number;
		private TextView song;
		private TextView duration;
		private CheckBox addToPlaylist;
		private LinearLayout item;
	}
}
