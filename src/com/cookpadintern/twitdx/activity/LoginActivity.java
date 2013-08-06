package com.cookpadintern.twitdx.activity;

import com.cookpadintern.twitdx.R;
import com.cookpadintern.twitdx.common.*;
import com.cookpadintern.twitdx.customize.BaseActivity;
import com.cookpadintern.twitdx.customize.MainApplication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.*;
import android.widget.*;
import twitter4j.*;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class LoginActivity extends BaseActivity implements OnClickListener {
    private ImageButton loginButton;
    private static Twitter mTwitter;
    private static RequestToken mRequestToken;
    private static SharedPreferences mSharedPreferences;

    /**
     * ************************* 
     * Activity default method
     * *************************
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.login);
        loginButton = (ImageButton) findViewById(R.id.btn_login);
        loginButton.setOnClickListener(this);

        mSharedPreferences = getSharedPreferences(Const.PREFERENCE_NAME, MODE_PRIVATE);
        FetchTokenTask fetchToken = new FetchTokenTask();
        fetchToken.execute();
    }

    @Override
    public void onClick(View v) {
        if (!Utils.haveNetworkConnection(this)) {
            Toast.makeText(this, Const.NETWORK_ERROR, Toast.LENGTH_LONG).show();
            return;
        }
        switch (v.getId()) {
            case (R.id.btn_login):
                OauthTask oauthExec = new OauthTask();
                oauthExec.execute();
                break;
            default:
                break;
        }
    }

    /**
     * ************************* 
     * Background stuffs
     * *************************
     */
    private class FetchTokenTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            mSharedPreferences = getSharedPreferences(Const.PREFERENCE_NAME, MODE_PRIVATE);

            // handle oAuth callback
            Uri uri = getIntent().getData();
            if (uri != null && uri.toString().startsWith(Const.CALLBACK_URL)) {
                String verifier = uri.getQueryParameter(Const.IEXTRA_OAUTH_VERIFIER);
                try {
                    AccessToken accessToken = mTwitter.getOAuthAccessToken(mRequestToken, verifier);
                    Editor e = mSharedPreferences.edit();
                    e.putString(Const.PREF_KEY_TOKEN, accessToken.getToken());
                    e.putString(Const.PREF_KEY_SECRET, accessToken.getTokenSecret());
                    e.putBoolean(Const.LOGGED_IN, true);
                    e.commit();
                    startActivity(new Intent(LoginActivity.this, TimelineActivity.class));
                } catch (Exception e) {
                    Activity currentActivity = ((MainApplication) getApplicationContext())
                            .getCurrentActivity();
                    Toast.makeText(currentActivity, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
            return null;
        }

        protected void onPostExecute(Void result) {

        }
    }

    private class OauthTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
            configurationBuilder.setOAuthConsumerKey(Const.CONSUMER_KEY);
            configurationBuilder.setOAuthConsumerSecret(Const.CONSUMER_SECRET);
            Configuration configuration = configurationBuilder.build();
            mTwitter = new TwitterFactory(configuration).getInstance();

            try {
                mRequestToken = mTwitter.getOAuthRequestToken(Const.CALLBACK_URL);                
            } catch (TwitterException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            Activity currentActivity = ((MainApplication) getApplicationContext())
                    .getCurrentActivity();
            currentActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mRequestToken
                    .getAuthenticationURL())));
        }
    }
}
