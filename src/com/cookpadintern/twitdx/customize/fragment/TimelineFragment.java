package com.cookpadintern.twitdx.customize.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListView;

import com.cookpadintern.twitdx.R;
import com.cookpadintern.twitdx.activity.TimelineActivity;
import com.cookpadintern.twitdx.activity.helper.TimelineActivityHelper;
import com.cookpadintern.twitdx.common.Const;
import com.cookpadintern.twitdx.customize.TweetListviewAdapter;

public class TimelineFragment extends TweetListViewFragment implements OnScrollListener {
	FetchNewPageTimelineTask mFetchPageTask;
	int mCurrentPage = 1;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		fetchNewPage();
		mListView.setOnScrollListener(this);
		mListView.setClickable(true);
		return mListView;
	}
	
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
			int totalItemCount) {
		if (visibleItemCount == 0)
			return; // for the first time

		boolean needLoadMore = firstVisibleItem + visibleItemCount >= totalItemCount;
		if (needLoadMore) {
			fetchNewPage();
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) { }

	/**********************************/
	//background stuff
	private class FetchNewPageTimelineTask extends AsyncTask<Void, Void, Void> {
		ProgressDialog progressDialog;
		
		@Override
		protected Void doInBackground(Void... params) {
			TimelineActivity act = ((TimelineActivity) getActivity());
			Paging paging = new Paging(mCurrentPage, 40);
			mCurrentPage++;
			
			Twitter twitterInstance = act.getTwitterApiInstance();
			ArrayList<HashMap<String, String>> tweetList = act.getTweetList();
			
			if (tweetList == null)
				tweetList = new ArrayList<HashMap<String, String>>();

			try {
				List<twitter4j.Status> statuses = twitterInstance.getHomeTimeline(paging);
				for (twitter4j.Status status : statuses) {
					tweetList.add(((TimelineActivityHelper) act.getActivityHelper())
							.makeStatusMap(status));
					act.setTweetList(tweetList);
				}
			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(getActivity(), "", "Loading Timeline...");
			return;
		}

		protected void onPostExecute(Void result) {
			TimelineActivity act = ((TimelineActivity) getActivity());
			if (act.getTweetList() == null) return;
			
			mAdapter = new TweetListviewAdapter(getActivity(), act.getTweetList());
			setUpAdapter();
			progressDialog.dismiss();
			mListView.setSelectionFromTop(mIndex, mTop);
		}

	}
	

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		TimelineActivity act = ((TimelineActivity) getActivity());
		HashMap<String, String> single_tweet = new HashMap<String, String>();
		ArrayList<HashMap<String, String>> tweetList = act.getTweetList();
		single_tweet = tweetList.get(position);
		
		String url = single_tweet.get(Const.KEY_MEDIA);
		if (url  != null) {
			displayPopUpImage(url);
		}
	}
	
	
	
	/**********************************/
	// private stuffs
	private void setUpAdapter() {
		this.setListAdapter(mAdapter);
	}

	private void fetchNewPage() {
		if (mFetchPageTask != null && mFetchPageTask.getStatus() == AsyncTask.Status.RUNNING) {
			return;
		}

		mFetchPageTask = new FetchNewPageTimelineTask();
		mIndex = mListView.getFirstVisiblePosition();
		View v = mListView.getChildAt(0);
		mTop = (v == null) ? 0 : v.getTop();
		mFetchPageTask.execute();
	}
	
	private void displayPopUpImage(String url) {
		Dialog dialog = new Dialog(getActivity());
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        View vi = mInflater.inflate(R.layout.popup_image, null);
        dialog.setContentView(vi);
        dialog.setCancelable(true);
        ImageView imgView = (ImageView) vi.findViewById(R.id.popup_image);
        mImageLoader.DisplayImage(url, imgView);
        dialog.show();
	}
	
	/*** helper public stuffs****/
	public void invalidate() {
		mAdapter.notifyDataSetChanged();
	}
}
