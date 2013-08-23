package com.cookpadintern.twitdx.ui;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.Toast;

import com.cookpadintern.twitdx.R;
import com.cookpadintern.twitdx.common.Const;
import com.cookpadintern.twitdx.common.Utils;
import com.cookpadintern.twitdx.core_ext.BaseActivity;
import com.cookpadintern.twitdx.core_ext.MainApplication;

public class LoginActivity extends BaseActivity implements OnClickListener {
	private ImageButton mLoginButton;
	private static Twitter sTwitter;
	private static RequestToken sRequestToken;

	/**
	 * ************************* Activity default method
	 * *************************
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.login_view);

		mLoginButton = (ImageButton) findViewById(R.id.btn_login);
		mLoginButton.setOnClickListener(this);

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
	 * ************************* Background stuffs *************************
	 */
	private class FetchTokenTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {

			// handle oAuth callback
			Uri uri = getIntent().getData();
			if (uri != null && uri.toString().startsWith(Const.CALLBACK_URL)) {
				String verifier = uri.getQueryParameter(Const.IEXTRA_OAUTH_VERIFIER);
				try {
					AccessToken accessToken = sTwitter.getOAuthAccessToken(sRequestToken, verifier);
					getTwitdxApplication().getAccount().saveToken(accessToken);
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
			sTwitter = new TwitterFactory(configuration).getInstance();

			try {
				sRequestToken = sTwitter.getOAuthRequestToken(Const.CALLBACK_URL);
			} catch (TwitterException e) {
				e.printStackTrace();
			}
			return null;
		}

		protected void onPostExecute(Void result) {
			Activity currentActivity = ((MainApplication) getApplicationContext())
					.getCurrentActivity();
			currentActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(sRequestToken
					.getAuthenticationURL())));
		}
	}
}
