package com.android.media.model;

public class CategoryItem {
	private String name;
	private String type;
	public static final String CATEGORY_TYPE_CHILD_STR = "1";
	public static final String CATEGORY_TYPE_LOVE_STR = "1_";
	public static final String CATEGORY_TYPE_EUROPE_STR = "1__";
	public static final String CATEGORY_TYPE_JASK_STR = "1___";
	public static final String CATEGORY_TYPE_CLASSIC_STR = "1____";
	public static final String CATEGORY_TYPE_TELEVISION_STR = "1_____";
	public static final String CATEGORY_TYPE_HKAT_STR = "1______";
	public static final String CATEGORY_TYPE_CHINA_STR = "1_______";

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public enum CategoryType {
		CATEGORY_TYPE_CHILD(CATEGORY_TYPE_CHILD_STR), CATEGORY_TYPE_LOVE(CATEGORY_TYPE_LOVE_STR), CATEGORY_TYPE_EUROPE(CATEGORY_TYPE_EUROPE_STR), CATEGORY_TYPE_JASK(CATEGORY_TYPE_JASK_STR), CATEGORY_TYPE_CLASSIC(
				CATEGORY_TYPE_CLASSIC_STR), CATEGORY_TYPE_TELEVISION(CATEGORY_TYPE_TELEVISION_STR), CATEGORY_TYPE_HKAT(CATEGORY_TYPE_HKAT_STR), CATEGORY_TYPE_CHINA(CATEGORY_TYPE_CHINA_STR);
		private String type;

		private CategoryType(String type) {
			this.type = type;
		}

		@Override
		public String toString() {
			return type;
		}
	}

	public CategoryItem(String name, CategoryType categoryType) {
		this.name = name;
		this.type = categoryType.toString();
	}
}
