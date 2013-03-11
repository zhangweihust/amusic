package com.amusic.media.services;

import java.util.Stack;

import com.amusic.media.model.ScreenArgs;

public interface IScreenService<T> extends IService {

	public boolean show(Class<? extends T> screen, boolean addToBack, ScreenArgs args, int visibility);

	public boolean show(Class<? extends T> screen, boolean addToBack);

	public boolean show(Class<? extends T> screen, ScreenArgs args);

	public boolean show(Class<? extends T> screen, ScreenArgs args, int visibility);

	public boolean show(Class<? extends T> screen, int visibility);

	public boolean show(Class<? extends T> screen, boolean addToBack, int visibility);

	public boolean show(Class<? extends T> screen);

	public boolean show(String screen_id);

	public boolean show(String screen_id, boolean addToBack);

	public boolean goback();

	boolean show(String screen_id, boolean addToBack, ScreenArgs args);

	public Stack<Mark> getMarks();

	public Stack<String> getBackList();
	
	public class Mark {
		private String screenId;
		private Integer id;

		public Mark(String screenId, Integer id) {
			this.screenId = screenId;
			this.id = id;
		}

		public String getScreenId() {
			return screenId;
		}

		public Integer getId() {
			return id;
		}

	}

}
