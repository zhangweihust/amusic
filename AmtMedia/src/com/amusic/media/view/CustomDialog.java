package com.amusic.media.view;

import java.util.List;
import java.util.Map;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amusic.media.R;

public class CustomDialog extends Dialog {
	public static final int contentIsTextView = 0;//Dialog的内容是TextVIew
	public static final int contentIsListView = 1;//Dialog的内容是ListView
	public static final int contentIsEditText = 2;//Dialog的内容是EditText
	public static final int contentIsCheckBox = 3;//Dialog的内容是CheckBox
	public static final int LISTVIEW_ITEM_TEXTVIEW = 4;//Dialog的内容是ListView,ListView里面的内容是一行文字
	public static final int LISTVIEW_ITEM_TEXTVIEW_AND_EDITTEXT = 5;//Dialog的内容是ListView,ListView里面的内容是一行文字和编辑框
	public static final int contentIsProgressBar = 6;
	
	//Dialog的内容是ListView,里面每一行点击以后相应的处理
	public static final int GO_BACK = 100;
	public static final int RESUME_DOWNLOAD = 101;
	public static final int PAUSE_DOWNLOAD = 102;
	public static final int RE_DOWNLOAD = 103;
	public static final int CANCEL_DOWNLOAD = 104;
	public static final int DELETE_DOWNLOAD = 105;
	public static final int DOWNLOAD_ACCOMPANY = 106;
	public static final int ORIGINAL_AUDIO_PLAY = 107;

	public CustomDialog(Context context, int theme) {
		super(context, theme);
		setCanceledOnTouchOutside(true);
//	    WindowManager.LayoutParams lp=this.getWindow().getAttributes(); 
//		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND); 
//		lp.dimAmount=0.5f;  
//		getWindow().setAttributes(lp);
	}

	public CustomDialog(Context context) {
		super(context);
		setCanceledOnTouchOutside(true);
	}

	/**
	 * Helper class for creating a custom dialog
	 */
	public static class Builder {

		private Context context;
		private String title;
		private String message;
		//Dialog里面3个BUTTON的显示文字
		private String positiveButtonText;
		private String negativeButtonText;
		private String neutralButtonText;
		private String checkBoxText;
        //Dialog的内容是ListView，ListView填入的数据集合
		private List<Map<String, Object>> listViewData;
		//Dialog的内容是ListView，ListView的itemClickListener
		private OnItemClickListener itemClickListener;
		private View contentView;
		//设置Dialog的内容显示什么View
		private int whichViewVisible = -1;
		
		private int layoutID;
		
		private int layoutXml;
		
		private boolean checked;
		
		public CheckBox getmCheckBox() {
			return mCheckBox;
		}
		
		public void setCheckBoxChecked(boolean checked){
			this.checked = checked;
		}

		public EditText getEditText(){
			return mEditText;
		}
		
		public ProgressBar getProgressBar()
		{
			return mProgressBar;
		}
		
		public void setProgressBar(int progress)
		{
			mProgressBar.setProgress(progress);
		}
		
		public int getProgressBarpro()
		{
			return mProgressBar.getProgress();
		}
		
		public TextView getProgressTextView()
		{
			return mTextView;
		}

		private TextView mTextView;
		private EditText mEditText;
		private ListView mListView;
		private CheckBox mCheckBox;
		private ProgressBar mProgressBar;

		private DialogInterface.OnClickListener positiveButtonClickListener, negativeButtonClickListener, neutralButtonClickListener;
		private CompoundButton.OnCheckedChangeListener CheckBoxListener = null;
		
		public Builder(Context context) {
			this.context = context;
			layoutXml = R.layout.custom_dialog;
		}

		
		
		/**
		 * Set the Dialog message from String
		 * 
		 * @param title
		 * @return
		 */
		public Builder setMessage(String message) {
			this.message = message;
			return this;
		}

		/**
		 * Set the Dialog message from resource
		 * 
		 * @param title
		 * @return
		 */
		public Builder setMessage(int message) {
			this.message = (String) context.getText(message);
			return this;
		}

		/**
		 * Set the Dialog title from resource
		 * 
		 * @param title
		 * @return
		 */
		public Builder setTitle(int title) {
			this.title = (String) context.getText(title);
			return this;
		}

		/**
		 * Set the Dialog title from String
		 * 
		 * @param title
		 * @return
		 */
		public Builder setTitle(String title) {
			this.title = title;
			return this;
		}

		/**
		 * Set a custom content view for the Dialog. If a message is set, the
		 * contentView is not added to the Dialog...
		 * 
		 * @param v
		 * @return
		 */
		public Builder setContentView(View v) {
			this.contentView = v;
			return this;
		}

		public Builder setWhichViewVisible(int whichViewVisible) {
			this.whichViewVisible = whichViewVisible;
			return this;
		}
		
		public Builder setLayoutID(int layoutID) {
			this.layoutID = layoutID;
			return this;
		}
		
		public Builder setListViewData(List<Map<String, Object>> listViewData) {
			this.listViewData = listViewData;
			return this;
		}
		
