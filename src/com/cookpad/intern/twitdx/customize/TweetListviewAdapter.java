package com.cookpad.intern.twitdx.customize;

import com.cookpadintern.twitdx.R;
import com.cookpadintern.twitdx.common.*;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TweetListviewAdapter extends BaseAdapter {
    
    private Activity activity;
    private ArrayList<HashMap<String, String>> tweets;
    private static LayoutInflater inflater=null;
    public ImageLoader imageLoader; 
    
    public TweetListviewAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
        activity = a;
        tweets = d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader=new ImageLoader(activity.getApplicationContext());
    }

    @Override
    public int getCount() {
        return tweets.size();
    }
    
    @Override
    public Object getItem(int position) {
        return position;
    }
    
    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if(convertView == null) {
            view  = inflater.inflate(R.layout.single_tweet, null);
        }
        
        TextView uname = (TextView)view .findViewById(R.id.uname);
        TextView tweet = (TextView)view .findViewById(R.id.tweet);
        TextView date = (TextView)view .findViewById(R.id.date);
        ImageView thumb_image = (ImageView)view .findViewById(R.id.avatar);
        
        
        
        HashMap<String, String> single_tweet = new HashMap<String, String>();
        single_tweet = tweets.get(position);
        uname.setText(single_tweet.get(Const.KEY_UNAME));
        tweet.setText(single_tweet.get(Const.KEY_TWEET));
        date.setText(single_tweet.get(Const.KEY_DATE));
        
        imageLoader.DisplayImage(single_tweet.get(Const.KEY_AVATAR), thumb_image);
        return view ;
    }
}