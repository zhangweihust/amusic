package com.amusic.media.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.amusic.media.R;
import com.amusic.media.provider.MediaDictionaryDatabaseHelper;

public class SearchSingerAdapter extends CursorAdapter {
	private LayoutInflater inflater;

	public SearchSingerAdapter(Context context, Cursor c) {
		super(context, c);
		inflater = LayoutInflater.from(context);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.screen_search_singer_item, null);
		SearchSingerItem mediaSingerItem = new SearchSingerItem();
		mediaSingerItem.singerTextView = (TextView) view.findViewById(R.id.screen_search_singer_item_above);
		view.setTag(R.layout.screen_search_singer_item, mediaSingerItem);
		return view;
	}

	@Override
	public void bindView(View view, final Context context, Cursor cursor) {
		final String singer = cursor.getString(cursor.getColumnIndex(MediaDictionaryDatabaseHelper.COLUMN_DICTIONARY_SINGER));
		SearchSingerItem mediaSingerItem = (SearchSingerItem) view.getTag(R.layout.screen_search_singer_item);
		mediaSingerItem.singerTextView.setText(singer);
		view.setTag(singer);
	}

	private class SearchSingerItem {
		private TextView singerTextView;
	}
}
