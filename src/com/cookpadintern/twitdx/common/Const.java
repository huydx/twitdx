/*
author:huydx
github:https://github.com/huydx
*/
package com.cookpadintern.twitdx.common;

public class Const {
    public static String CONSUMER_KEY = "gTYB5AULlvAJbqv609NdLA";
    public static String CONSUMER_SECRET = "q2RQH3ZQ2ldcECLzxdBN98t5nF42ONhDf7gtr3fM";

    public static String PREFERENCE_NAME = "twitter_oauth";
    public static final String PREF_KEY_SECRET = "oauth_token_secret";
    public static final String PREF_KEY_TOKEN = "oauth_token";

    public final static String CALLBACK_URL = "twitter://twitdx";

    public static final String IEXTRA_AUTH_URL = "auth_url";
    public static final String IEXTRA_OAUTH_VERIFIER = "oauth_verifier";
    public static final String IEXTRA_OAUTH_TOKEN = "oauth_token";
    
    public static final String LOGGED_IN = "logged_in";
    
    public static final int LOGIN_REQUEST = 1234;
    
    public static final String KEY_AVATAR = "avatar"; // parent node
    public static final String KEY_UNAME = "uname";
    public static final String KEY_TWEET = "tweet";
    public static final String KEY_DATE = "date";    
    
    public static final String UPDATE_STATUS_ERROR = "some error occurred when update status";
    public static final int MENTION_PER_PAGE = 30;
}
