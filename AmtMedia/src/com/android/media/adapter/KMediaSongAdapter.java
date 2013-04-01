package com.android.media.adapter;


import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.amusic.media.R;
import com.android.media.model.SongInfo;
import com.android.media.screens.Screen;
import com.android.media.view.RemoteImageView;

public class KMediaSongAdapter extends BaseAdapter{
	private ArrayList<SongInfo> kmediaSongs;
	private LayoutInflater inflater;
	private ListView mListView;
	public KMediaSongAdapter(Context context, Screen screen, ListView list){
		inflater = LayoutInflater.from(context);
		mListView = list;
	}
	public void setkmediaSongs(ArrayList<SongInfo> kmediaSongs){
		this.kmediaSongs = kmediaSongs;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return kmediaSongs != null ? kmediaSongs.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return kmediaSongs != null ? kmediaSongs.get(position) : null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View view, ViewGroup parent) {
		KmdiaSong kmdiaSong = null;
		if(view == null){
			kmdiaSong = new KmdiaSong();
			view = inflater.inflate(R.layout.screen_kmedia_song_item, null);
			kmdiaSong.image = (RemoteImageView) view.findViewById(R.id.screen_kmedia_singer_icon);
			kmdiaSong.song = (TextView) view.findViewById(R.id.screen_kmedia_song_song);
			kmdiaSong.singer = (TextView) view.findViewById(R.id.screen_kmedia_song_singer);
			view.setTag(kmdiaSong);
		}else{
			kmdiaSong = (KmdiaSong) view.getTag();
		}
		final SongInfo songInfo = kmediaSongs.get(position);
		if(songInfo != null){
			final String singerName = songInfo.getSingerName();
			final String songName = songInfo.getSongName();
			kmdiaSong.song.setText(songName);
			kmdiaSong.singer.setText(singerName);
			kmdiaSong.image.setDefaultImage(R.drawable.screen_audio_item_singers_bg);
			kmdiaSong.image.setImageUrl(singerName, position ,mListView);
		}
		return view;
	}
	
	private class KmdiaSong {
		private RemoteImageView image;
		private TextView song;
		private TextView singer;
	}
}