		public Builder setOnItemClickListener(OnItemClickListener itemClickListener) {
			this.itemClickListener = itemClickListener;
			return this;
		}


		public Builder setCheckBoxText(String checkBoxText) {
			this.checkBoxText = checkBoxText;
			return this;
		}
		
		public Builder setCheckBoxListener(CompoundButton.OnCheckedChangeListener CheckBoxListener)
		{
			this.CheckBoxListener = CheckBoxListener;
			return this;
		}
		
		/**
		 * Set the positive button resource and it's listener
		 * 
		 * @param positiveButtonText
		 * @param listener
		 * @return
		 */
		public Builder setPositiveButton(int positiveButtonText, DialogInterface.OnClickListener listener) {
			this.positiveButtonText = (String) context.getText(positiveButtonText);
			this.positiveButtonClickListener = listener;
			return this;
		}

		/**
		 * Set the positive button text and it's listener
		 * 
		 * @param positiveButtonText
		 * @param listener
		 * @return
		 */
		public Builder setPositiveButton(String positiveButtonText, DialogInterface.OnClickListener listener) {
			this.positiveButtonText = positiveButtonText;
			this.positiveButtonClickListener = listener;
			return this;
		}

		/**
		 * Set the negative button resource and it's listener
		 * 
		 * @param negativeButtonText
		 * @param listener
		 * @return
		 */
		public Builder setNegativeButton(int negativeButtonText, DialogInterface.OnClickListener listener) {
			this.negativeButtonText = (String) context.getText(negativeButtonText);
			this.negativeButtonClickListener = listener;
			return this;
		}

		/**
		 * Set the negative button text and it's listener
		 * 
		 * @param negativeButtonText
		 * @param listener
		 * @return
		 */
		public Builder setNegativeButton(String negativeButtonText, DialogInterface.OnClickListener listener) {
			this.negativeButtonText = negativeButtonText;
			this.negativeButtonClickListener = listener;
			return this;
		}

		/**
		 * Set the negative button resource and it's listener
		 * 
		 * @param negativeButtonText
		 * @param listener
		 * @return
		 */
		public Builder setNeutralButton(int neutralButtonText, DialogInterface.OnClickListener listener) {
			this.neutralButtonText = (String) context.getText(neutralButtonText);
			this.neutralButtonClickListener = listener;
			return this;
		}

		/**
		 * Set the negative button text and it's listener
		 * 
		 * @param negativeButtonText
		 * @param listener
		 * @return
		 */
		public Builder setNeutralButton(String neutralButtonText, DialogInterface.OnClickListener listener) {
			this.neutralButtonText = neutralButtonText;
			this.neutralButtonClickListener = listener;
			return this;
		}
		
		public Builder setLayoutXml(int xml) {
			this.layoutXml = xml;
			return this;
		}

