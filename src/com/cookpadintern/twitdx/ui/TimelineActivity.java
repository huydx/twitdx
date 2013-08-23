package com.cookpadintern.twitdx.ui;

import java.util.ArrayList;
import java.util.HashMap;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.UserStreamAdapter;
import twitter4j.conf.Configuration;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;

import com.cookpadintern.twitdx.R;
import com.cookpadintern.twitdx.common.Const;
import com.cookpadintern.twitdx.common.Utils;
import com.cookpadintern.twitdx.core_ext.BaseActivity;
import com.cookpadintern.twitdx.core_ext.MainApplication;
import com.cookpadintern.twitdx.model.TwitterAccount;
import com.cookpadintern.twitdx.ui.helper.TimelineActivityHelper;

public class TimelineActivity extends BaseActivity {
	enum FRAGMENT_TYPE {
		TIMELINE, MENTION, ABOUT, LOGOUT
	}

	private static final String TAG = "TimelineActivity";

	protected MainApplication mMainApp;

	private static TwitterStream sTwitterStream;
	private static Twitter sTwitter;

	private TwitterAccount mAccount;

	private ListView mLeftMenu;
	private DrawerLayout mTimelineLayout;
	private String[] mLeftMenuItems;
	private ActionBarDrawerToggle mMenuChangeToggle;
	private Fragment mCurrentFragment;
	private ArrayList<HashMap<String, String>> mTweets = null;
	private ArrayList<HashMap<String, String>> mMentions = null;
	

