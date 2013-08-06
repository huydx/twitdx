package com.cookpadintern.twitdx.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.UserStreamAdapter;
import twitter4j.conf.Configuration;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.cookpadintern.twitdx.R;
import com.cookpadintern.twitdx.activity.helper.TimelineActivityHelper;
import com.cookpadintern.twitdx.common.Const;
import com.cookpadintern.twitdx.common.Utils;
import com.cookpadintern.twitdx.customize.BaseActivity;
import com.cookpadintern.twitdx.customize.MainApplication;
import com.cookpadintern.twitdx.customize.TweetListviewAdapter;
import com.cookpadintern.twitdx.model.TwitterAccount;

public class TimelineActivity extends BaseActivity implements OnClickListener {
    protected MainApplication mMainApp;

    private static TwitterStream sTwitterStream;
    private static Twitter sTwitter;
    private TwitterAccount mAccount;

    private LinearLayout mMenu;
    private LinearLayout mContent;
    private LinearLayout.LayoutParams mContentParams;
    private ImageButton mMenuBtn;
    private ImageButton mPostBtn;
    private ImageButton mRefreshButton;

    private Button mTimelineBtn;
    private Button mMentionBtn;
    private Button mLogoutBtn;
    private Button mAboutBtn;
    private ListView mListView;

    private ArrayList<HashMap<String, String>> mTweets = null;
    private ArrayList<HashMap<String, String>> mMentions = null;

    private TweetListviewAdapter mTweetAdapter;
    private TweetListviewAdapter mMentionAdapter;

    private int mMenuWidth = 0;
    private int mCurrentScreenId;

    private TimelineActivityHelper mActivityHelper;

    /**
     * ************************* 
     * Activity default override methods
     * *************************
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);

        mActivityHelper = new TimelineActivityHelper(this);

        initViews();
        initActivityOrStartLogin();
    }

    private void initViews() {
        mListView = (ListView) findViewById(R.id.tweetListView);
        mMenu = (LinearLayout) findViewById(R.id.menu);
        mContent = (LinearLayout) findViewById(R.id.content);
        mTimelineBtn = (Button) findViewById(R.id.btn_timeline);
        mMentionBtn = (Button) findViewById(R.id.btn_mention);
        mAboutBtn = (Button) findViewById(R.id.btn_about);
        mLogoutBtn = (Button) findViewById(R.id.btn_logout);
        mPostBtn = (ImageButton) findViewById(R.id.tweet_button);
        mRefreshButton = (ImageButton) findViewById(R.id.refresh_button);

        mCurrentScreenId = R.id.btn_timeline;

        mMenuWidth = mMenu.getLayoutParams().width;
        mContentParams = (LinearLayout.LayoutParams) mContent.getLayoutParams();
        mContentParams.width = this.getResources().getDisplayMetrics().widthPixels;
        mContentParams.leftMargin = -mMenuWidth;
        mContent.setLayoutParams(mContentParams);
        // find and set listener for btn_menu
        mMenuBtn = (ImageButton) findViewById(R.id.menu_button);

        mMenuBtn.setOnClickListener(this);
        mTimelineBtn.setOnClickListener(this);
        mMentionBtn.setOnClickListener(this);
        mAboutBtn.setOnClickListener(this);
        mLogoutBtn.setOnClickListener(this);
        mPostBtn.setOnClickListener(this);
        mRefreshButton.setOnClickListener(this);
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

            setTimelineToView();
            startStreamingTimeline();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Const.LOGIN_REQUEST:
                break;
            default:
                break;
        }
    }

    private TimelineActivityHelper.TweetDialogClickListener mTweetDialogClickListener =
            new TimelineActivityHelper.TweetDialogClickListener() {
        @Override
        public void onOkClick(String tweet) {
            UpdateStatusTask updateTask = new UpdateStatusTask();
            updateTask.execute(tweet);
        }

        @Override
        public void onCancelClick() {}
    };

    @Override
    public void onClick(View v) {
        Button c = (Button) findViewById(mCurrentScreenId);
        switch (v.getId()) {
            case R.id.tweet_button:
                mActivityHelper.openTweetDialog(mTweetDialogClickListener);
                return;

            case R.id.refresh_button:
                if (mCurrentScreenId == R.id.btn_timeline) { //at timeline screen
                    mTweetAdapter.notifyDataSetChanged();
                    mListView.invalidateViews();
                }
                return;

            case R.id.btn_about:
                c.setTextColor(Color.parseColor(getString(R.string.BtnTextNormalColor)));
                mAboutBtn.setTextColor(Color.parseColor(getString(R.string.BtnTextPressedColor)));
                mCurrentScreenId = v.getId();
                // go to about
                break;

            case R.id.btn_timeline:
                c.setTextColor(Color.parseColor(getString(R.string.BtnTextNormalColor)));
                mTimelineBtn.setTextColor(Color.parseColor(getString(R.string.BtnTextPressedColor)));
                mCurrentScreenId = v.getId();
                mActivityHelper.slideMenuAnimate(mContent, mMenuWidth);
                setTimelineToView();
                mRefreshButton.setVisibility(View.VISIBLE);
                return;

            case R.id.btn_mention:
                c.setTextColor(Color.parseColor(getString(R.string.BtnTextNormalColor)));
                mMentionBtn.setTextColor(Color.parseColor(getString(R.string.BtnTextPressedColor)));
                mCurrentScreenId = v.getId();
                mActivityHelper.slideMenuAnimate(mContent, mMenuWidth);
                setMentionListview();
                mRefreshButton.setVisibility(View.GONE);
                return;

            case R.id.btn_logout:
                c.setTextColor(Color.parseColor(getString(R.string.BtnTextNormalColor)));
                mLogoutBtn.setTextColor(Color.parseColor(getString(R.string.BtnTextPressedColor)));
                mCurrentScreenId = mTimelineBtn.getId();
                logOut();
                break;
            default:
                mActivityHelper.slideMenuAnimate(mContent, mMenuWidth);
                break;
        }
    }

    private void logOut() {
        mAccount.logOut();
        startActivity(new Intent(TimelineActivity.this, LoginActivity.class));
    }

    /**
     * ************************* 
     * Twitter API wrapper methods
     * *************************
     */
    private void setTimelineToView() {
        FetchTimelineTask task = new FetchTimelineTask();
        task.execute();
    }

