package com.cookpadintern.twitdx.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import twitter4j.DirectMessage;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.*;

import com.cookpad.intern.twitdx.customize.MainApplication;
import com.cookpad.intern.twitdx.customize.TweetListviewAdapter;
import com.cookpadintern.twitdx.R;
import com.cookpadintern.twitdx.common.*;
import com.cookpad.intern.twitdx.customize.*;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ImageButton;

public class TimelineActivity extends BaseActivity implements OnClickListener {
    protected MainApplication mMainApp;
    
    private static SharedPreferences mSharedPreferences;
    private static TwitterStream mTwitterStream;
    private static Twitter mTwitter;

    private LinearLayout mMenu;
    private LinearLayout mContent;
    private LinearLayout.LayoutParams mContentParams;
    private TranslateAnimation mSlide;
    private ImageButton mMenuBtn;
    private ImageButton mPostBtn;
    private Button mTimelineBtn;
    private Button mMentionBtn;
    private Button mLogoutBtn;
    private Button mAboutBtn;
    private ListView mListView;
    
    private ArrayList<HashMap<String, String>> mTweets;
    private TweetListviewAdapter mTweetAdapter; 
    
    private int mMenuWidth = 0;
    private int mCurrentScreenId;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        mListView = (ListView) findViewById(R.id.tweetListView);
        mMenu = (LinearLayout) findViewById(R.id.menu);
        mContent = (LinearLayout) findViewById(R.id.content);
        mTimelineBtn = (Button) findViewById(R.id.btn_timeline);
        mMentionBtn = (Button) findViewById(R.id.btn_mention);
        mAboutBtn = (Button) findViewById(R.id.btn_about);
        mLogoutBtn = (Button) findViewById(R.id.btn_logout);
        mPostBtn = (ImageButton) findViewById(R.id.tweet_button);
        
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
        