	/**
	 * ************************* Activity default override methods
	 * *************************
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timeline_view);

		initViews();
		initActivityOrStartLogin();
	}

	private void initViews() {
		mActivityHelper = new TimelineActivityHelper(this);
		mLeftMenu = (ListView) findViewById(R.id.left_menu);
		mTimelineLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mLeftMenuItems = getResources().getStringArray(R.array.leftMenuItems);

		mLeftMenu.setAdapter(new ArrayAdapter<String>(this, R.layout.left_menu, mLeftMenuItems));
		mLeftMenu.setOnItemClickListener(new DrawerItemClickListener());

		mMenuChangeToggle = new ActionBarDrawerToggle(this, /* host Activity */
		mTimelineLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
		R.string.drawer_open, /* "open drawer" description for accessibility */
		R.string.drawer_close /* "close drawer" description for accessibility */
		) {
			public void onDrawerClosed(View view) {
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				invalidateOptionsMenu();
			}
		};
		mTimelineLayout.setDrawerListener(mMenuChangeToggle);

	}

	private void initActivityOrStartLogin() {
		mAccount = getTwitdxApplication().getAccount();
		if (mAccount.isNotOnline() || !Utils.haveNetworkConnection(this)) {
			startActivity(new Intent(TimelineActivity.this, LoginActivity.class));
		} else {
			Configuration conf = mAccount.buildTwitterConfiguration();

			// first fetch current timeline
			sTwitter = new TwitterFactory(conf).getInstance();
			sTwitterStream = new TwitterStreamFactory(conf).getInstance();
			selectItem(0);
			startStreamingTimeline();
		}
	}

	private TimelineActivityHelper.TweetDialogClickListener mTweetDialogClickListener = new TimelineActivityHelper.TweetDialogClickListener() {
		@Override
		public void onOkClick(String tweet) {
			UpdateStatusTask updateTask = new UpdateStatusTask();
			updateTask.execute(tweet);
		}

		@Override
		public void onCancelClick() {
		}
	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (mMenuChangeToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle action buttons
		switch (item.getItemId()) {
		case R.id.action_post:
			try {
				((TimelineActivityHelper) mActivityHelper)
						.openTweetDialog(mTweetDialogClickListener);
				return true;
			} catch (Exception e) {
				Log.v(TAG, "some error occur at open tweet dialog");
				return false;
			}
		case R.id.action_refresh:
			if (mCurrentFragment instanceof TimelineFragment) {
				((TimelineFragment) mCurrentFragment).invalidate();
			}
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// TODO refactoring to helper later
	/* The click listner for ListView in the navigation drawer */
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectItem(position);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content
		boolean drawerOpen = mTimelineLayout.isDrawerOpen(mLeftMenu);
		menu.findItem(R.id.action_post).setVisible(!drawerOpen);
		menu.findItem(R.id.action_refresh).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mMenuChangeToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(android.content.res.Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mMenuChangeToggle.onConfigurationChanged(newConfig);
	}

	/**
	 * ************************* 
	 * Twitter API wrapper methods
	 * *************************
	 */
	public void startStreamingTimeline() {
		if (sTwitterStream == null)
			return;

		UserStreamAdapter streamAdapter = new UserStreamAdapter() {
			@Override
			public void onStatus(final Status status) {
				mTweets.add(0, ((TimelineActivityHelper) mActivityHelper).makeStatusMap(status));
			}
		};

		sTwitterStream.addListener(streamAdapter);
		sTwitterStream.user();
	}

	private void logOut() {
		mAccount.logOut();
		startActivity(new Intent(TimelineActivity.this, LoginActivity.class));
	}

	private void selectItem(int position) {
		// 0 timeline // 1 mention // 2 about // 3 log out
		switch (position) {
		case 0:
			setCenterFragment(FRAGMENT_TYPE.TIMELINE);
			break;
		case 1:
			setCenterFragment(FRAGMENT_TYPE.MENTION);
			break;
		case 2:
			setCenterFragment(FRAGMENT_TYPE.ABOUT);
			break;
		case 3:
			logOut();
			break;
		default:
			break;

		}
		mLeftMenu.setItemChecked(position, true);
		// drawerlayout have to close layout manually
		mTimelineLayout.closeDrawer(mLeftMenu);
	}

	public void setCenterFragment(FRAGMENT_TYPE type) {
		FragmentManager fragmentManager = getFragmentManager();

		switch (type) {
		case TIMELINE:
			TimelineFragment tmfragment = new TimelineFragment();
			fragmentManager.beginTransaction().replace(R.id.timline_content_fragment, tmfragment)
					.commit();
			mCurrentFragment = tmfragment;
			break;
		case MENTION:
			MentionFragment mtfragment = new MentionFragment();
			fragmentManager.beginTransaction().replace(R.id.timline_content_fragment, mtfragment)
				.commit();
			mCurrentFragment = mtfragment;
			break;
		case ABOUT:
			AboutFragment afragment = new AboutFragment();
			fragmentManager.beginTransaction().replace(R.id.timline_content_fragment, afragment)
				.commit();
			mCurrentFragment = afragment;
		default:
			break;
		}
	}

	/**
	 * ************************* 
	 * Background stuffs 
	 * *************************
	 */

	private class UpdateStatusTask extends AsyncTask<String, Void, Void> {
		@Override
		protected Void doInBackground(String... params) {
			String status = params[0];
			try {
				sTwitter.updateStatus(status);
			} catch (TwitterException e) {
				Activity currentActivity = ((MainApplication) getApplicationContext())
						.getCurrentActivity();
				Toast.makeText(currentActivity, Const.UPDATE_STATUS_ERROR, Toast.LENGTH_SHORT)
						.show();
			}
			return null;
		}

		protected void onPostExecute(Void result) {
			Activity currentActivity = ((MainApplication) getApplicationContext())
					.getCurrentActivity();
			Toast.makeText(currentActivity, Const.UPDATE_STATUS_SUCCESS, Toast.LENGTH_SHORT).show();
		}
	}

	/** get/set stuffs **/
	public Twitter getTwitterApiInstance() {
		return sTwitter;
	}

	public void setTwitterApiInstance(Twitter sTwitter) {
		TimelineActivity.sTwitter = sTwitter;
	}

	public ArrayList<HashMap<String, String>> getTweetList() {
		return mTweets;
	}

	public ArrayList<HashMap<String, String>> getMentions() {
		return mMentions;
	}

	public void setMentionsList(ArrayList<HashMap<String, String>> mMentions) {
		this.mMentions = mMentions;
	}

	public void setTweetList(ArrayList<HashMap<String, String>> mTweets) {
		this.mTweets = mTweets;
	}
}
