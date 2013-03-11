package com.amusic.media.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.amusic.media.MediaApplication;
import com.amusic.media.R;
import com.amusic.media.provider.MediaDatabaseHelper;
import com.amusic.media.provider.MediaManagerDB;
import com.amusic.media.screens.Screen;
import com.amusic.media.screens.impl.ScreenAudio;
import com.amusic.media.services.impl.ServiceManager;

public class AudioAlbumAdapter extends CursorAdapter {

	private LayoutInflater inflater;
	private MediaManagerDB db;
	private Integer albumId;
	private Screen screen;

	public AudioAlbumAdapter(Context context, Cursor c, Screen screen) {
		super(context, c);
		inflater = LayoutInflater.from(context);
		db = ServiceManager.getMediaService().getMediaDB();
		albumId = screen.getHighlightId();
		this.screen = screen;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.screen_audio_album_item, null);
		AudioAlbum audioAlbum = new AudioAlbum();
		audioAlbum.name = (TextView) view.findViewById(R.id.screen_audio_album_name);
		audioAlbum.count = (TextView) view.findViewById(R.id.screen_audio_album_count);
		view.setTag(audioAlbum);
		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final AudioAlbum audioAlbum = (AudioAlbum) view.getTag();
		final int id = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ID));
		String albumName = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ALBUMNAME));
		audioAlbum.name.setText(albumName);
		if (albumId != null && id == albumId) {
			audioAlbum.name.setTextColor(MediaApplication.color_highlight);
			audioAlbum.count.setTextColor(MediaApplication.color_highlight);
			screen.setHighlightScreenAudioId(ScreenAudio.SCREEN_AUDIO_ALBUMS);
		} else {
			audioAlbum.name.setTextColor(Color.WHITE);
			audioAlbum.count.setTextColor(Color.WHITE);
		}
		Cursor c = db.queryAlbumAudios(albumName);
		String resultsText = String.format(context.getResources().getString(R.string.screen_audio_songs_count), String.valueOf(c.getCount())); 
		audioAlbum.count.setText(resultsText);
		c.close();
	}

	private class AudioAlbum {
		private TextView name;
		private TextView count;
	}

	@Override
	public void changeCursor(Cursor cursor) {
		albumId = screen.getHighlightId();
		super.changeCursor(cursor);
	}
}