		/**
		 * Create the custom dialog
		 */
		public CustomDialog create() {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// instantiate the dialog with the custom Theme
			final CustomDialog dialog = new CustomDialog(context, R.style.CustomDialog);
			View layout = inflater.inflate(layoutXml, null);
			dialog.addContentView(layout, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			// set the dialog title
			((TextView) layout.findViewById(R.id.title)).setText(title);

			// set the confirm button
			if (positiveButtonText != null) {
				((Button) layout.findViewById(R.id.positiveButton)).setText(positiveButtonText);
				if (positiveButtonClickListener != null) {
					((Button) layout.findViewById(R.id.positiveButton)).setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							positiveButtonClickListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
						}
					});
				}
			} else {
				// if no confirm button just set the visibility to GONE
				layout.findViewById(R.id.positiveButton).setVisibility(View.GONE);
			}
			// set the cancel button
			if (negativeButtonText != null) {
				((Button) layout.findViewById(R.id.negativeButton)).setText(negativeButtonText);
				if (negativeButtonClickListener != null) {
					((Button) layout.findViewById(R.id.negativeButton)).setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							negativeButtonClickListener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
						}
					});
				}
			} else {
				// if no confirm button just set the visibility to GONE
				layout.findViewById(R.id.negativeButton).setVisibility(View.GONE);
			}

			if (neutralButtonText != null) {
				((Button) layout.findViewById(R.id.neutralButton)).setText(neutralButtonText);
				if (neutralButtonClickListener != null) {
					((Button) layout.findViewById(R.id.neutralButton)).setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							neutralButtonClickListener.onClick(dialog, DialogInterface.BUTTON_NEUTRAL);
						}
					});
				}
			} else {
				// if no confirm button just set the visibility to GONE
				layout.findViewById(R.id.neutralButton).setVisibility(View.GONE);
			} 
			if(positiveButtonText == null && negativeButtonText == null && neutralButtonText == null) {
				layout.findViewById(R.id.dialog_line).setVisibility(View.GONE);
			}
			
			if (CheckBoxListener != null)
			{
				((CheckBox) layout.findViewById(R.id.checkBox)).setOnCheckedChangeListener(CheckBoxListener);
			}
			
			// set the content message
			switch (whichViewVisible) {
			case contentIsTextView:
				mTextView = (TextView) layout.findViewById(R.id.textView);
				mTextView.setVisibility(View.VISIBLE);
				if(message != null){
					mTextView.setText(message);
				}
				break;
			case contentIsListView:
				mListView = (ListView) layout.findViewById(R.id.listView);
				mListView.setVisibility(View.VISIBLE);
				if(listViewData != null){
					mListView.setAdapter(new MyAdapter(context, layoutID, listViewData));
					mListView.setOnItemClickListener(itemClickListener);
				}
				break;
			case contentIsEditText:
				mEditText = (EditText) layout.findViewById(R.id.editText);
				mEditText.setVisibility(View.VISIBLE);
				break;
			case contentIsCheckBox:
				if(message != null){
					mTextView = (TextView) layout.findViewById(R.id.textView);
					mTextView.setVisibility(View.VISIBLE);
					mTextView.setGravity(Gravity.CENTER);
					mTextView.setText(message);
				}
				mCheckBox = (CheckBox) layout.findViewById(R.id.checkBox);
				mCheckBox.setText(checkBoxText);
				mCheckBox.setVisibility(View.VISIBLE);
				if(checked) {
					mCheckBox.setChecked(checked);
				}
				break;
			case contentIsProgressBar:
				mProgressBar = (ProgressBar) layout.findViewById(R.id.scan_progress); 
				mProgressBar.setMax(100);
				mProgressBar.setVisibility(View.VISIBLE);
				
				mTextView = (TextView) layout.findViewById(R.id.scantextView);
				mTextView.setVisibility(View.VISIBLE);
				if(message != null){
					mTextView.setText(message);
				}
				break;
			}
			if (contentView != null) {
				// add the contentView to the dialog body
				((LinearLayout) layout.findViewById(R.id.content)).removeAllViews();
				((LinearLayout) layout.findViewById(R.id.content)).addView(contentView, new LayoutParams(LayoutParams.FILL_PARENT,
						LayoutParams.FILL_PARENT));
			}
			dialog.setContentView(layout);
			return dialog;
		}

	
    public static class ViewHolder {
		public TextView title;
		public EditText info;
		public int caseId;
	}
	
	class MyAdapter extends BaseAdapter {

		private LayoutInflater mInflater;
		private int layoutID;
		private List<Map<String, Object>> mData;

		public MyAdapter(Context context, int layoutID, List<Map<String, Object>> mData) {
			this.mInflater = LayoutInflater.from(context);
			this.layoutID = layoutID;
			this.mData = mData;
		}
		
		@Override
		public int getCount() {
			return mData.size();
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
			ViewHolder holder = new ViewHolder();
			String str = (String) mData.get(position).get("title");
			Drawable img = null;
			
			if (layoutID == LISTVIEW_ITEM_TEXTVIEW) {
				convertView = mInflater.inflate(R.layout.custom_dialog_listview_item2, null);
			} else if(layoutID == LISTVIEW_ITEM_TEXTVIEW_AND_EDITTEXT){
				convertView = mInflater.inflate(R.layout.custom_dialog_listview_item1, null);
				holder.info = (EditText) convertView.findViewById(R.id.info);
			}
			holder.title = (TextView) convertView.findViewById(R.id.title);
			if (str.equals(context.getResources().getString(R.string.screen_audio_download_pause)))
			{
				img = context.getResources().getDrawable(R.drawable.screen_audio_download_pause);
			}
			else if (str.equals(context.getResources().getString(R.string.screen_audio_download_cancel)))
			{
				img = context.getResources().getDrawable(R.drawable.screen_audio_download_cancel);
			}
			else if (str.equals(context.getResources().getString(R.string.screen_audio_download_continue)))
			{
				img = context.getResources().getDrawable(R.drawable.screen_audio_download_continue);
			}
			else if (str.equals(context.getResources().getString(R.string.screen_audio_download_delete)))
			{
				img = context.getResources().getDrawable(R.drawable.screen_audio_song_options_delete_icon);
			}
			else if (str.equals(context.getResources().getString(R.string.screen_audio_download_accompany)))
			{
				img = context.getResources().getDrawable(R.drawable.screen_kmedia_song_options_accompaniment_icon);
			}
			else if (str.equals(context.getResources().getString(R.string.screen_audio_download_play_orginal)))
			{
				img = context.getResources().getDrawable(R.drawable.screen_audio_download_continue);
			}
			if (img != null)
			{
				img.setBounds(15, 0, img.getMinimumWidth()+15, img.getMinimumHeight());
				holder.title.setCompoundDrawables(img, null, null, null);
				holder.title.setCompoundDrawablePadding(30);
			}
			holder.title.setText(str);
			Map map = mData.get(position);
			if(map.containsKey("caseId")){
				holder.caseId = (Integer)mData.get(position).get("caseId");
			}
			convertView.setTag(holder);
			return convertView;
		}

		}
		
	}

}
