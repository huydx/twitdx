package com.cookpadintern.twitdx.customize;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cookpadintern.twitdx.R;
import com.cookpadintern.twitdx.common.Const;
import com.cookpadintern.twitdx.common.ImageLoader;

public class TweetListviewAdapter extends BaseAdapter {

	protected static final String TAG = "TweetListviewAdapter";
	private Activity mActivity;
	private ArrayList<HashMap<String, String>> mTweets;
	private static LayoutInflater sInflater = null;
	public ImageLoader imageLoader;

	public TweetListviewAdapter(Activity activity, ArrayList<HashMap<String, String>> tweetList) {
		mActivity = activity;
		mTweets = tweetList;
		sInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		boolean needDownSample = false;
		imageLoader = new ImageLoader(mActivity.getApplicationContext(), needDownSample);
	}

	@Override
	public int getCount() {
		return mTweets.size();
	}

	@Override
	public Object getItem(int position) {
		return mTweets.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (convertView == null) {
			view = sInflater.inflate(R.layout.single_tweet, null);
		}
		HashMap<String, String> single_tweet = new HashMap<String, String>();
		single_tweet = mTweets.get(position);
		
		TextView uname = (TextView) view.findViewById(R.id.uname);
		TextView tweet = (TextView) view.findViewById(R.id.tweet);
		TextView date = (TextView) view.findViewById(R.id.date);
		ImageView hasPopUpView = (ImageView) view.findViewById(R.id.has_popup);
		
		int displayPopupIcon = (single_tweet.get(Const.KEY_MEDIA)) == null ? View.INVISIBLE : View.VISIBLE;
		hasPopUpView.setVisibility(displayPopupIcon);
		
		ImageView thumb_image = (ImageView) view.findViewById(R.id.avatar);

		uname.setText(single_tweet.get(Const.KEY_UNAME));
		tweet.setText(single_tweet.get(Const.KEY_TWEET));
		date.setText(single_tweet.get(Const.KEY_DATE));

		imageLoader.DisplayImage(single_tweet.get(Const.KEY_AVATAR), thumb_image);
		return view;
	}


}