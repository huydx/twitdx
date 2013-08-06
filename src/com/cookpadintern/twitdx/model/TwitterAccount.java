package com.cookpadintern.twitdx.model;

import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.cookpadintern.twitdx.common.Const;

public class TwitterAccount {
    private static final String PREFERENCE_NAME = "twitter_oauth";
    private static final String PREF_KEY_SECRET = "oauth_token_secret";
    private static final String PREF_KEY_TOKEN = "oauth_token";

    private SharedPreferences sSharedPreferences;

    public TwitterAccount(Context context) {
        sSharedPreferences = context.getSharedPreferences(
                PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    public void saveToken(AccessToken accessToken) {
        Editor editor = sSharedPreferences.edit();
        editor.putString(PREF_KEY_TOKEN, accessToken.getToken());
        editor.putString(PREF_KEY_SECRET, accessToken.getTokenSecret());
        editor.putBoolean(Const.LOGGED_IN, true);
        editor.commit();
    }

    public String getAccessToken() {
        return sSharedPreferences.getString(PREF_KEY_TOKEN, "");
    }

    public String getAccessTokenSecret() {
        return sSharedPreferences.getString(PREF_KEY_SECRET, "");
    }

    public boolean isOnline() {
        return sSharedPreferences.getString(PREF_KEY_TOKEN, null) != null;
    }

    public boolean isNotOnline() {
        return !isOnline();
    }

    public void logOut() {
        Editor editor = sSharedPreferences.edit();
        editor.clear();
        editor.commit();
    }

    public Configuration buildTwitterConfiguration() {
        String oauthAccessToken = getAccessToken();
        String oAuthAccessTokenSecret = getAccessTokenSecret();

        ConfigurationBuilder confbuilder = new ConfigurationBuilder();
        Configuration conf = confbuilder.setOAuthConsumerKey(Const.CONSUMER_KEY)
                .setOAuthConsumerSecret(Const.CONSUMER_SECRET)
                .setOAuthAccessToken(oauthAccessToken)
                .setOAuthAccessTokenSecret(oAuthAccessTokenSecret).build();

        return conf;
    }
}
