package com.amusic.media.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ScreenArgs implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Map<String, Object> args = new HashMap<String, Object>();

	public Object getExtra(String key) {
		return args.get(key);
	}
	
	public Object getExtra(String key, Object defaultValue) {
		return args.get(key) == null ? defaultValue : args.get(key);
	}

	public ScreenArgs putExtra(String key, Object value) {
		args.put(key, value);
		return this;
	}
}
