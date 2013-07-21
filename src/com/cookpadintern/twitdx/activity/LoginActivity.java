package com.cookpadintern.twitdx.activity;

import com.cookpadintern.twitdx.R;
import com.cookpadintern.twitdx.common.*;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.*;
import android.widget.*;
import twitter4j.*;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class LoginActivity extends Activity implements OnClickListener {
    private ImageButton loginButton;
    private static Twitter mTwitter;
    private static RequestToken mRequestToken;
    private static SharedPreferences mSharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        loginButton = (ImageButton) findViewById(R.id.btn_login);
        loginButton.setOnClickListener(this);
        
        //[TODO] detect Internet first
        
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
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case (R.id.btn_login):
            oauthExec();
            break;
        default:
            break;
        }

    }

    private void oauthExec() {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setOAuthConsumerKey(Const.CONSUMER_KEY);
        configurationBuilder.setOAuthConsumerSecret(Const.CONSUMER_SECRET);
        Configuration configuration = configurationBuilder.build();
        mTwitter = new TwitterFactory(configuration).getInstance();

        try {
            mRequestToken = mTwitter.getOAuthRequestToken(Const.CALLBACK_URL); //[TODO]separate to AsyncTask for android 4
            Toast.makeText(this, "Please authorize this app!", Toast.LENGTH_LONG).show();
            this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mRequestToken
                    .getAuthenticationURL())));
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }
   
}
