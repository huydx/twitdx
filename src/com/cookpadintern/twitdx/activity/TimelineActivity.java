package com.cookpadintern.twitdx.activity;

import twitter4j.DirectMessage;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.*;

import com.cookpadintern.twitdx.R;
import com.cookpadintern.twitdx.common.Const;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ScrollView;


public class TimelineActivity extends Activity {
    private static SharedPreferences mSharedPreferences;
    private static TwitterStream mTwitterStream;
    private ScrollView mScrollView;
    private TextView mTextView;
    private LinearLayout mMenu;
    private LinearLayout mContent;
    private LinearLayout.LayoutParams mContentParams;
    private TranslateAnimation mSlide;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mScrollView = (ScrollView) findViewById(R.id.scrollView);
        mTextView = (TextView) findViewById(R.id.tweetText);
        
        mSharedPreferences = getSharedPreferences(Const.PREFERENCE_NAME, MODE_PRIVATE);
        if (!isOnline()) {
            startActivityForResult(new Intent(TimelineActivity.this, LoginActivity.class), Const.LOGIN_REQUEST);
        } else {
            String oauthAccessToken = mSharedPreferences.getString(Const.PREF_KEY_TOKEN, "");
            String oAuthAccessTokenSecret = mSharedPreferences.getString(Const.PREF_KEY_SECRET, "");

            ConfigurationBuilder confbuilder = new ConfigurationBuilder();
            Configuration conf = confbuilder
                    .setOAuthConsumerKey(Const.CONSUMER_KEY)
                    .setOAuthConsumerSecret(Const.CONSUMER_SECRET)
                    .setOAuthAccessToken(oauthAccessToken)
                    .setOAuthAccessTokenSecret(oAuthAccessTokenSecret).build();
            mTwitterStream = new TwitterStreamFactory(conf).getInstance();
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
    
    private boolean isOnline() {
        return mSharedPreferences.getString(Const.PREF_KEY_TOKEN, null) != null;
    }
    
    public void startStreamingTimeline() {
        UserStreamListener listener = new UserStreamListener() {
            @Override
            public void onStatus(Status status) {
                final String tweet = "@" + status.getUser().getScreenName() + " : " + status.getText() + "\n"; 
                mTextView.post(new Runnable() {
                    @Override
                    public void run() {
                        mTextView.append(tweet);
                        mScrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });              
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
