package net.madroom.dnp.sample.provider;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Uri uri = Uri.parse("content://net.madroom.dnp.provider.droidnpprovider/playing_info");
        Cursor c = managedQuery(uri, null, null, null, null);
        if(c.moveToFirst()) {
            String artistName = c.getString(c.getColumnIndex("artist"));
            String albumName = c.getString(c.getColumnIndex("album"));
            String trackName = c.getString(c.getColumnIndex("track"));
            Log.v("DEBUG", "artist:"+artistName);
            Log.v("DEBUG", "album :"+albumName);
            Log.v("DEBUG", "track :"+trackName);
        }
    }
}
