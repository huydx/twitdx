package com.cookpadintern.twitdx.customize.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cookpadintern.twitdx.activity.TimelineActivity;
import com.cookpadintern.twitdx.activity.helper.TimelineActivityHelper;
import com.cookpadintern.twitdx.customize.TweetListviewAdapter;

public class MentionFragment extends TweetListViewFragment{
	private ArrayList<HashMap<String, String>> mMentions = null;
	private TweetListviewAdapter mMentionAdapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		setMentionList();
		return mListView;
	}
	
	private void setMentionList() {
		FetchMentionTask task = new FetchMentionTask();
		task.execute();
	}
	
	
	private class FetchMentionTask extends AsyncTask<Void, Void, Void> {
		ProgressDialog progressDialog;

		@Override
		protected Void doInBackground(Void... params) {
			TimelineActivity act = ((TimelineActivity) getActivity());
			mMentions = act.getMentions();
			
			if (mMentions == null) {
				mMentions = new ArrayList<HashMap<String, String>>();
			}

			try {
				Twitter twitterInstance = act.getTwitterApiInstance();

				List<twitter4j.Status> mentions;
				mentions = twitterInstance.getMentionsTimeline();
				for (twitter4j.Status status : mentions) {
					mMentions.add(((TimelineActivityHelper) act.getActivityHelper()).makeStatusMap(status));
				}
			} catch (TwitterException e) {
				e.printStackTrace();
			}
			return null;
		}

		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(getActivity(), "", "Loading Mention...");
			return;
		}

		protected void onPostExecute(Void result) {
			mMentionAdapter = new TweetListviewAdapter(getActivity(), mMentions);
			mListView.setAdapter(mMentionAdapter);
			mMentionAdapter.notifyDataSetChanged();
			progressDialog.dismiss();
		}
	}

}