    private void setMentionListview() {
        FetchMentionTask task = new FetchMentionTask();
        task.execute();
    }

    public void startStreamingTimeline() {
        if (sTwitterStream == null) return;

        UserStreamAdapter streamAdapter = new UserStreamAdapter() {
            @Override
            public void onStatus(final Status status) {
                mTweets.add(0, mActivityHelper.makeStatusMap(status));
            }
        };

        sTwitterStream.addListener(streamAdapter);
        sTwitterStream.user();
    }

    /**
     * ************************* Background stuffs *************************
     */
    private class FetchMentionTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDialog;

        @Override
        protected Void doInBackground(Void... params) {
            if (mMentions == null) {
                mMentions = new ArrayList<HashMap<String, String>>();
            }

            try {
                List<twitter4j.Status> mentions;
                mentions = sTwitter.getMentionsTimeline();
                for (twitter4j.Status status : mentions) {
                    mMentions.add(mActivityHelper.makeStatusMap(status));
                }
            } catch (TwitterException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(TimelineActivity.this, "", "Loading Mention...");
            return;
        }

        protected void onPostExecute(Void result) {
            Activity currentActivity = ((MainApplication) getApplicationContext())
                    .getCurrentActivity();
            mMentionAdapter = new TweetListviewAdapter(currentActivity, mMentions);
            mListView.setAdapter(mMentionAdapter);
            mMentionAdapter.notifyDataSetChanged();
            progressDialog.dismiss();
        }
    }


    private class FetchTimelineTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDialog;

        @Override
        protected Void doInBackground(Void... params) {
            List<twitter4j.Status> statuses;
            try {
                statuses = sTwitter.getHomeTimeline();
                mTweets = new ArrayList<HashMap<String, String>>();

                for (twitter4j.Status status : statuses) {
                    mTweets.add(mActivityHelper.makeStatusMap(status));
                }
            } catch (TwitterException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(TimelineActivity.this, "", "Loading Timeline...");
            return;
        }

        protected void onPostExecute(Void result) {
            Activity currentActivity = ((MainApplication) getApplicationContext())
                    .getCurrentActivity();
            mTweetAdapter = new TweetListviewAdapter(currentActivity, mTweets);
            mListView.setAdapter(mTweetAdapter);
            mTweetAdapter.notifyDataSetChanged();
            progressDialog.dismiss();
        }
    }

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
            Toast.makeText(currentActivity, Const.UPDATE_STATUS_SUCCESS, Toast.LENGTH_SHORT)
            .show();
        }
    }
}
