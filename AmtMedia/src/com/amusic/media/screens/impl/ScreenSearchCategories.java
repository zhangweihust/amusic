package com.amusic.media.screens.impl;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.amusic.media.MediaApplication;
import com.amusic.media.R;
import com.amusic.media.model.CategoryItem;
import com.amusic.media.model.ScreenArgs;
import com.amusic.media.screens.SearchScreen;

public class ScreenSearchCategories extends SearchScreen implements
		OnClickListener {

	public static final int SCREEN_SEARCH_CATEGORYS_NEW = 1;
	public static final int SCREEN_SEARCH_CATEGORYS_EUROPE = 2;
	public static final int SCREEN_SEARCH_CATEGORYS_JASK = 3;
	public static final int SCREEN_SEARCH_CATEGORYS_HKAT = 4;
	public static final int SCREEN_SEARCH_CATEGORYS_CHINA = 5;
	public static final int SCREEN_SEARCH_CATEGORYS_LOVE = 6;
	public static final int SCREEN_SEARCH_CATEGORYS_TELEVISION = 7;
	public static final int SCREEN_SEARCH_CATEGORYS_CLASSIC = 8;
	public static final int SCREEN_SEARCH_CATEGORYS_CHILD = 9;

	private List<CategoryItem> categoryItems = new ArrayList<CategoryItem>();
	private ImageButton newBtn;
	private ImageButton europeBtn;
	private ImageButton jaskBtn;
	private ImageButton hkatBtn;
	private ImageButton chinaBtn;
	private ImageButton loveBtn;
	private ImageButton televitionBtn;
	private ImageButton classicBtn;
	private ImageButton childBtn;

	private TextView  newText;
	private TextView  europeText;
	private TextView  jaskText;
	private TextView  hkatText;
	private TextView  chinaText;
	private TextView  loveText;
	private TextView  televitionText;
	private TextView  classicText;
	private TextView  childText;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_search_categorys);
		// CategoryItem categoryItem;
		// String name = "";
		// categoryItems.clear();
		// for (CategoryType categoryType : CategoryType.values()) {
		// switch (categoryType) {
		// case CATEGORY_TYPE_CHILD: // 儿歌
		// name = this
		// .getString(R.string.screen_search_category_item_child_name);
		// break;
		// case CATEGORY_TYPE_CHINA: // 内地
		// name = this
		// .getString(R.string.screen_search_category_item_china_name);
		// break;
		// case CATEGORY_TYPE_CLASSIC: // 经典
		// name = this
		// .getString(R.string.screen_search_category_item_classic_name);
		// break;
		// case CATEGORY_TYPE_EUROPE: // 欧美
		// name = this
		// .getString(R.string.screen_search_category_item_europe_name);
		// break;
		// case CATEGORY_TYPE_HKAT: // 港台
		// name = this
		// .getString(R.string.screen_search_category_item_hkat_name);
		// break;
		// case CATEGORY_TYPE_JASK: // 日韩
		// name = this
		// .getString(R.string.screen_search_category_item_jask_name);
		// break;
		// case CATEGORY_TYPE_LOVE: // 情歌
		// name = this
		// .getString(R.string.screen_search_category_item_love_name);
		// break;
		// case CATEGORY_TYPE_TELEVISION: // 影视金曲
		// name = this
		// .getString(R.string.screen_search_category_item_television_name);
		// break;
		// }
		// categoryItem = new CategoryItem(name, categoryType);
		// categoryItems.add(categoryItem);
		// }
		setScreenTitle(getString(R.string.screen_search_categories_title));
		newBtn = (ImageButton) findViewById(R.id.screen_search_categorys_new);
		newBtn.setOnClickListener(this);
		newText = (TextView) findViewById(R.id.screen_search_categorys_new_text);
		
		europeBtn = (ImageButton) findViewById(R.id.screen_search_categorys_europe);
		europeBtn.setOnClickListener(this);
		europeText = (TextView) findViewById(R.id.screen_search_categorys_europe_text);
		
		jaskBtn = (ImageButton) findViewById(R.id.screen_search_categorys_jask);
		jaskBtn.setOnClickListener(this);
		jaskText = (TextView) findViewById(R.id.screen_search_categorys_jask_text);
		
		hkatBtn = (ImageButton) findViewById(R.id.screen_search_categorys_hkat);
		hkatBtn.setOnClickListener(this);
		hkatText = (TextView) findViewById(R.id.screen_search_categorys_hkat_text);
		
		chinaBtn = (ImageButton) findViewById(R.id.screen_search_categorys_china);
		chinaBtn.setOnClickListener(this);
		chinaText = (TextView) findViewById(R.id.screen_search_categorys_china_text);
		
		loveBtn = (ImageButton) findViewById(R.id.screen_search_categorys_love);
		loveBtn.setOnClickListener(this);
		loveText = (TextView) findViewById(R.id.screen_search_categorys_love_text);
		
		televitionBtn = (ImageButton) findViewById(R.id.screen_search_categorys_televition);
		televitionBtn.setOnClickListener(this);
		televitionText = (TextView) findViewById(R.id.screen_search_categorys_televition_text);
		
		classicBtn = (ImageButton) findViewById(R.id.screen_search_categorys_classic);
		classicBtn.setOnClickListener(this);
		classicText = (TextView) findViewById(R.id.screen_search_categorys_classic_text);
		
		childBtn = (ImageButton) findViewById(R.id.screen_search_categorys_child);
		childBtn.setOnClickListener(this);
		childText = (TextView) findViewById(R.id.screen_search_categorys_child_text);
		
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setScreenTitle(getString(R.string.screen_search_categories_title));
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		setScreenTitle(getString(R.string.screen_search_categories_title));
		europeText.setTextColor(MediaApplication.color_normal);
		jaskText.setTextColor(MediaApplication.color_normal);
		hkatText.setTextColor(MediaApplication.color_normal);
		chinaText.setTextColor(MediaApplication.color_normal);
		loveText.setTextColor(MediaApplication.color_normal);
		televitionText.setTextColor(MediaApplication.color_normal);
		classicText.setTextColor(MediaApplication.color_normal);
		childText.setTextColor(MediaApplication.color_normal);
	}

	@Override
	public void onClick(View v) {
		ScreenArgs args;
		String name = "";
		switch (v.getId()) {
		case R.id.screen_search_categorys_new:
			args = new ScreenArgs();
			args.putExtra("goback", true);
			searchScreenService.show(ScreenSearchCategorySongs.class, args);
			break;
		case R.id.screen_search_categorys_europe:
			args = new ScreenArgs();
			name = getString(R.string.screen_search_category_item_europe_name);
			args.putExtra("category", name);
			args.putExtra("categoryType", CategoryItem.CATEGORY_TYPE_EUROPE_STR);
			args.putExtra("goback", true);
			searchScreenService.show(ScreenSearchCategorySongs.class, args);
			break;
		case R.id.screen_search_categorys_jask:
			args = new ScreenArgs();
			name = getString(R.string.screen_search_category_item_jask_name);
			args.putExtra("category", name);
			args.putExtra("categoryType", CategoryItem.CATEGORY_TYPE_JASK_STR);
			args.putExtra("goback", true);
			searchScreenService.show(ScreenSearchCategorySongs.class, args);
			break;
		case R.id.screen_search_categorys_hkat:
			args = new ScreenArgs();
			name = getString(R.string.screen_search_category_item_hkat_name);
			args.putExtra("category", name);
			args.putExtra("categoryType", CategoryItem.CATEGORY_TYPE_HKAT_STR);
			args.putExtra("goback", true);
			searchScreenService.show(ScreenSearchCategorySongs.class, args);
			break;
		case R.id.screen_search_categorys_china:
			args = new ScreenArgs();
			name = getString(R.string.screen_search_category_item_china_name);
			args.putExtra("category", name);
			args.putExtra("categoryType", CategoryItem.CATEGORY_TYPE_CHINA_STR);
			args.putExtra("goback", true);
			searchScreenService.show(ScreenSearchCategorySongs.class, args);
			break;
		case R.id.screen_search_categorys_love:
			args = new ScreenArgs();
			name = getString(R.string.screen_search_category_item_love_name);
			args.putExtra("category", name);
			args.putExtra("categoryType", CategoryItem.CATEGORY_TYPE_LOVE_STR);
			args.putExtra("goback", true);
			searchScreenService.show(ScreenSearchCategorySongs.class, args);
			break;
		case R.id.screen_search_categorys_televition:
			args = new ScreenArgs();
			name = getString(R.string.screen_search_category_item_television_name);
			args.putExtra("category", name);
			args.putExtra("categoryType",
					CategoryItem.CATEGORY_TYPE_TELEVISION_STR);
			args.putExtra("goback", true);
			searchScreenService.show(ScreenSearchCategorySongs.class, args);
			break;
		case R.id.screen_search_categorys_classic:
			args = new ScreenArgs();
			name = getString(R.string.screen_search_category_item_classic_name);
			args.putExtra("category", name);
			args.putExtra("categoryType",
					CategoryItem.CATEGORY_TYPE_CLASSIC_STR);
			args.putExtra("goback", true);
			searchScreenService.show(ScreenSearchCategorySongs.class, args);
			break;
		case R.id.screen_search_categorys_child:
			args = new ScreenArgs();
			name = getString(R.string.screen_search_category_item_child_name);
			args.putExtra("category", name);
			args.putExtra("categoryType", CategoryItem.CATEGORY_TYPE_CHILD_STR);
			args.putExtra("goback", true);
			searchScreenService.show(ScreenSearchCategorySongs.class, args);
			break;
		}
	}
}