        mSharedPreferences = getSharedPreferences(Const.PREFERENCE_NAME, MODE_PRIVATE);
        if (!isOnline()) {
            startActivity(new Intent(TimelineActivity.this, LoginActivity.class));
        } else {
            String oauthAccessToken = mSharedPreferences.getString(Const.PREF_KEY_TOKEN, "");
            String oAuthAccessTokenSecret = mSharedPreferences.getString(Const.PREF_KEY_SECRET, "");

            ConfigurationBuilder confbuilder = new ConfigurationBuilder();
            Configuration conf = confbuilder.setOAuthConsumerKey(Const.CONSUMER_KEY)
                    .setOAuthConsumerSecret(Const.CONSUMER_SECRET)
                    .setOAuthAccessToken(oauthAccessToken)
                    .setOAuthAccessTokenSecret(oAuthAccessTokenSecret).build();

            // first fetch current timeline
            mTwitter = new TwitterFactory(conf).getInstance();
            List<Status> statuses;
            try {
                statuses = mTwitter.getHomeTimeline();
                setTimelineToView(statuses);

                // then stream
                mTwitterStream = new TwitterStreamFactory(conf).getInstance();
                startStreamingTimeline();
            } catch (TwitterException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                // goto login

            }
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

    @Override
    public void onClick(View v) {
        Button c = (Button) findViewById(mCurrentScreenId);
        switch (v.getId()) {
        case R.id.tweet_button:
            openTweetDialog();
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
            // refresh time line
            break;
        case R.id.btn_mention:
            c.setTextColor(Color.parseColor(getString(R.string.BtnTextNormalColor)));
            mMentionBtn.setTextColor(Color.parseColor(getString(R.string.BtnTextPressedColor)));
            mCurrentScreenId = v.getId();
            // go to mention screen
            break;
        case R.id.btn_logout:
            c.setTextColor(Color.parseColor(getString(R.string.BtnTextNormalColor)));
            mLogoutBtn.setTextColor(Color.parseColor(getString(R.string.BtnTextPressedColor)));
            mCurrentScreenId = mTimelineBtn.getId();
            logOut();
            break;

        }
        int marginX, animateFromX, animateToX = 0;
        // menu is hidden
        if (mContentParams.leftMargin == -mMenuWidth) {
            animateFromX = 0;
            animateToX = mMenuWidth;
            marginX = 0;
        } else { // menu is visible
            animateFromX = 0;
            animateToX = -mMenuWidth;
            marginX = -mMenuWidth;
        }
        slideMenuIn(animateFromX, animateToX, marginX);
    }
    
    private void openTweetDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Title");
        alert.setMessage("Message");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                return;
            }
        });

        alert.show();

    }

    private void slideMenuIn(int animateFromX, int animateToX, final int marginX) {
        mSlide = new TranslateAnimation(animateFromX, animateToX, 0, 0);
        mSlide.setDuration(200);
        mSlide.setFillEnabled(true);
        mSlide.setAnimationListener(new AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                mContentParams.setMargins(marginX, 0, 0, 0);
                mContent.setLayoutParams(mContentParams);
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationStart(Animation animation) {
            }
        });
        mContent.startAnimation(mSlide);
    }

    private boolean isOnline() {
        return mSharedPreferences.getString(Const.PREF_KEY_TOKEN, null) != null;
    }

    private void setTimelineToView(List<Status> statuses) {
        mTweets = new ArrayList<HashMap<String, String>>();

        for (Status status : statuses) {
            HashMap<String, String> map = new HashMap<String, String>();

            map.put(Const.KEY_UNAME, status.getUser().getScreenName());
            map.put(Const.KEY_TWEET, status.getText());
            map.put(Const.KEY_DATE, status.getCreatedAt().toString());
            map.put(Const.KEY_AVATAR, status.getUser().getProfileImageURL());
            
            mTweets.add(map);
        }
        
        mTweetAdapter = new TweetListviewAdapter(this, mTweets);   
        mListView.setAdapter(mTweetAdapter);
    }

    private void logOut() {
        Editor e = mSharedPreferences.edit();
        e.clear();
        e.commit();
        startActivity(new Intent(TimelineActivity.this, LoginActivity.class));
    }

    public void startStreamingTimeline() {
        UserStreamListener listener = new UserStreamListener() {
            @Override
            public void onStatus(final Status status) {
                //there are odd behaviours if add dynamically to list
//                HashMap<String, String> map = new HashMap<String, String>();
//
//                map.put(Const.KEY_UNAME, status.getUser().getScreenName());
//                map.put(Const.KEY_TWEET, status.getText());
//                map.put(Const.KEY_DATE, status.getCreatedAt().toString());
//                map.put(Const.KEY_AVATAR, status.getUser().getProfileImageURL());
//                
//                mTweets.add(0, map);
//                mTweetAdapter.notifyDataSetChanged();
            }

            @Override
            public void onUserListSubscription(User arg0, User arg1, UserList arg2) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onScrubGeo(long arg0, long arg1) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStallWarning(StallWarning arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onTrackLimitationNotice(int arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onException(Exception arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onBlock(User arg0, User arg1) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onDeletionNotice(long arg0, long arg1) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onDirectMessage(DirectMessage arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onFavorite(User arg0, User arg1, Status arg2) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onFollow(User arg0, User arg1) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onFriendList(long[] arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onUnblock(User arg0, User arg1) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onUnfavorite(User arg0, User arg1, Status arg2) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onUserListCreation(User arg0, UserList arg1) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onUserListDeletion(User arg0, UserList arg1) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onUserListMemberAddition(User arg0, User arg1, UserList arg2) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onUserListMemberDeletion(User arg0, User arg1, UserList arg2) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onUserListUnsubscription(User arg0, User arg1, UserList arg2) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onUserListUpdate(User arg0, UserList arg1) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onUserProfileUpdate(User arg0) {
                // TODO Auto-generated method stub

            }
        };
        mTwitterStream.addListener(listener);
        mTwitterStream.user();
    }
}
