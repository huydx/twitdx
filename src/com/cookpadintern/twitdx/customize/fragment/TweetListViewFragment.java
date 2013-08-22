package com.cookpadintern.twitdx.customize.fragment;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.cookpadintern.twitdx.R;
import com.cookpadintern.twitdx.common.ImageLoader;
import com.cookpadintern.twitdx.customize.TweetListviewAdapter;

public class TweetListViewFragment extends ListFragment {
	protected static final String TAG = "TweetListViewFragment";
	TweetListviewAdapter mAdapter;
	ListView mListView;
	int mIndex, mTop;
	LayoutInflater mInflater;
	ImageLoader mImageLoader;

	public TweetListViewFragment() {
	}

	/*********************************************/
	// default stuffs
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mInflater = inflater;
		mListView = (ListView) inflater.inflate(R.layout.tweet_list_fragment, container, false);
		boolean needDownSample = false;
		mImageLoader = new ImageLoader(getActivity().getApplicationContext(), needDownSample);
		return mListView;
	}

	
}
