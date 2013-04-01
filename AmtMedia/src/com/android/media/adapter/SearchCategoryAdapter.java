package com.android.media.adapter;

import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.amusic.media.R;
import com.android.media.MediaApplication;
import com.android.media.model.CategoryItem;
import com.android.media.model.ScreenArgs;
import com.android.media.provider.MediaManagerDB;
import com.android.media.services.impl.ServiceManager;

public class SearchCategoryAdapter extends BaseAdapter {
	private List<CategoryItem> categoryItems;
	private LayoutInflater inflater;
	private MediaManagerDB db;

	@Override
	public int getCount() {
		return categoryItems.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		SearchCategoryItem searchCategoryItem;
		if (convertView == null) {
			searchCategoryItem = new SearchCategoryItem();
			convertView = inflater.inflate(R.layout.screen_search_list_item, null);
			searchCategoryItem.nameTextView = (TextView) convertView.findViewById(R.id.screen_search_list_item_above);
			searchCategoryItem.countTextView = (TextView) convertView.findViewById(R.id.screen_search_list_item_below);
			convertView.setTag(R.layout.screen_search_list_item, searchCategoryItem);
		} else {
			searchCategoryItem = (SearchCategoryItem) convertView.getTag(R.layout.screen_search_list_item);
		}
		CategoryItem categoryItem = categoryItems.get(position);
		searchCategoryItem.nameTextView.setText(categoryItem.getName());
		searchCategoryItem.countTextView.setText(String.valueOf(db.queryCategoryAudiosCount(categoryItem.getType(), null)));
		ScreenArgs args = new ScreenArgs();
		args.putExtra("category", categoryItem.getName());
		args.putExtra("categoryType", categoryItem.getType().toString());
		convertView.setTag(args);
		return convertView;
	}

	public SearchCategoryAdapter(List<CategoryItem> categoryItems) {
		this.categoryItems = categoryItems;
		inflater = LayoutInflater.from(MediaApplication.getContext());
		db = ServiceManager.getMediaService().getMediaDB();
	}

	private class SearchCategoryItem {
		private TextView nameTextView;
		private TextView countTextView;
	}
}
